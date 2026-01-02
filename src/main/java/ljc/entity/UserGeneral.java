package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "user_general")
/**
 * 玩家武将实体：
 * 1. 存储武将的养成等级、性格和实时战斗状态。
 * 2. baseAtk 字段为单挑伤害的基础基数。
 */
public class UserGeneral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;
    private Integer templateId; // 关联武将模版

    private String name;
    private String personality; // BRAVE, RASH, CALM, CAUTIOUS

    // --- 基础数值 (养成基数，CombatEngine 依赖这些字段) ---
    private int baseAtk = 50;      // 基础攻击力，默认为 50
    private int baseHp = 1000;     // 基础血量上限，默认为 1000

    // --- 实时状态 (战斗属性) ---
    private int maxHp = 1000;      // 当前等级/装备后的最大血量
    private int currentHp = 1000;  // 剩余血量
    private int currentArmyCount;  // 逻辑带兵数
    private String status = "HEALTHY"; // 状态：HEALTHY, WOUNDED, KILLED

    // --- 养成属性 ---
    private int level = 1;
    private int currentExp = 0;

    // 持久化存储兵力配置的字符串（例如：INFANTRY:100,ARCHER:50）
    @Column(columnDefinition = "TEXT")
    private String armyConfigStr;
}