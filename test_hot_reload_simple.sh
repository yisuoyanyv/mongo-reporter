#!/bin/bash

echo "🧪 简单热更新测试"
echo ""

# 检查服务状态
echo "📋 检查后端服务..."
if curl -s http://localhost:8080/api/datasource > /dev/null; then
    echo "✅ 后端服务运行正常"
else
    echo "❌ 后端服务未运行"
    exit 1
fi

echo ""
echo "📝 测试步骤："
echo "1. 修改任意Java文件（如添加注释）"
echo "2. 保存文件"
echo "3. 观察控制台输出"
echo "4. 等待几秒钟让服务重启"
echo "5. 再次测试API"
echo ""

echo "🔍 当前API响应："
curl -s http://localhost:8080/api/datasource | jq '.[0].name' 2>/dev/null || echo "需要安装jq来格式化输出"

echo ""
echo "⏰ 现在可以修改Java文件来测试热更新..."
echo "修改完成后，等待几秒钟，然后运行："
echo "curl -s http://localhost:8080/api/datasource" 