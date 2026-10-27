CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    display_name VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS topic_meta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic VARCHAR(128) NOT NULL,
    owner VARCHAR(64),
    remark VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_topic_meta_topic UNIQUE (topic)
);

CREATE TABLE IF NOT EXISTS metrics_snapshot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic VARCHAR(128),
    consumer_group VARCHAR(128),
    metric_type VARCHAR(32) NOT NULL,
    metric_value BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_metrics_topic_time ON metrics_snapshot (topic, created_at);
CREATE INDEX IF NOT EXISTS idx_metrics_group_time ON metrics_snapshot (consumer_group, created_at);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64),
    module VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    method VARCHAR(255),
    params CLOB,
    success BOOLEAN NOT NULL,
    error_message CLOB,
    duration_ms BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_username_time ON audit_log (username, created_at);
