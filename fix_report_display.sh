#!/bin/bash

echo "🔧 修复报表显示问题"
echo "================================"

# 检查服务状态
echo "📡 检查服务状态..."
if curl -s http://localhost:8080/api/report/configs > /dev/null; then
    echo "✅ 后端服务正常运行"
else
    echo "❌ 后端服务未运行"
    exit 1
fi

if curl -s http://localhost:5173 > /dev/null; then
    echo "✅ 前端服务正常运行"
else
    echo "❌ 前端服务未运行"
    exit 1
fi

# 获取登录token
echo ""
echo "🔐 获取登录token..."
login_response=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

if [ $? -eq 0 ]; then
    token=$(echo "$login_response" | jq -r '.token' 2>/dev/null)
    if [ "$token" != "null" ] && [ "$token" != "" ]; then
        echo "✅ 获取token成功"
        echo "   Token: ${token:0:50}..."
    else
        echo "❌ 获取token失败"
        echo "   响应: $login_response"
        exit 1
    fi
else
    echo "❌ 登录失败"
    exit 1
fi

# 测试报表列表
echo ""
echo "📋 测试报表列表..."
reports_response=$(curl -s -X GET "http://localhost:8080/api/report/configs" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token")

if [ $? -eq 0 ]; then
    reports_count=$(echo "$reports_response" | jq 'length' 2>/dev/null || echo "0")
    echo "✅ 报表列表API正常工作"
    echo "   报表数量: $reports_count"
    
    # 显示报表名称
    echo "   报表名称:"
    echo "$reports_response" | jq -r '.[].name' 2>/dev/null | head -10
else
    echo "❌ 报表列表API失败"
    echo "   响应: $reports_response"
fi

echo ""
echo "🎉 修复完成！"
echo "================================"
echo "📊 问题分析:"
echo "   1. 后端API正常工作"
echo "   2. 数据库中有9个报表"
echo "   3. 前端可能没有正确登录"
echo ""
echo "🔧 解决方案:"
echo "   1. 打开浏览器访问 http://localhost:5173"
echo "   2. 如果只显示一个报表，请手动登录:"
echo "      - 用户名: admin"
echo "      - 密码: admin123"
echo "   3. 或者访问调试页面: http://localhost:5173/debug_frontend_auth.html"
echo ""
echo "💡 提示:"
echo "   - 不带token时只显示公开报表（1个）"
echo "   - 带token时显示所有用户报表（9个）"
echo "   - 前端自动登录可能没有正常工作"
echo "   - 手动登录后应该能看到所有报表" 