package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备基础信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Component(BasicInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BasicInfoCodeAnalyzeStrategy
        extends BaseIotInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy, LogComponent {

    public static final String BEAN_NAME = "equipmentBasicInfoCodeAnalyzeStrategy";

    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.BASIC;
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        Map<String, Set<String>> objectIdAndQueryColumns = aggregates.stream().collect(Collectors.groupingBy(item ->
                item.getIdentifier().getObject().getId(), Collectors.mapping(item ->
                item.getConfigModel().getInfoCode(), Collectors.toSet())));
        //noinspection DuplicatedCode
        Map<String, DtpRwdObjectDTO> objectIdAndData = Collections.emptyMap();
        if (MapUtils.isNotEmpty(objectIdAndQueryColumns)) {
            DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
            queryParam.setId(objectIdAndQueryColumns.keySet());
            queryParam.setWithColumns(objectIdAndQueryColumns.values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet()));
            objectIdAndData = DtpRwdObjectDTO.mapIdAndData(dtpRwdClientService.listObject(queryParam));
        }
        for (InfoCodeAggregate aggregate : aggregates) {
            DtpRwdObjectDTO object = aggregate.getIdentifier().getObject();
            String infoCode = aggregate.getConfigModel().getInfoCode();

            // 优先从对象中获取数据
            object = objectIdAndData.getOrDefault(object.getId(), object);

            Object dataValue = object.analyzeExpressionByRwdObject(infoCode, Object.class);
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, dataValue, null, null));
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-基础信息点";
    }
}
