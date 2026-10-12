package com.persagy.apccontrol.basic.service;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 业务计算服务
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
public interface BizComputeService {

    /**
     * 默认四舍五入模式
     */
    RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * 计算分位值
     *
     * @param quantile 分位数
     * @param values   数据
     * @return 分位值
     * @since 5.240502.0
     */
    BigDecimal calculatePercentile(int quantile, @NonNull List<BigDecimal> values);

    /**
     * 计算中位数
     *
     * @param values 数据
     * @return 中位数
     * @since 5.240502.0
     */
    BigDecimal calculateMedian(@NonNull List<BigDecimal> values);

    /**
     * 计算中位数
     *
     * @param values 数据
     * @param scale  精度
     * @return 中位数
     * @since 5.240502.0
     */
    BigDecimal calculateMedian(@NonNull List<BigDecimal> values, @Nullable Integer scale);

    /**
     * 计算上四分位数
     *
     * @param values 数值
     * @return 上四分位数
     * @since 5.240502.0
     */
    BigDecimal calculateUpperQuartile(@NonNull List<BigDecimal> values);

    /**
     * 计算上四分位数
     *
     * @param values 数值
     * @param scale  精度
     * @return 上四分位数
     * @since 5.240502.0
     */
    BigDecimal calculateUpperQuartile(@NonNull List<BigDecimal> values, @Nullable Integer scale);

    /**
     * 计算下四分位数
     *
     * @param values 数值
     * @return 下四分位数
     * @since 5.240502.0
     */
    BigDecimal calculateLowerQuartile(@NonNull List<BigDecimal> values);

    /**
     * 计算下四分位数
     *
     * @param values 数值
     * @param scale  精度
     * @return 下四分位数
     * @since 5.240502.0
     */
    BigDecimal calculateLowerQuartile(@NonNull List<BigDecimal> values, @Nullable Integer scale);

    /**
     * 计算平均数
     *
     * @param values 数值
     * @return 平均数
     * @since 5.240502.0
     */
    BigDecimal calculateAverage(@NonNull List<BigDecimal> values);

    /**
     * 计算平均数
     *
     * @param values 数值
     * @param scale  精度
     * @return 平均数
     * @since 5.240502.0
     */
    BigDecimal calculateAverage(@NonNull List<BigDecimal> values, @Nullable Integer scale);

    /**
     * 计算最大值
     *
     * @param values 数值
     * @return 最大值
     * @since 5.240502.0
     */
    BigDecimal calculateMaximum(@NonNull List<BigDecimal> values);

    /**
     * 计算最小值
     *
     * @param values 数值
     * @return 最小值
     * @since 5.240502.0
     */
    BigDecimal calculateMinimum(@NonNull List<BigDecimal> values);

}
