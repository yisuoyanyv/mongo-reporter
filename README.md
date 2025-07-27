# MongoReporter - MongoDB数据报表平台

一个基于Spring Boot + Vue3的现代化数据报表平台，支持MongoDB数据源的动态配置和可视化报表设计。

## 🚀 功能特性

- **动态数据源配置**: 支持多个MongoDB数据源的动态添加和管理
- **拖拽式报表设计**: 直观的拖拽界面，支持多种图表类型（折线图、柱状图、饼图等）
- **数据持久化**: 报表配置、用户数据、数据源信息全部持久化到MongoDB
- **用户认证**: 基于JWT的用户认证和授权系统
- **响应式设计**: 现代化的UI设计，支持多种屏幕尺寸
- **实时预览**: 设计时实时预览图表效果

## 🛠 技术栈

### 后端
- **Spring Boot 3.5.4**: 主框架
- **Spring Data MongoDB**: 数据访问层
- **Spring Security**: 安全认证
- **JWT**: 用户认证令牌
- **Maven**: 依赖管理

### 前端
- **Vue 3**: 前端框架
- **Element Plus**: UI组件库
- **ECharts**: 图表库
- **Vue Router**: 路由管理
- **Axios**: HTTP客户端
- **Vite**: 构建工具

## 📋 系统要求

- Java 17+
- Node.js 16+
- MongoDB 4.2+
- Maven 3.6+

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd mongo-reporter
```

### 2. 启动MongoDB
确保MongoDB服务正在运行：
```bash
# macOS (使用Homebrew)
brew services start mongodb-community

# 或者直接启动
mongod
```

### 3. 启动后端
```bash
cd backend
./mvnw spring-boot:run
```
后端将在 http://localhost:8080 启动

### 4. 启动前端
```bash
cd frontend
npm install
npm run dev
```
前端将在 http://localhost:5173 启动

### 5. 访问应用
打开浏览器访问 http://localhost:5173

## 👤 默认用户

系统会自动创建以下默认用户：

- **管理员**: admin / admin123
- **测试用户**: testuser / test123

## 📊 示例数据

系统启动时会自动生成以下示例数据：

### 数据源
- 测试数据源1 (mongodb://localhost:27017/test1)
- 测试数据源2 (mongodb://localhost:27017/test2)
- 销售数据 (mongodb://localhost:27017/sales)
- 用户数据 (mongodb://localhost:27017/users)
- 产品数据 (mongodb://localhost:27017/products)
- 订单数据 (mongodb://localhost:27017/orders)

### 示例报表
- 销售趋势图 (公开)
- 产品分类统计 (公开)
- 订单金额统计 (私有)

## 🎯 使用指南

### 1. 用户登录
- 访问应用首页，使用默认账号登录
- 支持用户注册新账号

### 2. 数据源管理
- 在"数据源管理"页面添加新的MongoDB连接
- 支持测试连接功能
- 可以设置默认数据源

### 3. 报表设计
- 在"报表管理"页面点击"新建报表"
- 选择数据源和集合
- 拖拽图表组件到设计画布
- 配置图表参数和数据映射
- 保存报表配置

### 4. 报表查看
- 在报表列表中点击"查看"按钮
- 支持报表的编辑和删除
- 可以导出为图片或PDF（开发中）

## 🔧 开发指南

### 项目结构
```
mongo-reporter/
├── backend/                 # Spring Boot后端
│   ├── src/main/java/
│   │   └── com/mongo/reporter/backend/
│   │       ├── controller/  # 控制器
│   │       ├── model/       # 数据模型
│   │       ├── repository/  # 数据访问层
│   │       └── config/      # 配置类
│   └── pom.xml
├── frontend/                # Vue3前端
│   ├── src/
│   │   ├── views/          # 页面组件
│   │   ├── router/         # 路由配置
│   │   └── main.js         # 入口文件
│   └── package.json
└── README.md
```

### API文档

#### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

#### 数据源管理
- `GET /api/datasource` - 获取数据源列表
- `POST /api/datasource` - 创建数据源
- `PUT /api/datasource/{id}` - 更新数据源
- `DELETE /api/datasource/{id}` - 删除数据源

#### 报表管理
- `GET /api/report/configs` - 获取报表列表
- `POST /api/report/configs` - 创建报表
- `PUT /api/report/configs/{id}` - 更新报表
- `DELETE /api/report/configs/{id}` - 删除报表
- `GET /api/report/collections` - 获取集合列表
- `GET /api/report/fields` - 获取字段列表
- `GET /api/report/data` - 获取数据

## 🐛 故障排除

### 常见问题

1. **MongoDB连接失败**
   - 确保MongoDB服务正在运行
   - 检查连接字符串是否正确
   - 确认MongoDB版本兼容性

2. **前端无法访问后端API**
   - 检查后端是否正常启动
   - 确认CORS配置正确
   - 检查网络连接

3. **JWT认证失败**
   - 检查token是否过期
   - 确认JWT密钥配置正确
   - 重新登录获取新token

## 📝 更新日志

### v1.0.0 (2025-07-26)
- 初始版本发布
- 支持基础报表设计功能
- 实现用户认证和数据持久化
- 提供示例数据和报表

## 🤝 贡献指南

欢迎提交Issue和Pull Request来改进项目！

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交Issue
- 发送邮件
- 项目讨论区

---

**MongoReporter** - 让数据可视化变得简单！ 