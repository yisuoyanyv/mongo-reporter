// 创建管理员用户
use admin
db.createUser({
  user: "admin",
  pwd: "admin123456",
  roles: [
    { role: "userAdminAnyDatabase", db: "admin" },
    { role: "readWriteAnyDatabase", db: "admin" }
  ]
})

// 创建应用用户
use mongo-reporter
db.createUser({
  user: "mongo_reporter_user",
  pwd: "mongo_reporter_pass123",
  roles: [
    { role: "readWrite", db: "mongo-reporter" }
  ]
})

// 创建测试用户
use test_db
db.createUser({
  user: "test_user",
  pwd: "test_pass123",
  roles: [
    { role: "readWrite", db: "test_db" }
  ]
})

// 插入测试数据
db.products.insertMany([
  { name: "笔记本电脑", price: 5999, category: "电子产品", stock: 50 },
  { name: "手机", price: 3999, category: "电子产品", stock: 100 },
  { name: "平板电脑", price: 2999, category: "电子产品", stock: 30 }
])

db.orders.insertMany([
  { customer: "张三", amount: 3000, date: new Date("2024-01-15"), status: "已完成" },
  { customer: "李四", amount: 1500, date: new Date("2024-01-16"), status: "处理中" }
])

print("用户创建完成") 