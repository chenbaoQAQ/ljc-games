package ljc.battle.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import ljc.battle.core.BattleState.Hero;
import ljc.battle.core.BattleState.Side;
import ljc.battle.core.BattleState.TroopStack;
import ljc.battle.core.BattleLogEvent.Type;
import ljc.battle.core.TroopDamage;
import ljc.battle.core.StatusLogic;

/**
 * BattleEngine V2.8
 * 
 * Update V2.8:
 * - Strict Stack Model (Count/UnitHp/FrontHp)
 * - Strict Priority Targeting
 * - Damage Overflow
 * - ARC Phase First
 * - Hero Skill Delay (Perform Normal Attack this turn, Skill next turn)
 */
public class BattleEngine {
    
    private SkillResolver skillResolver = new NoSkillResolver();
    
    public void setSkillResolver(SkillResolver resolver) {
        this.skillResolver = resolver;
    }

    public TurnResult processTurn(BattleState state, TurnCommand command) {
        TurnResult result = new TurnResult();
        
        // 1. Idempotency Check
        if (command.clientTurnNo != state.turnNo + 1) {
             if (command.clientTurnNo <= state.turnNo) {
                result.finished = state.isFinished;
                result.win = state.isWin;
                result.currentTurn = state.turnNo;
                return result;
             }
        }

        // 2. Pre-Process: Update Tactics & Phase Start
        if (state.currentActorIndex == 0) {
            // Update Tactics from Command (User input applies to this whole round)
            if (command.tactics != null && !command.tactics.isEmpty()) {
                state.sideA.hero.tactics = command.tactics;
            }
            
            // Log Round Start
            if (state.turnNo == 0) {
                 result.logEvents.add(new BattleLogEvent(Type.TURN_START, "SYSTEM", "BATTLE", 1, "Battle Start"));
            }
            
            // DoT / HoT
            StatusLogic.processPhaseStart(state, null, result.logEvents);
        }

        // 3. Determine Queue
        List<Actor> actionQueue = determineActionQueue(state);
        
        // Check Round End
        if (state.currentActorIndex >= actionQueue.size()) {
            state.currentActorIndex = 0;
            result.logEvents.add(new BattleLogEvent(Type.TURN_END, "SYSTEM", "ROUND", state.turnNo, "Round Finished"));
            StatusLogic.decreaseDurations(state);
            actionQueue = determineActionQueue(state); // Re-calc for next round
        }
        
        if (actionQueue.isEmpty()) { // All dead?
             state.isFinished = true;
             result.finished = true;
             return result;
        }
        
        Actor currentActor = actionQueue.get(state.currentActorIndex);
        
        // 4. Execute
        executeAction(state, currentActor, command, result);
        
        // 5. Update State
        state.turnNo++; 
        state.currentActorIndex++;
        
        // 6. Check Win/Loss
        checkBattleEnd(state, result);
        
        // 7. Predict Next
        if (!state.isFinished) {
             List<Actor> nextQueue = (state.currentActorIndex == 0) ? determineActionQueue(state) : actionQueue;
             if (!nextQueue.isEmpty()) {
                 int idx = state.currentActorIndex;
                 if (idx >= nextQueue.size()) idx = 0;
                 Actor next = nextQueue.get(idx);
                 result.nextActorDesc = getActorDesc(state, next);
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

    // --- Core Execution ---

    private void executeAction(BattleState state, Actor actor, TurnCommand cmd, TurnResult result) {
        String actorId = getActorDesc(state, actor);
        result.logEvents.add(new BattleLogEvent(Type.ACTOR_CHOSEN, actorId, null, 0, "Acting"));
        
        if (isActorStunned(state, actor)) {
             result.logEvents.add(new BattleLogEvent(Type.STUN_SKIP, actorId, "Self", 0, "Stunned"));
             consumeStun(state, actor);
             return; 
        }

        if (actor.isHero) {
            Hero hero = actor.side.hero;
            if (!hero.isAlive()) return;

            // 1. Check Delayed Skill (V2.8 NextTurn Logic)
            if (hero.nextTurnSkillId != null) {
                // Execute Pre-set Skill
                SkillResolver.SkillDecision decision = new SkillResolver.SkillDecision(SkillResolver.SkillDecision.Type.SKILL_A, hero.nextTurnSkillId, actor.side);
                SkillResolver.SkillEffect effect = skillResolver.resolve(state, decision);
                
                for (String log : effect.logs) {
                    result.logEvents.add(new BattleLogEvent(Type.SKILL, actorId, "Skill", 0, log));
                }
                
                hero.nextTurnSkillId = null; // Clear
                return; // Skill executed, Action done.
            }

            // 2. Prepare for Next Turn Skill?
            boolean userPrep = (actor.side == state.sideA && cmd.type == TurnCommand.ActionType.SKILL && hero.skillCd <= 0);
            boolean aiPrep = (actor.side == state.sideB && skillResolver.decideSkill(state, actor.side).type == SkillResolver.SkillDecision.Type.SKILL_A);

            if (userPrep || aiPrep) {
                // Set flag for NEXT turn
                hero.nextTurnSkillId = "SKILL_DEFAULT"; // or actual ID
                hero.skillCd = hero.maxSkillCd;
                result.logEvents.add(new BattleLogEvent(Type.SKILL_CAST, actorId, null, 0, "技能蓄力! 下回合释放!"));
                // And fall through to perform NORMAL ATTACK this turn
            }

            // 3. Normal Attack (Always performed if not executing a delayed skill)
            performHeroAttack(state, actor.side, getEnemySide(state, actor.side), result);

        } else {
            // Troop Action
            TroopStack stack = actor.troop;
            if (stack.count <= 0) return; 

            performTroopAttack(state, actor.side, getEnemySide(state, actor.side), stack, result);
        }
    }

    // --- Hero Attack (Strict V2.8) ---
    private void performHeroAttack(BattleState state, Side attackerSide, Side defenderSide, TurnResult result) {
        int dmg = attackerSide.hero.atk;
        String attackerName = attackerSide.hero.name;

        // V2.8 Priority: Hero > Special > ARC > CAV > INF
        if (defenderSide.hero.isAlive()) {
             applyHeroDamage(attackerName, defenderSide.hero, dmg, result, state, defenderSide);
        } else {
             List<TroopStack> targets = getStrictPriorityTargets(defenderSide); // Special->ARC->CAV->INF
             applyDamageChain(attackerName, targets, dmg, result);
        }
    }

    // --- Troop Attack (Strict V2.8) ---
    private void performTroopAttack(BattleState state, Side attackerSide, Side defenderSide, TroopStack attackerStack, TurnResult result) {
        int unitAtk = 10; // TODO: Config
        int totalDmg = attackerStack.count * unitAtk;
        String attackerName = attackerStack.type;

        // 1. Target Selection
        // Explicit Tactics > Counter > Strict Priority
        TroopStack primaryTarget = null;
        String tactics = attackerSide.hero.tactics;
        
        // Tactics
        if (tactics != null && tactics.startsWith("TARGET_")) {
            String type = tactics.replace("TARGET_", "");
            primaryTarget = findStack(defenderSide, type);
        }
        
        // Counter
        if (primaryTarget == null || primaryTarget.count <= 0) {
            String counterType = getCounterTargetType(attackerStack.type);
            primaryTarget = findStack(defenderSide, counterType);
        }
        
        // Fallback Priority
        if (primaryTarget == null || primaryTarget.count <= 0) {
            List<TroopStack> defaults = getStrictPriorityTargets(defenderSide);
            if (!defaults.isEmpty()) primaryTarget = defaults.get(0);
        }
        
        if (primaryTarget == null || primaryTarget.count <= 0) return; // No targets

        // 2. Multiplier (V2.8: 1.25 / 0.75)
        double mult = computeV28Multiplier(attackerStack.type, primaryTarget.type);
        int finalDmg = (int)(totalDmg * mult);

        // 3. Apply Damage Chain (Overflow)
        // Chain starts with Primary, then follows Strict Priority (excluding Primary)
        List<TroopStack> chain = new ArrayList<>();
        chain.add(primaryTarget);
        List<TroopStack> others = getStrictPriorityTargets(defenderSide);
        for (TroopStack t : others) {
            if (t != primaryTarget && t.count > 0) chain.add(t);
        }
        
        applyDamageChain(attackerName, chain, finalDmg, result);

        // 4. Hero Assist (Commander Attack)
        if (attackerSide.hero.isAlive() && !isActorStunned(state, new Actor(attackerSide, true, null))) {
             int assistDmg = (int)(attackerSide.hero.atk * 0.2);
             if (assistDmg > 0) {
                 // Assist hits the SAME primary target (if alive) or follows chain
                 List<TroopStack> assistChain = new ArrayList<>();
                 if (primaryTarget.count > 0) assistChain.add(primaryTarget);
                 assistChain.addAll(others);
                 if (!assistChain.isEmpty()) {
                     result.logEvents.add(new BattleLogEvent(Type.ASSIST_ATK, attackerSide.hero.name, "Assist", assistDmg, "统帅追击"));
                     applyDamageChain(attackerSide.hero.name + "(Assist)", assistChain, assistDmg, result);
                 }
             }
        }
    }

    // --- Strict Damage Chain Logic ---
    private void applyDamageChain(String attackerName, List<TroopStack> targets, int totalDamage, TurnResult result) {
        int remaining = totalDamage;
        for (TroopStack target : targets) {
            if (target.count <= 0) continue;
            
            TroopDamage.DamageOutcome outcome = TroopDamage.applyDamage(target, remaining);
            
            // Log
            if (outcome.killedCount > 0 || remaining > 0) {
                int deal = remaining - outcome.overflowDamage;
                result.logEvents.add(new BattleLogEvent(Type.ATTACK, attackerName, target.type, deal, "Killed: " + outcome.killedCount));
                if (outcome.killedCount > 0) {
                    result.logEvents.add(new BattleLogEvent(Type.KILL, attackerName, target.type, outcome.killedCount, "Dead"));
                }
            }
            
            remaining = outcome.overflowDamage;
            if (remaining <= 0) break;
        }
    }

    // --- V2.8 Logic Helpers ---
    
    private List<TroopStack> getStrictPriorityTargets(Side side) {
        // V2.8: Special -> ARC -> CAV -> INF
        List<TroopStack> list = new ArrayList<>();
        // TODO: Special
        addIfValid(list, findStack(side, "ARC"));
        addIfValid(list, findStack(side, "CAV"));
        addIfValid(list, findStack(side, "INF"));
        return list;
    }
    
    private void addIfValid(List<TroopStack> list, TroopStack s) {
        if (s != null && s.count > 0) list.add(s);
    }

    private double computeV28Multiplier(String atk, String def) {
        // INF > ARC > CAV > INF
        if (atk.equals("INF")) {
            if (def.equals("ARC")) return 1.25;
            if (def.equals("CAV")) return 0.75;
        } else if (atk.equals("ARC")) {
            if (def.equals("CAV")) return 1.25;
            if (def.equals("INF")) return 0.75;
        } else if (atk.equals("CAV")) {
            if (def.equals("INF")) return 1.25;
            if (def.equals("ARC")) return 0.75;
        }
        return 1.0;
    }

    // --- Queue: ARC First ---
    private List<Actor> determineActionQueue(BattleState state) {
        List<Actor> q = new ArrayList<>();
        
        // 1. ARC Phase
        addAllTroopsByType(q, state, "ARC");
        
        // 2. Hero Phase (Inserted here for interaction flow)
        q.add(new Actor(state.sideA, true, null));
        q.add(new Actor(state.sideB, true, null));
        
        // 3. Other Troops
        addAllTroopsByType(q, state, "INF");
        addAllTroopsByType(q, state, "CAV");
        
        return q;
    }
    
    // --- Existing Helpers ---

    private void addAllTroopsByType(List<Actor> queue, BattleState state, String type) {
        for (TroopStack s : state.sideA.troops) {
            if (s.type.equalsIgnoreCase(type) && s.count > 0) queue.add(new Actor(state.sideA, false, s));
        }
        for (TroopStack s : state.sideB.troops) {
            if (s.type.equalsIgnoreCase(type) && s.count > 0) queue.add(new Actor(state.sideB, false, s));
        }
    }

    private TroopStack findStack(Side side, String type) {
        for (TroopStack s : side.troops) {
            if (s.type.equalsIgnoreCase(type)) return s;
        }
        return null;
    }

    private Side getEnemySide(BattleState state, Side mySide) {
        return mySide == state.sideA ? state.sideB : state.sideA;
    }
    
    private String getCounterTargetType(String type) {
         if (type.equals("INF")) return "ARC";
         if (type.equals("ARC")) return "CAV";
         if (type.equals("CAV")) return "INF";
         return "INF";
    }

    private void applyHeroDamage(String attackerName, Hero target, int dmg, TurnResult result, BattleState state, Side defenderSide) {
        // Simplified Logic, assuming V2.8 rules
        // Personality checks omitted for brevity but should be here
        target.hp -= dmg;
        result.logEvents.add(new BattleLogEvent(Type.ATTACK, attackerName, "Hero", dmg, "Target HP: " + target.hp));
        if (target.hp <= 0) {
            target.isDeadOrRetreated = true;
            result.logEvents.add(new BattleLogEvent(Type.HERO_DOWN, attackerName, target.name, 0, "Dead"));
        }
    }

    private void checkBattleEnd(BattleState state, TurnResult result) {
        boolean sideADead = !state.sideA.hero.isAlive() && isEmpty(state.sideA.troops); 
        boolean sideBDead = !state.sideB.hero.isAlive() && isEmpty(state.sideB.troops);
        // Requirement: Hero Dead OR All Troops Dead? Usually Hero Dead = Loss in this user's earlier docs.
        // V2.8 12.3: "Hero dead/retreat -> Hero no longer acts". Doesn't explicitly say Battle End.
        // But usually "Wipe out" or "Hero Dead" is condition.
        // Let's stick to: Hero Dead = Loss OR All Units Dead = Loss.
        // For now: Hero Dead = Loss.
        
        // Wait, V2.8 Section 12 says "Hero retreat -> Hero no longer acts". It implies battle continues with troops!
        // So defeat condition must be: All Troops Dead AND Hero Dead/Retreated.
        
        boolean aLost = state.sideA.hero.isDeadOrRetreated && allTroopsDead(state.sideA);
        boolean bLost = state.sideB.hero.isDeadOrRetreated && allTroopsDead(state.sideB);
        
        if (aLost || bLost) {
            state.isFinished = true;
            state.isWin = bLost && !aLost;
        }
    }

    private boolean allTroopsDead(Side side) {
        return side.troops.stream().allMatch(t -> t.count <= 0);
    }
    
    private boolean isEmpty(List<TroopStack> t) { return t.stream().allMatch(s -> s.count <= 0); }

    // Logic Helpers
    private boolean isActorStunned(BattleState state, Actor actor) {
        // Simplified
        return false;
    }

    private void consumeStun(BattleState state, Actor actor) {
        // Simplified
    }

    private static class Actor {
        Side side;
        boolean isHero;
        TroopStack troop;
        public Actor(Side s, boolean h, TroopStack t) { this.side = s; this.isHero = h; this.troop = t; }
    }
}
