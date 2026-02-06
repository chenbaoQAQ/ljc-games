# 接口测试指南 (API Test README)

本通过 `curl` 命令验证 **大厅系统** 与 **战斗框架** 的核心功能。

> **前置条件**：
> 1. 启动服务器 (`LjcGamesServerApplication`)
> 2. 数据库已初始化 (`schema.sql` 和 `data.sql` 已执行)
> 3. 确保存在测试用户 (userId=1) 和基础数据 (武将、装备、物品)

---

## 1. 大厅系统 (Hall System)

### 1.1 武将管理

#### 激活武将 (Activate)
*消耗金币激活新武将*
```bash
curl -X POST "http://localhost:8080/hall/general/activate?userId=1&generalId=1"
```

#### 升级武将 (Upgrade)
*消耗金币提升武将等级 (Level +1, HP +50)*
```bash
curl -X POST "http://localhost:8080/hall/general/upgrade?userId=1&generalId=1"
```

#### 穿戴装备 (Equip)
*将装备 (equipmentId=1) 穿戴到武将 (generalId=1) 的武器槽位 (weapon)*
```bash
curl -X POST "http://localhost:8080/hall/general/equip?userId=1" \
     -H "Content-Type: application/json" \
     -d '{
           "generalId": 1,
           "equipmentId": 1,
           "slot": "weapon"
         }'
```

#### 学习技能 (Learn Skill)
*消耗技能书 (itemId=301) 给武将学习技能*
```bash
curl -X POST "http://localhost:8080/hall/skill/learn?userId=1&generalId=1&bookItemId=301"
```

### 1.2 装备与宝石

#### 强化装备 (Enhance)
*消耗金币强化装备 (Level +1)*
```bash
curl -X POST "http://localhost:8080/hall/equipment/enhance?userId=1&equipmentId=1"
```

#### 宝石镶嵌 (Gem Inlay)
*将宝石 (gemId=101) 镶嵌到装备 (equipmentId=1) 的第1个孔位*
```bash
curl -X POST "http://localhost:8080/hall/gem/inlay?userId=1" \
     -H "Content-Type: application/json" \
     -d '{
           "equipmentId": 1,
           "socketIndex": 1,
           "gemId": 101
         }'
```

### 1.3 兵营 (Troops)

#### 招募士兵 (Recruit)
*消耗金币招募兵种 (troopId=1) 100个*
```bash
curl -X POST "http://localhost:8080/hall/recruit?userId=1" \
     -H "Content-Type: application/json" \
     -d '{
           "troopId": 1,
           "count": 100
         }'
```

---

## 2. 战斗框架 (Battle Framework)

### 2.1 开始战斗 (Start Battle)
*携带 10个兵种1 和 5个兵种2 出战*
* **注意**: 此接口会创建 `battle_sessions` 记录，并锁定兵力。
```bash
curl -X POST "http://localhost:8080/battle/start?userId=1" \
     -H "Content-Type: application/json" \
     -d '{
           "generalId": 1,
           "stageId": 101,
           "troopConfig": {
             "1": 10,
             "2": 5
           }
         }'
```
> **返回结果**: 成功将返回 `sessionId` (例如 `1700000000123`)。请记下此 ID 用于后续交互。

### 2.2 回合推进 (Battle Action)
*推进到下一回合 (模拟)*
```bash
# 请替换 <SESSION_ID> 为上面接口返回的 ID
curl -X POST "http://localhost:8080/battle/action?userId=1" \
     -H "Content-Type: application/json" \
     -d '{
           "sessionId": "<SESSION_ID>",
           "actionType": "SKIP"
         }'
```

---

## 3. 常见问题排查

- **金币不足**: 请手动修改数据库给用户加钱: `UPDATE users SET gold = 999999 WHERE id = 1;`
- **武将不存在**: 检查 `user_generals` 表是否有数据。
- **装备不存在**: 检查 `user_equipments` 表。
- **兵力不足**: 先调用招募接口或手动修改 `user_troops`。
