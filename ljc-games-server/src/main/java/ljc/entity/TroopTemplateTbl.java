package ljc.entity;

import lombok.Data;

@Data
public class TroopTemplateTbl {
    private Integer troopId;    // 比如 1001
    private String name;        // 义勇兵
    private String troopType;   // INF (步兵)
    private Integer cost;       // 占用人口 (1)
    private Long baseAtk;       // 攻击力 (10)
    private Long baseHp;        // 生命值 (50)
}