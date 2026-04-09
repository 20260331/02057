package com.university.sms.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.student.entity.StudentChangeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学生信息变更日志 Mapper
 */
@Mapper
public interface StudentChangeLogMapper extends BaseMapper<StudentChangeLog> {
    
    /**
     * 查询学生的变更日志
     */
    @Select("SELECT * FROM stu_student_change_log WHERE student_id = #{studentId} ORDER BY created_at DESC")
    List<StudentChangeLog> selectByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 查询学生指定字段的变更日志
     */
    @Select("SELECT * FROM stu_student_change_log WHERE student_id = #{studentId} AND field_name = #{fieldName} ORDER BY created_at DESC")
    List<StudentChangeLog> selectByStudentIdAndField(@Param("studentId") Long studentId, @Param("fieldName") String fieldName);
}
