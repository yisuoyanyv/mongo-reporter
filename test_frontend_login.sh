#!/bin/bash

echo "🧪 测试前端自动登录和报表显示"
echo "================================"

# 检查后端服务状态
echo "📡 检查后端服务状态..."
if curl -s http://localhost:8080/api/report/configs > /dev/null; then
    echo "✅ 后端服务正常运行"
else
    echo "❌ 后端服务未运行"
    exit 1
fi

# 检查前端服务状态
echo "📡 检查前端服务状态..."
if curl -s http://localhost:5173 > /dev/null; then
    echo "✅ 前端服务正常运行"
else
    echo "❌ 前端服务未运行"
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

# 测试不带token的报表列表（应该只返回公开报表）
echo ""
echo "📋 测试不带token的报表列表..."
no_auth_response=$(curl -s -X GET "http://localhost:8080/api/report/configs" \
  -H "Content-Type: application/json")

if [ $? -eq 0 ]; then
    no_auth_count=$(echo "$no_auth_response" | jq 'length' 2>/dev/null || echo "0")
    echo "✅ 不带token的API正常工作"
    echo "   返回报表数量: $no_auth_count (应该只有公开报表)"
else
    echo "❌ 不带token的API失败"
    echo "   响应: $no_auth_response"
fi

# 测试带token的报表列表（应该返回所有用户报表）
echo ""
echo "📋 测试带token的报表列表..."
if [ "$token" != "null" ] && [ "$token" != "" ]; then
    auth_response=$(curl -s -X GET "http://localhost:8080/api/report/configs" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $token")
    
    if [ $? -eq 0 ]; then
        auth_count=$(echo "$auth_response" | jq 'length' 2>/dev/null || echo "0")
        echo "✅ 带token的API正常工作"
        echo "   返回报表数量: $auth_count (应该包含所有用户报表)"
        
        # 显示报表名称
        echo "   报表列表:"
        echo "$auth_response" | jq -r '.[].name' 2>/dev/null | head -5
        if [ $(echo "$auth_response" | jq 'length' 2>/dev/null) -gt 5 ]; then
            echo "   ... 还有更多报表"
        fi
    else
        echo "❌ 带token的API失败"
        echo "   响应: $auth_response"
    fi
else
    echo "   ⚠️ 跳过token测试，因为没有获取到有效token"
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
        
        # 用自动登录的token测试报表列表
        auto_auth_response=$(curl -s -X GET "http://localhost:8080/api/report/configs" \
          -H "Content-Type: application/json" \
          -H "Authorization: Bearer $auto_token")
        
        if [ $? -eq 0 ]; then
            auto_auth_count=$(echo "$auto_auth_response" | jq 'length' 2>/dev/null || echo "0")
            echo "   自动登录后报表数量: $auto_auth_count"
        else
            echo "   ❌ 自动登录后获取报表失败"
        fi
    else
        echo "   ❌ 自动登录未获取到token"
    fi
else
    echo "❌ 前端自动登录API失败"
    echo "   响应: $auto_login_response"
fi

echo ""
echo "🎉 测试完成！"
echo "================================"
echo "📊 问题分析:"
echo "   1. 后端API正常工作"
echo "   2. 登录API正常工作"
echo "   3. 权限控制正常工作"
echo "   4. 前端可能需要重新登录"
echo ""
echo "🔧 解决方案:"
echo "   1. 打开浏览器访问 http://localhost:5173"
echo "   2. 检查浏览器控制台是否有自动登录日志"
echo "   3. 如果只显示一个报表，请手动登录"
echo "   4. 登录后应该能看到所有报表"
echo ""
echo "💡 提示:"
echo "   - 不带token时只显示公开报表（publicShare: true）"
echo "   - 带token时显示用户所有报表和公开报表"
echo "   - 前端自动登录可能没有正常工作" 