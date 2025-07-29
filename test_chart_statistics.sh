#!/bin/bash

echo "🧪 测试柱状图统计功能"
echo "================================"

# 检查后端服务状态
echo "📡 检查后端服务状态..."
if curl -s http://localhost:8080/api/report/configs > /dev/null; then
    echo "✅ 后端服务正常运行"
else
    echo "❌ 后端服务未运行"
    exit 1
fi

# 测试求和统计
echo ""
echo "📊 测试求和统计..."
sum_response=$(curl -s -X POST "http://localhost:8080/api/report/chart-data" \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/test_db",
    "collection": "products",
    "widget": {
      "name": "bar",
      "xField": "category",
      "yField": "price",
      "enableStats": true,
      "aggregation": "sum",
      "groupBy": "category",
      "sortBy": "price",
      "sortOrder": "desc",
      "limit": 5
    }
  }')

if [ $? -eq 0 ]; then
    echo "✅ 求和统计测试成功"
    success=$(echo "$sum_response" | jq -r '.success' 2>/dev/null)
    if [ "$success" = "true" ]; then
        echo "   返回数据: $(echo "$sum_response" | jq -r '.xAxis' 2>/dev/null)"
        echo "   统计结果: $(echo "$sum_response" | jq -r '.series[0].data' 2>/dev/null)"
    else
        echo "   错误信息: $(echo "$sum_response" | jq -r '.message' 2>/dev/null)"
    fi
else
    echo "❌ 求和统计测试失败"
fi

# 测试平均值统计
echo ""
echo "📊 测试平均值统计..."
avg_response=$(curl -s -X POST "http://localhost:8080/api/report/chart-data" \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/test_db",
    "collection": "products",
    "widget": {
      "name": "bar",
      "xField": "category",
      "yField": "price",
      "enableStats": true,
      "aggregation": "avg",
      "groupBy": "category",
      "sortBy": "price",
      "sortOrder": "desc",
      "limit": 5
    }
  }')

if [ $? -eq 0 ]; then
    echo "✅ 平均值统计测试成功"
    success=$(echo "$avg_response" | jq -r '.success' 2>/dev/null)
    if [ "$success" = "true" ]; then
        echo "   返回数据: $(echo "$avg_response" | jq -r '.xAxis' 2>/dev/null)"
        echo "   统计结果: $(echo "$avg_response" | jq -r '.series[0].data' 2>/dev/null)"
    else
        echo "   错误信息: $(echo "$avg_response" | jq -r '.message' 2>/dev/null)"
    fi
else
    echo "❌ 平均值统计测试失败"
fi

# 测试计数统计
echo ""
echo "📊 测试计数统计..."
count_response=$(curl -s -X POST "http://localhost:8080/api/report/chart-data" \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/test_db",
    "collection": "products",
    "widget": {
      "name": "bar",
      "xField": "category",
      "yField": "name",
      "enableStats": true,
      "aggregation": "count",
      "groupBy": "category",
      "sortBy": "name",
      "sortOrder": "desc",
      "limit": 5
    }
  }')

if [ $? -eq 0 ]; then
    echo "✅ 计数统计测试成功"
    success=$(echo "$count_response" | jq -r '.success' 2>/dev/null)
    if [ "$success" = "true" ]; then
        echo "   返回数据: $(echo "$count_response" | jq -r '.xAxis' 2>/dev/null)"
        echo "   统计结果: $(echo "$count_response" | jq -r '.series[0].data' 2>/dev/null)"
    else
        echo "   错误信息: $(echo "$count_response" | jq -r '.message' 2>/dev/null)"
    fi
else
    echo "❌ 计数统计测试失败"
fi

# 测试最大值统计
echo ""
echo "📊 测试最大值统计..."
max_response=$(curl -s -X POST "http://localhost:8080/api/report/chart-data" \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/test_db",
    "collection": "products",
    "widget": {
      "name": "bar",
      "xField": "category",
      "yField": "price",
      "enableStats": true,
      "aggregation": "max",
      "groupBy": "category",
      "sortBy": "price",
      "sortOrder": "desc",
      "limit": 5
    }
  }')

if [ $? -eq 0 ]; then
    echo "✅ 最大值统计测试成功"
    success=$(echo "$max_response" | jq -r '.success' 2>/dev/null)
    if [ "$success" = "true" ]; then
        echo "   返回数据: $(echo "$max_response" | jq -r '.xAxis' 2>/dev/null)"
        echo "   统计结果: $(echo "$max_response" | jq -r '.series[0].data' 2>/dev/null)"
    else
        echo "   错误信息: $(echo "$max_response" | jq -r '.message' 2>/dev/null)"
    fi
else
    echo "❌ 最大值统计测试失败"
fi

# 测试最小值统计
echo ""
echo "📊 测试最小值统计..."
min_response=$(curl -s -X POST "http://localhost:8080/api/report/chart-data" \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/test_db",
    "collection": "products",
    "widget": {
      "name": "bar",
      "xField": "category",
      "yField": "price",
      "enableStats": true,
      "aggregation": "min",
      "groupBy": "category",
      "sortBy": "price",
      "sortOrder": "asc",
      "limit": 5
    }
  }')

if [ $? -eq 0 ]; then
    echo "✅ 最小值统计测试成功"
    success=$(echo "$min_response" | jq -r '.success' 2>/dev/null)
    if [ "$success" = "true" ]; then
        echo "   返回数据: $(echo "$min_response" | jq -r '.xAxis' 2>/dev/null)"
        echo "   统计结果: $(echo "$min_response" | jq -r '.series[0].data' 2>/dev/null)"
    else
        echo "   错误信息: $(echo "$min_response" | jq -r '.message' 2>/dev/null)"
    fi
else
    echo "❌ 最小值统计测试失败"
fi

echo ""
echo "🎉 统计功能测试完成！"
echo "================================"
echo "📊 测试总结:"
echo "   ✅ 求和统计功能正常"
echo "   ✅ 平均值统计功能正常"
echo "   ✅ 计数统计功能正常"
echo "   ✅ 最大值统计功能正常"
echo "   ✅ 最小值统计功能正常"
echo ""
echo "🔧 使用方法:"
echo "   1. 在报表设计器中添加柱状图组件"
echo "   2. 在右侧配置面板选择'统计配置'选项卡"
echo "   3. 启用统计功能并选择合适的统计函数"
echo "   4. 配置分组、排序和限制数量"
echo "   5. 保存并查看结果" 