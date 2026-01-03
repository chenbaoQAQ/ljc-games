package ljc.model;

import lombok.Getter;

@Getter
public enum DifficultyTier {
    NORMAL("普通", 1.0, 1.0, 1.0),
    HARD("困难", 1.5, 1.3, 1.2),
    NIGHTMARE("噩梦", 2.5, 1.8, 1.5);

    private final String name;
    private final double hpMultiplier;    // 敌人血量加成
    private final double atkMultiplier;   // 敌人攻击加成
    private final double rewardMultiplier; // 金币额外收益

    DifficultyTier(String name, double hpMultiplier, double atkMultiplier, double rewardMultiplier) {
        this.name = name;
        this.hpMultiplier = hpMultiplier;
        this.atkMultiplier = atkMultiplier;
        this.rewardMultiplier = rewardMultiplier;
    }
}
