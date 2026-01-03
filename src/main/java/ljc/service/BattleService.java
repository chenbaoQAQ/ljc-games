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
        UserGeneral general = generalRepository.findById(generalId).orElseThrow();

        // 1. 初始化部队 (JSON 还原)
        Army army = new Army();
        army.fromJson(general.getArmyConfigStr(), unitRepo);

        if (army.getTotalUnitCount() <= 0) {
            battleLog.add("【警告】主将麾下无兵，无法出征！");
            return battleLog;
        }

        // 2. 数值与难度判定
        int enemyHp = (tier != null) ? (int)(stage.getEnemyBaseHp() * tier.getHpMultiplier()) : stage.getEnemyBaseHp();
        double enemyAtkFactor = (tier != null) ? (stage.getEnemyAtkBuff().doubleValue() * tier.getAtkMultiplier()) : stage.getEnemyAtkBuff().doubleValue();
        String enemyType = stage.getMainEnemyType() != null ? stage.getMainEnemyType() : "INFANTRY";
        int round = 1;

        battleLog.add(String.format("【开战】主将 [%s] 领兵决战 %s (主力: %s)！", general.getName(), stage.getStageName(), enemyType));

        // 阶段一：武将单挑 (英雄流判定)
        List<Equipment> equips = equipmentRepository.findByOwnerGeneralId(generalId);
        while (round <= 3 && general.getCurrentHp() > 0 && enemyHp > 0) {
            battleLog.add("--- 第 " + round + " 回合：武将单挑 ---");
            int heroBuffs = army.calculateHeroBuffCount();
            if (heroBuffs > 0) battleLog.add(String.format(">> [英雄流] 亲卫加持，攻击提升 %d 次！", heroBuffs));

            double dmg = combatEngine.calculatePKDamage(general, equips, heroBuffs);
            enemyHp -= (int)dmg;
            general.setCurrentHp(general.getCurrentHp() - (int)(100 * enemyAtkFactor));
            battleLog.add(String.format("[%s] 造成 %d 伤害，自身余血: %d", general.getName(), (int)dmg, Math.max(0, general.getCurrentHp())));
            checkGeneralStatus(general, army, battleLog);
            if ("KILLED".equals(general.getStatus())) break;
            round++;
        }

        // 阶段二：全军混战 (战术集火版)
        if (enemyHp > 0 && !"KILLED".equals(general.getStatus())) {
            battleLog.add("=== 进入混战阶段 ===");
            while (army.getTotalUnitCount() > 0 && enemyHp > 0) {
                Map<String, Integer> buffs = army.calculateSpecialBuffs();

                // 弓兵波次
                if (combatEngine.getAttackPriority("ARCHER", enemyType) >= 100) battleLog.add(">> [战术] 弓兵方阵锁定克制目标进行优先集火...");
                int archerDmg = army.getUnitAttackPower("ARCHER", enemyType, buffs.getOrDefault("ARCHER", 0), combatEngine);
                enemyHp -= archerDmg;
                if(archerDmg > 0) battleLog.add(String.format(">> 弓兵造成 %d 伤害 %s", archerDmg, combatEngine.isCounter("ARCHER", enemyType) ? "【克制！】" : ""));

                // 在混战阶段循环内补全
                int infantryDmg = army.getUnitAttackPower("INFANTRY", enemyType, buffs.getOrDefault("INFANTRY", 0), combatEngine);
                enemyHp -= infantryDmg;
                if(infantryDmg > 0) battleLog.add(String.format(">> 步兵造成 %d 伤害 %s", infantryDmg, combatEngine.isCounter("INFANTRY", enemyType) ? "【克制！】" : ""));

                int cavalryDmg = army.getUnitAttackPower("CAVALRY", enemyType, buffs.getOrDefault("CAVALRY", 0), combatEngine);
                enemyHp -= cavalryDmg;
                if(cavalryDmg > 0) battleLog.add(String.format(">> 骑兵造成 %d 伤害 %s", cavalryDmg, combatEngine.isCounter("CAVALRY", enemyType) ? "【克制！】" : ""));

                int specialDmg = army.getSpecialUnitPersonalAttack(combatEngine);
                enemyHp -= specialDmg;
                if(specialDmg > 0) battleLog.add(String.format(">> 特种兵袭杀造成 %d 额外伤害", specialDmg));

                if (enemyHp <= 0) break;
                army.receiveDamage((int)(150 * enemyAtkFactor));
                if (Math.random() < 0.15) { general.setCurrentHp(general.getCurrentHp() - 50); checkGeneralStatus(general, army, battleLog); }
                round++;
                if (round > 50) break;
            }
        }

        // 3. 结算与 JSON 保存
        processFinalSettlement(userId, general, army, stage, tier, enemyHp <= 0, battleLog);
        general.setArmyConfigStr(army.toJson());
        general.setCurrentArmyCount(army.getTotalUnitCount());
        generalRepository.save(general);
        return battleLog;
    }

    private void processFinalSettlement(Integer userId, UserGeneral general, Army army, StageConfig stage, DifficultyTier tier, boolean isVictory, List<String> log) {
        UserProfile user = userProfileRepository.findById(userId).get();
        if (isVictory) {
            log.add("--- 战斗胜利 ---");
            army.recoverTroops(0.7);
            if (tier != null) {
                Optional<UserStageProgress> prog = progressRepository.findByUserIdAndStageIdAndDifficulty(userId, stage.getId(), tier.name());
                if (prog.isEmpty() || !prog.get().isFirstCleared()) {
                    user.setGold(user.getGold() + stage.getGoldReward());
                    user.setDiamond(user.getDiamond() + stage.getDiamondReward());
                    UserStageProgress p = prog.orElse(new UserStageProgress());
                    p.setUserId(userId); p.setStageId(stage.getId()); p.setDifficulty(tier.name()); p.setFirstCleared(true);
                    progressRepository.save(p);
                    log.add(">> [首通奖] 成功领取该难度钻石金币奖励！");
                }
            }
            general.setCurrentExp(general.getCurrentExp() + 50);
        } else {
            log.add("--- 战败 ---");
            army.recoverTroops(0.2);
        }
        userProfileRepository.save(user);
    }

    private void checkGeneralStatus(UserGeneral general, Army army, List<String> log) {
        double hpPercent = (double) general.getCurrentHp() / general.getMaxHp();
        if (hpPercent <= 0.8 && "HEALTHY".equals(general.getStatus())) {
            general.setStatus("WOUNDED");
            army.receiveDamage((int) (army.getTotalUnitCount() * 0.1));
            log.add("！！！主将负伤，士兵逃亡 10%！");
        }
        if (general.getCurrentHp() <= 0 && !"KILLED".equals(general.getStatus())) {
            general.setStatus("KILLED");
            army.receiveDamage((int) (army.getTotalUnitCount() * 0.5));
            log.add("！！！主将阵亡，军心崩溃损失 50%！");
        }
    }
}
