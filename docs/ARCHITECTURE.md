# 系统架构文档

## 1. 架构概览

本系统采用前后端分离的 B/S 架构，通过 Docker Compose 进行容器化部署。

```
┌─────────────┐      ┌───────────────────────────────────────────┐
│   Browser   │──────│  Nginx (sms-frontend :8081)               │
│   Client    │      │  ├─ 静态资源服务 (Vue 3 SPA)               │
└─────────────┘      │  └─ /api 反向代理 → backend:8080           │
                     └──────────────┬────────────────────────────┘
                                    │
                     ┌──────────────▼────────────────────────────┐
                     │  Spring Boot (sms-backend :8080)           │
                     │  ├─ Security Filter Chain (JWT)            │
                     │  ├─ Controller Layer (REST API)            │
                     │  ├─ Service Layer (Business Logic)         │
                     │  ├─ MyBatis-Plus (Data Access)             │
                     │  └─ AOP (Permission, Audit)                │
                     └──────┬───────────────────┬────────────────┘
                            │                   │
                ┌───────────▼──────┐  ┌─────────▼─────────┐
                │  MySQL 8.0       │  │  Redis 7           │
                │  (sms-mysql)     │  │  (sms-redis)       │
                │  :3306           │  │  :6379              │
                └──────────────────┘  └───────────────────┘
```

## 2. 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.2.0 | 应用框架 |
| Spring Security | 6.x | 认证授权 |
| JWT (jjwt) | 0.12.3 | 令牌认证 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0 | 关系数据库 |
| Redis | 7 | 缓存/会话 |
| Hutool | 5.8.24 | 工具库 |
| EasyExcel | 3.3.3 | Excel 导入导出 |
| Lombok | - | 代码简化 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4 | UI 框架 |
| Vite | 5.x | 构建工具 |
| TypeScript | - | 类型安全 |
| Element Plus | 2.6+ | UI 组件库 |
| Pinia | 2.1 | 状态管理 |
| Vue Router | 4.3 | 路由管理 |
| Axios | 1.6 | HTTP 客户端 |
| Scss | - | CSS 预处理 |

### 基础设施

| 技术 | 用途 |
|------|------|
| Docker / Docker Compose | 容器化部署 |
| Nginx | 前端静态服务 + 反向代理 |
| Maven | 后端构建工具 |
| npm | 前端包管理 |

## 3. 后端分层架构

```
com.university.sms
├── common/                        # 公共层
│   ├── response/                  #   统一响应封装 (Result, ResultCode)
│   ├── exception/                 #   全局异常处理 (BusinessException, GlobalExceptionHandler)
│   └── config/                    #   公共配置 (CORS, Jackson, MyBatis)
│
├── security/                      # 安全层
│   ├── config/SecurityConfig      #   Spring Security 配置
│   ├── filter/JwtAuthFilter       #   JWT 认证过滤器
│   ├── util/JwtUtil                #   JWT 工具类
│   ├── annotation/RequirePermission #  权限校验注解
│   ├── aspect/PermissionAspect    #   权限校验切面 (AOP)
│   └── service/                   #   认证服务 (AuthService, UserDetailsService)
│
├── student/                       # 学生信息模块
│   ├── controller/                #   StudentController, StatusChangeController, CertificateController
│   ├── service/                   #   StudentService, StatusChangeService, CertificateService
│   ├── mapper/                    #   MyBatis Mapper 接口
│   ├── entity/                    #   Student, StatusChangeApplication, CertificateRecord 等
│   └── dto/                       #   DTO 对象
│
├── course/                        # 选课管理模块
│   ├── controller/                #   CourseController, CourseSelectionController
│   ├── service/                   #   CourseService, CourseSelectionService
│   ├── mapper/                    #   CourseMapper, CourseSelectionMapper
│   ├── entity/                    #   Course, CourseSelection, CoursePrerequisite
│   ├── enums/                     #   SelectionStatus
│   └── dto/                       #   CourseDTO, SelectionPhaseDTO, LotteryCourseDTO 等
│
├── grade/                         # 成绩管理模块
│   ├── controller/                #   GradeController
│   ├── service/                   #   GradeService
│   ├── mapper/                    #   GradeMapper, GradeChangeLogMapper
│   ├── entity/                    #   Grade, GradeChangeLog
│   └── dto/                       #   GradeDTO, GPAInfoDTO, TranscriptDTO 等
│
├── leave/                         # 请销假模块
│   ├── controller/                #   LeaveController, FileController
│   ├── service/                   #   LeaveService
│   ├── mapper/                    #   LeaveMapper
│   ├── entity/                    #   LeaveRequest, LeaveApproval, LeaveAttachment
│   └── dto/                       #   LeaveDTO
│
└── system/                        # 系统管理模块
    ├── controller/                #   UserController, RoleController, SystemConfigController,
    │                              #   DataManageController, AuditLogController, DashboardController,
    │                              #   NotificationController
    ├── service/                   #   UserService, RoleService, SystemConfigService,
    │                              #   AuditLogService, DashboardService, NotificationService
    ├── mapper/                    #   各 Mapper 接口
    ├── entity/                    #   User, Role, SystemConfig, AuditLog,
    │                              #   NotificationTemplate, Notification, NotificationRead
    └── dto/                       #   各 DTO 对象
```

## 4. 安全架构

### 4.1 认证流程

```
Client                     Server
  │                          │
  │  GET /auth/login/encryption-key  │
  │─────────────────────────────────>│
  │  {algorithm, publicKey}         │
  │<─────────────────────────────────│
  │                          │
  │  POST /auth/login        │
  │  {username, encryptedPassword}    │
  │─────────────────────────>│
  │                          │  RSA 私钥解密密码并验证凭据
  │                          │  生成 JWT (accessToken + refreshToken)
  │  {token, refreshToken,   │
  │   userInfo, permissions} │
  │<─────────────────────────│
  │                          │
  │  GET /api/xxx            │
  │  Authorization: Bearer   │
  │  <accessToken>           │
  │─────────────────────────>│
  │                          │  JwtAuthFilter 解析 Token
  │                          │  设置 SecurityContext
  │                          │  PermissionAspect 检查权限
  │  Response                │
  │<─────────────────────────│
```

### 4.2 RBAC 权限模型

```
User ──(N:M)── Role ──(N:M)── Permission
```

**预置角色**：

| 角色 | 编码 | 权限范围 |
|------|------|---------|
| 管理员 | ADMIN | 所有权限 |
| 教务处 | ACADEMIC_AFFAIRS | 所有权限 |
| 辅导员 | COUNSELOR | 学生管理 + 请假审批 + 学籍异动审批 |
| 院系领导 | DEPARTMENT_HEAD | 请假二级审批 |
| 教师 | TEACHER | 成绩录入 + 课程查看 |
| 学生 | STUDENT | 个人信息 + 选课 + 成绩查询 + 请假 |

### 4.3 JWT 配置

| 参数 | 值 | 说明 |
|------|-----|------|
| 算法 | HS256 | HMAC-SHA256 签名 |
| accessToken 有效期 | 24 小时 | 正常访问令牌 |
| refreshToken 有效期 | 7 天 | 刷新令牌 |
| 请求头 | `Authorization: Bearer <token>` | 标准 Bearer 方案 |

### 4.4 公开接口白名单

- `GET /api/auth/login/encryption-key`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `/api/error`
- `/api/swagger-ui/**`
- `/api/v3/api-docs/**`

## 5. 前端架构

```
frontend/src/
├── api/                    # API 接口层（按模块拆分）
│   ├── request.ts          #   Axios 封装（拦截器、Token 注入、错误处理）
│   ├── types.ts            #   公共类型定义
│   ├── auth.ts             #   认证 API
│   ├── student.ts          #   学生 API
│   ├── course.ts           #   选课 API
│   ├── grade.ts            #   成绩 API
│   ├── leave.ts            #   请假 API
│   ├── system.ts           #   系统管理 API
│   └── notification.ts     #   通知 API
│
├── stores/                 # Pinia 状态管理
│   └── auth.ts             #   认证状态（Token、用户信息、权限）
│
├── router/                 # Vue Router
│   └── index.ts            #   路由定义 + 导航守卫（权限检查）
│
├── views/                  # 页面组件
│   ├── auth/               #   登录页
│   ├── dashboard/          #   首页仪表盘
│   ├── student/            #   学生模块（信息、列表、学籍异动、证明）
│   ├── course/             #   选课模块（课程列表、课表、抽签管理）
│   ├── grade/              #   成绩模块（查询、录入）
│   ├── leave/              #   请假模块（申请、记录、审批）
│   ├── notification/       #   通知模块（我的通知）
│   ├── system/             #   系统管理（用户、角色、配置、数据、审计、模板、通知发送）
│   ├── layout/             #   主布局（侧边栏菜单 + 顶部导航）
│   └── error/              #   404 页面
│
├── styles/                 # 全局样式
│   └── variables.scss      #   SCSS 变量
│
└── main.ts                 # 应用入口（Vue + Element Plus + Icons 全局注册）
```

### 前端路由守卫流程

```
路由跳转
  │
  ├─ 无需认证? ──> 放行
  │
  ├─ 未登录? ──> 重定向 /login
  │
  └─ 检查权限
      ├─ ADMIN/ACADEMIC_AFFAIRS ──> 放行（全部权限）
      ├─ 路由 meta.permissions 匹配 ──> 放行
      └─ 不匹配 ──> 提示"无权限"并阻止
```

## 6. 核心业务流程

### 6.1 选课流程

```
学生发起选课
  │
  ├─ 检查选课阶段（预选/正选/补退选）
  ├─ 检查课程状态（是否开放）
  ├─ 检查是否重复选课
  ├─ 检查时间冲突
  ├─ 检查先修课程
  │
  ├─ 预选阶段 + 启用抽签
  │   ├─ 未超容量 → 直接选中 (SELECTED)
  │   └─ 超容量 → 进入待抽签 (LOTTERY_PENDING)
  │       └─ 管理员执行抽签 → SELECTED / LOTTERY_FAILED
  │
  └─ 正选/补退选阶段
      ├─ 有容量 → 选中 (SELECTED)
      └─ 已满 → 失败
```

### 6.2 成绩管理流程

```
教师录入成绩 (DRAFT)
  │
  ├─ 提交审核 (SUBMITTED)
  │
  └─ 教务处审批 (APPROVED)
      │
      ├─ GPA 自动计算（按学分加权）
      ├─ 成绩单生成（HTML 正式文档）
      └─ 成绩修改 → 记录变更日志 → 重置为 DRAFT
```

### 6.3 学籍异动审批流程

```
学生提交申请 (PENDING)
  │
  ├─ 辅导员审批
  │   ├─ 通过 (COUNSELOR_APPROVED) → 教务处审批
  │   └─ 拒绝 (REJECTED)
  │
  └─ 教务处审批
      ├─ 通过 (APPROVED) → 自动更新学籍状态
      └─ 拒绝 (REJECTED)
```

### 6.4 请假审批流程

```
学生提交请假 (PENDING)
  │
  ├─ ≤3天：辅导员审批
  │   ├─ 通过 (APPROVED)
  │   └─ 拒绝 (REJECTED)
  │
  └─ >3天：辅导员 → 院系领导 两级审批
      ├─ 辅导员通过 (COUNSELOR_APPROVED) → 院系领导审批
      └─ 最终 APPROVED / REJECTED
  
  审批通过后 → 学生销假 (COMPLETED)
```

## 7. 数据流

### 请求生命周期

```
HTTP Request
  │
  ├─ Nginx (前端静态 / API 反向代理)
  │
  ├─ Spring Security Filter Chain
  │   ├─ JwtAuthenticationFilter (Token 解析)
  │   └─ 设置 SecurityContext (userId, username, roles)
  │
  ├─ Controller (参数校验、DTO 绑定)
  │   └─ @RequirePermission → PermissionAspect (AOP 权限检查)
  │
  ├─ Service (业务逻辑、事务管理)
  │
  ├─ Mapper (MyBatis-Plus → SQL 执行)
  │
  └─ Response → Result<T> 统一封装 → JSON
```

## 8. 测试架构

```
src/test/java/
├── SmsApplicationTests.java                    # Spring 上下文加载测试
├── common/ResultAndExceptionTest.java          # Result 响应 + 异常 + DTO 测试
├── grade/
│   ├── service/impl/GradeServiceImplTest.java  # GPA计算、成绩录入、统计、成绩单 
│   └── controller/GradeControllerTest.java     # Controller 层单元测试
├── course/
│   └── service/impl/CourseSelectionServiceImplTest.java  # 选课/退课/抽签
└── system/
    └── service/impl/NotificationServiceImplTest.java     # 通知模板/发送/已读
```

- **单元测试**：Mockito 隔离依赖，JUnit 5 + AssertJ 断言
- **参数化测试**：@ParameterizedTest 覆盖边界值
- **无外部依赖**：不需要数据库或 Redis 即可运行
