CREATE TABLE IF NOT EXISTS plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    target_job_id BIGINT NULL,
    title VARCHAR(255) NOT NULL,
    summary TEXT NULL,
    status VARCHAR(32) NOT NULL,
    progress DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_plans_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_plans_profile_id FOREIGN KEY (profile_id) REFERENCES student_profiles (id),
    CONSTRAINT fk_plans_target_job_id FOREIGN KEY (target_job_id) REFERENCES jobs (id)
);

CREATE INDEX idx_plans_user_profile ON plans (user_id, profile_id);

CREATE TABLE IF NOT EXISTS plan_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    phase_name VARCHAR(128) NOT NULL,
    task_title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    recommended_days INT NOT NULL DEFAULT 7,
    completed TINYINT(1) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_tasks_plan_id FOREIGN KEY (plan_id) REFERENCES plans (id)
);

CREATE INDEX idx_plan_tasks_plan_id ON plan_tasks (plan_id);

CREATE TABLE IF NOT EXISTS reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    target_job_id BIGINT NULL,
    title VARCHAR(255) NOT NULL,
    content_json LONGTEXT NOT NULL,
    polished_content LONGTEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reports_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reports_profile_id FOREIGN KEY (profile_id) REFERENCES student_profiles (id),
    CONSTRAINT fk_reports_target_job_id FOREIGN KEY (target_job_id) REFERENCES jobs (id)
);

CREATE INDEX idx_reports_user_profile ON reports (user_id, profile_id);
