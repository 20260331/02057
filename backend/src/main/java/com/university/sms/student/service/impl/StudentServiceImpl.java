package com.university.sms.student.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.sms.common.exception.BusinessException;
import com.university.sms.common.response.PageResult;
import com.university.sms.student.dto.*;
import com.university.sms.student.entity.Student;
import com.university.sms.student.entity.StudentChangeLog;
import com.university.sms.student.enums.AcademicStatus;
import com.university.sms.student.mapper.StudentChangeLogMapper;
import com.university.sms.student.mapper.StudentMapper;
import com.university.sms.student.service.StudentService;
import com.university.sms.system.entity.User;
import com.university.sms.system.mapper.UserMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    
    private final StudentMapper studentMapper;
    private final StudentChangeLogMapper changeLogMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public StudentDTO getById(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        StudentDTO dto = StudentDTO.fromEntity(student);
        fillCounselorName(dto);
        return dto;
    }

    @Override
    public StudentDTO getByUserId(Long userId) {
        Student student = studentMapper.selectByUserId(userId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        StudentDTO dto = StudentDTO.fromEntity(student, false);
        fillCounselorName(dto);
        return dto;
    }
    
    @Override
    public StudentDTO getByUserIdOrNull(Long userId) {
        Student student = studentMapper.selectByUserId(userId);
        if (student == null) {
            return null;
        }
        StudentDTO dto = StudentDTO.fromEntity(student, false);
        fillCounselorName(dto);
        return dto;
    }
    
    @Override
    public StudentDTO getByStudentNo(String studentNo) {
        Student student = studentMapper.selectByStudentNo(studentNo);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        StudentDTO dto = StudentDTO.fromEntity(student);
        fillCounselorName(dto);
        return dto;
    }
    
    @Override
    public PageResult<StudentDTO> queryPage(StudentQueryDTO query) {
        Page<Student> page = new Page<>(query.getPage(), query.getSize());
        page = studentMapper.selectStudentPage(page, query);
        
        List<StudentDTO> records = page.getRecords().stream()
                .map(StudentDTO::fromEntity)
                .collect(Collectors.toList());
        fillCounselorNames(records);
        
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }
    
    @Override
    public PageResult<StudentDTO> queryByCounselor(Long counselorId, StudentQueryDTO query) {
        query.setCounselorId(counselorId);
        return queryPage(query);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContact(Long studentId, UpdateContactDTO dto, Long operatorId, String operatorName) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        
        if (dto.getPhone() != null && !dto.getPhone().equals(student.getPhone())) {
            saveChangeLog(studentId, "phone", "联系电话", student.getPhone(), dto.getPhone(), 
                    StudentChangeLog.TYPE_UPDATE, null, operatorId, operatorName);
            student.setPhone(dto.getPhone());
        }
        
        if (dto.getEmail() != null && !dto.getEmail().equals(student.getEmail())) {
            saveChangeLog(studentId, "email", "邮箱", student.getEmail(), dto.getEmail(),
                    StudentChangeLog.TYPE_UPDATE, null, operatorId, operatorName);
            student.setEmail(dto.getEmail());
        }
        
        if (dto.getAddress() != null && !dto.getAddress().equals(student.getAddress())) {
            saveChangeLog(studentId, "address", "家庭住址", student.getAddress(), dto.getAddress(),
                    StudentChangeLog.TYPE_UPDATE, null, operatorId, operatorName);
            student.setAddress(dto.getAddress());
        }
        
        studentMapper.updateById(student);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudent(Long studentId, StudentUpdateDTO dto, Long operatorId, String operatorName) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        
        if (dto.getPhone() != null && !dto.getPhone().equals(student.getPhone())) {
            saveChangeLog(studentId, "phone", "联系电话", student.getPhone(), dto.getPhone(),
                    StudentChangeLog.TYPE_UPDATE, null, operatorId, operatorName);
            student.setPhone(dto.getPhone());
        }
        
        if (dto.getEmail() != null && !dto.getEmail().equals(student.getEmail())) {
            saveChangeLog(studentId, "email", "邮箱", student.getEmail(), dto.getEmail(),
                    StudentChangeLog.TYPE_UPDATE, null, operatorId, operatorName);
            student.setEmail(dto.getEmail());
        }
        
        if (dto.getAddress() != null && !dto.getAddress().equals(student.getAddress())) {
            saveChangeLog(studentId, "address", "家庭住址", student.getAddress(), dto.getAddress(),
                    StudentChangeLog.TYPE_UPDATE, null, operatorId, operatorName);
            student.setAddress(dto.getAddress());
        }
        
        if (dto.getClassNo() != null && !dto.getClassNo().equals(student.getClassNo())) {
            saveChangeLog(studentId, "classNo", "班级", student.getClassNo(), dto.getClassNo(),
                    StudentChangeLog.TYPE_UPDATE, null, operatorId, operatorName);
            student.setClassNo(dto.getClassNo());
        }
        
        if (dto.getGraduationDate() != null && !dto.getGraduationDate().equals(student.getGraduationDate())) {
            saveChangeLog(studentId, "graduationDate", "预计毕业日期", 
                    student.getGraduationDate() != null ? student.getGraduationDate().toString() : null,
                    dto.getGraduationDate().toString(),
                    StudentChangeLog.TYPE_UPDATE, null, operatorId, operatorName);
            student.setGraduationDate(dto.getGraduationDate());
        }
        
        studentMapper.updateById(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeAcademicStatus(Long studentId, AcademicStatusChangeDTO dto, Long operatorId, String operatorName) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        
        AcademicStatus currentStatus = AcademicStatus.fromCode(student.getAcademicStatus());
        AcademicStatus targetStatus = AcademicStatus.fromCode(dto.getTargetStatus());
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new BusinessException("不允许从 " + currentStatus.getDescription() + " 变更为 " + targetStatus.getDescription());
        }
        
        saveChangeLog(studentId, "academicStatus", "学籍状态", 
                currentStatus.getDescription(), targetStatus.getDescription(),
                StudentChangeLog.TYPE_STATUS_CHANGE, dto.getReason(), operatorId, operatorName);
        
        student.setAcademicStatus(dto.getTargetStatus());
        studentMapper.updateById(student);
    }
    
    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("学生导入模板", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
            
            ExcelWriter writer = ExcelUtil.getWriter(true);
            // 设置表头
            writer.writeHeadRow(java.util.Arrays.asList(
                "学号", "姓名", "性别", "身份证号", "联系电话", "邮箱", "院系", "专业", "班级"
            ));
            // 写入示例数据
            writer.writeRow(java.util.Arrays.asList(
                "2024001001", "张三", "男", "110101200001011234", "13800000001", "zhangsan@example.com", "计算机学院", "软件工程", "软件2401"
            ));
            writer.flush(response.getOutputStream(), true);
            writer.close();
        } catch (IOException e) {
            throw new BusinessException("下载模板失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultDTO importStudents(MultipartFile file, Long operatorId, String operatorName) {
        ImportResultDTO result = new ImportResultDTO();
        
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            List<Map<String, Object>> rows = reader.readAll();
            result.setTotalCount(rows.size());
            
            for (int i = 0; i < rows.size(); i++) {
                int rowNum = i + 2;
                Map<String, Object> row = rows.get(i);
                
                try {
                    StudentImportDTO importDTO = parseImportRow(row, rowNum, result);
                    if (importDTO == null) continue;
                    
                    Student existing = studentMapper.selectByStudentNo(importDTO.getStudentNo());
                    if (existing != null) {
                        updateExistingStudent(existing, importDTO);
                        studentMapper.updateById(existing);
                    } else {
                        createNewStudent(importDTO, operatorId);
                    }
                    result.incrementSuccess();
                } catch (Exception e) {
                    log.error("导入第 {} 行失败: {}", rowNum, e.getMessage());
                    result.addError(rowNum, "row", e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new BusinessException("读取文件失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public void exportStudents(StudentQueryDTO query, HttpServletResponse response) {
        List<Student> students = studentMapper.selectStudentPage(
                new Page<>(1, 10000), query).getRecords();
        
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("学生信息_" + LocalDate.now(), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
            
            ExcelWriter writer = ExcelUtil.getWriter(true);
            writer.addHeaderAlias("studentNo", "学号");
            writer.addHeaderAlias("name", "姓名");
            writer.addHeaderAlias("gender", "性别");
            writer.addHeaderAlias("idNumber", "身份证号");
            writer.addHeaderAlias("department", "院系");
            writer.addHeaderAlias("major", "专业");
            writer.addHeaderAlias("classNo", "班级");
            writer.addHeaderAlias("phone", "联系电话");
            writer.addHeaderAlias("email", "邮箱");
            writer.addHeaderAlias("academicStatus", "学籍状态");
            writer.setOnlyAlias(true);

            List<StudentDTO> exportData = students.stream()
                    .map(s -> StudentDTO.fromEntity(s, true))
                    .collect(Collectors.toList());

            writer.write(exportData, true);
            writer.flush(response.getOutputStream(), true);
            writer.close();
        } catch (IOException e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] generateAcademicCertificate(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        // 简化实现，返回证明文本的字节数组
        String certificate = String.format(
                "学籍证明\n\n兹证明 %s，学号 %s，系我校 %s 学院 %s 专业学生，" +
                "于 %s 入学，当前学籍状态为：%s。\n\n特此证明。\n\n日期：%s",
                student.getName(), student.getStudentNo(), student.getDepartment(),
                student.getMajor(), student.getEnrollmentDate(),
                AcademicStatus.fromCode(student.getAcademicStatus()).getDescription(),
                LocalDate.now()
        );
        return certificate.getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public List<StudentChangeLog> getChangeLogs(Long studentId) {
        return changeLogMapper.selectByStudentId(studentId);
    }
    
    @Override
    public boolean isManagedByCounselor(Long studentId, Long counselorId) {
        Student student = studentMapper.selectById(studentId);
        return student != null && counselorId.equals(student.getCounselorId());
    }
    
    @Override
    public Student getEntityById(Long id) {
        return studentMapper.selectById(id);
    }

    // ========== 私有方法 ==========
    
    private void fillCounselorName(StudentDTO dto) {
        if (dto.getCounselorId() != null) {
            User counselor = userMapper.selectById(dto.getCounselorId());
            if (counselor != null) {
                dto.setCounselorName(counselor.getRealName());
            }
        }
    }
    
    private void fillCounselorNames(List<StudentDTO> dtos) {
        dtos.forEach(this::fillCounselorName);
    }
    
    private void saveChangeLog(Long studentId, String fieldName, String fieldLabel,
                               String oldValue, String newValue, String changeType,
                               String reason, Long operatorId, String operatorName) {
        StudentChangeLog log = new StudentChangeLog();
        log.setStudentId(studentId);
        log.setFieldName(fieldName);
        log.setFieldLabel(fieldLabel);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setChangeType(changeType);
        log.setReason(reason);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreatedAt(LocalDateTime.now());
        changeLogMapper.insert(log);
    }
    
    private StudentImportDTO parseImportRow(Map<String, Object> row, int rowNum, ImportResultDTO result) {
        StudentImportDTO dto = new StudentImportDTO();
        dto.setRowNum(rowNum);
        
        String studentNo = getStringValue(row, "学号");
        if (StrUtil.isBlank(studentNo)) {
            result.addError(rowNum, "学号", "学号不能为空");
            return null;
        }
        dto.setStudentNo(studentNo);
        
        String name = getStringValue(row, "姓名");
        if (StrUtil.isBlank(name)) {
            result.addError(rowNum, "姓名", "姓名不能为空");
            return null;
        }
        dto.setName(name);
        
        dto.setGender(getStringValue(row, "性别"));
        dto.setIdNumber(getStringValue(row, "身份证号"));
        dto.setPhone(getStringValue(row, "联系电话"));
        dto.setEmail(getStringValue(row, "邮箱"));
        dto.setDepartment(getStringValue(row, "院系"));
        dto.setMajor(getStringValue(row, "专业"));
        dto.setClassNo(getStringValue(row, "班级"));
        
        return dto;
    }
    
    private String getStringValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString().trim() : null;
    }
    
    private void updateExistingStudent(Student student, StudentImportDTO dto) {
        if (StrUtil.isNotBlank(dto.getName())) student.setName(dto.getName());
        if (StrUtil.isNotBlank(dto.getGender())) student.setGender(dto.getGender());
        if (StrUtil.isNotBlank(dto.getIdNumber())) student.setIdNumber(dto.getIdNumber());
        if (StrUtil.isNotBlank(dto.getPhone())) student.setPhone(dto.getPhone());
        if (StrUtil.isNotBlank(dto.getEmail())) student.setEmail(dto.getEmail());
        if (StrUtil.isNotBlank(dto.getDepartment())) student.setDepartment(dto.getDepartment());
        if (StrUtil.isNotBlank(dto.getMajor())) student.setMajor(dto.getMajor());
        if (StrUtil.isNotBlank(dto.getClassNo())) student.setClassNo(dto.getClassNo());
    }
    
    @Transactional(rollbackFor = Exception.class)
    protected void createNewStudent(StudentImportDTO dto, Long operatorId) {
        // 创建用户账号
        User user = new User();
        user.setUsername(dto.getStudentNo());
        user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        user.setRealName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(User.STATUS_ENABLED);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        
        // 创建学生记录
        Student student = new Student();
        student.setUserId(user.getId());
        student.setStudentNo(dto.getStudentNo());
        student.setName(dto.getName());
        student.setGender("男".equals(dto.getGender()) ? "M" : "F");
        student.setIdNumber(dto.getIdNumber());
        student.setPhone(dto.getPhone());
        student.setEmail(dto.getEmail());
        student.setDepartment(dto.getDepartment());
        student.setMajor(dto.getMajor());
        student.setClassNo(dto.getClassNo());
        student.setAcademicStatus(AcademicStatus.ENROLLED.getCode());
        student.setEnrollmentDate(LocalDate.now());
        student.setCreatedAt(LocalDateTime.now());
        studentMapper.insert(student);
    }
}
