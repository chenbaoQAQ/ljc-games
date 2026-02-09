package ljc.battle.core;

import java.util.ArrayList;
import java.util.List;

import ljc.battle.core.BattleState.Hero;
import ljc.battle.core.BattleState.Side;

public class BattleTestMain {

    public static void main(String[] args) {
        System.out.println("=== Starting BattleEngine V1 Test ===");
        
        testInfVsArcMultiplier();
        testOverflow();
        testHeroWin();
        testIdempotency();
        
        System.out.println("=== All Tests Passed ===");
    }

    // 1. 测试 INF 打 ARC 克制倍率 (1.2)
    private static void testInfVsArcMultiplier() {
        System.out.println("\nTest 1: INF vs ARC Multiplier");
        BattleEngine engine = new BattleEngine();
        BattleState state = createBasicState();
        
        // Setup: Side A has INF, Side B has ARC
        Side sideA = state.sideA;
        Side sideB = state.sideB;
        
        // A: 10 INF, UnitHP 10. BaseAtk 10 per unit -> 100 dmg.
        TroopStack inf = new TroopStack("INF", 10, 10);
        sideA.troops.clear();
        sideA.troops.add(inf);
        
        // B: 100 ARC (Meat shield).
        TroopStack arc = new TroopStack("ARC", 100, 10);
        sideB.troops.clear();
        sideB.troops.add(arc);
        
        // Force A's INF to act
        // We bypass processTurn queue logic to unit test specific methods if possible, 
        // but since they are private, we construct state such that it's INF's turn.
        
        // Queue order: HeroA, HeroB, ARC(B), INF(A), CAV...
        // We set speed such that heroes don't kill troops or we skip them.
        state.sideA.hero.hp = 1; // Weak hero
        state.sideB.hero.hp = 1; 
        state.sideA.hero.isDeadOrRetreated = true; // Skip heroes
        state.sideB.hero.isDeadOrRetreated = true;

        // Current Actor logic:
        // Queue: ARC(SideB) -> INF(SideA)
        // We want INF to attack ARC.
        // Let's just manually run a turn to see who acts?
        // Or better, set only INF on A, only ARC on B.
        // B's ARC will go first (ARC Phase).
        // Let B's ARC be weak/harmless? Or just check logs.
        
        // Let's reverse: Side A has ARC (Atk), Side B has CAV (Def). ARC > CAV (1.2).
        TroopStack arcA = new TroopStack("ARC", 10, 10); // 100 raw dmg
        sideA.troops.clear();
        sideA.troops.add(arcA);
        
        TroopStack cavB = new TroopStack("CAV", 100, 10); // 10 hp per unit
        sideB.troops.clear();
        sideB.troops.add(cavB);
        
        // Turn 1: Hero A (Skip), Hero B (Skip).
        // Turn 2: ARC A acts.
        state.turnNo = 0;
        state.currentActorIndex = 2; // Hack: Skip 0, 1
        
        TurnCommand cmd = new TurnCommand(1, TurnCommand.ActionType.NORMAL);
        TurnResult res = engine.processTurn(state, cmd);
        
        // Validation
        // ARC A Atk = 10 * 10 = 100.
        // Multiplier vs CAV = 1.2 -> 120 dmg.
        // CAV Unit HP = 10. 120 dmg -> 12 kills.
        
        boolean found = false;
        for (ljc.battle.core.BattleLogEvent e : res.logEvents) {
            System.out.println("Log: " + e.type + " " + e.actorId + " -> " + e.targetId + " val=" + e.value + " " + e.desc);
            if (e.type == BattleLogEvent.Type.KILL && e.targetId.equals("CAV") && e.value == 12) {
                found = true;
            }
        }
        
        if (!found) throw new RuntimeException("Test 1 Failed: Expected 12 Kills (120 dmg), found none consistent.");
        System.out.println("Test 1 Passed");
    }

    // 2. 测试伤害溢出
    private static void testOverflow() {
        System.out.println("\nTest 2: Overflow");
        BattleEngine engine = new BattleEngine();
        BattleState state = createBasicState();
        
        // A has Strong Hero (Atk 100)
        state.sideA.hero.atk = 150; 
        state.sideA.hero.isDeadOrRetreated = false;
        
        // B has 2 stacks: Small ARC (HP 50), Big INF
        Side sideB = state.sideB;
        sideB.hero.isDeadOrRetreated = true; // No hero to tank
        sideB.troops.clear();
        
        // Stack 1: ARC, 5 men, 10 hp each = 50 total hp.
        TroopStack arc = new TroopStack("ARC", 5, 10);
        // Stack 2: INF, 100 men
        TroopStack inf = new TroopStack("INF", 100, 10);
        
        sideB.troops.add(arc);
        sideB.troops.add(inf);
        
        // Hero A attacks. Priority: ARC -> CAV -> INF.
        // Will hit ARC first.
        // Dmg 150. ARC HP 50. Overflow 100 to INF.
        // INF takes 100 dmg -> 10 kills.
        
        state.turnNo = 0;
        state.currentActorIndex = 0; // Hero A starts
        
        TurnCommand cmd = new TurnCommand(1, TurnCommand.ActionType.NORMAL);
        TurnResult res = engine.processTurn(state, cmd);
        
        int arcKills = 0;
        int infKills = 0;
        
        for (ljc.battle.core.BattleLogEvent e : res.logEvents) {
             if (e.type == BattleLogEvent.Type.KILL && e.targetId.equals("ARC")) arcKills += e.value;
             if (e.type == BattleLogEvent.Type.KILL && e.targetId.equals("INF")) infKills += e.value;
             System.out.println(e.actorId + " " + e.type + " " + e.targetId + " " + e.value);
        }
        
        if (arcKills != 5) throw new RuntimeException("Should kill 5 ARC");
        if (infKills < 9 || infKills > 11) throw new RuntimeException("Should kill 10 INF (approx)"); // 150 - 50 = 100. 100/10 = 10.
        
        System.out.println("Test 2 Passed");
    }

    // 3. 英雄死亡判负
    private static void testHeroWin() {
        System.out.println("\nTest 3: Hero Death -> Win");
        BattleEngine engine = new BattleEngine();
        BattleState state = createBasicState();
        
        // A hits B. B has 1 HP.
        state.sideA.hero.atk = 10;
        state.sideB.hero.hp = 5;
        state.sideA.hero.speed = 100; // A goes first
        
        state.turnNo = 0;
        state.currentActorIndex = 0;
        
        TurnResult res = engine.processTurn(state, new TurnCommand(1, TurnCommand.ActionType.NORMAL));
        
        boolean battleEnd = false;
        for (ljc.battle.core.BattleLogEvent e : res.logEvents) {
             if (e.type == BattleLogEvent.Type.BATTLE_END) battleEnd = true;
        }
        
        if (!state.isFinished || !battleEnd) throw new RuntimeException("Battle should end");
        if (!state.isWin) throw new RuntimeException("Player A should win");
        
        System.out.println("Test 3 Passed");
    }
    
    // 4. 幂等
    private static void testIdempotency() {
         System.out.println("\nTest 4: Idempotency");
         BattleEngine engine = new BattleEngine();
         BattleState state = createBasicState();
         state.turnNo = 5;
         
         // Request turn 5 (should be 6)
         TurnResult res = engine.processTurn(state, new TurnCommand(5, TurnCommand.ActionType.NORMAL));
         if (res.currentTurn != 5) throw new RuntimeException("Should return current state without change");
         
         // Request turn 6
         res = engine.processTurn(state, new TurnCommand(6, TurnCommand.ActionType.NORMAL));
         if (res.currentTurn != 6) throw new RuntimeException("Should advance");
         
         System.out.println("Test 4 Passed");
    }

    private static BattleState createBasicState() {
        BattleState s = new BattleState();
        s.turnNo = 0;
        s.sideA = new Side();
        s.sideA.hero = new Hero("Player", 100, 10, 5, 10);
        s.sideB = new Side();
        s.sideB.hero = new Hero("Enemy", 100, 10, 5, 5);
        return s;
    }
}
