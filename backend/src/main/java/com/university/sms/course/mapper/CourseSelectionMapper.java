package com.university.sms.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.course.dto.CourseRosterDTO;
import com.university.sms.course.dto.ScheduleDTO;
import com.university.sms.course.entity.Course;
import com.university.sms.course.entity.CourseSelection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 选课记录 Mapper
 */
@Mapper
public interface CourseSelectionMapper extends BaseMapper<CourseSelection> {
    
    /**
     * 查询学生的选课记录
     */
    @Select("SELECT * FROM crs_course_selection WHERE student_id = #{studentId}")
    List<CourseSelection> selectByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 查询课程的选课记录
     */
    @Select("SELECT * FROM crs_course_selection WHERE course_id = #{courseId}")
    List<CourseSelection> selectByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 根据学生和课程查询选课记录
     */
    @Select("SELECT * FROM crs_course_selection WHERE student_id = #{studentId} AND course_id = #{courseId} ORDER BY created_at DESC LIMIT 1")
    CourseSelection selectByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    /**
     * 根据课程和状态查询
     */
    @Select("SELECT * FROM crs_course_selection WHERE course_id = #{courseId} AND status = #{status}")
    List<CourseSelection> selectByCourseIdAndStatus(@Param("courseId") Long courseId, @Param("status") String status);
    
    /**
     * 检查学生是否已选某课程
     */
    @Select("SELECT COUNT(*) > 0 FROM crs_course_selection WHERE student_id = #{studentId} AND course_id = #{courseId} AND status = 'SELECTED'")
    boolean existsByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    /**
     * 获取学生已选课程列表
     */
    List<Course> selectStudentCourses(@Param("studentId") Long studentId, @Param("semester") String semester);
    
    /**
     * 获取学生课表
     */
    List<ScheduleDTO> selectStudentSchedule(@Param("studentId") Long studentId, @Param("semester") String semester);
    
    /**
     * 获取课程学生名单
     */
    List<CourseRosterDTO> selectCourseRoster(@Param("courseId") Long courseId);
    
    /**
     * 统计课程选课人数
     */
    @Select("SELECT COUNT(*) FROM crs_course_selection WHERE course_id = #{courseId} AND status = 'SELECTED'")
    int countByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 获取待抽签的选课记录
     */
    @Select("SELECT * FROM crs_course_selection WHERE course_id = #{courseId} AND status = 'LOTTERY_PENDING'")
    List<CourseSelection> selectLotteryPending(@Param("courseId") Long courseId);
    
    /**
     * 获取学生在指定学期的已选课程ID列表
     */
    @Select("SELECT cs.course_id FROM crs_course_selection cs " +
            "INNER JOIN crs_course c ON cs.course_id = c.id " +
            "WHERE cs.student_id = #{studentId} AND c.semester = #{semester} AND cs.status = 'SELECTED'")
    List<Long> selectStudentCourseIds(@Param("studentId") Long studentId, @Param("semester") String semester);

    /**
     * 获取有待抽签记录的课程ID列表
     */
    @Select("SELECT DISTINCT course_id FROM crs_course_selection WHERE status = 'LOTTERY_PENDING'")
    List<Long> selectCoursesWithLotteryPending();

    /**
     * 统计课程的待抽签人数
     */
    @Select("SELECT COUNT(*) FROM crs_course_selection WHERE course_id = #{courseId} AND status = 'LOTTERY_PENDING'")
    int countLotteryPendingByCourseId(@Param("courseId") Long courseId);

    /**
     * 查询学生在某课程的待抽签或抽签失败记录
     */
    @Select("SELECT * FROM crs_course_selection WHERE student_id = #{studentId} AND course_id = #{courseId} " +
            "AND status IN ('LOTTERY_PENDING', 'LOTTERY_FAILED') ORDER BY created_at DESC LIMIT 1")
    CourseSelection selectLotteryRecord(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
