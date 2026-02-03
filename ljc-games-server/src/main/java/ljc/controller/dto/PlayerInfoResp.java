package ljc.controller.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlayerInfoResp {
    // 1. 基础信息 (把 UserTbl 里的东西拆出来放)
    private Long id;
    private String nickname;
    private Long gold;      // 注意：UserTbl里是 Long
    private Long diamond;   // 注意：UserTbl里是 Long

    // 2. 兵力列表 (只给前端看 ID 和 数量)
    private List<TroopDto> troops;

    // 内部类：简单的兵力展示对象
    @Data
    public static class TroopDto {
        private Integer troopId; // 对应 UserTroopTbl.troopId
        private Long count;      // 对应 UserTroopTbl.count
    }
}