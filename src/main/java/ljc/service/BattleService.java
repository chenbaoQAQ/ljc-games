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
    private EquipmentRepository equipmentRepository; // 补上这一行，消灭变量名红字
    @Autowired
    private CombatEngine combatEngine; // 刚才写的数值修正引擎
    @Autowired
    private LootService lootService;     // 装备掉落服务
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private UserGeneralRepository generalRepository;

    @Transactional
    public List<String> conductBattle(Integer userId, Integer generalId, StageConfig stage, Army army) {
        List<String> battleLog = new ArrayList<>();

        // 1. 战前准备：加载武将与装备
        UserGeneral general = generalRepository.findById(generalId)
                .orElseThrow(() -> new RuntimeException("找不到武将"));
        List<Equipment> equips = equipmentRepository.findByOwnerGeneralId(generalId);

        battleLog.add(String.format("【出征】主将 [%s] (性格:%s) 领兵出战！",
                general.getName(), general.getPersonality()));

        // 2. 初始数据计算
        int enemyHP = stage.getEnemyBaseHp();
        int round = 1;
        boolean isVictory = false;

        // 3. 核心回合逻辑
        while (army.getTotalUnitCount() > 0 && enemyHP > 0 && !general.getStatus().equals("KILLED")) {
            // A. 动态计算玩家实时攻击力 (包含：实时人数、装备、性格、状态惩罚)
            double currentAtk = combatEngine.calculateFinalAtk(army.calculateTotalPower(), equips, general);

            enemyHP -= (int)currentAtk;
            battleLog.add(String.format("第%d回合: 玩家进攻造成 %d 伤害，敌方残余血量: %d",
                    round, (int)currentAtk, Math.max(0, enemyHP)));

            if (enemyHP <= 0) {
                isVictory = true;
                break;
            }

            // B. 敌方反击与主将风险判定
            int enemyAtk = (int)(stage.getEnemyAtkBuff().doubleValue() * 50); // 示例基础伤害
            army.receiveDamage(enemyAtk); // 士兵受损

            // 判定主将是否被流矢射中 (性格影响：暴躁性格更容易受伤)
            checkGeneralStatus(general, enemyAtk, battleLog);

            battleLog.add(String.format("第%d回合: 敌方反击，玩家兵力剩余: %d，主将状态: %s",
                    round, army.getTotalUnitCount(), general.getStatus()));

            round++;
            if (round > 50) break;
        }

        // 4. 战后清算 (这是最关键的逻辑更新)
        processPostBattle(userId, general, army, stage, isVictory, battleLog);

        return battleLog;
    }

    private void checkGeneralStatus(UserGeneral general, int enemyAtk, List<String> log) {
        double injuryChance = (enemyAtk > 100) ? 0.15 : 0.05;
        // 暴躁性格受伤概率翻倍
        if ("RASH".equals(general.getPersonality())) injuryChance *= 2;

        if (Math.random() < injuryChance) {
            if ("HEALTHY".equals(general.getStatus())) {
                general.setStatus("WOUNDED");
                log.add("！！！【战报】主将受伤，战力下降 20%！");
            } else if ("WOUNDED".equals(general.getStatus()) && Math.random() < 0.2) {
                general.setStatus("KILLED");
                log.add("！！！【噩耗】主将阵亡，全军溃散！");
            }
        }
    }

    private void processPostBattle(Integer userId, UserGeneral general, Army army,
                                   StageConfig stage, boolean isVictory, List<String> log) {
        UserProfile user = userProfileRepository.findById(userId).get();

        if (isVictory) {
            log.add("--- 战斗胜利 ---");
            // 恢复 70% 伤兵
            army.recoverTroops(0.7);
            // 发放金币与钻石
            user.setGold(user.getGold() + stage.getGoldReward());
            // 触发掉落逻辑
            log.add(lootService.dropEquipment(userId, stage));
        } else {
            log.add("--- 战斗失败 ---");
            // 仅存 20% 溃军
            army.recoverTroops(0.2);
            // 失败惩罚：如果是主将阵亡，兵力清零
            if ("KILLED".equals(general.getStatus())) army.clearTroops();
        }

        // 保存所有状态
        userProfileRepository.save(user);
        generalRepository.save(general);
    }
}