package ljc.controller.dto;

import ljc.entity.UserGeneralTbl;
import ljc.entity.UserTbl;
import ljc.entity.UserTroopTbl;
import lombok.Data;
import java.util.List;

@Data
public class PlayerInfoResp {
    // 1. 玩家本体信息
    private UserTbl userInfo;

    // 2. 玩家拥有的武将列表 (List代表可能有多个)
    private List<UserGeneralTbl> generals;

    // 3. 玩家拥有的兵力列表
    private List<UserTroopTbl> troops;
}