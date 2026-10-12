package com.persagy.apccontrol.basic.utils.systemgraph;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SafeDateUtil {

    private static final String[] formatters = {
            "yyyy-MM-dd",
            "yyyy-MM-dd HH:mm:ss",
            "HH:mm:ss",
            "yyyyMMdd",
            "yyyy.MM.dd",
            "yyyy/MM/dd",
    };

    private static LocalDate getDate(String str, String formatter) {
        try {
            return LocalDate.parse(str, DateTimeFormatter.ofPattern(formatter));
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDateTime getDateTime(String str, String formatter) {
        try {
            return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(formatter));
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalTime getTime(String str, String formatter) {
        try {
            return LocalTime.parse(str, DateTimeFormatter.ofPattern(formatter));
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDate tryGetDate(String str, @Nullable String bdtpFormatter) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        if (StringUtils.isNotBlank(bdtpFormatter)) {
            String formatter = bdtpFormatter.replace("YYYY", "yyyy").replace("DD", "dd");
            LocalDate result = getDate(str, formatter);
            if (result != null) {
                return result;
            }
        }

        for (String formatter : formatters) {
            LocalDate result = getDate(str, formatter);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static String tryGetDateStr(String str, String bdtpFormatter) {
        LocalDate localDate = tryGetDate(str, bdtpFormatter);
        if (localDate == null) {
            return str;
        }
        return DateTimeFormatter.ofPattern("yyyy.MM.dd").format(localDate);
    }


    public static LocalDateTime tryGetDateTime(String str, @Nullable String bdtpFormatter) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        if (StringUtils.isNotBlank(bdtpFormatter)) {
            String formatter = bdtpFormatter.replace("YYYY", "yyyy").replace("DD", "dd");
            LocalDateTime result = getDateTime(str, formatter);
            if (result != null) {
                return result;
            }
        }

        for (String formatter : formatters) {
            LocalDateTime result = getDateTime(str, formatter);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static String tryGetDateTimeStr(String str, String bdtpFormatter) {
        LocalDateTime localDateTime = tryGetDateTime(str, bdtpFormatter);
        if (localDateTime == null) {
            return str;
        }
        return DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss").format(localDateTime);
    }

    public static LocalTime tryGetTime(String str, @Nullable String bdtpFormatter) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        if (StringUtils.isNotBlank(bdtpFormatter)) {
            String formatter = bdtpFormatter.replace("YYYY", "yyyy").replace("DD", "dd");
            LocalTime result = getTime(str, formatter);
            if (result != null) {
                return result;
            }
        }

        for (String formatter : formatters) {
            LocalTime result = getTime(str, formatter);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static String tryGetTimeStr(String str, String bdtpFormatter) {
        LocalTime localTime = tryGetTime(str, bdtpFormatter);
        if (localTime == null) {
            return str;
        }
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(localTime);
    }

}
