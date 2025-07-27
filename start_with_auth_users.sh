#!/bin/bash

echo "🚀 启动MongoReporter应用（带认证用户）"
echo "======================================"

# 检查MongoDB是否运行
if ! pgrep -x "mongod" > /dev/null; then
    echo "❌ MongoDB未运行，请先启动MongoDB"
    exit 1
fi

echo "✅ MongoDB正在运行"

echo
echo "📋 用户信息（请手动创建）:"
echo "=========================="
echo "管理员用户: admin / admin123456"
echo "应用用户: mongo_reporter_user / mongo_reporter_pass123"
echo "测试用户: test_user / test_pass123"
echo
echo "🔗 连接字符串:"
echo "mongodb://mongo_reporter_user:mongo_reporter_pass123@localhost:27017/mongo-reporter?authSource=mongo-reporter"
echo

echo "📝 请按照以下步骤手动创建用户:"
echo "1. 打开新的终端窗口"
echo "2. 运行: mongo"
echo "3. 执行以下命令:"
echo
echo "use admin"
echo "db.createUser({user: 'admin', pwd: 'admin123456', roles: [{role: 'userAdminAnyDatabase', db: 'admin'}, {role: 'readWriteAnyDatabase', db: 'admin'}]})"
echo
echo "use mongo-reporter"
echo "db.createUser({user: 'mongo_reporter_user', pwd: 'mongo_reporter_pass123', roles: [{role: 'readWrite', db: 'mongo-reporter'}]})"
echo
echo "use test_db"
echo "db.createUser({user: 'test_user', pwd: 'test_pass123', roles: [{role: 'readWrite', db: 'test_db'}]})"
echo

read -p "用户创建完成后，按回车键启动应用..."

echo
echo "🔧 启动后端服务..."
cd backend
./mvnw spring-boot:run &
BACKEND_PID=$!

echo "⏳ 等待后端服务启动..."
sleep 10

echo "🌐 启动前端服务..."
cd ../frontend
npm run dev &
FRONTEND_PID=$!

echo
echo "✅ 服务启动完成！"
echo "=================="
echo "后端服务: http://localhost:8080"
echo "前端服务: http://localhost:5173"
echo
echo "📊 测试数据源:"
echo "mongodb://test_user:test_pass123@localhost:27017/test_db?authSource=test_db"
echo
echo "🔧 应用数据源:"
echo "mongodb://mongo_reporter_user:mongo_reporter_pass123@localhost:27017/mongo-reporter?authSource=mongo-reporter"
echo
echo "按 Ctrl+C 停止所有服务"

# 等待用户中断
trap "echo '🛑 停止服务...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit" INT
wait 