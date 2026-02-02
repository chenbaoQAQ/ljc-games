package ljc.entity; // 注意包名要和你截图里的一致

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户表实体
 * 对应数据库表: users
 */
@Data
public class UserTbl {
    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private Long gold;
    private Long diamond;
    private Integer stamina;
    private String initialCiv;
    private LocalDateTime createdAt;
}