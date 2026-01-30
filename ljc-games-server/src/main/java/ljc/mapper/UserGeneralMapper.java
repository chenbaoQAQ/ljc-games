package ljc.mapper;

import ljc.entity.UserGeneral;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserGeneralMapper {
    // 插入新武将
    int insert(UserGeneral general);
}