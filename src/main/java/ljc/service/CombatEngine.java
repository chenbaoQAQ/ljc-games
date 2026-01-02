package ljc.service;

import ljc.entity.Equipment;
import ljc.entity.UserGeneral;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CombatEngine {


    public double calculateFinalAtk(int armyBasePower, List<Equipment> equips, UserGeneral general) {
        // 1. 累加装备攻击加成
        int equipAtkBonus = 0;
        if (equips != null) {
            equipAtkBonus = equips.stream().mapToInt(Equipment::getAtkBonus).sum();
            //mapToInt(Equipment::getAtkBonus).sum()从Equipment里面取AtkBonus累加
        }

        double totalBaseAtk = armyBasePower + equipAtkBonus;

        // 2. 获取性格修正系数
        double personalityMod = getPersonalityModifier(general.getPersonality());

        // 3. 获取状态惩罚系数
        double statusMod = 1.0;
        if ("WOUNDED".equals(general.getStatus())) {
            statusMod = 0.9; // 受伤战力发挥 80%
        } else if ("KILLED".equals(general.getStatus())) {
            statusMod = 0.0; // 阵亡全军溃散
        }

        return totalBaseAtk * personalityMod * statusMod;
    }

    public double calculateGeneralOnlyAtk(UserGeneral general, List<Equipment> equips) {
        // 1. 获取装备带来的攻击加成
        int equipAtkBonus = 0;
        if (equips != null) {
            equipAtkBonus = equips.stream().mapToInt(Equipment::getAtkBonus).sum();
        }

        // 2. 基础攻击力（武将本身攻击力 + 装备攻击力）
        // 注意：这里我们假设 UserGeneral 还没有 baseAtk 属性，如果以后加了可以再加上
        double totalAtk = 50.0 + equipAtkBonus; // 50 是给武将设定的一个基础初始战力

        // 3. 性格修正系数
        double personalityMod = getPersonalityModifier(general.getPersonality());

        // 4. 状态惩罚系数（比如已经负伤了，PK伤害也会降低）
        double statusMod = 1.0;
        if ("WOUNDED".equals(general.getStatus())) {
            statusMod = 0.9;
        } else if ("KILLED".equals(general.getStatus())) {
            statusMod = 0.0;
        }

        return totalAtk * personalityMod * statusMod;
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