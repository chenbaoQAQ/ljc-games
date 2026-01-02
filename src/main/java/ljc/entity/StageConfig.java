package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "stage_config")
/**
 * 关卡配置实体：
 * 1. 定义了敌人的基础属性（血量、难度加成）。
 * 2. 存储了战术核心字段 mainEnemyType，用于兵种克制集火逻辑。
 * 3. 包含了通关的金币、钻石奖励及掉落概率。
 */
public class StageConfig {
    @Id
    private Integer id;

    private Integer regionId;
    private String stageName;
    private Boolean hasWall;    // 是否有城墙
    private Boolean isBoss;    // 是否为BOSS关
    private Integer wallCost;   // 破城需要的额外代价

    private BigDecimal enemyAtkBuff; // 敌人攻击力倍率系数
    private Integer enemyBaseHp;     // 敌人基础血量上限

    // --- 战术联动核心字段 ---
    // 标注本关卡敌人的主力兵种（例如 "ARCHER"），
    // 战斗引擎会根据这个值让玩家的克制兵种（如 步兵）优先集火。
    private String mainEnemyType;

    // --- 奖励与掉落逻辑字段 ---
    private Integer goldReward;     // 通关金币奖励
    private Integer diamondReward;  // 首通钻石奖励
    private BigDecimal lootRate;    // 装备掉落率 (如 0.10 代表 10%)
}