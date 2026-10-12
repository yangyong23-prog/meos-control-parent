package com.persagy.apccontrol.basic.utils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.date.DatePattern;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 时间相关的工具类
 *
 * @author cuixubin
 */
public class  DateUtils extends org.apache.commons.lang.time.DateUtils {
    public static final String SECOND = "0";
    public static final String MINUTE = "1";
    public static final String HOUR = "2";
    public static final String DAY = "3";
    public static final String MONTH = "4";
    public static final String YEAR = "5";
    public static final String SDF_SECOND = "yyyy-MM-dd HH:mm:ss";
    public static final String SDF_MINUTE = "yyyy-MM-dd HH:mm";
    public static final String SDF_HOUR = "yyyy-MM-dd HH";
    public static final String SDF_DAY = "yyyy-MM-dd";
    public static final String SDFDAY = "yyyyMMdd";
    public static final String SDF_DOT_DAY = "yyyy.MM.dd";
    public static final String SDFDAY2 = "yyyy.MM.dd";
    public static final String SDFSECOND = "yyyyMMddHHmmss";
    public static final String SDF_MONTH = "yyyy-MM-01";
    public static final String SDF_DOT_MONTH = "yyyy.MM";
    public static final String SDFMONTH2 = "yyyy.MM";
    public static final String SDF_YEAR = "yyyy-01-01";
    public static final String SDF_ONLY_HOUR_MINUTE = "HH:mm";
    public static final String SDF_MONTH_DAY = "MM.dd";
    public static final String SDF_DOT_MINUTE = "yyyy.MM.dd HH:mm";
    //时间格式-显示
    public final static String date_format_show = "yyyy-MM-dd HH:mm:ss";

    /**
     * 将pattern格式的字符串日期转换为Date类型日期
     *
     * @param source  pattern格式的字符串日期
     * @param pattern 日期格式如yyyy-MM-dd HH:mm:ss
     * @return
     * @throws Exception
     */
    public static Date str2Date(String source, String pattern) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(source);
    }


    public static LocalDateTime getLocalDateTime(String time){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtils.SDF_DAY);
        String dateStr = LocalDate.now().format(formatter);
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern(DateUtils.SDF_SECOND);
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr +" "+ time,formatterTime);

        return localDateTime;
    }

    /**
     * Description:返回两个日期间相差多少天
     *
     * @param start
     * @param end
     * @return Integer
     * @author cuixubin
     * @since 2018年9月18日: 下午12:12:38
     * Update By cuixubin 2018年9月18日: 下午12:12:38
     */
    public static Integer getDayNum(Date start, Date end) {
        if (null == start || end == null) {
            return null;
        }

        if (start.getTime() == end.getTime()) {
            return 0;
        }

        int num = 0;
        Date temp = start.before(end) ? start : end;
        Date endDate = start.before(end) ? end : start;
        while (temp.before(endDate)) {
            num++;
            temp = getDayOff(temp, 1);
        }
        return num;
    }

    public static LocalDateTime str2LocalDateTime(String time){
        DateTimeFormatter df = DateTimeFormatter.ofPattern(DateUtils.SDFSECOND);

        LocalDateTime localTime = LocalDateTime.parse(time,df);

        return localTime;

    }

    /**
     * 将Date类型的日期转换成pattern格式的字符串日期
     *
     * @param date
     * @param pattern
     * @return
     * @throws Exception
     */
    public static String date2Str(Date date, String pattern) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 获取时间按年偏移offset个单位后的时间
     *
     * @param date
     * @param offset
     * @return
     */
    public static Date getYearOff(Date date, int offset) {
        return getOffset(date, offset, 5);
    }

    /**
     * 获取时间按月偏移offset个单位后的时间
     *
     * @param date
     * @param offset
     * @return
     */
    public static Date getMonthOff(Date date, int offset) {
        return getOffset(date, offset, 4);
    }

    /**
     * 获取时间按日偏移offset个单位后的时间
     *
     * @param date
     * @param offset
     * @return
     */
    public static Date getDayOff(Date date, int offset) {
        return getOffset(date, offset, 2);
    }

    /**
     * 获取时间按周偏移offset个单位后的时间
     *
     * @param date
     * @param offset
     * @return
     */
    public static Date getWeekOff(Date date, int offset) {
        return getOffset(date, offset, 3);
    }

    /**
     * 获取时间按小时偏移offset个单位后的时间
     *
     * @param date
     * @param offset
     * @return
     */
    public static Date getHourOff(Date date, int offset) {
        return getOffset(date, offset, 1);
    }

    /**
     * description 获取时间按分钟偏移offset个单位后的时间
     *
     * @param date
     * @param offset
     * @return java.util.Date
     * @author feng
     * @since 18:51 2018/12/10
     */
    public static Date getMinuteOff(Date date, int offset) {
        return getOffset(date, offset, 0);
    }


    /**
     * 获取指定时间粒度偏移量偏移后的时间
     *
     * @param date
     * @param offset int 偏移量
     * @param type   int 时间粒度0-分，1-时，2-天，3-周，4-月，5-年
     * @return
     */
    public static Date getOffset(Date date, int offset, int type) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        switch (type) {
            case 0:
                cal.add(Calendar.MINUTE, offset);
                break;
            case 1:
                cal.add(Calendar.HOUR_OF_DAY, offset);
                break;
            case 2:
                cal.add(Calendar.DAY_OF_MONTH, offset);
                break;
            case 3:
                cal.add(Calendar.DAY_OF_MONTH, offset * 7);
                break;
            case 4:
                cal.add(Calendar.MONTH, offset);
                break;
            case 5:
                cal.add(Calendar.YEAR, offset);
                break;
            default:
                break;
        }
        return cal.getTime();
    }

    /**
     * date转local
     * @param date
     * @return
     */
    public static LocalDateTime tooLocalDateTime(Date date) {

        LocalDateTime createTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return createTime;
    }

    /**
     * 得到某月最后一天的结束时间 即2017-01-31 23:59:59.999
     *
     * @param date 时间参数
     * @return
     */
    public static Date getLastDayOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        // 月加1，日减1 得到当前月最后1天
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DAY_OF_YEAR, -1);
        return c.getTime();
    }

    public static Date getEndTimeOfDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        return c.getTime();
    }

    public static Date getLastDayStartTimeOfMonth(Date date) {
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
        // 月加1，日减1 得到当前月最后1天
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DAY_OF_YEAR, -1);
        return c.getTime();
    }

    /**
     * 得到某年最后一天的结束时间 即2017-12-31 23:59:59.999
     *
     * @param date 时间参数
     * @return
     */
    public static Date getLastDayStartTimeOfYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.DAY_OF_MONTH, 31);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, 11);
        return c.getTime();
    }

    public static void main(String[] args) {
        System.out.println(getLastDayStartTimeOfMonth(new Date()));
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
    public static Date getStartTimeOfDay(Date date) throws ParseException {
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
     * 获取某一天的开始时间 即2017-01-01 00:00:00
     *
     * @param date 时间参数
     * @return
     * @throws ParseException
     */
    public static String getStartTimeOfDayStr(Date date) throws ParseException {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdfDay = new SimpleDateFormat(SDF_DAY);
        return sdfDay.format(getStartTimeOfDay(date));
    }

    public static String getStartTimeOfDayStrNew(Date date) throws ParseException {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdfDay = new SimpleDateFormat(SDF_SECOND);
        return sdfDay.format(getStartTimeOfDay(date));
    }

    public static String getStartTimeOfDayStrNow(Date date) throws ParseException {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdfDay = new SimpleDateFormat(SDF_SECOND);
        return sdfDay.format(date);
    }

    public static String getStartTimeOfDayEndNew(Date date) throws ParseException {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdfDay = new SimpleDateFormat(SDF_SECOND);
        return sdfDay.format(getEndTimeOfDay(date));
    }




    /**
     * endDate比beginDate多的天数
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static int differentDays(Date beginDate, Date endDate) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(beginDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(endDate);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if (year1 != year2) {
            int timeDistance = 0;
            for (int i = year1; i < year2; i++) {
                // 闰年
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {
                    timeDistance += 366;
                } else {
                    timeDistance += 365;
                }
            }
            return timeDistance + (day2 - day1);
        } else {
            return day2 - day1;
        }
    }
	
/*	public static void main(String[] args) throws Exception {
		Date start = str2Date("2018.01.01", "yyyy.MM.dd");
		Date end = str2Date("2018.01.02", "yyyy.MM.dd");
		
		int index = 0;
		Date temp = start;
		while (temp.before(end)) {
			System.out.println(date2Str(temp, "yyy.MM.dd HH:mm:ss"));
			
			temp = getMinuteOff(temp, 15);
			
			index ++;
			if(index == 96) {
				break;
			}
		}
	}*/

    /**
     * 获取周几(周一为1,周二为2,周日为7)
     *
     * @param date
     * @return
     */
    public static int getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day > 1) {
            day = day - 1;
        } else {
            day = 7;
        }
        return day;
    }

    /**
     * 获取月份
     *
     * @param date
     * @return
     */
    public static int getMonthOfYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        return month + 1;
    }

    /**
     * 获取年份
     *
     * @param date
     * @return
     */
    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        return year;
    }

    /**
     * 返回小时 0~23
     *
     * @param date
     * @return
     */
    public static int getHour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    /**
     * Description: 返回0-59
     *
     * @param date
     * @return int
     * @author cuixubin
     * @since 2018年12月18日: 下午6:29:43
     */
    public static int getMinute(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int minute = calendar.get(Calendar.MINUTE);
        return minute;
    }

    /**
     * 获取某一月的总天数
     *
     * @param date
     * @return
     */
    public static int getTotalDaysOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static int getTotalDaysOfYear(Date date) {
        return ifRunYear(date) ? 366 : 365;
    }

    /**
     * 获得本周星期一的日期
     *
     * @param time
     * @return
     */
    public static Date getCurrentMonday(Date time) {
        int mondayPlus = 0;
        Calendar cd = Calendar.getInstance();
        cd.setTime(time);
        // 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) {
            mondayPlus = -6;
        } else {
            mondayPlus = 2 - dayOfWeek;
        }
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus);
        Date monday = currentDate.getTime();
        return monday;
    }

    /**
     * 获取日期所在周的周一
     *
     * @param date
     * @return
     */
    public static Date getMonday(Date date) {
        int daynum = getDayOfWeek(date);
        try {
            date = getShortDate(date, SDF_DAY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getDayOff(date, 1 - daynum);
    }

    /**
     * 获取日期所在周周六0点
     *
     * @param date
     * @return
     */
    public static Date getWeekDay(Date date) {
        int daynum = getDayOfWeek(date);
        try {
            date = getShortDate(date, SDF_DAY);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (daynum == 6) {
            try {
                return date;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (daynum == 7) {
            return getDayOff(date, -1);
        } else {
            return getDayOff(date, 6 - daynum);
        }
    }

    /**
     * 将日期精度更改到年, 月，天或秒
     *
     * @param date
     * @param pattern DateUtils.SDF_SECOND、DateUtils.SDF_DAY、DateUtils.SDF_HOUR...
     * @return
     * @throws Exception
     */
    public static Date getShortDate(Date date, String pattern) throws Exception {
        String dateStr = null;
        Date dateR = null;
        switch (pattern) {
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
            default:
                break;
        }

        return dateR;
    }

    /**
     * 判断两个日期是否为同一年/月/日
     *
     * @param date1
     * @param date2
     * @param type  3-日；4-月；5-年
     * @return
     */
    public static boolean ifSameDate(Date date1, Date date2, String type) {
        boolean result = false;
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        int month1 = cal1.get(Calendar.MONTH);
        int month2 = cal2.get(Calendar.MONTH);
        int day1 = cal1.get(Calendar.DAY_OF_MONTH);
        int day2 = cal2.get(Calendar.DAY_OF_MONTH);

        switch (type) {
            case YEAR:
                result = year1 == year2;
                break;
            case MONTH:
                result = (year1 == year2) && (month1 == month2);
                break;
            case DAY:
                result = (year1 == year2) && (month1 == month2) && (day1 == day2);
                break;
            default:
                break;
        }

        return result;
    }

    /**
     * 返回同一年中两个时间点相差的天数，两个时间点若不是同一年则返回0
     *
     * @param fromDate
     * @param toDate
     * @return
     */
    public static int dayNumber(Date fromDate, Date toDate) {
        int num = 0;
        if (fromDate.after(toDate)) {
            Date temp = fromDate;
            fromDate = toDate;
            toDate = temp;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(fromDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(toDate);


        if (cal.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
            num = cal2.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR);
        }

        return num;
    }

    /**
     * 获取某月的第几天 如month="2017-11-23 00:00:00",返回23
     *
     * @param month
     * @return
     */
    public static int getDayOfMonth(Date month) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(month);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 判断某一时间所在年是否为润年
     *
     * @param date
     * @return
     */
    public static boolean ifRunYear(Date date) {
        int year = getYear(date);
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取日期所在周为本年第几周
     *
     * @param date
     * @return
     */
    public static int getWeekNum(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    public static Date getWeekStart(Date date, int weekNumOfYear) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.WEEK_OF_YEAR, weekNumOfYear);

        return cal.getTime();
    }

    /**
     * 获取某时间所在周（非）工作日的开始结束时间
     *
     * @param date
     * @param offSet 0-date所在周，1-下一周，-1-上一周，n-下n周
     * @return {"startTime":null,"endTime":null,"daynum":2} (非)工作日的开始时间、结束时间、天数
     */
    public static Map<String, Object> getWorkTime(Date date, int offSet) {
        Map<String, Object> returnMap = new HashMap<>();

        int weekDay = getDayOfWeek(date);
        Date startDate = getMonday(date);
        try {
            startDate = getShortDate(startDate, SDF_DAY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (weekDay < 6) {
            returnMap.put("daynum", 5);
            returnMap.put("startTime", getDayOff(startDate, offSet * 7));
            returnMap.put("endTime", getDayOff(startDate, 5 + offSet * 7));
        } else {
            returnMap.put("daynum", 2);
            returnMap.put("startTime", getDayOff(startDate, 5 + offSet * 7));
            returnMap.put("endTime", getDayOff(startDate, 7 + offSet * 7));
        }
        return returnMap;
    }

    /**
     * 获取日期信息
     *
     * @param date
     * @param field 0-秒，1-分，2-时（24），3-天（月），4-月，5-年
     * @return
     */
    public static int getFieldOfDate(Date date, int field) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int num = 0;
        switch (field) {
            case 0:
                num = cal.get(Calendar.MILLISECOND);
                break;
            case 1:
                num = cal.get(Calendar.MILLISECOND);
                break;
            case 2:
                num = cal.get(Calendar.HOUR_OF_DAY);
                break;
            case 3:
                num = cal.get(Calendar.DAY_OF_MONTH);
                break;
            case 4:
                num = cal.get(Calendar.MONTH);
                break;
            case 5:
                num = cal.get(Calendar.YEAR);
                break;
            case 21:
                num = cal.get(Calendar.HOUR);
                break;
            case 22:
                num = cal.get(Calendar.DAY_OF_YEAR);
                break;
            default:
                break;
        }
        return num;
    }

    /**
     * 获取当前时间所处于本年第几
     *
     * @param date
     * @return
     */
    public static int getWeeksOfYear(Date date) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setMinimalDaysInFirstWeek(7);
        c.setTime(date);

        return c.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * 获取某一年共有多少周
     *
     * @param year
     * @return
     */
    public static int getMaxWeekNumOfYear(int year) {
        Calendar c = new GregorianCalendar();
        c.set(year, Calendar.DECEMBER, 31, 23, 59, 59);

        return getWeeksOfYear(c.getTime());
    }

    /**
     * 获取某年的第几周的开始日期
     *
     * @param year
     * @param week 从1开始
     * @return
     */
    public static Date getFirstDayOfWeek(int year, int week) {
        week--;

        Date startDate = null;
        try {
            startDate = str2Date(year + "-01-01", SDF_DAY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 周几
        int weekDayNum = getDayOfWeek(startDate);
        int offset = 0;
        if (weekDayNum > 1) {
            offset = 8 - weekDayNum;
        }

        Date returnDay = getDayOff(startDate, offset);

        return getDayOff(returnDay, 7 * week);
    }


    public static String getNowAddOneMinuStr(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newTime = now.plusMinutes(1);
        return newTime.withSecond(0).withNano(0).format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN));
    }

    public static String getNowStr(){
        LocalDateTime now = LocalDateTime.now();
        return now.withSecond(0).withNano(0).format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN));
    }


    /**
     * 更具20180104标准数据接口定义，获取环比的对应时间段 ， 不建议使用，可由getCompareDateRange(TimeDTO timeDTO)替代
     *
     * @param timeFrom 周（月/年）开始时间，周开始时间为周一
     * @param timeTo   结束时间，精确到分钟，在一周(月/年)内
     * @param timeType 时间类型 3-周，4-月，5-年
     * @param ifCircle true-环比，false-同比
     * @return {"timeFrom":,"timeTo":} 相应的环比时间段
     * @throws Exception
     */
    @Deprecated
    public static Map<String, Date> getCompareDateRange(Date timeFrom, Date timeTo, int timeType, boolean ifCircle)
            throws Exception {
        Map<String, Date> returnMap = new HashMap<>();
        Date timeFrom_c = null, timeTo_c = null;

        if (ifCircle) {
            // 环比时间段计算
            switch (timeType) {
                case 3: // 周环比
                {
                    // 开始时间为上周周一0点，结束时间为当前时间整点对应的上周时间
                    timeFrom_c = getDayOff(timeFrom, -7);
                    timeTo_c = getDayOff(getShortDate(timeTo, SDF_HOUR), -7);
                }
                break;
                case 4: // 月环比
                {
                    // 开始时间为上个月首日0点
                    timeFrom_c = getMonthOff(timeFrom, -1);
                    int dayOfMonth2 = 28;
                    if (ifRunYear(timeFrom)) {
                        dayOfMonth2 = 29;
                    }
                    if (getMonthOfYear(timeTo) == 3 && getDayOfMonth(timeTo) > dayOfMonth2) {
                        // 如当前为3月，日期 > 28或29，时间段为整个二月，结束时间点是3月首日0点
                        timeTo_c = timeFrom;
                    } else {
                        timeTo_c = getMonthOff(getShortDate(timeTo, SDF_HOUR), -1);
                    }
                }
                break;
                case 5: // 年环比
                {
                    timeFrom_c = getYearOff(timeFrom, -1);
                    // 结束时间为润年2月29日
                    if (ifRunYear(timeFrom) && getMonthOfYear(timeTo) == 2 && getDayOfMonth(timeTo) == 29) {
                        timeTo_c = getDayOff(getShortDate(getYearOff(timeTo, -1), SDF_DAY), 1);
                    } else {
                        timeTo_c = getYearOff(getShortDate(timeTo, SDF_HOUR), -1);
                    }
                }
                break;

                default:
                    break;
            }
        } else {
            // 同比时间段计算
            if (timeType == 3) {
                // 周同比

                // 开始时间(及周一)对应的年份
                int yearNum = getYear(timeFrom);
                // 当前周为本年第几周
                int weekNum = getWeeksOfYear(timeFrom);
                // 开始时间所在年最多有几周
                int weekNumMax = getMaxWeekNumOfYear(yearNum);
                // 开始时间前一年最多有几周
                int weekNumLast = getMaxWeekNumOfYear(yearNum + 1);
                if (weekNum == weekNumMax && weekNumMax > weekNumLast) {
                    // 开始时间为当年最后一周，且前一年周数比当年少一周，取前年最后一周
                    timeFrom_c = getFirstDayOfWeek(yearNum - 1, weekNum);
                } else {
                    // 取前年同一周
                    timeFrom_c = getFirstDayOfWeek(yearNum - 1, weekNum);
                }
                long secnum = timeFrom_c.getTime() + timeTo.getTime() - timeFrom.getTime();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(secnum);
                timeTo_c = getShortDate(cal.getTime(), SDF_HOUR);

            } else if (timeType == 4 || timeType == 5) {
                // 月同比或年同比

                timeFrom_c = getYearOff(timeFrom, -1);
                if (ifRunYear(timeFrom) && getMonthOfYear(timeTo) == 2 && getDayOfMonth(timeTo) == 29) {
                    timeTo_c = getMonthOff(timeFrom_c, 1);
                } else {
                    timeTo_c = getYearOff(getShortDate(timeTo, SDF_HOUR), -1);
                }
            }
        }

        returnMap.put("timeFrom", timeFrom_c);
        returnMap.put("timeTo", timeTo_c);
        return returnMap;
    }

    /**
     * 是否当前月
     *
     * @param date
     * @return
     * @throws Exception
     */
    public static boolean isCurrentMonth(Date date) throws Exception {
        return (getFirstDayOfMonth(date).getTime() == getFirstDayOfMonth(new Date()).getTime());
    }

    public static boolean isCurrentYear(Date date) throws Exception {
        return (getFirstDayOfYear(date).getTime() == getFirstDayOfYear(new Date()).getTime());
    }

    public static boolean isCurrentDay(Date date) throws Exception {
        return (getStartTimeOfDay(date).getTime() == getStartTimeOfDay(new Date()).getTime());
    }

    /**
     * description 获取一天15分钟的时间列表
     *
     * @param date
     * @return
     * @author feng
     * @since 18:42 2018/12/10
     */
    public static List<Date> get15MinDateList(Date date) throws Exception {
        List<Date> resultDateList = new ArrayList<>();
        Date min15Date = getStartTimeOfDay(date);
        for (int i = 0; i < 96; i++) {
            resultDateList.add(min15Date);
            min15Date = getMinuteOff(min15Date, 15);
        }
        return resultDateList;
    }

    /**
     * Description: 获取某一时间所在15min的向下取整时间点，如2018-01-01 20:17:15 对应的为 2018-01-01 20:15:00;2018-01-01 20:30:02 对应的为
     * 2018-01-01 20:30:00
     *
     * @param timeNow
     * @return Date
     * @throws Exception
     * @author cuixubin
     * @since 2018年12月18日: 下午6:24:52
     */
    public static Date get15MinLite(Date timeNow) throws Exception {
        if (null == timeNow) {
            return null;
        }

        int hourMinute = getMinute(timeNow);
        if (hourMinute < 15) {
            hourMinute = 0;
        } else if (hourMinute < 30) {
            hourMinute = 15;
        } else if (hourMinute < 45) {
            hourMinute = 30;
        } else if (hourMinute < 60) {
            hourMinute = 45;
        }

        Date resultDate = getShortDate(timeNow, SDF_HOUR);
        resultDate = getMinuteOff(resultDate, hourMinute);

        return resultDate;
    }

    /**
     * description 获取时间段的月份列表
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return java.util.List<java.util.Date>
     * @author feng
     * @since 12:16 2019/4/22
     */
    public static List<Date> getMonthList(Date startDate, Date endDate) throws Exception {
        List<Date> resultList = new ArrayList<>();
        while (startDate.before(endDate)) {
            resultList.add(startDate);
            startDate = getMonthOff(startDate, 1);
        }
        return resultList;
    }

    public static List<Date> getYearList(Date startDate, Date endDate) throws Exception {
        List<Date> resultList = new ArrayList<>();
        while (startDate.before(endDate)) {
            resultList.add(startDate);
            startDate = getYearOff(startDate, 1);
        }
        return resultList;
    }

    /**
     * description 获取时间段的
     *
     * @param startDate
     * @param endDate
     * @return java.util.List<java.util.Date>
     * @author feng
     * @since 17:29 2019/4/23
     */
    public static List<Date> getDayList(Date startDate, Date endDate) throws Exception {
        List<Date> resultList = new ArrayList<>();
        while (startDate.before(endDate)) {
            resultList.add(startDate);
            startDate = getDayOff(startDate, 1);
        }
        return resultList;
    }

    public static boolean isSameMonth(Date date1, Date date2) throws Exception {
        return getFirstDayOfMonth(date1).getTime() == getFirstDayOfMonth(date2).getTime();
    }

    public static Double getYearPassedTimeRatio(Date startTime, Date endTime) {
        Date now = new Date();
        Date nowYearStartTime = DateUtils.getFirstDayOfYear(now);
        Date nextYearStartTime = DateUtils.getYearOff(nowYearStartTime, 1);
        if (!endTime.after(nowYearStartTime)) {
            return 1.0;
        }
        if (!startTime.before(nextYearStartTime)) {
            return 0.0;
        }

        return (endTime.getTime() - nowYearStartTime.getTime()) * 1.0 / (nextYearStartTime.getTime() - nowYearStartTime.getTime());
    }

    public static Double getMonthPassedTimeRatio(Date startTime, Date endTime) {
        Date now = new Date();
        Date nowMonthStartTime = DateUtils.getFirstDayOfMonth(now);
        Date nextMonthStartTime = DateUtils.getMonthOff(nowMonthStartTime, 1);
        if (!endTime.after(nowMonthStartTime)) {
            return 1.0;
        }
        if (!startTime.before(nextMonthStartTime)) {
            return 0.0;
        }
        return (endTime.getTime() - nowMonthStartTime.getTime()) * 1.0 / (nextMonthStartTime.getTime() - nowMonthStartTime.getTime());
    }


    public static List<Date> get15MinOrHourDateList(Date beginDate, Date endDate, boolean hourFlag, Integer code) throws Exception {

        if (beginDate == null && endDate == null) {
            String startTime = DateUtils.getStartTimeOfDayStrNew(com.persagy.apccontrol.basic.utils.dateUtil.DateUtils.getDateNow());
            String endTime = DateUtils.getStartTimeOfDayEndNew(com.persagy.apccontrol.basic.utils.dateUtil.DateUtils.getDateNow());

            beginDate = DateUtils.str2Date(startTime, DateUtils.SDF_SECOND);
            endDate = DateUtils.str2Date(endTime, DateUtils.SDF_SECOND);
        }

        List<Date> resultDateList = new ArrayList<>();
        Date minOrHourDate = beginDate;
        if (hourFlag) {
            minOrHourDate.setMinutes(0);
            if (beginDate.getTime() > minOrHourDate.getTime()) {
                minOrHourDate = DateUtils.getHourOff(minOrHourDate, 1);
            }
        } else {
            if (minOrHourDate.getMinutes() % code != 0) {
                minOrHourDate = DateUtils.getMinuteOff(minOrHourDate, 1);
                while (DateUtils.getMinute(minOrHourDate) % code != 0) {
                    minOrHourDate = DateUtils.getMinuteOff(minOrHourDate, 1);
                }
            }
        }

        while (minOrHourDate.getTime() < endDate.getTime()) {
            resultDateList.add(minOrHourDate);
            if (hourFlag) {
                minOrHourDate = DateUtils.getHourOff(minOrHourDate, 1);
            } else {
                minOrHourDate = getMinuteOff(minOrHourDate, code);
            }
        }
        return resultDateList;
    }

    /**
     * @Description:  获取几个5分钟
     * @Author: laixingqi
     * @Date: 2024/1/4 10:57
     * @param: [beginDate, endDate, hourFlag, code]
     * @return: java.util.List<java.util.Date>
     **/
    public static List<LocalTime> get5MinOrHourDateList(LocalDateTime beginDateTime, LocalDateTime endDateTime,
                                                   boolean hourFlag, Integer minutes,
                                                   Integer value) throws Exception {

        int time = value * minutes; //几个minutes

        beginDateTime = endDateTime.minusMinutes(time);

        Date beginDate = Date.from(beginDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

        List<LocalTime> resultDateList = new ArrayList<>();
        Date minOrHourDate = beginDate;
        if (hourFlag) {
            minOrHourDate.setMinutes(0);
            if (beginDate.getTime() > minOrHourDate.getTime()) {
                minOrHourDate = DateUtils.getHourOff(minOrHourDate, 1);
            }
        } else {
            if (minOrHourDate.getMinutes() % minutes != 0) {
                minOrHourDate = DateUtils.getMinuteOff(minOrHourDate, 1);
                while (DateUtils.getMinute(minOrHourDate) % minutes != 0) {
                    minOrHourDate = DateUtils.getMinuteOff(minOrHourDate, 1);
                }
            }else{
                minOrHourDate = DateUtils.getMinuteOff(minOrHourDate, 1);
                while (DateUtils.getMinute(minOrHourDate) % minutes != 0) {
                    minOrHourDate = DateUtils.getMinuteOff(minOrHourDate, 1);
                }
            }
        }

        while (minOrHourDate.getTime() <= endDate.getTime()) {
            String sTime = date2Str(minOrHourDate, SDF_ONLY_HOUR_MINUTE);
            LocalTime localTime = LocalTime.parse(sTime, DateTimeFormatter.ofPattern(SDF_ONLY_HOUR_MINUTE));
            resultDateList.add(localTime);
            if (hourFlag) {
                minOrHourDate = DateUtils.getHourOff(minOrHourDate, 1);
            } else {
                minOrHourDate = getMinuteOff(minOrHourDate, minutes);
            }
        }
        return resultDateList;
    }


    /**
     * 最近的五分钟
     * @param dateTime
     * @return
     */
    public static LocalDateTime getNear5(LocalDateTime dateTime) {
        if (null == dateTime) {
            return null;
        }
        Integer minute = dateTime.getMinute();
        minute = minute/5*5;
        return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(), dateTime.getHour(), minute, 0);
    }

    /**
     * 最近的五分钟
     * @param date
     * @return
     */
    public static LocalDateTime getNear5(String date) {
        if (null == date) {
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return getNear5(dateTime);
    }

    public static Double getDateDuration(Date beginDate, Date endDate) {
        long nh = 1000 * 60 * 60;
        long diff = endDate.getTime() - beginDate.getTime();
        return (double) diff / nh;
    }

    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        if (day == 0 && hour != 0 && min != 0) {
            return hour + LocaleMessageUtil.getMessage("key_bcf89cdac8e84cc5b787fbc96d97922f") + min + LocaleMessageUtil.getMessage("key_0c337df7bee44a529951778dc1f24c44");
        } else if (day == 0 && hour == 0 && min != 0) {
            return min + LocaleMessageUtil.getMessage("key_0c337df7bee44a529951778dc1f24c44");
        } else if (day == 0 && hour != 0 && min == 0) {
            return hour + LocaleMessageUtil.getMessage("key_bcf89cdac8e84cc5b787fbc96d97922f");
        } else if (day == 0 && hour == 0 && min == 0) {
            return min + LocaleMessageUtil.getMessage("key_0c337df7bee44a529951778dc1f24c44");
        } else if (day != 0 && hour == 0 && min == 0) {
            return day + LocaleMessageUtil.getMessage("key_e86e42b3978a48878983970408005299");
        } else if (day != 0 && hour == 0 && min != 0) {
            return day + LocaleMessageUtil.getMessage("key_e86e42b3978a48878983970408005299") + min + LocaleMessageUtil.getMessage("key_0c337df7bee44a529951778dc1f24c44");
        } else if (day != 0 && hour != 0 && min == 0) {
            return day + LocaleMessageUtil.getMessage("key_e86e42b3978a48878983970408005299") + hour + LocaleMessageUtil.getMessage("key_bcf89cdac8e84cc5b787fbc96d97922f");
        }
        return day + LocaleMessageUtil.getMessage("key_e86e42b3978a48878983970408005299") + hour + LocaleMessageUtil.getMessage("key_bcf89cdac8e84cc5b787fbc96d97922f") + min + LocaleMessageUtil.getMessage("key_0c337df7bee44a529951778dc1f24c44");
    }

    public static boolean checkDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat(DateUtils.date_format_show);
        try {
            Date date1 = format.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            if (calendar.get(Calendar.YEAR) < 1970) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
