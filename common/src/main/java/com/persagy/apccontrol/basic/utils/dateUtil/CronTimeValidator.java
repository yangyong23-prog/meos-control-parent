package com.persagy.apccontrol.basic.utils.dateUtil;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

public class CronTimeValidator {
    
    /**
     * 判断指定时间是否应该触发CRON表达式对应的任务
     *
     * @param cronExpression CRON表达式
     * @param targetTime     要判断的时间（包含时区信息）
     * @return 如果应该触发返回true，否则返回false
     */
    public static boolean isTriggerTime(String cronExpression, ZonedDateTime targetTime)
            throws ParseException {
        
        // 验证CRON表达式有效性
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("无效的CRON表达式: " + cronExpression);
        }
        
        // 创建CRON表达式解析器并设置时区
        CronExpression cron = new CronExpression(cronExpression);
        cron.setTimeZone(TimeZone.getTimeZone(targetTime.getZone()));
        
        // 将目标时间转
        Date targetDate = Date.from(targetTime.toInstant());
        
        // 检查该时间是否满足CRON表达式
        return cron.isSatisfiedBy(targetDate);
    }
    
    /**
     * 容错判断：允许一定的时间误差范围（默认1秒）
     * 适用于系统时间存在微小偏差的场景
     */
    public static boolean isTriggerTimeWithTolerance(String cronExpression, ZonedDateTime targetTime) throws ParseException {
        return isTriggerTimeWithTolerance(cronExpression, targetTime, 1);
    }
    
    /**
     * 容错判断：允许指定的时间误差范围（单位：秒）
     */
    public static boolean isTriggerTimeWithTolerance(String cronExpression, ZonedDateTime targetTime,
                                                     long toleranceSeconds) throws ParseException {
        
        if (toleranceSeconds < 0) {
            throw new IllegalArgumentException("误差范围不能为负数");
        }
        
        // 检查目标时间本身
        if (isTriggerTime(cronExpression, targetTime)) {
            return true;
        }
        
        // 检查误差范围内的时间点
        for (long i = 1; i <= toleranceSeconds; i++) {
            // 检查前i秒
            ZonedDateTime before = targetTime.minusSeconds(i);
            if (isTriggerTime(cronExpression, before)) {
                return true;
            }
            
            // 检查后i秒
            ZonedDateTime after = targetTime.plusSeconds(i);
            if (isTriggerTime(cronExpression, after)) {
                return true;
            }
        }
        
        return false;
    }
    
    // 使用示例
    public static void main(String[] args) throws ParseException {
        String cron = "0 0/30 9-17 * * ?"; // 每天9点到17点，每30分钟执行一次
        
        // 1. 创建一个与CRON同时区的测试时间（应该触发）
        ZonedDateTime testTime1 = ZonedDateTime.of(
                LocalDateTime.of(2023, 10, 1, 9, 0),
                ZoneId.of("Asia/Shanghai")
        );
        
        // 2. 创建一个不同时区的测试时间（纽约时间3:00 = 上海时间9:00，应该触发）
        ZonedDateTime testTime2 = ZonedDateTime.of(
                LocalDateTime.of(2023, 10, 1, 3, 0),
                ZoneId.of("America/New_York")
        );
        
        // 3. 创建一个不应该触发的时间
        ZonedDateTime testTime3 = ZonedDateTime.of(
                LocalDateTime.of(2023, 10, 1, 10, 15),
                ZoneId.of("Asia/Shanghai")
        );
        
        // 4. 创建一个触发时间后1秒的时间（用于测试容错判断）
        ZonedDateTime testTime4 = testTime1.plus(1, ChronoUnit.SECONDS);
        
        System.out.println("时间 " + testTime1 + " 是否应该触发: " +
                isTriggerTime(cron, testTime1));
        System.out.println("时间 " + testTime2 + "（纽约时区）是否应该触发: " +
                isTriggerTime(cron, testTime2));
        System.out.println("时间 " + testTime3 + " 是否应该触发: " +
                isTriggerTime(cron, testTime3));
        
        System.out.println("\n时间 " + testTime4 + " 精确判断: " +
                isTriggerTime(cron, testTime4));
        System.out.println("时间 " + testTime4 + " 容错判断: " +
                isTriggerTimeWithTolerance(cron, testTime4));
    }
}

