package ljc.battle.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 回合制行动结果
 */
public class TurnResult {
    public boolean finished;
    public boolean win;
    public int currentTurn;
    public List<String> events; // JSON logs (String for now, or Custom Event Object)
    
    // 或者使用严格的事件对象列表
    public List<BattleLogEvent> logEvents;
    
    // 指示下一行动方（用于前端显示“轮到谁了”）
    public int nextActorIndex; 
    public String nextActorDesc;

    public TurnResult() {
        logEvents = new ArrayList<>();
        events = new ArrayList<>();
    }
}
