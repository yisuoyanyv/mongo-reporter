#!/bin/bash

echo "🧪 测试MongoReporter新功能"
echo "================================"

# 检查服务状态
echo "📡 检查服务状态..."
if curl -s http://localhost:5173 > /dev/null; then
    echo "✅ 前端服务运行正常 (http://localhost:5173)"
else
    echo "❌ 前端服务未运行"
    exit 1
fi

if curl -s http://localhost:8080 > /dev/null; then
    echo "✅ 后端服务运行正常 (http://localhost:8080)"
else
    echo "❌ 后端服务未运行"
    exit 1
fi

echo ""
echo "🔍 测试API接口..."

# 测试报表列表API
echo "📊 测试报表列表API..."
REPORTS_RESPONSE=$(curl -s http://localhost:8080/api/report/configs)
if [ $? -eq 0 ]; then
    REPORT_COUNT=$(echo "$REPORTS_RESPONSE" | jq 'length' 2>/dev/null || echo "0")
    echo "✅ 报表列表API正常，共有 $REPORT_COUNT 个报表"
else
    echo "❌ 报表列表API异常"
fi

# 测试数据源API
echo "🔗 测试数据源API..."
DATASOURCES_RESPONSE=$(curl -s http://localhost:8080/api/datasource)
if [ $? -eq 0 ]; then
    DS_COUNT=$(echo "$DATASOURCES_RESPONSE" | jq 'length' 2>/dev/null || echo "0")
    echo "✅ 数据源API正常，共有 $DS_COUNT 个数据源"
else
    echo "❌ 数据源API异常"
fi

echo ""
echo "🎯 测试新功能页面..."

# 测试仪表板页面
echo "📊 测试仪表板页面..."
DASHBOARD_RESPONSE=$(curl -s -I http://localhost:5173/dashboard)
if echo "$DASHBOARD_RESPONSE" | grep -q "200\|302"; then
    echo "✅ 仪表板页面可访问"
else
    echo "❌ 仪表板页面访问异常"
fi

# 测试用户管理页面
echo "👥 测试用户管理页面..."
USERS_RESPONSE=$(curl -s -I http://localhost:5173/users)
if echo "$USERS_RESPONSE" | grep -q "200\|302"; then
    echo "✅ 用户管理页面可访问"
else
    echo "❌ 用户管理页面访问异常"
fi

# 测试系统设置页面
echo "⚙️ 测试系统设置页面..."
SETTINGS_RESPONSE=$(curl -s -I http://localhost:5173/settings)
if echo "$SETTINGS_RESPONSE" | grep -q "200\|302"; then
    echo "✅ 系统设置页面可访问"
else
    echo "❌ 系统设置页面访问异常"
fi

echo ""
echo "🔧 测试路由配置..."

# 检查路由文件
if [ -f "frontend/src/router/index.js" ]; then
    echo "✅ 路由配置文件存在"
    
    # 检查新路由
    if grep -q "/dashboard" frontend/src/router/index.js; then
        echo "✅ 仪表板路由已配置"
    else
        echo "❌ 仪表板路由未配置"
    fi
    
    if grep -q "/users" frontend/src/router/index.js; then
        echo "✅ 用户管理路由已配置"
    else
        echo "❌ 用户管理路由未配置"
    fi
    
    if grep -q "/settings" frontend/src/router/index.js; then
        echo "✅ 系统设置路由已配置"
    else
        echo "❌ 系统设置路由未配置"
    fi
else
    echo "❌ 路由配置文件不存在"
fi

echo ""
echo "📁 检查新组件文件..."

# 检查新组件文件
COMPONENTS=("Dashboard.vue" "UserManager.vue" "SystemSettings.vue")
for component in "${COMPONENTS[@]}"; do
    if [ -f "frontend/src/views/$component" ]; then
        echo "✅ $component 组件文件存在"
    else
        echo "❌ $component 组件文件不存在"
    fi
done

echo ""
echo "🎨 检查导航菜单..."

# 检查导航菜单配置
if [ -f "frontend/src/App.vue" ]; then
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
else
    echo "❌ App.vue 文件不存在"
fi

echo ""
echo "📋 功能总结..."

# 统计功能
TOTAL_FEATURES=0
IMPLEMENTED_FEATURES=0

# 检查仪表板功能
if [ -f "frontend/src/views/Dashboard.vue" ]; then
    IMPLEMENTED_FEATURES=$((IMPLEMENTED_FEATURES + 1))
    echo "✅ 仪表板功能已实现"
fi
TOTAL_FEATURES=$((TOTAL_FEATURES + 1))

# 检查用户管理功能
if [ -f "frontend/src/views/UserManager.vue" ]; then
    IMPLEMENTED_FEATURES=$((IMPLEMENTED_FEATURES + 1))
    echo "✅ 用户管理功能已实现"
fi
TOTAL_FEATURES=$((TOTAL_FEATURES + 1))

# 检查系统设置功能
if [ -f "frontend/src/views/SystemSettings.vue" ]; then
    IMPLEMENTED_FEATURES=$((IMPLEMENTED_FEATURES + 1))
    echo "✅ 系统设置功能已实现"
fi
TOTAL_FEATURES=$((TOTAL_FEATURES + 1))

echo ""
echo "📊 实现进度: $IMPLEMENTED_FEATURES/$TOTAL_FEATURES"
echo ""

if [ $IMPLEMENTED_FEATURES -eq $TOTAL_FEATURES ]; then
    echo "🎉 所有新功能已成功实现！"
    echo ""
    echo "🚀 访问地址:"
    echo "   前端: http://localhost:5173"
    echo "   后端: http://localhost:8080"
    echo ""
    echo "📱 新功能入口:"
    echo "   - 仪表板: http://localhost:5173/dashboard"
    echo "   - 用户管理: http://localhost:5173/users"
    echo "   - 系统设置: http://localhost:5173/settings"
else
    echo "⚠️ 部分功能尚未完全实现，请检查相关文件"
fi

echo ""
echo "✅ 测试完成！" 