package ljc.battle.core;

/**
 * 回合指令
 */
public class TurnCommand {
    public enum ActionType {
        NORMAL, // 普攻
        SKILL   // 技能
    }

    public int clientTurnNo; // 前端提交的回合号（防重放）
    public ActionType type;
    public String target; // 目标ID（若有手动选择）
    public boolean isAi;  // 是否AI
    public String tactics; // 战术策略: TARGET_INF, TARGET_ARC, TARGET_CAV

    public TurnCommand() {}

    public TurnCommand(int turnNo, ActionType type) {
        this.clientTurnNo = turnNo;
        this.type = type;
    }
}
