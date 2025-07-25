# Mongo Reporter 数据报表平台

> 基于 Spring Boot + Vue3 + MongoDB 的现代化开源数据报表平台，支持动态数据源、拖拽式图表设计、报表持久化、权限与分享、导出等高级功能。

## 功能特性
- 🗃️ 支持 MongoDB 4.2 及以上，动态配置多数据源
- 🖱️ 前端拖拽式报表设计，所见即所得
- 📊 多种图表类型（表格、柱状图、折线图、饼图等）
- ⚡ 图表配置项可视化编辑，实时预览
- 🔒 用户注册/登录/鉴权，报表归属与权限管理
- 🔗 报表一键分享（唯一链接，公开/私有权限）
- 📤 报表一键导出（图片、PDF、Excel）
- 🧩 代码结构清晰，易于二次开发

## 技术栈
- **后端**：Spring Boot、Spring Security、Spring Data MongoDB、JWT
- **前端**：Vue3、Element Plus、ECharts、vue-draggable-next、axios
- **数据库**：MongoDB 4.2+

## 快速启动
### 1. 启动后端
```bash
cd backend
./mvnw spring-boot:run
```
默认端口：8080，MongoDB 默认连接：`mongodb://localhost:27017/mongo-reporter`

### 2. 启动前端
```bash
cd frontend
npm install
npm run dev
```
默认端口：5173

### 3. 访问系统
浏览器访问：http://localhost:5173

## 目录结构
```
mongo-reporter/
├── backend/   # Spring Boot 后端
├── frontend/  # Vue3 前端
└── README.md  # 项目说明
```

## 贡献指南
欢迎 Issue、PR 和 Star！如需定制开发或有建议，欢迎联系作者。

## 许可证
本项目基于 MIT License 开源，详见 LICENSE 文件。 