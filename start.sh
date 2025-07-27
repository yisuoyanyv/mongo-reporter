#!/bin/bash

echo "🚀 启动 Mongo Reporter 应用..."

# 检查MongoDB是否运行
echo "📊 检查MongoDB状态..."
if ! pgrep -x "mongod" > /dev/null; then
    echo "❌ MongoDB未运行，请先启动MongoDB"
    echo "   可以使用命令: brew services start mongodb-community"
    exit 1
fi
echo "✅ MongoDB正在运行"

# 停止现有服务
echo "🛑 停止现有服务..."
pkill -f "java.*MongoReporterApplication" 2>/dev/null
pkill -f "vite" 2>/dev/null
sleep 2

# 启动后端服务
echo "🔧 启动后端服务..."
cd backend
./mvnw spring-boot:run > ../backend.log 2>&1 &
BACKEND_PID=$!
cd ..

# 等待后端启动
echo "⏳ 等待后端服务启动..."
sleep 15

# 检查后端是否启动成功
if curl -s http://localhost:8080/api/datasource > /dev/null; then
    echo "✅ 后端服务启动成功 (端口: 8080)"
else
    echo "❌ 后端服务启动失败，请检查 backend.log"
    exit 1
fi

# 启动前端服务
echo "🎨 启动前端服务..."
cd frontend
npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

# 等待前端启动
echo "⏳ 等待前端服务启动..."
sleep 5

# 检查前端是否启动成功
if curl -s http://localhost:5173 > /dev/null; then
    echo "✅ 前端服务启动成功 (端口: 5173)"
else
    echo "❌ 前端服务启动失败，请检查 frontend.log"
    exit 1
fi

echo ""
echo "🎉 应用启动完成！"
echo "📱 前端地址: http://localhost:5173"
echo "🔧 后端地址: http://localhost:8080"
echo ""
echo "📋 默认登录信息:"
echo "   用户名: admin"
echo "   密码: admin123"
echo ""
echo "📝 日志文件:"
echo "   后端日志: backend.log"
echo "   前端日志: frontend.log"
echo ""
echo "🛑 停止服务: 按 Ctrl+C 或运行 ./stop.sh"
echo ""

# 保存进程ID
echo $BACKEND_PID > .backend.pid
echo $FRONTEND_PID > .frontend.pid

# 等待用户中断
trap 'echo ""; echo "🛑 正在停止服务..."; pkill -f "java.*MongoReporterApplication"; pkill -f "vite"; rm -f .backend.pid .frontend.pid; echo "✅ 服务已停止"; exit 0' INT

# 保持脚本运行
while true; do
    sleep 1
done 