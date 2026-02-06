package ljc.mapper;

import ljc.entity.StoryStageConfigTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface StoryStageConfigMapper {
    StoryStageConfigTbl selectByCivAndStage(@Param("civ") String civ, @Param("stageNo") Integer stageNo);
}
