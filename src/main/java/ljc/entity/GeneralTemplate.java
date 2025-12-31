package ljc.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "general_template")
public class GeneralTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name; // 武将姓名 (General Name)

    @Column(name = "rarity")
    private String rarity; // 稀有度 (SSR/UR)

    @Column(name = "base_leadership")
    private Integer baseLeadership; // 基础统帅值 (Base Leadership)

    @Column(name = "country")
    private String country; // 所属国家 (Country/Civilization)
}