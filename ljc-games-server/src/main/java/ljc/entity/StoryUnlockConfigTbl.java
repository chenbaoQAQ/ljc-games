package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StoryUnlockConfigTbl {
    private String civ;
    private Integer stageNo;
    private Integer unlockGeneralTemplateId;
    private String unlockNextCiv; // 通关解锁国家 (nullable)
    private Integer unlockTroopId; // 通关解锁兵种 ID (nullable)
    private Integer unlockEvolutionTroopId; // 通关解锁兵种进化 ID (nullable)
    private LocalDateTime createdAt;
}
