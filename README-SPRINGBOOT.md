# Spring Boot 后端配置说明

## 项目结构

```
lab/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/acc/lab/
│       │       ├── LabApplication.java      # Spring Boot 主应用类
│       │       └── config/
│       │           ├── WebConfig.java        # 静态资源配置
│       │           └── CorsConfig.java       # CORS 跨域配置
│       └── resources/
│           ├── application.yml               # 应用配置文件
│           └── static/                       # 静态资源目录（可选）
├── pom.xml                                   # Maven 依赖配置
└── [前端 HTML 文件]                          # 根目录下的 HTML 文件
```

## 环境要求

- JDK 17 或更高版本
- Maven 3.6+（或使用 IDE 内置的 Maven）
- MySQL 数据库

## 配置说明

### 1. 数据库配置

编辑 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/acc_system_db?...
    username: root
    password: your_password
```

### 2. 邮件配置

同样在 `application.yml` 中配置邮件服务：

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: your_email@qq.com
    password: your_auth_code
```

## 运行方式

### 方式一：使用 Maven

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

### 方式二：打包后运行

```bash
# 打包
mvn clean package

# 运行 JAR 文件
java -jar target/lab-1.0.0.jar
```

### 方式三：使用 IDE

1. 在 IntelliJ IDEA 或 Eclipse 中导入项目
2. 找到 `LabApplication.java`
3. 右键运行 `main` 方法

## 访问地址

启动后访问：`http://localhost:8080`

- 首页：`http://localhost:8080/index.html`
- 登录页：`http://localhost:8080/login.html`
- 忘记密码：`http://localhost:8080/forgot-password.html`

## 静态资源说明

Spring Boot 会自动提供以下位置的静态资源：

1. `src/main/resources/static/` - 标准静态资源目录
2. 项目根目录（`./`）- 包含所有 HTML 文件
3. `photos/` 目录 - 轮播图等图片资源

## 注意事项

1. **端口冲突**：默认端口是 8080，如果被占用，可在 `application.yml` 中修改 `server.port`
2. **数据库**：确保 MySQL 服务已启动，数据库 `acc_system_db` 已创建
3. **前端 API 地址**：如果前端代码中使用的是 `http://localhost:3000`，需要改为 `http://localhost:8080`
4. **Node.js 服务**：如果需要同时运行 Node.js 服务，请使用不同的端口

## 下一步

1. 将现有的 Express.js API 端点迁移到 Spring Boot Controller
2. 创建对应的 Service 和 Repository 层
3. 配置 JWT 认证过滤器
4. 实现邮件发送服务

