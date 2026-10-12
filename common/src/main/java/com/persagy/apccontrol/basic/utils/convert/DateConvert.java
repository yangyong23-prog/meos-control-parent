package com.persagy.apccontrol.basic.utils.convert;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * 时间转换器
 *
 * @author JiYinchuan
 * @since v4.5.0
 */
public class DateConvert {

    /**
     * 将 {@link Date} 转为 {@link LocalDateTime}
     *
     * @param date Date
     * @return LocalDateTime实例, 当 {@code date} 参数为 {@code null} 时返回当前时间
     * @see #toLocalDateTime(Date, LocalDateTime)
     * @since v4.5.0
     */
    public static LocalDateTime toLocalDateTime(@Nullable Date date) {
        if (Objects.isNull(date)) {
            return LocalDateTime.now();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 将 {@link Date} 转为 {@link LocalDateTime}
     *
     * @param date         Date
     * @param defaultValue 当 {@code date} 参数为空时的默认值
     * @return LocalDateTime实例, 当 {@code date} 参数为 {@code null} 时返回 {@code defaultValue} 参数
     * @since v4.5.0
     */
    public static LocalDateTime toLocalDateTime(@Nullable Date date, @NonNull LocalDateTime defaultValue) {
        if (Objects.isNull(date)) {
            return defaultValue;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 将 {@link LocalDateTime} 转为 {@link Date}
     *
     * @param localDateTime LocalDateTime
     * @return Date实例, 当 {@code localDateTime} 参数为 {@code null} 时返回当前时间
     * @since v4.5.0
     */
    public static Date toDate(@Nullable LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return new Date();
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将 {@link LocalDateTime} 转为 {@link Date}
     *
     * @param localDateTime LocalDateTime
     * @param defaultValue  当 {@code LocalDateTime} 为空时的默认值
     * @return Date实例, 当 {@code localDateTime} 参数为 {@code null} 时返回 {@code defaultValue} 参数
     * @since v4.5.0
     */
    public static Date toDate(@Nullable LocalDateTime localDateTime, @NonNull Date defaultValue) {
        if (Objects.isNull(localDateTime)) {
            return defaultValue;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
