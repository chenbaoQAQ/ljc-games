package ljc.service;

import ljc.controller.dto.StartBattleReq;
import ljc.entity.*;
import ljc.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BattleService {

    private final UserMapper userMapper;
    private final UserGeneralMapper userGeneralMapper;
    private final UserTroopMapper userTroopMapper;
    private final TroopTemplateMapper troopTemplateMapper;

    private final BattleSessionsMapper battleSessionsMapper;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Transactional(rollbackFor = Exception.class)
    public String startBattle(Long userId, StartBattleReq req) throws Exception {
        // 1. 战前检查
        UserTbl user = userMapper.selectById(userId);
        UserGeneralTbl general = userGeneralMapper.selectById(req.getGeneralId());

        if (general == null || !general.getUserId().equals(userId)) {
            throw new RuntimeException("武将归属错误或不存在");
        }
        if (!general.getActivated()) {
            throw new RuntimeException("武将未激活");
        }
        if (general.getRestTurns() > 0) {
            throw new RuntimeException("武将正在休息中");
        }

        // 2. 兵力统帅值校验 (Σ count * cost <= capacity)
        int usedCapacity = 0;
        Map<Integer, Integer> config = req.getTroopConfig();

        if (config != null) {
            for (Map.Entry<Integer, Integer> entry : config.entrySet()) {
                TroopTemplateTbl tpl = troopTemplateMapper.selectById(entry.getKey());
                if (tpl != null) {
                    usedCapacity += (entry.getValue() * tpl.getCost());
                }
            }
        }

        if (usedCapacity > general.getCapacity()) {
            throw new RuntimeException("超过武将统帅上限！当前: " + usedCapacity + " 上限: " + general.getCapacity());
        }

        // 3. 执行原子扣兵 (锁定资源)
        if (config != null) {
            for (Map.Entry<Integer, Integer> entry : config.entrySet()) {
                int rows = userTroopMapper.safeDeduct(userId, entry.getKey(), entry.getValue());
                if (rows == 0) {
                    throw new RuntimeException("兵力不足，无法出征。ID: " + entry.getKey());
                }
            }
        }

        // 4. 创建 BattleSession
        // 先清理旧会话 (Schema V2.3 这里的user_id是PK，意味着单线程)
        battleSessionsMapper.deleteByUserId(userId);

        BattleSessionsTbl session = new BattleSessionsTbl();
        session.setUserId(userId);
        session.setBattleId(System.currentTimeMillis()); // 简单 ID
        session.setStatus("ACTIVE");
        session.setMode("STORY"); // 默认
        session.setCiv("CN"); // 默认
        session.setStageNo(1);
        session.setGeneralId(general.getId());
        session.setSeed(System.currentTimeMillis());
        session.setCurrentTurn(1);
        session.setMaxTurn(20);
        
        session.setContextJson(objectMapper.writeValueAsString(config));
        session.setStateJson("{}"); 

        battleSessionsMapper.insert(session);
        return String.valueOf(session.getBattleId());
    }

    public void processTurn(Long userId, String sessionId) {
        // Simple turn progression stub
        BattleSessionsTbl session = battleSessionsMapper.selectByUserId(userId);
        if (session == null || !String.valueOf(session.getBattleId()).equals(sessionId)) {
            throw new RuntimeException("无效的会话");
        }
        session.setCurrentTurn(session.getCurrentTurn() + 1);
        battleSessionsMapper.update(session);
    }
}