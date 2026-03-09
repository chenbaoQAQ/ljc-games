package ljc.service;

import ljc.entity.TroopTreeNodeConfigTbl;
import ljc.entity.UserTroopProgressTbl;
import ljc.mapper.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TroopTreeService {

    private final TroopTreeNodeConfigMapper treeConfigMapper;
    private final UserTroopProgressMapper userTroopProgressMapper;
    private final UserCivProgressMapper userCivProgressMapper;
    private final UserMapper userMapper;
    private final TroopTemplateMapper troopTemplateMapper;

    // 进化树响应 DTO
    @Data
    public static class TickInfo {
        private Long nodeId;
        private Integer troopId;
        private String name;
        private Integer tier;
        private Long parentNodeId;
        private String unlockHint;
        private String status; // 状态值：LOCKED / DISCOVERED / UNLOCKED / EVOLVED / BRANCH_LOCKED
        private Boolean isEvolvable;
        private Integer evolveCost;
        private Integer xPos;
        private Integer yPos;
    }
    
    @Data
    public static class TreeResponse {
        private List<TickInfo> nodes;
        private List<Edge> edges;
    }
    
    @Data
    public static class Edge {
        private Long from;
        private Long to;
    }

    public TreeResponse getTroopTree(Long userId, String civ) {
        List<TroopTreeNodeConfigTbl> configs = treeConfigMapper.selectByCiv(civ);
        List<UserTroopProgressTbl> progressList = userTroopProgressMapper.selectByUserId(userId);
        
        Map<Integer, UserTroopProgressTbl> progressMap = new HashMap<>();
        for (UserTroopProgressTbl p : progressList) {
            progressMap.put(p.getTroopId(), p);
        }
        
        // 构建节点映射，便于计算分支互斥
        Map<Long, TroopTreeNodeConfigTbl> configMap = new HashMap<>();
        for (TroopTreeNodeConfigTbl c : configs) configMap.put(c.getNodeId(), c);
        
        List<TickInfo> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        
        for (TroopTreeNodeConfigTbl cfg : configs) {
            TickInfo node = new TickInfo();
            node.setNodeId(cfg.getNodeId());
            node.setTroopId(cfg.getTroopId());
            node.setTier(cfg.getTier());
            node.setParentNodeId(cfg.getParentNodeId());
            node.setEvolveCost(cfg.getEvolveCost());
            node.setXPos(cfg.getXPos());
            node.setYPos(cfg.getYPos());
            
            // 名称
            ljc.entity.TroopTemplateTbl tpl = troopTemplateMapper.selectById(cfg.getTroopId());
            node.setName(tpl != null ? tpl.getName() : "Unknown");
            
            // 状态计算
            UserTroopProgressTbl p = progressMap.get(cfg.getTroopId());
            
            // 检查父节点分支互斥
            boolean isBranchLocked = false;
            if (cfg.getParentNodeId() != null) {
                TroopTreeNodeConfigTbl parentCfg = configMap.get(cfg.getParentNodeId());
                if (parentCfg != null) {
                    UserTroopProgressTbl parentP = progressMap.get(parentCfg.getTroopId());
                    // 父节点已选其他分支时，当前分支锁定
                    if (parentP != null && parentP.getChosenChildNodeId() != null 
                            && !parentP.getChosenChildNodeId().equals(cfg.getNodeId())) {
                        isBranchLocked = true;
                    }
                }
                Edge edge = new Edge();
                edge.setFrom(cfg.getParentNodeId());
                edge.setTo(cfg.getNodeId());
                edges.add(edge);
            }
            
            if (isBranchLocked) {
                node.setStatus("BRANCH_LOCKED");
                node.setIsEvolvable(false);
                node.setUnlockHint("已选择其他进化分支");
            } else if (p != null && (p.getStatus() == 2)) { // 已解锁
                 // 若已有下级分支，展示为已进化
                 if (p.getChosenChildNodeId() != null) {
                     node.setStatus("EVOLVED");
                 } else {
                     node.setStatus("UNLOCKED");
                 }
                 node.setIsEvolvable(false); // 当前节点本身已解锁，不可再次进化
            } else {
                // 未解锁节点：继续校验条件
                // 1. 校验父节点是否已解锁
                boolean parentUnlocked = true;
                if (cfg.getParentNodeId() != null) {
                     TroopTreeNodeConfigTbl parentCfg = configMap.get(cfg.getParentNodeId());
                     UserTroopProgressTbl parentP = progressMap.get(parentCfg.getTroopId());
                     if (parentP == null || parentP.getStatus() < 2) {
                         parentUnlocked = false;
                     }
                }
                
                if (!parentUnlocked) {
                     node.setStatus("LOCKED");
                     node.setUnlockHint("请先解锁前置兵种");
                     node.setIsEvolvable(false);
                } else {
                    // 父节点满足后，再校验外部条件（国家/关卡）
                    boolean condMet = true;
                    if (cfg.getUnlockCiv() != null && cfg.getUnlockStageNo() != null) {
                        ljc.entity.UserCivProgressTbl civP = userCivProgressMapper.selectByUserIdAndCiv(userId, cfg.getUnlockCiv());
                        if (civP == null || !Boolean.TRUE.equals(civP.getUnlocked()) || civP.getMaxStageCleared() < cfg.getUnlockStageNo()) {
                            condMet = false;
                            node.setUnlockHint("需通关 " + cfg.getUnlockCiv() + " " + cfg.getUnlockStageNo());
                        }
                    }
                    
                    if (condMet) {
                        node.setStatus("DISCOVERED"); // 可进化状态
                        node.setIsEvolvable(true);
                        node.setUnlockHint("可进化");
                    } else {
                        node.setStatus("LOCKED");
                        node.setIsEvolvable(false);
                    }
                }
            }
            
            nodes.add(node);
        }
        
        TreeResponse response = new TreeResponse();
        response.setNodes(nodes);
        response.setEdges(edges);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public void evolveNode(Long userId, Long fromNodeId, Long toNodeId) {
        // 1. 配置校验
        TroopTreeNodeConfigTbl toCfg = treeConfigMapper.selectById(toNodeId);
        if (toCfg == null) throw new RuntimeException("目标节点不存在");
        
        if (fromNodeId == null || !fromNodeId.equals(toCfg.getParentNodeId())) {
             throw new RuntimeException("非法的父子节点关系");
        }
        
        TroopTreeNodeConfigTbl fromCfg = treeConfigMapper.selectById(fromNodeId);
        
        // 2. 父节点状态校验
        UserTroopProgressTbl fromProgress = userTroopProgressMapper.selectByPrimaryKey(userId, fromCfg.getTroopId());
        if (fromProgress == null || fromProgress.getStatus() < 2) {
             throw new RuntimeException("前置兵种未解锁");
        }
        
        // 3. 分支互斥校验
        if (fromProgress.getChosenChildNodeId() != null) {
             if (fromProgress.getChosenChildNodeId().equals(toNodeId)) {
                 throw new RuntimeException("该分支已进化");
             } else {
                 throw new RuntimeException("已选择其他分支，互斥锁定");
             }
        }
        
        // 4. 解锁条件校验
        if (toCfg.getUnlockCiv() != null && toCfg.getUnlockStageNo() != null) {
            ljc.entity.UserCivProgressTbl civP = userCivProgressMapper.selectByUserIdAndCiv(userId, toCfg.getUnlockCiv());
            if (civP == null || !Boolean.TRUE.equals(civP.getUnlocked()) || civP.getMaxStageCleared() < toCfg.getUnlockStageNo()) {
                 throw new RuntimeException("解锁条件未满足: 通关 " + toCfg.getUnlockCiv() + "-" + toCfg.getUnlockStageNo());
            }
        }
        
        // 5. 资源校验
        if (toCfg.getEvolveCost() > 0) {
            int rows = userMapper.reduceGold(userId, toCfg.getEvolveCost());
            if (rows == 0) throw new RuntimeException("金币不足");
        }
        
        // 6. 执行进化
        // A. 记录父节点已选择分支
        fromProgress.setChosenChildNodeId(toNodeId);
        userTroopProgressMapper.updateByPrimaryKey(fromProgress);
        
        // B. 解锁子节点
        UserTroopProgressTbl toProgress = userTroopProgressMapper.selectByPrimaryKey(userId, toCfg.getTroopId());
        if (toProgress == null) {
            toProgress = new UserTroopProgressTbl();
            toProgress.setUserId(userId);
            toProgress.setTroopId(toCfg.getTroopId());
            toProgress.setStatus(2); // 已解锁
            toProgress.setEvolutionUnlocked((byte)1);
            toProgress.setEvolutionTier(0); // 初始化进化层级
            userTroopProgressMapper.insert(toProgress);
        } else {
            toProgress.setStatus(2);
            userTroopProgressMapper.updateByPrimaryKey(toProgress);
        }
    }
}
