package com.university.sms.grade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.grade.entity.Grade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩 Mapper
 */
@Mapper
public interface GradeMapper extends BaseMapper<Grade> {
    
    /**
     * 查询学生的成绩列表
     */
    @Select("SELECT * FROM grd_grade WHERE student_id = #{studentId} AND status = 'APPROVED' ORDER BY created_at DESC")
    List<Grade> selectByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 查询学生指定学期的成绩
     */
    List<Grade> selectByStudentAndSemester(@Param("studentId") Long studentId, @Param("semester") String semester);
    
    /**
     * 查询课程的成绩列表
     */
    @Select("SELECT * FROM grd_grade WHERE course_id = #{courseId} ORDER BY student_id")
    List<Grade> selectByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 查询学生某课程的成绩
     */
    @Select("SELECT * FROM grd_grade WHERE student_id = #{studentId} AND course_id = #{courseId} LIMIT 1")
    Grade selectByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    /**
     * 统计课程平均分
     */
    @Select("SELECT AVG(score) FROM grd_grade WHERE course_id = #{courseId} AND status = 'APPROVED'")
    BigDecimal selectAverageScore(@Param("courseId") Long courseId);
    
    /**
     * 统计课程及格率
     */
    @Select("SELECT COUNT(*) * 100.0 / (SELECT COUNT(*) FROM grd_grade WHERE course_id = #{courseId} AND status = 'APPROVED') " +
            "FROM grd_grade WHERE course_id = #{courseId} AND status = 'APPROVED' AND score >= 60")
    BigDecimal selectPassRate(@Param("courseId") Long courseId);
    
    /**
     * 统计各等级人数
     */
    @Select("SELECT letter_grade, COUNT(*) as count FROM grd_grade " +
            "WHERE course_id = #{courseId} AND status = 'APPROVED' GROUP BY letter_grade")
    List<java.util.Map<String, Object>> selectGradeDistribution(@Param("courseId") Long courseId);
    
    /**
     * 查询待审批的成绩
     */
    @Select("SELECT * FROM grd_grade WHERE course_id = #{courseId} AND status = 'SUBMITTED'")
    List<Grade> selectPendingApproval(@Param("courseId") Long courseId);
}
