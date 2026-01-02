package ljc.model;

import ljc.entity.UnitConfig;
import ljc.entity.UserGeneral;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
//它是一个逻辑模型，专门负责管理小兵的分配与战损。比如 100 个人受了伤，它负责计算步兵和弓兵分别死多少个。
public class Army {
    private UserGeneral leader;
    // 初始化 troopMap 防止空指针
    private Map<UnitConfig, Integer> troopMap = new HashMap<>();

    /**
     * 获取当前部队总兵力
     */
    public int getTotalUnitCount() {
        return troopMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 按比例分摊战损
     */
    public void receiveDamage(int damage) {
        int total = getTotalUnitCount();
        if (total <= 0) return;

        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            UnitConfig unit = entry.getKey();
            int count = entry.getValue();

            double ratio = (double) count / total;
            int loss = (int) Math.ceil(damage * ratio);

            troopMap.put(unit, Math.max(0, count - loss));
        }
    }

    /**
     * 1. 战后恢复逻辑 (已移出嵌套，消灭红字)
     * @param rate 恢复比例，例如 0.7 代表恢复 70% 的伤兵
     */
    public void recoverTroops(double rate) {
        if (troopMap == null) return;

        for (Map.Entry<UnitConfig, Integer> entry : troopMap.entrySet()) {
            int currentCount = entry.getValue();
            int recoveredCount = (int) (currentCount * rate);
            entry.setValue(recoveredCount);
        }
    }

    /**
     * 2. 兵力清零逻辑
     */
    public void clearTroops() {
        if (troopMap != null) {
            troopMap.clear();
        }
    }

    /**
     * 计算带兵种强化的总战力
     */
    public int calculateTotalPower() {
        int finalPower = 0;
        int infantryQuota = 0;

        // 1. 统计特种兵提供的强化额度
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