#!/bin/bash

echo "=== 测试ElColorPicker类型错误修复 ==="

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
echo "1. ✅ 修复了ElColorPicker的modelValue类型错误"
echo "2. ✅ 将单个颜色选择器改为多颜色管理"
echo "3. ✅ 添加了颜色添加、删除、清空功能"
echo "4. ✅ 添加了默认颜色预设"
echo "5. ✅ 改进了颜色选择器的UI设计"

echo ""
echo "=== 测试步骤 ==="
echo "1. 访问 http://localhost:5173"
echo "2. 进入报表设计器页面"
echo "3. 添加一个图表组件（如折线图）"
echo "4. 点击配置按钮"
echo "5. 切换到'样式'标签页"
echo "6. 测试颜色配置功能："
echo "   - 添加新颜色"
echo "   - 删除已有颜色"
echo "   - 添加默认颜色"
echo "   - 清空所有颜色"
echo "7. 检查浏览器控制台是否还有类型错误"

echo ""
echo "=== 预期结果 ==="
echo "✅ 不再出现 'Invalid prop: type check failed for prop "modelValue"' 错误"
echo "✅ 颜色选择器正常工作"
echo "✅ 可以管理多个颜色"
echo "✅ UI界面美观易用"

echo ""
echo "=== 技术细节 ==="
echo "- 将 v-model 从数组改为单个颜色字符串"
echo "- 使用数组管理多个颜色"
echo "- 添加颜色管理方法：addColor, removeColor, addDefaultColors, clearColors"
echo "- 改进UI设计，支持颜色预览和删除"

echo ""
echo "=== 如果仍有问题 ==="
echo "1. 检查浏览器控制台的错误信息"
echo "2. 确认Element Plus版本兼容性"
echo "3. 验证Vue 3和Element Plus的集成"
echo "4. 检查组件导入是否正确"

echo ""
echo "=== 测试完成 ===" 