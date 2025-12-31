package ljc.constant;

import lombok.Getter;

@Getter
public enum DifficultyTier {
    // 1.0 档：普通 (NORMAL)
    NORMAL(1.0, "普通"),
    // 1.2 档：困难 (HARD)
    HARD(1.2, "困难"),
    // 1.5 档：噩梦 (NIGHTMARE)
    NIGHTMARE(1.5, "噩梦");

    private final double factor; // 倍率因子
    private final String displayName; // 中文显示名

    DifficultyTier(double factor, String displayName) {
        this.factor = factor;
        this.displayName = displayName;
    }
}