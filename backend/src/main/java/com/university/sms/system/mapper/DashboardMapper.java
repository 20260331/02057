package com.university.sms.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 首页统计 Mapper
 */
@Mapper
public interface DashboardMapper {
    
    /**
     * 统计在籍学生数
     */
    @Select("SELECT COUNT(*) FROM stu_student WHERE academic_status = 'ENROLLED' AND deleted = 0")
    Long countStudents();
    
    /**
     * 统计当前学期课程数
     */
    @Select("SELECT COUNT(*) FROM crs_course WHERE deleted = 0")
    Long countCourses();
    
    /**
     * 统计待审批成绩数
     */
    @Select("SELECT COUNT(*) FROM grd_grade WHERE status = 'SUBMITTED'")
    Long countPendingGrades();
    
    /**
     * 统计待审批请假数
     */
    @Select("SELECT COUNT(*) FROM lev_leave_request WHERE status IN ('PENDING', 'COUNSELOR_APPROVED')")
    Long countPendingLeaves();
    
    /**
     * 统计请假中人数
     */
    @Select("SELECT COUNT(*) FROM lev_leave_request WHERE status = 'APPROVED'")
    Long countOnLeave();
}
