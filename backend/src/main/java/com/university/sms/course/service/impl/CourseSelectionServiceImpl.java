package com.university.sms.course.service.impl;

import com.university.sms.common.exception.BusinessException;
import com.university.sms.course.dto.*;
import com.university.sms.course.entity.Course;
import com.university.sms.course.entity.CoursePrerequisite;
import com.university.sms.course.entity.CourseSelection;
import com.university.sms.course.enums.SelectionStatus;
import com.university.sms.course.mapper.CourseMapper;
import com.university.sms.course.mapper.CoursePrerequisiteMapper;
import com.university.sms.course.mapper.CourseSelectionMapper;
import com.university.sms.course.service.CourseSelectionService;
import com.university.sms.course.service.CourseService;
import com.university.sms.grade.entity.Grade;
import com.university.sms.grade.mapper.GradeMapper;
import com.university.sms.student.entity.Student;
import com.university.sms.student.mapper.StudentMapper;
import com.university.sms.system.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 选课服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSelectionServiceImpl implements CourseSelectionService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CourseSelectionMapper selectionMapper;
    private final CourseMapper courseMapper;
    private final CoursePrerequisiteMapper prerequisiteMapper;
    private final StudentMapper studentMapper;
    private final GradeMapper gradeMapper;
    private final CourseService courseService;
    private final SystemConfigService systemConfigService;

    // ==================== 阶段管理 ====================

    @Override
    public String getSelectionPhase() {
        return resolveCurrentPhase();
    }

    @Override
    public SelectionPhaseDTO getPhaseInfo() {
        SelectionPhaseDTO dto = new SelectionPhaseDTO();

        LocalDateTime preStart = parseDateTime("pre_selection_start_time");
        LocalDateTime preEnd = parseDateTime("pre_selection_end_time");
        LocalDateTime formalStart = parseDateTime("formal_selection_start_time");
        LocalDateTime formalEnd = parseDateTime("formal_selection_end_time");
        LocalDateTime adjStart = parseDateTime("adjustment_start_time");
        LocalDateTime adjEnd = parseDateTime("adjustment_end_time");

        dto.setPreSelectionStart(preStart);
        dto.setPreSelectionEnd(preEnd);
        dto.setFormalSelectionStart(formalStart);
        dto.setFormalSelectionEnd(formalEnd);
        dto.setAdjustmentStart(adjStart);
        dto.setAdjustmentEnd(adjEnd);

        String phase = resolveCurrentPhase();
        dto.setCurrentPhase(phase);
        dto.setCurrentPhaseName(SelectionPhaseDTO.phaseDisplayName(phase));
        dto.setLotteryEnabled(isLotteryEnabled());

        switch (phase) {
            case SelectionPhaseDTO.PHASE_PRE_SELECTION -> {
                dto.setCanSelect(true);
                dto.setCanWithdraw(true);
                dto.setSelectionMode("预选阶段：所有学生均可报名选课。若课程报名人数超出容量上限，将在预选结束后进行抽签决定选课名额。");
                dto.setNextPhaseName("正选阶段");
                dto.setNextPhaseStart(formalStart);
            }
            case SelectionPhaseDTO.PHASE_FORMAL_SELECTION -> {
                dto.setCanSelect(true);
                dto.setCanWithdraw(true);
                dto.setSelectionMode("正选阶段：先到先得，课程满员后无法再选。预选未中签的同学可在此阶段重新选课。");
                dto.setNextPhaseName("补退选阶段");
                dto.setNextPhaseStart(adjStart);
            }
            case SelectionPhaseDTO.PHASE_ADJUSTMENT -> {
                dto.setCanSelect(true);
                dto.setCanWithdraw(true);
                dto.setSelectionMode("补退选阶段：可以补选有余量的课程或退掉已选课程，进行最后调整。");
                dto.setNextPhaseName(null);
                dto.setNextPhaseStart(null);
            }
            case SelectionPhaseDTO.PHASE_NOT_STARTED -> {
                dto.setCanSelect(false);
                dto.setCanWithdraw(false);
                dto.setSelectionMode("选课尚未开始，请等待预选阶段开放。");
                dto.setNextPhaseName("预选阶段");
                dto.setNextPhaseStart(preStart);
            }
            default -> {
                dto.setCanSelect(false);
                dto.setCanWithdraw(false);
                dto.setSelectionMode("选课已结束。");
                dto.setNextPhaseName(null);
                dto.setNextPhaseStart(null);
            }
        }

        return dto;
    }

    /**
     * 基于各阶段配置时间自动判定当前处于哪个选课阶段。
     * 优先从时间窗口判定，若未配置时间则回退到手动设置的 selection_phase。
     */
    private String resolveCurrentPhase() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime preStart = parseDateTime("pre_selection_start_time");
        LocalDateTime preEnd = parseDateTime("pre_selection_end_time");
        LocalDateTime formalStart = parseDateTime("formal_selection_start_time");
        LocalDateTime formalEnd = parseDateTime("formal_selection_end_time");
        LocalDateTime adjStart = parseDateTime("adjustment_start_time");
        LocalDateTime adjEnd = parseDateTime("adjustment_end_time");

        boolean hasTimeConfig = (preStart != null && preEnd != null);

        if (hasTimeConfig) {
            if (preStart != null && preEnd != null && !now.isBefore(preStart) && !now.isAfter(preEnd)) {
                return SelectionPhaseDTO.PHASE_PRE_SELECTION;
            }
            if (formalStart != null && formalEnd != null && !now.isBefore(formalStart) && !now.isAfter(formalEnd)) {
                return SelectionPhaseDTO.PHASE_FORMAL_SELECTION;
            }
            if (adjStart != null && adjEnd != null && !now.isBefore(adjStart) && !now.isAfter(adjEnd)) {
                return SelectionPhaseDTO.PHASE_ADJUSTMENT;
            }
            if (preStart != null && now.isBefore(preStart)) {
                return SelectionPhaseDTO.PHASE_NOT_STARTED;
            }
            return SelectionPhaseDTO.PHASE_CLOSED;
        }

        // 回退：从手动配置的 selection_phase 映射
        String manual = systemConfigService.getConfigValue("selection_phase", "CLOSED");
        return mapLegacyPhase(manual);
    }

    /** 兼容旧的阶段码 FIRST/SECOND → 新阶段码 */
    private String mapLegacyPhase(String legacy) {
        if (legacy == null) return SelectionPhaseDTO.PHASE_CLOSED;
        return switch (legacy) {
            case "FIRST" -> SelectionPhaseDTO.PHASE_PRE_SELECTION;
            case "SECOND" -> SelectionPhaseDTO.PHASE_FORMAL_SELECTION;
            case "ADJUSTMENT" -> SelectionPhaseDTO.PHASE_ADJUSTMENT;
            case "NOT_STARTED" -> SelectionPhaseDTO.PHASE_NOT_STARTED;
            case "CLOSED" -> SelectionPhaseDTO.PHASE_CLOSED;
            case "PRE_SELECTION" -> SelectionPhaseDTO.PHASE_PRE_SELECTION;
            case "FORMAL_SELECTION" -> SelectionPhaseDTO.PHASE_FORMAL_SELECTION;
            default -> legacy;
        };
    }

    private boolean isLotteryEnabled() {
        return "true".equalsIgnoreCase(systemConfigService.getConfigValue("lottery_enabled", "false"));
    }

    private LocalDateTime parseDateTime(String configKey) {
        String value = systemConfigService.getConfigValue(configKey);
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value.trim(), DT_FMT);
        } catch (Exception e) {
            log.warn("解析时间配置失败 {}={}", configKey, value);
            return null;
        }
    }

    // ==================== 选课核心逻辑 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseSelectionResultDTO selectCourse(Long studentId, Long courseId) {
        CourseSelectionResultDTO result = new CourseSelectionResultDTO();

        String phase = resolveCurrentPhase();

        // 不在可选课阶段
        if (SelectionPhaseDTO.PHASE_NOT_STARTED.equals(phase) || SelectionPhaseDTO.PHASE_CLOSED.equals(phase)) {
            result.setSuccess(false);
            result.setMessage(SelectionPhaseDTO.PHASE_NOT_STARTED.equals(phase)
                    ? "选课尚未开始，请耐心等待" : "选课已结束");
            return result;
        }
        
        // 检查课程是否存在
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() != Course.STATUS_OPEN) {
            result.setSuccess(false);
            result.setMessage("课程不存在或已停开");
            return result;
        }
        
        // 检查是否已有选课记录
        CourseSelection existing = selectionMapper.selectByStudentAndCourse(studentId, courseId);
        if (existing != null && SelectionStatus.SELECTED.getCode().equals(existing.getStatus())) {
            result.setSuccess(false);
            result.setMessage("您已选择该课程");
            return result;
        }
        if (existing != null && SelectionStatus.LOTTERY_PENDING.getCode().equals(existing.getStatus())) {
            result.setSuccess(false);
            result.setMessage("您已报名该课程，正在等待抽签结果");
            return result;
        }
        
        // 检查时间冲突
        List<CourseDTO> conflicts = checkTimeConflict(studentId, courseId);
        if (!conflicts.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("与已选课程时间冲突");
            result.setConflictCourses(conflicts);
            return result;
        }
        
        // 检查先修课程
        List<String> missingPrerequisites = checkPrerequisites(studentId, courseId);
        if (!missingPrerequisites.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("未完成先修课程");
            result.setMissingPrerequisites(missingPrerequisites);
            return result;
        }

        // 根据阶段执行不同策略
        return switch (phase) {
            case SelectionPhaseDTO.PHASE_PRE_SELECTION ->
                    handlePreSelection(studentId, courseId, course, existing);
            case SelectionPhaseDTO.PHASE_FORMAL_SELECTION, SelectionPhaseDTO.PHASE_ADJUSTMENT ->
                    handleDirectSelection(studentId, courseId, course, existing);
            default -> {
                result.setSuccess(false);
                result.setMessage("当前不在选课时间内");
                yield result;
            }
        };
    }

    /**
     * 预选阶段：启用抽签时超容量进入待抽签，未启用抽签时先到先得
     */
    private CourseSelectionResultDTO handlePreSelection(
            Long studentId, Long courseId, Course course, CourseSelection existing) {

        if (isLotteryEnabled()) {
            return handlePreSelectionWithLottery(studentId, courseId, course, existing);
        }
        return handleDirectSelection(studentId, courseId, course, existing);
    }

    /**
     * 预选 + 抽签模式：未超容量直接选中，超容量进入待抽签
     */
    private CourseSelectionResultDTO handlePreSelectionWithLottery(
            Long studentId, Long courseId, Course course, CourseSelection existing) {

        CourseSelectionResultDTO result = new CourseSelectionResultDTO();

        // 先尝试原子性增加已选人数
        // 数据库中的 incrementEnrolledCount 使用条件更新防止超额
        boolean enrolled = courseService.incrementEnrolledCount(courseId);

        if (enrolled) {
            try {
                saveOrUpdateSelection(studentId, courseId, existing, SelectionStatus.SELECTED);
                result.setSuccess(true);
                result.setMessage("选课成功（预选阶段）");
                result.setStatus(SelectionStatus.SELECTED.getCode());
            } catch (Exception e) {
                // 如果保存选课记录失败，需要回滚已选人数
                courseService.decrementEnrolledCount(courseId);
                // 进入待抽签状态作为降级处理
                saveOrUpdateSelection(studentId, courseId, existing, SelectionStatus.LOTTERY_PENDING);
                result.setSuccess(true);
                result.setMessage("课程报名人数较多，已进入待抽签状态");
                result.setStatus(SelectionStatus.LOTTERY_PENDING.getCode());
                log.error("预选阶段保存选课记录失败，降级为待抽签状态，studentId={}, courseId={}", studentId, courseId, e);
            }
        } else {
            // 已满员，进入待抽签状态
            saveOrUpdateSelection(studentId, courseId, existing, SelectionStatus.LOTTERY_PENDING);
            result.setSuccess(true);
            result.setMessage("课程报名人数超出容量，已进入待抽签状态，请等待预选结束后的抽签结果");
            result.setStatus(SelectionStatus.LOTTERY_PENDING.getCode());
        }
        return result;
    }

    /**
     * 正选/补退选：先到先得
     */
    private CourseSelectionResultDTO handleDirectSelection(
            Long studentId, Long courseId, Course course, CourseSelection existing) {

        CourseSelectionResultDTO result = new CourseSelectionResultDTO();

        // 先尝试原子性增加已选人数，成功后再保存选课记录
        // 这样可以防止并发下的超额选课问题
        boolean enrolled = courseService.incrementEnrolledCount(courseId);
        if (!enrolled) {
            result.setSuccess(false);
            result.setMessage("课程已满");
            return result;
        }

        try {
            saveOrUpdateSelection(studentId, courseId, existing, SelectionStatus.SELECTED);
            result.setSuccess(true);
            result.setMessage("选课成功");
            result.setStatus(SelectionStatus.SELECTED.getCode());
        } catch (Exception e) {
            // 如果保存选课记录失败，需要回滚已选人数
            courseService.decrementEnrolledCount(courseId);
            result.setSuccess(false);
            result.setMessage("选课失败，请重试");
            log.error("保存选课记录失败，studentId={}, courseId={}", studentId, courseId, e);
        }
        return result;
    }

    private void saveOrUpdateSelection(Long studentId, Long courseId,
                                        CourseSelection existing, SelectionStatus status) {
        if (existing != null) {
            existing.setStatus(status.getCode());
            existing.setSelectedAt(LocalDateTime.now());
            existing.setWithdrawnAt(null);
            selectionMapper.updateById(existing);
        } else {
            CourseSelection selection = new CourseSelection();
            selection.setStudentId(studentId);
            selection.setCourseId(courseId);
            selection.setStatus(status.getCode());
            selection.setSelectedAt(LocalDateTime.now());
            selectionMapper.insert(selection);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawCourse(Long studentId, Long courseId) {
        String phase = resolveCurrentPhase();
        if (SelectionPhaseDTO.PHASE_NOT_STARTED.equals(phase) || SelectionPhaseDTO.PHASE_CLOSED.equals(phase)) {
            throw new BusinessException("当前阶段不允许退课");
        }

        CourseSelection selection = selectionMapper.selectByStudentAndCourse(studentId, courseId);
        if (selection == null) {
            throw new BusinessException("未找到选课记录");
        }

        String currentStatus = selection.getStatus();
        if (!SelectionStatus.SELECTED.getCode().equals(currentStatus)
                && !SelectionStatus.LOTTERY_PENDING.getCode().equals(currentStatus)) {
            throw new BusinessException("当前状态不可退课");
        }
        
        selection.setStatus(SelectionStatus.WITHDRAWN.getCode());
        selection.setWithdrawnAt(LocalDateTime.now());
        selectionMapper.updateById(selection);
        
        if (SelectionStatus.SELECTED.getCode().equals(currentStatus)) {
            courseService.decrementEnrolledCount(courseId);
        }
    }
    
    @Override
    public List<CourseDTO> checkTimeConflict(Long studentId, Long courseId) {
        Course newCourse = courseMapper.selectById(courseId);
        if (newCourse == null) {
            return Collections.emptyList();
        }
        
        List<CourseSelection> selections = selectionMapper.selectByStudentId(studentId);
        List<Long> activeCourseIds = selections.stream()
                .filter(s -> SelectionStatus.SELECTED.getCode().equals(s.getStatus())
                        || SelectionStatus.LOTTERY_PENDING.getCode().equals(s.getStatus()))
                .map(CourseSelection::getCourseId)
                .collect(Collectors.toList());
        
        if (activeCourseIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<CourseDTO> conflicts = new ArrayList<>();
        for (Long selectedId : activeCourseIds) {
            Course selected = courseMapper.selectById(selectedId);
            if (selected != null && hasScheduleConflict(newCourse.getSchedule(), selected.getSchedule())) {
                conflicts.add(CourseDTO.fromEntity(selected));
            }
        }
        
        return conflicts;
    }
    
    @Override
    public List<String> checkPrerequisites(Long studentId, Long courseId) {
        // 获取先修课程要求
        List<CoursePrerequisite> prerequisites = prerequisiteMapper.selectByCourseId(courseId);
        if (prerequisites.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> missing = new ArrayList<>();
        for (CoursePrerequisite prereq : prerequisites) {
            // 检查学生是否通过先修课程（仅认可已审批成绩）
            Grade grade = gradeMapper.selectByStudentAndCourse(studentId, prereq.getPrerequisiteCourseId());
            // 成绩必须存在、已审批、有分数且分数达到最低要求
            if (grade == null ||
                !Grade.STATUS_APPROVED.equals(grade.getStatus()) ||
                grade.getScore() == null ||
                grade.getScore().doubleValue() < prereq.getMinScore().doubleValue()) {
                Course prereqCourse = courseMapper.selectById(prereq.getPrerequisiteCourseId());
                if (prereqCourse != null) {
                    missing.add(prereqCourse.getName());
                }
            }
        }

        return missing;
    }

    @Override
    public List<ScheduleDTO> getStudentSchedule(Long studentId) {
        List<CourseSelection> selections = selectionMapper.selectByStudentId(studentId);
        
        return selections.stream()
                .filter(s -> SelectionStatus.SELECTED.getCode().equals(s.getStatus()))
                .map(s -> {
                    Course course = courseMapper.selectById(s.getCourseId());
                    if (course == null) return null;
                    
                    ScheduleDTO dto = new ScheduleDTO();
                    dto.setCourseId(course.getId());
                    dto.setCourseCode(course.getCourseCode());
                    dto.setCourseName(course.getName());
                    dto.setTeacherName(course.getTeacherName());
                    dto.setSchedule(course.getSchedule());
                    dto.setLocation(course.getLocation());
                    dto.setCredits(course.getCredits());
                    parseSchedule(dto, course.getSchedule());
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CourseRosterDTO> getCourseRoster(Long courseId) {
        List<CourseSelection> selections = selectionMapper.selectByCourseId(courseId);
        
        return selections.stream()
                .filter(s -> SelectionStatus.SELECTED.getCode().equals(s.getStatus()))
                .map(s -> {
                    Student student = studentMapper.selectById(s.getStudentId());
                    if (student == null) return null;
                    
                    CourseRosterDTO dto = new CourseRosterDTO();
                    dto.setStudentId(student.getId());
                    dto.setStudentNo(student.getStudentNo());
                    dto.setStudentName(student.getName());
                    dto.setDepartment(student.getDepartment());
                    dto.setMajor(student.getMajor());
                    dto.setClassNo(student.getClassNo());
                    dto.setSelectedAt(s.getSelectedAt());
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CourseSelectionDTO> getStudentSelections(Long studentId) {
        List<CourseSelection> selections = selectionMapper.selectByStudentId(studentId);
        
        return selections.stream()
                .map(s -> {
                    Course course = courseMapper.selectById(s.getCourseId());
                    if (course == null) return null;
                    
                    CourseSelectionDTO dto = new CourseSelectionDTO();
                    dto.setId(s.getId());
                    dto.setStudentId(s.getStudentId());
                    dto.setCourseId(s.getCourseId());
                    dto.setCourseCode(course.getCourseCode());
                    dto.setCourseName(course.getName());
                    dto.setCredits(course.getCredits());
                    dto.setTeacherName(course.getTeacherName());
                    dto.setSchedule(course.getSchedule());
                    dto.setLocation(course.getLocation());
                    dto.setStatus(s.getStatus());
                    dto.setWithdrawnAt(s.getWithdrawnAt());
                    dto.setSelectedAt(s.getSelectedAt());

                    SelectionStatus statusEnum = SelectionStatus.fromCode(s.getStatus());
                    dto.setStatusDesc(statusEnum != null ? statusEnum.getDescription() : s.getStatus());
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void executeLottery(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 重新查询课程获取最新已选人数（防止并发问题）
        int currentEnrolled = course.getEnrolledCount();
        int capacity = course.getCapacity();
        int available = capacity - currentEnrolled;

        if (available <= 0) {
            log.info("课程 {} 已满员，所有待抽签记录将标记为未中签", course.getName());
            // 所有待抽签记录标记为未中签
            List<CourseSelection> pending = selectionMapper.selectByCourseIdAndStatus(
                    courseId, SelectionStatus.LOTTERY_PENDING.getCode());
            for (CourseSelection selection : pending) {
                selection.setStatus(SelectionStatus.LOTTERY_FAILED.getCode());
                selectionMapper.updateById(selection);
            }
            return;
        }

        List<CourseSelection> pending = selectionMapper.selectByCourseIdAndStatus(
                courseId, SelectionStatus.LOTTERY_PENDING.getCode());

        if (pending.isEmpty()) {
            log.info("课程 {} 没有待抽签记录", course.getName());
            return;
        }

        Collections.shuffle(pending);

        int selected = 0;
        int enrolled = currentEnrolled;
        for (int i = 0; i < pending.size(); i++) {
            CourseSelection selection = pending.get(i);
            if (i < available && enrolled < capacity) {
                selection.setStatus(SelectionStatus.SELECTED.getCode());
                // 使用数据库原子操作增加已选人数
                boolean incremented = courseService.incrementEnrolledCount(courseId);
                if (incremented) {
                    selected++;
                    enrolled++;
                } else {
                    // 增加失败，说明已满，标记为未中签
                    selection.setStatus(SelectionStatus.LOTTERY_FAILED.getCode());
                }
            } else {
                selection.setStatus(SelectionStatus.LOTTERY_FAILED.getCode());
            }
            selectionMapper.updateById(selection);
        }

        log.info("课程 {} ({}) 抽签完成：{} 人参与，{} 人中签，{} 人未中签",
                course.getName(), course.getCourseCode(),
                pending.size(), selected, pending.size() - selected);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeLotteryAll() {
        List<Long> courseIds = selectionMapper.selectCoursesWithLotteryPending();
        if (courseIds.isEmpty()) {
            log.info("没有需要抽签的课程");
            return 0;
        }

        for (Long courseId : courseIds) {
            executeLottery(courseId);
        }

        log.info("批量抽签完成，共处理 {} 门课程", courseIds.size());
        return courseIds.size();
    }

    @Override
    public List<LotteryCourseDTO> getLotteryPendingCourses() {
        List<Long> courseIds = selectionMapper.selectCoursesWithLotteryPending();
        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        return courseIds.stream().map(courseId -> {
            Course course = courseMapper.selectById(courseId);
            if (course == null) return null;

            LotteryCourseDTO dto = new LotteryCourseDTO();
            dto.setCourseId(course.getId());
            dto.setCourseCode(course.getCourseCode());
            dto.setCourseName(course.getName());
            dto.setCredits(course.getCredits());
            dto.setTeacherName(course.getTeacherName());
            dto.setSchedule(course.getSchedule());
            dto.setLocation(course.getLocation());
            dto.setSemester(course.getSemester());
            dto.setCategory(course.getCategory());
            dto.setCapacity(course.getCapacity());
            dto.setEnrolledCount(course.getEnrolledCount());
            dto.setPendingCount(selectionMapper.countLotteryPendingByCourseId(courseId));
            dto.setLotteryExecuted(false);
            return dto;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    // ========== 私有方法 ==========
    
    /**
     * 检查时间是否冲突
     * 简化实现：比较 schedule 字符串
     */
    private boolean hasScheduleConflict(String schedule1, String schedule2) {
        if (schedule1 == null || schedule2 == null) {
            return false;
        }
        // 简化实现：如果包含相同的时间段则认为冲突
        // 实际应该解析时间进行精确比较
        return schedule1.equals(schedule2);
    }
    
    /**
     * 解析课程时间
     */
    private void parseSchedule(ScheduleDTO dto, String schedule) {
        if (schedule == null) return;
        
        // 简化解析，格式如: "周一1-2节"
        if (schedule.startsWith("周一")) dto.setDayOfWeek(1);
        else if (schedule.startsWith("周二")) dto.setDayOfWeek(2);
        else if (schedule.startsWith("周三")) dto.setDayOfWeek(3);
        else if (schedule.startsWith("周四")) dto.setDayOfWeek(4);
        else if (schedule.startsWith("周五")) dto.setDayOfWeek(5);
        else if (schedule.startsWith("周六")) dto.setDayOfWeek(6);
        else if (schedule.startsWith("周日")) dto.setDayOfWeek(7);
        
        // 解析节次
        try {
            String sections = schedule.replaceAll("[^0-9-]", "");
            String[] parts = sections.split("-");
            if (parts.length >= 1) {
                dto.setStartSection(Integer.parseInt(parts[0]));
            }
            if (parts.length >= 2) {
                dto.setEndSection(Integer.parseInt(parts[1]));
            }
        } catch (Exception e) {
            // 解析失败忽略
        }
    }
}
