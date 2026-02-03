package ljc.controller.dto;

import lombok.Data;
import java.util.Map;

@Data
public class StartBattleReq {
    // 1. 派谁去打？ (武将ID)
    private Long generalId;

    // 2. 打哪一关？ (关卡ID，比如 1)
    private Integer stageId;

    // 3. 带多少兵？ (关键！)
    // Map<兵种ID, 数量> -> 比如 {1001: 10, 2001: 5}
    // 这样设计是为了支持以后同时带步兵、弓兵、骑兵
    private Map<Integer, Integer> troopConfig;
}