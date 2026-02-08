package ljc.service;

import ljc.controller.dto.PlayerInfoResp;
import ljc.entity.UserCivProgressTbl;
import ljc.entity.UserTbl;
import ljc.entity.UserTroopTbl;
import ljc.mapper.UserCivProgressMapper;
import ljc.mapper.UserMapper;
import ljc.mapper.UserTroopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerInfoService {

    private final UserMapper userMapper;
    private final UserTroopMapper userTroopMapper;
    private final UserCivProgressMapper userCivProgressMapper;

    /**
     * 获取玩家主界面信息
     */
    public PlayerInfoResp getPlayerInfo(Long userId) {

        // 1. 查人
        UserTbl user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("玩家不存在");
        }

        // 2. 查兵
        List<UserTroopTbl> myTroops = userTroopMapper.selectByUserId(userId);

        // 3. 准备返回值
        PlayerInfoResp resp = new PlayerInfoResp();

        // === 任务 A: 搬运基础信息 ===
        resp.setId(user.getId());
        resp.setNickname(user.getNickname());
        resp.setGold(user.getGold());
        resp.setDiamond(user.getDiamond());

        // === 任务 B: 搬运兵力列表 ===
        List<PlayerInfoResp.TroopDto> dtoList = new ArrayList<>();

        for (UserTroopTbl t : myTroops) {
            PlayerInfoResp.TroopDto dto = new PlayerInfoResp.TroopDto();
            dto.setTroopId(t.getTroopId());
            dto.setCount(t.getCount());
            dtoList.add(dto);
        }

        // === 任务 C: 塞入列表 ===
        resp.setTroops(dtoList);

        return resp;
    }

    /**
     * 获取玩家四国进度
     */
    public List<UserCivProgressTbl> getPlayerProgress(Long userId) {
        return userCivProgressMapper.selectByUserId(userId);
    }
}
