package ljc.battle.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 战斗状态（可序列化）
 * 文档：BattleState - 
 * 必须包含：battleId, civ, stageNo, turnNo, rngSeed, sideA, sideB, ...
 */
public class BattleState {
    public String battleId;
    public int civId;
    public int stageNo;
    public int turnNo;      // 当前回合数 (1, 2, ...)
    public int currentActorIndex; // 当前行动者索引 (0=HeroA, 1=HeroB, 2=InfA, ...)
    public long rngSeed;    // 随机种子
    public int actionNo;    // 行动计数，用于回放和随机数生成

    public Map<String, List<StatusEffect>> statusesA; // Key: "Hero", "INF", etc.
    public Map<String, List<StatusEffect>> statusesB;
    
    public BattleState() {
        statusesA = new java.util.HashMap<>();
        statusesB = new java.util.HashMap<>();
    }

    public Side sideA;      // 玩家方
    public Side sideB;      // 敌方

    public String lastActionLog; // 调试用
    public boolean isFinished;
    public boolean isWin;       // true=玩家胜
    public String nextActorDesc; // 下一个行动者描述 (HeroA, INF_A, etc)

    // ========== 内部类定义 ==========

    public static class Side {
        public String userId;    // 玩家ID or NPC
        public Hero hero;        // 英雄信息
        public List<TroopStack> troops; // 兵种堆栈列表: INF, ARC, CAV (必须按顺序/类型)

        public Side() {
            troops = new ArrayList<>();
        }
    }

    public static class Hero {
        public String name;
        public int maxHp;
        public int hp;
        public int atk;
        public int def;
        public int speed;
        public boolean isDeadOrRetreated; // 撤退/死亡标记
        
        // Metadata
        public String gender; // M, F, U
        public List<String> passives;
        public List<String> actives;

        // 技能CD相关
        public int skillCd; // 剩余CD
        public int maxSkillCd; // 技能最大CD
        
        // 延迟技能机制 (V2)
        public String castingSkillId; // 正在吟唱/准备的技能
        public int castingSkillTurns; // 剩余吟唱回合
        public String tactics;        // 当前战术指令 (TARGET_INF, ETC)

        public Hero() {
            passives = new ArrayList<>();
            actives = new ArrayList<>();
        }
        
        public Hero(String name, int hp, int atk, int def, int speed) {
            this();
            this.name = name;
            this.maxHp = hp;
            this.hp = hp;
            this.atk = atk;
            this.def = def;
            this.speed = speed;
            this.skillCd = 0;
            this.maxSkillCd = 3; // 默认3
            this.gender = "M"; // Default
        }

        public boolean isAlive() {
            return !isDeadOrRetreated && hp > 0;
        }
    }
}
