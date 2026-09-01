USE iwantjob;
SELECT id, name FROM company WHERE name LIKE '%测试%';
SELECT 'job_application' t, COUNT(*) c FROM job_application WHERE user_id IN (1,7,8,9,10) OR job_id=7
UNION ALL SELECT 'job_favorite', COUNT(*) FROM job_favorite WHERE user_id IN (1,7,8,9,10) OR job_id=7
UNION ALL SELECT 'notification', COUNT(*) FROM notification WHERE user_id IN (1,7,8,9,10)
UNION ALL SELECT 'user_badge', COUNT(*) FROM user_badge WHERE user_id IN (1,7,8,9,10)
UNION ALL SELECT 'portfolio_like', COUNT(*) FROM portfolio_like WHERE user_id IN (1,7,8,9,10) OR portfolio_id IN (7,8)
UNION ALL SELECT 'user_profile', COUNT(*) FROM user_profile WHERE user_id IN (1,7,8,9,10)
UNION ALL SELECT 'user_auth', COUNT(*) FROM user_auth WHERE user_id IN (1,7,8,9,10)
UNION ALL SELECT 'resume', COUNT(*) FROM resume WHERE user_id IN (1,7,8,9,10)
UNION ALL SELECT 'post', COUNT(*) FROM post WHERE author_id IN (1,7,8,9,10)
UNION ALL SELECT 'point_transaction', COUNT(*) FROM point_transaction WHERE user_id IN (1,7,8,9,10)
UNION ALL SELECT 'badge6_awarded', COUNT(*) FROM user_badge WHERE badge_id=6;
