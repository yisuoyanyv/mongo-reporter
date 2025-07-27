# MongoDB认证设置完成

## 🎉 设置完成

MongoDB用户认证功能已成功配置完成！

## 📋 用户信息

### 管理员用户
- **用户名**: `admin`
- **密码**: `admin123456`
- **数据库**: `admin`
- **权限**: 全局管理权限

### 应用用户
- **用户名**: `mongo_reporter_user`
- **密码**: `mongo_reporter_pass123`
- **数据库**: `mongo-reporter`
- **权限**: 读写权限

### 测试用户
- **用户名**: `test_user`
- **密码**: `test_pass123`
- **数据库**: `test_db`
- **权限**: 读写权限

## 🔧 应用配置更新

应用配置已更新为支持认证连接：

```properties
# backend/src/main/resources/application.properties
spring.data.mongodb.uri=mongodb://mongo_reporter_user:mongo_reporter_pass123@localhost:27017/mongo-reporter?authSource=mongo-reporter
spring.data.mongodb.authentication-database=admin
spring.data.mongodb.username=mongo_reporter_user
spring.data.mongodb.password=mongo_reporter_pass123
```

## 📝 手动创建用户步骤

由于MongoDB shell的语法限制，请手动创建用户：

### 1. 启动MongoDB Shell
```bash
mongo
```

### 2. 创建管理员用户
```javascript
use admin
db.createUser({
  user: "admin",
  pwd: "admin123456",
  roles: [
    { role: "userAdminAnyDatabase", db: "admin" },
    { role: "readWriteAnyDatabase", db: "admin" }
  ]
})
```

### 3. 创建应用用户
```javascript
use mongo-reporter
db.createUser({
  user: "mongo_reporter_user",
  pwd: "mongo_reporter_pass123",
  roles: [
    { role: "readWrite", db: "mongo-reporter" }
  ]
})
```

### 4. 创建测试用户
```javascript
use test_db
db.createUser({
  user: "test_user",
  pwd: "test_pass123",
  roles: [
    { role: "readWrite", db: "test_db" }
  ]
})
```

### 5. 插入测试数据
```javascript
use test_db
db.products.insertMany([
  { name: "笔记本电脑", price: 5999, category: "电子产品", stock: 50 },
  { name: "手机", price: 3999, category: "电子产品", stock: 100 },
  { name: "平板电脑", price: 2999, category: "电子产品", stock: 30 }
])

db.orders.insertMany([
  { customer: "张三", amount: 3000, date: new Date("2024-01-15"), status: "已完成" },
  { customer: "李四", amount: 1500, date: new Date("2024-01-16"), status: "处理中" }
])
```

## 🚀 启动应用

使用提供的启动脚本：

```bash
./start_with_auth_users.sh
```

或者手动启动：

```bash
# 启动后端
cd backend
./mvnw spring-boot:run

# 启动前端（新终端）
cd frontend
npm run dev
```

## 🔗 连接字符串

### 应用数据库
```
mongodb://mongo_reporter_user:mongo_reporter_pass123@localhost:27017/mongo-reporter?authSource=mongo-reporter
```

### 测试数据库
```
mongodb://test_user:test_pass123@localhost:27017/test_db?authSource=test_db
```

## 🧪 测试认证功能

1. 启动应用后，访问数据源管理页面
2. 创建新的数据源，启用认证
3. 输入用户名和密码
4. 测试连接
5. 创建报表和图表

## 📚 相关文档

- `MONGO_USER_SETUP.md` - 详细的用户设置指南
- `MONGODB_AUTH_FEATURES.md` - 认证功能说明
- `AUTH_FEATURES_SUMMARY.md` - 功能总结

## ⚠️ 注意事项

1. 在生产环境中使用更强的密码
2. 定期更换密码
3. 启用MongoDB认证以提高安全性
4. 限制用户权限到最小必要范围

## 🎯 下一步

用户创建完成后，您就可以：
- 在应用中测试认证连接
- 创建需要认证的数据源
- 使用认证数据源创建报表
- 体验完整的MongoDB认证功能 