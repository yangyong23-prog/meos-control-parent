package com.persagy.apccontrol.basic.utils.dateUtil;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.parser.ParserConfig;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.fastjson.deserializer.zone.Jdk8DateWithZoneDeserializer;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.cloud.timezone.TimeZoneManager;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.validate.utility.enumeration.DateTimeTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.lang.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 时间相关的工具类
 *
 * @author duanruixiang
 */
public class DateUtils extends org.apache.commons.lang.time.DateUtils {
    public static final String SDFSECOND = "yyyyMMddHHmmss";
    public static final String SDF_SECOND = "yyyy-MM-dd HH:mm:ss";
    public static final String SDF_MINUTE = "yyyy-MM-dd HH:mm";
    public static final String SDF_HOUR = "yyyy-MM-dd HH";
    public static final String SDFDAY = "yyyyMMdd";
    public static final String SDF_DAY = "yyyy-MM-dd";
    public static final String SDF_MONTH = "yyyy-MM-01";
    public static final String S_MONTH = "yyyy-MM";
    public static final String SDF_YEAR = "yyyy-01-01";

    /**
     * 定义日期格式 yyyyMMdd
     */
    public static final DateTimeFormatter DTF_SDFDAY = DateTimeFormatter.ofPattern(SDFDAY);
    /**
     * 定义日期格式 yyyy-MM-dd HH:mm:ss
     */
    public static final DateTimeFormatter DTF_SDF_SECOND = DateTimeFormatter.ofPattern(SDF_SECOND);
    /**
     * 定义日期格式 yyyyMMddHHmmss
     */
    public static final DateTimeFormatter DTF_SDF_SECOND_UNSIGNED = DateTimeFormatter.ofPattern(SDFSECOND);

    /**
     * 多格式的解析器
     */
    private static final DateTimeFormatter MULTI_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("[" + SDFSECOND + "]")
            .appendPattern("[" + SDF_SECOND + "]")
            .toFormatter();

    public static ZoneId getZoneId() {
        try {
            return TimeZoneManager.getProjectZoneIdDefaultConfigTimeZone(ContextUtil.getProjectId());
        }catch (Exception e){
            return TimeZoneManager.getServerZoneId();
        }
    }

    public static ParserConfig getParserConfigWithZone() {
        ParserConfig parserConfig = new ParserConfig();
        parserConfig.putDeserializer(LocalTime.class, new Jdk8DateWithZoneDeserializer());
        parserConfig.putDeserializer(LocalDate.class, new Jdk8DateWithZoneDeserializer());
        parserConfig.putDeserializer(LocalDateTime.class, new Jdk8DateWithZoneDeserializer());
        return parserConfig;
    }

    public static LocalDateTime now() {
        ZoneId zoneId = ZoneId.systemDefault();
        ZoneId projectZoneId = getZoneId();
        return TimeZoneManager.convertToTimeZone(LocalDateTime.now(), zoneId, projectZoneId);
    }

    public static LocalDateTime toLocalDateTime(Long timestamp) {
        ZonedDateTime zoneDateTime = toZoneDateTime(timestamp);
        return Objects.nonNull(zoneDateTime) ? zoneDateTime.toLocalDateTime() : null;
    }

    public static ZonedDateTime toZoneDateTime(Long timestamp) {
        if (Objects.isNull(timestamp)) {
            return null;
        }
        return Instant.ofEpochMilli(timestamp).atZone(getZoneId());
    }

    /**
     * 获取指定日期的开始或结束时间
     *
     * @param offsetDays 偏移天数（0=今天，-1=昨天，1=明天等）
     * @param isStart    true=获取开始时间(00:00:00)，false=获取结束时间(23:59:59.999999999)
     * @return 对应的日期时间
     */
    public static LocalDateTime getDayBoundary(int offsetDays, boolean isStart) {
        LocalDate date = now().toLocalDate().plusDays(offsetDays);
        return isStart ? LocalDateTime.of(date, LocalTime.MIN)
                : LocalDateTime.of(date, LocalTime.MAX);
    }

    /**
     * 获取指定日期的上边界或者下边界
     *
     * @param offsetDays 偏移天数（0=今天，-1=昨天，1=明天等）
     * @param isStart    true=获取开始时间(00:00:00)，false=获取结束时间(23:59:59.999999999)
     * @return 对应的日期时间
     */
    public static LocalDateTime getDayBoundaryByDate(LocalDate localDate,int offsetDays, boolean isStart) {
        LocalDate date = localDate.plusDays(offsetDays);
        return isStart ? LocalDateTime.of(date, LocalTime.MIN)
                : LocalDateTime.of(date, LocalTime.MAX);
    }

    /**
     * 获取指定日期当月1号
     *
     * @param date 指定日期
     * @return 对应的日期时间
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }
    public static LocalDate getFirstDayOfLastMonth(LocalDate date) {
        return date.withDayOfMonth(1).plusMonths(1);
    }

    public static String getNowTimeStr() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SDF_SECOND);
        return now().format(formatter);
        // SimpleDateFormat sdf = new SimpleDateFormat(SDF_SECOND);
        // return sdf.format(new Date());
    }


    /**
     * 将pattern格式的字符串日期转换为Date类型日期
     *
     * @param source  pattern格式的字符串日期
     * @param pattern 日期格式如yyyy-MM-dd HH:mm:ss
     * @return
     * @throws Exception
     */
    public static Date str2Date(String source, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date data;
        try {
            data = sdf.parse(source);
            return data;
        } catch (ParseException e) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_d2a41f5cb64e462b9347b885e0468d35"));
        }
    }

    /**
     * 将pattern格式的字符串日期转换为Date类型日期
     *
     * @param source  pattern格式的字符串日期
     * @param pattern 日期格式如yyyy-MM-dd HH:mm:ss
     * @return
     * @throws Exception
     */
    public static LocalDate str2LocalDate(String source, String pattern) {
        return LocalDate.parse(source, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 将LocalDateTime类型的日期转换成pattern格式的字符串日期
     *
     * @param date
     * @param pattern
     * @return
     * @throws Exception
     */
    public static String date2Str(LocalDateTime date, String pattern) {

        if (date == null) {
            return null;
        }
        Instant instant = date.atZone(getZoneId()).toInstant();
        Date resultDate = Date.from(instant);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(resultDate);
    }


    public static String date2Str(Date date, String pattern) {

        if (date == null) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }


    /**
     * 获取某一时间所在月某月第一天的开始时间 即2017-01-01 00:00:00.000
     *
     * @param date 时间参数
     * @return
     */
    public static Date getFirstDayOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        return c.getTime();
    }

    /**
     * 获取某一时间所在年第一天的开始时间 即2017-01-01 00:00:00.000
     *
     * @param date 时间参数
     * @return
     */
    public static Date getFirstDayOfYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, 0);
        return c.getTime();
    }

    /**
     * 获取某一天的开始时间 即2017-01-01 00:00:00
     *
     * @param date 时间参数
     * @return
     * @throws ParseException
     */
    public static Date getStartTimeOfDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        return c.getTime();
    }

    /**
     * 获取某一天的开始时间 即20170101000000
     *
     * @param date 时间参数
     * @return
     * @throws ParseException
     */
    public static String getStartTimeOfDayString(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdfDay = new SimpleDateFormat(SDFSECOND);
        return sdfDay.format(getStartTimeOfDay(date));
    }


    /**
     * 当前时间的24小时之前的时间
     *
     * @return
     */
    public static String get24TimeStringBeforeHour() {
        // 取得当前时间的24小时之前的时间
        LocalDateTime twentyFourHoursAgo = now().minusHours(24);
        return twentyFourHoursAgo.format(DTF_SDF_SECOND_UNSIGNED);
    }


    /**
     * 获取当前时间 即20170101000000
     *
     * @return
     * @throws ParseException
     */
    public static String getNowTimeString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SDFSECOND);
        return now().format(formatter);
    }


    /**
     * 将日期精度更改到年, 月，天或秒
     *
     * @param date
     * @param pattern DateUtils.SDF_SECOND、DateUtils.SDF_DAY、DateUtils.SDF_HOUR...
     * @return
     * @throws Exception
     */
    public static Date getShortDate(Date date, String pattern) {

        if (date == null || StringUtils.isBlank(pattern)) {
            return date;
        }

        String dateStr = null;
        Date dateR = null;
        switch (pattern) {
            case SDF_MINUTE:
                dateStr = date2Str(date, SDF_MINUTE);
                dateR = str2Date(dateStr, SDF_MINUTE);
                break;
            case SDF_SECOND:
                dateStr = date2Str(date, SDF_SECOND);
                dateR = str2Date(dateStr, SDF_SECOND);
                break;
            case SDF_DAY:
                dateStr = date2Str(date, SDF_DAY);
                dateR = str2Date(dateStr, SDF_DAY);
                break;
            case SDF_MONTH:
                dateStr = date2Str(date, SDF_MONTH);
                dateR = str2Date(dateStr, SDF_MONTH);
                break;
            case SDF_HOUR:
                dateStr = date2Str(date, SDF_HOUR);
                dateR = str2Date(dateStr, SDF_HOUR);
                break;
            case SDF_YEAR:
                dateStr = date2Str(date, SDF_YEAR);
                dateR = str2Date(dateStr, SDF_YEAR);
                break;
            case S_MONTH:
                dateStr = date2Str(date, S_MONTH);
                dateR = str2Date(dateStr, S_MONTH);
                break;
            default:
                break;
        }

        return dateR;
    }


    /**
     * @param dateStr
     * @param fromDateFormat
     * @param toDateFormat
     * @return
     * @throws ParseException
     */
    public static String transferDateFormat(String dateStr, String fromDateFormat, String toDateFormat) {
        SimpleDateFormat fromSdf = new SimpleDateFormat(fromDateFormat);
        SimpleDateFormat toSdf = new SimpleDateFormat(toDateFormat);
        try {
            return toSdf.format(fromSdf.parse(dateStr));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getTime(Date date) {
        return DatePattern.PURE_DATETIME_FORMAT.format(date);
    }


    /**
     * 获取当天0点的时间
     *
     * @return
     */
    public static LocalDate getLocalDateNow() {
        return now().toLocalDate().atStartOfDay().toLocalDate();
    }

    public static Date getDateNow() {
        LocalDate now = now().toLocalDate();
        return Date.from(now.atStartOfDay(getZoneId()).toInstant());
    }

    /**
     * 获取第二天零点的时间
     *
     * @return
     */
    public static LocalDate getLocalDateTomorrow() {
        return now().toLocalDate().plusDays(1).atStartOfDay().toLocalDate();
    }

    /**
     * 判断字符串时间是否在当前一个小时内
     *
     * @param dataTime iot上报时间 yyyyMMddHHmmss
     * @return 为true则为一个小时内，false则没有
     */
    public static boolean isInOneHour(String dataTime) {
        try {
            LocalDateTime time = LocalDateTime.parse(dataTime, MULTI_FORMATTER);
            LocalDateTime currentTime = now();
            long minutes = ChronoUnit.MINUTES.between(time, currentTime);
            if (minutes <= 60) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("时间格式解析错误：" + e.getMessage());
        }
        return false;
    }

    /**
     * 当前时间往前退多少个五分钟
     * 1个小时是12个五分钟，24小时为288
     *
     * @return 五分钟时间数组
     */
    public static LinkedList<LocalDateTime> getFiveMinutesWithTimeRange(int num) {
        LinkedList<LocalDateTime> resultList = new LinkedList<>();
        LocalDateTime currentTime = now().withNano(0).withSecond(0);
        int minute = currentTime.getMinute();
        int adjust = 5 - (minute % 5);
        LocalDateTime currentFiveMinute = currentTime.plusMinutes(adjust).minusMinutes(num * 5L);

        for (int i = 0; i < num; i++) {
            resultList.add(currentFiveMinute);
            currentFiveMinute = currentFiveMinute.plusMinutes(5);
        }
        return resultList;
    }

    /**
     * 获得当天五分钟精确时间，24小时所有十五分钟精度时间
     *
     * @return 五分钟时间数组
     */
    public static LinkedList<LocalDateTime> getTheSameDayFiveMinuteAccuracyTime() {
        LocalDateTime todayStart = getTodayStart();
        LocalDateTime theSecondDay = todayStart.plusDays(1);
        LinkedList<LocalDateTime> resultList = new LinkedList<>();
        while (todayStart.isBefore(theSecondDay)) {
            resultList.add(todayStart);
            todayStart = todayStart.plusMinutes(15);
        }
        return resultList;
    }

    /**
     * 方式1：使用 Stream API（推荐，简洁）
     * 返回 LocalDateTime 列表（当天时间部分为 00:00:00）
     */
    public static List<LocalDateTime> getDatesFromMonthStart(LocalDateTime localDateTime) {
        int startDay = 1;
        int endDay = localDateTime.getDayOfMonth();

        return IntStream.rangeClosed(startDay, endDay)
                .mapToObj(day -> localDateTime.withDayOfMonth(day).with(LocalTime.MIN))
                .collect(Collectors.toList());
    }

    public static List<LocalDate> getDatesFromMonthStart(LocalDate localDate) {
        return IntStream.rangeClosed(1, localDate.getDayOfMonth())
                .mapToObj(localDate::withDayOfMonth)
                .collect(Collectors.toList());
    }

    /**
     * 获得当天五分钟精确时间，2小时所有十五分钟精度时间
     *
     * @return 五分钟时间数组
     */
    public static LinkedList<LocalDateTime> getTwoHourFiveMinuteAccuracyTime() {
        LinkedList<LocalDateTime> result = new LinkedList<>();
        // 获取当前时间往前推两个小时的时间
        LocalDateTime twoHoursAgo = now().minusHours(2);
        // 使用循环生成两个小时内所有整十五分钟的时间，并在添加进数组的时候就转换好格式
        for (long i = 0; i <= ChronoUnit.MINUTES.between(twoHoursAgo, now()); i += 15) {
            LocalDateTime dateTime = twoHoursAgo.plusMinutes(i);
            // 计算分钟数（向下取整到最近的15分钟）
            dateTime = dateTime.withMinute((dateTime.getMinute() / 15) * 15).withSecond(0).withNano(0);
            result.add(dateTime);
        }
        return result;
    }

    /**
     * 24小时内天的数组
     *
     * @return 五分钟时间数组
     */
    public static List<String> get24WithHourDay() {
        // 取得当前时间
        LocalDateTime now = now();

        // 取得当前时间的24小时之前的时间
        LocalDateTime twentyFourHoursAgo = now.minusHours(24);

        // 创建一个用于存放日期的集合
        return Lists.newArrayList(
                now.format(DTF_SDFDAY),
                twentyFourHoursAgo.format(DTF_SDFDAY)
        );
    }

    /**
     * 获取最近5分钟时间
     *
     * @param source 数据源
     * @return 获取最近5分钟时间
     * @since 5.240702.0
     */
    public static LocalTime getRecent5Minute(@NonNull LocalTime source) {
        int minute = source.getMinute();
        int minuteMod = minute % 5;
        if (minuteMod != 0) {
            source = source.minusMinutes(minuteMod);
        }
        return source;
    }

    /**
     * 获取下一个最近的15分钟时刻
     *
     * @param dateTime 当前时间
     * @return 下一个最近的15分钟时刻
     */
    public static LocalDateTime getNextNearest15Minute(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        // 将秒和纳秒置为0
        LocalDateTime truncated = DateTimeTypeEnum.MINUTE.truncate(dateTime);

        // 获取当前分钟数
        int minute = truncated.getMinute();

        // 计算下一个15分钟时刻
        int next15Minute = ((minute / 15) + 1) * 15;

        // 如果超过60分钟，则需要进位到下一小时
        if (next15Minute >= 60) {
            next15Minute = 0;
            // 进位到下一小时
            truncated = truncated.plusHours(1);
        }

        // 设置分钟并返回结果
        return truncated.withMinute(next15Minute);
    }

    /**
     * 获取当前时间最近的五分钟时间
     *
     * @param timeString
     * @return
     */
    public static String getNearFiveMinutes(String timeString) {

        // 解析字符串为 LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(timeString, DTF_SDF_SECOND_UNSIGNED);

        // 计算分钟数（向下取整到最近的五分钟）
        int minutes = dateTime.getMinute();
        int roundedMinutes = (minutes / 5) * 5; // 向下取整到最接近的五分钟

        // 创建新的 LocalDateTime
        LocalDateTime roundedDateTime = dateTime.withMinute(roundedMinutes).withSecond(0).withNano(0);

        // 格式化回字符串
        return roundedDateTime.format(DTF_SDF_SECOND_UNSIGNED);
    }

    /**
     * 当前时间一个小时前的时间
     *
     * @return 一个小时前的当前时间
     */
    public static String currentTimeOneHourAgo() {
        return now().minusHours(1).format(DTF_SDF_SECOND);
    }

    /**
     * 获取多少天以前的时间
     *
     * @param day 多少天以前
     * @return LocalDateTime
     */
    public static LocalDateTime getPastTime(int day) {
        return getTodayStart().minusDays(day);
    }

    /**
     * 获取多少天以后的时间
     *
     * @param day 多少天以前
     * @return LocalDateTime
     */
    public static LocalDateTime getLastTime(int day) {
        return getTodayStart().plusDays(day);
    }

    /**
     * 获取今天0点开始的时间
     *
     * @return LocalDateTime
     */
    public static LocalDateTime getTodayStart() {
        return now().withSecond(0).withHour(0).withMinute(0).withNano(0);
    }

    /**
     * localDateTime转字符串
     *
     * @param localDateTime 时间
     * @return yyyyMMddHHmms字符串时间
     */
    public static String localDateTimeToString(LocalDateTime localDateTime) {
        return localDateTime.format(DTF_SDF_SECOND_UNSIGNED);
    }

    /**
     * 返回小时时间
     *
     * @param localDateTime 时间
     * @return 如：2023-07-06 09:00:00
     */
    public static LocalDateTime localDateTimeWithHour(LocalDateTime localDateTime) {
        return localDateTime.withMinute(0).withSecond(0).withNano(0);
    }


    /**
     * 获取当前时间的上一个{quotientOfDivision}分钟精度的时间点
     * 云端控制台需求产生具体精度时间
     *
     * @param quotientOfDivision 时间除以多少的商
     * @return 上一个整十五分钟精度
     * @version 2.0 增加入参-商  如：传入5则时间向前取5分钟精度整的时间 传入5则时间向前取15分钟精度整的时间
     * @version 1.0 仅为十五分钟精度整的时间
     */
    public static LocalDateTime getTheLastPrecisionMinutes(int quotientOfDivision) {
        // 获取当前时间
        LocalDateTime now = now();

        // 计算当前分钟数除以15的商，然后乘以15，得到上一个整15分钟的分钟数
        int previousQuarterMinute = ((now.getMinute() / quotientOfDivision) * quotientOfDivision);

        // 设置秒和纳秒为0，得到一个向上取整15分钟的时间，再减去十五分钟
        return now.withMinute(previousQuarterMinute)
                .withSecond(0).withNano(0);
    }

    /**
     * 获取当前时间的上一个十五分钟精度的时间点
     * 云端控制台需求产生具体精度时间
     *
     * @return 上一个整十五分钟精度再减十五分钟
     */
    public static String getTheLast15MinutesString() {
        return localDateTimeToString(getTheLastPrecisionMinutes(15));
    }

    /**
     * 获取当前时间的上一个十五分钟精度的时间点再减十五分钟
     * 云端控制台需求产生具体精度时间
     *
     * @return 上一个整十五分钟精度再减十五分钟
     */
    public static LocalDateTime getTheLast15Minus15Minutes() {
        return getTheLastPrecisionMinutes(15).minusMinutes(15);
    }


    /**
     * 获取当前时间距离当日结束剩多少秒
     *
     * @return 返回纳秒
     */
    public static long getTimeUntilEndOfDay() {
        // 获取当前时间
        LocalDateTime now = now();
        // 获取当日结束时间，将当前时间的小时、分钟、秒和纳秒都设置为 0，然后加一天
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(0).withNano(999_999_999);
        // 计算秒数差
        return endOfDay.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC);
    }

    public static Date getTodayEndTime() {
        LocalDateTime endOfDay = LocalDateTime.of(now().toLocalDate(), LocalTime.MAX);
        endOfDay = endOfDay.plusSeconds(-1);
        return Date.from(endOfDay.toInstant(ZoneOffset.UTC));
    }

}
