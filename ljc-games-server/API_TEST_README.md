# 接口测试指南 (API Test README)

本文档提供适配 **Apifox** 的接口测试用例。你可以直接复制下方的 `curl` 命令并在 Apifox 中选择 "导入 cURL" 即可快速生成接口。

> **环境依赖**:
> 1. 服务器已启动 (`LjcGamesServerApplication`)。
> 2. `data.sql` 已包含基础数据 (启动后自动加载)。
> 3. 默认主机: `http://localhost:8080` (在 Apifox 中建议配置为环境变量 `{{baseUrl}}`)。

---

## 0. 账号体系 (Auth)

该系统现已预置测试账号 (`userId=1`)。如需创建新号，请使用注册接口。

### 注册新账号
*如果需要测试多用户流程，可先注册新号*
```bash
curl -X POST "http://localhost:8080/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "测试数据",
    "password": "123",
    "nickname": "李俊辰",
    "initialCiv": "CN"
  }'
```

---

## 1. 大厅系统 (Hall System)

**测试前置**: 使用预置账号 `userId=1`，已拥有初始武将(ID=1001)、装备(ID=1)和金币。

### 1.1 武将管理

#### 激活武将 (Activate)
*消耗金币激活已解锁的武将 (测试用ID: 2)*
```bash
curl -X POST "http://localhost:8080/hall/general/activate?userId=1&generalId=2"
```

#### 升级武将 (Upgrade)
*消耗金币提升等级，每次调用升1级*
```bash
curl -X POST "http://localhost:8080/hall/general/upgrade?userId=1&generalId=1"
```

#### 穿戴装备 (Equip)
*将"铁剑" (ID=1) 穿戴到"新手主公"的武器槽*
```bash
curl -X POST "http://localhost:8080/hall/general/equip?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "generalId": 1,
    "equipmentId": 1
  }'
```

#### 学习技能 (Learn Skill)
*消耗背包中的技能书 (ItemID=301) 学习技能*
```bash
curl -X POST "http://localhost:8080/hall/skill/learn?userId=1&generalId=1&bookItemId=301"
```

### 1.2 装备与宝石

#### 强化装备 (Enhance)
*消耗金币强化装备等级*
```bash
curl -X POST "http://localhost:8080/hall/equipment/enhance?userId=1&equipmentId=1"
```

#### 宝石镶嵌 (Inlay)
*将攻击宝石 (ID=1) 镶嵌到装备的第1个孔位*
```bash
curl -X POST "http://localhost:8080/hall/gem/inlay?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "equipmentId": 1,
    "socketIndex": 1,
    "gemId": 1
  }'
```

---

## 2. 战斗框架 (Battle)

### 开始战斗 (Start)
*创建一场新战斗会话*
```bash
curl -X POST "http://localhost:8080/battle/start?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "generalId": 1,
    "stageId": 101,
    "troopConfig": {
      "1001": 10
    }
  }'
```
> **注意**: 响应中的 `sessionId` 需要记录下来，用于后续的回合推进接口。

### 回合行动 (Action)
*推进到下一回合 (示例中使用 SKIP 跳过)*
```bash
# 请将 <SESSION_ID> 替换为 开始战斗 接口返回的值
curl -X POST "http://localhost:8080/battle/action?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "<SESSION_ID>",
    "actionType": "SKIP"
  }'
```

---

## 常见问题 (FAQ)

1. **报错 "武将不存在"**: 请确认是否重启了服务器以加载新的 `data.sql`。
2. **Apifox 使用技巧**:
   - 复制上方的代码块。
   - 在 Apifox 左上角点击 **+** -> **导入 cURL**。
   - 粘贴代码即可自动解析 Body 和 Params。
