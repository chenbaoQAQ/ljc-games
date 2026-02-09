package ljc.battle.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 战斗配置常量
 * 后续可对接配置表
 */
public class BattleConstants {
    
    // Status Durations (Default)
    public static final int DURATION_POISON = 3;
    public static final int DURATION_HOT = 3;
    public static final int DURATION_STUN = 1;
    public static final int DURATION_IMMUNE = 1;
    
    // Values
    public static final int VAL_POISON_DMG = 50; // Per turn
    public static final int VAL_HOT_HEAL = 50;   // Per turn
    public static final int VAL_HEAL_BIG = 200;  // Direct heal
    public static final int VAL_HEAL_TROOP = 100; // Troop heal
    public static final double MULT_GENDER_ADVANTAGE = 1.5;
    public static final double MULT_VULNERABLE = 1.3;
    public static final double PCT_REFLECT = 0.3; // 30% reflect

    // Skills
    public static final String SKILL_DEF_HEAL_SELF_BIG_HOT = "DEF_HEAL_SELF_BIG_HOT";
    public static final String SKILL_DEF_HEAL_TROOPS_3 = "DEF_HEAL_TROOPS_3";
    public static final String SKILL_DEF_IMMUNE_TEAM_1TURN = "DEF_IMMUNE_TEAM_1TURN";
    public static final String SKILL_ATK_SPLASH_HERO_TO_TROOPS = "ATK_SPLASH_HERO_TO_TROOPS";
    public static final String SKILL_ATK_POISON_HERO = "ATK_POISON_HERO";
    public static final String SKILL_ATK_STUN_HERO_CHANCE = "ATK_STUN_HERO_CHANCE";
    public static final String SKILL_ATK_AMBUSH_ARCHER_VOLLEY = "ATK_AMBUSH_ARCHER_VOLLEY"; // Triggered via Passive-like hook or check

    // Passives
    public static final String PASSIVE_DamageUpVsOppositeGender = "PASSIVE_DAMAGE_UP_VS_OPPOSITE_GENDER";
    public static final String PASSIVE_Tanky = "PASSIVE_TANKY";
    public static final String PASSIVE_ReflectPercent = "PASSIVE_REFLECT_PERCENT";
    public static final String PASSIVE_FirstTurnPriority = "PASSIVE_FIRST_TURN_PRIORITY";
    public static final String PASSIVE_HarderToTarget = "PASSIVE_HARDER_TO_TARGET_IN_TROOP_PHASE";

}
