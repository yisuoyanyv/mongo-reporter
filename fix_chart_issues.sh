#!/bin/bash

echo "=== 修复图表显示问题 ==="

# 等待服务启动
echo "等待服务启动..."
sleep 15

# 检查服务状态
echo "检查后端服务..."
if curl -s http://localhost:8080/api/report/configs > /dev/null; then
    echo "✅ 后端服务运行正常"
else
    echo "❌ 后端服务未运行，请手动启动"
    exit 1
fi

echo "检查前端服务..."
if curl -s http://localhost:5173/ > /dev/null; then
    echo "✅ 前端服务运行正常"
else
    echo "❌ 前端服务未运行，请手动启动"
    exit 1
fi

echo ""
echo "=== 修复内容说明 ==="
echo "1. ✅ 修复了饼图标签显示格式"
echo "2. ✅ 增强了表格数据加载的调试信息"
echo "3. ✅ 改进了错误处理和用户提示"
echo "4. ✅ 优化了图表配置逻辑"

echo ""
echo "=== 测试修复效果 ==="

# 测试折线图
echo "测试折线图..."
curl -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "orders",
    "widget": {
      "name": "line",
      "xField": "date",
      "yField": "amount",
      "seriesField": "product"
    },
    "filters": []
  }' | jq '.success'

# 测试饼图
echo "测试饼图..."
curl -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "orders",
    "widget": {
      "name": "pie",
      "nameField": "product",
      "valueField": "amount"
    },
    "filters": []
  }' | jq '.success'

# 测试表格
echo "测试表格..."
curl -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "orders",
    "widget": {
      "name": "table",
      "displayFields": ["date", "product", "amount"],
      "limit": 10
    },
    "filters": []
  }' | jq '.success'

echo ""
echo "=== 使用说明 ==="
echo "1. 访问 http://localhost:5173"
echo "2. 进入报表设计器"
echo "3. 添加图表组件并配置字段"
echo "4. 检查浏览器控制台的调试信息"
echo "5. 如果仍有问题，请查看具体的错误信息"

echo ""
echo "=== 常见问题解决 ==="
echo "1. 折线图/饼图不显示："
echo "   - 确保选择了正确的字段"
echo "   - 检查数据源连接"
echo "   - 查看浏览器控制台错误"
echo ""
echo "2. 表格提示不支持："
echo "   - 检查后端服务是否正常运行"
echo "   - 查看API响应是否正确"
echo "   - 确认表格配置是否完整"
echo ""
echo "3. 调试方法："
echo "   - 打开浏览器开发者工具"
echo "   - 查看Network标签页的API请求"
echo "   - 查看Console标签页的日志信息"
echo "   - 检查Vue组件的数据绑定状态"

echo ""
echo "=== 修复完成 ===" 