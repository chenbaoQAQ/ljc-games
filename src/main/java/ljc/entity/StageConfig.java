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

    // NORMAL, HARD, NIGHTMARE
    private String difficultyTier;

    private String mainEnemyType; // 敌方主力兵种
    private BigDecimal enemyAtkBuff;
    private Integer enemyBaseHp;

    // 奖励
    private Integer goldReward;
    private Integer diamondReward; // 首通钻石
    private BigDecimal lootRate;

    public double getTierMultiplier() {
        if ("HARD".equals(difficultyTier)) return 1.5;
        if ("NIGHTMARE".equals(difficultyTier)) return 2.5;
        return 1.0;
    }
}