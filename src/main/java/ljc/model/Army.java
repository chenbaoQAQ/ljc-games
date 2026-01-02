package ljc.model;

import ljc.entity.UnitConfig;
import ljc.service.CombatEngine;
import lombok.Data;
import java.util.*;

@Data
/**
 * 升级后的部队模型：
 * 1. 支持分波次火力输出查询。
 * 2. 严格执行特种兵对基础兵种的 2 倍强化逻辑。
 */
public class Army {
    // 存储当前部队中各兵种的配置与对应的人数
    private Map<UnitConfig, Integer> troopMap = new HashMap<>();

    /**
     * 获取当前部队总人数（所有兵种累加）
     */
    public int getTotalUnitCount() {
        return troopMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 💡 核心新增：计算特种兵提供的“强化额度”
     * 规则：1个特种兵强化2个基础单位。
     * 返回 Map，Key 是受强化的兵种名（如 INFANTRY），Value 是可强化的最大人数。
     */
    public Map<String, Integer> calculateSpecialBuffs() {
        Map<String, Integer> buffs = new HashMap<>();
        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            String name = entry.getKey().getUnitName();
            int count = entry.getValue();

            // 根据企划：中、日、韩特种兵分别强化 步、弓、骑
            if ("CN_SPECIAL".equals(name)) buffs.put("INFANTRY", buffs.getOrDefault("INFANTRY", 0) + count * 2);
            if ("JP_SPECIAL".equals(name)) buffs.put("ARCHER", buffs.getOrDefault("ARCHER", 0) + count * 2);
            if ("KR_SPECIAL".equals(name)) buffs.put("CAVALRY", buffs.getOrDefault("CAVALRY", 0) + count * 2);
            // 英国特种兵逻辑已在 BattleService 或 CombatEngine 中由武将个人加成处理
        }
        return buffs;
    }

    /**
     * 💡 核心新增：计算单一兵种波次的实时输出
     * @param targetUnitName 正在进攻的兵种（如 ARCHER）
     * @param enemyType 敌方主要兵种（用于判定克制）
     * @param buffQuota 当前波次可享受特种兵强化的名额
     * @param engine 战斗引擎（负责克制倍率计算）
     */
    public int getUnitAttackPower(String targetUnitName, String enemyType, int buffQuota, CombatEngine engine) {
        int power = 0;
        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            UnitConfig unit = entry.getKey();
            if (unit.getUnitName().equals(targetUnitName)) {
                int count = entry.getValue();

                // 计算受强化的兵力和普通兵力
                int buffedCount = Math.min(count, buffQuota);
                int normalCount = count - buffedCount;

                // 1. 强化部分伤害：ATK * 2(加拐) * 2(如果克制)
                power += (int)engine.calculateUnitDamage(unit.getBaseAtk(), targetUnitName, enemyType, true) * buffedCount;

                // 2. 普通部分伤害：ATK * 1 * 2(如果克制)
                power += (int)engine.calculateUnitDamage(unit.getBaseAtk(), targetUnitName, enemyType, false) * normalCount;
            }
        }
        return power;
    }

    /**
     * 💡 核心新增：计算特种兵自身的输出（收割波次）
     */
    public int getSpecialUnitPersonalAttack(CombatEngine engine) {
        int power = 0;
        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            String name = entry.getKey().getUnitName();
            if (name.endsWith("_SPECIAL")) {
                // 特种兵自身攻击不享受加拐，但享受性格或状态加成（此处暂取基础值）
                power += entry.getValue() * entry.getKey().getBaseAtk();
            }
        }
        return power;
    }

    /**
     * 按比例扣除战损（保持各兵种比例不变）
     */
    public void receiveDamage(int damage) {
        int total = getTotalUnitCount();
        if (total <= 0) return;

        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            int count = entry.getValue();
            double ratio = (double) count / total;
            int loss = (int) Math.ceil(damage * ratio);
            entry.setValue(Math.max(0, count - loss));
        }
    }

    /**
     * 战后伤兵回收
     */
    public void recoverTroops(double rate) {
        troopMap.entrySet().forEach(e -> e.setValue((int)(e.getValue() * rate)));
    }

    /**
     * 兵力清零
     */
    public void clearTroops() {
        troopMap.clear();
    }
}