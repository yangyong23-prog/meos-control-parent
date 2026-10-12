package com.persagy.apccontrol.basic.structure.intervention.context;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.intervention.InterventionConfigQueryResourceService;
import com.persagy.apccontrol.basic.resource.intervention.param.InterventionConfigQueryDaoParam;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.common.model.valueobject.DateTimeRangeValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionStatusEnum;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 干预配置模型查询上下文
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Data
@Accessors(chain = true)
public class InterventionConfigModelQueryContext<T extends InterventionConfigData> {

    // ------------------------- 入参 -------------------------

    /**
     * 运行监测二级类型
     */
    private final RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 实例类型 -> 实例ID
     */
    private final Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds;

    /**
     * 配置信息
     */
    private final List<InterventionConfig> configs;

    /**
     * 时间范围
     */
    private final DateTimeRangeValueObject dateTimeRange;

    /**
     * 干预类型
     */
    private final InterventionStatusEnum interventionStatus;

    // ------------------------- 过程 -------------------------

    /**
     * 实例ID -> 上级实例
     */
    private Map<String, List<DemandInstanceValueObject>> instanceIdAndParents = Collections.emptyMap();

    /**
     * 上级配置
     */
    private List<InterventionConfig> parentConfigs = Collections.emptyList();

    /**
     * 所有实例ID -> 本地名称
     */
    private Map<String, String> allInstanceIdAndLocalName = Collections.emptyMap();

    // ------------------------- 出参 -------------------------


    // ------------------------- 行为 -------------------------

    /**
     * 构造器
     *
     * @param runningMonitorSecondType 运行监测二级类型
     * @param instanceTypeAndIds       实例类型 -> 实例ID
     * @param configs                  配置信息
     * @param dateTimeRange            时间范围
     * @param interventionStatus       干预类型
     * @since 5.240702.0
     */
    public InterventionConfigModelQueryContext(
            RunningMonitorSecondTypeEnum runningMonitorSecondType,
            Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds,
            List<InterventionConfig> configs,
            DateTimeRangeValueObject dateTimeRange,
            InterventionStatusEnum interventionStatus) {
        this.runningMonitorSecondType = runningMonitorSecondType;
        this.instanceTypeAndIds = instanceTypeAndIds;
        this.configs = configs;
        this.dateTimeRange = dateTimeRange;
        this.interventionStatus = interventionStatus;
    }

    /**
     * 构造器
     *
     * @param runningMonitorSecondType 运行监测二级类型
     * @param instance                 实例
     * @param configs                  配置信息
     * @param dateTimeRange            时间范围
     * @param interventionStatus       干预类型
     * @since 5.240702.0
     */
    public InterventionConfigModelQueryContext(
            RunningMonitorSecondTypeEnum runningMonitorSecondType,
            DemandInstanceValueObject instance,
            List<InterventionConfig> configs,
            DateTimeRangeValueObject dateTimeRange,
            InterventionStatusEnum interventionStatus) {
        this.runningMonitorSecondType = runningMonitorSecondType;
        this.configs = configs;
        this.dateTimeRange = dateTimeRange;
        Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds = new HashMap<>();
        MapExecutor.dynamicPut(instanceTypeAndIds, instance.getInstanceType(), instance.getInstanceId(), HashSet::new);
        this.instanceTypeAndIds = instanceTypeAndIds;
        this.interventionStatus = interventionStatus;
    }

    /**
     * 填充实例ID和上级实例映射
     *
     * @param instanceService 实例服务
     * @since 5.240702.0
     */
    public void fillInstanceIdAndParents(@NonNull InstanceService instanceService) {
        if (MapUtils.isEmpty(instanceTypeAndIds)) {
            return;
        }
        this.instanceIdAndParents = instanceService.mapInstanceIdAndAllParents(instanceTypeAndIds);
    }

    /**
     * 填充上级配置
     *
     * @param interventionConfigQueryResourceService 干预配置资源服务
     * @since 5.240702.0
     */
    public void fillParentConfig(@NonNull InterventionConfigQueryResourceService interventionConfigQueryResourceService) {
        if (MapUtils.isEmpty(instanceIdAndParents)) {
            return;
        }
        // 所有上级实例ID
        Set<String> allParentInstanceIds = instanceIdAndParents.values().stream()
                .flatMap(Collection::stream)
                .map(DemandInstanceValueObject::getInstanceId)
                .collect(Collectors.toSet());
        InterventionConfigQueryDaoParam queryDaoParam = new InterventionConfigQueryDaoParam();
        queryDaoParam.setRunningMonitorSecondType(runningMonitorSecondType);
        queryDaoParam.setInstanceIds(allParentInstanceIds);
        queryDaoParam.setTimeContainRange(Pair.of(dateTimeRange.getBeginDateTime(), dateTimeRange.getEndDateTime()));
        queryDaoParam.setInterventionStatus(interventionStatus);
        List<InterventionConfig> dbParentConfigs = interventionConfigQueryResourceService.list(queryDaoParam);
        if (CollectionUtils.isEmpty(dbParentConfigs)) {
            return;
        }
        this.parentConfigs = dbParentConfigs;
    }

    public List<InterventionConfigModel<T>> toInterventionConfigModels() {
        if (CollectionUtils.isEmpty(configs) && CollectionUtils.isEmpty(parentConfigs)) {
            return Lists.newArrayList();
        }
        Map<String, List<InterventionConfig>> instanceIdAndConfig = configs.stream()
                .collect(Collectors.groupingBy(InterventionConfig::getInstanceId));
        Map<String, List<InterventionConfig>> parentInstanceIdAndConfig = parentConfigs.stream()
                .collect(Collectors.groupingBy(InterventionConfig::getInstanceId));

        List<InterventionConfigModel<T>> result = new ArrayList<>();
        for (Map.Entry<DemandInstanceTypeEnum, Collection<String>> entry : instanceTypeAndIds.entrySet()) {
            DemandInstanceTypeEnum instanceType = entry.getKey();
            Collection<String> instanceIds = entry.getValue();
            for (String instanceId : instanceIds) {
                String instanceName = allInstanceIdAndLocalName.get(instanceId);

                DemandInstanceValueObject instance = new DemandInstanceValueObject(instanceType, instanceId, instanceName);
                List<InterventionConfigModel<T>> configModels = instanceIdAndConfig.getOrDefault(instanceId, Collections.emptyList()).stream()
                        .map(item -> InterventionConfigModel.<T>of(item, instanceName))
                        .sorted(Comparator.comparing(item -> item.getDateTimeRange().getBeginDateTime()))
                        .collect(Collectors.toList());
                Set<Long> allSelfConfigIds = configModels.stream()
                        .map(item -> item.getIdentifier().getInterventionConfigId())
                        .collect(Collectors.toSet());

                // 当查询对象没有上级时, 直接使用当前配置
                List<DemandInstanceValueObject> parentInstances = instanceIdAndParents.get(instanceId);
                if (CollectionUtils.isEmpty(parentInstances)) {
                    result.addAll(configModels);
                    continue;
                }
                // 当查询对象存在上级时, 进行继承处理
                for (DemandInstanceValueObject parentInstance : parentInstances) {
                    String parentInstanceId = parentInstance.getInstanceId();
                    List<InterventionConfig> parentConfigs = parentInstanceIdAndConfig.getOrDefault(
                            parentInstanceId, Collections.emptyList());
                    // 当上级配置不存在跳过, 继续往上找上级
                    if (CollectionUtils.isEmpty(parentConfigs)) {
                        continue;
                    }
                    parentConfigs.sort(Comparator.comparing(InterventionConfig::getBeginTime));
                    mergeConfig(configModels, instance, allSelfConfigIds, parentConfigs);
                }
                result.addAll(configModels);
            }
        }
        result.sort(Comparator.comparing(item -> item.getDateTimeRange().getBeginDateTime()));
        return result;
    }

    /**
     * 填充实例ID和本地名称映射
     *
     * @param dtpRwdClientService 物理世界服务
     * @since 5.240702.0
     */
    public void fillAllInstanceIdAndLocalName(@NonNull DtpRwdClientService dtpRwdClientService) {
        if (CollectionUtils.isEmpty(configs) && CollectionUtils.isEmpty(parentConfigs)) {
            return;
        }
        // 所有实例ID
        Set<String> allInstanceIds = configs.stream().map(InterventionConfig::getInstanceId).collect(Collectors.toSet());
        Set<String> allParentInstanceIds = parentConfigs.stream().map(InterventionConfig::getInstanceId).collect(Collectors.toSet());
        this.allInstanceIdAndLocalName = dtpRwdClientService.mapObjectIdAndLocalName(true, SetUtils.union(allInstanceIds, allParentInstanceIds));
    }

    private List<InterventionConfigModel<T>> mergeConfig(
            @NonNull List<InterventionConfigModel<T>> target,
            @NonNull DemandInstanceValueObject instance,
            @NonNull Set<Long> allSelfConfigIds,
            @NonNull List<InterventionConfig> parentConfigs) {
        TemporalUnit temporalUnit = InterventionConfigModel.DATE_TIME_TYPE.convertTemporalUnit();
        for (InterventionConfig parentConfig : parentConfigs) {
            LocalDateTime parentBeginTime = parentConfig.getBeginTime();
            LocalDateTime parentEndTime = parentConfig.getEndTime();
            String parentInstanceName = allInstanceIdAndLocalName.get(parentConfig.getInstanceId());
            DateTimeRangeValueObject parentTimeRange = new DateTimeRangeValueObject(
                    parentBeginTime, parentEndTime);
            Range<ChronoLocalDateTime<?>> parentRange = new DateTimeRangeValueObject(
                    parentBeginTime, parentEndTime).toRange(temporalUnit);
            boolean notHandledParentConfig = true;
            List<InterventionConfigModel<T>> tempResult = new ArrayList<>();
            for (int i = 0; i < target.size(); i++) {
                InterventionConfigModel<T> configModel = target.get(i);
                Long interventionConfigId = configModel.getIdentifier().getInterventionConfigId();
                T selfConfig = allSelfConfigIds.contains(interventionConfigId)
                        ? configModel.getConfig().getSelfConfig() : null;
                DateTimeRangeValueObject selfTimeRange = configModel.getDateTimeRange();
                Range<ChronoLocalDateTime<?>> selfRange = selfTimeRange.toRange(temporalUnit);
                InterventionConfigModel<T> previousConfigModel = (i > 0) ? target.get(i - 1) : null;
                InterventionConfigModel<T> nextConfigModel = (i < target.size() - 1) ? target.get(i + 1) : null;
                // 自身时段全量覆盖上级时段直接跳过
                if (selfRange.containsRange(parentRange)) {
                    notHandledParentConfig = false;
                    continue;
                }
                // 上级时段全量覆盖自身时段, 进行前后填充
                if (parentRange.containsRange(selfRange)) {
                    // 处理前一段
                    DateTimeRangeValueObject newParentTimeRange = handleFrontTimeRange(
                            parentTimeRange, selfTimeRange, previousConfigModel);
                    if (Objects.nonNull(newParentTimeRange)) {
                        tempResult.add(InterventionConfigModel.ofParent(instance, selfConfig,
                                parentConfig, parentInstanceName, newParentTimeRange));
                    }
                    // 处理后一段
                    newParentTimeRange = handleBehindTimeRange(parentTimeRange, selfTimeRange, nextConfigModel);
                    if (Objects.nonNull(newParentTimeRange)) {
                        tempResult.add(InterventionConfigModel.ofParent(instance, selfConfig,
                                parentConfig, parentInstanceName, newParentTimeRange));
                    }
                    notHandledParentConfig = false;
                    continue;
                }
                if (parentRange.isOverlappedBy(selfRange)) {
                    // 处理前半段重叠
                    if (selfTimeRange.getBeginDateTime().isBefore(parentBeginTime)) {
                        DateTimeRangeValueObject newParentTimeRange = handleBehindTimeRange(parentTimeRange, selfTimeRange, nextConfigModel);
                        if (Objects.nonNull(newParentTimeRange)) {
                            tempResult.add(InterventionConfigModel.ofParent(instance, selfConfig,
                                    parentConfig, parentInstanceName, newParentTimeRange));
                            continue;
                        }
                    }
                    // 处理后半段重叠
                    if (parentBeginTime.isBefore(selfTimeRange.getBeginDateTime())) {
                        DateTimeRangeValueObject newParentTimeRange = handleFrontTimeRange(parentTimeRange, selfTimeRange, previousConfigModel);
                        if (Objects.nonNull(newParentTimeRange)) {
                            tempResult.add(InterventionConfigModel.ofParent(instance, selfConfig,
                                    parentConfig, parentInstanceName, newParentTimeRange));
                        }
                    }
                }
            }
            if (notHandledParentConfig) {
                tempResult.add(InterventionConfigModel.ofParent(instance, null,
                        parentConfig, parentInstanceName, null));
            }
            if (CollectionUtils.isNotEmpty(tempResult)) {
                target.addAll(tempResult);
                target.sort(Comparator.comparing(item -> item.getDateTimeRange().getBeginDateTime()));
            }
        }
        return target;
    }

    @Nullable
    private DateTimeRangeValueObject handleFrontTimeRange(
            @NonNull DateTimeRangeValueObject parentTimeRange,
            @NonNull DateTimeRangeValueObject selfTimeRange,
            @Nullable InterventionConfigModel<T> previousConfigModel) {
        DateTimeRangeValueObject newParentTimeRange = new DateTimeRangeValueObject(
                parentTimeRange.getBeginDateTime(), selfTimeRange.getBeginDateTime());
        if (Objects.nonNull(previousConfigModel)) {
            LocalDateTime previousEndTime = previousConfigModel.getDateTimeRange().getEndDateTime();
            if (previousEndTime.isAfter(newParentTimeRange.getBeginDateTime())) {
                newParentTimeRange.setBeginDateTime(previousEndTime);
            }
        }
        if (newParentTimeRange.getBeginDateTime().isEqual(newParentTimeRange.getEndDateTime())) {
            return null;
        }
        return newParentTimeRange;
    }

    @Nullable
    private DateTimeRangeValueObject handleBehindTimeRange(
            @NonNull DateTimeRangeValueObject parentTimeRange,
            @NonNull DateTimeRangeValueObject selfTimeRange,
            @Nullable InterventionConfigModel<T> nextConfigModel) {
        DateTimeRangeValueObject newParentTimeRange = new DateTimeRangeValueObject(
                selfTimeRange.getEndDateTime(), parentTimeRange.getEndDateTime());
        if (Objects.nonNull(nextConfigModel)) {
            LocalDateTime nextBeginTime = nextConfigModel.getDateTimeRange().getBeginDateTime();
            if (nextBeginTime.isBefore(newParentTimeRange.getEndDateTime())) {
                newParentTimeRange.setEndDateTime(nextBeginTime);
            }
        }
        if (newParentTimeRange.getBeginDateTime().isEqual(newParentTimeRange.getEndDateTime())) {
            return null;
        }
        return newParentTimeRange;
    }
}
