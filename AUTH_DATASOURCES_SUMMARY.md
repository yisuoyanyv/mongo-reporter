# MongoDB认证数据源总结

## 🎉 认证数据源已成功添加！

### 📋 可用的数据源列表

#### 🔐 认证数据源（推荐使用）

1. **测试数据库(认证)**
   - URI: `mongodb://localhost:27017/test_db`
   - 用户名: `test_user`
   - 密码: `test_pass123`
   - 认证数据库: `test_db`
   - 状态: ✅ 可用（包含测试数据）

2. **应用数据库(认证)**
   - URI: `mongodb://localhost:27017/mongo-reporter`
   - 用户名: `mongo_reporter_user`
   - 密码: `mongo_reporter_pass123`
   - 认证数据库: `mongo-reporter`
   - 状态: ✅ 可用

3. **销售数据(认证)**
   - URI: `mongodb://localhost:27017/sales`
   - 用户名: `admin`
   - 密码: `admin123456`
   - 认证数据库: `admin`
   - 状态: ✅ 可用

4. **产品数据(认证)**
   - URI: `mongodb://localhost:27017/products`
   - 用户名: `admin`
   - 密码: `admin123456`
   - 认证数据库: `admin`
   - 状态: ✅ 可用

5. **用户数据(认证)**
   - URI: `mongodb://localhost:27017/users`
   - 用户名: `admin`
   - 密码: `admin123456`
   - 认证数据库: `admin`
   - 状态: ✅ 可用

#### 🔓 无认证数据源（原有）

1. **销售数据** - `mongodb://localhost:27017/mongo-reporter`
2. **产品数据** - `mongodb://localhost:27017/mongo-reporter`
3. **用户数据** - `mongodb://localhost:27017/mongo-reporter`
4. **订单数据** - `mongodb://localhost:27017/mongo-reporter`

### 🌐 访问应用

- **前端地址**: http://localhost:5173
- **后端API**: http://localhost:8080

### 📝 使用步骤

1. **访问数据源管理页面**
   - 打开 http://localhost:5173
   - 点击"数据源管理"

2. **选择认证数据源**
   - 在数据源列表中选择任意一个带"(认证)"标识的数据源
   - 点击"测试连接"验证连接是否正常

3. **创建报表**
   - 进入"报表设计器"
   - 选择认证数据源
   - 选择集合和字段
   - 配置图表类型和样式

### 🧪 测试数据

**测试数据库** 包含以下测试数据：

#### 产品集合 (products)
```json
[
  { "name": "笔记本电脑", "price": 5999, "category": "电子产品", "stock": 50 },
  { "name": "手机", "price": 3999, "category": "电子产品", "stock": 100 },
  { "name": "平板电脑", "price": 2999, "category": "电子产品", "stock": 30 }
]
```

#### 订单集合 (orders)
```json
[
  { "customer": "张三", "amount": 3000, "date": "2024-01-15", "status": "已完成" },
  { "customer": "李四", "amount": 1500, "date": "2024-01-16", "status": "处理中" }
]
```

### 🔧 技术细节

- **MongoDB版本**: 4.2.11
- **认证方式**: SCRAM-SHA-1
- **连接池**: 默认配置
- **超时设置**: 10秒连接超时

### 🚀 下一步

现在您可以在页面上直接使用这些认证数据源来：
- 测试连接功能
- 创建各种类型的图表
- 生成数据报表
- 验证认证功能是否正常工作

所有数据源都已经配置完成，可以直接在MongoReporter应用中使用！ 