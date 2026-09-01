-- ==================== 第七阶段：微服务 + 企业库 + 作品集 + 通知/收藏/面试日程 ====================

CREATE TABLE `company` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '公司名称',
  `industry` varchar(50) DEFAULT NULL COMMENT '行业',
  `scale` varchar(30) DEFAULT NULL COMMENT '规模',
  `headquarters` varchar(50) DEFAULT NULL COMMENT '总部',
  `logo` varchar(255) DEFAULT NULL COMMENT 'LOGO（emoji）',
  `intro` text COMMENT '企业介绍',
  `culture` varchar(500) DEFAULT NULL COMMENT '企业文化',
  `welfare` varchar(500) DEFAULT NULL COMMENT '福利待遇',
  `website` varchar(255) DEFAULT NULL COMMENT '官网',
  `claimed_by` bigint DEFAULT NULL COMMENT '认领HR用户ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业信息';

CREATE TABLE `portfolio` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(100) NOT NULL COMMENT '作品标题',
  `description` text COMMENT '作品描述',
  `cover` varchar(50) DEFAULT NULL COMMENT '封面（emoji）',
  `repo_url` varchar(255) DEFAULT NULL COMMENT '仓库链接',
  `demo_url` varchar(255) DEFAULT NULL COMMENT '演示链接',
  `tech_tags` varchar(200) DEFAULT NULL COMMENT '技术标签（逗号分隔）',
  `view_count` int DEFAULT '0',
  `like_count` int DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_portfolio_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生作品集';

CREATE TABLE `portfolio_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `portfolio_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_portfolio_like` (`portfolio_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品点赞';

CREATE TABLE `job_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_favorite` (`user_id`, `job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位收藏';

CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` tinyint DEFAULT '0' COMMENT '0系统/1投递状态/2面试邀请',
  `title` varchar(100) NOT NULL,
  `content` varchar(500) DEFAULT NULL,
  `related_id` bigint DEFAULT NULL,
  `is_read` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_notify_user` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知';

ALTER TABLE `job`
  ADD COLUMN `company_id` bigint DEFAULT NULL AFTER `company_name`,
  ADD COLUMN `recruitment_batch` tinyint DEFAULT '0' COMMENT '0日常/1春招/2秋招/3实习批' AFTER `company_id`;

ALTER TABLE `job_application`
  ADD COLUMN `interview_time` datetime DEFAULT NULL,
  ADD COLUMN `interview_location` varchar(100) DEFAULT NULL,
  ADD COLUMN `interview_note` varchar(255) DEFAULT NULL;