package ljc.mapper;
import ljc.entity.UserTroopTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserTroopMapper {
    int insert(UserTroopTbl entity);
    // 新增这个方法：根据 userId 查所有兵力
    List<UserTroopTbl> selectByUserId(Long userId);

    // 增加兵力
    int addTroopCount(@Param("userId") Long userId, @Param("troopId") Long troopId, @Param("count") Integer count);
}
