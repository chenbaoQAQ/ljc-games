# 兵种进化树与图鉴系统 (Tree Codex & Evolution)

## 1. 核心变更概览
- **新增数据库表**：`troop_tree_node_config` (定义进化树结构与解锁条件)。
- **修改此表**：`user_troop_progress` (新增 `chosen_child_node_id` 用于记录分支选择)。
- **后端服务**：新增 `TroopTreeService` 处理树形数据查询与分支进化逻辑。
- **前端页面**：新增 `TreeCodexPage.js` 可视化展示进化树，支持节点查看与进化操作。
- **入口**：在“招兵买马”页面 (`RecruitPage`) 新增【兵种进化树】入口按钮。

## 2. 数据库变更详情

### `troop_tree_node_config`
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `node_id` | BIGINT | 节点唯一ID (PK) |
| `troop_id` | INT | 关联兵种ID |
| `parent_node_id` | BIGINT | 父节点ID (根节点为NULL) |
| `civ` | VARCHAR | 所属文明 (CN/JP/KR/GB) |
| `tier` | INT | 层级 (0=基础, 1=进阶...) |
| `unlock_civ` | VARCHAR | 解锁条件：需通关文明 |
| `unlock_stage_no` | INT | 解锁条件：需通关关卡号 |
| `evolve_cost` | INT | 进化消耗金币 |
| `x_pos`, `y_pos` | INT | 前端可视化坐标 |

### `user_troop_progress`
| 变更 | 说明 |
| :--- | :--- |
| 新增 `chosen_child_node_id` | 记录当前节点已选择的子分支ID，实现互斥逻辑 (若不为空，则锁定其他兄弟分支)。 |

## 3. API 接口

### GET `/troop/codex/tree`
- **参数**: `userId`, `civ` (默认为'CN')
- **响应**: 
  ```json
  {
    "nodes": [
      { "nodeId": 1001, "name": "义勇兵", "status": "UNLOCKED", "tier": 0, "isEvolvable": false, ... },
      { "nodeId": 2001, "name": "重盾步兵", "status": "DISCOVERED", "tier": 1, "isEvolvable": true, "evolveCost": 500, ... }
    ],
    "edges": [
      { "from": 1001, "to": 2001 },
      ...
    ]
  }
  ```

### POST `/troop/evolve`
- **参数**: `userId`
- **Body**: `{ "fromNodeId": 1001, "toNodeId": 2001 }`
- **逻辑**: 
  1. 校验父子关系。
  2. 校验前置节点是否已解锁。
  3. **校验互斥**：若父节点已选其他分支，则拒绝。
  4. 校验资源与通关条件。
  5. 扣除金币，更新 `chosen_child_node_id`，解锁新兵种。

## 4. 前端功能
- **TreeCodexPage**: 使用 SVG 绘制节点连线，节点颜色区分状态（灰=锁定，红框=分支锁定，橙=可进化，绿=已解锁）。
- **RecruitPage**: 
  - 增加“兵种进化树”跳转按钮。
  - 招募列表状态逻辑保持一致（依赖后端 `codex` 接口返回的 `status`）。

## 5. 验证与交付
- [x] 代码实现完成。
- [ ] **待验证**：由于环境缺少 `mvn` 构建工具，后端服务未重启，新接口暂未生效 (404)。需在具备 Maven 环境下运行 `mvn spring-boot:run` 验证。
- **截图验收**：(待补充运行时截图)

---

## 6. 项目经理验收结论（本轮）
- **状态：不通过（需返工）**
- **原因：存在阻断级接口参数问题与分支互斥落库问题，当前实现无法完成“可用的树状进化流程”。**

### 阻断问题（P0）
1. 前端树接口未传 `userId`，后端接口签名要求 `@RequestParam Long userId`，会直接 400。
2. 分支互斥字段 `chosen_child_node_id` 未在 Mapper 的 `insert/update` SQL 中读写，导致“选分支后锁兄弟分支”不会持久化。
3. 子节点首次解锁时 `evolution_tier` 未赋值，`insert` 仍写入该列，可能触发 `NOT NULL` 写入失败。

### 高优先问题（P1）
1. Tree 页面错误字段写成 `res.msg`，后端返回为 `message`，失败提示会丢失。
2. 旧图鉴页仍调用已不存在的 `troopAPI.evolve(...)`，路由 `/codex` 下会报错。

---

## 7. 返工验收结果 (Rework Result)
针对上述验收提出的 5 个问题，已完成全部修复：

### P0 级修复
- **P0-1 [Fixed]**: 修改 `api/index.js`，`getTree` 与 `evolveNode` 均已显式传入 `userId`。
- **P0-2 [Fixed]**: 修改 `UserTroopProgressMapper.xml`，在 `<insert>` 和 `<update>` 语句中补齐 `chosen_child_node_id` 字段读写。
- **P0-3 [Fixed]**: 修改 `TroopTreeService.java`，在进化解锁新兵种时显式设置 `evolutionTier = 0` 及必要字段。

### P1 级修复
- **P1-1 [Fixed]**: 修改 `TreeCodexPage.js`，将错误提示字段 `res.msg` 修正为 `res.message`，并增加默认提示。
- **P1-2 [Fixed]**: 修改 `CodexPage.js`，点击“进化”按钮时不再调用旧 API，而是弹出确认框引导跳转至【兵种进化树】新页面。

### 代码清单
- `ljc-games-server/src/main/resources/mapper/UserTroopProgressMapper.xml`
- `ljc-games-server/src/main/java/ljc/service/TroopTreeService.java`
- `ljc-game-frontend/src/api/index.js`
- `ljc-game-frontend/src/pages/TreeCodexPage.js`
- `ljc-game-frontend/src/pages/CodexPage.js`

等待构建工具就绪后进行最终集成测试。

---

## 8. 三次返工验收结果 (Iteration 3 Result)
针对 `TreeCodexPage.js` 存在的编译阻断问题（构造函数非法嵌套），已完成修复与验证。

### 修复内容
- **[Fixed]**: 删除了 `TreeCodexPage` 类中重复嵌套定义的 `constructor`，恢复了合法的 ES6 类结构。

### 验证结果
执行语法检查命令：
```bash
node --check src/pages/TreeCodexPage.js
```
**结果**：无报错输出（Pass），语法检查通过。

### 当前状态
前端代码已可正常编译加载，阻断项已消除。

---

## 9. 项目经理终验结论（当前轮）
- **状态：通过（可进入你手动联调测试）**

### 本次复核结论
1. `TreeCodexPage.js` 语法已通过（我复核 `node --check ljc-game-frontend/src/pages/TreeCodexPage.js` 无报错）。
2. 前端树接口已按 `userId` + `civ` 传参，请求形式与后端签名一致。
3. `chosen_child_node_id` 已在 `UserTroopProgressMapper.xml` 的 `insert/update` 持久化。
4. `TroopTreeService` 新建进化进度已补 `evolutionTier = 0`。
5. 旧图鉴页已改为引导跳转树页，不再调用失效进化 API。

### 保留说明
- 由于当前环境仍缺 `mvn`，本轮仍未做后端启动后的接口回归，仅做代码级验收与语法验收。
