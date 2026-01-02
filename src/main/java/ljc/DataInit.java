package ljc;

import ljc.entity.*;
import ljc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Optional;

@Component
// 数据初始化：为系统注入测试用的“全量”数据
public class DataInit implements CommandLineRunner {

    @Autowired private StageConfigRepository stageRepo;
    @Autowired private UserGeneralRepository generalRepo;
    @Autowired private UserProfileRepository profileRepo;
    @Autowired private GeneralTemplateRepository templateRepo;
    @Autowired private UnitConfigRepository unitRepo;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [系统初始化] 开始注入适配新版本的战斗数据...");

        // --- 1. 初始化兵种配置 (UnitConfig) ---
        // 这是计算特种兵加成的核心，必须先有这些数据
        initUnit("INFANTRY", 1, 15, 100, "NONE", 1.0);
        initUnit("ARCHER", 1, 20, 80, "NONE", 1.0);
        initUnit("CAVALRY", 2, 40, 200, "NONE", 1.0);
        initUnit("CN_SPECIAL", 3, 30, 250, "INFANTRY", 2.0); // 步兵统领

        // --- 2. 创建武将模板 (GeneralTemplate) ---
        if (templateRepo.findById(101).isEmpty()) {
            GeneralTemplate t = new GeneralTemplate();
            t.setId(101);
            t.setName("五虎上将模板");
            t.setBaseAtk(60);
            t.setBaseHp(1500); // 模板血量给高一点
            t.setRarity("SSR");
            templateRepo.save(t);
            System.out.println(">>> [模板] 赵云所属模板已创建");
        }

        // --- 3. 初始化关卡配置 (StageConfig) ---
        // 增加奖励和掉落率
        StageConfig stage = stageRepo.findById(1).orElse(new StageConfig());
        stage.setId(1);
        stage.setStageName("大鹿泽 (首战黄巾)");
        stage.setEnemyBaseHp(2000); // 提高敌人血量，让战斗能打满3回合PK
        stage.setGoldReward(500);
        stage.setDiamondReward(20);
        stage.setLootRate(BigDecimal.valueOf(0.3)); // 30% 掉宝率
        stage.setEnemyAtkBuff(BigDecimal.valueOf(1.0)); // 1倍难度
        stageRepo.save(stage);
        System.out.println(">>> [关卡] 初始关卡数据已同步");

        // --- 4. 初始化玩家存档 (UserProfile) ---
        if (profileRepo.findById(1).isEmpty()) {
            UserProfile p = new UserProfile();
            p.setUserId(1);
            p.setGold(2000); // 给点初始资金招兵
            p.setDiamond(100);
            p.setUnlockedCountries("CN");
            profileRepo.save(p);
            System.out.println(">>> [玩家] 存档已建立");
        }

        // --- 5. 初始化测试武将 (UserGeneral) ---
        // 补全所有新字段：技能、实时血量、等级
        if (generalRepo.findById(1).isEmpty()) {
            UserGeneral g = new UserGeneral();
            g.setId(1);
            g.setName("赵云");
            g.setTemplateId(101);
            g.setUserId(1);
            g.setPersonality("BRAVE");
            g.setStatus("HEALTHY");

            // 重要：同步血量数值，不要再给100了
            g.setBaseAtk(60);
            g.setBaseHp(1500);
            g.setMaxHp(1500);
            g.setCurrentHp(1500);

            // 初始技能设定
            g.setActiveSkillName("七进七出");
            g.setSkillDamageRatio(2.0); // 2倍伤害
            g.setSkillTriggerChance(0.3); // 30% 概率

            g.setLevel(1);
            g.setCurrentExp(0);
            g.setCurrentArmyCount(200); // 初始带200兵

            generalRepo.save(g);
            System.out.println(">>> [武将] 赵云已全副武装就绪！");
        }

        System.out.println(">>> [系统初始化] 全部绿色通过！策划大人可以开战了。");
    }

    // 辅助方法：快速初始化兵种
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
            System.out.println(">>> [兵种] " + name + " 配置已加载");
        }
    }
}