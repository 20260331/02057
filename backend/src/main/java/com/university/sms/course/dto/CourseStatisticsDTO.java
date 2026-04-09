package com.university.sms.course.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 选课统计 DTO
 */
@Data
public class CourseStatisticsDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 学期
     */
    private String semester;
    
    /**
     * 总课程数
     */
    private Integer totalCourses;
    
    /**
     * 总选课人次
     */
    private Integer totalSelections;
    
    /**
     * 总已选人数
     */
    private Integer totalEnrolled;
    
    /**
     * 总容量
     */
    private Integer totalCapacity;
    
    /**
     * 平均选课人数
     */
    private BigDecimal averageEnrollment;
    
    /**
     * 容量利用率
     */
    private Double capacityUtilization;
    
    /**
     * 热门课程列表
     */
    private List<PopularCourse> popularCourses;
    
    /**
     * 按类别统计
     */
    private Map<String, CategoryStats> categoryStats;
    
    /**
     * 按院系统计
     */
    private Map<String, DepartmentStats> departmentStats;
    
    /**
     * 热门课程
     */
    @Data
    public static class PopularCourse implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long courseId;
        private String courseCode;
        private String courseName;
        private String teacherName;
        private Integer capacity;
        private Integer enrolledCount;
        private BigDecimal utilizationRate;
    }
    
    /**
     * 类别统计
     */
    @Data
    public static class CategoryStats implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String category;
        private String categoryDesc;
        private Integer courseCount;
        private Integer totalCapacity;
        private Integer totalEnrolled;
        private BigDecimal utilizationRate;
    }
    
    /**
     * 院系统计
     */
    @Data
    public static class DepartmentStats implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String department;
        private Integer courseCount;
        private Integer totalCapacity;
        private Integer totalEnrolled;
        private BigDecimal utilizationRate;
    }
}
