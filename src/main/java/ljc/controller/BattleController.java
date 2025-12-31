package ljc.controller;

import ljc.entity.StageConfig;
import ljc.entity.UnitConfig;
import ljc.model.Army;
import ljc.repository.StageConfigRepository;
import ljc.service.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/battle")
public class BattleController {

    @Autowired
    private BattleService battleService;

    @Autowired
    private StageConfigRepository stageRepository;

    @GetMapping("/start")
    public List<String> startBattle(
            @RequestParam Integer userId,
            @RequestParam Integer generalId,
            @RequestParam Integer stageId) {

        try {
            // 1. 获取关卡
            StageConfig stage = stageRepository.findById(stageId)
                    .orElseThrow(() -> new RuntimeException("找不到关卡ID: " + stageId));

            // 2. 紧急组建一支演习部队
            Army testArmy = new Army();

            // 模拟一个基础步兵配置
            UnitConfig infantry = new UnitConfig();
            infantry.setUnitName("INFANTRY"); // 必须匹配 Army.java 里的逻辑
            infantry.setBaseAtk(15);

            // 往部队里塞 100 名步兵
            testArmy.getTroopMap().put(infantry, 100);

            // 3. 开始战斗并返回日志
            return battleService.conductBattle(userId, generalId, stage, testArmy);

        } catch (Exception e) {
            // 如果报错了，把错误信息打印在页面上，方便排查
            List<String> errorLog = new ArrayList<>();
            errorLog.add("【系统报错】: " + e.getMessage());
            e.printStackTrace(); // 在 IDEA 控制台打印堆栈信息
            return errorLog;
        }
    }
}