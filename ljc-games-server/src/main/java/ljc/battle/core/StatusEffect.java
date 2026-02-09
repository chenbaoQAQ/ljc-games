package ljc.battle.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 状态效果
 */
public class StatusEffect {

    public enum StatusType {
        POISON,
        HOT,
        STUN,
        IMMUNE,
        VULNERABLE
    }

    public StatusType type;
    public int remainingTurns;
    public Map<String, Object> params;

    public StatusEffect() {
        params = new HashMap<>();
    }

    public StatusEffect(StatusType type, int turns) {
        this();
        this.type = type;
        this.remainingTurns = turns;
    }
}
