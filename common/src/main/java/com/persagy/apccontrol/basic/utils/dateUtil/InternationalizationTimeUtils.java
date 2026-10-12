package com.persagy.apccontrol.basic.utils.dateUtil;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.utils.i18n.LocaleContext;
import com.persagy.framework.common.i18n.I18nProperties;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * 国际化时间工具
 */
public class InternationalizationTimeUtils {
    
    // 开关：true=中文格式（2024年9月1日）；false=英文序数词格式（sep 1st 2024）
    public static String formatDate(LocalDate date, boolean isChineseFormat) {
        if (isChineseFormat) {
            return date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        } else {
            // 英文序数词格式：分步处理
            // 1. 获取小写的月份缩写（如 sep）
            String month = date.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)).toLowerCase();
            // 2. 获取带序数词后缀的日期（如 1st）
            int day = date.getDayOfMonth();
            String dayWithSuffix = day + getDaySuffixJdk8(day);
            // 3. 获取4位年份
            int year = date.getYear();
            // 4. 拼接最终格式
            return String.format("%s %s %d", month, dayWithSuffix, year);
        }
    }
    
    /**
     * 日期转换中文、英文
     */
    public static String timeTransition(String time) {
        try {
            boolean isChineseFormat = true;
            if (Objects.isNull(LocaleContext.getLocaleContext())) {
                // 从paas的环境配置中取
                String i18n = SpringUtil.getBean(I18nProperties.class).getI18n();
                final Locale locale = Locale.forLanguageTag(i18n);
                LocaleContext.setLocaleContext(locale);
            }
            String code = LocaleContext.getLocaleContext().getLanguage();
            if (!Locale.CHINESE.getLanguage().equals(code)) {
                isChineseFormat = false;
            }
            return formatDate(LocalDate.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")), isChineseFormat);
        } catch (Exception e) {
            //失败返回原始日期
            return time;
        }
        
    }
    
    private static String getDaySuffixJdk8(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
    
    // 测试：直接运行即可验证
    public static void main(String[] args) {
        // 测试日期：涵盖特殊序数词（1st、11th、23rd）
        LocalDate date1 = LocalDate.of(2024, 9, 1);
        LocalDate date2 = LocalDate.of(2024, 10, 11);
        LocalDate date3 = LocalDate.of(2024, 12, 23);
        
        // 开关true：中文格式
        System.out.println(formatDate(date1, true)); // 2024年09月01日
        System.out.println(formatDate(date2, true)); // 2024年10月11日
        // 开关false：英文序数词格式
        System.out.println(formatDate(date1, false));// sep 1st 2024
        System.out.println(formatDate(date3, false));// dec 23rd 2024
    }
    
}
