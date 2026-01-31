package ljc.entity;

import lombok.Data;

@Data
public class UserItem {
    private Integer id;           // 物品唯一流水号
    private Long userId;
    private Integer templateId;   // 物品配置ID
    private String type;          // "WEAPON", "MATERIAL", "BLUEPRINT"
    private Integer count;        // 数量 (装备通常是1)

    // 装备特有属性
    private String slot;          // "WEAPON", "ARMOR"...
    private Integer enhanceLevel; // 强化等级 (+0 ~ +8)
    private Boolean isEquipped;   // 是否穿戴中
    private Integer equippedGeneralId; // 穿在哪个武将身上
}