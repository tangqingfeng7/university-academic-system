#!/bin/bash

###############################################################################
# 大学教务管理系统 - 启动脚本
# 用途: 启动Spring Boot应用
# 使用: ./start.sh [环境]
# 例如: ./start.sh prod
###############################################################################

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 应用配置
APP_NAME="academic-system"
APP_JAR="academic-system.jar"
APP_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PID_FILE="${APP_DIR}/app.pid"
LOG_DIR="${APP_DIR}/logs"
LOG_FILE="${LOG_DIR}/application.log"

# 默认环境
PROFILE=${1:-dev}

# JVM参数
JVM_OPTS="-Xms512m -Xmx2048m"
JVM_OPTS="${JVM_OPTS} -XX:+UseG1GC"
JVM_OPTS="${JVM_OPTS} -XX:MaxGCPauseMillis=200"
JVM_OPTS="${JVM_OPTS} -XX:+HeapDumpOnOutOfMemoryError"
JVM_OPTS="${JVM_OPTS} -XX:HeapDumpPath=${LOG_DIR}/heapdump.hprof"
JVM_OPTS="${JVM_OPTS} -Dfile.encoding=UTF-8"
JVM_OPTS="${JVM_OPTS} -Duser.timezone=Asia/Shanghai"

# Spring Boot参数
SPRING_OPTS="-Dspring.profiles.active=${PROFILE}"

# 函数: 打印信息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 函数: 检查应用是否已运行
is_running() {
    if [ -f "${PID_FILE}" ]; then
        PID=$(cat "${PID_FILE}")
        if ps -p ${PID} > /dev/null 2>&1; then
            return 0
        else
            rm -f "${PID_FILE}"
            return 1
        fi
    fi
    return 1
}

# 函数: 创建必要的目录
create_directories() {
    if [ ! -d "${LOG_DIR}" ]; then
        mkdir -p "${LOG_DIR}"
        print_info "创建日志目录: ${LOG_DIR}"
    fi
}

# 函数: 检查jar文件
check_jar() {
    if [ ! -f "${APP_DIR}/${APP_JAR}" ]; then
        print_error "找不到应用文件: ${APP_DIR}/${APP_JAR}"
        print_info "请先运行: mvn clean package"
        exit 1
    fi
}

# 主逻辑
print_info "======================================"
print_info "启动 ${APP_NAME}"
print_info "环境: ${PROFILE}"
print_info "======================================"

# 检查是否已运行
if is_running; then
    PID=$(cat "${PID_FILE}")
    print_warn "应用已经在运行中 (PID: ${PID})"
    print_info "如需重启，请先执行: ./stop.sh"
    exit 1
fi

# 创建目录
create_directories

# 检查jar文件
check_jar

# 加载环境变量（如果存在）
if [ -f "${APP_DIR}/.env" ]; then
    print_info "加载环境变量文件: .env"
    set -a
    source "${APP_DIR}/.env"
    set +a
fi

# 启动应用
print_info "启动应用..."
print_info "JVM参数: ${JVM_OPTS}"
print_info "Spring参数: ${SPRING_OPTS}"

cd "${APP_DIR}"

nohup java ${JVM_OPTS} ${SPRING_OPTS} -jar ${APP_JAR} > "${LOG_FILE}" 2>&1 &

echo $! > "${PID_FILE}"
PID=$(cat "${PID_FILE}")

# 等待应用启动
sleep 3

# 验证启动
if is_running; then
    print_info "应用启动成功 (PID: ${PID})"
    print_info "日志文件: ${LOG_FILE}"
    print_info "查看日志: tail -f ${LOG_FILE}"
    
    # 显示访问地址
    PORT=${SERVER_PORT:-8080}
    print_info "访问地址: http://localhost:${PORT}"
    
    # 如果是开发环境，显示Swagger地址
    if [ "${PROFILE}" = "dev" ]; then
        print_info "Swagger UI: http://localhost:${PORT}/swagger-ui.html"
    fi
else
    print_error "应用启动失败"
    print_info "请查看日志: ${LOG_FILE}"
    exit 1
fi

print_info "======================================"

