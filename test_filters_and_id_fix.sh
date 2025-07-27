#!/bin/bash

echo "=== 测试过滤条件和ID修复 ==="

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
echo "=== 测试ID字段显示 ==="
echo "测试products集合的数据（ID应该显示为字符串而不是Object）..."

curl -X GET "http://localhost:8080/api/report/data?uri=mongodb://localhost:27017/mongo-reporter&collection=products&limit=3" | jq '.[0]'

echo ""
echo "=== 测试过滤条件 ==="
echo "测试带过滤条件的饼图数据..."

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
echo "=== 测试表格数据ID显示 ==="
echo "测试表格数据的ID字段显示..."

curl -X POST http://localhost:8080/api/report/chart-data \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "mongodb://localhost:27017/mongo-reporter",
    "collection": "products",
    "widget": {
      "name": "table",
      "limit": 3
    },
    "filters": []
  }' | jq '.data[0]'

echo ""
echo "=== 修复说明 ==="
echo "修复内容："
echo "1. 过滤条件保存：确保reportForm.value.filters在保存时被正确包含"
echo "2. ID字段显示：将MongoDB的ObjectId转换为字符串显示"
echo "3. 数据预览：修复预览数据中的ID显示问题"
echo "4. 表格数据：修复表格组件中的ID显示问题"

echo ""
echo "=== 测试步骤 ==="
echo "1. 访问 http://localhost:5173"
echo "2. 进入报表设计器"
echo "3. 添加过滤条件："
echo "   - 字段：category"
echo "   - 操作符：等于"
echo "   - 值：电子产品"
echo "4. 添加饼图组件"
echo "5. 配置字段：nameField=category, valueField=price"
echo "6. 保存报表"
echo "7. 重新加载报表，检查过滤条件是否保存"
echo "8. 检查表格数据中的ID是否显示为字符串"

echo ""
echo "=== 预期结果 ==="
echo "1. 过滤条件应该被正确保存和加载"
echo "2. ID字段应该显示为字符串格式（如：507f1f77bcf86cd799439011）"
echo "3. 数据预览中的ID也应该显示为字符串"
echo "4. 表格组件中的ID应该正确显示"

echo ""
echo "=== 如果仍有问题 ==="
echo "1. 检查浏览器控制台是否有错误"
echo "2. 确认后端服务已重启并应用了最新代码"
echo "3. 验证MongoDB连接正常"
echo "4. 查看网络请求中的过滤条件是否正确传递"

echo ""
echo "=== 修复完成 ===" 