-- 仓库排行榜（周/月），用于存储 OpenDigger 或自有榜单快照
CREATE TABLE IF NOT EXISTS `repo_ranking` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `owner` VARCHAR(100) NOT NULL COMMENT '仓库所有者/组织',
  `repo` VARCHAR(200) NOT NULL COMMENT '仓库名',
  `display_name` VARCHAR(200) COMMENT '展示名称',
  `period_type` VARCHAR(10) NOT NULL COMMENT '排行类型：WEEK/MONTH',
  `period` VARCHAR(10) NOT NULL COMMENT '排行周期，例如 2026-01 或 2026-W02',
  `rank` INT COMMENT '排名（1 为最高）',
  `openrank` DOUBLE COMMENT 'OpenRank 分值',
  `stars` BIGINT COMMENT 'Star 总数',
  `delta_stars` BIGINT COMMENT '期间 Star 增量',
  `attention` DOUBLE COMMENT 'Attention 指标',
  `activity` DOUBLE COMMENT 'Activity 指标',
  `description` VARCHAR(500) COMMENT '描述或摘要',
  `tags` VARCHAR(255) COMMENT '标签，逗号分隔',
  `updated_at` DATETIME COMMENT '更新时间',
  UNIQUE KEY `uniq_repo_period` (`owner`, `repo`, `period_type`, `period`),
  KEY `idx_period` (`period_type`, `period`, `rank`)
) COMMENT='仓库排行榜（周/月）';

-- 示例数据：月榜（2026-01）
INSERT INTO `repo_ranking` (`owner`, `repo`, `display_name`, `period_type`, `period`, `rank`, `openrank`, `stars`, `delta_stars`, `attention`, `activity`, `description`, `tags`, `updated_at`)
VALUES
('microsoft', 'vscode', 'VS Code', 'MONTH', '2026-01', 1, 9500.0, 160000, 1200, 210.5, 180.3, '流行的开源编辑器', 'editor,devtools', NOW()),
('pytorch', 'pytorch', 'PyTorch', 'MONTH', '2026-01', 2, 8800.5, 78000, 980, 190.1, 175.8, '深度学习框架', 'ai,ml', NOW()),
('vercel', 'next.js', 'Next.js', 'MONTH', '2026-01', 3, 7200.2, 120000, 800, 160.4, 140.6, 'React 全栈框架', 'web,framework', NOW());

-- 示例数据：周榜（2026-W02）
INSERT INTO `repo_ranking` (`owner`, `repo`, `display_name`, `period_type`, `period`, `rank`, `openrank`, `stars`, `delta_stars`, `attention`, `activity`, `description`, `tags`, `updated_at`)
VALUES
('apache', 'spark', 'Apache Spark', 'WEEK', '2026-W02', 1, 5300.3, 36000, 320, 120.5, 110.2, '大数据计算引擎', 'data,infra', NOW()),
('apache', 'flink', 'Apache Flink', 'WEEK', '2026-W02', 2, 4800.7, 27000, 260, 105.3, 99.1, '流批一体引擎', 'streaming,infra', NOW());

-- 当前月（示例 2026-02）榜单
INSERT INTO `repo_ranking` (`owner`, `repo`, `display_name`, `period_type`, `period`, `rank`, `openrank`, `stars`, `delta_stars`, `attention`, `activity`, `description`, `tags`, `updated_at`)
VALUES
('huggingface', 'transformers', 'Transformers', 'MONTH', '2026-02', 1, 9100.0, 120000, 1500, 200.2, 185.6, 'NLP 模型库', 'ai,nlp', NOW()),
('openai', 'whisper', 'Whisper', 'MONTH', '2026-02', 2, 7600.4, 65000, 950, 150.8, 140.2, '语音识别模型', 'audio,ai', NOW());

-- 当前周（示例 2026-W03）榜单
INSERT INTO `repo_ranking` (`owner`, `repo`, `display_name`, `period_type`, `period`, `rank`, `openrank`, `stars`, `delta_stars`, `attention`, `activity`, `description`, `tags`, `updated_at`)
VALUES
('apache', 'kafka', 'Apache Kafka', 'WEEK', '2026-W03', 1, 5100.5, 25000, 210, 115.4, 108.7, '分布式消息流平台', 'streaming,infra', NOW()),
('apache', 'iceberg', 'Apache Iceberg', 'WEEK', '2026-W03', 2, 4700.9, 19000, 180, 102.3, 95.6, '表格式湖存储', 'data,lakehouse', NOW());
