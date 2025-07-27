#!/bin/bash

echo "=== 诊断图表显示问题 ==="

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
echo "=== 测试图表数据API ==="

# 测试折线图数据
echo "测试折线图数据..."
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
echo "测试饼图数据..."
curl -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "orders",
    "widget": {
      "name": "pie",
      "nameField": "product",
      "valueField": "amount"
    },
    "filters": []
  }' | jq '.'

echo ""
echo "测试表格数据..."
curl -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "orders",
    "widget": {
      "name": "table",
      "displayFields": ["date", "product", "amount"],
      "limit": 10
    },
    "filters": []
  }' | jq '.'

echo ""
echo "=== 问题诊断 ==="
echo "1. 检查MongoDB数据是否存在..."
echo "2. 检查字段名称是否正确..."
echo "3. 检查API响应格式..."
echo "4. 检查前端图表配置..."

echo ""
echo "=== 常见问题解决方案 ==="
echo "1. 折线图/饼图不显示："
echo "   - 检查字段名称是否正确"
echo "   - 检查数据格式是否匹配"
echo "   - 检查ECharts配置是否正确"
echo ""
echo "2. 表格提示不支持："
echo "   - 检查表格组件配置"
echo "   - 检查displayFields设置"
echo "   - 检查数据加载逻辑"
echo ""
echo "3. 调试步骤："
echo "   - 打开浏览器开发者工具"
echo "   - 查看Network标签页的API请求"
echo "   - 查看Console标签页的错误信息"
echo "   - 检查Vue组件的数据绑定"

echo ""
echo "=== 测试完成 ===" 