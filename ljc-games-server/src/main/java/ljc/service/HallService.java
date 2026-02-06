package ljc.service;

import ljc.entity.*;
import ljc.mapper.*;
import ljc.entity.EquipmentTemplateTbl;
import ljc.mapper.EquipmentTemplateMapper;
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
    private final UserGeneralSkillMapper userGeneralSkillMapper;
    private final EquipmentTemplateMapper equipmentTemplateMapper;

    
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
    public void equip(Long userId, Long generalId, Long equipmentId) {
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
        
        // 3. 获取装备模板以推断 Slot
        EquipmentTemplateTbl tpl = equipmentTemplateMapper.selectById(equip.getTemplateId());
        if (tpl == null) {
            throw new RuntimeException("装备模板不存在");
        }
        String slot = tpl.getSlot();
        if (slot == null || slot.isEmpty()) {
            throw new RuntimeException("装备模板未配置槽位");
        }

        // 如果此装备已被别人穿戴，且不是自己
        if (equip.getGeneralId() != null && !equip.getGeneralId().equals(generalId)) {
             throw new RuntimeException("装备已被其他武将穿戴"); 
        }

        // 4. 卸下该武将该槽位上的旧装备 (如果有)
        // 临时方案：遍历用户所有装备 
        java.util.List<UserEquipmentTbl> allEquips = userEquipmentMapper.selectByUserId(userId);
        
        for (UserEquipmentTbl e : allEquips) {
            // 注意：这里需要再次查模板才能确认 e 的 slot，或者直接用 e.getSlot() (如果我们在 UserEquipment 存了 slot)
            // UserEquipment 表存了 slot 字段，这很有用！
            if (generalId.equals(e.getGeneralId()) && slot.equals(e.getSlot())) {
                e.setGeneralId(null);
                e.setSlot(null);
                userEquipmentMapper.update(e);
            }
        }
        
        // 5. 更新当前装备
        equip.setGeneralId(generalId);
        equip.setSlot(slot);
        userEquipmentMapper.update(equip);
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
    @Transactional(rollbackFor = Exception.class)
    public void activateGeneral(Long userId, Long generalId) {
        UserGeneralTbl general = userGeneralMapper.selectById(generalId);
        if (general == null || !general.getUserId().equals(userId)) {
            throw new RuntimeException("武将不存在");
        }
        if (Boolean.TRUE.equals(general.getActivated())) {
            throw new RuntimeException("武将已激活");
        }
        // check if unlocked
        if (Boolean.FALSE.equals(general.getUnlocked())) {
            throw new RuntimeException("武将未解锁");
        }
        general.setActivated(true);
        userGeneralMapper.update(general);
    }

    @Transactional(rollbackFor = Exception.class)
    public void enhanceEquipment(Long userId, Long equipmentId) {
        UserEquipmentTbl equip = userEquipmentMapper.selectById(equipmentId);
        if (equip == null || !equip.getUserId().equals(userId)) {
            throw new RuntimeException("装备不存在");
        }
        
        // Simple cost: (level+1)*100
        int currentLevel = (equip.getEnhanceLevel() == null) ? 0 : equip.getEnhanceLevel();
        int cost = (currentLevel + 1) * 100;
        
        int rows = userMapper.reduceGold(userId, cost);
        if (rows == 0) {
            throw new RuntimeException("金币不足");
        }

        equip.setEnhanceLevel(currentLevel + 1);
        userEquipmentMapper.update(equip);
    }

    @Transactional(rollbackFor = Exception.class)
    public void learnSkill(Long userId, Long generalId, Integer bookItemId) {
        UserGeneralTbl general = userGeneralMapper.selectById(generalId);
        if (general == null || !general.getUserId().equals(userId)) {
             throw new RuntimeException("武将不存在");
        }
        
        Integer count = userInventoryMapper.selectCount(userId, bookItemId);
        if (count == null || count < 1) {
            throw new RuntimeException("道具不足");
        }
        
        userInventoryMapper.decreaseItem(userId, bookItemId, 1);
        
        UserGeneralSkillTbl skill = new UserGeneralSkillTbl();
        skill.setGeneralId(generalId);
        skill.setCurrentSkillId(bookItemId); 
        skill.setUpdatedAt(java.time.LocalDateTime.now());
        
        userGeneralSkillMapper.insert(skill);
    }
}
