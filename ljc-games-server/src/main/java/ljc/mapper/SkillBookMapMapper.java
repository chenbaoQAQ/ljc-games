package ljc.mapper;

import ljc.entity.SkillBookMapTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SkillBookMapMapper {
    @Select("SELECT * FROM skill_book_map WHERE item_id = #{itemId}")
    SkillBookMapTbl selectByItemId(@Param("itemId") Integer itemId);
}
