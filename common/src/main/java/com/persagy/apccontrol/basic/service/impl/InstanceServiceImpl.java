package com.persagy.apccontrol.basic.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeTypeCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.EquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.rwd.*;
import com.persagy.apccontrol.basic.model.vo.instance.EquipmentVO;
import com.persagy.apccontrol.basic.model.vo.instance.InstanceTreeColdHotSystemVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdRelationResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectModelViewQueryDaoParam;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheRuleRepository;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.context.InfoCodeRwdObjectDetailByTemplateContext;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectDetailModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 实例服务实现
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class InstanceServiceImpl implements InstanceService, LogComponent {

    private final Executor executor;
    private final DtpRwdConvert dtpRwdConvert;
    private final BizDtpRwdService bizDtpRwdService;
    private final DtpRwdClientService dtpRwdClientService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final AbstractIotDataCacheServer<?> abstractIotDataCacheServer;
    private final RedisCacheTemplate redisCacheTemplate;

    /**
     * 新缓存对象仓库，读取缓存设备数据。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheObjectRepository cacheObjectRepository;

    /**
     * 新缓存对象关系仓库，读取设备-空间关系。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheObjectRelRepository cacheObjectRelRepository;

    /**
     * 缓存规则仓库，读取 IoT 规则可运行状态。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheRuleRepository cacheRuleRepository;

    /**
     * 缓存对象模型状态服务，检查 isReady 状态。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheObjectModelStatusService cacheObjectModelStatusService;

    /**
     * 缓存对象模型视图仓库，读取 BIM 模型视图数据。
     *
     * @since v7.260802.0
     */
    private final @NonNull CacheObjectModelViewRepository cacheObjectModelViewRepository;

    @Override
    public List<DtpRwdBuildingDTO> treeBuildingFloorSpace(@NonNull TreeBuildingFloorSpaceQueryParam param) {
        String projectId = ContextUtil.getProjectId();
        String systemId = param.getSystemId();
        Map<InstanceTypeEnum, Set<String>> instanceTypeAdnWithColumns = param.getInstanceTypeAndWithColumns();

        List<String> systemIds;
        if (StringUtils.isNotBlank(systemId)) {
            systemIds = Lists.newArrayList(systemId);
        } else {
            systemIds = null;
        }
        DtpRwdObjectQueryParam buildingQueryParam = new DtpRwdObjectQueryParam();
        DtpRwdObjectQueryParam floorQueryParam = new DtpRwdObjectQueryParam();
        DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam();
        if (MapUtils.isNotEmpty(instanceTypeAdnWithColumns)) {
            buildingQueryParam.setWithColumns(instanceTypeAdnWithColumns.get(InstanceTypeEnum.BUILDING));
            floorQueryParam.setWithColumns(instanceTypeAdnWithColumns.get(InstanceTypeEnum.FLOOR));
            spaceQueryParam.setWithColumns(instanceTypeAdnWithColumns.get(InstanceTypeEnum.SPACE));
        }

        RedisCacheTemplate.MethodCall<List<DtpRwdBuildingDTO>> methodToCache = () -> bizDtpRwdService.treeBuildingFloorSpace(false, false
                , projectId,
                systemIds, buildingQueryParam, floorQueryParam, spaceQueryParam, param.getIsQuerySpace());
        return redisCacheTemplate.asyncRefreshTemplate("treeBuildingFloorSpace", param, methodToCache);
    }

    @Override
    public List<DtpRwdBuildingDTO> treeBuildingFloorRoomFuncTypeSpace(
            @NonNull TreeBuildingFloorRoomFuncTypeSpaceQueryParam param) {
        param.fillDefaultInstanceTypeAndWithColumns();
        Set<String> filterBuildingIds = param.getFilterBuildingIds();
        Set<String> filterFloorIds = param.getFilterFloorIds();
        Set<String> filterRoomFuncTypes = param.getFilterRoomFuncTypes();
        Set<String> filerSpaceIds = param.getFilerSpaceIds();
        boolean querySpace = param.isQuerySpace();
        Map<InstanceTypeEnum, Set<String>> instanceTypeAndWithColumns = param.getInstanceTypeAndWithColumns();

        // 查询建筑
        DtpRwdObjectQueryParam buildingQueryParam = new DtpRwdObjectQueryParam();
        buildingQueryParam.setId(CollectionUtils.isNotEmpty(filterBuildingIds) ? filterBuildingIds : null);
        buildingQueryParam.setWithColumns(instanceTypeAndWithColumns.get(InstanceTypeEnum.BUILDING));
        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuilding(buildingQueryParam);
        // 查询楼层
        DtpRwdObjectQueryParam floorQueryParam = new DtpRwdObjectQueryParam();
        floorQueryParam.setId(CollectionUtils.isNotEmpty(filterFloorIds) ? filterFloorIds : null);
        if (CollectionUtils.isNotEmpty(filterBuildingIds)) {
            floorQueryParam.addInfoCode(DtpRwdFloorDTO.Fields.buildingId, filterBuildingIds);
        }
        floorQueryParam.setWithColumns(instanceTypeAndWithColumns.get(InstanceTypeEnum.FLOOR));
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(floorQueryParam);
        // 查询空间
        List<DtpRwdSpaceDTO> spaces = Collections.emptyList();
        if (querySpace) {
            DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam();
            spaceQueryParam.setId(CollectionUtils.isNotEmpty(filerSpaceIds) ? filerSpaceIds : null);
            if (CollectionUtils.isNotEmpty(filterBuildingIds)) {
                floorQueryParam.addInfoCode(DtpRwdSpaceDTO.Fields.buildingId, filterBuildingIds);
            }
            if (CollectionUtils.isNotEmpty(filterFloorIds)) {
                floorQueryParam.addInfoCode(DtpRwdSpaceDTO.Fields.floorId, filterFloorIds);
            }
            if (CollectionUtils.isNotEmpty(filterRoomFuncTypes)) {
                spaceQueryParam.addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType, filterRoomFuncTypes);
            }
            spaceQueryParam.setWithColumns(instanceTypeAndWithColumns.get(InstanceTypeEnum.SPACE));
            spaces = dtpRwdClientService.listSpace(spaceQueryParam);
        }
        return treeBuildingFloorRoomFuncTypeSpace(buildings, floors, spaces);
    }

    @Override
    public List<DtpRwdBuildingDTO> treeBuildingFloorDashboardSpaces(@NonNull TreeBuildingFloorDashboardSpacesQueryParam param) {
        param.fillDefaultInstanceTypeAndWithColumns();
        Set<String> filterBuildingIds = param.getFilterBuildingIds();
        Set<String> filterFloorIds = param.getFilterFloorIds();
        Set<String> filerSpaceIds = param.getFilerSpaceIds();
        Map<InstanceTypeEnum, Set<String>> instanceTypeAndWithColumns = param.getInstanceTypeAndWithColumns();

        // 查询建筑
        DtpRwdObjectQueryParam buildingQueryParam = new DtpRwdObjectQueryParam();
        buildingQueryParam.setId(CollectionUtils.isNotEmpty(filterBuildingIds) ? filterBuildingIds : null);
        buildingQueryParam.setWithColumns(instanceTypeAndWithColumns.get(InstanceTypeEnum.BUILDING));
        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuilding(buildingQueryParam);
        // 查询楼层
        DtpRwdObjectQueryParam floorQueryParam = new DtpRwdObjectQueryParam();
        floorQueryParam.setId(CollectionUtils.isNotEmpty(filterFloorIds) ? filterFloorIds : null);
        if (CollectionUtils.isNotEmpty(filterBuildingIds)) {
            floorQueryParam.addInfoCode(DtpRwdFloorDTO.Fields.buildingId, filterBuildingIds);
        }
        floorQueryParam.setWithColumns(instanceTypeAndWithColumns.get(InstanceTypeEnum.FLOOR));
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(floorQueryParam);
        Map<String, List<DtpRwdFloorDTO>> buildingIdAndFloors = DtpRwdFloorDTO.mapBuildingIdAndFloors(floors);
        // 查询空间
        RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
        spaceQueryParam.setFilterObjectIds(filerSpaceIds);
        spaceQueryParam.setWithColumns(instanceTypeAndWithColumns.get(InstanceTypeEnum.SPACE));
        spaceQueryParam.setSpaceTree(true);
        spaceQueryParam.setRunningMonitorSecondType(param.getRunningMonitorSecondType());
        List<? extends DtpRwdSpaceVO> spaces = param.getRunningMonitorType().getRunningMonitorStrategy().listDashboardSpaces(spaceQueryParam);
        MultiKeyMap<String, List<DtpRwdSpaceDTO>> buildingFloorIdAndSpaces = dtpRwdClientService.parseBuildingFloorIdAndSpaces(BeanUtil.copyToList(spaces, DtpRwdSpaceDTO.class));

        for (DtpRwdBuildingDTO building : buildings) {
            String buildingId = building.getId();
            building.setFloors(new ArrayList<>());
            List<DtpRwdFloorDTO> tempFloors = buildingIdAndFloors.get(buildingId);
            if (CollectionUtils.isEmpty(tempFloors)) {
                continue;
            }
            for (DtpRwdFloorDTO tempFloor : tempFloors) {
                String floorId = tempFloor.getId();
                List<DtpRwdSpaceDTO> tempSpaces = buildingFloorIdAndSpaces.get(buildingId, floorId);
                if (CollectionUtils.isEmpty(tempSpaces)) {
                    continue;
                }
                tempFloor.setSpaces(tempSpaces);
                building.getFloors().add(tempFloor);
            }
        }
        // 如果建筑下没有楼层把这个建筑数据删掉
        buildings.removeIf(b -> CollectionUtils.isEmpty(b.getFloors()));
        return buildings;
    }

    @Override
    public @NonNull List<@NonNull InfoCodeRwdObjectDetailDTO> listObjectsByTackValueLogic(
            @NonNull ObjectQueryParam param) {
        param.checkAndSet();
        InfoCodeTakeValueLogicRwdObjectDetailModel takeValueLogic = param.getTakeValueLogic();
        LocalDateTime startTime = param.getStartTime();
        LocalDateTime endTime = param.getEndTime();

        Map<String, Object> additionalValues = InfoCodeAnalyzeStrategy.fillAdditionalValuesTimeRange(
                null, startTime, endTime);
        InfoCodeTemplateTypeModel templateTypeModel = param.toTemplateTypeModel();
        InfoCodeRwdObjectDetailByTemplateContext context = new InfoCodeRwdObjectDetailByTemplateContext(
                templateTypeModel, takeValueLogic, additionalValues);
        // 填充数据源映射
        context.fillSourceMap();
        // 填充对象
        context.fillObjects();
        // 填充对象ID和关联上下文
        context.fillObjectIdAndRelation();
        return context.toResult();
    }

    @Override
    public List<DtpRwdBuildingDTO> listBuildings(@NonNull BuildingQueryParam param) {
        List<String> buildingIds = param.getBuildingIds();
        String[] withColumns = param.getWithColumns();
        return dtpRwdClientService.listBuilding(new DtpRwdObjectQueryParam().setId(buildingIds).addWithColumn(withColumns));
    }

    @Override
    public List<DtpRwdFloorDTO> listFloors(@NonNull FloorQueryParam param) {
        List<String> buildingIds = param.getBuildingIds();
        List<String> floorIds = param.getFloorIds();
        String[] withColumns = param.getWithColumns();

        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setId(floorIds).addWithColumn(withColumns);
        if (CollectionUtils.isNotEmpty(buildingIds)) {
            queryParam.addInfoCode(DtpRwdFloorDTO.INFO_CODE_BUILDING_ID, buildingIds);
        }
        return dtpRwdClientService.listFloor(queryParam);
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull SpaceQueryParam param) {
        List<String> buildingIds = param.getBuildingIds();
        List<String> floorIds = param.getFloorIds();
        List<String> spaceIds = param.getSpaceIds();
        String[] withColumns = param.getWithColumns();

        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setId(spaceIds);
        if (ArrayUtils.isNotEmpty(withColumns)) {
            queryParam.addWithColumn(withColumns);
        } else {
            queryParam.addWithColumn(DtpRwdSpaceDTO.Fields.class);
        }
        if (CollectionUtils.isNotEmpty(buildingIds)) {
            queryParam.addInfoCode(DtpRwdSpaceDTO.Fields.buildingId, buildingIds);
        }
        if (CollectionUtils.isNotEmpty(floorIds)) {
            queryParam.addInfoCode(DtpRwdSpaceDTO.Fields.floorId, floorIds);
        }
        return dtpRwdClientService.listSpace(queryParam);
    }

    @Override
    public List<DtpRwdObjectVO> listFloorRelationSpacesBySystem(
            @NonNull FloorRelationSpacesBySystemQueryParam param) {
        String floorId = param.getFloorId();
        Map<String, List<DtpRwdRelationResponse>> floorRelationSpaces = bizDtpRwdService.mapFloorRelationSpacesBySystem(
                false, true, param.getProjectId(),
                param.getSystemId(), Lists.newArrayList(floorId), null);
        List<DtpRwdRelationResponse> relationSpaces = floorRelationSpaces.get(floorId);
        if (CollectionUtils.isEmpty(relationSpaces)) {
            relationSpaces = new ArrayList<>();
        }
        return dtpRwdConvert.toObjectVo(relationSpaces);
    }

    @Override
    public List<EquipmentVO> listEquipmentsInBySpaceIds(@NonNull EquipmentInSpaceQueryParam param) {
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        String spaceId = param.getSpaceId();
        Set<String> filterEquipmentIds = param.getEquipmentIds();
        String[] withColumns = param.getWithColumns();
        Set<String> filterInSystemIds = param.getFilterInSystemIds();
        Set<String> filterClassCodes = param.getFilterClassCodes();
        Set<String> filterControlledClassCodes = param.getFilterControlledClassCodes();
        Boolean queryOperableRunningStatus = param.getQueryOperableRunningStatus();
        Boolean queryClassCodeName = param.getQueryClassCodeName();
        Boolean queryMajorName = param.getQueryMajorName();
        EquipmentInSpaceQueryParam additionalQueryParam = param.getAdditionalQueryParam();

        // 查询策略类中的设备ID
        Set<String> equipmentIdsInStrategy = Collections.emptySet();
        if (Objects.nonNull(runningMonitorSecondType)) {
            // 当查询字段不为空时, 需要加上ID, 然后最后根据ID打上标识
            if (ArrayUtils.isNotEmpty(withColumns)) {
                withColumns = ArrayUtils.add(withColumns, DtpRwdObjectDTO.Fields.id);
            }
            RunningMonitorStrategy runningMonitorStrategy = runningMonitorSecondType
                    .toRunningMonitorTypeEnum().getRunningMonitorStrategy();
            RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
            equipmentQueryParam.setRunningMonitorSecondType(runningMonitorSecondType);
            equipmentQueryParam.setFilterObjectIds(filterEquipmentIds);
            equipmentQueryParam.setFilterEquipmentInSpaceIds(Sets.newHashSet(spaceId));
            equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            equipmentIdsInStrategy = runningMonitorStrategy.listEquipments(equipmentQueryParam).stream()
                    .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        }
        // 查询所在空间的设备
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setId(filterEquipmentIds);
        if (ArrayUtils.isNotEmpty(withColumns)) {
            queryParam.addWithColumn(withColumns);
        } else {
            queryParam.addWithColumn(DtpRwdEquipmentDTO.Fields.class);
        }
        if (Objects.nonNull(filterClassCodes)) {
            queryParam.setClassCode(filterClassCodes);
        }
        if (Objects.nonNull(filterControlledClassCodes)) {
            queryParam.addWithColumn(DtpRwdObjectDTO.Fields.classCode);
            queryParam.addWithColumnByMonitoringSurveillanceStatus();
        }
        if (BooleanUtils.isTrue(queryOperableRunningStatus)) {
            queryParam.addWithColumn(DtpRwdObjectDTO.Fields.classCode);
        }
        if (BooleanUtils.isTrue(queryClassCodeName)) {
            queryParam.addWithColumn(DtpRwdObjectDTO.Fields.classCode);
        }
        if (BooleanUtils.isTrue(queryMajorName)) {
            queryParam.addWithColumn(InfoCodeEnum.MAJOR.getCode());
        }
        List<DtpRwdEquipmentDTO> equipments = dtpRwdClientService
                .listEquipmentInBySpaceIds(Lists.newArrayList(spaceId), queryParam);
        if (Objects.nonNull(filterControlledClassCodes)) {
            equipments.removeIf(item -> {
                if (CollectionUtils.isEmpty(filterControlledClassCodes)) {
                    return !item.isControlled();
                }
                return filterControlledClassCodes.contains(item.getClassCode()) && !item.isControlled();
            });
        }
        if (CollectionUtils.isEmpty(equipments)) {
            return Lists.newArrayList();
        }
        // 过滤设备所在系统
        if (CollectionUtils.isNotEmpty(filterInSystemIds)) {
            List<String> equipmentIds = equipments.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
            DtpRwdObjectQueryParam equipmentRelSystemQueryParam = new DtpRwdObjectQueryParam();
            equipmentRelSystemQueryParam.setId(equipmentIds);
            equipmentRelSystemQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            List<String> matchEquipmentIds = dtpRwdClientService.listEquipmentInBySystemIds(
                            filterInSystemIds, equipmentRelSystemQueryParam).stream()
                    .map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
            equipments.removeIf(item -> !matchEquipmentIds.contains(item.getId()));
            if (CollectionUtils.isEmpty(equipments)) {
                return Lists.newArrayList();
            }
        }
        List<EquipmentVO> equipmentVos = equipments.stream()
                .map(dtpRwdConvert::toEquipmentVo)
                .collect(Collectors.toList());
        // 存在策略设备ID时打上指定标识
        if (CollectionUtils.isNotEmpty(equipmentIdsInStrategy)) {
            for (EquipmentVO equipmentVo : equipmentVos) {
                equipmentVo.setInStrategy(equipmentIdsInStrategy.contains(equipmentVo.getId()));
            }
        }
        // 查询可运行设备状态
        if (BooleanUtils.isTrue(queryOperableRunningStatus)) {
            Map<String, Set<String>> classCodeAndEquipmentIds = equipments.stream().collect(Collectors.groupingBy(
                    DtpRwdObjectDTO::getClassCode, Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));
            Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = abstractIotDataCacheServer
                    .mapObjectIdAndExistsRuleInfoCodes(classCodeAndEquipmentIds, Lists.newArrayList(
                            InfoCodeEnum.RUN_STATUS.value(),
                            InfoCodeEnum.SWITCH_STATUS.value(),
                            InfoCodeEnum.ON_OFF_STATUS.value(),
                            InfoCodeEnum.DEPLOY_WITHDRAWALARM_STATUS.value()
                    ));
            for (EquipmentVO equipmentVo : equipmentVos) {
                equipmentVo.setOperableRunning(objectIdAndExistsRuleInfoCodes.containsKey(equipmentVo.getId()));
            }
        }
        if (BooleanUtils.isTrue(queryClassCodeName)) {
            Set<String> classCodes = equipments.stream().map(DtpRwdObjectDTO::getClassCode).collect(Collectors.toSet());
            Map<String, String> classCodeAndName = dtpKnowledgeClientService.mapClassCodeAndName(
                    false, false, classCodes);
            for (EquipmentVO equipmentVo : equipmentVos) {
                equipmentVo.setClassCodeName(classCodeAndName.get(equipmentVo.getClassCode()));
            }
        }
        if (BooleanUtils.isTrue(queryMajorName)) {
            Map<String, String> typeCodeAndName = dtpKnowledgeClientService.mapTypeCodeAndName(
                    DtpKnowledgeTypeCodeEnum.KNOWLEDGE_MAJORS);
            for (EquipmentVO equipmentVo : equipmentVos) {
                String majorCode = equipmentVo.dynamicAnalyzeProperties(InfoCodeEnum.MAJOR.getCode());
                if (StringUtils.isBlank(majorCode)) {
                    continue;
                }
                equipmentVo.setMajorName(typeCodeAndName.get(majorCode));
            }
        }
        if (Objects.nonNull(additionalQueryParam)) {
            equipmentVos.addAll(listEquipmentsInBySpaceIds(additionalQueryParam));
        }
        return equipmentVos;
    }

    @Override
    public List<EquipmentVO> listEquipmentsInBySpaceIdsComputerRoomSecurity(String spaceId) {
        // 新缓存路径
        if (this.cacheObjectModelStatusService.isReady(RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY)) {
            return listEquipmentsInBySpaceIdsComputerRoomSecurityFromCache(spaceId);
        }

        String[] withColumns = {"major", "id", "bimLocation", "localName", "classCode", "objType", "floorId", "buildingId", "relObjIds", "floorId", "bimLocation", "realTimeStreamProtocol", "manufacturer", "isControl"};
        Set<String> filterControlledClassCodes = Sets.newHashSet("CFCSDR", "CFCSFR", "SPEGFG");
        Boolean queryOperableRunningStatus = Boolean.TRUE;
        Boolean queryClassCodeName = Boolean.TRUE;
        Boolean queryMajorName = Boolean.TRUE;

        // 查询所在空间的设备
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        if (ArrayUtils.isNotEmpty(withColumns)) {
            queryParam.addWithColumn(withColumns);
        } else {
            queryParam.addWithColumn(DtpRwdEquipmentDTO.Fields.class);
        }
        if (Objects.nonNull(filterControlledClassCodes)) {
            queryParam.addWithColumn(DtpRwdObjectDTO.Fields.classCode);
            queryParam.addWithColumnByMonitoringSurveillanceStatus();
        }
        if (BooleanUtils.isTrue(queryOperableRunningStatus)) {
            queryParam.addWithColumn(DtpRwdObjectDTO.Fields.classCode);
        }
        if (BooleanUtils.isTrue(queryClassCodeName)) {
            queryParam.addWithColumn(DtpRwdObjectDTO.Fields.classCode);
        }
        if (BooleanUtils.isTrue(queryMajorName)) {
            queryParam.addWithColumn(InfoCodeEnum.MAJOR.getCode());
        }
        List<DtpRwdEquipmentDTO> equipments = dtpRwdClientService
                .listEquipmentInBySpaceIds(Lists.newArrayList(spaceId), queryParam);
        if (Objects.nonNull(filterControlledClassCodes)) {
            equipments.removeIf(item -> {
                if (CollectionUtils.isEmpty(filterControlledClassCodes)) {
                    return !item.isControlled();
                }
                return filterControlledClassCodes.contains(item.getClassCode()) && !item.isControlled();
            });
        }
        if (CollectionUtils.isEmpty(equipments)) {
            return Lists.newArrayList();
        }
        List<EquipmentVO> equipmentVos = equipments.stream()
                .map(dtpRwdConvert::toEquipmentVo)
                .collect(Collectors.toList());
        // 查询可运行设备状态
        if (BooleanUtils.isTrue(queryOperableRunningStatus)) {
            Map<String, Set<String>> classCodeAndEquipmentIds = equipments.stream().collect(Collectors.groupingBy(
                    DtpRwdObjectDTO::getClassCode, Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));
            Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = abstractIotDataCacheServer
                    .mapObjectIdAndExistsRuleInfoCodes(classCodeAndEquipmentIds, Lists.newArrayList(
                            InfoCodeEnum.RUN_STATUS.value(),
                            InfoCodeEnum.SWITCH_STATUS.value(),
                            InfoCodeEnum.ON_OFF_STATUS.value(),
                            InfoCodeEnum.DEPLOY_WITHDRAWALARM_STATUS.value()
                    ));
            for (EquipmentVO equipmentVo : equipmentVos) {
                equipmentVo.setOperableRunning(objectIdAndExistsRuleInfoCodes.containsKey(equipmentVo.getId()));
            }
        }
        if (BooleanUtils.isTrue(queryClassCodeName)) {
            Set<String> classCodes = equipments.stream().map(DtpRwdObjectDTO::getClassCode).collect(Collectors.toSet());
            Map<String, String> classCodeAndName = dtpKnowledgeClientService.mapClassCodeAndName(
                    false, false, classCodes);
            for (EquipmentVO equipmentVo : equipmentVos) {
                equipmentVo.setClassCodeName(classCodeAndName.get(equipmentVo.getClassCode()));
            }
        }
        if (BooleanUtils.isTrue(queryMajorName)) {
            Map<String, String> typeCodeAndName = dtpKnowledgeClientService.mapTypeCodeAndName(
                    DtpKnowledgeTypeCodeEnum.KNOWLEDGE_MAJORS);
            for (EquipmentVO equipmentVo : equipmentVos) {
                String majorCode = equipmentVo.dynamicAnalyzeProperties(InfoCodeEnum.MAJOR.getCode());
                if (StringUtils.isBlank(majorCode)) {
                    continue;
                }
                equipmentVo.setMajorName(typeCodeAndName.get(majorCode));
            }
        }
        return equipmentVos;
    }

    /**
     * 从新缓存读取空间设备列表（API 2）。
     *
     * <p>通过 cache_object_rel 按 spaceId 查设备 ID，从 cache_object 取设备数据，
     * 从 cache_rule 读可运行状态，Knowledge 数据实时查询。</p>
     *
     * @param spaceId 空间 ID
     * @return 设备 VO 列表
     * @since v7.260802.0
     */
    private @NonNull List<EquipmentVO> listEquipmentsInBySpaceIdsComputerRoomSecurityFromCache(@NonNull String spaceId) {
        long cacheStart = System.currentTimeMillis();
        Set<String> filterControlledClassCodes = Sets.newHashSet("CFCSDR", "CFCSFR", "SPEGFG");

        // Step 1: 从 cache_object_rel 按 spaceId 查设备 ID
        CacheObjectRelQueryDaoParam relParam = new CacheObjectRelQueryDaoParam();
        relParam.setRelObjectIds(Collections.singleton(spaceId));
        relParam.setGraphCode(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE.value().getLeft());
        relParam.setRelCode(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE.value().getRight());
        List<CacheObjectRel> rels = this.cacheObjectRelRepository.listReverse(relParam);
        if (CollectionUtils.isEmpty(rels)) {
            return Lists.newArrayList();
        }
        // 保留设备→空间映射，用于后续回填 relObjIds
        Map<String, Set<String>> equipmentToSpaceIds = new LinkedHashMap<>();
        for (CacheObjectRel rel : rels) {
            equipmentToSpaceIds
                    .computeIfAbsent(rel.getObjectId(), k -> new LinkedHashSet<>())
                    .add(rel.getRelObjectId());
        }
        Set<String> equipmentIds = equipmentToSpaceIds.keySet();
        logProjectInstance().info("[性能] listEquipmentsInBySpaceIdsComputerRoomSecurityFromCache-查询设备ID 耗时: " + (System.currentTimeMillis() - cacheStart) + "ms, 数据量: " + equipmentIds.size());

        // Step 2: 从 cache_object 按设备 ID 查设备数据
        List<DtpRwdEquipmentDTO> equipments = this.cacheObjectRepository.listByObjectIds(
                equipmentIds, DtpRwdEquipmentDTO.class, null);
        if (CollectionUtils.isEmpty(equipments)) {
            return Lists.newArrayList();
        }
        logProjectInstance().info("[性能] listEquipmentsInBySpaceIdsComputerRoomSecurityFromCache-缓存读取设备 耗时: " + (System.currentTimeMillis() - cacheStart) + "ms, 数据量: " + equipments.size());

        // Step 3: 受控过滤 (CFCSDR/CFCSFR/SPEGFG 移除 !isControlled)
        equipments.removeIf(item -> {
            if (CollectionUtils.isEmpty(filterControlledClassCodes)) {
                return !item.isControlled();
            }
            return filterControlledClassCodes.contains(item.getClassCode()) && !item.isControlled();
        });
        if (CollectionUtils.isEmpty(equipments)) {
            return Lists.newArrayList();
        }
        logProjectInstance().info("[性能] listEquipmentsInBySpaceIdsComputerRoomSecurityFromCache-受控过滤 耗时: " + (System.currentTimeMillis() - cacheStart) + "ms, 剩余数据量: " + equipments.size());

        // Step 4: 从 cache_rule 查可运行状态
        Set<String> filteredIds = equipments.stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        Map<String, Set<String>> objectIdAndInfoCodes = this.cacheRuleRepository.mapInfoCodesByObjectIds(
                filteredIds,
                Lists.newArrayList(
                        InfoCodeEnum.RUN_STATUS.value(),
                        InfoCodeEnum.SWITCH_STATUS.value(),
                        InfoCodeEnum.ON_OFF_STATUS.value(),
                        InfoCodeEnum.DEPLOY_WITHDRAWALARM_STATUS.value()),
                CacheIotRuleTypeEnum.PERSISTENCE);
        logProjectInstance().info("[性能] listEquipmentsInBySpaceIdsComputerRoomSecurityFromCache-cache_rule查询 耗时: " + (System.currentTimeMillis() - cacheStart) + "ms");

        // Step 5: DTO → VO 转换
        List<EquipmentVO> equipmentVos = equipments.stream()
                .map(dtpRwdConvert::toEquipmentVo)
                .collect(Collectors.toList());

        // 回填 relObjIds（设备关联的空间ID集合）
        for (EquipmentVO vo : equipmentVos) {
            vo.setRelObjIds(equipmentToSpaceIds.getOrDefault(vo.getId(), Collections.emptySet()));
        }

        // Step 5.5: 从 cache_object_model_view 回填 bimId/bimLocation
        CacheObjectModelViewQueryDaoParam mvParam = new CacheObjectModelViewQueryDaoParam();
        mvParam.setIds(filteredIds);
        mvParam.addColumns(CacheObjectModelView::getId, CacheObjectModelView::getViewData);
        List<CacheObjectModelView> modelViews = this.cacheObjectModelViewRepository.listModelViews(mvParam);
        Map<String, CacheObjectModelView> modelViewById = new LinkedHashMap<>();
        for (CacheObjectModelView mv : modelViews) {
            modelViewById.put(mv.getId(), mv);
        }
        for (EquipmentVO vo : equipmentVos) {
            CacheObjectModelView mv = modelViewById.get(vo.getId());
            if (mv != null && StringUtils.isNotBlank(mv.getViewData())) {
                Map<String, Object> viewFields = ObjectMapperUtils.readMapFailSafe(mv.getViewData());
                if (viewFields != null) {
                    if (viewFields.containsKey("bimId")) {
                        vo.setBimId(String.valueOf(viewFields.get("bimId")));
                    }
                    if (viewFields.containsKey("bimLocation")) {
                        vo.setBimLocation(String.valueOf(viewFields.get("bimLocation")));
                    }
                }
            }
        }

        // 赋值 operableRunning
        for (EquipmentVO vo : equipmentVos) {
            vo.setOperableRunning(objectIdAndInfoCodes.containsKey(vo.getId()));
        }

        // Step 6: Knowledge 实时填充 classCodeName
        Set<String> classCodes = equipments.stream()
                .map(DtpRwdObjectDTO::getClassCode).collect(Collectors.toSet());
        Map<String, String> classCodeAndName = dtpKnowledgeClientService.mapClassCodeAndName(
                false, false, classCodes);
        for (EquipmentVO vo : equipmentVos) {
            vo.setClassCodeName(classCodeAndName.get(vo.getClassCode()));
        }

        // Knowledge 实时填充 majorName
        Map<String, String> typeCodeAndName = dtpKnowledgeClientService.mapTypeCodeAndName(
                DtpKnowledgeTypeCodeEnum.KNOWLEDGE_MAJORS);
        for (EquipmentVO vo : equipmentVos) {
            String majorCode = vo.dynamicAnalyzeProperties(InfoCodeEnum.MAJOR.getCode());
            if (StringUtils.isBlank(majorCode)) {
                continue;
            }
            vo.setMajorName(typeCodeAndName.get(majorCode));
        }

        logProjectInstance().info("[性能] listEquipmentsInBySpaceIdsComputerRoomSecurityFromCache-总耗时: " + (System.currentTimeMillis() - cacheStart) + "ms, 结果数据量: " + equipmentVos.size());
        return equipmentVos;
    }

    @Override
    public List<InstanceTreeColdHotSystemVO> treeColdHotSystem(@NonNull InstanceTreeColdHotSystemQueryParam param) {
        Set<String> withColumns = param.getWithColumns();

        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCodeEnum(Lists.newArrayList(
                DtpRwdClassCodeEnum.COLD_SYSTEM,
                DtpRwdClassCodeEnum.HOT_SYSTEM,
                DtpRwdClassCodeEnum.COLD_HOT_SYSTEM));
        queryParam.addWithColumnWithDefault(withColumns);
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.classCode);
        List<DtpRwdObjectDTO> objects = dtpRwdClientService.listObject(queryParam);
        if (CollectionUtils.isEmpty(objects)) {
            return Lists.newArrayList();
        }
        Map<String, List<DtpRwdObjectDTO>> classCodeAndObjects = objects.stream()
                .collect(Collectors.groupingBy(DtpRwdObjectDTO::getClassCode));
        Map<String, String> classCodeAndName = dtpKnowledgeClientService.mapClassCodeAndName(
                false, false, classCodeAndObjects.keySet());
        List<InstanceTreeColdHotSystemVO> result = new ArrayList<>();
        for (Map.Entry<String, List<DtpRwdObjectDTO>> entry : classCodeAndObjects.entrySet()) {
            String classCode = entry.getKey();
            List<DtpRwdObjectDTO> relObjects = entry.getValue();

            InstanceTreeColdHotSystemVO resultItem = new InstanceTreeColdHotSystemVO();
            resultItem.setClassCode(classCode);
            resultItem.setClassCodeName(classCodeAndName.get(classCode));
            resultItem.setSystems(relObjects);
            result.add(resultItem);
        }
        return result;
    }

    @Override
    public Set<String> listSpaceIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @Nullable String instanceId) {
        Set<String> instanceIds = StringUtils.isNotBlank(instanceId) ? Sets.newHashSet(instanceId) : null;
        return listSpaceIdsByInstance(instanceType, instanceIds);
    }

    @Override
    public Set<String> listSpaceIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @Nullable Set<String> instanceIds) {
        String projectId = ContextUtil.getProjectId();
        Set<String> result;
        switch (instanceType) {
            case PROJECT:
                result = dtpRwdClientService.listSpace(new DtpRwdObjectQueryParam()
                                .addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
                break;
            case BUILDING:
                result = dtpRwdClientService.listSpaceByBuildingId(instanceIds, new DtpRwdObjectQueryParam()
                                .addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
                break;
            case FLOOR:
                result = dtpRwdClientService.listSpaceByFloorId(instanceIds, new DtpRwdObjectQueryParam()
                                .addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
                break;
            case SPACE:
                result = instanceIds;
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(instanceType.getClass().getSimpleName(), instanceType);
        }
        if (CollectionUtils.isEmpty(result)) {
            LogExecutor.project(projectId).warn(log(), logTag(), "未查询到实例关联空间", Arrays.asList(
                    Pair.of("instanceType", instanceType),
                    Pair.of("instanceIds", instanceIds)
            ));
        }
        return result;
    }

    @Override
    public Set<String> listFloorIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId) {
        Set<String> instanceIds = StringUtils.isNotBlank(instanceId) ? Sets.newHashSet(instanceId) : null;
        return listFloorIdsByInstance(instanceType, instanceIds);
    }

    @Override
    public Set<String> listFloorIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @Nullable Set<String> instanceIds) {
        Set<String> result;
        switch (instanceType) {
            case PROJECT:
                result = dtpRwdClientService.listFloor(new DtpRwdObjectQueryParam()
                                .addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                break;
            case BUILDING:
                result = dtpRwdClientService.listFloorByBuildingId(instanceIds, new DtpRwdObjectQueryParam()
                                .addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                break;
            case FLOOR:
                result = instanceIds;
                break;
            case SPACE:
                result = dtpRwdClientService.listFloorBySpaceId(instanceIds, new DtpRwdObjectQueryParam()
                                .addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(instanceType.getClass().getSimpleName(), instanceType);
        }
        if (CollectionUtils.isEmpty(result)) {
            LogExecutor.project().warn(log(), logTag(), "未查询配置实例关联楼层实例", Arrays.asList(
                    Pair.of("instanceType", instanceType),
                    Pair.of("instanceIds", instanceIds)
            ));
        }
        return result;
    }

    @Override
    public Set<String> listBuildingIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId) {
        Set<String> instanceIds = StringUtils.isNotBlank(instanceId) ? Sets.newHashSet(instanceId) : null;
        return listBuildingIdsByInstance(instanceType, instanceIds);
    }

    @Override
    public Set<String> listBuildingIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @Nullable Set<String> instanceIds) {
        Set<String> result;
        switch (instanceType) {
            case PROJECT:
                result = dtpRwdClientService.listBuilding(new DtpRwdObjectQueryParam()
                                .addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                break;
            case BUILDING:
                result = instanceIds;
                break;
            case FLOOR:
                result = dtpRwdClientService.listBuildingByFloorId(instanceIds, new DtpRwdObjectQueryParam()
                                .addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                break;
            case SPACE:
                result = dtpRwdClientService.listBuildingBySpaceId(instanceIds, new DtpRwdObjectQueryParam()
                                .addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                        .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(instanceType.getClass().getSimpleName(), instanceType);
        }
        if (CollectionUtils.isEmpty(result)) {
            LogExecutor.project().warn(log(), logTag(), "未查询配置实例关联建筑实例", Arrays.asList(
                    Pair.of("instanceType", instanceType),
                    Pair.of("instanceIds", instanceIds)
            ));
        }
        return result;
    }

    @Override
    public Set<String> listChildrenInstanceIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId) {
        Set<String> result = new HashSet<>();
        switch (instanceType) {
            case PROJECT:
                result.addAll(listBuildingIdsByInstance(instanceType, instanceId));
                result.addAll(listFloorIdsByInstance(instanceType, instanceId));
                result.addAll(listSpaceIdsByInstance(instanceType, instanceId));
                break;
            case BUILDING:
                result.addAll(listFloorIdsByInstance(instanceType, instanceId));
                result.addAll(listSpaceIdsByInstance(instanceType, instanceId));
                break;
            case FLOOR:
                result.addAll(listSpaceIdsByInstance(instanceType, instanceId));
                break;
            case SPACE:
                result.add(instanceId);
                break;
        }
        return result;
    }

    @Override
    public List<DtpRwdFloorDTO> listFloorsByInstance(
            @NonNull String projectId,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId) {
        List<DtpRwdFloorDTO> result;
        switch (instanceType) {
            case PROJECT:
                result = dtpRwdClientService.listFloor(new DtpRwdObjectQueryParam()
                        .addWithColumn(DtpRwdFloorDTO.Fields.class));
                break;
            case BUILDING:
                result = dtpRwdClientService.listFloorByBuildingId(Lists.newArrayList(instanceId), new DtpRwdObjectQueryParam()
                        .addWithColumn(DtpRwdFloorDTO.Fields.class));
                break;
            case FLOOR:
                result = dtpRwdClientService.listFloor(new DtpRwdObjectQueryParam()
                        .addWithColumn(DtpRwdFloorDTO.Fields.class).setId(instanceId));
                break;
            case SPACE:
                result = dtpRwdClientService.listFloorBySpaceId(Lists.newArrayList(instanceId), new DtpRwdObjectQueryParam()
                        .addWithColumn(DtpRwdFloorDTO.Fields.class));
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(instanceType.getClass().getSimpleName(), instanceType);
        }
        if (CollectionUtils.isEmpty(result)) {
            LogExecutor.project(projectId).warn(log(), logTag(), "未查询配置实例关联楼层实例", Arrays.asList(
                    Pair.of("instanceType", instanceType),
                    Pair.of("instanceId", instanceId)
            ));
        }
        return result;
    }

    @Override
    public Map<String, List<DemandInstanceValueObject>> mapInstanceIdAndAllParents(
            @NonNull Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds) {
        return mapInstanceIdAndAllParents(instanceTypeAndIds, true);
    }

    @Override
    public Map<String, List<DemandInstanceValueObject>> mapInstanceIdAndAllParentsWithoutName(
            @NonNull Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds) {
        return mapInstanceIdAndAllParents(instanceTypeAndIds, false);
    }

    @Override
    public Collection<DtpRwdTwinControlObjectDTO> listTwinControlObjects(
            @NonNull DtpRwdTwinControlObjectQueryParam param) {
        String projectId = param.getProjectId();
        List<String> classCodes = param.getClassCodes();

        List<DtpRwdTwinControlObjectDTO> result = dtpRwdClientService.listTwinControlObjects(
                false, projectId, classCodes);
        if (CollectionUtils.isEmpty(result)) {
            logProjectInstance(projectId).warn("未查询到孪生控制对象", Pair.of("classCodes", classCodes));
            return Lists.newArrayList();
        }
        return result;
    }

    @Override
    public JSONObject queryColumnAndWallObjectByFloorId(String groupCode, String projectId, String floorId) {
        JSONObject result = new JSONObject();

        // 1.获取柱子
        List<JSONObject> columns = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("CFCSCL"), "MechInArch", "Eq2Fl", null, Lists.newArrayList(floorId));
        result.put("columnList", columns);

        // 2.获取墙
        List<JSONObject> walls = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("CFCSWL"), "MechInArch", "Eq2Fl", null, Lists.newArrayList(floorId));
        result.put("wallList", walls);

        return result;
    }

    @Override
    public List<JSONObject> listEquipmentByClassCode(String projectId, String... claasCode) {
        return this.bizDtpRwdService.queryObjectList(ContextUtil.getGroupCode(), projectId, Lists.newArrayList(claasCode), null, null, null, null);
    }

    @Override
    public List<DtpRwdEquipmentDTO> listObject(EquipmentQueryParam param){
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        return param.queryObject( queryParam);
    }

    /**
     * 组装建筑楼层空间功能类型空间树
     *
     * @param buildings 建筑
     * @param floors    楼层
     * @param spaces    空间
     * @return 建筑楼层空间功能类型空间树
     * @since 7.250101.0
     */
    private List<DtpRwdBuildingDTO> treeBuildingFloorRoomFuncTypeSpace(
            @NonNull List<DtpRwdBuildingDTO> buildings,
            @NonNull List<DtpRwdFloorDTO> floors,
            @Nullable List<DtpRwdSpaceDTO> spaces) {
        DtpKnowledgeInfoCodeDTO roomFuncTypeDefine = dtpKnowledgeClientService.getInfoCode(false,
                DtpRwdClassCodeEnum.SPACE.value(), DtpRwdSpaceDTO.Fields.roomFuncType);
        Map<String, String> dataSourceCodeAndName = Objects.nonNull(roomFuncTypeDefine)
                ? roomFuncTypeDefine.toDataSourceCodeAndName() : Collections.emptyMap();
        Map<String, List<DtpRwdFloorDTO>> buildingIdAndFloors = DtpRwdFloorDTO.mapBuildingIdAndFloors(floors);
        MultiKeyMap<String, List<DtpRwdSpaceDTO>> buildingFloorIdAndSpace =
                DtpRwdSpaceDTO.mapBuildingFloorIdAndSpace(spaces);
        for (DtpRwdBuildingDTO building : buildings) {
            String buildingId = building.getId();
            List<DtpRwdFloorDTO> floorsInBuilding = buildingIdAndFloors.get(buildingId);
            if (CollectionUtils.isNotEmpty(floorsInBuilding)) {
                for (DtpRwdFloorDTO floor : floorsInBuilding) {
                    String floorId = floor.getId();
                    List<DtpRwdSpaceDTO> spacesInFloor = buildingFloorIdAndSpace.get(buildingId, floorId);
                    List<InfoCodeDataSourceDefineModel> roomFuncTypes = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(spacesInFloor)) {
                        Map<String, List<DtpRwdSpaceDTO>> roomFuncTypeAndSpaces = spacesInFloor.stream()
                                .filter(item -> StringUtils.isNotBlank(item.getRoomFuncType()))
                                .collect(Collectors.groupingBy(DtpRwdSpaceDTO::getRoomFuncType));
                        for (Map.Entry<String, List<DtpRwdSpaceDTO>> entry : roomFuncTypeAndSpaces.entrySet()) {
                            String roomFuncType = entry.getKey();
                            List<DtpRwdSpaceDTO> spacesInRoomFuncType = entry.getValue();
                            String roomFuncTypeName = dataSourceCodeAndName.get(roomFuncType);
                            InfoCodeDataSourceDefineModel roomFuncTypeDefineModel =
                                    new InfoCodeDataSourceDefineModel(roomFuncType, roomFuncTypeName);
                            roomFuncTypeDefineModel.setSpaces(spacesInRoomFuncType);
                            roomFuncTypes.add(roomFuncTypeDefineModel);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(roomFuncTypes)) {
                        roomFuncTypes.sort(Comparator.nullsLast(Comparator.comparing(
                                InfoCodeDataSourceDefineModel::getCode)));
                    }
                    floor.setRoomFuncTypes(roomFuncTypes);
                }
            }
            building.setFloors(floorsInBuilding);
        }
        return buildings;
    }

    /**
     * 查询实例ID和所有上级实例映射
     *
     * @param instanceTypeAndIds 实例类型 -> 实例ID
     * @param queryName          是否查询名称
     * @return 实例ID -> 上级实例
     * @since 5.241201.0
     */
    public Map<String, List<DemandInstanceValueObject>> mapInstanceIdAndAllParents(
            @NonNull Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds,
            boolean queryName) {
        Map<String, List<DemandInstanceValueObject>> result = new ConcurrentHashMap<>();
        if (MapUtils.isEmpty(instanceTypeAndIds)) {
            return result;
        }
        String projectId = ContextUtil.getProjectId();
        AtomicReference<String> projectLocalName = new AtomicReference<>(null);
        if (queryName) {
            Optional<DtpRwdObjectDTO> projectOpt = dtpRwdClientService.getObjectOpt(
                    new DtpRwdObjectQueryParam().setId(projectId).addWithColumn(
                            DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName));
            projectOpt.ifPresent(item -> projectLocalName.set(item.getLocalName()));
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<DemandInstanceTypeEnum, Collection<String>> entry : instanceTypeAndIds.entrySet()) {
            DemandInstanceTypeEnum instanceType = entry.getKey();
            Collection<String> instanceIds = entry.getValue();
            if (CollectionUtils.isEmpty(instanceIds)) {
                continue;
            }
            futures.add(CompletableFuture.runAsync(() -> {
                DtpRwdObjectQueryParam queryParam;
                switch (instanceType) {
                    case PROJECT:
                        break;
                    case BUILDING:
                        for (String instanceId : instanceIds) {
                            MapExecutor.dynamicPut(result, instanceId, new DemandInstanceValueObject(
                                    DemandInstanceTypeEnum.PROJECT, projectId, projectLocalName.get()), ArrayList::new);
                        }
                        break;
                    case FLOOR:
                        queryParam = new DtpRwdObjectQueryParam().addWithColumn(
                                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
                        if (queryName) {
                            queryParam.addWithColumn(DtpRwdObjectDTO.Fields.localName);
                        }
                        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService
                                .listBuildingByFloorId(instanceIds, queryParam);
                        if (CollectionUtils.isNotEmpty(buildings)) {
                            for (DtpRwdBuildingDTO building : buildings) {
                                String buildingId = building.getId();
                                String localName = building.getLocalName();
                                Set<String> relObjIds = building.getRelObjIds();
                                if (CollectionUtils.isEmpty(relObjIds)) {
                                    continue;
                                }
                                for (String relObjId : relObjIds) {
                                    MapExecutor.dynamicPut(result, relObjId, new DemandInstanceValueObject(
                                            DemandInstanceTypeEnum.BUILDING, buildingId, localName), ArrayList::new);
                                    MapExecutor.dynamicPut(result, relObjId, new DemandInstanceValueObject(
                                            DemandInstanceTypeEnum.PROJECT, projectId, projectLocalName.get()), ArrayList::new);
                                }
                            }
                        }
                        break;
                    case SPACE:
                        queryParam = new DtpRwdObjectQueryParam().addWithColumn(
                                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
                        if (queryName) {
                            queryParam.addWithColumn(DtpRwdObjectDTO.Fields.localName);
                        }
                        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloorBySpaceId(instanceIds, queryParam);
                        if (CollectionUtils.isNotEmpty(floors)) {
                            Map<DemandInstanceTypeEnum, Collection<String>> tempFloorAndFloorIds =
                                    Collections.singletonMap(DemandInstanceTypeEnum.FLOOR, floors.stream()
                                            .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet()));
                            Map<String, List<DemandInstanceValueObject>> floorIdAndParents =
                                    mapInstanceIdAndAllParents(tempFloorAndFloorIds);
                            for (DtpRwdFloorDTO floor : floors) {
                                String floorId = floor.getId();
                                String localName = floor.getLocalName();
                                Set<String> relObjIds = floor.getRelObjIds();
                                if (CollectionUtils.isEmpty(relObjIds)) {
                                    continue;
                                }
                                for (String relObjId : relObjIds) {
                                    MapExecutor.dynamicPut(result, relObjId, new DemandInstanceValueObject(
                                            DemandInstanceTypeEnum.FLOOR, floorId, localName), ArrayList::new);
                                    List<DemandInstanceValueObject> floorParents = floorIdAndParents.get(floorId);
                                    if (CollectionUtils.isNotEmpty(floorParents)) {
                                        MapExecutor.dynamicPutAll(result, relObjId, floorParents, ArrayList::new);
                                    }
                                }
                            }
                        }
                        break;
                    default:
                }
            }, TtlExecutors.getTtlExecutor(executor)));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return result;
    }

    /**
     * 查询实例ID和所有上级实例映射（直接查询对象方式）
     * <p>
     * 内部逻辑改为直接查询对象，然后取对象中的floorId和buildingId来查询上级
     *
     * @param instanceTypeAndIds 实例类型 -> 实例ID
     * @param queryName          是否查询名称
     * @return 实例ID -> 上级实例
     * @since 7.260302.0
     */
    @Override
    public Map<String, List<DemandInstanceValueObject>> mapInstanceIdAndAllParentsByDirectQuery(
            @NonNull Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds,
            boolean queryName) {
        Map<String, List<DemandInstanceValueObject>> result = new ConcurrentHashMap<>();
        if (MapUtils.isEmpty(instanceTypeAndIds)) {
            return result;
        }
        String projectId = ContextUtil.getProjectId();
        AtomicReference<String> projectLocalName = new AtomicReference<>(null);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 项目查询与其他实例查询并行执行
        if (queryName) {
            futures.add(CompletableFuture.runAsync(() -> {
                Optional<DtpRwdObjectDTO> projectOpt = dtpRwdClientService.getObjectOpt(
                        new DtpRwdObjectQueryParam().setId(projectId).addWithColumn(
                                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName));
                projectOpt.ifPresent(item -> projectLocalName.set(item.getLocalName()));
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }

        for (Map.Entry<DemandInstanceTypeEnum, Collection<String>> entry : instanceTypeAndIds.entrySet()) {
            DemandInstanceTypeEnum instanceType = entry.getKey();
            Collection<String> instanceIds = entry.getValue();
            if (CollectionUtils.isEmpty(instanceIds)) {
                continue;
            }
            futures.add(CompletableFuture.runAsync(() -> {
                DtpRwdObjectQueryParam queryParam;
                switch (instanceType) {
                    case PROJECT:
                        break;
                    case BUILDING:
                        for (String instanceId : instanceIds) {
                            MapExecutor.dynamicPut(result, instanceId, new DemandInstanceValueObject(
                                    DemandInstanceTypeEnum.PROJECT, projectId, projectLocalName.get()), ArrayList::new);
                        }
                        break;
                    case FLOOR:
                        // 直接查询楼层对象
                        queryParam = new DtpRwdObjectQueryParam()
                                .setId(instanceIds)
                                .addWithColumn(DtpRwdFloorDTO.Fields.buildingId);
                        if (queryName) {
                            queryParam.addWithColumn(DtpRwdObjectDTO.Fields.localName);
                        }
                        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(queryParam);
                        if (CollectionUtils.isNotEmpty(floors)) {
                            // 只有需要名称时才查询建筑对象
                            Map<String, DtpRwdBuildingDTO> buildingIdAndBuilding = Collections.emptyMap();
                            if (queryName) {
                                Set<String> buildingIds = floors.stream()
                                        .map(DtpRwdFloorDTO::getBuildingId)
                                        .filter(StringUtils::isNotBlank)
                                        .collect(Collectors.toSet());
                                if (CollectionUtils.isNotEmpty(buildingIds)) {
                                    DtpRwdObjectQueryParam buildingQueryParam = new DtpRwdObjectQueryParam()
                                            .setId(buildingIds)
                                            .addWithColumn(DtpRwdObjectDTO.Fields.localName);
                                    buildingIdAndBuilding = dtpRwdClientService.listBuilding(buildingQueryParam)
                                            .stream()
                                            .collect(Collectors.toMap(DtpRwdBuildingDTO::getId, v -> v, (v1, v2) -> v1));
                                }
                            }
                            // 组装结果
                            for (DtpRwdFloorDTO floor : floors) {
                                String floorId = floor.getId();
                                String buildingId = floor.getBuildingId();
                                if (StringUtils.isBlank(buildingId)) {
                                    continue;
                                }
                                String buildingLocalName = null;
                                if (queryName) {
                                    DtpRwdBuildingDTO building = buildingIdAndBuilding.get(buildingId);
                                    buildingLocalName = building != null ? building.getLocalName() : null;
                                }
                                MapExecutor.dynamicPut(result, floorId, new DemandInstanceValueObject(
                                        DemandInstanceTypeEnum.BUILDING, buildingId, buildingLocalName), ArrayList::new);
                                MapExecutor.dynamicPut(result, floorId, new DemandInstanceValueObject(
                                        DemandInstanceTypeEnum.PROJECT, projectId, projectLocalName.get()), ArrayList::new);
                            }
                        }
                        break;
                    case SPACE:
                        // 直接查询空间对象
                        queryParam = new DtpRwdObjectQueryParam()
                                .setId(instanceIds)
                                .addWithColumn(DtpRwdSpaceDTO.Fields.buildingId, DtpRwdSpaceDTO.Fields.floorId);
                        if (queryName) {
                            queryParam.addWithColumn(DtpRwdObjectDTO.Fields.localName);
                        }
                        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpace(queryParam);
                        if (CollectionUtils.isNotEmpty(spaces)) {
                            // 只有需要名称时才查询楼层和建筑对象
                            Map<String, DtpRwdFloorDTO> floorIdAndFloor = new ConcurrentHashMap<>();
                            Map<String, DtpRwdBuildingDTO> buildingIdAndBuilding = new ConcurrentHashMap<>();

                            if (queryName) {
                                Set<String> floorIds = spaces.stream()
                                        .map(DtpRwdSpaceDTO::getFloorId)
                                        .filter(StringUtils::isNotBlank)
                                        .collect(Collectors.toSet());
                                Set<String> buildingIds = spaces.stream()
                                        .map(DtpRwdSpaceDTO::getBuildingId)
                                        .filter(StringUtils::isNotBlank)
                                        .collect(Collectors.toSet());

                                List<CompletableFuture<Void>> queryFutures = new ArrayList<>();

                                if (CollectionUtils.isNotEmpty(floorIds)) {
                                    queryFutures.add(CompletableFuture.runAsync(() -> {
                                        DtpRwdObjectQueryParam floorQueryParam = new DtpRwdObjectQueryParam()
                                                .setId(floorIds)
                                                .addWithColumn(DtpRwdObjectDTO.Fields.localName);
                                        Map<String, DtpRwdFloorDTO> floorMap = dtpRwdClientService.listFloor(floorQueryParam)
                                                .stream()
                                                .collect(Collectors.toMap(DtpRwdFloorDTO::getId, Function.identity(),
                                                        (v1, v2) -> v1));
                                        floorIdAndFloor.putAll(floorMap);
                                    }, BusinessThreadPoolExecutor.ttlExecutor()));
                                }

                                if (CollectionUtils.isNotEmpty(buildingIds)) {
                                    queryFutures.add(CompletableFuture.runAsync(() -> {
                                        DtpRwdObjectQueryParam buildingQueryParam = new DtpRwdObjectQueryParam()
                                                .setId(buildingIds)
                                                .addWithColumn(DtpRwdObjectDTO.Fields.localName);
                                        Map<String, DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuilding(buildingQueryParam)
                                                .stream()
                                                .collect(Collectors.toMap(DtpRwdBuildingDTO::getId, Function.identity(),
                                                        (v1, v2) -> v1));
                                        buildingIdAndBuilding.putAll(buildings);
                                    }, BusinessThreadPoolExecutor.ttlExecutor()));
                                }

                                CompletableFuture.allOf(queryFutures.toArray(new CompletableFuture[0])).join();
                            }

                            // 组装结果
                            for (DtpRwdSpaceDTO space : spaces) {
                                String spaceId = space.getId();
                                String floorId = space.getFloorId();
                                String buildingId = space.getBuildingId();
                                if (StringUtils.isNotBlank(floorId)) {
                                    String floorLocalName = null;
                                    if (queryName) {
                                        DtpRwdFloorDTO floor = floorIdAndFloor.get(floorId);
                                        floorLocalName = floor != null ? floor.getLocalName() : null;
                                    }
                                    MapExecutor.dynamicPut(result, spaceId, new DemandInstanceValueObject(
                                            DemandInstanceTypeEnum.FLOOR, floorId, floorLocalName), ArrayList::new);
                                }
                                if (StringUtils.isNotBlank(buildingId)) {
                                    String buildingLocalName = null;
                                    if (queryName) {
                                        DtpRwdBuildingDTO building = buildingIdAndBuilding.get(buildingId);
                                        buildingLocalName = building != null ? building.getLocalName() : null;
                                    }
                                    MapExecutor.dynamicPut(result, spaceId, new DemandInstanceValueObject(
                                            DemandInstanceTypeEnum.BUILDING, buildingId, buildingLocalName), ArrayList::new);
                                }
                                MapExecutor.dynamicPut(result, spaceId, new DemandInstanceValueObject(
                                        DemandInstanceTypeEnum.PROJECT, projectId, projectLocalName.get()), ArrayList::new);
                            }
                        }
                        break;
                    default:
                }
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return result;
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipmentInByFloorIds(EquipmentInByFloorIdQueryParam param) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setClassCode(param.getClassCodes());
        if (ArrayUtils.isEmpty(param.getWithColumns())) {
            queryParam.addWithColumn(DtpRwdObjectDTO.listSimpleWithColumns());
        }
        if (ArrayUtils.isNotEmpty(param.getAdditionalWithColumns())) {
            queryParam.addWithColumn(param.getAdditionalWithColumns());
        }
        List<String> monitoringSurveillanceStatus = param.getMonitoringSurveillanceStatus();
        if (CollectionUtils.isNotEmpty(monitoringSurveillanceStatus)) {
            queryParam.addInfoCode(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(), monitoringSurveillanceStatus);
        } else {
            queryParam.filterMonitoringSurveillanceStatus();
        }
        return this.dtpRwdClientService.listEquipmentInByFloorIds(param.getFloorIds(), queryParam);
    }

    @Override
    public String logTag() {
        return "实例服务";
    }
}
