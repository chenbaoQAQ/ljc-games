package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BattleSessionTbl {
    private Long id;
    private Long userId;        // 玩家ID (Unique)
    private Long battleId;      // 战斗会话ID (对外暴露)
    private Integer dungeonId;  // 关卡ID
    private Integer status;     // 0:进行中, 1:胜利, 2:失败
    private Integer currentTurn;// 当前回合数 (1-20)
    private String contextJson; // 完整战场快照 (JSON)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
