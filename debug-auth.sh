#!/bin/bash

echo "🔍 调试认证问题..."

# 1. 测试登录
echo "1. 测试登录API..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

echo "登录响应: $LOGIN_RESPONSE"

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    echo "✅ 登录成功"
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    echo "Token: $TOKEN"
else
    echo "❌ 登录失败"
    exit 1
fi

# 2. 测试报表配置API（带token）
echo ""
echo "2. 测试报表配置API（带token）..."
REPORT_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/report/configs)
echo "报表配置响应: $REPORT_RESPONSE"

# 3. 测试报表配置API（不带token）
echo ""
echo "3. 测试报表配置API（不带token）..."
REPORT_RESPONSE_NO_TOKEN=$(curl -s http://localhost:8080/api/report/configs)
echo "无token响应: $REPORT_RESPONSE_NO_TOKEN"

# 4. 检查前端localStorage
echo ""
echo "4. 前端认证状态检查..."
echo "请打开浏览器开发者工具，检查localStorage中是否有token:"
echo "localStorage.getItem('token')"

echo ""
echo "🔧 可能的解决方案:"
echo "1. 确保用户已登录"
echo "2. 检查localStorage中是否有token"
echo "3. 检查axios拦截器是否正确设置Authorization头"
echo "4. 检查后端CORS配置" 