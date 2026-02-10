package ljc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ljc.battle.core.*;
import ljc.context.*; // BattleContext, etc
import ljc.entity.*; // UserGeneralTbl, etc
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

    // The Engine (Stateless, reused)
    private final BattleEngine battleEngine = new BattleEngine();

    {
        // Inject Default Resolver (V1)
        battleEngine.setSkillResolver(new SkillResolverImpl());
        // Configure ObjectMapper for State
        // Ensure unknown properties don't crash (forward compat)
        // Check if ObjectMapper bean is configured this way globally better.
        // Assuming default injection is fine.
    }

    // --- Start Battle (Story) ---
    @Transactional(rollbackFor = Exception.class)
    public BattleStartResult startStoryBattle(Long userId, String civ, Integer stageNo, Long generalId, Map<Integer, Integer> troopConfig) {
        // 1. Check existing session
        BattleSessionTbl existing = battleSessionMapper.selectByUserId(userId);
        if (existing != null && existing.getStatus() == 0) {
            throw new RuntimeException("已有进行中的战斗");
        }

        // 2. Validate
        UserGeneralTbl general = userGeneralMapper.selectById(generalId);
        if (general == null || !Boolean.TRUE.equals(general.getUnlocked())) {
            throw new RuntimeException("武将未解锁");
        }
        if (general.getRestTurns() != null && general.getRestTurns() > 0) {
             throw new RuntimeException("武将正在休整中");
        }

        // 3. Stage Config
        StoryStageConfigTbl stageConfig = storyStageConfigMapper.selectByCivAndStage(civ, stageNo);
        if (stageConfig == null) {
            throw new RuntimeException("关卡不存在");
        }

        // 4. Prepare Sides
        BattleState.Side sideA = prepareAllySide(userId, general, troopConfig, stageConfig);
        BattleState.Side sideB = prepareEnemySide(stageConfig);

        // 5. Deduct Troops (Inventory)
        deductTroopsFromInventory(userId, sideA.troops);

        // 6. Create Battle State
        BattleState state = new BattleState();
        state.turnNo = 0;
        state.sideA = sideA;
        state.sideB = sideB;
        state.rngSeed = System.currentTimeMillis();
        state.actionNo = 0;
        state.statusesA = new HashMap<>();
        state.statusesB = new HashMap<>();

        // 7. Save Session
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

        // 9. 初始化 nextActorDesc (V2)
        state.nextActorDesc = battleEngine.predictNextActor(state);
        try {
            // Save initial state with actor
            session.setContextJson(objectMapper.writeValueAsString(state));
            battleSessionMapper.insert(session);
        } catch (Exception e) {
            throw new RuntimeException("JSON error", e);
        }

        // 8. 返回 battleId + 初始快照
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
            throw new RuntimeException("Context Error", e);
        }
    }

    // --- Process Turn ---
    @Transactional(rollbackFor = Exception.class)
    public BattleContext processTurn(Long userId, Boolean castSkill, Integer clientTurnNo) {
        // Note: Returning BattleContext for frontend compatibility. 
        // Ideally frontend should adapt to TurnResult or BattleState.
        // For now, I will map BattleState -> BattleContext Structure broadly or just return state wrapped.
        
        // 1. Get Session
        BattleSessionTbl session = battleSessionMapper.selectByUserId(userId);
        if (session == null || session.getStatus() != 0) {
            throw new RuntimeException("没有进行中的战斗");
        }

        // 2. Load State
        BattleState state;
        try {
            state = objectMapper.readValue(session.getContextJson(), BattleState.class);
        } catch (Exception e) {
            throw new RuntimeException("Context Error", e);
        }

        // 3. Construct Command
        // Frontend sends "castSkill" boolean.
        // Construct TurnCommand compatible with Engine.
        // Engine expects sequence of commands.
        // But Frontend is "One Click Turn".
        // V1 Engine requires iterative calls for each actor action.
        // Service needs to LOOP until Round Ends or Player Input required?
        // IF we want "One Click = One Round":
        //    Loop Engine.processTurn until state.currentActorIndex == 0 (Round Reset) AND some log exists.
        
        // 幂等校验：clientTurnNo 必须 == session.currentTurn + 1
        if (clientTurnNo != null) {
            int expectedTurn = session.getCurrentTurn() + 1;
            if (clientTurnNo != expectedTurn) {
                // 不匹配 → 返回当前状态，不推进
                return convertToContext(state, session.getBattleId(), new ArrayList<>());
            }
        }

        TurnCommand cmd = new TurnCommand();
        cmd.clientTurnNo = state.turnNo + 1;
        if (Boolean.TRUE.equals(castSkill)) {
            cmd.type = TurnCommand.ActionType.SKILL; 
            // Only effective if it's Player's Turn?
            // Engine checks currentActor. 
            // If currentActor is Player Hero -> Apply Skill.
            // If currentActor is Troop -> Skill flag ignored (Normal Attack).
        } else {
            cmd.type = TurnCommand.ActionType.NORMAL;
        }

        // 4. Execution Loop (Run until Round Ends)
        // Logic: Frontend press "Turn" -> Backend runs FULL ROUND (All actors act).
        // This abstracts the queue from frontend.
        List<BattleLogEvent> aggregatedLogs = new ArrayList<>();
        
        // Safety Break
        int maxActions = 20; 
        int actions = 0;
        
        // Initial check: if Battle Finished
        if (state.isFinished) {
             return convertToContext(state, session.getBattleId(), aggregatedLogs);
        }
        
        // The Engine processes ONE ACTION per call.
        // We want to run one full round (or until battle ends).
        // Round start condition: currentActorIndex == 0.
        // We start: process(action 0).
        // then process(action 1)...
        // until process(action N) -> index resets to 0.
        
        boolean roundStarted = (state.currentActorIndex == 0);
        
        do {
            // Fix clientTurnNo for loop (Engine checks exact match)
            cmd.clientTurnNo = state.turnNo + 1; 
            
            // Pass Skill flag only for Player Hero? 
            // If we loop, we reuse same cmd?
            // "castSkill" applies to the whole round? 
            // If Player Hero acts in this round, he uses skill.
            // If not (e.g. Stunned), flag wasted. OK.
            
            TurnResult res = battleEngine.processTurn(state, cmd);
            aggregatedLogs.addAll(res.logEvents);
            
            if (res.finished) break;
            
            actions++;
            // If index went back to 0, Round Finished.
            if (state.currentActorIndex == 0) break;
            
        } while (actions < maxActions);
        
        // 5. Update Session
        session.setCurrentTurn((int)state.turnNo); // Approx
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
    
    // --- Helper: Prepare Ally Side ---
    private BattleState.Side prepareAllySide(Long userId, UserGeneralTbl general, Map<Integer, Integer> troopConfig, StoryStageConfigTbl stageConfig) {
        BattleState.Side side = new BattleState.Side();
        side.troops = new ArrayList<>();
        
        // 1. Hero
        BattleState.Hero hero = new BattleState.Hero();
        GeneralTemplateTbl genTpl = generalTemplateMapper.selectById(general.getTemplateId());
        hero.name = (genTpl != null) ? genTpl.getName() : "Hero";
        hero.speed = (genTpl != null) ? genTpl.getSpeed() : 50; // Default 50
        
        // Skills Mapping
        hero.actives = new ArrayList<>();
        hero.passives = new ArrayList<>();
            // Personality -> Passive
        if (genTpl != null && genTpl.getPersonalityCode() != null) {
            String p = SkillIdMapper.getEnginePassiveId(genTpl.getPersonalityCode());
            if (p != null) hero.passives.add(p);
        }
            // User Skill (DB: user_general_skill) -> Active
            // Need mapper (not yet autowired? Add it or query directly)
            // Simplified: Default Skill from Template
        if (genTpl != null && genTpl.getDefaultSkillId() != 0) {
             String skillName = SkillIdMapper.getEngineSkillId(genTpl.getDefaultSkillId());
             if (skillName != null) hero.actives.add(skillName);
        }
        
        // Stats
        long maxHp = general.getMaxHp();
        // Calc Equipment Stats... (Same as before code, omitted for brevity, assuming Load logic)
        // Let's assume UserGeneralTbl has *final* values or we calc basic here
        // Re-use logic:
        long atk = (genTpl != null) ? genTpl.getBaseAtk() : 50; 
        // ... (Equipment calc omitted, using base + level*10 for MVP)
        atk += (general.getLevel() - 1) * 10L;
        maxHp += (general.getLevel() - 1) * 50L;
        
        hero.maxHp = (int)maxHp;
        hero.hp = (int)maxHp;
        hero.atk = (int)atk;
        hero.isDeadOrRetreated = false;
        
        side.hero = hero;

        // 2. Troops
        // Validate Capacity
        int capacity = general.getCapacity() == null ? 5 : general.getCapacity();
        int used = 0;
        
        if (troopConfig != null) {
            for (Map.Entry<Integer, Integer> entry : troopConfig.entrySet()) {
                if (entry.getValue() <= 0) continue;
                TroopTemplateTbl tpl = troopTemplateMapper.selectById(entry.getKey());
                if (tpl != null) {
                     int cost = tpl.getCost() == null ? 1 : tpl.getCost();
                     if (used + entry.getValue() * cost > capacity) continue; // Skip overflow
                     used += entry.getValue() * cost;
                     
                     // Create Stack with troopId and initialCount
                     TroopStack stack = new TroopStack(
                         tpl.getTroopId(),
                         tpl.getTroopType(), // "INF"
                         entry.getValue(), 
                         tpl.getBaseHp().intValue()
                     );
                     stack.initialCount = stack.count; // Explicitly set for new field
                     stack.frontHp = stack.unitHp;
                     side.troops.add(stack);
                }
            }
        }
        
        // 3. Wall Cost Logic (Reuse logic: reduce counts)
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
             if (side.troops.stream().allMatch(s -> s.count <= 0)) throw new RuntimeException("Troops died at wall");
        }
        
        return side;
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
                    int tId = ((Number) t.getOrDefault("troopId", 0)).intValue(); // Enemy troop ID
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
    
    // --- Helpers ---
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
    
    // Finish
    private void finishBattle(BattleSessionTbl session, int result, BattleState state) {
        session.setStatus(result);
        session.setUpdatedAt(LocalDateTime.now());
        try {
            session.setContextJson(objectMapper.writeValueAsString(state));
        } catch (Exception e) {
            e.printStackTrace();
        }
        battleSessionMapper.update(session);
        
        // Handle Rewards & Settlement
        // Even if lose, we might recover some troops
        handleSettlement(session, state, result == 1);
    }
    
    private void handleSettlement(BattleSessionTbl session, BattleState state, boolean isWin) {
        Long userId = session.getUserId();
        String civ = session.getCiv();
        Integer stageNo = session.getStageNo();
        
        // 1. Troop Recovery (Dead Troops Rescue)
        // Rule: Survivors return 100%. Dead troops recovered at 50% (Win) or 10% (Lose).
        double rescueRate = isWin ? 0.5 : 0.1;
        
        for (TroopStack s : state.sideA.troops) {
            if (s.troopId <= 0) continue; // Skip summons/unknown
            
            int survivors = s.count;
            int initial = s.initialCount > survivors ? s.initialCount : survivors; // Safety
            int dead = initial - survivors;
            int rescued = (int) (dead * rescueRate);
            int totalReturn = survivors + rescued;
            
            if (totalReturn > 0) {
                userTroopMapper.upsertAdd(userId, s.troopId, (long)totalReturn);
            }
        }
        
        // 2. Victory Rewards (Only if Win)
        if (isWin) {
            handleVictoryRewards(userId, civ, stageNo);
        }
    }

    private void handleVictoryRewards(Long userId, String civ, Integer stageNo) {
        // A. Update Progress (Max Stage)
        UserCivProgressTbl p = userCivProgressMapper.selectByUserIdAndCiv(userId, civ);
        if (p != null) {
            if (stageNo > (p.getMaxStageCleared() == null ? 0 : p.getMaxStageCleared())) {
                p.setMaxStageCleared(stageNo);
                userCivProgressMapper.update(p);
            }
        } else {
            // Should exist if we entered battle, but safe fallback
            p = new UserCivProgressTbl();
            p.setUserId(userId);
            p.setCiv(civ);
            p.setMaxStageCleared(stageNo);
            p.setUnlocked(true);
            userCivProgressMapper.insert(p);
        }
        
        // B. Configuration Unlocks (General, Civ)
        StoryUnlockConfigTbl unlock = storyUnlockConfigMapper.selectByCivAndStage(civ, stageNo);
        if (unlock != null) {
            // Unlock General（防重：已拥有则跳过）
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
            
            // Unlock Next Civ
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
        }
        
        // C. Drops (Gold) — MVP: 只取第一个 GOLD entry 发一次
        StoryStageConfigTbl stageCfg = storyStageConfigMapper.selectByCivAndStage(civ, stageNo);
        if (stageCfg != null && stageCfg.getDropPoolId() != null) {
             DropPoolTbl pool = dropPoolMapper.selectById(stageCfg.getDropPoolId());
             if (pool != null && pool.getEntriesJson() != null) {
                 try {
                     List<Map<String, Object>> entries = objectMapper.readValue(pool.getEntriesJson(), List.class);
                     // MVP: 找到第一个 GOLD entry 就发，不循环所有
                     for (Map<String, Object> entry : entries) {
                         String type = (String) entry.getOrDefault("type", "");
                         if ("GOLD".equals(type)) {
                             int min = ((Number) entry.getOrDefault("min", 0)).intValue();
                             int max = ((Number) entry.getOrDefault("max", 0)).intValue();
                             int val = min + (int)(Math.random() * (max - min + 1));
                             if (val > 0) {
                                  userMapper.reduceGold(userId, -val);
                             }
                             break; // 只发一次
                         }
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
        }
    }

    // Convert to Context for Frontend
    private BattleContext convertToContext(BattleState state, Long battleId, List<BattleLogEvent> logs) {
        BattleContext ctx = new BattleContext();
        ctx.setBattleId(battleId);
        
        // V2: Direct Mapping
        ctx.setSideA(state.sideA);
        ctx.setSideB(state.sideB);
        ctx.setNextActorDesc(state.nextActorDesc);
        ctx.setLastEvents(logs);
        ctx.setTurnNo((long)state.turnNo);
        ctx.setFinished(state.isFinished);
        ctx.setWin(state.isWin);

        // Map Logs (Legacy String)
        List<String> strLogs = new ArrayList<>();
        if(logs != null) {
            for (BattleLogEvent e : logs) {
                strLogs.add(String.format("[%s] %s -> %s: %s (Val:%d)", e.type, e.actorId, e.targetId, e.desc, e.value));
            }
            ctx.setLogs(strLogs);
        }
        
        // Ally/Enemy (Legacy Mapping, keep for safety)
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

    // Start Battle Override (Legacy)
    @Transactional(rollbackFor = Exception.class)
    public BattleStartResult startBattle(Long userId, Integer dungeonId) { 
        List<UserGeneralTbl> gs = userGeneralMapper.selectByUserId(userId);
        Long gid = null;
        for(UserGeneralTbl g : gs) { if(Boolean.TRUE.equals(g.getActivated())) { gid = g.getId(); break; }}
        if(gid == null) throw new RuntimeException("No active general");
        
        return startStoryBattle(userId, "CN", dungeonId, gid, null);
    }
}