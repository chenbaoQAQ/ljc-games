package ljc.mapper;
import ljc.entity.UserCivProgressTbl;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCivProgressMapper {
    int insert(UserCivProgressTbl entity);
}
