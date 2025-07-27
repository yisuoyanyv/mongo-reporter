#!/bin/bash

echo "🚀 MongoReporter 认证功能演示"
echo "================================"
echo

# 检查MongoDB状态
echo "📊 检查MongoDB服务状态..."
if pgrep -x "mongod" > /dev/null; then
    echo "✅ MongoDB服务正在运行"
else
    echo "❌ MongoDB服务未运行"
    echo "请先启动MongoDB服务："
    echo "  brew services start mongodb-community"
    exit 1
fi

# 创建演示用户和数据
echo
echo "👤 创建演示用户和数据..."
cat > /tmp/demo_setup.js << 'EOF'
// 切换到admin数据库
use admin

// 创建演示用户
db.createUser({
  user: "demo_user",
  pwd: "demo_pass123",
  roles: [
    { role: "readWrite", db: "demo_db" }
  ]
})

// 切换到演示数据库
use demo_db

// 创建演示数据
db.sales.insertMany([
  { product: "笔记本电脑", amount: 5999, date: new Date("2024-01-15"), region: "北京" },
  { product: "手机", amount: 3999, date: new Date("2024-01-16"), region: "上海" },
  { product: "平板电脑", amount: 2999, date: new Date("2024-01-17"), region: "广州" },
  { product: "耳机", amount: 599, date: new Date("2024-01-18"), region: "深圳" },
  { product: "键盘", amount: 299, date: new Date("2024-01-19"), region: "北京" }
])

db.customers.insertMany([
  { name: "张三", email: "zhangsan@example.com", vip: true, joinDate: new Date("2023-01-01") },
  { name: "李四", email: "lisi@example.com", vip: false, joinDate: new Date("2023-06-15") },
  { name: "王五", email: "wangwu@example.com", vip: true, joinDate: new Date("2022-12-01") }
])

print("演示数据创建完成！")
print("用户名: demo_user")
print("密码: demo_pass123")
print("数据库: demo_db")
EOF

mongosh --quiet /tmp/demo_setup.js
if [ $? -eq 0 ]; then
    echo "✅ 演示用户和数据创建成功"
else
    echo "⚠️  用户可能已存在，继续..."
fi

# 启动后端
echo
echo "🔧 启动后端服务..."
cd backend
if [ ! -f "./mvnw" ]; then
    echo "❌ 找不到Maven wrapper"
    exit 1
fi

echo "启动Spring Boot应用..."
./mvnw spring-boot:run > ../backend_demo.log 2>&1 &
BACKEND_PID=$!

# 等待后端启动
echo "⏳ 等待后端服务启动..."
for i in {1..15}; do
    if curl -s http://localhost:8080/api/datasource > /dev/null 2>&1; then
        echo "✅ 后端服务启动成功"
        break
    fi
    if [ $i -eq 15 ]; then
        echo "❌ 后端服务启动超时"
        kill $BACKEND_PID 2>/dev/null
        exit 1
    fi
    sleep 1
    echo -n "."
done

# 启动前端
echo
echo "🎨 启动前端服务..."
cd ../frontend
if [ ! -f "package.json" ]; then
    echo "❌ 找不到前端项目"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

echo "启动Vue开发服务器..."
npm run dev > ../frontend_demo.log 2>&1 &
FRONTEND_PID=$!

# 等待前端启动
echo "⏳ 等待前端服务启动..."
for i in {1..10}; do
    if curl -s http://localhost:5173 > /dev/null 2>&1; then
        echo "✅ 前端服务启动成功"
        break
    fi
    if [ $i -eq 10 ]; then
        echo "❌ 前端服务启动超时"
        kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
        exit 1
    fi
    sleep 1
    echo -n "."
done

echo
echo "🎉 服务启动完成！"
echo "================================"
echo "🌐 前端地址: http://localhost:5173"
echo "🔧 后端地址: http://localhost:8080"
echo
echo "📋 演示步骤："
echo "1. 打开浏览器访问: http://localhost:5173"
echo "2. 进入'数据源管理'页面"
echo "3. 点击'新增数据源'"
echo "4. 填写以下信息："
echo "   - 名称: 演示数据源"
echo "   - 连接字符串: mongodb://localhost:27017/demo_db"
echo "   - 启用认证: ✅"
echo "   - 用户名: demo_user"
echo "   - 密码: demo_pass123"
echo "   - 认证数据库: demo_db"
echo "5. 点击'测试连接'验证"
echo "6. 保存数据源"
echo "7. 进入'报表设计器'创建图表"
echo
echo "📊 可用数据："
echo "- sales集合: 销售数据"
echo "- customers集合: 客户数据"
echo
echo "🔍 测试场景："
echo "✅ 无认证连接测试"
echo "✅ 认证连接测试"
echo "✅ 错误认证信息测试"
echo "✅ 数据查询测试"
echo "✅ 图表生成测试"
echo
echo "按 Ctrl+C 停止所有服务"

# 清理函数
cleanup() {
    echo
    echo "🛑 正在停止服务..."
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
    rm -f /tmp/demo_setup.js
    echo "✅ 服务已停止"
    exit 0
}

trap cleanup INT

# 显示实时日志
echo
echo "📝 实时日志 (按 Ctrl+C 停止):"
echo "================================"
tail -f backend_demo.log frontend_demo.log 