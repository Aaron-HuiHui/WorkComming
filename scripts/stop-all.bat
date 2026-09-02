@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM  我要工作 平台 - 一键停止全部服务
REM  用法: scripts\stop-all.bat
REM ============================================================

echo ============================================================
echo   我要工作 平台 - 一键停止
echo ============================================================
echo.

REM 停止顺序: 前端 → Java服务 → MinIO → ES → RabbitMQ → Redis → MySQL

echo [1/9] 停止 前端开发服务器 ...
for /f "tokens=5" %%a in ('netstat -ano ^| find ":5173 " ^| find "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>&1 && echo   已停止 PID %%a
)

echo [2/9] 停止 职位服务（8082）...
for /f "tokens=5" %%a in ('netstat -ano ^| find ":8082 " ^| find "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>&1 && echo   已停止 PID %%a
)

echo [3/9] 停止 核心服务（8081）...
for /f "tokens=5" %%a in ('netstat -ano ^| find ":8081 " ^| find "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>&1 && echo   已停止 PID %%a
)

echo [4/9] 停止 网关服务（8080）...
for /f "tokens=5" %%a in ('netstat -ano ^| find ":8080 " ^| find "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>&1 && echo   已停止 PID %%a
)

echo [5/9] 停止 MinIO（9000）...
for /f "tokens=5" %%a in ('netstat -ano ^| find ":9000 " ^| find "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>&1 && echo   已停止 PID %%a
)

echo [6/9] 停止 Elasticsearch（9200）...
for /f "tokens=5" %%a in ('netstat -ano ^| find ":9200 " ^| find "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>&1 && echo   已停止 PID %%a
)
REM ES 可能有多进程，也按进程名杀
taskkill /FI "WINDOWTITLE eq Elasticsearch*" /F >nul 2>&1

echo [7/9] 停止 RabbitMQ（5672）...
tasklist /FI "IMAGENAME eq erl.exe" 2>nul | find /i "erl.exe" >nul
if !errorlevel! == 0 (
    taskkill /IM erl.exe /F >nul 2>&1 && echo   RabbitMQ 已停止
) else (
    echo   RabbitMQ 未运行
)

echo [8/9] 停止 Redis（6379）...
for /f "tokens=5" %%a in ('netstat -ano ^| find ":6379 " ^| find "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>&1 && echo   已停止 PID %%a
)

echo [9/9] 停止 MySQL（3306）...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>nul | find /i "mysqld.exe" >nul
if !errorlevel! == 0 (
    taskkill /IM mysqld.exe /F >nul 2>&1 && echo   MySQL 已停止
) else (
    echo   MySQL 未运行
)

echo.
echo ============================================================
echo   全部服务已停止
echo ============================================================
echo.
pause
