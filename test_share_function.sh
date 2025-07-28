#!/bin/bash

echo "🧪 测试报表分享功能"
echo "=================="

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
echo "2. 获取报表列表..."
REPORTS_RESPONSE=$(curl -s -X GET http://localhost:8080/api/report/configs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token")

if echo "$REPORTS_RESPONSE" | grep -q "id"; then
    echo "✅ 成功获取报表列表"
    REPORT_ID=$(echo "$REPORTS_RESPONSE" | jq -r '.[0].id' 2>/dev/null)
    if [ "$REPORT_ID" != "null" ] && [ "$REPORT_ID" != "" ]; then
        echo "📋 使用报表ID: $REPORT_ID"
    else
        echo "❌ 未找到可用的报表"
        exit 1
    fi
else
    echo "❌ 获取报表列表失败"
    echo "响应: $REPORTS_RESPONSE"
    exit 1
fi

echo ""
echo "3. 测试分享链接访问..."
SHARE_URL="http://localhost:5173/share/$REPORT_ID"
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
echo "4. 测试分享API..."
SHARE_API_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/report/configs/$REPORT_ID" \
  -H "Content-Type: application/json")

if echo "$SHARE_API_RESPONSE" | grep -q "id"; then
    echo "✅ 分享API响应正常"
    REPORT_NAME=$(echo "$SHARE_API_RESPONSE" | jq -r '.name' 2>/dev/null)
    echo "📋 报表名称: $REPORT_NAME"
else
    echo "❌ 分享API响应异常"
    echo "响应: $SHARE_API_RESPONSE"
fi

echo ""
echo "5. 测试图表数据API..."
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
else
    echo "❌ 图表数据API响应异常"
    echo "响应: $CHART_RESPONSE"
fi

echo ""
echo "🎉 分享功能测试完成！"
echo ""
echo "📋 使用说明："
echo "1. 在报表查看页面点击'分享'按钮"
echo "2. 选择'复制链接'获取分享链接"
echo "3. 分享链接格式: http://localhost:5173/share/{报表ID}"
echo "4. 任何人都可以通过分享链接查看报表（如果报表已公开分享）"
echo ""
echo "🔧 注意事项："
echo "- 只有设置为公开分享的报表才能通过链接访问"
echo "- 分享页面不需要登录即可访问"
echo "- 分享页面包含完整的报表功能和导出功能" 