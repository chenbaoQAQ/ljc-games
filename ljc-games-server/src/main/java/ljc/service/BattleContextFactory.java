package ljc.service;

import ljc.dto.BattleContext;
import ljc.dto.BattleContext.CombatSide;
import ljc.dto.TroopGroup;
import ljc.entity.UserGeneral;
import ljc.entity.UserTroop;
import ljc.mapper.UserGeneralMapper;
import ljc.mapper.UserTroopMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BattleContextFactory {

    @Autowired
    private UserGeneralMapper generalMapper;

    @Autowired
    private UserTroopMapper userTroopMapper; // ✅ 改用新的 Inventory Mapper

    /**
     * 🏭 核心生产线：组装战场
     * @param userId 玩家ID
     * @param generalId 出征武将ID
     * @param stageId 关卡ID (目前先模拟，不查库)
     * @param troopConfig 出征兵力配置 (如: {"INFANTRY": 100, "ARCHER": 50})
     */
    public BattleContext createContext(Long userId, Integer generalId, Integer stageId, Map<String, Integer> troopConfig) {
        BattleContext ctx = new BattleContext();

        // 1. 组装进攻方 (Attacker) - 玩家
        UserGeneral general = generalMapper.selectById(generalId);
        if (general == null) throw new RuntimeException("出征武将不存在");
        // v2.2 校验：如果武将没激活或者在休息，应该报错 (此处暂跳过，由 Controller 校验)

        CombatSide attacker = new CombatSide();
        attacker.setName("我方-" + general.getId()); // 暂时用 ID 代替名字，或者去读配置
        attacker.setGeneralMaxHp(general.getMaxHp());
        attacker.setGeneralHp(general.getCurrentHp());
        attacker.setGeneralAtk(general.getAtk());

        // 2. 组装我方兵团 (核心修正点！)
        Map<String, TroopGroup> myTroops = new HashMap<>();

        for (Map.Entry<String, Integer> entry : troopConfig.entrySet()) {
            String type = entry.getKey();     // e.g., "INFANTRY"
            Integer deployCount = entry.getValue(); // e.g., 100

            if (deployCount <= 0) continue;

            // 🔍 校验库存：玩家真的有这么多兵吗？
            UserTroop stock = userTroopMapper.selectByType(userId, type);
            if (stock == null || stock.getCount() < deployCount) {
                throw new RuntimeException("兵力不足: " + type);
            }

            // 📊 获取属性：不再查库，直接获取静态配置
            TroopStats stats = getTroopStats(type);

            // 创建战斗单位
            // TroopGroup(type, count, atk, hp)
            TroopGroup group = new TroopGroup(type, deployCount, stats.atk, stats.hp);
            myTroops.put(type, group);
        }

        attacker.setTroops(myTroops);
        ctx.setAttacker(attacker);

        // 3. 组装防守方 (Defender) - 模拟敌人
        // 既然 StageConfig 删了，我们先在这里硬编码一个敌人用于测试
        mockDefender(ctx);

        return ctx;
    }

    // --- 内部辅助类与方法 ---

    // 模拟敌人数据
    private void mockDefender(BattleContext ctx) {
        CombatSide defender = new CombatSide();
        defender.setName("敌方-测试守军");
        defender.setGeneralHp(0); // 假设无武将
        defender.setGeneralMaxHp(0);
        defender.setGeneralAtk(0);

        Map<String, TroopGroup> enemyTroops = new HashMap<>();
        // 假想敌：200 个步兵
        TroopStats infStats = getTroopStats("INFANTRY");
        enemyTroops.put("INFANTRY", new TroopGroup("INFANTRY", 200, infStats.atk, infStats.hp));

        defender.setTroops(enemyTroops);
        ctx.setDefender(defender);
    }

    /**
     * 📖 静态配置表 (替代了原来的 troop_template 数据库表)
     * 在 Steam 单机版里，这种基础数值写死在代码或 JSON 里更高效
     */
    private TroopStats getTroopStats(String type) {
        switch (type) {
            case "INFANTRY": return new TroopStats(10, 100); // 攻10 血100
            case "ARCHER":   return new TroopStats(20, 50);  // 攻20 血50
            case "CAVALRY":  return new TroopStats(15, 80);  // 攻15 血80
            case "ELITE":    return new TroopStats(30, 120); // 特种兵
            default: throw new RuntimeException("未知兵种: " + type);
        }
    }

    // 简单的数值容器
    private static class TroopStats {
        int atk;
        int hp;
        public TroopStats(int atk, int hp) { this.atk = atk; this.hp = hp; }
    }
}