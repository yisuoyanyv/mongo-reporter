# Mongo Reporter 使用指南

## 🚀 快速开始

### 1. 启动应用

```bash
# 启动所有服务
./start.sh

# 或者手动启动
cd backend && ./mvnw spring-boot:run
cd frontend && npm run dev
```

### 2. 访问应用

- **前端地址**: http://localhost:5173
- **后端地址**: http://localhost:8080

### 3. 登录信息

- **用户名**: admin
- **密码**: admin123

## 📊 功能特性

### 数据源管理
- ✅ 添加、编辑、删除数据源
- ✅ 测试数据源连接
- ✅ 显示连接状态
- ✅ 支持多个MongoDB数据库

### 报表设计器
- ✅ 拖拽式组件设计
- ✅ 字段列表显示
- ✅ 数据预览功能
- ✅ 实时图表渲染
- ✅ 支持多种图表类型

### 图表类型
- 📈 **折线图**: 展示趋势数据
- 📊 **柱状图**: 对比数值数据
- 🥧 **饼图**: 显示占比数据
- 📋 **表格**: 展示详细数据（开发中）

## 🎨 报表设计流程

### 1. 选择数据源
1. 进入"数据源管理"页面
2. 添加新的数据源或选择现有数据源
3. 测试连接确保可用

### 2. 创建报表
1. 进入"报表管理"页面
2. 点击"新建报表"
3. 选择数据源和集合
4. 查看字段列表和数据预览

### 3. 设计图表
1. 从左侧组件库拖拽图表组件到画布
2. 点击"配置"按钮设置图表参数
3. 选择对应的字段映射
4. 实时预览图表效果

### 4. 保存报表
1. 填写报表名称和描述
2. 点击"保存报表"
3. 返回报表列表查看

## 🔧 技术架构

### 前端技术栈
- **Vue 3**: 前端框架
- **Element Plus**: UI组件库
- **ECharts**: 图表库
- **Vue Router**: 路由管理
- **Axios**: HTTP客户端
- **Vuedraggable**: 拖拽功能

### 后端技术栈
- **Spring Boot**: 后端框架
- **Spring Data MongoDB**: 数据访问
- **Spring Security**: 安全认证
- **JWT**: 身份验证
- **MongoDB**: 数据库

## 📁 项目结构

```
mongo-reporter/
├── frontend/                 # 前端项目
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 通用组件
│   │   └── router/          # 路由配置
│   └── package.json
├── backend/                  # 后端项目
│   ├── src/main/java/
│   │   ├── controller/      # 控制器
│   │   ├── model/          # 数据模型
│   │   ├── repository/     # 数据访问层
│   │   └── config/         # 配置类
│   └── pom.xml
├── start.sh                 # 启动脚本
├── stop.sh                  # 停止脚本
└── README.md
```

## 🛠️ 开发指南

### 环境要求
- **Node.js**: 16+
- **Java**: 17+
- **MongoDB**: 4.2+
- **Maven**: 3.6+ (或使用Maven Wrapper)

### 开发模式
```bash
# 前端开发
cd frontend
npm install
npm run dev

# 后端开发
cd backend
./mvnw spring-boot:run
```

### API文档
- **数据源管理**: `/api/datasource`
- **报表管理**: `/api/report/configs`
- **数据查询**: `/api/report/data`
- **图表数据**: `/api/report/chart-data`

## 🐛 故障排除

### 常见问题

#### 1. MongoDB连接失败
```bash
# 检查MongoDB状态
brew services list | grep mongodb

# 启动MongoDB
brew services start mongodb-community
```

#### 2. 端口被占用
```bash
# 查看端口占用
lsof -i :8080
lsof -i :5173

# 停止占用进程
pkill -f "java.*MongoReporterApplication"
pkill -f "vite"
```

#### 3. 前端无法访问后端
- 检查后端服务是否启动
- 确认代理配置正确
- 查看浏览器控制台错误

#### 4. 图表不显示数据
- 检查数据源连接
- 确认集合名称正确
- 验证字段映射配置

### 日志查看
```bash
# 查看后端日志
tail -f backend.log

# 查看前端日志
tail -f frontend.log
```

## 📈 性能优化

### 数据查询优化
- 使用适当的索引
- 限制查询结果数量
- 缓存常用数据

### 前端优化
- 组件懒加载
- 图表按需渲染
- 数据分页加载

## 🔒 安全建议

### 生产环境配置
- 修改默认密码
- 配置HTTPS
- 设置防火墙规则
- 定期备份数据

### 数据安全
- 加密敏感数据
- 限制数据库访问权限
- 审计日志记录

## 📞 支持

如有问题或建议，请：
1. 查看日志文件
2. 检查配置是否正确
3. 参考故障排除部分
4. 提交Issue到项目仓库

---

**版本**: 1.0.0  
**更新时间**: 2024-07-27 