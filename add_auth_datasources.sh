#!/bin/bash

echo "🔧 添加认证数据源"
echo "=================="

# 检查后端服务是否运行
if ! curl -s http://localhost:8080/api/datasource > /dev/null; then
    echo "❌ 后端服务未运行，请先启动后端服务"
    exit 1
fi

echo "✅ 后端服务正在运行"
echo

# 数据源配置 - 只使用已存在的用户
declare -a datasources=(
    '{
        "name": "测试数据库(认证)",
        "uri": "mongodb://localhost:27017/test_db",
        "username": "test_user",
        "password": "test_pass123",
        "authDatabase": "test_db",
        "useAuth": true,
        "default": false
    }'
    '{
        "name": "应用数据库(认证)",
        "uri": "mongodb://localhost:27017/mongo-reporter",
        "username": "mongo_reporter_user",
        "password": "mongo_reporter_pass123",
        "authDatabase": "mongo-reporter",
        "useAuth": true,
        "default": false
    }'
)

# 添加数据源
for i in "${!datasources[@]}"; do
    echo "📝 添加数据源 $((i+1)):"
    echo "${datasources[$i]}" | jq -r '.name'
    
    response=$(curl -s -X POST http://localhost:8080/api/datasource \
        -H "Content-Type: application/json" \
        -d "${datasources[$i]}")
    
    if echo "$response" | jq -e '.id' > /dev/null; then
        echo "✅ 添加成功: $(echo "$response" | jq -r '.name')"
    else
        echo "❌ 添加失败: $response"
    fi
    echo
done

echo "🎉 数据源添加完成！"
echo
echo "📋 所有数据源:"
echo "======================"
curl -s http://localhost:8080/api/datasource | jq -r '.[] | "• \(.name) - \(.uri) - 认证: \(.useAuth)"'

echo
echo "🌐 访问地址: http://localhost:5173"
echo "📝 在数据源管理页面中可以看到新添加的认证数据源" 