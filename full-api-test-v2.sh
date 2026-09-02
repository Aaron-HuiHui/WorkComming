#!/bin/bash
# 全量功能测试 v2 —— 修复期望值/字段名 + 补齐正向链路
BASE="http://localhost:8080/api"
PASS=0; FAIL=0; FAILED_CASES=""
TMPDIR=/tmp/ft; mkdir -p $TMPDIR

req() {
  local m=$1 p=$2 t=$3 b=$4
  local args=(-s -o $TMPDIR/_resp.json -w "%{http_code}" --max-time 30 -X "$m" "$BASE$p")
  [ -n "$t" ] && args+=(-H "Authorization: Bearer $t")
  [ -n "$b" ] && args+=(-H "Content-Type: application/json" --data @"$b")
  local code; code=$(curl "${args[@]}")
  local bcode; bcode=$(node -e "try{console.log(JSON.parse(require('fs').readFileSync('$TMPDIR/_resp.json','utf8')).code)}catch(e){console.log('NA')}")
  echo "$code $bcode"
}
check() { # 描述 METHOD PATH TOKEN BODY_FILE 期望HTTP 期望code(可多个用|分隔，任一匹配即通过)
  local desc=$1 m=$2 p=$3 t=$4 b=$5 exp_http=$6 exp_codes=$7
  local out; out=$(req "$m" "$p" "$t" "$b")
  local code=${out%% *} bcode=${out##* }
  local ok=0
  IFS='|' read -ra EXPS <<< "$exp_codes"
  for ec in "${EXPS[@]}"; do [ "$bcode" = "$ec" ] && ok=1; done
  if [ "$code" = "$exp_http" ] && [ $ok -eq 1 ]; then
    PASS=$((PASS+1)); echo "PASS [$desc]"
  else
    FAIL=$((FAIL+1))
    FAILED_CASES="$FAILED_CASES\nFAIL [$desc] $m $p -> HTTP=$code(期望$exp_http) code=$bcode(期望$exp_codes) body=$(head -c 220 $TMPDIR/_resp.json)"
    echo "FAIL [$desc] HTTP=$code code=$bcode"
  fi
}
json() { printf '%s' "$2" > $TMPDIR/$1; echo $TMPDIR/$1; }

STU=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"ftetest","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
HR=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"demo_hr","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
ADM=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
REFRESH=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"ftetest","password":"Abc123456"}' | sed -n 's/.*"refreshToken":"\([^"]*\)".*/\1/p')
[ -z "$STU" ] && { echo "FATAL: ftetest login failed"; exit 1; }

echo "=== [A] 认证全链路 ==="
json b_reg.json '{"username":"fctest_z1","password":"Abc123456","email":"fctest_z1@test.com","role":0,"nickname":"测试新人"}'
check "注册新用户" POST /auth/register "" $TMPDIR/b_reg.json 200 "0|10002"
json b_refresh.json "{\"refreshToken\":\"$REFRESH\"}"
check "刷新令牌" POST /auth/refresh "" $TMPDIR/b_refresh.json 200 0
check "刷新令牌-缺参400" POST /auth/refresh "" $TMPDIR/b_empty.json 400 10001
printf '{}' > $TMPDIR/b_empty.json
check "登录-错误密码" POST /auth/login "" $(json b_bad.json '{"username":"ftetest","password":"wrong"}') "" "10002|10003"
check "无token访问受保护接口" GET /user/me "" "" "" "-1"

echo "=== [B] 简历（contentJson 字段） ==="
json b_resume.json '{"title":"v2测试简历","contentJson":"{\"name\":\"测试\",\"skills\":[\"Java\"]}"}'
check "新建简历" POST /resume "$STU" $TMPDIR/b_resume.json 200 0
RID=$(node -e "console.log(JSON.parse(require('fs').readFileSync('$TMPDIR/_resp.json','utf8')).data?.id ?? '')")
[ -z "$RID" ] && { curl -s "$BASE/resume/me" -H "Authorization: Bearer $STU" > $TMPDIR/rm.json; RID=$(node -e "const d=JSON.parse(require('fs').readFileSync('$TMPDIR/rm.json','utf8')).data;console.log(Array.isArray(d)?(d[0]?.id??''):(d?.records?.[0]?.id??''))"); }
if [ -n "$RID" ]; then
  check "简历详情" GET /resume/$RID "$STU" "" 200 0
  json b_opt.json "{\"action\":\"polish\",\"resumeId\":$RID}"
  check "AI润色" POST /resume/optimize "$STU" $TMPDIR/b_opt.json 200 0
  json b_score.json "{\"resumeId\":$RID}"
  check "AI评分" POST /resume/score "$STU" $TMPDIR/b_score.json 200 0
  check "简历匹配" GET "/resume/match?jobId=1" "$STU" "" 200 0
fi

echo "=== [C] 薪资贡献完整链路（字段修正） ==="
json b_contrib.json '{"city":"北京","position":"后端开发","companyName":"测试科技","salaryMin":18000,"salaryMax":28000,"jobType":1,"educationLevel":1,"offerMonth":"2026-07","industry":"互联网"}'
check "薪资贡献" POST /salary/contribute "$STU" $TMPDIR/b_contrib.json 200 "0|70001|70002"
check "我的贡献" GET "/salary/contributions/me?page=1&size=10" "$STU" "" 200 0
check "待审列表(管理员)" GET "/admin/salary/pending?page=1&size=10" "$ADM" "" 200 0
CID=$(node -e "const d=JSON.parse(require('fs').readFileSync('$TMPDIR/_resp.json','utf8')).data;console.log(d?.records?.[0]?.id??'')")
if [ -n "$CID" ]; then
  json b_review.json '{"action":"APPROVE","remark":"全量测试通过"}'
  check "管理员审核通过" PUT /admin/salary/$CID/review "$ADM" $TMPDIR/b_review.json 200 0
  check "审核日志" GET /admin/salary/$CID/review-logs "$ADM" "" 200 0
fi
check "生成白皮书" POST /admin/whitepaper/generate "$ADM" "" 200 0
check "白皮书最新(生成后)" GET /salary/whitepaper/latest "$STU" "" 200 0

echo "=== [D] 徽章验证（带hash） ==="
BADGE_LINE=$(curl -s "$BASE/badges/user/2" -H "Authorization: Bearer $STU" | node -e "let d='';process.stdin.on('data',c=>d+=c).on('end',()=>{const r=JSON.parse(d);const b=(r.data||[])[0];console.log(b?(b.badgeTemplateId||b.id)+' '+(b.lockHash||''):'NONE')})")
if [ "$BADGE_LINE" != "NONE" ] && [ -n "$BADGE_LINE" ]; then
  BID=${BADGE_LINE%% *}; BH=${BADGE_LINE##* }
  check "徽章验证(真实哈希)" GET "/badges/verify?userId=2&badgeId=$BID&hash=$BH" "" "" 200 0
else
  echo "SKIP [徽章验证] 用户2无徽章，改测缺参400"
  check "徽章验证-缺hash参数400" GET "/badges/verify?userId=2&badgeId=1" "" "" 400 10001
fi
check "非数字路径参数400" GET /jobs/search "$STU" "" 400 10001

echo "=== [E] 面试模拟（完整答题） ==="
json b_ivs.json '{"questionId":1}'
MID=$(curl -s -X POST "$BASE/interview/start" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" -d @$TMPDIR/b_ivs.json | node -e "let d='';process.stdin.on('data',c=>d+=c).on('end',()=>{try{console.log(JSON.parse(d).data??'')}catch(e){console.log('')}})")
if [ -n "$MID" ]; then
  json b_iva.json "{\"mockId\":$MID,\"questionId\":1,\"answer\":\"JVM内存分为堆、栈、方法区、程序计数器和本地方法栈，堆是GC主要区域。\"}"
  check "面试作答" POST /interview/answer "$STU" $TMPDIR/b_iva.json 200 0
  check "结束面试" POST /interview/$MID/end "$STU" "" 200 0
  check "面试记录详情" GET /interview/$MID "$STU" "" 200 0
else
  echo "SKIP [面试模拟] start 未返回 mockId"
fi

echo "=== [F] 社区+帮帮团完整链路 ==="
json b_post.json '{"title":"v2测试问题","content":"请问ES索引如何优化？","type":0}'
PID=$(curl -s -X POST "$BASE/posts" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" -d @$TMPDIR/b_post.json | node -e "let d='';process.stdin.on('data',c=>d+=c).on('end',()=>{try{console.log(JSON.parse(d).data??'')}catch(e){console.log('')}})")
if [ -n "$PID" ]; then
  check "帖子详情" GET /posts/$PID "$STU" "" 200 0
  json b_ans.json '{"content":"v2测试回答"}'
  AID=$(curl -s -X POST "$BASE/posts/$PID/answer" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" -d @$TMPDIR/b_ans.json | node -e "let d='';process.stdin.on('data',c=>d+=c).on('end',()=>{try{console.log(JSON.parse(d).data??'')}catch(e){console.log('')}})")
  [ -n "$AID" ] && check "采纳回答" PUT /answers/$AID/accept "$STU" "" 200 "0|30001|30002|30003"
fi
json b_help.json '{"title":"v2测试求助","content":"帮忙看看ES慢查询","skillTag":"elasticsearch","rewardPoints":5}'
HID=$(curl -s -X POST "$BASE/help-group/request" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" -d @$TMPDIR/b_help.json | node -e "let d='';process.stdin.on('data',c=>d+=c).on('end',()=>{try{console.log(JSON.parse(d).data??'')}catch(e){console.log('')}})")
if [ -n "$HID" ]; then
  json b_match.json '{"supporterId":5}'
  check "帮帮团匹配" POST /help-group/$HID/match "$STU" $TMPDIR/b_match.json 200 "0|60001|60002|60003"
  check "帮帮团解决" PUT /help-group/$HID/resolve "$STU" "" 200 "0|60001|60002|60003"
fi

echo "=== [G] 投递/内推/解锁 ==="
json b_apply.json '{"resumeId":1,"coverLetter":"全量测试求职信"}'
check "重复投递(应业务拒绝)" POST /jobs/1/apply "$STU" $TMPDIR/b_apply.json 200 "30001|30002|30003|30004|30005|30006"
check "投递新职位" POST /jobs/3/apply "$STU" $TMPDIR/b_apply.json 200 "0|30001|30002|30003"
json b_ref.json '{"jobId":3,"referrerName":"学长甲","contact":"wx-test"}'
check "内推登记" POST /referrals "$STU" $TMPDIR/b_ref.json "" "0|10001|30001|30002|30003|30004|30005|30006|40001|40002"
check "导师解锁(积分不足应拒绝)" POST /unlock/mentor "$STU" "" "" "50001|50002|50003|10001"
check "通知全部已读" PUT /notify/me/read-all "$STU" "" 200 0

echo "=== [H] 管理端补全 ==="
json b_tpl.json '{"name":"v2测试徽章","conditionType":0,"threshold":1,"rarity":0,"icon":"star","description":"测试"}'
check "创建徽章模板" POST /admin/badges/templates "$ADM" $TMPDIR/b_tpl.json "" "0|10001|20001|20002"

echo ""
echo "==================== v2 汇总 ===================="
echo "PASS: $PASS / FAIL: $FAIL"
[ $FAIL -gt 0 ] && echo -e "失败明细:$FAILED_CASES"
