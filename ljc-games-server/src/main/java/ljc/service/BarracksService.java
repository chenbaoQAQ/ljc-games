package ljc.service;

import ljc.entity.*;
import ljc.model.Army;
import ljc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BarracksService {
    @Autowired private UserGeneralRepository generalRepo;
    @Autowired private UserProfileRepository profileRepo;
    @Autowired private UnitConfigRepository unitRepo;

    @Transactional
    public String recruitTroops(Integer userId, Integer generalId, String unitName, int count) {
        UserProfile profile = profileRepo.findById(userId).get();
        UserGeneral general = generalRepo.findById(generalId).get();
        UnitConfig unit = unitRepo.findByUnitName(unitName).orElseThrow();

        int totalCost = count * 10;
        if (profile.getGold() < totalCost) return "【兵营】金币不足！";

        profile.setGold(profile.getGold() - totalCost);

        // 💡 核心：读旧 JSON -> 累加 -> 写新 JSON
        Army army = new Army();
        army.fromJson(general.getArmyConfigStr(), unitRepo);

        int current = army.getTroopMap().getOrDefault(unit, 0);
        army.getTroopMap().put(unit, current + count);

        general.setArmyConfigStr(army.toJson());
        general.setCurrentArmyCount(army.getTotalUnitCount());

        profileRepo.save(profile);
        generalRepo.save(general);
        return String.format("成功为 %s 招募 %d 名 %s！", general.getName(), count, unitName);
    }
}