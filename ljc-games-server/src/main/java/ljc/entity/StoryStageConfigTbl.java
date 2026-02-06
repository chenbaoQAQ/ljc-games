package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StoryStageConfigTbl {
    private Long id;
    private String civ;
    private Integer stageNo;
    private Integer staminaCost;
    private String stageType;        // NORMAL/WALL/BOSS
    private Integer wallCostTroops;  // for WALL type
    private Integer enemyMultiplier; // default 1000
    private String enemyConfigJson; // JSON: {hero:..., troops:[...]}
    private Integer dropPoolId;
    private LocalDateTime createdAt;
}
