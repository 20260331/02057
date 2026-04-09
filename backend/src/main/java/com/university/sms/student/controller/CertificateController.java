package com.university.sms.student.controller;

import com.university.sms.common.response.Result;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.student.dto.CertificateInfoDTO;
import com.university.sms.student.dto.CertificateRequestDTO;
import com.university.sms.student.dto.StudentDTO;
import com.university.sms.student.service.CertificateService;
import com.university.sms.student.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学籍证明材料控制器
 */
@RestController
@RequestMapping("/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final StudentService studentService;

    /**
     * 学生申请生成证明
     */
    @PostMapping
    public Result<CertificateInfoDTO> generateCertificate(@Valid @RequestBody CertificateRequestDTO dto,
                                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        StudentDTO student = studentService.getByUserIdOrNull(userId);
        if (student == null) {
            return Result.error(400, "只有学生才能申请证明");
        }
        CertificateInfoDTO info = certificateService.generateCertificate(
                student.getId(), dto, userId, username);
        return Result.success(info);
    }

    /**
     * 管理员为指定学生生成证明
     */
    @PostMapping("/student/{studentId}")
    @RequirePermission({"student:view", "student:update"})
    public Result<CertificateInfoDTO> generateForStudent(@PathVariable Long studentId,
                                                          @Valid @RequestBody CertificateRequestDTO dto,
                                                          HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        String operatorName = (String) request.getAttribute("username");
        CertificateInfoDTO info = certificateService.generateCertificate(
                studentId, dto, operatorId, operatorName);
        return Result.success(info);
    }

    /**
     * 获取证明详情
     */
    @GetMapping("/{id}")
    public Result<CertificateInfoDTO> getCertificateById(@PathVariable Long id) {
        CertificateInfoDTO info = certificateService.getCertificateById(id);
        return Result.success(info);
    }

    /**
     * 通过编号验证证明（公开接口）
     */
    @GetMapping("/verify/{certNo}")
    public Result<CertificateInfoDTO> verifyCertificate(@PathVariable String certNo) {
        CertificateInfoDTO info = certificateService.getCertificateByCertNo(certNo);
        return Result.success(info);
    }

    /**
     * 获取我的证明记录
     */
    @GetMapping("/my")
    public Result<List<CertificateInfoDTO>> getMyCertificates(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserIdOrNull(userId);
        if (student == null) {
            return Result.error(400, "只有学生才能查看证明记录");
        }
        List<CertificateInfoDTO> list = certificateService.getStudentCertificates(student.getId());
        return Result.success(list);
    }

    /**
     * 获取指定学生的证明记录
     */
    @GetMapping("/student/{studentId}")
    @RequirePermission("student:view")
    public Result<List<CertificateInfoDTO>> getStudentCertificates(@PathVariable Long studentId) {
        List<CertificateInfoDTO> list = certificateService.getStudentCertificates(studentId);
        return Result.success(list);
    }

    /**
     * 获取所有证明记录（管理员）
     */
    @GetMapping("/recent")
    @RequirePermission("student:view")
    public Result<List<CertificateInfoDTO>> getRecentCertificates(
            @RequestParam(defaultValue = "100") int limit) {
        List<CertificateInfoDTO> list = certificateService.getRecentCertificates(limit);
        return Result.success(list);
    }

    /**
     * 获取学生可用的证明类型
     */
    @GetMapping("/types")
    public Result<List<String>> getAvailableCertTypes(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserIdOrNull(userId);
        if (student == null) {
            return Result.error(400, "只有学生才能查看可用证明类型");
        }
        List<String> types = certificateService.getAvailableCertTypes(student.getId());
        return Result.success(types);
    }

    /**
     * 作废证明
     */
    @PutMapping("/{id}/revoke")
    @RequirePermission({"student:update", "student:status:change"})
    public Result<Void> revokeCertificate(@PathVariable Long id,
                                           @RequestBody RevokeDTO dto,
                                           HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        certificateService.revokeCertificate(id, dto.getReason(), operatorId);
        return Result.success();
    }

    @lombok.Data
    public static class RevokeDTO {
        private String reason;
    }
}
