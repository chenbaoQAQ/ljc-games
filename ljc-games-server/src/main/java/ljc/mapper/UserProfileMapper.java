package ljc.mapper;

import ljc.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileMapper {

    // 基础查询：根据 ID 查玩家
    UserProfile selectById(@Param("userId") Integer userId);

    // 基础更新：保存玩家所有信息
    int update(UserProfile userProfile);

    /**
     * 【核心防御】扣减金币
     * @param userId 玩家ID
     * @param delta 扣多少钱 (正数)
     * @return 1=扣款成功, 0=余额不足或失败
     */
    int decreaseGold(@Param("userId") Integer userId, @Param("delta") Long delta);
}