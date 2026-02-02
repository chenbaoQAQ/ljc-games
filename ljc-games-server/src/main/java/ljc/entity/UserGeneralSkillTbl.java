package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;
//武将技能关联
@Data
public class UserGeneralSkillTbl {
    private Long generalId;       // 对应 UserGeneralTbl.id
    private Integer currentSkillId;
    private LocalDateTime updatedAt;
}