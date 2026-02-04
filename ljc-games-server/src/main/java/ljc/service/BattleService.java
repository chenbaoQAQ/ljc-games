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

    @Transactional(rollbackFor = Exception.class)
    public String startBattle(Long userId, StartBattleReq req) {
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
                usedCapacity += (entry.getValue() * tpl.getCost());
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

        // 4. TODO: 创建 BattleSession 并存入 context_json
        // 目前返回战前准备完成
        return "战前资源已锁定，BattleSession 待建立。统帅消耗: " + usedCapacity;
    }
}