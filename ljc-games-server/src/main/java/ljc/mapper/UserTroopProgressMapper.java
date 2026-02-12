package ljc.mapper;

import ljc.entity.UserTroopProgressTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserTroopProgressMapper {
    int insert(UserTroopProgressTbl record);
    int updateByPrimaryKey(UserTroopProgressTbl record);
    
    UserTroopProgressTbl selectByPrimaryKey(@Param("userId") Long userId, @Param("troopId") Integer troopId);
    List<UserTroopProgressTbl> selectByUserId(Long userId);
    
    // Quick upsert similar to user_troops if needed, but here status might be non-additive.
    // We'll stick to insert/update.
}
