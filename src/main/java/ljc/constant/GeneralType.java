package ljc.constant;

import lombok.Getter;

@Getter
public enum GeneralType {
    RIDER("骑手"),
    ARCHER("弓手"),
    MELEE("近战");

    private final String description;

    GeneralType(String description) {
        this.description = description;
    }
}