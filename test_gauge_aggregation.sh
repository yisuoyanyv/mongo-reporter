#!/bin/bash

echo "🔧 测试仪表盘组件的统计值功能"
echo "=================================="

# 检查后端服务状态
echo "1. 检查后端服务状态..."
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "❌ 后端服务未运行，请先启动后端服务"
    exit 1
fi
echo "✅ 后端服务运行正常"

# 测试不同的统计函数
echo ""
echo "2. 测试仪表盘组件的不同统计函数..."

# 测试求和
echo "📊 测试求和 (sum):"
SUM_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "gauge",
      "valueField": "price",
      "min": 0,
      "max": 20000,
      "enableStats": true,
      "aggregation": "sum"
    }
  }')

echo "求和响应: $SUM_RESPONSE"

# 测试平均值
echo ""
echo "📊 测试平均值 (avg):"
AVG_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "gauge",
      "valueField": "price",
      "min": 0,
      "max": 2000,
      "enableStats": true,
      "aggregation": "avg"
    }
  }')

echo "平均值响应: $AVG_RESPONSE"

# 测试最大值
echo ""
echo "📊 测试最大值 (max):"
MAX_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "gauge",
      "valueField": "price",
      "min": 0,
      "max": 20000,
      "enableStats": true,
      "aggregation": "max"
    }
  }')

echo "最大值响应: $MAX_RESPONSE"

# 测试最小值
echo ""
echo "📊 测试最小值 (min):"
MIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "gauge",
      "valueField": "price",
      "min": 0,
      "max": 2000,
      "enableStats": true,
      "aggregation": "min"
    }
  }')

echo "最小值响应: $MIN_RESPONSE"

# 测试计数
echo ""
echo "📊 测试计数 (count):"
COUNT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "gauge",
      "valueField": "price",
      "min": 0,
      "max": 100,
      "enableStats": true,
      "aggregation": "count"
    }
  }')

echo "计数响应: $COUNT_RESPONSE"

# 测试标准差
echo ""
echo "📊 测试标准差 (std):"
STD_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "gauge",
      "valueField": "price",
      "min": 0,
      "max": 2000,
      "enableStats": true,
      "aggregation": "std"
    }
  }')

echo "标准差响应: $STD_RESPONSE"

# 测试方差
echo ""
echo "📊 测试方差 (variance):"
VARIANCE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "gauge",
      "valueField": "price",
      "min": 0,
      "max": 1000000,
      "enableStats": true,
      "aggregation": "variance"
    }
  }')

echo "方差响应: $VARIANCE_RESPONSE"

# 测试未启用统计的情况（应该显示平均值）
echo ""
echo "📊 测试未启用统计 (默认平均值):"
DEFAULT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "gauge",
      "valueField": "price",
      "min": 0,
      "max": 2000
    }
  }')

echo "默认响应: $DEFAULT_RESPONSE"

echo ""
echo "🎯 测试完成！"
echo ""
echo "📋 验证要点："
echo "1. 每个统计函数都应该返回不同的数值"
echo "2. 显示名称应该正确对应统计函数"
echo "3. 未启用统计时应该显示平均值"
echo ""
echo "💡 如果所有测试都通过，说明仪表盘组件的统计功能已修复！" 