# 战斗系统重构方案 (基于 V2.8 文档与最新交互需求)

## 1. 核心差异修正

根据 `LJC_后端最终对齐版需求说明_V2_8.md` 与用户最新口述需求，我们需要对战斗系统进行以下修正：

### A. 兵力模型 (Strict Stack Model)
**现状**：已有 `count`, `unitHp`, `frontHp`，但伤害结算逻辑可能未严格遵循“溢出流向下一个堆栈”的规则。
**修正**：
1.  **严格伤害堆栈结算**：
    - 先扣 `frontHp`，致死则 `count--`，`frontHp` 重置。
    - 剩余伤害整除 `unitHp` 批量扣除 `count`。
    - 剩余残伤扣除新 `frontHp`。
    - **关键**：若 `count` 归零，剩余伤害**必须**返回给调用者，以便流向下一个目标。
2.  **溢出流向**：
    - 定义明确的目标优先级链（Priority Chain）。
    - 当一个目标被打空，自动获取链上的下一个目标继续结算剩余伤害。

### B. 攻击目标优先级 (Strict Targeting)
**现状**：简单的克制优先，无将时随机或简单顺序。
**修正**：
1.  **Hero 攻击**：
    - 有敌将 -> 必打敌将。
    - 无敌将 -> 打兵，优先级：**特种(Special) -> 弓(ARC) -> 骑(CAV) -> 步(INF)**。
2.  **Troop 攻击**：
    - 优先打克制 (INF->ARC->CAV->INF)。
    - 克制目标不存在 -> 打兵，优先级同上 (Special -> ARC -> CAV -> INF)。
    - *用户新需求*：如果选择了“战术(Tactics)”，则战术目标优先级最高。

### C. 战斗流程 (Interactive Phasing)
**现状**：Act-by-Act (Hero -> Turn wait -> Troop -> Turn wait...)，基于速度排序。
**修正**（融合 V2.8 规则与手动操作需求）：
1.  **回合结构**：
    - **Phase 1: 准备/指令阶段**（前端交互核心）
        - 玩家设置本回合**全军战术**（攻步/攻弓...）。
        - 玩家选择**主公行动**（普攻 / 蓄力技能 / 如果已蓄力则提示释放）。
    - **Phase 2: 结算执行**（后端逻辑核心）
        - 为了满足 V2.8 的 "ARC 先手" 规则，行动队列应调整为：
            - **Group A**: 双方 ARC (速度快/射程远)。
            - **Group B**: 双方 INF/CAV (近战)。
            - **Group C**: 双方 Hero (穿插或最后? V2.8 未明确，建议穿插)。
    - **交互体验**：
        - 保持 Act-by-Act 的**请求模式**，让用户感觉到每个单位都在动。
        - 但**行动顺序**严格按照 V2.8：每一轮 (Round) 内，先让所有 ARC 动，再让其他动。

## 2. 具体代码修改点

### 2.1 `TroopStack` & `DamageLogic`
- 重写 `applyDamage`，确保返回 `long overflowDamage`。
- 实现 `applyDamageChain(List<Stack> targets, long totalDamage)`，自动处理溢出。

### 2.2 `BattleEngine` 目标选择接口
- 实现 `getHeroTarget(Side enemySide)` -> 返回 Hero 或 兵堆栈链。
- 实现 `getTroopTarget(Side enemySide, String attackerType, String tactics)` -> 返回 目标堆栈链。
    - 逻辑：Tactics Target -> Counter Target -> Default Priority Chain (Special->ARC->CAV->INF)。

### 2.3 `BattleEngine` 行动顺序 (`determineActionQueue`)
- 重写排序逻辑：
    - 优先级 1: ARC (Side A & B)
    - 优先级 2: Others (INF, CAV) & Hero? 
    - *注*：用户文档 V2.8 P216 明确：**弓兵 ARC phase：先出手**。
    - 所以 ActionQueue 应该是：`[ARC_A, ARC_B]` -> `[Hero_A, Hero_B, INF_A, CAV_A...]`。

### 2.4 技能逻辑
- 主动技能本质是**伤害**。
- 目标规则严格遵循 V2.8：有将打将，无将打兵（按优先级）。

## 3. 待确认的用户需求 (Q&A)
- **Q**: 用户提到的“武将回合小兵不参与，小兵回合武将参战”？
- **A**: 这属于额外机制。
    - "武将回合小兵不参与" -> 现在的逻辑就是这样（Hero Act 时主要是 Hero 打）。
    - "小兵回合武将参战" -> 已实现为 `ASSIST_ATK`（统帅追击）。保留此特性。

## 4. 下一步行动
1. 修正 `TroopDamage.java` (溢出算法)。
2. 修正 `BattleEngine.java` 的 `executeAction` (目标选择与溢出流转)。
3. 修正 `BattleEngine.java` 的 `determineActionQueue` (ARC 先手)。
