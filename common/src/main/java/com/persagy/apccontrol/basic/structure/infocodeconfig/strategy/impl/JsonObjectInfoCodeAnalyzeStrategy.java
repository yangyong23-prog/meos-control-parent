package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * JSON信息点解析策略
 *
 * @author JiYinchuan
 * @since 7.250902.0
 */
@Component(JsonObjectInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JsonObjectInfoCodeAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "jsonObjectInfoCodeAnalyzeStrategy";

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.JSON_OBJECT;
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        for (InfoCodeAggregate aggregate : aggregates) {
            String infoCode = aggregate.getConfigModel().getInfoCode();
            JsonNode tackValueLogic = aggregate.getConfigModel().getTackValueLogic();
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, tackValueLogic, null, null));
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-JSON对象";
    }
}
