package ljc.controller;

import ljc.common.Result;
import ljc.controller.dto.RecruitReq;
import ljc.service.TroopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/troop")
@RequiredArgsConstructor
public class TroopController {

    private final TroopService troopService;

    // 征兵接口
    // URL: POST http://localhost:8080/troop/recruit?userId=1
    // Body: { "troopId": 1001, "count": 10 }
    @PostMapping("/recruit")
    public ljc.common.Result<String> recruit(@RequestParam Long userId, @RequestBody RecruitReq req) {
        troopService.recruit(userId, req);
        return ljc.common.Result.success("征兵成功！兵力已到账！");
    }
    
    // GET /codex?userId=1
    @GetMapping("/codex")
    public ljc.common.Result<java.util.List<ljc.controller.dto.TroopCodexVO>> getCodex(@RequestParam Long userId) {
        return ljc.common.Result.success(troopService.getTroopCodex(userId));
    }
    
    // GET /progression?userId=1 (Alias for codex or specific progress)
    @GetMapping("/progression")
    public ljc.common.Result<java.util.List<ljc.controller.dto.TroopCodexVO>> getProgression(@RequestParam Long userId) {
        return ljc.common.Result.success(troopService.getTroopCodex(userId));
    }
    
    // POST /evolve?userId=1 Body: { "troopId": 1001 }
    @PostMapping("/evolve")
    public ljc.common.Result<String> evolve(@RequestParam Long userId, @RequestBody ljc.controller.dto.EvolveTroopReq req) {
        troopService.evolveTroop(userId, req.getTroopId());
        return ljc.common.Result.success("进化成功！");
    }
}