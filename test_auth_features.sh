#!/bin/bash

echo "=== MongoDB认证功能测试脚本 ==="
echo "此脚本将测试MongoDB用户密码模式的支持"
echo

# 检查MongoDB是否运行
echo "1. 检查MongoDB服务状态..."
if ! pgrep -x "mongod" > /dev/null; then
    echo "MongoDB未运行，请先启动MongoDB服务"
    echo "可以使用以下命令启动："
    echo "  brew services start mongodb-community"
    echo "  或者"
    echo "  mongod --config /usr/local/etc/mongod.conf"
    exit 1
fi
echo "✓ MongoDB服务正在运行"

# 创建测试用户
echo
echo "2. 创建MongoDB测试用户..."
echo "连接到MongoDB并创建测试用户..."

# 创建测试用户的JavaScript脚本
cat > /tmp/create_test_user.js << 'EOF'
// 切换到admin数据库
use admin

// 创建管理员用户（如果不存在）
db.createUser({
  user: "admin",
  pwd: "admin123",
  roles: [
    { role: "userAdminAnyDatabase", db: "admin" },
    { role: "readWriteAnyDatabase", db: "admin" }
  ]
})

// 切换到测试数据库
use mongo-reporter

// 创建测试用户
db.createUser({
  user: "testuser",
  pwd: "testpass123",
  roles: [
    { role: "readWrite", db: "mongo-reporter" }
  ]
})

// 插入一些测试数据
db.products.insertMany([
  { name: "产品A", price: 100, category: "电子产品" },
  { name: "产品B", price: 200, category: "服装" },
  { name: "产品C", price: 150, category: "电子产品" },
  { name: "产品D", price: 80, category: "食品" }
])

db.orders.insertMany([
  { customer: "客户1", amount: 300, date: new Date() },
  { customer: "客户2", amount: 150, date: new Date() },
  { customer: "客户3", amount: 450, date: new Date() }
])

print("测试用户和数据创建完成")
EOF

# 执行用户创建脚本
mongosh --quiet /tmp/create_test_user.js
if [ $? -eq 0 ]; then
    echo "✓ 测试用户创建成功"
else
    echo "⚠ 用户可能已存在或创建失败，继续测试..."
fi

# 启动后端服务
echo
echo "3. 启动后端服务..."
cd backend
if [ ! -f "./mvnw" ]; then
    echo "错误：找不到Maven wrapper"
    exit 1
fi

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "错误：未找到Java，请先安装Java 17或更高版本"
    exit 1
fi

echo "启动Spring Boot应用..."
./mvnw spring-boot:run > ../backend_auth.log 2>&1 &
BACKEND_PID=$!

# 等待后端启动
echo "等待后端服务启动..."
sleep 10

# 检查后端是否启动成功
if curl -s http://localhost:8080/api/datasource > /dev/null; then
    echo "✓ 后端服务启动成功"
else
    echo "✗ 后端服务启动失败，请检查日志：backend_auth.log"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

# 启动前端服务
echo
echo "4. 启动前端服务..."
cd ../frontend
if [ ! -f "package.json" ]; then
    echo "错误：找不到前端项目"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

echo "启动Vue开发服务器..."
npm run dev > ../frontend_auth.log 2>&1 &
FRONTEND_PID=$!

# 等待前端启动
echo "等待前端服务启动..."
sleep 5

# 检查前端是否启动成功
if curl -s http://localhost:5173 > /dev/null; then
    echo "✓ 前端服务启动成功"
else
    echo "✗ 前端服务启动失败，请检查日志：frontend_auth.log"
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
    exit 1
fi

echo
echo "=== 服务启动完成 ==="
echo "后端服务: http://localhost:8080"
echo "前端服务: http://localhost:5173"
echo
echo "=== 测试说明 ==="
echo "1. 打开浏览器访问: http://localhost:5173"
echo "2. 进入数据源管理页面"
echo "3. 创建新的数据源，启用认证功能"
echo "4. 使用以下认证信息进行测试："
echo "   - 用户名: testuser"
echo "   - 密码: testpass123"
echo "   - 认证数据库: mongo-reporter"
echo "   - 连接字符串: mongodb://localhost:27017/mongo-reporter"
echo
echo "=== 测试步骤 ==="
echo "1. 测试无认证连接（使用默认连接）"
echo "2. 测试认证连接（启用认证开关）"
echo "3. 测试错误的认证信息"
echo "4. 测试数据查询功能"
echo
echo "按 Ctrl+C 停止所有服务"

# 等待用户中断
trap 'echo "正在停止服务..."; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; rm -f /tmp/create_test_user.js; echo "服务已停止"; exit 0' INT

wait 