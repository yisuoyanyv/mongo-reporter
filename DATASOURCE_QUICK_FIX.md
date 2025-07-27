# 数据源管理问题快速修复指南

## 🚨 问题描述
前端页面看不到数据源数据，页面显示空白或加载状态。

## 🔧 快速修复步骤

### 1. 检查后端服务
```bash
# 检查后端是否运行
curl -X GET http://localhost:8080/api/datasource
```

**预期结果：** 返回JSON数据，包含数据源列表

### 2. 检查前端代理
```bash
# 检查前端代理是否工作
curl -X GET http://localhost:5173/api/datasource
```

**预期结果：** 返回相同的JSON数据

### 3. 测试页面访问
访问以下测试页面来验证功能：

#### 简单测试页面
```
http://localhost:5173/simple-datasource-test.html
```

#### 详细调试页面
```
http://localhost:5173/debug-datasource.html
```

#### 主应用页面
```
http://localhost:5173/datasources
```

### 4. 浏览器调试

#### 打开开发者工具
1. 按 `F12` 或右键选择"检查"
2. 切换到 `Console` 标签
3. 查看是否有错误信息

#### 检查网络请求
1. 切换到 `Network` 标签
2. 刷新页面
3. 查看 `/api/datasource` 请求的状态

### 5. 常见问题解决

#### 问题1：403 Forbidden
**原因：** 认证问题
**解决：**
```javascript
// 在浏览器控制台执行
localStorage.setItem('token', 'your-jwt-token')
```

#### 问题2：CORS错误
**原因：** 跨域问题
**解决：** 检查后端CORS配置

#### 问题3：页面空白
**原因：** JavaScript错误
**解决：** 查看浏览器控制台错误信息

### 6. 手动测试API

#### 使用curl测试
```bash
# 测试无认证
curl -X GET http://localhost:8080/api/datasource

# 测试带认证
curl -X GET http://localhost:8080/api/datasource \
  -H "Authorization: Bearer your-jwt-token"
```

#### 使用浏览器测试
```javascript
// 在浏览器控制台执行
fetch('/api/datasource')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error(error))
```

### 7. 重启服务

#### 重启后端
```bash
cd backend
./mvnw spring-boot:run
```

#### 重启前端
```bash
cd frontend
npm run dev
```

### 8. 验证修复

#### 检查数据源列表
1. 访问 `http://localhost:5173/datasources`
2. 应该看到数据源表格
3. 包含以下列：名称、连接字符串、创建者、操作

#### 检查功能
1. 点击"新增数据源"按钮
2. 填写表单并保存
3. 验证数据是否刷新

## 📋 调试信息

### 后端日志
查看后端控制台输出，寻找：
- 数据源API调用日志
- 错误信息
- 认证相关日志

### 前端日志
查看浏览器控制台，寻找：
- 网络请求日志
- JavaScript错误
- Vue组件生命周期日志

## 🎯 预期结果

修复成功后，数据源管理页面应该显示：

```
┌─────────────────────────────────────────────────────────────┐
│ 数据源管理                                    [新增数据源]   │
├─────────────────────────────────────────────────────────────┤
│ 名称         │ 连接字符串                    │ 创建者 │ 操作 │
├─────────────────────────────────────────────────────────────┤
│ 测试数据源1   │ mongodb://localhost:27017/test1 │ admin │ 编辑│
│ 测试数据源2   │ mongodb://localhost:27017/test2 │ admin │ 编辑│
│ 销售数据     │ mongodb://localhost:27017/sales  │ admin │ 编辑│
│ 用户数据     │ mongodb://localhost:27017/users  │ admin │ 编辑│
└─────────────────────────────────────────────────────────────┘
```

## 🆘 如果问题仍然存在

1. **检查MongoDB连接**
   ```bash
   mongo --eval "db.adminCommand('ping')"
   ```

2. **检查数据源数据**
   ```bash
   mongo --eval "use mongo_reporter; db.dataSource.find()"
   ```

3. **查看完整日志**
   - 后端：查看Spring Boot启动日志
   - 前端：查看Vite开发服务器日志

4. **重置数据**
   ```bash
   # 重启MongoDB
   brew services restart mongodb-community
   ```

---

**如果按照以上步骤仍然无法解决问题，请提供：**
1. 浏览器控制台错误信息
2. 后端启动日志
3. 网络请求的详细错误信息 