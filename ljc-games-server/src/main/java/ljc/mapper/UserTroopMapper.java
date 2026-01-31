package ljc.mapper;

import ljc.entity.UserTroop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserTroopMapper {
    // 查玩家所有兵
    List<UserTroop> selectAll(@Param("userId") Long userId);

    // 查特定兵种
    UserTroop selectByType(@Param("userId") Long userId, @Param("type") String type);

    // 增加兵力 (ON DUPLICATE KEY UPDATE)
    int addStock(@Param("userId") Long userId, @Param("type") String type, @Param("count") Long count);

    // 扣除兵力
    int decreaseStock(@Param("userId") Long userId, @Param("type") String type, @Param("delta") Long delta);
}