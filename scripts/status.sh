#!/bin/bash

###############################################################################
# 大学教务管理系统 - 状态查看脚本
# 用途: 查看应用运行状态
# 使用: ./status.sh
###############################################################################

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 应用配置
APP_NAME="academic-system"
APP_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PID_FILE="${APP_DIR}/app.pid"

# 函数: 打印信息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 函数: 检查应用是否运行
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

# 函数: 获取进程信息
get_process_info() {
    PID=$1
    ps -p ${PID} -o pid,ppid,%cpu,%mem,etime,cmd --no-headers
}

# 函数: 检查端口
check_port() {
    PORT=${1:-8080}
    if command -v lsof &> /dev/null; then
        lsof -i :${PORT} > /dev/null 2>&1
        return $?
    elif command -v netstat &> /dev/null; then
        netstat -tuln | grep ":${PORT}" > /dev/null 2>&1
        return $?
    else
        return 2
    fi
}

# 函数: 获取磁盘使用情况
get_disk_usage() {
    df -h "${APP_DIR}" | awk 'NR==2 {print $5}'
}

# 主逻辑
echo ""
print_info "======================================"
print_info "应用状态检查: ${APP_NAME}"
print_info "======================================"
echo ""

# 检查应用状态
if is_running; then
    PID=$(cat "${PID_FILE}")
    print_success "应用状态: 运行中"
    echo ""
    
    # 进程信息
    print_info "进程信息:"
    echo "  PID:     ${PID}"
    echo "  详情:    $(get_process_info ${PID})"
    echo ""
    
    # 端口检查
    print_info "端口状态:"
    PORT=${SERVER_PORT:-8080}
    if check_port ${PORT}; then
        print_success "  端口 ${PORT}: 监听中"
    else
        print_warn "  端口 ${PORT}: 未监听"
    fi
    echo ""
    
    # 环境检查
    print_info "运行环境:"
    ENV_FILE="${APP_DIR}/.env"
    if [ -f "${ENV_FILE}" ]; then
        PROFILE=$(grep "SPRING_PROFILES_ACTIVE" "${ENV_FILE}" | cut -d'=' -f2)
        echo "  配置环境: ${PROFILE:-未指定}"
    else
        echo "  配置环境: 默认"
    fi
    echo ""
    
    # 日志文件
    print_info "日志文件:"
    LOG_FILE="${APP_DIR}/logs/academic-system.log"
    if [ -f "${LOG_FILE}" ]; then
        LOG_SIZE=$(du -h "${LOG_FILE}" | cut -f1)
        LOG_LINES=$(wc -l < "${LOG_FILE}")
        echo "  位置: ${LOG_FILE}"
        echo "  大小: ${LOG_SIZE}"
        echo "  行数: ${LOG_LINES}"
        
        # 最后几行日志
        print_info "最近日志 (最后5行):"
        tail -5 "${LOG_FILE}" | sed 's/^/    /'
    else
        print_warn "  日志文件不存在"
    fi
    echo ""
    
    # 系统资源
    print_info "系统资源:"
    echo "  磁盘使用: $(get_disk_usage)"
    if command -v free &> /dev/null; then
        MEM_USAGE=$(free -h | awk 'NR==2 {print $3 "/" $2}')
        echo "  内存使用: ${MEM_USAGE}"
    fi
    echo ""
    
    # 访问地址
    print_info "访问地址:"
    echo "  应用: http://localhost:${PORT}"
    if [ "${PROFILE}" = "dev" ] || [ -z "${PROFILE}" ]; then
        echo "  Swagger: http://localhost:${PORT}/swagger-ui.html"
        echo "  API文档: http://localhost:${PORT}/v3/api-docs"
    fi
    echo ""
    
    # 操作提示
    print_info "操作命令:"
    echo "  查看日志: tail -f ${LOG_FILE}"
    echo "  停止应用: ./scripts/stop.sh"
    echo "  重启应用: ./scripts/restart.sh"
    
else
    print_error "应用状态: 未运行"
    echo ""
    print_info "操作命令:"
    echo "  启动应用: ./scripts/start.sh [dev|prod]"
fi

echo ""
print_info "======================================"
echo ""

