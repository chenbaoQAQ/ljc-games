package ljc.service;

import ljc.entity.Equipment;
import ljc.entity.UserProfile;
import ljc.repository.EquipmentRepository;
import ljc.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// 武器工坊：消耗金币直接强化数值
public class WeaponService {

    @Autowired
    private EquipmentRepository equipmentRepo;
    @Autowired
    private UserProfileRepository profileRepo;

    /**
     * 强化武器
     * @param userId 玩家ID
     * @param equipId 装备ID
     */
    @Transactional
    public String strengthenWeapon(Integer userId, Integer equipId) {
        UserProfile user = profileRepo.findById(userId).get();
        Equipment equip = equipmentRepo.findById(equipId)
                .orElseThrow(() -> new RuntimeException("装备不存在"));

        // 强化费用公式：等级 * 500
        int cost = equip.getLevel() * 500;

        if (user.getGold() < cost) {
            return "【工坊】金币不足！强化需要 " + cost + " 金币。";
        }

        // 扣钱并升级
        user.setGold(user.getGold() - cost);
        equip.upgrade(); // 调用我们之前写的 upgrade 方法，加攻击力

        profileRepo.save(user);
        equipmentRepo.save(equip);

        return String.format("【工坊】强化成功！[%s] 升至 Lv.%d，攻击加成提升至 %d！",
                equip.getEquipName(), equip.getLevel(), equip.getAtkBonus());
    }
}