# 高校学生信息管理系统

基于 Vue 3 + Spring Boot 3 的高校学生信息管理系统，支持学生信息管理、选课管理、成绩管理、请销假管理等核心功能。

## 文档导航

| 文档 | 说明 |
|------|------|
| [系统架构文档](docs/ARCHITECTURE.md) | 技术栈、分层架构、安全设计、核心业务流程、前后端架构详解 |
| [API 接口文档](docs/API.md) | 全部 120+ 个 REST API 接口详细说明，含请求/响应示例 |
| [部署文档](docs/DEPLOYMENT.md) | Docker 一键部署、本地开发环境搭建、配置说明、生产环境注意事项 |
| [数据库设计文档](docs/DATABASE.md) | 25 张表结构详解、ER 关系图、索引设计、枚举值定义、业务规则 |

## How to run

使用 Docker Compose 一键启动所有服务：

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看服务运行状态
docker-compose ps

# 查看服务日志
docker-compose logs -f

# 停止所有服务
docker-compose down

# 停止并删除数据卷（会清除数据库数据）
docker-compose down -v
```

## Services

| 服务名称 | 容器名称 | 描述 | 端口映射 | 访问地址 | 依赖 |
|---------|---------|------|---------|---------|------|
| **frontend** | sms-frontend | Vue 3 前端应用 (Nginx) | 8081:80 | http://localhost:8081 | backend |
| **backend** | sms-backend | Spring Boot 后端 API | 8080:8080 | http://localhost:8080/api | mysql, redis |
| **mysql** | sms-mysql | MySQL 8.0 数据库 | 3306:3306 | localhost:3306 | - |
| **redis** | sms-redis | Redis 7 缓存 | 6379:6379 | localhost:6379 | - |


## 题目内容
详细功能需求 
3.1 学生信息管理模块 
3.1.1 基本信息维护 
功能描述 ：管理学生基础信息 
用户角色 ：学生、辅导员、教务处 
具体需求 ： 
学生可查看个人基本信息 
学生可修改部分非关键信息（如联系方式） 
辅导员可维护所辖学生信息 
教务处具有完整信息管理权限 
支持信息导入/导出功能 
信息变更历史记录 
3.1.2 学籍状态管理 
学籍状态跟踪（在读、休学、毕业等） 
学籍异动申请与审批 
学籍证明材料管理 
3.2 选课管理模块 
3.2.1 课程信息展示 
课程目录浏览与搜索 
课程详情查看（课程描述、教师信息、时间地点、容量等） 
课程冲突检测 
先修课程要求验证 
3.2.2 选课操作 
预选课与正式选课阶段管理 
选课申请提交与撤销 
选课结果实时反馈 
选课抽签机制（如需） 
补选/退选流程 
3.2.3 选课结果管理 
个人课表生成与展示 
班级名单生成 
选课统计分析 
3.3 成绩管理模块 
3.3.1 成绩录入与维护 
功能描述 ：教师录入和管理学生成绩 
用户角色 ：授课教师、教务处 
具体需求 ： 
按课程批量录入成绩 
单个学生成绩调整 
成绩审核与确认流程 
成绩修改记录追踪 
成绩分布统计分析 
3.3.2 成绩查询与统计 
学生查询个人成绩 
多维度成绩查询（按学期、课程类型等） 
GPA自动计算 
成绩单在线生成 
成绩预警机制（针对不及格科目） 
3.4 请销假管理模块 
3.4.1 请假申请 
在线填写请假申请 
请假类型定义（病假、事假、公假等） 
请假材料附件上传 
请假时长自动计算 
3.4.2 审批流程 
多级审批配置（辅导员、院系领导等） 
审批进度实时查看 
审批意见填写 
紧急请假快速通道 
3.4.3 销假管理 
返校销假登记 
请假记录归档 
请假统计与分析 
缺勤情况自动关联 
3.5 系统管理模块 
3.5.1 用户权限管理 
基于角色的访问控制 
权限细粒度配置 
用户登录审计 
3.5.2 数据管理 
数据备份与恢复 
数据导入导出 
数据字典维护 
3.5.3 系统配置 
学年学期设置 
选课时间配置 
通知模板管理 
4. 非功能性需求 
4.1 性能需求 
系统响应时间：普通操作<2秒，复杂查询<5秒 
并发支持：支持500用户同时在线 
数据容量：支持10万级学生数据存储 
4.2 可用性需求 
7×24小时可用，计划维护时间除外 
年可用性不低于99.5% 
友好的用户界面，符合操作习惯 
4.3 安全性需求 
用户身份认证与授权 
数据传输加密 
敏感数据脱敏处理 
操作日志完整记录 
防止SQL注入等常见攻击 
4.4 兼容性需求 
支持主流浏览器（Chrome、Firefox、Edge等） 
支持移动端访问 
支持与现有教务系统数据对接 
4.5 可维护性需求 
模块化设计，便于功能扩展 
详细的系统文档 
完善的错误处理机制 
5. 数据需求 
5.1 数据实体 
学生信息实体 
课程信息实体 
成绩记录实体 
请假记录实体 
用户账户实体 
5.2 数据关系 
学生与课程的多对多关系 
学生与成绩的一对多关系 
学生与请假的一对多关系 
5.3 数据保留策略 
学生毕业数据保留至少10年 
操作日志保留至少3年 
6. 界面需求 
6.1 总体要求 
界面风格统一，符合教育系统特点 
操作流程直观，减少用户学习成本 
关键操作提供确认提示 
6.2 响应式设计 
适配桌面端、平板和手机 
重要功能在移动端完整可用 
前端使用Vue3技术，后端使用Spring Boot技术实现


## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.0
- Spring Security + JWT
- MyBatis-Plus 3.5.5
- MySQL 8.0

### 前端
- Vue 3.4
- Vite 5
- Element Plus 2.4
- Pinia 2.1
- Vue Router 4
- Axios
- TypeScript
- Scss

## 项目结构

```
├── backend/                    # 后端项目
│   ├── src/main/java/com/university/sms/
│   │   ├── common/            # 公共组件（响应封装、异常处理、工具类）
│   │   ├── security/          # 安全模块（JWT、权限控制）
│   │   ├── student/           # 学生信息模块
│   │   ├── course/            # 选课管理模块
│   │   ├── grade/             # 成绩管理模块
│   │   ├── leave/             # 请销假模块
│   │   └── system/            # 系统管理模块
│   └── src/main/resources/
│       ├── db/schema.sql      # 数据库建表脚本
│       ├── db/data.sql        # 初始化数据
│       └── application.yml    # 配置文件
│
├── frontend/                   # 前端项目
│   └── src/
│       ├── api/               # API 接口定义
│       ├── stores/            # Pinia 状态管理
│       ├── views/             # 页面组件
│       ├── components/        # 公共组件
│       ├── router/            # 路由配置
│       ├── utils/             # 工具函数
│       └── styles/            # 全局样式
```

## 功能模块

### 1. 用户认证与权限
- JWT Token 认证
- 登录密码前端 RSA-OAEP 加密传输（公钥由后端下发）
- RBAC 角色权限控制
- 登录失败锁定机制
- 六种用户角色：学生、教师、辅导员、院系领导、教务处、管理员

### 2. 学生信息管理
- 学生基本信息查看与修改
- 敏感信息脱敏显示
- 辅导员学生管理
- 教务处完整信息管理
- 学籍状态管理
- 数据导入/导出

### 3. 选课管理
- 课程目录浏览与搜索
- 选课冲突检测
- 先修课程验证
- 选课容量控制
- 选课抽签机制
- 个人课表生成

### 4. 成绩管理
- 教师批量录入成绩
- 成绩等级自动转换
- GPA 自动计算
- 成绩审批流程
- 成绩统计分析
- 不及格预警

#### 成绩预警机制实现细节（不及格科目）

当前“成绩预警”采用**实时查询 + 前端提示**模式，核心逻辑如下：

- **预警判定规则**
  - 判定条件：学生任一课程存在 `score < 60` 即判定为“存在预警”。
  - 当前实现不区分成绩状态（草稿、已提交、已审批），只要有不及格分数即返回预警。

- **后端实现**
  - 接口：`GET /api/grades/my/failing`
  - 控制层：`GradeController.hasMyFailingGrades`
    - 从请求上下文读取当前登录用户 `userId`
    - 通过 `StudentService.getByUserId` 映射到学生ID
    - 调用 `GradeService.hasFailingGrades(studentId)`
  - 服务层：`GradeServiceImpl.hasFailingGrades`
    - 查询该学生全部成绩
    - 通过流式判断是否存在 `score != null && score < 60`
    - 返回布尔值给前端（`true/false`）

- **前端实现**
  - 页面：`frontend/src/views/grade/GradeQueryView.vue`
  - 页面加载时并行请求：
    - 成绩列表：`GET /api/grades/my`
    - GPA 信息：`GET /api/grades/gpa`
    - 预警状态：`GET /api/grades/my/failing`
  - 当 `hasFailingGrades = true` 时显示 `el-alert` 警告条：
    - “存在不及格课程，请及时关注学习情况，并与任课教师或辅导员沟通。”

- **触发与更新时机**
  - 学生成绩查询页初次进入时触发一次。
  - 切换学期筛选后会重新调用 `loadData()`，同步刷新预警状态。

- **当前边界与可优化点**
  - 当前返回“是否存在预警”，不返回具体不及格课程清单（课程名、学期、分数）。
  - 未按严重等级分层（如单科不及格/多科不及格/GPA 连续下滑）。
  - 可扩展为“预警详情接口 + 消息通知（站内信/短信/邮件）+ 学业辅导闭环跟踪”。

### 5. 请销假管理
- 在线请假申请
- 多级审批流程
- 紧急请假通道
- 销假登记
- 请假统计

### 6. 系统管理
- 用户管理
- 角色权限配置
- 系统配置
- 审计日志
- 数据备份与恢复
- 数据字典维护

### 7. 通知管理
- 通知模板管理（CRUD + 变量占位符）
- 通知发送（基于模板或自定义内容）
- 目标选择（全体 / 按角色 / 指定用户）
- 我的通知（未读数量、标记已读）

### 8. 学籍管理
- 学籍异动申请与审批（休学、退学、转学等）
- 学籍证明材料管理（在读证明、毕业证明等）
- 证明编号验证与作废

## 与现有教务系统数据对接说明

针对“支持与现有教务系统数据对接”的需求，项目采用 **REST API + 文件导入/导出** 的方式实现数据交换，说明如下：

- **对接模式**
  - 在线对接：通过标准 HTTP API 进行系统间数据同步
  - 离线交换：通过 CSV/备份文件导入导出进行批量交换

- **已提供的数据交换接口（示例）**
  - 学生数据导入：`POST /api/students/import`
  - 学生数据导出：`GET /api/students/export`
  - 成绩单导出：`GET /api/grades/transcript/export`
  - 系统数据导出：`GET /api/system/data/export/{type}`（`student/course/grade/leave`）
  - 数据备份下载：`GET /api/system/data/backup/download/{fileName}`
  - 数据恢复：`POST /api/system/data/backup/restore/{fileName}`（后台异步恢复）

- **推荐对接流程**
  - 教务系统通过定时任务调用导出接口，拉取增量或全量数据
  - 对方系统按约定字段映射后调用导入接口写入本系统
  - 对关键业务（学籍异动、成绩）增加“对账任务”，比对记录数与关键字段哈希
  - 对失败记录保留重试队列与错误日志，避免脏数据进入生产库

- **接口约定建议（落地时）**
  - 统一鉴权：JWT 或服务间签名（建议增加 `appId + timestamp + sign`）
  - 幂等控制：导入接口支持业务主键去重（如学号、课程号、成绩唯一键）
  - 版本管理：接口增加版本前缀（如 `/api/v1/...`），确保兼容升级
  - 安全要求：全链路 HTTPS，敏感字段脱敏与审计日志留痕

> 说明：当前仓库已具备“导入/导出/备份恢复”能力，可满足与既有教务系统的数据交换基础场景；如需实时双向同步，建议再增加消息队列或 CDC 方案。

## 快速开始

### 方式一：Docker 一键启动（推荐）

确保已安装 Docker 和 Docker Compose，然后执行：

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

服务启动后：
- 前端访问：http://localhost:8081
- 后端 API：http://localhost:8080/api
- MySQL：localhost:3306
- Redis：localhost:6379

数据库会在首次启动时自动初始化（建表 + 导入初始数据）。

停止服务：
```bash
docker-compose down

# 停止并删除数据卷（清空数据）
docker-compose down -v
```

### 方式二：本地开发环境

#### 1. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE sms_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行建表脚本
source backend/src/main/resources/db/schema.sql

-- 导入初始数据
source backend/src/main/resources/db/data.sql
```

#### 2. 后端启动

```bash
cd backend

# 修改数据库配置
# 编辑 src/main/resources/application.yml

# 启动项目
mvn spring-boot:run
```

后端服务默认运行在 http://localhost:8080

#### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务默认运行在 http://localhost:5173

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 教务处 | academic | 123456 |
| 辅导员 | counselor01 | 123456 |
| 教师 | teacher01 | 123456 |
| 学生 | student01 | 123456 |

> 注：所有测试账号密码均为 `123456`

## 后端测试与覆盖情况

当前后端已包含基础自动化测试，补充说明如下：

- **测试框架**：JUnit 5（`spring-boot-starter-test`）、`spring-security-test`、`jqwik`
- **测试层次**：`Controller` 层测试、`Service` 层测试、公共组件与异常处理测试
- **测试文件数量**：`backend/src/test` 下共 6 个测试类
- **已覆盖测试类**：
  - `SmsApplicationTests`（应用启动上下文）
  - `common/ResultAndExceptionTest`（统一响应与异常处理）
  - `grade/controller/GradeControllerTest`（成绩接口层）
  - `grade/service/impl/GradeServiceImplTest`（成绩业务层）
  - `course/service/impl/CourseSelectionServiceImplTest`（选课业务层）
  - `system/service/impl/NotificationServiceImplTest`（通知业务层）

- **模块覆盖现状（功能维度）**
  - 已有：`common`、`grade`、`course`、`system`（部分核心流程）
  - 相对薄弱：`student`、`leave`、`security`（认证/鉴权异常分支）、`system` 部分管理接口

- **覆盖率统计现状**
  - 当前 `pom.xml` 尚未集成 JaCoCo 插件，暂未产出可量化的行覆盖率/分支覆盖率报表。
  - 因此目前可给出“测试类与覆盖模块范围”，但无法给出精确覆盖率百分比。

- **执行方式**：

```bash
cd backend
mvn test
```

- **建议补齐方式（覆盖率）**
  - 接入 `jacoco-maven-plugin`，生成 `target/site/jacoco/index.html`
  - 在 CI 中增加覆盖率阈值门禁（如：行覆盖率 >= 70%，分支覆盖率 >= 60%）
  - 每次迭代在 README/测试报告中更新覆盖率趋势（新增测试数、提升幅度）



## 定期备份策略

当前系统已提供“手动备份/恢复”功能（系统管理 -> 数据管理），为满足长期运维要求，建议采用以下定期备份策略：

- **备份对象**
  - MySQL 业务数据库（核心）
  - 上传文件目录（如 `/app/uploads`，按业务需要）
  - 关键配置（部署配置、环境变量模板、初始化脚本）

- **备份频率（建议）**
  - 每日增量备份：凌晨低峰时段（如 `02:00`）
  - 每周全量备份：每周日凌晨（如 `03:00`）
  - 关键变更前备份：系统升级、数据迁移、批量导入前执行一次临时全量备份

- **保留与清理策略（建议）**
  - 日备份保留 7-14 天
  - 周备份保留 8-12 周
  - 月备份保留 6-12 个月
  - 采用自动清理策略，避免备份文件无限增长占满磁盘

- **存储与容灾（建议）**
  - 本地磁盘 + 异地/对象存储双副本（至少 2 份）
  - 备份文件建议开启压缩与加密存储
  - 备份目录需限制访问权限，仅运维/管理员可读写

- **恢复演练（建议）**
  - 至少每月进行一次恢复演练（在测试环境）
  - 演练内容包括：全量恢复、最近增量回放、数据一致性抽查
  - 记录 RPO/RTO 指标，持续优化恢复时长

- **自动化执行方式（推荐）**
  - 使用 `cron` 或 CI 定时任务调用备份脚本（`mysqldump` 或平台化备份工具）
  - 备份任务执行后推送结果通知（成功/失败/耗时/文件大小）
  - 失败自动重试，并在连续失败时触发告警

> 说明：当前仓库默认提供的是应用内手动备份能力；“定时自动备份、异地存储、备份告警”属于运维增强项，建议在生产部署时按上述策略落地。

## 数据保留策略实现说明（毕业数据 10 年 / 操作日志 3 年）

针对需求中“学生毕业数据保留至少 10 年、操作日志保留至少 3 年”，当前建议的落地方式如下：

- **保留对象与期限**
  - 学生毕业相关数据（学生主档、成绩单、学籍异动结论等）：`>= 10 年`
  - 操作审计日志（登录、权限变更、关键业务操作）：`>= 3 年`

- **实现机制（推荐）**
  - 数据库层：在相关表保留 `created_at / updated_at / archived_at` 时间字段，作为生命周期依据。
  - 应用层：通过定时任务（如 Spring `@Scheduled`）执行“到期归档、保留校验、清理”流程。
  - 存储层：冷热分离（在线库保留近期数据，历史库/对象存储保留长期数据）。

- **毕业数据 10 年策略（建议）**
  - 毕业后数据标记为“历史数据”，进入归档队列，但不得在 10 年内物理删除。
  - 超过 10 年后可按合规策略脱敏或删除（需管理员审批 + 审计记录）。
  - 成绩单等关键凭证建议生成不可变归档文件（HTML/PDF）并保留校验信息（哈希/签名）。

- **操作日志 3 年策略（建议）**
  - 审计日志按月分区或分表（便于检索与清理）。
  - 近 3 年保留在线可检索；超过 3 年转离线归档或按制度删除。
  - 所有删除/归档动作必须再写一条“保留策略执行日志”（谁、何时、处理了多少条）。

- **执行与校验**
  - 每日低峰执行保留策略任务，并输出任务报告（处理条数、失败条数、耗时）。
  - 每月执行一次抽样核查，确认“应保留数据未误删、应清理数据已按规则处理”。
  - 结合备份策略，确保清理前有可回溯备份。

> 现状说明：当前仓库已具备审计日志、数据导出与备份能力；“按 10 年/3 年自动归档与清理”的定时任务和历史库策略属于运维增强项，建议在生产环境按上述方案落地。

## 性能测试与验收验证

针对需求中“普通操作 < 2 秒、复杂查询 < 5 秒、支持 500 用户同时在线、支持 10 万级学生数据存储”，建议采用以下测试与验收方式：

- **测试目标与验收口径**
  - 普通操作接口（如列表查询、详情查询、保存操作）：`P95 < 2s`
  - 复杂查询接口（如统计、成绩单生成、多条件分页）：`P95 < 5s`
  - 并发能力：持续并发 500 虚拟用户下系统可用，无大面积 5xx/超时
  - 数据规模：数据库至少准备 10 万级学生数据及关联课程/成绩数据

- **推荐测试工具**
  - 压测工具：JMeter / k6 / Locust（三选一即可）
  - 监控工具：Prometheus + Grafana（观察 CPU、内存、QPS、错误率、响应时间）
  - 日志分析：结合应用日志与 Nginx/MySQL 日志定位慢点

- **测试场景设计（建议）**
  - 登录与鉴权链路：登录、刷新 token、鉴权访问
  - 高频读接口：学生列表、课程列表、成绩查询
  - 写操作接口：成绩录入、请假申请、审批流程
  - 复杂查询接口：成绩统计、成绩单导出、多维筛选分页
  - 混合读写场景：读写比例 7:3 或 8:2，模拟真实教学业务高峰

- **测试数据准备（10 万级）**
  - 通过 SQL 脚本或批量导入接口生成 10 万学生基础数据
  - 关联生成课程选课与成绩记录，避免“只有学生主档、无业务关联”的失真数据集
  - 对常用查询字段建立索引（学号、课程号、学期、状态等）后再进行基准压测

- **执行步骤（建议）**
  - 步骤 1：单接口基准压测（确认瓶颈接口）
  - 步骤 2：逐步升压（100 -> 200 -> 300 -> 500 并发）
  - 步骤 3：稳定性长压（30-60 分钟）
  - 步骤 4：记录报告（P50/P90/P95/P99、吞吐量、错误率、资源曲线）
  - 步骤 5：针对瓶颈优化后回归压测，形成前后对比报告

- **当前现状说明**
  - 当前仓库已给出性能目标，但尚未提交标准化压测报告（如并发 500 与 10 万数据规模下的指标证明）。
  - 建议在课程验收或上线前补齐《性能测试报告》（包含测试环境、数据规模、脚本、结果与优化结论）。

## 安全性测试与验证

项目已实现 JWT 认证、登录 RSA 加密传输、敏感信息脱敏、审计日志等安全能力。为避免“有功能无验证”，建议按下述清单执行安全测试并沉淀报告。

- **测试范围（对应已实现能力）**
  - 身份认证与会话安全：JWT 登录、刷新、过期、无效 token 处理
  - 传输与密码安全：登录公钥获取、RSA-OAEP 加密登录、后端解密校验
  - 权限控制安全：RBAC 权限拦截、越权访问拦截（401/403）
  - 数据安全：敏感字段脱敏展示、审计日志留痕完整性
  - 常见 Web 安全：SQL 注入、XSS、暴力破解、重放请求等

- **核心测试用例（建议）**
  - JWT：
    - 无 token 访问受保护接口应返回 401
    - 伪造/篡改 token 应被拒绝
    - 过期 token 触发刷新逻辑，刷新失败应强制重新登录
  - RSA 登录：
    - 正常公钥加密登录成功
    - 使用旧公钥/非法密文登录应失败，并返回明确错误
    - 明文密码直传应不通过接口契约校验（只接受 `encryptedPassword`）
  - 权限控制：
    - 低权限账户访问高权限接口返回 403
    - 管理员与普通角色接口能力差异符合权限矩阵
  - 脱敏与日志：
    - 手机号/邮箱在返回体中按规则脱敏
    - 关键操作（登录、权限拒绝、数据恢复等）有审计日志记录
    - 审计日志中不应出现明文密码、密钥、完整敏感字段

- **攻击模拟与工具（建议）**
  - 接口安全扫描：OWASP ZAP / Burp（被动+主动扫描）
  - SQL 注入验证：对分页筛选、搜索参数进行注入 payload 测试
  - XSS 验证：对输入型字段进行脚本注入与输出编码检查
  - 暴力破解：对登录接口做限频和失败锁定策略验证
  - 重放攻击：重复发送历史密文请求，验证时间窗/幂等/签名策略（如有）

- **验收指标（建议）**
  - 高危漏洞（SQL 注入、认证绕过、越权）为 0
  - 中危漏洞闭环率 100%（有修复或明确风险接受说明）
  - 安全测试报告包含：测试环境、用例清单、扫描结果、风险分级、整改结论

- **当前现状说明**
  - 当前仓库已实现安全机制，但尚未附带系统化《安全测试报告》。
  - 建议在课程验收或上线前补齐报告，并至少覆盖 JWT、RSA 登录、权限与脱敏四大主链路。

## API 接口

> 完整接口文档请参考 [API 接口文档](docs/API.md)，包含全部 120+ 个接口的详细说明。

### 认证接口 `/api/auth`
- GET `/api/auth/login/encryption-key` - 获取登录加密公钥（RSA-OAEP）
- POST `/api/auth/login` - 用户登录
- POST `/api/auth/logout` - 用户登出
- POST `/api/auth/refresh` - 刷新 Token
- GET `/api/auth/current` - 获取当前用户信息

> 登录请求说明：`/api/auth/login` 使用 `encryptedPassword` 字段（前端使用 `/api/auth/login/encryption-key` 返回的公钥进行 RSA-OAEP 加密后提交），不再直接传输明文密码。

### 学生接口 `/api/students`
- GET `/api/students/me` - 查询当前学生信息
- GET `/api/students/{id}` - 查询学生信息
- PUT `/api/students/{id}/contact` - 修改联系方式
- GET `/api/students` - 学生列表查询
- POST `/api/students/import` - 批量导入
- GET `/api/students/export` - 数据导出

### 学籍异动接口 `/api/status-changes`
- POST `/api/status-changes` - 提交学籍异动申请
- PUT `/api/status-changes/{id}/approve` - 审批
- GET `/api/status-changes/my` - 我的申请记录

### 学籍证明接口 `/api/certificates`
- POST `/api/certificates` - 申请证明
- GET `/api/certificates/verify/{certNo}` - 验证证明
- GET `/api/certificates/my` - 我的证明记录

### 课程接口 `/api/courses`
- GET `/api/courses` - 课程列表
- GET `/api/courses/{id}` - 课程详情
- POST `/api/courses` - 创建课程
- PUT `/api/courses/{id}` - 更新课程

### 选课接口 `/api/course-selections`
- POST `/api/course-selections` - 选课
- DELETE `/api/course-selections/{courseId}` - 退课
- GET `/api/course-selections/schedule` - 个人课表
- GET `/api/course-selections/phase/info` - 选课阶段信息
- POST `/api/course-selections/lottery/{courseId}` - 执行抽签

### 成绩接口 `/api/grades`
- GET `/api/grades/my` - 我的成绩
- GET `/api/grades/my/failing` - 不及格预警状态
- POST `/api/grades/batch` - 批量录入成绩
- GET `/api/grades/gpa` - GPA 查询
- GET `/api/grades/statistics/{courseId}` - 成绩统计
- GET `/api/grades/transcript` - 成绩单生成
- GET `/api/grades/transcript/export` - 成绩单导出

### 请假接口 `/api/leaves`
- POST `/api/leaves` - 提交请假申请
- GET `/api/leaves/{id}` - 请假详情
- PUT `/api/leaves/{id}/approve` - 审批请假
- PUT `/api/leaves/{id}/return` - 销假登记

### 通知接口 `/api/notifications`
- GET `/api/notifications/templates` - 模板列表
- POST `/api/notifications/send` - 发送通知
- GET `/api/notifications/my` - 我的通知
- GET `/api/notifications/my/unread-count` - 未读数量

### 系统管理接口
- GET `/api/system/users` - 用户管理
- GET `/api/system/roles` - 角色管理
- GET `/api/system/config` - 系统配置
- GET `/api/system/audit-logs` - 审计日志
- GET `/api/system/data/backup/restore-status` - 数据恢复任务状态
- GET `/api/dashboard/stats` - 首页统计

## 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sms_db
    username: root
    password: your_password

jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时
```

### 前端配置 (vite.config.ts)

```typescript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

## License

MIT License
