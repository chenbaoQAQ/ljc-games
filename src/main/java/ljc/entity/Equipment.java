package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    @Enumerated(EnumType.STRING)
    private EquipType equipType; // 使用枚举：WEAPON, ARMOR, TALLY

    private String equipName;
    private Integer level = 1;
    private Integer currentExp = 0;

    // 对应 Schema 里的三个加成字段
    private Integer atkBonus = 0;
    private Integer hp_bonus = 0;         // 对应数据库字段 hp_bonus
    private Integer leadershipBonus = 0;  // 对应数据库字段 leadership_bonus

    private Integer ownerGeneralId; // 当前穿在哪个武将身上

    public enum EquipType {
        WEAPON, ARMOR, TALLY
    }
}