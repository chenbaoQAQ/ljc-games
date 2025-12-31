package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "general_template")
public class GeneralTemplate {
    @Id
    private Integer id;
    private String name;
    private Integer baseAtk;
    private Integer baseHp;

    // --- 补上这个字段 ---
    private String rarity;
}