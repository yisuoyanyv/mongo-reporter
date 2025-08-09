# MongoDB用户设置指南

## 概述
本指南将帮助您为MongoReporter应用设置MongoDB用户认证。

## 用户信息

### 1. 管理员用户
- **用户名**: `admin`
- **密码**: `admin123456`
- **数据库**: `admin`
- **权限**: 全局管理权限

### 2. 应用用户
- **用户名**: `mongo_reporter_user`
- **密码**: `mongo_reporter_pass123`
- **数据库**: `mongo-reporter`
- **权限**: 读写权限

### 3. 测试用户
- **用户名**: `test_user`
- **密码**: `test_pass123`
- **数据库**: `test_db`
- **权限**: 读写权限

## 手动创建用户步骤

### 步骤1: 启动MongoDB Shell
```bash
mongo
```

### 步骤2: 创建管理员用户
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

### 步骤3: 创建应用用户
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

### 步骤4: 创建测试用户
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

### 步骤5: 插入测试数据
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

## 连接字符串

### 应用数据库连接
```
mongodb://mongo_reporter_user:mongo_reporter_pass123@localhost:27017/mongo-reporter?authSource=mongo-reporter
```

### 测试数据库连接
```
mongodb://test_user:test_pass123@localhost:27017/test_db?authSource=test_db
```

## 应用配置

应用已配置为使用认证连接。配置文件位置：`backend/src/main/resources/application.properties`

```properties
spring.data.mongodb.uri=mongodb://mongo_reporter_user:mongo_reporter_pass123@localhost:27017/mongo-reporter?authSource=mongo-reporter
spring.data.mongodb.authentication-database=admin
spring.data.mongodb.username=mongo_reporter_user
spring.data.mongodb.password=mongo_reporter_pass123
```

## 启用MongoDB认证

要启用MongoDB认证，需要重启MongoDB服务：

1. 停止MongoDB服务
2. 启动MongoDB服务时添加 `--auth` 参数
3. 或者修改MongoDB配置文件添加 `security.authorization: enabled`

## 测试连接

创建用户后，可以使用以下命令测试连接：

```bash
# 测试应用用户连接
mongo mongodb://mongo_reporter_user:mongo_reporter_pass123@localhost:27017/mongo-reporter?authSource=mongo-reporter

# 测试测试用户连接
mongo mongodb://test_user:test_pass123@localhost:27017/test_db?authSource=test_db
```

## 注意事项

1. 在生产环境中，请使用更强的密码
2. 定期更换密码
3. 限制用户权限到最小必要范围
4. 启用MongoDB认证以提高安全性 