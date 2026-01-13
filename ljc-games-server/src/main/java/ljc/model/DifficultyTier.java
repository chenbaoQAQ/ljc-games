package ljc.model;

import lombok.Getter;

@Getter
public enum DifficultyTier {
    NORMAL("普通", 1.0, 1.0, 1.0),
    HARD("困难", 1.5, 1.3, 1.5),
    NIGHTMARE("噩梦", 2.5, 1.8, 2.0);

    private final String name;
    private final double hpMultiplier;
    private final double atkMultiplier;
    private final double rewardMultiplier;

    DifficultyTier(String name, double hpMultiplier, double atkMultiplier, double rewardMultiplier) {
        this.name = name;
        this.hpMultiplier = hpMultiplier;
        this.atkMultiplier = atkMultiplier;
        this.rewardMultiplier = rewardMultiplier;
    }
}
