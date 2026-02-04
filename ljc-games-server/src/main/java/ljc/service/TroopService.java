package ljc.service;

import ljc.controller.dto.RecruitReq;
import ljc.entity.TroopTemplateTbl;
import ljc.mapper.TroopTemplateMapper;
import ljc.mapper.UserCivProgressMapper;
import ljc.mapper.UserMapper;
import ljc.mapper.UserTroopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TroopService {

    private final UserMapper userMapper;
    private final UserTroopMapper userTroopMapper;
    private final TroopTemplateMapper troopTemplateMapper;

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

        // 3. 计算并扣除金币 (修正后的方法名)
        int totalCost = tpl.getRecruitGoldCost().intValue() * req.getCount();
        int rows = userMapper.reduceGold(userId, totalCost);

        if (rows == 0) {
            throw new RuntimeException("金币不足，无法招募。需要: " + totalCost);
        }

        // 4. 执行原子增兵 (Upsert)
        userTroopMapper.upsertAdd(userId, req.getTroopId().intValue(), req.getCount().longValue());
    }
}