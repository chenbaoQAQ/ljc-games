package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "user_general")
public class UserGeneral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;
    private Integer templateId; // 关联武将模版（如：赵云、关羽）

    private String name;        // 武将姓名
    private String personality; // 性格：BRAVE, CAUTIOUS, RASH, CALM

    private Integer currentHp;  // 武将当前血量（判定受伤/阵亡）
    private Integer maxHp;

    // 状态枚举：HEALTHY(健康), WOUNDED(受伤), KILLED(阵亡)
    private String status = "HEALTHY";
}