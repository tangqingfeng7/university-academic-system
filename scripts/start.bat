@echo off
REM ============================================================================
REM 大学教务管理系统 - Windows启动脚本
REM 用途: 在Windows系统上启动Spring Boot应用
REM 使用: start.bat [环境]
REM 例如: start.bat prod
REM ============================================================================

setlocal

REM 设置变量
set APP_NAME=academic-system
set APP_JAR=academic-system.jar
set APP_DIR=%~dp0..
set LOG_DIR=%APP_DIR%\logs
set LOG_FILE=%LOG_DIR%\application.log

REM 环境配置
set PROFILE=%1
if "%PROFILE%"=="" set PROFILE=dev

REM JVM参数
set JVM_OPTS=-Xms512m -Xmx2048m
set JVM_OPTS=%JVM_OPTS% -XX:+UseG1GC
set JVM_OPTS=%JVM_OPTS% -XX:MaxGCPauseMillis=200
set JVM_OPTS=%JVM_OPTS% -XX:+HeapDumpOnOutOfMemoryError
set JVM_OPTS=%JVM_OPTS% -XX:HeapDumpPath=%LOG_DIR%\heapdump.hprof
set JVM_OPTS=%JVM_OPTS% -Dfile.encoding=UTF-8
set JVM_OPTS=%JVM_OPTS% -Duser.timezone=Asia/Shanghai

REM Spring Boot参数
set SPRING_OPTS=-Dspring.profiles.active=%PROFILE%

echo ======================================
echo 启动 %APP_NAME%
echo 环境: %PROFILE%
echo ======================================

REM 创建日志目录
if not exist "%LOG_DIR%" (
    echo 创建日志目录: %LOG_DIR%
    mkdir "%LOG_DIR%"
)

REM 检查jar文件
if not exist "%APP_DIR%\%APP_JAR%" (
    echo [错误] 找不到应用文件: %APP_DIR%\%APP_JAR%
    echo 请先运行: mvn clean package
    pause
    exit /b 1
)

REM 加载环境变量（如果存在）
if exist "%APP_DIR%\.env" (
    echo 加载环境变量文件: .env
    for /f "tokens=*" %%i in ('type "%APP_DIR%\.env"') do set %%i
)

REM 启动应用
echo 启动应用...
echo JVM参数: %JVM_OPTS%
echo Spring参数: %SPRING_OPTS%

cd /d "%APP_DIR%"

REM 启动应用（后台运行）
start "Academic-System" java %JVM_OPTS% %SPRING_OPTS% -jar %APP_JAR%

echo 应用已启动
echo 日志文件: %LOG_FILE%

REM 显示访问地址
set PORT=8080
if defined SERVER_PORT set PORT=%SERVER_PORT%
echo 访问地址: http://localhost:%PORT%

if "%PROFILE%"=="dev" (
    echo Swagger UI: http://localhost:%PORT%/swagger-ui.html
)

echo ======================================
pause

