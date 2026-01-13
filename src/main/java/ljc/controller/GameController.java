package ljc.controller;

import ljc.entity.UserGeneral;
import ljc.entity.UserProfile;
import ljc.repository.UserProfileRepository;
import ljc.service.GachaService;
import ljc.service.WeaponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GachaService gachaService;

    @Autowired
    private WeaponService weaponService;

    @Autowired
    private UserProfileRepository profileRepo;
    // 抽卡接口
    @PostMapping("/gacha")
    public String draw(@RequestParam Integer userId){
        try {
            UserGeneral g = gachaService.drawGeneral(userId, 100); // 消耗100钻
            return String.format("【招募成功】你获得了 %s 级武将 [%s]！性格为：%s",
                    "SSR", g.getName(), g.getPersonality());
        } catch (Exception e) {
            return "【抽卡失败】" + e.getMessage();
        }
    }

    // 强化接口
    @PostMapping("/strengthen")
    public String strengthen(@RequestParam Integer userId, @RequestParam Integer equipId) {
        return weaponService.strengthenWeapon(userId, equipId);
    }

    //玩家资源接口
    @GetMapping("/profile")
    public UserProfile getProfile(@RequestParam Integer userId) {
        return profileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}