#!/bin/bash

echo "=== 测试过滤条件修复 ==="

# 检查服务状态
echo "检查后端服务..."
if curl -s http://localhost:8080/api/report/configs > /dev/null; then
    echo "✅ 后端服务运行正常"
else
    echo "❌ 后端服务未运行"
    exit 1
fi

echo "检查前端服务..."
if curl -s http://localhost:5173/ > /dev/null; then
    echo "✅ 前端服务运行正常"
else
    echo "❌ 前端服务未运行"
    exit 1
fi

echo ""
echo "=== 测试过滤条件API ==="
echo "测试无过滤条件的饼图数据..."

curl -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "filters": [],
    "widget": {
      "name": "pie",
      "nameField": "category",
      "valueField": "price"
    }
  }' | jq '.'

echo ""
echo "=== 测试带过滤条件的饼图数据 ==="
echo "测试过滤category=电子产品的饼图数据..."

curl -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "filters": [
      {
        "field": "category",
        "operator": "eq",
        "value": "电子产品",
        "logic": "and"
      }
    ],
    "widget": {
      "name": "pie",
      "nameField": "category",
      "valueField": "price"
    }
  }' | jq '.'

echo ""
echo "=== 测试多个过滤条件 ==="
echo "测试过滤category=电子产品且price>1000的饼图数据..."

curl -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
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
        "value": 1000,
        "logic": "and"
      }
    ],
    "widget": {
      "name": "pie",
      "nameField": "category",
      "valueField": "price"
    }
  }' | jq '.'

echo ""
echo "=== 测试表格数据过滤 ==="
echo "测试过滤category=电子产品的表格数据..."

curl -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "filters": [
      {
        "field": "category",
        "operator": "eq",
        "value": "电子产品",
        "logic": "and"
      }
    ],
    "widget": {
      "name": "table",
      "limit": 5
    }
  }' | jq '.data | length'

echo ""
echo "=== 修复说明 ==="
echo "修复内容："
echo "1. 过滤条件变化时触发图表重新渲染"
echo "2. 添加过滤条件时触发图表重新渲染"
echo "3. 删除过滤条件时触发图表重新渲染"
echo "4. 过滤条件值变化时触发图表重新渲染"
echo "5. 过滤条件操作符变化时触发图表重新渲染"
echo "6. 过滤条件逻辑变化时触发图表重新渲染"
echo "7. 加载报表时应用保存的过滤条件"
echo "8. 保存报表时包含过滤条件"

echo ""
echo "=== 前端测试步骤 ==="
echo "1. 访问 http://localhost:5173"
echo "2. 进入报表设计器"
echo "3. 选择数据源和集合"
echo "4. 添加过滤条件："
echo "   - 字段：category"
echo "   - 操作符：等于"
echo "   - 值：电子产品"
echo "5. 添加饼图组件"
echo "6. 配置字段：nameField=category, valueField=price"
echo "7. 检查图表是否只显示电子产品数据"
echo "8. 修改过滤条件值，检查图表是否实时更新"
echo "9. 添加第二个过滤条件："
echo "   - 字段：price"
echo "   - 操作符：大于"
echo "   - 值：1000"
echo "10. 检查图表是否只显示价格大于1000的电子产品"
echo "11. 保存报表"
echo "12. 重新加载报表，检查过滤条件是否保存"

echo ""
echo "=== 预期结果 ==="
echo "1. 过滤条件变化时图表应该实时更新"
echo "2. 过滤条件应该正确应用到图表数据"
echo "3. 保存报表时过滤条件应该被保存"
echo "4. 加载报表时过滤条件应该被恢复"
echo "5. 多个过滤条件应该正确组合"

echo ""
echo "=== 如果仍有问题 ==="
echo "1. 检查浏览器控制台是否有错误"
echo "2. 确认后端服务已重启并应用了最新代码"
echo "3. 验证MongoDB连接正常"
echo "4. 查看网络请求中的过滤条件是否正确传递"
echo "5. 检查过滤条件的数据格式是否正确"

echo ""
echo "=== 修复完成 ===" 