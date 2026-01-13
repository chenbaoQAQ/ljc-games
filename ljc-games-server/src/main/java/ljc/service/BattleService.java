package ljc.service;

import ljc.entity.*;
import ljc.model.*;
import ljc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class BattleService {
    @Autowired private UserGeneralRepository generalRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private UserStageProgressRepository progressRepository;
    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private UnitConfigRepository unitRepo;
    @Autowired private CombatEngine combatEngine;
    @Autowired private LootService lootService;

    @Transactional
    public List<String> conductBattle(Integer userId, Integer generalId, StageConfig stage, DifficultyTier tier) {
        List<String> battleLog = new ArrayList<>();

        // 0. 加载基础数据
        UserGeneral general = generalRepository.findById(generalId)
                .orElseThrow(() -> new RuntimeException("找不到指定武将"));

        // 1. 初始化部队 (通过 JSON 还原兵力构成)
        Army army = new Army();
        army.fromJson(general.getArmyConfigStr(), unitRepo);

        if (army.getTotalUnitCount() <= 0) {
            battleLog.add("【警告】主将麾下无兵，无法出征！请先去兵营招募士兵。");
            return battleLog;
        }

        // 2. 数值与难度系数判定 [逻辑修正：统一难度加成]
        int enemyHp = (tier != null) ? (int)(stage.getEnemyBaseHp() * tier.getHpMultiplier()) : stage.getEnemyBaseHp();
        // 最终敌人攻击系数 = 关卡基础系数 * 难度额外倍率
        double finalEnemyAtkFactor = (tier != null) ?
                (stage.getEnemyAtkBuff().doubleValue() * tier.getAtkMultiplier()) :
                stage.getEnemyAtkBuff().doubleValue();

        String enemyType = stage.getMainEnemyType() != null ? stage.getMainEnemyType() : "INFANTRY";
        int round = 1;

        battleLog.add(String.format("【开战】主将 [%s] 领兵决战 %s (难度: %s)！",
                general.getName(), stage.getStageName(), (tier != null ? tier.getName() : "普通")));
        battleLog.add(String.format(">> 敌方主力兵种: %s，全军做好克制准备！", enemyType));

        // ==========================================
        // 阶段一：武将单挑 (英雄流加持阶段)
        // ==========================================
        List<Equipment> equips = equipmentRepository.findByOwnerGeneralId(generalId);
        while (round <= 3 && general.getCurrentHp() > 0 && enemyHp > 0) {
            battleLog.add("--- 第 " + round + " 回合：武将单挑 ---");

            // 统计英国亲卫加持 (英雄流)
            int heroBuffs = army.calculateHeroBuffCount();
            if (heroBuffs > 0) {
                battleLog.add(String.format(">> [英雄流] 王室亲卫发起 %d 次力量齐吼，武将气势大增！", heroBuffs));
            }

            // 计算武将伤害
            double dmg = combatEngine.calculatePKDamage(general, equips, heroBuffs);
            enemyHp -= (int)dmg;

            // 敌方反击武将：受难度攻击系数影响
            int counterAtkToHero = (int)(100 * finalEnemyAtkFactor);
            general.setCurrentHp(general.getCurrentHp() - counterAtkToHero);

            battleLog.add(String.format("[%s] 勇冠三军造成 %d 伤害，自身余血: %d",
                    general.getName(), (int)dmg, Math.max(0, general.getCurrentHp())));

            checkGeneralStatus(general, army, battleLog);
            if ("KILLED".equals(general.getStatus())) break;
            round++;
        }

        // ==========================================
        // 阶段二：全军混战 (战术集火补全版)
        // ==========================================
        if (enemyHp > 0 && !"KILLED".equals(general.getStatus()) && army.getTotalUnitCount() > 0) {
            battleLog.add("=== 进入全军混战阶段：三军听令，发起总攻！ ===");

            while (army.getTotalUnitCount() > 0 && enemyHp > 0) {
                battleLog.add(String.format("--- 第 %d 回合：混战对决 ---", round));

                // 1. 获取特种兵强化名额
                Map<String, Integer> buffs = army.calculateSpecialBuffs();

                // 2. 弓兵攻击波次
                if (combatEngine.getAttackPriority("ARCHER", enemyType) >= 100) {
                    battleLog.add(">> [战术] 弓兵方阵锁定克制目标，正在执行远程优先集火...");
                }
                int archerDmg = army.getUnitAttackPower("ARCHER", enemyType, buffs.getOrDefault("ARCHER", 0), combatEngine);
                enemyHp -= archerDmg;
                if (archerDmg > 0) {
                    battleLog.add(String.format(">> 弓兵部队齐射造成 %d 伤害 %s",
                            archerDmg, combatEngine.isCounter("ARCHER", enemyType) ? "【克制★双倍】" : ""));
                }

                // 3. 步兵攻击波次 (战术描述补全)
                if (combatEngine.getAttackPriority("INFANTRY", enemyType) >= 100) {
                    battleLog.add(">> [战术] 步兵方阵发现敌方防御薄弱，持盾发起集群冲锋...");
                }
                int infantryDmg = army.getUnitAttackPower("INFANTRY", enemyType, buffs.getOrDefault("INFANTRY", 0), combatEngine);
                enemyHp -= infantryDmg;
                if (infantryDmg > 0) {
                    battleLog.add(String.format(">> 步兵前锋营造成 %d 伤害 %s",
                            infantryDmg, combatEngine.isCounter("INFANTRY", enemyType) ? "【克制★双倍】" : ""));
                }

                // 4. 骑兵攻击波次 (战术描述补全)
                if (combatEngine.getAttackPriority("CAVALRY", enemyType) >= 100) {
                    battleLog.add(">> [战术] 铁骑部队侧翼包抄，正在进行毁灭性践踏...");
                }
                int cavalryDmg = army.getUnitAttackPower("CAVALRY", enemyType, buffs.getOrDefault("CAVALRY", 0), combatEngine);
                enemyHp -= cavalryDmg;
                if (cavalryDmg > 0) {
                    battleLog.add(String.format(">> 铁骑主力突击造成 %d 伤害 %s",
                            cavalryDmg, combatEngine.isCounter("CAVALRY", enemyType) ? "【克制★双倍】" : ""));
                }

                // 5. 特种兵精锐输出
                int specialDmg = army.getSpecialUnitPersonalAttack(combatEngine);
                enemyHp -= specialDmg;
                if (specialDmg > 0) {
                    battleLog.add(String.format(">> 特种兵精锐执行斩首战术，额外造成 %d 伤害", specialDmg));
                }

                if (enemyHp <= 0) break;

                // 6. 敌方反击士兵：受难度系数影响
                int enemyToArmyAtk = (int)(150 * finalEnemyAtkFactor);
                army.receiveDamage(enemyToArmyAtk);

                // 7. 乱军风险判定
                if (Math.random() < 0.15) {
                    int sniperDmg = 50;
                    general.setCurrentHp(general.getCurrentHp() - sniperDmg);
                    battleLog.add(String.format("！！！危险：主将 [%s] 在混战中被流矢射中，血量减少 %d", general.getName(), sniperDmg));
                    checkGeneralStatus(general, army, battleLog);
                }

                if ("KILLED".equals(general.getStatus()) || army.getTotalUnitCount() <= 0) break;

                round++;
                if (round > 50) {
                    battleLog.add(">> [系统] 战斗陷入僵局，双方因体力不支各自收兵...");
                    break;
                }
            }
        }

        // 3. 最终结算与持久化
        boolean isVictory = (enemyHp <= 0);
        processFinalSettlement(userId, general, army, stage, tier, isVictory, battleLog);

        // 保存最新的兵力 JSON 和总数
        general.setArmyConfigStr(army.toJson());
        general.setCurrentArmyCount(army.getTotalUnitCount());
        generalRepository.save(general);

        return battleLog;
    }

    private void processFinalSettlement(Integer userId, UserGeneral general, Army army, StageConfig stage, DifficultyTier tier, boolean isVictory, List<String> log) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到玩家存档"));

        if (isVictory) {
            log.add("--- 凯旋而归：战斗取得大捷！ ---");
            // 胜利回收 70% 伤兵
            army.recoverTroops(0.7);

            // 闯关模式奖励逻辑 (含首通钻石判定)
            if (tier != null) {
                Optional<UserStageProgress> prog = progressRepository.findByUserIdAndStageIdAndDifficulty(userId, stage.getId(), tier.name());
                if (prog.isEmpty() || !prog.get().isFirstCleared()) {
                    // 发放首通奖励 (金币与钻石)
                    int goldReward = (int)(stage.getGoldReward() * tier.getRewardMultiplier());
                    user.setGold(user.getGold() + goldReward);
                    user.setDiamond(user.getDiamond() + stage.getDiamondReward());

                    // 记录首通进度
                    UserStageProgress p = prog.orElse(new UserStageProgress());
                    p.setUserId(userId);
                    p.setStageId(stage.getId());
                    p.setDifficulty(tier.name());
                    p.setFirstCleared(true);
                    progressRepository.save(p);

                    log.add(String.format(">> [首通奖] 成功领取 %s 难度首通礼包：金币+%d, 钻石+%d！",
                            tier.getName(), goldReward, stage.getDiamondReward()));
                } else {
                    // 非首通仅领取金币奖励 (按系数打折，防止过度刷钱)
                    int basicGold = (int)(stage.getGoldReward() * 0.5 * tier.getRewardMultiplier());
                    user.setGold(user.getGold() + basicGold);
                    log.add(String.format(">> [结算] 获得常规战利品：金币+%d", basicGold));
                }
            }
            general.setCurrentExp(general.getCurrentExp() + 50);

            // 检查升级逻辑
            if (general.getCurrentExp() >= 100) {
                general.setLevel(general.getLevel() + 1);
                general.setMaxHp(general.getMaxHp() + 50);
                general.setCurrentHp(general.getMaxHp());
                general.setCurrentExp(0);
                log.add(String.format("【升级】叮！%s 等级提升至 Lv.%d，状态已恢复全满！", general.getName(), general.getLevel()));
            }
        } else {
            log.add("--- 战败撤退：部队损失惨重 ---");
            // 失败仅能回收 20% 伤兵
            army.recoverTroops(0.2);
            if ("KILLED".equals(general.getStatus())) {
                log.add(">> [噩耗] 主将阵亡且战败，残部在撤退途中全数溃散...");
                army.clearTroops();
            }
        }
        userProfileRepository.save(user);
    }

    private void checkGeneralStatus(UserGeneral general, Army army, List<String> log) {
        double hpPercent = (double) general.getCurrentHp() / general.getMaxHp();

        // 负伤状态判定 (HP < 80%)
        if (hpPercent <= 0.8 && "HEALTHY".equals(general.getStatus())) {
            general.setStatus("WOUNDED");
            int loss = (int) (army.getTotalUnitCount() * 0.1);
            army.receiveDamage(loss);
            log.add(String.format("！！！【惊恐】主将 [%s] 负伤，士兵士气受挫，瞬间逃亡 %d 人！", general.getName(), loss));
        }

        // 阵亡状态判定 (HP <= 0)
        if (general.getCurrentHp() <= 0 && !"KILLED".equals(general.getStatus())) {
            general.setStatus("KILLED");
            general.setCurrentHp(0);
            int loss = (int) (army.getTotalUnitCount() * 0.5);
            army.receiveDamage(loss);
            log.add(String.format("！！！【悲剧】主将 [%s] 壮烈牺牲，全军崩溃，损失 50%% 兵力！", general.getName()));
        }
    }

    private void returnSurvivorsToReserve(UserGeneral general, Army activeArmy) {
        Army reserve = new Army();
        reserve.fromJson(general.getReserveArmyConfigStr(), unitRepo);

        // 将幸存者加回仓库
        activeArmy.getTroopMap().forEach((unit, count) -> {
            int current = reserve.getTroopMap().getOrDefault(unit, 0);
            reserve.getTroopMap().put(unit, current + count);
        });

        general.setReserveArmyConfigStr(reserve.toJson());
        general.setArmyConfigStr("{}"); // 清空阵前，强制下场战斗重新分配
        general.setCurrentArmyCount(0);
    }
}