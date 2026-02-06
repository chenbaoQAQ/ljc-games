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

    @Data
    public static class TurnReq {
        private Boolean castSkill;
    }
}