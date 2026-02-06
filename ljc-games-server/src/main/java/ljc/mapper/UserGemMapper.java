package ljc.mapper;

import ljc.entity.UserGemTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserGemMapper {
    int insert(UserGemTbl gem);
    int update(UserGemTbl gem);
    UserGemTbl selectById(@Param("id") Long id);
    List<UserGemTbl> selectByUserId(@Param("userId") Long userId);

    List<UserGemTbl> selectForSynthesis(@Param("userId") Long userId, @Param("gemType") String gemType, @Param("gemLevel") Integer gemLevel);

    int deleteBatch(@Param("ids") List<Long> ids);
}
