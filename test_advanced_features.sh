#!/bin/bash

echo "=== 测试报表设计器高级功能 ==="

# 等待后端启动
echo "等待后端服务启动..."
sleep 5

# 测试1: 创建带有过滤条件的报表
echo "测试1: 创建带有过滤条件的报表"
FILTER_REPORT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/configs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "name": "高级功能测试报表",
    "description": "测试过滤条件和统计配置",
    "dataSourceUri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "filters": [
      {
        "field": "category",
        "operator": "eq",
        "value": "电子产品",
        "logic": "and"
      },
      {
        "field": "price",
        "operator": "gt",
        "value": "1000",
        "logic": "and"
      }
    ],
    "widgets": [
      {
        "id": "test-widget-1",
        "name": "bar",
        "label": "柱状图",
        "config": {
          "title": "产品价格统计",
          "xField": "name",
          "yField": "price",
          "enableStats": true,
          "aggregation": "sum",
          "groupBy": "category",
          "sortBy": "price",
          "sortOrder": "desc",
          "limit": 5,
          "showLegend": true,
          "showLabel": true,
          "animation": true,
          "theme": "default"
        }
      }
    ]
  }')

echo "过滤条件报表创建响应: $FILTER_REPORT_RESPONSE"

# 提取报表ID
FILTER_REPORT_ID=$(echo "$FILTER_REPORT_RESPONSE" | grep -o '"_id":"[^"]*"' | cut -d'"' -f4)
echo "过滤条件报表ID: $FILTER_REPORT_ID"

if [ -n "$FILTER_REPORT_ID" ]; then
  # 测试2: 获取创建的报表
  echo "测试2: 获取创建的报表"
  GET_REPORT_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/report/configs/$FILTER_REPORT_ID" \
    -H "Authorization: Bearer test-token")
  echo "获取报表响应: $GET_REPORT_RESPONSE"
  
  # 测试3: 测试图表数据生成（包含过滤条件）
  echo "测试3: 测试图表数据生成（包含过滤条件）"
  CHART_DATA_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
    -H "Content-Type: application/json" \
    -d "{
      \"uri\": \"mongodb://localhost:27017/mongo-reporter\",
      \"collection\": \"products\",
      \"filters\": [
        {
          \"field\": \"category\",
          \"operator\": \"eq\",
          \"value\": \"电子产品\",
          \"logic\": \"and\"
        },
        {
          \"field\": \"price\",
          \"operator\": \"gt\",
          \"value\": \"1000\",
          \"logic\": \"and\"
        }
      ],
      \"widget\": {
        \"name\": \"bar\",
        \"xField\": \"name\",
        \"yField\": \"price\",
        \"enableStats\": true,
        \"aggregation\": \"sum\",
        \"groupBy\": \"category\",
        \"sortBy\": \"price\",
        \"sortOrder\": \"desc\",
        \"limit\": 5
      }
    }")
  
  echo "图表数据生成响应: $CHART_DATA_RESPONSE"
  
  # 测试4: 测试统计功能
  echo "测试4: 测试统计功能"
  STATS_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
    -H "Content-Type: application/json" \
    -d '{
      "uri": "mongodb://localhost:27017/mongo-reporter",
      "collection": "products",
      "widget": {
        "name": "pie",
        "nameField": "category",
        "valueField": "price",
        "enableStats": true,
        "aggregation": "sum",
        "groupBy": "category",
        "sortBy": "price",
        "sortOrder": "desc",
        "limit": 10
      }
    }')
  
  echo "统计功能响应: $STATS_RESPONSE"
  
  # 清理测试数据
  echo "清理测试数据..."
  curl -s -X DELETE "http://localhost:8080/api/report/configs/$FILTER_REPORT_ID"
  echo "测试数据已清理"
else
  echo "错误: 无法获取报表ID"
fi

echo "=== 测试完成 ===" 