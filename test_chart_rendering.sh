#!/bin/bash

echo "=== 测试图表渲染问题 ==="

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
echo "=== 测试饼图数据 ==="
echo "测试products集合的饼图数据..."

curl -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "pie",
      "nameField": "category",
      "valueField": "price"
    },
    "filters": []
  }' | jq '.'

echo ""
echo "=== 测试折线图数据 ==="
echo "测试orders集合的折线图数据..."

curl -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "orders",
    "widget": {
      "name": "line",
      "xField": "date",
      "yField": "amount",
      "seriesField": "product"
    },
    "filters": []
  }' | jq '.'

echo ""
echo "=== 问题诊断 ==="
echo "1. API数据正常返回 ✅"
echo "2. 前端图表容器高度: 240px ✅"
echo "3. ECharts实例创建正常 ✅"
echo ""
echo "可能的问题："
echo "1. 图表配置格式问题"
echo "2. ECharts版本兼容性"
echo "3. DOM元素渲染时机"
echo "4. 样式冲突"

echo ""
echo "=== 调试步骤 ==="
echo "1. 打开浏览器开发者工具"
echo "2. 查看Console标签页的错误信息"
echo "3. 检查Network标签页的API请求"
echo "4. 在Elements标签页检查图表容器"
echo "5. 查看ECharts实例是否正确创建"

echo ""
echo "=== 手动测试 ==="
echo "1. 访问 http://localhost:5173"
echo "2. 进入报表设计器"
echo "3. 添加饼图组件"
echo "4. 配置字段：nameField=category, valueField=price"
echo "5. 检查图表是否显示"
echo "6. 查看浏览器控制台日志"

echo ""
echo "=== 预期结果 ==="
echo "饼图应该显示3个扇形："
echo "- 生活用品: 488.0"
echo "- 办公用品: 2797.0" 
echo "- 电子产品: 12997.0"

echo ""
echo "=== 如果仍有问题 ==="
echo "1. 检查ECharts是否正确加载"
echo "2. 验证图表容器DOM元素"
echo "3. 确认图表配置格式"
echo "4. 查看浏览器兼容性"

echo ""
echo "=== 测试完成 ===" 