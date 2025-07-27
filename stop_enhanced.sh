#!/bin/bash

echo "=== 停止增强版MongoDB报表系统 ==="

# 停止后端服务
if [ -f ".backend.pid" ]; then
    BACKEND_PID=$(cat .backend.pid)
    if kill -0 $BACKEND_PID 2>/dev/null; then
        echo "停止后端服务 (PID: $BACKEND_PID)..."
        kill $BACKEND_PID
        sleep 3
        if kill -0 $BACKEND_PID 2>/dev/null; then
            echo "强制停止后端服务..."
            kill -9 $BACKEND_PID
        fi
        echo "✓ 后端服务已停止"
    else
        echo "后端服务未运行"
    fi
    rm -f .backend.pid
else
    echo "后端服务PID文件不存在"
fi

# 停止前端服务
if [ -f ".frontend.pid" ]; then
    FRONTEND_PID=$(cat .frontend.pid)
    if kill -0 $FRONTEND_PID 2>/dev/null; then
        echo "停止前端服务 (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID
        sleep 2
        if kill -0 $FRONTEND_PID 2>/dev/null; then
            echo "强制停止前端服务..."
            kill -9 $FRONTEND_PID
        fi
        echo "✓ 前端服务已停止"
    else
        echo "前端服务未运行"
    fi
    rm -f .frontend.pid
else
    echo "前端服务PID文件不存在"
fi

# 清理可能的残留进程
echo "清理残留进程..."
pkill -f "spring-boot:run" 2>/dev/null
pkill -f "vite" 2>/dev/null
pkill -f "npm run dev" 2>/dev/null

echo ""
echo "=== 服务停止完成 ==="
echo "所有服务已停止"
echo ""
echo "=== 清理日志文件 ==="
echo "如需清理日志文件，请手动删除:"
echo "  rm -f backend.log frontend.log" 