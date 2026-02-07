# 进度对齐：V2.8 完整实现 (Hall + Battle MVP)

> **当前状态**：大厅系统 (V2.3) 与 战斗核心逻辑 (V2.8) 均已完成开发与验证。
> **下一步**：前端对接、数值调优、爬塔模式。

## 1. 已完成 (Done)

### 1.1 基础架构 & 数据库
- [x] **数据库 Schema**: 完整即实现用户(Users)、资产(Troops/Items)、战斗(Sessions)等 15+ 张表。
    - **重置脚本**: `data.sql` 已优化，支持一键清空重置，方便反复测试。
- [x] **Entity/Mapper**: 全原子操作，严禁 Entity 混用，MyBatis XML 映射完整。
- [x] **Auth**: 注册/登录流程打通，支持新号自动初始资源（CN文明/义勇兵）。

### 1.2 大厅系统 (Hall Service)
- [x] **武将管理**:
    - **激活/升级/升阶**: 逻辑闭环，消耗金币，校验前置条件。
    - **属性计算**: 动态计算 `Base + Growth * Level`。
- [x] **装备 & 宝石**:
    - **穿戴**: 自动识别槽位，关联 `UserEquipment` -> `General`。
    - **强化**: 严格遵循 `V2.8` 规则（+3后失败降级）。
    - **镶嵌/合成**: 宝石 5合1 逻辑完成，镶嵌属性动态叠加至武将。
- [x] **兵营**:
    - **招募**: 原子化扣金币加兵，防止并发负数。
- [x] **技能**:
    - **学习**: 技能书覆盖逻辑，记录学习日志。

### 1.3 战斗系统 (Battle Service - V2.8 Core)
**这是本次迭代的重点，完全遵循 `V2.8` 复杂战斗规则。**

- [x] **启动战斗 (Start Story)**:
    - **前置校验**: 验证体力、关卡解锁、武将状态（休整/激活）。
    - **统帅校验**: 严格检查 `Troop Cost * Count <= General Capacity`。
    - **城墙机制**: 实现 `WALL` 类型关卡（如第5/9关），战前自动扣除部分兵力（优先 INF->ARC）。
    - **属性构建**: 完整加载 `Hero Stats` (含装备/宝石/等级) + `Enemy Stats` (含倍率放大)。

- [x] **回合逻辑 (Process Turn)**:
    - **速度机制**: 动态判断先手（基于 `Hero Speed`），不再是固定死顺序。
    - **兵力堆栈 (Stack Model)**: 
        - 摒弃了“总血条”，采用 `Count + UnitHp + FrontHp` 模型。
        - 伤害先结算“死几个兵”，人数减少直接影响下一轮输出。
        - **伤害溢出**: 打死当前堆栈后，剩余伤害自动流向下一个优先级堆栈（严格溢出算法）。
    - **目标优先级**: 
        - **Hero**: 优先打 Hero (若存在)。
        - **Troop**: 按 `Counter(克制) > Elite > ARC > CAV > INF` 固定顺序索敌。
    - **撤退/死战**: 
        - 引入 `Personality` (性格) 配置。
        - 当 HP <= 10% (或性格阈值) 时，自动触发撤退，停止行动。
    - **日志系统**: 回包包含详细 `logs` 数组，前端可直接逐条播放战报。

- [x] **结算逻辑 (Finish)**:
    - **伤兵返还**: 胜利后自动返还存活兵力。
    - **进度解锁**: 只有胜利才推进 `max_stage`。
    - **武将伤病**: 战损过高触发 `Rest Turns`（休整）。

## 2. 待办事项 (Pending / Next Steps)

### 2.1 玩法扩展
- [ ] **爬塔模式 (Tower)**: 复用 BattleService，但需新增 `TowerFloorConfig` 读取与掉落池逻辑。
- [ ] **特种兵**: 目前仅部分实现配置，需完善具体的特种兵数值Buff逻辑。

### 2.2 优化与对接
- [ ] **数值配置化**: 将部分硬编码的战斗常数（如 1.5倍暴击、10基础伤）提取到配置表。
- [ ] **前端联调**: 提供 `API_TEST_README.md` 给前端同学，联调战斗回放渲染。

---


## 3. 近期更新 (Recent Updates)
- [x] **体力系统移除**: 彻底删除了 DB (`users`), Entity (`UserTbl`), Service (`BattleService/PlayerInfoService`) 中的所有体力逻辑，实现了无限挑战。
- [x] **Bug Fixes**:
    - 修复了 `BattleService` 中 `source` 参数命名不一致导致的编译错误。
    - 修复了 `BattleSessionTbl` 缺失 `civ` 字段导致的编译错误。
    - 修复了 `BattleService` 中 `session` 变量未初始化的错误。
    - 修复了 `AuthService` 中残留的 `setStamina` 调用。
- [x] **验证通过**: 提供了 `API_TEST_README.md` 并通过了核心流程的编译检查。


