package ljc.entity;

import ljc.constant.GeneralType;
import ljc.constant.Personality;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class General extends BaseEntity {

    //武将职业
    private GeneralType type;

    //武将性格
    private Personality personality;

    //武将造成伤害
    public int calculateDamage(int equipAtk) {
        return (equipAtk + this.baseAtk) * this.level;
    }
}