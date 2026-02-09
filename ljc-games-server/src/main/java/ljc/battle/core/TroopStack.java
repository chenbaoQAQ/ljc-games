package ljc.battle.core;

/**
 * 兵种堆栈（个体压缩表达）
 * 对应文档：兵为“个体堆栈（Stack）”模型
 */
public class TroopStack {
    // 兵种类型：INF/ARC/CAV/ELITE_* 
    // 文档没给具体枚举，这里用 String 或 int，建议 String 方便扩展
    public String type; 

    public int count;       // 当前存活数量
    public int unitHp;      // 单个单位最大HP
    public int frontHp;     // 当前最前排单位剩余HP (1..unitHp)
    
    // 为了方便 JSON 序列化和克隆
    public TroopStack() {}

    public TroopStack(String type, int count, int unitHp) {
        this.type = type;
        this.count = count;
        this.unitHp = unitHp;
        this.frontHp = unitHp; // 满血
    }
    
    /**
     * 深拷贝（为了状态快照）
     */
    public TroopStack copy() {
        TroopStack s = new TroopStack();
        s.type = this.type;
        s.count = this.count;
        s.unitHp = this.unitHp;
        s.frontHp = this.frontHp;
        return s;
    }
    
    public boolean isDead() {
        return count <= 0;
    }

    @Override
    public String toString() {
        return String.format("Stack[%s](cnt=%d, fhp=%d/%d)", type, count, frontHp, unitHp);
    }
}
