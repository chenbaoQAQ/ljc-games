package ljc.controller;

import ljc.common.Result;
import ljc.controller.dto.PlayerInfoResp;
import ljc.service.PlayerInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerInfoService playerInfoService;

    /**
     * 主界面信息接口
     * URL: GET /player/info?userId=10086
     */
    @GetMapping("/info")
    public Result<PlayerInfoResp> getPlayerInfo(@RequestParam Long userId) {

        // TODO: 调用 Service，拿到 resp
        PlayerInfoResp resp = playerInfoService.getPlayerInfo(userId);

        // 用我们写的统一盒子包装成功结果
        return Result.success(resp);
    }
}