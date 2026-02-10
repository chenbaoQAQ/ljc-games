package ljc.dto;

import lombok.Data;

@Data
public class UserEquipmentVO {
    private Long id;
    private Long userId;
    private Integer templateId;
    private String name; // From template
    private String slot; // From template
    private Integer enhanceLevel;
    private Long generalId; // If equipped
    private Boolean isLocked;
    
    // Sockets
    private Long socket1GemId;
    private Long socket2GemId;
    
    // Optional: Stats for display?
    private Long baseAtk;
    private Long baseHp;
}
