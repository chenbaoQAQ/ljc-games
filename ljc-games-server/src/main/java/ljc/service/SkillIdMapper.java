package ljc.service;

import ljc.battle.core.BattleConstants;

public class SkillIdMapper {
    /**
     * Map DB integer skill ID to Engine String ID
     */
    public static String getEngineSkillId(Integer dbId) {
        if (dbId == null) return null;
        switch (dbId) {
            case 1: return BattleConstants.SKILL_DEF_HEAL_SELF_BIG_HOT; // 鼓舞 -> Big Heal
            case 2: return BattleConstants.SKILL_ATK_SPLASH_HERO_TO_TROOPS; // 乱舞 -> Splash
            case 3: return BattleConstants.SKILL_ATK_POISON_HERO; // 毒箭 -> Poison
            case 4: return BattleConstants.SKILL_ATK_STUN_HERO_CHANCE; // 威压 -> Stun
            case 5: return BattleConstants.SKILL_DEF_IMMUNE_TEAM_1TURN; // 铁壁 -> Immune
            case 6: return BattleConstants.SKILL_ATK_AMBUSH_ARCHER_VOLLEY; // 伏兵 -> Volley
            default: return null;
        }
    }
    
    /**
     * Map Passive Codes (String in DB) to Engine Passive Names
     */
    public static String getEnginePassiveId(String personalityCode) {
        if (personalityCode == null) return null;
        switch (personalityCode) {
            // Personality -> Passive Mapping (Example)
            case "BERSERKER": return BattleConstants.PASSIVE_DamageUpVsOppositeGender; // Just reuse existing V1 passive for demo
            case "STOIC": return BattleConstants.PASSIVE_Tanky;
            default: return null;
        }
    }
}
