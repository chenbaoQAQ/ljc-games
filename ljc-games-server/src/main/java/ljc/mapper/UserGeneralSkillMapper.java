package ljc.mapper;
import ljc.entity.UserGeneralSkillTbl;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserGeneralSkillMapper {
    int insert(UserGeneralSkillTbl entity);
    
    @org.apache.ibatis.annotations.Select("SELECT * FROM user_general_skill WHERE general_id = #{generalId}")
    UserGeneralSkillTbl selectByGeneralId(@org.apache.ibatis.annotations.Param("generalId") Long generalId);

    @org.apache.ibatis.annotations.Update("UPDATE user_general_skill SET current_skill_id = #{currentSkillId}, updated_at = NOW() WHERE general_id = #{generalId}")
    int update(UserGeneralSkillTbl entity);
}
