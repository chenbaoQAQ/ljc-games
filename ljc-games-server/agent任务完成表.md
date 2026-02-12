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
- **进化效果实装**: 当前进化仅修改了数据库层级, 战斗数值计算 (`BattleEngine`) 需进一步开发.

---

## 5. 项目经理追加需求交付 (Completed)
### 5.1 后端改动
- **Schema**:
  - `user_troop_progress` 新增 `evolution_unlocked` (TINYINT)，用于独立控制进化权限。
  - 添加 `DROP TABLE` 语句，确保环境重启清理干净。
- **Data**:
  - 新增 KR (Stage 5 -> 3003) 和 GB (Stage 5 -> 3004) 特种兵解锁配置。
- **Service (TroopService/HallService)**:
  - `TroopService.evolveTroop`: 增加 `evolution_unlocked == 1` 强校验。
  - `TroopService.getTroopCodex`: 聚合 `story_unlock_config`，下发 `unlockHint` (例如“通关 KR 第5关解锁”) 和 `evolutionUnlocked` 状态。
  - `HallService.recruit`: 增加前置检查，未解锁兵种 (`status < 2`) 禁止招募，防止 API 绕过。
- **DTO**:
  - `TroopCodexVO`: 增加 `unlockHint`, `unlockStageNo`, `evolutionUnlocked`。

### 5.2 前端改动 (ljc-game-frontend)
- **RecruitPage.js**:
  - 招募卡片未解锁时，展示后端下发的精准提示文案 (`unlockHint`)。
- **CodexPage.js**:
  - 图鉴卡片增加后端 `unlockHint` 展示。
  - 进化按钮现在根据 `evolutionUnlocked` 字段动态禁用/启用，未解锁时提示“进化未解锁”。

### 5.3 交付确认
- ✅ **招募阻断**: 必须解锁才能招募 (HallService 拦截)。
- ✅ **进化阻断**: 必须触发进化解锁事件才能进化 (TroopService 拦截)。
- ✅ **特种兵配置**: 补全四国特种兵解锁。
- ✅ **图鉴提示**: 前端展示“通关 X 国 X 关解锁”。

## 6. 阻断问题紧急修复 (Hotfix)
针对复验中发现的阻断级问题，已完成如下修复：
1. **编译错误修复**: 删除了 `TroopService.java` 中重复声明的 `unlockEvolution` 方法。
2. **SQL 语法修复**: 修正了 `UserTroopProgressMapper.xml` 中的双重 `WHERE` 子句。
3. **数据主键冲突修复**: 删除了 `data.sql` 中冗余且导致冲突的 KR/GB `story_unlock_config` 插入语句，保留了包含 `unlock_troop_id` 的正确配置块。

当前代码已通过静态检查，数据脚本可安全重放。请再次复验。

## 7. 第3轮补丁 (Round 3 Fixes)
响应 `agent阶段性任务.md` 中的 F1/F2 要求，完成以下清理与安全加固：
1. **招募安全加固 (P1)**:
   - `TroopService.recruit` 增加了与 `HallService` 一致的解锁校验 (`status < 2` 阻断)。
   - 杜绝了绕过 API 直接调用底层服务进行非法招募的风险。
2. **代码质量清理 (P2)**:
   - 移除了 `unlockTroop` 和 `getTroopCodex` 中重复的 `setEvolutionTier(0)` 赋值。
   - 移除了 `evolveTroop` 中重复的“兵种未解锁”校验逻辑。
   - 代码逻辑更加精简，无冗余分支。

### 自测结果
- ✅ **招募阻断**: 调用 `TroopService.recruit` 招募未解锁兵种 -> 抛出异常“兵种未解锁，无法招募”。
- ✅ **进化校验**: 保持原有逻辑，且无重复代码干扰。
- ✅ **初始化**: SQL 脚本无变更，保持上轮修复后的稳定状态。
