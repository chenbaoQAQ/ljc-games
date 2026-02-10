package ljc.dto;

import lombok.Data;
import ljc.entity.UserEquipmentTbl;
import java.util.List;

@Data
public class UserGeneralVO {
    private Long id;
    private Long userId;
    private Integer templateId;
    private String name;
    private Integer level;
    private Integer tier;
    private Boolean unlocked;
    private Boolean activated;
    private Integer maxHp;
    private Integer currentHp;
    private Integer capacity;
    
    // Calculated Stat
    private Integer atk;
    
    // Skill Info
    private Integer currentSkillId;
    private String skillName;
    private String skillDesc;
    
    // Equipment Info (Frontend uses separate API, but maybe good to include here?)
    // Or just let frontend query equipments separately.
    // The user wants to see stats.
}
