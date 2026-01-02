package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "user_general")
/**
 * 武将实体：
 * 1. 增加了基础属性，用于装备百分比加成的基数。
 * 2. 增加了技能系统字段。
 * 3. 增加了兵力配置存储，用于持久化玩家的带兵情况。
 */
public class UserGeneral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;
    private Integer templateId;

    private String name;
    private String personality; // BRAVE, RASH, CALM, CAUTIOUS

    // --- 基础战斗属性 ---
    private int baseAtk = 50;      // 基础攻击
    private int baseHp = 1000;     // 基础血量上限

    // --- 实时战斗属性 ---
    private int maxHp = 1000;      // 实时血量上限（受等级和装备影响）
    private int currentHp = 1000;  // 当前实时血量
    private int currentArmyCount;  // 逻辑带兵总数
    private String status = "HEALTHY"; // HEALTHY, WOUNDED, KILLED

    // --- 养成属性 ---
    private int level = 1;
    private int currentExp = 0;

    // --- 武将技能系统 ---
    private String activeSkillName = "连击";
    private double skillDamageRatio = 1.8;   // 技能伤害倍率
    private double skillTriggerChance = 0.25; // 触发概率 25%

    // --- 兵力持久化配置 ---
    // 存储格式为 "INFANTRY:50,ARCHER:30,CAVALRY:20,CN_SPECIAL:10"
    @Column(columnDefinition = "TEXT")
    private String armyConfigStr = "INFANTRY:100";

    // 获取生存百分比
    public double getHpRatio() {
        return (double) currentHp / maxHp;
    }
}