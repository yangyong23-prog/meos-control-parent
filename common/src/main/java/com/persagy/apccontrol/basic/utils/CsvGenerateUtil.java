package com.persagy.apccontrol.basic.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * CSV生成工具类
 *
 * @author: system
 * @create: 2026-03-17
 */
@Slf4j
public class CsvGenerateUtil {

    /**
     * UTF-8 BOM，用于防止中文乱码
     */
    private static final String UTF8_BOM = "\uFEFF";

    /**
     * CSV分隔符
     */
    private static final String CSV_SEPARATOR = ",";

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");

    /**
     * 时间点格式化器
     */
    private static final DateTimeFormatter TIME_POINT_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm-HH:mm");

    /**
     * 小数格式化器
     */
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

    /**
     * 格式化BigDecimal为字符串，保留两位小数
     *
     * @param value BigDecimal值
     * @return 格式化后的字符串
     */
    public static String formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return DECIMAL_FORMAT.format(value);
    }

    /**
     * 格式化Local日期
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * 格式化LocalDateTime为时间点
     *
     * @param dateTime 日期时间
     * @return 格式化后的时间点字符串
     */
    public static String formatTimePoint(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(TIME_POINT_FORMATTER);
    }

    /**
     * 转义CSV特殊字符
     *
     * @param value 原始值
     * @return 转义后的值
     */
    public static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // 如果包含双引号、换行符或逗号，则需要用双引号包裹，并将内部的双引号转义
        if (value.contains("\"") || value.contains("\n") || value.contains(",") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * 转义整数
     *
     * @param value 整数值
     * @return 转义后的字符串
     */
    public static String escapeCsv(Integer value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }

    /**
     * 生成CSV行
     *
     * @param values 字段值列表
     * @return CSV行字符串
     */
    public static String generateCsvRow(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            sb.append(escapeCsv(values.get(i)));
            if (i < values.size() - 1) {
                sb.append(CSV_SEPARATOR);
            }
        }
        sb.append("\r\n");
        return sb.toString();
    }

    /**
     * 生成对象为CSV行（按字段顺序）
     *
     * @param fieldValues 字段值映射（键为字段名，值为字段值）
     * @param fieldNames 字段名顺序列表
     * @return CSV行字符串
     */
    public static String generateCsvRow(Map<String, Object> fieldValues, List<String> fieldNames) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            Object value = fieldValues.get(fieldNames.get(i));
            sb.append(escapeCsv(convertToString(value)));
            if (i < fieldNames.size() - 1) {
                sb.append(CSV_SEPARATOR);
            }
        }
        sb.append("\r\n");
        return sb.toString();
    }

    /**
     * 将对象转换为字符串
     *
     * @param value 对象值
     * @return 字符串
     */
    private static String convertToString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal) {
            return formatBigDecimal((BigDecimal) value);
        } else if (value instanceof LocalDate) {
            return formatDate((LocalDate) value);
        } else if (value instanceof LocalDateTime) {
            return formatTimePoint((LocalDateTime) value);
        } else if (value instanceof Integer) {
            return String.valueOf(value);
        } else {
            return String.valueOf(value);
        }
    }

    /**
     * 生成CSV内容（带BOM）
     *
     * @param header 表头
     * @param rows 数据行
     * @return CSV内容
     */
    public static String generateCsvContent(String header, List<String> rows) {
        StringBuilder sb = new StringBuilder(UTF8_BOM);
        sb.append(header);
        for (String row : rows) {
            sb.append(row);
        }
        return sb.toString();
    }

    /**
     * 生成CSV章节分隔符
     *
     * @param chapterName 章节名称
     * @return 空行+章节标题+空行
     */
    public static String generateChapterSeparator(String chapterName) {
        return "\r\n\r\n" + escapeCsv(chapterName) + "\r\n";
    }
}
