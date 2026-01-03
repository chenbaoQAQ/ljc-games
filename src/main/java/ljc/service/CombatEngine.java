package ljc.service;

import ljc.entity.Equipment;
import ljc.entity.UserGeneral;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
/**
 * 战斗引擎：确保与 BattleService 的调用逻辑完全对齐。
 * 用来处理动态数值
 */
public class CombatEngine {

    /**
     * 计算武将 PK 阶段的最终伤害
     * @param general 武将实例
     * @param equips 装备列表
     * @param heroBuffCount 英雄流加持次数（由 Army.calculateHeroBuffCount() 计算得出）
     */
    public double calculatePKDamage(UserGeneral general, List<Equipment> equips, int heroBuffCount) {
        // 1. 汇总装备攻击力 (过滤非武器加成)
        int weaponAtk = (equips == null) ? 0 : equips.stream()
                .filter(e -> e.getEquipType() == Equipment.EquipType.WEAPON)
                .mapToInt(Equipment::getAtkBonus).sum();

        // 2. 获取武将基础攻击力 (增加 baseAtk 字段支持)
        double atkBase = general.getBaseAtk() + weaponAtk;

        // 3. 计算性格加成与状态惩罚
        double personalityMod = getPersonalityModifier(general.getPersonality());
        double statusMod = getStatusModifier(general.getStatus());
        double finalDamage = atkBase * personalityMod * statusMod;

        // 4. 【补全】英国特种兵“英雄流”加成逻辑
        // 规则：每 5 个亲卫加持 1 次，每次提供额外的固定伤害，并受性格系数微调
        if (heroBuffCount > 0) {
            double extraDamage = heroBuffCount * 10.0 * personalityMod;
            finalDamage += extraDamage;
        }

        return finalDamage;
    }

    /**
     * 计算特定波次的兵种输出伤害
     * @param baseAtk 兵种单体攻击
     * @param unitType 进攻方兵种
     * @param enemyType 防御方（关卡）主力兵种
     * @param isBuffed 是否受到特种兵强化
     */
    public double calculateUnitDamage(int baseAtk, String unitType, String enemyType, boolean isBuffed) {
        double damage = baseAtk;

        // 特种兵加拐逻辑 (伤害 x2)
        if (isBuffed) {
            damage *= 2.0;
        }

        // 兵种克制逻辑 (伤害 x2)
        if (isCounter(unitType, enemyType)) {
            damage *= 2.0;
        }

        return damage;
    }

    /**
     * 获取攻击优先级（用于战术集火）
     * 规则：克制目标 100分，同类目标 50分
     */
    public int getAttackPriority(String attacker, String victim) {
        if (isCounter(attacker, victim)) return 100;
        return (attacker != null && attacker.equals(victim)) ? 50 : 30;
    }

    /**
     * 判定克制闭环：步兵 -> 弓兵 -> 骑兵 -> 步兵
     */
    public boolean isCounter(String attacker, String victim) {
        if (attacker == null || victim == null) return false;
        if (attacker.equals("INFANTRY") && victim.equals("ARCHER")) return true;
        if (attacker.equals("ARCHER") && victim.equals("CAVALRY")) return true;
        if (attacker.equals("CAVALRY") && victim.equals("INFANTRY")) return true;
        return false;
    }

    /**
     * 状态惩罚系数：受伤打8折
     */
    private double getStatusModifier(String status) {
        if ("WOUNDED".equals(status)) return 0.8;
        return "KILLED".equals(status) ? 0.0 : 1.0;
    }

    /**
     * 性格修正系数
     */
    private double getPersonalityModifier(String personality) {
        if (personality == null) return 1.0;
        return switch (personality) {
            case "BRAVE" -> 1.1; // 勇敢：稳定
            case "RASH" -> 1.2;  // 暴躁：爆发
            case "CALM" -> 1.05; // 冷静：精准
            default -> 1.0;
        };
    }
}