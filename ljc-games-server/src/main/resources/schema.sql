-- =========================================================
-- LJC 三国群英传 · Schema V2.3（对齐当前企划：大厅优先 + 回合会话）
-- 约定：所有 *_id = 0 表示空（不加外键约束，业务层校验归属与存在性）
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 0. 清表（顺序按依赖从运行时 -> 配置）
-- =========================
DROP TABLE IF EXISTS skill_learn_log;
DROP TABLE IF EXISTS skill_book_map;
DROP TABLE IF EXISTS user_general_skill;
DROP TABLE IF EXISTS skill_template;

DROP TABLE IF EXISTS craft_history;
DROP TABLE IF EXISTS troop_ledger;
DROP TABLE IF EXISTS currency_ledger;

DROP TABLE IF EXISTS battle_turn_log;
DROP TABLE IF EXISTS battle_sessions;
DROP TABLE IF EXISTS battle_log;

DROP TABLE IF EXISTS user_inventory;
DROP TABLE IF EXISTS user_gems;
DROP TABLE IF EXISTS user_equipments;
DROP TABLE IF EXISTS user_troops;
DROP TABLE IF EXISTS user_generals;

DROP TABLE IF EXISTS user_flags;
DROP TABLE IF EXISTS user_tower_progress;
DROP TABLE IF EXISTS user_civ_progress;
DROP TABLE IF EXISTS users;

DROP TABLE IF EXISTS tower_floor_config;
DROP TABLE IF EXISTS story_stage_config;
DROP TABLE IF EXISTS drop_pool;

DROP TABLE IF EXISTS blueprint_template;
DROP TABLE IF EXISTS gem_template;
DROP TABLE IF EXISTS equipment_template;
DROP TABLE IF EXISTS troop_template;
DROP TABLE IF EXISTS general_template;
DROP TABLE IF EXISTS personality_config;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1. 用户与进度
-- =========================================================

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(128) NOT NULL,
  nickname VARCHAR(64),

  gold BIGINT NOT NULL DEFAULT 0,
  diamond BIGINT NOT NULL DEFAULT 0,

  initial_civ VARCHAR(10) NOT NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_users_created (created_at)
) ENGINE=InnoDB;

CREATE TABLE user_civ_progress (
  user_id BIGINT NOT NULL,
  civ VARCHAR(10) NOT NULL,
  unlocked BOOLEAN NOT NULL DEFAULT FALSE,
  max_stage_cleared INT NOT NULL DEFAULT 0,

  PRIMARY KEY (user_id, civ),
  INDEX idx_ucp_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE user_tower_progress (
  user_id BIGINT PRIMARY KEY,
  unlocked BOOLEAN NOT NULL DEFAULT FALSE,
  max_floor_cleared INT NOT NULL DEFAULT 0,
  season INT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE user_flags (
  user_id BIGINT PRIMARY KEY,
  ascension_token_used BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =========================================================
-- 2. 资产：兵、武将、装备、宝石、背包
-- =========================================================

CREATE TABLE user_troops (
  user_id BIGINT NOT NULL,
  troop_id INT NOT NULL,
  count BIGINT NOT NULL DEFAULT 0,

  PRIMARY KEY (user_id, troop_id),
  INDEX idx_ut_user (user_id),
  INDEX idx_ut_troop (troop_id)
) ENGINE=InnoDB;

CREATE TABLE user_generals (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  template_id INT NOT NULL,

  unlocked BOOLEAN NOT NULL DEFAULT FALSE,
  activated BOOLEAN NOT NULL DEFAULT FALSE,

  level INT NOT NULL DEFAULT 1,      -- 无经验，金币升级（受 tier 限制）
  tier INT NOT NULL DEFAULT 0,       -- 升阶

  current_hp BIGINT NOT NULL DEFAULT 0,
  max_hp BIGINT NOT NULL DEFAULT 0,

  rest_turns INT NOT NULL DEFAULT 0, -- 战后休息回合数，>0 不可出战
  capacity INT NOT NULL DEFAULT 0,   -- 统帅上限（空间）

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_ug_user (user_id),
  INDEX idx_ug_user_tpl (user_id, template_id)
) ENGINE=InnoDB;

CREATE TABLE user_equipments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  template_id INT NOT NULL,

  enhance_level INT NOT NULL DEFAULT 0,   -- 0~8，默认全成功（不做概率）

  socket1_gem_id BIGINT NOT NULL DEFAULT 0,
  socket2_gem_id BIGINT NOT NULL DEFAULT 0,

  general_id BIGINT DEFAULT NULL,         -- 穿戴在哪个武将身上 (NULL=未穿戴)
  slot VARCHAR(12) DEFAULT NULL,          -- 穿戴在哪个槽位 (NULL=未穿戴)

  is_locked BOOLEAN NOT NULL DEFAULT FALSE,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_ue_user (user_id),
  INDEX idx_ue_tpl (template_id)
) ENGINE=InnoDB;

CREATE TABLE user_gems (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,

  gem_type VARCHAR(10) NOT NULL,     -- ATK / HP（后续可扩）
  gem_level INT NOT NULL,            -- 1~5
  stat_value BIGINT NOT NULL,        -- 冗余存值（从 gem_template 来，方便快速读）
  is_used BOOLEAN NOT NULL DEFAULT FALSE, -- 镶嵌后 true（不可拆）

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_ugem_user (user_id),
  INDEX idx_ugem_query (user_id, gem_type, gem_level, is_used)
) ENGINE=InnoDB;

CREATE TABLE user_inventory (
  user_id BIGINT NOT NULL,
  item_id INT NOT NULL,
  count BIGINT NOT NULL DEFAULT 0,

  PRIMARY KEY (user_id, item_id),
  INDEX idx_inv_user (user_id),
  INDEX idx_inv_item (item_id)
) ENGINE=InnoDB;

-- =========================================================
-- 3. 战斗：会话（进行中）+ 归档（结束）+（可选）回合日志
-- =========================================================

-- 进行中会话：一个玩家同一时刻只允许 1 场进行中的战斗（符合你的游戏）
CREATE TABLE battle_sessions (
  user_id BIGINT PRIMARY KEY,

  battle_id BIGINT NOT NULL,                -- 本场战斗唯一标识（可用雪花/自增/时间戳）
  status VARCHAR(12) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / FINISHED / ABORTED

  mode VARCHAR(10) NOT NULL,                -- STORY / TOWER
  civ VARCHAR(10) DEFAULT NULL,
  stage_no INT DEFAULT NULL,
  tower_floor INT DEFAULT NULL,

  general_id BIGINT NOT NULL,
  seed BIGINT NOT NULL,

  current_turn INT NOT NULL DEFAULT 1,      -- 当前回合（从1开始）
  max_turn INT NOT NULL DEFAULT 20,         -- 最大回合（默认20，可配）

  -- 不可变上下文：配兵、关卡、初始属性快照等
  context_json JSON NOT NULL,
  -- 可变状态：当前HP、撤退标记、技能CD、兵存活等
  state_json JSON NOT NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_bs_status (status),
  INDEX idx_bs_battle (battle_id)
) ENGINE=InnoDB;

-- 可选：如果你希望未来复盘每回合事件（本期不强制实现）
CREATE TABLE battle_turn_log (
  battle_id BIGINT NOT NULL,
  turn_no INT NOT NULL,
  events_json JSON NOT NULL,      -- 本回合事件（技能释放/roll/伤害/撤退等）
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (battle_id, turn_no)
) ENGINE=InnoDB;

-- 结束归档：只在战斗结束写一次
CREATE TABLE battle_log (
  battle_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,

  mode VARCHAR(10) NOT NULL,
  civ VARCHAR(10) DEFAULT NULL,
  stage_no INT DEFAULT NULL,
  tower_floor INT DEFAULT NULL,

  general_id BIGINT NOT NULL,
  seed BIGINT NOT NULL,
  turn_count INT NOT NULL DEFAULT 0,

  result VARCHAR(10) NOT NULL,  -- WIN/LOSE/DRAW/ABORT

  troop_config_json JSON NOT NULL,
  calc_log_json JSON NOT NULL,        -- 结算摘要（不是每回合全量）
  troop_result_json JSON NOT NULL,
  reward_json JSON NOT NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_battle_user_time (user_id, created_at)
) ENGINE=InnoDB;

-- =========================================================
-- 4. 台账：金币/钻石、兵力变化、合成记录
-- =========================================================

CREATE TABLE currency_ledger (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,

  currency_type VARCHAR(10) NOT NULL, -- GOLD/DIAMOND/STAMINA
  delta BIGINT NOT NULL,
  before_value BIGINT NOT NULL,
  after_value BIGINT NOT NULL,

  reason VARCHAR(32) NOT NULL,        -- RECRUIT / UPGRADE / ENHANCE / CRAFT / REWARD ...
  ref_id VARCHAR(64) DEFAULT NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_cur_user_time (user_id, created_at),
  INDEX idx_cur_reason (reason)
) ENGINE=InnoDB;

CREATE TABLE troop_ledger (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  troop_id INT NOT NULL,

  delta BIGINT NOT NULL,
  before_value BIGINT NOT NULL,
  after_value BIGINT NOT NULL,

  reason VARCHAR(32) NOT NULL,    -- RECRUIT / BATTLE_DEDUCT / BATTLE_RETURN ...
  ref_id VARCHAR(64) DEFAULT NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tr_user_time (user_id, created_at),
  INDEX idx_tr_troop (troop_id)
) ENGINE=InnoDB;

-- 设计图合成：成功率在 blueprint_template
CREATE TABLE craft_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,

  blueprint_item_id INT NOT NULL,
  success BOOLEAN NOT NULL,
  produced_equipment_id BIGINT NOT NULL DEFAULT 0,

  seed BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_craft_user_time (user_id, created_at)
) ENGINE=InnoDB;

-- =========================================================
-- 5. 技能系统（主动/被动 + CD 回合 + 技能书覆盖）
-- =========================================================

CREATE TABLE skill_template (
  skill_id INT PRIMARY KEY,
  name VARCHAR(32) NOT NULL,
  description VARCHAR(255) NOT NULL,

  skill_type VARCHAR(16) NOT NULL,       -- ACTIVE / PASSIVE
  cooldown_turns INT NOT NULL DEFAULT 0, -- 主动技能CD（回合），被动为0

  trigger_timing VARCHAR(32) NOT NULL DEFAULT 'NONE', -- 被动触发点：TURN_START/ON_HIT/ON_KILL...
  effect_json JSON NOT NULL,             -- 技能效果配置（后端暂不解析也行）

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 每个武将只有一个技能槽：学习技能书会覆盖 old -> new
CREATE TABLE user_general_skill (
  general_id BIGINT PRIMARY KEY,
  current_skill_id INT NOT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE skill_book_map (
  item_id INT PRIMARY KEY,   -- 背包道具id（技能书）
  skill_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE skill_learn_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  general_id BIGINT NOT NULL,
  old_skill_id INT NOT NULL,
  new_skill_id INT NOT NULL,
  book_item_id INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_sll_user_time (user_id, created_at)
) ENGINE=InnoDB;

-- =========================================================
-- 6. 配置表（策划表：只读）
-- =========================================================

CREATE TABLE personality_config (
  personality_code VARCHAR(20) PRIMARY KEY,
  display_name VARCHAR(32) NOT NULL,

  -- 伤害/承伤倍率：1000=1.0
  deal_mult INT NOT NULL DEFAULT 1000,
  taken_mult INT NOT NULL DEFAULT 1000,

  -- 战后救治加成：例如 +200 表示 +20%（具体解释由后端实现）
  rescue_rate_bonus INT NOT NULL DEFAULT 0,

  -- 死战倾向：>0 更倾向死战，<0 更倾向撤退
  last_stand_bias INT NOT NULL DEFAULT 0,

  -- “打将比例 roll”偏置：影响每兵种 roll 的结果（可选实现）
  roll_bias INT NOT NULL DEFAULT 0,
  roll_variance_scale INT NOT NULL DEFAULT 1000,

  note VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB;

CREATE TABLE general_template (
  template_id INT PRIMARY KEY,
  civ VARCHAR(10) NOT NULL,
  name VARCHAR(32) NOT NULL,

  base_atk BIGINT NOT NULL,
  base_hp BIGINT NOT NULL,
  base_capacity INT NOT NULL,

  speed INT NOT NULL DEFAULT 50,        -- 速度（你自己补的需求）

  personality_code VARCHAR(20) NOT NULL,

  activate_gold_cost BIGINT NOT NULL DEFAULT 0,
  activate_diamond_cost BIGINT NOT NULL DEFAULT 0,

  max_level_tier0 INT NOT NULL DEFAULT 20,
  default_skill_id INT NOT NULL DEFAULT 0,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_gt_civ (civ),
  INDEX idx_gt_personality (personality_code)
) ENGINE=InnoDB;

CREATE TABLE troop_template (
  troop_id INT PRIMARY KEY,
  civ VARCHAR(10) NOT NULL,
  name VARCHAR(32) NOT NULL,

  troop_type VARCHAR(10) NOT NULL,          -- INF/ARC/CAV/ELITE...
  is_elite BOOLEAN NOT NULL DEFAULT FALSE,

  cost INT NOT NULL,                        -- 空间占用：1/2
  base_atk BIGINT NOT NULL,
  base_hp BIGINT NOT NULL,

  recruit_gold_cost BIGINT NOT NULL DEFAULT 0,
  unlock_civ_required BOOLEAN NOT NULL DEFAULT FALSE,

  INDEX idx_tt_civ (civ),
  INDEX idx_tt_type (troop_type)
) ENGINE=InnoDB;

CREATE TABLE equipment_template (
  template_id INT PRIMARY KEY,
  slot VARCHAR(12) NOT NULL,                -- WEAPON/ARMOR/SHOES/FLAG/TALISMAN
  name VARCHAR(32) NOT NULL,

  base_atk BIGINT NOT NULL DEFAULT 0,
  base_hp BIGINT NOT NULL DEFAULT 0,
  base_spd BIGINT NOT NULL DEFAULT 0,
  base_capacity INT NOT NULL DEFAULT 0,

  -- 旗子光环（后端可先不实现，只存配置）
  aura_scope_type VARCHAR(16) DEFAULT NULL,
  aura_scope_value VARCHAR(16) DEFAULT NULL,
  aura_stat VARCHAR(16) DEFAULT NULL,
  aura_base_value INT DEFAULT NULL,
  aura_growth_per_enhance INT DEFAULT NULL,

  enhance_growth_json JSON NOT NULL,        -- 强化成长
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_et_slot (slot)
) ENGINE=InnoDB;

CREATE TABLE gem_template (
  gem_type VARCHAR(10) NOT NULL,
  gem_level INT NOT NULL,
  stat_value BIGINT NOT NULL,
  PRIMARY KEY (gem_type, gem_level)
) ENGINE=InnoDB;

-- blueprint_item_id：背包中的“设计图道具id”
CREATE TABLE blueprint_template (
  blueprint_item_id INT PRIMARY KEY,
  equipment_template_id INT NOT NULL,
  success_rate INT NOT NULL,                -- 0~10000（建议万分比）或 0~100（百分比），你自行定口径
  gold_cost BIGINT NOT NULL DEFAULT 0,
  materials_json JSON NOT NULL
) ENGINE=InnoDB;

CREATE TABLE story_stage_config (
  civ VARCHAR(10) NOT NULL,
  stage_no INT NOT NULL,
  stage_type VARCHAR(10) NOT NULL,          -- NORMAL/WALL/BOSS
  wall_cost_troops INT NOT NULL DEFAULT 0,  -- 城墙关额外扣兵（例如100）
  enemy_multiplier INT NOT NULL DEFAULT 1000,

  enemy_troop_comp_json JSON NOT NULL,
  drop_pool_id INT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (civ, stage_no)
) ENGINE=InnoDB;

CREATE TABLE drop_pool (
  pool_id INT PRIMARY KEY,
  entries_json JSON NOT NULL
) ENGINE=InnoDB;

CREATE TABLE tower_floor_config (
  floor_no INT PRIMARY KEY,
  floor_type VARCHAR(10) NOT NULL,
  enemy_multiplier INT NOT NULL DEFAULT 1000,

  enemy_troop_comp_json JSON NOT NULL,
  drop_pool_id INT NOT NULL
) ENGINE=InnoDB;
