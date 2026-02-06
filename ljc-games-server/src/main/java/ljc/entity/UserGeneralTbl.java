package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;
//武将实例
@Data
public class UserGeneralTbl {
    private Long id;              // 自增主键
    private Long userId;
    private Integer templateId;
    private Boolean unlocked;
    private Boolean activated;
    private Integer level;
    private Integer tier;
    private Long currentHp;
    private Long maxHp;
    private Integer restTurns;
    private Integer capacity;
    
    private Long equipWeaponId;
    private Long equipArmor1Id;
    private Long equipArmor2Id;
    private Long equipShoesId;
    private Long equipFlagId;
    private Long equipTalismanId;

    private LocalDateTime createdAt;
}