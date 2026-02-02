package ljc.controller.dto;

import lombok.Data;

@Data
public class RegisterReq {
    private String username;    // 账号
    private String password;    // 密码
    private String nickname;    // 昵称 (比如 "曹孟德")
    private String initialCiv;  // 初始国家 (CN/JP/KR/GB)
}