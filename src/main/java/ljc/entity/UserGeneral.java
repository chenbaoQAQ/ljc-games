package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "user_general")
/**
 * 简化后的武将：删除了复杂的技能系统和防御字段
 * 专注核心：HP、ATK、性格
 */
public class UserGeneral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;
    private Integer templateId;

    private String name;
    private String personality; // BRAVE, RASH, CALM, CAUTIOUS

    private int baseAtk = 50;
    private int baseHp = 1000;

    private int maxHp = 1000;
    private int currentHp = 1000;
    private int currentArmyCount;
    private String status = "HEALTHY";

    private int level = 1;
    private int currentExp = 0;

    @Column(columnDefinition = "TEXT")
    private String armyConfigStr = "INFANTRY:100";
}