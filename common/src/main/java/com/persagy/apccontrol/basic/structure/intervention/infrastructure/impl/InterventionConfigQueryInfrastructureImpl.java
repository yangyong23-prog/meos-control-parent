package com.persagy.apccontrol.basic.structure.intervention.infrastructure.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.intervention.InterventionConfigQueryResourceService;
import com.persagy.apccontrol.basic.resource.intervention.param.InterventionConfigQueryDaoParam;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.common.model.valueobject.DateTimeRangeValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.intervention.context.InterventionConfigModelQueryContext;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionStatusEnum;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.InterventionConfigQueryInfrastructure;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.param.InterventionConfigQueryParam;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 干预需求配置基础设施服务
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InterventionConfigQueryInfrastructureImpl<T extends InterventionConfigData>
        implements InterventionConfigQueryInfrastructure<T>, BizComponent<InterventionConfigQueryInfrastructure<T>> {

    private final InterventionConfigQueryResourceService interventionConfigQueryResourceService;
    private final InstanceService instanceService;
    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public Optional<InterventionConfigModel<T>> of(@NonNull Long id) {
        InterventionConfigQueryDaoParam queryDaoParam = new InterventionConfigQueryDaoParam();
        queryDaoParam.setId(id);
        List<InterventionConfig> configs = interventionConfigQueryResourceService.list(queryDaoParam);
        if (CollectionUtils.isEmpty(configs)) {
            return Optional.empty();
        }
        InterventionConfig config = IterableUtils.first(configs);
        RunningMonitorSecondTypeEnum runningMonitorSecondType = config.getRunningMonitorSecondType();
        DemandInstanceValueObject instance = new DemandInstanceValueObject(
                config.getInstanceType(), config.getInstanceId());
        InterventionConfigModelQueryContext<T> context = new InterventionConfigModelQueryContext<>(
                runningMonitorSecondType, instance, configs, config.toDateTimeRange(), config.toInterventionStatusEnum());
        fillContextProcess(context);
        List<InterventionConfigModel<T>> configModels = context.toInterventionConfigModels();
        if (CollectionUtils.isEmpty(configModels)) {
            return Optional.empty();
        }
        return Optional.of(IterableUtils.first(configModels));
    }

    @Override
    public List<InterventionConfigModel<T>> of(@NonNull InterventionConfigQueryParam param) {
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        DemandInstanceTypeEnum instanceType = param.getInstanceType();
        Collection<String> filterInstanceIds = param.getFilterInstanceIds();
        DateTimeRangeValueObject dateTimeRange = param.getDateTimeContainRange();
        InterventionStatusEnum interventionStatus = param.getInterventionStatus();

        InterventionConfigQueryDaoParam queryDaoParam = new InterventionConfigQueryDaoParam();
        queryDaoParam.setRunningMonitorSecondType(runningMonitorSecondType);
        queryDaoParam.setInstanceType(instanceType);
        queryDaoParam.setTimeContainRange(Pair.of(dateTimeRange.getBeginDateTime(), dateTimeRange.getEndDateTime()));
        if (CollectionUtils.isEmpty(filterInstanceIds)) {
            return Lists.newArrayList();
        }
        queryDaoParam.setInstanceIds(filterInstanceIds);
        if (Objects.nonNull(interventionStatus)) {
            queryDaoParam.setInterventionStatus(interventionStatus);
        }
        List<InterventionConfig> configs = interventionConfigQueryResourceService.list(queryDaoParam);
        Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds =
                mapInstanceTypeAndIds(instanceType, filterInstanceIds);
        InterventionConfigModelQueryContext<T> context = new InterventionConfigModelQueryContext<>(
                runningMonitorSecondType, instanceTypeAndIds, configs, dateTimeRange, interventionStatus);
        fillContextProcess(context);
        return context.toInterventionConfigModels();
    }

    /**
     * 查询实例类型和实例ID映射
     *
     * @param instanceType      实例类型
     * @param filterInstanceIds 过滤实例ID, 为空时查询类型下所有对象
     * @return 实例类型 -> 实例ID
     * @since 5.240702.0
     */
    private Map<DemandInstanceTypeEnum, Collection<String>> mapInstanceTypeAndIds(
            @NonNull DemandInstanceTypeEnum instanceType,
            @Nullable Collection<String> filterInstanceIds) {
        Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds = new HashMap<>();
        if (CollectionUtils.isNotEmpty(filterInstanceIds)) {
            MapExecutor.dynamicPutAll(instanceTypeAndIds, instanceType, filterInstanceIds, HashSet::new);
            return instanceTypeAndIds;
        }
        String projectId = ContextUtil.getProjectId();
        switch (instanceType) {
            case PROJECT:
                MapExecutor.dynamicPut(instanceTypeAndIds, instanceType, projectId, HashSet::new);
                break;
            case BUILDING:
                List<String> buildingIds = dtpRwdClientService.listBuilding(false, false, projectId,
                                new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
                MapExecutor.dynamicPutAll(instanceTypeAndIds, instanceType, buildingIds, HashSet::new);
                break;
            case FLOOR:
                List<String> floorIds = dtpRwdClientService.listFloor(false, false, projectId,
                                new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
                MapExecutor.dynamicPutAll(instanceTypeAndIds, instanceType, floorIds, HashSet::new);
                break;
            case SPACE:
                List<String> spaceIds = dtpRwdClientService.listSpace(false, false, projectId,
                                new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
                MapExecutor.dynamicPutAll(instanceTypeAndIds, instanceType, spaceIds, HashSet::new);
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(instanceType.getClass().getSimpleName(), instanceType);
        }
        return instanceTypeAndIds;
    }

    /**
     * 填充上下文过程
     *
     * @param context 上下文
     * @since 5.240702.0
     */
    private void fillContextProcess(@NonNull InterventionConfigModelQueryContext<T> context) {
        // 填充实例ID和父级实例ID
        context.fillInstanceIdAndParents(instanceService);
        // 填充父级配置
        context.fillParentConfig(interventionConfigQueryResourceService);
        // 填充所有实例ID和本地名称映射
        context.fillAllInstanceIdAndLocalName(dtpRwdClientService);
    }

    @Override
    public String logTag() {
        return "干预配置模型-基础设施";
    }
}
