# 进度对齐：V2.10 四国主线骨架完备 (Core Loop & Unlocks)

> **当前状态**：体力系统已移除，战斗核心逻辑 (V2.10) 重构完毕。支持 **多文明 (CN/JP/...) 进度校验**与**配置驱动的解锁机制**。JP 内容已实现最小闭环。
> **下一步**：技能效果实装、爬塔模式、前端对接。

## 1. 已完成 (Done)

### 1.1 基础架构 & 数据库
- [x] **数据库 Schema**: 完整实现用户、资产、战斗会话等核心表结构。
    - **重置脚本**: `data.sql` 已优化，支持一键清空重置，预置了 CN/JP 基础数据。
- [x] **Entity/Mapper**: 全原子操作，MyBatis XML 映射完整。
- [x] **Auth & Init**: 
    - 注册/登录流程打通。
    - **新号初始化**: 修正了初始兵种 ID (2001)，增加了模板存在性校验。

### 1.2 大厅系统 (Hall Service) (V2.3)
- [x] **武将管理**: 激活、升级、升阶、属性计算 (Base + Growth * Level)。
- [x] **装备 & 宝石**: 穿戴、强化 (+3降级规则)、镶嵌/合成 (5合1)。
- [x] **兵营**: 招募逻辑原子化。
- [x] **技能**: 学习逻辑闭环。

### 1.3 战斗系统 (Battle Service - V2.10 Refactor)
**本次迭代重点：彻底移除体力，重构会话与结算逻辑，支持多国主线。**

- [x] **去体力化 (Stamina Removal)**:
    - 全局移除 `stamina` 字段与相关逻辑，实现无限挑战。
- [x] **会话重构 (Session)**:
    - **正名**: `dungeonId` -> `stageNo`，消除歧义。
    - **Schema 对齐**: `battle_sessions` 表结构精简，与代码实体完全一致。
- [x] **主线推进 (Story Progression)**:
    - **启动校验**: 
        - **国家解锁**: 未解锁国家 (如 JP) 无法开启战斗。
        - **关卡顺序**: 严格限制 `stageNo <= maxStageCleared + 1`，禁止跳关。
    - **单一来源结算 (Finish)**: 
        - 统一了进度更新逻辑，按顺序处理：返兵 -> 进度 -> 英雄解锁 -> 国家解锁。
- [x] **配置驱动解锁 (Config-Driven Unlock)**:
    - 新增 `story_unlock_config` 表，替代硬编码。
    - 实现了 CN 1/5/10 奖励与 10 -> JP 解锁的配置化。
- [x] **JP 内容闭环**:
    - 新增 JP 英雄模板 (2001-2003) 与 JP-1 关卡配置。
    - 验证了 CN 通关后解锁并进入 JP 的流程。

## 2. 近期更新 (Recent Updates - V2.8~V2.10)
- [x] **V2.8**: 彻底移除体力系统，修复 `BattleSessionTbl` 编译错误。
- [x] **V2.9**: 重构 `BattleService`，将 `dungeonId` 更名为 `stageNo`，实现 `finishBattle` 单一来源结算与国家进度校验。
- [x] **V2.10**: 
    - 修复 `PlayerInitService` 初始兵种 bug。
    - 实现 `story_unlock_config` 配置驱动。
    - 补完 JP-1 关卡与英雄数据，打通 CN->JP 链路。

## 3. 待办事项 (Pending / Next Steps)

### 3.1 核心玩法补完
- [ ] **技能效果 (Skill Effects)**: 目前 `checkSkill` 仅扣 CD，需解析 `effect_json` 实现真实 buff/damage/heal。
- [ ] **爬塔模式 (Tower)**: Schema 虽有，但 `startTowerBattle` 逻辑尚未实装 (Config 读取与层数校验)。
- [ ] **特种兵特性**: 实现具体的数值/机制 Buff。

### 3.2 内容扩展
- [ ] **多国关卡**: 补全 JP (2-10), KR (1-10), GB (1-10) 的 `story_stage_config`。
- [ ] **英雄配置**: 补全四国所有英雄模板数据。

### 3.3 工程化
- [ ] **前端对接**: 提供 API 文档，联调战斗回放。
- [ ] **数值配置化**: 将战斗公式中的硬编码系数提取至常量或配置表。

---
**备注**: 当前后端已具备完整的主线骨架，可随时支持前端接入与数值填充。


