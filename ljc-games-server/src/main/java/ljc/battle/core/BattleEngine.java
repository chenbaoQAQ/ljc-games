package ljc.battle.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import ljc.battle.core.BattleState.Hero;
import ljc.battle.core.BattleState.Side;
import ljc.battle.core.BattleLogEvent.Type;

/**
 * BattleEngine V1
 * 核心战斗逻辑引擎（纯逻辑，无DB）
 */
public class BattleEngine {
    
    // 注入技能解析器 (V1 使用默认)
    private SkillResolver skillResolver = new NoSkillResolver();
    
    public void setSkillResolver(SkillResolver resolver) {
        this.skillResolver = resolver;
    }

    /**
     * 推进一个行动 (Action)
     * 根据 2.1 要求：每次推进一个行动，不是整回合
     * 前端可能需要多次调用来完成一个 Round
     */
    public TurnResult processTurn(BattleState state, TurnCommand command) {
        TurnResult result = new TurnResult();
        
        // 1. 幂等校验 (turnNo 是全局动作计数)
        if (command.clientTurnNo != state.turnNo + 1) {
            // 如果请求的是旧回合，直接返回当前状态（不执行）
            // 或者抛错。为了鲁棒性，这里返回当前状态，标记 invalid
            // 但 V1 简单起见，假设客户端逻辑正常，若不匹配则认为已处理
             if (command.clientTurnNo <= state.turnNo) {
                // 已处理过
                result.finished = state.isFinished;
                result.win = state.isWin;
                result.currentTurn = state.turnNo;
                return result;
             }
             // 若跳号太多，暂不处理，直接由错误处理兜底
        }

        // 2. 确定本 Round 的行动顺序
        // 注意：state.currentActorIndex 记录当前 Round 执行到第几个 actor
        List<Actor> actionQueue = determineActionQueue(state);
        
        if (state.currentActorIndex >= actionQueue.size()) {
            // Round 结束，进入下一 Round (理论上上一步结尾会重置，但防万一)
            state.currentActorIndex = 0;
            
            // Log Round End
            result.logEvents.add(new BattleLogEvent(Type.TURN_END, "SYSTEM", "ROUND", state.turnNo, "Round Finished"));
             
            // Decrease Status Durations at end of Round?
            StatusLogic.decreaseDurations(state);
             
            // 重新计算（可能有人死了）
            actionQueue = determineActionQueue(state);
        }
        
        // 3. 取出当前行动者
        if (actionQueue.isEmpty()) {
            // Should not happen unless everyone dead
             state.isFinished = true;
             result.finished = true;
             return result;
        }
        
        Actor currentActor = actionQueue.get(state.currentActorIndex);
        
        // 4. 执行行动
        executeAction(state, currentActor, command, result);
        
        // 5. 更新状态
        state.turnNo++; // 全局 Action 计数 +1
        state.currentActorIndex++;
        
         // 6. 检查 Round 是否结束 (After increment)
        if (state.currentActorIndex >= actionQueue.size()) {
            state.currentActorIndex = 0; // 重置给下一 Round
            // Log Round End is usually done at start of next processing or here.
            // Let's do nothing here, let next call handle "Round Start" logic if needed.
            // But we can Log for clarity.
             result.logEvents.add(new BattleLogEvent(Type.TURN_END, "SYSTEM", "ROUND", state.turnNo, "Round Finished"));
             StatusLogic.decreaseDurations(state);
             
             // Check End again?
        }
        
        // 7. 检查战斗胜负 (每次行动后都查)
        checkBattleEnd(state, result);
        
        // 8. 填充下一行动方信息
        if (!state.isFinished) {
             // actionQueue changed? Next round queue might differ.
             // Predict next actor for UI
             List<Actor> nextQueue = (state.currentActorIndex == 0) ? determineActionQueue(state) : actionQueue;
             
             if (!nextQueue.isEmpty()) {
                 int idx = state.currentActorIndex;
                 if (idx >= nextQueue.size()) idx = 0;
                 Actor next = nextQueue.get(idx);
                 String desc = next.isHero 
                     ? (next.side == state.sideA ? "HeroA" : "HeroB") 
                     : (next.troop.type + (next.side == state.sideA ? "_A" : "_B"));
                 
                 result.nextActorIndex = idx;
                 result.nextActorDesc = desc;
                 state.nextActorDesc = desc; // Sync to State for persistence
             } else {
                 state.nextActorDesc = null;
             }
        }
        
        result.finished = state.isFinished;
        result.win = state.isWin;
        result.currentTurn = state.turnNo;
        
        return result;
    }

    // Expose for Start Battle Initialization
    public String predictNextActor(BattleState state) {
        List<Actor> q = determineActionQueue(state);
        if (q.isEmpty()) return null;
        int idx = state.currentActorIndex;
        if (idx >= q.size()) idx = 0;
        Actor next = q.get(idx);
        return next.isHero 
             ? (next.side == state.sideA ? "HeroA" : "HeroB") 
             : (next.troop.type + (next.side == state.sideA ? "_A" : "_B"));
    }

    // --- 内部逻辑 ---

    private void executeAction(BattleState state, Actor actor, TurnCommand cmd, TurnResult result) {
        
        // 0. Round 开始统一结算 DoT/HoT 的逻辑
        if (state.currentActorIndex == 0) {
            StatusLogic.processPhaseStart(state, null, result.logEvents);
        }

        String actorId = actor.isHero ? (actor.side == state.sideA ? "HeroA" : "HeroB") 
                                      : actor.troop.type + (actor.side == state.sideA ? "_A" : "_B");
        result.logEvents.add(new BattleLogEvent(Type.ACTOR_CHOSEN, actorId, null, 0, "Turn Start"));
        
        if (isActorStunned(state, actor)) {
             result.logEvents.add(new BattleLogEvent(Type.STUN_SKIP, actorId, null, 0, "Stunned, Turn Skipped"));
             consumeStun(state, actor);
             return; 
        }

        // 英雄行动
        if (actor.isHero) {
            Hero hero = actor.side.hero;
            if (!hero.isAlive()) return;
            
            // 更新战术指令
            if (cmd.tactics != null && !cmd.tactics.isEmpty()) {
                hero.tactics = cmd.tactics;
            }
            
            // 判定延迟技能生效
            if (hero.castingSkillTurns > 0) {
                hero.castingSkillTurns--;
                if (hero.castingSkillTurns <= 0) {
                    // 技能爆发！
                    SkillResolver.SkillDecision decision = new SkillResolver.SkillDecision(SkillResolver.SkillDecision.Type.SKILL_A);
                    SkillResolver.SkillEffect effect = skillResolver.resolve(state, decision);
                    for (String log : effect.logs) {
                        result.logEvents.add(new BattleLogEvent(Type.SKILL, actorId, "Skill", 0, log));
                    }
                    hero.castingSkillId = null;
                } else {
                    result.logEvents.add(new BattleLogEvent(Type.SKILL_CAST, actorId, null, 0, "正在蓄力..."));
                }
                return; // 蓄力/施法中不普攻
            }

            // AMBUSH_VOLLEY Hook
            checkAmbushVolley(state, actor.side, getEnemySide(state, actor.side), result);
            
            // 用户发起施法 (ActionType.SKILL)
            boolean userCast = (actor.side == state.sideA && cmd.type == TurnCommand.ActionType.SKILL && hero.skillCd <= 0);
            boolean aiCast = (actor.side == state.sideB && skillResolver.decideSkill(state, actor.side).type == SkillResolver.SkillDecision.Type.SKILL_A);
            
            if (userCast || aiCast) {
                // 进入蓄力状态 (1回合延迟)
                hero.castingSkillTurns = 1; 
                hero.skillCd = hero.maxSkillCd;
                result.logEvents.add(new BattleLogEvent(Type.SKILL_CAST, actorId, null, 0, "开始施法准备!"));
                // 本回合动作结束
            } else {
                // 自动普攻
                performHeroAttack(state, actor.side, getEnemySide(state, actor.side), result);
            }

        } else {
            // 兵种行动
            TroopStack stack = actor.troop;
            if (stack.count <= 0) return; 

            performTroopAttack(state, actor.side, getEnemySide(state, actor.side), stack, result);
        }
    }
    
    // --- Phase Helpers ---
    private boolean isActorStunned(BattleState state, Actor actor) {
        if (!actor.isHero) return false; 
        Map<String, List<StatusEffect>> map = (actor.side == state.sideA) ? state.statusesA : state.statusesB;
        List<StatusEffect> list = map.get("Hero");
        if (list == null) return false;
        return list.stream().anyMatch(s -> s.type == StatusEffect.StatusType.STUN);
    }
    
    private void checkAmbushVolley(BattleState state, Side actor, Side enemy, TurnResult result) {
        if (actor.hero.actives != null && actor.hero.actives.contains(BattleConstants.SKILL_ATK_AMBUSH_ARCHER_VOLLEY)) {
             TroopStack arc = findStack(actor, "ARC");
             if (arc != null && arc.count > 0 && enemy.hero.isAlive()) {
                 int volleyDmg = arc.count * 10; 
                 enemy.hero.hp -= volleyDmg;
                 result.logEvents.add(new BattleLogEvent(Type.AMBUSH_VOLLEY, "ARC_Ambush", "Hero", volleyDmg, "Volley"));
                 
                 if (enemy.hero.hp <= 0) {
                     enemy.hero.isDeadOrRetreated = true;
                     result.logEvents.add(new BattleLogEvent(Type.HERO_DOWN, "ARC_Ambush", "Hero", 0, "Dead"));
                 }
             }
        }
    }

    // 英雄普攻
    private void performHeroAttack(BattleState state, Side attackerSide, Side defenderSide, TurnResult result) {
        int dmg = attackerSide.hero.atk;
        
        // 目标选择
        if (defenderSide.hero.isAlive()) {
             applyHeroDamage(attackerSide.hero.name, defenderSide.hero, dmg, result, state, defenderSide);
        } else {
             List<TroopStack> targets = getTargetTroopsByPriority(defenderSide);
             int remainingDmg = dmg;
             for (TroopStack targetStack : targets) {
                 if (targetStack.count <= 0) continue;
                 TroopDamage.DamageOutcome outcome = TroopDamage.applyDamage(targetStack, remainingDmg);
                 result.logEvents.add(new BattleLogEvent(Type.ATTACK, attackerSide.hero.name, targetStack.type, remainingDmg, "Hero Attack Troop"));
                 if (outcome.killedCount > 0) {
                     result.logEvents.add(new BattleLogEvent(Type.KILL, attackerSide.hero.name, targetStack.type, outcome.killedCount, "Killed Units"));
                 }
                 remainingDmg = outcome.overflowDamage;
                 if (remainingDmg <= 0) break;
             }
        }
    }

    // 兵种普攻 (增强版：支持战术 + 武将参战)
    private void performTroopAttack(BattleState state, Side attackerSide, Side defenderSide, TroopStack attackerStack, TurnResult result) {
        int unitAtk = 10; 
        int totalRawDmg = attackerStack.count * unitAtk;
        
        // 1. 寻找目标 (基于战术)
        TroopStack targetStack = null;
        String tactics = attackerSide.hero.tactics; // e.g. "TARGET_ARC"
        
        if (tactics != null && !tactics.isEmpty() && !tactics.equals("DEFAULT")) {
             // Try find specific target
             String preferType = tactics.replace("TARGET_", ""); // "ARC"
             targetStack = findStack(defenderSide, preferType);
        }
        
        // Fallback: Counter Type
        if (targetStack == null || targetStack.count <= 0) {
             String targetType = getCounterTargetType(attackerStack.type);
             targetStack = findStack(defenderSide, targetType);
        }
        
        // Fallback: Helper Logic (Priority List)
        if (targetStack == null || targetStack.count <= 0) {
            List<TroopStack> targets = getTargetTroopsByPriority(defenderSide);
            for (TroopStack t : targets) {
                if (t.count > 0) {
                    targetStack = t;
                    break;
                }
            }
        }
        
        if (targetStack == null || targetStack.count <= 0) return; // No targets
        
        // 2. 计算兵种克制 + 伤害
        double mult = computeTypeMultiplier(attackerStack.type, targetStack.type);
        int finalDmg = (int)(totalRawDmg * mult);
        
        // Commit troops (Spillover logic)
        List<TroopStack> commitChain = new ArrayList<>();
        commitChain.add(targetStack);
        List<TroopStack> others = getTargetTroopsByPriority(defenderSide);
        for (TroopStack t : others) {
            if (t != targetStack && t.count > 0) commitChain.add(t);
        }
        
        int remainingDmg = finalDmg;
        for (TroopStack t : commitChain) {
             if (t.count <= 0) continue;
             TroopDamage.DamageOutcome outcome = TroopDamage.applyDamage(t, remainingDmg);
             
             result.logEvents.add(new BattleLogEvent(Type.ATTACK, attackerStack.type, t.type, finalDmg, "Mult:" + mult)); 
             if (outcome.killedCount > 0) {
                 result.logEvents.add(new BattleLogEvent(Type.KILL, attackerStack.type, t.type, outcome.killedCount, "Killed"));
             }
             
             remainingDmg = outcome.overflowDamage;
             if (remainingDmg <= 0) break;
        }
        
        // 3. 武将参战 (Assist)
        // 逻辑：如果武将存活且未被控制，追加一次 20% 攻击力的伤害给当前目标
        if (attackerSide.hero.isAlive() && !isActorStunned(state, new Actor(attackerSide, true, null))) {
            int assistDmg = (int)(attackerSide.hero.atk * 0.2); // 20% ATK
            if (assistDmg > 0 && targetStack.count > 0) { // Still alive?
                 TroopDamage.DamageOutcome outcome = TroopDamage.applyDamage(targetStack, assistDmg);
                 result.logEvents.add(new BattleLogEvent(Type.ASSIST_ATK, attackerSide.hero.name, targetStack.type, assistDmg, "统帅追击"));
                 if (outcome.killedCount > 0) {
                     result.logEvents.add(new BattleLogEvent(Type.KILL, attackerSide.hero.name, targetStack.type, outcome.killedCount, "Assist Kill"));
                 }
            }
        }
    }
    
    private void applyHeroDamage(String attackerName, Hero target, int dmg, TurnResult result, BattleState state, Side defenderSide) {
        // 1. Status Check: Immune
        if (isImmune(state, defenderSide, "Hero")) {
            result.logEvents.add(new BattleLogEvent(Type.ATTACK, attackerName, "Hero", 0, "Blocked (Immune)"));
            return;
        }

        double mult = 1.0;
        Side attackerSide = (defenderSide == state.sideA) ? state.sideB : state.sideA;
        
        // 2. Passive: Gender (Opposite Gender Dmg Up)
        if (hasPassive(attackerSide, BattleConstants.PASSIVE_DamageUpVsOppositeGender)) {
             if (isOppositeGender(attackerSide.hero, defenderSide.hero)) {
                 mult *= BattleConstants.MULT_GENDER_ADVANTAGE;
             }
        }
        
        // 3. Passive: Tanky (Assuming dynamic reduction here, though doc says Init. V1 Safe implementation)
        if (hasPassive(defenderSide, BattleConstants.PASSIVE_Tanky)) {
             mult *= 0.8;
        }
        
        // 4. Status: Vulnerable
        if (hasStatus(state, defenderSide, "Hero", StatusEffect.StatusType.VULNERABLE)) {
             mult *= BattleConstants.MULT_VULNERABLE;
        }

        int finalDmg = (int)(dmg * mult);
        target.hp -= finalDmg;
        result.logEvents.add(new BattleLogEvent(Type.ATTACK, attackerName, "Hero", finalDmg, "Target HP: " + target.hp));
        
        // 5. Passive: Reflect
        if (hasPassive(defenderSide, BattleConstants.PASSIVE_ReflectPercent)) {
             int reflect = (int)(finalDmg * BattleConstants.PCT_REFLECT);
             if (reflect > 0) {
                 attackerSide.hero.hp -= reflect;
                 result.logEvents.add(new BattleLogEvent(Type.REFLECT_DAMAGE, defenderSide.hero.name, attackerName, reflect, "Reflected"));
             }
        }

        if (target.hp <= 0) {
            target.isDeadOrRetreated = true;
            result.logEvents.add(new BattleLogEvent(Type.HERO_DOWN, attackerName, target.name, 0, "Dead"));
        } 
        else if (target.hp <= target.maxHp * 0.1) {
             result.logEvents.add(new BattleLogEvent(Type.HERO_PERSONALITY_TRIGGERED, target.name, "Personality", 0, "Low HP"));
             target.isDeadOrRetreated = true; 
             result.logEvents.add(new BattleLogEvent(Type.HERO_DOWN, attackerName, target.name, 0, "Retreated"));
        }
    }

    private void checkBattleEnd(BattleState state, TurnResult result) {
        boolean sideADead = !state.sideA.hero.isAlive(); 
        boolean sideBDead = !state.sideB.hero.isAlive();
        
        if (sideADead || sideBDead) {
            state.isFinished = true;
            state.isWin = sideBDead && !sideADead; 
            result.logEvents.add(new BattleLogEvent(Type.BATTLE_END, "SYSTEM", null, 0, state.isWin ? "WIN" : "LOSE"));
        }
    }

    // --- Helpers ---

    private Side getEnemySide(BattleState state, Side mySide) {
        return mySide == state.sideA ? state.sideB : state.sideA;
    }

    // 核心：行动队列计算
    private List<Actor> determineActionQueue(BattleState state) {
        List<Actor> queue = new ArrayList<>();
        
        // 1. Heroes
        queue.add(new Actor(state.sideA, true, null));
        queue.add(new Actor(state.sideB, true, null));
        Collections.sort(queue, (a, b) -> b.side.hero.speed - a.side.hero.speed);
        
        // Passive: First Turn Priority
        // Check if turnNo suggests start of battle. 
        // Logic: Only applied if turnNo < InitialQueueSize (Approx).
        // Since we don't track Rounds, assume global turnNo=0 is first action.
        if (state.turnNo == 0) {
            boolean aFirst = hasPassive(state.sideA, BattleConstants.PASSIVE_FirstTurnPriority);
            boolean bFirst = hasPassive(state.sideB, BattleConstants.PASSIVE_FirstTurnPriority);
            
            if (aFirst && !bFirst) {
                Actor actorA = findActor(queue, state.sideA);
                if (actorA != null) { queue.remove(actorA); queue.add(0, actorA); }
            } else if (bFirst && !aFirst) {
                Actor actorB = findActor(queue, state.sideB);
                if (actorB != null) { queue.remove(actorB); queue.add(0, actorB); }
            }
        }
        
        // 2. Troops (Sort by Elite priority or just add all)
        // Strategy: Iterate all troops of both sides, add to queue.
        // We want specific order: ARC(A/B) -> INF(A/B) -> CAV(A/B) ?
        // Or just let them act based on some speed? Troops don't have speed in config yet.
        // Let's stick to type order: ARC first, then INF, then CAV.
        
        addAllTroopsByType(queue, state, "ARC");
        addAllTroopsByType(queue, state, "INF");
        addAllTroopsByType(queue, state, "CAV");
        
        return queue;
    }

    private void addAllTroopsByType(List<Actor> queue, BattleState state, String type) {
        // Side A
        for (TroopStack s : state.sideA.troops) {
            if (s.type.equalsIgnoreCase(type) && s.count > 0) {
                queue.add(new Actor(state.sideA, false, s));
            }
        }
        // Side B
        for (TroopStack s : state.sideB.troops) {
            if (s.type.equalsIgnoreCase(type) && s.count > 0) {
                queue.add(new Actor(state.sideB, false, s));
            }
        }
    }

    private boolean hasPassive(Side side, String passive) {
        return side.hero != null && side.hero.passives != null && side.hero.passives.contains(passive);
    }
    
    private Actor findActor(List<Actor> queue, Side side) {
        for (Actor a : queue) {
            if (a.isHero && a.side == side) return a;
        }
        return null;
    }

    private TroopStack findStack(Side side, String type) {
        for (TroopStack s : side.troops) {
            if (s.type.equalsIgnoreCase(type)) return s;
        }
        return null; 
    }

    private List<TroopStack> getTargetTroopsByPriority(Side side) {
        List<TroopStack> list = new ArrayList<>();
        String[] priorities = {"ELITE", "ARC", "CAV", "INF"};
        for (String p : priorities) {
            TroopStack s = findStack(side, p);
            if (s != null) list.add(s);
        }
        return list;
    }

    private double computeTypeMultiplier(String atkType, String defType) {
        if (atkType.equalsIgnoreCase("INF")) {
            if (defType.equalsIgnoreCase("ARC")) return 1.2;
            if (defType.equalsIgnoreCase("CAV")) return 0.8;
        } else if (atkType.equalsIgnoreCase("ARC")) {
            if (defType.equalsIgnoreCase("CAV")) return 1.2;
            if (defType.equalsIgnoreCase("INF")) return 0.8;
        } else if (atkType.equalsIgnoreCase("CAV")) {
            if (defType.equalsIgnoreCase("INF")) return 1.2;
            if (defType.equalsIgnoreCase("ARC")) return 0.8;
        }
        return 1.0;
    }
    
    private String getCounterTargetType(String type) {
        if (type.equalsIgnoreCase("INF")) return "ARC";
        if (type.equalsIgnoreCase("ARC")) return "CAV";
        if (type.equalsIgnoreCase("CAV")) return "INF";
        return "INF";
    }

    private boolean isImmune(BattleState state, Side side, String key) {
        Map<String, List<StatusEffect>> map = (side == state.sideA) ? state.statusesA : state.statusesB;
        if (!map.containsKey(key)) return false;
        return map.get(key).stream().anyMatch(s -> s.type == StatusEffect.StatusType.IMMUNE);
    }
    
    // Status / Passive Helpers
    
    private boolean hasStatus(BattleState state, Side side, String key, StatusEffect.StatusType type) {
        Map<String, List<StatusEffect>> map = (side == state.sideA) ? state.statusesA : state.statusesB;
        if (!map.containsKey(key)) return false;
        return map.get(key).stream().anyMatch(s -> s.type == type);
    }

    private void consumeStun(BattleState state, Actor actor) {
        if (!actor.isHero) return;
        Map<String, List<StatusEffect>> map = (actor.side == state.sideA) ? state.statusesA : state.statusesB;
        List<StatusEffect> list = map.get("Hero");
        if (list != null) {
            list.removeIf(e -> {
                 if (e.type == StatusEffect.StatusType.STUN) {
                     e.remainingTurns--;
                     return e.remainingTurns <= 0;
                 }
                 return false;
            });
        }
    }

    private boolean isOppositeGender(Hero h1, Hero h2) {
        if (h1.gender == null || h2.gender == null) return false;
        return !h1.gender.equals(h2.gender) && !h1.gender.equals("U") && !h2.gender.equals("U");
    } 

    private static class Actor {
        Side side;
        boolean isHero;
        TroopStack troop;
        
        public Actor(Side s, boolean h, TroopStack t) {
            this.side = s; this.isHero = h; this.troop = t;
        }
    }
}
