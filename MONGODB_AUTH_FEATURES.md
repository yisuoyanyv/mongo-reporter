# MongoDB认证功能说明

## 概述

MongoReporter现在支持MongoDB的用户密码认证模式，允许用户连接到需要认证的MongoDB实例。

## 功能特性

### 1. 认证支持
- ✅ 支持用户名/密码认证
- ✅ 支持自定义认证数据库
- ✅ 支持无认证连接（向后兼容）
- ✅ 安全的密码存储

### 2. 连接管理
- ✅ 认证连接测试
- ✅ 连接状态监控
- ✅ 错误处理和提示
- ✅ 连接池管理

### 3. 用户界面
- ✅ 认证开关控制
- ✅ 用户名/密码输入
- ✅ 认证数据库配置
- ✅ 连接测试功能

## 使用方法

### 1. 创建数据源

#### 无认证连接
```
连接字符串: mongodb://localhost:27017/database
认证设置: 关闭
```

#### 认证连接
```
连接字符串: mongodb://localhost:27017/database
认证设置: 启用
用户名: your_username
密码: your_password
认证数据库: admin (或用户所在的数据库)
```

### 2. 测试连接

1. 在数据源管理页面点击"新增数据源"
2. 填写连接信息
3. 启用认证开关（如需要）
4. 填写认证信息
5. 点击"测试连接"验证

### 3. 使用数据源

创建数据源后，可以在报表设计器中使用：
- 选择数据源
- 选择集合
- 配置图表
- 预览数据

## 技术实现

### 后端实现

#### 1. 数据模型更新
```java
public class DataSource {
    private String username;
    private String password;
    private String authDatabase;
    private boolean useAuth;
    
    // 构建认证URI的方法
    public String buildAuthenticatedUri() {
        // 根据认证信息构建完整的连接URI
    }
}
```

#### 2. 连接工具类
```java
@Component
public class MongoConnectionUtil {
    public MongoClient createMongoClient(DataSource dataSource) {
        // 根据数据源配置创建MongoDB客户端
    }
    
    public boolean testConnection(DataSource dataSource) {
        // 测试MongoDB连接
    }
}
```

#### 3. API接口
- `POST /api/datasource/test` - 测试连接（支持认证）
- `POST /api/report/collections/auth` - 获取集合列表（支持认证）
- `POST /api/report/fields/auth` - 获取字段列表（支持认证）
- `POST /api/report/data/auth` - 获取数据（支持认证）

### 前端实现

#### 1. 表单组件
- 认证开关控制
- 条件显示认证字段
- 表单验证

#### 2. 连接测试
- 支持认证的连接测试
- 错误信息显示
- 连接状态管理

## 安全考虑

### 1. 密码存储
- 密码在传输时使用HTTPS
- 密码在数据库中加密存储
- 密码在日志中不显示

### 2. 连接安全
- 使用MongoDB官方驱动
- 支持SSL/TLS连接
- 连接超时设置

### 3. 访问控制
- 用户只能访问自己的数据源
- 默认数据源只读访问
- 权限验证

## 测试指南

### 1. 环境准备
```bash
# 启动MongoDB
brew services start mongodb-community

# 创建测试用户
mongosh
use admin
db.createUser({
  user: "testuser",
  pwd: "testpass123",
  roles: [{ role: "readWrite", db: "testdb" }]
})
```

### 2. 运行测试
```bash
# 运行认证功能测试脚本
./test_auth_features.sh
```

### 3. 测试场景
- ✅ 无认证连接测试
- ✅ 正确认证信息测试
- ✅ 错误认证信息测试
- ✅ 认证数据库测试
- ✅ 数据查询测试

## 故障排除

### 常见问题

#### 1. 连接失败
**问题**: 认证连接失败
**解决**: 
- 检查用户名和密码是否正确
- 确认认证数据库是否正确
- 检查用户权限

#### 2. 权限错误
**问题**: 用户没有访问权限
**解决**:
- 检查用户角色和权限
- 确认数据库名称正确
- 联系数据库管理员

#### 3. 网络问题
**问题**: 无法连接到MongoDB
**解决**:
- 检查MongoDB服务状态
- 确认网络连接
- 检查防火墙设置

### 日志查看
```bash
# 查看后端日志
tail -f backend_auth.log

# 查看前端日志
tail -f frontend_auth.log
```

## 更新日志

### v1.1.0 (当前版本)
- ✨ 新增MongoDB认证支持
- ✨ 新增认证连接测试
- ✨ 新增认证数据库配置
- ✨ 改进用户界面
- 🐛 修复连接池问题
- 📝 更新文档

### v1.0.0
- 🎉 初始版本发布
- ✨ 基础报表功能
- ✨ 数据源管理
- ✨ 图表配置

## 未来计划

### 短期计划
- [ ] 支持MongoDB Atlas连接
- [ ] 支持连接字符串模板
- [ ] 改进错误提示信息

### 长期计划
- [ ] 支持LDAP认证
- [ ] 支持Kerberos认证
- [ ] 支持SSL证书认证
- [ ] 连接池监控

## 贡献指南

欢迎提交Issue和Pull Request来改进认证功能。

### 开发环境
- Java 17+
- Node.js 16+
- MongoDB 4.4+
- Vue 3
- Element Plus

### 代码规范
- 遵循Java代码规范
- 遵循Vue.js代码规范
- 添加适当的注释
- 编写单元测试 