package com.university.sms.course.service;

import com.university.sms.common.response.PageResult;
import com.university.sms.course.dto.CourseDTO;
import com.university.sms.course.dto.CourseQueryDTO;
import com.university.sms.course.dto.CourseStatisticsDTO;
import com.university.sms.course.entity.Course;

import java.util.List;

/**
 * 课程服务接口
 */
public interface CourseService {
    
    /**
     * 分页查询课程列表
     */
    PageResult<CourseDTO> queryCourses(CourseQueryDTO query);
    
    /**
     * 搜索课程（按名称、编号、教师）
     */
    List<CourseDTO> searchCourses(String keyword, String semester);
    
    /**
     * 获取课程详情
     */
    CourseDTO getCourseById(Long id);
    
    /**
     * 获取课程实体
     */
    Course getCourseEntity(Long id);
    
    /**
     * 创建课程
     */
    CourseDTO createCourse(CourseDTO courseDTO);
    
    /**
     * 更新课程
     */
    CourseDTO updateCourse(Long id, CourseDTO courseDTO);
    
    /**
     * 删除课程
     */
    void deleteCourse(Long id);
    
    /**
     * 获取教师的课程列表
     */
    List<CourseDTO> getTeacherCourses(Long teacherId);
    
    /**
     * 获取学期课程列表
     */
    List<CourseDTO> getSemesterCourses(String semester);
    
    /**
     * 获取选课统计
     */
    CourseStatisticsDTO getStatistics(String semester);
    
    /**
     * 增加已选人数
     */
    boolean incrementEnrolledCount(Long courseId);
    
    /**
     * 减少已选人数
     */
    boolean decrementEnrolledCount(Long courseId);
    
    /**
     * 检查课程是否有余量
     */
    boolean hasAvailableCapacity(Long courseId);
    
    /**
     * 获取课程可选容量
     */
    int getAvailableCapacity(Long courseId);
}
