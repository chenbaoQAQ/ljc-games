package ljc.mapper;

import ljc.entity.TroopTemplateTbl;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TroopTemplateMapper {
    // 根据ID查兵种属性
    TroopTemplateTbl selectById(Integer troopId);
    
    // 查询所有兵种
    java.util.List<TroopTemplateTbl> selectAll();
}