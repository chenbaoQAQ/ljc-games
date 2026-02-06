package ljc.service;

import ljc.entity.StoryStageConfigTbl;
import ljc.mapper.StoryStageConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StageService {
    private final StoryStageConfigMapper storyStageConfigMapper;

    public StoryStageConfigTbl getStageConfig(String civ, Integer stageNo) {
        return storyStageConfigMapper.selectByCivAndStage(civ, stageNo);
    }
}
