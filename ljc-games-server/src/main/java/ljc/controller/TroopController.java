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
    public Result<String> recruit(@RequestParam Long userId, @RequestBody RecruitReq req) {

        // 调用你刚才写的 Service
        troopService.recruit(userId, req);

        return Result.success("征兵成功！兵力已到账！");
    }
}