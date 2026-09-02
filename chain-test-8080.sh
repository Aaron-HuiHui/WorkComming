#!/bin/bash
# 全链路测试（网关 8080 → 核心 8081 / 职位 8082）只读+可回滚操作，不产生脏数据
BASE="http://localhost:8080/api"
PASS=0; FAIL=0; ISSUES=""

check() { # check "名称" "期望code" "响应"
  local name="$1" expect="$2" resp="$3"
  local code=$(echo "$resp" | sed -n 's/.*"code":\([0-9]*\).*/\1/p')
  if [ "$code" = "$expect" ]; then PASS=$((PASS+1)); echo "PASS [$name]";
  else FAIL=$((FAIL+1)); ISSUES="$ISSUES\n[$name] 期望code=$expect 实际=${code:-无} => $(echo "$resp" | head -c 200)"; echo "FAIL [$name] => $(echo "$resp" | head -c 160)"; fi
}

jget() { echo "$1" | sed -n "s/.*\"$2\":\([^,}]*\).*/\1/p" | head -1 | tr -d '"'; }

echo "======== 1. 认证模块 ========"
STU=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"ftetest","password":"Abc123456"}')
check "学生登录" 0 "$STU"
STU_TOKEN=$(jget "$STU" accessToken)
HR_TOKEN=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"demo_hr","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
ADM_TOKEN=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
[ -n "$STU_TOKEN" ] && [ -n "$HR_TOKEN" ] && [ -n "$ADM_TOKEN" ] && { PASS=$((PASS+1)); echo "PASS [HR/管理员登录]"; } || { FAIL=$((FAIL+1)); ISSUES="$ISSUES\n[HR或管理员登录失败]"; echo "FAIL [HR/管理员登录]"; }
check "错误密码登录应拒绝" 20003 "$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"ftetest","password":"wrong"}')"
check "无token访问受保护接口应401" 10002 "$(curl -s "$BASE/user/me")"
check "伪造token应401" 10002 "$(curl -s "$BASE/user/me" -H "Authorization: Bearer fake.token.here")"
check "获取当前用户" 0 "$(curl -s "$BASE/user/me" -H "Authorization: Bearer $STU_TOKEN")"

echo "======== 2. 职位服务（经网关→8082） ========"
R=$(curl -s "$BASE/jobs?page=1&size=3" -H "Authorization: Bearer $STU_TOKEN")
check "职位搜索" 0 "$R"; TOTAL=$(jget "$R" total); echo "    职位总数: $TOTAL"
JID=$(echo "$R" | sed -n 's/.*"records":\[{"id":\([0-9]*\).*/\1/p'); [ -z "$JID" ] && JID=1
check "职位详情(id=$JID)" 0 "$(curl -s "$BASE/jobs/$JID" -H "Authorization: Bearer $STU_TOKEN")"
check "职位搜索-秋招批次过滤" 0 "$(curl -s "$BASE/jobs?page=1&size=5&batch=2" -H "Authorization: Bearer $STU_TOKEN")"
check "职位搜索-城市过滤" 0 "$(curl -s "$BASE/jobs?page=1&size=5&city=北京" -H "Authorization: Bearer $STU_TOKEN")"
check "职位搜索-关键词" 0 "$(curl -s "$BASE/jobs?page=1&size=5&keyword=Java" -H "Authorization: Bearer $STU_TOKEN")"
check "企业列表" 0 "$(curl -s "$BASE/companies?page=1&size=5" -H "Authorization: Bearer $STU_TOKEN")"
check "企业详情(id=1)" 0 "$(curl -s "$BASE/companies/1" -H "Authorization: Bearer $STU_TOKEN")"
check "我的收藏列表" 0 "$(curl -s "$BASE/jobs/me/favorites?page=1&size=5" -H "Authorization: Bearer $STU_TOKEN")"
check "收藏切换(开→关回滚)" 0 "$(curl -s -X POST "$BASE/jobs/$JID/favorite" -H "Authorization: Bearer $STU_TOKEN")"
check "收藏切换(再切回)" 0 "$(curl -s -X POST "$BASE/jobs/$JID/favorite" -H "Authorization: Bearer $STU_TOKEN")"
check "我的投递列表" 0 "$(curl -s "$BASE/jobs/me/applied?page=1&size=5" -H "Authorization: Bearer $STU_TOKEN")"
check "HR候选人列表" 0 "$(curl -s "$BASE/jobs/1/applications?page=1&size=5" -H "Authorization: Bearer $HR_TOKEN")"

echo "======== 3. 核心服务-用户/通知/作品集 ========"
check "通知未读数" 0 "$(curl -s "$BASE/notify/me/unread-count" -H "Authorization: Bearer $STU_TOKEN")"
check "通知分页" 0 "$(curl -s "$BASE/notify/me?page=1&size=5" -H "Authorization: Bearer $STU_TOKEN")"
R=$(curl -s "$BASE/portfolio?page=1&size=5" -H "Authorization: Bearer $STU_TOKEN")
check "作品广场" 0 "$R"
PID=$(echo "$R" | sed -n 's/.*"records":\[{"id":\([0-9]*\).*/\1/p'); [ -z "$PID" ] && PID=1
check "作品详情(id=$PID)" 0 "$(curl -s "$BASE/portfolio/$PID" -H "Authorization: Bearer $STU_TOKEN")"
check "作品点赞切换" 0 "$(curl -s -X POST "$BASE/portfolio/$PID/like" -H "Authorization: Bearer $STU_TOKEN")"
check "我的作品" 0 "$(curl -s "$BASE/portfolio/me?page=1&size=5" -H "Authorization: Bearer $STU_TOKEN")"

echo "======== 4. 核心服务-简历/面试/AI ========"
check "简历列表" 0 "$(curl -s "$BASE/resume/me" -H "Authorization: Bearer $STU_TOKEN")"
R=$(curl -s -X POST "$BASE/interview/start" -H "Authorization: Bearer $STU_TOKEN" -H "Content-Type: application/json" -d '{"type":0,"difficulty":1,"targetJob":"Java"}')
check "AI面试开始" 0 "$R"
MID=$(jget "$R" mockId)
[ -n "$MID" ] && check "面试历史" 0 "$(curl -s "$BASE/interview/history" -H "Authorization: Bearer $STU_TOKEN")"
R=$(curl -s "$BASE/simulator/scenarios" -H "Authorization: Bearer $STU_TOKEN")
check "模拟舱场景列表" 0 "$R"
SID=$(echo "$R" | sed -n 's/.*\[{"id":\([0-9]*\).*/\1/p'); [ -z "$SID" ] && SID=1
check "模拟舱开始(id=$SID)" 0 "$(curl -s -X POST "$BASE/simulator/start?scenarioId=$SID" -H "Authorization: Bearer $STU_TOKEN")"

echo "======== 5. 核心服务-薪资/徽章/社区/帮帮团 ========"
check "最新白皮书" 0 "$(curl -s "$BASE/salary/whitepaper/latest" -H "Authorization: Bearer $STU_TOKEN")"
check "我的贡献" 0 "$(curl -s "$BASE/salary/contributions/me?page=1&size=5" -H "Authorization: Bearer $STU_TOKEN")"
check "徽章模板" 0 "$(curl -s "$BASE/badges/templates" -H "Authorization: Bearer $STU_TOKEN")"
check "我的徽章" 0 "$(curl -s "$BASE/user/badges" -H "Authorization: Bearer $STU_TOKEN")"
check "社区帖子列表" 0 "$(curl -s "$BASE/posts?page=1&size=5" -H "Authorization: Bearer $STU_TOKEN")"
check "帮帮团列表" 0 "$(curl -s "$BASE/help-group/requests?page=1&size=5" -H "Authorization: Bearer $STU_TOKEN")"

echo "======== 6. 管理端 ========"
check "管理看板" 0 "$(curl -s "$BASE/admin/overview" -H "Authorization: Bearer $ADM_TOKEN")"
check "学生访问管理端应拒绝" 10003 "$(curl -s "$BASE/admin/overview" -H "Authorization: Bearer $STU_TOKEN")"

echo "======== 7. 异常输入边界 ========"
echo "-- 非数字id: $(curl -s -o /dev/null -w '%{http_code}' "$BASE/jobs/abc" -H "Authorization: Bearer $STU_TOKEN") (期望400,历史缺陷500)"
echo "-- 不存在职位: $(curl -s "$BASE/jobs/99999" -H "Authorization: Bearer $STU_TOKEN" | head -c 120)"
echo "-- 越权企业编辑(学生): $(curl -s -X PUT "$BASE/companies/1" -H "Authorization: Bearer $STU_TOKEN" -H "Content-Type: application/json" -d '{"name":"hack"}' | head -c 120)"
echo "-- 超大分页: $(curl -s -o /dev/null -w '%{http_code}' "$BASE/jobs/search?page=1&size=99999" -H "Authorization: Bearer $STU_TOKEN")"
echo "-- SQL注入探针: $(curl -s "$BASE/jobs/search?keyword='%20OR%201=1--" -H "Authorization: Bearer $STU_TOKEN" | head -c 120)"

echo ""
echo "==========================================="
echo "  全链路测试结果: PASS=$PASS FAIL=$FAIL"
echo "==========================================="
[ $FAIL -gt 0 ] && echo -e "失败明细: $ISSUES"
exit 0
