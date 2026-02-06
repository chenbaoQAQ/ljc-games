package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 战斗会话实体
 * 对应数据库表: battle_sessions
 */
@Data
public class BattleSessionsTbl {
    private Long userId;
    private Long battleId;
    private String status;
    private String mode;
    private String civ;
    private Integer stageNo;
    private Integer towerFloor;
    private Long generalId;
    private Long seed;
    private Integer currentTurn;
    private Integer maxTurn;
    private String contextJson;
    private String stateJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
