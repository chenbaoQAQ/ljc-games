package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserEquipmentTbl {
    private Long id;
    private Long userId;
    private Integer templateId;
    private Integer enhanceLevel;
    private Long socket1GemId;
    private Long socket2GemId;
    private Boolean isEquipped;
    private Boolean isLocked;
    private LocalDateTime createdAt;
}
