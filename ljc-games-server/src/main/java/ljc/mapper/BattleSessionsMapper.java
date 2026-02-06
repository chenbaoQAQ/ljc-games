package ljc.mapper;

import ljc.entity.BattleSessionsTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BattleSessionsMapper {
    int insert(BattleSessionsTbl session);
    int update(BattleSessionsTbl session);
    BattleSessionsTbl selectBySessionId(@Param("sessionId") String sessionId); // Note: schema says session_id but in V2.3 user changed to user_id pk?
    // Wait, verification: Schema V2.3 says:
    // CREATE TABLE battle_sessions (
    //   user_id BIGINT PRIMARY KEY, ...
    
    // So ONLY ONE active session per user. PK is user_id.
    
    BattleSessionsTbl selectByUserId(@Param("userId") Long userId);
    int deleteByUserId(@Param("userId") Long userId);
}
