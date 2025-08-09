# Spring Boot 热更新配置指南

## 概述

本项目已配置Spring Boot DevTools，支持热更新功能。当您修改Java代码或配置文件时，应用会自动重启，无需手动停止和启动服务。

## 配置说明

### 1. 依赖配置

在 `backend/pom.xml` 中添加了Spring Boot DevTools依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### 2. 应用配置

在 `backend/src/main/resources/application.properties` 中添加了热更新配置：

```properties
# 热更新配置
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
spring.devtools.restart.poll-interval=2s
spring.devtools.restart.quiet-period=1s
spring.devtools.restart.additional-paths=src/main/java
spring.devtools.restart.exclude=static/**,public/**,templates/**
```

## 使用方法

### 启动开发模式

```bash
# 使用开发模式启动脚本
./start_backend_dev.sh

# 或者手动启动
cd backend
./mvnw spring-boot:run
```

### 热更新功能

1. **Java文件修改**：修改 `src/main/java/` 下的任何Java文件，保存后应用会自动重启
2. **配置文件修改**：修改 `application.properties` 或其他配置文件，保存后应用会自动重启
3. **静态资源**：修改静态资源文件不会触发重启（如CSS、JS、图片等）

### 监控热更新

```bash
# 启动热更新监控
./test_hot_reload.sh
```

## 配置参数说明

- `spring.devtools.restart.enabled=true`：启用自动重启
- `spring.devtools.livereload.enabled=true`：启用LiveReload
- `spring.devtools.restart.poll-interval=2s`：文件变化检测间隔
- `spring.devtools.restart.quiet-period=1s`：静默期，避免频繁重启
- `spring.devtools.restart.additional-paths=src/main/java`：监控的额外路径
- `spring.devtools.restart.exclude=static/**,public/**,templates/**`：排除的路径

## 注意事项

1. **IDE集成**：如果使用IDE（如IntelliJ IDEA），确保启用了自动编译
2. **文件系统**：确保文件系统支持文件变化通知
3. **性能**：热更新会增加一些性能开销，生产环境建议禁用
4. **日志**：重启时会看到类似以下的日志：
   ```
   [INFO] --- spring-boot:3.5.4:run (default-cli) @ backend ---
   [INFO] Attaching agents: []
   ```

## 故障排除

### 热更新不工作

1. 检查DevTools依赖是否正确添加
2. 确认配置文件中的热更新设置
3. 检查文件系统权限
4. 查看启动日志是否有错误信息

### 频繁重启

1. 调整 `poll-interval` 参数
2. 检查 `exclude` 配置是否正确
3. 避免在监控目录中放置临时文件

## 开发建议

1. **代码修改**：修改代码后观察控制台输出，确认重启成功
2. **测试验证**：重启后及时测试相关功能
3. **日志监控**：关注重启过程中的错误日志
4. **性能优化**：在开发完成后，生产环境禁用热更新功能

## 相关文件

- `backend/pom.xml`：Maven配置
- `backend/src/main/resources/application.properties`：应用配置
- `start_backend_dev.sh`：开发模式启动脚本
- `test_hot_reload.sh`：热更新测试脚本 