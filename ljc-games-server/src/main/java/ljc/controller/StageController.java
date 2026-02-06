package ljc.controller;

import ljc.common.Result;
import ljc.entity.StoryStageConfigTbl;
import ljc.service.StageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stage")
@RequiredArgsConstructor
public class StageController {
    
    private final StageService stageService;

    @GetMapping("/story/{civ}/{stageNo}")
    public Result<StoryStageConfigTbl> getStoryStage(@PathVariable String civ, @PathVariable Integer stageNo) {
        StoryStageConfigTbl config = stageService.getStageConfig(civ, stageNo);
        if (config == null) {
            return Result.error("Stage config not found");
        }
        return Result.success(config);
    }
}
