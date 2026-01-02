package ljc.service;

import ljc.entity.*;
import ljc.model.Army;
import ljc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
// 核心战斗指挥中心：负责调度武将单挑与全军集火混战
public class BattleService {
    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private CombatEngine combatEngine;
    @Autowired private LootService lootService;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private UserGeneralRepository generalRepository;

    @Transactional
    public List<String> conductBattle(Integer userId, Integer generalId, StageConfig stage, Army army) {
        List<String> battleLog = new ArrayList<>();

        // 1. 数据准备
        UserGeneral general = generalRepository.findById(generalId)
                .orElseThrow(() -> new RuntimeException("找不到武将 ID: " + generalId));
        List<Equipment> equips = equipmentRepository.findByOwnerGeneralId(generalId);

        int enemyHp = stage.getEnemyBaseHp();
        // 获取本关的主力兵种，用于判定集火优先级
        String enemyType = stage.getMainEnemyType() != null ? stage.getMainEnemyType() : "INFANTRY";
        int round = 1;
        boolean isVictory = false;

        battleLog.add(String.format("【开战】主将 [%s] (Lv.%d) 领兵对决 %s！(敌军主力: %s)",
                general.getName(), general.getLevel(), stage.getStageName(), enemyType));

        // ==========================================
        // 阶段一：武将对决 (PK阶段)
        // 规则：前 3 回合进行武将间输出计算
        // ==========================================
        while (round <= 3 && general.getCurrentHp() > 0 && enemyHp > 0) {
            battleLog.add("--- 第 " + round + " 回合：武将 PK ---");

            // 计算武将伤害 (包含性格加成与状态惩罚)
            double damage = combatEngine.calculatePKDamage(general, equips);
            enemyHp -= (int)damage;

            // 敌方固定反击 (可根据 stage.enemyAtkBuff 动态调整)
            int counterAtk = 100;
            general.setCurrentHp(general.getCurrentHp() - counterAtk);

            battleLog.add(String.format("[%s] 施展武艺造成 %d 伤害，自身余血: %d",
                    general.getName(), (int)damage, Math.max(0, general.getCurrentHp())));

            // 实时检查主将状态：若负伤或阵亡，会牵连小兵损耗
            checkGeneralStatus(general, army, battleLog);

            if ("KILLED".equals(general.getStatus())) break;
            round++;
        }

        // ==========================================
        // 阶段二：混战阶段 (执行战法序列)
        // 规则：弓兵 -> 步兵 -> 骑兵 -> 特种兵
        // ==========================================
        if (!"KILLED".equals(general.getStatus()) && enemyHp > 0 && army.getTotalUnitCount() > 0) {
            battleLog.add("=== PK 结束，全军混战开始 ===");

            while (army.getTotalUnitCount() > 0 && enemyHp > 0) {
                battleLog.add("--- 第 " + round + " 回合：全军进攻 ---");

                // 1. 扫描特种兵提供的强化名额
                Map<String, Integer> buffs = army.calculateSpecialBuffs();

                // 2. 弓兵攻击序列 (判定集火优先级)
                if (combatEngine.getAttackPriority("ARCHER", enemyType) >= 100) {
                    battleLog.add(">> [战术] 弓手部队锁定敌方克制单位，正在进行优先集火...");
                }
                int archerDmg = army.getUnitAttackPower("ARCHER", enemyType, buffs.getOrDefault("ARCHER", 0), combatEngine);
                enemyHp -= archerDmg;
                if(archerDmg > 0) battleLog.add(String.format(">> 弓兵波次造成 %d 伤害 %s",
                        archerDmg, combatEngine.isCounter("ARCHER", enemyType) ? "【克制★翻倍】" : ""));

                // 3. 步兵攻击序列
                if (combatEngine.getAttackPriority("INFANTRY", enemyType) >= 100) {
                    battleLog.add(">> [战术] 步兵阵列发现敌方防线薄弱，正在发起集群冲锋...");
                }
                int infantryDmg = army.getUnitAttackPower("INFANTRY", enemyType, buffs.getOrDefault("INFANTRY", 0), combatEngine);
                enemyHp -= infantryDmg;
                if(infantryDmg > 0) battleLog.add(String.format(">> 步兵波次造成 %d 伤害 %s",
                        infantryDmg, combatEngine.isCounter("INFANTRY", enemyType) ? "【克制★翻倍】" : ""));

                // 4. 骑兵攻击序列
                if (combatEngine.getAttackPriority("CAVALRY", enemyType) >= 100) {
                    battleLog.add(">> [战术] 铁骑部队发现敌方散兵，正在进行毁灭性践踏...");
                }
                int cavalryDmg = army.getUnitAttackPower("CAVALRY", enemyType, buffs.getOrDefault("CAVALRY", 0), combatEngine);
                enemyHp -= cavalryDmg;
                if(cavalryDmg > 0) battleLog.add(String.format(">> 骑兵波次造成 %d 伤害 %s",
                        cavalryDmg, combatEngine.isCounter("CAVALRY", enemyType) ? "【克制★翻倍】" : ""));

                // 5. 特种兵自身输出 (收割波次)
                int specialDmg = army.getSpecialUnitPersonalAttack(combatEngine);
                enemyHp -= specialDmg;
                if(specialDmg > 0) battleLog.add(String.format(">> 特种兵袭杀造成 %d 额外伤害", specialDmg));

                if (enemyHp <= 0) break;

                // 敌方反击士兵 (受关卡 buff 影响)
                int enemyReflex = (int)(150 * stage.getEnemyAtkBuff().doubleValue());
                army.receiveDamage(enemyReflex);

                // 混战中主将亦有风险（15%几率被流弹击中）
                if (Math.random() < 0.15) {
                    general.setCurrentHp(general.getCurrentHp() - 50);
                    checkGeneralStatus(general, army, battleLog);
                }

                if ("KILLED".equals(general.getStatus())) break;

                round++;
                if (round > 50) break; // 强制熔断
            }
        }

        if (enemyHp <= 0) isVictory = true;

        // 3. 战后总结与持久化
        processPostBattle(userId, general, army, stage, isVictory, battleLog);

        return battleLog;
    }

    /**
     * 实时监控主将生命值：触发受伤/阵亡带来的连锁兵力损失
     */
    private void checkGeneralStatus(UserGeneral general, Army army, List<String> log) {
        double hpPercent = (double) general.getCurrentHp() / general.getMaxHp();

        // 80% 触发负伤：损失 10% 现有兵力
        if (hpPercent <= 0.8 && "HEALTHY".equals(general.getStatus())) {
            general.setStatus("WOUNDED");
            int loss = (int) (army.getTotalUnitCount() * 0.1);
            army.receiveDamage(loss);
            general.setCurrentArmyCount(army.getTotalUnitCount());
            log.add("！！！主将负伤！士气大跌，士兵恐慌性损失 10%！");
        }

        // 0% 触发阵亡：损失 50% 现有兵力
        if (general.getCurrentHp() <= 0 && !"KILLED".equals(general.getStatus())) {
            general.setStatus("KILLED");
            general.setCurrentHp(0);
            int loss = (int) (army.getTotalUnitCount() * 0.5);
            army.receiveDamage(loss);
            general.setCurrentArmyCount(army.getTotalUnitCount());
            log.add("！！！主将战死沙场！军心崩坏，部队瞬间损失 50% 兵力！");
        }
    }

    /**
     * 战后清算逻辑
     */
    private void processPostBattle(Integer userId, UserGeneral general, Army army,
                                   StageConfig stage, boolean isVictory, List<String> log) {
        UserProfile user = userProfileRepository.findById(userId).get();

        if (isVictory) {
            log.add("--- 战役胜利：凯旋而归 ---");
            // 恢复 70% 伤兵
            army.recoverTroops(0.7);

            // 奖励发放
            user.setGold(user.getGold() + stage.getGoldReward());

            // 经验与升级判定
            general.setCurrentExp(general.getCurrentExp() + 50);
            if (general.getCurrentExp() >= 100) {
                general.setLevel(general.getLevel() + 1);
                general.setMaxHp(general.getMaxHp() + 50);
                general.setCurrentHp(general.getMaxHp()); // 升级状态自动回满
                general.setCurrentExp(0);
                log.add("【升级】叮！等级提升至 Lv." + general.getLevel() + "，血量上限提升并回满！");
            }

            // 触发装备掉落
            log.add(lootService.dropEquipment(userId, stage));

        } else {
            log.add("--- 战役失败：溃退回城 ---");
            // 仅存 20% 残兵
            army.recoverTroops(0.2);
            if ("KILLED".equals(general.getStatus())) {
                army.clearTroops(); // 主将阵亡且战败，残部全数消失
            }
        }

        // 同步最新的带兵数量回武将实体
        general.setCurrentArmyCount(army.getTotalUnitCount());

        // 持久化保存
        userProfileRepository.save(user);
        generalRepository.save(general);
    }
}