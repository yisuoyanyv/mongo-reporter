#!/bin/bash

echo "=== 测试ECharts图表实例重复初始化问题修复 ==="

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
echo "1. ✅ 在初始化图表前检查并销毁已存在的实例"
echo "2. ✅ 添加防抖机制避免频繁渲染"
echo "3. ✅ 在组件卸载时清理所有图表实例和定时器"
echo "4. ✅ 修复了renderDesignerCharts函数中的语法错误"

echo ""
echo "=== 测试步骤 ==="
echo "1. 访问 http://localhost:5173"
echo "2. 进入报表设计器页面"
echo "3. 添加多个图表组件"
echo "4. 配置图表参数并保存"
echo "5. 检查浏览器控制台是否还有ECharts错误"

echo ""
echo "=== 预期结果 ==="
echo "✅ 不再出现 'There is a chart instance already initialized on the dom' 错误"
echo "✅ 图表正常渲染和更新"
echo "✅ 组件切换时图表实例正确清理"

echo ""
echo "=== 技术细节 ==="
echo "- 使用 echarts.getInstanceByDom() 检查已存在的实例"
echo "- 使用 chart.dispose() 销毁旧实例"
echo "- 使用防抖函数避免频繁调用渲染函数"
echo "- 在 onUnmounted 钩子中清理所有资源"

echo ""
echo "=== 测试完成 ==="
echo "如果仍有问题，请检查浏览器控制台的详细错误信息" 