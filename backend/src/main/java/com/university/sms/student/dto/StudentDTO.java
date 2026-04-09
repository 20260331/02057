package com.university.sms.student.dto;

import com.university.sms.common.util.MaskUtils;
import com.university.sms.student.entity.Student;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学生信息 DTO
 */
@Data
public class StudentDTO {
    
    private Long id;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 性别: M-男, F-女
     */
    private String gender;
    
    /**
     * 性别描述
     */
    private String genderText;
    
    /**
     * 出生日期
     */
    private LocalDate birthDate;
    
    /**
     * 身份证号（脱敏）
     */
    private String idNumber;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 家庭住址
     */
    private String address;
    
    /**
     * 院系
     */
    private String department;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 班级
     */
    private String classNo;
    
    /**
     * 入学日期
     */
    private LocalDate enrollmentDate;
    
    /**
     * 预计毕业日期
     */
    private LocalDate graduationDate;
    
    /**
     * 学籍状态
     */
    private String academicStatus;
    
    /**
     * 学籍状态描述
     */
    private String academicStatusText;
    
    /**
     * 辅导员ID
     */
    private Long counselorId;
    
    /**
     * 辅导员姓名
     */
    private String counselorName;
    
    /**
     * 从实体转换为 DTO（带脱敏）
     */
    public static StudentDTO fromEntity(Student student) {
        return fromEntity(student, true);
    }
    
    /**
     * 从实体转换为 DTO
     * @param student 学生实体
     * @param mask 是否脱敏
     */
    public static StudentDTO fromEntity(Student student, boolean mask) {
        if (student == null) {
            return null;
        }
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setStudentNo(student.getStudentNo());
        dto.setName(student.getName());
        dto.setGender(student.getGender());
        dto.setGenderText("M".equals(student.getGender()) ? "男" : "女");
        dto.setBirthDate(student.getBirthDate());
        dto.setIdNumber(mask ? MaskUtils.maskIdNumber(student.getIdNumber()) : student.getIdNumber());
        dto.setPhone(mask ? MaskUtils.maskPhone(student.getPhone()) : student.getPhone());
        dto.setEmail(mask ? MaskUtils.maskEmail(student.getEmail()) : student.getEmail());
        dto.setAddress(mask ? maskAddress(student.getAddress()) : student.getAddress());
        dto.setDepartment(student.getDepartment());
        dto.setMajor(student.getMajor());
        dto.setClassNo(student.getClassNo());
        dto.setEnrollmentDate(student.getEnrollmentDate());
        dto.setGraduationDate(student.getGraduationDate());
        dto.setAcademicStatus(student.getAcademicStatus());
        dto.setAcademicStatusText(getAcademicStatusText(student.getAcademicStatus()));
        dto.setCounselorId(student.getCounselorId());
        return dto;
    }
    
    private static String maskAddress(String address) {
        if (address == null || address.length() <= 6) return address;
        return address.substring(0, 6) + "****";
    }

    private static String getAcademicStatusText(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "ENROLLED" -> "在读";
            case "SUSPENDED" -> "休学";
            case "WITHDRAWN" -> "退学";
            case "GRADUATED" -> "毕业";
            case "TRANSFERRED" -> "转学";
            default -> status;
        };
    }
}
