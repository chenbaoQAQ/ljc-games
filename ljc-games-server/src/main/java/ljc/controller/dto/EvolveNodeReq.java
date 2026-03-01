package ljc.controller.dto;

import lombok.Data;

@Data
public class EvolveNodeReq {
    private Long fromNodeId;
    private Long toNodeId;
}
