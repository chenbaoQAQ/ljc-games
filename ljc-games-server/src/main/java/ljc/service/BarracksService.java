package ljc.service;

import ljc.entity.*;
import ljc.model.Army;
import ljc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class BarracksService {
    @Autowired private UserGeneralRepository generalRepo;
    @Autowired private UserProfileRepository profileRepo;
    @Autowired private UnitConfigRepository unitRepo;
    /**
     * 招募士兵：进入后备仓库
     */
    @Transactional
    public String recruitTroops(Integer userId, Integer generalId, String unitName, int count) {
        UserProfile profile = profileRepo.findById(userId).get();
        UserGeneral general = generalRepo.findById(generalId).get();
        UnitConfig unit = unitRepo.findByUnitName(unitName).get();

        int totalCost = count * unit.getBaseAtk(); // 假设单价
        if (profile.getGold() < totalCost) return "金币不足";

        profile.setGold(profile.getGold() - totalCost);

        // 加载仓库数据
        Army reserve = new Army();
        reserve.fromJson(general.getReserveArmyConfigStr(), unitRepo);

        // 增加仓库数量
        int current = reserve.getTroopMap().getOrDefault(unit, 0);
        reserve.getTroopMap().put(unit, current + count);

        general.setReserveArmyConfigStr(reserve.toJson());
        profileRepo.save(profile);
        generalRepo.save(general);
        return "成功招募并存入仓库";
    }

    /**
     * 分配兵力：从仓库拨到阵前
     * @param assignmentMap 格式如：{"INFANTRY": 20, "ARCHER": 10}
     */
    @Transactional
    public String assignTroops(Integer generalId, Map<String, Integer> assignmentMap) {
        UserGeneral general = generalRepo.findById(generalId).get();

        Army reserve = new Army();
        reserve.fromJson(general.getReserveArmyConfigStr(), unitRepo);

        Army active = new Army(); // 准备上阵的部队

        int requiredSpace = 0;

        for (Map.Entry<String, Integer> entry : assignmentMap.entrySet()) {
            UnitConfig unit = unitRepo.findByUnitName(entry.getKey()).get();
            int wantCount = entry.getValue();
            int haveCount = reserve.getTroopMap().getOrDefault(unit, 0);

            // 1. 校验库存是否足够
            if (wantCount > haveCount) return "分配失败：仓库中 " + entry.getKey() + " 数量不足";

            // 2. 累计统帅值空间
            requiredSpace += wantCount * unit.getSpaceCost();
            active.getTroopMap().put(unit, wantCount);
        }

        // 3. 校验统帅值是否超限
        if (requiredSpace > general.getMaxLeadership()) {
            return "分配失败：超出统帅值上限 " + general.getMaxLeadership();
        }

        // 4. 执行扣除库存并更新上阵
        for (Map.Entry<UnitConfig, Integer> entry : active.getTroopMap().entrySet()) {
            int currentReserve = reserve.getTroopMap().get(entry.getKey());
            reserve.getTroopMap().put(entry.getKey(), currentReserve - entry.getValue());
        }

        general.setArmyConfigStr(active.toJson());
        general.setReserveArmyConfigStr(reserve.toJson());
        general.setCurrentArmyCount(requiredSpace); // 更新前端显示的当前占用

        generalRepo.save(general);
        return "部队分配成功，准备开战！";
    }
}
