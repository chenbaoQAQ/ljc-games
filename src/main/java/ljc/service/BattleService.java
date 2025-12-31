package ljc.service;

import ljc.constant.DifficultyTier;
import ljc.entity.StageConfig;
import ljc.entity.UnitConfig;
import ljc.model.Army;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BattleService {

    /**
     * 1. 破墙逻辑 (保持不变)
     */
    public void processWallBreachWithPlan(Army army, StageConfig stage, Map<UnitConfig, Integer> sacrificePlan) {
        if (!stage.getHasWall()) return;
        int requiredCost = stage.getWallCost();
        int totalPlanned = sacrificePlan.values().stream().mapToInt(Integer::intValue).sum();

        if (totalPlanned < requiredCost) {
            throw new RuntimeException("牺牲人数不足以破墙！需要: " + requiredCost);
        }

        Map<UnitConfig, Integer> currentTroops = army.getTroopMap();
        sacrificePlan.forEach((unit, count) -> {
            int current = currentTroops.getOrDefault(unit, 0);
            currentTroops.put(unit, Math.max(0, current - count));
        });
    }

    /**
     * 2. 计算敌方动态战力 (含 BOSS 和 难度)
     */
    public int getEnemyPower(int basePower, StageConfig stage, DifficultyTier tier) {
        double stageBuff = stage.getEnemyAtkBuff().doubleValue();
        double tierFactor = tier.getFactor();
        return (int) (basePower * stageBuff * tierFactor);
    }

    /**
     * 3. 核心：回合制模拟战 (Simulated Round Battle)
     * 解决你说的：人少了，伤害也得跟着少
     */
    public List<String> conductBattle(Army army, StageConfig stage, DifficultyTier tier, Map<UnitConfig, Integer> sacrificePlan) {
        List<String> battleLog = new ArrayList<>();

        // A. 战前处理
        if (stage.getHasWall()) {
            processWallBreachWithPlan(army, stage, sacrificePlan);
            battleLog.add("【城墙】破墙成功，部队已产生损耗。");
        }

        // B. 敌军初始化 (假设敌军生命值等于其总战力)
        int enemyBasePower = 1000;
        int enemyHP = getEnemyPower(enemyBasePower, stage, tier);
        int round = 1;

        // C. 回合循环 (交互开始)
        // 只要玩家还有兵，且敌军血量 > 0，就继续打
        while (army.getTotalUnitCount() > 0 && enemyHP > 0) {
            // 1. 玩家回合：基于【当前剩余兵力】计算攻击力
            int currentPlayerAtk = army.calculateTotalPower();
            enemyHP -= currentPlayerAtk;
            battleLog.add(String.format("第%d回合: 玩家发动进攻，造成%d伤害，敌方剩余血量:%d", round, currentPlayerAtk, Math.max(0, enemyHP)));

            if (enemyHP <= 0) break; // 敌军全灭

            // 2. 敌军回合：反击玩家
            // 这里的逻辑：敌军伤害直接按比例扣除玩家的兵力 (简单的战损模型)
            int enemyAtk = (int) (enemyHP * 0.1); // 假设敌军每10点血提供1点攻击
            army.receiveDamage(enemyAtk); // 这是一个新方法，需要你在Army里写：按比例减员

            battleLog.add(String.format("第%d回合: 敌方发起反击，造成%d战损，玩家剩余总兵力:%d", round, enemyAtk, army.getTotalUnitCount()));

            round++;
            if (round > 50) break; // 防止死循环，强行平局
        }

        // D. 结果判定
        if (enemyHP <= 0) {
            battleLog.add("--- 最终结果: VICTORY ---");
        } else {
            battleLog.add("--- 最终结果: DEFEAT ---");
        }

        return battleLog;
    }
}