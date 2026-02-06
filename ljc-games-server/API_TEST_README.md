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

### 1.3 资源与招募

#### 招兵 (Recruit)
*消耗金币招募兵种 (ID=1001), 数量=10*
```bash
curl -X POST "http://localhost:8080/hall/recruit?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "troopId": 1001,
    "count": 10
  }'
```

#### 宝石合成 (Combine Gem)
*消耗5颗同类同级宝石合成高一级宝石*
```bash
curl -X POST "http://localhost:8080/hall/gem/combine?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "gemType": "ATK",
    "level": 1
  }'
```

#### 武将升阶 (Ascend)
*消耗金币提升武将阶数 (Break Limit)*
```bash
curl -X POST "http://localhost:8080/hall/general/ascend?userId=1&generalId=1"
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

> **重要提示**: 战斗模块依赖新的数据库表。请务必先执行以下 SQL 脚本：
> `src/main/resources/schema_update_battle.sql`


### 开始战斗 (Start)
*创建一场新战斗会话 (Session)*
```bash
curl -X POST "http://localhost:8080/battle/start?userId=1&dungeonId=101"
```
> **注意**: 响应中的 `data` 即为 `battleId` (虽然实际逻辑使用 userId 查找会话，但 ID 可用于展示).

### 回合行动 (Action / Turn)
*推进到下一回合 (可选择是否施放技能)*
```bash
curl -X POST "http://localhost:8080/battle/turn?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "castSkill": true
  }'
```
*响应包含了当前的 Context 快照，可用于前端复现战斗画面。*

---

## 常见问题 (FAQ)

1. **报错 "武将不存在"**: 请确认是否重启了服务器以加载新的 `data.sql`。
2. **Apifox 使用技巧**:
   - 复制上方的代码块。
   - 在 Apifox 左上角点击 **+** -> **导入 cURL**。
   - 粘贴代码即可自动解析 Body 和 Params。
