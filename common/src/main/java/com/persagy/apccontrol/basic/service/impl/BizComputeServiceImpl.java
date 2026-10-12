package com.persagy.apccontrol.basic.service.impl;

import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.service.BizComputeService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 计算服务实现
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Service
public class BizComputeServiceImpl implements BizComputeService, BizComponent<BizComputeService> {

    @Override
    public BigDecimal calculatePercentile(int quantile, @NonNull List<BigDecimal> values) {
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        double[] tempValues = toDoubleArray(values);
        Percentile referenceNinetyPercentile = new Percentile(quantile);
        double referenceNinetyPercentileValue = referenceNinetyPercentile.evaluate(tempValues);
        return BigDecimal.valueOf(referenceNinetyPercentileValue);
    }

    @Override
    public BigDecimal calculateMedian(@NonNull List<BigDecimal> values) {
        return calculateMedian(values, null);
    }

    @Override
    public BigDecimal calculateMedian(@NonNull List<BigDecimal> values, @Nullable Integer scale) {
        return scale(calculatePercentile(50, values), scale);
    }

    @Override
    public BigDecimal calculateUpperQuartile(@NonNull List<BigDecimal> values) {
        return calculateUpperQuartile(values, null);
    }

    @Override
    public BigDecimal calculateUpperQuartile(@NonNull List<BigDecimal> values, @Nullable Integer scale) {
        return scale(calculatePercentile(75, values), scale);
    }

    @Override
    public BigDecimal calculateLowerQuartile(@NonNull List<BigDecimal> values) {
        return calculateLowerQuartile(values, null);
    }

    @Override
    public BigDecimal calculateLowerQuartile(@NonNull List<BigDecimal> values, @Nullable Integer scale) {
        return scale(calculatePercentile(25, values), scale);
    }

    @Override
    public BigDecimal calculateAverage(@NonNull List<BigDecimal> values) {
        return calculateAverage(values, null);
    }

    @Override
    public BigDecimal calculateAverage(@NonNull List<BigDecimal> values, @Nullable Integer scale) {
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        double[] tempValues = toDoubleArray(values);
        double average = StatUtils.mean(tempValues);
        return scale(BigDecimal.valueOf(average), scale);
    }

    @Override
    public BigDecimal calculateMaximum(@NonNull List<BigDecimal> values) {
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        double[] tempValues = toDoubleArray(values);
        double max = StatUtils.max(tempValues);
        return BigDecimal.valueOf(max);
    }

    @Override
    public BigDecimal calculateMinimum(@NonNull List<BigDecimal> values) {
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        double[] tempValues = toDoubleArray(values);
        double min = StatUtils.min(tempValues);
        return BigDecimal.valueOf(min);
    }

    /**
     * 处理精度
     *
     * @param value 数值
     * @param scale 精度
     * @return 处理精度后的数值
     * @since 5.240502.0
     */
    private BigDecimal scale(@Nullable BigDecimal value, @Nullable Integer scale) {
        if (Objects.isNull(value)) {
            return null;
        }
        return Objects.nonNull(scale) ? value.setScale(scale, DEFAULT_ROUNDING_MODE) : value;
    }

    /**
     * 转换double数组
     *
     * @param values 值集合
     * @return double数组
     * @since 5.240502.0
     */
    private double[] toDoubleArray(@NonNull List<BigDecimal> values) {
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        double[] tempValues = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            tempValues[i] = values.get(i).doubleValue();
        }
        return tempValues;
    }

    @Override
    public String logTag() {
        return "计算服务";
    }
}
