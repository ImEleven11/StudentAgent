INSERT INTO jobs (id, title, industry, location, salary_min, salary_max, description, skills_json, portrait_summary)
SELECT 1, '前端开发工程师', '互联网', '成都', 8000, 15000,
       '负责 Web 前端页面开发、组件封装和接口联调。', '["vue","javascript","css","vite"]',
       '重视前端工程化、组件化能力以及和后端联调协作能力。'
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE id = 1);

INSERT INTO jobs (id, title, industry, location, salary_min, salary_max, description, skills_json, portrait_summary)
SELECT 2, 'Java后端开发工程师', '互联网', '成都', 10000, 18000,
       '负责 Java 服务开发、数据库设计、接口实现与系统稳定性建设。', '["java","spring boot","mysql","redis"]',
       '要求后端架构意识、接口设计能力和数据库建模能力。'
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE id = 2);

INSERT INTO jobs (id, title, industry, location, salary_min, salary_max, description, skills_json, portrait_summary)
SELECT 3, '机器学习工程师', '人工智能', '成都', 12000, 22000,
       '负责模型训练、特征工程、算法评估与模型服务化部署。', '["python","pytorch","机器学习","数据处理"]',
       '强调算法理解、实验能力和模型落地能力。'
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE id = 3);

INSERT INTO job_paths (job_id, path_type, step_order, target_job_id, target_job_title, description)
SELECT 1, 'PROMOTION', 1, NULL, '高级前端工程师', '完成复杂交互、性能优化和工程化建设。'
WHERE NOT EXISTS (SELECT 1 FROM job_paths WHERE job_id = 1 AND path_type = 'PROMOTION' AND step_order = 1);

INSERT INTO job_paths (job_id, path_type, step_order, target_job_id, target_job_title, description)
SELECT 1, 'TRANSITION', 1, 2, '全栈开发工程师', '加强后端接口和数据库能力后可向全栈发展。'
WHERE NOT EXISTS (SELECT 1 FROM job_paths WHERE job_id = 1 AND path_type = 'TRANSITION' AND step_order = 1);

INSERT INTO resources (id, resource_type, title, description, link, skills_json, level_tag, duration_text)
SELECT 1, 'COURSE', 'Vue 3 实战课程', '系统学习组件化开发、状态管理和工程化实践。',
       'https://example.com/vue-course', '["vue","javascript","css"]', 'beginner', '24h'
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE id = 1);

INSERT INTO resources (id, resource_type, title, description, link, skills_json)
SELECT 2, 'BOOK', '深入理解 Java 虚拟机', '理解 JVM、性能调优和后端基础设施。',
       'https://example.com/jvm-book', '["java","jvm"]'
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE id = 2);

INSERT INTO resources (id, resource_type, title, description, link, skills_json, difficulty_tag)
SELECT 3, 'PROJECT', '学生职业规划平台项目', '从前后端到模型服务的综合实践项目。',
       'https://example.com/project', '["java","vue","mysql"]', 'medium'
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE id = 3);

INSERT INTO resources (id, resource_type, title, description, link, skills_json, location_tag)
SELECT 4, 'INTERNSHIP', '成都软件研发实习', '适合具备基础开发能力的在校生实习岗位。',
       'https://example.com/internship', '["java","sql"]', '成都'
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE id = 4);

INSERT INTO resources (id, resource_type, title, description, link, category_tag, time_range_tag)
SELECT 5, 'COMPETITION', '大学生服务外包创新创业大赛', '适合做系统设计、工程实现和展示答辩。',
       'https://example.com/competition', '创新创业', '全年'
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE id = 5);

INSERT INTO market_job_metrics (job_id, supply_demand_index, level_tag, hot_score, demand_trend, salary_trend, talent_gap, forecast_period, forecast_result, forecast_confidence)
SELECT 1, 76.00, '高热', 88.00, '上升', '上涨', '中高级前端工程人才仍有缺口', '6个月', '需求上升', 0.82
WHERE NOT EXISTS (SELECT 1 FROM market_job_metrics WHERE job_id = 1);

INSERT INTO market_job_metrics (job_id, supply_demand_index, level_tag, hot_score, demand_trend, salary_trend, talent_gap, forecast_period, forecast_result, forecast_confidence)
SELECT 2, 81.00, '高热', 91.00, '上升', '上涨', '具备架构与业务理解的后端人才紧缺', '6个月', '需求上升', 0.86
WHERE NOT EXISTS (SELECT 1 FROM market_job_metrics WHERE job_id = 2);

INSERT INTO market_job_metrics (job_id, supply_demand_index, level_tag, hot_score, demand_trend, salary_trend, talent_gap, forecast_period, forecast_result, forecast_confidence)
SELECT 3, 73.00, '高热', 84.00, '上升', '上涨', '工程化落地能力强的算法人才供给不足', '6个月', '需求上升', 0.80
WHERE NOT EXISTS (SELECT 1 FROM market_job_metrics WHERE job_id = 3);

INSERT INTO system_configs (config_key, config_value)
SELECT 'site.notice', 'StudentAgent demo backend configuration'
WHERE NOT EXISTS (SELECT 1 FROM system_configs WHERE config_key = 'site.notice');
