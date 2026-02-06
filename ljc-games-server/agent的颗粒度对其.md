# 实现计划：大厅系统与战斗框架 (v2.3)

> 严格遵照《游戏企划书 v2.3》| 不做 SLG | 不做世界地图

## 1. 大厅系统 (Hall System)

大厅是玩家进行资源管理和养成的地方。

### 1.1 核心功能点

| 模块 | 功能 | 逻辑简述 | 涉及表 (Schema v2.3) |
| :--- | :--- | :--- | :--- |
| **四国进度** | 解锁国家 | 检查前置条件(如上一国通关)，更新 `user_civ_progress` | `user_civ_progress` |
| **兵营** | 招兵 | 消耗 Gold -> 增加 `user_troops` 数量 | `user_troops`, `currency_ledger` |
| **武将** | 解锁 | 剧情/条件满足 -> `user_generals.unlocked = 1` | `user_generals` |
| | 激活 | 消耗 Gold/Diamond -> `user_generals.activated = 1` | `user_generals` |
| | 升级 | 消耗 Gold -> `level++`, 属性提升 | `user_generals` |
| | 升阶 | 消耗材料/爬塔资格 -> `tier++`, 等级上限提升 | `user_generals` |
| **装备** | 强化 | 消耗 Gold -> `simulate success rate` -> `enhance_level++` (Max +8) | `user_equipments` |
| | 穿戴 | 检查槽位 `slot` -> 更新 `user_generals` 的 `equip_*_id` | `user_equipments`, `user_generals` |
| **宝石** | 镶嵌 | 检查孔位 -> `user_equipments.socket*_gem_id` | `user_equipments`, `user_gems` |
| | 合成 | 5合1 -> `count -= 5`, `next_level_count += 1` | `user_gems` |
| **技能** | 学习 | 消耗技能书 -> 覆盖 `user_general_skill` | `user_general_skill`, `skill_learn_log` |

### 1.2 接口设计 (API Draft)

-   `POST /hall/recruit` (troopId, count)
-   `POST /general/upgrade` (generalId)
-   `POST /general/equip` (generalId, equipmentId, slot)
-   `POST /equipment/enhance` (equipmentId)
-   `POST /gem/combine` (gemType, level)
-   `POST /gem/inlay` (equipmentId, socketIndex, gemId)

---

## 2. 战斗框架 (Battle Framework)

仅实现会话管理与流程推进，**不仅算胜负细节**，专注交互结构。

### 2.1 核心逻辑 (v2.3)

1.  **无坐标/无行军**：点击即战斗。
2.  **会话制**：
    -   战斗开始 -> 生成 `session_id`。
    -   战斗中 -> 仅通过 `session_id` 交互。
    -   支持 **Civ + Stage** (主线) 或 **TowerFloor** (爬塔)。

### 2.2 数据库 (Schema v2.3 复用)

直接使用 `schema.sql` 中已存在的表，不做冗余设计。

-   **`battle_sessions`** (注意是复数): 存储运行时状态 `context_json`。
-   **`battle_log`**: 存储回合日志。

### 2.3 接口设计 (API Draft)

-   `POST /battle/start`
    -   Input: `generalId`, `civ`, `stageNo` (or `towerFloor`), `troopConfig`
    -   Output: `sessionId`, `turn: 0`
-   `POST /battle/action`
    -   Input: `sessionId`, `actionType` (Skill/Skip)
    -   Output: `turn: N+1`, `logs: [...]`

## 3. 下一步计划

1.  **用户确认**：确认本计划符合 v2.3 要求。
2.  **大厅开发**：优先完成兵营、武将、装备逻辑。
3.  **战斗连接**：实现 `start` 和 `action` 的基础空转逻辑（Log only）。
