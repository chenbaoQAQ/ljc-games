package ljc.mapper;
import ljc.entity.UserCivProgressTbl;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCivProgressMapper {
    int insert(UserCivProgressTbl entity);

    UserCivProgressTbl selectByUserIdAndCiv(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("civ") String civ);

    int update(UserCivProgressTbl entity);

    java.util.List<UserCivProgressTbl> selectByUserId(Long userId);
}

