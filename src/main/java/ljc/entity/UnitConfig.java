package ljc.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity//告诉spring这是实体表
@Table(name = "unit_config")
public class UnitConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "unit_name")
    private String unitName;

    @Column(name = "space_cost")
    private Integer spaceCost;

    @Column(name = "base_atk")
    private Integer baseAtk;

    @Column(name = "base_hp")
    private Integer baseHp;

    @Column(name = "target_type")
    private String targetType; // 强化目标

    @Column(name = "buff_ratio")
    private BigDecimal buffRatio; // 强化倍率
}