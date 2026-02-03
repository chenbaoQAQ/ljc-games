package ljc.controller;

import ljc.common.Result;
import ljc.controller.dto.StartBattleReq;
import ljc.service.BattleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/battle")
@RequiredArgsConstructor
public class BattleController {

    private final BattleService battleService;

    // 宣战接口
    // POST http://localhost:8080/battle/start?userId=1
    @PostMapping("/start")
    public Result<String> startBattle(@RequestParam Long userId, @RequestBody StartBattleReq req) {

        String battleResult = battleService.startBattle(userId, req);

        return Result.success(battleResult);
    }
}