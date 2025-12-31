-- ---------------------------------------------------------
-- 1. 清理旧表 (严格按照从属关系逆序删除)
-- ---------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0; -- 暂时关闭检查以便暴力清理
DROP TABLE IF EXISTS army_slot;
DROP TABLE IF EXISTS user_general;
DROP TABLE IF EXISTS general_template;
DROP TABLE IF EXISTS unit_config;
DROP TABLE IF EXISTS stage_config;
DROP TABLE IF EXISTS skill_config;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------
-- 2. 基础配置表 (主表)
-- ---------------------------------------------------------
CREATE TABLE unit_config (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             unit_name VARCHAR(50) NOT NULL UNIQUE,
                             space_cost INT NOT NULL DEFAULT 1,
                             base_atk INT NOT NULL DEFAULT 1,
                             base_hp INT NOT NULL DEFAULT 1,
                             target_type VARCHAR(20) DEFAULT 'NONE',
                             buff_ratio DECIMAL(3,2) DEFAULT 1.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE general_template (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  name VARCHAR(50) NOT NULL,
                                  rarity VARCHAR(10) NOT NULL,
                                  base_leadership INT NOT NULL DEFAULT 100,
                                  country VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- 3. 业务实例表 (从表，添加外键约束)
-- ---------------------------------------------------------
CREATE TABLE user_general (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              user_id INT NOT NULL,
                              template_id INT NOT NULL,
                              personality VARCHAR(20) NOT NULL,
                              level INT DEFAULT 1,
                              current_exp INT DEFAULT 0,
    -- 外键约束：template_id 必须引用自 general_template
                              CONSTRAINT fk_gen_template FOREIGN KEY (template_id)
                                  REFERENCES general_template(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE army_slot (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           general_id INT NOT NULL,
                           unit_id INT NOT NULL,
                           unit_count INT DEFAULT 0,
    -- 外键约束：关联武将和兵种
                           CONSTRAINT fk_slot_general FOREIGN KEY (general_id)
                               REFERENCES user_general(id) ON DELETE CASCADE,
                           CONSTRAINT fk_slot_unit FOREIGN KEY (unit_id)
                               REFERENCES unit_config(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- 4. 关卡配置 (逻辑同前)
-- ---------------------------------------------------------
CREATE TABLE stage_config (
                              id INT PRIMARY KEY,
                              region_id INT NOT NULL,
                              region_name VARCHAR(50) NOT NULL,
                              has_wall BOOLEAN DEFAULT FALSE,
                              is_boss BOOLEAN DEFAULT FALSE,
                              wall_cost INT DEFAULT 0,
                              enemy_atk_buff DECIMAL(3,2) DEFAULT 1.00,
                              enemy_base_hp INT DEFAULT 1000,
                              description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- 5. 插入初始数据 (注意插入顺序：先主后从)
-- ---------------------------------------------------------
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp, target_type, buff_ratio) VALUES
                                                                                                ('INFANTRY', 1, 1, 2, 'NONE', 1.0),
                                                                                                ('ARCHER', 1, 2, 1, 'NONE', 1.0),
                                                                                                ('CAVALRY', 2, 3, 3, 'NONE', 1.0),
                                                                                                ('CN_SPECIAL', 3, 2, 4, 'INFANTRY', 2.0),
                                                                                                ('EN_SPECIAL', 3, 2, 4, 'HERO', 0.2);

INSERT INTO general_template (name, rarity, base_leadership, country) VALUES
                                                                          ('赵云', 'SSR', 120, 'CN'),
                                                                          ('关羽', 'SSR', 150, 'CN');

-- 生成 30 关逻辑
SET @i = 0;
INSERT INTO stage_config (id, region_id, region_name, has_wall, is_boss, wall_cost, enemy_atk_buff, enemy_base_hp)
SELECT
    @i := @i + 1 AS id,
    FLOOR((@i - 1) / 3) + 1 AS region_id,
    CONCAT('Region-', FLOOR((@i - 1) / 3) + 1) AS region_name,
    CASE WHEN @i % 10 = 1 THEN TRUE ELSE FALSE END AS has_wall,
    CASE WHEN @i % 10 = 0 THEN TRUE ELSE FALSE END AS is_boss,
    CASE WHEN @i % 10 = 1 THEN 100 ELSE 0 END AS wall_cost,
    CASE WHEN @i % 10 = 0 THEN 1.20 ELSE 1.00 END AS enemy_atk_buff,
    (@i * 200) AS enemy_base_hp
FROM
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) AS t1,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) AS t2
LIMIT 30;