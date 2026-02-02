package ljc.mapper;
import ljc.entity.UserTroopTbl;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserTroopMapper {
    int insert(UserTroopTbl entity);
}
