package com.university.sms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.sms.common.exception.BusinessException;
import com.university.sms.system.dto.*;
import com.university.sms.system.entity.Notification;
import com.university.sms.system.entity.NotificationRead;
import com.university.sms.system.entity.NotificationTemplate;
import com.university.sms.system.mapper.NotificationMapper;
import com.university.sms.system.mapper.NotificationReadMapper;
import com.university.sms.system.mapper.NotificationTemplateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl 单元测试")
class NotificationServiceImplTest {

    @Mock private NotificationTemplateMapper templateMapper;
    @Mock private NotificationMapper notificationMapper;
    @Mock private NotificationReadMapper readMapper;
    @Spy  private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationTemplate systemTemplate;

    @BeforeEach
    void setUp() {
        systemTemplate = new NotificationTemplate();
        systemTemplate.setId(1L);
        systemTemplate.setTemplateCode("COURSE_SELECTED");
        systemTemplate.setTemplateName("选课成功通知");
        systemTemplate.setCategory("COURSE");
        systemTemplate.setSubject("选课成功 - ${courseName}");
        systemTemplate.setContent("同学你好，你已成功选修课程【${courseName}】（${courseCode}）");
        systemTemplate.setVariables("[\"courseName\",\"courseCode\"]");
        systemTemplate.setChannel("SITE");
        systemTemplate.setStatus(1);
        systemTemplate.setIsSystem(1);
    }

    // ===== 模板管理测试 =====

    @Nested
    @DisplayName("模板 CRUD")
    class TemplateCRUDTests {

        @Test
        @DisplayName("创建模板 - 编码已存在时抛出异常")
        void shouldThrow_whenTemplateCodeExists() {
            when(templateMapper.selectByCode("COURSE_SELECTED")).thenReturn(systemTemplate);

            NotificationTemplateSaveDTO dto = new NotificationTemplateSaveDTO();
            dto.setTemplateCode("COURSE_SELECTED");
            dto.setTemplateName("test");
            dto.setCategory("COURSE");
            dto.setSubject("test");
            dto.setContent("test");

            assertThatThrownBy(() -> notificationService.createTemplate(dto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("模板编码已存在");
        }

        @Test
        @DisplayName("创建模板成功")
        void shouldCreateTemplate() {
            when(templateMapper.selectByCode("NEW_TPL")).thenReturn(null);

            NotificationTemplateSaveDTO dto = new NotificationTemplateSaveDTO();
            dto.setTemplateCode("NEW_TPL");
            dto.setTemplateName("新模板");
            dto.setCategory("SYSTEM");
            dto.setSubject("标题 ${name}");
            dto.setContent("内容 ${name}");
            dto.setVariables(List.of("name"));
            dto.setChannel("SITE");

            notificationService.createTemplate(dto, 1L);

            verify(templateMapper).insert(argThat(t ->
                t.getTemplateCode().equals("NEW_TPL") &&
                t.getIsSystem() == 0 &&
                t.getStatus() == 1
            ));
        }

        @Test
        @DisplayName("删除系统内置模板应抛出异常")
        void shouldThrow_whenDeletingSystemTemplate() {
            when(templateMapper.selectById(1L)).thenReturn(systemTemplate);

            assertThatThrownBy(() -> notificationService.deleteTemplate(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("系统内置模板不允许删除");
        }

        @Test
        @DisplayName("删除不存在的模板应抛出异常")
        void shouldThrow_whenTemplateNotFound() {
            when(templateMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> notificationService.deleteTemplate(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("模板不存在");
        }

        @Test
        @DisplayName("切换模板状态")
        void shouldToggleStatus() {
            NotificationTemplate tpl = new NotificationTemplate();
            tpl.setId(2L);
            tpl.setStatus(1);
            when(templateMapper.selectById(2L)).thenReturn(tpl);

            notificationService.toggleTemplateStatus(2L);

            verify(templateMapper).updateById(argThat(t -> t.getStatus() == 0));
        }

        @Test
        @DisplayName("获取模板详情 - DTO 转换正确")
        void shouldConvertToDTO() {
            when(templateMapper.selectById(1L)).thenReturn(systemTemplate);

            NotificationTemplateDTO result = notificationService.getTemplateById(1L);

            assertThat(result.getTemplateCode()).isEqualTo("COURSE_SELECTED");
            assertThat(result.getCategoryName()).isEqualTo("选课通知");
            assertThat(result.getChannelName()).isEqualTo("站内信");
            assertThat(result.getVariables()).containsExactly("courseName", "courseCode");
        }
    }

    // ===== 通知发送测试 =====

    @Nested
    @DisplayName("sendNotification - 通知发送")
    class SendNotificationTests {

        @Test
        @DisplayName("使用模板发送 - 变量正确替换")
        void shouldReplaceVariables() {
            when(templateMapper.selectById(1L)).thenReturn(systemTemplate);

            NotificationSendDTO dto = new NotificationSendDTO();
            dto.setTemplateId(1L);
            dto.setTargetType("ALL");
            dto.setVariables(Map.of(
                "courseName", "高等数学",
                "courseCode", "MATH101"
            ));

            notificationService.sendNotification(dto, 1L, "admin");

            verify(notificationMapper).insert(argThat(n ->
                n.getTitle().equals("选课成功 - 高等数学") &&
                n.getContent().contains("高等数学") &&
                n.getContent().contains("MATH101") &&
                n.getCategory().equals("COURSE") &&
                n.getTargetType().equals("ALL") &&
                n.getStatus().equals(Notification.STATUS_SENT)
            ));
        }

        @Test
        @DisplayName("模板不存在时抛出异常")
        void shouldThrow_whenTemplateNotFound() {
            when(templateMapper.selectById(999L)).thenReturn(null);

            NotificationSendDTO dto = new NotificationSendDTO();
            dto.setTemplateId(999L);
            dto.setTargetType("ALL");

            assertThatThrownBy(() -> notificationService.sendNotification(dto, 1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("通知模板不存在");
        }

        @Test
        @DisplayName("模板已禁用时抛出异常")
        void shouldThrow_whenTemplateDisabled() {
            systemTemplate.setStatus(0);
            when(templateMapper.selectById(1L)).thenReturn(systemTemplate);

            NotificationSendDTO dto = new NotificationSendDTO();
            dto.setTemplateId(1L);
            dto.setTargetType("ALL");

            assertThatThrownBy(() -> notificationService.sendNotification(dto, 1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已禁用");
        }

        @Test
        @DisplayName("自定义发送 - 标题为空时抛出异常")
        void shouldThrow_whenCustomTitleEmpty() {
            NotificationSendDTO dto = new NotificationSendDTO();
            dto.setTargetType("ALL");
            dto.setTitle("  ");
            dto.setContent("some content");

            assertThatThrownBy(() -> notificationService.sendNotification(dto, 1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("标题不能为空");
        }

        @Test
        @DisplayName("自定义发送 - 成功")
        void shouldSendCustomNotification() {
            NotificationSendDTO dto = new NotificationSendDTO();
            dto.setTargetType("ROLE");
            dto.setTargetValue("STUDENT");
            dto.setTitle("停课通知");
            dto.setContent("因天气原因明日停课");
            dto.setPriority("URGENT");

            notificationService.sendNotification(dto, 1L, "admin");

            verify(notificationMapper).insert(argThat(n ->
                n.getTitle().equals("停课通知") &&
                n.getPriority().equals("URGENT") &&
                n.getTargetType().equals("ROLE") &&
                n.getTargetValue().equals("STUDENT") &&
                n.getTemplateId() == null
            ));
        }

        @Test
        @DisplayName("未匹配的变量保持原样")
        void shouldKeepUnmatchedVariables() {
            when(templateMapper.selectById(1L)).thenReturn(systemTemplate);

            NotificationSendDTO dto = new NotificationSendDTO();
            dto.setTemplateId(1L);
            dto.setTargetType("ALL");
            dto.setVariables(Map.of("courseName", "高等数学"));

            notificationService.sendNotification(dto, 1L, "admin");

            verify(notificationMapper).insert(argThat(n ->
                n.getContent().contains("${courseCode}")
            ));
        }
    }

    // ===== 已读管理测试 =====

    @Nested
    @DisplayName("markAsRead / getUnreadCount - 已读管理")
    class ReadManagementTests {

        @Test
        @DisplayName("首次阅读应创建新记录")
        void shouldCreateReadRecord_whenFirstRead() {
            when(readMapper.selectByNotificationAndUser(1L, 1L)).thenReturn(null);

            notificationService.markAsRead(1L, 1L);

            verify(readMapper).insert(argThat(r ->
                r.getNotificationId().equals(1L) &&
                r.getUserId().equals(1L) &&
                r.getIsRead() == 1 &&
                r.getReadTime() != null
            ));
        }

        @Test
        @DisplayName("重复阅读不应重复创建")
        void shouldNotDuplicate_whenAlreadyRead() {
            NotificationRead existing = new NotificationRead();
            existing.setId(10L);
            existing.setIsRead(1);
            when(readMapper.selectByNotificationAndUser(1L, 1L)).thenReturn(existing);

            notificationService.markAsRead(1L, 1L);

            verify(readMapper, never()).insert(any());
            verify(readMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("将未读标记为已读")
        void shouldUpdateToRead() {
            NotificationRead existing = new NotificationRead();
            existing.setId(10L);
            existing.setIsRead(0);
            when(readMapper.selectByNotificationAndUser(1L, 1L)).thenReturn(existing);

            notificationService.markAsRead(1L, 1L);

            verify(readMapper).updateById(argThat(r -> r.getIsRead() == 1));
        }

        @Test
        @DisplayName("获取未读数量")
        void shouldReturnUnreadCount() {
            when(readMapper.countUnread(1L)).thenReturn(5);

            int count = notificationService.getUnreadCount(1L);

            assertThat(count).isEqualTo(5);
        }
    }

    // ===== DTO 转换和辅助方法测试 =====

    @Nested
    @DisplayName("DTO 转换与显示名称")
    class DTOConversionTests {

        @Test
        @DisplayName("分类显示名称映射正确")
        void shouldMapCategoryNames() {
            assertThat(NotificationTemplateDTO.getCategoryName("ACADEMIC")).isEqualTo("教务通知");
            assertThat(NotificationTemplateDTO.getCategoryName("COURSE")).isEqualTo("选课通知");
            assertThat(NotificationTemplateDTO.getCategoryName("GRADE")).isEqualTo("成绩通知");
            assertThat(NotificationTemplateDTO.getCategoryName("LEAVE")).isEqualTo("请假通知");
            assertThat(NotificationTemplateDTO.getCategoryName("SYSTEM")).isEqualTo("系统通知");
            assertThat(NotificationTemplateDTO.getCategoryName(null)).isEmpty();
        }

        @Test
        @DisplayName("优先级显示名称映射正确")
        void shouldMapPriorityNames() {
            assertThat(NotificationDTO.getPriorityName("LOW")).isEqualTo("低");
            assertThat(NotificationDTO.getPriorityName("NORMAL")).isEqualTo("普通");
            assertThat(NotificationDTO.getPriorityName("HIGH")).isEqualTo("高");
            assertThat(NotificationDTO.getPriorityName("URGENT")).isEqualTo("紧急");
            assertThat(NotificationDTO.getPriorityName(null)).isEmpty();
        }

        @Test
        @DisplayName("目标类型显示名称映射正确")
        void shouldMapTargetTypeNames() {
            assertThat(NotificationDTO.getTargetTypeName("ALL")).isEqualTo("全体用户");
            assertThat(NotificationDTO.getTargetTypeName("ROLE")).isEqualTo("按角色");
            assertThat(NotificationDTO.getTargetTypeName("USER")).isEqualTo("指定用户");
        }

        @Test
        @DisplayName("渠道显示名称映射正确")
        void shouldMapChannelNames() {
            assertThat(NotificationTemplateDTO.getChannelName("SITE")).isEqualTo("站内信");
            assertThat(NotificationTemplateDTO.getChannelName("EMAIL")).isEqualTo("邮件");
            assertThat(NotificationTemplateDTO.getChannelName("ALL")).isEqualTo("全渠道");
        }
    }
}
