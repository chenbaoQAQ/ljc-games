package ljc.mapper;

import ljc.entity.UserEquipmentTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserEquipmentMapper {
    int insert(UserEquipmentTbl equipment);
    int update(UserEquipmentTbl equipment);
    UserEquipmentTbl selectById(@Param("id") Long id);
    List<UserEquipmentTbl> selectByUserId(@Param("userId") Long userId);
}
