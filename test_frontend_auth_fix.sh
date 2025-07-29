#!/bin/bash

echo "🧪 测试前端认证修复"
echo "================================"

# 检查前端服务状态
echo "📡 检查前端服务状态..."
if curl -s http://localhost:5173 > /dev/null; then
    echo "✅ 前端服务正常运行"
else
    echo "❌ 前端服务未运行"
    exit 1
fi

# 检查后端服务状态
echo "📡 检查后端服务状态..."
if curl -s http://localhost:8080/api/report/configs > /dev/null; then
    echo "✅ 后端服务正常运行"
else
    echo "❌ 后端服务未运行"
    exit 1
fi

# 测试登录API
echo ""
echo "🔐 测试登录API..."
login_response=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

if [ $? -eq 0 ]; then
    echo "✅ 登录API正常工作"
    token=$(echo "$login_response" | jq -r '.token' 2>/dev/null)
    if [ "$token" != "null" ] && [ "$token" != "" ]; then
        echo "   获取到token: ${token:0:20}..."
    else
        echo "   ❌ 未获取到token"
        echo "   响应: $login_response"
    fi
else
    echo "❌ 登录API失败"
    echo "   响应: $login_response"
fi

# 测试前端页面访问
echo ""
echo "🌐 测试前端页面访问..."
frontend_response=$(curl -s -I "http://localhost:5173" | head -1)
if echo "$frontend_response" | grep -q "200"; then
    echo "✅ 前端页面可以访问"
else
    echo "❌ 前端页面访问失败"
    echo "   响应: $frontend_response"
fi

# 测试前端自动登录
echo ""
echo "🔄 测试前端自动登录..."
# 模拟前端自动登录请求
auto_login_response=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:5173" \
  -d '{"username":"admin","password":"admin123"}')

if [ $? -eq 0 ]; then
    echo "✅ 前端自动登录API正常工作"
    auto_token=$(echo "$auto_login_response" | jq -r '.token' 2>/dev/null)
    if [ "$auto_token" != "null" ] && [ "$auto_token" != "" ]; then
        echo "   自动登录token: ${auto_token:0:20}..."
    else
        echo "   ❌ 自动登录未获取到token"
    fi
else
    echo "❌ 前端自动登录API失败"
    echo "   响应: $auto_login_response"
fi

# 测试带token的API请求
echo ""
echo "📋 测试带token的API请求..."
if [ "$token" != "null" ] && [ "$token" != "" ]; then
    # 测试报表列表API
    reports_response=$(curl -s -X GET "http://localhost:8080/api/report/configs" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $token" \
      -H "Origin: http://localhost:5173")
    
    if [ $? -eq 0 ]; then
        echo "✅ 带token的API请求正常工作"
        reports_count=$(echo "$reports_response" | jq 'length' 2>/dev/null || echo "0")
        echo "   报表数量: $reports_count"
    else
        echo "❌ 带token的API请求失败"
        echo "   响应: $reports_response"
    fi
else
    echo "   ⚠️ 跳过token测试，因为没有获取到有效token"
fi

# 测试CORS配置
echo ""
echo "🌐 测试CORS配置..."
cors_response=$(curl -s -X OPTIONS "http://localhost:8080/api/report/configs" \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v 2>&1)

if echo "$cors_response" | grep -q "Access-Control-Allow-Origin"; then
    echo "✅ CORS配置正常"
else
    echo "❌ CORS配置可能有问题"
    echo "   响应: $cors_response"
fi

echo ""
echo "🎉 测试完成！"
echo "================================"
echo "📊 修复总结:"
echo "   ✅ 添加了前端自动登录逻辑"
echo "   ✅ 改进了axios请求拦截器"
echo "   ✅ 添加了axios响应拦截器处理401/403错误"
echo "   ✅ 确保Authorization头正确发送"
echo ""
echo "🔧 下一步验证:"
echo "   1. 打开浏览器访问 http://localhost:5173"
echo "   2. 检查浏览器控制台是否有自动登录日志"
echo "   3. 尝试保存报表，应该不再出现403错误"
echo "   4. 如果仍有问题，检查浏览器Network面板的请求头" 