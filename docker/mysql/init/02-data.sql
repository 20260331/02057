-- =====================================================
-- 学生信息管理系统初始化数据
-- Database: MySQL 8.0
-- =====================================================

SET NAMES utf8mb4;

-- =====================================================
-- 1. 初始化角色数据
-- =====================================================
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `sort_order`, `status`) VALUES
('ADMIN', '系统管理员', '系统管理员，负责系统配置和用户权限管理', 1, 1),
('ACADEMIC_AFFAIRS', '教务处', '教务处用户，具有完整系统管理权限', 2, 1),
('DEPARTMENT_HEAD', '院系领导', '院系领导用户，可审批请假、查看统计', 3, 1),
('COUNSELOR', '辅导员', '辅导员用户，可管理所辖学生、审批请假', 4, 1),
('TEACHER', '授课教师', '授课教师用户，可录入成绩、查看课程学生名单', 5, 1),
('STUDENT', '学生', '在校学生用户，可查看个人信息、选课、查成绩、请假', 6, 1);

-- =====================================================
-- 2. 初始化权限数据
-- =====================================================

-- 学生信息模块权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `resource`, `action`, `description`) VALUES
('student:view', '查看学生信息', 'student', 'view', '查看学生基本信息'),
('student:view:own', '查看个人信息', 'student', 'view', '学生查看自己的信息'),
('student:update', '修改学生信息', 'student', 'update', '修改学生信息'),
('student:update:contact', '修改联系方式', 'student', 'update', '学生修改自己的联系方式'),
('student:create', '创建学生信息', 'student', 'create', '创建新学生信息'),
('student:delete', '删除学生信息', 'student', 'delete', '删除学生信息'),
('student:export', '导出学生数据', 'student', 'export', '导出学生数据'),
('student:import', '导入学生数据', 'student', 'import', '批量导入学生数据'),
('student:status:change', '变更学籍状态', 'student', 'update', '变更学生学籍状态（直接变更）'),
('student:status:apply', '申请学籍异动', 'student', 'create', '学生提交学籍异动申请'),
('student:status:approve', '审批学籍异动', 'student', 'update', '审批学籍异动申请');

-- 选课模块权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `resource`, `action`, `description`) VALUES
('course:view', '查看课程信息', 'course', 'view', '查看课程列表和详情'),
('course:create', '创建课程', 'course', 'create', '创建新课程'),
('course:update', '修改课程', 'course', 'update', '修改课程信息'),
('course:delete', '删除课程', 'course', 'delete', '删除课程'),
('course:selection:create', '选课', 'course_selection', 'create', '学生选课'),
('course:selection:delete', '退课', 'course_selection', 'delete', '学生退课'),
('course:selection:view', '查看选课记录', 'course_selection', 'view', '查看选课记录'),
('course:schedule:view', '查看课表', 'course_schedule', 'view', '查看个人课表'),
('course:roster:view', '查看课程名单', 'course_roster', 'view', '教师查看课程学生名单'),
('course:selection:manage', '管理选课', 'course_selection', 'update', '管理选课时间和抽签');

-- 成绩模块权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `resource`, `action`, `description`) VALUES
('grade:view', '查看成绩', 'grade', 'view', '查看成绩信息'),
('grade:view:own', '查看个人成绩', 'grade', 'view', '学生查看自己的成绩'),
('grade:create', '录入成绩', 'grade', 'create', '教师录入成绩'),
('grade:entry', '成绩录入', 'grade', 'create', '教师录入和修改成绩'),
('grade:update', '修改成绩', 'grade', 'update', '修改成绩'),
('grade:submit', '提交成绩', 'grade', 'update', '教师提交成绩'),
('grade:approve', '审批成绩', 'grade', 'update', '教务处审批成绩'),
('grade:statistics:view', '查看成绩统计', 'grade_statistics', 'view', '查看成绩统计信息'),
('grade:transcript:export', '导出成绩单', 'grade', 'export', '导出学生成绩单');

-- 请假模块权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `resource`, `action`, `description`) VALUES
('leave:view', '查看请假记录', 'leave', 'view', '查看请假记录'),
('leave:view:own', '查看个人请假', 'leave', 'view', '学生查看自己的请假记录'),
('leave:create', '提交请假申请', 'leave', 'create', '学生提交请假申请'),
('leave:update', '修改请假申请', 'leave', 'update', '修改请假申请'),
('leave:cancel', '取消请假申请', 'leave', 'delete', '取消请假申请'),
('leave:approve', '审批请假', 'leave', 'update', '审批请假申请'),
('leave:return', '销假登记', 'leave', 'update', '登记销假'),
('leave:statistics:view', '查看请假统计', 'leave_statistics', 'view', '查看请假统计信息');

-- 系统管理权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `resource`, `action`, `description`) VALUES
('system:user:manage', '用户管理', 'user', 'manage', '用户管理权限'),
('user:view', '查看用户', 'user', 'view', '查看用户列表'),
('user:create', '创建用户', 'user', 'create', '创建新用户'),
('user:update', '修改用户', 'user', 'update', '修改用户信息'),
('user:delete', '删除用户', 'user', 'delete', '删除用户'),
('role:view', '查看角色', 'role', 'view', '查看角色列表'),
('role:create', '创建角色', 'role', 'create', '创建新角色'),
('role:update', '修改角色', 'role', 'update', '修改角色信息'),
('role:delete', '删除角色', 'role', 'delete', '删除角色'),
('permission:assign', '分配权限', 'permission', 'update', '为角色分配权限'),
('config:view', '查看系统配置', 'config', 'view', '查看系统配置'),
('config:update', '修改系统配置', 'config', 'update', '修改系统配置'),
('audit:view', '查看审计日志', 'audit', 'view', '查看审计日志'),
('backup:create', '数据备份', 'backup', 'create', '创建数据备份'),
('backup:restore', '数据恢复', 'backup', 'update', '恢复数据备份');

-- =====================================================
-- 3. 初始化角色权限关联
-- =====================================================

-- 系统管理员权限 (所有权限)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission`;

-- 教务处权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, id FROM `sys_permission` 
WHERE `permission_code` NOT IN ('user:create', 'user:delete', 'role:create', 'role:delete', 'permission:assign', 'backup:create', 'backup:restore');

-- 院系领导权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 3, id FROM `sys_permission` 
WHERE `permission_code` IN (
    'student:view', 'course:view', 'grade:view', 'grade:statistics:view',
    'leave:view', 'leave:approve', 'leave:statistics:view'
);

-- 辅导员权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 4, id FROM `sys_permission` 
WHERE `permission_code` IN (
    'student:view', 'student:update', 'student:export', 'student:status:change', 'student:status:approve',
    'course:view', 'grade:view',
    'leave:view', 'leave:approve', 'leave:statistics:view'
);

-- 授课教师权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 5, id FROM `sys_permission` 
WHERE `permission_code` IN (
    'course:view', 'course:roster:view',
    'grade:view', 'grade:create', 'grade:entry', 'grade:update', 'grade:submit', 'grade:statistics:view'
);

-- 学生权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 6, id FROM `sys_permission` 
WHERE `permission_code` IN (
    'student:view:own', 'student:update:contact', 'student:status:apply',
    'course:view', 'course:selection:create', 'course:selection:delete', 
    'course:selection:view', 'course:schedule:view',
    'grade:view:own',
    'leave:view:own', 'leave:create', 'leave:update', 'leave:cancel', 'leave:return'
);

-- =====================================================
-- 4. 初始化用户账号 (密码: 123456)
-- =====================================================
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000000', 'admin@university.edu.cn', 1),
('academic', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '教务处管理员', '13800000001', 'academic@university.edu.cn', 1),
('teacher01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张老师', '13800000002', 'teacher01@university.edu.cn', 1),
('counselor01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王辅导员', '13800000003', 'counselor01@university.edu.cn', 1),
('student01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李明', '13800000004', 'student01@university.edu.cn', 1);

-- 关联用户角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES 
(1, 1),  -- admin -> 系统管理员
(2, 2),  -- academic -> 教务处
(3, 5),  -- teacher01 -> 授课教师
(4, 4),  -- counselor01 -> 辅导员
(5, 6);  -- student01 -> 学生

-- =====================================================
-- 5. 初始化学生数据
-- =====================================================
INSERT INTO `stu_student` (`user_id`, `student_no`, `name`, `gender`, `birth_date`, `id_number`, `phone`, `email`, `department`, `major`, `class_no`, `enrollment_date`, `academic_status`, `counselor_id`) VALUES
(5, '2024001001', '李明', 'M', '2005-03-15', '110101200503150011', '13800000004', 'student01@university.edu.cn', '计算机学院', '软件工程', '软件2401', '2024-09-01', 'ENROLLED', 4),
(NULL, '2024001002', '王芳', 'F', '2005-06-20', '110101200506200022', '13800000005', 'wangfang@university.edu.cn', '计算机学院', '软件工程', '软件2401', '2024-09-01', 'ENROLLED', 4),
(NULL, '2024001003', '张伟', 'M', '2005-01-10', '110101200501100033', '13800000006', 'zhangwei@university.edu.cn', '计算机学院', '计算机科学', '计科2401', '2024-09-01', 'ENROLLED', 4),
(NULL, '2024001004', '刘洋', 'M', '2005-08-25', '110101200508250044', '13800000007', 'liuyang@university.edu.cn', '计算机学院', '计算机科学', '计科2401', '2024-09-01', 'ENROLLED', 4),
(NULL, '2024001005', '陈静', 'F', '2005-04-12', '110101200504120055', '13800000008', 'chenjing@university.edu.cn', '数学学院', '数学与应用数学', '数学2401', '2024-09-01', 'ENROLLED', 4),
(NULL, '2024002001', '赵强', 'M', '2004-11-08', '110101200411080066', '13800000009', 'zhaoqiang@university.edu.cn', '计算机学院', '软件工程', '软件2301', '2023-09-01', 'ENROLLED', 4),
(NULL, '2024002002', '孙丽', 'F', '2004-07-18', '110101200407180077', '13800000010', 'sunli@university.edu.cn', '计算机学院', '软件工程', '软件2301', '2023-09-01', 'ENROLLED', 4),
(NULL, '2023001001', '周杰', 'M', '2003-09-22', '110101200309220088', '13800000011', 'zhoujie@university.edu.cn', '外国语学院', '英语', '英语2201', '2022-09-01', 'ENROLLED', 4),
(NULL, '2022001001', '吴敏', 'F', '2002-12-05', '110101200212050099', '13800000012', 'wumin@university.edu.cn', '数学学院', '统计学', '统计2101', '2021-09-01', 'GRADUATED', 4),
(NULL, '2024001006', '郑浩', 'M', '2005-02-28', '110101200502280100', '13800000013', 'zhenghao@university.edu.cn', '计算机学院', '人工智能', 'AI2401', '2024-09-01', 'ENROLLED', 4);

-- =====================================================
-- 6. 初始化课程数据
-- enrolled_count 与选课记录保持一致
-- =====================================================
INSERT INTO `crs_course` (`course_code`, `name`, `credits`, `teacher_id`, `teacher_name`, `schedule`, `location`, `capacity`, `enrolled_count`, `semester`, `category`, `department`, `description`) VALUES
('CS101', '计算机导论', 3.0, 3, '张老师', '周一1-2节', '教学楼A101', 100, 7, '2024-2025-1', 'REQUIRED', '计算机学院', '计算机科学基础入门课程'),
('CS201', '数据结构', 4.0, 3, '张老师', '周三3-4节', '教学楼A201', 80, 5, '2024-2025-1', 'REQUIRED', '计算机学院', '数据结构与算法基础'),
('CS301', '数据库原理', 3.5, 3, '张老师', '周五5-6节', '教学楼B101', 60, 2, '2024-2025-1', 'REQUIRED', '计算机学院', '数据库系统原理与应用'),
('CS401', '操作系统', 4.0, 3, '张老师', '周二5-6节', '教学楼A301', 70, 2, '2024-2025-1', 'REQUIRED', '计算机学院', '操作系统原理与实践'),
('CS501', '计算机网络', 3.5, 3, '张老师', '周四1-2节', '教学楼B201', 65, 1, '2024-2025-1', 'REQUIRED', '计算机学院', '计算机网络基础'),
('GE101', '大学英语', 2.0, NULL, '李老师', '周二1-2节', '外语楼201', 120, 6, '2024-2025-1', 'GENERAL', '外国语学院', '大学英语基础课程'),
('GE102', '高等数学', 4.0, NULL, '王老师', '周四3-4节', '理学楼301', 100, 6, '2024-2025-1', 'GENERAL', '数学学院', '高等数学基础课程'),
('GE103', '线性代数', 3.0, NULL, '王老师', '周一5-6节', '理学楼302', 90, 5, '2024-2025-1', 'GENERAL', '数学学院', '线性代数基础课程'),
('GE104', '大学物理', 3.5, NULL, '刘老师', '周三1-2节', '理学楼401', 80, 3, '2024-2025-1', 'GENERAL', '物理学院', '大学物理基础课程'),
('PE101', '体育', 1.0, NULL, '陈老师', '周五3-4节', '体育馆', 150, 5, '2024-2025-1', 'ELECTIVE', '体育部', '大学体育课程');

-- =====================================================
-- 7. 初始化选课记录数据
-- 学生选课情况（逻辑合理：大一新生选基础课，高年级选专业课）
-- =====================================================
INSERT INTO `crs_course_selection` (`student_id`, `course_id`, `status`, `selected_at`) VALUES
-- 李明(id=1, 软件2401大一新生): 计算机导论、大学英语、高等数学、线性代数、体育
(1, 1, 'SELECTED', '2024-08-26 09:15:00'),
(1, 6, 'SELECTED', '2024-08-26 09:20:00'),
(1, 7, 'SELECTED', '2024-08-26 09:25:00'),
(1, 8, 'SELECTED', '2024-08-26 10:00:00'),
(1, 10, 'SELECTED', '2024-08-26 10:05:00'),
-- 王芳(id=2, 软件2401大一新生): 计算机导论、大学英语、高等数学、大学物理、体育
(2, 1, 'SELECTED', '2024-08-26 08:30:00'),
(2, 6, 'SELECTED', '2024-08-26 08:35:00'),
(2, 7, 'SELECTED', '2024-08-26 08:40:00'),
(2, 9, 'SELECTED', '2024-08-26 08:45:00'),
(2, 10, 'SELECTED', '2024-08-26 08:50:00'),
-- 张伟(id=3, 计科2401大一新生): 计算机导论、数据结构、大学英语、高等数学、体育
(3, 1, 'SELECTED', '2024-08-26 10:00:00'),
(3, 2, 'SELECTED', '2024-08-26 10:05:00'),
(3, 6, 'SELECTED', '2024-08-26 10:10:00'),
(3, 7, 'SELECTED', '2024-08-26 10:15:00'),
(3, 10, 'SELECTED', '2024-08-26 10:20:00'),
-- 刘洋(id=4, 计科2401大一新生): 计算机导论、数据结构、大学英语、高等数学、线性代数
(4, 1, 'SELECTED', '2024-08-26 11:00:00'),
(4, 2, 'SELECTED', '2024-08-26 11:05:00'),
(4, 6, 'SELECTED', '2024-08-26 11:10:00'),
(4, 7, 'SELECTED', '2024-08-26 11:15:00'),
(4, 8, 'SELECTED', '2024-08-26 11:20:00'),
-- 陈静(id=5, 数学2401大一新生): 计算机导论、大学英语、高等数学、线性代数、大学物理
(5, 1, 'SELECTED', '2024-08-26 14:00:00'),
(5, 6, 'SELECTED', '2024-08-26 14:05:00'),
(5, 7, 'SELECTED', '2024-08-26 14:10:00'),
(5, 8, 'SELECTED', '2024-08-26 14:15:00'),
(5, 9, 'SELECTED', '2024-08-26 14:20:00'),
-- 赵强(id=6, 软件2301大二): 数据结构、数据库原理、操作系统、计算机网络、大学英语
(6, 2, 'SELECTED', '2024-08-25 09:00:00'),
(6, 3, 'SELECTED', '2024-08-25 09:05:00'),
(6, 4, 'SELECTED', '2024-08-25 09:10:00'),
(6, 5, 'SELECTED', '2024-08-25 09:15:00'),
(6, 6, 'SELECTED', '2024-08-25 09:20:00'),
-- 孙丽(id=7, 软件2301大二): 数据结构、数据库原理、操作系统、线性代数、体育
(7, 2, 'SELECTED', '2024-08-25 10:00:00'),
(7, 3, 'SELECTED', '2024-08-25 10:05:00'),
(7, 4, 'SELECTED', '2024-08-25 10:10:00'),
(7, 8, 'SELECTED', '2024-08-25 10:15:00'),
(7, 10, 'SELECTED', '2024-08-25 10:20:00'),
-- 周杰(id=8, 英语2201大三): 计算机导论、大学英语、体育
(8, 1, 'SELECTED', '2024-08-25 11:00:00'),
(8, 6, 'SELECTED', '2024-08-25 11:05:00'),
(8, 10, 'SELECTED', '2024-08-25 11:10:00'),
-- 郑浩(id=10, AI2401大一新生): 计算机导论、数据结构、高等数学、线性代数、大学物理
(10, 1, 'SELECTED', '2024-08-26 15:00:00'),
(10, 2, 'SELECTED', '2024-08-26 15:05:00'),
(10, 7, 'SELECTED', '2024-08-26 15:10:00'),
(10, 8, 'SELECTED', '2024-08-26 15:15:00'),
(10, 9, 'SELECTED', '2024-08-26 15:20:00');

-- =====================================================
-- 8. 初始化成绩数据
-- 上学期(2023-2024-2)的历史成绩，本学期成绩部分录入中
-- =====================================================
INSERT INTO `grd_grade` (`student_id`, `course_id`, `score`, `letter_grade`, `grade_points`, `grade_type`, `status`, `entered_by`, `entered_at`, `submitted_by`, `submitted_at`, `approved_by`, `approved_at`, `semester`) VALUES
-- 赵强(大二)上学期成绩 - 已审批通过
(6, 1, 85.00, 'B', 3.00, 'NORMAL', 'APPROVED', 3, '2024-06-20 10:00:00', 3, '2024-06-25 10:00:00', 2, '2024-06-28 10:00:00', '2023-2024-2'),
(6, 7, 78.00, 'C', 2.00, 'NORMAL', 'APPROVED', 3, '2024-06-20 10:05:00', 3, '2024-06-25 10:05:00', 2, '2024-06-28 10:05:00', '2023-2024-2'),
-- 孙丽(大二)上学期成绩 - 已审批通过
(7, 1, 92.00, 'A', 4.00, 'NORMAL', 'APPROVED', 3, '2024-06-20 10:10:00', 3, '2024-06-25 10:10:00', 2, '2024-06-28 10:10:00', '2023-2024-2'),
(7, 7, 88.00, 'B', 3.00, 'NORMAL', 'APPROVED', 3, '2024-06-20 10:15:00', 3, '2024-06-25 10:15:00', 2, '2024-06-28 10:15:00', '2023-2024-2'),
-- 周杰(大三)历史成绩 - 已审批通过
(8, 7, 72.00, 'C', 2.00, 'NORMAL', 'APPROVED', 3, '2024-06-20 10:20:00', 3, '2024-06-25 10:20:00', 2, '2024-06-28 10:20:00', '2023-2024-2'),
-- 本学期成绩 - 部分已录入(草稿状态)
(1, 1, 88.00, 'B', 3.00, 'NORMAL', 'DRAFT', 3, '2024-12-15 14:00:00', NULL, NULL, NULL, NULL, '2024-2025-1'),
(2, 1, 95.00, 'A', 4.00, 'NORMAL', 'DRAFT', 3, '2024-12-15 14:05:00', NULL, NULL, NULL, NULL, '2024-2025-1'),
(3, 1, 82.00, 'B', 3.00, 'NORMAL', 'DRAFT', 3, '2024-12-15 14:10:00', NULL, NULL, NULL, NULL, '2024-2025-1'),
(4, 1, 76.00, 'C', 2.00, 'NORMAL', 'DRAFT', 3, '2024-12-15 14:15:00', NULL, NULL, NULL, NULL, '2024-2025-1'),
(5, 1, 90.00, 'A', 4.00, 'NORMAL', 'DRAFT', 3, '2024-12-15 14:20:00', NULL, NULL, NULL, NULL, '2024-2025-1'),
-- 数据结构成绩 - 已提交待审批
(3, 2, 85.00, 'B', 3.00, 'NORMAL', 'SUBMITTED', 3, '2024-12-10 10:00:00', 3, '2024-12-12 10:00:00', NULL, NULL, '2024-2025-1'),
(4, 2, 79.00, 'C', 2.00, 'NORMAL', 'SUBMITTED', 3, '2024-12-10 10:05:00', 3, '2024-12-12 10:05:00', NULL, NULL, '2024-2025-1'),
(6, 2, 91.00, 'A', 4.00, 'NORMAL', 'SUBMITTED', 3, '2024-12-10 10:10:00', 3, '2024-12-12 10:10:00', NULL, NULL, '2024-2025-1'),
(7, 2, 87.00, 'B', 3.00, 'NORMAL', 'SUBMITTED', 3, '2024-12-10 10:15:00', 3, '2024-12-12 10:15:00', NULL, NULL, '2024-2025-1'),
(10, 2, 93.00, 'A', 4.00, 'NORMAL', 'SUBMITTED', 3, '2024-12-10 10:20:00', 3, '2024-12-12 10:20:00', NULL, NULL, '2024-2025-1');

-- =====================================================
-- 9. 初始化请假记录数据
-- =====================================================
INSERT INTO `lev_leave_request` (`student_id`, `leave_type`, `start_date`, `end_date`, `duration`, `reason`, `status`, `is_urgent`, `current_approver_id`, `return_date`, `return_registered_at`, `created_at`) VALUES
-- 李明: 病假2天，已完成销假
(1, 'SICK', '2024-10-15', '2024-10-16', 2.0, '感冒发烧，需要休息治疗', 'COMPLETED', 0, NULL, '2024-10-16', '2024-10-16 18:00:00', '2024-10-14 20:00:00'),
-- 王芳: 事假1天，已批准，待销假
(2, 'PERSONAL', '2024-12-20', '2024-12-20', 1.0, '家中有事需要回家处理', 'APPROVED', 0, NULL, NULL, NULL, '2024-12-18 10:00:00'),
-- 张伟: 病假3天，辅导员已批准，等待院系领导审批
(3, 'SICK', '2024-12-22', '2024-12-24', 3.0, '急性肠胃炎，医生建议休息三天', 'COUNSELOR_APPROVED', 1, 3, NULL, NULL, '2024-12-21 08:00:00'),
-- 刘洋: 事假申请中，等待辅导员审批
(4, 'PERSONAL', '2024-12-25', '2024-12-26', 2.0, '参加家庭重要活动', 'PENDING', 0, 4, NULL, NULL, '2024-12-20 15:00:00'),
-- 赵强: 公假3天，参加比赛，已完成
(6, 'OFFICIAL', '2024-11-10', '2024-11-12', 3.0, '代表学校参加省级程序设计竞赛', 'COMPLETED', 0, NULL, '2024-11-12', '2024-11-12 20:00:00', '2024-11-05 09:00:00'),
-- 孙丽: 病假1天，已被拒绝（材料不全）
(7, 'SICK', '2024-11-20', '2024-11-20', 1.0, '身体不适', 'REJECTED', 0, NULL, NULL, NULL, '2024-11-19 22:00:00'),
-- 周杰: 事假申请中
(8, 'PERSONAL', '2024-12-28', '2024-12-29', 2.0, '办理出国留学相关手续', 'PENDING', 0, 4, NULL, NULL, '2024-12-22 14:00:00');

-- =====================================================
-- 10. 初始化请假审批记录
-- =====================================================
INSERT INTO `lev_leave_approval` (`leave_request_id`, `approver_id`, `approver_name`, `approver_role`, `approval_order`, `action`, `comment`, `created_at`) VALUES
-- 李明病假审批记录
(1, 4, '王辅导员', 'COUNSELOR', 1, 'APPROVE', '同意，注意休息，按时返校', '2024-10-14 21:00:00'),
-- 王芳事假审批记录
(2, 4, '王辅导员', 'COUNSELOR', 1, 'APPROVE', '同意，注意安全', '2024-12-18 14:00:00'),
-- 张伟病假审批记录（辅导员已批，等待院系）
(3, 4, '王辅导员', 'COUNSELOR', 1, 'APPROVE', '情况属实，建议批准', '2024-12-21 09:00:00'),
-- 赵强公假审批记录
(5, 4, '王辅导员', 'COUNSELOR', 1, 'APPROVE', '支持参加比赛，为校争光', '2024-11-05 10:00:00'),
(5, 2, '教务处管理员', 'ACADEMIC_AFFAIRS', 2, 'APPROVE', '同意，预祝取得好成绩', '2024-11-05 14:00:00'),
-- 孙丽病假被拒记录
(6, 4, '王辅导员', 'COUNSELOR', 1, 'REJECT', '请提供医院证明材料后重新申请', '2024-11-20 08:00:00');

-- =====================================================
-- 11. 初始化审计日志数据
-- =====================================================
INSERT INTO `sys_audit_log` (`user_id`, `username`, `module`, `operation`, `method`, `request_url`, `request_method`, `ip_address`, `execution_time`, `status`, `created_at`) VALUES
(1, 'admin', '系统管理', '用户登录', 'AuthController.login', '/api/auth/login', 'POST', '192.168.1.100', 156, 1, '2024-12-20 08:30:00'),
(1, 'admin', '系统管理', '查看用户列表', 'UserController.list', '/api/system/users', 'GET', '192.168.1.100', 45, 1, '2024-12-20 08:31:00'),
(3, 'teacher01', '成绩管理', '录入成绩', 'GradeController.batchSave', '/api/grades/batch', 'POST', '192.168.1.101', 234, 1, '2024-12-15 14:00:00'),
(3, 'teacher01', '成绩管理', '提交成绩', 'GradeController.submit', '/api/grades/submit/2', 'POST', '192.168.1.101', 123, 1, '2024-12-12 10:00:00'),
(4, 'counselor01', '请假管理', '审批请假', 'LeaveController.approve', '/api/leaves/1/approve', 'PUT', '192.168.1.102', 89, 1, '2024-10-14 21:00:00'),
(4, 'counselor01', '请假管理', '审批请假', 'LeaveController.approve', '/api/leaves/2/approve', 'PUT', '192.168.1.102', 76, 1, '2024-12-18 14:00:00'),
(5, 'student01', '选课管理', '学生选课', 'CourseSelectionController.select', '/api/course-selections', 'POST', '192.168.1.103', 156, 1, '2024-08-26 09:15:00'),
(5, 'student01', '请假管理', '提交请假', 'LeaveController.create', '/api/leaves', 'POST', '192.168.1.103', 112, 1, '2024-10-14 20:00:00'),
(2, 'academic', '成绩管理', '审批成绩', 'GradeController.approve', '/api/grades/approve/1', 'POST', '192.168.1.104', 98, 1, '2024-06-28 10:00:00'),
(1, 'admin', '系统管理', '修改系统配置', 'ConfigController.update', '/api/system/config', 'PUT', '192.168.1.100', 67, 1, '2024-12-19 16:00:00');

-- =====================================================
-- 13. 初始化系统配置
-- =====================================================
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `config_name`, `description`, `sort_order`) VALUES
('current_semester', '2024-2025-1', 'ACADEMIC', '当前学期', '当前学期编号', 1),
('semester_start_date', '2024-09-01', 'ACADEMIC', '学期开始日期', '当前学期开始日期', 2),
('semester_end_date', '2025-01-15', 'ACADEMIC', '学期结束日期', '当前学期结束日期', 3),
('selection_start_time', '2024-08-25 08:00:00', 'SELECTION', '选课开始时间', '选课总开始时间（兼容旧版）', 10),
('selection_end_time', '2024-09-05 23:59:59', 'SELECTION', '选课结束时间', '选课总结束时间（兼容旧版）', 11),
('selection_phase', 'CLOSED', 'SELECTION', '选课阶段', '手动选课阶段（当未配置分阶段时间时使用）', 12),
('pre_selection_start_time', '2024-08-25 08:00:00', 'SELECTION', '预选开始时间', '预选阶段开始时间', 13),
('pre_selection_end_time', '2024-08-30 23:59:59', 'SELECTION', '预选结束时间', '预选阶段结束时间', 14),
('formal_selection_start_time', '2024-09-01 08:00:00', 'SELECTION', '正选开始时间', '正选阶段开始时间', 15),
('formal_selection_end_time', '2024-09-03 23:59:59', 'SELECTION', '正选结束时间', '正选阶段结束时间', 16),
('adjustment_start_time', '2024-09-04 08:00:00', 'SELECTION', '补退选开始时间', '补退选阶段开始时间', 17),
('adjustment_end_time', '2024-09-05 23:59:59', 'SELECTION', '补退选结束时间', '补退选阶段结束时间', 18),
('lottery_enabled', 'true', 'SELECTION', '启用抽签', '预选阶段是否启用抽签机制', 19),
('leave_counselor_max_days', '3', 'SYSTEM', '辅导员审批最大天数', '辅导员可审批的最大请假天数', 20),
('grade_scale_a_min', '90', 'SYSTEM', 'A等级最低分', 'A等级最低分数', 30),
('grade_scale_b_min', '80', 'SYSTEM', 'B等级最低分', 'B等级最低分数', 31),
('grade_scale_c_min', '70', 'SYSTEM', 'C等级最低分', 'C等级最低分数', 32),
('grade_scale_d_min', '60', 'SYSTEM', 'D等级最低分', 'D等级最低分数', 33),
('system_name', '高校学生信息管理系统', 'SYSTEM', '系统名称', '系统显示名称', 40);


-- =====================================================
-- 14. 初始化数据字典
-- =====================================================

-- 数据字典类型
INSERT INTO `sys_dict_type` (`dict_code`, `dict_name`, `description`, `is_system`, `status`, `sort_order`) VALUES
('academic_status', '学籍状态', '学生学籍状态枚举', 1, 1, 1),
('course_category', '课程类别', '课程分类枚举', 1, 1, 2),
('grade_status', '成绩状态', '成绩审批状态枚举', 1, 1, 3),
('leave_type', '请假类型', '请假类型枚举', 1, 1, 4),
('leave_status', '请假状态', '请假审批状态枚举', 1, 1, 5),
('selection_status', '选课状态', '选课状态枚举', 1, 1, 6),
('gender', '性别', '性别枚举', 1, 1, 7);

-- 数据字典项
INSERT INTO `sys_dict_item` (`dict_type_id`, `dict_code`, `item_value`, `item_label`, `description`, `css_class`, `status`, `sort_order`) VALUES
-- 学籍状态
(1, 'academic_status', 'ENROLLED', '在读', '正常在校学习', 'success', 1, 1),
(1, 'academic_status', 'SUSPENDED', '休学', '暂时休学', 'warning', 1, 2),
(1, 'academic_status', 'WITHDRAWN', '退学', '已退学', 'danger', 1, 3),
(1, 'academic_status', 'GRADUATED', '毕业', '已毕业', 'info', 1, 4),
(1, 'academic_status', 'TRANSFERRED', '转学', '已转学', 'info', 1, 5),
-- 课程类别
(2, 'course_category', 'REQUIRED', '必修', '必修课程', 'danger', 1, 1),
(2, 'course_category', 'ELECTIVE', '选修', '选修课程', 'success', 1, 2),
(2, 'course_category', 'GENERAL', '通识', '通识教育课程', 'info', 1, 3),
-- 成绩状态
(3, 'grade_status', 'DRAFT', '草稿', '成绩录入中', 'info', 1, 1),
(3, 'grade_status', 'SUBMITTED', '已提交', '已提交待审批', 'warning', 1, 2),
(3, 'grade_status', 'APPROVED', '已审批', '审批通过', 'success', 1, 3),
-- 请假类型
(4, 'leave_type', 'SICK', '病假', '因病请假', 'warning', 1, 1),
(4, 'leave_type', 'PERSONAL', '事假', '因事请假', 'info', 1, 2),
(4, 'leave_type', 'OFFICIAL', '公假', '公务请假', 'success', 1, 3),
-- 请假状态
(5, 'leave_status', 'PENDING', '待审批', '等待审批', 'info', 1, 1),
(5, 'leave_status', 'COUNSELOR_APPROVED', '辅导员已批', '辅导员已审批', 'warning', 1, 2),
(5, 'leave_status', 'APPROVED', '已批准', '审批通过', 'success', 1, 3),
(5, 'leave_status', 'REJECTED', '已拒绝', '审批拒绝', 'danger', 1, 4),
(5, 'leave_status', 'COMPLETED', '已销假', '已完成销假', 'success', 1, 5),
-- 选课状态
(6, 'selection_status', 'SELECTED', '已选', '已选课', 'success', 1, 1),
(6, 'selection_status', 'WITHDRAWN', '已退', '已退课', 'info', 1, 2),
(6, 'selection_status', 'LOTTERY_PENDING', '待抽签', '等待抽签', 'warning', 1, 3),
(6, 'selection_status', 'LOTTERY_FAILED', '抽签未中', '抽签未中签', 'danger', 1, 4),
-- 性别
(7, 'gender', 'M', '男', '男性', '', 1, 1),
(7, 'gender', 'F', '女', '女性', '', 1, 2);

-- =====================================================
-- 通知模板初始数据
-- =====================================================
INSERT INTO `sys_notification_template` (`template_code`, `template_name`, `category`, `subject`, `content`, `variables`, `channel`, `status`, `is_system`) VALUES
('COURSE_SELECTED', '选课成功通知', 'COURSE', '选课成功 - ${courseName}', '同学你好，你已成功选修课程【${courseName}】（${courseCode}），上课时间：${schedule}，上课地点：${location}，任课教师：${teacherName}。请按时上课。', '["courseName","courseCode","schedule","location","teacherName"]', 'SITE', 1, 1),
('COURSE_LOTTERY_RESULT', '选课抽签结果通知', 'COURSE', '选课抽签结果 - ${courseName}', '同学你好，课程【${courseName}】（${courseCode}）的抽签已完成。你的抽签结果为：${result}。${message}', '["courseName","courseCode","result","message"]', 'SITE', 1, 1),
('GRADE_PUBLISHED', '成绩发布通知', 'GRADE', '${semester} 成绩已发布', '同学你好，${semester} 学期课程【${courseName}】的成绩已发布，你的成绩为 ${score} 分（${letterGrade}），绩点 ${gradePoints}。', '["semester","courseName","score","letterGrade","gradePoints"]', 'SITE', 1, 1),
('LEAVE_APPROVED', '请假审批通过通知', 'LEAVE', '请假申请已批准', '同学你好，你于 ${applyDate} 提交的${leaveType}申请（${startDate} 至 ${endDate}）已审批通过。请注意按时销假。', '["applyDate","leaveType","startDate","endDate"]', 'SITE', 1, 1),
('LEAVE_REJECTED', '请假审批拒绝通知', 'LEAVE', '请假申请未通过', '同学你好，你于 ${applyDate} 提交的${leaveType}申请（${startDate} 至 ${endDate}）未通过审批。原因：${reason}。如有疑问请联系辅导员。', '["applyDate","leaveType","startDate","endDate","reason"]', 'SITE', 1, 1),
('STATUS_CHANGE_RESULT', '学籍异动审批结果', 'ACADEMIC', '学籍异动申请${result}', '同学你好，你提交的学籍异动申请（${changeType}）审批结果为：${result}。${message}', '["changeType","result","message"]', 'SITE', 1, 1),
('SYSTEM_ANNOUNCEMENT', '系统公告', 'SYSTEM', '${title}', '${content}', '["title","content"]', 'ALL', 1, 1),
('SEMESTER_START', '新学期开学通知', 'ACADEMIC', '${semester} 开学通知', '各位同学，${semester} 学期将于 ${startDate} 正式开学。选课时间：${selectionStart} 至 ${selectionEnd}，请及时完成选课。', '["semester","startDate","selectionStart","selectionEnd"]', 'ALL', 1, 1);
