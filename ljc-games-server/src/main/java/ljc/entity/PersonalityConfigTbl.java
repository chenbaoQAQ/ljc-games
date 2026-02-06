package ljc.entity;

import lombok.Data;

@Data
public class PersonalityConfigTbl {
    private String personalityCode;
    private String displayName;
    private Integer dealMult;
    private Integer takenMult;
    private Integer rescueRateBonus;
    private Integer lastStandBias; // >0死战, <0撤退
    private Integer rollBias;
    private Integer rollVarianceScale;
    private String note;
}
