package ljc.service;

import ljc.controller.dto.RegisterReq;
import ljc.entity.UserTbl;
import ljc.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PlayerInitService playerInitService; // 注入我们刚写好的初始化服务

    /**
     * 处理注册逻辑
     */
    @Transactional(rollbackFor = Exception.class) // 事务！要么全成功，要么全失败
    public UserTbl register(RegisterReq req) {

        // 1. 创建玩家基础档案
        UserTbl user = new UserTbl();
        user.setUsername(req.getUsername());
        user.setNickname(req.getNickname());
        user.setPasswordHash(req.getPassword()); // 暂存明文，以后教你加密
        user.setInitialCiv(req.getInitialCiv());

        // 给点启动资金 (也可以写在常量里)
        user.setGold(1000L);
        user.setDiamond(100L);
        user.setStamina(100);

        // 2. 存入数据库 (获取 userId)
        userMapper.insert(user);
        Long newUserId = user.getId();

        // 3. 【关键一步】调用初始化服务，发放武将和小兵
        // 这一步如果不成功，上面的 userMapper.insert 也会自动回滚（撤销），因为有 @Transactional
        playerInitService.initPlayerData(newUserId, req.getInitialCiv());

        return user;
    }
}