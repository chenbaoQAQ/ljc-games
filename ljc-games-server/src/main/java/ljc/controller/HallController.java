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
        hallService.equip(userId, req.getGeneralId(), req.getEquipmentId());
        return Result.success("装备成功");
    }

    @Data
    public static class EquipReq {
        private Long generalId;
        private Long equipmentId;
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
        String msg = hallService.enhanceEquipment(userId, equipmentId);
        return Result.success(msg);
    }



    @PostMapping("/skill/learn")
    public Result<String> learnSkill(@RequestParam Long userId, @RequestParam Long generalId, @RequestParam Integer bookItemId) {
        System.out.println("Controller: learnSkill called");
        hallService.learnSkill(userId, generalId, bookItemId);
        return Result.success("学习成功");
    }

    @PostMapping("/recruit")
    public Result<String> recruit(@RequestParam Long userId, @RequestBody RecruitReq req) {
        hallService.recruit(userId, req.getTroopId(), req.getCount());
        return Result.success("招募成功");
    }

    @Data
    public static class RecruitReq {
        private Integer troopId;
        private Integer count;
    }

    @PostMapping("/gem/combine")
    public Result<String> combineGem(@RequestParam Long userId, @RequestBody CombineGemReq req) {
        hallService.combineGem(userId, req.getGemType(), req.getLevel());
        return Result.success("合成成功");
    }

    @Data
    public static class CombineGemReq {
        private String gemType;
        private Integer level;
    }

    @PostMapping("/general/ascend")
    public Result<String> ascendGeneral(@RequestParam Long userId, @RequestParam Long generalId) {
        hallService.ascendGeneral(userId, generalId);
        return Result.success("升阶成功");
    }

    @GetMapping("/progress")
    public Result<java.util.List<ljc.entity.UserCivProgressTbl>> getProgress(@RequestParam Long userId) {
        return Result.success(hallService.getProgress(userId)); // Use Service method
    }

    @GetMapping("/generals")
    public Result<java.util.List<ljc.entity.UserGeneralTbl>> getGenerals(@RequestParam Long userId) {
        return Result.success(hallService.getGenerals(userId));
    }

    @GetMapping("/equipments")
    public Result<java.util.List<ljc.entity.UserEquipmentTbl>> getEquipments(@RequestParam Long userId) {
        return Result.success(hallService.getEquipments(userId));
    }

    @GetMapping("/gems")
    public Result<java.util.List<ljc.entity.UserGemTbl>> getGems(@RequestParam Long userId) {
        return Result.success(hallService.getGems(userId));
    }

    @GetMapping("/items")
    public Result<java.util.List<ljc.entity.UserInventoryTbl>> getItems(@RequestParam Long userId) {
        return Result.success(hallService.getItems(userId));
    }
}




