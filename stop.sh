#!/bin/bash

echo "🛑 停止 Mongo Reporter 应用..."

# 停止后端服务
echo "🔧 停止后端服务..."
pkill -f "java.*MongoReporterApplication" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ 后端服务已停止"
else
    echo "ℹ️  后端服务未运行"
fi

# 停止前端服务
echo "🎨 停止前端服务..."
pkill -f "vite" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ 前端服务已停止"
else
    echo "ℹ️  前端服务未运行"
fi

# 清理进程ID文件
rm -f .backend.pid .frontend.pid 2>/dev/null

echo "🎉 所有服务已停止" 