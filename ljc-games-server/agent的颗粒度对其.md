# 进度对齐：大厅系统 (v2.3)

> 根据 `企划.md`，暂不包含战斗模块的完整实现。以下是当前已完成功能与待办事项的盘点。

## 1. 已完成 (Done)

### 1.1 基础架构
- [x] **数据库 Schema v2.3**: 完整实现了用户、武将、装备、宝石、战斗会话等核心表结构。(已修复 `created_at` 重复报错)
- [x] **Entity/Mapper 层**: 对应所有表的 Java 实体与 MyBatis 映射文件。
- [x] **用户注册/登录 (Auth)**: 
    - 实现了基于 JWT (或基础 Session) 的注册流程。
    - 修复了注册时的数据库字段约束错误 (`equip_weapon_id` null 问题)。
    - 完成了 `PlayerInitService`，支持新用户自动发放：四国进度、初始武将、初始兵力。

### 1.2 大厅功能 (Hall Service)
- [x] **武将系统**:
    - **激活**: 消耗金币激活已解锁武将 (逻辑已修复，支持重复校验)。
    - **升级**: 消耗金币提升等级，包含基础属性 (HP) 成长。
- [x] **装备系统 (重构优化版)**:
    - **穿戴**: 
        - 实现了 `UserEquipment` -> `UserGeneral` 的关联逻辑 (通过 `general_id` 字段)。
        - **API 优化**: 接口不再需要传 `slot`，后端自动通过 `EquipmentTemplate` 推断槽位。
    - **强化**: 消耗金币提升装备 `enhance_level`。
- [x] **宝石系统**:
    - **镶嵌**: 将宝石镶嵌到装备的指定孔位 (Socket 1/2)。
    - (*注: 合成接口已定义但逻辑暂为 Stub*)
- [x] **技能系统**:
    - **学习**: 消耗技能书覆盖武将当前技能。

### 1.3 测试与联调
- [x] **API 文档**: `API_TEST_README.md` 已全面更新，支持 Apifox 一键导入 cURL (已同步去除 slot 参数)。
- [x] **测试数据**: `data.sql` 预置了：
    - 性格、兵种、武将模板 (1001/1002)、装备模板 (铁剑/皮甲)、宝石模板。
    - 技能书与技能 (ID 1 鼓舞)。
    - 初始测试用户 `admin` (ID 1) 拥有完整测试资源。
- [x] **Bug 修复**:
    - 解决了初始数据导致的“武将已激活”错误。
    - 解决了数据库字段非空约束导致的 500 错误。
    - 解决了 HallService 的编译错误 (缺失 Import / 重复注解)。

---

## 2. 待办事项 (Pending - Hall Service Scope)

在进入战斗模块开发前，大厅系统仍需完善以下细节：

### 2.1 缺失的大厅功能 (已完成)
- [x] **兵营 (Recruit)**: 已实现 `recruit` 接口 (HallService).
- [x] **宝石合成 (Combine)**: 已实现 `combineGem` 逻辑 (HallService).
- [x] **武将升阶 (Ascend)**: 已实现 `ascendGeneral` 逻辑 (HallService).

### 2.2 逻辑完善
- [x] **强化失败机制**: 已严格按照 V2.8 实现 (+3后概率失败降级).
- [ ] **数值配置化**: (暂缓，硬编码于 Service 中).

---

## 3. 战斗模块进度 (Battle Module)

### 3.1 基础架构 (Done)
- [x] **数据库**: `battle_sessions` 表已创建，核心字段 `context_json` / `status`.
- [x] **Entity/Mapper**: 对应的 BattleSession 结构已就绪.
- [x] **API**: `BattleController` (Start/Turn) 已联调.

### 3.2 核心逻辑 V2.8 (Done)
- [x] **Context 构建**:
    - [x] 自动读取 UserGeneral/Troop/Equipment 数据.
    - [x] **数值对齐**: 装备/宝石属性已按 `X*n*(n+1)/2` 公式计算并叠加到 Hero Stats.
- [x] **回合推进 (ProcessTurn)**:
    - [x] **Phase 0-6**: 完整的回合阶段 (CD -> Skill -> Troops -> Hero -> Retreat -> End).
    - [x] **堆栈模型**: 实现 `applyDamage`，支持“个体死亡”与“伤害溢出”.
    - [x] **目标规则**: 严格遵循 V2.8 (固定优先级 / 克制优先).

## 4. 下一步计划
- [ ] **前端对接**: 联调 Battle Context JSON 的渲染.
- [ ] **数值调优**: 调整兵种/怪物的基础数值平衡.

