package ljc.mapper;

import ljc.entity.DropPoolTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DropPoolMapper {
    @Select("SELECT * FROM drop_pool WHERE pool_id = #{poolId}")
    DropPoolTbl selectById(Integer poolId);
}
