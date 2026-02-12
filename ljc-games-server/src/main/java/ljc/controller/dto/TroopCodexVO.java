package ljc.controller.dto;

import lombok.Data;

@Data
public class TroopCodexVO {
    private Integer troopId;
    private String name;
    private String civ;
    private String type; // INF/ARC/CAV
    private Boolean isElite;
    
    // Status
    private Integer status; // 0=LOCKED, 1=DISCOVERED, 2=UNLOCKED
    private Integer evolutionTier;
    
    // Requirements (If locked)
    private String unlockCiv; // e.g. "CN" if unlocked by CN stage
    private Integer unlockStageNo;
    private String unlockHint;
    
    // Evolution
    private Boolean evolutionUnlocked;
    
    // Stats
    private Long baseAtk;
    private Long baseHp;
    private Integer cost;
}
