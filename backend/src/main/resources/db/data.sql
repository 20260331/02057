-- =====================================================
-- 学生信息管理系统初始化数据
-- Database: MySQL 8.0
-- Charset: utf8mb4
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
('student:status:change', '变更学籍状态', 'student', 'update', '变更学生学籍状态');


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

-- 教务处权限 (除系统管理外的所有权限)
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
    'student:view', 'student:update', 'student:export', 'student:status:change',
    'course:view', 'grade:view',
    'leave:view', 'leave:approve', 'leave:statistics:view'
);

-- 授课教师权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 5, id FROM `sys_permission` 
WHERE `permission_code` IN (
    'course:view', 'course:roster:view',
    'grade:view', 'grade:create', 'grade:update', 'grade:submit', 'grade:statistics:view'
);

-- 学生权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 6, id FROM `sys_permission` 
WHERE `permission_code` IN (
    'student:view:own', 'student:update:contact',
    'course:view', 'course:selection:create', 'course:selection:delete', 
    'course:selection:view', 'course:schedule:view',
    'grade:view:own',
    'leave:view:own', 'leave:create', 'leave:update', 'leave:cancel', 'leave:return'
);

-- =====================================================
-- 4. 初始化管理员账号
-- =====================================================
-- 密码: admin123 (BCrypt加密)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `status`) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '系统管理员', '13800000000', 'admin@university.edu.cn', 1);

-- 测试用户 (密码: 123456)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `status`) VALUES
('teacher01', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '张老师', '13800000001', 'teacher01@university.edu.cn', 1),
('student01', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '李明', '13800000002', 'student01@university.edu.cn', 1),
('counselor01', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '王辅导员', '13800000003', 'counselor01@university.edu.cn', 1);

-- 关联用户角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES 
(1, 1),  -- admin -> 系统管理员
(2, 5),  -- teacher01 -> 授课教师
(3, 6),  -- student01 -> 学生
(4, 4);  -- counselor01 -> 辅导员

-- =====================================================
-- 5. 初始化系统配置
-- =====================================================
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `config_name`, `description`, `sort_order`) VALUES
-- 学期配置
('current_semester', '2024-2025-1', 'ACADEMIC', '当前学期', '当前学期编号', 1),
('semester_start_date', '2024-09-01', 'ACADEMIC', '学期开始日期', '当前学期开始日期', 2),
('semester_end_date', '2025-01-15', 'ACADEMIC', '学期结束日期', '当前学期结束日期', 3),

-- 选课配置
('selection_start_time', '2024-08-25 08:00:00', 'SELECTION', '选课开始时间', '选课开始时间', 10),
('selection_end_time', '2024-09-05 23:59:59', 'SELECTION', '选课结束时间', '选课结束时间', 11),
('selection_phase', 'CLOSED', 'SELECTION', '选课阶段', '当前选课阶段: FIRST_ROUND-第一轮, SECOND_ROUND-第二轮, LOTTERY-抽签, CLOSED-关闭', 12),
('lottery_enabled', 'true', 'SELECTION', '启用抽签', '是否启用选课抽签机制', 13),

-- 请假配置
('leave_counselor_max_days', '3', 'SYSTEM', '辅导员审批最大天数', '辅导员可审批的最大请假天数', 20),
('leave_urgent_enabled', 'true', 'SYSTEM', '启用紧急请假', '是否启用紧急请假通道', 21),

-- 成绩配置
('grade_scale_a_min', '90', 'SYSTEM', 'A等级最低分', 'A等级(4.0绩点)最低分数', 30),
('grade_scale_b_min', '80', 'SYSTEM', 'B等级最低分', 'B等级(3.0绩点)最低分数', 31),
('grade_scale_c_min', '70', 'SYSTEM', 'C等级最低分', 'C等级(2.0绩点)最低分数', 32),
('grade_scale_d_min', '60', 'SYSTEM', 'D等级最低分', 'D等级(1.0绩点)最低分数', 33),

-- 系统配置
('system_name', '高校学生信息管理系统', 'SYSTEM', '系统名称', '系统显示名称', 40),
('system_logo', '/logo.png', 'SYSTEM', '系统Logo', '系统Logo路径', 41),
('audit_log_retention_years', '3', 'SYSTEM', '审计日志保留年限', '审计日志保留年限', 42);


-- =====================================================
-- 6. 初始化数据字典
-- =====================================================
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `config_name`, `description`, `sort_order`) VALUES
-- 性别字典
('dict_gender_M', '男', 'DICT', '性别-男', '性别数据字典', 100),
('dict_gender_F', '女', 'DICT', '性别-女', '性别数据字典', 101),

-- 学籍状态字典
('dict_academic_status_ENROLLED', '在读', 'DICT', '学籍状态-在读', '学籍状态数据字典', 110),
('dict_academic_status_SUSPENDED', '休学', 'DICT', '学籍状态-休学', '学籍状态数据字典', 111),
('dict_academic_status_WITHDRAWN', '退学', 'DICT', '学籍状态-退学', '学籍状态数据字典', 112),
('dict_academic_status_GRADUATED', '毕业', 'DICT', '学籍状态-毕业', '学籍状态数据字典', 113),
('dict_academic_status_TRANSFERRED', '转学', 'DICT', '学籍状态-转学', '学籍状态数据字典', 114),

-- 课程类别字典
('dict_course_category_REQUIRED', '必修', 'DICT', '课程类别-必修', '课程类别数据字典', 120),
('dict_course_category_ELECTIVE', '选修', 'DICT', '课程类别-选修', '课程类别数据字典', 121),
('dict_course_category_GENERAL', '通识', 'DICT', '课程类别-通识', '课程类别数据字典', 122),

-- 选课状态字典
('dict_selection_status_SELECTED', '已选', 'DICT', '选课状态-已选', '选课状态数据字典', 130),
('dict_selection_status_WITHDRAWN', '已退', 'DICT', '选课状态-已退', '选课状态数据字典', 131),
('dict_selection_status_LOTTERY_PENDING', '待抽签', 'DICT', '选课状态-待抽签', '选课状态数据字典', 132),
('dict_selection_status_LOTTERY_FAILED', '抽签未中', 'DICT', '选课状态-抽签未中', '选课状态数据字典', 133),

-- 成绩状态字典
('dict_grade_status_DRAFT', '草稿', 'DICT', '成绩状态-草稿', '成绩状态数据字典', 140),
('dict_grade_status_SUBMITTED', '已提交', 'DICT', '成绩状态-已提交', '成绩状态数据字典', 141),
('dict_grade_status_APPROVED', '已确认', 'DICT', '成绩状态-已确认', '成绩状态数据字典', 142),

-- 请假类型字典
('dict_leave_type_SICK', '病假', 'DICT', '请假类型-病假', '请假类型数据字典', 150),
('dict_leave_type_PERSONAL', '事假', 'DICT', '请假类型-事假', '请假类型数据字典', 151),
('dict_leave_type_OFFICIAL', '公假', 'DICT', '请假类型-公假', '请假类型数据字典', 152),

-- 请假状态字典
('dict_leave_status_PENDING', '待审批', 'DICT', '请假状态-待审批', '请假状态数据字典', 160),
('dict_leave_status_COUNSELOR_APPROVED', '辅导员已批', 'DICT', '请假状态-辅导员已批', '请假状态数据字典', 161),
('dict_leave_status_APPROVED', '已批准', 'DICT', '请假状态-已批准', '请假状态数据字典', 162),
('dict_leave_status_REJECTED', '已拒绝', 'DICT', '请假状态-已拒绝', '请假状态数据字典', 163),
('dict_leave_status_COMPLETED', '已销假', 'DICT', '请假状态-已销假', '请假状态数据字典', 164),
('dict_leave_status_CANCELLED', '已取消', 'DICT', '请假状态-已取消', '请假状态数据字典', 165);
