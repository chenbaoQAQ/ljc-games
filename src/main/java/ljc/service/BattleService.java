package ljc.service;

import ljc.entity.*;
import ljc.model.Army;
import ljc.repository.EquipmentRepository;
import ljc.repository.UserGeneralRepository;
import ljc.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.*;
import java.util.*;

@Service
public class BattleService {
    @Autowired
    private EquipmentRepository equipmentRepository; // 装备库管理员
    @Autowired
    private CombatEngine combatEngine; // 战斗数值引擎
    @Autowired
    private LootService lootService;     // 掉落策划师
    @Autowired
    private UserProfileRepository userProfileRepository;//玩家档案管理员
    @Autowired
    private UserGeneralRepository generalRepository;//武将库管理员

    @Transactional
    public List<String> conductBattle(Integer userId, Integer generalId, StageConfig stage, Army army) {
        List<String> battleLog = new ArrayList<>();

        // 1. 加载数据
        UserGeneral general = generalRepository.findById(generalId)
                .orElseThrow(() -> new RuntimeException("找不到武将"));
        List<Equipment> equips = equipmentRepository.findByOwnerGeneralId(generalId);
        int enemyHp = stage.getEnemyBaseHp();
        int round = 1;
        boolean isVictory = false;

        battleLog.add(String.format("【开战】主将 [%s] 挺枪出马，当前兵力: %d", general.getName(), army.getTotalUnitCount()));

        // ==========================================
        // 阶段一：武将 PK 阶段 (前 3 回合或直到受伤)
        // ==========================================
        while (round <= 3 && general.getCurrentHp() > 0 && enemyHp > 0) {
            battleLog.add("--- 第 " + round + " 回合：武将 PK ---");

            // 武将个人攻击 (不带小兵伤害)
            double generalAtk = combatEngine.calculateGeneralOnlyAtk(general, equips);
            enemyHp -= (int)generalAtk;
            battleLog.add(String.format("[%s] 施展技能造成 %d 伤害，敌方血量: %d", general.getName(), (int)generalAtk, Math.max(0, enemyHp)));

            // 敌方反击武将
            int enemyCounterAtk = 100; // 假设敌方武将反击伤害
            general.setCurrentHp(general.getCurrentHp() - enemyCounterAtk);

            // --- 核心联动：80% 血量必受伤判定 ---
            checkGeneralStatus(general, battleLog);

            // 如果受伤了，根据你的需求：这里其实应该中断返回，让玩家选择是否 Round 2
            if ("WOUNDED".equals(general.getStatus())) {
                battleLog.add("【重要提示】主将已负伤！是否继续战斗？(此时撤退可保留残部)");
                // 如果是真实业务，这里可能直接 return 战报，等待玩家在前端点击“继续”
            }

            round++;
        }

        // ==========================================
        // 阶段二：全军混战阶段 (如果武将还没阵亡且敌方还有血)
        // ==========================================
        if (!"KILLED".equals(general.getStatus()) && enemyHp > 0) {
            battleLog.add("=== PK 结束，全军混战开始 ===");

            while (army.getTotalUnitCount() > 0 && enemyHp > 0) {
                // 此时计算总战力，用的 army 已经是被 checkGeneralStatus 扣除过 10% 或 50% 后的残部了
                double totalAtk = combatEngine.calculateFinalAtk(army.calculateTotalPower(), equips, general);
                enemyHp -= (int)totalAtk;

                // 士兵对砍损耗
                army.receiveDamage(50);

                if (enemyHp <= 0) { isVictory = true; break; }
                round++;
                if (round > 50) break; // 防止死循环
            }
        }

        // 4. 结算
        processPostBattle(userId, general, army, stage, isVictory, battleLog);
        return battleLog;
    }


    private void checkGeneralStatus(UserGeneral general, List<String> log) {
        double hpPercent = (double) general.getCurrentHp() / general.getMaxHp();

        // 1. 80% 阈值必受伤 + 扣 10% 兵
        if (hpPercent <= 0.8 && "HEALTHY".equals(general.getStatus())) {
            general.setStatus("WOUNDED");
            int loss = (int) (general.getCurrentArmyCount() * 0.1);
            general.setCurrentArmyCount(general.getCurrentArmyCount() - loss);
            log.add("！！！【战报】主将血量跌破 80%！负伤触发，士兵惊恐损失 10%！");
        }

        // 2. 0% 阈值判定阵亡 + 扣 50% 兵
        if (general.getCurrentHp() <= 0 && !"KILLED".equals(general.getStatus())) {
            general.setStatus("KILLED");
            int loss = (int) (general.getCurrentArmyCount() * 0.5);
            general.setCurrentArmyCount(general.getCurrentArmyCount() - loss);
            log.add("！！！【惨剧】主将战死沙场！军队大溃散，损失 50% 兵力！");
        }
    }

    //战后结算方法
    private void processPostBattle(Integer userId, UserGeneral general, Army army,
                                   StageConfig stage, boolean isVictory, List<String> log) {

        UserProfile user = userProfileRepository.findById(userId).get();

        if (isVictory) {
            log.add("--- 战斗胜利 ---");

            // 1. 兵力恢复：恢复 70% 伤兵
            army.recoverTroops(0.7);

            // 2. 财富发放：发放金币与钻石
            user.setGold(user.getGold() + stage.getGoldReward());
            if (stage.getDiamondReward() != null) {
                user.setDiamond(user.getDiamond() + stage.getDiamondReward());
            }

            // 3. 武将成长：增加武将经验 (每场胜利得 50 点)
            int expGain = 50;
            general.setCurrentExp(general.getCurrentExp() + expGain);
            log.add(String.format("【成长】%s 获得了 %d 点经验！", general.getName(), expGain));

            // 4. 升级判定：100 经验升一级
            if (general.getCurrentExp() >= 100) {
                general.setLevel(general.getLevel() + 1);
                general.setMaxHp(general.getMaxHp() + 50); // 升级提升生存上限
                general.setCurrentHp(general.getMaxHp());   // 升级瞬间状态回满
                general.setCurrentExp(0);                  // 经验重置
                log.add(String.format("【升级】叮！%s 等级提升至 %d，最大血量增加 50！", general.getName(), general.getLevel()));
            }

            // 5. 掉落判定：触发掉落逻辑
            log.add(lootService.dropEquipment(userId, stage));

        } else {
            log.add("--- 战斗失败 ---");
            // 仅存 20% 溃军
            army.recoverTroops(0.2);
            // 失败惩罚：如果是主将阵亡，兵力清零
            if ("KILLED".equals(general.getStatus())) {
                army.clearTroops();
            }
        }

        // 最后统一保存所有状态，确保数据入库
        userProfileRepository.save(user);
        generalRepository.save(general);
    }
}