package com.university.sms.grade.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩单 DTO - 用于生成正式学业成绩单文档
 */
@Data
public class TranscriptDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== 学生基本信息 =====
    private String studentNo;
    private String studentName;
    private String gender;
    private String department;
    private String major;
    private String classNo;
    private String enrollmentDate;
    private String academicStatus;

    // ===== GPA 汇总 =====
    private BigDecimal cumulativeGPA;
    private BigDecimal totalCredits;
    private BigDecimal earnedCredits;
    private Integer totalCourses;
    private Integer passedCourses;

    // ===== 按学期分组的成绩 =====
    private List<SemesterBlock> semesters;

    // ===== 文档信息 =====
    private String schoolName;
    private String generatedDate;
    /** HTML 格式的完整成绩单（可直接渲染/打印） */
    private String htmlContent;

    @Data
    public static class SemesterBlock implements Serializable {
        private static final long serialVersionUID = 1L;
        private String semester;
        private BigDecimal semesterGPA;
        private BigDecimal semesterCredits;
        private List<CourseGrade> courses;
    }

    @Data
    public static class CourseGrade implements Serializable {
        private static final long serialVersionUID = 1L;
        private String courseCode;
        private String courseName;
        private BigDecimal credits;
        private BigDecimal score;
        private String letterGrade;
        private BigDecimal gradePoints;
    }
}
