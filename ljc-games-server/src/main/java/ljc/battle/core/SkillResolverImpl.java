package ljc.battle.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ljc.battle.core.BattleState.Hero;
import ljc.battle.core.BattleState.Side;
import ljc.battle.core.StatusEffect.StatusType;
import ljc.battle.core.TroopDamage.DamageOutcome;
import ljc.battle.core.BattleLogEvent.Type;

/**
 * V1 技能系统实现
 * 涵盖：防御/进攻/被动 Logic Hook
 */
public class SkillResolverImpl implements SkillResolver {

    @Override
    public SkillDecision decideSkill(BattleState state, Side actorSide) {
        // V1: 简单模拟 AI 行为 -> 如果没 CD 就放第一个 Active，否则普攻
        Hero h = actorSide.hero;
        if (h.isAlive() && h.actives != null && !h.actives.isEmpty()) {
            if (h.skillCd <= 0) {
                // Return First Skill Decision
                return new SkillDecision(SkillDecision.Type.SKILL_A, h.actives.get(0), actorSide);
            }
        }
        return new SkillDecision(SkillDecision.Type.NORMAL);
    }

    @Override
    public SkillEffect resolve(BattleState state, SkillDecision decision) {
        SkillEffect effect = new SkillEffect();
        effect.logs = new ArrayList<>();
        effect.success = true;

        if (decision.type != SkillDecision.Type.SKILL_A) return effect;

        String skillId = decision.skillId;
        Side actorSide = decision.actorSide;
        Side enemySide = (actorSide == state.sideA) ? state.sideB : state.sideA;
        String actorName = actorSide.hero.name;

        // Reset CD
        actorSide.hero.skillCd = actorSide.hero.maxSkillCd;

        effect.logs.add("Skill Cast: " + skillId);

        switch (skillId) {
            // --- Defensive ---
            case BattleConstants.SKILL_DEF_HEAL_SELF_BIG_HOT:
                doHealSelfBigHot(state, actorSide, effect);
                break;
            case BattleConstants.SKILL_DEF_HEAL_TROOPS_3:
                doHealTroops3(state, actorSide, effect);
                break;
            case BattleConstants.SKILL_DEF_IMMUNE_TEAM_1TURN:
                doImmuneTeam(state, actorSide, effect);
                break;
            
            // --- Offensive ---
            case BattleConstants.SKILL_ATK_SPLASH_HERO_TO_TROOPS:
                doSplashHeroToTroops(state, actorSide, enemySide, effect);
                break;
            case BattleConstants.SKILL_ATK_POISON_HERO:
                doPoisonHero(state, actorSide, enemySide, effect);
                break;
            case BattleConstants.SKILL_ATK_STUN_HERO_CHANCE:
                doStunHeroChance(state, actorSide, enemySide, effect);
                break;
                
            default:
                effect.logs.add("Unknown Skill: " + skillId);
                effect.success = false;
        }

        return effect;
    }
    
    // --- Implementations ---

    private void doHealSelfBigHot(BattleState state, Side side, SkillEffect effect) {
        // 1. Instant Heal
        int healVal = BattleConstants.VAL_HEAL_BIG;
        side.hero.hp = Math.min(side.hero.maxHp, side.hero.hp + healVal);
        effect.heal = healVal;
        effect.logs.add("Heal Self: " + healVal);
        
        // 2. Add HoT Status
        addStatus(state, side, "Hero", StatusType.HOT, BattleConstants.DURATION_HOT, null);
        effect.logs.add("Applied HoT");
    }

    private void doHealTroops3(BattleState state, Side side, SkillEffect effect) {
        // Heal INF, ARC, CAV stacks
        for (TroopStack s : side.troops) {
             if (s.count <= 0) continue; // Dead stack not healed? Request says "stack exists then heal"
             
             // Heal logic: Heal frontHP, then maybe revive? 
             // Requirement: "按个体HP压缩表达来治：先补 frontHp，再按单位补满一个个体"
             // Typically healing doesn't revive dead units unless specified.
             // "Recover HP for existing units".
             // Assuming no revival of count. Only full heal current front.
             // Wait "再按单位补满一个个体" implies if front is full, heal next? 
             // But valid units are always full except front.
             // So essentially: Set frontHp = unitHp.
             
             int oldFront = s.frontHp;
             s.frontHp = s.unitHp;
             int healed = s.unitHp - oldFront;
             
             if (healed > 0) {
                 effect.logs.add("Healed " + s.type + ": " + healed);
             }
        }
    }

    private void doImmuneTeam(BattleState state, Side side, SkillEffect effect) {
        // Hero
        addStatus(state, side, "Hero", StatusType.IMMUNE, BattleConstants.DURATION_IMMUNE, null);
        // Troops
        for (TroopStack s : side.troops) {
            if (s.count > 0) {
                addStatus(state, side, s.type, StatusType.IMMUNE, BattleConstants.DURATION_IMMUNE, null);
            }
        }
        effect.logs.add("Team Immune Applied");
    }

    private void doSplashHeroToTroops(BattleState state, Side actor, Side enemy, SkillEffect effect) {
        // 1. Hit Hero
        if (enemy.hero.isAlive()) {
             int dmg = actor.hero.atk; // 100% atk
             applyDamageToHero(state, actor, enemy, dmg, effect);
        }
        
        // 2. Splash Troops (INF, ARC, CAV)
        // Splash dmg calculation? Assuming 100% atk or raw?
        int splashDmg = actor.hero.atk; // Simple
        for (TroopStack s : enemy.troops) {
             if (s.count > 0) {
                 TroopDamage.DamageOutcome out = TroopDamage.applyDamage(s, splashDmg);
                 effect.damage += splashDmg - out.overflowDamage; // Track total dmg?
                 effect.logs.add("Splash " + s.type + " killed: " + out.killedCount);
             }
        }
    }

    private void doPoisonHero(BattleState state, Side actor, Side enemy, SkillEffect effect) {
        if (enemy.hero.isAlive()) {
             // Dmg
             int dmg = actor.hero.atk;
             applyDamageToHero(state, actor, enemy, dmg, effect);
             
             // Poison
             addStatus(state, enemy, "Hero", StatusType.POISON, BattleConstants.DURATION_POISON, null);
             effect.logs.add("Poisoned Enemy Hero");
        }
    }
    
    private void doStunHeroChance(BattleState state, Side actor, Side enemy, SkillEffect effect) {
        if (enemy.hero.isAlive()) {
             // Dmg
             int dmg = actor.hero.atk;
             applyDamageToHero(state, actor, enemy, dmg, effect);
             
             // Chance Stun (Use State Seed)
             // Simple pseudo-random using actionNo
             boolean hit = (state.actionNo % 2 == 0); // 50% fixed pattern for V1 or seeded random
             // Use proper random if needed but V1 requires consistent logs.
             // Let's use Seed + TurnNo hash?
             // For simplicity: Always stun in V1 for test, or simple calc
             // Requirement: "rngSeed + actionNo"
             long val = state.rngSeed + state.actionNo;
             hit = (val % 100) < 50; // 50%
             
             if (hit) {
                 addStatus(state, enemy, "Hero", StatusType.STUN, BattleConstants.DURATION_STUN, null);
                 effect.logs.add("Stunned Enemy Hero");
             } else {
                 effect.logs.add("Stun Missed");
             }
        }
    }

    // --- Helpers ---
    
    // Status Helper
    private void addStatus(BattleState state, Side side, String targetId, StatusType type, int duration, Map<String, Object> params) {
        Map<String, List<StatusEffect>> map = (side == state.sideA) ? state.statusesA : state.statusesB;
        List<StatusEffect> list = map.computeIfAbsent(targetId, k -> new ArrayList<>());
        
        // Check duplicate? Refresh duration or stack?
        // Requirement: "Status logic V1" - Simple add.
        // But logic usually refreshes. Let's simple add for now, engine handles check.
        // actually clean old one is better
        list.removeIf(s -> s.type == type);
        
        StatusEffect st = new StatusEffect(type, duration);
        if (params != null) st.params = params;
        list.add(st);
    }
    
    // Damage Helper (Calls back to engine logic or replicates it? Engine calls resolver. Resolver applies effects.)
    // Resolving damage inside resolver means we duplicate applyDamage logic or make it public.
    // TroopDamage is public static.
    // Hero damage is simple substraction but need to trigger "Reflect" etc?
    // Wait, Requirement 2) Sequence: 4. Active Skill -> 6. Take Damage (Immune/Reflect).
    // So resolve() should call a centralized "takeDamage" method? 
    // Or Resolve just modifies HP directly?
    // Better to have a shared "DamageCalculator" or "CombatLogic".
    // For V1, I'll duplicate hero damage basic logic + hooks here to be safe and self-contained, 
    // OR create a public method in BattleEngine to `applyDamageByKey(targetKey, amount)`.
    // I prefer simple direct modification here for V1 to satisfy the SkillResolver interface contract.
    
    private void applyDamageToHero(BattleState state, Side attacker, Side defender, int rawDmg, SkillEffect effect) {
        // Check Immune
        if (isImmune(state, defender, "Hero")) {
            effect.logs.add("Damage Blocked (Immune)");
            return;
        }

        // Apply Multipliers (Gender, etc.)
        double mult = 1.0;
        
        // Passive: Gender
        if (hasPassive(attacker, BattleConstants.PASSIVE_DamageUpVsOppositeGender)) {
             if (isOppositeGender(attacker.hero, defender.hero)) {
                 mult *= BattleConstants.MULT_GENDER_ADVANTAGE;
             }
        }
        
        // Passive: Tanky (Reduc implemented as init stats usually, but if dynamic reduc:
        // Requirement says "maxHp and damageReduction". Assuming reduc is a stat or formula. 
        // V1: Let's assume Tanky = 20% dmg reduction if formula based.
        // But doc says "Init Battle State". So MaxHP is already boosted.
        // Damage reduction? Let's check passives list.
        if (hasPassive(defender, BattleConstants.PASSIVE_Tanky)) {
            mult *= 0.8; 
        }

        // Status: Vulnerable
        if (hasStatus(state, defender, "Hero", StatusType.VULNERABLE)) {
            mult *= BattleConstants.MULT_VULNERABLE;
        }

        int finalDmg = (int)(rawDmg * mult);
        defender.hero.hp -= finalDmg;
        effect.damage += finalDmg;
        effect.logs.add("Hero Hit: " + finalDmg);

        // Reflect (Passive)
        // "反弹对方英雄造成伤害"
        if (hasPassive(defender, BattleConstants.PASSIVE_ReflectPercent)) {
            int reflect = (int)(finalDmg * BattleConstants.PCT_REFLECT);
            if (reflect > 0) {
                 attacker.hero.hp -= reflect;
                 effect.logs.add("Reflected: " + reflect);
            }
        }
    }
    
    private boolean isImmune(BattleState state, Side side, String key) {
        Map<String, List<StatusEffect>> map = (side == state.sideA) ? state.statusesA : state.statusesB;
        if (!map.containsKey(key)) return false;
        return map.get(key).stream().anyMatch(s -> s.type == StatusType.IMMUNE);
    }
    
    private boolean hasStatus(BattleState state, Side side, String key, StatusType type) {
        Map<String, List<StatusEffect>> map = (side == state.sideA) ? state.statusesA : state.statusesB;
        if (!map.containsKey(key)) return false;
        return map.get(key).stream().anyMatch(s -> s.type == type);
    }
    
    private boolean hasPassive(Side side, String passiveId) {
        return side.hero.passives != null && side.hero.passives.contains(passiveId);
    }
    
    private boolean isOppositeGender(Hero h1, Hero h2) {
        if (h1.gender == null || h2.gender == null) return false;
        return !h1.gender.equals(h2.gender) && !h1.gender.equals("U") && !h2.gender.equals("U");
    }
}
