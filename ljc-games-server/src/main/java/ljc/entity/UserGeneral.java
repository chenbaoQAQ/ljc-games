package ljc.entity;

import lombok.Data;

@Data
public class UserGeneral {
    private Integer id;           // 唯一流水号
    private Long userId;
    private Integer templateId;   // 关联模版ID (101=赵云)

    // 成长属性
    private Integer level;
    private Long exp;
    private Integer ascension;    // 升阶次数 (0或1)

    // 战斗属性
    private Integer currentHp;
    private Integer maxHp;
    private Integer atk;
    private Integer capacity;     // 统帅上限 (能带多少兵)

    // v2.2 核心机制字段
    private String status;        // "HEALTHY", "RESTING", "DEAD"
    private Boolean isActive;     // true=已招募可用, false=仅解锁未招募
    private String personality;   // "NONE", "RETREAT", "LAST_STAND"
}