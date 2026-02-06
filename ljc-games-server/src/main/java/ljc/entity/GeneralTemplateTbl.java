package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;
//读取初始武将（如曹操）的默认属性
@Data
public class GeneralTemplateTbl {
    private Integer templateId;
    private String civ;
    private String name;
    private Long baseAtk;
    private Long baseHp;
    private Integer baseCapacity;
    private Integer speed;
    private String personalityCode;
    private Long activateGoldCost;
    private Integer maxLevelTier0;
    private Integer defaultSkillId; // 初始技能ID
    private LocalDateTime createdAt;
}