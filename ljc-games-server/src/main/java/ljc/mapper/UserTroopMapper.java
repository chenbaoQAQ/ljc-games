package ljc.mapper;

import ljc.entity.UserTroopTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserTroopMapper {
    /**
     * 基础插入
     */
    int insert(UserTroopTbl entity);

    /**
     * 招兵专属：Upsert (存在则累加，不存在则新增)
     */
    int upsertAdd(@Param("userId") Long userId, @Param("troopId") Integer troopId, @Param("delta") Long delta);

    /**
     * 安全扣兵：原子操作，确保不会扣成负数
     * @return 影响行数，1表示扣除成功，0表示兵力不足
     */
    int safeDeduct(@Param("userId") Long userId, @Param("troopId") Integer troopId, @Param("want") Integer want);

    /**
     * 根据玩家ID查询所有库存
     */
    List<UserTroopTbl> selectByUserId(Long userId);
}