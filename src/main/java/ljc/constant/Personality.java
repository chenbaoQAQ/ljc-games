package ljc.constant;
import lombok.Getter;
/**
 * Personality: 武将性格枚举
 */
@Getter // 自动生成 getDescription 方法
public enum Personality {

    // 定义四个选项，括号里是它们的中文描述
    DIE_IN_BATTLE("战死沙场"),
    COWARDICE("贪生怕死"),
    DO_BEST("竭尽全力"),
    BETRAYAL("叛逃之心");

    private final String description;

    // 枚举的构造函数（固定写法，不用深究）
    Personality(String description) {
        this.description = description;
    }
}