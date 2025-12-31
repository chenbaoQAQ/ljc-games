package ljc.model;

import ljc.entity.UnitConfig;
import ljc.entity.UserGeneral;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
@Data
public class Army {
    private UserGeneral leader;

    private Map<UnitConfig, Integer> troopMap;

    public int calculateTotalPower() {
        //计算总战力的方法
        // 1. 增益清单 (Buff Map): Key是受影响兵种(如INFANTRY), Value是倍率(如2.0)
        // 英语词汇：activeBuffs (激活中的增益)
        Map<String, Double> activeBuffs = new HashMap<>();

        // 2. 第一遍扫描：收集所有特种兵的增益
        for (UnitConfig unit : troopMap.keySet()) {
            if (!"NONE".equals(unit.getTargetType())) {
                // 如果这个兵种有强化目标，就把它的倍率存进清单
                //unit 代表当前正在检查的某一种兵
                activeBuffs.put(unit.getTargetType(), unit.getBuffRatio().doubleValue());
            }
        }
    return 1;
    }
}
