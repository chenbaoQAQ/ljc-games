package ljc.controller.dto;

import lombok.Data;

@Data
public class RecruitReq {
    // 买哪个兵？(ID)
    private Long troopId;

    // 买多少个？
    private Integer count;
}