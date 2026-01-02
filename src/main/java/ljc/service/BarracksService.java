package ljc.service;

import ljc.entity.UnitConfig;
import ljc.entity.UserGeneral;
import ljc.entity.UserProfile;
import ljc.model.Army;
import ljc.repository.UnitConfigRepository;
import ljc.repository.UserGeneralRepository;
import ljc.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
// 兵营系统：负责招募士兵并将兵力保存到武将身上
public class BarracksService {

    @Autowired
    private UserGeneralRepository generalRepo;
    @Autowired
    private UserProfileRepository profileRepo;
    @Autowired
    private UnitConfigRepository unitRepo;

    /**
     * 招募士兵
     * @param generalId 给哪个武将招兵
     * @param unitName 兵种名
     * @param count 数量
     */
    @Transactional
    public String recruitTroops(Integer userId, Integer generalId, String unitName, int count) {
        UserProfile profile = profileRepo.findById(userId).get();
        UserGeneral general = generalRepo.findById(generalId).get();
        UnitConfig unit = unitRepo.findByUnitName(unitName)
                .orElseThrow(() -> new RuntimeException("未知兵种"));

        // 1. 计算费用 (假设每个兵 10 金币)
        int totalCost = count * 10;
        if (profile.getGold() < totalCost) {
            return "【兵营】金币不足，招募失败！";
        }

        // 2. 扣钱
        profile.setGold(profile.getGold() - totalCost);

        // 3. 更新兵力（这里简单模拟，后期需接入 JSON 解析）
        // 暂时直接增加逻辑兵力数
        general.setCurrentArmyCount(general.getCurrentArmyCount() + count);

        profileRepo.save(profile);
        generalRepo.save(general);

        return String.format("【兵营】招募成功！%s 麾下新增了 %d 名 %s", general.getName(), count, unitName);
    }
}