package ljc.mapper;

import ljc.entity.UserTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    int insert(UserTbl user);

    /**
     * 通用更新方法
     */
    int update(UserTbl user);

    UserTbl selectByUsername(@Param("username") String username);

    UserTbl selectById(@Param("id") Long id);

    /**
     * 安全扣钱：确保金币不为负数
     */
    int reduceGold(@Param("userId") Long userId, @Param("cost") Integer cost);

    /**
     * 安全扣体力
     */
    int reduceStamina(@Param("userId") Long userId, @Param("cost") Integer cost);
}