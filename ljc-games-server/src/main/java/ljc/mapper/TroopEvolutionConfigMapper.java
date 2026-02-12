package ljc.mapper;

import ljc.entity.TroopEvolutionConfigTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TroopEvolutionConfigMapper {
    List<TroopEvolutionConfigTbl> selectByTroopId(Integer troopId);
    TroopEvolutionConfigTbl selectByTroopIdAndTier(@Param("troopId") Integer troopId, @Param("nextTier") Integer nextTier);
}
