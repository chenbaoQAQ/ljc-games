package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "stage_config")
public class StageConfig {
    @Id
    private Integer id;

    private Integer regionId;
    private String stageName;

    // --- 难度与类型核心 ---
    // NORMAL, HARD, NIGHTMARE
    // 闯关模式使用此字段调整系数，无尽模式默认为 NORMAL
    private String difficultyTier;

    private String mainEnemyType; // 敌方主力兵种
    private BigDecimal enemyAtkBuff; // 最终攻击系数
    private Integer enemyBaseHp;

    // --- 奖励逻辑 ---
    private Integer goldReward;
    private Integer diamondReward; // 首通/通关钻石
    private BigDecimal lootRate;

    /**
     * 获取基于难度的奖励倍率
     * 策划逻辑：难度越高，额外掉率和金币越高
     */
    public double getTierMultiplier() {
        if ("HARD".equals(difficultyTier)) return 1.5;
        if ("NIGHTMARE".equals(difficultyTier)) return 2.5;
        return 1.0;
    }
}