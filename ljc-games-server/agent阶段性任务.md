# Agent 阶段性任务（美术资源产出 V1）

## 目标
基于提供的概念图风格，产出可直接接入前端的英雄/兵种美术资源（Q版、2D、明亮、粗描边）。

## 视觉基准（必须遵守）
- 参考风格：Q版（约2~3头身），偏塔防卡通。
- 画面特征：高饱和、清晰轮廓、阵营辨识强。
- 角色朝向：默认正面偏三分之二视角（与当前UI兼容）。
- 输出背景：透明底（PNG）。

## 资产范围（本期）

### 1. CN 阵营
- 英雄：刘备、关羽、张飞、赵云
- 兵种：重盾步兵、强弩兵、虎豹骑、青囊医官（特种）

### 2. JP 阵营
- 英雄：织田信长、真田幸村、德川家康
- 兵种：足轻、弓足轻、骑马武者、爆裂火筒队（特种）

### 3. KR 阵营
- 英雄：李舜臣、金庾信、乙支文德
- 兵种：步卒、长弓手、重骑卒、军乐旗卫（特种）

### 4. GB 阵营
- 英雄：亚瑟、兰斯洛特、莫德雷德
- 兵种：长枪步兵、长弓手、重骑士、破甲工兵（特种）

## 输出规格（严格）

### A. 单位头像（用于卡片/图鉴）
- 尺寸：`256x256`
- 格式：`PNG`（透明）
- 命名：`{civ}_{type}_{id}_avatar.png`
  - 示例：`CN_hero_1001_avatar.png`、`JP_troop_3002_avatar.png`

### B. 全身立绘（用于详情弹层）
- 尺寸：`768x768`
- 格式：`PNG`（透明）
- 命名：`{civ}_{type}_{id}_full.png`

### C. 阵营总览图（用于对照验收）
- 每阵营 2 张：英雄合集、兵种合集
- 尺寸：`2048x1024`
- 格式：`PNG`
- 命名：`{civ}_heroes_sheet.png`、`{civ}_troops_sheet.png`

## 路径规范（必须落仓库）
- 目标目录：
  - `/Users/lijunchen/Documents/ljc/ljc-games/ljc-game-frontend/public/assets/art/heroes/`
  - `/Users/lijunchen/Documents/ljc/ljc-games/ljc-game-frontend/public/assets/art/troops/`
  - `/Users/lijunchen/Documents/ljc/ljc-games/ljc-game-frontend/public/assets/art/sheets/`

## 执行步骤
1. 按阵营分批生成（CN -> JP -> KR -> GB）。
2. 每批先出“英雄合集+兵种合集”供风格确认。
3. 风格确认后再导出对应头像与全身立绘。
4. 落地到指定目录并按命名规则整理。
5. 更新映射清单（见下方交付要求）。

## 交付要求（给项目经理查收）
在 `agent任务完成表.md` 新增章节：`美术资源交付 (Art Assets Delivery)`，必须包含：
- 生成文件总数（头像/全身/合集分别统计）
- 文件清单（完整相对路径）
- 每阵营 2 张验收缩略图路径
- 风格一致性说明（与概念图对齐点）
- 未完成项（若无写“无”）

## 验收标准（DoD）
1. 四阵营全部角色与兵种覆盖，无漏项。
2. 文件命名、尺寸、透明底、目录路径全部符合规范。
3. 可直接通过前端静态资源路径访问（不需要二次处理）。
4. 阵营视觉可区分（颜色、装束、武器特征明显）。
5. `agent任务完成表.md` 中有完整可核对交付清单。

---

# Agent 阶段性任务（返工：兵种进化树闭环修复）

## 目标
修复树状图鉴当前阻断问题，确保“查看树 -> 进化 -> 分支互斥 -> 招募联动”完整可用。

## 必做项（按优先级）

### P0-1 前端请求参数修复
- 修改 `troopAPI.getTree` 和 `troopAPI.evolveNode`，显式传 `userId`。
- 要求：
  - `GET /troop/codex/tree?userId=1&civ=CN`
  - `POST /troop/evolve?userId=1`
- 文件：`ljc-game-frontend/src/api/index.js`

### P0-2 分支互斥字段落库修复
- 在 `UserTroopProgressMapper.xml` 的 `insert/update` 中补齐 `chosen_child_node_id` 读写。
- 要求：
  - 进化 A 分支后，父节点 `chosen_child_node_id = A_node_id`
  - 刷新页面后，兄弟分支保持 `BRANCH_LOCKED`
- 文件：`ljc-games-server/src/main/resources/mapper/UserTroopProgressMapper.xml`

### P0-3 进化插入空字段修复
- `TroopTreeService.evolveNode` 新建 `toProgress` 时补齐 `evolutionTier`（至少置 0）和必要字段，避免插入失败。
- 文件：`ljc-games-server/src/main/java/ljc/service/TroopTreeService.java`

### P1-1 Tree 页面错误提示字段修复
- `res.msg` 改为 `res.message`（含加载失败与进化失败两处）。
- 文件：`ljc-game-frontend/src/pages/TreeCodexPage.js`

### P1-2 旧图鉴页兼容处理
- `CodexPage.js` 当前调用 `troopAPI.evolve` 已失效。
- 二选一：
  - A. 恢复 `troopAPI.evolve(userId, troopId)` 兼容旧页；
  - B. 旧页按钮改为跳转树页并移除旧进化调用。
- 文件：`ljc-game-frontend/src/pages/CodexPage.js`
- 文件：`ljc-game-frontend/src/api/index.js`

## 交付物要求
- 代码变更提交。
- `agent任务完成表.md` 新增“返工验收结果”章节，必须包含：
  - 修改文件清单
  - 每个 P0/P1 的修复说明
  - 2 组接口样例（成功/失败）
  - 3 张截图（初始、可进化、分支锁定后）

---

# Agent 阶段性任务（三次返工：Tree 页面编译阻断修复）

## 目标
修复 `TreeCodexPage.js` 语法错误，恢复页面可编译、可路由进入、可调用树接口。

## 必做项（P0）
1. 修正 `TreeCodexPage` 类定义，删除重复嵌套 `constructor`，保证类结构合法。
2. 本地执行语法检查并回填结果到完成表：
   - `node --check ljc-game-frontend/src/pages/TreeCodexPage.js`
3. 复查 `onMount/loadTree/handleEvolve` 方法均在类作用域内，且 `this.userId` 初始化一次即可。

## 交付要求
- 更新 `agent任务完成表.md`：新增“三次返工结果”，贴出语法检查通过结果（文本）。
- 不需要改需求，只修编译阻断。
