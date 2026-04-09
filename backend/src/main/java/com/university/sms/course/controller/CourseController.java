package com.university.sms.course.controller;

import com.university.sms.common.response.PageResult;
import com.university.sms.common.response.Result;
import com.university.sms.course.dto.*;
import com.university.sms.course.service.CourseSelectionService;
import com.university.sms.course.service.CourseService;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.student.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程控制器
 */
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    private final CourseSelectionService selectionService;
    private final StudentService studentService;
    
    /**
     * 分页查询课程列表
     */
    @GetMapping
    public Result<PageResult<CourseDTO>> queryCourses(CourseQueryDTO query) {
        PageResult<CourseDTO> result = courseService.queryCourses(query);
        return Result.success(result);
    }
    
    /**
     * 搜索课程
     */
    @GetMapping("/search")
    public Result<List<CourseDTO>> searchCourses(@RequestParam String keyword,
                                                  @RequestParam(required = false) String semester) {
        List<CourseDTO> courses = courseService.searchCourses(keyword, semester);
        return Result.success(courses);
    }
    
    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    public Result<CourseDTO> getCourseById(@PathVariable Long id) {
        CourseDTO course = courseService.getCourseById(id);
        return Result.success(course);
    }
    
    /**
     * 创建课程
     */
    @PostMapping
    @RequirePermission("course:create")
    public Result<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        CourseDTO created = courseService.createCourse(courseDTO);
        return Result.success(created);
    }
    
    /**
     * 更新课程
     */
    @PutMapping("/{id}")
    @RequirePermission("course:update")
    public Result<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody CourseDTO courseDTO) {
        CourseDTO updated = courseService.updateCourse(id, courseDTO);
        return Result.success(updated);
    }
    
    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    @RequirePermission("course:delete")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return Result.success();
    }
    
    /**
     * 获取教师的课程列表
     */
    @GetMapping("/teacher")
    public Result<List<CourseDTO>> getTeacherCourses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<CourseDTO> courses = courseService.getTeacherCourses(userId);
        return Result.success(courses);
    }
    
    /**
     * 获取选课统计
     */
    @GetMapping("/statistics")
    public Result<CourseStatisticsDTO> getStatistics(@RequestParam String semester) {
        CourseStatisticsDTO stats = courseService.getStatistics(semester);
        return Result.success(stats);
    }
}
