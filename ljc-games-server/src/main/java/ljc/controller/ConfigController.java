package ljc.controller;

import ljc.entity.SkillTemplateTbl;
import ljc.mapper.SkillTemplateMapper;
import ljc.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    private final SkillTemplateMapper skillTemplateMapper;
    // Inject other mappers as needed (TroopTemplateMapper, GeneralTemplateMapper, etc.)

    @GetMapping("/skills")
    public Result<List<SkillTemplateTbl>> getSkills() {
        return Result.success(skillTemplateMapper.selectAll());
    }
    
    // Add other endpoints like /troops, /generals, /equipments
}
