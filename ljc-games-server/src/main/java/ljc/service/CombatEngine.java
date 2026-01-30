package ljc.service.core;

import ljc.dto.BattleContext;
import ljc.dto.BattleContext.CombatSide;
import ljc.dto.TroopGroup;
import org.springframework.stereotype.Component;

@Component
public class CombatEngine {

    /**
     * ⚔️ 执行整场战斗
     */
    public void process(BattleContext ctx) {
        ctx.addLog("【战报】战斗开始！全军列阵！");

        // 战斗主循环 (最多 20 回合)
        while (ctx.getCurrentTurn() < ctx.getMaxTurns()) {
            ctx.setCurrentTurn(ctx.getCurrentTurn() + 1);
            ctx.addLog(String.format("=== 第 %d 回合 ===", ctx.getCurrentTurn()));

            // 执行一回合的攻防
            processTurn(ctx);

            // 每一回合结束，立即检查胜负
            if (checkResult(ctx)) break;
        }

        // 20回合打满若未分胜负，则平局
        if (ctx.getResult() == null) {
            ctx.setResult("DRAW");
            ctx.addLog("【战报】双方鸣金收兵，以平局收场。");
        }
    }

    /**
     * 🔄 单回合标准流程
     * 顺序：武将互殴 -> 亡语检查 -> 小兵互殴
     */
    private void processTurn(BattleContext ctx) {
        CombatSide p1 = ctx.getAttacker();
        CombatSide p2 = ctx.getDefender();

        // ==========================================
        // Phase 1: 武将回合 (不管怎么样，武将先战斗)
        // ==========================================
        if (p1.getGeneralHp() > 0 && p2.getGeneralHp() > 0) {
            // 双方武将都活着，互相造成伤害
            // 1. P1 打 P2
            int dmg1 = Math.max(1, p1.getGeneralAtk());
            p2.setGeneralHp(Math.max(0, p2.getGeneralHp() - dmg1));
            ctx.addLog(String.format(">> [武将] %s 发起进攻，对 %s 造成 %d 点伤害", p1.getName(), p2.getName(), dmg1));

            // 2. P2 反击 P1 (只要没被秒杀)
            if (p2.getGeneralHp() > 0) {
                int dmg2 = Math.max(1, p2.getGeneralAtk());
                p1.setGeneralHp(Math.max(0, p1.getGeneralHp() - dmg2));
                ctx.addLog(String.format(">> [武将] %s 进行反击，对 %s 造成 %d 点伤害", p2.getName(), p1.getName(), dmg2));
            }
        } else {
            // 如果有一方武将已经不在了，跳过单挑环节
            ctx.addLog(">> [武将] 只有一方主将在场，跳过单挑环节...");
        }

        // ==========================================
        // Phase 1.5: 亡语判定 (检查武将是否刚刚阵亡)
        // ==========================================
        checkGeneralDeath(ctx, p1);
        checkGeneralDeath(ctx, p2);

        // ==========================================
        // Phase 2: 小兵回合 (决定生死的战斗)
        // ==========================================
        // 顺序：弓(远程) -> 步 -> 骑 -> 特
        String[] order = {"ARCHER", "INFANTRY", "CAVALRY", "SPECIAL"};

        for (String type : order) {
            processUnitClash(ctx, type);
        }
    }

    /**
     * 🚑 检查武将阵亡逻辑 (实现“武将死后兵战斗，但会有惩罚”)
     */
    private void checkGeneralDeath(BattleContext ctx, CombatSide side) {
        // 如果血量归零，且之前标记为“活着” (说明是本回合刚死的)
        if (side.getGeneralHp() <= 0 && !side.isGeneralDead()) {
            side.setGeneralDead(true); // 标记已死，防止下一回合重复触发

            // 触发惩罚：全军动摇
            ctx.addLog(String.format("！！！噩耗：[%s] 主将阵亡！全军士气大跌！", side.getName()));

            // 逻辑：遍历所有兵团，扣除 10% 当前兵力作为逃兵
            side.getTroops().values().forEach(troop -> {
                if (troop.isAlive()) {
                    int fleeCount = (int) Math.ceil(troop.getCount() * 0.1);
                    troop.setCount(troop.getCount() - fleeCount);
                    // 也可以降低攻击力，这里先只做逃兵逻辑
                }
            });
            ctx.addLog(">>> 因主将阵亡，该阵营 10% 的士兵逃离了战场。");
        }
    }

    private void processUnitClash(BattleContext ctx, String type) {
        TroopGroup t1 = ctx.getAttacker().getTroops().get(type);
        TroopGroup t2 = ctx.getDefender().getTroops().get(type);

        // 只有双方都有这个兵种时才互殴 (这里简化逻辑，实际可能是找克制目标)
        if (t1 != null && t1.isAlive() && t2 != null && t2.isAlive()) {
            // 互殴计算
            int dmgTo2 = t1.getCount() * t1.getAtk();
            int dead2 = t2.takeDamage(dmgTo2);

            int dmgTo1 = t2.getCount() * t2.getAtk(); // 这是一个简化的同时结算，不考虑先手打死对方减少反击
            int dead1 = t1.takeDamage(dmgTo1);

            ctx.addLog(String.format(">> [%s] 交锋：我方阵亡 %d 人，敌方阵亡 %d 人", type, dead1, dead2));
        }
    }

    private boolean checkResult(BattleContext ctx) {
        // 核心修正：只检查 isDefeated (内部只看兵)
        boolean p1Lose = ctx.getAttacker().isDefeated();
        boolean p2Lose = ctx.getDefender().isDefeated();

        if (p1Lose && p2Lose) {
            ctx.setResult("DRAW");
            return true;
        } else if (p1Lose) {
            ctx.setResult("LOSE"); // 进攻方输
            return true;
        } else if (p2Lose) {
            ctx.setResult("WIN"); // 进攻方赢
            return true;
        }
        return false;
    }
}