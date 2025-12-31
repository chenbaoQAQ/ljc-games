package ljc.service;

import ljc.entity.Equipment;
import ljc.entity.UserGeneral;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CombatEngine {

    /**
     * 计算最终战力：融合 兵力战力 + 装备加成 + 性格修正 + 伤病惩罚
     */
    public double calculateFinalAtk(int armyBasePower, List<Equipment> equips, UserGeneral general) {
        // 1. 累加装备攻击加成
        int equipAtkBonus = 0;
        if (equips != null) {
            equipAtkBonus = equips.stream().mapToInt(Equipment::getAtkBonus).sum();
        }

        double totalBaseAtk = armyBasePower + equipAtkBonus;

        // 2. 获取性格修正系数
        double personalityMod = getPersonalityModifier(general.getPersonality());

        // 3. 获取状态惩罚系数
        double statusMod = 1.0;
        if ("WOUNDED".equals(general.getStatus())) {
            statusMod = 0.8; // 受伤战力发挥 80%
        } else if ("KILLED".equals(general.getStatus())) {
            statusMod = 0.0; // 阵亡全军溃散
        }

        return totalBaseAtk * personalityMod * statusMod;
    }

    private double getPersonalityModifier(String personality) {
        if (personality == null) return 1.0;
        switch (personality) {
            case "BRAVE": return 1.1;  // 勇敢：+10% 攻击
            case "RASH":  return 1.2;  // 暴躁：+20% 攻击
            case "CALM":  return 1.05; // 冷静：+5% 攻击
            case "CAUTIOUS": return 1.0; // 谨慎：基础攻击，但生存高（后续可加防御逻辑）
            default: return 1.0;
        }
    }
}