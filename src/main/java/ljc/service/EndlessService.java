package ljc.service;

import ljc.entity.StageConfig;
import ljc.entity.UserProfile;
import ljc.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
// 无尽挑战：根据层数动态计算关卡难度，不依赖固定的数据库配置
public class EndlessService {

    @Autowired
    private UserProfileRepository profileRepository;

    /**
     * 根据当前层数生成一个虚拟关卡
     * @param floor 当前层数
     */
    public StageConfig generateEndlessStage(int floor) {
        StageConfig stage = new StageConfig();
        stage.setId(9999 + floor); // 虚拟ID
        stage.setStageName("无尽远征 - 第 " + floor + " 层");

        // 难度公式：血量随层数线性增加，攻击力按百分比递增
        stage.setEnemyBaseHp(1000 + (floor * 500));

        // 1.0 + 0.05 * floor 的难度加成
        double atkFactor = 1.0 + (floor * 0.05);
        stage.setEnemyAtkBuff(BigDecimal.valueOf(atkFactor));

        // 爬塔奖励更高
        stage.setGoldReward(200 + (floor * 50));
        stage.setDiamondReward(1); // 每层给1个钻石

        return stage;
    }

    /**
     * 玩家通关后更新最高层数
     */
    public void updateMaxFloor(Integer userId, int completedFloor) {
        UserProfile profile = profileRepository.findById(userId).get();
        // 假设我们在 UserProfile 增加了 towerFloor 字段
        // profile.setTowerFloor(Math.max(profile.getTowerFloor(), completedFloor + 1));
        profileRepository.save(profile);
    }
}