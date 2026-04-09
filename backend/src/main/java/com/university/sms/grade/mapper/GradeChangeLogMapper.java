package com.university.sms.grade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.grade.entity.GradeChangeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 成绩变更日志 Mapper
 */
@Mapper
public interface GradeChangeLogMapper extends BaseMapper<GradeChangeLog> {
    
    /**
     * 查询成绩的变更日志
     */
    @Select("SELECT * FROM grd_grade_change_log WHERE grade_id = #{gradeId} ORDER BY created_at DESC")
    List<GradeChangeLog> selectByGradeId(@Param("gradeId") Long gradeId);
    
    /**
     * 查询课程的所有成绩变更日志
     */
    @Select("SELECT * FROM grd_grade_change_log WHERE course_id = #{courseId} ORDER BY created_at DESC")
    List<GradeChangeLog> selectByCourseId(@Param("courseId") Long courseId);
}
