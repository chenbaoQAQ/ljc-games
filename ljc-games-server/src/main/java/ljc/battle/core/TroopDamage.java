package ljc.battle.core;

/**
 * 兵力伤害结算工具类
 */
public class TroopDamage {

    public static class DamageOutcome {
        public int killedCount;      // 击杀数
        public int overflowDamage;   // 溢出伤害
        
        public DamageOutcome(int k, int o) {
            this.killedCount = k;
            this.overflowDamage = o;
        }
    }

    /**
     * 对单个堆栈结算伤害 (In-place modification)
     */
    public static DamageOutcome applyDamage(TroopStack stack, int dmg) {
        if (stack == null || stack.count <= 0 || dmg <= 0) {
            return new DamageOutcome(0, dmg);
        }

        int originalCount = stack.count;
        int remaining = dmg;

        // 1. Hit front unit
        if (remaining < stack.frontHp) {
            stack.frontHp -= remaining;
            return new DamageOutcome(0, 0); // No kills, no overflow
        }
        
        // Kill front unit
        remaining -= stack.frontHp;
        stack.count--;
        
        if (stack.count <= 0) {
            stack.frontHp = 0;
            return new DamageOutcome(originalCount, remaining); // All dead, return overflow
        }

        // 2. Batch kill full units
        int unitHp = stack.unitHp;
        int killFull = Math.min(stack.count, remaining / unitHp);
        
        stack.count -= killFull;
        remaining -= killFull * unitHp;
        
        if (stack.count <= 0) {
            stack.frontHp = 0;
            return new DamageOutcome(originalCount, remaining); // All dead
        }

        // 3. New front takes remaining damage
        stack.frontHp = unitHp; // Reset new front to full first
        if (remaining > 0) {
            stack.frontHp -= remaining;
            // remaining consumed
        }

        return new DamageOutcome(originalCount - stack.count, 0);
    }
}
