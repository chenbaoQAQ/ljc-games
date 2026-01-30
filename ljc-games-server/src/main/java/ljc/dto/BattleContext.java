package ljc.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class BattleContext {
    private Long battleId;
    private int maxTurns = 20;
    private int currentTurn = 0;

    // 双方阵营
    private CombatSide attacker;
    private CombatSide defender;

    // 战报列表
    private List<BattleLog> logs = new ArrayList<>();
    private String result;

    public void addLog(String message) {
        logs.add(new BattleLog(currentTurn, message));
    }

    @Data
    public static class CombatSide {
        private String name;
        private int generalHp;
        private int generalMaxHp;
        private int generalAtk;

        // 标记：武是否已经阵亡（防止重复触发亡语）
        private boolean isGeneralDead = false;

        private Map<String, TroopGroup> troops;

        /**
         * 【修正：败北判定】
         * 只要兵死光了，就算输。
         * 武将死活不影响“是否战败”，只影响兵的属性（亡语）。
         */
        public boolean isDefeated() {
            // 检查是否所有兵团都死光了
            return troops.values().stream().noneMatch(TroopGroup::isAlive);
        }
    }
}