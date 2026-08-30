-- =============================================================================
-- 我要工作 平台 - 完整建表脚本（修订版 v2.0）
-- 文档参考：开发文档 v2.0 第5.3章
-- 执行环境：MySQL 8.0
-- 字符集：utf8mb4
-- 设计规范：
--   1. 移除所有跨模块物理外键，仅保留索引（应用层校验，支撑未来分库）
--   2. 业务表统一含 is_deleted/created_at/updated_at（MyBatis-Plus 自动填充）
--   3. 枚举值统一抽 iwantjob-common/enums 包，DB 注释标注枚举类名
--   4. 余额类字段（mutual_points.balance）配 version 乐观锁
--   5. 软删除统一：MyBatis-Plus @TableLogic，删除即 is_deleted=1
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `iwantjob` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `iwantjob`;

-- 关闭外键检查，避免触发器与表创建顺序问题
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 用户与认证 ====================
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` tinyint NOT NULL DEFAULT '0' COMMENT 'UserRoleEnum: 0-学生,1-校友,2-HR,3-导师,9-管理员',
  `real_name` varchar(50) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '0-禁用,1-正常',
  `last_login` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户主表';

CREATE TABLE `user_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `school` varchar(100) DEFAULT NULL,
  `major` varchar(100) DEFAULT NULL,
  `graduation_year` int DEFAULT NULL,
  `skills` varchar(500) DEFAULT NULL,
  `bio` varchar(500) DEFAULT NULL,
  `available_status` tinyint DEFAULT '1',
  `resume_id` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料';

CREATE TABLE `user_auth` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `identity_type` varchar(30) NOT NULL,
  `id_card_image` varchar(255) DEFAULT NULL,
  `verified` tinyint DEFAULT '0',
  `verify_comment` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_auth` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户实名认证';

-- [v2.0新增] 审计日志
CREATE TABLE `sys_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `action` varchar(50) NOT NULL COMMENT '操作类型，如 SALARY_REVIEW/BADGE_LOCK/POINT_ADJUST/USER_BAN',
  `target_type` varchar(30) DEFAULT NULL,
  `target_id` bigint DEFAULT NULL,
  `detail` json DEFAULT NULL,
  `ip` varchar(50) DEFAULT NULL,
  `ua` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_audit_user` (`user_id`),
  KEY `idx_audit_action` (`action`),
  KEY `idx_audit_time` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统审计日志';

-- ==================== 职位相关 ====================
CREATE TABLE `job` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `company_name` varchar(100) NOT NULL,
  `job_type` tinyint NOT NULL COMMENT 'JobTypeEnum: 0-实习,1-校招,2-社招',
  `description` text,
  `requirements` text,
  `salary_range` varchar(50) DEFAULT NULL,
  `location` varchar(100) DEFAULT NULL,
  `source` tinyint DEFAULT '0',
  `contact_email` varchar(100) DEFAULT NULL,
  `expiry_date` datetime DEFAULT NULL,
  `view_count` int DEFAULT '0',
  `poster_id` bigint DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_job_title` (`title`),
  KEY `idx_job_company` (`company_name`),
  KEY `idx_job_location` (`location`),
  KEY `idx_job_type` (`job_type`),
  KEY `idx_job_expiry` (`expiry_date`),
  KEY `idx_job_poster` (`poster_id`),
  FULLTEXT KEY `ft_job_search` (`title`,`description`,`requirements`)  -- [v2.0] ES未上时兜底
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位';

CREATE TABLE `job_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `resume_id` bigint DEFAULT NULL,
  `cover_letter` text,
  `status` tinyint DEFAULT '0' COMMENT 'ApplicationStatusEnum: 0-投递成功,1-初筛,2-面试,3-录用,4-拒绝',
  `hr_remark` varchar(255) DEFAULT NULL,
  `applied_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_application_job` (`job_id`),
  KEY `idx_application_user` (`user_id`),
  KEY `idx_application_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位投递';

CREATE TABLE `internal_referral` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `referral_code` varchar(20) NOT NULL,
  `max_count` int DEFAULT '10',
  `used_count` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_referral_code` (`referral_code`),
  KEY `idx_referral_user` (`user_id`),
  KEY `idx_referral_job` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内推码';

-- ==================== 简历 ====================
CREATE TABLE `resume` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(100) DEFAULT NULL,
  `content_json` json NOT NULL,
  `ai_score` int DEFAULT NULL,
  `is_default` tinyint DEFAULT '0',
  `version` int DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_resume_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历';

CREATE TABLE `resume_optimization_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resume_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `original_text` text,
  `optimized_text` text,
  `type` tinyint NOT NULL COMMENT 'OptimTypeEnum: 0-润色,1-翻译,2-强化',
  `feedback` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_optim_log_resume` (`resume_id`),
  KEY `idx_optim_log_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历优化日志';

-- ==================== 模拟面试 ====================
CREATE TABLE `mock_interview` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT 'InterviewTypeEnum: 0-技术,1-行为,2-综合',
  `difficulty` tinyint DEFAULT '1',
  `target_job` varchar(100) DEFAULT NULL,
  `status` tinyint DEFAULT '0' COMMENT 'MockStatusEnum: 0-进行中,1-完成,2-中断',
  `score_summary` json DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_mock_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟面试会话';

CREATE TABLE `interview_question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mock_id` bigint NOT NULL,
  `question_text` text NOT NULL,
  `answer_text` text,
  `ai_feedback` text,
  `reference_answer` text,
  `sort_order` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_iq_mock` (`mock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试问答明细';

CREATE TABLE `question_bank` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` tinyint NOT NULL COMMENT 'InterviewTypeEnum: 0-技术,1-行为,2-综合',
  `sub_category` varchar(50) DEFAULT NULL,
  `question_text` text NOT NULL,
  `expected_keywords` text,
  `difficulty` tinyint DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_qb_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库';

-- ==================== 社区 ====================
CREATE TABLE `post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `author_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT 'PostTypeEnum: 0-问答,1-面经,2-技能交换,3-生活互助,4-其他',
  `title` varchar(200) NOT NULL,
  `content` text,
  `tags` varchar(200) DEFAULT NULL,
  `view_count` int DEFAULT '0',
  `like_count` int DEFAULT '0',
  `is_pinned` tinyint DEFAULT '0',
  `is_solved` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_post_author` (`author_id`),
  KEY `idx_post_type` (`type`),
  KEY `idx_post_created` (`created_at`),
  FULLTEXT KEY `ft_post_search` (`title`,`content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子';

CREATE TABLE `answer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `author_id` bigint NOT NULL,
  `content` text NOT NULL,
  `is_accepted` tinyint DEFAULT '0',
  `like_count` int DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_answer_post` (`post_id`),
  KEY `idx_answer_author` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回答';

CREATE TABLE `skill_exchange` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint NOT NULL,
  `to_user_id` bigint DEFAULT NULL,
  `offer_skill` varchar(50) DEFAULT NULL,
  `want_skill` varchar(50) DEFAULT NULL,
  `status` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_skill_from` (`from_user_id`),
  KEY `idx_skill_to` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能交换';

CREATE TABLE `crowdfunding_project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `initiator_id` bigint NOT NULL,
  `title` varchar(100) DEFAULT NULL,
  `description` text,
  `goal_amount` decimal(10,2) NOT NULL,
  `current_amount` decimal(10,2) DEFAULT '0.00',
  `status` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `end_date` datetime DEFAULT NULL,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_cf_initiator` (`initiator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='众筹项目';

-- ==================== 帮帮团 ====================
CREATE TABLE `help_group_request` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `applicant_id` bigint NOT NULL,
  `reason_type` tinyint NOT NULL COMMENT 'ReasonTypeEnum: 0-投递失败,1-面试屡败,2-职业迷茫,3-其他',
  `description` text,
  `match_tags` varchar(200) DEFAULT NULL COMMENT '[v2.0新增] 匹配标签，如 目标行业/城市，支撑自动匹配',
  `status` tinyint DEFAULT '0' COMMENT 'HelpStatusEnum: 0-待匹配,1-已匹配,2-完成,3-关闭',
  `supporter_id` bigint DEFAULT NULL,
  `matched_at` datetime DEFAULT NULL,
  `resolved_at` datetime DEFAULT NULL,
  `feedback` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_help_applicant` (`applicant_id`),
  KEY `idx_help_status` (`status`),
  KEY `idx_help_supporter` (`supporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帮帮团求助';

-- ==================== 积分（归属 user 模块） ====================
CREATE TABLE `mutual_points` (
  `user_id` bigint NOT NULL,
  `balance` int DEFAULT '0',
  `total_earned` int DEFAULT '0',
  `version` int DEFAULT '0' COMMENT '[v2.0新增] 乐观锁版本号',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互助积分账户';

CREATE TABLE `point_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `points` int NOT NULL COMMENT '正数增加，负数扣减',
  `reason` varchar(100) NOT NULL,
  `related_id` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pt_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水';

CREATE TABLE `unlock_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `benefit` tinyint NOT NULL COMMENT 'BenefitEnum: 0-导师咨询,1-高级简历优化,2-模拟面试次数,3-其他',
  `cost_points` int NOT NULL,
  `status` tinyint DEFAULT '1',
  `used_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_unlock_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权益解锁记录';

-- ==================== AI 对话 ====================
CREATE TABLE `ai_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `scene` tinyint NOT NULL COMMENT 'AiSceneEnum: 0-简历,1-面试,2-职位推荐,3-通用',
  `title` varchar(100) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_ai_conv_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话';

CREATE TABLE `ai_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NOT NULL,
  `role` varchar(10) NOT NULL COMMENT 'user/assistant',
  `content` text NOT NULL,
  `token_count` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_msg_conv` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI消息';

-- ==================== 薪资白皮书（创新1） ====================
CREATE TABLE `salary_report_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '仅用于积分发放与去重，对外查询禁止按user_id反查明细',
  `city` varchar(50) DEFAULT NULL,
  `position` varchar(100) DEFAULT NULL,
  `salary_min` int DEFAULT NULL,
  `salary_max` int DEFAULT NULL,
  `company_scale` varchar(30) DEFAULT NULL COMMENT '仅规模档位，不存公司全称',
  `industry` varchar(50) DEFAULT NULL,
  `job_type` tinyint DEFAULT NULL COMMENT 'JobTypeEnum: 0-实习,1-全职',
  `education_level` tinyint DEFAULT NULL COMMENT 'EduEnum: 0-专科,1-本科,2-硕士,3-博士,4-其他',
  `is_double_first_class` tinyint DEFAULT NULL COMMENT '0-否,1-是',
  `offer_month` char(7) DEFAULT NULL,
  `is_anonymous` tinyint DEFAULT '1' COMMENT '默认1且不可改为0',
  `verified` tinyint DEFAULT '0' COMMENT '0-待审核,1-通过,2-驳回',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_salary_user` (`user_id`),
  KEY `idx_salary_city_pos` (`city`, `position`),
  KEY `idx_salary_verified` (`verified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资贡献数据';

CREATE TABLE `salary_whitepaper` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `version` varchar(20) DEFAULT NULL,
  `title` varchar(100) DEFAULT NULL,
  `report_json` json,
  `generated_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `access_level` tinyint DEFAULT '0' COMMENT '0-公开,1-贡献者专属',
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资白皮书';

CREATE TABLE `salary_contribution_reward` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `report_data_id` bigint DEFAULT NULL,
  `points_awarded` int DEFAULT '0',
  `unlock_match_boost` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_scr_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资贡献奖励';

-- [v2.0新增] 薪资审核流转日志
CREATE TABLE `salary_review_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `report_data_id` bigint NOT NULL,
  `reviewer_id` bigint DEFAULT NULL,
  `action` varchar(20) NOT NULL COMMENT 'APPROVE/REJECT',
  `comment` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_review_data` (`report_data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资审核日志';

-- ==================== AI 职业模拟舱（创新2） ====================
CREATE TABLE `simulator_scenario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `type` tinyint NOT NULL COMMENT 'ScenarioTypeEnum: 0-入职,1-向上汇报,2-冲突处理,3-跨部门协作',
  `description` text,
  `initial_context` text,
  `start_node_id` bigint DEFAULT NULL COMMENT '[v2.0新增] 起始节点',
  `difficulty` tinyint DEFAULT '1',
  `is_active` tinyint DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟舱场景';

-- [v2.0新增] 节点表（显式建模分支图）
CREATE TABLE `simulator_node` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `scenario_id` bigint NOT NULL,
  `node_desc` text NOT NULL,
  `ai_prompt_snippet` text DEFAULT NULL COMMENT '注入大模型的场景片段提示',
  `is_end` tinyint DEFAULT '0',
  `sort_order` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_node_scenario` (`scenario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟舱节点';

-- [v2.0新增] 节点选项表
CREATE TABLE `simulator_node_option` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `node_id` bigint NOT NULL,
  `option_text` varchar(500) NOT NULL,
  `next_node_id` bigint DEFAULT NULL,
  `soft_skill_tags` varchar(200) DEFAULT NULL COMMENT '选择该项触发的软技能标签',
  PRIMARY KEY (`id`),
  KEY `idx_option_node` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟舱节点选项';

CREATE TABLE `simulator_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `scenario_id` bigint NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT 'SimStatusEnum: 0-进行中,1-已完成,2-中断',
  `current_node_id` bigint DEFAULT NULL,
  `started_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `completed_at` datetime DEFAULT NULL,
  `overall_score` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_sim_user` (`user_id`),
  KEY `idx_sim_scenario` (`scenario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟舱会话';

CREATE TABLE `simulator_choice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `node_id` bigint DEFAULT NULL,
  `node_desc` text,
  `options_json` json COMMENT '[v2.0变更] 当时呈现的选项快照',
  `option_id` bigint DEFAULT NULL COMMENT '[v2.0变更] 用户选择的选项ID',
  `user_choice` varchar(500) DEFAULT NULL COMMENT '选项文本冗余',
  `ai_feedback` text,
  `soft_skill_tags` varchar(200) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_choice_session` (`session_id`),
  KEY `idx_choice_node` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟舱选择记录';

-- ==================== 成就徽章（创新3） ====================
CREATE TABLE `badge_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  `icon_url` varchar(255) DEFAULT NULL,
  `condition_type` tinyint NOT NULL COMMENT 'BadgeCondEnum: 0-分享面经次数,1-帮助他人次数,2-薪资贡献,3-模拟舱完成,4-项目合作评价',
  `threshold` int DEFAULT '1',
  `rarity` tinyint DEFAULT '0' COMMENT 'RarityEnum: 0-普通,1-稀有,2-史诗',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='徽章模板';

CREATE TABLE `user_badge` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `badge_id` bigint NOT NULL,
  `earned_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_locked` tinyint DEFAULT '0' COMMENT '铸造后立即置1，不可回退',
  `lock_hash` varchar(64) DEFAULT NULL COMMENT 'SHA256(user_id+badge_id+earned_at+system_salt)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_badge` (`user_id`, `badge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户徽章';

-- [v2.0新增] 防篡改约束：仅允许INSERT，禁止UPDATE/DELETE（DB触发器）
-- 说明：业务层铸造徽章后立即 is_locked=1 + lock_hash，触发器阻止后续任何修改
DELIMITER //
CREATE TRIGGER `trg_user_badge_no_update`
BEFORE UPDATE ON `user_badge`
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'user_badge is immutable after insert';
END//
CREATE TRIGGER `trg_user_badge_no_delete`
BEFORE DELETE ON `user_badge`
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'user_badge is immutable after insert';
END//
DELIMITER ;

CREATE TABLE `badge_lock_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_badge_id` bigint NOT NULL,
  `operated_by` bigint DEFAULT NULL COMMENT 'null表示系统自动',
  `action` varchar(20) DEFAULT 'LOCK',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_lock_badge` (`user_badge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='徽章锁定日志';

-- ==================== 薪资审核基准（[agent-db 扩展] 审核规则参考） ====================
CREATE TABLE `salary_baseline` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `city` varchar(50) NOT NULL COMMENT '城市，如 北京/上海',
  `education_level` tinyint NOT NULL COMMENT 'EduEnum: 0-专科,1-本科,2-硕士,3-博士,4-其他',
  `job_type` tinyint NOT NULL COMMENT 'JobTypeEnum: 0-实习,1-全职',
  `salary_min` int NOT NULL COMMENT '最低月薪参考',
  `salary_p99` int NOT NULL COMMENT 'P99 月薪参考（审核上限）',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_baseline` (`city`, `education_level`, `job_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资审核基准';

SET FOREIGN_KEY_CHECKS = 1;
