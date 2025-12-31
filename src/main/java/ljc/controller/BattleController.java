package ljc.controller;

import ljc.constant.DifficultyTier;
import ljc.entity.StageConfig;
import ljc.entity.UnitConfig;
import ljc.model.Army;
import ljc.repository.StageConfigRepository;
import ljc.repository.UnitConfigRepository;
import ljc.service.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/battle")
public class BattleController {

    @Autowired
    private BattleService battleService;

    @Autowired
    private StageConfigRepository stageRepository;

    @Autowired
    private UnitConfigRepository unitRepository;

    @GetMapping("/test/{stageId}")
    public List<String> quickBattle(@PathVariable Integer stageId) {
        // 1. 模拟环境：从数据库读关卡
        StageConfig stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("关卡不存在"));

        // 2. 模拟玩家：创建一个带 1000 步兵的部队
        Army playerArmy = new Army();
        UnitConfig infantry = unitRepository.findByUnitName("INFANTRY")
                .orElseThrow(() -> new RuntimeException("兵种不存在"));
        playerArmy.getTroopMap().put(infantry, 1000);

        // 3. 模拟方案：如果是墙，全用步兵填
        Map<UnitConfig, Integer> plan = new HashMap<>();
        if (stage.getHasWall()) {
            plan.put(infantry, 100);
        }

        // 4. 开始模拟战斗 (默认 HARD 难度)
        return battleService.conductBattle(playerArmy, stage, DifficultyTier.HARD, plan);
    }
}