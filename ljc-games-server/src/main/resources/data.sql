-- =========================================================
-- LJC Data v1.0 (基础配置数据)
-- =========================================================

-- 1. 性格配置 (基于你的设计)
-- BERSERKER: 狂战 (攻+20%, 承伤+20%)
-- STOIC: 中庸 (无加成)
-- MEDIC: 军医 (攻-10%, 救治率+15%)
-- IRONWILL: 死战 (死战触发权重+5000)
-- DEFENDER: 护军 (攻-20%, 承伤-10%, 嘲讽度-20)
-- TACTICIAN: 谋略 (承伤+10%, 伤害波动减半)
INSERT INTO personality_config
(personality_code, display_name, deal_mult, taken_mult, rescue_rate_bonus, last_stand_bias, roll_bias, roll_variance_scale, note)
VALUES
    ('BERSERKER', '狂战', 1200, 1200, 0,   0,    0,    1000, '伤害+20%, 承伤+20%'),
    ('STOIC',     '中庸', 1000, 1000, 0,   0,    0,    1000, '白板'),
    ('MEDIC',     '军医', 900,  1000, 150, 0,    0,    1000, '攻击-10%, 救治率+15%'),
    ('IRONWILL',  '死战', 1000, 1000, 0,   5000, 0,    1000, '死战触发权重大幅增加'),
    ('DEFENDER',  '护军', 800,  900,  0,   0,   -20,   1000, '攻击-20%, 承伤-10%, 兵更替武将挡刀'),
    ('TACTICIAN', '谋略', 1000, 1100, 0,   0,    0,    500,  '承伤+10%, 伤害波动收敛50%');

-- 2. 初始技能 (防止空指针)
-- ID=1: 基础鼓舞
INSERT INTO skill_template (skill_id, name, description, skill_type, trigger_timing, effect_json)
VALUES
    (1, '初级鼓舞', '我军全体攻击力微量提升', 'PASSIVE', 'BATTLE_START', '{"atk_pct_buff": 5}');

-- 3. 基础兵种 (义勇兵)
-- ID=1, 只有CN能用, 消耗1, 极便宜
INSERT INTO troop_template
(troop_id, civ, name, troop_type, is_elite, cost, base_atk, base_hp, recruit_gold_cost, unlock_civ_required)
VALUES
    (1, 'CN', '义勇兵', 'INF', 0, 1, 10, 50, 10, 0);

-- 4. 初始武将 (给新号送的)
-- ID=1001, CN, 性格=中庸(STOIC), 默认技能=1
INSERT INTO general_template
(template_id, civ, name, base_atk, base_hp, base_capacity, personality_code, activate_gold_cost, max_level_tier0, default_skill_id)
VALUES
    (1001, 'CN', '新手主公', 50, 500, 10, 'STOIC', 0, 10, 1);

-- 5. 主线关卡第一关 (防止报错)
INSERT INTO story_stage_config
(civ, stage_no, stage_type, stamina_cost, wall_cost_troops, enemy_multiplier, enemy_troop_comp_json)
VALUES
    ('CN', 1, 'WALL', 0, 100, 1000, '{}');