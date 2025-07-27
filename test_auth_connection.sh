#!/bin/bash

echo "🧪 测试MongoDB认证连接"
echo "======================"

echo "📋 测试用户信息:"
echo "=================="
echo "应用用户: mongo_reporter_user / mongo_reporter_pass123"
echo "测试用户: test_user / test_pass123"
echo

echo "🔗 测试连接字符串:"
echo "=================="
echo "应用数据库: mongodb://mongo_reporter_user:mongo_reporter_pass123@localhost:27017/mongo-reporter?authSource=mongo-reporter"
echo "测试数据库: mongodb://test_user:test_pass123@localhost:27017/test_db?authSource=test_db"
echo

echo "📊 测试应用数据库连接..."
mongo "mongodb://mongo_reporter_user:mongo_reporter_pass123@localhost:27017/mongo-reporter?authSource=mongo-reporter" --eval "db.runCommand({connectionStatus: 1})"

echo
echo "📊 测试测试数据库连接..."
mongo "mongodb://test_user:test_pass123@localhost:27017/test_db?authSource=test_db" --eval "db.runCommand({connectionStatus: 1})"

echo
echo "📊 测试数据查询..."
mongo "mongodb://test_user:test_pass123@localhost:27017/test_db?authSource=test_db" --eval "db.products.find().pretty()"

echo
echo "🎉 认证测试完成！"
echo
echo "🌐 应用访问地址:"
echo "=================="
echo "前端: http://localhost:5173"
echo "后端: http://localhost:8080"
echo
echo "📝 下一步操作:"
echo "1. 访问 http://localhost:5173"
echo "2. 进入数据源管理页面"
echo "3. 创建新的认证数据源"
echo "4. 使用以下连接字符串:"
echo "   mongodb://test_user:test_pass123@localhost:27017/test_db?authSource=test_db"
echo "5. 测试连接并创建报表" 