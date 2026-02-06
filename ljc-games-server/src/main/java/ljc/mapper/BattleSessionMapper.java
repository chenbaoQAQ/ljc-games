package ljc.mapper;

import ljc.entity.BattleSessionTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
@org.springframework.stereotype.Repository
public interface BattleSessionMapper {
    int insert(BattleSessionTbl session);
    int update(BattleSessionTbl session);
    BattleSessionTbl selectByUserId(@Param("userId") Long userId);
    BattleSessionTbl selectById(@Param("id") Long id);
}
