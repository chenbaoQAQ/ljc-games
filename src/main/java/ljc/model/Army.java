package ljc.model;

import ljc.entity.UnitConfig;
import ljc.entity.UserGeneral;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Army {
    private UserGeneral leader;
    // 初始化 troopMap 防止空指针
    private Map<UnitConfig, Integer> troopMap = new HashMap<>();

    /**
     * 获取当前部队总兵力 (Total Unit Count)
     */
    public int getTotalUnitCount() {
        return troopMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 核心交互：按比例分摊战损 (Proportional Damage)
     * 解决“人被打死，伤害下降”的问题
     */
    public void receiveDamage(int damage) {
        int total = getTotalUnitCount();
        if (total <= 0) return;

        // 遍历每一种兵，按其占总人数的比例分摊伤害
        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            UnitConfig unit = entry.getKey();
            int count = entry.getValue();

            // 计算该兵种在当前总兵力中的占比
            double ratio = (double) count / total;
            // 向上取整，确保哪怕只有 1 点伤害，也会产生实际减员
            int loss = (int) Math.ceil(damage * ratio);

            troopMap.put(unit, Math.max(0, count - loss));
        }
    }

    /**
     * 计算带兵种强化的总战力 (Precision Buff Logic)
     */
    public int calculateTotalPower() {
        int finalPower = 0;
        int infantryQuota = 0;

        // 1. 统计特种兵提供的强化额度 (Quota)
        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            if ("CN_SPECIAL".equals(entry.getKey().getUnitName())) {
                infantryQuota += entry.getValue() * 2;
            }
        }

        // 2. 遍历兵种计算实时战力
        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            UnitConfig unit = entry.getKey();
            int count = entry.getValue();

            if ("INFANTRY".equals(unit.getUnitName())) {
                int buffedCount = Math.min(count, infantryQuota);
                int normalCount = count - buffedCount;
                finalPower += (buffedCount * unit.getBaseAtk() * 2) + (normalCount * unit.getBaseAtk());
            } else {
                finalPower += count * unit.getBaseAtk();
            }
        }
        return finalPower;
    }
}