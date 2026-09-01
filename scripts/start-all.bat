@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM  我要工作 平台 - 一键启动全部中间件与服务
REM  用法: 双击或 scripts\start-all.bat
REM  日志: 各中间件自带日志；Java服务输出到控制台新窗口
REM ============================================================

set TOOLS=C:\Users\Lenovo\tools
set PROJECT=E:\毕业设计
set BACKEND=%PROJECT%\iwantjob-backend

echo ============================================================
echo   我要工作 平台 - 一键启动
echo ============================================================
echo.

REM ---------- 1. MySQL ----------
echo [1/9] 启动 MySQL ...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>nul | find /i "mysqld.exe" >nul
if !errorlevel! == 0 (
    echo   MySQL 已在运行，跳过
) else (
    start "MySQL" /MIN "%TOOLS%\mysql\mysql-8.0.29\bin\mysqld.exe" --console
    echo   MySQL 启动中（端口 3306）
)
timeout /t 3 /nobreak >nul

REM ---------- 2. Redis ----------
echo [2/9] 启动 Redis ...
netstat -ano | find ":6379 " | find "LISTENING" >nul 2>&1
if !errorlevel! == 0 (
    echo   Redis 已在运行，跳过
) else (
    start "Redis" /MIN /D "%TOOLS%\redis" redis-server.exe redis.windows.conf --requirepass iwantjob
    echo   Redis 启动中（端口 6379，密码 iwantjob）
)
timeout /t 2 /nobreak >nul

REM ---------- 3. RabbitMQ ----------
echo [3/9] 启动 RabbitMQ ...
netstat -ano | find ":5672 " | find "LISTENING" >nul 2>&1
if !errorlevel! == 0 (
    echo   RabbitMQ 已在运行，跳过
) else (
    if exist "%TOOLS%\rabbitmq\start-rabbitmq.bat" (
        call "%TOOLS%\rabbitmq\start-rabbitmq.bat"
        echo   RabbitMQ 启动中（端口 5672 / 管理台 15672）
    ) else (
        echo   [警告] 未找到 start-rabbitmq.bat，请手动启动 RabbitMQ
    )
)
timeout /t 5 /nobreak >nul

REM ---------- 4. Elasticsearch ----------
echo [4/9] 启动 Elasticsearch ...
netstat -ano | find ":9200 " | find "LISTENING" >nul 2>&1
if !errorlevel! == 0 (
    echo   Elasticsearch 已在运行，跳过
) else (
    start "Elasticsearch" /MIN "%TOOLS%\elasticsearch-8.15.5\bin\elasticsearch.bat"
    echo   Elasticsearch 启动中（端口 9200，约需 15-30 秒）
)
timeout /t 2 /nobreak >nul

REM ---------- 5. MinIO ----------
echo [5/9] 启动 MinIO ...
netstat -ano | find ":9000 " | find "LISTENING" >nul 2>&1
if !errorlevel! == 0 (
    echo   MinIO 已在运行，跳过
) else (
    start "MinIO" /MIN "%TOOLS%\minio\minio.exe" server "%TOOLS%\minio\data" --address ":9000" --console-address ":9001"
    echo   MinIO 启动中（端口 9000 / 控制台 9001）
)
timeout /t 2 /nobreak >nul

REM ---------- 等待中间件就绪 ----------
echo.
echo [中间件] 等待 MySQL 与 ES 就绪 ...
timeout /t 8 /nobreak >nul

REM ---------- 6. 网关 ----------
echo [6/9] 启动 网关服务（端口 8000）...
netstat -ano | find ":8000 " | find "LISTENING" >nul 2>&1
if !errorlevel! == 0 (
    echo   网关已在运行，跳过
) else (
    start "IWantJob-Gateway" /D "%BACKEND%\iwantjob-gateway\target" java -jar iwantjob-gateway-1.0.0-SNAPSHOT.jar
    echo   网关启动中
)

REM ---------- 7. 核心服务 ----------
echo [7/9] 启动 核心服务（端口 8081）...
netstat -ano | find ":8081 " | find "LISTENING" >nul 2>&1
if !errorlevel! == 0 (
    echo   核心服务已在运行，跳过
) else (
    start "IWantJob-Core" /D "%BACKEND%\iwantjob-api\target" java -jar iwantjob-api-1.0.0-SNAPSHOT.jar
    echo   核心服务启动中（约需 15 秒）
)

REM ---------- 8. 职位服务 ----------
echo [8/9] 启动 职位服务（端口 8082）...
netstat -ano | find ":8082 " | find "LISTENING" >nul 2>&1
if !errorlevel! == 0 (
    echo   职位服务已在运行，跳过
) else (
    start "IWantJob-Job" /D "%BACKEND%\iwantjob-job-server\target" java -jar iwantjob-job-server-1.0.0-SNAPSHOT.jar
    echo   职位服务启动中（约需 14 秒）
)

REM 等待后端就绪
timeout /t 15 /nobreak >nul

REM ---------- 9. 前端 ----------
echo [9/9] 启动 前端开发服务器（端口 5173）...
netstat -ano | find ":5173 " | find "LISTENING" >nul 2>&1
if !errorlevel! == 0 (
    echo   前端已在运行，跳过
) else (
    start "IWantJob-Frontend" /D "%PROJECT%\iwantjob-frontend" cmd /c "npm run dev"
    echo   前端启动中
)

echo.
echo ============================================================
echo   全部启动命令已发出！
echo.
echo   访问地址：
echo     前端:        http://localhost:5173
echo     网关:        http://localhost:8000
echo     API 文档:    http://localhost:8081/api/doc.html
echo     ES:          http://localhost:9200
echo     MinIO 控制台: http://localhost:9001
echo     RabbitMQ:    http://localhost:15672  (guest/guest)
echo.
echo   测试账号:
echo     学生 ftetest / Abc123456
echo     HR   demo_hr / Abc123456
echo     管理员 admin / Abc123456
echo ============================================================
echo.
pause
