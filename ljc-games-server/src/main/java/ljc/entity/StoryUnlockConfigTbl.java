package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StoryUnlockConfigTbl {
    private String civ;
    private Integer stageNo;
    private Integer unlockGeneralTemplateId;
    private String unlockNextCiv;
    private LocalDateTime createdAt;
}
