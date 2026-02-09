package ljc.battle.core;

import java.util.List;

/**
 * 技能决策与解析接口
 * 允许后续扩展不同英雄的技能逻辑
 */
public interface SkillResolver {
    
    // 决定是否释放技能与目标
    SkillDecision decideSkill(BattleState state, BattleState.Side actorSide);
    
    // 执行技能效果
    SkillEffect resolve(BattleState state, SkillDecision decision);

    class SkillDecision {
        public enum Type {
            NORMAL,   // 普攻
            SKILL_A,  // 技能A (Active)
            SKILL_P,  // 被动触发 (Passive)
            NONE      // 无行动
        }
        public Type type;
        public String skillId;
        public String targetId;
        public BattleState.Side actorSide; // V1 Add
        
        public SkillDecision(Type type) { this.type = type; }
        public SkillDecision(Type type, String skillId, BattleState.Side actorSide) { 
            this.type = type; this.skillId = skillId; this.actorSide = actorSide; 
        }
    }

    class SkillEffect {
        public int damage;    // 造成的伤害
        public int heal;      // 治疗量
        public List<String> logs; // 产生的特定日志
        public boolean success; // 是否成功释放
    }
}
