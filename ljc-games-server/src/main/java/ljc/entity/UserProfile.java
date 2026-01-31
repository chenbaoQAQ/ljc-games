package ljc.entity;

import lombok.Data;

@Data
public class UserProfile {
    private Long userId;

    // 基础货币
    private Long gold;
    private Long diamond;   // 预留

    // v2.2 新增：Steam版核心资源
    private Long material;  // 强化材料
    private Long blueprint; // 装备图纸

    // 进度与限制
    private Integer stamina;    // 体力
    private Integer towerFloor; // 爬塔层数 (1-100)
    private String country;     // 所属国家 (CN/JP/KR/GB)
}