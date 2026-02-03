package ljc.service;

import ljc.controller.dto.RecruitReq;
import ljc.entity.UserTbl;
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

    @Transactional
    public void recruit(Long userId, RecruitReq req) {

        // 1. 定义单价 (假设 10 金币一个兵)
        int pricePerUnit = 10;

        //防呆
        if (req.getCount() <= 0) {
            throw new RuntimeException("招募数量必须大于0");
        }
        // 2. 计算总花费
        int totalCost = pricePerUnit* req.getCount();

        // 3. 检查玩家余额 (这一步是业务校验)
        UserTbl user = userMapper.selectById(userId);

        // === 防鬼代码 ===
        if (user == null) {
            throw new RuntimeException("玩家不存在！请检查ID是否正确。当前传参ID: " + userId);
        }

        if (user.getGold() < totalCost) {
            throw new RuntimeException("您的金币不足");
        }

        // =================================================
        // 4. 执行扣钱 (调用 reduceGold)
        // 注意：reduceGold 会返回影响行数。
        // 如果返回 0，说明数据库层发现钱不够(双重保险)，此时应该抛异常回滚。
        int rows = userMapper.reduceGold(userId, totalCost);

        if (rows <= 0) {
            throw new RuntimeException("金币不足，无法招募");
        }
        // 5. 执行加兵 (调用 addTroopCount)
        int troopRows = userTroopMapper.addTroopCount(userId, req.getTroopId(), req.getCount());

        //因为row是你涂改的行数所以要判断troopRow如果是0就说明没改成功
        if (troopRows == 0) {
            throw new RuntimeException("增兵失败：可能没找到这个兵种");
        }
        // =================================================
    }
}