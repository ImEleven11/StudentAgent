CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    industry VARCHAR(128) NULL,
    location VARCHAR(128) NULL,
    salary_min DECIMAL(10,2) NULL,
    salary_max DECIMAL(10,2) NULL,
    description TEXT NULL,
    skills_json LONGTEXT NULL,
    portrait_summary TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_jobs_title ON jobs (title);
CREATE INDEX idx_jobs_industry ON jobs (industry);
CREATE INDEX idx_jobs_location ON jobs (location);

CREATE TABLE IF NOT EXISTS job_paths (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    path_type VARCHAR(32) NOT NULL,
    step_order INT NOT NULL,
    target_job_id BIGINT NULL,
    target_job_title VARCHAR(128) NOT NULL,
    description VARCHAR(512) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_paths_job_id FOREIGN KEY (job_id) REFERENCES jobs (id)
);

CREATE INDEX idx_job_paths_job_id ON job_paths (job_id);

CREATE TABLE IF NOT EXISTS match_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    overall_score DECIMAL(5,2) NOT NULL,
    basic_score DECIMAL(5,2) NOT NULL,
    skill_score DECIMAL(5,2) NOT NULL,
    potential_score DECIMAL(5,2) NOT NULL,
    details_json LONGTEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_match_records_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_match_records_profile_id FOREIGN KEY (profile_id) REFERENCES student_profiles (id),
    CONSTRAINT fk_match_records_job_id FOREIGN KEY (job_id) REFERENCES jobs (id)
);

CREATE INDEX idx_match_records_user_profile ON match_records (user_id, profile_id);
