package com.university.sms.course.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 选课阶段详情 DTO
 */
@Data
public class SelectionPhaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前阶段代码: NOT_STARTED / PRE_SELECTION / FORMAL_SELECTION / ADJUSTMENT / CLOSED */
    private String currentPhase;

    /** 当前阶段中文名称 */
    private String currentPhaseName;

    /** 是否允许选课 */
    private Boolean canSelect;

    /** 是否允许退课 */
    private Boolean canWithdraw;

    /** 当前阶段选课模式说明 */
    private String selectionMode;

    /** 是否启用抽签（仅预选阶段相关） */
    private Boolean lotteryEnabled;

    // ===== 预选阶段 =====
    private LocalDateTime preSelectionStart;
    private LocalDateTime preSelectionEnd;

    // ===== 正选阶段 =====
    private LocalDateTime formalSelectionStart;
    private LocalDateTime formalSelectionEnd;

    // ===== 补退选阶段 =====
    private LocalDateTime adjustmentStart;
    private LocalDateTime adjustmentEnd;

    /** 下一个阶段名称（供前端倒计时提示） */
    private String nextPhaseName;

    /** 下一个阶段开始时间 */
    private LocalDateTime nextPhaseStart;

    // ===== 阶段代码常量 =====
    public static final String PHASE_NOT_STARTED = "NOT_STARTED";
    public static final String PHASE_PRE_SELECTION = "PRE_SELECTION";
    public static final String PHASE_FORMAL_SELECTION = "FORMAL_SELECTION";
    public static final String PHASE_ADJUSTMENT = "ADJUSTMENT";
    public static final String PHASE_CLOSED = "CLOSED";

    public static String phaseDisplayName(String phase) {
        if (phase == null) return "未知";
        return switch (phase) {
            case PHASE_NOT_STARTED -> "未开始";
            case PHASE_PRE_SELECTION -> "预选阶段";
            case PHASE_FORMAL_SELECTION -> "正选阶段";
            case PHASE_ADJUSTMENT -> "补退选阶段";
            case PHASE_CLOSED -> "选课已结束";
            default -> phase;
        };
    }
}
