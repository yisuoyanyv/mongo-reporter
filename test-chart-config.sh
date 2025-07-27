#!/bin/bash

echo "🧪 测试图表配置保存和查看功能..."

# 测试数据
TEST_REPORT='{
  "name": "测试图表配置",
  "description": "测试图表配置是否正确保存和加载",
  "dataSourceUri": "mongodb://localhost:27017/mongo-reporter",
  "collection": "products",
  "widgets": [
    {
      "id": "test-widget-1",
      "name": "pie",
      "label": "饼图",
      "nameField": "category",
      "valueField": "price",
      "title": "产品分类统计"
    },
    {
      "id": "test-widget-2", 
      "name": "bar",
      "label": "柱状图",
      "xField": "category",
      "yField": "price",
      "title": "产品价格对比"
    }
  ]
}'

echo "📝 创建测试报表..."
RESPONSE=$(curl -s -X POST "http://localhost:8080/api/report/configs" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d "$TEST_REPORT")

echo "📊 响应: $RESPONSE"

# 提取报表ID
REPORT_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
echo "🆔 报表ID: $REPORT_ID"

if [ -n "$REPORT_ID" ]; then
  echo "✅ 报表创建成功"
  
  echo "📖 获取报表配置..."
GET_RESPONSE=$(curl -s "http://localhost:8080/api/report/configs/$REPORT_ID")
echo "📊 获取响应长度: ${#GET_RESPONSE}"
echo "📊 获取响应前100字符: ${GET_RESPONSE:0:100}..."
  
  echo "🎨 测试图表数据生成..."
  
  # 测试饼图
  echo "🥧 测试饼图数据..."
  PIE_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/report/chart-data" \
    -H "Content-Type: application/json" \
    -d "{
      \"uri\": \"mongodb://localhost:27017/mongo-reporter\",
      \"collection\": \"products\",
      \"widget\": {
        \"name\": \"pie\",
        \"nameField\": \"category\",
        \"valueField\": \"price\"
      }
    }")
  echo "📊 饼图响应: $PIE_RESPONSE"
  
  # 测试柱状图
  echo "📊 测试柱状图数据..."
  BAR_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/report/chart-data" \
    -H "Content-Type: application/json" \
    -d "{
      \"uri\": \"mongodb://localhost:27017/mongo-reporter\",
      \"collection\": \"products\",
      \"widget\": {
        \"name\": \"bar\",
        \"xField\": \"category\",
        \"yField\": \"price\"
      }
    }")
  echo "📊 柱状图响应: $BAR_RESPONSE"
  
  echo "🧹 清理测试数据..."
  curl -s -X DELETE "http://localhost:8080/api/report/configs/$REPORT_ID" \
    -H "Authorization: Bearer test-token"
  
  echo "✅ 测试完成"
else
  echo "❌ 报表创建失败"
fi 