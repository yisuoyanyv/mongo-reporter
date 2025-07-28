#!/bin/bash

echo "🧪 测试MongoReporter增强功能"
echo ""

# 检查服务状态
echo "📋 检查服务状态..."
if curl -s http://localhost:8080/api/datasource > /dev/null; then
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
echo "🔍 测试分类和标签功能..."

# 测试获取分类
echo "📊 获取所有分类..."
curl -s -H "Authorization: Bearer test-token" http://localhost:8080/api/report/categories | jq '.'

# 测试获取标签
echo "🏷️  获取所有标签..."
curl -s -H "Authorization: Bearer test-token" http://localhost:8080/api/report/tags | jq '.'

# 测试搜索功能
echo "🔍 测试搜索功能..."
curl -s -H "Authorization: Bearer test-token" "http://localhost:8080/api/report/configs/search?keyword=测试" | jq '.'

echo ""
echo "📈 测试新图表类型..."

# 测试热力图数据
echo "🔥 测试热力图数据..."
curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "heatmap",
      "xField": "category",
      "yField": "name",
      "valueField": "price"
    }
  }' | jq '.'

# 测试桑基图数据
echo "🌊 测试桑基图数据..."
curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "orders",
    "widget": {
      "name": "sankey",
      "sourceField": "customer",
      "targetField": "status",
      "valueField": "amount"
    }
  }' | jq '.'

# 测试仪表板数据
echo "📊 测试仪表板数据..."
curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "dashboard",
      "metricFields": "price,stock"
    }
  }' | jq '.'

echo ""
echo "🎨 测试面积图数据..."
curl -s -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "area",
      "xField": "category",
      "yField": "price",
      "seriesField": "name"
    }
  }' | jq '.'

echo ""
echo "📋 功能测试完成！"
echo ""
echo "🎯 新增功能包括："
echo "  ✅ 报表分类和标签管理"
echo "  ✅ 高级搜索和筛选"
echo "  ✅ 热力图支持"
echo "  ✅ 桑基图支持"
echo "  ✅ 仪表板支持"
echo "  ✅ 面积图支持"
echo "  ✅ 树图支持"
echo "  ✅ 地图支持"
echo "  ✅ K线图支持"
echo ""
echo "🌐 访问地址："
echo "  前端: http://localhost:5173"
echo "  后端: http://localhost:8080"
echo ""
echo "📝 使用说明："
echo "1. 在报表设计器中可以看到新的图表类型"
echo "2. 在报表列表中可以使用分类和标签筛选"
echo "3. 支持关键词搜索和状态筛选"
echo "4. 新图表类型需要配置相应的字段映射" 