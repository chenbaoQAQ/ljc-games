-- 初始化基础兵种
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp) VALUES ('INFANTRY', 1, 10, 2);
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp) VALUES ('ARCHER', 1, 15, 1);
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp) VALUES ('CAVALRY', 2, 25, 3);

-- 初始化你的特色特种兵 (拐机制)
-- 中国特种兵：强化刀兵(INFANTRY) 1:2
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp, target_type, buff_ratio)
VALUES ('CN_SPECIAL', 3, 30, 4, 'INFANTRY', 2.0);

-- 英国特种兵：强化武将(HERO) 5:1 (倍率设为0.2，算法里用 count*ratio)
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp, target_type, buff_ratio)
VALUES ('EN_SPECIAL', 3, 35, 4, 'HERO', 0.2);

-- 初始化一个演示武将
INSERT INTO general_template (name, rarity, base_leadership, country)
VALUES ('赵云', 'SSR', 500, 'CN');