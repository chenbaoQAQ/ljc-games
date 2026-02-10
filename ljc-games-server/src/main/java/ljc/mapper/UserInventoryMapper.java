package ljc.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserInventoryMapper {
    /**
     * Reduces the count of an item. Returns updated row count (or check count>=cost before).
     * Since schema check (count >= 0), simple update works.
     */
    int decreaseItem(@Param("userId") Long userId, @Param("itemId") Integer itemId, @Param("count") Integer count);
    
    // Add select if needed
    Integer selectCount(@Param("userId") Long userId, @Param("itemId") Integer itemId);
    
    // 查询用户所有道具
    java.util.List<ljc.entity.UserInventoryTbl> selectByUserId(@Param("userId") Long userId);

    int insert(ljc.entity.UserInventoryTbl item);
}
