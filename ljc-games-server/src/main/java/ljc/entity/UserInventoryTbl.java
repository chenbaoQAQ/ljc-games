package ljc.entity;

import lombok.Data;

@Data
public class UserInventoryTbl {
    private Long id;
    private Long userId;
    private Integer itemId;
    private Integer count;
}
