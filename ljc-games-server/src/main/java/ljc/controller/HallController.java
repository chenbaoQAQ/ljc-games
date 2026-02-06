package ljc.controller;

import ljc.common.Result;
import ljc.service.HallService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hall")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @PostMapping("/general/upgrade")
    public Result<String> upgradeGeneral(@RequestParam Long userId, @RequestParam Long generalId) {
        hallService.upgradeGeneral(userId, generalId);
        return Result.success("升级成功");
    }

    @PostMapping("/general/equip")
    public Result<String> equipGeneral(@RequestParam Long userId, @RequestBody EquipReq req) {
        hallService.equip(userId, req.getGeneralId(), req.getEquipmentId(), req.getSlot());
        return Result.success("装备成功");
    }

    @Data
    public static class EquipReq {
        private Long generalId;
        private Long equipmentId;
        private String slot; // weapon, armor1, etc.
    }

    @PostMapping("/gem/inlay")
    public Result<String> inlayGem(@RequestParam Long userId, @RequestBody InlayGemReq req) {
        hallService.inlayGem(userId, req.getEquipmentId(), req.getSocketIndex(), req.getGemId());
        return Result.success("镶嵌成功");
    }

    @Data
    public static class InlayGemReq {
        private Long equipmentId;
        private Integer socketIndex;
        private Long gemId;
    }

    @PostMapping("/general/activate")
    public Result<String> activateGeneral(@RequestParam Long userId, @RequestParam Long generalId) {
        hallService.activateGeneral(userId, generalId);
        return Result.success("激活成功");
    }

    @PostMapping("/equipment/enhance")
    public Result<String> enhanceEquipment(@RequestParam Long userId, @RequestParam Long equipmentId) {
        hallService.enhanceEquipment(userId, equipmentId);
        return Result.success("强化成功");
    


    @PostMapping("/skill/learn")
    public Result<String> learnSkill(@RequestParam Long userId, @RequestParam Long generalId, @RequestParam Integer bookItemId) {
        hallService.learnSkill(userId, generalId, bookItemId);
        return Result.success("学习成功");
    }
}

}


