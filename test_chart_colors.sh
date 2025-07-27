#!/bin/bash

echo "=== 测试图表颜色修复 ==="

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
echo "=== 测试饼图颜色 ==="
echo "测试products集合的饼图数据（应该显示彩色扇形）..."

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
echo "=== 测试折线图颜色 ==="
echo "测试orders集合的折线图数据（应该显示彩色线条）..."

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
echo "=== 颜色修复说明 ==="
echo "修复内容："
echo "1. 为所有图表添加了默认颜色配置"
echo "2. 默认颜色数组：['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']"
echo "3. 修复了ReportDesigner.vue和ReportViewer.vue中的颜色配置"
echo "4. 确保当没有自定义颜色时，图表仍能正常显示"

echo ""
echo "=== 测试步骤 ==="
echo "1. 访问 http://localhost:5173"
echo "2. 进入报表设计器"
echo "3. 添加饼图组件"
echo "4. 配置字段：nameField=category, valueField=price"
echo "5. 检查图表是否显示彩色扇形"
echo "6. 添加折线图组件"
echo "7. 配置字段：xField=date, yField=amount"
echo "8. 检查图表是否显示彩色线条"

echo ""
echo "=== 预期结果 ==="
echo "饼图应该显示3个不同颜色的扇形："
echo "- 生活用品: 蓝色 (#5470c6)"
echo "- 办公用品: 绿色 (#91cc75)" 
echo "- 电子产品: 黄色 (#fac858)"

echo ""
echo "折线图应该显示彩色线条和数据点"

echo ""
echo "=== 如果仍有问题 ==="
echo "1. 检查浏览器控制台是否有错误"
echo "2. 确认ECharts正确加载"
echo "3. 验证图表容器DOM元素"
echo "4. 查看网络请求是否正常"

echo ""
echo "=== 修复完成 ===" 