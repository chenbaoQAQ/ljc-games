# LJC Games 当前项目报告（技术负责人版，已按代码核对）

> 核对基准：前端 `ljc-game-frontend/src`、后端 `ljc-games-server/src`、初始化脚本 `schema.sql + data.sql`。

## 1. 项目结构与运行
- 前端：Vite + 原生 JS SPA，路由入口 `ljc-game-frontend/src/main.js`。
- 后端：Spring Boot + MyBatis + MySQL。
- 数据初始化：`spring.sql.init.mode=always`，启动自动执行 `schema.sql` + `data.sql`。
- 端口：前端开发 `3000`，后端 `8080`，前端走 `/api` 代理。

## 2. 前端模块（当前实际）

### 2.1 登录 `/login`
- 注册/登录，登录后写入 `localStorage`，进入大厅。

### 2.2 大厅 `/hall`
- 展示玩家资源、武将摘要、国家进度、功能入口。
- 兵力区已改为动态网格：
  - 支持多兵种滚动展示（非固定3卡）。
  - 按当前国家过滤显示。
  - 展示特种标记。
- 功能按钮：武将、招募、关卡、装备、宝石、技能、图鉴、爬塔入口（爬塔仍为占位提示）。

### 2.3 招募 `/recruit`
- 数据来源：`/player/info` + `/troop/codex`。
- 已解锁兵种可调数量招募；未解锁兵种灰态并展示 `unlockHint`。
- 招募调用 `/hall/recruit`。

### 2.4 武将 `/generals`
- 武将列表与详情。
- 激活、升级、升阶。
- 6装备栏穿戴管理：武器、防具、头盔、鞋子、坐骑、饰品。

### 2.5 装备 `/equipment`
- 展示装备模板名、槽位、基础属性、强化等级、穿戴状态。
- 支持强化。

### 2.6 宝石 `/gem`
- 宝石合成（5合1）。
- 装备孔位镶嵌。

### 2.7 技能 `/skill`
- 选择武将 + 技能书学习技能（覆盖式）。
- 技能书显示名称映射（前端 `SKILL_BOOK_META` 已扩到 `301~313`）。

### 2.8 关卡 `/stages`
- 按国家显示主线进度与解锁状态。
- 进入战前整备。

### 2.9 战前整备 `/battle/prepare`
- 选择主将，分配兵力。
- 统率占用实时校验，超限禁用出征。
- 可见兵种按国家过滤（含本国特种兵）。

### 2.10 战斗 `/battle`
- 状态恢复：`/battle/state`。
- 推进回合：`/battle/turn`。
- 前端按事件流展示日志与状态。

### 2.11 图鉴 `/codex`
- 全兵种列表 + 国家筛选。
- 展示解锁状态、基础属性、解锁提示。
- 进化按钮受 `evolutionUnlocked` 控制，未解锁时禁用。

## 3. 后端模块与接口（当前实际）

### 3.1 认证与玩家
- `AuthController`
  - `POST /auth/register`
  - `POST /auth/login`
- `PlayerController`
  - `GET /player/info`（含资源、兵力、`initialCiv`）
  - `GET /player/progress`

### 3.2 大厅聚合
- `HallController` 主要能力：
  - 武将：查询/激活/升级/升阶
  - 装备：查询/穿戴/强化
  - 宝石：查询/镶嵌/合成
  - 技能：学习/道具查询
  - 招募：`POST /hall/recruit`
- `HallService.recruit` 已做兵种解锁校验（未解锁会拦截）。

### 3.3 战斗
- `BattleController`
  - `POST /battle/story/start`
  - `POST /battle/turn`
  - `GET /battle/state`
- `BattleService` + `BattleEngine`
  - 当前战斗分两阶段：`HERO_SOLO -> TROOP_WAR`。
  - 战斗胜利后 `handleVictoryRewards` 会读取 `story_unlock_config` 并触发：
    - 武将解锁
    - 国家解锁
    - 兵种解锁（`unlock_troop_id`）
    - 进化权限解锁（`unlock_evolution_troop_id`）

### 3.4 兵种/图鉴/进化
- `TroopController`
  - `POST /troop/recruit`
  - `GET /troop/codex`
  - `GET /troop/progression`
  - `POST /troop/evolve`
- `TroopService` 当前行为：
  - `recruit`：已做解锁校验（与 Hall 侧一致）。
  - `unlockTroop`：写入/提升 `status=UNLOCKED`。
  - `unlockEvolution`：写入 `evolution_unlocked=1`。
  - `evolveTroop`：校验解锁状态 + 进化权限 + 关卡前置 + 金币消耗。
  - `getTroopCodex`：返回 `status`、`evolutionTier`、`evolutionUnlocked`、`unlockCiv`、`unlockStageNo`、`unlockHint`。

## 4. 数据层现状（Schema + Seed）

### 4.1 关键表
- 资产：`user_generals`、`user_troops`、`user_equipments`、`user_gems`、`user_inventory`。
- 配置：`general_template`、`troop_template`、`equipment_template`、`gem_template`、`skill_template`、`skill_book_map`。
- 进度：`user_civ_progress`、`story_unlock_config`、`user_troop_progress`、`troop_evolution_config`。
- 战斗：`battle_sessions`、`battle_log`、`battle_turn_log`。

### 4.2 装备/宝石/技能书
- 装备模板已覆盖6栏：`weapon/armor1/helm/boots/mount/accessory`，并补了多件测试装。
- 宝石模板已包含：`ATK/HP/SPD/CAP`，等级 `1~5`。
- 技能模板已扩展到 `1~13`（主动+被动初版）。
- 技能书映射已扩展到 `301~313`，并给测试号发放对应数量。

### 4.3 兵种精细化
- 已补“兵种精细化补丁V2”：四国三基础兵差异化定位，四特种兵参数重设。
- 前端 `gameData.js` 已同步名称、招募价、统率占用，显示口径与后端一致。

## 5. 测试账号态（admin）
- 账号：`admin`（用户ID=1，初始化脚本写入）。
- 当前设置为全模块联调态：
  - 四国进度全开（10关）。
  - 全兵种库存。
  - `user_troop_progress` 全解锁 + 进化权限开启。
  - 全武将已解锁（脚本按 `general_template` 自动补齐）。
  - 爬塔进度已置为可测状态。

## 6. 当前可走通流程
1. 登录 -> 大厅。
2. 武将管理（激活/升级/升阶/穿戴）。
3. 装备强化 + 宝石镶嵌/合成 + 技能学习。
4. 招募（按解锁状态控制）-> 战前整备 -> 开战 -> 推回合 -> 结算。
5. 图鉴查看解锁条件并触发进化。

## 7. 已知风险与技术债（真实保留项）
- 数据脚本存在“前段插入 + 后段覆盖（ON DUPLICATE/UPDATE）”的模式：
  - 最终落库以脚本后段为准；可用但维护成本高。
- 爬塔页面前端仍是入口占位，未形成完整玩法闭环。
- 本机环境未装 Maven（`mvn` 不可用），本报告基于代码与脚本静态核对；后端构建级验证需在有 Maven 的环境执行。

## 8. 建议的下一步治理
1. 把 `data.sql` 拆分为“基础种子 + 平衡补丁 + 测试态覆盖”三层文件，减少覆盖冲突。
2. 收敛 `TroopService.recruit` 与 `HallService.recruit` 到单一服务入口，避免双处逻辑漂移。
3. 把技能 `effect_json` 逐步接入战斗引擎真实结算，减少“配置已写但战斗未生效”的落差。
