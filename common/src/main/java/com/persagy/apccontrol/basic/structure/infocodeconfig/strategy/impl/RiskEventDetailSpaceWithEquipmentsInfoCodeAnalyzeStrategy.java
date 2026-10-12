package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 空间异常事件带信息点
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component(RiskEventDetailSpaceWithEquipmentsInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RiskEventDetailSpaceWithEquipmentsInfoCodeAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "riskEventDetailSpaceWithEquipmentsInfoCodeAnalyzeStrategy";

    private final AbstractCommonServer abstractCommonServer;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.RISK_EVENT_DETAIL_SPACE_WITH_EQUIPMENTS;
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
        MultiKeyMap<BaseEnum<?>, Set<String>> runningMonitorTypeAndSpaceIds = new MultiKeyMap<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            RunningMonitorTypeEnum runningMonitorType = aggregate.getTemplateTypeModel().getRunningMonitorType();
            RunningMonitorSecondTypeEnum runningMonitorSecondType =
                    aggregate.getTemplateTypeModel().getRunningMonitorSecondType();
            String spaceId = aggregate.getIdentifier().getObject().getId();
            String classCode = aggregate.getIdentifier().getObject().getClassCode();
            if (!StringUtils.equals(classCode, DtpRwdClassCodeEnum.SPACE.value())) {
                continue;
            }
            if (!aggregate.getConfigModel().analyzeTackValueLogicModel().isExecute()) {
                continue;
            }
            MultiKey<BaseEnum<?>> multiKey = new MultiKey<>(runningMonitorType, runningMonitorSecondType);
            MapExecutor.dynamicPut(runningMonitorTypeAndSpaceIds, multiKey, spaceId, HashSet::new);
        }
        Map<String, List<ExceptionEventBaseInfoVO>> allSpaceIdAndEvents = new HashMap<>();
        if (MapUtils.isNotEmpty(runningMonitorTypeAndSpaceIds)) {
            for (Map.Entry<MultiKey<? extends BaseEnum<?>>, Set<String>> entry :
                    runningMonitorTypeAndSpaceIds.entrySet()) {
                RunningMonitorTypeEnum runningMonitorType = (RunningMonitorTypeEnum) entry.getKey().getKey(0);
                RunningMonitorSecondTypeEnum runningMonitorSecondType = (RunningMonitorSecondTypeEnum) entry.getKey().getKey(1);
                Set<String> spaceIds = entry.getValue();

                // 所有对象ID
                Set<String> allObjectIds = new HashSet<>(spaceIds);

                // 查询空间下设备
                RunningMonitorStrategy runningMonitorStrategy = runningMonitorType.getRunningMonitorStrategy();
                RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
                equipmentQueryParam.setRunningMonitorSecondType(runningMonitorSecondType);
                equipmentQueryParam.setFilterEquipmentInSpaceIds(spaceIds);
                equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
                List<? extends DtpRwdEquipmentDTO> equipments = runningMonitorStrategy.listEquipments(equipmentQueryParam);
                // 空间ID -> 空间下设备ID
                Map<String, Set<String>> spaceIdAndEquipmentIds = Collections.emptyMap();
                if (CollectionUtils.isNotEmpty(equipments)) {
                    allObjectIds.addAll(equipments.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet()));
                    spaceIdAndEquipmentIds = DtpRwdEquipmentDTO.mapRelObjIdAndIds(equipments);
                }

                // 查询异常事件
                Map<String, List<ExceptionEventBaseInfoVO>> instanceIdAndEvents = abstractCommonServer
                        .mapRiskEvent(allObjectIds, runningMonitorStrategy.exceptionFilterRiskMeasureAimLevel2());
                if (MapUtils.isNotEmpty(instanceIdAndEvents)) {
                    for (String spaceId : spaceIds) {
                        // 添加空间的异常事件
                        List<ExceptionEventBaseInfoVO> spaceEvents = instanceIdAndEvents.get(spaceId);
                        if (CollectionUtils.isNotEmpty(spaceEvents)) {
                            MapExecutor.dynamicPutAll(allSpaceIdAndEvents, spaceId,
                                    instanceIdAndEvents.get(spaceId), ArrayList::new);
                        }
                        // 添加空间下设备的异常事件
                        Set<String> equipmentIds = spaceIdAndEquipmentIds.get(spaceId);
                        if (CollectionUtils.isNotEmpty(equipmentIds)) {
                            for (String equipmentId : equipmentIds) {
                                List<ExceptionEventBaseInfoVO> equipmentEvents = instanceIdAndEvents.get(equipmentId);
                                if (CollectionUtils.isNotEmpty(equipmentEvents)) {
                                    MapExecutor.dynamicPutAll(allSpaceIdAndEvents, spaceId,
                                            equipmentEvents, ArrayList::new);
                                }
                            }
                        }
                    }
                }
            }
        }
        fillAggregateDataValueByMap(aggregates, allSpaceIdAndEvents);
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-空间&设备异常事件详情";
    }
}
