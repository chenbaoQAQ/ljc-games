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

@Service
// 战斗指挥中心：处理从开战、单挑、混战到战后结算的完整业务闭环
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

        // 1. 初始化数据：加载武将、装备与敌军血量
        UserGeneral general = generalRepository.findById(generalId)
                .orElseThrow(() -> new RuntimeException("找不到武将"));
        List<Equipment> equips = equipmentRepository.findByOwnerGeneralId(generalId);

        // 预处理：如果是初次战斗，同步武将状态
        general.setCurrentArmyCount(army.getTotalUnitCount());

        int enemyHp = stage.getEnemyBaseHp();
        int round = 1;
        boolean isVictory = false;

        battleLog.add(String.format("【出征】主将 [%s] (Lv.%d) 领兵出战！当前总兵力: %d",
                general.getName(), general.getLevel(), army.getTotalUnitCount()));

        // ==========================================
        // 阶段一：武将 PK 阶段 (前 3 回合)
        // 此阶段仅看武将个人勇武与技能触发
        // ==========================================
        while (round <= 3 && general.getCurrentHp() > 0 && enemyHp > 0) {
            battleLog.add("--- 第 " + round + " 回合：武将 PK ---");

            // 武将个人攻击判定（包含技能触发逻辑）
            double pkAtk = combatEngine.calculatePKDamage(general, equips, battleLog);
            enemyHp -= (int)pkAtk;
            battleLog.add(String.format("[%s] 发起进攻造成 %d 伤害，敌方血量残余: %d",
                    general.getName(), (int)pkAtk, Math.max(0, enemyHp)));

            if (enemyHp <= 0) break;

            // 敌方反击武将（示例：固定伤害或基于关卡强度）
            int enemyCounterAtk = (int)(100 * stage.getEnemyAtkBuff().doubleValue());
            general.setCurrentHp(general.getCurrentHp() - enemyCounterAtk);

            // 核心判定：检查武将健康度与实时兵力损耗联动
            checkGeneralStatus(general, army, battleLog);

            if ("KILLED".equals(general.getStatus())) break;
            round++;
        }

        // ==========================================
        // 阶段二：全军混战阶段 (若敌我皆在，则开启全面冲突)
        // 此阶段武将作为加成点，小兵为主要输出
        // ==========================================
        if (!"KILLED".equals(general.getStatus()) && enemyHp > 0 && army.getTotalUnitCount() > 0) {
            battleLog.add("=== 混战开始：全军压上！ ===");

            while (army.getTotalUnitCount() > 0 && enemyHp > 0) {
                // 计算全军总战力（已包含特种兵强化逻辑与性格修正）
                double totalAtk = combatEngine.calculateFinalAtk(army.calculateTotalPower(), equips, general);
                enemyHp -= (int)totalAtk;

                battleLog.add(String.format("第%d回合: 全军猛攻造成 %d 伤害，敌方剩余: %d",
                        round, (int)totalAtk, Math.max(0, enemyHp)));

                if (enemyHp <= 0) { isVictory = true; break; }

                // 敌方反击小兵
                int enemyToArmyAtk = (int)(200 * stage.getEnemyAtkBuff().doubleValue());
                army.receiveDamage(enemyToArmyAtk);

                // 混战中主将亦有风险（20%概率被流矢射中）
                if (Math.random() < 0.2) {
                    general.setCurrentHp(general.getCurrentHp() - 50);
                    checkGeneralStatus(general, army, battleLog);
                }

                if ("KILLED".equals(general.getStatus())) break;

                round++;
                if (round > 50) break; // 战斗时长上限
            }
        }

        if (enemyHp <= 0) isVictory = true;

        // 4. 战后清算：发放资源、更新等级、保存持久化数据
        processPostBattle(userId, general, army, stage, isVictory, battleLog);

        return battleLog;
    }

    /**
     * 健康检查：血量跌破阈值时同步扣除兵力，体现“主将负伤，军心涣散”
     */
    private void checkGeneralStatus(UserGeneral general, Army army, List<String> log) {
        double hpPercent = (double) general.getCurrentHp() / general.getMaxHp();

        // 1. 负伤判定：80% 阈值
        if (hpPercent <= 0.8 && "HEALTHY".equals(general.getStatus())) {
            general.setStatus("WOUNDED");
            // 兵力由于恐慌瞬间损失 10%
            int loss = (int) (army.getTotalUnitCount() * 0.1);
            army.receiveDamage(loss);
            general.setCurrentArmyCount(army.getTotalUnitCount());
            log.add("！！！【惊恐】主将负伤！全军士气受损，士兵溃逃 10%！");
        }

        // 2. 阵亡判定：0 阈值
        if (general.getCurrentHp() <= 0 && !"KILLED".equals(general.getStatus())) {
            general.setStatus("KILLED");
            general.setCurrentHp(0);
            // 主将战死，军队大溃散损失 50%
            int loss = (int) (army.getTotalUnitCount() * 0.5);
            army.receiveDamage(loss);
            general.setCurrentArmyCount(army.getTotalUnitCount());
            log.add("！！！【悲剧】主将战死沙场！军队发生大规模溃散，损失 50% 兵力！");
        }
    }

    /**
     * 战后结算：处理升级、金币、掉落、伤兵回收
     */
    private void processPostBattle(Integer userId, UserGeneral general, Army army,
                                   StageConfig stage, boolean isVictory, List<String> log) {

        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到玩家档案"));

        if (isVictory) {
            log.add("--- 战斗胜利：凯旋归来 ---");

            // 1. 兵力恢复：胜利可救回 70% 伤兵
            army.recoverTroops(0.7);

            // 2. 奖励发放
            user.setGold(user.getGold() + stage.getGoldReward());
            if (stage.getDiamondReward() != null) {
                user.setDiamond(user.getDiamond() + stage.getDiamondReward());
            }

            // 3. 武将经验与等级提升
            int expGain = 50;
            general.setCurrentExp(general.getCurrentExp() + expGain);
            log.add(String.format("【成长】%s 获得了 %d 点经验！", general.getName(), expGain));

            // 升级逻辑：每 100 经验一级，提升 HP 并回满
            if (general.getCurrentExp() >= 100) {
                general.setLevel(general.getLevel() + 1);
                general.setMaxHp(general.getMaxHp() + 50);
                general.setCurrentHp(general.getMaxHp()); // 升级即回满血
                general.setCurrentExp(0);
                log.add(String.format("【升级】叮！等级提升至 Lv.%d，最大血量增加 50，已回满状态！", general.getLevel()));
            }

            // 4. 触发掉落
            log.add(lootService.dropEquipment(userId, stage));

        } else {
            log.add("--- 战斗失败：残部撤退 ---");
            // 失败仅能救回 20%
            army.recoverTroops(0.2);
            // 若主将战死，剩余兵力也要归零或重置（根据企划定）
            if ("KILLED".equals(general.getStatus())) {
                army.clearTroops();
            }
        }

        // 同步逻辑兵力数
        general.setCurrentArmyCount(army.getTotalUnitCount());

        // 持久化保存
        userProfileRepository.save(user);
        generalRepository.save(general);
    }
}