package ljc.entity;

import lombok.Data;

@Data
public class UserGeneral {
    private Integer id;           // 自增主键
    private Integer userId;
    private Integer templateId;   // 关联模版 (101=赵云)

    private String name;
    private String status;        // HEALTHY, WOUNDED

    // 战斗属性
    private Integer maxHp;
    private Integer currentHp;

    //TODO: 省略其他字段，先跑通核心流程...
}