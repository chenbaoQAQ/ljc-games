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
    private Integer templateId;

    private String name;
    private String personality; // BRAVE, RASH, CALM, CAUTIOUS

    // 基础数值 (养成基数)
    private int baseAtk = 50;
    private int baseHp = 1000;

    // 增加最大统帅值字段，用于记录该武将当前的带兵上限
    private int maxLeadership = 100;

    // 实时状态
    private int maxHp = 1000;
    private int currentHp = 1000;
    private int currentArmyCount;  // 逻辑总兵力
    private String status = "HEALTHY"; // HEALTHY, WOUNDED, KILLED
    private int level = 1;
    private int currentExp = 0;


    // 阵前兵力：战斗时实际带走的
    @Column(columnDefinition = "TEXT")
    private String armyConfigStr = "{}";

    @Column(columnDefinition = "TEXT")
    private String reserveArmyConfigStr = "{}";
}
