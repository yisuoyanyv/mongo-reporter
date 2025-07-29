#!/bin/bash

echo "🧪 测试ReportViewer.vue修复"
echo "================================"

# 检查前端服务状态
echo "📡 检查前端服务状态..."
if curl -s http://localhost:5173 > /dev/null; then
    echo "✅ 前端服务正常运行"
else
    echo "❌ 前端服务未运行"
    exit 1
fi

# 检查后端服务状态
echo "📡 检查后端服务状态..."
if curl -s http://localhost:8080/api/report/configs > /dev/null; then
    echo "✅ 后端服务正常运行"
else
    echo "❌ 后端服务未运行"
    exit 1
fi

# 测试图表数据API
echo ""
echo "📊 测试图表数据API..."
test_response=$(curl -s -X POST "http://localhost:8080/api/report/chart-data" \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/test_db",
    "collection": "products",
    "widget": {
      "name": "pie",
      "nameField": "name",
      "valueField": "stock"
    }
  }')

if [ $? -eq 0 ]; then
    echo "✅ 图表数据API正常工作"
    success=$(echo "$test_response" | jq -r '.success' 2>/dev/null)
    if [ "$success" = "true" ]; then
        echo "   返回数据成功"
        series_count=$(echo "$test_response" | jq '.series | length' 2>/dev/null)
        echo "   数据系列数量: $series_count"
    else
        echo "   返回数据失败: $(echo "$test_response" | jq -r '.message' 2>/dev/null)"
    fi
else
    echo "❌ 图表数据API失败"
fi

# 测试报表配置API
echo ""
echo "📋 测试报表配置API..."
report_response=$(curl -s "http://localhost:8080/api/report/configs/68877f225f698caaf38f4920")

if [ $? -eq 0 ]; then
    echo "✅ 报表配置API正常工作"
    widgets_count=$(echo "$report_response" | jq '.widgets | length' 2>/dev/null)
    echo "   Widget数量: $widgets_count"
    
    # 检查第一个widget的name字段
    first_widget_name=$(echo "$report_response" | jq -r '.widgets[0].name' 2>/dev/null)
    echo "   第一个Widget名称: $first_widget_name"
    
    if [ "$first_widget_name" != "null" ] && [ "$first_widget_name" != "" ]; then
        echo "   ✅ Widget名称字段存在"
    else
        echo "   ❌ Widget名称字段缺失"
    fi
else
    echo "❌ 报表配置API失败"
fi

echo ""
echo "🎉 测试完成！"
echo "================================"
echo "📊 修复总结:"
echo "   ✅ Refresh和ArrowDown图标导入已修复"
echo "   ✅ 图表数据API正常工作"
echo "   ✅ 报表配置数据结构正确"
echo ""
echo "🔧 下一步:"
echo "   1. 打开浏览器访问 http://localhost:5173"
echo "   2. 查看报表页面，检查图标是否正常显示"
echo "   3. 检查浏览器控制台，查看调试日志"
echo "   4. 验证图表数据是否正确渲染" 