SET FOREIGN_KEY_CHECKS = 0;

-- 0. 清理旧数据 (防止主键冲突)
DELETE FROM story_stage_config;
DELETE FROM user_inventory;
DELETE FROM skill_book_map;
DELETE FROM user_gems;
DELETE FROM user_equipments;
DELETE FROM user_generals;
DELETE FROM users;
DELETE FROM gem_template;
DELETE FROM equipment_template;
DELETE FROM general_template;
DELETE FROM troop_template;
DELETE FROM skill_template;
DELETE FROM personality_config;
DELETE FROM user_civ_progress;
DELETE FROM user_troops;
DELETE FROM user_troop_progress;
DELETE FROM troop_evolution_config;
DELETE FROM story_unlock_config;
DELETE FROM drop_pool;

-- 0.5 插入掉落池 (必须先于关卡配置)
INSERT INTO drop_pool (pool_id, entries_json) VALUES
(1, '[{"type":"GOLD","min":100,"max":200,"weight":100}]'),
(2, '[{"type":"GOLD","min":200,"max":400,"weight":100}]'),
(3, '[{"type":"GOLD","min":400,"max":700,"weight":100}]'),
(4, '[{"type":"GOLD","min":700,"max":1200,"weight":100}]'),
(5, '[{"type":"GOLD","min":1200,"max":2000,"weight":100}]');

-- 1. 先插入性格 (否则插武将会报错外键错误)
INSERT INTO personality_config (personality_code, display_name, deal_mult, taken_mult, rescue_rate_bonus, last_stand_bias, roll_bias, roll_variance_scale, note) VALUES
                                                                                                                                                                     ('STOIC', '中庸', 1000, 1000, 0, 0, 0, 1000, '白板'),
                                                                                                                                                                     ('BERSERKER', '狂战', 1200, 1200, 0, 0, 0, 1000, '高攻高承伤');

-- 2. 插入技能 (否则武将没法绑默认技能)
INSERT INTO skill_template (skill_id, name, description, skill_type, trigger_timing, effect_json) VALUES
    (1, '鼓舞', '提升士气', 'PASSIVE', 'BATTLE_START', '{}');

-- 3. 插入初始兵种 (义勇兵)
INSERT INTO troop_template (troop_id, civ, name, troop_type, is_elite, cost, base_atk, base_hp, recruit_gold_cost, unlock_civ_required) VALUES
    (1001, 'CN', '义勇兵', 'INF', 0, 1, 10, 50, 10, 0),
    (2001, 'CN', '步兵', 'INF', 0, 1, 15, 60, 20, 0),
    (2002, 'CN', '弓兵', 'ARC', 0, 1, 20, 40, 20, 0),
    (2003, 'CN', '骑兵', 'CAV', 0, 2, 25, 80, 40, 0),
    -- JP troops
    (2101, 'JP', '足轻', 'INF', 0, 1, 15, 60, 20, 0),
    (2102, 'JP', '弓足轻', 'ARC', 0, 1, 20, 40, 20, 0),
    (2103, 'JP', '骑马武者', 'CAV', 0, 2, 25, 80, 40, 0),
    -- KR troops
    (2201, 'KR', '步卒', 'INF', 0, 1, 15, 60, 20, 0),
    (2202, 'KR', '弓手', 'ARC', 0, 1, 20, 40, 20, 0),
    (2203, 'KR', '骑卒', 'CAV', 0, 2, 25, 80, 40, 0),
    -- GB troops
    (2301, 'GB', 'Footman', 'INF', 0, 1, 15, 60, 20, 0),
    (2302, 'GB', 'Archer', 'ARC', 0, 1, 20, 40, 20, 0),
    (2303, 'GB', 'Cavalry', 'CAV', 0, 2, 25, 80, 40, 0),
    
    -- Elite Troops
    (3001, 'CN', '诸葛连弩', 'ARC', 1, 2, 35, 60, 100, 1),
    (3002, 'JP', '鬼武者', 'INF', 1, 2, 40, 80, 100, 1),
    (3003, 'KR', '花郎箭手', 'ARC', 1, 2, 30, 50, 100, 1),
    (3004, 'GB', '皇家骑士', 'CAV', 1, 3, 45, 120, 150, 1);


-- 4. 插入初始武将 (关键！ID=1001)
INSERT INTO general_template (template_id, civ, name, base_atk, base_hp, base_capacity, speed, personality_code, activate_gold_cost, max_level_tier0, default_skill_id) VALUES
    (1001, 'CN', '新手主公', 50, 500, 5, 50, 'STOIC', 0, 10, 1),
    (1002, 'CN', '未激活武将', 60, 600, 5, 45, 'BERSERKER', 1000, 10, 1),
    (1003, 'CN', '城门守卫', 70, 700, 6, 55, 'STOIC', 0, 10, 1),
    (1004, 'CN', '无双战将', 100, 1000, 8, 80, 'BERSERKER', 0, 10, 1);


-- 5. 插入装备模板（6栏：weapon/armor1/helm/boots/mount/accessory）
INSERT INTO equipment_template (template_id, slot, name, base_atk, base_hp, base_spd, base_capacity, enhance_growth_json) VALUES
    (1, 'weapon', '铁剑', 10, 0, 0, 0, '{"atk": 5}'),
    (2, 'armor1', '皮甲', 0, 50, 0, 0, '{"hp": 20}'),
    (3, 'helm', '轻钢盔', 0, 30, 4, 0, '{"hp": 10, "spd": 1}'),
    (4, 'boots', '疾风靴', 0, 0, 8, 0, '{"spd": 2}'),
    (5, 'mount', '战马', 6, 20, 12, 1, '{"atk": 2, "hp": 8, "spd": 3, "cap": 1}'),
    (6, 'accessory', '虎符', 8, 10, 0, 2, '{"atk": 3, "hp": 5, "cap": 1}'),
    (7, 'weapon', '青锋剑', 20, 0, 0, 0, '{"atk": 7}'),
    (8, 'armor1', '锁子甲', 0, 90, 0, 0, '{"hp": 28}'),
    (9, 'helm', '玄铁盔', 0, 55, 6, 0, '{"hp": 16, "spd": 1}'),
    (10, 'boots', '追风靴', 0, 0, 14, 0, '{"spd": 3}'),
    (11, 'mount', '赤兔', 10, 30, 18, 1, '{"atk": 3, "hp": 10, "spd": 4, "cap": 1}'),
    (12, 'accessory', '龙纹佩', 12, 20, 2, 3, '{"atk": 4, "hp": 8, "spd": 1, "cap": 1}');

-- 饰品光环：给兵种加成（Aura）
UPDATE equipment_template
SET
  aura_scope_type = 'TROOP',
  aura_scope_value = 'ALL',
  aura_stat = 'TROOP_ATK_RATE',
  aura_base_value = 100,
  aura_growth_per_enhance = 20
WHERE template_id = 6;

UPDATE equipment_template
SET
  aura_scope_type = 'TROOP',
  aura_scope_value = 'ALL',
  aura_stat = 'TROOP_ATK_RATE',
  aura_base_value = 150,
  aura_growth_per_enhance = 30
WHERE template_id = 12;

-- 6. 插入宝石模板
INSERT INTO gem_template (gem_type, gem_level, stat_value) VALUES
    ('ATK', 1, 10),
    ('ATK', 2, 24),
    ('ATK', 3, 42),
    ('ATK', 4, 68),
    ('ATK', 5, 102),
    ('HP', 1, 100),
    ('HP', 2, 240),
    ('HP', 3, 420),
    ('HP', 4, 680),
    ('HP', 5, 1020),
    ('SPD', 1, 2),
    ('SPD', 2, 5),
    ('SPD', 3, 9),
    ('SPD', 4, 14),
    ('SPD', 5, 20),
    ('CAP', 1, 1),
    ('CAP', 2, 2),
    ('CAP', 3, 3),
    ('CAP', 4, 4),
    ('CAP', 5, 5);

-- 7. 插入测试用户 (admin)
-- 密码 hash 暂为 dummy，实际应由 AuthService 加密。这里假设 Auth 逻辑允许明文或已有 hash
INSERT INTO users (id, username, password_hash, nickname, gold, diamond, initial_civ) VALUES
    (1, 'admin', 'pass123', '测试主公', 100000, 1000, 'CN');


-- 8. 给测试用户发初始资产
-- 8.1 初始武将
INSERT INTO user_generals (user_id, template_id, unlocked, activated, level, current_hp, max_hp, capacity, rest_turns, tier) VALUES
    (1, 1001, 1, 1, 1, 500, 500, 5, 0, 0),
    (1, 1002, 1, 0, 1, 600, 600, 5, 0, 0);

-- 8.2 初始装备 (未穿戴)
INSERT INTO user_equipments (user_id, template_id, enhance_level, general_id, slot, is_locked) VALUES
    (1, 1, 0, NULL, NULL, 0), -- 铁剑
    (1, 2, 0, NULL, NULL, 0), -- 皮甲
    (1, 3, 0, NULL, NULL, 0), -- 轻钢盔
    (1, 4, 0, NULL, NULL, 0), -- 疾风靴
    (1, 5, 0, NULL, NULL, 0), -- 战马
    (1, 6, 0, NULL, NULL, 0), -- 虎符
    (1, 7, 0, NULL, NULL, 0), -- 青锋剑
    (1, 8, 0, NULL, NULL, 0), -- 锁子甲
    (1, 11, 0, NULL, NULL, 0), -- 赤兔
    (1, 12, 0, NULL, NULL, 0); -- 龙纹佩

-- 8.3 初始宝石
INSERT INTO user_gems (user_id, gem_type, gem_level, stat_value, is_used) VALUES
    (1, 'ATK', 1, 10, 0),
    (1, 'ATK', 1, 10, 0),
    (1, 'ATK', 1, 10, 0),
    (1, 'ATK', 1, 10, 0),
    (1, 'ATK', 1, 10, 0),
    (1, 'HP', 1, 100, 0),
    (1, 'HP', 1, 100, 0),
    (1, 'HP', 1, 100, 0),
    (1, 'SPD', 1, 2, 0),
    (1, 'SPD', 1, 2, 0),
    (1, 'CAP', 1, 1, 0),
    (1, 'CAP', 1, 1, 0),
    (1, 'ATK', 2, 24, 0),
    (1, 'HP', 2, 240, 0);

-- 8.4 初始道具 (技能书: ItemId=301 -> SkillId=1)
-- 需先配置 skill_book_map
INSERT INTO skill_book_map (item_id, skill_id) VALUES (301, 1);

INSERT INTO user_inventory (user_id, item_id, count) VALUES
    (1, 301, 10); -- 10本技能书

-- 8.5 初始化四国进度 (CN已解锁,其他未解锁)
INSERT INTO user_civ_progress (user_id, civ, unlocked, max_stage_cleared) VALUES
(1, 'CN', 1, 10),
(1, 'JP', 1, 10),
(1, 'KR', 1, 10),
(1, 'GB', 1, 10);

-- 8.6 初始兵力 (四国全兵种，方便测试)
INSERT INTO user_troops (user_id, troop_id, count) VALUES
(1, 2001, 1000), (1, 2002, 1000), (1, 2003, 1000),
(1, 2101, 1000), (1, 2102, 1000), (1, 2103, 1000),
(1, 2201, 1000), (1, 2202, 1000), (1, 2203, 1000),
(1, 2301, 1000), (1, 2302, 1000), (1, 2303, 1000);

-- 9. 故事模式关卡配置 (CN 1-10)
-- 简单起见，所有关卡敌人配置类似，只是属性倍率不同
-- enemy_config_json 包含 hero 和 troops
-- 格式: { "hero": {...}, "troops": [...] }
INSERT INTO story_stage_config (civ, stage_no, stage_type, wall_cost_troops, enemy_multiplier, drop_pool_id, enemy_config_json) VALUES
('CN', 1, 'NORMAL', 0, 1000, 1, '{"hero":{"name":"山贼头目","maxHp":1000,"currentHp":1000,"atk":50,"speed":40,"personality":"STOIC"},"troops":[{"troopId":2001,"type":"INF","count":10,"unitHp":20,"frontHp":20}]}'),
('CN', 2, 'NORMAL', 0, 1100, 1, '{"hero":{"name":"黄巾小队长","maxHp":1200,"currentHp":1200,"atk":60,"speed":45,"personality":"STOIC"},"troops":[{"troopId":2001,"type":"INF","count":20,"unitHp":20,"frontHp":20}]}'),
('CN', 3, 'NORMAL', 0, 1200, 1, '{"hero":{"name":"黄巾猛将","maxHp":1500,"currentHp":1500,"atk":70,"speed":50,"personality":"BERSERKER"},"troops":[{"troopId":2001,"type":"INF","count":30,"unitHp":20,"frontHp":20}]}'),
('CN', 4, 'NORMAL', 0, 1300, 1, '{"hero":{"name":"黄巾军师","maxHp":1000,"currentHp":1000,"atk":100,"speed":60,"personality":"STOIC"},"troops":[{"troopId":2002,"type":"ARC","count":20,"unitHp":15,"frontHp":15}]}'),
('CN', 5, 'WALL',   50, 1500, 2, '{"hero":{"name":"城门守将","maxHp":3000,"currentHp":3000,"atk":80,"speed":30,"personality":"STOIC"},"troops":[{"troopId":2002,"type":"ARC","count":50,"unitHp":20,"frontHp":20}]}'),
('CN', 6, 'NORMAL', 0, 1600, 2, '{"hero":{"name":"西凉前锋","maxHp":2000,"currentHp":2000,"atk":90,"speed":70,"personality":"BERSERKER"},"troops":[{"troopId":2003,"type":"CAV","count":20,"unitHp":30,"frontHp":30}]}'),
('CN', 7, 'NORMAL', 0, 1700, 2, '{"hero":{"name":"西凉铁骑","maxHp":2500,"currentHp":2500,"atk":95,"speed":75,"personality":"BERSERKER"},"troops":[{"troopId":2003,"type":"CAV","count":30,"unitHp":30,"frontHp":30}]}'),
('CN', 8, 'NORMAL', 0, 1800, 2, '{"hero":{"name":"虎牢关守卫","maxHp":3000,"currentHp":3000,"atk":100,"speed":50,"personality":"STOIC"},"troops":[{"troopId":2001,"type":"INF","count":50,"unitHp":25,"frontHp":25}]}'),
('CN', 9, 'WALL',   100,2000, 3, '{"hero":{"name":"华雄","maxHp":5000,"currentHp":5000,"atk":150,"speed":80,"personality":"BERSERKER"},"troops":[{"troopId":2003,"type":"CAV","count":50,"unitHp":40,"frontHp":40}]}'),
('CN', 10,'BOSS',   0,  2500, 5, '{"hero":{"name":"吕布","maxHp":10000,"currentHp":10000,"atk":300,"speed":100,"personality":"BERSERKER"},"troops":[{"troopId":2003,"type":"CAV","count":100,"unitHp":50,"frontHp":50}]}');


-- CN unlock config
-- CN unlock config
INSERT INTO story_unlock_config (civ, stage_no, unlock_general_template_id, unlock_next_civ, unlock_troop_id, unlock_evolution_troop_id) VALUES
('CN', 1, 1002, NULL, 2001, NULL),  -- 1关解锁步兵
('CN', 2, NULL, NULL, 2002, NULL),  -- 2关解锁弓兵
('CN', 3, NULL, NULL, 2003, NULL),  -- 3关解锁骑兵
('CN', 5, 1003, NULL, 3001, NULL),  -- 5关解锁特种兵(诸葛连弩)
('CN', 8, NULL, NULL, NULL, 2001),  -- 8关解锁步兵进化
('CN', 10, 1004, 'JP', NULL, NULL);

INSERT INTO story_unlock_config (civ, stage_no, unlock_general_template_id, unlock_next_civ, unlock_troop_id, unlock_evolution_troop_id) VALUES
('JP', 1, 2001, NULL, 2101, NULL),
('JP', 5, 2002, NULL, 3002, NULL),
('JP', 10, 2003, 'KR', NULL, NULL),
('KR', 1, 2201, NULL, 2201, NULL),
('KR', 5, 2202, NULL, 3003, NULL),
('KR', 10, 2203, 'GB', NULL, NULL),
('GB', 1, 2301, NULL, 2301, NULL),
('GB', 5, 2302, NULL, 3004, NULL),
('GB', 10, 2303, NULL, NULL, NULL);


INSERT INTO general_template (template_id, civ, name, base_atk, base_hp, base_capacity, speed, personality_code, activate_gold_cost, max_level_tier0, default_skill_id) VALUES
    (2001, 'JP', 'JP_H1', 50, 500, 5, 50, 'STOIC', 0, 10, 1),
    (2002, 'JP', 'JP_H2', 60, 600, 5, 45, 'BERSERKER', 1000, 10, 1),
    (2003, 'JP', 'JP_H3', 100, 1000, 8, 80, 'BERSERKER', 0, 10, 1),
    -- KR heroes
    (2201, 'KR', 'KR_H1', 50, 500, 5, 50, 'STOIC', 0, 10, 1),
    (2202, 'KR', 'KR_H2', 60, 600, 5, 45, 'BERSERKER', 1000, 10, 1),
    (2203, 'KR', 'KR_H3', 100, 1000, 8, 80, 'BERSERKER', 0, 10, 1);


INSERT INTO story_stage_config (civ, stage_no, stage_type, wall_cost_troops, enemy_multiplier, drop_pool_id, enemy_config_json) VALUES
('JP', 1, 'NORMAL', 0, 1000, 1, '{"hero":{"name":"浪人","maxHp":1000,"currentHp":1000,"atk":50,"speed":40,"personality":"STOIC"},"troops":[{"troopId":2101,"type":"INF","count":12,"unitHp":20,"frontHp":20}]}'),
('JP', 2, 'NORMAL', 0, 1100, 1, '{"hero":{"name":"弓队头","maxHp":1200,"currentHp":1200,"atk":55,"speed":45,"personality":"STOIC"},"troops":[{"troopId":2102,"type":"ARC","count":14,"unitHp":15,"frontHp":15}]}'),
('JP', 3, 'NORMAL', 0, 1200, 1, '{"hero":{"name":"骑马武者","maxHp":1400,"currentHp":1400,"atk":60,"speed":55,"personality":"BERSERKER"},"troops":[{"troopId":2103,"type":"CAV","count":10,"unitHp":30,"frontHp":30}]}'),
('JP', 4, 'NORMAL', 0, 1300, 1, '{"hero":{"name":"侍从","maxHp":1600,"currentHp":1600,"atk":70,"speed":50,"personality":"STOIC"},"troops":[{"troopId":2101,"type":"INF","count":18,"unitHp":20,"frontHp":20},{"troopId":2102,"type":"ARC","count":12,"unitHp":15,"frontHp":15}]}'),
('JP', 5, 'WALL', 50, 1500, 2, '{"hero":{"name":"城门番","maxHp":3000,"currentHp":3000,"atk":80,"speed":30,"personality":"STOIC"},"troops":[{"troopId":2102,"type":"ARC","count":55,"unitHp":20,"frontHp":20}]}'),
('JP', 6, 'NORMAL', 0, 1600, 2, '{"hero":{"name":"旗本","maxHp":2200,"currentHp":2200,"atk":90,"speed":70,"personality":"BERSERKER"},"troops":[{"troopId":2102,"type":"ARC","count":20,"unitHp":18,"frontHp":18},{"troopId":2103,"type":"CAV","count":14,"unitHp":30,"frontHp":30}]}'),
('JP', 7, 'NORMAL', 0, 1700, 2, '{"hero":{"name":"武家小队长","maxHp":2500,"currentHp":2500,"atk":95,"speed":60,"personality":"BERSERKER"},"troops":[{"troopId":2101,"type":"INF","count":30,"unitHp":22,"frontHp":22},{"troopId":2103,"type":"CAV","count":12,"unitHp":32,"frontHp":32}]}'),
('JP', 8, 'NORMAL', 0, 1800, 2, '{"hero":{"name":"家臣","maxHp":3000,"currentHp":3000,"atk":100,"speed":55,"personality":"STOIC"},"troops":[{"troopId":2101,"type":"INF","count":28,"unitHp":25,"frontHp":25},{"troopId":2102,"type":"ARC","count":24,"unitHp":18,"frontHp":18},{"troopId":2103,"type":"CAV","count":14,"unitHp":35,"frontHp":35}]}'),
('JP', 9, 'WALL', 100, 2000, 3, '{"hero":{"name":"大将亲卫","maxHp":5000,"currentHp":5000,"atk":150,"speed":75,"personality":"BERSERKER"},"troops":[{"troopId":2103,"type":"CAV","count":60,"unitHp":40,"frontHp":40}]}'),
('JP', 10, 'BOSS', 0, 2500, 5, '{"hero":{"name":"幕府猛将","maxHp":10000,"currentHp":10000,"atk":300,"speed":100,"personality":"BERSERKER"},"troops":[{"troopId":2103,"type":"CAV","count":110,"unitHp":50,"frontHp":50},{"troopId":2102,"type":"ARC","count":80,"unitHp":20,"frontHp":20}]}');


-- KR 1-10 story stages
INSERT INTO story_stage_config (civ, stage_no, stage_type, wall_cost_troops, enemy_multiplier, drop_pool_id, enemy_config_json) VALUES
('KR', 1, 'NORMAL', 0, 1000, 1, '{"hero":{"name":"义军","maxHp":1000,"currentHp":1000,"atk":50,"speed":40,"personality":"STOIC"},"troops":[{"troopId":2201,"type":"INF","count":12,"unitHp":20,"frontHp":20}]}'),
('KR', 2, 'NORMAL', 0, 1100, 1, '{"hero":{"name":"弓队长","maxHp":1200,"currentHp":1200,"atk":55,"speed":45,"personality":"STOIC"},"troops":[{"troopId":2202,"type":"ARC","count":14,"unitHp":15,"frontHp":15}]}'),
('KR', 3, 'NORMAL', 0, 1200, 1, '{"hero":{"name":"骑队头","maxHp":1400,"currentHp":1400,"atk":60,"speed":55,"personality":"BERSERKER"},"troops":[{"troopId":2203,"type":"CAV","count":10,"unitHp":30,"frontHp":30}]}'),
('KR', 4, 'NORMAL', 0, 1300, 1, '{"hero":{"name":"前锋","maxHp":1600,"currentHp":1600,"atk":70,"speed":50,"personality":"STOIC"},"troops":[{"troopId":2201,"type":"INF","count":18,"unitHp":20,"frontHp":20},{"troopId":2202,"type":"ARC","count":12,"unitHp":15,"frontHp":15}]}'),
('KR', 5, 'WALL', 50, 1500, 2, '{"hero":{"name":"城门守军","maxHp":3000,"currentHp":3000,"atk":80,"speed":30,"personality":"STOIC"},"troops":[{"troopId":2202,"type":"ARC","count":55,"unitHp":20,"frontHp":20}]}'),
('KR', 6, 'NORMAL', 0, 1600, 2, '{"hero":{"name":"精锐","maxHp":2200,"currentHp":2200,"atk":90,"speed":70,"personality":"BERSERKER"},"troops":[{"troopId":2202,"type":"ARC","count":20,"unitHp":18,"frontHp":18},{"troopId":2203,"type":"CAV","count":14,"unitHp":30,"frontHp":30}]}'),
('KR', 7, 'NORMAL', 0, 1700, 2, '{"hero":{"name":"骑军统领","maxHp":2500,"currentHp":2500,"atk":95,"speed":60,"personality":"BERSERKER"},"troops":[{"troopId":2201,"type":"INF","count":30,"unitHp":22,"frontHp":22},{"troopId":2203,"type":"CAV","count":12,"unitHp":32,"frontHp":32}]}'),
('KR', 8, 'NORMAL', 0, 1800, 2, '{"hero":{"name":"护卫","maxHp":3000,"currentHp":3000,"atk":100,"speed":55,"personality":"STOIC"},"troops":[{"troopId":2201,"type":"INF","count":28,"unitHp":25,"frontHp":25},{"troopId":2202,"type":"ARC","count":24,"unitHp":18,"frontHp":18},{"troopId":2203,"type":"CAV","count":14,"unitHp":35,"frontHp":35}]}'),
('KR', 9, 'WALL', 100, 2000, 3, '{"hero":{"name":"亲卫统领","maxHp":5000,"currentHp":5000,"atk":150,"speed":75,"personality":"BERSERKER"},"troops":[{"troopId":2203,"type":"CAV","count":60,"unitHp":40,"frontHp":40}]}'),
('KR', 10, 'BOSS', 0, 2500, 5, '{"hero":{"name":"大将军","maxHp":10000,"currentHp":10000,"atk":300,"speed":100,"personality":"BERSERKER"},"troops":[{"troopId":2203,"type":"CAV","count":110,"unitHp":50,"frontHp":50},{"troopId":2202,"type":"ARC","count":80,"unitHp":20,"frontHp":20}]}');

-- GB heroes
INSERT INTO general_template (template_id, civ, name, base_atk, base_hp, base_capacity, speed, personality_code, activate_gold_cost, max_level_tier0, default_skill_id) VALUES
    (2301, 'GB', 'GB_H1', 50, 500, 5, 50, 'STOIC', 0, 10, 1),
    (2302, 'GB', 'GB_H2', 60, 600, 5, 45, 'BERSERKER', 1000, 10, 1),
    (2303, 'GB', 'GB_H3', 100, 1000, 8, 80, 'BERSERKER', 0, 10, 1);

-- GB 1-10 story stages
INSERT INTO story_stage_config (civ, stage_no, stage_type, wall_cost_troops, enemy_multiplier, drop_pool_id, enemy_config_json) VALUES
('GB', 1, 'NORMAL', 0, 1000, 1, '{"hero":{"name":"Militia Captain","maxHp":1000,"currentHp":1000,"atk":50,"speed":40,"personality":"STOIC"},"troops":[{"troopId":2301,"type":"INF","count":12,"unitHp":20,"frontHp":20}]}'),
('GB', 2, 'NORMAL', 0, 1100, 1, '{"hero":{"name":"Bow Sergeant","maxHp":1200,"currentHp":1200,"atk":55,"speed":45,"personality":"STOIC"},"troops":[{"troopId":2302,"type":"ARC","count":14,"unitHp":15,"frontHp":15}]}'),
('GB', 3, 'NORMAL', 0, 1200, 1, '{"hero":{"name":"Knight","maxHp":1400,"currentHp":1400,"atk":60,"speed":55,"personality":"BERSERKER"},"troops":[{"troopId":2303,"type":"CAV","count":10,"unitHp":30,"frontHp":30}]}'),
('GB', 4, 'NORMAL', 0, 1300, 1, '{"hero":{"name":"Vanguard","maxHp":1600,"currentHp":1600,"atk":70,"speed":50,"personality":"STOIC"},"troops":[{"troopId":2301,"type":"INF","count":18,"unitHp":20,"frontHp":20},{"troopId":2302,"type":"ARC","count":12,"unitHp":15,"frontHp":15}]}'),
('GB', 5, 'WALL', 50, 1500, 2, '{"hero":{"name":"Gate Warden","maxHp":3000,"currentHp":3000,"atk":80,"speed":30,"personality":"STOIC"},"troops":[{"troopId":2302,"type":"ARC","count":55,"unitHp":20,"frontHp":20}]}'),
('GB', 6, 'NORMAL', 0, 1600, 2, '{"hero":{"name":"Ranger","maxHp":2200,"currentHp":2200,"atk":90,"speed":70,"personality":"BERSERKER"},"troops":[{"troopId":2302,"type":"ARC","count":20,"unitHp":18,"frontHp":18},{"troopId":2303,"type":"CAV","count":14,"unitHp":30,"frontHp":30}]}'),
('GB', 7, 'NORMAL', 0, 1700, 2, '{"hero":{"name":"Knight Commander","maxHp":2500,"currentHp":2500,"atk":95,"speed":60,"personality":"BERSERKER"},"troops":[{"troopId":2301,"type":"INF","count":30,"unitHp":22,"frontHp":22},{"troopId":2303,"type":"CAV","count":12,"unitHp":32,"frontHp":32}]}'),
('GB', 8, 'NORMAL', 0, 1800, 2, '{"hero":{"name":"Royal Guard","maxHp":3000,"currentHp":3000,"atk":100,"speed":55,"personality":"STOIC"},"troops":[{"troopId":2301,"type":"INF","count":28,"unitHp":25,"frontHp":25},{"troopId":2302,"type":"ARC","count":24,"unitHp":18,"frontHp":18},{"troopId":2303,"type":"CAV","count":14,"unitHp":35,"frontHp":35}]}'),
('GB', 9, 'WALL', 100, 2000, 3, '{"hero":{"name":"Castle Guard","maxHp":5000,"currentHp":5000,"atk":150,"speed":75,"personality":"BERSERKER"},"troops":[{"troopId":2303,"type":"CAV","count":60,"unitHp":40,"frontHp":40}]}'),
('GB', 10, 'BOSS', 0, 2500, 5, '{"hero":{"name":"Warlord","maxHp":10000,"currentHp":10000,"atk":300,"speed":100,"personality":"BERSERKER"},"troops":[{"troopId":2303,"type":"CAV","count":110,"unitHp":50,"frontHp":50},{"troopId":2302,"type":"ARC","count":80,"unitHp":20,"frontHp":20}]}');



-- 10. V1数值补丁（武将基础值 + 特种兵定位 + 技能书补齐）
-- 说明：
-- 1) 这里用 ON DUPLICATE KEY 做“覆盖式配置”，方便你反复初始化。
-- 2) 特种兵 4 个定位先落在模板与命名层；具体“治疗/AOE/Buff/减攻”效果由战斗逻辑实现。
INSERT INTO skill_template (skill_id, name, description, skill_type, cooldown_turns, trigger_timing, effect_json) VALUES
    (1, '鼓舞', '奶自己（一大口+持续回血）', 'ACTIVE', 3, 'NONE', '{"kind":"heal_self_hot","instant":240,"hotTurns":2,"hotPerTurn":80}'),
    (2, '战地医治', '治疗我方三个基础兵种', 'ACTIVE', 3, 'NONE', '{"kind":"heal_three_basic_troops","healPerTroop":180}'),
    (3, '毒箭', '对敌方武将造成伤害并附加中毒', 'ACTIVE', 3, 'NONE', '{"kind":"hero_poison","poisonTurns":2,"poisonPerTurn":70}'),
    (4, '威压', '对敌方武将造成伤害并概率眩晕1回合', 'ACTIVE', 3, 'NONE', '{"kind":"hero_stun","chance":0.5,"stunTurns":1}'),
    (5, '铁壁', '给全队套1回合护盾/免疫效果', 'ACTIVE', 4, 'NONE', '{"kind":"team_immune","turns":1}'),
    (6, '一石二鸟', '攻击敌方武将并溅射敌方三个基础兵种', 'ACTIVE', 3, 'NONE', '{"kind":"hero_splash_troops","heroRatio":1.0,"troopRatio":0.55}'),
    (7, '偷袭', '武将对决阶段我方弓兵额外齐射敌将一轮', 'ACTIVE', 4, 'NONE', '{"kind":"ambush_archer_volley","ratio":0.7}'),
    (8, '红颜克制', '对异性造成伤害增加', 'PASSIVE', 0, 'ON_HIT', '{"kind":"bonus_vs_opposite_gender","bonusPermille":250}'),
    (9, '铁躯', '伤害减少并提高最大生命', 'PASSIVE', 0, 'BATTLE_START', '{"kind":"reduce_damage_and_boost_hp","damageReducePermille":120,"hpBonusPermille":180}'),
    (10, '易伤掌控', '提高易伤叠加效率并造成更多伤害', 'PASSIVE', 0, 'ON_HIT', '{"kind":"vuln_and_damage_bonus","bonusPermille":150,"vulnExtraPermille":120}'),
    (11, '反震', '反弹敌方英雄造成伤害的百分比', 'PASSIVE', 0, 'ON_TAKEN_HIT', '{"kind":"reflect_hero_damage","reflectPermille":180}'),
    (12, '先机', '第一回合必定先出手', 'PASSIVE', 0, 'BATTLE_START', '{"kind":"guaranteed_first_action_round1"}'),
    (13, '避锋', '第二阶段更低概率成为攻击目标', 'PASSIVE', 0, 'PHASE_CHANGE', '{"kind":"lower_target_rate_phase2","avoidTargetPermille":220}')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    skill_type = VALUES(skill_type),
    cooldown_turns = VALUES(cooldown_turns),
    trigger_timing = VALUES(trigger_timing),
    effect_json = VALUES(effect_json);

INSERT INTO skill_book_map (item_id, skill_id) VALUES
    (301, 1),
    (302, 2),
    (303, 3),
    (304, 4),
    (305, 5),
    (306, 6),
    (307, 7),
    (308, 8),
    (309, 9),
    (310, 10),
    (311, 11),
    (312, 12),
    (313, 13)
ON DUPLICATE KEY UPDATE
    skill_id = VALUES(skill_id);

INSERT INTO user_inventory (user_id, item_id, count) VALUES
    (1, 301, 10),
    (1, 302, 6),
    (1, 303, 6),
    (1, 304, 6),
    (1, 305, 6),
    (1, 306, 6),
    (1, 307, 4),
    (1, 308, 4),
    (1, 309, 4),
    (1, 310, 4),
    (1, 311, 4),
    (1, 312, 4),
    (1, 313, 4)
ON DUPLICATE KEY UPDATE
    count = VALUES(count);

INSERT INTO general_template (template_id, civ, name, base_atk, base_hp, base_capacity, speed, personality_code, activate_gold_cost, max_level_tier0, default_skill_id) VALUES
    -- CN：治疗向、坦中速
    (1001, 'CN', '刘备', 62, 980, 12, 62, 'STOIC', 0, 20, 1),
    (1002, 'CN', '关羽', 88, 1160, 13, 72, 'BERSERKER', 1200, 20, 2),
    (1003, 'CN', '张飞', 96, 1320, 14, 58, 'BERSERKER', 1800, 20, 4),
    (1004, 'CN', '赵云', 92, 1080, 13, 82, 'STOIC', 2400, 20, 6),
    -- JP：AOE与爆发倾向
    (2001, 'JP', '织田信长', 84, 1020, 12, 76, 'BERSERKER', 0, 20, 2),
    (2002, 'JP', '真田幸村', 90, 1100, 13, 80, 'BERSERKER', 1200, 20, 3),
    (2003, 'JP', '德川家康', 74, 1240, 14, 66, 'STOIC', 2200, 20, 5),
    -- KR：偏Buff与控场
    (2201, 'KR', '李舜臣', 78, 1120, 12, 70, 'STOIC', 0, 20, 5),
    (2202, 'KR', '金庾信', 82, 1060, 13, 74, 'STOIC', 1200, 20, 1),
    (2203, 'KR', '乙支文德', 94, 1180, 14, 78, 'BERSERKER', 2200, 20, 4),
    -- GB：偏减益与压制
    (2301, 'GB', '亚瑟', 80, 1140, 12, 68, 'STOIC', 0, 20, 5),
    (2302, 'GB', '兰斯洛特', 92, 1080, 13, 84, 'BERSERKER', 1400, 20, 3),
    (2303, 'GB', '莫德雷德', 98, 1200, 14, 79, 'BERSERKER', 2400, 20, 4)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    base_atk = VALUES(base_atk),
    base_hp = VALUES(base_hp),
    base_capacity = VALUES(base_capacity),
    speed = VALUES(speed),
    personality_code = VALUES(personality_code),
    activate_gold_cost = VALUES(activate_gold_cost),
    max_level_tier0 = VALUES(max_level_tier0),
    default_skill_id = VALUES(default_skill_id);

-- 兵种精细化补丁V2（四国差异化定位）
-- 统一口径：INF/ARC 统率2，CAV/ELITE 统率3（下方会再统一覆盖一次）
INSERT INTO troop_template (troop_id, civ, name, troop_type, is_elite, cost, base_atk, base_hp, recruit_gold_cost, unlock_civ_required) VALUES
    -- CN：稳健（防守型）
    (2001, 'CN', '重盾步兵', 'INF', 0, 2, 18, 78, 22, 0),
    (2002, 'CN', '强弩兵', 'ARC', 0, 2, 24, 52, 24, 0),
    (2003, 'CN', '虎豹骑', 'CAV', 0, 3, 34, 94, 42, 0),
    -- JP：高爆发（进攻型）
    (2101, 'JP', '足轻', 'INF', 0, 2, 20, 70, 22, 0),
    (2102, 'JP', '弓足轻', 'ARC', 0, 2, 29, 46, 26, 0),
    (2103, 'JP', '骑马武者', 'CAV', 0, 3, 36, 86, 45, 0),
    -- KR：均衡控场（功能型）
    (2201, 'KR', '步卒', 'INF', 0, 2, 19, 82, 23, 0),
    (2202, 'KR', '长弓手', 'ARC', 0, 2, 25, 54, 25, 0),
    (2203, 'KR', '重骑卒', 'CAV', 0, 3, 33, 90, 44, 0),
    -- GB：压制（肉盾+破甲）
    (2301, 'GB', '长枪步兵', 'INF', 0, 2, 18, 88, 24, 0),
    (2302, 'GB', '长弓手', 'ARC', 0, 2, 28, 50, 28, 0),
    (2303, 'GB', '重骑士', 'CAV', 0, 3, 31, 96, 46, 0),
    -- Elite：治疗 / AOE分流 / 英雄Buff / 敌军减攻
    (3001, 'CN', '青囊医官', 'ARC', 1, 3, 31, 96, 130, 1),
    (3002, 'JP', '爆裂火筒队', 'ARC', 1, 3, 48, 66, 145, 1),
    (3003, 'KR', '军乐旗卫', 'INF', 1, 3, 34, 124, 140, 1),
    (3004, 'GB', '破甲工兵', 'INF', 1, 3, 39, 132, 150, 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    troop_type = VALUES(troop_type),
    is_elite = VALUES(is_elite),
    cost = VALUES(cost),
    base_atk = VALUES(base_atk),
    base_hp = VALUES(base_hp),
    recruit_gold_cost = VALUES(recruit_gold_cost),
    unlock_civ_required = VALUES(unlock_civ_required);

-- admin 全武将解锁（避免只测到前两名武将）
INSERT INTO user_generals (user_id, template_id, unlocked, activated, level, current_hp, max_hp, capacity, rest_turns, tier)
SELECT
    1,
    gt.template_id,
    1,
    1,
    1,
    gt.base_hp,
    gt.base_hp,
    gt.base_capacity,
    0,
    0
FROM general_template gt
WHERE NOT EXISTS (
    SELECT 1
    FROM user_generals ug
    WHERE ug.user_id = 1
      AND ug.template_id = gt.template_id
);

INSERT INTO user_tower_progress (user_id, unlocked, max_floor_cleared, season)
VALUES (1, 1, 10, 1)
ON DUPLICATE KEY UPDATE
    unlocked = VALUES(unlocked),
    max_floor_cleared = VALUES(max_floor_cleared),
    season = VALUES(season);

INSERT INTO troop_template (troop_id, civ, name, troop_type, is_elite, cost, base_atk, base_hp, recruit_gold_cost, unlock_civ_required) VALUES
    -- 特种兵定位：
    -- 3001 治疗、3002 AOE分流、3003 英雄Buff、3004 敌军减攻（当前版本先体现在定位与数值）
    (3001, 'CN', '青囊医官', 'ARC', 1, 3, 31, 96, 130, 1),
    (3002, 'JP', '爆裂火筒队', 'ARC', 1, 3, 48, 66, 145, 1),
    (3003, 'KR', '军乐旗卫', 'INF', 1, 3, 34, 124, 140, 1),
    (3004, 'GB', '破甲工兵', 'INF', 1, 3, 39, 132, 150, 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    troop_type = VALUES(troop_type),
    is_elite = VALUES(is_elite),
    cost = VALUES(cost),
    base_atk = VALUES(base_atk),
    base_hp = VALUES(base_hp),
    recruit_gold_cost = VALUES(recruit_gold_cost),
    unlock_civ_required = VALUES(unlock_civ_required);

INSERT INTO user_troops (user_id, troop_id, count) VALUES
    -- admin 全解锁测试态：全部兵种给足兵力，便于全模块联调
    (1, 1001, 999),
    (1, 2001, 999), (1, 2002, 999), (1, 2003, 999), (1, 3001, 300),
    (1, 2101, 999), (1, 2102, 999), (1, 2103, 999), (1, 3002, 300),
    (1, 2201, 999), (1, 2202, 999), (1, 2203, 999), (1, 3003, 300),
    (1, 2301, 999), (1, 2302, 999), (1, 2303, 999), (1, 3004, 300)
ON DUPLICATE KEY UPDATE
    count = VALUES(count);

-- 8.7 admin 全解锁兵种 + 开放进化权限
INSERT INTO user_troop_progress (user_id, troop_id, status, evolution_tier, evolution_unlocked) VALUES
    (1, 1001, 2, 0, 1),
    (1, 2001, 2, 0, 1), (1, 2002, 2, 0, 1), (1, 2003, 2, 0, 1), (1, 3001, 2, 0, 1),
    (1, 2101, 2, 0, 1), (1, 2102, 2, 0, 1), (1, 2103, 2, 0, 1), (1, 3002, 2, 0, 1),
    (1, 2201, 2, 0, 1), (1, 2202, 2, 0, 1), (1, 2203, 2, 0, 1), (1, 3003, 2, 0, 1),
    (1, 2301, 2, 0, 1), (1, 2302, 2, 0, 1), (1, 2303, 2, 0, 1), (1, 3004, 2, 0, 1)
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    evolution_tier = VALUES(evolution_tier),
    evolution_unlocked = VALUES(evolution_unlocked);

-- 同步我方初始武将实例数值（与模板对齐，便于直接测试）
UPDATE user_generals
SET level = 1, tier = 0, current_hp = 980, max_hp = 980, capacity = 12, unlocked = 1, activated = 1
WHERE user_id = 1 AND template_id = 1001;

UPDATE user_generals
SET level = 1, tier = 0, current_hp = 1160, max_hp = 1160, capacity = 13, unlocked = 1, activated = 0
WHERE user_id = 1 AND template_id = 1002;

-- 统一兵种统率占用（与前端 capCost 保持一致）
UPDATE troop_template SET cost = 2 WHERE is_elite = 0 AND troop_type IN ('INF', 'ARC');
UPDATE troop_template SET cost = 3 WHERE is_elite = 0 AND troop_type = 'CAV';
UPDATE troop_template SET cost = 3 WHERE is_elite = 1;

-- 关卡重填（CN 1-10）：前期更平滑，后期逐步抬压
INSERT INTO story_stage_config (civ, stage_no, stage_type, wall_cost_troops, enemy_multiplier, drop_pool_id, enemy_config_json) VALUES
('CN', 1, 'NORMAL', 0, 1000, 1, '{"hero":{"name":"山贼哨长","maxHp":760,"currentHp":760,"atk":44,"speed":36,"personality":"STOIC"},"troops":[{"troopId":2001,"type":"INF","count":8,"unitHp":20,"frontHp":20}]}'),
('CN', 2, 'NORMAL', 0, 1050, 1, '{"hero":{"name":"黄巾什长","maxHp":900,"currentHp":900,"atk":50,"speed":40,"personality":"STOIC"},"troops":[{"troopId":2001,"type":"INF","count":10,"unitHp":20,"frontHp":20},{"troopId":2002,"type":"ARC","count":6,"unitHp":16,"frontHp":16}]}'),
('CN', 3, 'NORMAL', 0, 1120, 1, '{"hero":{"name":"黄巾校尉","maxHp":1080,"currentHp":1080,"atk":58,"speed":44,"personality":"BERSERKER"},"troops":[{"troopId":2001,"type":"INF","count":14,"unitHp":20,"frontHp":20},{"troopId":2002,"type":"ARC","count":8,"unitHp":16,"frontHp":16}]}'),
('CN', 4, 'NORMAL', 0, 1200, 1, '{"hero":{"name":"黄巾军师","maxHp":1260,"currentHp":1260,"atk":66,"speed":52,"personality":"STOIC"},"troops":[{"troopId":2002,"type":"ARC","count":12,"unitHp":18,"frontHp":18},{"troopId":2003,"type":"CAV","count":6,"unitHp":28,"frontHp":28}]}'),
('CN', 5, 'WALL', 25, 1300, 2, '{"hero":{"name":"城门守将","maxHp":1650,"currentHp":1650,"atk":72,"speed":38,"personality":"STOIC"},"troops":[{"troopId":2001,"type":"INF","count":18,"unitHp":22,"frontHp":22},{"troopId":2002,"type":"ARC","count":16,"unitHp":18,"frontHp":18}]}'),
('CN', 6, 'NORMAL', 0, 1400, 2, '{"hero":{"name":"西凉前锋","maxHp":1880,"currentHp":1880,"atk":82,"speed":62,"personality":"BERSERKER"},"troops":[{"troopId":2003,"type":"CAV","count":12,"unitHp":32,"frontHp":32},{"troopId":2002,"type":"ARC","count":14,"unitHp":18,"frontHp":18}]}'),
('CN', 7, 'NORMAL', 0, 1520, 2, '{"hero":{"name":"西凉铁骑统领","maxHp":2200,"currentHp":2200,"atk":92,"speed":70,"personality":"BERSERKER"},"troops":[{"troopId":2003,"type":"CAV","count":18,"unitHp":34,"frontHp":34},{"troopId":2001,"type":"INF","count":20,"unitHp":24,"frontHp":24}]}'),
('CN', 8, 'NORMAL', 0, 1650, 3, '{"hero":{"name":"虎牢关守卫","maxHp":2550,"currentHp":2550,"atk":104,"speed":58,"personality":"STOIC"},"troops":[{"troopId":2001,"type":"INF","count":28,"unitHp":26,"frontHp":26},{"troopId":2002,"type":"ARC","count":22,"unitHp":20,"frontHp":20},{"troopId":2003,"type":"CAV","count":10,"unitHp":36,"frontHp":36}]}'),
('CN', 9, 'WALL', 60, 1820, 3, '{"hero":{"name":"华雄","maxHp":3200,"currentHp":3200,"atk":128,"speed":76,"personality":"BERSERKER"},"troops":[{"troopId":2003,"type":"CAV","count":30,"unitHp":40,"frontHp":40},{"troopId":2002,"type":"ARC","count":24,"unitHp":22,"frontHp":22}]}'),
('CN', 10, 'BOSS', 0, 2050, 5, '{"hero":{"name":"吕布","maxHp":4300,"currentHp":4300,"atk":165,"speed":92,"personality":"BERSERKER"},"troops":[{"troopId":2003,"type":"CAV","count":36,"unitHp":44,"frontHp":44},{"troopId":2002,"type":"ARC","count":30,"unitHp":24,"frontHp":24},{"troopId":2001,"type":"INF","count":24,"unitHp":28,"frontHp":28}]}')
ON DUPLICATE KEY UPDATE
    stage_type = VALUES(stage_type),
    wall_cost_troops = VALUES(wall_cost_troops),
    enemy_multiplier = VALUES(enemy_multiplier),
    drop_pool_id = VALUES(drop_pool_id),
    enemy_config_json = VALUES(enemy_config_json);

-- 重新启用外键检查
-- 11. 插入兵种进化配置
INSERT INTO troop_evolution_config (troop_id, next_tier, required_civ, required_stage_no, cost_gold, stat_modifiers_json) VALUES
-- CN 步兵进化 -> 精锐步兵
(2001, 1, 'CN', 8, 2000, '{"atk": 5, "hp": 20}'),
-- CN 弓兵进化 -> 长弓手
(2002, 1, 'CN', 9, 2000, '{"atk": 8, "range": 1}'),
-- CN 骑兵进化 -> 重骑兵
(2003, 1, 'CN', 10, 3000, '{"atk": 10, "hp": 50}');


SET FOREIGN_KEY_CHECKS = 1;
