package ljc.battle.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ljc.battle.core.BattleState.Hero;
import ljc.battle.core.BattleState.Side;
import ljc.battle.core.BattleLogEvent.Type;
import ljc.battle.core.BattleLogEvent.Phase; // If using explicit Phase enum
import ljc.battle.core.TroopDamage;
import ljc.battle.core.StatusLogic;
import ljc.battle.core.TroopStack;

/**
 * BattleEngine V3.0 (Final)
 * 
 * Features:
 * - Phase 1: HERO_SOLO (Turn 1-3)
 * - Phase 2: TROOP_WAR (Turn 4+)
 * - Troop Damage Split (Roll 0-100 to Hero/Troop)
 * - Event Driven (Strict V3 Event Schema)
 */
public class BattleEngine {
    
    private SkillResolver skillResolver = new NoSkillResolver();
    
    public void setSkillResolver(SkillResolver resolver) {
        this.skillResolver = resolver;
    }

    public TurnResult processTurn(BattleState state, TurnCommand command) {
        TurnResult result = new TurnResult();
        normalizeSideTempEffects(state);
        
        // 1. Idempotency Check
        if (command.clientTurnNo != state.turnNo + 1) {
             if (command.clientTurnNo <= state.turnNo) {
                result.finished = state.isFinished;
                result.win = state.isWin;
                result.currentTurn = state.turnNo;
                return result;
             }
        }

        // 2. Define Phase & Init
        if (state.phase == null || state.phase.isEmpty()) {
            state.phase = "HERO_SOLO";
        }
        if (state.turnNo == 0) {
            result.logEvents.add(new BattleLogEvent(Type.TURN_START, "SYSTEM", "BATTLE", 1, "Battle Start"));
        }
        
        // 3. Pre-Process (Start of Round)
        if (state.currentActorIndex == 0) {
            if (command.tactics != null && !command.tactics.isEmpty()) {
                state.sideA.hero.tactics = command.tactics;
            }
            
            // Turn Start Event
            BattleLogEvent startEvt = new BattleLogEvent();
            startEvt.type = Type.TURN_START;
            startEvt.turn = state.turnNo + 1;
            // startEvt.phase = Phase.valueOf(state.phase); // If strictly typed
            result.logEvents.add(startEvt);
            
            StatusLogic.processPhaseStart(state, null, result.logEvents);
        }

        // 4. Determine Queue
        List<Actor> actionQueue = determineActionQueue(state);
        
        // Round End Check
        if (state.currentActorIndex >= actionQueue.size()) {
            state.currentActorIndex = 0;
            StatusLogic.decreaseDurations(state);
            decayTemporarySideEffects(state);
            maybeSwitchToTroopWar(state, result);
            actionQueue = determineActionQueue(state); 
        }
        
        if (actionQueue.isEmpty()) { 
             state.isFinished = true;
             result.finished = true;
             return result;
        }
        
        Actor currentActor = actionQueue.get(state.currentActorIndex);
        
        // 5. Execute Action
        executeAction(state, currentActor, command, result);
        
        // 6. Update State
        state.turnNo++; 
        state.currentActorIndex++;
        
        // Keep actor index aligned at round boundary so service can stop at "one round".
        if (state.currentActorIndex >= actionQueue.size()) {
            state.currentActorIndex = 0;
            StatusLogic.decreaseDurations(state);
            decayTemporarySideEffects(state);
            maybeSwitchToTroopWar(state, result);
        }
        
        // 7. Check Win/Loss
        checkBattleEnd(state, result);
        
        // 8. Predict Next
        if (!state.isFinished) {
             List<Actor> nextQueue = (state.currentActorIndex == 0) ? determineActionQueue(state) : actionQueue;
             if (!nextQueue.isEmpty()) {
                 int idx = state.currentActorIndex;
                 if (idx >= nextQueue.size()) idx = 0;
                 result.nextActorDesc = getActorDesc(state, nextQueue.get(idx));
                 state.nextActorDesc = result.nextActorDesc;
             } else {
                 state.nextActorDesc = null;
             }
        }
        
        result.finished = state.isFinished;
        result.win = state.isWin;
        result.currentTurn = state.turnNo;
        
        return result;
    }

    public String predictNextActor(BattleState state) {
        List<Actor> q = determineActionQueue(state);
        if (q.isEmpty()) return null;
        int idx = state.currentActorIndex;
        if (idx >= q.size()) idx = 0;
        return getActorDesc(state, q.get(idx));
    }

    private String getActorDesc(BattleState state, Actor actor) {
        return actor.isHero 
             ? (actor.side == state.sideA ? "HeroA" : "HeroB") 
             : (actor.troop.type + (actor.side == state.sideA ? "_A" : "_B"));
    }

    // --- Execution Dispatcher ---

    private void executeAction(BattleState state, Actor actor, TurnCommand cmd, TurnResult result) {
        if (isActorStunned(state, actor)) return;

        if (actor.isHero) {
            performHeroAction(state, actor, cmd, result);
        } else {
            performTroopAction(state, actor, result);
        }
    }
    
    // --- V3 Hero Action ---

    private void performHeroAction(BattleState state, Actor actor, TurnCommand cmd, TurnResult result) {
        Hero hero = actor.side.hero;
        if (!hero.isAlive()) return;
        
        String sideStr = (actor.side == state.sideA) ? "my" : "enemy";

        // 1. Skill Execution (Delayed)
        if (hero.nextTurnSkillId != null) {
            int heroAtk = applyRate(hero.atk, actor.side.heroAtkRatePermille);
            int skillDmg = (int)(heroAtk * 1.5); 
            
            BattleLogEvent evt = new BattleLogEvent();
            evt.type = Type.HERO_SKILL;
            evt.actorSide = sideStr;
            evt.skillId = hero.nextTurnSkillId;
            evt.value = skillDmg;
            result.logEvents.add(evt);
            
            performHeroAttackLogic(state, actor.side, getEnemySide(state, actor.side), skillDmg, result);
            
            hero.nextTurnSkillId = null;
            return;
        }
        
        // 2. Skill Prep
        boolean userPrep = (actor.side == state.sideA && cmd.type == TurnCommand.ActionType.SKILL && hero.skillCd <= 0);
        // AI Logic omitted
        
        if (userPrep) {
            hero.nextTurnSkillId = "SKILL_DEFAULT";
            hero.skillCd = hero.maxSkillCd;
            // No event required by V3 doc for prep
        }
        
        // 3. Normal Attack
        BattleLogEvent atkEvt = new BattleLogEvent();
        atkEvt.type = Type.HERO_ATTACK;
        atkEvt.actorSide = sideStr;
        int heroAtk = applyRate(hero.atk, actor.side.heroAtkRatePermille);
        atkEvt.value = heroAtk;
        result.logEvents.add(atkEvt);
        
        performHeroAttackLogic(state, actor.side, getEnemySide(state, actor.side), heroAtk, result);
    }
    
    private void performHeroAttackLogic(BattleState state, Side attackerSide, Side defenderSide, int dmg, TurnResult result) {
        // Priority: Hero > Special > ARC > CAV > INF
        if (defenderSide.hero.isAlive()) {
             applyHeroDamage(state, defenderSide, dmg, result);
        } else {
             List<TroopStack> targets = getStrictPriorityTargets(defenderSide);
             String actorSideStr = (attackerSide == state.sideA) ? "my" : "enemy";
             String targetSideStr = (attackerSide == state.sideA) ? "enemy" : "my";
             applyDamageChain(attackerSide.hero.name, targets, dmg, result, targetSideStr);
        }
    }
    
    // --- V3 Troop Action ---

    private void performTroopAction(BattleState state, Actor actor, TurnResult result) {
        TroopStack stack = actor.troop;
        if (stack.count <= 0) return;
        
        Side attackerSide = actor.side;
        Side defenderSide = getEnemySide(state, attackerSide);
        String sideStr = (attackerSide == state.sideA) ? "my" : "enemy";
        String defSideStr = (attackerSide == state.sideA) ? "enemy" : "my";
        
        int totalDmg = applyRate(stack.count * 10, attackerSide.troopAtkRatePermille);
        String specialNote = applyEliteTroopEffects(state, stack, attackerSide, defenderSide, result);
        
        // 1. Roll Split
        int roll = (int)(Math.random() * 101); // 0-100
        if (!defenderSide.hero.isAlive()) roll = 0;
        
        int dmgToHero = (int)(totalDmg * roll / 100.0);
        int dmgToTroops = totalDmg - dmgToHero;
        
        // 2. Emit TROOP_ATTACK
        BattleLogEvent attEvt = new BattleLogEvent();
        attEvt.type = Type.TROOP_ATTACK;
        attEvt.actorSide = sideStr;
        attEvt.attackerTroopType = stack.type;
        attEvt.rollToHero = roll;
        attEvt.damageTotal = totalDmg;
        attEvt.damageToHero = dmgToHero;
        attEvt.damageToTroops = dmgToTroops;
        attEvt.note = specialNote;
        result.logEvents.add(attEvt);
        
        // 3. Apply Hero Damage
        if (dmgToHero > 0 && defenderSide.hero.isAlive()) {
            applyHeroDamage(state, defenderSide, dmgToHero, result);
        }
        
        // 4. Apply Troop Damage
        if (dmgToTroops > 0) {
            if (stack.troopId == 3002) {
                applyAoEDistributedTroopDamage(defenderSide, dmgToTroops, result, defSideStr);
                return;
            }
            TroopStack primaryTarget = null;
            // Tactics logic (Simplified)
            String tactics = attackerSide.hero.tactics;
            if (tactics != null && tactics.startsWith("TARGET_")) {
                 primaryTarget = findStack(defenderSide, tactics.replace("TARGET_", ""));
            }
            // Strict Fallback
            if (primaryTarget == null || primaryTarget.count <= 0) {
                 List<TroopStack> defaults = getStrictPriorityTargets(defenderSide);
                 if (!defaults.isEmpty()) primaryTarget = defaults.get(0);
            }
            
            if (primaryTarget != null) {
                // Construct Chain (Strict Order starting from Primary if possible, or just strict)
                // V2.8 logic: Targeted -> Overflow to Strict Order
                List<TroopStack> chain = new ArrayList<>();
                chain.add(primaryTarget);
                List<TroopStack> others = getStrictPriorityTargets(defenderSide);
                for (TroopStack t : others) {
                    if (t != primaryTarget && t.count > 0) chain.add(t);
                }
                applyDamageChain(stack.type, chain, dmgToTroops, result, defSideStr);
            }
        }
    }
    
    // --- Helpers ---
    
    private void applyDamageChain(String attackerName, List<TroopStack> targets, int totalDamage, TurnResult result, String targetSideStr) {
        int remaining = totalDamage;
        for (TroopStack target : targets) {
            if (target.count <= 0) continue;
            
            int countBefore = target.count;
            int frontHpBefore = target.frontHp; // Not logged but for debug
            
            TroopDamage.DamageOutcome outcome = TroopDamage.applyDamage(target, remaining);
            
            if (outcome.killedCount > 0 || remaining > 0) {
                // Emit STACK_CHANGE
                BattleLogEvent stEvt = new BattleLogEvent();
                stEvt.type = Type.TROOP_STACK_CHANGE;
                stEvt.side = targetSideStr;
                stEvt.troopType = target.type;
                stEvt.killed = outcome.killedCount;
                stEvt.countBefore = countBefore;
                stEvt.countAfter = target.count;
                // Add frontHp info if needed by frontend
                result.logEvents.add(stEvt);
            }
            
            remaining = outcome.overflowDamage;
            if (remaining <= 0) break;
        }
    }
    
    private void applyHeroDamage(BattleState state, Side defenderSide, int dmg, TurnResult result) {
        defenderSide.hero.hp -= dmg;
        String defSideStr = (defenderSide == state.sideA) ? "my" : "enemy";
        
        BattleLogEvent hpEvt = new BattleLogEvent();
        hpEvt.type = Type.HERO_HP_CHANGE;
        hpEvt.side = defSideStr;
        hpEvt.value = -dmg; // Delta
        hpEvt.desc = String.valueOf(defenderSide.hero.hp); // HpAfter
        result.logEvents.add(hpEvt);
        
        if (defenderSide.hero.hp <= 0) {
            defenderSide.hero.isDeadOrRetreated = true;
            BattleLogEvent deadEvt = new BattleLogEvent();
            deadEvt.type = Type.HERO_DEAD;
            deadEvt.side = defSideStr;
            result.logEvents.add(deadEvt);
        }
    }

    private void applyAoEDistributedTroopDamage(Side defenderSide, int totalDamage, TurnResult result, String targetSideStr) {
        List<TroopStack> alive = getStrictPriorityTargets(defenderSide);
        if (alive.isEmpty() || totalDamage <= 0) return;
        int n = alive.size();
        int base = totalDamage / n;
        int remainder = totalDamage % n;
        for (int i = 0; i < n; i++) {
            int dmg = base + (i < remainder ? 1 : 0);
            if (dmg <= 0) continue;
            TroopStack target = alive.get(i);
            int countBefore = target.count;
            TroopDamage.DamageOutcome outcome = TroopDamage.applyDamage(target, dmg);

            BattleLogEvent stEvt = new BattleLogEvent();
            stEvt.type = Type.TROOP_STACK_CHANGE;
            stEvt.side = targetSideStr;
            stEvt.troopType = target.type;
            stEvt.killed = outcome.killedCount;
            stEvt.countBefore = countBefore;
            stEvt.countAfter = target.count;
            result.logEvents.add(stEvt);
        }
    }

    private List<Actor> determineActionQueue(BattleState state) {
        List<Actor> q = new ArrayList<>();
        
        boolean isSolo = "HERO_SOLO".equals(state.phase);
        
        if (isSolo) {
             q.add(new Actor(state.sideA, true, null));
             q.add(new Actor(state.sideB, true, null));
             Collections.sort(q, (a, b) -> b.side.hero.speed - a.side.hero.speed);
             return q;
        }

        // WAR Phase:
        // 1. ARC
        addAllTroopsByType(q, state, "ARC");
        // 2. Hero
        q.add(new Actor(state.sideA, true, null));
        q.add(new Actor(state.sideB, true, null));
        // 3. Others
        addAllTroopsByType(q, state, "INF");
        addAllTroopsByType(q, state, "CAV");
        
        return q;
    }
    
    private void checkBattleEnd(BattleState state, TurnResult result) {
        // V3 Rule: "一方没有任何可战斗单位" (Hero Dead/Retreated AND Troops Count=0)
        boolean aLost = !canFight(state.sideA);
        boolean bLost = !canFight(state.sideB);
        
        if (aLost || bLost) {
            state.isFinished = true;
            state.isWin = bLost && !aLost;
            
            BattleLogEvent endEvt = new BattleLogEvent();
            endEvt.type = Type.BATTLE_END;
            endEvt.value = state.isWin ? 1 : 0; // 1=Win
            result.logEvents.add(endEvt);
        }
    }

    private void maybeSwitchToTroopWar(BattleState state, TurnResult result) {
        if (!"HERO_SOLO".equals(state.phase)) return;
        if (state.isFinished) return;
        if (!shouldEnterTroopWar(state)) return;

        BattleLogEvent evt = new BattleLogEvent();
        evt.type = Type.PHASE_CHANGE;
        evt.fromPhase = "HERO_SOLO";
        evt.toPhase = "TROOP_WAR";
        evt.myHeroCanFight = state.sideA != null && state.sideA.hero != null && state.sideA.hero.isAlive();
        evt.enemyHeroCanFight = state.sideB != null && state.sideB.hero != null && state.sideB.hero.isAlive();
        evt.desc = "进入全军出击阶段";
        evt.phase = Phase.TROOP_WAR;
        result.logEvents.add(evt);

        state.phase = "TROOP_WAR";
        state.currentActorIndex = 0;
    }

    private boolean shouldEnterTroopWar(BattleState state) {
        boolean myHeroAlive = state.sideA != null && state.sideA.hero != null && state.sideA.hero.isAlive();
        boolean enemyHeroAlive = state.sideB != null && state.sideB.hero != null && state.sideB.hero.isAlive();
        return !myHeroAlive || !enemyHeroAlive;
    }
    
    private boolean canFight(Side side) {
        // Hero Alive OR Any Troop > 0
        if (side.hero.isAlive()) return true;
        for (TroopStack s : side.troops) {
            if (s.count > 0) return true;
        }
        return false;
    }

    private String applyEliteTroopEffects(BattleState state, TroopStack attacker, Side attackerSide, Side defenderSide, TurnResult result) {
        switch (attacker.troopId) {
            case 3001:
                return applyHealerElite(state, attacker, attackerSide, result);
            case 3002:
                return "AOE分散: 对敌方所有兵堆栈均摊伤害";
            case 3003:
                attackerSide.heroAtkRatePermille = 1300;
                attackerSide.heroAtkRateTurns = 2;
                return "英雄增益: 本方英雄攻击+30%(2回合)";
            case 3004:
                defenderSide.troopAtkRatePermille = 750;
                defenderSide.troopAtkRateTurns = 2;
                return "敌军减攻: 敌方小兵攻击-25%(2回合)";
            default:
                return null;
        }
    }

    private String applyHealerElite(BattleState state, TroopStack attacker, Side attackerSide, TurnResult result) {
        int heroHeal = Math.max(60, attacker.count * 2);
        int before = attackerSide.hero.hp;
        attackerSide.hero.hp = Math.min(attackerSide.hero.maxHp, attackerSide.hero.hp + heroHeal);
        int actualHeal = attackerSide.hero.hp - before;

        String sideStr = (attackerSide == state.sideA) ? "my" : "enemy";
        if (actualHeal > 0) {
            BattleLogEvent hpEvt = new BattleLogEvent();
            hpEvt.type = Type.HERO_HP_CHANGE;
            hpEvt.side = sideStr;
            hpEvt.value = actualHeal;
            hpEvt.desc = String.valueOf(attackerSide.hero.hp);
            result.logEvents.add(hpEvt);
        }

        int troopHealedStacks = 0;
        for (TroopStack s : attackerSide.troops) {
            if (s.count <= 0) continue;
            int healFront = Math.max(1, s.unitHp / 4);
            int oldFront = s.frontHp;
            s.frontHp = Math.min(s.unitHp, s.frontHp + healFront);
            if (s.frontHp > oldFront) {
                troopHealedStacks++;
            }
        }
        return "治疗支援: 英雄+" + actualHeal + ", 兵线恢复" + troopHealedStacks + "队";
    }

    private void normalizeSideTempEffects(BattleState state) {
        normalizeSingleSideTempEffects(state.sideA);
        normalizeSingleSideTempEffects(state.sideB);
    }

    private void normalizeSingleSideTempEffects(Side side) {
        if (side == null) return;
        if (side.heroAtkRatePermille <= 0) side.heroAtkRatePermille = 1000;
        if (side.troopAtkRatePermille <= 0) side.troopAtkRatePermille = 1000;
    }

    private void decayTemporarySideEffects(BattleState state) {
        decaySingleSideTempEffects(state.sideA);
        decaySingleSideTempEffects(state.sideB);
    }

    private void decaySingleSideTempEffects(Side side) {
        if (side == null) return;
        if (side.heroAtkRateTurns > 0) {
            side.heroAtkRateTurns--;
            if (side.heroAtkRateTurns <= 0) {
                side.heroAtkRatePermille = 1000;
            }
        }
        if (side.troopAtkRateTurns > 0) {
            side.troopAtkRateTurns--;
            if (side.troopAtkRateTurns <= 0) {
                side.troopAtkRatePermille = 1000;
            }
        }
    }

    private int applyRate(int base, int permille) {
        int p = permille <= 0 ? 1000 : permille;
        return Math.max(1, (int) Math.round(base * (p / 1000.0)));
    }

    // --- Basic Helpers ---
    
    private void addAllTroopsByType(List<Actor> queue, BattleState state, String type) {
        for (TroopStack s : state.sideA.troops) {
            if (s.type.equalsIgnoreCase(type) && s.count > 0) queue.add(new Actor(state.sideA, false, s));
        }
        for (TroopStack s : state.sideB.troops) {
            if (s.type.equalsIgnoreCase(type) && s.count > 0) queue.add(new Actor(state.sideB, false, s));
        }
    }

    private TroopStack findStack(Side side, String type) {
        for (TroopStack s : side.troops) { if (s.type.equalsIgnoreCase(type)) return s; }
        return null;
    }

    private Side getEnemySide(BattleState state, Side mySide) {
        return mySide == state.sideA ? state.sideB : state.sideA;
    }
    
    private List<TroopStack> getStrictPriorityTargets(Side side) {
        List<TroopStack> list = new ArrayList<>();
        // 1. Special Troops (troopId 3000+ or type ELITE_*)
        for (TroopStack s : side.troops) {
            if (s.count > 0 && (s.troopId >= 3000 || s.type.startsWith("ELITE"))) list.add(s);
        }
        // 2. Standard Types
        addIfValid(list, findStack(side, "ARC"));
        addIfValid(list, findStack(side, "CAV"));
        addIfValid(list, findStack(side, "INF"));
        return list;
    }
    
    private void addIfValid(List<TroopStack> list, TroopStack s) {
        if (s != null && s.count > 0) list.add(s);
    }

    private boolean isActorStunned(BattleState state, Actor actor) { return false; } // Stub

    private static class Actor {
        Side side;
        boolean isHero;
        TroopStack troop;
        public Actor(Side s, boolean h, TroopStack t) { this.side = s; this.isHero = h; this.troop = t; }
    }
}
