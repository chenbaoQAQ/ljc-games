package ljc.mapper;
import ljc.entity.GeneralTemplateTbl;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GeneralTemplateMapper {
    GeneralTemplateTbl selectById(Integer templateId);
}