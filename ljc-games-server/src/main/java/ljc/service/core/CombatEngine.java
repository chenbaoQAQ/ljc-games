package ljc.service.core;

import ljc.dto.BattleContext;
import ljc.dto.BattleContext.CombatSide;
import org.springframework.stereotype.Component;

/**
 * 核心战斗计算引擎 (v2.2 Roll点版 - 完整框架)
 * 包含：主循环 + 胜负裁判 + (待实现的)单挑与混战逻辑
 */
@Component
public class CombatEngine {

    // 最大回合数常量 = 20
    private static final int MAX_TURNS = 20;

    /**
     * ⚔️ 入口方法：执行整场战斗
     * (这里完全保留了主公刚才写的逻辑！)
     */
    public void process(BattleContext ctx) {
        // 1. 在战报里记录一句："战斗开始！"
        ctx.addLog("【战报】战斗开始！");

        // 2. 开启主循环：当 ctx 的 currentTurn 小于 MAX_TURNS 时，一直循环
        while (ctx.getCurrentTurn() < MAX_TURNS) {

            // 2.1 回合数 +1
            ctx.setCurrentTurn(ctx.getCurrentTurn() + 1);

            // 2.2 记录回合分割线
            ctx.addLog(String.format("--- 第 %d 回合 ---", ctx.getCurrentTurn()));

            // 2.3 执行这一回合的战斗 (分阶段：先武将Solo，再全军混战)
            processTurn(ctx);

            // 2.4 检查有没有人输了
            // 如果 checkResult 返回 true，说明分出胜负了，必须 break 跳出循环
            if (checkResult(ctx) == true) {
                break;
            }
        }

        // 3. 循环结束后，检查 result 是否还是 null
        // 如果代码跑到这里，result 还是空的，说明打满 20 回合都没人死光 -> 强制判平局
        if (ctx.getResult() == null) {
            ctx.setResult("DRAW");
            ctx.addLog("【战报】双方激战至日落，未分胜负，鸣金收兵。");
        }
    }

    /**
     * 🔄 单回合逻辑
     * 严格遵守 v2.2 企划：武将先 Solo，活着的那个参与小兵的战斗
     */
    private void processTurn(BattleContext ctx) {
        // ==========================================
        // 阶段一：武将单挑 (Solo Phase)
        // ==========================================
        processGeneralSolo(ctx);

        // ==========================================
        // 阶段二：军团混战 (Army Phase)
        // ==========================================
        // 这里的逻辑最复杂，包含 Roll 点和兵种克制
        processArmyCombat(ctx);
    }

    /**
     * (待填空) 武将单挑逻辑
     */
    private void processGeneralSolo(BattleContext ctx) {
        // TODO: 后面我们要在这里写：
        // 1. 判断双方武将是不是都活着？
        // 2. 只有都活着，才互相砍一刀
    }

    /**
     * (待填空) 军团混战逻辑
     */
    private void processArmyCombat(BattleContext ctx) {
        // TODO: 后面我们要在这里写：
        // 1. 遍历步、弓、骑、特种兵
        // 2. 计算 Roll 点 (多少打人，多少打武将)
        // 3. 结算伤害
    }

    /**
     * ⚖️ 胜负检查 (裁判员)
     * @return true=战斗结束, false=继续打
     */
    private boolean checkResult(BattleContext ctx) {
        CombatSide attacker = ctx.getAttacker();
        CombatSide defender = ctx.getDefender();

        // 获取双方是否战败 (v2.2 规则：兵死光=输)
        boolean p1Lose = attacker.isDefeated();
        boolean p2Lose = defender.isDefeated();

        // 情况 A: 同归于尽 (极为罕见)
        if (p1Lose && p2Lose) {
            ctx.setResult("DRAW");
            ctx.addLog("【战报】双方部队同归于尽！");
            return true;
        }
        // 情况 B: 进攻方输了
        else if (p1Lose) {
            ctx.setResult("LOSE");
            ctx.addLog("【战报】我方全军覆没，败局已定。");
            return true;
        }
        // 情况 C: 防守方输了 (也就是玩家赢了)
        else if (p2Lose) {
            ctx.setResult("WIN");
            ctx.addLog("【战报】敌军溃败，我方取得了辉煌的胜利！");
            return true;
        }

        // 还没分出胜负，返回 false (继续打)
        return false;
    }
}