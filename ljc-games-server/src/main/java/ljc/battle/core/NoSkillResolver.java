package ljc.battle.core;

import java.util.ArrayList;
import java.util.List;

import ljc.battle.core.SkillResolver;

/**
 * 默认无技能解析器：永远只普攻
 */
public class NoSkillResolver implements SkillResolver {

    @Override
    public SkillDecision decideSkill(BattleState state, BattleState.Side actorSide) {
        // 先只返回 NORMAL (普攻)
        return new SkillDecision(SkillDecision.Type.NORMAL);
    }

    @Override
    public SkillEffect resolve(BattleState state, SkillDecision decision) {
        // 技能逻辑
        // 因为总是普攻，这里不应该被调用，或者为空
        SkillEffect effect = new SkillEffect();
        effect.success = false;
        effect.logs = new ArrayList<>();
        return effect; 
    }
}
