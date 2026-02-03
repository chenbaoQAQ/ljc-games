package ljc.service;

import ljc.controller.dto.PlayerInfoResp;
import ljc.entity.UserGeneralTbl;
import ljc.entity.UserTbl;
import ljc.entity.UserTroopTbl;
import ljc.mapper.UserGeneralMapper;
import ljc.mapper.UserMapper;
import ljc.mapper.UserTroopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerInfoService {

    private final UserMapper userMapper;
    private final UserGeneralMapper userGeneralMapper;
    private final UserTroopMapper userTroopMapper;

    /**
     * 获取玩家主界面所需的所有信息
     * @param userId 玩家ID
     * @return 打包好的大对象
     */
    public PlayerInfoResp getPlayerInfo(Long userId) {

        // 1. 查询玩家本体信息
        UserTbl user = userMapper.selectById(userId);

        // 2. 查询玩家拥有的所有武将
        List<UserGeneralTbl> generals = userGeneralMapper.selectByUserId(userId);

        // 3. 查询玩家拥有的所有兵力
        List<UserTroopTbl> troops = userTroopMapper.selectByUserId(userId);

        // 4. 创建响应对象并组装
        PlayerInfoResp resp = new PlayerInfoResp();
        resp.setUserInfo(user);   // 塞入玩家信息
        resp.setGenerals(generals); // 塞入武将列表
        resp.setTroops(troops);     // 塞入兵力列表

        // 5. 返回结果
        return resp;
    }
}