#!/bin/bash

echo "🧪 测试保存报表403错误修复"
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

# 测试带token的报表列表API
echo ""
echo "📋 测试带token的报表列表API..."
if [ "$token" != "null" ] && [ "$token" != "" ]; then
    reports_response=$(curl -s -X GET "http://localhost:8080/api/report/configs" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $token")
    
    if [ $? -eq 0 ]; then
        echo "✅ 带token的报表列表API正常工作"
        reports_count=$(echo "$reports_response" | jq 'length' 2>/dev/null || echo "0")
        echo "   报表数量: $reports_count"
        
        # 获取第一个报表的ID用于测试
        if [ "$reports_count" -gt 0 ]; then
            first_report_id=$(echo "$reports_response" | jq -r '.[0].id' 2>/dev/null)
            echo "   第一个报表ID: $first_report_id"
            
            # 测试更新报表
            echo ""
            echo "📝 测试更新报表..."
            update_data='{
              "name": "测试更新报表",
              "description": "用于测试403错误修复的报表",
              "dataSourceUri": "mongodb://localhost:27017/mongo-reporter",
              "collection": "products",
              "widgets": [
                {
                  "id": "test-widget-1",
                  "name": "pie",
                  "label": "饼图",
                  "nameField": "category",
                  "valueField": "price",
                  "title": "产品分类统计"
                }
              ]
            }'
            
            update_response=$(curl -s -X PUT "http://localhost:8080/api/report/configs/$first_report_id" \
              -H "Content-Type: application/json" \
              -H "Authorization: Bearer $token" \
              -d "$update_data")
            
            if [ $? -eq 0 ]; then
                echo "✅ 更新报表API正常工作"
                echo "   响应: $(echo "$update_response" | jq -r '.name' 2>/dev/null)"
            else
                echo "❌ 更新报表API失败"
                echo "   响应: $update_response"
            fi
        else
            echo "   ⚠️ 没有可用的报表进行测试"
        fi
    else
        echo "❌ 带token的报表列表API失败"
        echo "   响应: $reports_response"
    fi
else
    echo "   ⚠️ 跳过token测试，因为没有获取到有效token"
fi

# 测试不带token的报表列表API（应该返回403）
echo ""
echo "🚫 测试不带token的报表列表API（应该返回403）..."
no_auth_response=$(curl -s -X GET "http://localhost:8080/api/report/configs" \
  -H "Content-Type: application/json")

if echo "$no_auth_response" | grep -q "403\|Forbidden\|Unauthorized"; then
    echo "✅ 不带token的API正确返回403"
else
    echo "❌ 不带token的API没有返回403"
    echo "   响应: $no_auth_response"
fi

echo ""
echo "🎉 测试完成！"
echo "================================"
echo "📊 问题分析:"
echo "   1. 检查后端服务是否正常运行"
echo "   2. 检查登录API是否正常工作"
echo "   3. 检查token是否正确生成"
echo "   4. 检查前端是否正确发送Authorization头"
echo "   5. 检查后端是否正确验证token"
echo ""
echo "🔧 解决方案:"
echo "   1. 确保前端在发送请求时包含有效的Authorization头"
echo "   2. 确保用户已登录并获取到有效的JWT token"
echo "   3. 检查后端的JWT验证逻辑"
echo "   4. 检查Spring Security配置" 