package com.university.sms.course.controller;

import com.university.sms.common.response.Result;
import com.university.sms.course.dto.*;
import com.university.sms.course.service.CourseSelectionService;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.student.dto.StudentDTO;
import com.university.sms.student.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 选课控制器
 */
@RestController
@RequestMapping("/course-selections")
@RequiredArgsConstructor
public class CourseSelectionController {
    
    private final CourseSelectionService selectionService;
    private final StudentService studentService;
    
    /**
     * 选课
     */
    @PostMapping
    public Result<CourseSelectionResultDTO> selectCourse(@RequestBody CourseSelectionRequest request,
                                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        StudentDTO student = studentService.getByUserId(userId);
        
        CourseSelectionResultDTO result = selectionService.selectCourse(student.getId(), request.getCourseId());
        return Result.success(result);
    }
    
    /**
     * 退课
     */
    @DeleteMapping("/{courseId}")
    public Result<Void> withdrawCourse(@PathVariable Long courseId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserId(userId);
        
        selectionService.withdrawCourse(student.getId(), courseId);
        return Result.success();
    }
    
    /**
     * 获取我的选课记录
     */
    @GetMapping("/my")
    public Result<List<CourseSelectionDTO>> getMySelections(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserId(userId);
        
        List<CourseSelectionDTO> selections = selectionService.getStudentSelections(student.getId());
        return Result.success(selections);
    }
    
    /**
     * 获取我的课表
     */
    @GetMapping("/schedule")
    public Result<List<ScheduleDTO>> getMySchedule(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserId(userId);
        
        List<ScheduleDTO> schedule = selectionService.getStudentSchedule(student.getId());
        return Result.success(schedule);
    }
    
    /**
     * 获取学生课表（教师/管理员）
     */
    @GetMapping("/schedule/{studentId}")
    @RequirePermission({"course:roster", "student:manage"})
    public Result<List<ScheduleDTO>> getStudentSchedule(@PathVariable Long studentId) {
        List<ScheduleDTO> schedule = selectionService.getStudentSchedule(studentId);
        return Result.success(schedule);
    }
    
    /**
     * 获取课程学生名单
     */
    @GetMapping("/roster/{courseId}")
    @RequirePermission({"course:roster:view", "grade:entry"})
    public Result<List<CourseRosterDTO>> getCourseRoster(@PathVariable Long courseId) {
        List<CourseRosterDTO> roster = selectionService.getCourseRoster(courseId);
        return Result.success(roster);
    }
    
    /**
     * 检查时间冲突
     */
    @GetMapping("/check-conflict")
    public Result<List<CourseDTO>> checkConflict(@RequestParam Long courseId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserId(userId);
        
        List<CourseDTO> conflicts = selectionService.checkTimeConflict(student.getId(), courseId);
        return Result.success(conflicts);
    }
    
    /**
     * 检查先修课程
     */
    @GetMapping("/check-prerequisites")
    public Result<List<String>> checkPrerequisites(@RequestParam Long courseId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserId(userId);
        
        List<String> missing = selectionService.checkPrerequisites(student.getId(), courseId);
        return Result.success(missing);
    }

    /**
     * 获取当前选课阶段（简单版，兼容旧调用）
     */
    @GetMapping("/phase")
    public Result<Map<String, Object>> getSelectionPhase() {
        String phase = selectionService.getSelectionPhase();
        Map<String, Object> data = Map.of("phase", phase);
        return Result.success(data);
    }

    /**
     * 获取选课阶段完整信息（含各阶段时间窗口、规则说明）
     */
    @GetMapping("/phase/info")
    public Result<SelectionPhaseDTO> getPhaseInfo() {
        return Result.success(selectionService.getPhaseInfo());
    }

    // ==================== 抽签管理接口（管理员/教务） ====================

    /**
     * 获取需要抽签的课程列表
     */
    @GetMapping("/lottery/pending")
    @RequirePermission({"course:manage"})
    public Result<List<LotteryCourseDTO>> getLotteryPendingCourses() {
        List<LotteryCourseDTO> courses = selectionService.getLotteryPendingCourses();
        return Result.success(courses);
    }

    /**
     * 对单门课程执行抽签
     */
    @PostMapping("/lottery/{courseId}")
    @RequirePermission({"course:manage"})
    public Result<Void> executeLottery(@PathVariable Long courseId) {
        selectionService.executeLottery(courseId);
        return Result.success();
    }

    /**
     * 对所有需抽签课程批量执行抽签
     */
    @PostMapping("/lottery/all")
    @RequirePermission({"course:manage"})
    public Result<Map<String, Object>> executeLotteryAll() {
        int count = selectionService.executeLotteryAll();
        return Result.success(Map.of("processedCount", count));
    }

    /**
     * 选课请求 DTO
     */
    @lombok.Data
    public static class CourseSelectionRequest {
        private Long courseId;
    }
}
