-- =====================================================
-- 学生信息管理系统数据库表结构
-- Database: MySQL 8.0
-- Charset: utf8mb4
-- =====================================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 用户权限相关表
-- =====================================================

-- 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `login_fail_count` INT NOT NULL DEFAULT 0 COMMENT '登录失败次数',
    `lock_time` DATETIME DEFAULT NULL COMMENT '账户锁定时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';


-- 权限表
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `resource` VARCHAR(100) NOT NULL COMMENT '资源类型',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型: view, create, update, delete, export, import',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_resource` (`resource`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限表';

-- 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =====================================================
-- 2. 学生模块表
-- =====================================================

-- 学生信息表
DROP TABLE IF EXISTS `stu_student`;
CREATE TABLE `stu_student` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `student_no` VARCHAR(20) NOT NULL COMMENT '学号',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` CHAR(1) NOT NULL COMMENT '性别: M-男, F-女',
    `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
    `id_number` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '家庭住址',
    `department` VARCHAR(100) NOT NULL COMMENT '院系',
    `major` VARCHAR(100) NOT NULL COMMENT '专业',
    `class_no` VARCHAR(50) NOT NULL COMMENT '班级',
    `enrollment_date` DATE NOT NULL COMMENT '入学日期',
    `graduation_date` DATE DEFAULT NULL COMMENT '预计毕业日期',
    `academic_status` VARCHAR(20) NOT NULL DEFAULT 'ENROLLED' COMMENT '学籍状态: ENROLLED-在读, SUSPENDED-休学, WITHDRAWN-退学, GRADUATED-毕业, TRANSFERRED-转学',
    `counselor_id` BIGINT DEFAULT NULL COMMENT '辅导员ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_department` (`department`),
    KEY `idx_major` (`major`),
    KEY `idx_class_no` (`class_no`),
    KEY `idx_academic_status` (`academic_status`),
    KEY `idx_counselor_id` (`counselor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生信息表';


-- 学生信息变更日志表
DROP TABLE IF EXISTS `stu_student_change_log`;
CREATE TABLE `stu_student_change_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `field_name` VARCHAR(50) NOT NULL COMMENT '变更字段名',
    `field_label` VARCHAR(50) NOT NULL COMMENT '变更字段标签',
    `old_value` TEXT DEFAULT NULL COMMENT '旧值',
    `new_value` TEXT DEFAULT NULL COMMENT '新值',
    `change_type` VARCHAR(20) NOT NULL COMMENT '变更类型: UPDATE-修改, STATUS_CHANGE-状态变更',
    `change_reason` VARCHAR(500) DEFAULT NULL COMMENT '变更原因',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_field_name` (`field_name`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生信息变更日志表';

-- 学籍异动申请表
DROP TABLE IF EXISTS `stu_status_change_application`;
CREATE TABLE `stu_status_change_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `current_status` VARCHAR(20) NOT NULL COMMENT '当前学籍状态',
    `target_status` VARCHAR(20) NOT NULL COMMENT '目标学籍状态',
    `reason` TEXT NOT NULL COMMENT '申请原因',
    `effective_date` DATE NOT NULL COMMENT '期望生效日期',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态: PENDING-待审批, COUNSELOR_APPROVED-辅导员已批, APPROVED-已通过, REJECTED-已拒绝, CANCELLED-已取消',
    `current_approver_id` BIGINT DEFAULT NULL COMMENT '当前审批人ID',
    `approved_at` DATETIME DEFAULT NULL COMMENT '最终审批时间',
    `executed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已执行状态变更: 0-否, 1-是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_status` (`status`),
    KEY `idx_target_status` (`target_status`),
    KEY `idx_current_approver_id` (`current_approver_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学籍异动申请表';

-- 学籍异动审批记录表
DROP TABLE IF EXISTS `stu_status_change_approval`;
CREATE TABLE `stu_status_change_approval` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `application_id` BIGINT NOT NULL COMMENT '申请ID',
    `approver_id` BIGINT NOT NULL COMMENT '审批人ID',
    `approver_name` VARCHAR(50) NOT NULL COMMENT '审批人姓名',
    `approver_role` VARCHAR(50) NOT NULL COMMENT '审批人角色',
    `approval_order` INT NOT NULL COMMENT '审批顺序',
    `action` VARCHAR(20) NOT NULL COMMENT '审批动作: APPROVE-批准, REJECT-拒绝',
    `comment` TEXT DEFAULT NULL COMMENT '审批意见',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    PRIMARY KEY (`id`),
    KEY `idx_application_id` (`application_id`),
    KEY `idx_approver_id` (`approver_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学籍异动审批记录表';

-- 学籍证明记录表
DROP TABLE IF EXISTS `stu_certificate_record`;
CREATE TABLE `stu_certificate_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `cert_no` VARCHAR(50) NOT NULL COMMENT '证明编号（唯一）',
    `cert_type` VARCHAR(30) NOT NULL COMMENT '证明类型: ENROLLMENT-学籍证明, ATTENDANCE-在读证明, GRADUATION-毕业证明, TRANSCRIPT-成绩证明, STATUS_CHANGE-学籍异动证明',
    `cert_title` VARCHAR(100) NOT NULL COMMENT '证明标题',
    `cert_content` TEXT NOT NULL COMMENT '证明正文内容(HTML)',
    `purpose` VARCHAR(200) DEFAULT NULL COMMENT '用途说明',
    `copies` INT NOT NULL DEFAULT 1 COMMENT '份数',
    `status` VARCHAR(20) NOT NULL DEFAULT 'VALID' COMMENT '状态: VALID-有效, REVOKED-已作废',
    `generated_by` BIGINT NOT NULL COMMENT '生成人ID',
    `generated_by_name` VARCHAR(50) NOT NULL COMMENT '生成人姓名',
    `revoked_at` DATETIME DEFAULT NULL COMMENT '作废时间',
    `revoke_reason` VARCHAR(500) DEFAULT NULL COMMENT '作废原因',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cert_no` (`cert_no`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_cert_type` (`cert_type`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学籍证明记录表';

-- =====================================================
-- 3. 选课模块表
-- =====================================================

-- 课程信息表
DROP TABLE IF EXISTS `crs_course`;
CREATE TABLE `crs_course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `course_code` VARCHAR(20) NOT NULL COMMENT '课程编号',
    `name` VARCHAR(100) NOT NULL COMMENT '课程名称',
    `credits` DECIMAL(3,1) NOT NULL COMMENT '学分',
    `teacher_id` BIGINT DEFAULT NULL COMMENT '授课教师ID',
    `teacher_name` VARCHAR(50) DEFAULT NULL COMMENT '授课教师姓名',
    `schedule` VARCHAR(100) DEFAULT NULL COMMENT '上课时间(如: 周一1-2节)',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '上课地点',
    `capacity` INT NOT NULL DEFAULT 0 COMMENT '课程容量',
    `enrolled_count` INT NOT NULL DEFAULT 0 COMMENT '已选人数',
    `semester` VARCHAR(20) NOT NULL COMMENT '学期(如: 2024-2025-1)',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '课程类别: REQUIRED-必修, ELECTIVE-选修, GENERAL-通识',
    `department` VARCHAR(100) DEFAULT NULL COMMENT '开课院系',
    `description` TEXT DEFAULT NULL COMMENT '课程描述',
    `syllabus` TEXT DEFAULT NULL COMMENT '教学大纲',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停开, 1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_code_semester` (`course_code`, `semester`),
    KEY `idx_teacher_id` (`teacher_id`),
    KEY `idx_semester` (`semester`),
    KEY `idx_category` (`category`),
    KEY `idx_department` (`department`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程信息表';

-- 选课记录表
DROP TABLE IF EXISTS `crs_course_selection`;
CREATE TABLE `crs_course_selection` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'SELECTED' COMMENT '状态: SELECTED-已选, WITHDRAWN-已退, LOTTERY_PENDING-待抽签, LOTTERY_FAILED-抽签未中',
    `selected_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    `withdrawn_at` DATETIME DEFAULT NULL COMMENT '退课时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_course` (`student_id`, `course_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课记录表';


-- 课程先修要求表
DROP TABLE IF EXISTS `crs_course_prerequisite`;
CREATE TABLE `crs_course_prerequisite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `prerequisite_course_id` BIGINT NOT NULL COMMENT '先修课程ID',
    `min_grade` DECIMAL(5,2) DEFAULT 60.00 COMMENT '最低成绩要求',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_prerequisite` (`course_id`, `prerequisite_course_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_prerequisite_course_id` (`prerequisite_course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程先修要求表';

-- =====================================================
-- 4. 成绩模块表
-- =====================================================

-- 成绩表
DROP TABLE IF EXISTS `grd_grade`;
CREATE TABLE `grd_grade` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `score` DECIMAL(5,2) DEFAULT NULL COMMENT '分数(0-100)',
    `letter_grade` VARCHAR(5) DEFAULT NULL COMMENT '等级: A, B, C, D, F',
    `grade_points` DECIMAL(3,2) DEFAULT NULL COMMENT '绩点',
    `grade_type` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '成绩类型: NORMAL-正常, PASS_FAIL-通过/不通过, AUDIT-旁听',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, SUBMITTED-已提交, APPROVED-已确认',
    `entered_by` BIGINT DEFAULT NULL COMMENT '录入人ID',
    `entered_at` DATETIME DEFAULT NULL COMMENT '录入时间',
    `submitted_by` BIGINT DEFAULT NULL COMMENT '提交人ID',
    `submitted_at` DATETIME DEFAULT NULL COMMENT '提交时间',
    `approved_by` BIGINT DEFAULT NULL COMMENT '审批人ID',
    `approved_at` DATETIME DEFAULT NULL COMMENT '审批时间',
    `semester` VARCHAR(20) NOT NULL COMMENT '学期',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_course` (`student_id`, `course_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_status` (`status`),
    KEY `idx_semester` (`semester`),
    KEY `idx_entered_by` (`entered_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩表';

-- 成绩变更日志表
DROP TABLE IF EXISTS `grd_grade_change_log`;
CREATE TABLE `grd_grade_change_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `grade_id` BIGINT NOT NULL COMMENT '成绩ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `old_score` DECIMAL(5,2) DEFAULT NULL COMMENT '原分数',
    `new_score` DECIMAL(5,2) DEFAULT NULL COMMENT '新分数',
    `old_letter_grade` VARCHAR(5) DEFAULT NULL COMMENT '原等级',
    `new_letter_grade` VARCHAR(5) DEFAULT NULL COMMENT '新等级',
    `change_reason` VARCHAR(500) NOT NULL COMMENT '修改原因',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_grade_id` (`grade_id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩变更日志表';


-- =====================================================
-- 5. 请假模块表
-- =====================================================

-- 请假申请表
DROP TABLE IF EXISTS `lev_leave_request`;
CREATE TABLE `lev_leave_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `leave_type` VARCHAR(20) NOT NULL COMMENT '请假类型: SICK-病假, PERSONAL-事假, OFFICIAL-公假',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `duration` DECIMAL(5,1) NOT NULL COMMENT '请假时长(天)',
    `reason` TEXT NOT NULL COMMENT '请假原因',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待审批, COUNSELOR_APPROVED-辅导员已批, APPROVED-已批准, REJECTED-已拒绝, COMPLETED-已销假, CANCELLED-已取消',
    `is_urgent` TINYINT NOT NULL DEFAULT 0 COMMENT '是否紧急: 0-否, 1-是',
    `current_approver_id` BIGINT DEFAULT NULL COMMENT '当前审批人ID',
    `return_date` DATE DEFAULT NULL COMMENT '实际返校日期',
    `return_registered_at` DATETIME DEFAULT NULL COMMENT '销假登记时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_status` (`status`),
    KEY `idx_leave_type` (`leave_type`),
    KEY `idx_start_date` (`start_date`),
    KEY `idx_current_approver_id` (`current_approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请表';

-- 请假审批记录表
DROP TABLE IF EXISTS `lev_leave_approval`;
CREATE TABLE `lev_leave_approval` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `leave_request_id` BIGINT NOT NULL COMMENT '请假申请ID',
    `approver_id` BIGINT NOT NULL COMMENT '审批人ID',
    `approver_name` VARCHAR(50) NOT NULL COMMENT '审批人姓名',
    `approver_role` VARCHAR(50) NOT NULL COMMENT '审批人角色',
    `approval_order` INT NOT NULL COMMENT '审批顺序',
    `action` VARCHAR(20) NOT NULL COMMENT '审批动作: APPROVE-批准, REJECT-拒绝, FORWARD-转交',
    `comment` TEXT DEFAULT NULL COMMENT '审批意见',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    PRIMARY KEY (`id`),
    KEY `idx_leave_request_id` (`leave_request_id`),
    KEY `idx_approver_id` (`approver_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假审批记录表';

-- 请假附件表
DROP TABLE IF EXISTS `lev_leave_attachment`;
CREATE TABLE `lev_leave_attachment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `leave_request_id` BIGINT NOT NULL COMMENT '请假申请ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
    `file_type` VARCHAR(100) DEFAULT NULL COMMENT '文件类型',
    `uploaded_by` BIGINT NOT NULL COMMENT '上传人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_leave_request_id` (`leave_request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假附件表';


-- =====================================================
-- 6. 系统配置表
-- =====================================================

-- 系统配置表
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT NOT NULL COMMENT '配置值',
    `config_type` VARCHAR(50) NOT NULL COMMENT '配置类型: SYSTEM-系统配置, ACADEMIC-学期配置, SELECTION-选课配置, DICT-数据字典',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '配置描述',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_config_type` (`config_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 审计日志表
DROP TABLE IF EXISTS `sys_audit_log`;
CREATE TABLE `sys_audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    `module` VARCHAR(50) NOT NULL COMMENT '操作模块',
    `operation` VARCHAR(50) NOT NULL COMMENT '操作类型: CREATE, UPDATE, DELETE, QUERY, LOGIN, LOGOUT, EXPORT, IMPORT',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    `request_method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法',
    `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
    `response_data` TEXT DEFAULT NULL COMMENT '响应数据',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `execution_time` BIGINT DEFAULT NULL COMMENT '执行时长(毫秒)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态: 0-失败, 1-成功',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_operation` (`operation`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- =====================================================
-- 通知模板与通知记录相关表
-- =====================================================

-- 通知模板表
DROP TABLE IF EXISTS `sys_notification_template`;
CREATE TABLE `sys_notification_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_code` VARCHAR(50) NOT NULL COMMENT '模板编码',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `category` VARCHAR(30) NOT NULL COMMENT '模板分类: ACADEMIC-教务, COURSE-选课, GRADE-成绩, LEAVE-请假, SYSTEM-系统',
    `subject` VARCHAR(200) NOT NULL COMMENT '通知标题模板',
    `content` TEXT NOT NULL COMMENT '通知内容模板(支持变量占位符 ${var})',
    `variables` VARCHAR(500) DEFAULT NULL COMMENT '可用变量列表(JSON数组)',
    `channel` VARCHAR(30) NOT NULL DEFAULT 'SITE' COMMENT '发送渠道: SITE-站内信, EMAIL-邮件, ALL-全部',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置: 0-否, 1-是',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code` (`template_code`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- 通知记录表
DROP TABLE IF EXISTS `sys_notification`;
CREATE TABLE `sys_notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT DEFAULT NULL COMMENT '使用的模板ID(可为空表示自定义通知)',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `category` VARCHAR(30) NOT NULL DEFAULT 'SYSTEM' COMMENT '通知分类',
    `priority` VARCHAR(10) NOT NULL DEFAULT 'NORMAL' COMMENT '优先级: LOW-低, NORMAL-普通, HIGH-高, URGENT-紧急',
    `target_type` VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '目标类型: ALL-全体, ROLE-按角色, USER-指定用户',
    `target_value` VARCHAR(500) DEFAULT NULL COMMENT '目标值(角色编码或用户ID列表, 逗号分隔)',
    `sender_id` BIGINT NOT NULL COMMENT '发送人ID',
    `sender_name` VARCHAR(50) NOT NULL COMMENT '发送人姓名',
    `send_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'SENT' COMMENT '状态: DRAFT-草稿, SENT-已发送',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_category` (`category`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_send_time` (`send_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知记录表';

-- 用户通知阅读状态表
DROP TABLE IF EXISTS `sys_notification_read`;
CREATE TABLE `sys_notification_read` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `notification_id` BIGINT NOT NULL COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notification_user` (`notification_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知阅读状态表';

SET FOREIGN_KEY_CHECKS = 1;
