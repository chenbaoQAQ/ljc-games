package ljc.service;

import ljc.entity.Equipment;
import ljc.entity.UserGeneral;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
/**
 * 战斗引擎：处理动态数值逻辑
 */
public class CombatEngine {

    /**
     * 计算武将 PK 阶段的最终伤害
     * @param general 武将实例
     * @param equips 装备列表
     * @param heroBuffCount 英雄流加持次数
     */
    public double calculatePKDamage(UserGeneral general, List<Equipment> equips, int heroBuffCount) {
        // 1. 汇总装备攻击力
        int weaponAtk = (equips == null) ? 0 : equips.stream()
                .filter(e -> e.getEquipType() == Equipment.EquipType.WEAPON)
                .mapToInt(Equipment::getAtkBonus).sum();

        // 2. 获取武将基础攻击力
        double atkBase = general.getBaseAtk() + weaponAtk;

        // 3. 计算性格加成与状态惩罚
        double personalityMod = getPersonalityModifier(general.getPersonality());
        double statusMod = getStatusModifier(general.getStatus());
        double finalDamage = atkBase * personalityMod * statusMod;

        // 4. 英国特种兵“英雄流”加成逻辑
        if (heroBuffCount > 0) {
            double extraDamage = heroBuffCount * 10.0 * personalityMod;
            finalDamage += extraDamage;
        }

        return finalDamage;
    }

    public double calculateUnitDamage(int baseAtk, String unitType, String enemyType, boolean isBuffed) {
        double damage = baseAtk;
        if (isBuffed) damage *= 2.0;
        if (isCounter(unitType, enemyType)) damage *= 2.0;
        return damage;
    }

    public int getAttackPriority(String attacker, String victim) {
        if (isCounter(attacker, victim)) return 100;
        return (attacker != null && attacker.equals(victim)) ? 50 : 30;
    }

    public boolean isCounter(String attacker, String victim) {
        if (attacker == null || victim == null) return false;
        if (attacker.equals("INFANTRY") && victim.equals("ARCHER")) return true;
        if (attacker.equals("ARCHER") && victim.equals("CAVALRY")) return true;
        if (attacker.equals("CAVALRY") && victim.equals("INFANTRY")) return true;
        return false;
    }

    private double getStatusModifier(String status) {
        if ("WOUNDED".equals(status)) return 0.8;
        return "KILLED".equals(status) ? 0.0 : 1.0;
    }

    private double getPersonalityModifier(String personality) {
        if (personality == null) return 1.0;
        return switch (personality) {
            case "BRAVE" -> 1.1;
            case "RASH" -> 1.2;
            case "CALM" -> 1.05;
            default -> 1.0;
        };
    }
}
