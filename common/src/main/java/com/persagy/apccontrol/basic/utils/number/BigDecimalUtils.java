package com.persagy.apccontrol.basic.utils.number;

import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

/**
 * {@link BigDecimal} 工具类
 *
 * @author JiYinchuan
 * @since v4.5.0
 */
public class BigDecimalUtils {

    /**
     * 动态进行数字累加汇总
     *
     * @param result 原始值
     * @param value  累加值
     * @return 累加后的值, 如果 {@code result} 为 {@code null} 则返回 {@link BigDecimal#ZERO}
     * @since v4.5.0
     */
    public static BigDecimal accumulateNumberByAdd(@Nullable BigDecimal result,
                                                   @Nullable Double value) {
        return accumulateNumberByAdd(result, value, null);
    }

    /**
     * 通过加法累加数字
     *
     * @param values 值列表
     * @return {@link BigDecimal }
     */
    public static BigDecimal add(@Nullable BigDecimal... values) {
        if (values == null) {
            return null;
        }
        if (values.length == 1) {
            return values[0];
        }
        if (values.length == 2) {
            return accumulateNumberByAdd(values[0], values[1]);
        }
        return Arrays.stream(values).filter(Objects::nonNull).reduce(BigDecimal::add).orElse(null);
    }


    /**
     * 动态进行数字累加汇总
     *
     * @param result 原始值
     * @param value  累加值
     * @return 累加后的值, 如果 {@code result} 为 {@code null} 则返回 {@link BigDecimal#ZERO}
     * @since v4.5.0
     */
    public static BigDecimal accumulateNumberByAdd(@Nullable BigDecimal result,
                                                   @Nullable BigDecimal value) {
        return accumulateNumberByAdd(result, value, null);
    }

    /**
     * 动态进行数字累加汇总
     *
     * @param result       原始值
     * @param value        累加值
     * @param defaultValue 当原始值为 {@code null} 时的默认值
     * @return 累加后的值, 如果 {@code result} 为 {@code null} 则返回 {@link BigDecimal#ZERO}
     * @since v4.5.0
     */
    public static BigDecimal accumulateNumberByAdd(@Nullable BigDecimal result,
                                                   @Nullable Double value,
                                                   @Nullable BigDecimal defaultValue) {
        return accumulateNumberByAdd(result, Objects.nonNull(value) ? BigDecimal.valueOf(value) : null, defaultValue);
    }

    /**
     * 动态进行数字累加汇总
     *
     * @param result       原始值
     * @param value        累加值
     * @param defaultValue 当原始值为 {@code null} 时的默认值
     * @return 累加后的值, 如果 {@code result} 为 {@code null} 则返回 {@link BigDecimal#ZERO}
     * @since v4.5.0
     */
    public static BigDecimal accumulateNumberByAdd(@Nullable BigDecimal result,
                                                   @Nullable BigDecimal value,
                                                   @Nullable BigDecimal defaultValue) {
        result = defaultIfNull(result, defaultValue);
        if (Objects.nonNull(value)) {
            result = Objects.nonNull(result) ? result.add(value) : value;
        }
        return result;
    }

    /**
     * 如果为空, 则为默认值
     *
     * @param value        原始值
     * @param defaultValue 默认值
     * @return 如果 {@code value} 为 {@code null}, 则返回 {@code defaultValue}, 否则返回 {@code value} 值
     * @since v4.5.0
     */
    public static BigDecimal defaultIfNull(@Nullable BigDecimal value, @Nullable BigDecimal defaultValue) {
        return Objects.isNull(value) ? defaultValue : value;
    }
}
