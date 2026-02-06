package ljc.service;

import ljc.entity.*;
import ljc.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HallService {

    private final UserGeneralMapper userGeneralMapper;
    private final UserMapper userMapper;
    private final GeneralTemplateMapper generalTemplateMapper;
    private final UserEquipmentMapper userEquipmentMapper;
    private final UserGemMapper userGemMapper;
    private final UserCivProgressMapper userCivProgressMapper;
    private final UserInventoryMapper userInventoryMapper;
    private final UserCivProgressMapper userCivProgressMapper;
    private final UserInventoryMapper userInventoryMapper;
    
    // 省略其他Mapper注入

    @Transactional(rollbackFor = Exception.class)
    public void upgradeGeneral(Long userId, Long generalId) {
        UserGeneralTbl general = userGeneralMapper.selectById(generalId);
        if (general == null || !general.getUserId().equals(userId)) {
            throw new RuntimeException("武将不存在");
        }
        
        // 检查等级上限 (基于 Tier)
        int maxLevel = 20 + (general.getTier() * 10); // 假定每阶+10级
        if (general.getLevel() >= maxLevel) {
            throw new RuntimeException("已达当前阶级等级上限，请先升阶");
        }

        // 简单金币升级公式: level * 100
        int cost = general.getLevel() * 100;
        int rows = userMapper.reduceGold(userId, cost);
        if (rows == 0) {
            throw new RuntimeException("金币不足");
        }

        general.setLevel(general.getLevel() + 1);
        
        // 简单属性成长：每级 HP + 50
        // 实际项目应读配置表成长系数
        general.setMaxHp(general.getMaxHp() + 50);
        general.setCurrentHp(general.getMaxHp()); 

        userGeneralMapper.update(general);
    }

    @Transactional(rollbackFor = Exception.class)
    public void equip(Long userId, Long generalId, Long equipmentId, String slot) {
        // 1. 校验武将
        UserGeneralTbl general = userGeneralMapper.selectById(generalId);
        if (general == null || !general.getUserId().equals(userId)) {
            throw new RuntimeException("武将不存在");
        }
        
        // 2. 校验装备
        UserEquipmentTbl equip = userEquipmentMapper.selectById(equipmentId);
        if (equip == null || !equip.getUserId().equals(userId)) {
            throw new RuntimeException("装备不存在");
        }
        if (equip.getIsEquipped()) {
           throw new RuntimeException("装备已被其他武将穿戴"); 
           // 实际项目可能支持“抢过来”，这里简化
        }
        
        // 校验槽位类型 (略：假设前端传的 slot 正确，或者通过 templateId 查 EquipmentTemplate 校验)
        // 假设 slot 是 "weapon", "armor1" 等
        
        // 3. 处理卸下旧装备 (如果有)
        Long oldEquipId = 0L;
        switch (slot) {
            case "weapon": oldEquipId = general.getEquipWeaponId(); general.setEquipWeaponId(equipmentId); break;
            case "armor1": oldEquipId = general.getEquipArmor1Id(); general.setEquipArmor1Id(equipmentId); break;
            case "armor2": oldEquipId = general.getEquipArmor2Id(); general.setEquipArmor2Id(equipmentId); break;
            case "shoes": oldEquipId = general.getEquipShoesId(); general.setEquipShoesId(equipmentId); break;
            case "flag": oldEquipId = general.getEquipFlagId(); general.setEquipFlagId(equipmentId); break;
            case "talisman": oldEquipId = general.getEquipTalismanId(); general.setEquipTalismanId(equipmentId); break;
            default: throw new RuntimeException("无效的装备槽位: " + slot);
        }
        
        if (oldEquipId != null && oldEquipId > 0) {
            UserEquipmentTbl oldEquip = userEquipmentMapper.selectById(oldEquipId);
            if (oldEquip != null) {
                oldEquip.setIsEquipped(false);
                userEquipmentMapper.update(oldEquip);
            }
        }
        
        // 4. 更新新装备状态
        equip.setIsEquipped(true);
        userEquipmentMapper.update(equip);
        
        // 5. 更新武将
        userGeneralMapper.update(general);
    }

    @Transactional(rollbackFor = Exception.class)
    public void synthesizeGem(Long userId, String gemType, Integer level) {
         // 1. 查找 5 个该类型等级的闲置宝石
         // 需要 UserGemMapper 支持 selectCount 和 selectListLimit
         // 这里简化处理：先查所有，再取前5个。实际应优化 SQL
         // ... implementation skipped for brevity, using pseudo-logic
         /*
         List<UserGemTbl> gems = userGemMapper.selectByUserIdAndTypeAndLevel(userId, gemType, level);
         if (gems.size() < 5) throw "宝石不足";
         
         // delete 5 gems
         for (int i=0; i<5; i++) userGemMapper.delete(gems.get(i).getId());
         
         // insert 1 new gem (level+1)
         UserGemTbl newGem = new UserGemTbl();
         newGem.setUserId(userId);
         newGem.setGemType(gemType);
         newGem.setGemLevel(level + 1);
         newGem.setStatValue( ... look up template ... );
         userGemMapper.insert(newGem);
         */
    }

    @Transactional(rollbackFor = Exception.class)
    public void inlayGem(Long userId, Long equipmentId, int socketIndex, Long gemId) {
        UserEquipmentTbl equip = userEquipmentMapper.selectById(equipmentId);
        UserGemTbl gem = userGemMapper.selectById(gemId); // need inject UserGemMapper
        
        // validations...
        if (!equip.getUserId().equals(userId) || !gem.getUserId().equals(userId)) throw new RuntimeException("归属错误");
        if (gem.getIsUsed()) throw new RuntimeException("宝石已使用");
        
        // update equipment socket
        if (socketIndex == 1) equip.setSocket1GemId(gemId);
        else if (socketIndex == 2) equip.setSocket2GemId(gemId);
        else throw new RuntimeException("无效孔位");
        
        userEquipmentMapper.update(equip);
        
        // update gem status
        gem.setIsUsed(true);
        userGemMapper.update(gem);
    }
}
