package ljc;

import ljc.entity.*;
import ljc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DataInit implements CommandLineRunner {

    // 1. 注入所有需要的仓库接口
    @Autowired private StageConfigRepository stageRepo;
    @Autowired private UserGeneralRepository generalRepo;
    @Autowired private UserProfileRepository profileRepo;
    @Autowired private GeneralTemplateRepository templateRepo; // 解决你刚才报红的关键

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [系统初始化] 开始注入测试数据...");

        // --- 第一步：创建武将模板 (父表数据) ---
        // 必须先有模板，赵云才能通过 template_id 找到依靠
        if (templateRepo.findById(101).isEmpty()) {
            GeneralTemplate t = new GeneralTemplate();
            t.setId(101);
            t.setName("五虎上将模板");
            t.setBaseAtk(50);
            t.setBaseHp(200);
            t.setRarity("LEGENDARY"); // 明确设置这个值
            templateRepo.save(t);
            System.out.println(">>> [1/4] 模板数据: 已就绪");
        }

        // --- [2/4] 关卡配置数据 (StageConfig) ---
        // 不再只是 isEmpty() 时插入，而是强制设置数值，确保数据库里永远是最全的
        StageConfig stage = stageRepo.findById(1).orElse(new StageConfig());
        stage.setId(1);
        stage.setStageName("大鹿泽 (黄巾起义)");
        stage.setEnemyBaseHp(500);      // 补齐血量
        stage.setGoldReward(100);       // 补齐金币
        stage.setDiamondReward(10);     // 补齐钻石
        stage.setLootRate(BigDecimal.valueOf(0.5));        // 补齐掉率
        stage.setEnemyAtkBuff(BigDecimal.valueOf(1.2));     // 补齐难度系数
        stageRepo.save(stage);          // save 会自动判断：ID存在就更新，不存在就插入
        System.out.println(">>> [2/4] 关卡数据: 已同步最新数值");

        // --- 第三步：初始化玩家存档 ---
        if (profileRepo.findById(1).isEmpty()) {
            UserProfile p = new UserProfile();
            p.setUserId(1);
            p.setGold(500);
            p.setDiamond(50);
            p.setUnlockedCountries("CN"); // 默认解锁中国（蜀魏吴）
            profileRepo.save(p);
            System.out.println(">>> [3/4] 玩家存档: 已就绪");
        }

        // --- 第四步：初始化测试武将 (子表数据) ---
        // 引用了 template_id=101 和 user_id=1
        if (generalRepo.findById(1).isEmpty()) {
            UserGeneral g = new UserGeneral();
            g.setId(1);
            g.setName("赵云");
            g.setTemplateId(101);
            g.setUserId(1);
            g.setPersonality("BRAVE"); // 勇敢性格：攻击+10%
            g.setStatus("HEALTHY");    // 初始状态：健康
            g.setMaxHp(100);
            g.setCurrentHp(100);
            generalRepo.save(g);
            System.out.println(">>> [4/4] 武将数据: 已就绪");
        }

        System.out.println(">>> [系统初始化] 测试数据注入完成！你可以发起战斗了。");
    }
}