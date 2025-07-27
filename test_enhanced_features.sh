#!/bin/bash

echo "=== 测试增强的报表功能 ==="

# 等待服务启动
echo "等待服务启动..."
sleep 5

# 测试后端服务
echo "测试后端服务..."
curl -s http://localhost:8080/api/report/configs || echo "后端服务未启动"

# 测试前端服务
echo "测试前端服务..."
curl -s http://localhost:5173/ || echo "前端服务未启动"

echo ""
echo "=== 功能增强说明 ==="
echo "1. 统计配置功能："
echo "   - 支持求和、平均值、计数、最大值、最小值、标准差、方差等统计函数"
echo "   - 支持按字段分组统计"
echo "   - 支持排序和限制数量"
echo ""
echo "2. 样式配置功能："
echo "   - 支持图表主题选择（默认、暗色、浅色）"
echo "   - 支持显示/隐藏图例"
echo "   - 支持显示/隐藏数据标签"
echo "   - 支持动画效果开关"
echo "   - 支持自定义颜色配置"
echo ""
echo "3. 表格功能："
echo "   - 支持字段选择显示"
echo "   - 支持排序功能"
echo "   - 支持分页显示"
echo "   - 支持数据过滤"
echo "   - 支持搜索功能"
echo ""
echo "4. 数据过滤功能："
echo "   - 支持多种操作符：等于、不等于、大于、小于、包含、正则等"
echo "   - 支持AND/OR逻辑组合"
echo "   - 支持多条件过滤"
echo ""
echo "5. 新增图表类型："
echo "   - 散点图：支持X轴、Y轴、大小字段配置"
echo "   - 仪表盘：支持数值字段、最小值、最大值配置"
echo ""
echo "=== 使用说明 ==="
echo "1. 访问 http://localhost:5173 进入前端应用"
echo "2. 创建或编辑报表，配置统计和样式选项"
echo "3. 添加表格组件，配置显示字段和分页选项"
echo "4. 使用数据过滤功能筛选数据"
echo "5. 保存报表并查看效果"
echo ""
echo "=== 测试完成 ===" 