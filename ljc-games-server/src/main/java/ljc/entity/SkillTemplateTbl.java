package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SkillTemplateTbl {
    private Integer skillId;
    private String name;
    private String description;
    private String skillType;
    private Integer cooldownTurns;
    private String triggerTiming;
    private String effectJson;
    private LocalDateTime createdAt;
}
