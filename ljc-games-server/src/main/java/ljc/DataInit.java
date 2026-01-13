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
 * 包含：基础兵种、三国名将模板、初始玩家存档、多级战术关卡、文明特种兵
 */
public class DataInit implements CommandLineRunner {

    @Autowired private StageConfigRepository stageRepo;
    @Autowired private UserGeneralRepository generalRepo;
    @Autowired private UserProfileRepository profileRepo;
    @Autowired private GeneralTemplateRepository templateRepo;
    @Autowired private UnitConfigRepository unitRepo;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [系统初始化] 正在同步文明战术版本测试数据...");

        // --- 1. 兵种配置初始化 (UnitConfig) ---
        // 基础兵种：容量、攻击、血量
        initUnit("INFANTRY", 1, 15, 120, "NONE", 1.0);
        initUnit("ARCHER", 1, 22, 90, "NONE", 1.0);
        initUnit("CAVALRY", 2, 45, 250, "NONE", 1.0);

        // 文明特种兵：大幅提升数值，体现文明选择的价值
        // CN(大汉)：汉之羽林 - 极高生存与反击
        initUnit("CN_SPECIAL", 2, 35, 450, "INFANTRY", 1.5);
        // JP(东瀛)：大和武士 - 极高爆发输出
        initUnit("JP_SPECIAL", 2, 55, 180, "ARCHER", 1.5);
        // KR(高丽)：高丽铁骑 - 坦克级的血量与冲锋
        initUnit("KR_SPECIAL", 3, 65, 600, "CAVALRY", 1.5);
        // GB(不列颠)：长弓勇士 - 极远距离压制 (英雄流辅助)
        initUnit("GB_SPECIAL", 2, 40, 350, "HERO", 1.2);

        // 修改后的 DataInit.java 调用代码
        if (templateRepo.findById(101).isEmpty()) saveTemplate(101, "赵云", 65, 1600, 120, "SSR");
        if (templateRepo.findById(102).isEmpty()) saveTemplate(102, "吕布", 110, 2000, 150, "UR");
        if (templateRepo.findById(103).isEmpty()) saveTemplate(103, "关羽", 90, 1800, 150, "SSR");
        if (templateRepo.findById(104).isEmpty()) saveTemplate(104, "诸葛亮", 45, 1300, 100, "SSR");

        // --- 3. 玩家存档初始化 (UserProfile) ---
        if (profileRepo.findById(1).isEmpty()) {
            UserProfile p = new UserProfile();
            p.setUserId(1);
            p.setGold(8000);   // 给点初始启动资金
            p.setDiamond(500);
            p.setUnlockedCountries(""); // 留空强制触发前端：文明选择页面
            profileRepo.save(p);
        }

        // --- 4. 战术关卡初始化 (StageConfig) ---
        saveStage(1, "大鹿泽 (铁骑突袭)", 4000, "CAVALRY", 1.0);
        saveStage(2, "虎牢关外 (步兵方阵)", 6000, "INFANTRY", 1.1);
        saveStage(3, "长坂坡 (箭雨封锁)", 8000, "ARCHER", 1.2);
        saveStage(5, "华容道 (曹操败走)", 12000, "CAVALRY", 1.4); // 第5关小BOSS
        saveStage(10, "下邳城 (战神吕布)", 25000, "INFANTRY", 1.8); // 第10关大BOSS

        // --- 5. 测试初始武将 (给玩家一个初始赵云) ---
        if (generalRepo.findById(1).isEmpty()) {
            UserGeneral g = new UserGeneral();
            g.setId(1);
            g.setName("赵云");
            g.setTemplateId(101);
            g.setUserId(1);
            g.setPersonality("BRAVE");
            g.setStatus("HEALTHY");
            g.setBaseAtk(65);
            g.setBaseHp(1600);
            g.setMaxHp(1600);
            g.setCurrentHp(1600);
            g.setLevel(1);
            g.setCurrentArmyCount(0); // 让玩家选完国家去招募特种兵
            generalRepo.save(g);
        }

        System.out.println(">>> [系统初始化] 文明版数据注入完成！");
    }

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

    private void saveTemplate(int id, String name, int atk, int hp, int leadership, String rarity) {
        GeneralTemplate t = new GeneralTemplate();
        t.setId(id);
        t.setName(name);
        t.setBaseAtk(atk);
        t.setBaseHp(hp);
        t.setBaseLeadership(leadership); // 注入带兵上限数据
        t.setRarity(rarity);
        templateRepo.save(t);
    }

    private void saveStage(int id, String name, int hp, String type, double atkBuff) {
        StageConfig stage = stageRepo.findById(id).orElse(new StageConfig());
        stage.setId(id);
        stage.setRegionId(1);
        stage.setStageName(name);
        stage.setEnemyBaseHp(hp);
        stage.setMainEnemyType(type);
        stage.setEnemyAtkBuff(BigDecimal.valueOf(atkBuff));
        stage.setGoldReward(500 + id * 200);
        stage.setDiamondReward(10 + id);
        double loot = (id % 5 == 0) ? 0.6 : 0.15; // 提高BOSS关掉率
        stage.setLootRate(BigDecimal.valueOf(loot));
        stageRepo.save(stage);
    }
}