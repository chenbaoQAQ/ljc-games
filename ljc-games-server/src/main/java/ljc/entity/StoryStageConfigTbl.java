package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StoryStageConfigTbl {
    private Long id;
    private String civ;
    private Integer stageNo;
    private Integer staminaCost;
    private String enemyConfigJson; // JSON: {hero:..., troops:[...]}
    private LocalDateTime createdAt;
}
