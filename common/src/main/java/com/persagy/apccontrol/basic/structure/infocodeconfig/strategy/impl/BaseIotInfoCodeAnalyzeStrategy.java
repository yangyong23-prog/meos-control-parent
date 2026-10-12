package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.structure.infocodeconfig.context.InfoCodeIotDataContext;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基础设备IOT信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public abstract class BaseIotInfoCodeAnalyzeStrategy extends BaseInfoCodeAnalyzeStrategy {

    @Override
    public void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        fillAggregateDefineValueByKnowledge(aggregates, additionalValues);
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates) || MapUtils.isEmpty(additionalValues)) {
            return;
        }
        InfoCodeIotDataContext context = analyzeAdditionalValues(additionalValues, ADDITIONAL_KEY_IOT_DATA_CONTEXT);
        // 填充聚合根信息点值模型
        if (Objects.nonNull(context)) {
            context.fillAggregateInfoCodeDataModel(aggregates);
        }
    }
}
