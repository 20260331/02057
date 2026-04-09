package com.university.sms.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.sms.course.dto.CourseQueryDTO;
import com.university.sms.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 课程 Mapper
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    
    /**
     * 分页查询课程列表
     */
    Page<Course> selectCoursePage(Page<Course> page, @Param("query") CourseQueryDTO query);
    
    /**
     * 根据关键词搜索课程
     */
    List<Course> searchCourses(@Param("keyword") String keyword, @Param("semester") String semester);
    
    /**
     * 增加已选人数
     */
    @Update("UPDATE crs_course SET enrolled_count = enrolled_count + 1 WHERE id = #{courseId} AND enrolled_count < capacity")
    int incrementEnrolledCount(@Param("courseId") Long courseId);
    
    /**
     * 减少已选人数
     */
    @Update("UPDATE crs_course SET enrolled_count = enrolled_count - 1 WHERE id = #{courseId} AND enrolled_count > 0")
    int decrementEnrolledCount(@Param("courseId") Long courseId);
    
    /**
     * 获取教师的课程列表
     */
    @Select("SELECT * FROM crs_course WHERE teacher_id = #{teacherId} AND deleted = 0 ORDER BY semester DESC, course_code")
    List<Course> selectByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 根据学期获取课程列表
     */
    @Select("SELECT * FROM crs_course WHERE semester = #{semester} AND deleted = 0 AND status = 1 ORDER BY course_code")
    List<Course> selectBySemester(@Param("semester") String semester);
    
    /**
     * 获取热门课程（按选课人数排序）
     */
    List<Course> selectPopularCourses(@Param("semester") String semester, @Param("limit") Integer limit);
    
    /**
     * 统计学期课程数量
     */
    @Select("SELECT COUNT(*) FROM crs_course WHERE semester = #{semester} AND deleted = 0 AND status = 1")
    int countBySemester(@Param("semester") String semester);
    
    /**
     * 统计学期总选课人次
     */
    @Select("SELECT COALESCE(SUM(enrolled_count), 0) FROM crs_course WHERE semester = #{semester} AND deleted = 0")
    int sumEnrolledCountBySemester(@Param("semester") String semester);
    
    /**
     * 统计学期总容量
     */
    @Select("SELECT COALESCE(SUM(capacity), 0) FROM crs_course WHERE semester = #{semester} AND deleted = 0 AND status = 1")
    int sumCapacityBySemester(@Param("semester") String semester);
}
