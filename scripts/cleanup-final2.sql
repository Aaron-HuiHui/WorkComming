USE iwantjob;

-- 绕过 user_badge 防篡改触发器清理测试用户徽章，随后恢复
DROP TRIGGER IF EXISTS trg_user_badge_no_delete;
DELETE FROM user_badge WHERE user_id IN (1,7,8,9,10,11);

DELIMITER $$
CREATE TRIGGER trg_user_badge_no_delete BEFORE DELETE ON user_badge FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'user_badge is immutable after insert';
END$$
DELIMITER ;

-- 测试用户其余关联数据
DELETE FROM mutual_points WHERE user_id IN (1,7,8,9,10,11);
DELETE FROM user_profile WHERE user_id IN (1,7,8,9,10,11);
DELETE FROM sys_user WHERE id IN (1,7,8,9,10,11);

-- 测试徽章模板（v2测试徽章，从未发放）
DELETE FROM badge_template WHERE id=6 AND name LIKE '%测试%';

-- 测试作品及其点赞
DELETE FROM portfolio_like WHERE portfolio_id IN (6,7,8);
DELETE FROM portfolio WHERE id IN (6,7,8);

-- 测试职位（测试科技）
DELETE FROM job WHERE id=7 AND company_name LIKE '%测试%';

-- 测试帖子（API测试残留）
DELETE FROM post WHERE title LIKE '%测试%' OR title LIKE '%全量%';

-- 测试互助请求
DELETE FROM help_group_request WHERE description LIKE '%slow query%';

-- 测试内推码（属于测试用户9）
DELETE FROM internal_referral WHERE user_id=9;

-- 测试薪资上报（全量测试岗，未审核）
DELETE FROM salary_report_data WHERE position LIKE '%测试%' AND verified=0;

-- 未完成的模拟面试记录（保留已完成的）
DELETE FROM mock_interview WHERE status=0 AND user_id=2;

-- 冗余演练会话（保留张三最新完成的一条）
DELETE FROM simulator_choice WHERE session_id IN (4,5,6,7,8,9);
DELETE FROM simulator_session WHERE id IN (4,5,6,7,8,9) AND user_id=2;

-- 连发测试的简历优化日志
DELETE FROM resume_optimization_log WHERE id IN (2,3,4,5);
