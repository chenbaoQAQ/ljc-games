package ljc.model;

import ljc.entity.UnitConfig;
import ljc.service.CombatEngine;
import lombok.Data;
import java.util.*;

@Data
/**
 * 部队模型，处理静态的条件
 */
public class Army {
    private Map<UnitConfig, Integer> troopMap = new HashMap<>();

    public int getTotalUnitCount() {
        return troopMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    // 计算特种兵提供的强化额度 (1个特种兵强化2个基础兵)
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
    //特种兵提供英雄buff
    public int calculateHeroBuffCount() {
        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            if ("EN_SPECIAL".equals(entry.getKey().getUnitName())) {
                return entry.getValue() / 5; // 每5个加持1次
            }
        }
        return 0;
    }

    // 获取特定兵种波次的实时伤害
    public int getUnitAttackPower(String targetUnitName, String enemyType, int buffQuota, CombatEngine engine) {
        int power = 0;
        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            UnitConfig unit = entry.getKey();
            if (unit.getUnitName().equals(targetUnitName)) {
                int count = entry.getValue();
                int buffed = Math.min(count, buffQuota);
                int normal = count - buffed;
                power += (int)engine.calculateUnitDamage(unit.getBaseAtk(), targetUnitName, enemyType, true) * buffed;
                power += (int)engine.calculateUnitDamage(unit.getBaseAtk(), targetUnitName, enemyType, false) * normal;
            }
        }
        return power;
    }

    // 特种兵自身伤害
    public int getSpecialUnitPersonalAttack(CombatEngine engine) {
        int power = 0;
        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            if (entry.getKey().getUnitName().endsWith("_SPECIAL")) {
                power += entry.getValue() * entry.getKey().getBaseAtk();
            }
        }
        return power;
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
}