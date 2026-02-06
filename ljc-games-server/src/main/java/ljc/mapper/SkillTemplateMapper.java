package ljc.mapper;

import ljc.entity.SkillTemplateTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SkillTemplateMapper {
    SkillTemplateTbl selectById(@Param("skillId") Integer skillId);
    List<SkillTemplateTbl> selectAll();
}
