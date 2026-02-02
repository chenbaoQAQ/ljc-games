package ljc.entity;

import lombok.Data;
//兵力库存
@Data
public class UserTroopTbl {
    private Long userId;
    private Integer troopId;
    private Long count;
}