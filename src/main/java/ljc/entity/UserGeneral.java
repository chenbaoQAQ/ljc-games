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

    // 实时状态
    private int maxHp = 1000;
    private int currentHp = 1000;
    private int currentArmyCount;  // 逻辑总兵力
    private String status = "HEALTHY"; // HEALTHY, WOUNDED, KILLED

    private int level = 1;
    private int currentExp = 0;

    // 💡 核心：JSON 存储，如 {"INFANTRY":100, "EN_SPECIAL":10}
    @Column(columnDefinition = "TEXT")
    private String armyConfigStr = "{}";
}
