CREATE TABLE IF NOT EXISTS assessments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    assessment_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    questions_json LONGTEXT NOT NULL,
    answers_json LONGTEXT NULL,
    result_json LONGTEXT NULL,
    adjustment_suggestions_json LONGTEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_assessments_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_assessments_plan_id FOREIGN KEY (plan_id) REFERENCES plans (id)
);

CREATE INDEX idx_assessments_plan_id ON assessments (plan_id);
