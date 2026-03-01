package ljc.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TroopTreeNodeConfigTbl {
    private Long nodeId;
    private Integer troopId;
    private Long parentNodeId;
    private String civ;
    private Integer tier;
    private String unlockCiv;
    private Integer unlockStageNo;
    private Integer evolveCost;
    private Integer xPos;
    private Integer yPos;
    private Date createdAt;
}
