package com.university.sms.student.controller;

import com.university.sms.common.response.PageResult;
import com.university.sms.common.response.Result;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.student.dto.*;
import com.university.sms.student.entity.StudentChangeLog;
import com.university.sms.student.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 学生信息控制器
 */
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentService studentService;
    
    /**
     * 获取当前登录学生的个人信息
     */
    @GetMapping("/me")
    public Result<StudentDTO> getCurrentStudent(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserId(userId);
        return Result.success(student);
    }
    
    /**
     * 根据ID查询学生信息
     */
    @GetMapping("/{id}")
    public Result<StudentDTO> getById(@PathVariable Long id) {
        StudentDTO student = studentService.getById(id);
        return Result.success(student);
    }
    
    /**
     * 根据学号查询学生信息
     */
    @GetMapping("/no/{studentNo}")
    public Result<StudentDTO> getByStudentNo(@PathVariable String studentNo) {
        StudentDTO student = studentService.getByStudentNo(studentNo);
        return Result.success(student);
    }
    
    /**
     * 分页查询学生列表
     */
    @GetMapping
    @RequirePermission({"student:view", "student:update"})
    public Result<PageResult<StudentDTO>> queryPage(StudentQueryDTO query) {
        PageResult<StudentDTO> result = studentService.queryPage(query);
        return Result.success(result);
    }

    /**
     * 辅导员查询所辖学生列表
     */
    @GetMapping("/counselor")
    @RequirePermission("student:view")
    public Result<PageResult<StudentDTO>> queryByCounselor(HttpServletRequest request, StudentQueryDTO query) {
        Long userId = (Long) request.getAttribute("userId");
        PageResult<StudentDTO> result = studentService.queryByCounselor(userId, query);
        return Result.success(result);
    }
    
    /**
     * 学生更新自己的联系方式
     */
    @PutMapping("/me/contact")
    public Result<Void> updateMyContact(HttpServletRequest request, @Valid @RequestBody UpdateContactDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        StudentDTO student = studentService.getByUserId(userId);
        studentService.updateContact(student.getId(), dto, userId, username);
        return Result.success();
    }
    
    /**
     * 更新学生联系方式
     */
    @PutMapping("/{id}/contact")
    @RequirePermission({"student:update", "student:manage"})
    public Result<Void> updateContact(@PathVariable Long id, 
                                      @Valid @RequestBody UpdateContactDTO dto,
                                      HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        String operatorName = (String) request.getAttribute("username");
        studentService.updateContact(id, dto, operatorId, operatorName);
        return Result.success();
    }
    
    /**
     * 辅导员更新学生信息
     */
    @PutMapping("/{id}")
    @RequirePermission({"student:update", "student:manage"})
    public Result<Void> updateStudent(@PathVariable Long id,
                                      @Valid @RequestBody StudentUpdateDTO dto,
                                      HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        String operatorName = (String) request.getAttribute("username");
        studentService.updateStudent(id, dto, operatorId, operatorName);
        return Result.success();
    }
    
    /**
     * 变更学籍状态
     */
    @PutMapping("/{id}/status")
    @RequirePermission("student:status:change")
    public Result<Void> changeAcademicStatus(@PathVariable Long id,
                                             @Valid @RequestBody AcademicStatusChangeDTO dto,
                                             HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        String operatorName = (String) request.getAttribute("username");
        studentService.changeAcademicStatus(id, dto, operatorId, operatorName);
        return Result.success();
    }
    
    /**
     * 下载导入模板
     */
    @GetMapping("/import/template")
    public void downloadImportTemplate(HttpServletResponse response) {
        studentService.downloadImportTemplate(response);
    }
    
    /**
     * 批量导入学生数据
     */
    @PostMapping("/import")
    @RequirePermission("student:import")
    public Result<ImportResultDTO> importStudents(@RequestParam("file") MultipartFile file,
                                                  HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        String operatorName = (String) request.getAttribute("username");
        ImportResultDTO result = studentService.importStudents(file, operatorId, operatorName);
        return Result.success(result);
    }
    
    /**
     * 导出学生数据
     */
    @GetMapping("/export")
    @RequirePermission("student:export")
    public void exportStudents(StudentQueryDTO query, HttpServletResponse response) {
        studentService.exportStudents(query, response);
    }
    
    /**
     * 生成学籍证明（旧接口保留兼容，建议使用 /api/certificates）
     */
    @GetMapping("/{id}/certificate")
    @Deprecated
    public Result<byte[]> generateCertificate(@PathVariable Long id) {
        byte[] certificate = studentService.generateAcademicCertificate(id);
        return Result.success(certificate);
    }
    
    /**
     * 查询学生变更日志
     */
    @GetMapping("/{id}/logs")
    @RequirePermission("student:view")
    public Result<List<StudentChangeLog>> getChangeLogs(@PathVariable Long id) {
        List<StudentChangeLog> logs = studentService.getChangeLogs(id);
        return Result.success(logs);
    }
}
