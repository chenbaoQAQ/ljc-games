package ljc.mapper;
import ljc.entity.UserGeneralTbl;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserGeneralMapper {
    // 注意：这个 insert 需要返回自增 ID，XML 里要配置
    int insert(UserGeneralTbl entity);
    List<UserGeneralTbl> selectByUserId(Long userId);
    // 根据主键查单个武将
    UserGeneralTbl selectById(Long id);

    int update(UserGeneralTbl entity);
}

