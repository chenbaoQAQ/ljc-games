package ljc.controller;

import ljc.common.Result;
import ljc.context.BattleContext;
import ljc.context.BattleStartResult;
import ljc.service.BattleService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/battle")
@RequiredArgsConstructor
public class BattleController {

    private final BattleService battleService;

    // Legacy: /battle/start
    @PostMapping("/start")
    public Result<BattleStartResult> startBattle(@RequestParam Long userId, @RequestParam Integer dungeonId) {
        try {
            BattleStartResult res = battleService.startBattle(userId, dungeonId);
            return Result.success(res);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 1. 只读：获取当前战斗状态（不推进回合）
    @GetMapping("/state")
    public Result<BattleContext> getBattleState(@RequestParam Long userId) {
        try {
            BattleContext ctx = battleService.getBattleState(userId);
            return Result.success(ctx);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. Start Story Battle → 返回 battleId + 初始 context
    @PostMapping("/story/start")
    public Result<BattleStartResult> startStoryBattle(@RequestParam Long userId, @RequestBody StoryStartReq req) {
        try {
            BattleStartResult res = battleService.startStoryBattle(
                userId, req.getCiv(), req.getStageNo(), req.getGeneralId(), req.getTroopConfig());
            return Result.success(res);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 3. Process Turn（支持幂等 clientTurnNo）
    @PostMapping("/turn")
    public Result<BattleContext> processTurn(@RequestParam Long userId, @RequestBody TurnReq req) {
        try {
            BattleContext ctx = battleService.processTurn(userId, req.getCastSkill(), req.getClientTurnNo(), req.getTactics());
            return Result.success(ctx);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Data
    public static class TurnReq {
        private Boolean castSkill;
        private Integer clientTurnNo; // 幂等：前端传 currentTurn + 1
        private String tactics;       // 战术指令
    }

    @Data
    public static class StoryStartReq {
        private String civ;
        private Integer stageNo;
        private Long generalId;
        private java.util.Map<Integer, Integer> troopConfig;
    }
}