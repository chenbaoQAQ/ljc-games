package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SkillLearnLogTbl {
    private Long id;
    private Long userId;
    private Long generalId;
    private Integer oldSkillId;
    private Integer newSkillId;
    private Integer bookItemId;
    private LocalDateTime createdAt;
}
