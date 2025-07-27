#!/bin/bash

echo "=== 启动增强版MongoDB报表系统 ==="

# 检查MongoDB是否运行
echo "检查MongoDB服务..."
if ! pgrep -x "mongod" > /dev/null; then
    echo "警告: MongoDB服务未运行，请先启动MongoDB"
    echo "可以使用以下命令启动MongoDB:"
    echo "  brew services start mongodb-community"
    echo "  或者"
    echo "  mongod --config /usr/local/etc/mongod.conf"
    echo ""
fi

# 启动后端服务
echo "启动后端服务..."
cd backend
if [ ! -f "./mvnw" ]; then
    echo "错误: Maven wrapper不存在，请确保在正确的目录中"
    exit 1
fi

# 检查Java版本
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$java_version" -lt "17" ]; then
    echo "错误: 需要Java 17或更高版本，当前版本: $java_version"
    exit 1
fi

echo "使用Java版本: $(java -version 2>&1 | head -n 1)"
echo "启动Spring Boot应用..."

# 在后台启动后端服务
./mvnw spring-boot:run > ../backend.log 2>&1 &
BACKEND_PID=$!
echo "后端服务已启动 (PID: $BACKEND_PID)"

# 等待后端服务启动
echo "等待后端服务启动..."
sleep 10

# 检查后端服务是否启动成功
if curl -s http://localhost:8080/api/report/configs > /dev/null; then
    echo "✓ 后端服务启动成功"
else
    echo "✗ 后端服务启动失败，请检查日志: backend.log"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

# 启动前端服务
echo ""
echo "启动前端服务..."
cd ../frontend

# 检查Node.js版本
node_version=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$node_version" -lt "16" ]; then
    echo "错误: 需要Node.js 16或更高版本，当前版本: $node_version"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

echo "使用Node.js版本: $(node -v)"

# 检查依赖是否安装
if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm install
fi

# 在后台启动前端服务
npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo "前端服务已启动 (PID: $FRONTEND_PID)"

# 等待前端服务启动
echo "等待前端服务启动..."
sleep 5

# 检查前端服务是否启动成功
if curl -s http://localhost:5173/ > /dev/null; then
    echo "✓ 前端服务启动成功"
else
    echo "✗ 前端服务启动失败，请检查日志: frontend.log"
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
    exit 1
fi

# 保存进程ID到文件
echo $BACKEND_PID > .backend.pid
echo $FRONTEND_PID > .frontend.pid

echo ""
echo "=== 服务启动完成 ==="
echo "后端服务: http://localhost:8080"
echo "前端服务: http://localhost:5173"
echo ""
echo "=== 新增功能说明 ==="
echo "1. 统计配置: 支持求和、平均值、计数等统计函数"
echo "2. 样式配置: 支持主题、图例、标签、动画等样式选项"
echo "3. 表格功能: 支持字段选择、分页、排序、搜索"
echo "4. 数据过滤: 支持多种操作符和逻辑组合"
echo "5. 新增图表: 散点图、仪表盘等"
echo ""
echo "=== 使用说明 ==="
echo "1. 访问 http://localhost:5173 进入应用"
echo "2. 创建或编辑报表，体验新增功能"
echo "3. 查看详细文档: ENHANCED_FEATURES.md"
echo ""
echo "=== 停止服务 ==="
echo "使用以下命令停止服务:"
echo "  ./stop_enhanced.sh"
echo ""
echo "=== 日志文件 ==="
echo "后端日志: backend.log"
echo "前端日志: frontend.log"
echo ""
echo "服务正在运行中..." 