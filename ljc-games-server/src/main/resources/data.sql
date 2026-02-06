-- 1. 先插入性格 (否则插武将会报错外键错误)
INSERT INTO personality_config (personality_code, display_name, deal_mult, taken_mult, rescue_rate_bonus, last_stand_bias, roll_bias, roll_variance_scale, note) VALUES
                                                                                                                                                                     ('STOIC', '中庸', 1000, 1000, 0, 0, 0, 1000, '白板'),
                                                                                                                                                                     ('BERSERKER', '狂战', 1200, 1200, 0, 0, 0, 1000, '高攻高承伤');

-- 2. 插入技能 (否则武将没法绑默认技能)
INSERT INTO skill_template (skill_id, name, description, skill_type, trigger_timing, effect_json) VALUES
    (1, '鼓舞', '提升士气', 'PASSIVE', 'BATTLE_START', '{}');

-- 3. 插入初始兵种 (义勇兵)
INSERT INTO troop_template (troop_id, civ, name, troop_type, is_elite, cost, base_atk, base_hp, recruit_gold_cost, unlock_civ_required) VALUES
    (1001, 'CN', '义勇兵', 'INF', 0, 1, 10, 50, 10, 0);

-- 4. 插入初始武将 (关键！ID=1001)
INSERT INTO general_template (template_id, civ, name, base_atk, base_hp, base_capacity, personality_code, activate_gold_cost, max_level_tier0, default_skill_id) VALUES
    (1001, 'CN', '新手主公', 50, 500, 5, 'STOIC', 0, 10, 1),
    (1002, 'CN', '未激活武将', 60, 600, 5, 'BERSERKER', 1000, 10, 1);

-- 5. 插入装备模板
INSERT INTO equipment_template (template_id, slot, name, base_atk, base_hp, base_spd, base_capacity, enhance_growth_json) VALUES
    (1, 'weapon', '铁剑', 10, 0, 0, 0, '{"atk": 5}'),
    (2, 'armor1', '皮甲', 0, 50, 0, 0, '{"hp": 20}');

-- 6. 插入宝石模板
INSERT INTO gem_template (gem_type, gem_level, stat_value) VALUES
    ('ATK', 1, 10),
    ('HP', 1, 100);

-- 7. 插入测试用户 (admin)
-- 密码 hash 暂为 dummy，实际应由 AuthService 加密。这里假设 Auth 逻辑允许明文或已有 hash
INSERT INTO users (id, username, password_hash, nickname, gold, diamond, stamina, initial_civ) VALUES
    (1, 'admin', 'pass123', '测试主公', 100000, 1000, 100, 'CN');

-- 8. 给测试用户发初始资产
-- 8.1 初始武将
INSERT INTO user_generals (user_id, template_id, unlocked, activated, level, current_hp, max_hp, capacity, rest_turns, tier) VALUES
    (1, 1001, 1, 1, 1, 500, 500, 5, 0, 0),
    (1, 1002, 1, 0, 1, 600, 600, 5, 0, 0);

-- 8.2 初始装备 (未穿戴)
INSERT INTO user_equipments (user_id, template_id, enhance_level, general_id, slot, is_locked) VALUES
    (1, 1, 0, NULL, NULL, 0), -- 铁剑
    (1, 2, 0, NULL, NULL, 0); -- 皮甲

-- 8.3 初始宝石
INSERT INTO user_gems (user_id, gem_type, gem_level, stat_value, is_used) VALUES
    (1, 'ATK', 1, 10, 0),
    (1, 'ATK', 1, 10, 0);

-- 8.4 初始道具 (技能书: ItemId=301 -> SkillId=1)
-- 需先配置 skill_book_map
INSERT INTO skill_book_map (item_id, skill_id) VALUES (301, 1);

INSERT INTO user_inventory (user_id, item_id, count) VALUES
    (1, 301, 10); -- 10本技能书