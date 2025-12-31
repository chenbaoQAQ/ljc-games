-- 清理旧数据并重新插入更合理的个位数数值
TRUNCATE TABLE unit_config;

-- 刀(1)、弓(1.5->四舍五入设为2)、骑(3)
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp) VALUES ('INFANTRY', 1, 1, 2);
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp) VALUES ('ARCHER', 1, 2, 1);
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp) VALUES ('CAVALRY', 2, 3, 3);

-- 特种兵：中国步兵拐 (CN_SPECIAL)
-- 自身攻击设为2，强化比例1:2，让刀兵攻击从1变成2 (buff_ratio=2.0)
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp, target_type, buff_ratio)
VALUES ('CN_SPECIAL', 3, 2, 4, 'INFANTRY', 2.0);

-- 特种兵：英国英雄拐 (EN_SPECIAL)
-- 自身攻击设为3，强化比例5:1 (buff_ratio=0.2)
INSERT INTO unit_config (unit_name, space_cost, base_atk, base_hp, target_type, buff_ratio)
VALUES ('EN_SPECIAL', 3, 3, 4, 'HERO', 0.2);