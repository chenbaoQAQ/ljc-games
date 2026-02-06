package ljc.controller;

import ljc.common.Result;
import ljc.context.BattleContext;
import ljc.service.BattleService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/battle")
@RequiredArgsConstructor
public class BattleController {

    private final BattleService battleService;

    @PostMapping("/start")
    public Result<Long> startBattle(@RequestParam Long userId, @RequestParam Integer dungeonId) {
        Long battleId = battleService.startBattle(userId, dungeonId);
        return Result.success(battleId);
    }

    @PostMapping("/turn")
    public Result<BattleContext> processTurn(@RequestParam Long userId, @RequestBody TurnReq req) {
        BattleContext ctx = battleService.processTurn(userId, req.getCastSkill());
        return Result.success(ctx);
    }

    @PostMapping("/story/start")
    public Result<Long> startStoryBattle(@RequestParam Long userId, @RequestBody StoryStartReq req) {
        try {
            Long battleId = battleService.startStoryBattle(userId, req.getCiv(), req.getStageNo(), req.getGeneralId(), req.getTroopConfig());
            return Result.success(battleId);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Data
    public static class TurnReq {
        private Boolean castSkill;
    }

    @Data
    public static class StoryStartReq {
        private String civ;
        private Integer stageNo;
        private Long generalId;
        private java.util.Map<Integer, Integer> troopConfig;
    }
}