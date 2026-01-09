package io.github.weijunfu.id.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 * @author weijunfu
 */
public class FuDate {

    public static final String DATE_PATTERN = "yyyy-MM-dd";                        // 2026-01-09
    public static final String DATE_PATTERN_SLASH = "yyyy/MM/dd";                    // 2026/01/09
    public static final String DATE_PATTERN_CHINESE = "yyyy年MM月dd日";               // 2026年01月09日

    public static final String TIME_PATTERN = "HH:mm:ss";                           // 14:30:45

    public static final String DATE_PATTERN_READABLE = "EEE, MMM d, yyyy";          // Mon, Dec 09, 2026

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";           // 2026-01-09 14:30:45
    public static final String DATE_TIME_PATTERN_WITH_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS"; // 2026-01-09 14:30:45.013

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    /**
     * 格式化日期
     * @return LocalDate  当前日期
     * @author weijunfu
     */
    public static String format(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    /**
     * 解析日期
     * @param date  格式化后的日期
     * @return LocalDate 日期
     * @author weijunfu
     */
    public LocalDate parseLocalDate(String date) {
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    /**
     * 格式化日期
     * @param date  日期
     * @param pattern 格式
     * @return  String  格式化后的日期
     * @author weijunfu
     */
    public static String format(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析日期
     * @param date  格式化后的日期
     * @param pattern 格式
     * @return LocalDate 日期
     * @author weijunfu
     */
    public static LocalDate parseLocalDate(String date, String pattern) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化时间
     * @param dateTime  时间
     * @return String  格式化后的时间
     * @author weijunfu
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * 解析时间
     * @param dateTime  格式化后的时间
     * @return LocalDateTime 时间
     * @author weijunfu
     */
    public static LocalDateTime parse(String dateTime) {
        return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
    }

    /**
     * 格式化时间
     * @param dateTime  时间
     * @param pattern  格式
     * @return String  格式化后的时间
     * @author weijunfu
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析时间
     * @param dateTime  格式化后的时间
     * @param pattern  格式
     * @return LocalDateTime 时间
     * @author weijunfu
     */
    public static LocalDateTime parseLocalDateTime(String dateTime, String pattern) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化时间
     * @param time  时间
     * @return String  格式化后的时间
     * @author weijunfu
     */
    public static String format(LocalTime time) {
        return time.format(TIME_FORMATTER);
    }

    /**
     * 解析时间
     * @param time  格式化后的时间
     * @return LocalTime 时间
     */
    public static LocalTime parseLocalTime(String time) {
        return LocalTime.parse(time, TIME_FORMATTER);
    }

    /**
     * 格式化时间
     * @param time  时间
     * @param pattern  格式
     * @return String  格式化后的时间
     * @author weijunfu
     */
    public static String format(LocalTime time, String pattern) {
        return time.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析时间
     * @param time  格式化后的时间
     * @param pattern  格式
     * @return LocalTime 时间
     * @author weijunfu
     */
    public static LocalTime parseLocalTime(String time, String pattern) {
        return LocalTime.parse(time, DateTimeFormatter.ofPattern(pattern));
    }
}
