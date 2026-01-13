-- ---------------------------------------------------------
-- 1. 玩家存档：增加金币、钻石、解锁进度
-- ---------------------------------------------------------
CREATE TABLE user_profile (
                              user_id INT PRIMARY KEY,
                              gold INT DEFAULT 1000 COMMENT '基础金币',
                              diamond INT DEFAULT 0 COMMENT '首通获得的钻石',
                              unlocked_countries VARCHAR(100) DEFAULT 'NONE' COMMENT '已解锁的国家,如:CN,EN'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- 2. 装备系统：武器、防具、兵符 (支持等级与经验)
-- ---------------------------------------------------------
CREATE TABLE equipment (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           user_id INT NOT NULL,
                           equip_type ENUM('WEAPON', 'ARMOR', 'TALLY') NOT NULL,
                           equip_name VARCHAR(50),
                           level INT DEFAULT 1,
                           current_exp INT DEFAULT 0,
    -- 对应属性加成
                           atk_bonus INT DEFAULT 0,
                           hp_bonus INT DEFAULT 0,
                           leadership_bonus INT DEFAULT 0,
    -- 装备在哪位武将身上 (可为空)
                           owner_general_id INT,
                           CONSTRAINT fk_equip_user FOREIGN KEY (user_id) REFERENCES user_profile(user_id),
                           CONSTRAINT fk_equip_gen FOREIGN KEY (owner_general_id) REFERENCES user_general(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- 3. 关卡表增强：增加掉落率与金币/钻石奖励
-- ---------------------------------------------------------
ALTER TABLE stage_config
    ADD COLUMN gold_reward INT DEFAULT 200,
ADD COLUMN diamond_reward INT DEFAULT 5,
ADD COLUMN loot_rate DECIMAL(3,2) DEFAULT 0.10 COMMENT '从对方身上抢到装备的概率';