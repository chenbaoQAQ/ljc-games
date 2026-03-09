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
    private final ljc.service.TroopTreeService troopTreeService;

    // 征兵接口
    // 示例：POST http://localhost:8080/troop/recruit?userId=1
    // 请求体：{ "troopId": 1001, "count": 10 }
    @PostMapping("/recruit")
    public ljc.common.Result<String> recruit(@RequestParam Long userId, @RequestBody RecruitReq req) {
        troopService.recruit(userId, req);
        return ljc.common.Result.success("征兵成功！兵力已到账！");
    }
    
    // 图鉴：GET /codex?userId=1
    @GetMapping("/codex")
    public ljc.common.Result<java.util.List<ljc.controller.dto.TroopCodexVO>> getCodex(@RequestParam Long userId) {
        return ljc.common.Result.success(troopService.getTroopCodex(userId));
    }
    
    // 进度别名：GET /progression?userId=1（等价 codex）
    @GetMapping("/progression")
    public ljc.common.Result<java.util.List<ljc.controller.dto.TroopCodexVO>> getProgression(@RequestParam Long userId) {
        return ljc.common.Result.success(troopService.getTroopCodex(userId));
    }
    
    // 树状进化：POST /evolve?userId=1，Body: { "fromNodeId": 100, "toNodeId": 101 }
    @PostMapping("/evolve")
    public ljc.common.Result<String> evolve(@RequestParam Long userId, @RequestBody ljc.controller.dto.EvolveNodeReq req) {
        if (req.getFromNodeId() != null && req.getToNodeId() != null) {
            troopTreeService.evolveNode(userId, req.getFromNodeId(), req.getToNodeId());
            return ljc.common.Result.success("进化成功！");
        }
        
        // 参数不完整时返回错误
        return ljc.common.Result.error("请求参数错误: 需提供 fromNodeId 和 toNodeId");
    }
    
    // 树状图鉴：GET /codex/tree?userId=1&civ=CN
    @GetMapping("/codex/tree")
    public ljc.common.Result<ljc.service.TroopTreeService.TreeResponse> getTree(@RequestParam Long userId, @RequestParam(defaultValue = "CN") String civ) {
        return ljc.common.Result.success(troopTreeService.getTroopTree(userId, civ));
    }
}
