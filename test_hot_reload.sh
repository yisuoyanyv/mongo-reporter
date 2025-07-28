#!/bin/bash

echo "🧪 测试Spring Boot热更新功能"
echo ""

# 检查后端服务是否运行
echo "📋 检查后端服务状态..."
if curl -s http://localhost:8080/api/datasource > /dev/null; then
    echo "✅ 后端服务正在运行"
else
    echo "❌ 后端服务未运行，请先启动后端服务"
    echo "   运行命令: ./start_backend_dev.sh"
    exit 1
fi

echo ""
echo "📝 热更新测试说明："
echo "1. 修改 backend/src/main/java/ 下的任何Java文件"
echo "2. 保存文件后，观察控制台输出"
echo "3. 应用会自动重启，无需手动操作"
echo ""
echo "🔍 测试步骤："
echo "1. 打开另一个终端窗口"
echo "2. 修改任意Java文件（如添加一个注释）"
echo "3. 保存文件"
echo "4. 观察当前终端窗口的自动重启日志"
echo ""
echo "📊 当前时间: $(date)"
echo "⏰ 开始监控后端服务重启..."

# 监控日志变化
echo "🔍 监控中... (按 Ctrl+C 停止)"
echo ""

# 持续监控，显示重启相关的日志
while true; do
    if curl -s http://localhost:8080/api/datasource > /dev/null; then
        echo -n "."
        sleep 5
    else
        echo ""
        echo "⚠️  检测到服务重启..."
        sleep 10
        if curl -s http://localhost:8080/api/datasource > /dev/null; then
            echo "✅ 服务重启完成"
        else
            echo "❌ 服务重启失败"
        fi
    fi
done 