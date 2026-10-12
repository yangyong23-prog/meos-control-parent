package com.persagy.apccontrol.basic.resource.intervention.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.convert.InterventionConvert;
import com.persagy.apccontrol.basic.mapper.intervention.InterventionConfigMapper;
import com.persagy.apccontrol.basic.model.context.intervention.InterventionInfoCodeConfigQueryContext;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionAddEntity;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionInfoQueryParamEntity;
import com.persagy.apccontrol.basic.model.entity.intervention.TimeSlotEntity;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionInfoQueryParam;
import com.persagy.apccontrol.basic.resource.intervention.InterventionConfigQueryResourceService;
import com.persagy.apccontrol.basic.resource.intervention.param.InterventionConfigQueryDaoParam;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 干预配置服务实现
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Service
@Slf4j
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InterventionConfigQueryResourceServiceImpl
        extends BaseServiceImpl<InterventionConfigMapper, InterventionConfig>
        implements InterventionConfigQueryResourceService {

    private final InterventionConvert interventionConvert;
    private final InstanceService instanceService;

    @Override
    public Optional<InterventionConfig> get(@NonNull InterventionConfigQueryDaoParam param) {
        return list(param).stream().findFirst();
    }

    @Override
    public List<InterventionConfig> list(@NonNull InterventionConfigQueryDaoParam param) {
        return getBaseMapper().list(param);
    }

    @Override
    public Boolean validatedAddConfigSlot(InterventionAddEntity entity) {
        AtomicReference<Boolean> result = new AtomicReference<>(true);
        InterventionInfoQueryParamEntity param = new InterventionInfoQueryParamEntity();
        Set<String> instanceIds = new HashSet<>();
        instanceIds.add(entity.getInstanceId());
        param.setInstanceIds(instanceIds);
        param.setRunningMonitorSecondType(entity.getRunningMonitorSecondType());
        LambdaQueryWrapper<InterventionConfig> query = new LambdaQueryWrapper<>();
        query.eq(InterventionConfig::getRunningMonitorSecondType, entity.getRunningMonitorSecondType())
                .in(CollectionUtils.isNotEmpty(instanceIds), InterventionConfig::getInstanceId, instanceIds);
        List<InterventionConfig> existConfigs = getBaseMapper().selectList(query);
        if (CollectionUtils.isEmpty(existConfigs)) {
            return result.get();
        }
        List<TimeSlotEntity> timeSlots = entity.getTimeSlot();
        if (CollectionUtils.isEmpty(timeSlots)) {
            return result.get();
        }
        timeSlots.forEach(timeSlot -> {
            existConfigs.forEach(config -> {
                if (!config.getId().equals(entity.getId())) {
                    boolean temp = timeSlot.getEndDateTime().isBefore(config.getBeginTime()) || config.getEndTime().isBefore(timeSlot.getBeginDateTime());
                    if (!temp) {
                        result.set(false);
                    }
                }
            });
        });
        return result.get();


    }

    @Override
    public void queryInterventionConfigList(@NonNull InterventionInfoCodeConfigQueryContext queryContext) {
//        public List<InterventionConfigEntity> queryInterventionConfigList (InterventionInfoCodeConfigQueryContext
//        queryContext){
        RunningMonitorSecondTypeEnum monitorTypeEnum = queryContext.getQueryParam().getRunningMonitorSecondType();
        InterventionInfoQueryParam queryParam = queryContext.getQueryParam();
        Set<String> instanceIds = queryParam.getInstanceIds();
        if (CollectionUtils.isEmpty(instanceIds)) {
            return;
        }
        if (monitorTypeEnum == null) {
            log.warn("RunningMonitorTypeEnum is null when queryInterventionConfigList");
            return;
        }
        if (DemandInstanceTypeEnum.SPACE == queryParam.getInstanceType()) {
            queryConfigBySpace(queryContext);
        }
        if (DemandInstanceTypeEnum.FLOOR == queryParam.getInstanceType()) {
            queryConfigByFloor(queryContext);
        }
        if (DemandInstanceTypeEnum.BUILDING == queryParam.getInstanceType()) {
            queryConfigByBuilding(queryContext);
        }
        if (DemandInstanceTypeEnum.PROJECT == queryParam.getInstanceType()) {
            queryConfigByProject(queryContext);
        }
    }

    @Override
    public List<InterventionConfig> listActive(String projectId, RunningMonitorSecondTypeEnum runningMonitorSecondType) {
        return getBaseMapper().listActive(projectId, runningMonitorSecondType);
    }

    @Override
    public List<InterventionConfig> listActiveByTime(String projectId, RunningMonitorSecondTypeEnum runningMonitorSecondType, LocalDateTime localDateTime) {
        return getBaseMapper().listActiveByTime(projectId, runningMonitorSecondType, localDateTime);
    }

    void queryConfigBySpace(@NotNull InterventionInfoCodeConfigQueryContext queryContext) {
        InterventionInfoQueryParam queryParam = queryContext.getQueryParam();
        Set<String> instanceIds = queryParam.getInstanceIds();
        queryParam.setProjectId(ContextUtil.getProjectId());
        InterventionInfoQueryParamEntity queryParamEntity = interventionConvert.queryParamToInfoQueryParamEntity(queryParam);
        List<InterventionConfig> spaceConfigPoList = getConfigByRunningMonitorType(queryParamEntity);
        if (CollectionUtils.isNotEmpty(spaceConfigPoList)) {
            Map<String, List<InterventionConfig>> groupedConfigs = spaceConfigPoList.stream()
                    .collect(Collectors.groupingBy(InterventionConfig::getInstanceId));
            groupedConfigs.forEach((key, value) -> queryContext.fillInstanceAllConfig(key, value));
        }
        InterventionInfoQueryParamEntity queryParamTemp = new InterventionInfoQueryParamEntity();
        queryParamTemp.setBeginDateTime(queryParamEntity.getBeginDateTime());
        queryParamTemp.setEndDateTime(queryParamEntity.getEndDateTime());
        queryParamTemp.setRunningMonitorSecondType(queryParamEntity.getRunningMonitorSecondType());
        queryParamTemp.setProjectId(ContextUtil.getProjectId());
        // 楼层的
        Map<String, String> spaceToFloorMap = new HashMap<>();
        Set<String> allFloor = new HashSet<>();
        instanceIds.forEach(spaceId -> {
            Set<String> floorId = instanceService.listFloorIdsByInstance(DemandInstanceTypeEnum.SPACE, spaceId);
            spaceToFloorMap.put(spaceId, floorId.stream().findFirst().orElse(null));
            if (CollectionUtils.isNotEmpty(floorId)) {
                allFloor.addAll(floorId);
            }
        });
        fetchAndGroupConfigs(allFloor, DemandInstanceTypeEnum.FLOOR, spaceToFloorMap, queryParamTemp, queryContext);
        // 查询建筑
        Map<String, String> spaceToBuildingMap = new HashMap<>();
        Set<String> allBuilding = new HashSet<>();
        instanceIds.forEach(spaceId -> {
            Set<String> buildingId = instanceService.listBuildingIdsByInstance(DemandInstanceTypeEnum.SPACE, spaceId);
            spaceToBuildingMap.put(spaceId, buildingId.stream().findFirst().orElse(null));
            if (CollectionUtils.isNotEmpty(buildingId)) {
                allBuilding.addAll(buildingId);
            }
        });
        fetchAndGroupConfigs(allBuilding, DemandInstanceTypeEnum.BUILDING, spaceToBuildingMap, queryParamTemp, queryContext);
        // 查询项目
        Map<String, String> spaceToProjectMap = new HashMap<>();
        Set<String> allProjectIds = new HashSet<>();
        allProjectIds.add(queryParamTemp.getProjectId());
        instanceIds.forEach(spaceId -> {
            spaceToProjectMap.put(spaceId, queryParamTemp.getProjectId());
        });
        fetchAndGroupConfigs(allProjectIds, DemandInstanceTypeEnum.PROJECT, spaceToProjectMap, queryParamTemp, queryContext);
    }


    public void queryConfigByFloor(@NotNull InterventionInfoCodeConfigQueryContext queryContext) {
        InterventionInfoQueryParam queryParam = queryContext.getQueryParam();
        Set<String> instanceIds = queryParam.getInstanceIds();
        queryParam.setProjectId(ContextUtil.getProjectId());
        InterventionInfoQueryParamEntity queryParamEntity = interventionConvert.queryParamToInfoQueryParamEntity(queryParam);
        List<InterventionConfig> floorConfigPoList = getConfigByRunningMonitorType(queryParamEntity);
        //填充楼层
        if (CollectionUtils.isNotEmpty(floorConfigPoList)) {
            Map<String, List<InterventionConfig>> groupedConfigs = floorConfigPoList.stream()
                    .collect(Collectors.groupingBy(InterventionConfig::getInstanceId));
            groupedConfigs.forEach((key, value) -> queryContext.fillInstanceAllConfig(key, value));
        }
        InterventionInfoQueryParamEntity queryParamTemp = new InterventionInfoQueryParamEntity();
        queryParamTemp.setBeginDateTime(queryParamEntity.getBeginDateTime());
        queryParamTemp.setEndDateTime(queryParamEntity.getEndDateTime());
        queryParamTemp.setRunningMonitorSecondType(queryParamEntity.getRunningMonitorSecondType());
        queryParamTemp.setProjectId(ContextUtil.getProjectId());
        // 查询建筑
        Map<String, String> floorToBuildingMap = new HashMap<>();
        Set<String> allBuilding = new HashSet<>();
        instanceIds.forEach(floorId -> {
            Set<String> buildingId = instanceService.listBuildingIdsByInstance(DemandInstanceTypeEnum.SPACE, floorId);
            floorToBuildingMap.put(floorId, buildingId.stream().findFirst().orElse(null));
            if (CollectionUtils.isNotEmpty(buildingId)) {
                allBuilding.addAll(buildingId);
            }
        });
        fetchAndGroupConfigs(allBuilding, DemandInstanceTypeEnum.BUILDING, floorToBuildingMap, queryParamTemp, queryContext);
        //项目
        Map<String, String> floorToProjectMap = new HashMap<>();
        Set<String> allProjectIds = new HashSet<>();
        allProjectIds.add(queryParamTemp.getProjectId());
        instanceIds.forEach(floorId -> {
            floorToProjectMap.put(floorId, queryParamTemp.getProjectId());
        });
        fetchAndGroupConfigs(allProjectIds, DemandInstanceTypeEnum.PROJECT, floorToProjectMap, queryParamTemp, queryContext);
    }

    public void queryConfigByBuilding(@NonNull InterventionInfoCodeConfigQueryContext queryContext) {
        InterventionInfoQueryParam queryParam = queryContext.getQueryParam();
        Set<String> instanceIds = queryParam.getInstanceIds();
        queryParam.setProjectId(ContextUtil.getProjectId());
        InterventionInfoQueryParamEntity queryParamEntity = interventionConvert.queryParamToInfoQueryParamEntity(queryParam);
        List<InterventionConfig> buildingConfigPoList = getConfigByRunningMonitorType(queryParamEntity);
        //填充建筑
        if (CollectionUtils.isNotEmpty(buildingConfigPoList)) {
            Map<String, List<InterventionConfig>> groupedConfigs = buildingConfigPoList.stream()
                    .collect(Collectors.groupingBy(InterventionConfig::getInstanceId));
            groupedConfigs.forEach((key, value) -> queryContext.fillInstanceAllConfig(key, value));
        }
        //项目
        InterventionInfoQueryParamEntity queryParamTemp = new InterventionInfoQueryParamEntity();
        queryParamTemp.setBeginDateTime(queryParamEntity.getBeginDateTime());
        queryParamTemp.setEndDateTime(queryParamEntity.getEndDateTime());
        queryParamTemp.setRunningMonitorSecondType(queryParamEntity.getRunningMonitorSecondType());
        queryParamTemp.setProjectId(ContextUtil.getProjectId());
        Map<String, String> buildingToProjectMap = new HashMap<>();
        Set<String> allProjectIds = new HashSet<>();
        allProjectIds.add(queryParamTemp.getProjectId());
        instanceIds.forEach(buildingId -> {
            buildingToProjectMap.put(buildingId, queryParamTemp.getProjectId());
        });
        fetchAndGroupConfigs(allProjectIds, DemandInstanceTypeEnum.PROJECT, buildingToProjectMap, queryParamTemp, queryContext);
    }

    public void queryConfigByProject(@NonNull InterventionInfoCodeConfigQueryContext queryContext) {
        InterventionInfoQueryParam queryParam = queryContext.getQueryParam();
        queryParam.setProjectId(ContextUtil.getProjectId());
        InterventionInfoQueryParamEntity queryParamEntity = interventionConvert.queryParamToInfoQueryParamEntity(queryParam);
        List<InterventionConfig> projectConfigPoList = getConfigByRunningMonitorType(queryParamEntity);
        if (CollectionUtils.isNotEmpty(projectConfigPoList)) {
            Map<String, List<InterventionConfig>> groupedConfigs = projectConfigPoList.stream()
                    .collect(Collectors.groupingBy(InterventionConfig::getInstanceId));
            groupedConfigs.forEach((key, value) -> queryContext.fillInstanceAllConfig(key, value));
        }


    }

    // 定义一个公共方法，用于获取配置并填充到上下文中
    private void fetchAndGroupConfigs(
            Set<String> instanceIds,
            DemandInstanceTypeEnum instanceType,
            Map<String, String> spaceToIdMap,
            InterventionInfoQueryParamEntity queryParamTemp,
            InterventionInfoCodeConfigQueryContext queryContext) {
        if (CollectionUtils.isNotEmpty(instanceIds)) {
            queryParamTemp.setInstanceIds(instanceIds);
            queryParamTemp.setInstanceType(instanceType);
            List<InterventionConfig> configs = getConfigByRunningMonitorType(queryParamTemp);

            if (CollectionUtils.isNotEmpty(configs)) {
                Map<String, List<InterventionConfig>> groupedConfigs = configs.stream()
                        .collect(Collectors.groupingBy(InterventionConfig::getInstanceId));
                groupedConfigs.forEach((key, value) -> {
                    String spaceId = spaceToIdMap.entrySet().stream()
                            .filter(entry -> entry.getValue() != null && entry.getValue().equals(key))
                            .map(Map.Entry::getKey)
                            .findFirst().orElse(null);
                    if (spaceId != null) {
                        queryContext.fillInstanceAllConfig(spaceId, value);
                    }
                });
            }
        }
    }


    @Override
    public List<InterventionConfig> getConfigByRunningMonitorType(InterventionInfoQueryParamEntity paramEntity) {
        return getBaseMapper().queryList(paramEntity);
    }
}
