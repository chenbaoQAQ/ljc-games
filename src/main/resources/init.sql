-- ---------------------------------------------------------
-- 数据库一键更新/重置脚本 (2025版)
-- ---------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS stage_config;
DROP TABLE IF EXISTS unit_config;
DROP TABLE IF EXISTS skill_config;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 兵种配置 (加入特种兵 target_type 逻辑)
CREATE TABLE unit_config (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             unit_name VARCHAR(50) NOT NULL UNIQUE,
                             space_cost INT NOT NULL DEFAULT 1,
                             base_atk INT NOT NULL DEFAULT 1,
                             base_hp INT NOT NULL DEFAULT 1,
                             target_type VARCHAR(20) DEFAULT 'NONE',
                             buff_ratio DECIMAL(3,2) DEFAULT 1.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp, target_type, buff_ratio) VALUES
                                                                                                ('INFANTRY', 1, 1, 2, 'NONE', 1.0),
                                                                                                ('ARCHER', 1, 2, 1, 'NONE', 1.0),
                                                                                                ('CAVALRY', 2, 3, 3, 'NONE', 1.0),
                                                                                                ('CN_SPECIAL', 3, 2, 4, 'INFANTRY', 2.0),
                                                                                                ('EN_SPECIAL', 3, 3, 4, 'HERO', 0.2);

-- 2. 关卡配置 (30关带城墙、BOSS与倍率)
CREATE TABLE stage_config (
                              id INT PRIMARY KEY,
                              region_id INT NOT NULL,
                              region_name VARCHAR(50) NOT NULL,
                              has_wall BOOLEAN DEFAULT FALSE,
                              is_boss BOOLEAN DEFAULT FALSE,
                              wall_cost INT DEFAULT 0,
                              enemy_atk_buff DECIMAL(3,2) DEFAULT 1.00,
                              enemy_base_hp INT DEFAULT 1000 COMMENT '这一关敌人的基础血量/战力'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 批量生成 30 关逻辑
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
    (@i * 200) AS enemy_base_hp -- 随着关卡增加，敌人越来越强
FROM
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) AS t1,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) AS t2
LIMIT 30;

-- 3. 预留技能配置表 (Skill Table)
CREATE TABLE skill_config (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              skill_name VARCHAR(50) NOT NULL,
                              trigger_round INT DEFAULT 1 COMMENT '第几回合触发',
                              effect_value DECIMAL(5,2) DEFAULT 0.00,
                              description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;