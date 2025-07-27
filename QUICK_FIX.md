# 403错误快速解决方案

## 🚨 问题描述
前端访问 `/api/report/configs` 时返回 `403 Forbidden` 错误。

## ⚡ 快速解决方案

### 方法1: 使用调试页面（推荐）
1. 打开浏览器访问: **http://localhost:5173/debug.html**
2. 点击"测试登录"按钮
3. 点击"测试报表API"按钮验证
4. 如果成功，返回主应用页面

### 方法2: 手动登录
1. 访问: **http://localhost:5173/login**
2. 输入用户名: `admin`，密码: `admin123`
3. 点击登录按钮

### 方法3: 浏览器控制台操作
1. 按F12打开开发者工具
2. 在Console中执行以下代码：
```javascript
// 清除现有数据
localStorage.clear();

// 登录获取token
fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'admin', password: 'admin123' })
})
.then(res => res.json())
.then(data => {
  if (data.token) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('username', data.username);
    console.log('登录成功！');
    location.reload();
  }
});
```

## 🔧 验证步骤

### 1. 检查后端状态
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. 检查前端状态
```bash
curl -I http://localhost:5173
```

### 3. 检查MongoDB状态
```bash
brew services list | grep mongodb
```

## 📋 常见问题

### Q: 为什么会出现403错误？
A: 前端没有有效的JWT token，无法访问需要认证的API。

### Q: 自动登录为什么没有工作？
A: 可能是网络问题或后端服务未完全启动。

### Q: 如何确认登录成功？
A: 检查localStorage中是否有token，或使用调试页面验证。

## 🎯 预期结果

解决后应该看到：
- ✅ 登录成功消息
- ✅ 可以正常访问报表页面
- ✅ API请求不再返回403错误

## 📞 如果问题仍然存在

1. 检查后端日志: `tail -f backend.log`
2. 检查前端控制台错误
3. 确认MongoDB服务正在运行
4. 重启应用: `./stop.sh && ./start.sh`

---

**快速解决403错误，让MongoReporter正常运行！** 🚀 