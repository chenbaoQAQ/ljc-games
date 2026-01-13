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
    private Integer baseLeadership;
    private String rarity;
    private String country;
}