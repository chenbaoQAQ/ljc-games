package ljc.service;

import ljc.entity.Equipment;
import ljc.entity.UserGeneral;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Random;

@Component
public class CombatEngine {

    private final Random random = new Random();

    /**
     * 计算特定兵种对特定敌人的最终伤害
     * @param baseAtk 兵种基础攻击
     * @param unitType 攻击方类型
     * @param enemyType 防御方类型
     * @param isBuffed 是否受到特种兵加持
     */
    public double calculateUnitDamage(int baseAtk, String unitType, String enemyType, boolean isBuffed) {
        double damage = baseAtk;

        // 1. 特种兵加拐逻辑 (攻击力 x2)
        if (isBuffed) damage *= 2.0;

        // 2. 兵种克制逻辑 (Double Damage)
        // 规则：INFANTRY -> ARCHER -> CAVALRY -> INFANTRY
        if (isCounter(unitType, enemyType)) {
            damage *= 2.0;
        }

        return damage;
    }

    /**
     * 💡 核心新增：目标优先级判定
     * 返回值越高，代表攻击欲望越强。例如：弓兵对骑兵会返回最高优先级。
     */
    public int getAttackPriority(String attacker, String victim) {
        if (attacker == null || victim == null) return 0;

        // 优先攻击被自己克制的兵种（收益最大化）
        if (isCounter(attacker, victim)) {
            return 100; // 最高优先级
        }

        // 其次攻击同等级或中立兵种
        if (attacker.equals(victim)) {
            return 50;
        }

        // 最后才去啃那些克制自己的“硬骨头”
        if (isCounter(victim, attacker)) {
            return 10;
        }

        return 30;
    }

    /**
     * 判定克制关系
     */
    public boolean isCounter(String attacker, String victim) {
        if (attacker == null || victim == null) return false;
        // 步兵克弓兵
        if (attacker.equals("INFANTRY") && victim.equals("ARCHER")) return true;
        // 弓兵克骑兵
        if (attacker.equals("ARCHER") && victim.equals("CAVALRY")) return true;
        // 骑兵克步兵
        if (attacker.equals("CAVALRY") && victim.equals("INFANTRY")) return true;
        return false;
    }

    // 武将 PK 逻辑保留并增强
    public double calculatePKDamage(UserGeneral general, List<Equipment> equips, List<String> log) {
        int weaponAtk = equips.stream()
                .filter(e -> e.getEquipType() == Equipment.EquipType.WEAPON)
                .mapToInt(Equipment::getAtkBonus).sum();

        double atkBase = general.getBaseAtk() + weaponAtk;
        double damage = atkBase * getPersonalityModifier(general.getPersonality()) * getStatusModifier(general.getStatus());

        if (random.nextDouble() < general.getSkillTriggerChance()) {
            damage *= general.getSkillDamageRatio();
            log.add(String.format("★★★【技能】%s 爆发技能 [%s]！", general.getName(), general.getActiveSkillName()));
        }
        return damage;
    }

    public double getStatusModifier(String status) {
        if ("WOUNDED".equals(status)) return 0.85;
        if ("KILLED".equals(status)) return 0.0;
        return 1.0;
    }

    public double getPersonalityModifier(String personality) {
        if (personality == null) return 1.0;
        return switch (personality) {
            case "BRAVE" -> 1.15;
            case "RASH" -> 1.25;
            case "CALM" -> 1.05;
            default -> 1.0;
        };
    }
}