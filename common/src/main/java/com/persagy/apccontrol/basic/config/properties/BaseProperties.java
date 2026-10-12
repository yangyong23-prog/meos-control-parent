package com.persagy.apccontrol.basic.config.properties;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基础配置
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
public abstract class BaseProperties {

    // ------------------------- 数字相关 -------------------------

    /**
     * 是否为负数
     *
     * @param source 数据源
     * @return 是否为负数
     * @since v5.7.0
     */
    protected boolean isNegative(@Nullable BigDecimal source) {
        return Objects.nonNull(source) && source.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 是否为有效的小时
     *
     * @param source 数据源
     * @return 是否为有效的小时
     * @since v5.7.0
     */
    protected boolean isValidHourOnDay(@Nullable BigDecimal source) {
        return Objects.nonNull(source) && ValueRange.of(0, 24).isValidValue(source.longValue());
    }

    // ------------------------- 时间相关 -------------------------

    /**
     * 格式化时间
     *
     * @param timeRange    时间范围
     * @param timeFunction 解析时间函数式接口
     * @param <T>          时间类型
     * @return 格式化后的时间
     * @since v5.7.0
     */
    protected <T extends Comparable<T>> List<Range<T>> formatTimeRange(
            @Nullable List<String> timeRange, @NonNull Function<String, T> timeFunction) {
        if (CollectionUtils.isEmpty(timeRange)) {
            return null;
        }
        return timeRange.stream()
                .filter(item -> StringUtils.isNotBlank(StringUtils.trimToNull(item)))
                .map(item -> {
                    String[] split = item.split("-");
                    return Range.between(timeFunction.apply(StringUtils.trimToNull(split[0])),
                            timeFunction.apply(StringUtils.trimToNull(split[1])));
                }).collect(Collectors.toList());
    }
}
