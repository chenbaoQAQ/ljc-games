package ljc.mapper;

import ljc.entity.PersonalityConfigTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PersonalityConfigMapper {
    @Select("SELECT * FROM personality_config WHERE personality_code = #{code}")
    PersonalityConfigTbl selectByCode(String code);
}
