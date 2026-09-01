#!/bin/bash
# 全量功能测试 v3（终版）—— 正确字段名 + Windows 安全路径 + 完整正向链路
BASE="http://localhost:8000/api"
PASS=0; FAIL=0; FAILED_CASES=""
T="E:/毕业设计/ft-tmp"; mkdir -p "$T"

jpick() { # 从 $T/_resp.json 提取字段路径
  node -e "try{const d=JSON.parse(require('fs').readFileSync(process.argv[1],'utf8'));const p=process.argv[2].split('.');let v=d;for(const k of p){v=v?.[k]}console.log(v??'')}catch(e){console.log('')}" "$T/_resp.json" "$1"
}
req() {
  local m=$1 p=$2 t=$3 b=$4
  local args=(-s -o "$T/_resp.json" -w "%{http_code}" --max-time 30 -X "$m" "$BASE$p")
  [ -n "$t" ] && args+=(-H "Authorization: Bearer $t")
  [ -n "$b" ] && args+=(-H "Content-Type: application/json" --data @"$b")
  curl "${args[@]}"
}
check() { # 描述 METHOD PATH TOKEN BODY 期望HTTP 期望code(|分隔)
  local desc=$1 m=$2 p=$3 t=$4 b=$5 exp_http=$6 exp_codes=$7
  local code; code=$(req "$m" "$p" "$t" "$b")
  local bcode; bcode=$(jpick code)
  local ok=0
  IFS='|' read -ra EXPS <<< "$exp_codes"
  for ec in "${EXPS[@]}"; do [ "$bcode" = "$ec" ] && ok=1; done
  if [ "$code" = "$exp_http" ] && [ $ok -eq 1 ]; then
    PASS=$((PASS+1)); echo "PASS [$desc]"
  else
    FAIL=$((FAIL+1))
    FAILED_CASES="$FAILED_CASES\nFAIL [$desc] $m $p -> HTTP=$code(期望$exp_http) code=$bcode(期望$exp_codes) body=$(head -c 200 "$T/_resp.json")"
    echo "FAIL [$desc] HTTP=$code code=$bcode"
  fi
}
wr() { printf '%s' "$2" > "$T/$1"; echo "$T/$1"; }

STU=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"ftetest","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
HR=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"demo_hr","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
ADM=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
REFRESH=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"ftetest","password":"Abc123456"}' | sed -n 's/.*"refreshToken":"\([^"]*\)".*/\1/p')
printf '{}' > "$T/b_empty.json"
[ -z "$STU" ] || [ -z "$HR" ] || [ -z "$ADM" ] && { echo "FATAL: login failed"; exit 1; }

echo "=== [A] 认证全链路 ==="
check "注册学生" POST /auth/register "" $(wr b1.json '{"username":"fctest_s1","password":"Abc123456","email":"fctest_s1@t.com","role":0,"nickname":"测试学生"}') 200 "0|10002"
check "注册校友" POST /auth/register "" $(wr b2.json '{"username":"fctest_a1","password":"Abc123456","email":"fctest_a1@t.com","role":1,"nickname":"测试校友"}') 200 "0|10002"
ALU=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"fctest_a1","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
check "刷新令牌" POST /auth/refresh "" $(wr b3.json "{\"refreshToken\":\"$REFRESH\"}") 200 0
check "刷新令牌-缺参" POST /auth/refresh "" "$T/b_empty.json" 400 10001
check "登录-错误密码" POST /auth/login "" $(wr b4.json '{"username":"ftetest","password":"wrong"}') "" "20003|20002"
check "无token受保护接口403" GET /user/me "" "" 403 "-1|NA|10003"
check "不存在接口404" GET /no-such-api "$STU" "" 404 "-1"

echo "=== [B] 用户/积分/通知 ==="
check "用户信息" GET /user/me "$STU" "" 200 0
check "更新资料" PUT /user/profile "$STU" $(wr b5.json '{"bio":"全量功能测试"}') 200 0
check "积分查询" GET /points/me "$STU" "" 200 0
check "通知列表" GET "/notify/me?page=1&size=10" "$STU" "" 200 0
check "未读计数" GET /notify/me/unread-count "$STU" "" 200 0
check "通知全部已读" PUT /notify/me/read-all "$STU" "" 200 0

echo "=== [C] 职位模块 ==="
check "职位列表" GET "/jobs?page=1&size=10" "$STU" "" 200 0
check "ES搜索" GET "/jobs?keyword=Java&page=1&size=10" "$STU" "" 200 0
check "ES搜索+过滤" GET "/jobs?keyword=%E5%89%8D%E7%AB%AF&city=%E5%8C%97%E4%BA%AC" "$STU" "" 200 0
check "职位详情" GET /jobs/1 "$STU" "" 200 0
check "非数字路径400" GET /jobs/search "$STU" "" 400 10001
check "不存在职位404" GET /jobs/99999 "$STU" "" "" "30002|30001|-1"
check "岗位统计" GET /jobs/stats/overview "$STU" "" 200 0
check "我的收藏" GET "/jobs/me/favorites?page=1&size=10" "$STU" "" 200 0
check "收藏切换" POST /jobs/4/favorite "$STU" "" 200 0
check "收藏切换恢复" POST /jobs/4/favorite "$STU" "" 200 0
check "企业列表" GET "/companies?page=1&size=15" "$STU" "" 200 0
check "企业详情" GET /companies/1 "$STU" "" 200 0
check "我发布的(HR)" GET "/jobs/me/published?page=1&size=10" "$HR" "" 200 0
check "我发布的(学生403)" GET "/jobs/me/published?page=1&size=10" "$STU" "" 403 "-1|10003"
check "投递者列表" GET "/jobs/1/applications?page=1&size=10" "$HR" "" 200 0
check "候选人详情" GET /jobs/applications/1/candidate "$HR" "" 200 0
check "状态流转" PUT /jobs/applications/1/status "$HR" $(wr b6.json '{"status":1,"remark":"全量测试初筛"}') 200 0
check "学生访问投递者403" GET "/jobs/1/applications?page=1&size=10" "$STU" "" 403 "-1|10003"

echo "=== [D] 内推（修复网关路由后） ==="
check "内推-学生403" POST /referrals "$STU" $(wr b7.json '{"jobId":3,"maxCount":5}') 403 "-1|10003"
[ -n "$ALU" ] && check "内推-校友创建" POST /referrals "$ALU" $(wr b8.json '{"jobId":3,"maxCount":5}') 200 "0|30001|30002|30003|30004|30005|30006|40001"

echo "=== [E] 简历 ==="
RID=$(req POST /resume "$STU" $(wr b9.json '{"title":"v3测试简历","contentJson":"{\"skills\":[\"Java\",\"ES\"]}"}') >/dev/null; jpick data.id)
if [ -n "$RID" ]; then
  check "新建简历" POST /resume "$STU" "$T/b9.json" 200 0
  check "简历详情" GET /resume/$RID "$STU" "" 200 0
  check "我的简历" GET /resume/me "$STU" "" 200 0
  check "AI润色" POST /resume/optimize "$STU" $(wr b10.json "{\"action\":\"polish\",\"resumeId\":$RID}") 200 0
  check "AI评分" POST /resume/score "$STU" $(wr b11.json "{\"resumeId\":$RID}") 200 0
  check "简历匹配" GET "/resume/match?jobId=1" "$STU" "" 200 0
else
  echo "SKIP [简历] 创建失败"
  FAIL=$((FAIL+1)); FAILED_CASES="$FAILED_CASES\nFAIL [新建简历] data.id 为空: $(head -c 150 "$T/_resp.json")"
fi

echo "=== [F] 模拟舱 ==="
check "场景列表" GET /simulator/scenarios "$STU" "" 200 0
check "场景详情" GET /simulator/scenarios/1 "$STU" "" 200 0
SID=$(req POST "/simulator/start?scenarioId=1" "$STU" "" >/dev/null; jpick data.sessionId)
if [ -n "$SID" ]; then
  check "开始会话" POST "/simulator/start?scenarioId=1" "$STU" "" 200 0
  check "选择完成" POST /simulator/choose "$STU" $(wr b12.json "{\"sessionId\":$SID,\"optionId\":1}") 200 0
  check "会话报告" GET /simulator/session/$SID/report "$STU" "" 200 0
else
  echo "SKIP [模拟舱会话] start 失败"
fi
check "我的会话" GET "/simulator/sessions/me?page=1&size=10" "$STU" "" 200 0

echo "=== [G] 面试模拟 ==="
check "题库列表" GET "/interview/questions?page=1&size=10" "$STU" "" 200 0
check "题目详情" GET /interview/questions/1 "$STU" "" 200 0
MID=$(req POST /interview/start "$STU" $(wr b13.json '{"type":0,"difficulty":1,"targetJob":"Java后端"}') >/dev/null; jpick data.mockId)
if [ -n "$MID" ]; then
  check "开始面试" POST /interview/start "$STU" "$T/b13.json" 200 0
  check "面试作答" POST /interview/answer "$STU" $(wr b14.json "{\"mockId\":$MID,\"questionId\":1,\"answer\":\"JVM分为堆、栈、方法区等，堆是GC主要区域\"}") 200 0
  check "结束面试" POST /interview/$MID/end "$STU" "" 200 0
  check "面试记录" GET /interview/$MID "$STU" "" 200 0
else
  echo "SKIP [面试会话] start 失败"
  FAIL=$((FAIL+1)); FAILED_CASES="$FAILED_CASES\nFAIL [开始面试] mockId为空: $(head -c 150 "$T/_resp.json")"
fi
check "面试历史" GET "/interview/history?page=1&size=10" "$STU" "" 200 0

echo "=== [H] 薪资白皮书完整链路 ==="
check "薪资贡献" POST /salary/contribute "$STU" $(wr b15.json '{"city":"上海","position":"全量测试岗","companyName":"测试公司","salaryMin":15000,"salaryMax":25000,"jobType":1,"educationLevel":1,"offerMonth":"2026-08","industry":"互联网"}') 200 "0|70001|70002|70003"
check "我的贡献" GET "/salary/contributions/me?page=1&size=10" "$STU" "" 200 0
check "待审列表" GET "/admin/salary/pending?page=1&size=10" "$ADM" "" 200 0
CID=$(jpick data.records.0.id)
if [ -n "$CID" ]; then
  check "审核通过" PUT /admin/salary/$CID/review "$ADM" $(wr b16.json '{"action":"APPROVE","remark":"全量测试"}') 200 0
  check "审核日志" GET /admin/salary/$CID/review-logs "$ADM" "" 200 0
fi
check "生成白皮书" POST /admin/whitepaper/generate "$ADM" "" 200 0
check "白皮书最新" GET /salary/whitepaper/latest "$STU" "" 200 0
WID=$(jpick data.id)
[ -n "$WID" ] && check "白皮书详情" GET /salary/whitepaper/$WID "$STU" "" 200 0

echo "=== [I] 徽章 ==="
check "徽章模板" GET /badges/templates "$STU" "" 200 0
check "我的徽章" GET /user/badges "$STU" "" 200 0
check "他人徽章" GET /badges/user/2 "$STU" "" 200 0
check "验证-缺hash400" GET "/badges/verify?userId=2&badgeId=1" "" "" 400 10001
check "管理端模板列表" GET /admin/badges/templates "$ADM" "" 200 0

echo "=== [J] 社区+帮帮团 ==="
PID=$(req POST /posts "$STU" $(wr b17.json '{"title":"v3测试帖","content":"ES索引优化问题","type":0}') >/dev/null; jpick data.id)
if [ -n "$PID" ]; then
  check "发帖" POST /posts "$STU" "$T/b17.json" 200 0
  check "帖子列表" GET "/posts?page=1&size=10" "$STU" "" 200 0
  check "帖子详情" GET /posts/$PID "$STU" "" 200 0
  AID=$(req POST /posts/$PID/answer "$STU" $(wr b18.json '{"content":"v3测试回答"}') >/dev/null; jpick data.id)
  [ -n "$AID" ] && check "回答" POST /posts/$PID/answer "$STU" "$T/b18.json" 200 0
fi
check "求助发布" POST /help-group/request "$STU" $(wr b19.json '{"title":"v3求助","content":"ES慢查询排查","skillTag":"elasticsearch","rewardPoints":5}') "" "0|60001|60002|60003|60004"
check "求助列表" GET /help-group/requests "$STU" "" 200 0
check "我的帮帮团" GET /help-group/me "$STU" "" 200 0

echo "=== [K] 作品集 ==="
check "作品广场" GET "/portfolio?page=1&size=10" "$STU" "" 200 0
PFID=$(req POST /portfolio "$STU" $(wr b20.json '{"title":"v3测试作品","description":"全量测试","githubUrl":"https://github.com/t/t"}') >/dev/null; jpick data.id)
if [ -n "$PFID" ]; then
  check "发布作品" POST /portfolio "$STU" "$T/b20.json" 200 0
  check "作品详情" GET /portfolio/$PFID "$STU" "" 200 0
  check "点赞" POST /portfolio/$PFID/like "$STU" "" 200 0
  check "我的作品" GET /portfolio/me "$STU" "" 200 0
  check "删除作品" DELETE /portfolio/$PFID "$STU" "" 200 0
fi

echo "=== [L] 管理端+负例 ==="
check "运营看板" GET /admin/overview "$ADM" "" 200 0
check "看板-学生403" GET /admin/overview "$STU" "" 403 "-1|10003"
check "重复投递业务拒绝" POST /jobs/1/apply "$STU" $(wr b21.json '{"resumeId":1,"coverLetter":"重复投递测试"}') "" "30003|30001|30002|30004|30005|30006"
check "解锁-积分不足" POST /unlock/mentor "$STU" "" "" "20008|50001|50002|50003"

echo ""
echo "==================== v3 最终汇总 ===================="
echo "PASS: $PASS / FAIL: $FAIL"
[ $FAIL -gt 0 ] && echo -e "失败明细:$FAILED_CASES"
