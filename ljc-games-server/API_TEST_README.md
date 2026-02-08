# API 测试指南 (Verification Guide)

本文档提供了使用 `curl` 命令测试 LJC 游戏服务器核心功能的指南，重点涵盖大厅系统、战斗逻辑和主线流程（1-1 至 1-10）。

## 前置条件

- 服务器运行在 `localhost:8080` (或配置端口)
- 已安装 `curl`
- 已安装 `jq` (可选，用于美化 JSON 输出)
- 数据库已通过 `data.sql` 初始化

## 1. 用户与初始状态

### 1.1 检查初始武将 (管理员用户 ID: 1)
```bash
curl "http://localhost:8080/hall/generals?userId=1"
```
*预期结果:* 应返回武将 1001 (已激活) 和 1002 (未激活)。

### 1.2 检查初始进度
```bash
curl "http://localhost:8080/hall/progress?userId=1"
```
*预期结果:* 空列表或初始 CN 记录 (如果已初始化)。

## 2. 大厅系统 (Hall System)

### 2.1 兵营招募
招募 50 个义勇兵 (ID: 2001)
```bash
curl -X POST "http://localhost:8080/hall/recruit?userId=1" \
     -H "Content-Type: application/json" \
     -d '{"troopId": 2001, "count": 50}'
```

### 2.2 激活武将
激活武将 1002 (需要 1000 金币，用户初始有 100000)
```bash
curl -X POST "http://localhost:8080/hall/general/activate?userId=1&generalId=2"
```
*注意:* `generalId` 依赖于数据库 ID。`data.sql` 中 user_generals 的 id 是自增的。请检查数据库或 1.1 的返回结果。假设模板 1002 对应的 ID 为 2。

### 2.3 升级武将
升级武将 1 (模板 1001)
```bash
curl -X POST "http://localhost:8080/hall/general/upgrade?userId=1&generalId=1"
```

## 3. 战斗系统 (主线循环)

### 3.1 开始战斗 (关卡 1-1)
*   **文明:** CN
*   **关卡:** 1
*   **武将:** 1 (UserGeneral ID)
*   **兵力:** 10 义勇兵 (Troop ID 2001)

```bash
curl -X POST "http://localhost:8080/battle/start" \
     -H "Content-Type: application/json" \
     -d '{
       "userId": 1,
       "civ": "CN",
       "stageNo": 1,
       "generalId": 1,
       "troops": {
         "2001": 10
       }
     }'
```
*响应:* 返回 `battleId` (例如: `1707300000000`)。

### 3.2 推进回合 (循环直至结束)
将 `1707300000000` 替换为实际的 `battleId`。
```bash
curl -X POST "http://localhost:8080/battle/action" \
     -H "Content-Type: application/json" \
     -d '{
       "userId": 1,
       "battleId": 1707300000000,
       "castSkill": false
     }'
```
*检查响应中的 `ended: true` 和 `win: true`。*

### 3.3 验证解锁 (主线 1 胜利后)
检查武将 1002 是否解锁 (如果尚未解锁)。
```bash
curl "http://localhost:8080/hall/generals?userId=1"
```

### 3.4 推进至 1-10 (模拟)
要测试第 10 关解锁，您可以重复 3.1 和 3.2 步骤挑战第 10 关 (假设您使用了作弊或打通了 2-9)。

**开始第 10 关 (Boss):**
```bash
curl -X POST "http://localhost:8080/battle/start" \
     -H "Content-Type: application/json" \
     -d '{
       "userId": 1,
       "civ": "CN",
       "stageNo": 10,
       "generalId": 1,
       "troops": {
         "2001": 100,
         "2003": 50
       }
     }'
```

胜利后，检查 **下一国家解锁**:
```bash
curl "http://localhost:8080/hall/progress?userId=1"
```
*预期结果:* CN 最大通关 10，JP 解锁。

## 4. 其他功能

### 4.1 装备强化
强化装备 1
```bash
curl -X POST "http://localhost:8080/hall/equipment/enhance?userId=1&equipmentId=1"
```

### 4.2 宝石镶嵌
将宝石 1 镶嵌到装备 1 的孔位 1
```bash
curl -X POST "http://localhost:8080/hall/gem/inlay" \
     -H "Content-Type: application/json" \
     -d '{"equipmentId": 1, "socketIndex": 1, "gemId": 1}'
```

## 5. 数据重置
重置数据以进行新一轮测试：
重启服务器 (重载 `data.sql`)
或者运行 SQL:

---

## 7. 四国主线验证方式

### 7.1 使用test.http完整测试
项目根目录下的 `test.http` 文件包含了四国主线(CN/JP/KR/GB)的完整测试流程。

**运行方式**:
- 使用 IntelliJ IDEA HTTP Client
- 或使用 VSCode REST Client 插件

**测试流程**:
1. 登录admin账号
2. 检查大厅状态(将领、进度、兵力)
3. CN主线1-10关顺序通关
4. JP主线1-10关顺序通关
5. KR主线1-10关顺序通关
6. GB主线1-10关顺序通关
7. 最终验证（四国进度和12个奖励英雄）

### 7.2 解锁链路
四国解锁必须按以下顺序进行：

```
CN-10通关 → JP解锁 → JP-10通关 → KR解锁 → KR-10通关 → GB解锁 → GB-10通关 → 主线结束
```

**关键验证点**:
- CN-10 胜利后: `user_civ_progress`中JP的`unlocked`变为1
- JP-10 胜利后: `user_civ_progress`中KR的`unlocked`变为1
- KR-10 胜利后: `user_civ_progress`中GB的`unlocked`变为1
- GB-10 胜利后: 主线完成，无新国家解锁

### 7.3 奖励英雄解锁
每个国家在1/5/10关通关后会自动解锁对应英雄：

| 国家 | 第1关 | 第5关 | 第10关 |
|------|-------|-------|--------|
| CN   | 1002  | 1003  | 1004   |
| JP   | 2001  | 2002  | 2003   |
| KR   | 2201  | 2202  | 2203   |
| GB   | 2301  | 2302  | 2303   |

完成四国主线后，`user_generals`表应包含13个将领（初始1个+奖励12个）。

---

## 8. 数据一致性校验

服务启动时会自动执行`DataIntegrityChecker`进行数据完整性检查：

**校验内容**:
1. ✅ 四国每国都有1-10关的配置（共40关）
2. ✅ 解锁配置中指向的关卡存在
3. ✅ 解锁配置中指向的英雄模板存在
4. ✅ 关卡使用的掉落池配置存在

**如果校验失败**:
- 服务启动会终止
- 控制台会输出详细的错误信息
- 需要修复`data.sql`中的数据配置

---

---

## 9. 如何运行四国主线自测脚本

### 9.1 前置条件（必须）
```bash
# 1. 创建数据库表结构
mysql -u root -p ljc_game < src/main/resources/schema.sql

# 2. 初始化基础数据（包含admin测试号）
mysql -u root -p ljc_game < src/main/resources/data.sql

# 3. 启动后端服务
./mvnw spring-boot:run
# 或在IDEA中直接运行LjcGamesApplication
```

### 9.2 使用test_story_4civ.http
**文件路径**: `/test_story_4civ.http`

**运行方式**:
- **IDEA**: 打开文件，点击每个请求左侧的绿色▶️按钮
- **VSCode**: 安装REST Client插件，点击"Send Request"

**操作流程**:
1. 执行"登录admin账号"请求
2. 执行"查询玩家信息"请求
3. **按顺序**执行CN-1到CN-10（每关的turn请求重复执行直到WIN）
4. 执行"验证：CN通关后JP已解锁"，确认JP的unlocked=1
5. 重复3-4步骤完成JP/KR/GB

### 9.3 关键注意事项
⚠️ **不能跳关**: 必须按1→2→3...→10顺序执行
⚠️ **重复turn**: 每关的turn请求需要多次执行直到battleStatus=WIN
⚠️ **顺序解锁**: 四国必须按CN→JP→KR→GB顺序解锁

### 9.4 验收标准
四国主线全部通关后，应满足：
```sql
-- 1. 四国全部解锁且通关
SELECT civ, unlocked, max_stage_cleared FROM user_civ_progress WHERE user_id=1;
-- 期望: CN/JP/KR/GB的unlocked都为1, max_stage_cleared都为10

-- 2. 12个奖励英雄全部解锁
SELECT COUNT(*) FROM user_generals WHERE user_id=1;
-- 期望: 13 (初始1个 + 奖励12个)
```

---
