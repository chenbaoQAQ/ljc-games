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

    /** 招募：进入仓库 */
    @Transactional
    public String recruitTroops(Integer userId, Integer generalId, String unitName, int count) {
        UserProfile profile = profileRepo.findById(userId).get();
        UserGeneral general = generalRepo.findById(generalId).get();
        UnitConfig unit = unitRepo.findByUnitName(unitName).get();

        int totalCost = count * unit.getBaseAtk(); // 单价
        if (profile.getGold() < totalCost) return "金币不足";

        profile.setGold(profile.getGold() - totalCost);

        // 加载仓库 (Reserve)
        Army reserve = new Army();
        reserve.fromJson(general.getReserveArmyConfigStr(), unitRepo);

        int current = reserve.getTroopMap().getOrDefault(unit, 0);
        reserve.getTroopMap().put(unit, current + count);

        general.setReserveArmyConfigStr(reserve.toJson()); // 存入仓库字段

        profileRepo.save(profile);
        generalRepo.save(general);
        return String.format("成功招募 %d 名 %s 进入仓库", count, unit.getUnitName());
    }

    /** 分配：仓库 -> 阵前 (战斗实际带走的兵) */
    @Transactional
    public String assignTroops(Integer generalId, Map<String, Integer> assignmentMap) {
        UserGeneral general = generalRepo.findById(generalId).get();
        Army reserve = new Army();
        reserve.fromJson(general.getReserveArmyConfigStr(), unitRepo);

        Army active = new Army(); // 阵前部队
        int spaceUsed = 0;

        for (Map.Entry<String, Integer> entry : assignmentMap.entrySet()) {
            UnitConfig unit = unitRepo.findByUnitName(entry.getKey()).get();
            int want = entry.getValue();
            int have = reserve.getTroopMap().getOrDefault(unit, 0);

            if (want > have) return "分配失败：仓库兵力不足";

            spaceUsed += want * unit.getSpaceCost();
            active.getTroopMap().put(unit, want);
            // 扣除仓库
            reserve.getTroopMap().put(unit, have - want);
        }

        if (spaceUsed > general.getMaxLeadership()) return "超过统帅上限";

        general.setArmyConfigStr(active.toJson()); // 存入阵前字段
        general.setReserveArmyConfigStr(reserve.toJson());
        general.setCurrentArmyCount(spaceUsed);

        generalRepo.save(general);
        return "SUCCESS";
    }
}
