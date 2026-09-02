#!/bin/bash
# v3 失败项最终回归（文件body，避免内联中文编码问题）
BASE="http://localhost:8080/api"
T="E:/毕业设计/ft-tmp"; mkdir -p "$T"
PASS=0; FAIL=0; FAILED=""
STU=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"ftetest","password":"Abc123456"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
[ -z "$STU" ] && { echo "FATAL login"; exit 1; }

# 5) 新建简历
printf '{"title":"final-resume","contentJson":"{\\"skills\\":[\\"Java\\"]}"}' > "$T/f_resume.json"
R=$(curl -s -X POST "$BASE/resume" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" --data @"$T/f_resume.json")
RID=$(echo "$R" | sed -n 's/.*"code":0.*"data":\([0-9]*\).*/\1/p')
if [ -n "$RID" ]; then PASS=$((PASS+1)); echo "PASS [新建简历] id=$RID"; else FAIL=$((FAIL+1)); FAILED="$FAILED\n[新建简历] $(head -c 150 "$T/f_resume.json") => $R"; echo "FAIL [新建简历] $R"; fi

# 6) 面试作答
MID=$(curl -s -X POST "$BASE/interview/start" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" -d '{"type":0,"difficulty":1,"targetJob":"Java"}' | sed -n 's/.*"mockId":\([0-9]*\).*/\1/p')
if [ -n "$MID" ]; then
  printf '{"mockId":%s,"questionId":1,"answerText":"JVM heap stack methods area GC OOM explanation for final regression test"}' "$MID" > "$T/f_ans.json"
  R=$(curl -s -X POST "$BASE/interview/answer" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" --data @"$T/f_ans.json")
  C=$(echo "$R" | sed -n 's/.*"code":\([0-9]*\).*/\1/p')
  [ "$C" = "0" ] && { PASS=$((PASS+1)); echo "PASS [面试作答]"; } || { FAIL=$((FAIL+1)); FAILED="$FAILED\n[面试作答] $R"; echo "FAIL [面试作答] $R"; }
else
  FAIL=$((FAIL+1)); FAILED="$FAILED\n[面试start失败]"; echo "FAIL [面试start]"
fi

# 7) 帮帮团求助
printf '{"reasonType":0,"description":"final regression ES slow query help request","matchTags":"elasticsearch,mysql"}' > "$T/f_help.json"
R=$(curl -s -X POST "$BASE/help-group/request" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" --data @"$T/f_help.json")
C=$(echo "$R" | sed -n 's/.*"code":\([0-9]*\).*/\1/p')
[ "$C" = "0" ] && { PASS=$((PASS+1)); echo "PASS [帮帮团求助]"; } || { FAIL=$((FAIL+1)); FAILED="$FAILED\n[帮帮团] $R"; echo "FAIL [帮帮团] $R"; }

# 8) 重复投递
printf '{"resumeId":1,"coverLetter":"duplicate apply final test"}' > "$T/f_apply.json"
R=$(curl -s -X POST "$BASE/jobs/1/apply" -H "Authorization: Bearer $STU" -H "Content-Type: application/json" --data @"$T/f_apply.json" | sed -n 's/.*"code":\([0-9]*\).*/\1/p')
[ "$R" = "30003" ] && { PASS=$((PASS+1)); echo "PASS [重复投递30003]"; } || { FAIL=$((FAIL+1)); FAILED="$FAILED\n[重复投递] code=$R"; echo "FAIL [重复投递] code=$R"; }

echo ""
echo "===== 最终回归: PASS=$PASS FAIL=$FAIL ====="
[ $FAIL -gt 0 ] && echo -e "失败:$FAILED"
