package ljc.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserGemTbl {
    private Long id;
    private Long userId;
    private String gemType;
    private Integer gemLevel;
    private Long statValue;
    private Boolean isUsed;
    private LocalDateTime createdAt;
}
