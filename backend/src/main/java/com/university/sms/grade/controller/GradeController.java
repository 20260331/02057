package com.university.sms.grade.controller;

import com.university.sms.common.response.Result;
import com.university.sms.grade.dto.*;
import com.university.sms.grade.service.GradeService;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.student.dto.StudentDTO;
import com.university.sms.student.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 成绩控制器
 */
@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
public class GradeController {
    
    private final GradeService gradeService;
    private final StudentService studentService;
    
    /**
     * 查询我的成绩
     */
    @GetMapping("/my")
    public Result<List<GradeDTO>> getMyGrades(@RequestParam(required = false) String semester,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserId(userId);
        
        List<GradeDTO> grades = gradeService.getStudentGrades(student.getId(), semester);
        return Result.success(grades);
    }

    /**
     * 查询我是否存在不及格成绩（成绩预警）
     */
    @GetMapping("/my/failing")
    public Result<Boolean> hasMyFailingGrades(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserId(userId);
        boolean hasFailing = gradeService.hasFailingGrades(student.getId());
        return Result.success(hasFailing);
    }
    
    /**
     * 查询学生成绩
     */
    @GetMapping("/student/{studentId}")
    @RequirePermission({"grade:view", "grade:manage"})
    public Result<List<GradeDTO>> getStudentGrades(@PathVariable Long studentId,
                                                    @RequestParam(required = false) String semester) {
        List<GradeDTO> grades = gradeService.getStudentGrades(studentId, semester);
        return Result.success(grades);
    }
    
    /**
     * 查询课程成绩
     */
    @GetMapping("/course/{courseId}")
    @RequirePermission({"grade:view", "grade:entry", "grade:approve"})
    public Result<List<GradeDTO>> getCourseGrades(@PathVariable Long courseId) {
        List<GradeDTO> grades = gradeService.getCourseGrades(courseId);
        return Result.success(grades);
    }
    
    /**
     * 批量录入成绩
     */
    @PostMapping("/batch")
    @RequirePermission("grade:entry")
    public Result<Void> batchSaveGrades(@Valid @RequestBody BatchGradeDTO dto,
                                        HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        gradeService.batchSaveGrades(dto, operatorId);
        return Result.success();
    }
    
    /**
     * 批量录入并提交成绩（一步完成保存和提交）
     */
    @PostMapping("/batch-submit")
    @RequirePermission("grade:entry")
    public Result<Void> batchSaveAndSubmitGrades(@Valid @RequestBody BatchGradeDTO dto,
                                                  HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        gradeService.batchSaveAndSubmitGrades(dto, operatorId);
        return Result.success();
    }
    
    /**
     * 修改成绩
     */
    @PutMapping("/{id}")
    @RequirePermission("grade:entry")
    public Result<Void> updateGrade(@PathVariable Long id,
                                    @Valid @RequestBody GradeUpdateDTO dto,
                                    HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        String operatorName = (String) request.getAttribute("username");
        gradeService.updateGrade(id, dto, operatorId, operatorName);
        return Result.success();
    }
    
    /**
     * 提交成绩
     */
    @PostMapping("/submit/{courseId}")
    @RequirePermission("grade:entry")
    public Result<Void> submitGrades(@PathVariable Long courseId, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        gradeService.submitGrades(courseId, operatorId);
        return Result.success();
    }
    
    /**
     * 审批成绩
     */
    @PostMapping("/approve/{courseId}")
    @RequirePermission("grade:approve")
    public Result<Void> approveGrades(@PathVariable Long courseId, HttpServletRequest request) {
        Long approverId = (Long) request.getAttribute("userId");
        gradeService.approveGrades(courseId, approverId);
        return Result.success();
    }
    
    /**
     * 获取我的 GPA
     */
    @GetMapping("/gpa")
    public Result<GPAInfoDTO> getMyGPA(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserId(userId);
        
        GPAInfoDTO gpa = gradeService.calculateGPA(student.getId());
        return Result.success(gpa);
    }
    
    /**
     * 获取学生 GPA
     */
    @GetMapping("/gpa/{studentId}")
    @RequirePermission({"grade:view", "grade:manage"})
    public Result<GPAInfoDTO> getStudentGPA(@PathVariable Long studentId) {
        GPAInfoDTO gpa = gradeService.calculateGPA(studentId);
        return Result.success(gpa);
    }
    
    /**
     * 获取课程成绩统计
     */
    @GetMapping("/statistics/{courseId}")
    @RequirePermission({"grade:view", "grade:entry", "grade:approve"})
    public Result<GradeStatisticsDTO> getStatistics(@PathVariable Long courseId) {
        GradeStatisticsDTO stats = gradeService.getStatistics(courseId);
        return Result.success(stats);
    }
    
    /**
     * 获取课程成绩修改记录
     */
    @GetMapping("/change-logs/{courseId}")
    @RequirePermission({"grade:view", "grade:entry", "grade:approve"})
    public Result<List<GradeChangeLogDTO>> getChangeLogs(@PathVariable Long courseId) {
        List<GradeChangeLogDTO> logs = gradeService.getChangeLogs(courseId);
        return Result.success(logs);
    }

    /**
     * 生成我的成绩单（JSON 数据 + HTML）
     */
    @GetMapping("/transcript")
    public Result<TranscriptDTO> getMyTranscript(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserId(userId);
        TranscriptDTO transcript = gradeService.generateTranscript(student.getId());
        return Result.success(transcript);
    }

    /**
     * 生成指定学生的成绩单
     */
    @GetMapping("/transcript/{studentId}")
    @RequirePermission({"grade:view", "grade:manage"})
    public Result<TranscriptDTO> getStudentTranscript(@PathVariable Long studentId) {
        TranscriptDTO transcript = gradeService.generateTranscript(studentId);
        return Result.success(transcript);
    }

    /**
     * 导出成绩单为 HTML 文件
     */
    @GetMapping("/transcript/export")
    public void exportMyTranscript(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserId(userId);
        TranscriptDTO transcript = gradeService.generateTranscript(student.getId());

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"transcript_" + student.getStudentNo() + ".html\"");
        response.getOutputStream().write(transcript.getHtmlContent().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 导出指定学生成绩单为 HTML 文件
     */
    @GetMapping("/transcript/export/{studentId}")
    @RequirePermission({"grade:view", "grade:manage"})
    public void exportStudentTranscript(@PathVariable Long studentId, HttpServletResponse response) throws IOException {
        TranscriptDTO transcript = gradeService.generateTranscript(studentId);

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"transcript_" + transcript.getStudentNo() + ".html\"");
        response.getOutputStream().write(transcript.getHtmlContent().getBytes(StandardCharsets.UTF_8));
    }
}
