package ljc.mapper;

import ljc.entity.UserItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserItemMapper {
    // 查背包里所有东西
    List<UserItem> selectAll(@Param("userId") Long userId);

    // 查特定武将穿的装备
    List<UserItem> selectEquipped(@Param("generalId") Integer generalId);

    int insert(UserItem item);
}