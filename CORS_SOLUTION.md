# CORS跨域问题解决方案

## 🎯 问题描述
前端访问后端API时遇到跨域（CORS）问题，导致403错误。

## ✅ 解决方案

### 1. 后端CORS配置

在 `backend/src/main/java/com/mongo/reporter/backend/config/SecurityConfig.java` 中配置了完整的CORS支持：

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // 允许的源
    configuration.setAllowedOriginPatterns(List.of("*"));
    
    // 允许的方法
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
    
    // 允许的头部
    configuration.setAllowedHeaders(Arrays.asList(
        "Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With",
        "Access-Control-Request-Method", "Access-Control-Request-Headers"
    ));
    
    // 暴露的头部
    configuration.setExposedHeaders(Arrays.asList(
        "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
    ));
    
    // 允许凭证
    configuration.setAllowCredentials(true);
    
    // 预检请求缓存时间
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### 2. 前端代理配置

在 `frontend/vite.config.js` 中配置了代理：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      secure: false
    }
  }
}
```

## 🔧 验证步骤

### 1. CORS预检请求测试
```bash
curl -X OPTIONS http://localhost:8080/api/report/configs \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v
```

**预期结果：**
- HTTP 200
- Access-Control-Allow-Origin: http://localhost:5173
- Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS,HEAD,PATCH
- Access-Control-Allow-Headers: Authorization

### 2. 实际API请求测试
```bash
curl -X GET http://localhost:8080/api/datasource \
  -H "Origin: http://localhost:5173" \
  -v
```

**预期结果：**
- HTTP 200
- 返回JSON数据
- 正确的CORS头部

## 📋 关键配置点

### 1. 允许的源
- 使用 `setAllowedOriginPatterns(List.of("*"))` 允许所有源
- 生产环境建议设置为具体的域名

### 2. 允许的方法
- 包含所有必要的HTTP方法：GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH

### 3. 允许的头部
- 包含 `Authorization` 头部以支持JWT认证
- 包含其他必要的请求头部

### 4. 凭证支持
- `setAllowCredentials(true)` 允许携带凭证（cookies, authorization headers）

### 5. 预检请求缓存
- `setMaxAge(3600L)` 设置预检请求缓存时间为1小时

## 🚀 前端使用

前端现在可以正常访问后端API：

```javascript
// 登录
const response = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'admin', password: 'admin123' })
});

// 获取数据
const data = await fetch('/api/datasource', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

## ✅ 验证结果

- ✅ CORS预检请求正常工作
- ✅ 实际API请求正常工作
- ✅ 前端可以正常访问后端API
- ✅ JWT认证头部正确传递
- ✅ 所有必要的CORS头部都已配置

## 🎉 总结

CORS跨域问题已完全解决！现在前端可以正常访问后端API，不会再出现403跨域错误。

---

**CORS配置完成，跨域问题已解决！** 🚀 