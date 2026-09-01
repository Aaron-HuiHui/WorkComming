USE iwantjob;

-- ============ 测试数据清理（第十九阶段E2E测试残留） ============
-- 原则：删除带"测试/v2/v3/全量/smoke/fctest"标记的残留与未完成的演练记录，
--       保留正规演示数据（张三的投递/徽章/通知、demo_hr 的职位、已审核薪资等）

-- 1. 测试用户 smoketest001/fctest_*/Aaron 及其关联数据
DELETE FROM simulator_choice WHERE session_id IN (SELECT id FROM (SELECT id FROM simulator_session WHERE user_id IN (1,7,8,9,10,11)) t);
DELETE FROM simulator_session WHERE user_id IN (1,7,8,9,10,11);
DELETE FROM badge_lock_log WHERE user_badge_id IN (SELECT id FROM (SELECT id FROM user_badge WHERE user_id IN (1,7,8,9,10,11)) t);
DELETE FROM user_badge WHERE user_id IN (1,7,8,9,10,11);
DELETE FROM mutual_points WHERE user_id IN (1,7,8,9,10,11);
DELETE FROM user_profile WHERE user_id IN (1,7,8,9,10,11);
DELETE FROM sys_user WHERE id IN (1,7,8,9,10,11);

-- 2. 测试徽章模板（v2测试徽章，从未发放）
DELETE FROM badge_template WHERE id=6 AND name LIKE '%测试%';

-- 3. 测试作品及其点赞（v3测试作品/测试作品-张三/已软删的全量测试作品）
DELETE FROM portfolio_like WHERE portfolio_id IN (6,7,8);
DELETE FROM portfolio WHERE id IN (6,7,8);

-- 4. 测试职位（测试科技；job 5/38"测试开发工程师"为正规职位，保留）
DELETE FROM job WHERE id=7 AND company_name LIKE '%测试%';

-- 5. 测试帖子（API测试残留，前端无社区页面）
DELETE FROM post WHERE title LIKE '%测试%' OR title LIKE '%全量%';

-- 6. 测试互助请求（英文测试数据）
DELETE FROM help_group_request WHERE description LIKE '%slow query%';

-- 7. 测试内推码（属于测试用户9）
DELETE FROM internal_referral WHERE user_id=9;

-- 8. 测试薪资上报（全量测试岗，未审核通过）
DELETE FROM salary_report_data WHERE position LIKE '%测试%' AND verified=0;

-- 9. 未完成/冗余的模拟面试记录（保留已完成的 id 1、6）
DELETE FROM mock_interview WHERE status=0 AND user_id=2;

-- 10. 冗余演练会话（保留张三最新完成的一条 id=10 及其选择记录）
DELETE FROM simulator_choice WHERE session_id IN (4,5,6,7,8,9);
DELETE FROM simulator_session WHERE id IN (4,5,6,7,8,9) AND user_id=2;

-- 11. 连发测试的简历优化日志（19:57 三连发，保留 1/6/7）
DELETE FROM resume_optimization_log WHERE id IN (2,3,4,5);
