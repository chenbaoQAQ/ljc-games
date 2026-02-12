package ljc.service;

import ljc.controller.dto.RecruitReq;
import ljc.entity.TroopTemplateTbl;
import ljc.entity.UserTroopProgressTbl;
import ljc.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TroopService {

    private final UserMapper userMapper;
    private final UserTroopMapper userTroopMapper;
    private final TroopTemplateMapper troopTemplateMapper;
    private final UserTroopProgressMapper userTroopProgressMapper;
    private final TroopEvolutionConfigMapper troopEvolutionConfigMapper;
    private final UserCivProgressMapper userCivProgressMapper; // For evolution requirement check

    // Status Constants
    public static final int STATUS_LOCKED = 0;
    public static final int STATUS_DISCOVERED = 1;
    public static final int STATUS_UNLOCKED = 2; // Can recruit


    @Transactional(rollbackFor = Exception.class)
    public void recruit(Long userId, RecruitReq req) {
        // 1. 基础校验
        if (req.getCount() <= 0) {
            throw new RuntimeException("招募数量必须大于0");
        }

        // 2. 获取兵种模板配置
        TroopTemplateTbl tpl = troopTemplateMapper.selectById(req.getTroopId().intValue());
        if (tpl == null) {
            throw new RuntimeException("无效的兵种ID: " + req.getTroopId());
        }
        
        // 校验 civ 解锁 (可选，根据企划)
        // if (tpl.getUnlockCivRequired()) { ... }

        // 3. 计算并扣除金币
        long totalCost = tpl.getRecruitGoldCost() * req.getCount();
        int rows = userMapper.reduceGold(userId, (int)totalCost); // 注意：UserMapper.reduceGold 参数是 Integer

        if (rows == 0) {
            throw new RuntimeException("金币不足，无法招募。需要: " + totalCost);
        }

        // 4. 执行原子增兵 (Upsert)
        userTroopMapper.upsertAdd(userId, req.getTroopId().intValue(), req.getCount().longValue());
    }

    /**
     * Unlock a troop (e.g. from Stage Clear)
     */
    @Transactional(rollbackFor = Exception.class)
    public void unlockTroop(Long userId, Integer troopId) {
        if (troopId == null) return;
        
        UserTroopProgressTbl progress = userTroopProgressMapper.selectByPrimaryKey(userId, troopId);
        if (progress == null) {
            progress = new UserTroopProgressTbl();
            progress.setUserId(userId);
            progress.setTroopId(troopId);
            progress.setStatus(STATUS_UNLOCKED);
            progress.setEvolutionTier(0);
            userTroopProgressMapper.insert(progress);
        } else if (progress.getStatus() < STATUS_UNLOCKED) {
            progress.setStatus(STATUS_UNLOCKED);
            userTroopProgressMapper.updateByPrimaryKey(progress);
        }
    }
    
    /**
     * Unlock evolution for a troop (e.g. from Stage Clear)
     * For now, this just ensures the user has a record. The actual "Unlock" might be implicit by stage clear,
     * but we can mark a flag if we want.
     * Simplification: Evolution is 'Unlockable' if stage requirement met. 
     * But we can use this to notify or init record.
     */
    @Transactional(rollbackFor = Exception.class)
    public void unlockEvolution(Long userId, Integer troopId) {
        if (troopId == null) return;
        // Just ensure record exists
        UserTroopProgressTbl progress = userTroopProgressMapper.selectByPrimaryKey(userId, troopId);
        if (progress == null) {
            progress = new UserTroopProgressTbl();
            progress.setUserId(userId);
            progress.setTroopId(troopId);
            progress.setStatus(STATUS_LOCKED); // Still locked, but knows about evolution? 
            // Actually if we unlock evolution, we probably imply we know the troop.
            // Let's assume unlocked status stays as is.
            progress.setEvolutionTier(0);
            userTroopProgressMapper.insert(progress);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void evolveTroop(Long userId, Integer troopId) {
        // 1. Check User Troop Progress
        UserTroopProgressTbl progress = userTroopProgressMapper.selectByPrimaryKey(userId, troopId);
        if (progress == null || progress.getStatus() != STATUS_UNLOCKED) {
             throw new RuntimeException("兵种未解锁");
        }
        
        // 2. Find Next Config
        int nextTier = progress.getEvolutionTier() + 1;
        ljc.entity.TroopEvolutionConfigTbl config = troopEvolutionConfigMapper.selectByTroopIdAndTier(troopId, nextTier);
        if (config == null) {
            throw new RuntimeException("已是最高阶或无法进化");
        }
        
        // 3. Check Requirements (Stage)
        if (config.getRequiredCiv() != null && config.getRequiredStageNo() != null) {
             ljc.entity.UserCivProgressTbl civP = userCivProgressMapper.selectByUserIdAndCiv(userId, config.getRequiredCiv());
             if (civP == null || !Boolean.TRUE.equals(civP.getUnlocked())) {
                 throw new RuntimeException("未满足前置国家条件");
             }
             int cleared = civP.getMaxStageCleared() == null ? 0 : civP.getMaxStageCleared();
             if (cleared < config.getRequiredStageNo()) {
                 throw new RuntimeException("需通关 " + config.getRequiredCiv() + " " + config.getRequiredStageNo());
             }
        }
        
        // 4. Check & Deduct Cost
        if (config.getCostGold() != null && config.getCostGold() > 0) {
             int rows = userMapper.reduceGold(userId, config.getCostGold().intValue());
             if (rows == 0) throw new RuntimeException("金币不足");
        }
        
        // 5. Update Progress
        progress.setEvolutionTier(nextTier);
        userTroopProgressMapper.updateByPrimaryKey(progress);
    }
    
    public java.util.List<ljc.controller.dto.TroopCodexVO> getTroopCodex(Long userId) {
        java.util.List<TroopTemplateTbl> allTroops = troopTemplateMapper.selectAll();
        java.util.List<ljc.entity.UserTroopProgressTbl> userProgress = userTroopProgressMapper.selectByUserId(userId);
        
        // Map progress by troopId
        java.util.Map<Integer, ljc.entity.UserTroopProgressTbl> progressMap = new java.util.HashMap<>();
        if (userProgress != null) {
            for (ljc.entity.UserTroopProgressTbl p : userProgress) {
                progressMap.put(p.getTroopId(), p);
            }
        }
        
        java.util.List<ljc.controller.dto.TroopCodexVO> result = new java.util.ArrayList<>();
        if (allTroops != null) {
            for (TroopTemplateTbl tpl : allTroops) {
                ljc.controller.dto.TroopCodexVO vo = new ljc.controller.dto.TroopCodexVO();
                vo.setTroopId(tpl.getTroopId());
                vo.setName(tpl.getName());
                vo.setCiv(tpl.getCiv());
                vo.setType(tpl.getTroopType());
                vo.setIsElite(tpl.getIsElite());
                
                vo.setBaseAtk(tpl.getBaseAtk());
                vo.setBaseHp(tpl.getBaseHp());
                vo.setCost(tpl.getCost());
                
                ljc.entity.UserTroopProgressTbl p = progressMap.get(tpl.getTroopId());
                if (p != null) {
                    vo.setStatus(p.getStatus());
                    vo.setEvolutionTier(p.getEvolutionTier());
                } else {
                    // Default locked
                    vo.setStatus(STATUS_LOCKED);
                    vo.setEvolutionTier(0);
                }
                
                result.add(vo);
            }
        }
        return result;
    }

}