package com.university.sms.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.sms.student.dto.StudentQueryDTO;
import com.university.sms.student.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学生 Mapper
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
    
    /**
     * 根据用户ID查询学生
     */
    @Select("SELECT * FROM stu_student WHERE user_id = #{userId} AND deleted = 0")
    Student selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据学号查询学生
     */
    @Select("SELECT * FROM stu_student WHERE student_no = #{studentNo} AND deleted = 0")
    Student selectByStudentNo(@Param("studentNo") String studentNo);
    
    /**
     * 分页查询学生列表
     */
    Page<Student> selectStudentPage(Page<Student> page, @Param("query") StudentQueryDTO query);
    
    /**
     * 查询辅导员管理的学生列表
     */
    @Select("SELECT * FROM stu_student WHERE counselor_id = #{counselorId} AND deleted = 0")
    List<Student> selectByCounselorId(@Param("counselorId") Long counselorId);
    
    /**
     * 统计辅导员管理的学生数量
     */
    @Select("SELECT COUNT(*) FROM stu_student WHERE counselor_id = #{counselorId} AND deleted = 0")
    int countByCounselorId(@Param("counselorId") Long counselorId);
}
