package ljc.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "equipment")
//记录每一件武器的加成数值（攻击力、强化等级等）。
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
    // Equipment.java 里的逻辑
    public void upgrade() {
        this.level += 1; // 强化等级 +1
        this.atkBonus += 20; // 每次强化固定增加 20 点攻击力
    }
}