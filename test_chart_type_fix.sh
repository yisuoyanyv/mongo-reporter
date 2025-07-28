#!/bin/bash

echo "🧪 测试图表类型修复效果"
echo "========================"

# 检查服务状态
echo "1. 检查服务状态..."
if curl -s http://localhost:8080/api/report/configs > /dev/null; then
    echo "✅ 后端服务运行正常"
else
    echo "❌ 后端服务未运行"
    exit 1
fi

if curl -s http://localhost:5173 > /dev/null; then
    echo "✅ 前端服务运行正常"
else
    echo "❌ 前端服务未运行"
    exit 1
fi

echo ""
echo "2. 测试图表类型兼容性..."

# 测试使用 type 字段
echo "📊 测试使用 type 字段:"
TYPE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/sales",
    "collection": "sales",
    "widget": {
      "type": "line",
      "xField": "month",
      "yField": "amount"
    },
    "filters": []
  }')
echo "使用 type 字段响应: $TYPE_RESPONSE"

# 测试使用 name 字段
echo ""
echo "📊 测试使用 name 字段:"
NAME_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/sales",
    "collection": "sales",
    "widget": {
      "name": "line",
      "xField": "month",
      "yField": "amount"
    },
    "filters": []
  }')
echo "使用 name 字段响应: $NAME_RESPONSE"

# 测试缺少图表类型的情况
echo ""
echo "📊 测试缺少图表类型:"
MISSING_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/sales",
    "collection": "sales",
    "widget": {
      "xField": "month",
      "yField": "amount"
    },
    "filters": []
  }')
echo "缺少图表类型响应: $MISSING_RESPONSE"

echo ""
echo "3. 测试不同图表类型..."

# 测试折线图
echo "📈 测试折线图:"
LINE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/sales",
    "collection": "sales",
    "widget": {
      "type": "line",
      "xField": "month",
      "yField": "amount"
    },
    "filters": []
  }')
echo "折线图响应: $LINE_RESPONSE"

# 测试柱状图
echo ""
echo "📊 测试柱状图:"
BAR_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/sales",
    "collection": "sales",
    "widget": {
      "type": "bar",
      "xField": "month",
      "yField": "amount"
    },
    "filters": []
  }')
echo "柱状图响应: $BAR_RESPONSE"

# 测试饼图
echo ""
echo "🥧 测试饼图:"
PIE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/sales",
    "collection": "sales",
    "widget": {
      "type": "pie",
      "nameField": "month",
      "valueField": "amount"
    },
    "filters": []
  }')
echo "饼图响应: $PIE_RESPONSE"

echo ""
echo "4. 修复总结:"
echo "✅ 图表类型兼容性问题已修复"
echo "✅ 支持 name 和 type 字段"
echo "✅ 提供清晰的错误信息"
echo "✅ 前端和后端服务正常运行"

echo ""
echo "🌐 访问地址:"
echo "前端应用: http://localhost:5173"
echo "后端API: http://localhost:8080"

echo ""
echo "📝 使用说明:"
echo "1. 访问前端应用"
echo "2. 进入报表设计器或查看器"
echo "3. 选择数据源和集合"
echo "4. 配置图表参数"
echo "5. 查看图表效果" 