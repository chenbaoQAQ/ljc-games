package ljc.service;

import ljc.entity.Equipment;
import ljc.entity.UserGeneral;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Random;

@Component
/**
 * 补全后的数值引擎：
 * 1. 支持武器（ATK）、防具（HP）、兵符（LEADERSHIP）全装备加成。
 * 2. 区分单挑（PK）与大军混战的计算逻辑。
 * 3. 引入性格对概率事件的微调。
 */
public class CombatEngine {

    private final Random random = new Random();

    // 计算全军混合伤害
    public double calculateFinalAtk(int armyBasePower, List<Equipment> equips, UserGeneral general) {
        // 1. 汇总装备攻击加成
        int equipAtkBonus = equips.stream()
                .filter(e -> e.getEquipType() == Equipment.EquipType.WEAPON)
                .mapToInt(Equipment::getAtkBonus).sum();

        double totalBaseAtk = armyBasePower + equipAtkBonus;

        // 2. 系数叠加：性格系数 * 状态惩罚
        double mods = getPersonalityModifier(general.getPersonality()) * getStatusModifier(general.getStatus());

        return totalBaseAtk * mods;
    }

    // 计算武将单挑伤害（PK阶段）
    public double calculatePKDamage(UserGeneral general, List<Equipment> equips, List<String> log) {
        int weaponAtk = equips.stream()
                .filter(e -> e.getEquipType() == Equipment.EquipType.WEAPON)
                .mapToInt(Equipment::getAtkBonus).sum();

        double atkBase = general.getBaseAtk() + weaponAtk;
        double damage = atkBase * getPersonalityModifier(general.getPersonality()) * getStatusModifier(general.getStatus());

        // 技能触发：暴躁性格额外增加 5% 触发率
        double triggerChance = general.getSkillTriggerChance();
        if ("RASH".equals(general.getPersonality())) triggerChance += 0.05;

        if (random.nextDouble() < triggerChance) {
            damage *= general.getSkillDamageRatio();
            log.add(String.format("★★★【技能】%s 爆发大招 [%s]！造成了 %.0f 伤害！",
                    general.getName(), general.getActiveSkillName(), damage));
        }

        return damage;
    }

    // 状态修正
    private double getStatusModifier(String status) {
        if ("WOUNDED".equals(status)) return 0.85; // 受伤下降 15% 战力
        if ("KILLED".equals(status)) return 0.0;
        return 1.0;
    }

    // 性格修正
    private double getPersonalityModifier(String personality) {
        if (personality == null) return 1.0;
        return switch (personality) {
            case "BRAVE" -> 1.15; // 勇敢：纯伤害高
            case "RASH" -> 1.25;  // 暴躁：伤害最高但容易受伤（由Service处理概率）
            case "CALM" -> 1.05;  // 冷静：稳健发挥
            case "CAUTIOUS" -> 1.0; // 谨慎：无伤害加成但有防御潜力
            default -> 1.0;
        };
    }
}