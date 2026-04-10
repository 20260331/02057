# 修复请假选课先修课程 Bug

> **Workspace:** d:\code\trae\20260408-GSB\02057\Kimi-K2.5

---

## User
当前项目存在以下一些问题，请在不改变现有接口结构，不引入额外依赖的情况下修复这些 bug. 修复后需要给出关键的修改点说明和必要的测试建议。 
 1. 在请假业务中，任何登录用户都能查看或销假他人的记录，存在权限越界的情况 
 2. 在选课业务中，抽签执行时可能出现超额选课，并且并发下会有重复选课的情况，需要补上并发安全方案 
 3. 先修课程校验中，未审核成绩也能作为先修依据，应该是仅认可已审批成绩才对

## Assistant
现在我已充分理解了代码结构，让我开始修复这三个问题：

## 问题1：请假业务权限越界

在 [LeaveController.java](file:///d:\code\trae\20260408-GSB\02057\Kimi-K2.5\backend\src\main\java\com\university\sms\leave\controller\LeaveController.java) 中：
- `getLeaveById` 方法：任何登录用户都能查看任意请假记录
- `registerReturn` 方法：任何登录用户都能销假任意请假记录

## 问题2：选课业务并发安全问题

在 [CourseSelectionServiceImpl.java](file:///d:\code\trae\20260408-GSB\02057\Kimi-K2.5\backend\src\main\java\com\university\sms\course\service\impl\CourseSelectionServiceImpl.java) 中：
- `executeLottery` 方法：并发下可能出现超额选课
- `selectCourse` 方法：并发下可能出现重复选课

## 问题3：先修课程校验问题

在 [CourseSelectionServiceImpl.java](file:///d:\code\trae\20260408-GSB\02057\Kimi-K2.5\backend\src\main\java\com\university\sms\course\service\impl\CourseSelectionServiceImpl.java) 的 `checkPrerequisites` 方法中：
- 未审核成绩也能作为先修依据，应该仅认可已审批成绩

现在修复选课业务的并发安全问题。主要问题：
1. `executeLottery` 方法中，并发情况下可能超额选课
2. `selectCourse` 方法中，并发情况下可能重复选课

解决方案：
1. 使用数据库乐观锁或悲观锁防止并发问题
2. 利用数据库唯一约束防止重复选课

现在我已经了解了数据库结构。选课表 `crs_course_selection` 已经有唯一约束 `uk_student_course`，但问题是当状态变更时（如从 LOTTERY_PENDING 变为 SELECTED），可能会因为并发导致超额选课。

让我修复选课服务的并发安全问题：

现在修复先修课程校验问题，仅认可已审批成绩：

现在让我验证代码是否正确编译：

所有修复已完成。以下是关键修改点说明和测试建议：

---

## 修复完成总结

### 1. 请假业务权限越界问题

**修改文件**: [LeaveController.java](file:///d:\code\trae\20260408-GSB\02057\Kimi-K2.5\backend\src\main\java\com\university\sms\leave\controller\LeaveController.java)

**关键修改点**:
- **获取请假详情** (`getLeaveById`): 添加权限检查，学生只能查看自己的请假记录，管理员/辅导员/院系领导可以查看所有记录
- **销假** (`registerReturn`): 添加权限检查，学生只能销假自己的请假记录，管理员可以销假任意记录

```java
// 权限检查逻辑
boolean isAdmin = roles != null && (roles.contains("ADMIN") || roles.contains("ACADEMIC_AFFAIRS"));
if (!isAdmin && !isCounselor && !isDepartmentHead) {
    // 普通学生只能查看/销假自己的记录
    StudentDTO student = studentService.getByUserIdOrNull(userId);
    if (student == null || !leave.getStudentId().equals(student.getId())) {
        return Result.error(403, "无权查看/销假他人的请假记录");
    }
}
```

---

### 2. 选课业务并发安全问题

**修改文件**: [CourseSelectionServiceImpl.java](file:///d:\code\trae\20260408-GSB\02057\Kimi-K2.5\backend\src\main\java\com\university\sms\course\service\impl\CourseSelectionServiceImpl.java)

**关键修改点**:

#### a. 抽签方法 (`executeLottery`)
- 添加 `synchronized` 关键字防止并发执行同一课程的抽签
- 重新计算可用名额时，实时检查已选人数是否已达到容量上限
- 每次增加已选人数时使用数据库原子操作，并校验返回值

```java
@Override
@Transactional(rollbackFor = Exception.class)
public synchronized void executeLottery(Long courseId) {
    // 重新查询课程获取最新已选人数
    int currentEnrolled = course.getEnrolledCount();
    int capacity = course.getCapacity();
    int available = capacity - currentEnrolled;
    
    // 中签时原子性增加已选人数
    boolean incremented = courseService.incrementEnrolledCount(courseId);
    if (incremented) {
        selected++;
        enrolled++;
    }
}
```

#### b. 正选/补退选方法 (`handleDirectSelection`)
- 调整执行顺序：先原子性增加已选人数，成功后再保存选课记录
- 添加异常回滚机制，保存失败时回滚已选人数

```java
// 先尝试原子性增加已选人数
boolean enrolled = courseService.incrementEnrolledCount(courseId);
if (enrolled) {
    try {
        saveOrUpdateSelection(studentId, courseId, existing, SelectionStatus.SELECTED);
    } catch (Exception e) {
        // 回滚已选人数
        courseService.decrementEnrolledCount(courseId);
    }
}
```

#### c. 预选方法 (`handlePreSelectionWithLottery`)
- 同样调整为先原子性增加已选人数
- 添加异常降级处理，保存失败时回滚并进入待抽签状态

---

### 3. 先修课程校验问题

**修改文件**: [CourseSelectionServiceImpl.java](file:///d:\code\trae\20260408-GSB\02057\Kimi-K2.5\backend\src\main\java\com\university\sms\course\service\impl\CourseSelectionServiceImpl.java)

**关键修改点**:
- 在 `checkPrerequisites` 方法中，增加成绩状态校验，仅认可 `APPROVED` 状态的成绩

```java
// 仅认可已审批成绩
if (grade == null ||
    !Grade.STATUS_APPROVED.equals(grade.getStatus()) ||  // 新增校验
    grade.getScore() == null ||
    grade.getScore().doubleValue() < prereq.getMinScore().doubleValue()) {
    missing.add(prereqCourse.getName());
}
```

---

## 测试建议

### 1. 请假权限测试
```bash
# 测试场景1: 学生A尝试查看学生B的请假记录
# 预期: 返回403错误

# 测试场景2: 学生A销假学生B的请假记录
# 预期: 返回403错误

# 测试场景3: 管理员查看任意请假记录
# 预期: 成功返回

# 测试场景4: 学生查看自己的请假记录
# 预期: 成功返回
```

### 2. 选课并发测试
```bash
# 测试场景1: 并发选课（课程容量为1，同时有5个学生选课）
# 预期: 只有1人成功，其他人返回"课程已满"

# 测试场景2: 并发抽签（课程容量10，20人待抽签）
# 预期: 只有10人中签，不会出现超额

# 测试场景3: 重复选课
# 预期: 数据库唯一约束防止重复记录
```

### 3. 先修课程校验测试
```bash
# 测试场景1: 先修课程成绩为DRAFT状态
# 预期: 校验不通过，提示缺少先修课程

# 测试场景2: 先修课程成绩为SUBMITTED状态
# 预期: 校验不通过，提示缺少先修课程

# 测试场景3: 先修课程成绩为APPROVED且分数达标
# 预期: 校验通过

# 测试场景4: 先修课程成绩为APPROVED但分数不达标
# 预期: 校验不通过
```

### 4. 单元测试示例
建议为以下方法添加单元测试：
- `LeaveController.getLeaveById()` - 权限校验
- `LeaveController.registerReturn()` - 权限校验
- `CourseSelectionServiceImpl.executeLottery()` - 并发安全
- `CourseSelectionServiceImpl.selectCourse()` - 超额选课防护
- `CourseSelectionServiceImpl.checkPrerequisites()` - 成绩状态校验

