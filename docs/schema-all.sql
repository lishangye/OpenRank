-- 仓库主表（替代 repo_ranking 主信息，用于最新概览）
CREATE TABLE IF NOT EXISTS `repo` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `owner` VARCHAR(100) NOT NULL COMMENT '仓库所有者/组织',
  `repo` VARCHAR(200) NOT NULL COMMENT '仓库名',
  `full_name` VARCHAR(256) NOT NULL COMMENT 'owner/repo 唯一标识',
  `display_name` VARCHAR(200) COMMENT '展示名称',
  `description` TEXT COMMENT '描述',
  `tags` VARCHAR(255) COMMENT '标签，逗号分隔',
  `status` VARCHAR(20) DEFAULT 'production' COMMENT '状态：production/beta/lab',
  `priority` VARCHAR(10) DEFAULT 'P2' COMMENT '优先级',
  `openrank` DOUBLE COMMENT '最新 OpenRank 值',
  `stars` BIGINT COMMENT '最新 Star 总数',
  `period` VARCHAR(10) COMMENT '最新指标周期，如 2026-02',
  `period_type` VARCHAR(10) DEFAULT 'MONTH' COMMENT '周期类型：MONTH/WEEK',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uniq_full` (`full_name`),
  KEY `idx_owner_repo` (`owner`,`repo`)
) COMMENT='仓库主数据表';

-- 仓库按月指标快照（若需要周指标，可复制为 repo_metric_week，period 用 YYYY-Www）
CREATE TABLE IF NOT EXISTS `repo_metric_month` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `repo_id` BIGINT NOT NULL COMMENT '关联 repo.id',
  `period` CHAR(7) NOT NULL COMMENT 'yyyy-MM 月份',
  `openrank` DOUBLE COMMENT 'OpenRank 月度值',
  `stars` BIGINT COMMENT 'Star 总数（当月快照）',
  `attention` DOUBLE COMMENT '关注度（attention）',
  `activity` DOUBLE COMMENT '活跃度（activity）',
  `bus_factor` DOUBLE COMMENT '巴士系数（月度）',
  `bus_factor_detail` JSON COMMENT '巴士系数明细（每人每月）',
  `participants` INT COMMENT '项目参与者人数',
  `technical_fork` BIGINT COMMENT '技术 fork 数',
  `issues_new` INT COMMENT '当月新建 issue 数',
  `issues_closed` INT COMMENT '当月关闭 issue 数',
  `issue_comments` INT COMMENT 'issue 评论数',
  `issue_age` DOUBLE COMMENT 'issue 生命周期（天，开启到关闭）',
  `issue_response_time` DOUBLE COMMENT 'issue 首次响应时长（天）',
  `issue_resolution_duration` DOUBLE COMMENT 'issue 解决时长（天）',
  `change_requests` INT COMMENT 'PR/变更请求数量',
  `change_requests_reviews` INT COMMENT 'PR 审阅者数量',
  `change_request_age` DOUBLE COMMENT 'PR 生命周期（天）',
  `change_request_response_time` DOUBLE COMMENT 'PR 首次响应时长（天）',
  `change_request_resolution_duration` DOUBLE COMMENT 'PR 解决时长（天）',
  `code_change_lines_add` BIGINT COMMENT '代码新增行数',
  `code_change_lines_remove` BIGINT COMMENT '代码删除行数',
  `code_change_lines_sum` BIGINT COMMENT '代码变更总行数',
  `inactive_contributors` INT COMMENT '不活跃贡献者数量',
  `new_contributors` INT COMMENT '新贡献者数量',
  `new_contributors_detail` JSON COMMENT '新贡献者明细列表',
  `contributor_email_suffixes` JSON COMMENT '贡献者邮箱后缀分布',
  `issues_and_change_request_active` INT COMMENT 'issue+PR 活跃数量',
  `activity_details` JSON COMMENT '每人每天活跃度明细（activity_details.json）',
  `active_dates_and_times` JSON COMMENT '每日活跃度（active_dates_and_times.json）',
  `bus_factor_detail_json` JSON COMMENT '巴士系数明细原始 JSON',
  `attention_json` JSON COMMENT '关注度原始 JSON',
  `participants_json` JSON COMMENT '参与者人数原始 JSON',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uniq_repo_period` (`repo_id`,`period`),
  KEY `idx_period` (`period`)
) COMMENT='仓库按月指标快照';

-- 仓库排行榜（周/月）
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

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
  `email` VARCHAR(120) COMMENT '邮箱',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='用户';

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `repo` VARCHAR(256) NOT NULL COMMENT 'owner/repo',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uniq_user_repo` (`user_id`,`repo`),
  KEY `idx_repo` (`repo`)
) COMMENT='用户关注的仓库';
