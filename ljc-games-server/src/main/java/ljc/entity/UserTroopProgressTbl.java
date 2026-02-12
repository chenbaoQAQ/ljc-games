package ljc.entity;

import lombok.Data;
// 用户兵种解锁与进化进度
@Data
public class UserTroopProgressTbl {
    private Long userId;
    private Integer troopId;
    
    // 0=LOCKED(未发现), 1=DISCOVERED(发现/可解锁), 2=UNLOCKED(已解锁/可招募)
    private Integer status;
    
    // 0=初始, 1=一阶, ...
    private Integer evolutionTier;
    
    // 0=未解锁进化, 1=已解锁
    private Byte evolutionUnlocked;
    
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
