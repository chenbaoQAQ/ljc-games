package ljc.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * /battle/story/start 返回体：battleId + 初始战场快照
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleStartResult {
    private Long battleId;
    private BattleContext context;
}
