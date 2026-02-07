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
```sql
DELETE FROM battle_sessions;
DELETE FROM user_generals WHERE id > 2;
UPDATE user_civ_progress SET max_stage_cleared=0 WHERE user_id=1;
```
