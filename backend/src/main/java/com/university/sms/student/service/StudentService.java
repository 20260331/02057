package com.university.sms.student.service;

import com.university.sms.common.response.PageResult;
import com.university.sms.student.dto.*;
import com.university.sms.student.entity.Student;
import com.university.sms.student.entity.StudentChangeLog;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 学生服务接口
 */
public interface StudentService {
    
    /**
     * 根据ID查询学生信息
     */
    StudentDTO getById(Long id);
    
    /**
     * 根据用户ID查询学生信息
     */
    StudentDTO getByUserId(Long userId);
    
    /**
     * 根据用户ID查询学生信息（不存在返回null）
     */
    StudentDTO getByUserIdOrNull(Long userId);
    
    /**
     * 根据学号查询学生信息
     */
    StudentDTO getByStudentNo(String studentNo);
    
    /**
     * 分页查询学生列表
     */
    PageResult<StudentDTO> queryPage(StudentQueryDTO query);
    
    /**
     * 查询辅导员管理的学生列表
     */
    PageResult<StudentDTO> queryByCounselor(Long counselorId, StudentQueryDTO query);
    
    /**
     * 更新学生联系方式（学生自己）
     */
    void updateContact(Long studentId, UpdateContactDTO dto, Long operatorId, String operatorName);
    
    /**
     * 更新学生信息（辅导员）
     */
    void updateStudent(Long studentId, StudentUpdateDTO dto, Long operatorId, String operatorName);
    
    /**
     * 变更学籍状态
     */
    void changeAcademicStatus(Long studentId, AcademicStatusChangeDTO dto, Long operatorId, String operatorName);
    
    /**
     * 批量导入学生数据
     */
    ImportResultDTO importStudents(MultipartFile file, Long operatorId, String operatorName);
    
    /**
     * 下载导入模板
     */
    void downloadImportTemplate(HttpServletResponse response);
    
    /**
     * 导出学生数据
     */
    void exportStudents(StudentQueryDTO query, HttpServletResponse response);
    
    /**
     * 生成学籍证明
     */
    byte[] generateAcademicCertificate(Long studentId);
    
    /**
     * 查询学生变更日志
     */
    List<StudentChangeLog> getChangeLogs(Long studentId);
    
    /**
     * 检查是否为辅导员管理的学生
     */
    boolean isManagedByCounselor(Long studentId, Long counselorId);
    
    /**
     * 获取学生实体（内部使用）
     */
    Student getEntityById(Long id);
}
