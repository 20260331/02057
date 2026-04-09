package com.university.sms.common;

import com.university.sms.common.exception.BusinessException;
import com.university.sms.common.response.Result;
import com.university.sms.common.response.ResultCode;
import com.university.sms.grade.dto.GradeDTO;
import com.university.sms.grade.dto.GPAInfoDTO;
import com.university.sms.grade.dto.TranscriptDTO;
import com.university.sms.course.dto.SelectionPhaseDTO;
import com.university.sms.system.dto.NotificationTemplateDTO;
import com.university.sms.system.dto.NotificationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("公共类 / DTO 单元测试")
class ResultAndExceptionTest {

    // ===== Result 统一响应 =====

    @Nested
    @DisplayName("Result - 统一响应封装")
    class ResultTests {

        @Test
        @DisplayName("success() 返回 200 状态码")
        void successWithoutData() {
            Result<Void> result = Result.success();

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getMessage()).isEqualTo("操作成功");
            assertThat(result.getData()).isNull();
            assertThat(result.getTimestamp()).isPositive();
        }

        @Test
        @DisplayName("success(data) 携带数据")
        void successWithData() {
            Result<String> result = Result.success("hello");

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).isEqualTo("hello");
        }

        @Test
        @DisplayName("error(message) 返回 500 和自定义消息")
        void errorWithMessage() {
            Result<Void> result = Result.error("something went wrong");

            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).isEqualTo("something went wrong");
            assertThat(result.getData()).isNull();
        }

        @Test
        @DisplayName("error(code, message) 返回自定义状态码")
        void errorWithCodeAndMessage() {
            Result<Void> result = Result.error(403, "forbidden");

            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).isEqualTo("forbidden");
        }

        @Test
        @DisplayName("error(ResultCode) 使用枚举值")
        void errorWithResultCode() {
            Result<Void> result = Result.error(ResultCode.UNAUTHORIZED);

            assertThat(result.getCode()).isEqualTo(401);
            assertThat(result.getMessage()).isEqualTo("未授权，请先登录");
        }
    }

    // ===== BusinessException =====

    @Nested
    @DisplayName("BusinessException - 业务异常")
    class BusinessExceptionTests {

        @Test
        @DisplayName("默认错误码为 500")
        void defaultCode() {
            BusinessException ex = new BusinessException("test error");

            assertThat(ex.getCode()).isEqualTo(500);
            assertThat(ex.getMessage()).isEqualTo("test error");
        }

        @Test
        @DisplayName("自定义错误码")
        void customCode() {
            BusinessException ex = new BusinessException(1001, "param error");

            assertThat(ex.getCode()).isEqualTo(1001);
            assertThat(ex.getMessage()).isEqualTo("param error");
        }

        @Test
        @DisplayName("从 ResultCode 构造")
        void fromResultCode() {
            BusinessException ex = new BusinessException(ResultCode.COURSE_FULL);

            assertThat(ex.getCode()).isEqualTo(4002);
            assertThat(ex.getMessage()).isEqualTo("课程已满");
        }

        @Test
        @DisplayName("ResultCode + 自定义消息")
        void fromResultCodeWithMessage() {
            BusinessException ex = new BusinessException(ResultCode.STUDENT_NOT_FOUND, "学生 2024001 不存在");

            assertThat(ex.getCode()).isEqualTo(3001);
            assertThat(ex.getMessage()).isEqualTo("学生 2024001 不存在");
        }
    }

    // ===== ResultCode 枚举 =====

    @Nested
    @DisplayName("ResultCode - 状态码枚举")
    class ResultCodeTests {

        @ParameterizedTest
        @EnumSource(ResultCode.class)
        @DisplayName("每个状态码都有非空消息")
        void allCodesHaveMessages(ResultCode code) {
            assertThat(code.getCode()).isNotNull();
            assertThat(code.getMessage()).isNotBlank();
        }

        @Test
        @DisplayName("成功码为 200")
        void successCodeIs200() {
            assertThat(ResultCode.SUCCESS.getCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("业务错误码范围 1xxx-6xxx")
        void businessCodesInRange() {
            assertThat(ResultCode.PARAM_ERROR.getCode()).isBetween(1000, 9999);
            assertThat(ResultCode.COURSE_FULL.getCode()).isBetween(1000, 9999);
            assertThat(ResultCode.GRADE_NOT_FOUND.getCode()).isBetween(1000, 9999);
        }
    }

    // ===== GradeDTO 状态文本 =====

    @Nested
    @DisplayName("GradeDTO.getStatusText - 状态文本映射")
    class GradeStatusTextTests {

        @Test
        @DisplayName("状态映射正确")
        void shouldMapStatusText() {
            assertThat(GradeDTO.getStatusText("DRAFT")).isEqualTo("草稿");
            assertThat(GradeDTO.getStatusText("SUBMITTED")).isEqualTo("已提交");
            assertThat(GradeDTO.getStatusText("APPROVED")).isEqualTo("已确认");
            assertThat(GradeDTO.getStatusText(null)).isEmpty();
            assertThat(GradeDTO.getStatusText("UNKNOWN")).isEqualTo("UNKNOWN");
        }
    }

    // ===== SelectionPhaseDTO 阶段名称 =====

    @Nested
    @DisplayName("SelectionPhaseDTO.phaseDisplayName - 阶段名称映射")
    class SelectionPhaseTests {

        @Test
        @DisplayName("阶段常量定义正确")
        void shouldHaveCorrectConstants() {
            assertThat(SelectionPhaseDTO.PHASE_PRE_SELECTION).isEqualTo("PRE_SELECTION");
            assertThat(SelectionPhaseDTO.PHASE_FORMAL_SELECTION).isEqualTo("FORMAL_SELECTION");
        }

        @Test
        @DisplayName("阶段显示名称映射正确")
        void shouldMapPhaseNames() {
            assertThat(SelectionPhaseDTO.phaseDisplayName("PRE_SELECTION")).isEqualTo("预选阶段");
            assertThat(SelectionPhaseDTO.phaseDisplayName("FORMAL_SELECTION")).isEqualTo("正选阶段");
            assertThat(SelectionPhaseDTO.phaseDisplayName("CLOSED")).isEqualTo("选课已结束");
            assertThat(SelectionPhaseDTO.phaseDisplayName("NOT_STARTED")).isEqualTo("未开始");
        }
    }

    // ===== GPAInfoDTO =====

    @Nested
    @DisplayName("GPAInfoDTO - GPA 数据结构")
    class GPAInfoTests {

        @Test
        @DisplayName("默认值应可正常设置和读取")
        void shouldGetAndSet() {
            GPAInfoDTO gpa = new GPAInfoDTO();
            gpa.setCumulativeGPA(new BigDecimal("3.50"));
            gpa.setTotalCredits(new BigDecimal("60"));
            gpa.setEarnedCredits(new BigDecimal("55"));

            assertThat(gpa.getCumulativeGPA()).isEqualByComparingTo(new BigDecimal("3.50"));
            assertThat(gpa.getTotalCredits()).isEqualByComparingTo(new BigDecimal("60"));
            assertThat(gpa.getEarnedCredits()).isEqualByComparingTo(new BigDecimal("55"));
        }

        @Test
        @DisplayName("学期 GPA 列表")
        void shouldHandleSemesterGPAs() {
            GPAInfoDTO.SemesterGPA sem1 = new GPAInfoDTO.SemesterGPA();
            sem1.setSemester("2024-2025-1");
            sem1.setGpa(new BigDecimal("3.80"));
            sem1.setCredits(new BigDecimal("20"));

            GPAInfoDTO gpa = new GPAInfoDTO();
            gpa.setSemesterGPAs(java.util.List.of(sem1));

            assertThat(gpa.getSemesterGPAs()).hasSize(1);
            assertThat(gpa.getSemesterGPAs().get(0).getSemester()).isEqualTo("2024-2025-1");
        }
    }

    // ===== TranscriptDTO =====

    @Nested
    @DisplayName("TranscriptDTO - 成绩单数据结构")
    class TranscriptDTOTests {

        @Test
        @DisplayName("学期分组结构正确")
        void shouldStructureSemesterBlocks() {
            TranscriptDTO.CourseGrade cg = new TranscriptDTO.CourseGrade();
            cg.setCourseCode("CS101");
            cg.setCourseName("计算机导论");
            cg.setCredits(new BigDecimal("3"));
            cg.setScore(new BigDecimal("90"));
            cg.setLetterGrade("A");
            cg.setGradePoints(new BigDecimal("4.0"));

            TranscriptDTO.SemesterBlock block = new TranscriptDTO.SemesterBlock();
            block.setSemester("2024-2025-1");
            block.setSemesterGPA(new BigDecimal("4.0"));
            block.setSemesterCredits(new BigDecimal("3"));
            block.setCourses(java.util.List.of(cg));

            assertThat(block.getCourses()).hasSize(1);
            assertThat(block.getCourses().get(0).getLetterGrade()).isEqualTo("A");
        }
    }
}
