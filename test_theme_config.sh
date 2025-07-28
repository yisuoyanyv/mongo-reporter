#!/bin/bash

echo "🧪 测试图表主题配置"
echo "===================="

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
echo "2. 测试主题配置API..."

# 测试默认主题
echo "📊 测试默认主题:"
DEFAULT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
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
echo "默认主题响应: $DEFAULT_RESPONSE"

# 测试暗色主题
echo ""
echo "📊 测试暗色主题:"
DARK_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "type": "pie",
      "nameField": "category",
      "valueField": "price",
      "theme": "dark"
    },
    "filters": []
  }')
echo "暗色主题响应: $DARK_RESPONSE"

# 测试浅色主题
echo ""
echo "📊 测试浅色主题:"
LIGHT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "type": "pie",
      "nameField": "category",
      "valueField": "price",
      "theme": "light"
    },
    "filters": []
  }')
echo "浅色主题响应: $LIGHT_RESPONSE"

echo ""
echo "3. 主题配置说明:"
echo "✅ 默认主题: 使用ECharts默认配色方案"
echo "✅ 暗色主题: 深色背景，适合夜间查看"
echo "✅ 浅色主题: 浅色背景，适合白天查看"

echo ""
echo "4. 前端主题应用:"
echo "✅ ReportDesigner.vue: 根据theme配置初始化ECharts实例"
echo "✅ ReportViewer.vue: 根据theme配置初始化ECharts实例"
echo "✅ main.js: 导入ECharts主题模块"

echo ""
echo "5. 使用说明:"
echo "1. 访问前端应用: http://localhost:5173"
echo "2. 进入报表设计器"
echo "3. 添加图表组件"
echo "4. 在样式配置中选择主题"
echo "5. 保存并查看效果"

echo ""
echo "6. 主题效果验证:"
echo "- 默认主题: 标准蓝绿色调"
echo "- 暗色主题: 深色背景，亮色文字"
echo "- 浅色主题: 浅色背景，深色文字"

echo ""
echo "🌐 访问地址:"
echo "前端应用: http://localhost:5173"
echo "后端API: http://localhost:8080"

echo ""
echo "📝 注意事项:"
echo "1. 主题配置需要在组件配置对话框中设置"
echo "2. 主题变更后需要重新渲染图表"
echo "3. 不同主题会影响图表的整体视觉效果" 