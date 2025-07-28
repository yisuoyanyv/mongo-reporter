#!/bin/bash

echo "🧪 测试分享页面图表渲染功能"
echo "=========================="

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
echo "2. 获取公开分享的报表..."
REPORTS_RESPONSE=$(curl -s -X GET http://localhost:8080/api/report/configs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token")

if echo "$REPORTS_RESPONSE" | grep -q "id"; then
    echo "✅ 成功获取报表列表"
    # 查找公开分享的报表
    PUBLIC_REPORT_ID=$(echo "$REPORTS_RESPONSE" | jq -r '.[] | select(.publicShare == true) | .id' 2>/dev/null | head -1)
    
    if [ "$PUBLIC_REPORT_ID" != "null" ] && [ "$PUBLIC_REPORT_ID" != "" ]; then
        echo "📋 找到公开分享的报表ID: $PUBLIC_REPORT_ID"
    else
        echo "❌ 未找到公开分享的报表，创建一个测试报表..."
        # 创建一个公开分享的测试报表
        TEST_REPORT_DATA='{
          "name": "测试分享报表",
          "description": "用于测试分享功能的报表",
          "dataSourceUri": "mongodb://localhost:27017/mongo-reporter",
          "collection": "products",
          "publicShare": true,
          "widgets": [
            {
              "id": "test-chart-1",
              "name": "pie",
              "label": "饼图",
              "nameField": "category",
              "valueField": "price",
              "title": "产品分类统计"
            },
            {
              "id": "test-chart-2", 
              "name": "bar",
              "label": "柱状图",
              "xField": "category",
              "yField": "price",
              "title": "产品价格对比"
            }
          ]
        }'
        
        CREATE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/configs \
          -H "Content-Type: application/json" \
          -H "Authorization: Bearer test-token" \
          -d "$TEST_REPORT_DATA")
        
        if echo "$CREATE_RESPONSE" | grep -q "id"; then
            PUBLIC_REPORT_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id')
            echo "✅ 创建测试报表成功，ID: $PUBLIC_REPORT_ID"
        else
            echo "❌ 创建测试报表失败"
            exit 1
        fi
    fi
else
    echo "❌ 获取报表列表失败"
    echo "响应: $REPORTS_RESPONSE"
    exit 1
fi

echo ""
echo "3. 测试分享页面访问..."
SHARE_URL="http://localhost:5173/share/$PUBLIC_REPORT_ID"
echo "📋 分享链接: $SHARE_URL"

# 测试分享页面是否可以访问
SHARE_RESPONSE=$(curl -s -I "$SHARE_URL" | head -1)
if echo "$SHARE_RESPONSE" | grep -q "200"; then
    echo "✅ 分享页面可以访问"
else
    echo "❌ 分享页面访问失败"
    echo "响应: $SHARE_RESPONSE"
fi

echo ""
echo "4. 测试图表数据API..."
CHART_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "type": "pie",
      "nameField": "category",
      "valueField": "price",
      "theme": "default"
    },
    "filters": []
  }')

if echo "$CHART_RESPONSE" | grep -q "success"; then
    echo "✅ 图表数据API响应正常"
    echo "📊 数据示例:"
    echo "$CHART_RESPONSE" | jq '.data[0:3]' 2>/dev/null || echo "$CHART_RESPONSE"
else
    echo "❌ 图表数据API响应异常"
    echo "响应: $CHART_RESPONSE"
fi

echo ""
echo "5. 测试分享API..."
SHARE_API_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/report/configs/$PUBLIC_REPORT_ID" \
  -H "Content-Type: application/json")

if echo "$SHARE_API_RESPONSE" | grep -q "id"; then
    echo "✅ 分享API响应正常"
    REPORT_NAME=$(echo "$SHARE_API_RESPONSE" | jq -r '.name' 2>/dev/null)
    echo "📋 报表名称: $REPORT_NAME"
    
    # 检查widgets配置
    WIDGETS_COUNT=$(echo "$SHARE_API_RESPONSE" | jq '.widgets | length' 2>/dev/null)
    echo "📊 图表组件数量: $WIDGETS_COUNT"
    
    if [ "$WIDGETS_COUNT" -gt 0 ]; then
        echo "✅ 报表包含图表组件"
        echo "📋 图表组件详情:"
        echo "$SHARE_API_RESPONSE" | jq '.widgets[] | {id, name, label, title}' 2>/dev/null
    else
        echo "❌ 报表不包含图表组件"
    fi
else
    echo "❌ 分享API响应异常"
    echo "响应: $SHARE_API_RESPONSE"
fi

echo ""
echo "🎉 分享页面图表渲染测试完成！"
echo ""
echo "📋 手动测试步骤："
echo "1. 打开浏览器访问: $SHARE_URL"
echo "2. 打开浏览器开发者工具 (F12)"
echo "3. 查看控制台是否有图表渲染相关的日志"
echo "4. 检查网络面板中的API请求是否正常"
echo "5. 检查图表容器是否正确生成"
echo ""
echo "🔧 常见问题排查："
echo "- 如果图表不显示，检查控制台是否有错误信息"
echo "- 如果API返回403，检查报表是否设置为公开分享"
echo "- 如果图表容器未找到，检查widget.id是否正确"
echo "- 如果数据为空，检查数据源和集合是否正确" 