-- ==================== 演示用户（密码同 ftetest：Abc123456）====================
INSERT INTO `sys_user` (`username`, `password`, `email`, `role`, `real_name`, `created_at`)
SELECT 'admin', password, 'admin@iwantjob.com', 9, '系统管理员', NOW() FROM sys_user WHERE username='ftetest';
INSERT INTO `sys_user` (`username`, `password`, `email`, `role`, `real_name`, `created_at`)
SELECT 'lisi_dev', password, 'lisi@example.com', 0, '李四', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM sys_user WHERE username='ftetest';
INSERT INTO `sys_user` (`username`, `password`, `email`, `role`, `real_name`, `created_at`)
SELECT 'wangwu_dev', password, 'wangwu@example.com', 0, '王五', DATE_SUB(NOW(), INTERVAL 4 DAY) FROM sys_user WHERE username='ftetest';

INSERT INTO `user_profile` (`user_id`, `school`, `major`, `graduation_year`, `skills`, `bio`, `available_status`)
SELECT id, '同济大学', '计算机科学与技术', 2026, 'Vue,TypeScript,Node.js', '前端方向，热爱开源与设计。', 1 FROM sys_user WHERE username='lisi_dev';
INSERT INTO `user_profile` (`user_id`, `school`, `major`, `graduation_year`, `skills`, `bio`, `available_status`)
SELECT id, '复旦大学', '数据科学', 2026, 'Python,PyTorch,SQL', '算法方向，Kaggle 二银。', 1 FROM sys_user WHERE username='wangwu_dev';

-- ==================== 作品集种子 ====================
INSERT INTO `portfolio` (`user_id`, `title`, `description`, `cover`, `repo_url`, `demo_url`, `tech_tags`, `view_count`, `like_count`, `created_at`) VALUES
((SELECT id FROM sys_user WHERE username='ftetest'), '校园二手交易平台', '基于 SpringBoot+Vue3 的校园二手交易系统，支持即时通讯、信用分与站内担保交易，服务在校生 3000+。', '🛒', 'https://github.com/example/campus-market', 'https://demo.example.com', 'Java,SpringBoot,Vue3,MySQL,Redis', 156, 3, DATE_SUB(NOW(), INTERVAL 6 DAY)),
((SELECT id FROM sys_user WHERE username='ftetest'), '个人记账小程序', '微信小程序记账应用，支持账单统计图表与预算提醒，累计用户 1200+。', '💰', 'https://github.com/example/mini-ledger', NULL, '小程序,TypeScript,ECharts', 89, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
((SELECT id FROM sys_user WHERE username='lisi_dev'), '低代码表单引擎', '可视化拖拽生成表单的开源引擎，支持 20+ 组件与自定义校验规则。', '🧩', 'https://github.com/example/form-engine', 'https://form.example.com', 'Vue3,TypeScript,Vite', 231, 4, DATE_SUB(NOW(), INTERVAL 5 DAY)),
((SELECT id FROM sys_user WHERE username='lisi_dev'), '终端贪吃蛇小游戏', '纯终端实现的贪吃蛇，支持排行榜与关卡编辑器。', '🐍', 'https://github.com/example/term-snake', NULL, 'C++,终端', 67, 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
((SELECT id FROM sys_user WHERE username='wangwu_dev'), 'Kaggle 房价预测 Top 5%', '特征工程 + XGBoost/LightGBM 模型融合方案，Kaggle 比赛前 5%。', '🏠', 'https://github.com/example/house-price', NULL, 'Python,机器学习,XGBoost', 198, 3, DATE_SUB(NOW(), INTERVAL 4 DAY));

INSERT INTO `portfolio_like` (`portfolio_id`, `user_id`)
SELECT p.id, (SELECT id FROM sys_user WHERE username='lisi_dev') FROM portfolio p WHERE p.title='校园二手交易平台';
INSERT INTO `portfolio_like` (`portfolio_id`, `user_id`)
SELECT p.id, (SELECT id FROM sys_user WHERE username='wangwu_dev') FROM portfolio p WHERE p.title='校园二手交易平台';
INSERT INTO `portfolio_like` (`portfolio_id`, `user_id`)
SELECT p.id, (SELECT id FROM sys_user WHERE username='admin') FROM portfolio p WHERE p.title='校园二手交易平台';
INSERT INTO `portfolio_like` (`portfolio_id`, `user_id`)
SELECT p.id, (SELECT id FROM sys_user WHERE username='ftetest') FROM portfolio p WHERE p.title='低代码表单引擎';
INSERT INTO `portfolio_like` (`portfolio_id`, `user_id`)
SELECT p.id, (SELECT id FROM sys_user WHERE username='wangwu_dev') FROM portfolio p WHERE p.title='低代码表单引擎';
INSERT INTO `portfolio_like` (`portfolio_id`, `user_id`)
SELECT p.id, (SELECT id FROM sys_user WHERE username='admin') FROM portfolio p WHERE p.title='低代码表单引擎';
INSERT INTO `portfolio_like` (`portfolio_id`, `user_id`)
SELECT p.id, (SELECT id FROM sys_user WHERE username='ftetest') FROM portfolio p WHERE p.title='Kaggle 房价预测 Top 5%';
INSERT INTO `portfolio_like` (`portfolio_id`, `user_id`)
SELECT p.id, (SELECT id FROM sys_user WHERE username='lisi_dev') FROM portfolio p WHERE p.title='Kaggle 房价预测 Top 5%';
INSERT INTO `portfolio_like` (`portfolio_id`, `user_id`)
SELECT p.id, (SELECT id FROM sys_user WHERE username='admin') FROM portfolio p WHERE p.title='Kaggle 房价预测 Top 5%';
INSERT INTO `portfolio_like` (`portfolio_id`, `user_id`)
SELECT p.id, (SELECT id FROM sys_user WHERE username='ftetest') FROM portfolio p WHERE p.title='终端贪吃蛇小游戏';
INSERT INTO `portfolio_like` (`portfolio_id`, `user_id`)
SELECT p.id, (SELECT id FROM sys_user WHERE username='wangwu_dev') FROM portfolio p WHERE p.title='个人记账小程序';

-- ==================== 职位收藏种子 ====================
INSERT INTO `job_favorite` (`user_id`, `job_id`)
SELECT (SELECT id FROM sys_user WHERE username='ftetest'), id FROM job WHERE title='推荐算法工程师' AND company_name='字节跳动';
INSERT INTO `job_favorite` (`user_id`, `job_id`)
SELECT (SELECT id FROM sys_user WHERE username='ftetest'), id FROM job WHERE title='大模型算法实习生' AND company_name='百度';

-- ==================== 通知种子 ====================
INSERT INTO `notification` (`user_id`, `type`, `title`, `content`, `related_id`, `is_read`, `created_at`) VALUES
((SELECT id FROM sys_user WHERE username='ftetest'), 1, '投递状态更新', '您投递的「Java后端开发工程师」（字节跳动）已进入【面试中】，请留意面试安排。', 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
((SELECT id FROM sys_user WHERE username='ftetest'), 0, '欢迎加入我要工作', '完善个人资料与简历，即可解锁 AI 简历优化、岗位市场分析等全部功能。', NULL, 0, NOW()),
((SELECT id FROM sys_user WHERE username='lisi_dev'), 0, '欢迎加入我要工作', '完善个人资料与简历，即可解锁 AI 简历优化、岗位市场分析等全部功能。', NULL, 0, DATE_SUB(NOW(), INTERVAL 2 DAY));

SELECT 'companies' t, COUNT(*) c FROM company
UNION ALL SELECT 'jobs', COUNT(*) FROM job
UNION ALL SELECT 'portfolios', COUNT(*) FROM portfolio
UNION ALL SELECT 'portfolio_likes', COUNT(*) FROM portfolio_like
UNION ALL SELECT 'job_favorites', COUNT(*) FROM job_favorite
UNION ALL SELECT 'notifications', COUNT(*) FROM notification
UNION ALL SELECT 'users', COUNT(*) FROM sys_user;