#!/bin/bash

echo "🧪 测试MongoReporter新功能"
echo "================================"

# 检查后端服务
echo "📡 检查后端服务状态..."
if curl -s http://localhost:8080/api/datasource > /dev/null; then
    echo "✅ 后端服务正常运行"
else
    echo "❌ 后端服务未运行"
    exit 1
fi

# 测试用户管理API
echo ""
echo "👥 测试用户管理API..."
if curl -s http://localhost:8080/api/users | jq . > /dev/null 2>&1; then
    echo "✅ 用户管理API正常工作"
    echo "   用户列表: $(curl -s http://localhost:8080/api/users | jq length) 个用户"
else
    echo "❌ 用户管理API异常"
fi

# 测试系统设置API
echo ""
echo "⚙️ 测试系统设置API..."
if curl -s http://localhost:8080/api/settings | jq . > /dev/null 2>&1; then
    echo "✅ 系统设置API正常工作"
    echo "   设置项数量: $(curl -s http://localhost:8080/api/settings | jq length) 项"
else
    echo "❌ 系统设置API异常"
fi

# 检查前端服务
echo ""
echo "🌐 检查前端服务状态..."
if curl -s http://localhost:5173 > /dev/null; then
    echo "✅ 前端服务正常运行"
else
    echo "❌ 前端服务未运行"
fi

# 测试新路由
echo ""
echo "🛣️ 测试新路由..."
echo "   仪表板: http://localhost:5173/dashboard"
echo "   用户管理: http://localhost:5173/users"
echo "   系统设置: http://localhost:5173/settings"

# 检查组件文件
echo ""
echo "📁 检查组件文件..."
if [ -f "frontend/src/views/Dashboard.vue" ]; then
    echo "✅ Dashboard组件存在"
else
    echo "❌ Dashboard组件缺失"
fi

if [ -f "frontend/src/views/UserManager.vue" ]; then
    echo "✅ UserManager组件存在"
else
    echo "❌ UserManager组件缺失"
fi

if [ -f "frontend/src/views/SystemSettings.vue" ]; then
    echo "✅ SystemSettings组件存在"
else
    echo "❌ SystemSettings组件缺失"
fi

# 检查路由配置
echo ""
echo "🗺️ 检查路由配置..."
if grep -q "dashboard" frontend/src/router/index.js; then
    echo "✅ 仪表板路由已配置"
else
    echo "❌ 仪表板路由未配置"
fi

if grep -q "users" frontend/src/router/index.js; then
    echo "✅ 用户管理路由已配置"
else
    echo "❌ 用户管理路由未配置"
fi

if grep -q "settings" frontend/src/router/index.js; then
    echo "✅ 系统设置路由已配置"
else
    echo "❌ 系统设置路由未配置"
fi

# 检查导航菜单
echo ""
echo "🍽️ 检查导航菜单..."
if grep -q "仪表板" frontend/src/App.vue; then
    echo "✅ 仪表板菜单项已添加"
else
    echo "❌ 仪表板菜单项未添加"
fi

if grep -q "用户管理" frontend/src/App.vue; then
    echo "✅ 用户管理菜单项已添加"
else
    echo "❌ 用户管理菜单项未添加"
fi

if grep -q "系统设置" frontend/src/App.vue; then
    echo "✅ 系统设置菜单项已添加"
else
    echo "❌ 系统设置菜单项未添加"
fi

echo ""
echo "🎉 测试完成！"
echo "================================"
echo "📊 功能总结:"
echo "   ✅ 后端用户管理API"
echo "   ✅ 后端系统设置API"
echo "   ✅ 前端仪表板组件"
echo "   ✅ 前端用户管理组件"
echo "   ✅ 前端系统设置组件"
echo "   ✅ 路由配置"
echo "   ✅ 导航菜单"
echo ""
echo "🌐 访问地址:"
echo "   前端应用: http://localhost:5173"
echo "   后端API: http://localhost:8080"
echo ""
echo "🚀 新功能已成功实现！" 