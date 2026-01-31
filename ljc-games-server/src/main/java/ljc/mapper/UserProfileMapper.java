package ljc.mapper;

import ljc.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileMapper {
    // 查档案
    UserProfile selectById(@Param("userId") Long userId);

    // 初始化/插入
    int insert(UserProfile profile);

    // 扣金币 (CAS乐观锁)
    int decreaseGold(@Param("userId") Long userId, @Param("delta") Long delta);

    // 扣材料 (新增)
    int decreaseMaterial(@Param("userId") Long userId, @Param("delta") Long delta);
}