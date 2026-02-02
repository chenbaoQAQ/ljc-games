package ljc.entity;

import lombok.Data;
//国家进度
@Data
public class UserCivProgressTbl {
    private Long userId;
    private String civ;           // CN, JP, KR, GB
    private Boolean unlocked;
    private Integer maxStageCleared;
}