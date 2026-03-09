package ljc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ljc.battle.core.*;
import ljc.context.*; // 上下文对象
import ljc.entity.*; // 实体对象
import ljc.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BattleService {

    private final BattleSessionMapper battleSessionMapper;
    private final UserMapper userMapper;
    private final UserGeneralMapper userGeneralMapper;
    private final UserTroopMapper userTroopMapper;
    private final GeneralTemplateMapper generalTemplateMapper;
    private final TroopTemplateMapper troopTemplateMapper;
    private final UserEquipmentMapper userEquipmentMapper;
    private final EquipmentTemplateMapper equipmentTemplateMapper;
    private final UserGemMapper userGemMapper;
    private final StoryStageConfigMapper storyStageConfigMapper;
    private final UserCivProgressMapper userCivProgressMapper;
    private final StoryUnlockConfigMapper storyUnlockConfigMapper;
    
    private final DropPoolMapper dropPoolMapper;
    private final ObjectMapper objectMapper;
    private final TroopService troopService; // 新增注入：处理兵种解锁/进化权限

    // 战斗引擎（无状态，可复用）
    private final BattleEngine battleEngine = new BattleEngine();

    {
        // 注入默认技能解析器（V1）
        battleEngine.setSkillResolver(new SkillResolverImpl());
        // 状态序列化由 Spring 注入的 ObjectMapper 统一处理
    }

    // --- 开始主线战斗 ---
    @Transactional(rollbackFor = Exception.class)
    public BattleStartResult startStoryBattle(Long userId, String civ, Integer stageNo, Long generalId, Map<Integer, Integer> troopConfig) {
        // 1. 检查是否已有进行中的战斗
        BattleSessionTbl existing = battleSessionMapper.selectByUserId(userId);
        if (existing != null && existing.getStatus() == 0) {
            throw new RuntimeException("已有进行中的战斗");
        }

        // 2. 校验武将状态
        UserGeneralTbl general = userGeneralMapper.selectById(generalId);
        if (general == null || !Boolean.TRUE.equals(general.getUnlocked())) {
            throw new RuntimeException("武将未解锁");
        }
        if (general.getRestTurns() != null && general.getRestTurns() > 0) {
             throw new RuntimeException("武将正在休整中");
        }

        // 3. 读取关卡配置
        StoryStageConfigTbl stageConfig = storyStageConfigMapper.selectByCivAndStage(civ, stageNo);
        if (stageConfig == null) {
            throw new RuntimeException("关卡不存在");
        }

        // 4. 准备双方战斗数据
        BattleState.Side sideA = prepareAllySide(userId, general, troopConfig, stageConfig);
        BattleState.Side sideB = prepareEnemySide(stageConfig);

        // 5. 扣除出征兵力（库存）
        deductTroopsFromInventory(userId, sideA.troops);

        // 6. 构建战斗状态
        BattleState state = new BattleState();
        state.turnNo = 0;
        state.phase = "HERO_SOLO";
        state.sideA = sideA;
        state.sideB = sideB;
        state.rngSeed = System.currentTimeMillis();
        state.actionNo = 0;
        state.statusesA = new HashMap<>();
        state.statusesB = new HashMap<>();

        // 7. 创建战斗会话
        BattleSessionTbl session = new BattleSessionTbl();
        session.setUserId(userId);
        session.setBattleId(System.currentTimeMillis());
        session.setCiv(civ);
        session.setStageNo(stageNo);
        session.setStatus(0);
        session.setCurrentTurn(0);
        try {
            session.setContextJson(objectMapper.writeValueAsString(state));
        } catch (Exception e) {
            throw new RuntimeException("JSON error", e);
        }

        // 8. 初始化下一个行动者描述
        state.nextActorDesc = battleEngine.predictNextActor(state);
        try {
            // 持久化初始状态
            session.setContextJson(objectMapper.writeValueAsString(state));
            battleSessionMapper.insert(session);
        } catch (Exception e) {
            throw new RuntimeException("JSON error", e);
        }

        // 9. 返回 battleId + 初始快照
        BattleContext ctx = convertToContext(state, session.getBattleId(), new ArrayList<>());
        return new BattleStartResult(session.getBattleId(), ctx);
    }

    // --- 只读：获取当前战斗状态（不推进回合） ---
    public BattleContext getBattleState(Long userId) {
        BattleSessionTbl session = battleSessionMapper.selectByUserId(userId);
        if (session == null) {
            throw new RuntimeException("没有战斗记录");
        }
        try {
            BattleState state = objectMapper.readValue(session.getContextJson(), BattleState.class);
            BattleContext ctx = convertToContext(state, session.getBattleId(), new ArrayList<>());
            ctx.setCurrentTurn(session.getCurrentTurn());
            ctx.setFinished(session.getStatus() != 0);
            ctx.setWin(session.getStatus() == 1);
            return ctx;
        } catch (Exception e) {
            throw new RuntimeException("战斗上下文解析失败", e);
        }
    }

    // --- 推进回合 ---
    @Transactional(rollbackFor = Exception.class)
    public BattleContext processTurn(Long userId, Boolean castSkill, Integer clientTurnNo, String tactics) {
        // 1. 读取战斗会话
        BattleSessionTbl session = battleSessionMapper.selectByUserId(userId);
        if (session == null || session.getStatus() != 0) {
            throw new RuntimeException("没有进行中的战斗");
        }
        
        // 读取并反序列化战斗状态
        BattleState state;
        try {
            state = objectMapper.readValue(session.getContextJson(), BattleState.class);
        } catch (Exception e) {
            throw new RuntimeException("战斗上下文解析失败", e);
        }
        
        if (clientTurnNo != null) {
            int expectedTurn = session.getCurrentTurn() + 1;
            if (clientTurnNo != expectedTurn) {
                return convertToContext(state, session.getBattleId(), new ArrayList<>());
            }
        }

        TurnCommand cmd = new TurnCommand();
        cmd.clientTurnNo = state.turnNo + 1;
        cmd.tactics = tactics; // 写入前端战术指令
        
        if (Boolean.TRUE.equals(castSkill)) {
            cmd.type = TurnCommand.ActionType.SKILL; 
        } else {
            cmd.type = TurnCommand.ActionType.NORMAL;
        }

        // 4. 执行循环（一次请求推进完整回合）
        // 前端点一次“推进回合”，后端执行到回合结束或战斗结束。
        List<BattleLogEvent> aggregatedLogs = new ArrayList<>();
        
        // 保护上限，防止异常死循环
        int maxActions = 20; 
        int actions = 0;
        
        // 若战斗已结束，直接返回当前状态
        if (state.isFinished) {
             return convertToContext(state, session.getBattleId(), aggregatedLogs);
        }
        
        do {
            // 循环内修正回合号（引擎会校验精确值）
            cmd.clientTurnNo = state.turnNo + 1; 

            TurnResult res = battleEngine.processTurn(state, cmd);
            aggregatedLogs.addAll(res.logEvents);
            
            if (res.finished) break;
            
            actions++;
            // 行动序列回到 0，代表完整回合已结束
            if (state.currentActorIndex == 0) break;
            
        } while (actions < maxActions);
        
        // 5. 更新会话
        session.setCurrentTurn((int)state.turnNo);
        if (state.isFinished) {
            int result = state.isWin ? 1 : 2;
            finishBattle(session, result, state);
        } else {
            try {
                session.setContextJson(objectMapper.writeValueAsString(state));
            } catch (Exception e) {
                e.printStackTrace();
            }
            battleSessionMapper.update(session);
        }

        return convertToContext(state, session.getBattleId(), aggregatedLogs);
    }
    
    // --- 辅助：构建我方阵容 ---
    private BattleState.Side prepareAllySide(Long userId, UserGeneralTbl general, Map<Integer, Integer> troopConfig, StoryStageConfigTbl stageConfig) {
        BattleState.Side side = new BattleState.Side();
        side.troops = new ArrayList<>();
        
        // 1. 武将
        BattleState.Hero hero = new BattleState.Hero();
        GeneralTemplateTbl genTpl = generalTemplateMapper.selectById(general.getTemplateId());
        hero.name = (genTpl != null) ? genTpl.getName() : "Hero";
        hero.speed = (genTpl != null) ? genTpl.getSpeed() : 50; // 默认速度 50
        
        // 技能映射
        hero.actives = new ArrayList<>();
        hero.passives = new ArrayList<>();
        // 性格 -> 被动
        if (genTpl != null && genTpl.getPersonalityCode() != null) {
            String p = SkillIdAdapter.getEnginePassiveId(genTpl.getPersonalityCode());
            if (p != null) hero.passives.add(p);
        }
        // 先使用模板默认技能；用户自定义技能后续再接入
        if (genTpl != null && genTpl.getDefaultSkillId() != 0) {
             String skillName = SkillIdAdapter.getEngineSkillId(genTpl.getDefaultSkillId());
             if (skillName != null) hero.actives.add(skillName);
        }
        
        // 属性计算
        long maxHp = general.getMaxHp();
        long atk = (genTpl != null) ? genTpl.getBaseAtk() : 50; 
        // 当前版本先用基础值 + 等级成长；完整装备结算后续再补
        atk += (general.getLevel() - 1) * 10L;
        maxHp += (general.getLevel() - 1) * 50L;
        
        hero.maxHp = (int)maxHp;
        hero.hp = (int)maxHp;
        hero.atk = (int)atk;
        hero.isDeadOrRetreated = false;
        
        side.hero = hero;

        // 2. 兵种
        // 校验统率占用
        int capacity = general.getCapacity() == null ? 5 : general.getCapacity();
        int used = 0;
        
        if (troopConfig != null) {
            for (Map.Entry<Integer, Integer> entry : troopConfig.entrySet()) {
                if (entry.getValue() <= 0) continue;
                TroopTemplateTbl tpl = troopTemplateMapper.selectById(entry.getKey());
                if (tpl != null) {
                     int cost = tpl.getCost() == null ? 1 : tpl.getCost();
                     if (used + entry.getValue() * cost > capacity) continue; // 超统率则跳过
                     used += entry.getValue() * cost;
                     
                     // 创建兵堆并记录初始数量
                     TroopStack stack = new TroopStack(
                         tpl.getTroopId(),
                         tpl.getTroopType(), // 兵种类型编码
                         entry.getValue(), 
                         tpl.getBaseHp().intValue()
                     );
                     stack.initialCount = stack.count; // 显式设置初始数量
                     stack.frontHp = stack.unitHp;
                     side.troops.add(stack);
                }
            }
        }
        
        // 3. 城墙战损（攻城关卡）
        if ("WALL".equals(stageConfig.getStageType())) {
             int deduct = stageConfig.getWallCostTroops() == null ? 0 : stageConfig.getWallCostTroops();
             for (TroopStack s : side.troops) {
                 if (deduct <= 0) break;
                 if (s.count <= deduct) {
                     deduct -= s.count;
                     s.count = 0;
                 } else {
                     s.count -= deduct;
                     deduct = 0;
                 }
             }
             // 城墙消耗 = 真实死亡，不参与战后救援
             // 将 initialCount 同步为 wall 扣除后的 count
             for (TroopStack s : side.troops) {
                 s.initialCount = s.count;
             }
             if (side.troops.stream().allMatch(s -> s.count <= 0)) throw new RuntimeException("攻城损耗后兵力不足");
        }

        // 4. 饰品光环：当前支持兵种攻击加成
        applyAccessoryAura(side, userId, general.getId());
        
        return side;
    }

    private void applyAccessoryAura(BattleState.Side side, Long userId, Long generalId) {
        List<UserEquipmentTbl> equips = userEquipmentMapper.selectByUserId(userId);
        if (equips == null || equips.isEmpty()) return;

        int troopAtkAuraPermille = 0;
        for (UserEquipmentTbl equip : equips) {
            if (equip == null) continue;
            if (equip.getGeneralId() == null || !equip.getGeneralId().equals(generalId)) continue;
            if (!"accessory".equalsIgnoreCase(equip.getSlot())) continue;

            EquipmentTemplateTbl tpl = equipmentTemplateMapper.selectById(equip.getTemplateId());
            if (tpl == null) continue;
            if (!"TROOP".equalsIgnoreCase(tpl.getAuraScopeType())) continue;

            String auraStat = tpl.getAuraStat();
            int base = tpl.getAuraBaseValue() == null ? 0 : tpl.getAuraBaseValue();
            int growth = tpl.getAuraGrowthPerEnhance() == null ? 0 : tpl.getAuraGrowthPerEnhance();
            int value = base + growth * (equip.getEnhanceLevel() == null ? 0 : equip.getEnhanceLevel());
            if (value <= 0) continue;

            if ("TROOP_ATK_RATE".equalsIgnoreCase(auraStat) || "ATK_RATE".equalsIgnoreCase(auraStat)) {
                troopAtkAuraPermille += value;
            }
        }

        if (troopAtkAuraPermille > 0) {
            side.troopAtkRatePermille = Math.min(2000, side.troopAtkRatePermille + troopAtkAuraPermille);
        }
    }
    
    private BattleState.Side prepareEnemySide(StoryStageConfigTbl config) {
        BattleState.Side side = new BattleState.Side();
        side.troops = new ArrayList<>();
        BattleState.Hero hero = new BattleState.Hero();
        
        try {
            Map<String, Object> map = objectMapper.readValue(config.getEnemyConfigJson(), Map.class);
            Map<String, Object> hMap = (Map) map.get("hero");
            List<Map> tList = (List) map.get("troops");
            
            hero.name = (String) hMap.getOrDefault("name", "Enemy");
            hero.maxHp = ((Number) hMap.getOrDefault("maxHp", 1000)).intValue();
            hero.hp = hero.maxHp;
            hero.atk = ((Number) hMap.getOrDefault("atk", 100)).intValue();
            hero.speed = ((Number) hMap.getOrDefault("speed", 50)).intValue();
            hero.actives = new ArrayList<>();
            side.hero = hero;
            
            if (tList != null) {
                for (Map t : tList) {
                    int tId = ((Number) t.getOrDefault("troopId", 0)).intValue(); // 敌方兵种ID
                    String type = (String) t.getOrDefault("type", "INF");
                    int count = ((Number) t.getOrDefault("count", 10)).intValue();
                    int unitHp = ((Number) t.getOrDefault("unitHp", 20)).intValue();
                    
                    TroopStack s = new TroopStack(tId, type, count, unitHp);
                    s.initialCount = count;
                    s.frontHp = unitHp;
                    side.troops.add(s);
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Enemy Config Error", e);
        }
        return side;
    }
    
    // --- 通用辅助 ---
    private void validateGeneral(UserGeneralTbl g, Long userId) {
        if (!g.getUserId().equals(userId)) throw new RuntimeException("General not owner");
        if (!Boolean.TRUE.equals(g.getActivated())) throw new RuntimeException("Not activated");
        if (g.getRestTurns() != null && g.getRestTurns() > 0) throw new RuntimeException("Resting");
    }
    
    private void validateProgress(Long userId, String civ, Integer stage) {
        UserCivProgressTbl p = userCivProgressMapper.selectByUserIdAndCiv(userId, civ);
        if (p == null || !Boolean.TRUE.equals(p.getUnlocked())) throw new RuntimeException("Civ Locked");
        int max = p.getMaxStageCleared() == null ? 0 : p.getMaxStageCleared();
        if (stage > max + 1) throw new RuntimeException("Stage Locked");
    }
    
    private void deductTroopsFromInventory(Long userId, List<TroopStack> stacks) {
        if (stacks == null) return;
        for (TroopStack s : stacks) {
            if (s.troopId > 0 && s.initialCount > 0) {
                int res = userTroopMapper.safeDeduct(userId, s.troopId, s.initialCount);
                if (res <= 0) {
                    throw new RuntimeException("Troops not enough: " + s.type);
                }
            }
        }
    }

    private void deductTroopsFromInventory(Long userId, Map<Integer, Integer> config) {
         if (config == null) return;
         for (Map.Entry<Integer, Integer> e : config.entrySet()) {
             if (e.getValue() > 0) {
                 userTroopMapper.safeDeduct(userId, e.getKey(), e.getValue());
             }
         }
    }
    
    // 结束战斗
    private void finishBattle(BattleSessionTbl session, int result, BattleState state) {
        session.setStatus(result);
        session.setUpdatedAt(LocalDateTime.now());
        try {
            session.setContextJson(objectMapper.writeValueAsString(state));
        } catch (Exception e) {
            e.printStackTrace();
        }
        battleSessionMapper.update(session);
        
        // 结算与奖励
        handleSettlement(session, state, result == 1);
    }
    
    private void handleSettlement(BattleSessionTbl session, BattleState state, boolean isWin) {
        Long userId = session.getUserId();
        String civ = session.getCiv();
        Integer stageNo = session.getStageNo();
        
        // 1. 兵力返还
        // 规则：存活100%返还；阵亡按胜利50%/失败10%救援返还。
        double rescueRate = isWin ? 0.5 : 0.1;
        
        for (TroopStack s : state.sideA.troops) {
            if (s.troopId <= 0) continue; // 跳过召唤物/未知兵种
            
            int survivors = s.count;
            int initial = s.initialCount > survivors ? s.initialCount : survivors; // 安全保护
            int dead = initial - survivors;
            int rescued = (int) (dead * rescueRate);
            int totalReturn = survivors + rescued;
            
            if (totalReturn > 0) {
                userTroopMapper.upsertAdd(userId, s.troopId, (long)totalReturn);
            }
        }
        
        // 2. 胜利奖励（仅胜利发放）
        if (isWin) {
            handleVictoryRewards(userId, civ, stageNo);
        }
    }

    private void handleVictoryRewards(Long userId, String civ, Integer stageNo) {
        // A. 更新主线进度（最大通关关卡）
        UserCivProgressTbl p = userCivProgressMapper.selectByUserIdAndCiv(userId, civ);
        if (p != null) {
            if (stageNo > (p.getMaxStageCleared() == null ? 0 : p.getMaxStageCleared())) {
                p.setMaxStageCleared(stageNo);
                userCivProgressMapper.update(p);
            }
        } else {
            // 理论上进入战斗前已存在，兜底创建
            p = new UserCivProgressTbl();
            p.setUserId(userId);
            p.setCiv(civ);
            p.setMaxStageCleared(stageNo);
            p.setUnlocked(true);
            userCivProgressMapper.insert(p);
        }
        
        // B. 配置解锁（武将、国家、兵种、进化权限）
        StoryUnlockConfigTbl unlock = storyUnlockConfigMapper.selectByCivAndStage(civ, stageNo);
        if (unlock != null) {
            // 解锁武将（防重：已拥有则跳过）
            if (unlock.getUnlockGeneralTemplateId() != null) {
                Integer gid = unlock.getUnlockGeneralTemplateId();
                UserGeneralTbl existing = userGeneralMapper.selectByUserIdAndTemplateId(userId, gid);
                if (existing == null) {
                    UserGeneralTbl ug = new UserGeneralTbl();
                    ug.setUserId(userId);
                    ug.setTemplateId(gid);
                    ug.setUnlocked(true);
                    ug.setActivated(false);
                    ug.setLevel(1);
                    ug.setTier(0);
                    
                    GeneralTemplateTbl tpl = generalTemplateMapper.selectById(gid);
                    if (tpl != null) {
                        ug.setMaxHp(tpl.getBaseHp());
                        ug.setCurrentHp(tpl.getBaseHp());
                        ug.setCapacity(tpl.getBaseCapacity());
                    } else {
                        ug.setMaxHp(100L); ug.setCurrentHp(100L); ug.setCapacity(5);
                    }
                    userGeneralMapper.insert(ug);
                }
            }
            
            // 解锁下一国家
            if (unlock.getUnlockNextCiv() != null) {
                String nextCiv = unlock.getUnlockNextCiv();
                UserCivProgressTbl np = userCivProgressMapper.selectByUserIdAndCiv(userId, nextCiv);
                if (np == null) {
                    np = new UserCivProgressTbl();
                    np.setUserId(userId);
                    np.setCiv(nextCiv);
                    np.setUnlocked(true);
                    np.setMaxStageCleared(0);
                    userCivProgressMapper.insert(np);
                } else if (!Boolean.TRUE.equals(np.getUnlocked())) {
                    np.setUnlocked(true);
                    userCivProgressMapper.update(np);
                }
            }
            
            // 解锁兵种
            if (unlock.getUnlockTroopId() != null) {
                troopService.unlockTroop(userId, unlock.getUnlockTroopId());
            }
            
            // 解锁进化权限
            if (unlock.getUnlockEvolutionTroopId() != null) {
                troopService.unlockEvolution(userId, unlock.getUnlockEvolutionTroopId());
            }
        }
        
        // C. 掉落（金）—— 当前版本只发第一个金币条目
        StoryStageConfigTbl stageCfg = storyStageConfigMapper.selectByCivAndStage(civ, stageNo);
        if (stageCfg != null && stageCfg.getDropPoolId() != null) {
             DropPoolTbl pool = dropPoolMapper.selectById(stageCfg.getDropPoolId());
             if (pool != null && pool.getEntriesJson() != null) {
                 try {
                     List<Map<String, Object>> entries = objectMapper.readValue(pool.getEntriesJson(), List.class);
                     // 找到第一个金币条目后发放一次
                     for (Map<String, Object> entry : entries) {
                         String type = (String) entry.getOrDefault("type", "");
                         if ("GOLD".equals(type)) {
                             int min = ((Number) entry.getOrDefault("min", 0)).intValue();
                             int max = ((Number) entry.getOrDefault("max", 0)).intValue();
                             int val = min + (int)(Math.random() * (max - min + 1));
                             if (val > 0) {
                                  userMapper.reduceGold(userId, -val);
                             }
                             break; // 仅发一次
                         }
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
        }
    }

    // 转换为前端上下文结构
    private BattleContext convertToContext(BattleState state, Long battleId, List<BattleLogEvent> logs) {
        BattleContext ctx = new BattleContext();
        ctx.setBattleId(battleId);
        
        // V2：直接映射
        ctx.setSideA(state.sideA);
        ctx.setSideB(state.sideB);
        ctx.setNextActorDesc(state.nextActorDesc);
        ctx.setLastEvents(logs);
        ctx.setTurnNo((long)state.turnNo);
        ctx.setPhase(state.phase);
        ctx.setFinished(state.isFinished);
        ctx.setWin(state.isWin);

        // 兼容旧前端：保留字符串日志
        List<String> strLogs = new ArrayList<>();
        if(logs != null) {
            for (BattleLogEvent e : logs) {
                strLogs.add(String.format("[%s] %s -> %s: %s (Val:%d)", e.type, e.actorId, e.targetId, e.desc, e.value));
            }
            ctx.setLogs(strLogs);
        }
        
        // 兼容旧前端：保留 ally/enemy 结构
        BattleContext.SideContext ally = new BattleContext.SideContext();
        ally.setHero(mapHero(state.sideA.hero));
        ally.setTroops(mapTroops(state.sideA.troops));
        ctx.setAlly(ally);
        
        BattleContext.SideContext enemy = new BattleContext.SideContext();
        enemy.setHero(mapHero(state.sideB.hero));
        enemy.setTroops(mapTroops(state.sideB.troops));
        ctx.setEnemy(enemy);
        
        ctx.setCurrentTurn((int) state.turnNo);
        
        return ctx;
    }

    private BattleContext.HeroState mapHero(BattleState.Hero h) {
        if (h == null) return new BattleContext.HeroState();
        BattleContext.HeroState hs = new BattleContext.HeroState();
        hs.setName(h.name);
        hs.setCurrentHp((long)h.hp);
        hs.setMaxHp((long)h.maxHp);
        hs.setAtk((long)h.atk);
        hs.setDead(h.hp <= 0);
        return hs;
    }
    
    private List<BattleContext.TroopStack> mapTroops(List<TroopStack> list) {
        List<BattleContext.TroopStack> ret = new ArrayList<>();
        if(list == null) return ret;
        for (TroopStack s : list) {
            BattleContext.TroopStack ts = new BattleContext.TroopStack();
            ts.setType(s.type);
            ts.setCount((long)s.count);
            ts.setUnitHp((long)s.unitHp);
            ts.setFrontHp((long)s.frontHp);
            ret.add(ts);
        }
        return ret;
    }

    // 兼容旧入口：startBattle
    @Transactional(rollbackFor = Exception.class)
    public BattleStartResult startBattle(Long userId, Integer dungeonId) { 
        List<UserGeneralTbl> gs = userGeneralMapper.selectByUserId(userId);
        Long gid = null;
        for(UserGeneralTbl g : gs) { if(Boolean.TRUE.equals(g.getActivated())) { gid = g.getId(); break; }}
        if(gid == null) throw new RuntimeException("没有可出战的激活武将");
        
        return startStoryBattle(userId, "CN", dungeonId, gid, null);
    }
}
