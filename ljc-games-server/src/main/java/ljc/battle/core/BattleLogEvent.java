package ljc.battle.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 战斗日志事件
 * 对应文档：BattleLogEvent，标准化事件输出
 */
public class BattleLogEvent { /* renamed from BattleLogEntry to avoid conflict */
    public enum Type {
        TURN_START,
        ACTOR_CHOSEN,        // 决定谁动
        ATTACK,              // 普攻/技能普攻
        KILL,                // 击杀
        HERO_DOWN,           // 英雄撤退/死亡
        TROOP_STACK_EMPTY,   // 兵种打光
        TURN_END, 
        BATTLE_END,
        HERO_PERSONALITY_TRIGGERED, // 占位
        STUN_SKIP,
        AMBUSH_VOLLEY,
        POISON_TICK,
        HEAL,
        STATUS_APPLIED,
        STATUS_EXPIRED,
        REFLECT_DAMAGE,
        SKILL,        // 技能爆发
        SKILL_CAST,   // 技能蓄力
        ASSIST_ATK    // 统帅追击
    }

    public Type type;
    public String actorId;     // 行动者 ID (HeroA/B or UnitSideType)
    public String targetId;    // 目标 ID
    public int value;          // 伤害值/击杀数
    public String desc;        // 其他信息（如 overflow）

    public BattleLogEvent(Type type, String actorId, String targetId, int value, String desc) {
        this.type = type;
        this.actorId = actorId;
        this.targetId = targetId;
        this.value = value;
        this.desc = desc;
    }
}
