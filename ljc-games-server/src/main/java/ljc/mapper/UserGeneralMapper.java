package ljc.mapper;

import ljc.entity.UserGeneral;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserGeneralMapper {
    // 查单个
    UserGeneral selectById(@Param("id") Integer id);

    // 查玩家所有武将
    List<UserGeneral> selectByUserId(@Param("userId") Long userId);

    // 查玩家当前可用武将 (未休息、已激活)
    List<UserGeneral> selectAvailable(@Param("userId") Long userId);

    // 插入新武将
    int insert(UserGeneral general);

    // 更新血量和状态
    int updateStatus(@Param("id") Integer id, @Param("hp") Integer hp, @Param("status") String status);
}