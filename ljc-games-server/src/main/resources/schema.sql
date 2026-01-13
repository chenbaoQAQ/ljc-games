-- 彻底清理
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS equipment, user_general, user_profile, general_template, unit_config, stage_config;
SET FOREIGN_KEY_CHECKS = 1;

-- 兵种配置 (个位数平衡版)
CREATE TABLE unit_config (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             unit_name VARCHAR(50) NOT NULL UNIQUE,
                             space_cost INT NOT NULL DEFAULT 1,
                             base_atk INT NOT NULL DEFAULT 1,
                             base_hp INT NOT NULL DEFAULT 1,
                             target_type VARCHAR(20) DEFAULT 'NONE',
                             buff_ratio DECIMAL(3,2) DEFAULT 1.00
) ENGINE=InnoDB;

-- 武将模版
CREATE TABLE general_template (
                                  id INT PRIMARY KEY,
                                  name VARCHAR(50) NOT NULL,
                                  rarity VARCHAR(10) NOT NULL,
                                  base_atk INT DEFAULT 50,
                                  base_hp INT DEFAULT 1000,
                                  base_leadership INT NOT NULL DEFAULT 100 COMMENT '带兵上限',
                                  country VARCHAR(20) NOT NULL
) ENGINE=InnoDB;

-- 玩家存档
CREATE TABLE user_profile (
                              user_id INT PRIMARY KEY,
                              gold INT DEFAULT 8000,
                              diamond INT DEFAULT 500,
                              unlocked_countries VARCHAR(100) DEFAULT ''
) ENGINE=InnoDB;

-- 玩家武将 (修正版)
CREATE TABLE user_general (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              user_id INT NOT NULL,
                              template_id INT NOT NULL,
                              name VARCHAR(50),
                              personality VARCHAR(20),
                              level INT DEFAULT 1,
                              current_exp INT DEFAULT 0,
                              max_hp INT DEFAULT 1000,
                              current_hp INT DEFAULT 1000,
                              base_atk INT DEFAULT 50,
                              base_hp INT DEFAULT 1000,
                              max_leadership INT DEFAULT 100, -- 带兵上限
                              current_army_count INT DEFAULT 0, -- 当前已占用空间
                              status VARCHAR(20) DEFAULT 'HEALTHY',
                              army_config_str TEXT,         -- 阵前兵力 (战斗时扣除)
                              reserve_army_config_str TEXT, -- 仓库兵力 (招募后进入此处)
                              CONSTRAINT fk_gen_user FOREIGN KEY (user_id) REFERENCES user_profile(user_id)
) ENGINE=InnoDB;

-- 初始填充
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp) VALUES
                                                                       ('INFANTRY', 1, 15, 120), ('ARCHER', 1, 22, 90), ('CAVALRY', 2, 45, 250),
                                                                       ('CN_SPECIAL', 2, 35, 450), ('JP_SPECIAL', 2, 55, 180), ('KR_SPECIAL', 3, 65, 600), ('GB_SPECIAL', 2, 40, 350);

INSERT INTO general_template (id, name, rarity, base_leadership, country) VALUES
                                                                              (101, '赵云', 'SSR', 120, 'CN'), (102, '吕布', 'UR', 150, 'CN');

INSERT INTO user_profile (user_id, gold, diamond) VALUES (1, 8000, 500);