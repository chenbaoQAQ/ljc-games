-- =========================================================
-- LJC 三国群英传 · Schema V2.3 (含性格配置表)
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 清理旧表
DROP TABLE IF EXISTS skill_learn_log;
DROP TABLE IF EXISTS skill_book_map;
DROP TABLE IF EXISTS user_general_skill;
DROP TABLE IF EXISTS skill_template;

DROP TABLE IF EXISTS drop_pool;
DROP TABLE IF EXISTS tower_floor_config;
DROP TABLE IF EXISTS story_stage_config;
DROP TABLE IF EXISTS blueprint_template;
DROP TABLE IF EXISTS gem_template;
DROP TABLE IF EXISTS equipment_template;
DROP TABLE IF EXISTS troop_template;
DROP TABLE IF EXISTS general_template;
DROP TABLE IF EXISTS personality_config;

DROP TABLE IF EXISTS craft_history;
DROP TABLE IF EXISTS troop_ledger;
DROP TABLE IF EXISTS currency_ledger;
DROP TABLE IF EXISTS battle_sessions; -- 新增：战斗会话表
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

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 2. 用户与进度
-- =========================================================

CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(64) NOT NULL UNIQUE,
                       password_hash VARCHAR(128) NOT NULL,
                       nickname VARCHAR(64),
                       gold BIGINT NOT NULL DEFAULT 0,
                       diamond BIGINT NOT NULL DEFAULT 0,
                       stamina INT NOT NULL DEFAULT 0,
                       initial_civ VARCHAR(10) NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
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
-- 3. 资产：兵、武将、物品
-- =========================================================

CREATE TABLE user_troops (
                             user_id BIGINT NOT NULL,
                             troop_id INT NOT NULL,
                             count BIGINT NOT NULL DEFAULT 0,
                             PRIMARY KEY (user_id, troop_id),
                             INDEX idx_ut_user (user_id),
                             CHECK (count >= 0)
) ENGINE=InnoDB;

CREATE TABLE user_generals (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               template_id INT NOT NULL,
                               unlocked BOOLEAN NOT NULL DEFAULT FALSE,
                               activated BOOLEAN NOT NULL DEFAULT FALSE,
                               level INT NOT NULL DEFAULT 1,
                               tier INT NOT NULL DEFAULT 0,
                               current_hp BIGINT NOT NULL DEFAULT 0,
                               max_hp BIGINT NOT NULL DEFAULT 0,
                               rest_turns INT NOT NULL DEFAULT 0,
                               capacity INT NOT NULL DEFAULT 0,
                               equip_weapon_id BIGINT NOT NULL DEFAULT 0,
                               equip_armor1_id BIGINT NOT NULL DEFAULT 0,
                               equip_armor2_id BIGINT NOT NULL DEFAULT 0,
                               equip_shoes_id BIGINT NOT NULL DEFAULT 0,
                               equip_flag_id BIGINT NOT NULL DEFAULT 0,
                               equip_talisman_id BIGINT NOT NULL DEFAULT 0,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               INDEX idx_ug_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE user_equipments (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 template_id INT NOT NULL,
                                 enhance_level INT NOT NULL DEFAULT 0,
                                 socket1_gem_id BIGINT NOT NULL DEFAULT 0,
                                 socket2_gem_id BIGINT NOT NULL DEFAULT 0,
                                 is_equipped BOOLEAN NOT NULL DEFAULT FALSE,
                                 is_locked BOOLEAN NOT NULL DEFAULT FALSE,
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 INDEX idx_ue_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE user_gems (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           gem_type VARCHAR(10) NOT NULL,
                           gem_level INT NOT NULL,
                           stat_value BIGINT NOT NULL,
                           is_used BOOLEAN NOT NULL DEFAULT FALSE,
                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           INDEX idx_ugem_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE user_inventory (
                                user_id BIGINT NOT NULL,
                                item_id INT NOT NULL,
                                count BIGINT NOT NULL DEFAULT 0,
                                PRIMARY KEY (user_id, item_id),
                                CHECK (count >= 0)
) ENGINE=InnoDB;

-- =========================================================
-- 4. 战斗与日志
-- =========================================================

CREATE TABLE battle_sessions (
                                 user_id BIGINT PRIMARY KEY,
                                 battle_id BIGINT NOT NULL,
                                 current_turn INT NOT NULL DEFAULT 1,
                                 context_json JSON NOT NULL,
                                 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

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
                            result VARCHAR(10) NOT NULL,
                            troop_config_json JSON NOT NULL,
                            calc_log_json JSON NOT NULL,
                            troop_result_json JSON NOT NULL,
                            reward_json JSON NOT NULL,
                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            INDEX idx_battle_user_time (user_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE currency_ledger (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 currency_type VARCHAR(10) NOT NULL,
                                 delta BIGINT NOT NULL,
                                 before_value BIGINT NOT NULL,
                                 after_value BIGINT NOT NULL,
                                 reason VARCHAR(32) NOT NULL,
                                 ref_id VARCHAR(64) DEFAULT NULL,
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE troop_ledger (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              troop_id INT NOT NULL,
                              delta BIGINT NOT NULL,
                              before_value BIGINT NOT NULL,
                              after_value BIGINT NOT NULL,
                              reason VARCHAR(32) NOT NULL,
                              ref_id VARCHAR(64) DEFAULT NULL,
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE craft_history (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               blueprint_item_id INT NOT NULL,
                               success BOOLEAN NOT NULL,
                               produced_equipment_id BIGINT NOT NULL DEFAULT 0,
                               seed BIGINT NOT NULL,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =========================================================
-- 5. 技能系统
-- =========================================================

CREATE TABLE skill_template (
                                skill_id INT PRIMARY KEY,
                                name VARCHAR(32) NOT NULL,
                                description VARCHAR(255) NOT NULL,
                                skill_type VARCHAR(16) NOT NULL,
                                trigger_timing VARCHAR(32) NOT NULL DEFAULT 'NONE',
                                effect_json JSON NOT NULL,
                                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE user_general_skill (
                                    general_id BIGINT PRIMARY KEY,
                                    current_skill_id INT NOT NULL,
                                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE skill_book_map (
                                item_id INT PRIMARY KEY,
                                skill_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE skill_learn_log (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 general_id BIGINT NOT NULL,
                                 old_skill_id INT NOT NULL,
                                 new_skill_id INT NOT NULL,
                                 book_item_id INT NOT NULL,
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =========================================================
-- 6. 配置表 (Config)
-- =========================================================

CREATE TABLE personality_config (
                                    personality_code VARCHAR(20) PRIMARY KEY,
                                    display_name VARCHAR(32) NOT NULL,
                                    deal_mult INT NOT NULL DEFAULT 1000,
                                    taken_mult INT NOT NULL DEFAULT 1000,
                                    rescue_rate_bonus INT NOT NULL DEFAULT 0,
                                    last_stand_bias INT NOT NULL DEFAULT 0,
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
                                  personality_code VARCHAR(20) NOT NULL,
                                  activate_gold_cost BIGINT NOT NULL DEFAULT 0,
                                  activate_diamond_cost BIGINT NOT NULL DEFAULT 0,
                                  max_level_tier0 INT NOT NULL DEFAULT 20,
                                  default_skill_id INT NOT NULL DEFAULT 0,
                                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_gt_personality FOREIGN KEY (personality_code) REFERENCES personality_config(personality_code)
) ENGINE=InnoDB;

CREATE TABLE troop_template (
                                troop_id INT PRIMARY KEY,
                                civ VARCHAR(10) NOT NULL,
                                name VARCHAR(32) NOT NULL,
                                troop_type VARCHAR(10) NOT NULL,
                                is_elite BOOLEAN NOT NULL DEFAULT FALSE,
                                cost INT NOT NULL,
                                base_atk BIGINT NOT NULL,
                                base_hp BIGINT NOT NULL,
                                recruit_gold_cost BIGINT NOT NULL DEFAULT 0,
                                unlock_civ_required BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

CREATE TABLE equipment_template (
                                    template_id INT PRIMARY KEY,
                                    slot VARCHAR(12) NOT NULL,
                                    name VARCHAR(32) NOT NULL,
                                    base_atk BIGINT NOT NULL DEFAULT 0,
                                    base_hp BIGINT NOT NULL DEFAULT 0,
                                    base_spd BIGINT NOT NULL DEFAULT 0,
                                    base_capacity INT NOT NULL DEFAULT 0,
                                    aura_scope_type VARCHAR(16) DEFAULT NULL,
                                    aura_scope_value VARCHAR(16) DEFAULT NULL,
                                    aura_stat VARCHAR(16) DEFAULT NULL,
                                    aura_base_value INT DEFAULT NULL,
                                    aura_growth_per_enhance INT DEFAULT NULL,
                                    enhance_growth_json JSON NOT NULL,
                                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE gem_template (
                              gem_type VARCHAR(10) NOT NULL,
                              gem_level INT NOT NULL,
                              stat_value BIGINT NOT NULL,
                              PRIMARY KEY (gem_type, gem_level)
) ENGINE=InnoDB;

CREATE TABLE blueprint_template (
                                    blueprint_item_id INT PRIMARY KEY,
                                    equipment_template_id INT NOT NULL,
                                    success_rate INT NOT NULL,
                                    gold_cost BIGINT NOT NULL DEFAULT 0,
                                    materials_json JSON NOT NULL
) ENGINE=InnoDB;

CREATE TABLE story_stage_config (
                                    civ VARCHAR(10) NOT NULL,
                                    stage_no INT NOT NULL,
                                    stage_type VARCHAR(10) NOT NULL,
                                    stamina_cost INT NOT NULL DEFAULT 0,
                                    wall_cost_troops INT NOT NULL DEFAULT 0,
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
                                    stamina_cost INT NOT NULL DEFAULT 0,
                                    enemy_multiplier INT NOT NULL DEFAULT 1000,
                                    enemy_troop_comp_json JSON NOT NULL,
                                    drop_pool_id INT NOT NULL
) ENGINE=InnoDB;

CREATE DATABASE ljc_sanguo CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;写·