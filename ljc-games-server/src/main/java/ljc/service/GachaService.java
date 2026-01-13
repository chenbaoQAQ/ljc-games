package ljc.service;

import ljc.entity.GeneralTemplate;
import ljc.entity.UserGeneral;
import ljc.entity.UserProfile;
import ljc.repository.GeneralTemplateRepository;
import ljc.repository.UserGeneralRepository;
import ljc.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
// 抽卡中心：负责消耗钻石并为玩家生成新的武将实例
public class GachaService {

    @Autowired
    private GeneralTemplateRepository templateRepo;
    @Autowired
    private UserGeneralRepository generalRepo;
    @Autowired
    private UserProfileRepository profileRepo;

    private final Random random = new Random();

    // 抽卡性格库
    private final String[] personalities = {"BRAVE", "CALM", "RASH", "CAUTIOUS"};

    /**
     * 单次抽卡逻辑
     * @param userId 玩家ID
     * @param cost 消耗钻石数
     */
    @Transactional
    public UserGeneral drawGeneral(Integer userId, int cost) {
        // 1. 检查并扣除钻石
        UserProfile profile = profileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (profile.getDiamond() < cost) {
            throw new RuntimeException("钻石不足，请去充值或通过关卡获得！");
        }
        profile.setDiamond(profile.getDiamond() - cost);
        profileRepo.save(profile);

        // 2. 随机获取一个武将模版
        List<GeneralTemplate> templates = templateRepo.findAll();
        if (templates.isEmpty()) {
            throw new RuntimeException("点将台目前没有武将模版，请先初始化数据！");
        }
        GeneralTemplate template = templates.get(random.nextInt(templates.size()));

        // 3. 实例化为玩家武将
        UserGeneral newGeneral = new UserGeneral();
        newGeneral.setUserId(userId);
        newGeneral.setTemplateId(template.getId());
        newGeneral.setName(template.getName());
        newGeneral.setMaxHp(template.getBaseHp());
        newGeneral.setCurrentHp(template.getBaseHp());
        // 随机赋予一种性格
        newGeneral.setPersonality(personalities[random.nextInt(personalities.length)]);
        newGeneral.setStatus("HEALTHY");
        newGeneral.setLevel(1);
        newGeneral.setCurrentExp(0);

        // 带兵数量
        newGeneral.setMaxLeadership(template.getBaseLeadership());

        return generalRepo.save(newGeneral);
    }
}