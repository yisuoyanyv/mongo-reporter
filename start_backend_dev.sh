#!/bin/bash

echo "🚀 启动MongoReporter后端服务（开发模式，支持热更新）"

# 停止现有的Java进程
echo "📋 停止现有Java进程..."
pkill -f java
sleep 2

# 检查后端目录
if [ ! -d "backend" ]; then
    echo "❌ 错误：找不到backend目录"
    exit 1
fi

# 进入后端目录
cd backend

# 检查Maven wrapper
if [ ! -f "./mvnw" ]; then
    echo "❌ 错误：找不到Maven wrapper (./mvnw)"
    exit 1
fi

# 给mvnw执行权限
chmod +x ./mvnw

echo "🔧 编译并启动Spring Boot应用（开发模式）..."
echo "📝 热更新功能已启用："
echo "   - 修改Java文件后会自动重启"
echo "   - 修改配置文件后会自动重启"
echo "   - 静态资源修改不会触发重启"
echo ""

# 使用Spring Boot DevTools启动应用
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"

echo ""
echo "✅ 后端服务已启动在 http://localhost:8080"
echo "�� 热更新已启用，修改代码后会自动重启" 