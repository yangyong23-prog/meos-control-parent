package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

/**
 * 需求目标信息点基础解析策略
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
public abstract class BaseDemandTargetInfoCodeAnalyzeStrategy extends BaseInfoCodeAnalyzeStrategy {

    @Override
    public void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        fillAggregateDefineValueByKnowledge(aggregates, additionalValues);
    }
}
