package com.university.sms.course.service.impl;

import com.university.sms.common.exception.BusinessException;
import com.university.sms.course.dto.CourseSelectionResultDTO;
import com.university.sms.course.dto.SelectionPhaseDTO;
import com.university.sms.course.entity.Course;
import com.university.sms.course.entity.CourseSelection;
import com.university.sms.course.enums.SelectionStatus;
import com.university.sms.course.mapper.CourseMapper;
import com.university.sms.course.mapper.CoursePrerequisiteMapper;
import com.university.sms.course.mapper.CourseSelectionMapper;
import com.university.sms.course.service.CourseService;
import com.university.sms.grade.mapper.GradeMapper;
import com.university.sms.student.mapper.StudentMapper;
import com.university.sms.system.service.SystemConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseSelectionServiceImpl 单元测试")
class CourseSelectionServiceImplTest {

    @Mock private CourseSelectionMapper selectionMapper;
    @Mock private CourseMapper courseMapper;
    @Mock private CoursePrerequisiteMapper prerequisiteMapper;
    @Mock private StudentMapper studentMapper;
    @Mock private GradeMapper gradeMapper;
    @Mock private CourseService courseService;
    @Mock private SystemConfigService systemConfigService;

    @InjectMocks
    private CourseSelectionServiceImpl selectionService;

    private Course openCourse;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        openCourse = new Course();
        openCourse.setId(1L);
        openCourse.setCourseCode("CS101");
        openCourse.setName("计算机导论");
        openCourse.setCredits(new BigDecimal("3"));
        openCourse.setCapacity(50);
        openCourse.setEnrolledCount(10);
        openCourse.setStatus(Course.STATUS_OPEN);
        openCourse.setSchedule("周一1-2节");
    }

    private void mockFormalSelectionPhase() {
        String now = LocalDateTime.now().minusHours(1).format(DT_FMT);
        String end = LocalDateTime.now().plusDays(1).format(DT_FMT);
        when(systemConfigService.getConfigValue("formal_selection_start_time", "")).thenReturn(now);
        when(systemConfigService.getConfigValue("formal_selection_end_time", "")).thenReturn(end);
        when(systemConfigService.getConfigValue("pre_selection_start_time", "")).thenReturn("");
        when(systemConfigService.getConfigValue("pre_selection_end_time", "")).thenReturn("");
        when(systemConfigService.getConfigValue("adjustment_start_time", "")).thenReturn("");
        when(systemConfigService.getConfigValue("adjustment_end_time", "")).thenReturn("");
        when(systemConfigService.getConfigValue(eq("selection_phase"), anyString())).thenReturn("FORMAL_SELECTION");
    }

    private void mockClosedPhase() {
        when(systemConfigService.getConfigValue(anyString(), anyString())).thenReturn("");
        when(systemConfigService.getConfigValue(eq("selection_phase"), anyString())).thenReturn("CLOSED");
    }

    // ===== 选课测试 =====

    @Nested
    @DisplayName("selectCourse - 选课")
    class SelectCourseTests {

        @Test
        @DisplayName("选课已关闭时应失败")
        void shouldFail_whenSelectionClosed() {
            mockClosedPhase();

            CourseSelectionResultDTO result = selectionService.selectCourse(1L, 1L);

            assertThat(result.getSuccess()).isFalse();
            assertThat(result.getMessage()).contains("结束");
        }

        @Test
        @DisplayName("课程不存在时应失败")
        void shouldFail_whenCourseNotFound() {
            mockFormalSelectionPhase();
            when(courseMapper.selectById(999L)).thenReturn(null);

            CourseSelectionResultDTO result = selectionService.selectCourse(1L, 999L);

            assertThat(result.getSuccess()).isFalse();
            assertThat(result.getMessage()).contains("课程不存在");
        }

        @Test
        @DisplayName("课程已停开时应失败")
        void shouldFail_whenCourseClosed() {
            mockFormalSelectionPhase();
            Course closed = new Course();
            closed.setId(1L);
            closed.setStatus(0);
            when(courseMapper.selectById(1L)).thenReturn(closed);

            CourseSelectionResultDTO result = selectionService.selectCourse(1L, 1L);

            assertThat(result.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("已选择过该课程时应失败")
        void shouldFail_whenAlreadySelected() {
            mockFormalSelectionPhase();
            when(courseMapper.selectById(1L)).thenReturn(openCourse);

            CourseSelection existing = new CourseSelection();
            existing.setStatus(SelectionStatus.SELECTED.getCode());
            when(selectionMapper.selectByStudentAndCourse(1L, 1L)).thenReturn(existing);

            CourseSelectionResultDTO result = selectionService.selectCourse(1L, 1L);

            assertThat(result.getSuccess()).isFalse();
            assertThat(result.getMessage()).contains("已选择");
        }

        @Test
        @DisplayName("正选阶段正常选课成功")
        void shouldSucceed_inFormalSelection() {
            mockFormalSelectionPhase();
            when(courseMapper.selectById(1L)).thenReturn(openCourse);
            when(selectionMapper.selectByStudentAndCourse(1L, 1L)).thenReturn(null);
            when(selectionMapper.selectByStudentId(1L)).thenReturn(Collections.emptyList());
            when(prerequisiteMapper.selectByCourseId(1L)).thenReturn(Collections.emptyList());
            when(courseService.hasAvailableCapacity(1L)).thenReturn(true);
            when(courseService.incrementEnrolledCount(1L)).thenReturn(true);

            CourseSelectionResultDTO result = selectionService.selectCourse(1L, 1L);

            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getMessage()).contains("成功");
            assertThat(result.getStatus()).isEqualTo(SelectionStatus.SELECTED.getCode());
            verify(selectionMapper).insert(any(CourseSelection.class));
        }

        @Test
        @DisplayName("课程已满时应失败（正选阶段）")
        void shouldFail_whenCourseFull() {
            mockFormalSelectionPhase();
            when(courseMapper.selectById(1L)).thenReturn(openCourse);
            when(selectionMapper.selectByStudentAndCourse(1L, 1L)).thenReturn(null);
            when(selectionMapper.selectByStudentId(1L)).thenReturn(Collections.emptyList());
            when(prerequisiteMapper.selectByCourseId(1L)).thenReturn(Collections.emptyList());
            when(courseService.hasAvailableCapacity(1L)).thenReturn(false);

            CourseSelectionResultDTO result = selectionService.selectCourse(1L, 1L);

            assertThat(result.getSuccess()).isFalse();
            assertThat(result.getMessage()).contains("已满");
        }
    }

    // ===== 退课测试 =====

    @Nested
    @DisplayName("withdrawCourse - 退课")
    class WithdrawCourseTests {

        @Test
        @DisplayName("选课关闭时不允许退课")
        void shouldThrow_whenPhaseClosed() {
            mockClosedPhase();

            assertThatThrownBy(() -> selectionService.withdrawCourse(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不允许退课");
        }

        @Test
        @DisplayName("无选课记录时抛出异常")
        void shouldThrow_whenNoSelectionRecord() {
            mockFormalSelectionPhase();
            when(selectionMapper.selectByStudentAndCourse(1L, 1L)).thenReturn(null);

            assertThatThrownBy(() -> selectionService.withdrawCourse(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未找到选课记录");
        }

        @Test
        @DisplayName("已退课状态不可再退")
        void shouldThrow_whenAlreadyWithdrawn() {
            mockFormalSelectionPhase();
            CourseSelection withdrawn = new CourseSelection();
            withdrawn.setStatus(SelectionStatus.WITHDRAWN.getCode());
            when(selectionMapper.selectByStudentAndCourse(1L, 1L)).thenReturn(withdrawn);

            assertThatThrownBy(() -> selectionService.withdrawCourse(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不可退课");
        }

        @Test
        @DisplayName("正常退课应更新状态并减少计数")
        void shouldWithdrawSuccessfully() {
            mockFormalSelectionPhase();
            CourseSelection selected = new CourseSelection();
            selected.setId(10L);
            selected.setStatus(SelectionStatus.SELECTED.getCode());
            when(selectionMapper.selectByStudentAndCourse(1L, 1L)).thenReturn(selected);

            selectionService.withdrawCourse(1L, 1L);

            verify(selectionMapper).updateById(argThat(s ->
                s.getStatus().equals(SelectionStatus.WITHDRAWN.getCode()) &&
                s.getWithdrawnAt() != null
            ));
            verify(courseService).decrementEnrolledCount(1L);
        }

        @Test
        @DisplayName("退掉待抽签状态的选课不应减少计数")
        void shouldNotDecrement_whenWithdrawingLotteryPending() {
            mockFormalSelectionPhase();
            CourseSelection pending = new CourseSelection();
            pending.setId(10L);
            pending.setStatus(SelectionStatus.LOTTERY_PENDING.getCode());
            when(selectionMapper.selectByStudentAndCourse(1L, 1L)).thenReturn(pending);

            selectionService.withdrawCourse(1L, 1L);

            verify(selectionMapper).updateById(any());
            verify(courseService, never()).decrementEnrolledCount(anyLong());
        }
    }

    // ===== 抽签测试 =====

    @Nested
    @DisplayName("executeLottery - 选课抽签")
    class LotteryTests {

        @Test
        @DisplayName("课程不存在时抛出异常")
        void shouldThrow_whenCourseNotFound() {
            when(courseMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> selectionService.executeLottery(999L))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("无待抽签记录时不执行任何更新")
        void shouldDoNothing_whenNoPendingRecords() {
            when(courseMapper.selectById(1L)).thenReturn(openCourse);
            when(selectionMapper.selectByCourseIdAndStatus(1L, SelectionStatus.LOTTERY_PENDING.getCode()))
                .thenReturn(Collections.emptyList());

            selectionService.executeLottery(1L);

            verify(selectionMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("抽签应按可用容量选中对应数量的学生")
        void shouldSelectCorrectNumberOfStudents() {
            openCourse.setCapacity(2);
            openCourse.setEnrolledCount(0);
            when(courseMapper.selectById(1L)).thenReturn(openCourse);

            List<CourseSelection> pending = new java.util.ArrayList<>();
            for (long i = 1; i <= 5; i++) {
                CourseSelection cs = new CourseSelection();
                cs.setId(i);
                cs.setStudentId(i);
                cs.setCourseId(1L);
                cs.setStatus(SelectionStatus.LOTTERY_PENDING.getCode());
                pending.add(cs);
            }
            when(selectionMapper.selectByCourseIdAndStatus(1L, SelectionStatus.LOTTERY_PENDING.getCode()))
                .thenReturn(pending);
            when(courseService.incrementEnrolledCount(1L)).thenReturn(true);

            selectionService.executeLottery(1L);

            // 5 条记录全部被更新（2 中签 + 3 未中签）
            verify(selectionMapper, times(5)).updateById(any());
            // 只有 2 人中签，调用 2 次 incrementEnrolledCount
            verify(courseService, times(2)).incrementEnrolledCount(1L);
        }
    }
}
