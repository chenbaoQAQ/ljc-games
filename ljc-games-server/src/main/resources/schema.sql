-- 1. 兵种模版表 (存放配置：步兵攻击力10，骑兵血量50...)
CREATE TABLE IF NOT EXISTS troop_template (
                                              troop_id INT PRIMARY KEY,
                                              name VARCHAR(32) NOT NULL,
                                              civ VARCHAR(4) NOT NULL, -- CN, JP, KR, GB
                                              type VARCHAR(16) NOT NULL, -- INFANTRY, ARCHER, CAVALRY, SPECIAL
                                              is_elite BOOLEAN DEFAULT FALSE,
                                              base_atk INT NOT NULL,
                                              base_hp INT NOT NULL,
                                              cost BIGINT NOT NULL -- 招募价格
);

-- 2. 玩家兵力库存表 (存放：张三有100个步兵)
CREATE TABLE IF NOT EXISTS user_troops (
                                           user_id BIGINT NOT NULL,
                                           troop_id INT NOT NULL,
                                           count BIGINT NOT NULL,
                                           PRIMARY KEY (user_id, troop_id)
);

-- 3. 预设一点测试数据 (CN: 步弓骑 + 特种)
INSERT IGNORE INTO troop_template (troop_id, name, civ, type, is_elite, base_atk, base_hp, cost) VALUES
                                                                                                     (101, '民兵', 'CN', 'INFANTRY', false, 10, 100, 10),
                                                                                                     (102, '弓箭手', 'CN', 'ARCHER', false, 20, 50, 15),
                                                                                                     (103, '轻骑兵', 'CN', 'CAVALRY', false, 15, 80, 20),
                                                                                                     (104, '诸葛连弩', 'CN', 'SPECIAL', true, 30, 60, 50);