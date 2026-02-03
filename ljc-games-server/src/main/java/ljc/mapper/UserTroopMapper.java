package ljc.mapper;
import ljc.entity.UserTroopTbl;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserTroopMapper {
    int insert(UserTroopTbl entity);
    // 新增这个方法：根据 userId 查所有兵力
    List<UserTroopTbl> selectByUserId(Long userId);
}
