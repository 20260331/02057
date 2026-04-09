# 部署文档

## 1. 环境要求

### Docker 部署（推荐）

| 软件 | 最低版本 | 说明 |
|------|---------|------|
| Docker | 20.10+ | 容器引擎 |
| Docker Compose | 2.0+ | 编排工具 |
| 内存 | 4GB+ | MySQL + Redis + Spring Boot + Nginx |
| 磁盘 | 10GB+ | 镜像 + 数据卷 + 日志 |

### 本地开发环境

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.9+ | 后端构建 |
| Node.js | 18+ | 前端构建 |
| npm | 9+ | 前端包管理 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7+ | 缓存 |

---

## 2. Docker Compose 一键部署

### 2.1 快速启动

```bash
# 克隆项目
git clone <repository-url>
cd label-02057

# 构建并启动所有服务
docker-compose up -d --build

# 查看服务运行状态
docker-compose ps

# 查看实时日志
docker-compose logs -f
```

### 2.2 服务清单

| 服务 | 容器名 | 端口映射 | 访问地址 |
|------|--------|---------|---------|
| 前端 | sms-frontend | 8081:80 | http://localhost:8081 |
| 后端 | sms-backend | 8080:8080 | http://localhost:8080/api |
| MySQL | sms-mysql | 3306:3306 | localhost:3306 |
| Redis | sms-redis | 6379:6379 | localhost:6379 |

### 2.3 启动顺序与健康检查

```
mysql (healthcheck: mysqladmin ping)
  └─> redis (healthcheck: redis-cli ping)
        └─> backend (depends_on: mysql, redis)
              └─> frontend (depends_on: backend)
```

- MySQL 通过 `healthcheck` 确保完全启动后，后端才开始连接
- 数据库初始化脚本在 `docker/mysql/init/` 目录下，首次启动自动执行
- 启动后约需 30-60 秒等待 Spring Boot 完全初始化

### 2.4 数据持久化

Docker Compose 定义了以下数据卷：

| 卷名 | 挂载路径 | 说明 |
|------|---------|------|
| mysql_data | /var/lib/mysql | MySQL 数据文件 |
| redis_data | /data | Redis 持久化数据 |
| backend_uploads | /app/uploads | 上传文件（请假附件等） |
| backend_backups | /app/backups | 数据库备份文件 |

### 2.5 常用运维命令

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（清空所有数据，慎用）
docker-compose down -v

# 仅重启后端
docker-compose restart backend

# 仅重建前端
docker-compose up -d --build frontend

# 查看后端日志
docker-compose logs -f backend

# 进入 MySQL 容器
docker exec -it sms-mysql mysql -uroot -proot123 sms_db

# 进入后端容器
docker exec -it sms-backend sh
```

---

## 3. 本地开发环境搭建

### 3.1 数据库初始化

```bash
# 启动 MySQL 服务
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE sms_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sms_db;

# 执行建表脚本（二选一）
source docker/mysql/init/01-schema.sql;
# 或
source backend/src/main/resources/db/schema.sql;

# 导入初始数据
source docker/mysql/init/02-data.sql;
# 或
source backend/src/main/resources/db/data.sql;
```

### 3.2 Redis 启动

```bash
# 启动 Redis（默认配置即可）
redis-server
```

### 3.3 后端启动

```bash
cd backend

# 修改数据库连接配置（如有需要）
# 编辑 src/main/resources/application.yml
# 调整 spring.datasource.url / username / password

# 方式一：Maven 启动
mvn spring-boot:run

# 方式二：先编译再启动
mvn clean package -DskipTests
java -jar target/student-management-system-1.0.0-SNAPSHOT.jar
```

后端默认运行在 `http://localhost:8080`，API 前缀为 `/api`。

### 3.4 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 生产构建
npm run build
```

- 开发服务器运行在 `http://localhost:3000`
- Vite 配置了 `/api` 代理到 `http://localhost:8080`，无需额外配置跨域

---

## 4. 配置说明

### 4.1 后端配置 (`application.yml`)

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sms_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver

  data:
    redis:
      host: localhost
      port: 6379

  servlet:
    multipart:
      max-file-size: 10MB        # 单文件最大 10MB
      max-request-size: 50MB     # 单次请求最大 50MB

jwt:
  secret: YourSuperSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong2024
  expiration: 86400000           # Access Token 有效期 24 小时（毫秒）
  refresh-expiration: 604800000  # Refresh Token 有效期 7 天（毫秒）
  header: Authorization
  prefix: Bearer

app:
  security:
    max-login-attempts: 5        # 最大登录失败次数
    lock-duration-minutes: 30    # 锁定时长（分钟）
  upload:
    path: /app/uploads           # 文件上传路径（Docker 环境）
  pagination:
    default-page-size: 10
    max-page-size: 100
```

### 4.2 Docker 环境变量覆盖

在 `docker-compose.yml` 中通过环境变量覆盖默认配置：

```yaml
backend:
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/sms_db?...
    SPRING_DATASOURCE_USERNAME: root
    SPRING_DATASOURCE_PASSWORD: root123
    SPRING_DATA_REDIS_HOST: redis
    SPRING_DATA_REDIS_PORT: 6379
```

### 4.3 Nginx 配置

Nginx 配置位于 `frontend/nginx.conf`，核心配置：

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 监听端口 | 80 | 容器内部端口 |
| 静态资源缓存 | 1 年 | js/css/图片等 |
| Gzip 压缩 | 开启 | 压缩级别 6 |
| API 代理 | `proxy_pass http://backend:8080` | 转发到后端容器 |
| SPA 路由 | `try_files $uri /index.html` | Vue Router History 模式 |
| 最大上传 | 20MB | `client_max_body_size` |
| 超时设置 | 60s | connect/send/read |

### 4.4 前端配置 (`vite.config.ts`)

```typescript
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

---

## 5. 构建产物说明

### 5.1 后端 Dockerfile

```
阶段 1：构建
  基础镜像: maven:3.9-eclipse-temurin-17
  构建命令: mvn clean package -DskipTests
  产物: target/student-management-system-1.0.0-SNAPSHOT.jar

阶段 2：运行
  基础镜像: eclipse-temurin:17-jre-alpine
  JVM 参数: -Xms512m -Xmx1024m -XX:+UseG1GC
  暴露端口: 8080
```

### 5.2 前端 Dockerfile

```
阶段 1：构建
  基础镜像: node:18-alpine
  构建命令: npm run build
  产物: dist/

阶段 2：运行
  基础镜像: nginx:alpine
  静态文件: /usr/share/nginx/html
  配置文件: nginx.conf → /etc/nginx/conf.d/default.conf
  暴露端口: 80
```

---

## 6. 数据库初始化

### 6.1 初始化脚本

位于 `docker/mysql/init/` 目录：

| 文件 | 说明 |
|------|------|
| `01-schema.sql` | 建表脚本（23 张表） |
| `02-data.sql` | 初始数据（角色、权限、默认用户、系统配置、通知模板等） |

Docker 首次启动时自动按文件名排序执行。

### 6.2 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 拥有所有权限 |
| 教务处 | academic | 123456 | 教务管理权限 |
| 辅导员 | counselor01 | 123456 | 学生管理 + 审批 |
| 院系领导 | department01 | 123456 | 二级审批 |
| 教师 | teacher01 | 123456 | 成绩录入 |
| 学生 | student01 | 123456 | 基本操作 |

> 所有测试账号密码均为 `123456`，生产环境部署后请务必修改。

---

## 7. 生产环境注意事项

### 7.1 安全加固

1. **修改 JWT Secret**：替换 `application.yml` 中的 `jwt.secret` 为高强度随机字符串（≥256 位）
2. **修改数据库密码**：不要使用默认的 `root123`
3. **修改默认账号密码**：首次登录后立即修改所有默认账号密码
4. **HTTPS**：在 Nginx 层配置 SSL 证书，启用 HTTPS
5. **防火墙**：仅开放 80/443 端口，关闭 3306/6379/8080 直接访问

### 7.2 性能优化

1. **JVM 调优**：根据服务器内存调整 `-Xms` / `-Xmx` 参数
2. **MySQL 调优**：调整 `innodb_buffer_pool_size`、`max_connections` 等参数
3. **Redis 配置**：根据需要配置持久化策略和内存限制
4. **Nginx 缓存**：静态资源已配置 1 年强缓存 + Gzip 压缩

### 7.3 备份策略

1. **数据库备份**：
   - 系统内置备份功能：管理员可在「数据管理」页面创建和下载备份
   - 建议额外配置定时 `mysqldump` 全量备份
   - Docker 环境备份文件存储在 `backend_backups` 卷中

2. **文件备份**：
   - 上传文件存储在 `backend_uploads` 卷中
   - 建议定期备份该卷内容

### 7.4 日志管理

- 后端日志通过 `docker-compose logs` 查看
- 审计日志存储在 `sys_audit_log` 表中
- 建议配置日志轮转和持久化存储

### 7.5 端口映射参考

| 环境 | 前端 | 后端 API | MySQL | Redis |
|------|------|---------|-------|-------|
| Docker 默认 | 8081 | 8080 | 3306 | 6379 |
| 本地开发 | 3000 | 8080 | 3306 | 6379 |
| 生产建议 | 80/443 | 不暴露 | 不暴露 | 不暴露 |

---

## 8. 常见问题

### Q1：Docker 启动后无法访问前端页面

- 检查端口是否被占用：`netstat -tlnp | grep 8081`
- 查看容器日志：`docker-compose logs frontend`
- 确认后端已完全启动：`docker-compose logs backend | grep "Started"`

### Q2：数据库连接失败

- 检查 MySQL 容器是否健康：`docker-compose ps`
- 确认数据库初始化完成：`docker exec -it sms-mysql mysql -uroot -proot123 -e "SHOW DATABASES;"`
- 本地开发需确认 `application.yml` 中的连接信息

### Q3：前端请求 API 返回 502

- 后端可能还未启动完成，等待 30-60 秒
- 检查后端日志：`docker-compose logs backend`

### Q4：修改代码后如何重新部署

```bash
# 重新构建并启动（保留数据）
docker-compose up -d --build

# 仅重建某个服务
docker-compose up -d --build backend
```

### Q5：如何重置数据库

```bash
# 停止并删除数据卷
docker-compose down -v

# 重新启动（自动重新初始化）
docker-compose up -d --build
```
