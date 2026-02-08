package ljc.controller;

import ljc.common.Result;
import ljc.controller.dto.PlayerInfoResp;
import ljc.entity.UserCivProgressTbl;
import ljc.service.PlayerInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        PlayerInfoResp resp = playerInfoService.getPlayerInfo(userId);
        return Result.success(resp);
    }

    /**
     * 四国进度查询
     * URL: GET /player/progress?userId=1
     */
    @GetMapping("/progress")
    public Result<List<UserCivProgressTbl>> getPlayerProgress(@RequestParam Long userId) {
        List<UserCivProgressTbl> progressList = playerInfoService.getPlayerProgress(userId);
        return Result.success(progressList);
    }
}
