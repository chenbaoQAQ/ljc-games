-- ---------------------------------------------------------
-- 1. 彻底清理旧环境 (严格按外键逆序删除)
-- ---------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS equipment;
DROP TABLE IF EXISTS army_slot;
DROP TABLE IF EXISTS user_general;
DROP TABLE IF EXISTS user_profile;
DROP TABLE IF EXISTS general_template;
DROP TABLE IF EXISTS unit_config;
DROP TABLE IF EXISTS stage_config;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------
-- 2. 核心基础表 (配置数据)
-- ---------------------------------------------------------

-- 兵种配置 (个位数平衡版)
CREATE TABLE unit_config (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             unit_name VARCHAR(50) NOT NULL UNIQUE,
                             space_cost INT NOT NULL DEFAULT 1,
                             base_atk INT NOT NULL DEFAULT 1,
                             base_hp INT NOT NULL DEFAULT 1,
                             target_type VARCHAR(20) DEFAULT 'NONE',
                             buff_ratio DECIMAL(3,2) DEFAULT 1.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 武将模版 (兑换制)
CREATE TABLE general_template (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  name VARCHAR(50) NOT NULL,
                                  rarity VARCHAR(10) NOT NULL,
                                  base_leadership INT NOT NULL DEFAULT 100 COMMENT '基础带兵上限',
                                  country VARCHAR(20) NOT NULL COMMENT '所属国家'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 关卡配置 (增强版：含掉落率与金/钻奖励)
CREATE TABLE stage_config (
                              id INT PRIMARY KEY,
                              region_id INT NOT NULL,
                              region_name VARCHAR(50) NOT NULL,
                              has_wall BOOLEAN DEFAULT FALSE,
                              is_boss BOOLEAN DEFAULT FALSE,
                              wall_cost INT DEFAULT 0,
                              enemy_atk_buff DECIMAL(3,2) DEFAULT 1.00,
                              enemy_base_hp INT DEFAULT 1000,
                              gold_reward INT DEFAULT 200,
                              diamond_reward INT DEFAULT 5,
                              loot_rate DECIMAL(3,2) DEFAULT 0.10 COMMENT '抢夺装备概率'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- 3. 玩家数据表 (业务实例)
-- ---------------------------------------------------------

-- 玩家主存档 (财富与进度)
CREATE TABLE user_profile (
                              user_id INT PRIMARY KEY,
                              gold INT DEFAULT 1000,
                              diamond INT DEFAULT 0,
                              unlocked_countries VARCHAR(100) DEFAULT 'NONE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 玩家武将
CREATE TABLE user_general (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              user_id INT NOT NULL,
                              template_id INT NOT NULL,
                              personality VARCHAR(20) NOT NULL,
                              CONSTRAINT fk_gen_template FOREIGN KEY (template_id) REFERENCES general_template(id),
                              CONSTRAINT fk_gen_user FOREIGN KEY (user_id) REFERENCES user_profile(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 装备系统 (核心变动：等级与经验)
CREATE TABLE equipment (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           user_id INT NOT NULL,
                           equip_type ENUM('WEAPON', 'ARMOR', 'TALLY') NOT NULL,
                           equip_name VARCHAR(50) NOT NULL,
                           level INT DEFAULT 1,
                           current_exp INT DEFAULT 0,
                           atk_bonus INT DEFAULT 0,
                           hp_bonus INT DEFAULT 0,
                           leadership_bonus INT DEFAULT 0,
                           owner_general_id INT DEFAULT NULL,
                           CONSTRAINT fk_equip_user FOREIGN KEY (user_id) REFERENCES user_profile(user_id),
                           CONSTRAINT fk_equip_gen FOREIGN KEY (owner_general_id) REFERENCES user_general(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- 4. 初始数据填充
-- ---------------------------------------------------------

-- 兵种初始化
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp, target_type, buff_ratio) VALUES
                                                                                                ('INFANTRY', 1, 1, 2, 'NONE', 1.0),
                                                                                                ('ARCHER', 1, 2, 1, 'NONE', 1.0),
                                                                                                ('CAVALRY', 2, 3, 3, 'NONE', 1.0),
                                                                                                ('CN_SPECIAL', 3, 2, 4, 'INFANTRY', 2.0),
                                                                                                ('EN_SPECIAL', 3, 2, 4, 'HERO', 0.2);

-- 武将模版初始化 (每个国家 5 名，先做 2 个样板)
INSERT INTO general_template (name, rarity, base_leadership, country) VALUES
                                                                          ('赵云', 'SSR', 120, 'CN'),
                                                                          ('关羽', 'SSR', 150, 'CN');

-- 自动化生成 30 关 (带奖励梯度)
SET @i = 0;
INSERT INTO stage_config (id, region_id, region_name, has_wall, is_boss, wall_cost, enemy_atk_buff, enemy_base_hp, gold_reward, loot_rate)
SELECT
    @i := @i + 1 AS id,
    FLOOR((@i - 1) / 3) + 1 AS region_id,
    CONCAT('Region-', FLOOR((@i - 1) / 3) + 1) AS region_name,
    CASE WHEN @i % 10 = 1 THEN TRUE ELSE FALSE END AS has_wall,
    CASE WHEN @i % 10 = 0 THEN TRUE ELSE FALSE END AS is_boss,
    CASE WHEN @i % 10 = 1 THEN 100 ELSE 0 END AS wall_cost,
    CASE WHEN @i % 10 = 0 THEN 1.20 ELSE 1.00 END AS enemy_atk_buff,
    (@i * 200) AS enemy_base_hp,
    CASE WHEN @i = 1 THEN 100 WHEN @i = 30 THEN 500 ELSE 200 END AS gold_reward,
    CASE WHEN @i % 10 = 0 THEN 0.50 ELSE 0.10 END AS loot_rate -- BOSS关掉率更高
FROM
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) AS t1,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) AS t2
LIMIT 30;

-- 创建一个初始玩家存档用于测试
INSERT INTO user_profile (user_id, gold, diamond) VALUES (1, 1000, 0);