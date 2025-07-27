#!/bin/bash

echo "🔧 测试所有图表类型的基础配置字段和默认颜色功能"
echo "=================================================="

# 检查后端服务状态
echo "1. 检查后端服务状态..."
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "❌ 后端服务未运行，请先启动后端服务"
    exit 1
fi
echo "✅ 后端服务运行正常"

# 测试所有图表类型
echo ""
echo "2. 测试所有图表类型..."

# 测试折线图
echo "📊 测试折线图:"
LINE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "line",
      "xField": "category",
      "yField": "price",
      "enableStats": true,
      "aggregation": "sum"
    }
  }')
echo "折线图响应: $LINE_RESPONSE"

# 测试柱状图
echo ""
echo "📊 测试柱状图:"
BAR_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "bar",
      "xField": "category",
      "yField": "price",
      "enableStats": true,
      "aggregation": "sum"
    }
  }')
echo "柱状图响应: $BAR_RESPONSE"

# 测试饼图
echo ""
echo "📊 测试饼图:"
PIE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "pie",
      "nameField": "category",
      "valueField": "price",
      "enableStats": true,
      "aggregation": "sum"
    }
  }')
echo "饼图响应: $PIE_RESPONSE"

# 测试散点图
echo ""
echo "📊 测试散点图:"
SCATTER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "scatter",
      "xField": "price",
      "yField": "stock",
      "sizeField": "price"
    }
  }')
echo "散点图响应: $SCATTER_RESPONSE"

# 测试仪表盘
echo ""
echo "📊 测试仪表盘:"
GAUGE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "gauge",
      "valueField": "price",
      "enableStats": true,
      "aggregation": "avg"
    }
  }')
echo "仪表盘响应: $GAUGE_RESPONSE"

# 测试漏斗图
echo ""
echo "📊 测试漏斗图:"
FUNNEL_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "funnel",
      "nameField": "category",
      "valueField": "price",
      "enableStats": true,
      "aggregation": "sum"
    }
  }')
echo "漏斗图响应: $FUNNEL_RESPONSE"

# 测试雷达图
echo ""
echo "📊 测试雷达图:"
RADAR_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "radar",
      "nameField": "category",
      "valueField": "price",
      "seriesField": "category"
    }
  }')
echo "雷达图响应: $RADAR_RESPONSE"

# 测试表格
echo ""
echo "📊 测试表格:"
TABLE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "table",
      "limit": 5
    }
  }')
echo "表格响应: $TABLE_RESPONSE"

echo ""
echo "🎯 测试完成！"
echo ""
echo "📋 验证要点："
echo "1. 所有图表类型都应该支持基础配置字段"
echo "2. 所有图表类型都应该有默认颜色配置"
echo "3. 所有图表类型都应该返回正确的数据格式"
echo "4. 前端应该显示所有图表类型的基础配置字段"
echo "5. 后端应该支持所有图表类型的数据处理"
echo ""
echo "💡 如果所有测试都通过，说明所有图表类型功能已完善！" 