package ljc.mapper;

import ljc.entity.EquipmentTemplateTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EquipmentTemplateMapper {
    EquipmentTemplateTbl selectById(@Param("id") Integer id);
}
