package ljc.service;

import ljc.controller.dto.StartBattleReq;
import ljc.entity.UserGeneralTbl;
import ljc.entity.UserTbl;
import ljc.entity.UserTroopTbl;
import ljc.mapper.UserGeneralMapper;
import ljc.mapper.UserMapper;
import ljc.mapper.UserTroopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BattleService {

    private final UserMapper userMapper;
    private final UserGeneralMapper userGeneralMapper;
    private final UserTroopMapper userTroopMapper;

    @Transactional
    public String startBattle(Long userId, StartBattleReq req) {
        // 1. 检查玩家存在
        UserTbl user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("玩家不存在");
        }

        // 2. 检查武将 (是否属于该玩家？)
        UserGeneralTbl general = userGeneralMapper.selectById(req.getGeneralId());

        // 先看看有没有这个将
        if (general == null) {
            throw new RuntimeException("武将不存在");
        }

        // 注意 Long 类型比较要用 !...equals(...)
        if (!general.getUserId().equals(userId)) {
            throw new RuntimeException("这不是你的武将，无法指挥！");
        }
        // 3. 检查并扣除兵力 (这是重点！)
        // ---------------------------------------------------------

        Map<Integer, Integer> troopsToTake = req.getTroopConfig();

        if (troopsToTake != null && !troopsToTake.isEmpty()) {

            // === 第一步：先查库存（把仓库账本拿出来）===
            // 原理：不要在循环里一次次查数据库（那叫N+1问题，很慢），而是查一次全部，放在内存里比对。
            List<UserTroopTbl> myInventory = userTroopMapper.selectByUserId(userId);

            // 把 List 转成 Map，方便按 ID 查数量
            // 原理：Map<兵种ID, 拥有的数量>
            // 以后写 map.get(1001) 就能直接拿到数量，不用写循环去 List 里找了
            Map<Integer, Long> inventoryMap = new java.util.HashMap<>();
            for (UserTroopTbl t : myInventory) {
                inventoryMap.put(t.getTroopId(), t.getCount());
            }

            // === 第二步：拿着“购物清单”去比对库存 ===
            // entrySet() 的意思就是把清单里的“每一行”都拿出来遍历
            for (Map.Entry<Integer, Integer> entry : troopsToTake.entrySet()) {
                Integer wantTroopId = entry.getKey();   // 你想要带哪个兵？
                Integer wantCount = entry.getValue();   // 你想要带多少个？

                if (wantCount <= 0) continue; // 防呆：如果传了0或负数，直接跳过

                // 1. 查：看看这个兵种我有多少？(getOrDefault意思是：如果有就返回数量，没有就返回0)
                Long haveCount = inventoryMap.getOrDefault(wantTroopId, 0L);

                // 2. 比：够不够？
                if (haveCount < wantCount) {
                    throw new RuntimeException("兵力不足！兵种ID:" + wantTroopId + " 需要:" + wantCount + " 实际拥有:" + haveCount);
                }

                // 3. 扣：去数据库执行扣除！
                // 原理：调用 addTroopCount，传“负数”就是扣减。
                // 注意：数据库定义的 troopId 是 Long，这里要转一下类型
                userTroopMapper.addTroopCount(userId, Long.valueOf(wantTroopId), -wantCount);
            }
        }
        // ---------------------------------------------------------
        // 4. 模拟战斗结果 (暂时先直接判赢)
        // 以后这里会写复杂的战斗计算公式
        boolean isWin = true;

        // 5. 发放奖励 (赢了给 100 金币)
        if (isWin) {
            // 骚操作：利用 reduceGold 扣除“负数”，负负得正，就变成了加钱
            // 原理：Update set gold = gold - (-100)  =>  gold + 100
            int rows = userMapper.reduceGold(userId, -100);

            if (rows == 0) {
                // 如果返回0，说明可能正好碰到极低概率的并发问题，或者账号被删了，暂时忽略
                System.out.println("奖励发放异常，可能是账号不存在");
            }
        }

        return isWin ? "战斗胜利！掠夺了 100 金币" : "战斗失败，兵力全军覆没...";
    }
}