# 数据库设计文档

## 1. 概述

- **数据库**：MySQL 8.0
- **字符集**：utf8mb4 / utf8mb4_unicode_ci
- **引擎**：InnoDB
- **表总数**：25 张
- **命名规范**：模块前缀 + 下划线分隔（`sys_`、`stu_`、`crs_`、`grd_`、`lev_`）
- **通用字段**：`id`（自增主键）、`created_at`、`updated_at`、`deleted`（逻辑删除）

## 2. ER 关系图

```
┌──────────────────┐
│    sys_user       │◄──────────────────────────────┐
│  (用户表)         │                                │
└──────┬───────────┘                                │
       │ 1:N                                        │
       ▼                                            │
┌──────────────────┐     ┌──────────────────┐       │
│  sys_user_role    │────▶│    sys_role       │       │
│  (用户角色关联)    │     │  (角色表)         │       │
└──────────────────┘     └──────┬───────────┘       │
                                │ 1:N               │
                                ▼                   │
                         ┌──────────────────┐       │
                         │sys_role_permission│       │
                         │ (角色权限关联)     │       │
                         └──────┬───────────┘       │
                                │ N:1               │
                                ▼                   │
                         ┌──────────────────┐       │
                         │  sys_permission   │       │
                         │  (权限表)         │       │
                         └──────────────────┘       │
                                                    │
┌──────────────────┐                                │
│   stu_student     │◄─ user_id ───────────────────┘
│  (学生信息表)     │◄─ counselor_id ──── sys_user
└──┬───┬───┬───────┘
   │   │   │
   │   │   │ 1:N  ┌─────────────────────────────┐
   │   │   └─────▶│stu_status_change_application │
   │   │          │    (学籍异动申请)              │
   │   │          └──────────┬──────────────────┘
   │   │                     │ 1:N
   │   │                     ▼
   │   │          ┌─────────────────────────────┐
   │   │          │ stu_status_change_approval    │
   │   │          │   (学籍异动审批记录)           │
   │   │          └──────────────────────────────┘
   │   │
   │   │ 1:N  ┌──────────────────────┐
   │   └─────▶│stu_certificate_record │
   │          │  (学籍证明记录)        │
   │          └──────────────────────┘
   │
   │ 1:N  ┌────────────────────┐
   ├─────▶│stu_student_change_log│
   │      │  (学生信息变更日志)   │
   │      └────────────────────┘
   │
   │                    ┌──────────────────┐
   │ N:M (通过选课表)    │   crs_course      │
   ├───────────────────▶│  (课程信息表)      │
   │                    └──┬───────────────┘
   │                       │
   │  ┌────────────────────┤
   │  │ 1:N                │ 1:N
   │  ▼                    ▼
   │ ┌───────────────┐  ┌───────────────────────┐
   │ │crs_course_    │  │crs_course_prerequisite │
   │ │  selection     │  │   (先修课程)            │
   │ │ (选课记录)     │  └───────────────────────┘
   │ └───────────────┘
   │
   │ 1:N  ┌──────────────┐
   ├─────▶│  grd_grade    │◄── course_id ── crs_course
   │      │  (成绩表)     │
   │      └──────┬───────┘
   │             │ 1:N
   │             ▼
   │      ┌──────────────────────┐
   │      │ grd_grade_change_log  │
   │      │  (成绩变更日志)       │
   │      └──────────────────────┘
   │
   │ 1:N  ┌───────────────────┐
   └─────▶│ lev_leave_request  │
          │  (请假申请表)       │
          └──┬────────────────┘
             │ 1:N         │ 1:N
             ▼             ▼
    ┌─────────────────┐  ┌────────────────────┐
    │lev_leave_approval│  │lev_leave_attachment │
    │ (请假审批记录)    │  │  (请假附件)         │
    └─────────────────┘  └────────────────────┘

独立表:
  sys_config               系统配置表
  sys_audit_log            审计日志表
  sys_dict_type            数据字典类型表
  sys_dict_item            数据字典项表
  sys_notification_template 通知模板表
  sys_notification         通知记录表
  sys_notification_read    通知阅读状态表
```

## 3. 表结构详细说明

---

### 3.1 用户权限模块（sys_）

#### sys_user — 系统用户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| password | VARCHAR(100) | NOT NULL | 密码（BCrypt 加密） |
| real_name | VARCHAR(50) | NOT NULL | 真实姓名 |
| phone | VARCHAR(20) | | 手机号 |
| email | VARCHAR(100) | | 邮箱 |
| avatar | VARCHAR(255) | | 头像 URL |
| status | TINYINT | NOT NULL, DEFAULT 1 | 0-禁用, 1-启用 |
| login_fail_count | INT | NOT NULL, DEFAULT 0 | 连续登录失败次数 |
| lock_time | DATETIME | | 账户锁定时间 |
| last_login_time | DATETIME | | 最后登录时间 |
| last_login_ip | VARCHAR(50) | | 最后登录 IP |
| created_at | DATETIME | NOT NULL, DEFAULT NOW | 创建时间 |
| updated_at | DATETIME | NOT NULL, AUTO UPDATE | 更新时间 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除标记 |

**索引**：`uk_username`、`idx_status`、`idx_phone`、`idx_email`

#### sys_role — 系统角色表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 主键 |
| role_code | VARCHAR(50) | NOT NULL, UNIQUE | 角色编码（ADMIN, STUDENT 等） |
| role_name | VARCHAR(50) | NOT NULL | 角色名称 |
| description | VARCHAR(255) | | 描述 |
| sort_order | INT | DEFAULT 0 | 排序 |
| status | TINYINT | DEFAULT 1 | 状态 |

**预置角色**：

| role_code | role_name |
|-----------|-----------|
| ADMIN | 系统管理员 |
| ACADEMIC_AFFAIRS | 教务处 |
| DEPARTMENT_HEAD | 院系领导 |
| COUNSELOR | 辅导员 |
| TEACHER | 教师 |
| STUDENT | 学生 |

#### sys_permission — 系统权限表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| permission_code | VARCHAR(100) | 权限编码（如 `student:view`） |
| permission_name | VARCHAR(100) | 权限名称 |
| resource | VARCHAR(100) | 资源类型 |
| action | VARCHAR(50) | 操作类型 |
| description | VARCHAR(255) | 描述 |

#### sys_user_role — 用户角色关联表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户 ID |
| role_id | BIGINT | 角色 ID |

**唯一约束**：`(user_id, role_id)`

#### sys_role_permission — 角色权限关联表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| role_id | BIGINT | 角色 ID |
| permission_id | BIGINT | 权限 ID |

**唯一约束**：`(role_id, permission_id)`

---

### 3.2 学生模块（stu_）

#### stu_student — 学生信息表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 主键 |
| user_id | BIGINT | | 关联用户 ID |
| student_no | VARCHAR(20) | NOT NULL, UNIQUE | 学号 |
| name | VARCHAR(50) | NOT NULL | 姓名 |
| gender | CHAR(1) | NOT NULL | 性别：M-男, F-女 |
| birth_date | DATE | | 出生日期 |
| id_number | VARCHAR(18) | | 身份证号（脱敏显示） |
| phone | VARCHAR(20) | | 联系电话 |
| email | VARCHAR(100) | | 邮箱 |
| address | VARCHAR(255) | | 家庭住址 |
| department | VARCHAR(100) | NOT NULL | 院系 |
| major | VARCHAR(100) | NOT NULL | 专业 |
| class_no | VARCHAR(50) | NOT NULL | 班级 |
| enrollment_date | DATE | NOT NULL | 入学日期 |
| graduation_date | DATE | | 预计毕业日期 |
| academic_status | VARCHAR(20) | NOT NULL, DEFAULT 'ENROLLED' | 学籍状态 |
| counselor_id | BIGINT | | 辅导员 ID |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除 |

**学籍状态枚举**：

| 值 | 说明 |
|----|------|
| ENROLLED | 在读 |
| SUSPENDED | 休学 |
| WITHDRAWN | 退学 |
| GRADUATED | 毕业 |
| EXPELLED | 开除 |
| TRANSFERRED | 转学 |
| RETAINED | 留级 |
| COMPLETED | 结业 |

#### stu_student_change_log — 学生信息变更日志表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| student_id | BIGINT | 学生 ID |
| field_name | VARCHAR(50) | 变更字段名 |
| field_label | VARCHAR(50) | 变更字段标签（中文） |
| old_value | TEXT | 旧值 |
| new_value | TEXT | 新值 |
| change_type | VARCHAR(20) | 变更类型 |
| change_reason | VARCHAR(500) | 变更原因 |
| operator_id | BIGINT | 操作人 ID |
| operator_name | VARCHAR(50) | 操作人姓名 |

#### stu_status_change_application — 学籍异动申请表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| student_id | BIGINT | 学生 ID |
| current_status | VARCHAR(20) | 当前学籍状态 |
| target_status | VARCHAR(20) | 目标学籍状态 |
| reason | TEXT | 申请原因 |
| effective_date | DATE | 期望生效日期 |
| status | VARCHAR(20) | 审批状态 |
| current_approver_id | BIGINT | 当前审批人 ID |
| approved_at | DATETIME | 最终审批时间 |
| executed | TINYINT | 是否已执行状态变更 |

**审批状态枚举**：

| 值 | 说明 |
|----|------|
| PENDING | 待审批 |
| COUNSELOR_APPROVED | 辅导员已批 |
| APPROVED | 已通过 |
| REJECTED | 已拒绝 |
| CANCELLED | 已取消 |

#### stu_status_change_approval — 学籍异动审批记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| application_id | BIGINT | 申请 ID |
| approver_id | BIGINT | 审批人 ID |
| approver_name | VARCHAR(50) | 审批人姓名 |
| approver_role | VARCHAR(50) | 审批人角色 |
| approval_order | INT | 审批顺序 |
| action | VARCHAR(20) | APPROVE / REJECT |
| comment | TEXT | 审批意见 |

#### stu_certificate_record — 学籍证明记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| student_id | BIGINT | 学生 ID |
| cert_no | VARCHAR(50) | 证明编号（唯一） |
| cert_type | VARCHAR(30) | 证明类型 |
| cert_title | VARCHAR(100) | 证明标题 |
| cert_content | TEXT | 证明正文（HTML） |
| purpose | VARCHAR(200) | 用途说明 |
| copies | INT | 份数 |
| status | VARCHAR(20) | VALID / REVOKED |
| generated_by | BIGINT | 生成人 ID |
| generated_by_name | VARCHAR(50) | 生成人姓名 |
| revoked_at | DATETIME | 作废时间 |
| revoke_reason | VARCHAR(500) | 作废原因 |

**证明类型**：ENROLLMENT（在读）、GRADUATION（毕业）、TRANSCRIPT（成绩）、DEGREE（学位）、COMPLETION（结业）

---

### 3.3 选课模块（crs_）

#### crs_course — 课程信息表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 主键 |
| course_code | VARCHAR(20) | NOT NULL | 课程编号 |
| name | VARCHAR(100) | NOT NULL | 课程名称 |
| credits | DECIMAL(3,1) | NOT NULL | 学分 |
| teacher_id | BIGINT | | 授课教师 ID |
| teacher_name | VARCHAR(50) | | 授课教师姓名 |
| schedule | VARCHAR(100) | | 上课时间 |
| location | VARCHAR(100) | | 上课地点 |
| capacity | INT | DEFAULT 0 | 课程容量 |
| enrolled_count | INT | DEFAULT 0 | 已选人数 |
| semester | VARCHAR(20) | NOT NULL | 学期 |
| category | VARCHAR(50) | | 课程类别 |
| department | VARCHAR(100) | | 开课院系 |
| description | TEXT | | 课程描述 |
| syllabus | TEXT | | 教学大纲 |
| status | TINYINT | DEFAULT 1 | 状态 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除 |

**唯一约束**：`(course_code, semester)` — 同一课程同一学期唯一

#### crs_course_selection — 选课记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| student_id | BIGINT | 学生 ID |
| course_id | BIGINT | 课程 ID |
| status | VARCHAR(20) | 选课状态 |
| selected_at | DATETIME | 选课时间 |
| withdrawn_at | DATETIME | 退课时间 |

**唯一约束**：`(student_id, course_id)` — 每人每门课程只有一条记录

**选课状态枚举**：

| 值 | 说明 |
|----|------|
| SELECTED | 已选中 |
| WITHDRAWN | 已退课 |
| LOTTERY_PENDING | 待抽签 |
| LOTTERY_FAILED | 抽签未中 |

#### crs_course_prerequisite — 课程先修要求表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| course_id | BIGINT | 课程 ID |
| prerequisite_course_id | BIGINT | 先修课程 ID |
| min_grade | DECIMAL(5,2) | 最低成绩要求（默认 60） |

---

### 3.4 成绩模块（grd_）

#### grd_grade — 成绩表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 主键 |
| student_id | BIGINT | NOT NULL | 学生 ID |
| course_id | BIGINT | NOT NULL | 课程 ID |
| score | DECIMAL(5,2) | | 分数 |
| letter_grade | VARCHAR(5) | | 等级（A, B+, B, C+, C, D, F） |
| grade_points | DECIMAL(3,2) | | 绩点 |
| grade_type | VARCHAR(20) | DEFAULT 'NORMAL' | 成绩类型 |
| status | VARCHAR(20) | DEFAULT 'DRAFT' | 状态 |
| entered_by | BIGINT | | 录入人 ID |
| entered_at | DATETIME | | 录入时间 |
| submitted_by | BIGINT | | 提交人 ID |
| submitted_at | DATETIME | | 提交时间 |
| approved_by | BIGINT | | 审批人 ID |
| approved_at | DATETIME | | 审批时间 |
| semester | VARCHAR(20) | NOT NULL | 学期 |

**唯一约束**：`(student_id, course_id)` — 每人每课一条成绩

**成绩状态枚举**：

| 值 | 说明 |
|----|------|
| DRAFT | 草稿（教师录入中） |
| SUBMITTED | 已提交（待审批） |
| APPROVED | 已审批（最终成绩） |

**绩点换算规则**：

| 分数区间 | 等级 | 绩点 |
|----------|------|------|
| 90-100 | A | 4.0 |
| 85-89 | B+ | 3.5 |
| 80-84 | B | 3.0 |
| 75-79 | C+ | 2.5 |
| 70-74 | C | 2.0 |
| 60-69 | D | 1.0 |
| 0-59 | F | 0.0 |

#### grd_grade_change_log — 成绩变更日志表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| grade_id | BIGINT | 成绩 ID |
| student_id | BIGINT | 学生 ID |
| course_id | BIGINT | 课程 ID |
| old_score | DECIMAL(5,2) | 原分数 |
| new_score | DECIMAL(5,2) | 新分数 |
| old_letter_grade | VARCHAR(5) | 原等级 |
| new_letter_grade | VARCHAR(5) | 新等级 |
| change_reason | VARCHAR(500) | 修改原因 |
| operator_id | BIGINT | 操作人 ID |
| operator_name | VARCHAR(50) | 操作人姓名 |

---

### 3.5 请假模块（lev_）

#### lev_leave_request — 请假申请表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 主键 |
| student_id | BIGINT | NOT NULL | 学生 ID |
| leave_type | VARCHAR(20) | NOT NULL | 请假类型 |
| start_date | DATE | NOT NULL | 开始日期 |
| end_date | DATE | NOT NULL | 结束日期 |
| duration | DECIMAL(5,1) | NOT NULL | 请假时长（天） |
| reason | TEXT | NOT NULL | 请假原因 |
| status | VARCHAR(20) | DEFAULT 'PENDING' | 审批状态 |
| is_urgent | TINYINT | DEFAULT 0 | 是否紧急 |
| current_approver_id | BIGINT | | 当前审批人 ID |
| return_date | DATE | | 实际返校日期 |
| return_registered_at | DATETIME | | 销假登记时间 |

**请假类型**：SICK（病假）、PERSONAL（事假）、PUBLIC（公假）、FUNERAL（丧假）

**审批状态**：PENDING → COUNSELOR_APPROVED → APPROVED / REJECTED → COMPLETED（已销假）

#### lev_leave_approval — 请假审批记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| leave_request_id | BIGINT | 请假申请 ID |
| approver_id | BIGINT | 审批人 ID |
| approver_name | VARCHAR(50) | 审批人姓名 |
| approver_role | VARCHAR(50) | 审批人角色 |
| approval_order | INT | 审批顺序 |
| action | VARCHAR(20) | APPROVE / REJECT |
| comment | TEXT | 审批意见 |

#### lev_leave_attachment — 请假附件表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| leave_request_id | BIGINT | 请假申请 ID |
| file_name | VARCHAR(255) | 文件名 |
| file_path | VARCHAR(500) | 文件路径 |
| file_size | BIGINT | 文件大小（字节） |
| file_type | VARCHAR(100) | MIME 类型 |
| uploaded_by | BIGINT | 上传人 ID |

---

### 3.6 系统配置模块

#### sys_config — 系统配置表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| config_key | VARCHAR(100) | 配置键（唯一） |
| config_value | TEXT | 配置值 |
| config_type | VARCHAR(50) | 配置类型 |
| config_name | VARCHAR(100) | 配置名称（中文） |
| description | VARCHAR(500) | 描述 |
| sort_order | INT | 排序号 |
| status | TINYINT | 状态 |

**主要配置项**：

| config_key | config_type | 说明 |
|-----------|------------|------|
| current_academic_year | ACADEMIC | 当前学年 |
| current_semester | ACADEMIC | 当前学期 |
| pre_selection_start | COURSE_SELECTION | 预选开始时间 |
| pre_selection_end | COURSE_SELECTION | 预选结束时间 |
| formal_selection_start | COURSE_SELECTION | 正选开始时间 |
| formal_selection_end | COURSE_SELECTION | 正选结束时间 |
| adjustment_start | COURSE_SELECTION | 补退选开始时间 |
| adjustment_end | COURSE_SELECTION | 补退选结束时间 |
| lottery_enabled | COURSE_SELECTION | 是否启用抽签 |
| regular_weight | GRADE | 平时成绩权重 |
| midterm_weight | GRADE | 期中成绩权重 |
| final_weight | GRADE | 期末成绩权重 |

#### sys_audit_log — 审计日志表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 操作用户 ID |
| username | VARCHAR(50) | 操作用户名 |
| module | VARCHAR(50) | 操作模块 |
| operation | VARCHAR(50) | 操作类型 |
| method | VARCHAR(200) | Java 方法签名 |
| request_url | VARCHAR(500) | 请求 URL |
| request_method | VARCHAR(10) | HTTP 方法 |
| request_params | TEXT | 请求参数（JSON） |
| response_data | TEXT | 响应数据 |
| ip_address | VARCHAR(50) | 客户端 IP |
| user_agent | VARCHAR(500) | 用户代理 |
| execution_time | BIGINT | 执行耗时（ms） |
| status | TINYINT | 操作状态 |
| error_message | TEXT | 错误信息 |

#### sys_dict_type — 数据字典类型表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| dict_code | VARCHAR(50) | 字典编码（唯一） |
| dict_name | VARCHAR(100) | 字典名称 |
| description | VARCHAR(500) | 描述 |
| is_system | TINYINT | 是否系统内置 |
| status | TINYINT | 状态 |
| sort_order | INT | 排序号 |

#### sys_dict_item — 数据字典项表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| dict_type_id | BIGINT | 字典类型 ID |
| dict_code | VARCHAR(50) | 所属字典编码 |
| item_value | VARCHAR(100) | 字典项值 |
| item_label | VARCHAR(100) | 字典项标签 |
| description | VARCHAR(500) | 描述 |
| css_class | VARCHAR(100) | CSS 样式类 |
| status | TINYINT | 状态 |
| sort_order | INT | 排序号 |

**唯一约束**：`(dict_code, item_value)`

---

### 3.7 通知模块

#### sys_notification_template — 通知模板表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| template_code | VARCHAR(50) | 模板编码（唯一） |
| template_name | VARCHAR(100) | 模板名称 |
| category | VARCHAR(30) | 分类：ACADEMIC/COURSE/GRADE/LEAVE/SYSTEM |
| subject | VARCHAR(200) | 标题模板（支持 `${var}` 变量） |
| content | TEXT | 内容模板（支持 `${var}` 变量） |
| variables | VARCHAR(500) | 可用变量列表（JSON 数组） |
| channel | VARCHAR(30) | 渠道：SITE/EMAIL/ALL |
| status | TINYINT | 状态 |
| is_system | TINYINT | 是否系统内置 |
| created_by | BIGINT | 创建人 ID |

**预置模板**：

| template_code | 说明 |
|--------------|------|
| COURSE_SELECTED | 选课成功通知 |
| GRADE_PUBLISHED | 成绩发布通知 |
| LEAVE_APPROVED | 请假审批通过通知 |
| LEAVE_REJECTED | 请假审批拒绝通知 |
| STATUS_CHANGE_RESULT | 学籍异动结果通知 |
| SYSTEM_MAINTENANCE | 系统维护通知 |

#### sys_notification — 通知记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| template_id | BIGINT | 模板 ID（可为空，自定义通知） |
| title | VARCHAR(200) | 通知标题 |
| content | TEXT | 通知内容 |
| category | VARCHAR(30) | 通知分类 |
| priority | VARCHAR(10) | 优先级：LOW/NORMAL/HIGH/URGENT |
| target_type | VARCHAR(20) | 目标类型：ALL/ROLE/USER |
| target_value | VARCHAR(500) | 目标值（角色编码或用户 ID） |
| sender_id | BIGINT | 发送人 ID |
| sender_name | VARCHAR(50) | 发送人姓名 |
| send_time | DATETIME | 发送时间 |
| status | VARCHAR(20) | DRAFT/SENT |

#### sys_notification_read — 通知阅读状态表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| notification_id | BIGINT | 通知 ID |
| user_id | BIGINT | 用户 ID |
| is_read | TINYINT | 0-未读, 1-已读 |
| read_time | DATETIME | 阅读时间 |

**唯一约束**：`(notification_id, user_id)` — 每条通知每个用户一条记录

---

## 4. 索引设计说明

### 设计原则

1. **主键索引**：所有表使用 `BIGINT AUTO_INCREMENT` 作为主键
2. **唯一索引**：业务唯一标识字段（用户名、学号、课程编号+学期、配置键等）
3. **外键索引**：所有关联字段添加普通索引，加速 JOIN 查询
4. **状态索引**：状态字段建立索引，支持状态筛选查询
5. **时间索引**：`created_at` 字段建立索引，支持时间范围查询

### 关键索引汇总

| 表 | 索引名 | 字段 | 类型 |
|----|--------|------|------|
| sys_user | uk_username | username | UNIQUE |
| stu_student | uk_student_no | student_no | UNIQUE |
| crs_course | uk_course_code_semester | (course_code, semester) | UNIQUE |
| crs_course_selection | uk_student_course | (student_id, course_id) | UNIQUE |
| grd_grade | uk_student_course | (student_id, course_id) | UNIQUE |
| sys_config | uk_config_key | config_key | UNIQUE |
| sys_notification_template | uk_template_code | template_code | UNIQUE |
| stu_certificate_record | uk_cert_no | cert_no | UNIQUE |

## 5. 数据约束与业务规则

1. **逻辑删除**：`sys_user`、`stu_student`、`crs_course` 使用 `deleted` 字段实现软删除，不做物理删除
2. **时间自动维护**：`created_at` 自动填充创建时间，`updated_at` 自动更新
3. **无外键约束**：表间通过应用层维护数据一致性，不使用数据库外键约束，保证性能和灵活性
4. **状态机**：学籍异动、请假审批、成绩流转均通过状态字段实现状态机控制，应用层保证状态转换合法性
5. **容量控制**：`crs_course.enrolled_count` 与 `capacity` 配合使用，选课时在应用层检查并更新
