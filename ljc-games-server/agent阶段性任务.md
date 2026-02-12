# Agent 阶段性任务（复修单 V2）

## 本次目标
修复当前交付中的阻断问题，保证后端可编译、SQL 可初始化、核心业务可用；修完后再提交 `agent任务完成表.md`。

## 阻断问题（必须全部修复）

### 1. Java 编译阻断：重复方法声明
- 文件：`src/main/java/ljc/service/TroopService.java`
- 问题：`unlockEvolution(Long userId, Integer troopId)` 被重复声明（出现两次）。
- 要求：
  - 删除重复声明，只保留一份实现。
  - 清理同类重复语句（如重复 `setEvolutionTier(0)`、重复状态判断）。

### 2. MyBatis SQL 语法阻断：双 WHERE
- 文件：`src/main/resources/mapper/UserTroopProgressMapper.xml`
- 问题：`updateByPrimaryKey` 中连续写了两次 `WHERE`。
- 要求：
  - 修成单一合法 SQL。
  - 检查 XML 其余语句无语法错误。

### 3. 数据初始化阻断：主键重复插入
- 文件：`src/main/resources/data.sql`
- 问题：`story_unlock_config` 的 KR/GB 记录被重复插入（同主键 `civ + stage_no`）。
- 要求：
  - 消除重复来源（保留一处真源）。
  - 如需要可重复执行，使用 `ON DUPLICATE KEY UPDATE` 风格。
  - 保证 KR/GB 特种兵解锁配置仍然存在：
    - KR 第5关 -> `unlock_troop_id = 3003`
    - GB 第5关 -> `unlock_troop_id = 3004`

## 业务正确性（修复后必须成立）

### A. 招募解锁校验
- `HallService.recruit` 必须校验 `user_troop_progress.status == 2`。
- 未解锁报错：`兵种未解锁，无法招募`。

### B. 进化解锁校验
- `user_troop_progress.evolution_unlocked` 字段生效。
- `unlockEvolution` 必须写 `evolution_unlocked=1`。
- `evolveTroop` 未解锁时报错：`该兵种进化尚未解锁`。

### C. 图鉴解锁提示
- `TroopCodexVO` 必须包含：`unlockCiv`、`unlockStageNo`、`unlockHint`。
- `getTroopCodex` 必须填充“通关 X 国第 Y 关解锁”提示。

## 交付要求（给项目经理验收）
提交 `agent任务完成表.md` 时必须包含：
1. 改动文件清单（精确路径）。
2. 每个阻断问题如何修复。
3. 自测结果（至少包含以下5条）：
   - 项目能正常启动（SQL 初始化通过）。
   - 未解锁兵种招募失败。
   - 解锁后招募成功。
   - 未解锁进化时进化失败。
   - `/troop/codex` 返回解锁提示字段。
4. 未完成项与风险（若无写“无”）。

## 验收标准（通过条件）
- 无编译阻断。
- 无初始化 SQL 阻断。
- 上述业务正确性 A/B/C 全部满足。
- 完成表与实际代码一致，无“文档写了但代码没落地”。

---

## 复验补充任务（第3轮）

### F1. 修复 `/troop/recruit` 绕过解锁问题（P1）
- 文件：`src/main/java/ljc/service/TroopService.java`
- 现状：`TroopService.recruit` 未校验 `user_troop_progress.status`，可绕过 `/hall/recruit` 的限制。
- 要求：
  - 在 `TroopService.recruit` 增加和 `HallService.recruit` 一致的解锁校验。
  - 仅允许 `status=2` 招募。
  - 未解锁报错统一为：`兵种未解锁，无法招募`。

### F2. 清理重复逻辑（P2，代码质量）
- 文件：`src/main/java/ljc/service/TroopService.java`
- 要求：
  - 删除重复 `setEvolutionTier(0)`。
  - 删除 `evolveTroop` 中重复的“兵种未解锁”判断分支。
  - 保持功能不变，减少噪音和后续维护风险。

## 第3轮验收通过条件
1. `/hall/recruit` 与 `/troop/recruit` 都会阻断未解锁兵种招募。
2. `TroopService.java` 无重复无效逻辑（至少清理上述两处）。
3. `agent任务完成表.md` 必须新增“第3轮补丁”条目，明确写出 F1/F2 的改动文件与自测结果。
