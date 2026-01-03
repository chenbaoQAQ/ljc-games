package ljc.service;

import ljc.entity.*;
import ljc.model.*;
import ljc.model.DifficultyTier;
import ljc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
/**
 * 核心战斗指挥中心：
 * 1. 区分 [闯关模式] (需传入 DifficultyTier) 与 [无尽模式] (tier 为 null)。
 * 2. 闯关模式下，不同难度的首通奖独立领取。
 * 3. 整合了英国特种兵（英雄流）的单挑加持逻辑。
 */
public class BattleService {
    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private CombatEngine combatEngine;
    @Autowired private LootService lootService;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private UserGeneralRepository generalRepository;
    @Autowired private UserStageProgressRepository progressRepository;

    @Transactional
    public List<String> conductBattle(Integer userId, Integer generalId, StageConfig stage, Army army, DifficultyTier tier) {
        List<String> battleLog = new ArrayList<>();

        // 1. 数据准备
        UserGeneral general = generalRepository.findById(generalId)
                .orElseThrow(() -> new RuntimeException("找不到武将 ID: " + generalId));
        List<Equipment> equips = equipmentRepository.findByOwnerGeneralId(generalId);

        // 2. 模式切换与数值动态计算
        int enemyHp;
        double enemyAtkFactor;

        if (tier != null) {
            // 【闯关模式】：数值受难度系数影响
            enemyHp = (int)(stage.getEnemyBaseHp() * tier.getHpMultiplier());
            enemyAtkFactor = stage.getEnemyAtkBuff().doubleValue() * tier.getAtkMultiplier();
            battleLog.add(String.format("【挑战】%s | 难度等级：%s", stage.getStageName(), tier.getName()));
        } else {
            // 【无尽模式】：使用基础/计算好的层数数值
            enemyHp = stage.getEnemyBaseHp();
            enemyAtkFactor = stage.getEnemyAtkBuff().doubleValue();
            battleLog.add("【挑战】正在进行无尽模式挑战...");
        }

        String enemyType = stage.getMainEnemyType() != null ? stage.getMainEnemyType() : "INFANTRY";
        int round = 1;

        battleLog.add(String.format("【开战】主将 [%s] (Lv.%d) 领兵出战！(敌军主力: %s)",
                general.getName(), general.getLevel(), enemyType));

        // ==========================================
        // 阶段一：武将对决 (PK阶段)
        // ==========================================
        while (round <= 3 && general.getCurrentHp() > 0 && enemyHp > 0) {
            battleLog.add("--- 第 " + round + " 回合：武将单挑 ---");

            // 计算英雄流加持次数
            int heroBuffs = army.calculateHeroBuffCount();
            if (heroBuffs > 0) {
                battleLog.add(String.format(">> [王室亲卫] 提供 %d 次力量加持，武将单挑伤害大幅提升！", heroBuffs));
            }

            // 计算武将伤害
            double damage = combatEngine.calculatePKDamage(general, equips, heroBuffs);
            enemyHp -= (int)damage;

            // 敌方反击
            int counterAtk = (int)(100 * enemyAtkFactor);
            general.setCurrentHp(general.getCurrentHp() - counterAtk);

            battleLog.add(String.format("[%s] 施展武艺造成 %d 伤害，自身余血: %d",
                    general.getName(), (int)damage, Math.max(0, general.getCurrentHp())));

            checkGeneralStatus(general, army, battleLog);
            if ("KILLED".equals(general.getStatus())) break;
            round++;
        }

        // ==========================================
        // 阶段二：混战阶段
        // ==========================================
        if (!"KILLED".equals(general.getStatus()) && enemyHp > 0 && army.getTotalUnitCount() > 0) {
            battleLog.add("=== 进入混战阶段：三军对垒 ===");

            while (army.getTotalUnitCount() > 0 && enemyHp > 0) {
                battleLog.add("--- 第 " + round + " 回合全军出击 ---");

                Map<String, Integer> buffs = army.calculateSpecialBuffs();

                // 弓兵
                if (combatEngine.getAttackPriority("ARCHER", enemyType) >= 100) {
                    battleLog.add(">> [战术] 弓兵方阵锁定敌方克制目标，开始优先集火...");
                }
                int archerDmg = army.getUnitAttackPower("ARCHER", enemyType, buffs.getOrDefault("ARCHER", 0), combatEngine);
                enemyHp -= archerDmg;
                if(archerDmg > 0) battleLog.add(String.format(">> 弓兵造成 %d 伤害 %s",
                        archerDmg, combatEngine.isCounter("ARCHER", enemyType) ? "【克制★】" : ""));

                // 步兵
                int infantryDmg = army.getUnitAttackPower("INFANTRY", enemyType, buffs.getOrDefault("INFANTRY", 0), combatEngine);
                enemyHp -= infantryDmg;
                if(infantryDmg > 0) battleLog.add(String.format(">> 步兵造成 %d 伤害 %s",
                        infantryDmg, combatEngine.isCounter("INFANTRY", enemyType) ? "【克制★】" : ""));

                // 骑兵
                int cavalryDmg = army.getUnitAttackPower("CAVALRY", enemyType, buffs.getOrDefault("CAVALRY", 0), combatEngine);
                enemyHp -= cavalryDmg;
                if(cavalryDmg > 0) battleLog.add(String.format(">> 骑兵造成 %d 伤害 %s",
                        cavalryDmg, combatEngine.isCounter("CAVALRY", enemyType) ? "【克制★】" : ""));

                // 特种兵自身输出
                int specialDmg = army.getSpecialUnitPersonalAttack(combatEngine);
                enemyHp -= specialDmg;
                if(specialDmg > 0) battleLog.add(String.format(">> 特种兵袭杀造成 %d 额外伤害", specialDmg));

                if (enemyHp <= 0) break;

                // 敌方反击
                int enemyReflex = (int)(150 * enemyAtkFactor);
                army.receiveDamage(enemyReflex);

                if (Math.random() < 0.15) {
                    general.setCurrentHp(general.getCurrentHp() - 50);
                    checkGeneralStatus(general, army, battleLog);
                }

                round++;
                if (round > 50) break;
            }
        }

        boolean isVictory = enemyHp <= 0;
        processFinalSettlement(userId, general, army, stage, tier, isVictory, battleLog);

        return battleLog;
    }

    private void processFinalSettlement(Integer userId, UserGeneral general, Army army,
                                        StageConfig stage, DifficultyTier tier, boolean isVictory, List<String> log) {
        UserProfile user = userProfileRepository.findById(userId).get();

        if (isVictory) {
            log.add("--- 战斗胜利：凯旋归来 ---");
            army.recoverTroops(0.7);

            if (tier != null) {
                String diffName = tier.name();
                Optional<UserStageProgress> progressOpt = progressRepository.findByUserIdAndStageIdAndDifficulty(userId, stage.getId(), diffName);

                if (progressOpt.isEmpty() || !progressOpt.get().isFirstCleared()) {
                    user.setGold(user.getGold() + stage.getGoldReward());
                    user.setDiamond(user.getDiamond() + stage.getDiamondReward());
                    log.add(String.format("【首通大奖】达成 [%s] 难度！获得金币：%d，钻石：%d！",
                            tier.getName(), stage.getGoldReward(), stage.getDiamondReward()));

                    UserStageProgress p = progressOpt.orElse(new UserStageProgress());
                    p.setUserId(userId); p.setStageId(stage.getId()); p.setDifficulty(diffName); p.setFirstCleared(true);
                    progressRepository.save(p);
                } else {
                    user.setGold(user.getGold() + 50);
                    log.add("【日常】已领过该难度首通，获得常规战利品金币 +50");
                }
            } else {
                user.setGold(user.getGold() + 100);
                log.add("【爬塔收益】获得无尽模式金币奖励 +100");
            }

            general.setCurrentExp(general.getCurrentExp() + 50);
            if (general.getCurrentExp() >= 100) {
                general.setLevel(general.getLevel() + 1);
                general.setMaxHp(general.getMaxHp() + 50);
                general.setCurrentHp(general.getMaxHp());
                general.setCurrentExp(0);
                log.add(">> [武将升级] 赵云战力提升，当前等级 Lv." + general.getLevel());
            }

            log.add(lootService.dropEquipment(userId, stage));

        } else {
            log.add("--- 战斗失败：残部撤退 ---");
            army.recoverTroops(0.2);
            if ("KILLED".equals(general.getStatus())) {
                army.clearTroops();
            }
        }

        general.setCurrentArmyCount(army.getTotalUnitCount());
        userProfileRepository.save(user);
        generalRepository.save(general);
    }

    private void checkGeneralStatus(UserGeneral general, Army army, List<String> log) {
        double hpPercent = (double) general.getCurrentHp() / general.getMaxHp();
        if (hpPercent <= 0.8 && "HEALTHY".equals(general.getStatus())) {
            general.setStatus("WOUNDED");
            army.receiveDamage((int) (army.getTotalUnitCount() * 0.1));
            log.add("！！！主将负伤！士兵恐慌损失 10%！");
        }
        if (general.getCurrentHp() <= 0 && !"KILLED".equals(general.getStatus())) {
            general.setStatus("KILLED");
            general.setCurrentHp(0);
            army.receiveDamage((int) (army.getTotalUnitCount() * 0.5));
            log.add("！！！主将阵亡！部队瞬间溃散 50%！");
        }
    }
}