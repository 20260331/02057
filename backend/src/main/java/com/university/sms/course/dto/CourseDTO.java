package com.university.sms.course.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 课程 DTO
 */
@Data
public class CourseDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 课程ID
     */
    private Long id;
    
    /**
     * 课程编号
     */
    private String courseCode;
    
    /**
     * 课程名称
     */
    private String name;
    
    /**
     * 学分
     */
    private BigDecimal credits;
    
    /**
     * 授课教师ID
     */
    private Long teacherId;
    
    /**
     * 授课教师姓名
     */
    private String teacherName;
    
    /**
     * 上课时间
     */
    private String schedule;
    
    /**
     * 上课地点
     */
    private String location;
    
    /**
     * 课程容量
     */
    private Integer capacity;
    
    /**
     * 已选人数
     */
    private Integer enrolledCount;
    
    /**
     * 可选容量
     */
    private Integer availableCapacity;
    
    /**
     * 学期
     */
    private String semester;
    
    /**
     * 课程类别
     */
    private String category;
    
    /**
     * 课程类别描述
     */
    private String categoryDesc;
    
    /**
     * 开课院系
     */
    private String department;
    
    /**
     * 课程描述
     */
    private String description;
    
    /**
     * 教学大纲
     */
    private String syllabus;
    
    /**
     * 先修课程列表
     */
    private List<PrerequisiteDTO> prerequisites;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 从实体转换
     */
    public static CourseDTO fromEntity(com.university.sms.course.entity.Course course) {
        if (course == null) return null;
        
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setName(course.getName());
        dto.setCredits(course.getCredits());
        dto.setTeacherId(course.getTeacherId());
        dto.setTeacherName(course.getTeacherName());
        dto.setSchedule(course.getSchedule());
        dto.setLocation(course.getLocation());
        dto.setCapacity(course.getCapacity());
        dto.setEnrolledCount(course.getEnrolledCount());
        dto.setAvailableCapacity(course.getAvailableCapacity());
        dto.setSemester(course.getSemester());
        dto.setCategory(course.getCategory());
        dto.setCategoryDesc(getCategoryDesc(course.getCategory()));
        dto.setDepartment(course.getDepartment());
        dto.setDescription(course.getDescription());
        dto.setSyllabus(course.getSyllabus());
        dto.setStatus(course.getStatus());
        return dto;
    }
    
    private static String getCategoryDesc(String category) {
        if (category == null) return "";
        return switch (category) {
            case "REQUIRED" -> "必修";
            case "ELECTIVE" -> "选修";
            case "GENERAL" -> "通识";
            default -> category;
        };
    }
    
    /**
     * 先修课程 DTO
     */
    @Data
    public static class PrerequisiteDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long courseId;
        private String courseCode;
        private String courseName;
        private BigDecimal minGrade;
    }
}
