package ljc.controller;

import ljc.common.Result;
import ljc.controller.dto.LoginReq;
import ljc.controller.dto.RegisterReq;
import ljc.entity.UserTbl;
import ljc.service.AuthService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController // 标记这是个接口控制器
@RequestMapping("/auth") // 所有接口以 /auth 开头
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 注册接口
     * URL: POST /auth/register
     */
    @PostMapping("/register")
    public Result<UserTbl> register(@RequestBody RegisterReq req) {
        try {
            UserTbl user = authService.register(req);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 登录接口
     * URL: POST /auth/login
     */
    @PostMapping("/login")
    public Result<UserTbl> login(@RequestBody LoginReq req) {
        try {
            UserTbl user = authService.login(req);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}