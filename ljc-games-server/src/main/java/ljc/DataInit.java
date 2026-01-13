package ljc;

import ljc.entity.*;
import ljc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
/**
 * 系统初始化：为项目注入全量测试数据
 * 包含：基础兵种、三国名将模板、初始玩家存档、多级战术关卡
 */
public class DataInit implements CommandLineRunner {

    @Autowired private StageConfigRepository stageRepo;
    @Autowired private UserGeneralRepository generalRepo;
    @Autowired private UserProfileRepository profileRepo;
    @Autowired private GeneralTemplateRepository templateRepo;
    @Autowired private UnitConfigRepository unitRepo;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [系统初始化] 正在同步战术版本测试数据...");

        // --- 1. 兵种配置初始化 (UnitConfig) ---
        // 基础兵种：容量、攻击、血量
        initUnit("INFANTRY", 1, 15, 100, "NONE", 1.0);
        initUnit("ARCHER", 1, 20, 80, "NONE", 1.0);
        initUnit("CAVALRY", 2, 40, 200, "NONE", 1.0);

        // 特种兵：CN强化步兵，JP强化弓兵，KR强化骑兵，EN强化武将(英雄流)
        initUnit("CN_SPECIAL", 3, 30, 250, "INFANTRY", 2.0);
        initUnit("JP_SPECIAL", 3, 35, 200, "ARCHER", 2.0);
        initUnit("KR_SPECIAL", 3, 45, 300, "CAVALRY", 2.0);
        initUnit("EN_SPECIAL", 3, 25, 400, "HERO", 0.2);

        // --- 2. 武将模板初始化 (GeneralTemplate) ---
        // 模板 101：赵云 (均衡型)
        if (templateRepo.findById(101).isEmpty()) {
            saveTemplate(101, "赵云", 60, 1500, "SSR");
        }
        // 模板 102：吕布 (极高攻击，割草型)
        if (templateRepo.findById(102).isEmpty()) {
            saveTemplate(102, "吕布", 100, 1800, "UR");
        }
        // 模板 103：关羽 (高血量，肉盾型)
        if (templateRepo.findById(103).isEmpty()) {
            saveTemplate(103, "关羽", 85, 1600, "SSR");
        }
        // 模板 104：诸葛亮 (低攻击，适合带EN特种兵走英雄流)
        if (templateRepo.findById(104).isEmpty()) {
            saveTemplate(104, "诸葛亮", 40, 1200, "SSR");
        }

        // --- 3. 玩家存档初始化 (UserProfile) ---
        if (profileRepo.findById(1).isEmpty()) {
            UserProfile p = new UserProfile();
            p.setUserId(1);
            p.setGold(10000); // 初始金币给够，方便测试招募和强化
            p.setDiamond(500);
            p.setUnlockedCountries("CN,JP,KR,EN");
            profileRepo.save(p);
        }

        // --- 4. 战术关卡初始化 (StageConfig) ---
        // 第一关：主打骑兵 (引导玩家用弓兵克制)
        saveStage(1, "大鹿泽 (铁骑突袭)", 3000, "CAVALRY", 1.0);
        // 第二关：主打步兵 (引导玩家用骑兵克制)
        saveStage(2, "虎牢关外 (步兵方阵)", 4500, "INFANTRY", 1.1);
        // 第三关：主打弓兵 (引导玩家用步兵克制)
        saveStage(3, "长坂坡 (箭雨封锁)", 6000, "ARCHER", 1.2);
        // 第十关：BOSS关 (综合难度)
        saveStage(10, "下邳城 (战神吕布)", 15000, "CAVALRY", 1.5);

        // --- 5. 测试初始武将 (给玩家一个初始赵云) ---
        if (generalRepo.findById(1).isEmpty()) {
            UserGeneral g = new UserGeneral();
            g.setId(1);
            g.setName("赵云");
            g.setTemplateId(101);
            g.setUserId(1);
            g.setPersonality("BRAVE");
            g.setStatus("HEALTHY");
            g.setBaseAtk(60);
            g.setBaseHp(1500);
            g.setMaxHp(1500);
            g.setCurrentHp(1500);
            g.setLevel(1);
            g.setCurrentArmyCount(300); // 初始带300兵
            generalRepo.save(g);
        }

        System.out.println(">>> [系统初始化] 数据注入完成！");
    }

    /**
     * 辅助方法：初始化兵种
     */
    private void initUnit(String name, int cost, int atk, int hp, String target, double ratio) {
        if (unitRepo.findByUnitName(name).isEmpty()) {
            UnitConfig u = new UnitConfig();
            u.setUnitName(name);
            u.setSpaceCost(cost);
            u.setBaseAtk(atk);
            u.setBaseHp(hp);
            u.setTargetType(target);
            u.setBuffRatio(BigDecimal.valueOf(ratio));
            unitRepo.save(u);
        }
    }

    /**
     * 辅助方法：保存武将模板
     */
    private void saveTemplate(int id, String name, int atk, int hp, String rarity) {
        GeneralTemplate t = new GeneralTemplate();
        t.setId(id);
        t.setName(name);
        t.setBaseAtk(atk);
        t.setBaseHp(hp);
        t.setRarity(rarity);
        templateRepo.save(t);
    }

    /**
     * 辅助方法：快速保存关卡
     */
    private void saveStage(int id, String name, int hp, String type, double atkBuff) {
        StageConfig stage = stageRepo.findById(id).orElse(new StageConfig());
        stage.setId(id);
        stage.setRegionId(1); // 默认第一区域
        stage.setStageName(name);
        stage.setEnemyBaseHp(hp);
        stage.setMainEnemyType(type);
        stage.setEnemyAtkBuff(BigDecimal.valueOf(atkBuff));
        stage.setGoldReward(200 + id * 100);
        stage.setDiamondReward(5 + id);
        // 掉率逻辑：普通关 10%，逢 5 的倍数关（BOSS）掉率提升到 50%
        double loot = (id % 5 == 0) ? 0.5 : 0.1;
        stage.setLootRate(BigDecimal.valueOf(loot));
        stageRepo.save(stage);
    }
}