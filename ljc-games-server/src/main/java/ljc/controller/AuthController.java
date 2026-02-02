package ljc.controller;

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
    public String register(@RequestBody RegisterReq req) {
        // 调用 Service
        UserTbl user = authService.register(req);

        // 返回简单结果 (以后我们会封装统一的 Result)
        return "注册成功！玩家ID: " + user.getId() + "，主公：" + user.getNickname();
    }
}