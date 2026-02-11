package ljc.battle.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 战斗日志事件
 * 对应文档：BattleLogEvent，标准化事件输出
 */
public class BattleLogEvent { /* renamed from BattleLogEntry to avoid conflict */
    public enum Phase {
        HERO_SOLO,
        TROOP_WAR
    }

    public enum Type {
        TURN_START,
        PHASE_CHANGE,
        
        HERO_ATTACK,
        HERO_SKILL,
        HERO_HP_CHANGE,
        HERO_RETREAT,
        HERO_DEAD,
        
        TROOP_ATTACK,       // 包含分流信息
        TROOP_STACK_CHANGE, // 兵堆栈变化
        
        BATTLE_END
    }

    public Type type;
    public Phase phase; // 当前阶段
    public String actorSide; // "my", "enemy" (or "A", "B")
    public String side;      // 受击方/变化方
    
    // Actor/Target Info
    public String actorId;   // HeroA, INF_A... (Legacy compatible)
    public String targetId;  
    
    // Values
    public int turn;
    public int value;        // Generic value (damage, heal)
    public String desc;
    public String fromPhase;
    public String toPhase;
    public boolean myHeroCanFight;
    public boolean enemyHeroCanFight;
    
    // V3.0 Specifics
    public int rollToHero;     // 0-100
    public int damageTotal;
    public int damageToHero;
    public int damageToTroops;
    public String attackerTroopType;
    public String note;
    
    // Stack Change Info
    public String troopType;
    public int killed;
    public int countBefore;
    public int countAfter;
    
    // Skill Info
    public String skillId;

    public BattleLogEvent() {}

    // Legacy Constructor Adapter
    public BattleLogEvent(Type type, String actorId, String targetId, int value, String desc) {
        this.type = type;
        this.actorId = actorId;
        this.targetId = targetId;
        this.value = value;
        this.desc = desc;
    }
}
