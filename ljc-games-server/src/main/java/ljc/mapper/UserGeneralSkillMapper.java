package ljc.mapper;
import ljc.entity.UserGeneralSkillTbl;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserGeneralSkillMapper {
    int insert(UserGeneralSkillTbl entity);
}
