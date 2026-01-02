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
 * 确保所有字段（包括新增的 mainEnemyType 和 baseAtk）都有初始值，防止运行报错。
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
        // 包含基础兵种和四国特种兵
        initUnit("INFANTRY", 1, 15, 100, "NONE", 1.0);
        initUnit("ARCHER", 1, 20, 80, "NONE", 1.0);
        initUnit("CAVALRY", 2, 40, 200, "NONE", 1.0);

        // 特种兵：CN强化步兵，JP强化弓兵，KR强化骑兵，EN强化武将
        initUnit("CN_SPECIAL", 3, 30, 250, "INFANTRY", 2.0);
        initUnit("JP_SPECIAL", 3, 35, 200, "ARCHER", 2.0);
        initUnit("KR_SPECIAL", 3, 45, 300, "CAVALRY", 2.0);
        initUnit("EN_SPECIAL", 3, 25, 400, "HERO", 0.2);

        // --- 2. 武将模板初始化 (GeneralTemplate) ---
        if (templateRepo.findById(101).isEmpty()) {
            GeneralTemplate t = new GeneralTemplate();
            t.setId(101);
            t.setName("五虎上将模板");
            t.setBaseAtk(60);
            t.setBaseHp(1500);
            t.setRarity("SSR");
            templateRepo.save(t);
        }

        // --- 3. 玩家存档初始化 (UserProfile) ---
        if (profileRepo.findById(1).isEmpty()) {
            UserProfile p = new UserProfile();
            p.setUserId(1);
            p.setGold(5000); // 初始金币多给点，方便测试
            p.setDiamond(200);
            p.setUnlockedCountries("CN,JP,KR,EN");
            profileRepo.save(p);
        }

        // --- 4. 战术关卡初始化 (StageConfig) ---
        // 💡 重点：我们将第一关设定为骑兵主力，测试你的弓兵集火！
        StageConfig stage = stageRepo.findById(1).orElse(new StageConfig());
        stage.setId(1);
        stage.setStageName("大鹿泽 (铁骑突袭)");
        stage.setEnemyBaseHp(3000);      // 提高血量，让混战打得久一点
        stage.setMainEnemyType("CAVALRY"); // 敌军主力：骑兵
        stage.setGoldReward(500);
        stage.setDiamondReward(50);
        stage.setLootRate(BigDecimal.valueOf(0.5));
        stage.setEnemyAtkBuff(BigDecimal.valueOf(1.0));
        stageRepo.save(stage);

        // --- 5. 测试武将初始化 (UserGeneral) ---
        // 💡 重点：补全 baseAtk 和实时血量，防止 CombatEngine 报错
        if (generalRepo.findById(1).isEmpty()) {
            UserGeneral g = new UserGeneral();
            g.setId(1);
            g.setName("赵云");
            g.setTemplateId(101);
            g.setUserId(1);
            g.setPersonality("BRAVE"); // 勇敢性格：伤害加成
            g.setStatus("HEALTHY");

            // 数值同步
            g.setBaseAtk(60);
            g.setBaseHp(1500);
            g.setMaxHp(1500);
            g.setCurrentHp(1500);
            g.setLevel(1);
            g.setCurrentExp(0);
            g.setCurrentArmyCount(300); // 初始带300兵

            generalRepo.save(g);
        }

        System.out.println(">>> [系统初始化] 数据注入完成！策划大人可以启动战斗测试了。");
    }

    /**
     * 兵种初始化辅助方法
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
            System.out.println(">> [兵种配置] 已加载: " + name);
        }
    }
}