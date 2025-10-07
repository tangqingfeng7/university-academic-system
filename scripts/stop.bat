@echo off
REM ============================================================================
REM 大学教务管理系统 - Windows停止脚本
REM 用途: 在Windows系统上停止Spring Boot应用
REM 使用: stop.bat
REM ============================================================================

setlocal

set APP_NAME=academic-system

echo ======================================
echo 停止 %APP_NAME%
echo ======================================

REM 查找并关闭Java进程
for /f "tokens=2" %%i in ('tasklist ^| findstr /i "java.exe"') do (
    echo 找到Java进程: %%i
    taskkill /PID %%i /F
)

echo 应用已停止
echo ======================================
pause

