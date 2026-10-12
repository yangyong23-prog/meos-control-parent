package com.persagy.apccontrol.basic.utils.number;

/**
 * 数值格式化工具
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/6/6 19:55
 * @since v5.240502.0
 */
public class NumericalUtils {

    public static String formatNumberWithDecimal(String numberStr, int decimalPlaces) {
        double number = Double.parseDouble(numberStr);
        String formatString = "%." + decimalPlaces + "f";
        return String.format(formatString, number);
    }

}
