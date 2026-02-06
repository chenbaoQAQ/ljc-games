package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EquipmentTemplateTbl {
    private Integer templateId;
    private String slot;
    private String name;
    private Long baseAtk;
    private Long baseHp;
    private Long baseSpd;
    private Integer baseCapacity;
    private LocalDateTime createdAt;
}
