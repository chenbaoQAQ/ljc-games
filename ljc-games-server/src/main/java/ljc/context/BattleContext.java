package ljc.context;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleContext {
    private Long battleId;
    private Long randomSeed; // For deterministic replay
    
    // 战斗进度
    private Integer currentTurn;
    private boolean finished;
    private boolean win;
    
    // Environment
    private Integer turnLimit = 20;
    private List<String> logs = new ArrayList<>();
    
    // Models
    private SideContext ally;
    private SideContext enemy;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SideContext {
        private HeroState hero;
        private List<TroopStack> troops; // Map or List? List is ordered (ARC, INF, CAV, ELITE)
        private List<Integer> eliteTypes; 
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeroState {
        private Long generalId;
        private String name;
        private Long maxHp;
        private Long currentHp;
        private Integer speed;
        private String personality; // 狂战/冷静/etc
        private boolean isRetreated;
        private boolean isDead;
        private Integer skillCd;
        // Stats
        private Long atk;
        // ... other stats
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TroopStack {
        private Integer troopId;
        private String type; // INF, ARC, CAV
        private Long count;
        private Long unitHp; 
        private Long frontHp; 
        
        // Helper to check if alive
        public boolean isAlive() {
            return count > 0;
        }
    }
}
