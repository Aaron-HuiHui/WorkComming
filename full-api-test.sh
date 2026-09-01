#!/bin/bash
# 全量功能测试脚本 —— 「我要工作」第十二阶段
# 用法: bash full-api-test.sh  (输出 PASS/FAIL 汇总)
BASE="http://localhost:8000/api"
PASS=0; FAIL=0; FAILED_CASES=""

# ---------- 工具函数 ----------
req() { # req METHOD PATH TOKEN [BODY_FILE] -> 输出 "HTTP_CODE|BODY"
  local m=$1 p=$2 t=$3 b=$4
  local args=(-s -o /tmp/_resp.json -w "%{http_code}" --max-time 25 -X "$m" "$BASE$p")
  [ -n "$t" ] && args+=(-H "Authorization: Bearer $t")
  [ -n "$b" ] && args+=(-H "Content-Type: application/json" --data @"$b")
  local code; code=$(curl "${args[@]}")
  echo "$code|$(head -c 500 /tmp/_resp.json)"
}
check() { # check 描述 METHOD PATH TOKEN [BODY_FILE] [期望HTTP] [期望code字段,空=0]
  local desc=$1 m=$2 p=$3 t=$4 b=$5 exp_http=${6:-200} exp_code=${7:-0}
  local out; out=$(req "$m" "$p" "$t" "$b")
  local code=${out%%|*} body=${out#*|}
  local bcode; bcode=$(echo "$body" | sed -n 's/.*"code":\([0-9-]*\).*/\1/p')
  if [ "$code" = "$exp_http" ] && [ "$bcode" = "$exp_code" ]; then
    PASS=$((PASS+1)); echo "PASS [$desc] $m $p -> $code/code=$bcode"
  else
    FAIL=$((FAIL+1)); FAILED_CASES="$FAILED_CASES\nFAIL [$desc] $m $p -> HTTP=$code(期望$exp_http) code=$bcode(期望$exp_code) body=${body:0:200}"
    echo "FAIL [$desc] $m $p -> HTTP=$code code=$bcode"
  fi
}

# ---------- 0. 登录三角色 ----------
STU=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"ftetest","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
HR=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"demo_hr","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
ADM=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
[ -z "$STU" ] && { echo "FATAL: ftetest login failed"; exit 1; }
[ -z "$HR" ] && { echo "FATAL: demo_hr login failed"; exit 1; }
[ -z "$ADM" ] && echo "WARN: admin login failed (admin cases will fail)"

echo "=== [1] 认证/用户/积分/通知 ==="

check "用户信息" GET /user/me "$STU"
check "更新资料" PUT /user/profile "$STU" /tmp/b_empty.json 2>/dev/null || true
printf '{"bio":"全量功能测试"}' > /tmp/b_profile.json; check "更新资料" PUT /user/profile "$STU" /tmp/b_profile.json
check "积分查询" GET /points/me "$STU"
check "通知列表" GET "/notify/me?page=1&size=10" "$STU"
check "未读计数" GET /notify/me/unread-count "$STU"
check "刷新令牌-无参负例" POST /auth/refresh "" /tmp/b_profile.json 400 -1

echo "=== [2] 职位模块(job-server 8082) ==="
check "职位列表" GET "/jobs?page=1&size=10" "$STU"
check "职位搜索ES" GET "/jobs?keyword=Java&page=1&size=10" "$STU"
check "职位详情" GET /jobs/1 "$STU"
check "岗位统计" GET /jobs/stats/overview "$STU"
check "我发布的(学生403)" GET "/jobs/me/published?page=1&size=10" "$STU" "" 403 -1
check "我发布的(HR)" GET "/jobs/me/published?page=1&size=10" "$HR"
check "投递者列表(HR)" GET "/jobs/1/applications?page=1&size=10" "$HR"
check "我的收藏" GET "/jobs/me/favorites?page=1&size=10" "$STU"
check "收藏ID列表" GET /jobs/me/favorite-ids "$STU"
check "收藏切换" POST /jobs/2/favorite "$STU"
check "收藏切换恢复" POST /jobs/2/favorite "$STU"
check "我的企业列表" GET "/companies?page=1&size=15" "$STU"
check "企业详情" GET /companies/1 "$STU"
check "企业更新(学生403)" PUT /companies/1 "$STU" /tmp/b_empty.json 403 -1 || true
printf '{"intro":"全量测试企业介绍","culture":"开放","benefits":"下午茶"}' > /tmp/b_company.json
check "企业更新(无发布权403/业务拒绝)" PUT /companies/1 "$HR" /tmp/b_company.json 200 -1 || true

echo "=== [3] 徽章 ==="
check "徽章模板" GET /badges/templates "$STU"
check "我的徽章" GET /user/badges "$STU"
check "他人徽章" GET /badges/user/2 "$STU"
check "徽章验证" GET "/badges/verify?userId=2&badgeId=1" "$STU" "" -2 || true

echo "=== [4] 简历 ==="
check "我的简历" GET /resume/me "$STU"
printf '{"title":"全量测试简历","content":"测试内容","skills":"Java"}' > /tmp/b_resume.json
check "新建简历" POST /resume "$STU" /tmp/b_resume.json
RES_ID=$(curl -s -X POST "$BASE/resume" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" -d @/tmp/b_resume.json | sed -n 's/.*"data":\([0-9]*\).*/\1/p')
[ -n "$RES_ID" ] && {
  check "简历详情" GET "/resume/$RES_ID" "$STU"
  printf '{"title":"全量测试简历-改"}' > /tmp/b_resume2.json
  check "更新简历" PUT "/resume/$RES_ID" "$STU" /tmp/b_resume2.json
  printf '{"action":"polish","resumeId":%s}' "$RES_ID" > /tmp/b_opt.json
  check "AI润色" POST /resume/optimize "$STU" /tmp/b_opt.json
  printf '{"resumeId":%s}' "$RES_ID" > /tmp/b_score.json
  check "AI评分" POST /resume/score "$STU" /tmp/b_score.json
  check "简历匹配" GET "/resume/match?jobId=1" "$STU"
  check "删除简历" DELETE "/resume/$RES_ID" "$STU"
}

echo "=== [5] 模拟舱 ==="
check "场景列表" GET /simulator/scenarios "$STU"
check "场景详情" GET /simulator/scenarios/1 "$STU"
SID=$(curl -s -X POST "$BASE/simulator/start?scenarioId=1" -H "Authorization: Bearer $STU" | sed -n 's/.*"sessionId":\([0-9]*\).*/\1/p')
[ -n "$SID" ] && {
  printf '{"sessionId":%s,"optionId":1}' "$SID" > /tmp/b_choose.json
  check "选择推进(完成)" POST /simulator/choose "$STU" /tmp/b_choose.json
  check "会话报告" GET "/simulator/session/$SID/report" "$STU"
}
check "我的会话" GET "/simulator/sessions/me?page=1&size=10" "$STU"

echo "=== [6] 面试 ==="
check "题库列表" GET "/interview/questions?page=1&size=10" "$STU"
check "题目详情" GET /interview/questions/1 "$STU"
printf '{"questionId":1}' > /tmp/b_iv_start.json
MID=$(curl -s -X POST "$BASE/interview/start" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" -d @/tmp/b_iv_start.json | sed -n 's/.*"data":\([0-9]*\).*/\1/p')
[ -n "$MID" ] && {
  printf '{"mockId":%s,"questionId":1,"answer":"全量功能测试回答"}' "$MID" > /tmp/b_iv_ans.json
  check "面试作答" POST /interview/answer "$STU" /tmp/b_iv_ans.json
  check "面试详情" GET "/interview/$MID" "$STU"
  check "结束面试" POST "/interview/$MID/end" "$STU"
}
check "面试历史" GET "/interview/history?page=1&size=10" "$STU"

echo "=== [7] 薪资白皮书 ==="
printf '{"companyName":"测试公司","position":"测试工程师","salaryTotal":200000,"city":"北京","education":"本科","experienceYear":3}' > /tmp/b_contrib.json
check "薪资贡献" POST /salary/contribute "$STU" /tmp/b_contrib.json 200 -1 || true
check "我的贡献" GET "/salary/contributions/me?page=1&size=10" "$STU"
check "白皮书最新" GET /salary/whitepaper/latest "$STU" "" 200 -1 || true

echo "=== [8] 社区 ==="
printf '{"title":"全量测试帖","content":"这是一个功能测试帖子","type":0}' > /tmp/b_post.json
POST_ID=$(curl -s -X POST "$BASE/posts" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" -d @/tmp/b_post.json | sed -n 's/.*"data":\([0-9]*\).*/\1/p')
check "帖子列表" GET "/posts?page=1&size=10" "$STU"
[ -n "$POST_ID" ] && {
  check "帖子详情" GET "/posts/$POST_ID" "$STU"
  printf '{"content":"全量测试回答"}' > /tmp/b_answer.json
  check "帖子回答" POST "/posts/$POST_ID/answer" "$STU" /tmp/b_answer.json
}

echo "=== [9] 帮帮团 ==="
printf '{"title":"全量测试求助","content":"需要一个懂ES的帮忙","skillTag":"elasticsearch","rewardPoints":10}' > /tmp/b_help.json
HELP_ID=$(curl -s -X POST "$BASE/help-group/request" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" -d @/tmp/b_help.json | sed -n 's/.*"data":\([0-9]*\).*/\1/p')
check "求助列表" GET /help-group/requests "$STU"
check "我的帮帮团" GET /help-group/me "$STU"

echo "=== [10] 作品集 ==="
check "作品广场" GET "/portfolio?page=1&size=10" "$STU"
printf '{"title":"全量测试作品","description":"测试","githubUrl":"https://github.com/test/test"}' > /tmp/b_pf.json
PF_ID=$(curl -s -X POST "$BASE/portfolio" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" -d @/tmp/b_pf.json | sed -n 's/.*"data":\([0-9]*\).*/\1/p')
[ -n "$PF_ID" ] && {
  check "作品详情" GET "/portfolio/$PF_ID" "$STU"
  check "作品点赞" POST "/portfolio/$PF_ID/like" "$STU"
  check "我的作品" GET /portfolio/me "$STU"
  check "删除作品" DELETE "/portfolio/$PF_ID" "$STU"
}

echo "=== [11] 管理端 ==="
check "运营看板" GET /admin/overview "$ADM"
check "看板学生403" GET /admin/overview "$STU" "" 403 -1
check "徽章模板管理" GET /admin/badges/templates "$ADM"
check "待审薪资" GET "/admin/salary/pending?page=1&size=10" "$ADM"

echo "=== [12] HR 工作流 ==="
check "投递状态流转负例(越权)" PUT /jobs/applications/1/status "$STU" /tmp/b_empty.json 403 -1 || true
printf '{"status":1,"remark":"初筛通过-全量测试"}' > /tmp/b_status.json
APP_ID=$(curl -s -X GET "$BASE/jobs/1/applications?page=1&size=1" -H "Authorization: Bearer $HR" | sed -n 's/.*"records":\[{"id":\([0-9]*\).*/\1/p')
if [ -n "$APP_ID" ]; then
  check "候选人详情" GET "/jobs/applications/$APP_ID/candidate" "$HR"
  check "状态流转" PUT "/jobs/applications/$APP_ID/status" "$HR" /tmp/b_status.json
fi

echo ""
echo "==================== 汇总 ===================="
echo "PASS: $PASS"
echo "FAIL: $FAIL"
[ $FAIL -gt 0 ] && echo -e "失败明细:$FAILED_CASES"
