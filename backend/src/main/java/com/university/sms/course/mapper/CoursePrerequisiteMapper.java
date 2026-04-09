package com.university.sms.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.course.entity.CoursePrerequisite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程先修要求 Mapper
 */
@Mapper
public interface CoursePrerequisiteMapper extends BaseMapper<CoursePrerequisite> {
    
    /**
     * 获取课程的先修课程列表
     */
    @Select("SELECT * FROM crs_course_prerequisite WHERE course_id = #{courseId}")
    List<CoursePrerequisite> selectByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 检查是否存在先修要求
     */
    @Select("SELECT COUNT(*) > 0 FROM crs_course_prerequisite WHERE course_id = #{courseId}")
    boolean hasPrerequisites(@Param("courseId") Long courseId);
    
    /**
     * 获取先修课程ID列表
     */
    @Select("SELECT prerequisite_course_id FROM crs_course_prerequisite WHERE course_id = #{courseId}")
    List<Long> selectPrerequisiteCourseIds(@Param("courseId") Long courseId);
}
