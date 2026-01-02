package ljc.service;

import ljc.entity.Equipment;
import ljc.entity.StageConfig;
import ljc.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
//战后摸奖。它会根据关卡的爆率，决定玩家能不能拿到新装备。
public class LootService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    private final Random random = new Random();

    /**
     * 掉落逻辑判定
     * 目前先返回简单的字符串，保证 BattleService 逻辑通顺
     */
    public String dropEquipment(Integer userId, StageConfig stage) {
        // 后续我们会在这里编写基于 stage.getLootRate() 的随机生成逻辑
        return "【系统】战斗结束，当前关卡暂无装备掉落。";
    }
}