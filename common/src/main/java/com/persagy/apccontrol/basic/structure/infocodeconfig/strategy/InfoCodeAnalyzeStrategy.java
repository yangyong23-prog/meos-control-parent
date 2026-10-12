package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy;

import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import org.apache.commons.lang3.Range;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * 信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public interface InfoCodeAnalyzeStrategy extends BizComponent<InfoCodeAnalyzeStrategy> {

    /**
     * 获取信息点类型
     *
     * @return 信息点类型
     * @since 5.240602.0
     */
    InfoCodeTypeEnum infoCodeType();

    /**
     * 填充信息点定义模型
     *
     * @param aggregates       聚合根
     * @param additionalValues 拓展值
     * @see BaseInfoCodeAnalyzeStrategy#analyzeAdditionalValues(Map, String, Supplier) 解析 {@code additionalValues} 方法
     * @since 5.240602.0
     */
    void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues);

    /**
     * 填充信息点值模型
     *
     * @param aggregates       聚合根
     * @param additionalValues 拓展值
     * @see BaseInfoCodeAnalyzeStrategy#analyzeAdditionalValues(Map, String, Supplier) 解析 {@code additionalValues} 方法
     * @since 5.240602.0
     */
    void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues);

    /**
     * 填充拓展值时间范围
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 拓展值
     * @since 7.250403.0
     */
    static @NonNull Map<@NonNull String, @NonNull Object> fillAdditionalValuesTimeRange(
            @Nullable Map<@NonNull String, @NonNull Object> additionalValues,
            @Nullable LocalDateTime startTime, @Nullable LocalDateTime endTime) {
        additionalValues = Optional.ofNullable(additionalValues).orElseGet(HashMap::new);
        if (Objects.nonNull(startTime) && Objects.nonNull(endTime)) {
            additionalValues.put(BaseInfoCodeAnalyzeStrategy.TIME_RANGE, Range.between(startTime, endTime));
        }
        return additionalValues;
    }
}
