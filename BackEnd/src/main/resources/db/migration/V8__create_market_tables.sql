CREATE TABLE IF NOT EXISTS market_job_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    supply_demand_index DECIMAL(5,2) NOT NULL,
    level_tag VARCHAR(64) NOT NULL,
    hot_score DECIMAL(5,2) NOT NULL,
    demand_trend VARCHAR(64) NOT NULL,
    salary_trend VARCHAR(64) NOT NULL,
    talent_gap VARCHAR(255) NOT NULL,
    forecast_period VARCHAR(64) NULL,
    forecast_result VARCHAR(128) NULL,
    forecast_confidence DECIMAL(5,2) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_market_job_metrics_job_id FOREIGN KEY (job_id) REFERENCES jobs (id)
);

CREATE INDEX idx_market_job_metrics_job_id ON market_job_metrics (job_id);
