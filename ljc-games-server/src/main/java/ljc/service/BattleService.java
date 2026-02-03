package ljc.service;

import ljc.controller.dto.StartBattleReq;
import ljc.entity.*;
import ljc.mapper.*;
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
    private final GeneralTemplateMapper generalTemplateMapper;
    private final TroopTemplateMapper troopTemplateMapper;

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
                    throw new RuntimeException
                            ("兵力不足！兵种ID:" + wantTroopId + " 需要:" + wantCount + " 实际拥有:" + haveCount);
                }

                // 3. 扣：去数据库执行扣除！
                // 原理：调用 addTroopCount，传“负数”就是扣减。
                // 注意：数据库定义的 troopId 是 Long，这里要转一下类型
                userTroopMapper.addTroopCount(userId, Long.valueOf(wantTroopId), -wantCount);
            }
        }
        // --- 第 4 步：计算我方总战斗力 (ATK 和 HP) ---
        long myTotalAtk = 0;
        long myTotalHp = 0;

        GeneralTemplateTbl genTpl = generalTemplateMapper.selectById(general.getTemplateId());

        long genAtk = genTpl.getBaseAtk() + (general.getLevel() * 5);

        myTotalAtk += genAtk;
        myTotalHp += general.getCurrentHp();

        if (troopsToTake != null) {
            // 必须加这个循环！把清单里的每一项拿出来
            for (Map.Entry<Integer, Integer> entry : troopsToTake.entrySet()) {

                // 1. 在这里定义 troopId 变量 (从 Map 的 Key 里取)
                Integer troopId = entry.getKey();
                Integer count = entry.getValue(); // 顺便把数量也取出来

                if (count <= 0) continue;

                TroopTemplateTbl troopTpl = troopTemplateMapper.selectById(troopId);

                // 3. 接着算属性...
                myTotalAtk += troopTpl.getBaseAtk() * count;
                myTotalHp += troopTpl.getBaseHp() * count;
            }
        }


        // --- 第 5 步：定义敌人 (第一关山贼) ---
        // 暂时写死：血量 500，攻击 50
        long enemyHp = 500;
        long enemyAtk = 50;


        // --- 第 6 步：死斗循环 (最热血的部分) ---
        // 提示：写一个 while(true) 循环
        // 每一轮：
        //    1. 敌人掉血：enemyHp -= myTotalAtk
        //    2. 判断敌人死没死？(enemyHp <= 0) -> 死了就 break (isWin = true)
        //    3. 我方掉血：myTotalHp -= enemyAtk
        //    4. 判断我方死没死？(myTotalHp <= 0) -> 死了就 break (isWin = false)
        //    5. (可选) 防止死循环，加个回合数限制，超过20回合强制判输

        boolean isWin = false;
        StringBuilder log = new StringBuilder("战斗开始...\n"); // 用来记录战报

        // (在这里写 while 循环...)


        // --- 第 7 步：结算发奖 ---
        if (isWin) {
            // 赢了！给钱 (reduceGold 传负数)
            userMapper.reduceGold(userId, -100);
            return log.toString() + "\n【胜利】获得 100 金币！";
        } else {
            return log.toString() + "\n【失败】全军覆没...";
        }
    }
}