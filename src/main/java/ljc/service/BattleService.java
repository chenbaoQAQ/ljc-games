package ljc.service;

import ljc.entity.*;
import ljc.model.Army;
import ljc.repository.EquipmentRepository;
import ljc.repository.UserGeneralRepository;
import ljc.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
// 战斗指挥中心：负责调度武将单挑与全军集火混战
public class BattleService {
    @Autowired
    private EquipmentRepository equipmentRepository;
    @Autowired
    private CombatEngine combatEngine;
    @Autowired
    private LootService lootService;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private UserGeneralRepository generalRepository;

    @Transactional
    public List<String> conductBattle(Integer userId, Integer generalId, StageConfig stage, Army army) {
        List<String> battleLog = new ArrayList<>();

        // 1. 初始化数据
        UserGeneral general = generalRepository.findById(generalId)
                .orElseThrow(() -> new RuntimeException("找不到武将"));
        List<Equipment> equips = equipmentRepository.findByOwnerGeneralId(generalId);

        // 获取关卡主要的敌人类型（用于克制判定）
        // 注意：如果 StageConfig 报红，请看下方的“补充建议”
        String enemyType = stage.getMainEnemyType() != null ? stage.getMainEnemyType() : "INFANTRY";
        int enemyHp = stage.getEnemyBaseHp();
        int round = 1;
        boolean isVictory = false;

        battleLog.add(String.format("【开战】主将 [%s] 挺枪出马，当前兵力: %d (敌方主力: %s)",
                general.getName(), army.getTotalUnitCount(), enemyType));

        // ==========================================
        // 阶段一：武将 PK 阶段 (前 3 回合)
        // ==========================================
        while (round <= 3 && general.getCurrentHp() > 0 && enemyHp > 0) {
            battleLog.add("--- 第 " + round + " 回合：武将单挑 ---");

            // 武将个人攻击 (包含技能触发判定)
            double pkAtk = combatEngine.calculatePKDamage(general, equips, battleLog);
            enemyHp -= (int)pkAtk;
            battleLog.add(String.format("[%s] 进攻造成 %d 伤害，敌方血量: %d",
                    general.getName(), (int)pkAtk, Math.max(0, enemyHp)));

            if (enemyHp <= 0) break;

            // 敌方反击武将
            int enemyCounterAtk = 100;
            general.setCurrentHp(general.getCurrentHp() - enemyCounterAtk);

            // 检查武将状态（此时需要传入 army 以同步扣除逃逸士兵）
            checkGeneralStatus(general, army, battleLog);

            if ("KILLED".equals(general.getStatus())) break;
            round++;
        }

        // ==========================================
        // 阶段二：全军混战 (波次序列：特种加持 -> 弓 -> 步 -> 骑 -> 特)
        // ==========================================
        if (!"KILLED".equals(general.getStatus()) && enemyHp > 0 && army.getTotalUnitCount() > 0) {
            battleLog.add("=== 进入混战阶段：执行战法序列 ===");

            while (army.getTotalUnitCount() > 0 && enemyHp > 0) {
                battleLog.add("--- 第 " + round + " 回合：全军冲锋 ---");

                // 1. 计算特种兵加持额度
                Map<String, Integer> buffs = army.calculateSpecialBuffs();

                // 2. 弓兵波次：判定是否优先集火
                if (combatEngine.getAttackPriority("ARCHER", enemyType) >= 100) {
                    battleLog.add(">> [战术] 弓兵部队发现敌方克制单位，正在进行优先集火...");
                }
                int archerDmg = army.getUnitAttackPower("ARCHER", enemyType, buffs.getOrDefault("ARCHER", 0), combatEngine);
                enemyHp -= archerDmg;
                if(archerDmg > 0) battleLog.add(String.format(">> 弓兵齐射：造成 %d 伤害 %s",
                        archerDmg, combatEngine.isCounter("ARCHER", enemyType) ? "【克制★】" : ""));

                // 3. 步兵(剑)波次
                int infantryDmg = army.getUnitAttackPower("INFANTRY", enemyType, buffs.getOrDefault("INFANTRY", 0), combatEngine);
                enemyHp -= infantryDmg;
                if(infantryDmg > 0) battleLog.add(String.format(">> 步兵推进：造成 %d 伤害 %s",
                        infantryDmg, combatEngine.isCounter("INFANTRY", enemyType) ? "【克制★】" : ""));

                // 4. 骑兵波次
                int cavalryDmg = army.getUnitAttackPower("CAVALRY", enemyType, buffs.getOrDefault("CAVALRY", 0), combatEngine);
                enemyHp -= cavalryDmg;
                if(cavalryDmg > 0) battleLog.add(String.format(">> 骑兵冲锋：造成 %d 伤害 %s",
                        cavalryDmg, combatEngine.isCounter("CAVALRY", enemyType) ? "【克制★】" : ""));

                // 5. 特种兵自身输出（收割波次）
                int specialDmg = army.getSpecialUnitPersonalAttack(combatEngine);
                enemyHp -= specialDmg;
                if(specialDmg > 0) battleLog.add(String.format(">> 特种兵袭杀：造成 %d 额外伤害", specialDmg));

                if (enemyHp <= 0) break;

                // 敌方反击 (分摊战损)
                army.receiveDamage(200);

                round++;
                if (round > 50) break; // 防止死循环
            }
        }

        if (enemyHp <= 0) isVictory = true;

        // 3. 战后清算
        processPostBattle(userId, general, army, stage, isVictory, battleLog);
        return battleLog;
    }

    /**
     * 健康检查：主将受伤会导致士兵逃逸
     */
    private void checkGeneralStatus(UserGeneral general, Army army, List<String> log) {
        double hpPercent = (double) general.getCurrentHp() / general.getMaxHp();

        // 1. 80% 阈值必受伤 + 扣 10% 兵
        if (hpPercent <= 0.8 && "HEALTHY".equals(general.getStatus())) {
            general.setStatus("WOUNDED");
            int loss = (int) (army.getTotalUnitCount() * 0.1);
            army.receiveDamage(loss); // 真实扣除 Army 里的士兵
            general.setCurrentArmyCount(army.getTotalUnitCount()); // 同步数值
            log.add("！！！【战报】主将血量跌破 80%！负伤触发，士兵惊恐损失 10%！");
        }

        // 2. 0% 阈值判定阵亡 + 扣 50% 兵
        if (general.getCurrentHp() <= 0 && !"KILLED".equals(general.getStatus())) {
            general.setStatus("KILLED");
            general.setCurrentHp(0);
            int loss = (int) (army.getTotalUnitCount() * 0.5);
            army.receiveDamage(loss);
            general.setCurrentArmyCount(army.getTotalUnitCount());
            log.add("！！！【惨剧】主将战死沙场！军队发生大溃散，损失 50% 兵力！");
        }
    }

    /**
     * 战后结算
     */
    private void processPostBattle(Integer userId, UserGeneral general, Army army,
                                   StageConfig stage, boolean isVictory, List<String> log) {

        UserProfile user = userProfileRepository.findById(userId).get();

        if (isVictory) {
            log.add("--- 战斗胜利 ---");
            army.recoverTroops(0.7);
            user.setGold(user.getGold() + stage.getGoldReward());

            // 经验与升级逻辑
            general.setCurrentExp(general.getCurrentExp() + 50);
            if (general.getCurrentExp() >= 100) {
                general.setLevel(general.getLevel() + 1);
                general.setMaxHp(general.getMaxHp() + 100);
                general.setCurrentHp(general.getMaxHp()); // 升级回满
                general.setCurrentExp(0);
                log.add(">> [升级] 武将提升至 Lv." + general.getLevel());
            }
            log.add(lootService.dropEquipment(userId, stage));
        } else {
            log.add("--- 战斗失败 ---");
            army.recoverTroops(0.2);
            if ("KILLED".equals(general.getStatus())) {
                army.clearTroops();
            }
        }

        // 同步逻辑兵力数
        general.setCurrentArmyCount(army.getTotalUnitCount());

        userProfileRepository.save(user);
        generalRepository.save(general);
    }
}