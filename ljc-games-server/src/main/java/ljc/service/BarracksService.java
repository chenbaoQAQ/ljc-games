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
        UnitConfig unit = unitRepo.findByUnitName(unitName)
                .orElseThrow(() -> new RuntimeException("找不到兵种配置：" + unitName));

        // 1. 动态获取单价：不再硬编码 10，而是读取数据库配置
        int unitCost = unit.getBaseAtk(); // 假设你用 BaseAtk 存储单价，或者根据你的实体类调整
        int totalCost = count * unitCost;

        if (profile.getGold() < totalCost) return "【兵营】金币不足！";

        profile.setGold(profile.getGold() - totalCost);

        // 2. 核心修复：读旧 JSON -> 累加 -> 写新 JSON
        Army army = new Army();
        army.fromJson(general.getArmyConfigStr(), unitRepo);

        int current = army.getTroopMap().getOrDefault(unit, 0);
        army.getTroopMap().put(unit, current + count);

        // 将最新的 JSON 字符串存回字段
        general.setArmyConfigStr(army.toJson());
        general.setCurrentArmyCount(army.getTotalUnitCount());

        profileRepo.save(profile);
        generalRepo.save(general);
        return String.format("成功为 %s 招募 %d 名 %s！", general.getName(), count, unit.getUnitName());
    }
}