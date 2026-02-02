-- 1. 先插入性格 (否则插武将会报错外键错误)
INSERT INTO personality_config (personality_code, display_name, deal_mult, taken_mult, rescue_rate_bonus, last_stand_bias, roll_bias, roll_variance_scale, note) VALUES
                                                                                                                                                                     ('STOIC', '中庸', 1000, 1000, 0, 0, 0, 1000, '白板'),
                                                                                                                                                                     ('BERSERKER', '狂战', 1200, 1200, 0, 0, 0, 1000, '高攻高承伤');

-- 2. 插入技能 (否则武将没法绑默认技能)
INSERT INTO skill_template (skill_id, name, description, skill_type, trigger_timing, effect_json) VALUES
    (1, '鼓舞', '提升士气', 'PASSIVE', 'BATTLE_START', '{}');

-- 3. 插入初始兵种 (义勇兵)
INSERT INTO troop_template (troop_id, civ, name, troop_type, is_elite, cost, base_atk, base_hp, recruit_gold_cost, unlock_civ_required) VALUES
    (1, 'CN', '义勇兵', 'INF', 0, 1, 10, 50, 10, 0);

-- 4. 插入初始武将 (关键！ID=1001)
INSERT INTO general_template (template_id, civ, name, base_atk, base_hp, base_capacity, personality_code, activate_gold_cost, max_level_tier0, default_skill_id) VALUES
    (1001, 'CN', '新手主公', 50, 500, 5, 'STOIC', 0, 10, 1);