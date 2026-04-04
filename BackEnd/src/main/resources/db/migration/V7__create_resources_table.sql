CREATE TABLE IF NOT EXISTS resources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_type VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    link VARCHAR(1024) NULL,
    skills_json LONGTEXT NULL,
    level_tag VARCHAR(64) NULL,
    difficulty_tag VARCHAR(64) NULL,
    location_tag VARCHAR(128) NULL,
    category_tag VARCHAR(128) NULL,
    time_range_tag VARCHAR(128) NULL,
    duration_text VARCHAR(128) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_resources_type ON resources (resource_type);
CREATE INDEX idx_resources_level ON resources (level_tag);
