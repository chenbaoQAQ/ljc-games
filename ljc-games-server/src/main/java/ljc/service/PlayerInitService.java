package ljc.service;

import ljc.entity.*;
import ljc.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerInitService {

    private final UserCivProgressMapper civMapper;
    private final UserGeneralMapper userGeneralMapper;
    private final UserGeneralSkillMapper userGeneralSkillMapper;
    private final UserTroopMapper userTroopMapper;
    private final GeneralTemplateMapper generalTemplateMapper;
    private final TroopTemplateMapper troopTemplateMapper;

    // 常量配置
    private static final int START_GENERAL_ID = 1001; // 初始武将ID
    private static final int START_TROOP_ID = 2001;      // 义勇兵ID (或CN步兵ID)
    private static final int START_TROOP_COUNT = 100; // 送100个

    /**
     * 初始化新玩家的所有数据
     * @param userId 已创建的用户ID
     * @param chosenCiv 用户选择的初始国家 (CN/JP/KR/GB)
     */
    @Transactional(rollbackFor = Exception.class)
    public void initPlayerData(long userId, String chosenCiv) {

        // 1. 初始化四国进度
        String[] allCivs = {"CN", "JP", "KR", "GB"};
        for (String civ : allCivs) {
            UserCivProgressTbl progress = new UserCivProgressTbl();
            progress.setUserId(userId);
            progress.setCiv(civ);

            // 如果 civ 等于 chosenCiv (注意字符串比较!)，unlocked = true，否则 false
            if (civ.equals(chosenCiv)) {
                progress.setUnlocked(true);
            } else {
                progress.setUnlocked(false);
            }
            // setMaxStageCleared(0);
            progress.setMaxStageCleared(0);
            // civMapper.insert(progress);
            civMapper.insert(progress);
            // ----------------------------------------------------
        }

        // 2. 发放初始武将
        // 先查模板，获取基础属性
        GeneralTemplateTbl tpl = generalTemplateMapper.selectById(START_GENERAL_ID);
        if (tpl == null) {
            throw new RuntimeException("初始武将模板缺失: " + START_GENERAL_ID);
        }

        UserGeneralTbl general = new UserGeneralTbl();
        general.setUserId(userId);
        general.setTemplateId(START_GENERAL_ID);

        // A. 设置 activated = true, unlocked = true
        //if里面无法直接写get
        general.setUnlocked(true);
        general.setActivated(true);

        // B. 设置 level = 1, currentHp = tpl.getBaseHp(), maxHp = tpl.getBaseHp()
        // C. 设置 capacity = tpl.getBaseCapacity()

        general.setLevel(1);
        general.setCurrentHp(tpl.getBaseHp());       // 从模板抄基础血量
        general.setMaxHp(tpl.getBaseHp());           // 从模板抄最大血量
        general.setCapacity(tpl.getBaseCapacity());  // 从模板抄带兵量
        // 阶数默认为 0
        general.setTier(0);
        // 休息回合默认为 0
        general.setRestTurns(0);
        
        // Initialize equipment slots - No longer needed after refactor
        // general.setEquipWeaponId(0L); ... removed


        // D. 插入武将: userGeneralMapper.insert(general);
        userGeneralMapper.insert(general);

        // E. 关键: 获取插入后的自增 ID，并插入初始技能
        Long newGeneralId = general.getId();
        //取技能表
        UserGeneralSkillTbl skill = new UserGeneralSkillTbl();
        //填数据，属于武将的id和自己的id
        skill.setGeneralId(newGeneralId);
        skill.setCurrentSkillId(tpl.getDefaultSkillId());
        //存档
        userGeneralSkillMapper.insert(skill);
        // ----------------------------------------------------


        // 3. 发放初始兵力
        TroopTemplateTbl troopTpl = troopTemplateMapper.selectById(START_TROOP_ID);
        if (troopTpl == null) {
            throw new RuntimeException("初始兵种模板缺失: ID " + START_TROOP_ID);
        }

        // 创建 UserTroopTbl 对象
        UserTroopTbl troop = new UserTroopTbl();
        // 给予 userId, START_TROOP_ID, START_TROOP_COUNT
        troop.setUserId(userId);
        troop.setTroopId(START_TROOP_ID);
        troop.setCount((long) START_TROOP_COUNT);

        // 插入数据库
        userTroopMapper.insert(troop);
        // ----------------------------------------------------

        System.out.println("玩家 " + userId + " 数据初始化完成！");
    }
}