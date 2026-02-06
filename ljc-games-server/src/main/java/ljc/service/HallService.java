package ljc.service;

import ljc.entity.*;
import ljc.mapper.*;
import ljc.entity.EquipmentTemplateTbl;
import ljc.mapper.EquipmentTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    private final UserTroopMapper userTroopMapper;
    private final TroopTemplateMapper troopTemplateMapper;

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
        
        
        int currentLevel = (equip.getEnhanceLevel() == null) ? 0 : equip.getEnhanceLevel();
        // Cost: (currentLevel + 1) * 100
        int cost = (currentLevel + 1) * 100;
        
        int rows = userMapper.reduceGold(userId, cost);
        if (rows == 0) {
            throw new RuntimeException("金币不足");
        }

        // V2.8 Logic: 
        // +0 -> +1 to +3: 100% Success
        // +3 -> +4 and above: Chance to fail.
        // Fail: Drop 1 level (min +0).
        
        boolean success = true;
        if (currentLevel >= 3) {
            // Simple logic: 80% at 3->4, 70% at 4->5, etc. Min 10%.
            // Base chance 90 - (level-3)*10 ?
            // Level 3->4: 90%
            // Level 4->5: 80%
            // ...
            int chance = 90 - (currentLevel - 3) * 10;
            if (chance < 10) chance = 10;
            
            // Roll
            int roll = new java.util.Random().nextInt(100);
            success = roll < chance;
        }

        if (success) {
            equip.setEnhanceLevel(currentLevel + 1);
            userEquipmentMapper.update(equip);
            // Can throw exception with specific message or return Result object if we change return type?
            // Controller returns Result<String>. 
            // We can't easily return distinction here without changing signature.
            // For now, let's just complete.
        } else {
            // Fail: Drop 1 level
            if (currentLevel > 0) {
                equip.setEnhanceLevel(currentLevel - 1);
                userEquipmentMapper.update(equip);
                throw new RuntimeException("强化失败！装备等级下降到 +" + (currentLevel - 1));
            } else {
                throw new RuntimeException("强化失败！(等级保持 +0)");
            }
        }
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

    @Transactional(rollbackFor = Exception.class)
    public void recruit(Long userId, Integer troopId, Integer count) {
        if (count == null || count <= 0) {
            throw new RuntimeException("招募数量必须为正整数");
        }

        TroopTemplateTbl tpl = troopTemplateMapper.selectById(troopId);
        if (tpl == null) {
            throw new RuntimeException("兵种不存在");
        }

        if (tpl.getRecruitGoldCost() == null) {
            throw new RuntimeException("该兵种无法招募");
        }

        long totalCost = tpl.getRecruitGoldCost() * count;
        if (totalCost > Integer.MAX_VALUE) {
             throw new RuntimeException("交易金额过大");
        }

        int rows = userMapper.reduceGold(userId, (int) totalCost);
        if (rows == 0) {
            throw new RuntimeException("金币不足");
        }

        userTroopMapper.upsertAdd(userId, troopId, (long) count);
    }

    @Transactional(rollbackFor = Exception.class)
    public void combineGem(Long userId, String gemType, Integer level) {
        // 1. Find 5 gems
        List<UserGemTbl> gems = userGemMapper.selectForSynthesis(userId, gemType, level);
        if (gems.size() < 5) {
            throw new RuntimeException("宝石数量不足，需要5颗同类型同等级宝石");
        }

        // 2. Calculate Base Stats and New Stats
        // Formula: X * n * (n + 1) / 2
        // Reverse X from first gem: X = stat * 2 / (n * (n+1))
        long stat = gems.get(0).getStatValue();
        long n = level;
        long x = (n == 0) ? stat : (stat * 2) / (n * (n + 1)); 
        // Note: If level 0, formula is 0? The doc example says Level 0 = 10.
        // Formula check: n=0 => 0. This contradicts example.
        // Doc Ex: +0: 10, +1: 20, +2: 40...
        // Wait, the doc says "Level n".
        // Example: +0: 10. +1: 20.
        // Formula: X * (1+2+...+n) ??
        // "Total Bonus = X * n * (n+1) / 2"
        // If n=1, 1*2/2 = 1. X=20?
        // If n=0, 0. But example says 10.
        // Maybe Level 1 is the first level?
        // Let's assume the doc example is the truth.
        // +0 (Level 1?): 10
        // +1 (Level 2?): 20  Wait, 10->20 is just x2?
        // Let's re-read the doc carefully.
        // "累加型成长: n级 = X * n * (n+1) / 2"
        // Example (X=10):
        // +0: 10
        // +1: 20
        // This is confusing. If n=0, result is 0.
        // Probably "Level 1" = +0? Or Level 0 has base value.
        // If X=10.
        // Level 1: 10 * 1 * 2 / 2 = 10. Matches "+0".
        // Level 2: 10 * 2 * 3 / 2 = 30. Doc says "+1: 20".
        
        // Let's not over-engineer the formula reverse for now.
        // The safest way is to take the stat of the previous gem, divide by its factor, and multiply by new factor?
        // Or simply: Level+1 Stat = ??
        
        // Let's look at the example progression:
        // 10 -> 20 -> 40 -> 70 -> 110
        // Diff: 10, 20, 30, 40
        // This matches Arithmetic Progression sum.
        // Level 1: 10 (Sum of 10)
        // Level 2: 10+10 = 20
        // Level 3: 10+10+20 = 40
        // Level 4: 10+10+20+30 = 70
        // Level 5: 10+10+20+30+40 = 110
        
        // So Value(Level k) = Value(Level k-1) + (k-1)*10? No.
        
        // Let's use a simpler heuristic for now:
        // If I have 5 gems of stat S.
        // New Stat = S + (some increment).
        
        // Let's respect the "Level" in DB.
        // If DB says Level 1 (User sees +0), Stat = 10.
        // New Level 2 (User sees +1).
        
        // I will just use the formula: NewStat = CurrentStat + (Level+1)*BaseX
        // BaseX = 10 for both ATK/HP for now (since I don't have GemTemplate).
        
        long newLevel = level + 1;
        long baseX = 10; // Default
        long currentStat = gems.get(0).getStatValue();
        // The increment seems to be: 
        // L1->L2 (+10)
        // L2->L3 (+20)
        // L3->L4 (+30)
        // So increment = level * 10.
        
        long newStat = currentStat + (level * 10); // Wait, if input level is 1 (displayed +0), next is 2.
        // If input level is 1. Increment should be 10.
        // newStat = 10 + 10 = 20. Correct.
        // If input level is 2 (20). Increment should be 20.
        // newStat = 20 + 20 = 40. Correct.
        // If input level is 3 (40). Increment should be 30.
        // newStat = 40 + 30 = 70. Correct.
        
        // So formula: newStat = currentStat + (level * 10);
        
        // 3. Delete old gems
        List<Long> ids = new ArrayList<>();
        for (UserGemTbl g : gems) {
            ids.add(g.getId());
        }
        userGemMapper.deleteBatch(ids);

        // 4. Insert new gem
        UserGemTbl newGem = new UserGemTbl();
        newGem.setUserId(userId);
        newGem.setGemType(gemType);
        newGem.setGemLevel((int) newLevel);
        newGem.setStatValue(newStat);
        newGem.setIsUsed(false);
        userGemMapper.insert(newGem);
    }

    @Transactional(rollbackFor = Exception.class)
    public void ascendGeneral(Long userId, Long generalId) {
        UserGeneralTbl general = userGeneralMapper.selectById(generalId);
        if (general == null || !general.getUserId().equals(userId)) {
            throw new RuntimeException("武将不存在");
        }

        // 1. Check Max Level condition
        int currentMaxLevel = 20 + (general.getTier() * 10);
        if (general.getLevel() < currentMaxLevel) {
            throw new RuntimeException("武将未达到当前阶级等级上限");
        }

        // 2. Check Tier Cap (e.g., 5)
        if (general.getTier() >= 5) {
             throw new RuntimeException("武将已达最高阶");
        }

        // 3. Cost (Placeholder: 1000 Gold per Tier)
        int cost = (general.getTier() + 1) * 1000;
        int rows = userMapper.reduceGold(userId, cost);
        if (rows == 0) {
            throw new RuntimeException("金币不足(升阶消耗 " + cost + ")");
        }

        // 4. Update General
        general.setTier(general.getTier() + 1);
        // Bonus stats for ascension?
        general.setMaxHp(general.getMaxHp() + 200); 
        general.setCurrentHp(general.getMaxHp());
        
        userGeneralMapper.update(general);
    }
}
