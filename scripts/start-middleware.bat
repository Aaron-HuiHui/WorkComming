@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM  我要工作 平台 - 仅启动中间件(IDEA 中运行 Java 服务时配套使用)
REM  用法: 双击或 scripts\start-middleware.bat
REM  说明: 不启动网关/核心/职位/前端,避免与 IDEA 运行端口冲突
REM ============================================================

set TOOLS=C:\Users\Lenovo\tools

echo ============================================================
echo   我要工作 平台 - 中间件一键启动
echo ============================================================
echo.

REM ---------- 1. MySQL ----------
echo [1/5] 启动 MySQL ...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>nul | find /i "mysqld.exe" >nul
if !errorlevel! == 0 (
    echo   MySQL 已在运行，跳过
) else (
    start "MySQL" /MIN "%TOOLS%\mysql\mysql-8.0.29\bin\mysqld.exe" --console
    echo   MySQL 启动中（端口 3306，密码 123456）
)
timeout /t 3 /nobreak >nul

REM ---------- 2. Redis ----------
echo [2/5] 启动 Redis ...
netstat -ano | find ":6379 " | find "LISTENING" >nul 2>&1
if !errorlevel! == 0 (
    echo   Redis 已在运行，跳过
) else (
    start "Redis" /MIN /D "%TOOLS%\redis" redis-server.exe redis.windows.conf --requirepass iwantjob
    echo   Redis 启动中（端口 6379，密码 iwantjob）
)
timeout /t 2 /nobreak >nul

REM ---------- 3. RabbitMQ ----------
echo [3/5] 启动 RabbitMQ ...
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
echo [4/5] 启动 Elasticsearch ...
netstat -ano | find ":9200 " | find "LISTENING" >nul 2>&1
if !errorlevel! == 0 (
    echo   Elasticsearch 已在运行，跳过
) else (
    start "Elasticsearch" /MIN "%TOOLS%\elasticsearch-8.15.5\bin\elasticsearch.bat"
    echo   Elasticsearch 启动中（端口 9200，约需 15-30 秒）
)
timeout /t 2 /nobreak >nul

REM ---------- 5. MinIO ----------
echo [5/5] 启动 MinIO ...
netstat -ano | find ":9000 " | find "LISTENING" >nul 2>&1
if !errorlevel! == 0 (
    echo   MinIO 已在运行，跳过
) else (
    start "MinIO" /MIN "%TOOLS%\minio\minio.exe" server "%TOOLS%\minio\data" --address ":9000" --console-address ":9001"
    echo   MinIO 启动中（端口 9000 / 控制台 9001）
)

echo.
echo [等待] MySQL/Redis 就绪 ...
timeout /t 8 /nobreak >nul

echo.
echo ============================================================
echo   中间件启动命令已全部发出！
echo.
echo   现在可以到 IDEA 中运行三个服务：
echo     1. IwantJobApplication   （核心，8081）
echo     2. JobServerApplication  （职位，8082）
echo     3. GatewayApplication    （网关，8080，最后启动）
echo.
echo   前端:  cd iwantjob-frontend ^&^& npm run dev
echo   停止中间件: scripts\stop-all.bat（会连 Java 服务一起停）
echo ============================================================
echo.
pause
