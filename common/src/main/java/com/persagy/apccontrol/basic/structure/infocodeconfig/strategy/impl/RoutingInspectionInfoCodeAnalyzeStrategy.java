package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.outside.dto.riskevent.TaskInfoDTO;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 设备巡检信息世界信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component(RoutingInspectionInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RoutingInspectionInfoCodeAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "equipmentRoutingInspectionInfoCodeAnalyzeStrategy";

    private final AbstractCommonServer abstractCommonServer;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.ROUTING_INSPECTION;
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
        List<String> objectIds = aggregates.stream()
                .map(item -> item.getIdentifier().getObject().getId())
                .collect(Collectors.toList());
        Map<String, TaskInfoDTO> objectIdAndTaskInfos = abstractCommonServer.mapTaskInfo(objectIds);
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();

            TaskInfoDTO taskInfos = Optional.ofNullable(objectIdAndTaskInfos.get(objectId)).orElseGet(TaskInfoDTO::new);

            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, taskInfos, null, null));
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-巡检信息";
    }
}
