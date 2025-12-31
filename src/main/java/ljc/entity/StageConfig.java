package ljc.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "stage_config")
public class StageConfig {
    @Id
    private Integer id;

    @Column(name = "region_id")
    private Integer regionId;

    @Column(name = "region_name")
    private String regionName;

    @Column(name = "has_wall")
    private Boolean hasWall; // 数据库 tinyint(1) 会自动对应 Boolean

    @Column(name = "is_boss")
    private Boolean isBoss;

    @Column(name = "wall_cost")
    private Integer wallCost;

    @Column(name = "enemy_atk_buff")
    private java.math.BigDecimal enemyAtkBuff; // 必须是 BigDecimal 才能存 1.20
}