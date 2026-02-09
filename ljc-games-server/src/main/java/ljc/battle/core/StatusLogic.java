package ljc.battle.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Helper to manipulate state statuses within Engine main loop
public class StatusLogic {

    // 2) 结算时序: PHASE_START
    // 处理 DoT / HoT
    public static void processPhaseStart(BattleState state, BattleLogEvent.Type phaseType, List<BattleLogEvent> logs) {
        // Only run once per action? Or "Round Start"? 
        // Doc 2) 1. "回合/行动开始". "每个 /battle/turn 推进一次行动".
        // If we do it every action, DoT/HoT ticks too fast?
        // Doc says: "回合开始统一结算 DoT/HoT 建议". But turns areactions.
        // Usually HoT/DoT ticks "On Round Start" or "On Actor Turn Start".
        // V1 Requirement 2): "PHASE_START：回合/行动开始，先结算 DoT/HoT".
        // Let's assume it means "Before THIS actor acts, handle THIS actor's DoT/HoT"?
        // Or handle global DoT/HoT every round?
        // Let's implement: "At start of Round (actorIndex == 0), process ALL statuses."
        // Or "Before actor acts, process actor's statuses." (Standard turn-based).
        // Let's stick to "Before generic action resolution".
        
        // V1 Decision: Process ALL statuses at start of ROUND (index=0).
        if (state.currentActorIndex == 0) {
            processSideStatuses(state, state.sideA, logs);
            processSideStatuses(state, state.sideB, logs);
        }
    }

    private static void processSideStatuses(BattleState state, BattleState.Side side, List<BattleLogEvent> logs) {
        Map<String, List<StatusEffect>> map = (side == state.sideA) ? state.statusesA : state.statusesB;
        String sideName = (side == state.sideA) ? "SideA" : "SideB";
        
        for (Map.Entry<String, List<StatusEffect>> entry : map.entrySet()) {
            String targetId = entry.getKey();
            List<StatusEffect> effects = entry.getValue();
            
            Iterator<StatusEffect> it = effects.iterator();
            while(it.hasNext()) {
                StatusEffect ef = it.next();
                
                // Tick Logic (Only tick once per round)
                // Assuming this is called once per round.
                
                if (ef.type == StatusEffect.StatusType.POISON) {
                    // Apply DMG
                    if (targetId.equals("Hero")) {
                        side.hero.hp -= BattleConstants.VAL_POISON_DMG;
                        logs.add(new BattleLogEvent(BattleLogEvent.Type.POISON_TICK, "SYSTEM", sideName + ".Hero", BattleConstants.VAL_POISON_DMG, "Poison Dmg"));
                    }
                } else if (ef.type == StatusEffect.StatusType.HOT) {
                    // Apply Heal
                    if (targetId.equals("Hero")) {
                         side.hero.hp = Math.min(side.hero.maxHp, side.hero.hp + BattleConstants.VAL_HOT_HEAL);
                         logs.add(new BattleLogEvent(BattleLogEvent.Type.HEAL, "SYSTEM", sideName + ".Hero", BattleConstants.VAL_HOT_HEAL, "HoT Heal"));
                    }
                }
                
                // Decrement happens at End of Turn or End of Round?
                // Doc 8: "行动结束，减少 BUFF/DEBUFF".
                // We handle decrement in `processPhaseEnd`.
            }
        }
    }
    
    public static void processPhaseEnd(BattleState state) {
        // Decrease duration at end of round? Or end of Actor turn?
        // If Stun(1 turn), it should skip 1 turn then expire.
        // If we decrease at end of round, and Stun was applied in middle of round?
        // Standard: Duration = Number of Rounds.
        
        if (state.currentActorIndex == 0) {
             // New Round Start -> Means previous round ended? NO.
             // We are at start of new round.
             // Let's decrease at end of round logic in Engine.
        }
    }
    
    public static void decreaseDurations(BattleState state) {
        decreaseSideDurations(state.statusesA);
        decreaseSideDurations(state.statusesB);
    }
    
    private static void decreaseSideDurations(Map<String, List<StatusEffect>> map) {
        for (List<StatusEffect> list : map.values()) {
            list.removeIf(e -> {
                // STUN consumed on trigger, not round end
                if (e.type == StatusEffect.StatusType.STUN) return false;
                
                e.remainingTurns--;
                return e.remainingTurns <= 0;
            });
        }
    }
}
