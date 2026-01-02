package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "stage_config")
/**
 * 关卡配置实体：
 * 1. 定义了敌人的强度和类型。
 * 2. 核心字段 mainEnemyType 用于支持兵种克制的“集火”逻辑。
 */
public class StageConfig {
    @Id
    private Integer id;

    private Integer regionId;
    private String stageName;
    private Boolean hasWall;
    private Boolean isBoss;
    private Integer wallCost;

    // 敌方数值倍率
    private BigDecimal enemyAtkBuff;
    private Integer enemyBaseHp;

    // --- 战术联动字段 ---
    // 标注本关敌军的主力兵种，例如 "INFANTRY", "ARCHER", "CAVALRY"
    // 战斗引擎会根据此字段判定玩家兵种是否触发“集火”和“双倍伤害”
    private String mainEnemyType;

    // --- 奖励与掉落字段 ---
    private Integer goldReward;     // 通关金币奖励
    private Integer diamondReward;  // 首通/通关钻石奖励
    private BigDecimal lootRate;    // 掉落装备的概率 (如 0.10)
}