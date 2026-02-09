package ljc.battle.core;

/**
 * 兵种堆栈（个体压缩表达）
 * 对应文档：兵为"个体堆栈（Stack）"模型
 */
public class TroopStack {
    public int troopId;       // 对应 troop_template.troop_id（用于战后结算）
    public String type;       // INF/ARC/CAV/ELITE_*
    public int count;         // 当前存活数量
    public int unitHp;        // 单个单位最大HP
    public int frontHp;       // 当前最前排单位剩余HP (1..unitHp)
    public int initialCount;  // 进入战斗时的数量（用于战后结算）

    public TroopStack() {}

    public TroopStack(String type, int count, int unitHp) {
        this.type = type;
        this.count = count;
        this.unitHp = unitHp;
        this.frontHp = unitHp;
        this.initialCount = count;
    }

    public TroopStack(int troopId, String type, int count, int unitHp) {
        this.troopId = troopId;
        this.type = type;
        this.count = count;
        this.unitHp = unitHp;
        this.frontHp = unitHp;
        this.initialCount = count;
    }

    public TroopStack copy() {
        TroopStack s = new TroopStack();
        s.troopId = this.troopId;
        s.type = this.type;
        s.count = this.count;
        s.unitHp = this.unitHp;
        s.frontHp = this.frontHp;
        s.initialCount = this.initialCount;
        return s;
    }
    
    public boolean isDead() {
        return count <= 0;
    }

    @Override
    public String toString() {
        return String.format("Stack[%s](id=%d, cnt=%d, fhp=%d/%d, init=%d)", type, troopId, count, frontHp, unitHp, initialCount);
    }
}
