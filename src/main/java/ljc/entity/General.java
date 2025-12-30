package ljc.entity;

import ljc.constant.GeneralType;
import ljc.constant.Personality;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class General extends BaseEntity {

    // 这里不再是简单的 String，而是受保护的枚举类型
    private GeneralType type;

    private Personality personality;

    public int calculateDamage(int equipAtk) {
        return (equipAtk + this.baseAtk) * this.level;
    }
}