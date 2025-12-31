-- Unit Configuration Table (兵种基础配置表)
CREATE TABLE IF NOT EXISTS unit_config (
                                           id INT AUTO_INCREMENT PRIMARY KEY,
                                           unit_name VARCHAR(50) NOT NULL COMMENT 'Unique identifier for unit type (e.g., INFANTRY)',
    space_cost INT NOT NULL DEFAULT 1 COMMENT 'Required capacity per unit (刀1/骑2/特3)',
    base_atk INT NOT NULL DEFAULT 1 COMMENT 'Basic attack power (数值控制在1-5之间)',
    base_hp INT NOT NULL DEFAULT 1 COMMENT 'Basic health points',
    target_type VARCHAR(20) DEFAULT 'NONE' COMMENT 'The unit type this special unit can buff (强化目标)',
    buff_ratio DECIMAL(3,2) DEFAULT 1.00 COMMENT 'Damage multiplier for the target (强化倍率)'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- General Template Table (武将模版表)
CREATE TABLE IF NOT EXISTS general_template (
                                                id INT AUTO_INCREMENT PRIMARY KEY,
                                                name VARCHAR(50) NOT NULL COMMENT 'General Name (e.g., Zhao Yun)',
    rarity VARCHAR(10) NOT NULL COMMENT 'Gacha rarity: SSR, UR, etc.',
    base_leadership INT NOT NULL DEFAULT 100 COMMENT 'Maximum troop capacity (统帅上限)',
    country VARCHAR(20) NOT NULL COMMENT 'Civilization origin (CN, JP, KR, EN)'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 玩家武将实例表
CREATE TABLE user_general (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              user_id INT NOT NULL,
                              template_id INT NOT NULL,
                              personality VARCHAR(20) NOT NULL,
                              level INT DEFAULT 1,
                              current_exp INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 阵容槽位表
CREATE TABLE army_slot (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           general_id INT NOT NULL,
                           unit_id INT NOT NULL,
                           unit_count INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;