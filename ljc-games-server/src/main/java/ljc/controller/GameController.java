package ljc.controller;

import ljc.entity.UserGeneral;
import ljc.entity.UserProfile;
import ljc.repository.UserGeneralRepository;
import ljc.repository.UserProfileRepository;
import ljc.service.BarracksService; // 关键：导入兵营服务包
import ljc.service.GachaService;
import ljc.service.WeaponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GachaService gachaService;

    @Autowired
    private WeaponService weaponService;

    @Autowired
    private UserProfileRepository profileRepo;

    @Autowired
    private BarracksService barracksService;

    @Autowired
    private UserGeneralRepository userGeneralRepository;

    /**
     * 玩家资源接口：获取金币、钻石等存档信息
     */
    @GetMapping("/profile")
    public UserProfile getProfile(@RequestParam Integer userId) {
        return profileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 抽卡接口：消耗100钻生成武将实例
     */
    @PostMapping("/gacha")
    public String draw(@RequestParam Integer userId) {
        try {
            UserGeneral g = gachaService.drawGeneral(userId, 100);
            return String.format("【招募成功】你获得了 %s 级武将 [%s]！性格为：%s",
                    "SSR", g.getName(), g.getPersonality());
        } catch (Exception e) {
            return "【抽卡失败】" + e.getMessage();
        }
    }

    /**
     * 获取玩家拥有的武将列表
     */
    @GetMapping("/generals")
    public List<UserGeneral> getMyGenerals(@RequestParam Integer userId) {
        // 获取该玩家下的所有已拥有武将实例
        return userGeneralRepository.findAll().stream()
                .filter(g -> g.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * 招募士兵接口：对应小程序“兵营”页面的请求
     */
    @PostMapping("/recruit")
    public String recruit(@RequestParam Integer userId,
                          @RequestParam Integer generalId,
                          @RequestParam String unitName,
                          @RequestParam int count) {
        try {
            // 调用 BarracksService 处理逻辑
            return barracksService.recruitTroops(userId, generalId, unitName, count);
        } catch (Exception e) {
            return "【招募失败】" + e.getMessage();
        }
    }

    /**
     * 强化接口：消耗金币强化装备
     */
    @PostMapping("/strengthen")
    public String strengthen(@RequestParam Integer userId, @RequestParam Integer equipId) {
        return weaponService.strengthenWeapon(userId, equipId);
    }
    /**
     * 选择国家接口：选择初始国家
     */
    @PostMapping("/choose-country")
    public String chooseCountry(@RequestParam Integer userId, @RequestParam String country) {
        UserProfile profile = profileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 设置文明代码：CN(汉), JP(倭), KR(高丽), EN(英)
        profile.setUnlockedCountries(country);
        profileRepo.save(profile);

        return "已成功加入文明：" + country;
    }

    @PostMapping("/assign-troops")
    public String assign(@RequestParam Integer generalId, @RequestParam String config) {
        try {
            // 将前端传来的 JSON 字符串手动转为 Map
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Integer> assignmentMap = mapper.readValue(config, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Integer>>() {});

            return barracksService.assignTroops(generalId, assignmentMap);
        } catch (Exception e) {
            return "分配报错: " + e.getMessage();
        }
    }
}