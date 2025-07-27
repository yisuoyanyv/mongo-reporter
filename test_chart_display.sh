#!/bin/bash

echo "=== 测试图表显示问题修复 ==="

# 检查服务状态
echo "检查后端服务..."
if curl -s http://localhost:8080/api/report/configs > /dev/null; then
    echo "✅ 后端服务运行正常"
else
    echo "❌ 后端服务未运行"
    exit 1
fi

echo "检查前端服务..."
if curl -s http://localhost:5173/ > /dev/null; then
    echo "✅ 前端服务运行正常"
else
    echo "❌ 前端服务未运行"
    exit 1
fi

echo ""
echo "=== 修复内容说明 ==="
echo "1. ✅ 添加了axios请求拦截器，自动添加Authorization header"
echo "2. ✅ 添加了axios响应拦截器，处理401错误"
echo "3. ✅ 修改了后端/chart-data端点，支持可选的Authorization header"
echo "4. ✅ 确保CORS配置正确"

echo ""
echo "=== 测试步骤 ==="
echo "1. 访问 http://localhost:5173"
echo "2. 等待自动登录完成"
echo "3. 进入报表设计器页面"
echo "4. 添加折线图或饼图组件"
echo "5. 配置数据源和字段"
echo "6. 检查图表是否正常显示"

echo ""
echo "=== 预期结果 ==="
echo "✅ 折线图和饼图正常显示"
echo "✅ 不再出现403错误"
echo "✅ 不再出现Authorization header缺失警告"
echo "✅ 图表数据正确加载"

echo ""
echo "=== 技术细节 ==="
echo "- axios拦截器自动添加Bearer token"
echo "- 后端支持可选的Authorization header"
echo "- CORS配置允许Authorization header"
echo "- 401错误自动处理，跳转登录页"

echo ""
echo "=== 如果仍有问题 ==="
echo "1. 检查浏览器控制台的网络请求"
echo "2. 确认Authorization header是否正确发送"
echo "3. 检查后端日志是否有错误信息"
echo "4. 验证MongoDB连接是否正常"

echo ""
echo "=== 测试完成 ===" 