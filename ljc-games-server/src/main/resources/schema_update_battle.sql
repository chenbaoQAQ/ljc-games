CREATE TABLE IF NOT EXISTS battle_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    battle_id BIGINT,
    dungeon_id INT NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0:ONGOING, 1:VICTORY, 2:DEFEAT',
    current_turn INT DEFAULT 1,
    context_json LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_status (user_id, status) -- Ensure only one active session per user roughly? No, Status changes. better unique key on user_id if we hard limit.
    -- For now, let's just index user_id and handle logic in app.
);

CREATE INDEX idx_battle_user ON battle_sessions(user_id);
