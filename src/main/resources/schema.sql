-- 1. 兵种配置表
CREATE TABLE unit_config (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             unit_name VARCHAR(50) NOT NULL,
                             space_cost INT NOT NULL DEFAULT 1,
                             base_atk INT NOT NULL DEFAULT 0,
                             base_hp INT NOT NULL DEFAULT 0,
                             target_type VARCHAR(20) DEFAULT 'NONE',
                             buff_ratio DECIMAL(3,2) DEFAULT 1.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 武将模板表
CREATE TABLE general_template (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  name VARCHAR(50) NOT NULL,
                                  rarity VARCHAR(10) NOT NULL,
                                  base_leadership INT NOT NULL DEFAULT 100,
                                  country VARCHAR(20) NOT NULL
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