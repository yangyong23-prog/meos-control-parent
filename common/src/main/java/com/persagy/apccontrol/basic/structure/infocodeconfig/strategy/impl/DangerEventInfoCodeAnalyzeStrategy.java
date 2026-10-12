package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 风险事件世界信息点解析策略
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Component(DangerEventInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DangerEventInfoCodeAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "dangerEventInfoCodeAnalyzeStrategy";

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.DANGER_EVENT;
    }

    @Override
    public void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-风险事件";
    }
}
