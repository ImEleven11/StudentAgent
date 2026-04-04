CREATE TABLE IF NOT EXISTS student_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    education_summary TEXT NULL,
    skills_json LONGTEXT NULL,
    experiences_json LONGTEXT NULL,
    certificates_json LONGTEXT NULL,
    innovation_summary TEXT NULL,
    interest_summary TEXT NULL,
    personality_summary TEXT NULL,
    ability_score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    competitiveness_score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    completeness_score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    resume_file_name VARCHAR(255) NULL,
    resume_storage_path VARCHAR(1024) NULL,
    resume_content_type VARCHAR(128) NULL,
    parse_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    parse_progress INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_profiles_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_student_profiles_user_id UNIQUE (user_id)
);

CREATE INDEX idx_student_profiles_parse_status ON student_profiles (parse_status);
