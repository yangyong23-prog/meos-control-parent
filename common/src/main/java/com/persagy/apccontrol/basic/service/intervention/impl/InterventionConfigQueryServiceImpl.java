package com.persagy.apccontrol.basic.service.intervention.impl;

import cn.hutool.core.collection.CollUtil;
import com.persagy.apccontrol.basic.convert.InterventionConvert;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.context.intervention.InterventionInfoCodeConfigQueryContext;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionConfigAddDto;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionInfoQueryListParamDto;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionQueryTimeSlotDto;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSceneDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLighting;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.entity.intervention.TimeSlotEntity;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.demand.SceneTypeEnum;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionInfoQueryParam;
import com.persagy.apccontrol.basic.model.param.rwd.TreeBuildingFloorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.intervention.*;
import com.persagy.apccontrol.basic.resource.intervention.InterventionConfigQueryResourceService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandLightingQueryService;
import com.persagy.apccontrol.basic.service.intervention.InterventionConfigQueryService;
import com.persagy.apccontrol.basic.structure.common.model.valueobject.DateTimeRangeValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionStatusEnum;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.InterventionConfigQueryInfrastructure;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.param.InterventionConfigQueryParam;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yuecaipu
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InterventionConfigQueryServiceImpl<T extends InterventionConfigData> implements InterventionConfigQueryService<T>, LogComponent {


    private final InterventionConvert convert;
    private final InterventionConfigQueryResourceService interventionConfigQueryResourceService;
    private final InterventionConfigQueryInfrastructure<T> interventionConfigQueryInfrastructure;
    private final DemandLightingStrategy demandLightingStrategy;
    private final DemandLightingQueryService demandLightingQueryService;
    private final InstanceService instanceService;

    @Override
    public List<InterventionQueryListVo<T>> queryInterventionConfig(@NotNull InterventionInfoQueryListParamDto paramDto) {
        InterventionInfoCodeConfigQueryContext context = new InterventionInfoCodeConfigQueryContext();
        context.setQueryParam(convert.queryParamToEntity(paramDto));
        InterventionConfigQueryParam queryParam = convert.paramToConfigQueryParam(paramDto);
        DateTimeRangeValueObject dateTimeRange = new DateTimeRangeValueObject(paramDto.getBeginDateTime(), paramDto.getEndDateTime());
        queryParam.setDateTimeContainRange(dateTimeRange);
        List<InterventionConfigModel<T>> interventionConfigModels = interventionConfigQueryInfrastructure.of(queryParam);

        if (CollectionUtils.isEmpty(interventionConfigModels)) {
            return new ArrayList<>();
        }
        //生效中的配置
        Map<String, List<InterventionConfigVo<T>>> instanceEffectConfigMap = new HashMap<>();

        //当前生效的配置
        Map<String, List<InterventionConfigVo<T>>> instanceNowConfigMap = new HashMap<>();

        //完成的配置
        Map<String, List<InterventionConfigVo<T>>> instanceCompleteConfigMap = new HashMap<>();

        interventionConfigModels.forEach(config -> {
            String instanceId = config.getInstance().getInstanceId();
            InterventionConfigVo<T> configVo = new InterventionConfigVo<>();
            configVo.setId(config.getIdentifier().getInterventionConfigId());
            configVo.setConfigData((config.getConfig().getFinalConfig()));
            configVo.setReason(config.getInterventionReason());
            configVo.setBeginTime(config.getDateTimeRange().getBeginDateTime());
            configVo.setEndTime(config.getDateTimeRange().getEndDateTime());

            List<InterventionConfigInheritDescVo> inherit = new ArrayList<>();
            List<DemandInstanceValueObject> inheritedInstanceMetaInfo = config.getConfig().getInheritInstances();
            if (!CollectionUtils.isEmpty(inheritedInstanceMetaInfo)) {
                inheritedInstanceMetaInfo.forEach(metaInfo -> {
                    InterventionConfigInheritDescVo descVo = new InterventionConfigInheritDescVo();
                    descVo.setInstanceId(metaInfo.getInstanceId());
                    descVo.setInstanceName(metaInfo.getInstanceName());
                    descVo.setInstanceType(metaInfo.getInstanceType());
                    inherit.add(descVo);
                });
            }
            configVo.setInherit(inherit);
            if(config.isEffectiveByNow()){
                List<InterventionConfigVo<T>> nowEffectConfig = instanceNowConfigMap.computeIfAbsent(instanceId, k -> new ArrayList<>());
                configVo.setStatus(InterventionStatusEnum.EFFECTING);
                nowEffectConfig.add(configVo);
            } else if (config.getDateTimeRange().getEndDateTime().isBefore(DateUtils.now())) {
                List<InterventionConfigVo<T>> completeConfig = instanceCompleteConfigMap.computeIfAbsent(instanceId, k -> new ArrayList<>());
                configVo.setStatus(InterventionStatusEnum.COMPLETED);
                completeConfig.add(configVo);
            }else {
                List<InterventionConfigVo<T>> effectConfigs = instanceEffectConfigMap.computeIfAbsent(instanceId, k -> new ArrayList<>());
                effectConfigs.add(configVo);
            }
        });
        //按照开始时间降序排
        if(!instanceEffectConfigMap.isEmpty()){
            instanceEffectConfigMap.forEach((instanceId,configs)->{
                configs.sort(Comparator.<InterventionConfigVo<T>>comparingInt(it -> it.getBeginTime().getSecond()).reversed());
            });
        }
        if(!instanceNowConfigMap.isEmpty()){
            instanceNowConfigMap.forEach((instanceId,configs)->{
                configs.sort(Comparator.<InterventionConfigVo<T>>comparingInt(it -> it.getBeginTime().getSecond()).reversed());
            });
        }
        if(!instanceCompleteConfigMap.isEmpty()){
            instanceCompleteConfigMap.forEach((instanceId,configs)->{
                configs.sort(Comparator.<InterventionConfigVo<T>>comparingInt(it -> it.getBeginTime().getSecond()).reversed());
            });
        }
        List<InterventionQueryListVo<T>> result = new ArrayList<>();
        paramDto.getInstanceIds().forEach(instanceId->{
            List<InterventionConfigVo<T>> configList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(instanceNowConfigMap.get(instanceId))){
                configList.addAll(instanceNowConfigMap.get(instanceId));
            }
            if(CollectionUtils.isNotEmpty(instanceEffectConfigMap.get(instanceId))){
                configList.addAll(instanceEffectConfigMap.get(instanceId));
            }
            if(CollectionUtils.isNotEmpty(instanceCompleteConfigMap.get(instanceId))){
                configList.addAll(instanceCompleteConfigMap.get(instanceId));
            }
            InterventionQueryListVo<T> vo =new InterventionQueryListVo<>();
            vo.setInstanceId(instanceId);
            vo.setConfigList(configList);
            result.add(vo);
        });
        return result;
    }

    @Override
    public Boolean validatedAddConfigSlot(InterventionConfigAddDto addDto) {
        return interventionConfigQueryResourceService.validatedAddConfigSlot(convert.addConfigDtoToEntity(addDto));
    }

    @Override
    public InterventionConfigTimeSlotVo queryInterventionTimeSlot(InterventionQueryTimeSlotDto timeSlotDto) {
        InterventionInfoCodeConfigQueryContext context = new InterventionInfoCodeConfigQueryContext();
        InterventionInfoQueryParam queryParam = new InterventionInfoQueryParam();
        Set<String> instanceIds = new HashSet<>();
        instanceIds.add(timeSlotDto.getInstanceId());
        queryParam.setInstanceIds(instanceIds);
        queryParam.setBeginDateTime(timeSlotDto.getBeginDateTime());
        queryParam.setEndDateTime(timeSlotDto.getEndDateTime());
        queryParam.setInstanceType(timeSlotDto.getInstanceType());
        queryParam.setRunningMonitorSecondType(timeSlotDto.getRunMonitorSecondType());
        queryParam.setProjectId(ContextUtil.getProjectId());
        context.setQueryParam(queryParam);
        interventionConfigQueryResourceService.queryInterventionConfigList(context);
        InterventionConfigTimeSlotVo vo = new InterventionConfigTimeSlotVo();
        context.sourceTimeSlot();
        context.aggregationTimeSlot();
        List<TimeSlotEntity> sourceList = context.getInstanceTimeSlotMap().get(timeSlotDto.getInstanceId());
        List<InterventionSourceTimeSlotVo> sources = new ArrayList<>();
        if(!CollectionUtils.isEmpty(sourceList)){
            Map<DemandInstanceTypeEnum, List<TimeSlotEntity>> groupedByInstanceType = sourceList.stream()
                    .collect(Collectors.groupingBy(TimeSlotEntity::getInstanceType));
            groupedByInstanceType.forEach((key, value) -> {
                InterventionSourceTimeSlotVo sourceTimeSlotVo = new InterventionSourceTimeSlotVo();
                sourceTimeSlotVo.setInstanceType(key);
                sourceTimeSlotVo.setTimeSlot(value);
                sources.add(sourceTimeSlotVo);
            });
        }
        vo.setSource(sources);
        vo.setAggregation(context.getInstanceAggregationTimeSlotMap().get(timeSlotDto.getInstanceId()));
        List<LightingSceneDTO> lightingSceneDTOList = demandLightingStrategy.listScenesByTime(ContextUtil.getProjectId(),
                timeSlotDto.getRunMonitorSecondType().toDemandTemplateTypeEnum(), DateUtils.now().toLocalDate());
        List<TimeSlotEntity> demandLighting = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(lightingSceneDTOList)){
            lightingSceneDTOList.forEach(dto->{
                TimeSlotEntity entity = new TimeSlotEntity(null,null,null,null);
                entity.setEndDateTime(dto.getEndDateTime());
                entity.setBeginDateTime(dto.getBeginDateTime());
                entity.setConfigId(dto.getSceneId());
                entity.setConfigName(dto.getSceneName());
                demandLighting.add(entity);
            });
        }
        vo.setDemand(demandLighting);
        return vo;
    }


    /**
     * 获取自动需求和人工干预聚合时间段配置
     *
     * @param timeSlotDto 参数
     * @return {@link List<TimeSlotEntity>}
     * @author yuecaipu
     * @date 2024/9/7 18:35
     * @since v5.240901
     */
    @Override
    public List<TimeSlotEntity> queryDemandTimeSlot(InterventionQueryTimeSlotDto timeSlotDto) {

        // =====1、获取自动需求和人工干预聚合时间段配置=====
        InterventionConfigTimeSlotVo timeSlot = queryInterventionTimeSlot(timeSlotDto);
        List<TimeSlotEntity> results = new ArrayList<>();
        if (CollUtil.isNotEmpty(timeSlot.getAggregation())) {
            results.addAll(timeSlot.getAggregation());
        }
        if (CollUtil.isNotEmpty(timeSlot.getDemand())) {
            results.addAll(timeSlot.getDemand());
        }
        if (CollUtil.isEmpty(results)) {
            return results;
        }
        List<TimeSlotEntity> aggregationTimeSlot = new ArrayList<>();
        results.forEach(sourceTime -> {
            List<TimeSlotEntity> addTimeSlot = TimeSlotEntity.calculateNonOverlappingPartsWithExisting(aggregationTimeSlot, sourceTime);
            if (CollectionUtils.isNotEmpty(addTimeSlot)) {
                aggregationTimeSlot.addAll(addTimeSlot);
            }
        });
        aggregationTimeSlot.sort(Comparator.comparing(TimeSlotEntity::getBeginDateTime));

        // =====2、补全空时间区间=====
        // 当日凌晨时间
        LocalDateTime midnight = LocalDateTime.of(aggregationTimeSlot.get(0).getBeginDateTime().toLocalDate(), LocalTime.MIDNIGHT);
        // 次日凌晨时间
        LocalDateTime nextMidnight = midnight.plusDays(1);
        // 补全0点到第一个时间区间的间隔时间
        if (midnight.isBefore(aggregationTimeSlot.get(0).getBeginDateTime())) {
            TimeSlotEntity timeSlotEntity = new TimeSlotEntity(null, null, midnight, aggregationTimeSlot.get(0).getBeginDateTime());
            aggregationTimeSlot.add(0, timeSlotEntity);
        }
        // 补全最后一个时间区间到次日凌晨的间隔时间
        if (nextMidnight.isAfter(aggregationTimeSlot.get(aggregationTimeSlot.size() - 1).getEndDateTime())) {
            TimeSlotEntity timeSlotEntity = new TimeSlotEntity(null, null, aggregationTimeSlot.get(aggregationTimeSlot.size() - 1).getEndDateTime(), nextMidnight);
            aggregationTimeSlot.add(timeSlotEntity);
        }

        // 补全中间空白时间区间
        LocalDateTime beginDateTime = aggregationTimeSlot.get(0).getEndDateTime();
        for (int i = 1; i < aggregationTimeSlot.size(); i++) {
            if (aggregationTimeSlot.get(i).getBeginDateTime().compareTo(beginDateTime) != 0) {
                TimeSlotEntity timeSlotEntity = new TimeSlotEntity(null, null, beginDateTime, aggregationTimeSlot.get(i).getBeginDateTime());
                aggregationTimeSlot.add(i, timeSlotEntity);
            }
            beginDateTime = aggregationTimeSlot.get(i).getEndDateTime();
        }

        return aggregationTimeSlot;
    }

    @Override
    public ConfigDetailsVo getConfigDetails(InterventionQueryParam param) {
        return Objects.requireNonNull(param.getRunningMonitorSecondType()
                .toRunningMonitorTypeEnum()
                .getInterventionConfigAnalyzeStrategy())
                .getConfigDetails(param);
    }

    @Override
    public SpaceStatisticsVo spaceStatistics(InterventionQueryParam param) {
        String projectId = ContextUtil.getProjectId();
        SpaceStatisticsVo spaceStatisticsVo = new SpaceStatisticsVo(0, 0, 0, 0);

        // 查询照明回路
        List<DtpRwdCircuitDTO> circuitList = demandLightingStrategy.listCircuitsByInstance(
                ContextUtil.getProjectId(), param.getRunningMonitorSecondType().toDemandTemplateTypeEnum(), param.getInstanceType(), param.getInstanceId());

        // 查询照明回路id去查询照明回路服务的空间
        Set<String> allSpaceIds = new HashSet<>();
        for (DtpRwdCircuitDTO circuit : circuitList) {
            assert circuit.getRelObjIds() != null;
            allSpaceIds.addAll(circuit.getRelObjIds());
        }
        spaceStatisticsVo.setTotal(allSpaceIds.size());

        // 获取对象实例id和其对应的配置映射，eg：Sp0001:AUTO:Bd001
        Map<String, String> objectId2Config = getInstanceConfigMap(projectId, param.getRunningMonitorSecondType());

        // 统计
        String baseConfig = objectId2Config.get(param.getInstanceId());
        for (String spaceId : allSpaceIds) {
            if (null == objectId2Config.get(spaceId)) {
                continue;
            }
            String config = objectId2Config.get(spaceId);
            if (config.equals(baseConfig)) {
                spaceStatisticsVo.addMatchingCount();
            } else if (config.startsWith(SceneTypeEnum.AUTO.getValue())) {
                spaceStatisticsVo.addAutoCount();
            } else if (config.startsWith(SceneTypeEnum.INTERVENTION.getValue())) {
                spaceStatisticsVo.addInterventionCount();
            }
        }

        return spaceStatisticsVo;
    }

    /**
     * 获取对象实例id和其对应的配置映射，eg：Sp0001:AUTO:Bd001
     *
     * @param projectId
     * @param runningMonitorSecondType 类型
     * @return {@link Map<String,String>}
     * @author yuecaipu
     * @date 2024/7/28 23:47
     * @since
     */
    private Map<String, String> getInstanceConfigMap(String projectId, RunningMonitorSecondTypeEnum runningMonitorSecondType) {
        // 查询建筑楼层空间树
        TreeBuildingFloorSpaceQueryParam treeParam = new TreeBuildingFloorSpaceQueryParam();
        List<DtpRwdBuildingDTO> dtpRwdBuildingList = instanceService.treeBuildingFloorSpace(treeParam);

        // 获取当天的需求配置下的所有场景
        List<String> autoConfigObjectId = getHaveAutoConfigInstanceId(projectId, runningMonitorSecondType.toDemandTemplateTypeEnum());

        // 获取干预配置
        List<String> interventionConfigObjectId = getHaveInterventionConfigInstanceId(projectId, runningMonitorSecondType);
        // 对象id和其对应的配置映射，eg：Sp0001:AUTO:Bd001
        // 干预需求>自动需要 ,自身配置大于引用上级配置
        Map<String, String> objectId2Config = new HashMap<>();
        if (interventionConfigObjectId.contains(projectId)) {
            objectId2Config.put(projectId, SceneTypeEnum.INTERVENTION.getValue() + ":" + projectId);
        } else if (autoConfigObjectId.contains(projectId)) {
            objectId2Config.put(projectId, SceneTypeEnum.AUTO.getValue() + ":" + projectId);
        }
        for (DtpRwdBuildingDTO build : dtpRwdBuildingList) {
            if (interventionConfigObjectId.contains(build.getId())) {
                objectId2Config.put(build.getId(), SceneTypeEnum.INTERVENTION.getValue() + ":" + build.getId());
            } else if (interventionConfigObjectId.contains(projectId)) {
                objectId2Config.put(build.getId(), SceneTypeEnum.INTERVENTION.getValue() + ":" + projectId);
            } else if (autoConfigObjectId.contains(build.getId())) {
                objectId2Config.put(build.getId(), SceneTypeEnum.AUTO.getValue() + ":" + build.getId());
            } else if (autoConfigObjectId.contains(projectId)) {
                objectId2Config.put(build.getId(), SceneTypeEnum.AUTO.getValue() + ":" + projectId);
            }
            if (CollUtil.isEmpty(build.getFloors())) {
                continue;
            }
            for (DtpRwdFloorDTO floor : build.getFloors()) {
                if (interventionConfigObjectId.contains(floor.getId())) {
                    objectId2Config.put(floor.getId(), SceneTypeEnum.INTERVENTION.getValue() + ":" + floor.getId());
                } else if (autoConfigObjectId.contains(floor.getId())
                        && (null == objectId2Config.get(build.getId())
                        || objectId2Config.get(build.getId()).startsWith(SceneTypeEnum.AUTO.getValue()))) {
                    objectId2Config.put(floor.getId(), SceneTypeEnum.AUTO.getValue() + ":" + floor.getId());
                } else {
                    objectId2Config.put(floor.getId(), objectId2Config.get(build.getId()));
                }
                if (CollUtil.isEmpty(floor.getSpaces())) {
                    continue;
                }
                for (DtpRwdSpaceDTO space : floor.getSpaces()) {
                    if (interventionConfigObjectId.contains(space.getId())) {
                        objectId2Config.put(space.getId(), SceneTypeEnum.INTERVENTION.getValue() + ":" + space.getId());
                    } else if (autoConfigObjectId.contains(space.getId())
                            && (null == objectId2Config.get(floor.getId())
                            || objectId2Config.get(floor.getId()).startsWith(SceneTypeEnum.AUTO.getValue()))) {
                        objectId2Config.put(space.getId(), SceneTypeEnum.AUTO.getValue() + ":" + space.getId());
                    } else {
                        objectId2Config.put(space.getId(), objectId2Config.get(floor.getId()));
                    }
                }
            }
        }
        return objectId2Config;
    }

    /**
     * 获取当前时间有配置有干预需求的实例对象id
     *
     * @param projectId
     * @param runningMonitorSecondType
     * @return {@link List<String>}
     * @author yuecaipu
     * @date 2024/7/28 23:45
     * @since
     */
    private List<String> getHaveInterventionConfigInstanceId(String projectId, RunningMonitorSecondTypeEnum runningMonitorSecondType) {
        List<String> interventionConfigObjectId = new ArrayList<>();
        List<InterventionConfig> interventionConfigs = interventionConfigQueryResourceService.listActive(projectId, runningMonitorSecondType);
        for (InterventionConfig interventionConfig : interventionConfigs) {
            interventionConfigObjectId.add(interventionConfig.getInstanceId());
        }
        return interventionConfigObjectId;
    }

    /**
     * 获取当前时间有配置有自动需求的实例对象id
     *
     * @param demandTemplateType
     * @param projectId
     * @return {@link List<String>}
     * @author yuecaipu
     * @date 2024/7/28 23:41
     * @since
     */
    private List<String> getHaveAutoConfigInstanceId(String projectId, DemandTemplateTypeEnum demandTemplateType) {
        // 获取当天的需求配置下的所有场景
        List<LightingSceneDTO> lightingSceneList = demandLightingStrategy.listScenesByTime(projectId, demandTemplateType, DateUtils.now().toLocalDate());
        List<String> autoConfigObjectId = new ArrayList<>();
        for (LightingSceneDTO lightingScene : lightingSceneList) {
            // 获取当前场景所有自动配置
            if (lightingScene.getIsCurrentScene()) {
                List<DemandConfigLighting> configList = demandLightingQueryService.listLightingConfigBySceneId(projectId, lightingScene.getSceneId());
                for (DemandConfigLighting demandConfigLighting : configList) {
                    autoConfigObjectId.add(demandConfigLighting.getDemandInstanceId());
                }
            }
        }
        return autoConfigObjectId;
    }

    @Override
    public String logTag() {
        return "需求干预服务";
    }
}
