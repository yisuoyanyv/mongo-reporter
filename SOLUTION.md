# 403 Forbidden 错误解决方案

## 🎯 问题描述

前端访问 `/api/report/configs` 时返回 `403 Forbidden` 错误。

## 🔍 问题分析

### 根本原因
用户未登录或JWT token无效，导致前端无法访问需要认证的API接口。

### 技术细节
1. **前端**: 尝试访问需要认证的API但没有发送有效的JWT token
2. **后端**: Spring Security拦截了未认证的请求，返回403错误
3. **认证流程**: 用户需要先登录获取JWT token，然后在后续请求中携带token

## ✅ 解决方案

### 方案1: 自动登录（已实现）
- **位置**: `frontend/src/App.vue`
- **功能**: 应用启动时自动使用默认账号登录
- **优点**: 用户体验好，无需手动输入账号密码
- **缺点**: 安全性较低（仅适用于开发环境）

### 方案2: 手动登录
- **步骤**: 
  1. 访问 http://localhost:5173/login
  2. 输入用户名: `admin`，密码: `admin123`
  3. 点击登录按钮
- **优点**: 安全性高，用户可控
- **缺点**: 需要手动操作

### 方案3: 测试页面验证
- **位置**: http://localhost:5173/test-login.html
- **功能**: 提供登录测试和API验证功能
- **用途**: 调试认证问题

## 🛠 技术实现

### 1. 自动登录实现
```javascript
// frontend/src/App.vue
const autoLogin = async () => {
  if (!localStorage.getItem('token')) {
    try {
      const response = await axios.post('/api/auth/login', {
        username: 'admin',
        password: 'admin123'
      })
      
      if (response.data.token) {
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('username', response.data.username)
        ElMessage.success('自动登录成功')
      }
    } catch (error) {
      console.log('自动登录失败，需要手动登录')
    }
  }
}
```

### 2. Axios拦截器
```javascript
// frontend/src/main.js
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
})
```

### 3. 路由守卫
```javascript
// frontend/src/router/index.js
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/reports')
  } else {
    next()
  }
})
```

## 🔧 调试工具

### 1. 认证调试脚本
```bash
./debug-auth.sh
```

### 2. 应用功能测试
```bash
./test-app.sh
```

### 3. 浏览器开发者工具
- **Console**: 查看JavaScript错误
- **Network**: 查看API请求和响应
- **Application**: 查看localStorage中的token

## 📋 验证步骤

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

## 🎯 最佳实践

### 1. 开发环境
- 使用自动登录功能简化开发流程
- 定期清除localStorage测试登录流程
- 使用测试页面验证API功能

### 2. 生产环境
- 禁用自动登录功能
- 实现完整的用户注册和登录流程
- 添加token刷新机制
- 实现登出功能

### 3. 调试流程
1. 检查后端服务是否正常运行
2. 检查MongoDB连接是否正常
3. 验证登录API是否返回有效token
4. 检查前端是否正确发送Authorization头
5. 查看浏览器控制台错误信息

## 📝 常见问题

### Q: 为什么会出现403错误？
A: 前端尝试访问需要认证的API但没有提供有效的JWT token。

### Q: 如何解决403错误？
A: 确保用户已登录，localStorage中有有效的token。

### Q: 自动登录失败怎么办？
A: 手动访问登录页面，输入用户名和密码登录。

### Q: 如何清除登录状态？
A: 在浏览器控制台执行 `localStorage.clear()` 或点击应用中的"退出"按钮。

## 🎉 解决方案效果

实施上述解决方案后：
- ✅ 应用启动时自动登录
- ✅ 前端可以正常访问需要认证的API
- ✅ 用户无需手动输入账号密码
- ✅ 提供完整的调试工具和测试页面
- ✅ 详细的文档和使用指南

---

**MongoReporter** - 认证问题已完美解决！🚀 