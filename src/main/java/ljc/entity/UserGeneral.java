package ljc.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_general")
public class UserGeneral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId; // 所属玩家ID

    @Column(name = "template_id")
    private Integer templateId; // 关联的武将模版ID（比如关联到赵云）

    @Column(name = "personality")
    private String personality; // 性格（影响战损的关键字段）

    @Column(name = "level")
    private Integer level = 1; // 等级

    @Column(name = "current_exp")
    private Integer currentExp = 0; // 当前经验值

    // UserGeneral.java 内部
    @ManyToOne
    @JoinColumn(name = "template_id", insertable = false, updatable = false)
    private GeneralTemplate template; // 这一行就是那根“连接线”
}