package ljc.entity;

import lombok.Data;

@Data
public class TroopTemplateTbl {
    private Integer troopId;    // 兵种ID
    private String civ;         // 所属国家
    private String name;        // 兵种名称
    private String troopType;   // 兵种类型 (INF/ARC/CAV)
    private Boolean isElite;    // 是否为特种兵
    private Integer cost;       // 占用统帅值
    private Long baseAtk;       // 基础攻击
    private Long baseHp;        // 基础血量
    private Long recruitGoldCost; // 招募金币消耗
    private Boolean unlockCivRequired; // 是否需要国家解锁
}