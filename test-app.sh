#!/bin/bash

echo "🧪 测试 MongoReporter 应用功能..."

# 测试后端API
echo "📊 测试后端API..."

# 1. 测试登录
echo "1. 测试用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    echo "✅ 登录成功"
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
else
    echo "❌ 登录失败"
    exit 1
fi

# 2. 测试数据源API
echo "2. 测试数据源API..."
DATASOURCE_RESPONSE=$(curl -s http://localhost:8080/api/datasource)
if echo "$DATASOURCE_RESPONSE" | grep -q "测试数据源"; then
    echo "✅ 数据源API正常"
else
    echo "❌ 数据源API异常"
fi

# 3. 测试报表配置API
echo "3. 测试报表配置API..."
REPORT_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/report/configs)
if [ -n "$REPORT_RESPONSE" ]; then
    echo "✅ 报表配置API正常"
else
    echo "❌ 报表配置API异常"
fi

# 4. 测试集合列表API
echo "4. 测试集合列表API..."
COLLECTION_RESPONSE=$(curl -s "http://localhost:8080/api/report/collections?uri=mongodb://localhost:27017/sales")
if [ -n "$COLLECTION_RESPONSE" ]; then
    echo "✅ 集合列表API正常"
else
    echo "❌ 集合列表API异常"
fi

# 5. 测试字段列表API
echo "5. 测试字段列表API..."
FIELD_RESPONSE=$(curl -s "http://localhost:8080/api/report/fields?uri=mongodb://localhost:27017/sales&collection=sales")
if [ -n "$FIELD_RESPONSE" ]; then
    echo "✅ 字段列表API正常"
else
    echo "❌ 字段列表API异常"
fi

# 6. 测试数据API
echo "6. 测试数据API..."
DATA_RESPONSE=$(curl -s "http://localhost:8080/api/report/data?uri=mongodb://localhost:27017/sales&collection=sales&limit=5")
if [ -n "$DATA_RESPONSE" ]; then
    echo "✅ 数据API正常"
else
    echo "❌ 数据API异常"
fi

# 测试前端
echo ""
echo "🎨 测试前端..."
FRONTEND_RESPONSE=$(curl -s -I http://localhost:5173 | head -1)
if echo "$FRONTEND_RESPONSE" | grep -q "200"; then
    echo "✅ 前端服务正常"
else
    echo "❌ 前端服务异常"
fi

echo ""
echo "🎉 应用功能测试完成！"
echo ""
echo "📱 前端地址: http://localhost:5173"
echo "🔧 后端地址: http://localhost:8080"
echo ""
echo "👤 测试账号: admin / admin123"
echo ""
echo "💡 提示: 打开浏览器访问 http://localhost:5173 开始使用应用" 