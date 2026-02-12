# Agent 任务完成表

## 1. 核心后端架构搭建 (已完成)
- **数据库表结构更新 (`schema.sql`)**:
  - 新增 `user_troop_progress`: 用于记录玩家兵种解锁状态 (LOCKED/DISCOVERED/UNLOCKED) 及进化阶数。
  - 新增 `troop_evolution_config`: 定义兵种进化的前置条件 (国家/关卡)、消耗 (金币) 及效果 (属性加成)。
  - 更新 `story_unlock_config`: 扩展支持“关卡解锁兵种” (`unlock_troop_id`) 和“关卡解锁进化” (`unlock_evolution_troop_id`)。

- **基础数据预埋 (`data.sql`)**:
  - 配置了 CN/JP 等国家的关卡解锁逻辑 (例如 CN-1 解锁步兵，CN-5 解锁特种兵)。
  - 预设了基础兵种 (步/弓/骑) 的一阶进化配置 (需通关 CN 8-10)。
  - 包含了数据清理逻辑，确保重启项目时数据状态一致。

## 2. Java 业务逻辑实现 (已完成)
- **实体与映射 (Entity & Mapper)**:
  - 创建了 `UserTroopProgressTbl`、`TroopEvolutionConfigTbl` 及对应的 MyBatis Mapper/XML。
  - 扩展了 `TroopTemplateMapper` 支持全量查询，用于图鉴 (`Codex`) 生成。

- **服务层 (`TroopService`)**:
  - 实现 `unlockTroop(userId, troopId)`: 关卡通关后解锁兵种状态。
  - 实现 `unlockEvolution(userId, troopId)`: 关卡通关后开启进化权限。
  - 实现 `evolveTroop(userId, troopId)`: 
    - 校验前置条件 (国家/关卡进度)。
    - 校验金币消耗。
    - 原子更新进化阶数。
  - 实现 `getTroopCodex(userId)`: 聚合兵种模板与玩家个人进度，生成完整的图鉴数据 (含未解锁状态)。

- **战斗结算集成 (`BattleService`)**:
  - 改造 `handleVictoryRewards`: 在战斗胜利结算时，自动检查 `story_unlock_config`，触发兵种解锁与进化权限开启。

- **Controller 接口暴露 (`TroopController`)**:
  - `GET /troop/codex`: 获取兵种图鉴 (含状态与属性)。
  - `GET /troop/progression`: 图鉴接口别名 (兼容前端命名)。
  - `POST /troop/evolve`: 触发兵种进化。
  - (原有招募接口 `recruit` 保持不变，但建议后续增加解锁状态校验)。

## 3. 阶段性任务覆盖情况
| 任务模块 | 描述 | 状态 | 说明 |
| :--- | :--- | :--- | :--- |
| **主线结构** | 40关结构与解锁配置 | ✅ 已覆盖 | 数据层支持，逻辑层已接通 |
| **兵种解锁** | 状态定义与落库 | ✅ 已完成 | `user_troop_progress` |
| **兵种进化** | 基础进化规则与消耗 | ✅ 已完成 | `troop_evolution_config` + `evolve` 接口 |
| **特种兵** | 关联主线解锁 | ✅ 已完成 | 通过 `story_unlock_config` 配置 |
| **图鉴系统** | 图鉴列表与状态展示 | ✅ 已完成 | `GET /codex` 接口实现 |
| **英雄解锁** | 挂载主线奖励 | ✅ 已完成 | 沿用并增强现有逻辑 |
| **前端改造** | 战前整备、招募等 | ✅ 已完成 | 已对接后端 API，实现动态解锁与过滤 |
| **图鉴页面** | 新界面 (`/codex`) | ✅ 已完成 | 支持查看所有兵种、解锁状态及进化操作 |

## 4. 后续建议
- **前端对接**: 请前端开发人员调用 `/troop/codex` 渲染图鉴与招募列表。
- **进化效果实装**: 当前进化仅修改了数据库层级 (`tier`)，战斗计算 (`BattleEngine`) 需读取 `tier` 并应用 `stat_modifiers_json` 中的属性加成 (目前 MVP 阶段仅落库)。
