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

        // 2. Validate General & Load
        if (generalId == null) throw new RuntimeException("Must select a general!");
        UserGeneralTbl general = userGeneralMapper.selectById(generalId);
        if (general == null || !general.getUserId().equals(userId)) {
            throw new RuntimeException("General not found");
        }
        if (!Boolean.TRUE.equals(general.getActivated())) {
            throw new RuntimeException("General not activated");
        }
        if (general.getRestTurns() != null && general.getRestTurns() > 0) {
            throw new RuntimeException("General is resting (" + general.getRestTurns() + " turns left)");
        }

        // 3. User & Progress Validation
        UserCivProgressTbl progress = userCivProgressMapper.selectByUserIdAndCiv(userId, civ);
        int currentMax = (progress == null) ? 0 : (progress.getMaxStageCleared() == null ? 0 : progress.getMaxStageCleared());
        if (stageNo > currentMax + 1) {
            throw new RuntimeException("Stage locked! Please clear previous stages first.");
        }

        // 4. Load Stage Config
        StoryStageConfigTbl stageConfig = storyStageConfigMapper.selectByCivAndStage(civ, stageNo);
        if (stageConfig == null) {
            throw new RuntimeException("Stage config not found: " + civ + "-" + stageNo);
        }

        // 5. Capacity Check & Prepare Troops
        int totalSpace = 0;
        List<BattleContext.TroopStack> battleStacks = new ArrayList<>();

        // This map tracks actual deducted amount from inventory (Config amount)
        // Battle amount might be lower due to Wall Cost

        // We need to load Troop Templates first to calculate capacity and type
        // Let's iterate troopConfig
        if (troopConfig != null) {
            for (Map.Entry<Integer, Integer> entry : troopConfig.entrySet()) {
                Integer count = entry.getValue();
                if (count <= 0) continue;

                TroopTemplateTbl tpl = troopTemplateMapper.selectById(entry.getKey());
                if (tpl == null) continue;

                totalSpace += count * (tpl.getCost() == null ? 1 : tpl.getCost());

                BattleContext.TroopStack stack = new BattleContext.TroopStack();
                stack.setTroopId(entry.getKey());
                stack.setType(tpl.getTroopType());
                stack.setCount(Long.valueOf(count));
                stack.setUnitHp(tpl.getBaseHp());
                stack.setFrontHp(tpl.getBaseHp());
                battleStacks.add(stack);
            }
        }

        if (totalSpace > (general.getCapacity() == null ? 0 : general.getCapacity())) {
            throw new RuntimeException("Exceeds General Capacity! Used: " + totalSpace + ", Max: " + general.getCapacity());
        }

        // 6. Wall Cost Deduction (Reduce Valid Battle Stacks)
        if ("WALL".equals(stageConfig.getStageType())) {
            int toDeduct = stageConfig.getWallCostTroops() == null ? 0 : stageConfig.getWallCostTroops();
            if (toDeduct > 0) {
                // Priority INF -> ARC -> CAV
                String[] priority = {"INF", "ARC", "CAV"};
                for (String type : priority) {
                    if (toDeduct <= 0) break;
                    // Find stacks of this type
                    for (BattleContext.TroopStack s : battleStacks) {
                        if (toDeduct <= 0) break;
                        if (s.getType().equals(type)) {
                            long count = s.getCount();
                            long deduct = Math.min(count, toDeduct);
                            s.setCount(count - deduct);
                            toDeduct -= deduct;
                        }
                    }
                }
                // Remove empty stacks
                List<BattleContext.TroopStack> cleanStacks = new ArrayList<>();
                for (BattleContext.TroopStack s : battleStacks) {
                    if (s.getCount() > 0) cleanStacks.add(s);
                }
                battleStacks = cleanStacks;

                if (battleStacks.isEmpty()) {
                    throw new RuntimeException("All troops died at the wall!");
                }
            }
        }



        // Troops (Deduct original config amount from Inventory)
        if (troopConfig != null) {
            for (Map.Entry<Integer, Integer> entry : troopConfig.entrySet()) {
                Integer count = entry.getValue();
                if (count > 0) {
                    int dRows = userTroopMapper.safeDeduct(userId, entry.getKey(), count);
                    if (dRows <= 0) {
                        throw new RuntimeException("Not enough troops for ID: " + entry.getKey());
                        // Note: Transaction will rollback.
                    }
                }
            }
        }

        // 8. Build Context
        BattleContext ctx = new BattleContext();
        ctx.setBattleId(System.currentTimeMillis());
        ctx.setRandomSeed(System.currentTimeMillis());

        // Ally
        BattleContext.SideContext allySide = new BattleContext.SideContext();
        allySide.setTroops(battleStacks);
        allySide.setHero(buildHeroState(userId, general));
        ctx.setAlly(allySide);

        // Enemy
        BattleContext.SideContext enemySide;
        try {
            enemySide = objectMapper.readValue(stageConfig.getEnemyConfigJson(), BattleContext.SideContext.class);
            // Apply Multiplier (HP/ATK * multiplier/1000)
            int mult = stageConfig.getEnemyMultiplier() == null ? 1000 : stageConfig.getEnemyMultiplier();
            if (mult != 1000) {
                if (enemySide.getHero() != null) {
                    enemySide.getHero().setMaxHp(enemySide.getHero().getMaxHp() * mult / 1000);
                    enemySide.getHero().setCurrentHp(enemySide.getHero().getMaxHp());
                    enemySide.getHero().setAtk(enemySide.getHero().getAtk() * mult / 1000);
                }
                if (enemySide.getTroops() != null) {
                    for (BattleContext.TroopStack s : enemySide.getTroops()) {
                        // s.getUnitHp() ? We don't have unitHp in JSON usually, we invoke template.
                        // But for now assume JSON has full snapshot or we assume standard stats.
                        // MVP: JSON contains snapshot.
                        s.setUnitHp(s.getUnitHp() * mult / 1000);
                        s.setFrontHp(s.getUnitHp());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Enemy Config JSON Error", e);
        }
        ctx.setEnemy(enemySide);

        // 9. Save Session
        BattleSessionTbl session = new BattleSessionTbl();
        session.setUserId(userId);
        session.setBattleId(ctx.getBattleId());
        session.setCiv(civ); // Set Civ
        session.setDungeonId(stageNo);
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
    private final PersonalityConfigMapper personalityConfigMapper; // Added
    private final ObjectMapper objectMapper;

    // Helper for Hero Stats
    private BattleContext.HeroState buildHeroState(Long userId, UserGeneralTbl general) {
        BattleContext.HeroState hero = new BattleContext.HeroState();
        hero.setGeneralId(general.getId());

        GeneralTemplateTbl genTpl = generalTemplateMapper.selectById(general.getTemplateId());
        hero.setName((genTpl != null) ? genTpl.getName() : "Hero");
        hero.setPersonality((genTpl != null) ? genTpl.getPersonalityCode() : "STOIC");

        long maxHp = general.getMaxHp();
        long atk = (genTpl != null) ? genTpl.getBaseAtk() : 0; // Simple base
        int speed = (genTpl != null) ? genTpl.getSpeed() : 50;

        List<UserEquipmentTbl> equips = userEquipmentMapper.selectByUserId(userId);
        for (UserEquipmentTbl eq : equips) {
            if (general.getId().equals(eq.getGeneralId())) {
                EquipmentTemplateTbl tpl = equipmentTemplateMapper.selectById(eq.getTemplateId());
                if (tpl != null) {
                    maxHp += (tpl.getBaseHp() == null ? 0 : tpl.getBaseHp());
                    atk += (tpl.getBaseAtk() == null ? 0 : tpl.getBaseAtk());
                    speed += (tpl.getBaseSpd() == null ? 0 : tpl.getBaseSpd());

                    int n = (eq.getEnhanceLevel() == null) ? 0 : eq.getEnhanceLevel();
                    long bonus = 10L * n * (n + 1) / 2;
                    if (tpl.getBaseHp() != null && tpl.getBaseHp() > 0) maxHp += bonus;
                    if (tpl.getBaseAtk() != null && tpl.getBaseAtk() > 0) atk += bonus;

                    // Gems
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
        hero.setSpeed(speed);
        hero.setDead(false);
        return hero;
    }

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

        ctx.getLogs().clear(); // Clear previous logs
        ctx.getLogs().add("=== Turn " + session.getCurrentTurn() + " ===");

        // --- PHASE 0: CD Reduction ---
        reduceCd(ctx.getAlly().getHero());
        reduceCd(ctx.getEnemy().getHero());

        int allySpeed = ctx.getAlly().getHero().getSpeed() == null ? 50 : ctx.getAlly().getHero().getSpeed();
        int enemySpeed = ctx.getEnemy().getHero().getSpeed() == null ? 50 : ctx.getEnemy().getHero().getSpeed();
        boolean allyFirst = allySpeed >= enemySpeed;

        // --- PHASE 1: Skill Phase ---
        if (Boolean.TRUE.equals(castSkill)) {
            // Player Casts Skill
            BattleContext.HeroState h = ctx.getAlly().getHero();
            if (canAct(h) && (h.getSkillCd() == null || h.getSkillCd() <= 0)) {
                ctx.getLogs().add("Wait, Player Skill Logic triggered in Turn?");
                // Requirement: "Skill release happens in Turn phase"
                // Execute Player Skill
                long dmg = (long) (h.getAtk() * 1.5); // Defines strict formula? "Attack * 1.5"
                ctx.getLogs().add(h.getName() + " casts Skill! Dmg: " + dmg);
                executeDamage(dmg, ctx.getEnemy(), "SKILL", ctx.getLogs());
                h.setSkillCd(3);
            }
        }
        // Enemy Skill (Simple AI: cast if ready)
        BattleContext.HeroState eh = ctx.getEnemy().getHero();
        if (canAct(eh) && (eh.getSkillCd() == null || eh.getSkillCd() <= 0)) {
            long dmg = (long) (eh.getAtk() * 1.5);
            ctx.getLogs().add(eh.getName() + " casts Skill! Dmg: " + dmg);
            executeDamage(dmg, ctx.getAlly(), "SKILL", ctx.getLogs());
            eh.setSkillCd(3);
        }

        // --- PHASE 2: ARC Phase ---
        if (allyFirst) {
            executeTroopPhase(ctx.getAlly(), ctx.getEnemy(), "ARC", ctx.getLogs());
            executeTroopPhase(ctx.getEnemy(), ctx.getAlly(), "ARC", ctx.getLogs());
        } else {
            executeTroopPhase(ctx.getEnemy(), ctx.getAlly(), "ARC", ctx.getLogs());
            executeTroopPhase(ctx.getAlly(), ctx.getEnemy(), "ARC", ctx.getLogs());
        }

        // --- PHASE 3: Other Troops ---
        String[] types = {"INF", "CAV"};
        for (String type : types) {
            if (allyFirst) {
                executeTroopPhase(ctx.getAlly(), ctx.getEnemy(), type, ctx.getLogs());
                executeTroopPhase(ctx.getEnemy(), ctx.getAlly(), type, ctx.getLogs());
            } else {
                executeTroopPhase(ctx.getEnemy(), ctx.getAlly(), type, ctx.getLogs());
                executeTroopPhase(ctx.getAlly(), ctx.getEnemy(), type, ctx.getLogs());
            }
        }

        // --- PHASE 4: Hero Attack ---
        if (allyFirst) {
            executeHeroAttack(ctx.getAlly(), ctx.getEnemy(), ctx.getLogs());
            executeHeroAttack(ctx.getEnemy(), ctx.getAlly(), ctx.getLogs());
        } else {
            executeHeroAttack(ctx.getEnemy(), ctx.getAlly(), ctx.getLogs());
            executeHeroAttack(ctx.getAlly(), ctx.getEnemy(), ctx.getLogs());
        }

        // --- PHASE 5: Retreat & End Turn ---
        checkRetreat(ctx.getAlly(), ctx.getLogs());
        checkRetreat(ctx.getEnemy(), ctx.getLogs());

        session.setCurrentTurn(session.getCurrentTurn() + 1);

        // Check Result
        int result = 0;
        if (isDefeated(ctx.getEnemy())) {
            result = 1; // Win
            ctx.getLogs().add("Enemy Defeated! Victory!");
        } else if (isDefeated(ctx.getAlly())) {
            result = 2; // Lose
            ctx.getLogs().add("Ally Defeated! Defeat!");
        } else if (session.getCurrentTurn() > ctx.getTurnLimit()) {
            result = 2; // Draw/Lose
            ctx.getLogs().add("Turn Limit Reached! Defeat!");
        }

        if (result != 0) {
            finishBattle(session, result);
        } else {
            try {
                session.setContextJson(objectMapper.writeValueAsString(ctx));
            } catch (Exception e) {
            }
            battleSessionMapper.update(session);
        }
        return ctx;
    }

    private void reduceCd(BattleContext.HeroState h) {
        if (h.getSkillCd() != null && h.getSkillCd() > 0) {
            h.setSkillCd(h.getSkillCd() - 1);
        }
    }

    private boolean canAct(BattleContext.HeroState h) {
        return !h.isDead() && !h.isRetreated();
    }

    private boolean isDefeated(BattleContext.SideContext side) {
        return side.getHero().isDead() || side.getHero().isRetreated();
    }

    private void executeTroopPhase(BattleContext.SideContext attacker, BattleContext.SideContext defender, String type, List<String> logs) {
        for (BattleContext.TroopStack s : attacker.getTroops()) {
            if (s.getType().equals(type) && s.isAlive()) {
                long dmg = s.getCount() * 10; // Simplified Dmg
                executeDamage(dmg, defender, type, logs); // Pass specific type
            }
        }
    }


    private void executeHeroAttack(BattleContext.SideContext attacker, BattleContext.SideContext defender, List<String> logs) {
        if (canAct(attacker.getHero())) {
            long dmg = attacker.getHero().getAtk();

            // Last Stand Bonus
            double hpRatio = (double)attacker.getHero().getCurrentHp() / attacker.getHero().getMaxHp();
            if (hpRatio <= 0.1) {
                // Check Personality
                PersonalityConfigTbl p = personalityConfigMapper.selectByCode(attacker.getHero().getPersonality());
                if (p != null && (p.getLastStandBias() == null ? 0 : p.getLastStandBias()) > 0) { // Brave/Berserker
                     dmg = (long)(dmg * 1.2);
                     logs.add(attacker.getHero().getName() + " enters Last Stand! (+20% Dmg)");
                }
            }

            logs.add(attacker.getHero().getName() + " attacks! Dmg: " + dmg);
            executeDamage(dmg, defender, "HERO_ATK", logs);
        }
    }


    private void executeDamage(long dmg, BattleContext.SideContext targetSide, String sourceType, List<String> logs) {
        // Target Logic
        // 1. If Source == HERO -> Priority: Hero (if alive)
        // 2. If Source == TROOP -> Priority: Counter > Elite > ARC > CAV > INF
        // But prompt says: "Hero attacks: priority=enemy troop stack... Default: troop stack"
        // Wait, "Hero attacks... priority = enemy troop stack?"
        // Re-read Requirement 3): "Skill... default target = Enemy Hero".
        // "Hero Normal Attack... priority = enemy troop stack" (implied by "Troop Attack default: enemy troop stack"?)
        // Let's stick to standard RPG: 
        // Skill -> Hero.
        // Normal Attack -> Troops first (Protection).

        if ("SKILL".equals(sourceType)) {
            // Attack Hero directly
            if (canAct(targetSide.getHero())) {
                dealDamageToHero(targetSide.getHero(), dmg, logs);
                return;
            }
        }


        // Find Troop Target
        BattleContext.TroopStack target = findTargetStack(targetSide.getTroops());

        // If no troops, attack Hero
        if (target == null) {
            if (canAct(targetSide.getHero())) {
                dealDamageToHero(targetSide.getHero(), dmg, logs);
            }
            return;
        }

        // Apply Counter Multiplier if Troop vs Troop
        // Types: INF, ARC, CAV
        // INF > ARC > CAV > INF
        double multiplier = 1.0;
        if (!"HERO_ATK".equals(sourceType) && !"SKILL".equals(sourceType)) {
             String tType = target.getType();
             if ("INF".equals(sourceType)) {
                 if ("ARC".equals(tType)) multiplier = 1.2;
                 else if ("CAV".equals(tType)) multiplier = 0.8;
             } else if ("ARC".equals(sourceType)) {
                 if ("CAV".equals(tType)) multiplier = 1.2;
                 else if ("INF".equals(tType)) multiplier = 0.8;
             } else if ("CAV".equals(sourceType)) {
                 if ("INF".equals(tType)) multiplier = 1.2;
                 else if ("ARC".equals(tType)) multiplier = 0.8;
             }
        }
        
        long finalDmg = (long)(dmg * multiplier);
        if (multiplier > 1.0) logs.add("Counter Attack! (x" + multiplier + ")");
        else if (multiplier < 1.0) logs.add("Weak Attack... (x" + multiplier + ")");

        // Apply Damage to Stack (with overflow)
        long remaining = finalDmg;

        while (remaining > 0 && target != null) {
            remaining = applyDamageToStack(target, remaining, logs);
            if (remaining > 0) {
                target = findTargetStack(targetSide.getTroops()); // Find NEXT target
            }
        }
    }

    private void dealDamageToHero(BattleContext.HeroState hero, long dmg, List<String> logs) {
        long old = hero.getCurrentHp();
        long now = Math.max(0, old - dmg);
        hero.setCurrentHp(now);
        logs.add(hero.getName() + " takes " + dmg + " dmg (" + old + "->" + now + ")");
        if (now <= 0) {
            hero.setDead(true);
            logs.add(hero.getName() + " is Defeated!");
        }
    }

    private BattleContext.TroopStack findTargetStack(List<BattleContext.TroopStack> stacks) {
        // Priority: INF > ARC > CAV (or whatever requirement says).
        // Let's use Front-to-Back: INF -> CAV -> ARC
        String[] prio = {"INF", "CAV", "ARC"};
        for (String p : prio) {
            for (BattleContext.TroopStack s : stacks) {
                if (s.getType().equals(p) && s.isAlive()) return s;
            }
        }
        return null;
    }

    private long applyDamageToStack(BattleContext.TroopStack s, long dmg, List<String> logs) {
        long remaining = dmg;
        long totalKill = 0;

        while (remaining > 0 && s.getCount() > 0) {
            if (remaining >= s.getFrontHp()) {
                remaining -= s.getFrontHp();
                s.setCount(s.getCount() - 1);
                s.setFrontHp(s.getUnitHp());
                totalKill++;
            } else {
                s.setFrontHp(s.getFrontHp() - remaining);
                remaining = 0;
            }
        }
        if (totalKill > 0) {
            logs.add("Killed " + totalKill + " " + s.getType());
        }
        if (s.getCount() <= 0) {
            s.setCount(0L);
            s.setFrontHp(0L);
        }
        return remaining;
    }

    private void checkRetreat(BattleContext.SideContext side, List<String> logs) {
        BattleContext.HeroState h = side.getHero();
        if (canAct(h)) {
            // Check Personality
            PersonalityConfigTbl p = personalityConfigMapper.selectByCode(h.getPersonality());
            double threshold = 0.1; // Default
            if (p != null && p.getLastStandBias() < 0) { // Coward
                threshold = 0.2;
            } else if (p != null && p.getLastStandBias() > 0) { // Brave
                threshold = 0.05;
            }

            if ((double) h.getCurrentHp() / h.getMaxHp() <= threshold) {
                h.setRetreated(true);
                logs.add(h.getName() + " Retreated!");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void finishBattle(BattleSessionTbl session, int result) {
        // 1. Update Session Status
        session.setStatus(result);
        session.setUpdatedAt(LocalDateTime.now());
        battleSessionMapper.update(session);

        // 2. Heal / Injury Logic (For General)
        // Need to load General
        // Assuming generalId available or we load from context
        // For MVP, if Result=Win, we process rewards.

        if (result == 1) { // VICTORY
            BattleContext ctx;
            try {
                ctx = objectMapper.readValue(session.getContextJson(), BattleContext.class);

                // Return Surviving Troops
                for (BattleContext.TroopStack s : ctx.getAlly().getTroops()) {
                    if (s.getCount() > 0) {
                        userTroopMapper.upsertAdd(session.getUserId(), s.getTroopId(), s.getCount());
                    }
                }

                // Update Progress 
                // Update Progress 
                String civ = session.getCiv(); 
                if (civ == null) civ = "CN"; 
                Integer stageNo = session.getDungeonId();
                UserCivProgressTbl progress = userCivProgressMapper.selectByUserIdAndCiv(session.getUserId(), civ);
                if (progress == null) {
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
                }

                // Handle General Injury
                BattleContext.HeroState h = ctx.getAlly().getHero();
                if (h != null) {
                    UserGeneralTbl g = userGeneralMapper.selectById(h.getGeneralId());
                    if (g != null) {
                        double ratio = (double) h.getCurrentHp() / h.getMaxHp();
                        if (ratio < 0.5) { // Injured
                            g.setRestTurns(3); // Rest 3 turns
                            userGeneralMapper.update(g); // Need update method
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // --- Unlock Logic ---
            try {
                // 1. Current Stage Info
                String civ = session.getCiv();
                if (civ == null) civ = "CN";
                Integer stageNo = session.getDungeonId();

                // 2. Unlock Next Stage
                UserCivProgressTbl progress = userCivProgressMapper.selectByUserIdAndCiv(session.getUserId(), civ);
                if (progress == null) {
                    progress = new UserCivProgressTbl();
                    progress.setUserId(session.getUserId());
                    progress.setCiv(civ);
                    progress.setMaxStageCleared(stageNo);
                    progress.setUnlocked(true);
                    userCivProgressMapper.insert(progress);
                } else {
                    if (stageNo >= progress.getMaxStageCleared()) { // >= because if I clear 5, max should be 5 (unlocks 6 check later)
                        // Actually logic is: if current is 5, max cleared becomes 5.
                        // canEnter uses maxStageCleared + 1.
                        if (stageNo > progress.getMaxStageCleared()) {
                             progress.setMaxStageCleared(stageNo);
                             userCivProgressMapper.update(progress);
                        }
                    }
                }

                // 3. Unlock Hero (Stage 1, 5, 10)
                Integer unlockHeroTplId = null;
                if ("CN".equals(civ)) {
                    if (stageNo == 1) unlockHeroTplId = 1002;
                    else if (stageNo == 5) unlockHeroTplId = 1003;
                    else if (stageNo == 10) unlockHeroTplId = 1004;
                }
                
                if (unlockHeroTplId != null) {
                    UserGeneralTbl existingHero = userGeneralMapper.selectByUserIdAndTemplateId(session.getUserId(), unlockHeroTplId);
                    if (existingHero == null) {
                        // Grant Hero
                        GeneralTemplateTbl tpl = generalTemplateMapper.selectById(unlockHeroTplId);
                        if (tpl != null) {
                            UserGeneralTbl newHero = new UserGeneralTbl();
                            newHero.setUserId(session.getUserId());
                            newHero.setTemplateId(unlockHeroTplId);
                            newHero.setUnlocked(true);
                            newHero.setActivated(true); // Auto activate reward? User "Unlock" usually means unlocked to recruit?
                            // Requirement says "Unlock Hero". Usually implies available.
                            // Let's set Activated=false (need to pay?) or true?
                            // "1/5/10 unlock heroes". Let's auto-activate for smooth flow in MVP.
                            newHero.setActivated(true);
                            newHero.setLevel(1);
                            newHero.setTier(0);
                            newHero.setMaxHp(tpl.getBaseHp());
                            newHero.setCurrentHp(tpl.getBaseHp());
                            newHero.setCapacity(tpl.getBaseCapacity());
                            newHero.setRestTurns(0);
                            userGeneralMapper.insert(newHero);
                        }
                    }
                }

                // 4. Unlock Next Country (Stage 10)
                if (stageNo == 10 && "CN".equals(civ)) {
                    String nextCiv = "JP"; // Simplified
                    UserCivProgressTbl nextProgress = userCivProgressMapper.selectByUserIdAndCiv(session.getUserId(), nextCiv);
                    if (nextProgress == null) {
                        nextProgress = new UserCivProgressTbl();
                        nextProgress.setUserId(session.getUserId());
                        nextProgress.setCiv(nextCiv);
                        nextProgress.setMaxStageCleared(0);
                        nextProgress.setUnlocked(true);
                        userCivProgressMapper.insert(nextProgress);
                    } else if (!nextProgress.getUnlocked()) {
                        nextProgress.setUnlocked(true);
                        userCivProgressMapper.update(nextProgress);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}