package ljc.mapper;

import ljc.entity.SkillLearnLogTbl;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SkillLearnLogMapper {
    @Insert("INSERT INTO skill_learn_log (user_id, general_id, old_skill_id, new_skill_id, book_item_id, created_at) " +
            "VALUES (#{userId}, #{generalId}, #{oldSkillId}, #{newSkillId}, #{bookItemId}, NOW())")
    int insert(SkillLearnLogTbl entity);
}
