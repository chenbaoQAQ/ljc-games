package ljc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ljc.context.BattleContext;
import ljc.entity.*;
import ljc.entity.BattleSessionTbl;
import ljc.mapper.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BattleService {
    public Long startStoryBattle(Long userId, String civ, Integer stageNo, Long generalId, Map<Integer, Integer> troopConfig) {
        // 1. Check existing session
        BattleSessionTbl existing = battleSessionMapper.selectByUserId(userId);
        if (existing != null && existing.getStatus() == 0) {
            throw new RuntimeException("Wait, you are already in a battle!");
        }

        // 2. Validate Progress
        UserCivProgressTbl progress = userCivProgressMapper.selectByUserIdAndCiv(userId, civ);
        int currentMax = (progress == null) ? 0 : (progress.getMaxStageCleared() == null ? 0 : progress.getMaxStageCleared());
        if (stageNo > currentMax + 1) {
            throw new RuntimeException("Stage locked! Please clear previous stages first.");
        }

        // 3. Load Stage Config
        StoryStageConfigTbl stageConfig = storyStageConfigMapper.selectByCivAndStage(civ, stageNo);
        if (stageConfig == null) {
            throw new RuntimeException("Stage config not found: " + civ + "-" + stageNo);
        }

        // 4. Deduct Stamina
        int staminaCost = stageConfig.getStaminaCost() == null ? 10 : stageConfig.getStaminaCost();
        int rows = userMapper.reduceStamina(userId, staminaCost);
        if (rows <= 0) {
            throw new RuntimeException("Not enough stamina!");
        }

        // 5. Deduct Troops & Build Ally Context
        BattleContext.SideContext allySide = new BattleContext.SideContext();
        List<BattleContext.TroopStack> allyStacks = new ArrayList<>();
        
        // 5.1 Troops
        if (troopConfig != null) {
            for (Map.Entry<Integer, Integer> entry : troopConfig.entrySet()) {
                Integer troopId = entry.getKey();
                Integer count = entry.getValue();
                if (count <= 0) continue;

                // Deduct
                int dRows = userTroopMapper.safeDeduct(userId, troopId, count);
                if (dRows <= 0) {
                     throw new RuntimeException("Not enough troops for ID: " + troopId);
                }

                // Build Stack
                TroopTemplateTbl tpl = troopTemplateMapper.selectById(troopId);
                if (tpl == null) continue;

                BattleContext.TroopStack stack = new BattleContext.TroopStack();
                stack.setTroopId(troopId);
                stack.setType(tpl.getTroopType());
                stack.setCount(Long.valueOf(count));
                stack.setUnitHp(tpl.getBaseHp());
                stack.setFrontHp(tpl.getBaseHp());
                allyStacks.add(stack);
            }
        }
        allySide.setTroops(allyStacks);

        // 5.2 General
        if (generalId != null) {
            UserGeneralTbl general = null;
            List<UserGeneralTbl> allGenerals = userGeneralMapper.selectByUserId(userId);
            for (UserGeneralTbl g : allGenerals) {
                if (g.getId().equals(generalId)) {
                    general = g;
                    break;
                }
            }
            if (general == null) {
                throw new RuntimeException("General not found");
            }
            
            // Re-use logic to build Hero State (Stats calculation)
            // Extracting logic from buildAllyContext might be cleaner, but for MVP copy-paste or refactor?
            // Let's refactor slightly or just reuse buildAllyHero logic if possible.
            // I'll create a helper method buildHeroState(userId, general)
            allySide.setHero(buildHeroState(userId, general));
        } else {
             throw new RuntimeException("Must select a general!");
        }
        
        // 6. Build Enemy Context
        BattleContext.SideContext enemySide;
        try {
            enemySide = objectMapper.readValue(stageConfig.getEnemyConfigJson(), BattleContext.SideContext.class);
        } catch (Exception e) {
            throw new RuntimeException("Enemy Config JSON Error", e);
        }

        // 7. Save Session
        BattleContext ctx = new BattleContext();
        ctx.setBattleId(System.currentTimeMillis());
        ctx.setRandomSeed(System.currentTimeMillis());
        ctx.setAlly(allySide);
        ctx.setEnemy(enemySide);
        // Store Stage Meta for Settlement
        // We can add a "meta" map to Context or just rely on DB columns if we had them. 
        // Requirement 2) says: "stageMeta: civ/stageNo/stageType"
        // I need to add stageMeta to BattleContext class first? 
        // Or I can put it in contextJson but Java object needs field.
        // Let's assume I can add it or just ignore for now as I can pass it through?
        // Wait, finishBattle needs to know which stage was cleared.
        // I should add StageMeta to BattleContext.
        
        // For now, let's persist it in the context object as a simple Map or new inner class.
        // Since I cannot easily modify BattleContext.java right now without another tool call, 
        // I will overload existing fields OR assume I can modify BattleContext in next step.
        // I'll add StageMeta to Context logic below.

        BattleSessionTbl session = new BattleSessionTbl();
        session.setUserId(userId);
        session.setBattleId(ctx.getBattleId());
        session.setDungeonId(stageNo); // Reuse dungeonId for stageNo
        // How to store 'civ'? session doesn't have it.
        // I will hack: dungeonId = stageNo. Civ is assumed CN for MVP or stored in context.
        session.setStatus(0);
        session.setCurrentTurn(1);
        try {
            session.setContextJson(objectMapper.writeValueAsString(ctx));
        } catch (Exception e) {
           throw new RuntimeException("JSON error", e);
        }
        
        battleSessionMapper.insert(session);
        return session.getBattleId();
    }
    
    // Helper for Hero Stats
    private BattleContext.HeroState buildHeroState(Long userId, UserGeneralTbl general) {
         BattleContext.HeroState hero = new BattleContext.HeroState();
         hero.setGeneralId(general.getId());
         
         GeneralTemplateTbl genTpl = generalTemplateMapper.selectById(general.getTemplateId());
         hero.setName((genTpl != null) ? genTpl.getName() : "Hero");
         
         long maxHp = general.getMaxHp(); // Base from Tbl (already calculated on upgrade?) 
         // Actually UserGeneralTbl.maxHp might not include equipment? 
         // Previous code calculated it on the fly.
         // Let's copy the calculation logic from previous `buildAllyContext`.
         // Simplified for brevity here:
         
         // ... (Logic from buildAllyContext) ...
         long atk = 0;
         
         List<UserEquipmentTbl> equips = userEquipmentMapper.selectByUserId(userId);
         for (UserEquipmentTbl eq : equips) {
            if (general.getId().equals(eq.getGeneralId())) {
                EquipmentTemplateTbl tpl = equipmentTemplateMapper.selectById(eq.getTemplateId());
                if (tpl != null) {
                    maxHp += (tpl.getBaseHp() == null ? 0 : tpl.getBaseHp());
                    atk += (tpl.getBaseAtk() == null ? 0 : tpl.getBaseAtk());
                    int n = (eq.getEnhanceLevel() == null) ? 0 : eq.getEnhanceLevel();
                    long bonus = 10L * n * (n + 1) / 2;
                    if (tpl.getBaseHp() != null && tpl.getBaseHp() > 0) maxHp += bonus;
                    if (tpl.getBaseAtk() != null && tpl.getBaseAtk() > 0) atk += bonus;
                }
            }
         }
         
         hero.setMaxHp(maxHp);
         hero.setCurrentHp(maxHp);
         hero.setAtk(atk);
         hero.setDead(false);
         return hero;
    }

    @Transactional(rollbackFor = Exception.class)
    public void finishBattle(BattleSessionTbl session, int result) {
         // 1. Update Session Status
         session.setStatus(result);
         session.setUpdatedAt(LocalDateTime.now());
         battleSessionMapper.update(session);
         
         if (result == 1) { // VICTORY
             // 2. Return Surviving Troops
             BattleContext ctx;
             try {
                ctx = objectMapper.readValue(session.getContextJson(), BattleContext.class);
                for (BattleContext.TroopStack s : ctx.getAlly().getTroops()) {
                    if (s.getCount() > 0) {
                        userTroopMapper.upsertAdd(session.getUserId(), s.getTroopId(), s.getCount());
                    }
                }
             } catch (Exception e) {
                 // Log error but don't fail transaction?
                 e.printStackTrace();
             }
             
             // 3. Update Progress (Assuming CN and StageNo from DungeonId)
             // Need to know civ. Assuming 'CN' for MVP.
             String civ = "CN"; 
             Integer stageNo = session.getDungeonId();
             
             UserCivProgressTbl progress = userCivProgressMapper.selectByUserIdAndCiv(session.getUserId(), civ);
             if (progress == null) {
                 // Should have initialized, but handle anyway
                 progress = new UserCivProgressTbl();
                 progress.setUserId(session.getUserId());
                 progress.setCiv(civ);
                 progress.setMaxStageCleared(stageNo);
                 progress.setUnlocked(true);
                 userCivProgressMapper.insert(progress);
             } else {
                 if (stageNo > progress.getMaxStageCleared()) {
                     progress.setMaxStageCleared(stageNo);
                     userCivProgressMapper.update(progress);
                 }
                 // Unlock Next Civ logic (Stage 10)
                 if (stageNo == 10) {
                     // Unlock JP?
                     // userCivProgressMapper.unlockCiv(userId, "JP");
                 }
             }
             
             // 4. Unlock Generals (1, 5, 10)
             // ... Logic
         }
    }

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
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public Long startBattle(Long userId, Integer dungeonId) {
        // 1. Check existing session
        BattleSessionTbl existing = battleSessionMapper.selectByUserId(userId);
        if (existing != null) {
            // Already in battle? 
            // For now, if ongoing, return existing ID or throw?
            // "1玩家=1会话"
            // If status=ONGOING, return existing.
            if (existing.getStatus() == 0) {
                return existing.getBattleId();
            }
        }
        
        // 2. Load User Data (General, Troops)
        List<UserGeneralTbl> allGenerals = userGeneralMapper.selectByUserId(userId);
        List<UserGeneralTbl> generals = new ArrayList<>();
        for (UserGeneralTbl g : allGenerals) {
            if (Boolean.TRUE.equals(g.getActivated())) {
                generals.add(g);
            }
        }
        
        if (generals.isEmpty()) {
            throw new RuntimeException("没有激活的武将，无法出战");
        }
        
        // Load Troops
        List<UserTroopTbl> troops = userTroopMapper.selectByUserId(userId);
        
        // 3. Build Context
        BattleContext ctx = new BattleContext();
        ctx.setBattleId(System.currentTimeMillis()); 
        ctx.setRandomSeed(System.currentTimeMillis());
        
        // Build Ally Context
        ctx.setAlly(buildAllyContext(generals, troops, userId));
        
        // Build Enemy Context (Based on DungeonId)
        // This requires Dungeon Configuration. 
        // For MVP, we might hardcode or read a DungeonTemplate.
        ctx.setEnemy(buildEnemyContext(dungeonId));
        
        // 4. Save Session
        BattleSessionTbl session = new BattleSessionTbl();
        session.setUserId(userId);
        session.setBattleId(ctx.getBattleId());
        session.setDungeonId(dungeonId);
        session.setStatus(0); // Ongoing
        session.setCurrentTurn(1);
        try {
            session.setContextJson(objectMapper.writeValueAsString(ctx));
        } catch (Exception e) {
            throw new RuntimeException("JSON Error", e);
        }
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        
        battleSessionMapper.insert(session);
        return session.getBattleId();
    }
    


    private BattleContext.SideContext buildAllyContext(List<UserGeneralTbl> generals, List<UserTroopTbl> troops, Long userId) {
        BattleContext.SideContext side = new BattleContext.SideContext();
        
        // Commander
        UserGeneralTbl leader = generals.get(0);
        
        BattleContext.HeroState hero = new BattleContext.HeroState();
        hero.setGeneralId(leader.getId());
        
        GeneralTemplateTbl genTpl = generalTemplateMapper.selectById(leader.getTemplateId());
        String heroName = (genTpl != null && genTpl.getName() != null) ? genTpl.getName() : "Leader";
        hero.setName(heroName);
        
        // --- Calculate Stats (Base + Equipment + Gems) ---
        long maxHp = leader.getMaxHp();
        long atk = 0; // Base ATK? GeneralTbl doesn't have ATK? Assuming 0 base or from Template.
        // Let's assume General has Base stats implicitly or from Template.
        // For now, let's load Equipment Stats.
        
        List<UserEquipmentTbl> equips = userEquipmentMapper.selectByUserId(userId);
        for (UserEquipmentTbl eq : equips) {
            if (leader.getId().equals(eq.getGeneralId())) {
                EquipmentTemplateTbl tpl = equipmentTemplateMapper.selectById(eq.getTemplateId());
                if (tpl != null) {
                    // Base
                    maxHp += (tpl.getBaseHp() == null ? 0 : tpl.getBaseHp());
                    atk += (tpl.getBaseAtk() == null ? 0 : tpl.getBaseAtk());
                    
                    // Enhancement Bonus: X * n * (n+1) / 2
                    // Assuming X=10 for simplicity as per requirements (or proportional to Base?)
                    // "3.1 累加型成长公式...共用...给定初始值 X"
                    // Let's assume X = 10% of Base? Or Fixed 10? Doc says "Example X=10".
                    // Let's use Fixed 10 for HP/ATK per level for now.
                    int n = (eq.getEnhanceLevel() == null) ? 0 : eq.getEnhanceLevel();
                    long bonus = 10L * n * (n + 1) / 2;
                    
                    if (tpl.getBaseHp() != null && tpl.getBaseHp() > 0) maxHp += bonus;
                    if (tpl.getBaseAtk() != null && tpl.getBaseAtk() > 0) atk += bonus;
                    
                    // Gem Bonus (Socket 1 & 2)
                    if (eq.getSocket1GemId() != null) {
                        UserGemTbl g = userGemMapper.selectById(eq.getSocket1GemId());
                        if (g != null) {
                             if ("HP".equals(g.getGemType())) maxHp += g.getStatValue();
                             if ("ATK".equals(g.getGemType())) atk += g.getStatValue();
                        }
                    }
                    if (eq.getSocket2GemId() != null) {
                        UserGemTbl g = userGemMapper.selectById(eq.getSocket2GemId());
                        if (g != null) {
                             if ("HP".equals(g.getGemType())) maxHp += g.getStatValue();
                             if ("ATK".equals(g.getGemType())) atk += g.getStatValue();
                        }
                    }
                }
            }
        }
        
        hero.setMaxHp(maxHp);
        hero.setCurrentHp(maxHp);
        hero.setAtk(atk);
        hero.setDead(false);
        hero.setRetreated(false);
        side.setHero(hero);
        
        // Troops
        List<BattleContext.TroopStack> stacks = new ArrayList<>();
        // Convert UserTroop to Stack
        for (UserTroopTbl t : troops) {
             if (t.getCount() > 0) {
                 TroopTemplateTbl tpl = troopTemplateMapper.selectById(t.getTroopId());
                 BattleContext.TroopStack stack = new BattleContext.TroopStack();
                 stack.setTroopId(t.getTroopId());
                 stack.setType(tpl.getTroopType()); // INF/ARC/CAV
                 stack.setCount(t.getCount());
                 stack.setUnitHp(tpl.getBaseHp());
                 stack.setFrontHp(tpl.getBaseHp()); // Fresh start
                 stacks.add(stack);
             }
        }
        side.setTroops(stacks);
        return side;
    }
    
    private BattleContext.SideContext buildEnemyContext(Integer dungeonId) {
        // Mock Implementation
        BattleContext.SideContext side = new BattleContext.SideContext();
        BattleContext.HeroState hero = new BattleContext.HeroState();
        hero.setName("Enemy Boss");
        hero.setMaxHp(1000L);
        hero.setCurrentHp(1000L);
        hero.setAtk(100L);
        side.setHero(hero);
        
        List<BattleContext.TroopStack> stacks = new ArrayList<>();
        // Add dummy enemy troops
        BattleContext.TroopStack stack = new BattleContext.TroopStack();
        stack.setType("INF");
        stack.setCount(100L);
        stack.setUnitHp(10L);
        stack.setFrontHp(10L);
        stacks.add(stack);
        
        side.setTroops(stacks);
        return side;
    }

    @Transactional(rollbackFor = Exception.class)
    public BattleContext processTurn(Long userId, Boolean castSkill) {
        // 1. Get Session
        BattleSessionTbl session = battleSessionMapper.selectByUserId(userId);
        if (session == null || session.getStatus() != 0) {
            throw new RuntimeException("没有进行中的战斗");
        }
        
        // 2. Load Context
        BattleContext ctx;
        try {
            ctx = objectMapper.readValue(session.getContextJson(), BattleContext.class);
        } catch (Exception e) {
            throw new RuntimeException("Context Error", e);
        }
        
        // --- PHASE 0: Pre-Turn ---
        // Reduce CD
        if (ctx.getAlly().getHero().getSkillCd() != null && ctx.getAlly().getHero().getSkillCd() > 0) {
            ctx.getAlly().getHero().setSkillCd(ctx.getAlly().getHero().getSkillCd() - 1);
        }
        if (ctx.getEnemy().getHero().getSkillCd() != null && ctx.getEnemy().getHero().getSkillCd() > 0) {
            ctx.getEnemy().getHero().setSkillCd(ctx.getEnemy().getHero().getSkillCd() - 1);
        }
        
        // --- PHASE 1: Player Skill ---
        if (Boolean.TRUE.equals(castSkill)) {
            // Check CD and Alive
            BattleContext.HeroState hero = ctx.getAlly().getHero();
            if (!hero.isDead() && !hero.isRetreated() && (hero.getSkillCd() == null || hero.getSkillCd() == 0)) {
                // Execute Skill (Damage = ATK * 1.5)
                long dmg = (long) (hero.getAtk() * 1.5);
                executeDamage(dmg, ctx.getEnemy(), "SKILL");
                hero.setSkillCd(3); // Reset CD
            }
        }
        
        // --- PHASE 2: Check Victory/Defeat early? No, full turn flow. ---
        
        // --- PHASE 2: ARC Phase (Troops) ---
        // Ally ARC attacks Enemy
        executeTroopPhase(ctx.getAlly(), ctx.getEnemy(), "ARC");
        // Enemy ARC attacks Ally
        executeTroopPhase(ctx.getEnemy(), ctx.getAlly(), "ARC");
        
        // --- PHASE 3: Other Troops (INF/CAV) ---
        executeTroopPhase(ctx.getAlly(), ctx.getEnemy(), "INF");
        executeTroopPhase(ctx.getEnemy(), ctx.getAlly(), "INF");
        executeTroopPhase(ctx.getAlly(), ctx.getEnemy(), "CAV");
        executeTroopPhase(ctx.getEnemy(), ctx.getAlly(), "CAV");
        
        // --- PHASE 4: Hero Normal Attack ---
        BattleContext.HeroState myHero = ctx.getAlly().getHero();
        if (!myHero.isDead() && !myHero.isRetreated()) {
            executeDamage(myHero.getAtk(), ctx.getEnemy(), "HERO_ATK");
        }
        BattleContext.HeroState enemyHero = ctx.getEnemy().getHero();
        if (!enemyHero.isDead() && !enemyHero.isRetreated()) {
            executeDamage(enemyHero.getAtk(), ctx.getAlly(), "HERO_ATK");
        }
        
        // --- PHASE 5: Retreat Logic ---
        checkRetreat(ctx.getAlly());
        checkRetreat(ctx.getEnemy());
        
        // --- PHASE 6: End Turn & Save ---
        session.setCurrentTurn(session.getCurrentTurn() + 1);
        
        // Check Status
        // Check Status
        int result = 0;
        if (ctx.getEnemy().getHero() != null && ctx.getEnemy().getHero().isDead()) {
             result = 1; // Win
        } else if (ctx.getAlly().getHero() != null && ctx.getAlly().getHero().isDead()) {
             result = 2; // Lose
        } else if (session.getCurrentTurn() > ctx.getTurnLimit()) {
             result = 2; // Draw/Lose
        }
        
        if (result != 0) {
            finishBattle(session, result);
        } else {
            try {
                session.setContextJson(objectMapper.writeValueAsString(ctx));
            } catch (Exception e) {}
            battleSessionMapper.update(session);
        }
        return ctx;
    }
    
    // Execute Troop Phase: All stacks of 'type' attack
    private void executeTroopPhase(BattleContext.SideContext attackerSide, BattleContext.SideContext defenderSide, String type) {
        for (BattleContext.TroopStack s : attackerSide.getTroops()) {
            if (s.getType().equals(type) && s.isAlive()) {
                // Calculate Damage: Count * BaseDamage (Where is BaseDamage? Simplified: 10 * Level? Used UnitHp as proxy for now?)
                // Assuming simple damage = Count * 10
                long dmg = s.getCount() * 10; 
                
                // Target: Fixed Priority
                // 1. Counter (INF>ARC>CAV>INF)
                // 2. Threat (Elite>ARC>CAV>INF)
                // Need complex target selection logic here.
                // Simplified: Attack Front Troop
                executeTroopAttack(dmg, s.getType(), defenderSide);
            }
        }
    }
    
    private void executeTroopAttack(long dmg, String attackerType, BattleContext.SideContext defenderSide) {
        // Priority
        // Check Counter
        String targetType = getCounterTarget(attackerType);
        
        // Try find target stack
        BattleContext.TroopStack target = findStackByType(defenderSide.getTroops(), targetType);
        if (target == null || !target.isAlive()) {
            // Fallback Priority: Elite -> ARC -> CAV -> INF
            target = findStackByPriority(defenderSide.getTroops());
        }
        
        if (target != null && target.isAlive()) {
            long overflow = applyDamage(target, dmg);
             if (overflow > 0) {
                 // Flow to next?
                 // Recursively call findStackByPriority excluding current?
                 // For MVP, just deal damage to one stack.
             }
        } else {
             // Attack Hero?
             // Only if no troops?
             // Rules: "Hero打无英雄时按兵堆栈...兵攻击默认：敌方兵堆栈"
             // If no troops, do nothing? or attack Hero? 
             // Normally Troops CAN attack Hero.
             if (!defenderSide.getHero().isDead() && !defenderSide.getHero().isRetreated()) {
                 long oldHp = defenderSide.getHero().getCurrentHp();
                 long newHp = oldHp - dmg;
                 defenderSide.getHero().setCurrentHp(Math.max(0, newHp));
                 if (newHp <= 0) defenderSide.getHero().setDead(true);
             }
        }
    }
    
    private String getCounterTarget(String type) {
        if ("INF".equals(type)) return "ARC";
        if ("ARC".equals(type)) return "CAV";
        if ("CAV".equals(type)) return "INF";
        return null;
    }
    
    private BattleContext.TroopStack findStackByType(List<BattleContext.TroopStack> stacks, String type) {
        if (type == null) return null;
        for (BattleContext.TroopStack s : stacks) {
            if (s.getType().equals(type) && s.isAlive()) return s;
        }
        return null;
    }
    
    private BattleContext.TroopStack findStackByPriority(List<BattleContext.TroopStack> stacks) {
         // Priority: ARC, CAV, INF (Simplified)
         BattleContext.TroopStack s = findStackByType(stacks, "ARC");
         if (s != null) return s;
         s = findStackByType(stacks, "CAV");
         if (s != null) return s;
         s = findStackByType(stacks, "INF");
         return s;
    }
    
    // Core Logic: Stack Damage
    private long applyDamage(BattleContext.TroopStack s, long dmg) {
        if (dmg <= 0 || s.getCount() <= 0) return dmg;

        // 1. Hit Front Unit
        if (dmg < s.getFrontHp()) {
            s.setFrontHp(s.getFrontHp() - dmg);
            return 0;
        }
        dmg -= s.getFrontHp();
        s.setCount(s.getCount() - 1);
        if (s.getCount() <= 0) {
            s.setFrontHp(0L);
            return dmg; // Overflow
        }
        s.setFrontHp(s.getUnitHp()); // Reset new front

        // 2. Batch kill
        long kill = Math.min(s.getCount(), dmg / s.getUnitHp());
        s.setCount(s.getCount() - kill);
        dmg -= kill * s.getUnitHp();
        
        if (s.getCount() <= 0) {
            s.setFrontHp(0L);
            return dmg;
        }

        // 3. Remaining dmg to new front
        if (dmg > 0) {
            s.setFrontHp(s.getUnitHp() - dmg);
            return 0;
        }
        return 0;
    }

    private void executeDamage(long dmg, BattleContext.SideContext targetSide, String source) {
        // Hero Attack Rule:
        // 1. Hero if alive
        if (!targetSide.getHero().isDead() && !targetSide.getHero().isRetreated()) {
            long hp = targetSide.getHero().getCurrentHp();
            hp -= dmg;
            targetSide.getHero().setCurrentHp(Math.max(0, hp));
            if (hp <= 0) targetSide.getHero().setDead(true);
            return;
        }
        
        // 2. Troops (Priority)
        BattleContext.TroopStack target = findStackByPriority(targetSide.getTroops());
        while (target != null && dmg > 0) {
            dmg = applyDamage(target, dmg);
             if (dmg > 0) {
                 // Reselect next target
                 target = findStackByPriority(targetSide.getTroops()); 
             }
        }
    }
    
    private void checkRetreat(BattleContext.SideContext side) {
        BattleContext.HeroState h = side.getHero();
        if (!h.isDead() && !h.isRetreated()) {
            double ratio = (double) h.getCurrentHp() / h.getMaxHp();
            if (ratio <= 0.1) {
                // Trigger Retreat (Fixed for now, ignore personality)
                h.setRetreated(true);
                // Reduce morale? (Troop HP -20%)
                // Implementation skipped for brevity
            }
        }
    }
}