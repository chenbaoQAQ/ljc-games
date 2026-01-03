package ljc.controller;

import ljc.entity.StageConfig;
import ljc.entity.UnitConfig;
import ljc.model.Army;
import ljc.model.DifficultyTier;
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
            @RequestParam Integer stageId,
            @RequestParam(required = false) String difficulty) { // 新增难度参数

        try {
            StageConfig stage = stageRepository.findById(stageId)
                    .orElseThrow(() -> new RuntimeException("找不到关卡ID: " + stageId));

            // 将字符串难度转换为枚举对象，如果是无尽关卡则传入 null
            DifficultyTier tier = (difficulty != null) ? DifficultyTier.valueOf(difficulty) : null;

            // 调用修正后的 conductBattle，注意现在不需要在 Controller 手动创建 Army 对象了
            return battleService.conductBattle(userId, generalId, stage, tier);

        } catch (Exception e) {
            List<String> errorLog = new ArrayList<>();
            errorLog.add("【系统报错】: " + e.getMessage());
            return errorLog;
        }
    }
}