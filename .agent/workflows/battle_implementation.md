# 战斗模块完整设计文档 (Battle System Design)

## 1. 核心目标
将“主线关卡战斗”打造为一个独立的策略回合制模块，核心包括：
- **独立战斗场景**：进入战斗后全屏沉浸式体验。
- **回合制推进**：类似《文明》或《率土》，每一步行动由后端计算并返回日志。
- **可视化反馈**：通过后端返回的 `logEvents` 驱动前端动画（如攻击、扣血、特效）。
- **策略性**：包括战前配兵（兵种克制、容量限制）和战斗中技能释放。

---

## 2. 战斗状态机 (Battle State Machine)

### A. Idle (非战斗状态)
- 用户位于主线地图 (`StageSelectionPage`)。
- 用户选择关卡，触发“战前整备”。

### B. Preparation (战前整备)
- **页面**：`BattlePreparePage`
- **操作**：
  1. 选择出战武将 (General)。
  2.配置携带兵力 (Troops)。
     - 限制：`Total Soldiers <= General Capacity`。
     - 限制：`Troop Count <= User Inventory`。
  3. 点击“出征”。
- **API**：`POST /battle/story/start`
  - 返回：`BattleContext` (包含初始状态) 和 `battleId`。

### C. InBattle (战斗进行中)
- **页面**：`BattlePage`
- **初始化**：
  - 如果是从 B 进入，直接使用 API 返回的初始状态。
  - 如果是刷新页面或重连，调用 `GET /battle/state` 恢复状态。
- **循环 (Turn Loop)**：
  1. **渲染状态**：展示双方 HP、兵力、Buff。
  2. **判断行动权**：
     - **我方行动 (HeroA)**：显示操作按钮 [普通攻击] [释放技能]。等待用户输入。
     - **非我方行动 (HeroB / Troops)**：显示“敌方行动中...”，自动调用 `processTurn`。
  3. **执行行动**：
     - 调用 `POST /battle/turn` (参数: `actionType`, `skillId` 等)。
  4. **播放动画**：
     - 收到后端返回的 `logEvents` 列表。
     - 逐条播放动画（如：A 攻击 B -> B 扣血 -> B 反击）。
     - 播放完毕后，更新界面到最新状态。
  5. **检查结束**：
     - 如果 `isFinished=true`，进入结算阶段。
     - 否则回到步骤 1。

### D. Finished (结算)
- **展示结果**：胜利/失败弹窗。
- **API**：(可选，如果 turn 接口已包含结算信息)
  - 后端在最后一回合的 `turn` 响应中包含 `rewards` 和 `unlocks`。
- **退出**：点击“返回大厅” -> 跳转回主线页面，刷新关卡进度。

---

## 3. 接口规范 (API Specification)

### 3.1 开启战斗 (Start)
- **Endpoint**: `POST /battle/story/start`
- **Params**: `userId`
- **Body**: 
  ```json
  {
    "civ": "CN",
    "stageNo": 1,
    "generalId": 101,
    "troopConfig": { "2001": 50, "2002": 30 }
  }
  ```
- **Response**: `BattleContext` (包含初始的 `sideA`, `sideB`, `turnNo=0`)

### 3.2 获取状态 (Get State)
- **Endpoint**: `GET /battle/state`
- **Params**: `userId`
- **Response**: `BattleContext`

### 3.3 推进回合 (Process Turn)
- **Endpoint**: `POST /battle/turn`
- **Params**: `userId`
- **Body**:
  ```json
  {
    "castSkill": true, // false for Attack
    "clientTurnNo": 5  // Optional, for idempotency
  }
  ```
- **Response**: `BattleContext` (包含 `lastEvents` 列表用于回放)

---

## 4. 前端数据映射 (Data Mapping)

由于后端 `UserTroopTbl` 仅包含 `troopId` 和 `count`，前端需维护一份**兵种字典**用于显示。

### 兵种字典 (Troop Definitions)
| TroopID | Name | Type | Description |
| :--- | :--- | :--- | :--- |
| 2001 | 步兵 | INF | 近战，克弓 |
| 2002 | 弓兵 | ARC | 远程，克骑 |
| 2003 | 骑兵 | CAV | 冲锋，克步 |
| 3001 | 诸葛连弩(CN) | ARC | 精英弓兵 |
| 3002 | 鬼武者(JP) | INF | 精英步兵 |
| 3003 | 花郎箭手(KR) | ARC | 精英弓兵 |
| 3004 | 皇家骑士(GB) | CAV | 精英骑兵 |

---

## 5. 待修复 Bug (Current Issues)
1. **BattlePreparePage**: 兵力列表显示 `undefined`。
   - 原因：引用了不存在的 `t.name` 和 `t.totalCount`。
   - 修复：引入兵种字典，使用 `t.count`。
2. **BattlePage**: `startNewBattle` 逻辑残留。
   - 原因：旧代码未清理干净。
   - 修复：已在 Step 889 清理，需验证。

