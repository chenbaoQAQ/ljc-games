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
    private Boolean hasWall;
    private Boolean isBoss;
    private Integer wallCost;
    private BigDecimal enemyAtkBuff;
    private Integer enemyBaseHp;

    // --- 新增：奖励与掉落逻辑字段 ---
    private Integer goldReward;     // 通关金币奖励
    private Integer diamondReward;  // 首通钻石奖励
    private BigDecimal lootRate;    // 掉落率，对应数据库 0.10 这种格式
}