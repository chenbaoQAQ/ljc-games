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
    // 装备槽暂时省略，后续需要再加
    private LocalDateTime createdAt;
}