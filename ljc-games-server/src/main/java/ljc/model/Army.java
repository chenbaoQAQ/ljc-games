package ljc.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ljc.repository.UnitConfigRepository;
import ljc.service.CombatEngine;
import lombok.Data;
import java.util.*;

@Data
public class Army {
    private Map<UnitConfig, Integer> troopMap = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    public void fromJson(String json, UnitConfigRepository unitRepo) {
        if (json == null || json.isEmpty() || "{}".equals(json)) return;
        try {
            Map<String, Integer> data = mapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
            troopMap.clear();
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                unitRepo.findByUnitName(entry.getKey()).ifPresent(unit -> {
                    troopMap.put(unit, entry.getValue());
                });
            }
        } catch (Exception e) {
            System.err.println("部队配置解析失败: " + e.getMessage());
        }
    }

    public String toJson() {
        try {
            Map<String, Integer> data = new HashMap<>();
            for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
                data.put(entry.getKey().getUnitName(), entry.getValue());
            }
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }

    public int getTotalUnitCount() {
        return troopMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int calculateHeroBuffCount() {
        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            if ("EN_SPECIAL".equals(entry.getKey().getUnitName())) {
                return entry.getValue() / 5;
            }
        }
        return 0;
    }

    public Map<String, Integer> calculateSpecialBuffs() {
        Map<String, Integer> buffs = new HashMap<>();
        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            String name = entry.getKey().getUnitName();
            int count = entry.getValue();
            if ("CN_SPECIAL".equals(name)) buffs.put("INFANTRY", buffs.getOrDefault("INFANTRY", 0) + count * 2);
            if ("JP_SPECIAL".equals(name)) buffs.put("ARCHER", buffs.getOrDefault("ARCHER", 0) + count * 2);
            if ("KR_SPECIAL".equals(name)) buffs.put("CAVALRY", buffs.getOrDefault("CAVALRY", 0) + count * 2);
        }
        return buffs;
    }

    public int getUnitAttackPower(String targetUnitName, String enemyType, int buffQuota, CombatEngine engine) {
        int power = 0;
        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            UnitConfig unit = entry.getKey();
            if (unit.getUnitName().equals(targetUnitName)) {
                int count = entry.getValue();
                int buffed = Math.min(count, buffQuota);
                power += (int)engine.calculateUnitDamage(unit.getBaseAtk(), targetUnitName, enemyType, true) * buffed;
                power += (int)engine.calculateUnitDamage(unit.getBaseAtk(), targetUnitName, enemyType, false) * (count - buffed);
            }
        }
        return power;
    }

    public int getSpecialUnitPersonalAttack(CombatEngine engine) {
        return troopMap.entrySet().stream()
                .filter(e -> e.getKey().getUnitName().endsWith("_SPECIAL"))
                .mapToInt(e -> e.getValue() * e.getKey().getBaseAtk()).sum();
    }

    public void receiveDamage(int damage) {
        int total = getTotalUnitCount();
        if (total <= 0) return;
        troopMap.entrySet().forEach(e -> {
            int loss = (int) Math.ceil(damage * ((double) e.getValue() / total));
            e.setValue(Math.max(0, e.getValue() - loss));
        });
    }

    public void recoverTroops(double rate) {
        troopMap.entrySet().forEach(e -> e.setValue((int)(e.getValue() * rate)));
    }

    public void clearTroops() { troopMap.clear(); }

    public int calculateTotalSpace() {
        return troopMap.entrySet().stream()
                .mapToInt(e -> e.getKey().getSpaceCost() * e.getValue())
                .sum();
    }
}
