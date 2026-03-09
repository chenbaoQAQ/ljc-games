package ljc.battle.core;

/**
 * 技能与性格在数据库字段与战斗引擎字段之间的映射器。
 */
public class SkillIdAdapter {

    /**
     * 将数据库中的技能ID映射为战斗引擎技能常量。
     */
    public static String getEngineSkillId(Integer dbId) {
        if (dbId == null) return null;
        switch (dbId) {
            case 1: return BattleConstants.SKILL_DEF_HEAL_SELF_BIG_HOT; // 鼓舞
            case 2: return BattleConstants.SKILL_ATK_SPLASH_HERO_TO_TROOPS; // 一石二鸟
            case 3: return BattleConstants.SKILL_ATK_POISON_HERO; // 毒箭
            case 4: return BattleConstants.SKILL_ATK_STUN_HERO_CHANCE; // 威压
            case 5: return BattleConstants.SKILL_DEF_IMMUNE_TEAM_1TURN; // 铁壁
            case 6: return BattleConstants.SKILL_ATK_AMBUSH_ARCHER_VOLLEY; // 偷袭
            default: return null;
        }
    }

    /**
     * 将性格编码映射为战斗引擎被动常量。
     */
    public static String getEnginePassiveId(String personalityCode) {
        if (personalityCode == null) return null;
        switch (personalityCode) {
            case "BERSERKER": return BattleConstants.PASSIVE_DamageUpVsOppositeGender;
            case "STOIC": return BattleConstants.PASSIVE_Tanky;
            default: return null;
        }
    }
}
