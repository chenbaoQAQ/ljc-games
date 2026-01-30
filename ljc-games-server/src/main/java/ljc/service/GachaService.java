package ljc.service;

import ljc.entity.UserGeneral;
import ljc.entity.GeneralTemplate;
import ljc.mapper.UserGeneralMapper; // 还没建，一会儿建
import ljc.mapper.GeneralTemplateMapper; // 还没建，一会儿建
import ljc.mapper.UserProfileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class GachaService {

    @Autowired
    private UserProfileMapper userProfileMapper;

    // 假设下面两个 Mapper 我们一会儿就会创建
    @Autowired
    private GeneralTemplateMapper templateMapper;
    @Autowired
    private UserGeneralMapper userGeneralMapper;

    private final Random random = new Random();

    /**
     * 单抽逻辑
     * 主公请填空：我们要保证扣钱和发武将要么都成功，要么都失败。
     * 应该在方法上加什么注解？
     */
    @Transactional(rollbackFor = Exception.class) // 👈 填空 1
    public String drawGeneral(Integer userId) {
        int cost = 100; // 单抽价格

        // 1. 先扣钱 (利用刚才写的原子 SQL)
        //int rows 是 MyBatis 执行 update 语句后的返回值
        int rows = userProfileMapper.decreaseGold(userId, (long) cost);
        if (rows == 0) {
            throw new RuntimeException("金币不足，无法招募！");
        }

        // 2. 随机抽一个武将 (这里先简化，假设只有 ID 101 的赵云)
        // 实际项目这里要读配置表算权重
        int templateId = 101;

        // 3. 实例化武将 (发货)
        UserGeneral newGeneral = new UserGeneral();
        newGeneral.setUserId(userId);
        newGeneral.setTemplateId(templateId); // 关联到赵云模版
        newGeneral.setName("赵云"); // 暂时写死，后续从 Template 表查
        newGeneral.setMaxHp(1000); // 暂时写死
        newGeneral.setCurrentHp(1000);
        newGeneral.setStatus("HEALTHY");

        return "招募成功";
    }
}