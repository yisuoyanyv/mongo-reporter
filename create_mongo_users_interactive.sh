#!/bin/bash

echo "🔧 MongoDB用户创建向导"
echo "======================"

# 检查MongoDB是否运行
if ! pgrep -x "mongod" > /dev/null; then
    echo "❌ MongoDB未运行，请先启动MongoDB"
    exit 1
fi

echo "✅ MongoDB正在运行"
echo

echo "📋 即将创建以下用户："
echo "======================"
echo "1. 管理员用户: admin / admin123456"
echo "2. 应用用户: mongo_reporter_user / mongo_reporter_pass123"
echo "3. 测试用户: test_user / test_pass123"
echo

read -p "是否继续创建用户？(y/n): " confirm

if [[ $confirm != "y" && $confirm != "Y" ]]; then
    echo "❌ 用户创建已取消"
    exit 0
fi

echo
echo "📝 开始创建用户..."
echo

# 创建管理员用户
echo "1️⃣ 创建管理员用户..."
mongo --eval "
use admin;
try {
  db.createUser({
    user: 'admin',
    pwd: 'admin123456',
    roles: [
      { role: 'userAdminAnyDatabase', db: 'admin' },
      { role: 'readWriteAnyDatabase', db: 'admin' }
    ]
  });
  print('✅ 管理员用户创建成功');
} catch (error) {
  if (error.code === 51003) {
    print('⚠️  管理员用户已存在');
  } else {
    print('❌ 管理员用户创建失败: ' + error.message);
  }
}
"

# 创建应用用户
echo
echo "2️⃣ 创建应用用户..."
mongo --eval "
use mongo-reporter;
try {
  db.createUser({
    user: 'mongo_reporter_user',
    pwd: 'mongo_reporter_pass123',
    roles: [
      { role: 'readWrite', db: 'mongo-reporter' }
    ]
  });
  print('✅ 应用用户创建成功');
} catch (error) {
  if (error.code === 51003) {
    print('⚠️  应用用户已存在');
  } else {
    print('❌ 应用用户创建失败: ' + error.message);
  }
}
"

# 创建测试用户
echo
echo "3️⃣ 创建测试用户..."
mongo --eval "
use test_db;
try {
  db.createUser({
    user: 'test_user',
    pwd: 'test_pass123',
    roles: [
      { role: 'readWrite', db: 'test_db' }
    ]
  });
  print('✅ 测试用户创建成功');
} catch (error) {
  if (error.code === 51003) {
    print('⚠️  测试用户已存在');
  } else {
    print('❌ 测试用户创建失败: ' + error.message);
  }
}
"

# 插入测试数据
echo
echo "4️⃣ 插入测试数据..."
mongo --eval "
use test_db;
try {
  db.products.insertMany([
    { name: '笔记本电脑', price: 5999, category: '电子产品', stock: 50 },
    { name: '手机', price: 3999, category: '电子产品', stock: 100 },
    { name: '平板电脑', price: 2999, category: '电子产品', stock: 30 }
  ]);
  print('✅ 产品数据插入成功');
} catch (error) {
  print('⚠️  产品数据可能已存在: ' + error.message);
}

try {
  db.orders.insertMany([
    { customer: '张三', amount: 3000, date: new Date('2024-01-15'), status: '已完成' },
    { customer: '李四', amount: 1500, date: new Date('2024-01-16'), status: '处理中' }
  ]);
  print('✅ 订单数据插入成功');
} catch (error) {
  print('⚠️  订单数据可能已存在: ' + error.message);
}
"

echo
echo "🎉 用户创建完成！"
echo
echo "📋 用户信息汇总:"
echo "=================="
echo "管理员用户: admin / admin123456"
echo "应用用户: mongo_reporter_user / mongo_reporter_pass123"
echo "测试用户: test_user / test_pass123"
echo
echo "🔗 连接字符串:"
echo "应用数据库: mongodb://mongo_reporter_user:mongo_reporter_pass123@localhost:27017/mongo-reporter?authSource=mongo-reporter"
echo "测试数据库: mongodb://test_user:test_pass123@localhost:27017/test_db?authSource=test_db"
echo
echo "🚀 现在可以启动应用测试认证功能了！" 