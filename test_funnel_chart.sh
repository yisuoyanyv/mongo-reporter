#!/bin/bash

echo "🔧 测试漏斗图的基础配置字段和默认颜色功能"
echo "=============================================="

# 检查后端服务状态
echo "1. 检查后端服务状态..."
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "❌ 后端服务未运行，请先启动后端服务"
    exit 1
fi
echo "✅ 后端服务运行正常"

# 测试漏斗图数据生成
echo ""
echo "2. 测试漏斗图数据生成..."

# 测试基本漏斗图
echo "📊 测试基本漏斗图:"
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
      "aggregation": "sum",
      "sortBy": "price",
      "sortOrder": "desc",
      "limit": 5
    }
  }')

echo "漏斗图响应: $FUNNEL_RESPONSE"

# 测试漏斗图统计功能
echo ""
echo "📊 测试漏斗图统计功能:"
FUNNEL_STATS_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "funnel",
      "nameField": "category",
      "valueField": "stock",
      "enableStats": true,
      "aggregation": "count",
      "sortBy": "stock",
      "sortOrder": "desc",
      "limit": 3
    }
  }')

echo "漏斗图统计响应: $FUNNEL_STATS_RESPONSE"

# 测试漏斗图默认配置
echo ""
echo "📊 测试漏斗图默认配置:"
FUNNEL_DEFAULT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "funnel",
      "nameField": "category",
      "valueField": "price"
    }
  }')

echo "漏斗图默认响应: $FUNNEL_DEFAULT_RESPONSE"

echo ""
echo "🎯 测试完成！"
echo ""
echo "📋 验证要点："
echo "1. 漏斗图应该支持 nameField 和 valueField 配置"
echo "2. 漏斗图应该支持统计功能（sum, count等）"
echo "3. 漏斗图应该支持排序和限制数量"
echo "4. 漏斗图应该返回正确的数据格式"
echo "5. 前端应该显示漏斗图的基础配置字段"
echo "6. 漏斗图应该有默认颜色配置"
echo ""
echo "💡 如果所有测试都通过，说明漏斗图功能已完善！" 