# MongoReporter 应用演示

## 🎉 应用已成功启动！

### 📱 访问地址
- **前端**: http://localhost:5173
- **后端**: http://localhost:8080

### 👤 默认用户
- **管理员**: `admin` / `admin123`
- **测试用户**: `testuser` / `test123`

## 🚀 功能演示

### 1. 用户登录
1. 打开浏览器访问 http://localhost:5173
2. 使用默认账号登录：`admin` / `admin123`
3. 登录成功后会自动跳转到报表管理页面

### 2. 数据源管理
- 在导航菜单中点击"数据源管理"
- 查看系统自动生成的示例数据源
- 可以添加新的MongoDB连接

### 3. 报表设计
- 在报表管理页面点击"新建报表"
- 选择数据源和集合
- 拖拽图表组件到设计画布
- 配置图表参数
- 保存报表配置

### 4. 报表查看
- 在报表列表中点击"查看"按钮
- 查看生成的图表
- 支持报表的编辑和删除

## 📊 示例数据

系统已自动生成以下示例数据：

### 数据源
- 测试数据源1 (mongodb://localhost:27017/test1)
- 测试数据源2 (mongodb://localhost:27017/test2)
- 销售数据 (mongodb://localhost:27017/sales)
- 用户数据 (mongodb://localhost:27017/users)
- 产品数据 (mongodb://localhost:27017/products)
- 订单数据 (mongodb://localhost:27017/orders)

### MongoDB集合
- `users` - 用户数据
- `sales` - 销售数据
- `products` - 产品数据
- `orders` - 订单数据
- `data_sources` - 数据源配置
- `report_configs` - 报表配置

## 🔧 技术特性

### 后端特性
- ✅ Spring Boot 3.5.4
- ✅ MongoDB 4.2+ 数据持久化
- ✅ JWT 用户认证
- ✅ RESTful API 设计
- ✅ 动态数据源支持
- ✅ 示例数据自动生成

### 前端特性
- ✅ Vue 3 + Element Plus
- ✅ 拖拽式报表设计器
- ✅ 响应式UI设计
- ✅ 现代化用户体验
- ✅ ECharts 图表支持

## 🛠 开发工具

### 启动应用
```bash
./start.sh
```

### 停止应用
```bash
./stop.sh
```

### 测试应用
```bash
./test-app.sh
```

## 📝 使用说明

1. **首次使用**: 直接使用默认账号登录即可
2. **创建报表**: 选择数据源 → 选择集合 → 拖拽组件 → 配置参数 → 保存
3. **查看报表**: 在报表列表中点击查看按钮
4. **管理数据源**: 在数据源管理页面添加新的MongoDB连接

## 🎯 核心功能

- **动态数据源**: 支持多个MongoDB数据源的动态配置
- **拖拽设计**: 直观的拖拽式报表设计界面
- **数据持久化**: 所有配置数据持久化到MongoDB
- **用户认证**: 基于JWT的安全认证系统
- **响应式设计**: 支持多种屏幕尺寸的现代化UI

---

**MongoReporter** - 让数据可视化变得简单！ 🚀 