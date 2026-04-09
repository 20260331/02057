package com.university.sms.grade.service.impl;

import com.university.sms.common.exception.BusinessException;
import com.university.sms.course.entity.Course;
import com.university.sms.course.mapper.CourseMapper;
import com.university.sms.grade.dto.*;
import com.university.sms.grade.entity.Grade;
import com.university.sms.grade.entity.GradeChangeLog;
import com.university.sms.grade.mapper.GradeChangeLogMapper;
import com.university.sms.grade.mapper.GradeMapper;
import com.university.sms.student.entity.Student;
import com.university.sms.student.mapper.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GradeServiceImpl 单元测试")
class GradeServiceImplTest {

    @Mock private GradeMapper gradeMapper;
    @Mock private GradeChangeLogMapper changeLogMapper;
    @Mock private CourseMapper courseMapper;
    @Mock private StudentMapper studentMapper;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private Course mathCourse;
    private Course physicsCourse;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        mathCourse = new Course();
        mathCourse.setId(1L);
        mathCourse.setCourseCode("MATH101");
        mathCourse.setName("高等数学");
        mathCourse.setCredits(new BigDecimal("4"));
        mathCourse.setSemester("2024-2025-1");

        physicsCourse = new Course();
        physicsCourse.setId(2L);
        physicsCourse.setCourseCode("PHY101");
        physicsCourse.setName("大学物理");
        physicsCourse.setCredits(new BigDecimal("3"));
        physicsCourse.setSemester("2024-2025-1");

        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setStudentNo("2024001");
        testStudent.setName("张三");
        testStudent.setGender("M");
        testStudent.setDepartment("计算机学院");
        testStudent.setMajor("软件工程");
        testStudent.setClassNo("软工2401");
        testStudent.setEnrollmentDate(LocalDate.of(2024, 9, 1));
    }

    // ===== GPA 计算测试 =====

    @Nested
    @DisplayName("calculateGPA - GPA 计算")
    class CalculateGPATests {

        @Test
        @DisplayName("无成绩时 GPA 应为 0")
        void shouldReturnZeroGPA_whenNoGrades() {
            when(gradeMapper.selectByStudentId(1L)).thenReturn(Collections.emptyList());

            GPAInfoDTO result = gradeService.calculateGPA(1L);

            assertThat(result.getCumulativeGPA()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getTotalCredits()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getEarnedCredits()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getSemesterGPAs()).isEmpty();
        }

        @Test
        @DisplayName("只统计 APPROVED 状态的成绩")
        void shouldOnlyCountApprovedGrades() {
            Grade approved = createGrade(1L, 1L, new BigDecimal("90"), new BigDecimal("4.0"), Grade.STATUS_APPROVED);
            Grade draft = createGrade(1L, 2L, new BigDecimal("80"), new BigDecimal("3.0"), Grade.STATUS_DRAFT);

            when(gradeMapper.selectByStudentId(1L)).thenReturn(List.of(approved, draft));
            when(courseMapper.selectById(1L)).thenReturn(mathCourse);

            GPAInfoDTO result = gradeService.calculateGPA(1L);

            assertThat(result.getCumulativeGPA()).isEqualByComparingTo(new BigDecimal("4.00"));
            assertThat(result.getTotalCredits()).isEqualByComparingTo(new BigDecimal("4"));
        }

        @Test
        @DisplayName("多科目加权 GPA 计算正确")
        void shouldCalculateWeightedGPA() {
            // 数学: 4学分 * 4.0绩点 = 16, 物理: 3学分 * 3.0绩点 = 9
            // 总: 25/7 = 3.57
            Grade math = createGrade(1L, 1L, new BigDecimal("95"), new BigDecimal("4.0"), Grade.STATUS_APPROVED);
            Grade physics = createGrade(1L, 2L, new BigDecimal("85"), new BigDecimal("3.0"), Grade.STATUS_APPROVED);

            when(gradeMapper.selectByStudentId(1L)).thenReturn(List.of(math, physics));
            when(courseMapper.selectById(1L)).thenReturn(mathCourse);
            when(courseMapper.selectById(2L)).thenReturn(physicsCourse);

            GPAInfoDTO result = gradeService.calculateGPA(1L);

            BigDecimal expected = new BigDecimal("25").divide(new BigDecimal("7"), 2, RoundingMode.HALF_UP);
            assertThat(result.getCumulativeGPA()).isEqualByComparingTo(expected);
            assertThat(result.getTotalCredits()).isEqualByComparingTo(new BigDecimal("7"));
            assertThat(result.getEarnedCredits()).isEqualByComparingTo(new BigDecimal("7"));
        }

        @Test
        @DisplayName("不及格成绩不计入已获学分")
        void shouldNotCountFailingCreditsAsEarned() {
            Grade failing = createGrade(1L, 1L, new BigDecimal("50"), BigDecimal.ZERO, Grade.STATUS_APPROVED);

            when(gradeMapper.selectByStudentId(1L)).thenReturn(List.of(failing));
            when(courseMapper.selectById(1L)).thenReturn(mathCourse);

            GPAInfoDTO result = gradeService.calculateGPA(1L);

            assertThat(result.getTotalCredits()).isEqualByComparingTo(new BigDecimal("4"));
            assertThat(result.getEarnedCredits()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("按学期分组计算正确")
        void shouldGroupBySemester() {
            Course sem2Course = new Course();
            sem2Course.setId(3L);
            sem2Course.setCredits(new BigDecimal("2"));
            sem2Course.setSemester("2024-2025-2");

            Grade g1 = createGrade(1L, 1L, new BigDecimal("90"), new BigDecimal("4.0"), Grade.STATUS_APPROVED);
            Grade g2 = createGrade(1L, 3L, new BigDecimal("80"), new BigDecimal("3.0"), Grade.STATUS_APPROVED);

            when(gradeMapper.selectByStudentId(1L)).thenReturn(List.of(g1, g2));
            when(courseMapper.selectById(1L)).thenReturn(mathCourse);
            when(courseMapper.selectById(3L)).thenReturn(sem2Course);

            GPAInfoDTO result = gradeService.calculateGPA(1L);

            assertThat(result.getSemesterGPAs()).hasSize(2);
        }
    }

    // ===== 等级/绩点计算测试 =====

    @Nested
    @DisplayName("calculateLetterGrade / calculateGradePoints - 等级和绩点映射")
    class GradeCalculationTests {

        @ParameterizedTest(name = "分数 {0} -> 等级 {1}, 绩点 {2}")
        @CsvSource({
            "95, A, 4.0",
            "90, A, 4.0",
            "89, B, 3.0",
            "80, B, 3.0",
            "79, C, 2.0",
            "70, C, 2.0",
            "69, D, 1.0",
            "60, D, 1.0",
            "59, F, 0",
            "0,  F, 0",
            "100, A, 4.0"
        })
        @DisplayName("分数到等级/绩点映射")
        void shouldMapScoreToGradeAndPoints(String scoreStr, String expectedGrade, String expectedPoints) {
            Grade grade = createGrade(1L, 1L, new BigDecimal(scoreStr), null, Grade.STATUS_DRAFT);

            when(gradeMapper.selectByStudentAndCourse(1L, 1L)).thenReturn(null);
            when(courseMapper.selectById(1L)).thenReturn(mathCourse);

            BatchGradeDTO dto = new BatchGradeDTO();
            dto.setCourseId(1L);
            BatchGradeDTO.GradeItem item = new BatchGradeDTO.GradeItem();
            item.setStudentId(1L);
            item.setScore(new BigDecimal(scoreStr));
            dto.setGrades(List.of(item));

            gradeService.batchSaveGrades(dto, 99L);

            verify(gradeMapper).insert(argThat(g ->
                g.getLetterGrade().equals(expectedGrade) &&
                g.getGradePoints().compareTo(new BigDecimal(expectedPoints)) == 0
            ));
        }
    }

    // ===== 不及格检查测试 =====

    @Nested
    @DisplayName("hasFailingGrades - 不及格成绩检测")
    class FailingGradesTests {

        @Test
        @DisplayName("存在不及格成绩时返回 true")
        void shouldReturnTrue_whenHasFailingGrade() {
            Grade failing = createGrade(1L, 1L, new BigDecimal("55"), BigDecimal.ZERO, Grade.STATUS_APPROVED);
            when(gradeMapper.selectByStudentId(1L)).thenReturn(List.of(failing));

            assertThat(gradeService.hasFailingGrades(1L)).isTrue();
        }

        @Test
        @DisplayName("全部及格时返回 false")
        void shouldReturnFalse_whenAllPassing() {
            Grade passing = createGrade(1L, 1L, new BigDecimal("75"), new BigDecimal("2.0"), Grade.STATUS_APPROVED);
            when(gradeMapper.selectByStudentId(1L)).thenReturn(List.of(passing));

            assertThat(gradeService.hasFailingGrades(1L)).isFalse();
        }

        @Test
        @DisplayName("无成绩时返回 false")
        void shouldReturnFalse_whenNoGrades() {
            when(gradeMapper.selectByStudentId(1L)).thenReturn(Collections.emptyList());

            assertThat(gradeService.hasFailingGrades(1L)).isFalse();
        }

        @Test
        @DisplayName("60 分恰好及格")
        void shouldReturnFalse_whenScoreExactly60() {
            Grade borderline = createGrade(1L, 1L, new BigDecimal("60"), new BigDecimal("1.0"), Grade.STATUS_APPROVED);
            when(gradeMapper.selectByStudentId(1L)).thenReturn(List.of(borderline));

            assertThat(gradeService.hasFailingGrades(1L)).isFalse();
        }
    }

    // ===== 批量成绩录入测试 =====

    @Nested
    @DisplayName("batchSaveGrades - 批量成绩录入")
    class BatchSaveGradesTests {

        @Test
        @DisplayName("课程不存在时抛出异常")
        void shouldThrowException_whenCourseNotFound() {
            when(courseMapper.selectById(999L)).thenReturn(null);

            BatchGradeDTO dto = new BatchGradeDTO();
            dto.setCourseId(999L);
            dto.setGrades(List.of());

            assertThatThrownBy(() -> gradeService.batchSaveGrades(dto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("课程不存在");
        }

        @Test
        @DisplayName("分数超出 0-100 范围时抛出异常")
        void shouldThrowException_whenScoreOutOfRange() {
            when(courseMapper.selectById(1L)).thenReturn(mathCourse);

            BatchGradeDTO dto = new BatchGradeDTO();
            dto.setCourseId(1L);
            BatchGradeDTO.GradeItem item = new BatchGradeDTO.GradeItem();
            item.setStudentId(1L);
            item.setScore(new BigDecimal("105"));
            dto.setGrades(List.of(item));

            assertThatThrownBy(() -> gradeService.batchSaveGrades(dto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分数必须在 0-100 之间");
        }

        @Test
        @DisplayName("负数分数抛出异常")
        void shouldThrowException_whenScoreNegative() {
            when(courseMapper.selectById(1L)).thenReturn(mathCourse);

            BatchGradeDTO dto = new BatchGradeDTO();
            dto.setCourseId(1L);
            BatchGradeDTO.GradeItem item = new BatchGradeDTO.GradeItem();
            item.setStudentId(1L);
            item.setScore(new BigDecimal("-5"));
            dto.setGrades(List.of(item));

            assertThatThrownBy(() -> gradeService.batchSaveGrades(dto, 1L))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("新成绩应执行 insert")
        void shouldInsertNewGrade() {
            when(courseMapper.selectById(1L)).thenReturn(mathCourse);
            when(gradeMapper.selectByStudentAndCourse(1L, 1L)).thenReturn(null);

            BatchGradeDTO dto = new BatchGradeDTO();
            dto.setCourseId(1L);
            BatchGradeDTO.GradeItem item = new BatchGradeDTO.GradeItem();
            item.setStudentId(1L);
            item.setScore(new BigDecimal("85"));
            dto.setGrades(List.of(item));

            gradeService.batchSaveGrades(dto, 99L);

            verify(gradeMapper).insert(argThat(g ->
                g.getStudentId().equals(1L) &&
                g.getCourseId().equals(1L) &&
                g.getScore().compareTo(new BigDecimal("85")) == 0 &&
                g.getStatus().equals(Grade.STATUS_DRAFT)
            ));
            verify(gradeMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("已有成绩应执行 update")
        void shouldUpdateExistingGrade() {
            Grade existing = createGrade(1L, 1L, new BigDecimal("70"), new BigDecimal("2.0"), Grade.STATUS_DRAFT);
            existing.setId(10L);

            when(courseMapper.selectById(1L)).thenReturn(mathCourse);
            when(gradeMapper.selectByStudentAndCourse(1L, 1L)).thenReturn(existing);

            BatchGradeDTO dto = new BatchGradeDTO();
            dto.setCourseId(1L);
            BatchGradeDTO.GradeItem item = new BatchGradeDTO.GradeItem();
            item.setStudentId(1L);
            item.setScore(new BigDecimal("85"));
            dto.setGrades(List.of(item));

            gradeService.batchSaveGrades(dto, 99L);

            verify(gradeMapper).updateById(argThat(g ->
                g.getId().equals(10L) &&
                g.getScore().compareTo(new BigDecimal("85")) == 0
            ));
            verify(gradeMapper, never()).insert(any());
        }

        @Test
        @DisplayName("已提交成绩被修改分数时应重置为草稿状态")
        void shouldResetToDraft_whenSubmittedGradeModified() {
            Grade submitted = createGrade(1L, 1L, new BigDecimal("70"), new BigDecimal("2.0"), Grade.STATUS_SUBMITTED);
            submitted.setId(10L);

            when(courseMapper.selectById(1L)).thenReturn(mathCourse);
            when(gradeMapper.selectByStudentAndCourse(1L, 1L)).thenReturn(submitted);

            BatchGradeDTO dto = new BatchGradeDTO();
            dto.setCourseId(1L);
            BatchGradeDTO.GradeItem item = new BatchGradeDTO.GradeItem();
            item.setStudentId(1L);
            item.setScore(new BigDecimal("85"));
            dto.setGrades(List.of(item));

            gradeService.batchSaveGrades(dto, 99L);

            verify(gradeMapper).updateById(argThat(g ->
                g.getStatus().equals(Grade.STATUS_DRAFT)
            ));
        }
    }

    // ===== 成绩查询测试 =====

    @Nested
    @DisplayName("getStudentGrades - 成绩查询")
    class GetStudentGradesTests {

        @Test
        @DisplayName("按学期查询")
        void shouldQueryBySemester() {
            Grade grade = createGrade(1L, 1L, new BigDecimal("90"), new BigDecimal("4.0"), Grade.STATUS_APPROVED);
            when(gradeMapper.selectByStudentAndSemester(1L, "2024-2025-1")).thenReturn(List.of(grade));
            when(studentMapper.selectById(1L)).thenReturn(testStudent);
            when(courseMapper.selectById(1L)).thenReturn(mathCourse);

            List<GradeDTO> result = gradeService.getStudentGrades(1L, "2024-2025-1");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCourseName()).isEqualTo("高等数学");
            verify(gradeMapper).selectByStudentAndSemester(1L, "2024-2025-1");
            verify(gradeMapper, never()).selectByStudentId(anyLong());
        }

        @Test
        @DisplayName("不指定学期查询全部")
        void shouldQueryAllWhenNoSemester() {
            when(gradeMapper.selectByStudentId(1L)).thenReturn(Collections.emptyList());

            List<GradeDTO> result = gradeService.getStudentGrades(1L, null);

            assertThat(result).isEmpty();
            verify(gradeMapper).selectByStudentId(1L);
        }
    }

    // ===== 成绩统计测试 =====

    @Nested
    @DisplayName("getStatistics - 成绩统计")
    class StatisticsTests {

        @Test
        @DisplayName("课程不存在时抛出异常")
        void shouldThrow_whenCourseNotFound() {
            when(courseMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> gradeService.getStatistics(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("课程不存在");
        }

        @Test
        @DisplayName("统计数据计算正确")
        void shouldCalculateStatisticsCorrectly() {
            Grade g1 = createGrade(1L, 1L, new BigDecimal("95"), new BigDecimal("4.0"), Grade.STATUS_APPROVED);
            g1.setLetterGrade("A");
            Grade g2 = createGrade(2L, 1L, new BigDecimal("75"), new BigDecimal("2.0"), Grade.STATUS_APPROVED);
            g2.setLetterGrade("C");
            Grade g3 = createGrade(3L, 1L, new BigDecimal("55"), BigDecimal.ZERO, Grade.STATUS_APPROVED);
            g3.setLetterGrade("F");

            when(courseMapper.selectById(1L)).thenReturn(mathCourse);
            when(gradeMapper.selectByCourseId(1L)).thenReturn(List.of(g1, g2, g3));

            GradeStatisticsDTO stats = gradeService.getStatistics(1L);

            assertThat(stats.getTotalStudents()).isEqualTo(3);
            assertThat(stats.getMaxScore()).isEqualByComparingTo(new BigDecimal("95"));
            assertThat(stats.getMinScore()).isEqualByComparingTo(new BigDecimal("55"));
            assertThat(stats.getDistribution()).containsKeys("A", "C", "F");
        }
    }

    // ===== 成绩单生成测试 =====

    @Nested
    @DisplayName("generateTranscript - 成绩单生成")
    class TranscriptTests {

        @Test
        @DisplayName("学生不存在时抛出异常")
        void shouldThrow_whenStudentNotFound() {
            when(studentMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> gradeService.generateTranscript(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("学生不存在");
        }

        @Test
        @DisplayName("应返回完整的成绩单数据和 HTML 内容")
        void shouldGenerateCompleteTranscript() {
            when(studentMapper.selectById(1L)).thenReturn(testStudent);

            Grade grade = createGrade(1L, 1L, new BigDecimal("90"), new BigDecimal("4.0"), Grade.STATUS_APPROVED);
            when(gradeMapper.selectByStudentId(1L)).thenReturn(List.of(grade));
            when(courseMapper.selectById(1L)).thenReturn(mathCourse);

            TranscriptDTO result = gradeService.generateTranscript(1L);

            assertThat(result.getStudentNo()).isEqualTo("2024001");
            assertThat(result.getStudentName()).isEqualTo("张三");
            assertThat(result.getGender()).isEqualTo("男");
            assertThat(result.getDepartment()).isEqualTo("计算机学院");
            assertThat(result.getTotalCourses()).isEqualTo(1);
            assertThat(result.getPassedCourses()).isEqualTo(1);
            assertThat(result.getSemesters()).hasSize(1);
            assertThat(result.getHtmlContent()).isNotBlank();
            assertThat(result.getHtmlContent()).contains("张三");
            assertThat(result.getHtmlContent()).contains("高等数学");
            assertThat(result.getHtmlContent()).contains("OFFICIAL ACADEMIC TRANSCRIPT");
        }
    }

    // ===== 工具方法 =====

    private Grade createGrade(Long studentId, Long courseId, BigDecimal score,
                              BigDecimal gradePoints, String status) {
        Grade grade = new Grade();
        grade.setStudentId(studentId);
        grade.setCourseId(courseId);
        grade.setScore(score);
        grade.setGradePoints(gradePoints != null ? gradePoints : BigDecimal.ZERO);
        grade.setStatus(status);
        return grade;
    }
}
