DROP TABLE IF EXISTS battle_sessions;

CREATE TABLE battle_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    battle_id BIGINT,
    dungeon_id INT NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0:ONGOING, 1:VICTORY, 2:DEFEAT',
    current_turn INT DEFAULT 1,
    context_json LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_battle_user ON battle_sessions(user_id);
