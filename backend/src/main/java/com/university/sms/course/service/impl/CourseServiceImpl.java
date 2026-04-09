package com.university.sms.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.sms.common.exception.BusinessException;
import com.university.sms.common.response.PageResult;
import com.university.sms.course.dto.CourseDTO;
import com.university.sms.course.dto.CourseQueryDTO;
import com.university.sms.course.dto.CourseStatisticsDTO;
import com.university.sms.course.entity.Course;
import com.university.sms.course.entity.CourseSelection;
import com.university.sms.course.mapper.CourseMapper;
import com.university.sms.course.mapper.CourseSelectionMapper;
import com.university.sms.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    
    private final CourseMapper courseMapper;
    private final CourseSelectionMapper courseSelectionMapper;
    
    @Override
    public PageResult<CourseDTO> queryCourses(CourseQueryDTO query) {
        Page<Course> page = new Page<>(query.getPage(), query.getSize());
        page = courseMapper.selectCoursePage(page, query);
        
        List<CourseDTO> records = page.getRecords().stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }
    
    @Override
    public List<CourseDTO> searchCourses(String keyword, String semester) {
        List<Course> courses = courseMapper.searchCourses(keyword, semester);
        return courses.stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public CourseDTO getCourseById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        return CourseDTO.fromEntity(course);
    }
    
    @Override
    public Course getCourseEntity(Long id) {
        return courseMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public CourseDTO createCourse(CourseDTO courseDTO) {
        // 检查课程编号是否已存在
        Course existing = courseMapper.selectOne(
            new LambdaQueryWrapper<Course>()
                .eq(Course::getCourseCode, courseDTO.getCourseCode())
                .eq(Course::getSemester, courseDTO.getSemester())
                .eq(Course::getDeleted, 0));
        if (existing != null) {
            throw new BusinessException("该学期已存在相同课程编号的课程");
        }
        
        Course course = new Course();
        course.setCourseCode(courseDTO.getCourseCode());
        course.setName(courseDTO.getName());
        course.setCredits(courseDTO.getCredits());
        course.setTeacherId(courseDTO.getTeacherId());
        course.setTeacherName(courseDTO.getTeacherName());
        course.setSchedule(courseDTO.getSchedule());
        course.setLocation(courseDTO.getLocation());
        course.setCapacity(courseDTO.getCapacity());
        course.setEnrolledCount(0);
        course.setSemester(courseDTO.getSemester());
        course.setCategory(courseDTO.getCategory());
        course.setDepartment(courseDTO.getDepartment());
        course.setDescription(courseDTO.getDescription());
        course.setStatus(1);
        course.setDeleted(0);
        
        courseMapper.insert(course);
        log.info("创建课程: {} - {}", course.getCourseCode(), course.getName());
        return CourseDTO.fromEntity(course);
    }
    
    @Override
    @Transactional
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 如果修改了课程编号，检查是否冲突
        if (!course.getCourseCode().equals(courseDTO.getCourseCode())) {
            Course existing = courseMapper.selectOne(
                new LambdaQueryWrapper<Course>()
                    .eq(Course::getCourseCode, courseDTO.getCourseCode())
                    .eq(Course::getSemester, courseDTO.getSemester())
                    .eq(Course::getDeleted, 0)
                    .ne(Course::getId, id));
            if (existing != null) {
                throw new BusinessException("该学期已存在相同课程编号的课程");
            }
        }
        
        course.setCourseCode(courseDTO.getCourseCode());
        course.setName(courseDTO.getName());
        course.setCredits(courseDTO.getCredits());
        course.setTeacherId(courseDTO.getTeacherId());
        course.setTeacherName(courseDTO.getTeacherName());
        course.setSchedule(courseDTO.getSchedule());
        course.setLocation(courseDTO.getLocation());
        course.setCapacity(courseDTO.getCapacity());
        course.setSemester(courseDTO.getSemester());
        course.setCategory(courseDTO.getCategory());
        course.setDepartment(courseDTO.getDepartment());
        course.setDescription(courseDTO.getDescription());
        
        courseMapper.updateById(course);
        log.info("更新课程: {} - {}", course.getCourseCode(), course.getName());
        return CourseDTO.fromEntity(course);
    }
    
    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 检查是否有学生已选该课程（只统计状态为SELECTED的）
        Long actualEnrolled = courseSelectionMapper.selectCount(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, id)
                .eq(CourseSelection::getStatus, "SELECTED")
        );
        
        if (actualEnrolled != null && actualEnrolled > 0) {
            throw new BusinessException("该课程已有 " + actualEnrolled + " 名学生选课，无法删除");
        }
        
        // 删除非SELECTED状态的选课记录（如已退课的记录）
        courseSelectionMapper.delete(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, id)
        );
        
        // 使用 MyBatis-Plus 的逻辑删除
        courseMapper.deleteById(id);
        log.info("删除课程: {} - {}", course.getCourseCode(), course.getName());
    }
    
    @Override
    public List<CourseDTO> getTeacherCourses(Long teacherId) {
        List<Course> courses = courseMapper.selectByTeacherId(teacherId);
        return courses.stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CourseDTO> getSemesterCourses(String semester) {
        List<Course> courses = courseMapper.selectBySemester(semester);
        return courses.stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public CourseStatisticsDTO getStatistics(String semester) {
        CourseStatisticsDTO stats = new CourseStatisticsDTO();
        stats.setSemester(semester);
        stats.setTotalCourses(courseMapper.countBySemester(semester));
        stats.setTotalEnrolled(courseMapper.sumEnrolledCountBySemester(semester));
        stats.setTotalCapacity(courseMapper.sumCapacityBySemester(semester));
        
        if (stats.getTotalCapacity() != null && stats.getTotalCapacity() > 0) {
            stats.setCapacityUtilization(
                    (double) stats.getTotalEnrolled() / stats.getTotalCapacity() * 100);
        }
        
        // 获取热门课程
        List<Course> popularCourses = courseMapper.selectPopularCourses(semester, 10);
        stats.setPopularCourses(popularCourses.stream()
                .map(c -> {
                    CourseStatisticsDTO.PopularCourse pc = new CourseStatisticsDTO.PopularCourse();
                    pc.setCourseId(c.getId());
                    pc.setCourseCode(c.getCourseCode());
                    pc.setCourseName(c.getName());
                    pc.setCapacity(c.getCapacity());
                    pc.setEnrolledCount(c.getEnrolledCount());
                    return pc;
                })
                .collect(Collectors.toList()));
        
        return stats;
    }
    
    @Override
    public boolean incrementEnrolledCount(Long courseId) {
        return courseMapper.incrementEnrolledCount(courseId) > 0;
    }
    
    @Override
    public boolean decrementEnrolledCount(Long courseId) {
        return courseMapper.decrementEnrolledCount(courseId) > 0;
    }
    
    @Override
    public boolean hasAvailableCapacity(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        return course != null && course.getAvailableCapacity() > 0;
    }
    
    @Override
    public int getAvailableCapacity(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        return course != null ? course.getAvailableCapacity() : 0;
    }
}
