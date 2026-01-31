package ljc.entity;

import lombok.Data;

@Data
public class UserTroop {
    private Long userId;

    // v2.2 变更：直接使用 String 类型区分 (INFANTRY, ARCHER...)
    // 对应数据库的 type VARCHAR(16)
    private String type;

    private Long count;
}