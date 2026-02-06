-- Create story_stage_config table
DROP TABLE IF EXISTS story_stage_config;
CREATE TABLE story_stage_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    civ VARCHAR(10) NOT NULL COMMENT 'Civilization: CN, JP, KR, GB',
    stage_no INT NOT NULL COMMENT '1-10',
    stamina_cost INT DEFAULT 10,
    enemy_config_json LONGTEXT COMMENT 'JSON defining enemy hero and troops',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_civ_stage (civ, stage_no)
);

-- Insert Data for CN Stages 1-10
-- Simplified Enemy Config for MVP
-- Stage 1: Weak Infantry
INSERT INTO story_stage_config (civ, stage_no, stamina_cost, enemy_config_json) VALUES 
('CN', 1, 10, '{"hero": null, "troops": [{"type": "INF", "count": 20, "unitHp": 10, "frontHp": 10}]}');

-- Stage 2: Weak Archers
INSERT INTO story_stage_config (civ, stage_no, stamina_cost, enemy_config_json) VALUES 
('CN', 2, 10, '{"hero": null, "troops": [{"type": "ARC", "count": 20, "unitHp": 10, "frontHp": 10}]}');

-- Stage 3: Mixed
INSERT INTO story_stage_config (civ, stage_no, stamina_cost, enemy_config_json) VALUES 
('CN', 3, 10, '{"hero": null, "troops": [{"type": "INF", "count": 30, "unitHp": 10, "frontHp": 10}, {"type": "ARC", "count": 20, "unitHp": 10, "frontHp": 10}]}');

-- Stage 4: Introduction of Cavalry
INSERT INTO story_stage_config (civ, stage_no, stamina_cost, enemy_config_json) VALUES 
('CN', 4, 10, '{"hero": null, "troops": [{"type": "CAV", "count": 20, "unitHp": 20, "frontHp": 20}]}');

-- Stage 5: Mini Boss (Has Hero)
INSERT INTO story_stage_config (civ, stage_no, stamina_cost, enemy_config_json) VALUES 
('CN', 5, 20, '{"hero": {"name": "Bandit Leader", "hp": 500, "atk": 50}, "troops": [{"type": "INF", "count": 50, "unitHp": 10, "frontHp": 10}]}');

-- Stage 6-9: Scaling Difficulty
INSERT INTO story_stage_config (civ, stage_no, stamina_cost, enemy_config_json) VALUES 
('CN', 6, 12, '{"hero": null, "troops": [{"type": "INF", "count": 60, "unitHp": 15, "frontHp": 15}]}');
INSERT INTO story_stage_config (civ, stage_no, stamina_cost, enemy_config_json) VALUES 
('CN', 7, 12, '{"hero": null, "troops": [{"type": "ARC", "count": 60, "unitHp": 15, "frontHp": 15}, {"type": "INF", "count": 20, "unitHp": 20, "frontHp": 20}]}');
INSERT INTO story_stage_config (civ, stage_no, stamina_cost, enemy_config_json) VALUES 
('CN', 8, 15, '{"hero": null, "troops": [{"type": "CAV", "count": 50, "unitHp": 25, "frontHp": 25}]}');
INSERT INTO story_stage_config (civ, stage_no, stamina_cost, enemy_config_json) VALUES 
('CN', 9, 15, '{"hero": {"name": "General Guard", "hp": 800, "atk": 80}, "troops": [{"type": "INF", "count": 100, "unitHp": 20, "frontHp": 20}]}');

-- Stage 10: Final Boss (Unlock next Civ)
INSERT INTO story_stage_config (civ, stage_no, stamina_cost, enemy_config_json) VALUES 
('CN', 10, 30, '{"hero": {"name": "Dong Zhuo", "hp": 2000, "atk": 150}, "troops": [{"type": "CAV", "count": 100, "unitHp": 30, "frontHp": 30}, {"type": "ARC", "count": 100, "unitHp": 15, "frontHp": 15}]}');
