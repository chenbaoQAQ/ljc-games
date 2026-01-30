package ljc.dto;

import lombok.Data;

@Data
public class TroopGroup {
    private String type;    // 兵种类型：INFANTRY, ARCHER, CAVALRY
    private int count;      // 当前存活数量
    private int maxCount;   // 初始数量 (用于计算伤兵)

    private int atk;        // 单体攻击力 (已包含科技/装备加成)
    private int hp;         // 单体血量

    // 构造函数：初始化时把属性填好
    public TroopGroup(String type, int count, int atk, int hp) {
        this.type = type;
        this.count = count;
        this.maxCount = count;
        this.atk = atk;
        this.hp = hp;
    }

    // 核心逻辑：承受伤害 (返回实际死亡数)
    public int takeDamage(int totalDamage) {
        if (count <= 0) return 0;

        // 总血池 = 数量 * 单兵血量
        long totalHp = (long) count * hp;
        long remainingHp = Math.max(0, totalHp - totalDamage);

        // 向上取整计算剩余人数
        int newCount = (int) Math.ceil((double) remainingHp / hp);
        int dead = count - newCount;

        this.count = newCount;
        return dead;
    }

    public boolean isAlive() {
        return count > 0;
    }
}