package ljc.entity;

import lombok.Data;
// 兵种进化配置
@Data
public class TroopEvolutionConfigTbl {
    private Long id;
    private Integer troopId;
    private Integer nextTier;
    
    private String requiredCiv;
    private Integer requiredStageNo;
    
    private Long costGold;
    private String costItemsJson;
    
    private String statModifiersJson;
}
