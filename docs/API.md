# API 接口文档

## 概述

- **基础路径**：`http://localhost:8080/api`（Docker 部署时通过 `http://localhost:8081/api` 经 Nginx 代理）
- **认证方式**：JWT Bearer Token
- **请求头**：`Authorization: Bearer <accessToken>`（除公开接口外均需携带）
- **响应格式**：统一 JSON 封装

### 统一响应结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 响应码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 / Token 过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 1. 认证接口 `/api/auth`

### 1.1 获取登录加密公钥

```
GET /api/auth/login/encryption-key
```

**响应**：

```json
{
  "code": 200,
  "data": {
    "algorithm": "RSA-OAEP-256",
    "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A..."
  }
}
```

### 1.2 用户登录

```
POST /api/auth/login
```

**请求体**：

```json
{
  "username": "admin",
  "encryptedPassword": "Base64(RSA-OAEP-Encrypted-Password)"
}
```

**响应**：

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "roles": ["ADMIN"]
    },
    "permissions": ["student:view", "student:update", ...]
  }
}
```

### 1.3 用户登出

```
POST /api/auth/logout
```

### 1.4 刷新令牌

```
POST /api/auth/refresh
```

**请求体**：

```json
{
  "refreshToken": "eyJhbGci..."
}
```

### 1.5 获取当前用户信息

```
GET /api/auth/current
```

### 1.5 获取当前用户权限列表

```
GET /api/auth/permissions
```

---

## 2. 学生信息接口 `/api/students`

### 2.1 获取当前登录学生信息

```
GET /api/students/me
```

### 2.2 根据 ID 查询学生

```
GET /api/students/{id}
```

### 2.3 根据学号查询学生

```
GET /api/students/no/{studentNo}
```

### 2.4 分页查询学生列表

```
GET /api/students
```

**权限**：`student:view` 或 `student:update`

**查询参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码（默认 1） |
| size | int | 每页条数（默认 10） |
| studentNo | string | 学号（模糊匹配） |
| name | string | 姓名（模糊匹配） |
| department | string | 院系 |
| major | string | 专业 |
| status | string | 学籍状态 |

### 2.5 辅导员查询所辖学生

```
GET /api/students/counselor
```

**权限**：`student:view`

### 2.6 学生更新自己的联系方式

```
PUT /api/students/me/contact
```

**请求体**：

```json
{
  "phone": "13800138000",
  "email": "student@example.com",
  "address": "北京市海淀区"
}
```

### 2.7 更新学生联系方式（管理端）

```
PUT /api/students/{id}/contact
```

**权限**：`student:update` 或 `student:manage`

### 2.8 更新学生信息

```
PUT /api/students/{id}
```

**权限**：`student:update` 或 `student:manage`

### 2.9 变更学籍状态

```
PUT /api/students/{id}/status
```

**权限**：`student:status:change`

### 2.10 下载导入模板

```
GET /api/students/import/template
```

**响应**：Excel 文件下载

### 2.11 批量导入学生

```
POST /api/students/import
```

**权限**：`student:import`  
**Content-Type**：`multipart/form-data`

### 2.12 导出学生数据

```
GET /api/students/export
```

**权限**：`student:export`  
**响应**：Excel 文件下载

### 2.13 查询学生变更日志

```
GET /api/students/{id}/logs
```

**权限**：`student:view`

---

## 3. 学籍异动接口 `/api/status-changes`

### 3.1 提交学籍异动申请

```
POST /api/status-changes
```

**请求体**：

```json
{
  "changeType": "SUSPENSION",
  "reason": "因健康原因申请休学",
  "attachmentUrl": ""
}
```

### 3.2 获取申请详情

```
GET /api/status-changes/{id}
```

### 3.3 获取我的学籍异动申请

```
GET /api/status-changes/my
```

### 3.4 辅导员待审批列表

```
GET /api/status-changes/pending/counselor
```

**权限**：`student:status:approve`

### 3.5 教务处待审批列表

```
GET /api/status-changes/pending/academic
```

**权限**：`student:status:approve`

### 3.6 审批学籍异动

```
PUT /api/status-changes/{id}/approve
```

**权限**：`student:status:approve`

**请求体**：

```json
{
  "approved": true,
  "comment": "同意休学申请"
}
```

### 3.7 学生取消申请

```
PUT /api/status-changes/{id}/cancel
```

---

## 4. 学籍证明接口 `/api/certificates`

### 4.1 学生申请生成证明

```
POST /api/certificates
```

**请求体**：

```json
{
  "certType": "ENROLLMENT",
  "purpose": "用于办理银行卡"
}
```

**证明类型**：

| 类型 | 说明 |
|------|------|
| ENROLLMENT | 在读证明 |
| GRADUATION | 毕业证明 |
| TRANSCRIPT | 成绩证明 |
| DEGREE | 学位证明 |
| COMPLETION | 结业证明 |

### 4.2 管理员为学生生成证明

```
POST /api/certificates/student/{studentId}
```

**权限**：`student:view` 或 `student:update`

### 4.3 获取证明详情

```
GET /api/certificates/{id}
```

### 4.4 通过编号验证证明

```
GET /api/certificates/verify/{certNo}
```

### 4.5 获取我的证明记录

```
GET /api/certificates/my
```

### 4.6 获取指定学生证明记录

```
GET /api/certificates/student/{studentId}
```

**权限**：`student:view`

### 4.7 获取最近证明记录

```
GET /api/certificates/recent
```

**权限**：`student:view`

### 4.8 获取可用证明类型

```
GET /api/certificates/types
```

### 4.9 作废证明

```
PUT /api/certificates/{id}/revoke
```

**权限**：`student:update` 或 `student:status:change`

---

## 5. 课程接口 `/api/courses`

### 5.1 分页查询课程列表

```
GET /api/courses
```

**查询参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| size | int | 每页条数 |
| keyword | string | 课程名/编号模糊搜索 |
| courseType | string | 课程类型 |
| semester | string | 学期 |
| department | string | 开课院系 |

### 5.2 搜索课程

```
GET /api/courses/search
```

### 5.3 获取课程详情

```
GET /api/courses/{id}
```

### 5.4 创建课程

```
POST /api/courses
```

**权限**：`course:create`

**请求体**：

```json
{
  "courseNo": "CS101",
  "courseName": "计算机导论",
  "courseType": "REQUIRED",
  "credit": 3.0,
  "totalHours": 48,
  "semester": "2024-2025-1",
  "department": "计算机学院",
  "teacherId": 5,
  "maxCapacity": 120,
  "scheduleTime": "周一 1-2节",
  "classroom": "教学楼A301",
  "description": "课程简介"
}
```

### 5.5 更新课程

```
PUT /api/courses/{id}
```

**权限**：`course:update`

### 5.6 删除课程

```
DELETE /api/courses/{id}
```

**权限**：`course:delete`

### 5.7 获取教师课程列表

```
GET /api/courses/teacher
```

### 5.8 获取选课统计

```
GET /api/courses/statistics
```

---

## 6. 选课接口 `/api/course-selections`

### 6.1 选课

```
POST /api/course-selections
```

**请求体**：

```json
{
  "courseId": 1,
  "selectionType": "REQUIRED"
}
```

**选课阶段规则**：

| 阶段 | 规则 |
|------|------|
| PRE_SELECTION（预选） | 允许超容量，可能进入抽签 |
| FORMAL_SELECTION（正选） | 先到先得，不超容量 |
| ADJUSTMENT（补退选） | 先到先得，可退课 |
| NOT_STARTED / CLOSED | 不允许选课 |

### 6.2 退课

```
DELETE /api/course-selections/{courseId}
```

### 6.3 获取我的选课记录

```
GET /api/course-selections/my
```

### 6.4 获取我的课表

```
GET /api/course-selections/schedule
```

### 6.5 获取学生课表

```
GET /api/course-selections/schedule/{studentId}
```

**权限**：`course:roster` 或 `student:manage`

### 6.6 获取课程学生名单

```
GET /api/course-selections/roster/{courseId}
```

**权限**：`course:roster:view` 或 `grade:entry`

### 6.7 检查时间冲突

```
GET /api/course-selections/check-conflict?courseId=1
```

### 6.8 检查先修课程

```
GET /api/course-selections/check-prerequisites?courseId=1
```

### 6.9 获取当前选课阶段

```
GET /api/course-selections/phase
```

**响应**：返回当前阶段名称字符串

### 6.10 获取选课阶段详情

```
GET /api/course-selections/phase/info
```

**响应**：

```json
{
  "code": 200,
  "data": {
    "currentPhase": "PRE_SELECTION",
    "phaseDisplayName": "预选阶段",
    "preSelectionStart": "2024-07-01 08:00:00",
    "preSelectionEnd": "2024-07-07 23:59:59",
    "formalSelectionStart": "2024-07-10 08:00:00",
    "formalSelectionEnd": "2024-07-14 23:59:59",
    "adjustmentStart": "2024-07-17 08:00:00",
    "adjustmentEnd": "2024-07-20 23:59:59",
    "lotteryEnabled": true
  }
}
```

### 6.11 获取待抽签课程

```
GET /api/course-selections/lottery/pending
```

**权限**：`course:manage`

### 6.12 对单门课程执行抽签

```
POST /api/course-selections/lottery/{courseId}
```

**权限**：`course:manage`

### 6.13 批量执行抽签

```
POST /api/course-selections/lottery/all
```

**权限**：`course:manage`

---

## 7. 成绩接口 `/api/grades`

### 7.1 查询我的成绩

```
GET /api/grades/my
```

**查询参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| semester | string | 学期筛选 |

### 7.1.1 查询我的不及格预警状态

```
GET /api/grades/my/failing
```

**响应示例**：

```json
{
  "code": 200,
  "data": true
}
```

### 7.2 查询学生成绩

```
GET /api/grades/student/{studentId}
```

**权限**：`grade:view` 或 `grade:manage`

### 7.3 查询课程成绩

```
GET /api/grades/course/{courseId}
```

**权限**：`grade:view` 或 `grade:entry` 或 `grade:approve`

### 7.4 批量录入成绩

```
POST /api/grades/batch
```

**权限**：`grade:entry`

**请求体**：

```json
{
  "courseId": 1,
  "grades": [
    { "studentId": 10, "regularScore": 85.0, "midtermScore": 78.0, "finalScore": 90.0 },
    { "studentId": 11, "regularScore": 92.0, "midtermScore": 88.0, "finalScore": 95.0 }
  ]
}
```

### 7.5 批量录入并提交成绩

```
POST /api/grades/batch-submit
```

**权限**：`grade:entry`

### 7.6 修改成绩

```
PUT /api/grades/{id}
```

**权限**：`grade:entry`

### 7.7 提交成绩审核

```
POST /api/grades/submit/{courseId}
```

**权限**：`grade:entry`

### 7.8 审批成绩

```
POST /api/grades/approve/{courseId}
```

**权限**：`grade:approve`

**请求体**：

```json
{
  "approved": true,
  "comment": "审批通过"
}
```

### 7.9 获取我的 GPA

```
GET /api/grades/gpa
```

### 7.10 获取学生 GPA

```
GET /api/grades/gpa/{studentId}
```

**权限**：`grade:view` 或 `grade:manage`

**响应**：

```json
{
  "code": 200,
  "data": {
    "studentId": 10,
    "studentNo": "2024001",
    "studentName": "张三",
    "totalCredits": 60.0,
    "earnedCredits": 57.0,
    "totalGradePoints": 213.0,
    "gpa": 3.74,
    "failedCourseCount": 1,
    "failedCourses": [
      {
        "courseName": "高等数学",
        "score": 55.0,
        "semester": "2024-2025-1"
      }
    ]
  }
}
```

### 7.11 获取课程成绩统计

```
GET /api/grades/statistics/{courseId}
```

**权限**：`grade:view` 或 `grade:entry` 或 `grade:approve`

### 7.12 获取成绩修改记录

```
GET /api/grades/change-logs/{courseId}
```

**权限**：`grade:view` 或 `grade:entry` 或 `grade:approve`

### 7.13 生成我的成绩单

```
GET /api/grades/transcript
```

### 7.14 生成指定学生成绩单

```
GET /api/grades/transcript/{studentId}
```

**权限**：`grade:view` 或 `grade:manage`

### 7.15 导出成绩单 HTML

```
GET /api/grades/transcript/export
GET /api/grades/transcript/export/{studentId}
```

**响应**：`text/html` 格式的正式成绩单文档

---

## 8. 请假接口 `/api/leaves`

### 8.1 提交请假申请

```
POST /api/leaves
```

**请求体**：

```json
{
  "leaveType": "SICK",
  "startTime": "2024-03-01 08:00:00",
  "endTime": "2024-03-02 17:00:00",
  "reason": "身体不适需要就医",
  "urgent": false
}
```

**请假类型**：

| 类型 | 说明 |
|------|------|
| SICK | 病假 |
| PERSONAL | 事假 |
| PUBLIC | 公假 |
| FUNERAL | 丧假 |

### 8.2 获取请假详情

```
GET /api/leaves/{id}
```

### 8.3 获取我的请假记录

```
GET /api/leaves/my
```

### 8.4 辅导员待审批列表

```
GET /api/leaves/pending/counselor
```

**权限**：`leave:approve`

### 8.5 院系领导待审批列表

```
GET /api/leaves/pending/department
```

**权限**：`leave:approve:department`

### 8.6 审批请假

```
PUT /api/leaves/{id}/approve
```

**权限**：`leave:approve`

**请求体**：

```json
{
  "approved": true,
  "comment": "同意"
}
```

### 8.7 销假

```
PUT /api/leaves/{id}/return
```

---

## 9. 文件接口 `/api/files`

### 9.1 上传请假附件

```
POST /api/files/leave/{leaveId}
```

**Content-Type**：`multipart/form-data`

### 9.2 获取请假附件列表

```
GET /api/files/leave/{leaveId}
```

### 9.3 下载附件

```
GET /api/files/download/{id}
```

### 9.4 删除附件

```
DELETE /api/files/{id}
```

---

## 10. 用户管理接口 `/api/system/users`

所有接口需要 `system:user:manage` 权限（除修改密码外）。

### 10.1 分页查询用户

```
GET /api/system/users?page=1&size=10&username=xxx
```

### 10.2 获取用户详情

```
GET /api/system/users/{id}
```

### 10.3 创建用户

```
POST /api/system/users
```

**请求体**：

```json
{
  "username": "newuser",
  "password": "123456",
  "realName": "新用户",
  "email": "user@example.com",
  "phone": "13800138000"
}
```

### 10.4 更新用户

```
PUT /api/system/users/{id}
```

### 10.5 删除用户

```
DELETE /api/system/users/{id}
```

### 10.6 重置密码

```
POST /api/system/users/{id}/reset-password
```

### 10.7 分配角色

```
POST /api/system/users/{id}/roles
```

**请求体**：

```json
{
  "roleIds": [1, 2]
}
```

### 10.8 修改当前用户密码

```
POST /api/system/users/change-password
```

**请求体**：

```json
{
  "oldPassword": "123456",
  "newPassword": "newpass123"
}
```

---

## 11. 角色管理接口 `/api/system/roles`

所有接口需要 `system:user:manage` 权限。

### 11.1 查询所有角色

```
GET /api/system/roles
```

### 11.2 获取角色详情

```
GET /api/system/roles/{id}
```

### 11.3 更新角色

```
PUT /api/system/roles/{id}
```

### 11.4 获取角色权限

```
GET /api/system/roles/{id}/permissions
```

### 11.5 配置角色权限

```
POST /api/system/roles/{id}/permissions
```

**请求体**：

```json
{
  "permissionIds": [1, 2, 3, 4, 5]
}
```

### 11.6 获取所有权限列表

```
GET /api/system/roles/permissions/all
```

---

## 12. 系统配置接口 `/api/system/config`

### 12.1 获取所有配置

```
GET /api/system/config
```

**权限**：`config:view`

### 12.2 按类型获取配置

```
GET /api/system/config/type/{configType}
```

**权限**：`config:view`

**配置类型**：

| 类型 | 说明 |
|------|------|
| ACADEMIC | 学年学期配置 |
| COURSE_SELECTION | 选课配置（阶段时间、抽签开关） |
| GRADE | 成绩配置（权重比例） |
| SYSTEM | 系统通用配置 |

### 12.3 获取单个配置值

```
GET /api/system/config/{configKey}
```

### 12.4 更新单个配置

```
PUT /api/system/config/{configKey}
```

**权限**：`config:update`

**请求体**：

```json
{
  "configValue": "2024-2025-1"
}
```

### 12.5 批量更新配置

```
PUT /api/system/config/batch
```

**权限**：`config:update`

### 12.6 获取当前学期

```
GET /api/system/config/current-semester
```

---

## 13. 数据管理接口 `/api/system/data`

### 13.1 获取备份列表

```
GET /api/system/data/backups
```

**权限**：`backup:create`

### 13.2 创建数据备份

```
POST /api/system/data/backup
```

**权限**：`backup:create`

### 13.3 下载备份文件

```
GET /api/system/data/backup/download/{fileName}
```

**权限**：`backup:create`

### 13.4 删除备份

```
DELETE /api/system/data/backup/{fileName}
```

**权限**：`backup:create`

### 13.4.1 启动异步恢复

```
POST /api/system/data/backup/restore/{fileName}
```

**说明**：该接口仅负责启动后台恢复任务，成功后立即返回。

**权限**：`backup:create`

### 13.4.2 查询恢复任务状态

```
GET /api/system/data/backup/restore-status
```

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "status": "RUNNING",
    "message": "恢复进行中"
  }
}
```

### 13.5 获取数据字典

```
GET /api/system/data/dictionary
```

**权限**：`config:view`

### 13.6 获取字典类型列表

```
GET /api/system/data/dictionary/types
```

**权限**：`config:view`

### 13.7 创建字典类型

```
POST /api/system/data/dictionary/types
```

**权限**：`config:update`

### 13.8 更新字典类型

```
PUT /api/system/data/dictionary/types/{id}
```

**权限**：`config:update`

### 13.9 删除字典类型

```
DELETE /api/system/data/dictionary/types/{id}
```

**权限**：`config:update`

### 13.10 获取字典项列表

```
GET /api/system/data/dictionary/items/{dictCode}
```

**权限**：`config:view`

### 13.11 创建字典项

```
POST /api/system/data/dictionary/items
```

**权限**：`config:update`

### 13.12 更新字典项

```
PUT /api/system/data/dictionary/items/{id}
```

**权限**：`config:update`

### 13.13 删除字典项

```
DELETE /api/system/data/dictionary/items/{id}
```

**权限**：`config:update`

### 13.14 导出数据

```
GET /api/system/data/export/{type}
```

**权限**：`student:export`

**支持类型**：`student`、`course`、`grade`、`leave`

---

## 14. 审计日志接口 `/api/system/audit-logs`

### 14.1 分页查询审计日志

```
GET /api/system/audit-logs?page=1&size=10
```

**权限**：`system:user:manage`

**查询参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| username | string | 操作用户 |
| module | string | 操作模块 |
| startTime | string | 开始时间 |
| endTime | string | 结束时间 |

---

## 15. 仪表盘接口 `/api/dashboard`

### 15.1 获取首页统计数据

```
GET /api/dashboard/stats
```

**响应**：

```json
{
  "code": 200,
  "data": {
    "totalStudents": 1200,
    "totalCourses": 85,
    "totalTeachers": 50,
    "pendingLeaves": 12,
    "currentSemester": "2024-2025-1"
  }
}
```

---

## 16. 通知管理接口 `/api/notifications`

### 模板管理（需要 `system:manage` 权限）

### 16.1 获取所有模板

```
GET /api/notifications/templates
```

### 16.2 按分类获取模板

```
GET /api/notifications/templates/category/{category}
```

**分类**：`ACADEMIC`、`COURSE`、`GRADE`、`LEAVE`、`SYSTEM`

### 16.3 获取模板详情

```
GET /api/notifications/templates/{id}
```

### 16.4 创建模板

```
POST /api/notifications/templates
```

**请求体**：

```json
{
  "templateCode": "EXAM_REMINDER",
  "templateName": "考试提醒",
  "category": "ACADEMIC",
  "title": "考试提醒：${courseName}",
  "content": "同学您好，${courseName}将于${examDate}在${location}进行考试，请准时参加。",
  "channel": "SITE",
  "priority": "HIGH"
}
```

### 16.5 更新模板

```
PUT /api/notifications/templates/{id}
```

### 16.6 删除模板

```
DELETE /api/notifications/templates/{id}
```

### 16.7 切换模板启用状态

```
POST /api/notifications/templates/{id}/toggle
```

### 通知发送管理（需要 `system:manage` 权限）

### 16.8 发送通知

```
POST /api/notifications/send
```

**请求体（基于模板）**：

```json
{
  "templateId": 1,
  "variables": {
    "courseName": "高等数学",
    "examDate": "2024-06-15"
  },
  "targetType": "ROLE",
  "targetValue": "STUDENT"
}
```

**请求体（自定义内容）**：

```json
{
  "title": "系统维护通知",
  "content": "系统将于今晚22:00进行维护升级...",
  "category": "SYSTEM",
  "channel": "SITE",
  "priority": "URGENT",
  "targetType": "ALL",
  "targetValue": ""
}
```

**目标类型**：

| targetType | targetValue | 说明 |
|-----------|-------------|------|
| ALL | 空 | 所有用户 |
| ROLE | 角色编码 | 指定角色的用户 |
| USER | 用户ID | 指定用户 |

### 16.9 获取所有通知（管理端）

```
GET /api/notifications/manage/list
```

### 16.10 删除通知

```
DELETE /api/notifications/{id}
```

### 用户通知（无特殊权限要求）

### 16.11 获取我的通知

```
GET /api/notifications/my?category=ACADEMIC&read=false
```

### 16.12 获取未读数量

```
GET /api/notifications/my/unread-count
```

### 16.13 获取通知详情

```
GET /api/notifications/{id}
```

### 16.14 标记已读

```
POST /api/notifications/{id}/read
```

### 16.15 全部标记已读

```
POST /api/notifications/my/read-all
```

---

## 附录：权限编码速查表

| 权限编码 | 说明 |
|---------|------|
| `student:view` | 查看学生信息 |
| `student:update` | 更新学生信息 |
| `student:manage` | 管理学生信息（教务处） |
| `student:import` | 导入学生数据 |
| `student:export` | 导出学生数据 |
| `student:status:change` | 变更学籍状态 |
| `student:status:approve` | 审批学籍异动 |
| `course:create` | 创建课程 |
| `course:update` | 更新课程 |
| `course:delete` | 删除课程 |
| `course:manage` | 管理课程（含抽签） |
| `course:roster` | 查看课程名单 |
| `course:roster:view` | 查看课程名单（教师） |
| `grade:view` | 查看成绩 |
| `grade:entry` | 录入成绩 |
| `grade:approve` | 审批成绩 |
| `grade:manage` | 管理成绩（教务处） |
| `leave:approve` | 审批请假 |
| `leave:approve:department` | 院系领导审批请假 |
| `config:view` | 查看系统配置 |
| `config:update` | 更新系统配置 |
| `backup:create` | 数据备份管理 |
| `system:manage` | 系统管理（通知模板等） |
| `system:user:manage` | 用户/角色管理 |
