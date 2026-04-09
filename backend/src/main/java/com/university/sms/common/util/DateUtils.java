package com.university.sms.common.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期工具类
 */
public class DateUtils {

    private DateUtils() {
    }

    /**
     * 默认日期格式
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 默认日期时间格式
     */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 中文日期格式
     */
    public static final String DATE_PATTERN_CN = "yyyy年MM月dd日";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    private static final DateTimeFormatter DATE_FORMATTER_CN = DateTimeFormatter.ofPattern(DATE_PATTERN_CN);

    /**
     * 格式化日期
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * 格式化日期时间
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化日期（中文格式）
     */
    public static String formatDateCn(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER_CN);
    }

    /**
     * 解析日期字符串
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 计算两个日期之间的天数（包含起止日期）
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * 计算两个日期之间的工作日天数（排除周末）
     */
    public static long workDaysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        long workDays = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                workDays++;
            }
            current = current.plusDays(1);
        }
        return workDays;
    }

    /**
     * 判断日期是否在指定范围内
     */
    public static boolean isBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * 获取当前学期（根据日期判断）
     * 2-7月为春季学期，8-1月为秋季学期
     */
    public static String getCurrentSemester() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();
        
        if (month >= 2 && month <= 7) {
            return year + "-春季";
        } else if (month >= 8) {
            return year + "-秋季";
        } else {
            return (year - 1) + "-秋季";
        }
    }

    /**
     * 获取学年（如：2024-2025）
     */
    public static String getAcademicYear() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();
        
        if (month >= 8) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }

    /**
     * 判断是否为周末
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * 获取本周一的日期
     */
    public static LocalDate getMonday(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return date.with(DayOfWeek.MONDAY);
    }

    /**
     * 获取本周日的日期
     */
    public static LocalDate getSunday(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return date.with(DayOfWeek.SUNDAY);
    }
}
