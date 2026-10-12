package com.persagy.apccontrol.basic.service.infocodeconfig.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.InfoCodeConfigQueryContext;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.*;
import com.persagy.apccontrol.basic.model.param.rwd.TreeBuildingFloorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.infocode.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeTemplateTypeResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.service.infocodeconfig.InfoCodeConfigService;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeConfigQueryConfigValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName EquipmentInfoCodeConfigServiceImpl
 * @Description 信息点配置
 * @Author 薛涛
 * @Date 2024/6/28 17:56
 * @Version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
public class InfoCodeConfigServiceImpl implements InfoCodeConfigService, LogComponent {

    private final InfoCodeConverter infoCodeConverter;
    private final InfoCodeTemplateTypeResource infoCodeTemplateTypeResource;
    private final InfoCodeGateway infoCodeGateway;
    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final InstanceService instanceService;

    /**
     * 缓存对象模型共享 READY 状态服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService
            cacheObjectModelStatusService;

    /**
     * 新缓存对象主表批量读取服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRepository cacheObjectRepository;

    @Override
    public List<InfoCodeTemplateVO> listTemplateTypes(@NonNull InfoCodeConfigTemplateTypeQueryParam param) {
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询模版列表");
        try (PerformanceTracker.SafeNode safeNode =
                     tracker.track("InfoCodeConfigServiceImpl#listTemplateTypes")) {
            Boolean notQueryObjectCount = param.getNotQueryObjectCount();
            boolean filterObjects = CollectionUtils.isNotEmpty(param.getFilterObjectIds())
                    || CollectionUtils.isNotEmpty(param.getFilterInSystemIds());
            if (filterObjects) {
                notQueryObjectCount = false;
            }
            // 查询所有模版的对象数量
            List<InfoCodeTemplateTypeModel> templateTypeModels = BooleanUtils.isTrue(notQueryObjectCount)
                    ? infoCodeGateway.listTemplateTypes(infoCodeConverter.toTemplateQueryDaoParam(param))
                    : infoCodeGateway.listTemplateTypesWithObjects(infoCodeConverter.toTemplateQueryParam(param));
            safeNode.recordStep("查询模版");
            if (CollectionUtils.isEmpty(templateTypeModels)) {
                return Collections.emptyList();
            }
            infoCodeGateway.fillInfoCodeTemplateTypeModel(templateTypeModels);
            safeNode.recordStep("填充信息点模版类型模型");
            List<InfoCodeTemplateVO> result = templateTypeModels.stream()
                    .map(InfoCodeTemplateVO::of)
                    .collect(Collectors.toList());
            safeNode.recordStep("转换模版列表");
            return result;
        }
    }

    @Override
    public R<Collection<InfoCodeDefineModel>> listObjectIotInfoCodesWithRuleByTemplateType(
            @NonNull InfoCodeConfigIotWithRuleByTemplateTypeQueryParam param) {
        param.checkAndSet();
        Long templateTypeId = param.getTemplateTypeId();
        Set<String> groupCodes = param.getGroupCodes();
        Set<InfoCodeTypeEnum> infoCodeTypes = param.getInfoCodeTypes();
        Boolean filterControlObject = param.getFilterControlObject();
        Optional<InfoCodeTemplateType> templateTypeOpt = infoCodeTemplateTypeResource.get(templateTypeId);
        if (!templateTypeOpt.isPresent()) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
        }
        InfoCodeConfigByTemplateIdQueryParam queryParam = new InfoCodeConfigByTemplateIdQueryParam();
        queryParam.setTemplateTypeId(templateTypeId);
        queryParam.setFilterControlObject(filterControlObject);
        InfoCodeConfigQueryContext context = new InfoCodeConfigQueryContext(queryParam);
        // 填充模版类型
        context.fillTemplateType();
        if (context.isFailed()) {
            return R.successMulti(Collections.emptyList());
        }
        // 填充RWD对象
        context.fillRwdObjects();
        if (context.isFailed()) {
            return R.successMulti(Collections.emptyList());
        }
        InfoCodeInitByObjectParam initByObjectParam = context.toEquipmentInfoCodeInitByObjectParam();
        initByObjectParam.setGroupCodes(groupCodes);
        initByObjectParam.setInfoCodeTypes(infoCodeTypes);
        List<InfoCodeAggregate> aggregates = infoCodeGateway.of(initByObjectParam);
        if (context.isFailed()) {
            return R.successMulti(Collections.emptyList());
        }
        infoCodeGateway.fillInfoCodeDefineModel(aggregates);
        context.setAggregates(aggregates);
        TreeSet<InfoCodeAggregate> uniqueInfoCodeAggregates = new TreeSet<>(
                Comparator.comparingInt(value -> value.getConfigModel().getSequence()));
        uniqueInfoCodeAggregates.addAll(aggregates.stream().collect(Collectors.toMap(item ->
                item.getConfigModel().getInfoCode(), Function.identity(), (o1, o2) -> o1)).values());
        return R.successMulti(uniqueInfoCodeAggregates.stream()
                .map(InfoCodeAggregate::getInfoCodeAndDefineModel)
                .collect(Collectors.toList()));
    }

    @Override
    public R<Collection<InfoCodeObjectGroupVO>> listInfoCodeConfigsByTemplateType(
            @NonNull InfoCodeConfigByTemplateIdQueryParam param) {
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询-信息点配置-按模版ID");
        try (PerformanceTracker.SafeNode ignored =
                     tracker.track("InfoCodeConfigServiceImpl#listInfoCodeConfigsByTemplateType")) {
            InfoCodeConfigQueryContext context = new InfoCodeConfigQueryContext(param);
            // 填充模版类型
            context.fillTemplateType();
            if (context.isFailed()) {
                return R.successMulti(Collections.emptyList());
            }
            // 填充RWD对象
            context.fillRwdObjects();
            if (context.isFailed()) {
                return R.successMulti(Collections.emptyList());
            }
            // 过滤动态信息点
            context.filterDynamicInfoCode();
            if (context.isFailed()) {
                return R.successMulti(Collections.emptyList());
            }
            // 填充聚合根
            context.fillAggregates();
            if (context.isFailed()) {
                return R.successMulti(Collections.emptyList());
            }
            List<InfoCodeObjectGroupVO> result;
            try (PerformanceTracker.SafeNode convertNode = tracker.track("InfoCodeObjectGroupVO#of")) {
                result = InfoCodeObjectGroupVO.of(context.getAggregates());
                convertNode.recordStep("转换对象信息点分组");
            }
            if (CollectionUtils.isEmpty(result)) {
                return R.successMulti(Collections.emptyList());
            }
            return R.successMulti(result, context.getObjectCount().longValue());
        }
    }

    @Override
    public List<InfoCodeConfigOriginVO> listOriginInfoCodeConfigs(@NonNull InfoCodeConfigsOriginQueryParam param) {
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        Set<InfoCodeTemplateTypeEnum> templateTypes = param.getTemplateTypes();
        Set<String> classCodes = param.getClassCodes();

        // 构建查询参数
        InfoCodeTemplateTypeQueryDaoParam queryDaoParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryDaoParam.setRunningMonitorType(runningMonitorType);
        queryDaoParam.setRunningMonitorSecondType(runningMonitorSecondType);
        queryDaoParam.setTemplateTypes(templateTypes);
        queryDaoParam.setClassCodes(classCodes);

        // 查询模版类型列表
        List<InfoCodeTemplateTypeModel> templateTypeModels = infoCodeGateway.listTemplateTypes(queryDaoParam);
        if (CollectionUtils.isEmpty(templateTypeModels)) {
            return Collections.emptyList();
        }

        // 填充信息点模版类型模型
        infoCodeGateway.fillInfoCodeTemplateTypeModel(templateTypeModels);

        // 转换为VO
        List<InfoCodeConfigOriginVO> result = new ArrayList<>();
        for (InfoCodeTemplateTypeModel templateTypeModel : templateTypeModels) {
            InfoCodeConfigOriginVO vo = new InfoCodeConfigOriginVO();
            // 设置模版信息
            vo.setTemplate(InfoCodeTemplateVO.of(templateTypeModel));

            // 构建InfoCodeInitByObjectParam参数
            InfoCodeInitByObjectParam initParam = InfoCodeInitByObjectParam.ofVirtualObject(templateTypeModel);
            initParam.setRunningMonitorType(templateTypeModel.getRunningMonitorType());
            initParam.setRunningMonitorSecondType(templateTypeModel.getRunningMonitorSecondType());
            initParam.setTemplateTypeModels(Collections.singletonList(templateTypeModel));
            initParam.setFilterGroups(param.getFilterGroups());
            initParam.setInfoCodeTypes(param.getInfoCodeTypes());
            initParam.setQueryIotInfoCodeRule(false);
            initParam.setFillInfoCodeDefineModel(true);

            // 使用InfoCodeAggregateInitContext查询信息点配置和分组
            List<InfoCodeAggregate> aggregates = this.infoCodeGateway.ofOrigin(initParam);

            // 填充信息点定义模型
            if (CollectionUtils.isNotEmpty(aggregates)) {
                vo.setRelInfoCodeGroupConfigs(InfoCodeObjectGroupVO.ofOrigin(aggregates));
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public List<InfoCodeObjectGroupVO> listInfoCodeConfigsByObject(@NonNull InfoCodeConfigByObjectQueryParam param) {
        Boolean filterControlObject = param.getFilterControlObject();
        Set<String> objectIds = param.getObjectIds();
        Set<String> withColumns = param.getWithColumns();

        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId(objectIds);
        queryParam.setWithColumns(withColumns);
        if (!LocalContextStorage.isDeprecatedVersion()) {
            queryParam.addWithColumn(InfoCodeGateway.listObjectRequiredColumns());
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("按对象ID查询信息点配置");
        try (PerformanceTracker.SafeNode safeNode =
                     tracker.track("InfoCodeConfigServiceImpl#listInfoCodeConfigsByObject")) {
            List<DtpRwdObjectDTO> objects;
            if (this.shouldUseNewModel(param)) {
                // 新模型按接口 withColumns 查询并返回实际命中对象，不因单个对象缺失回退 RWD。
                objects = this.cacheObjectRepository.listByObjectIds(
                        objectIds, queryParam.getWithColumns());
                safeNode.recordStep("查询新对象模型");
            } else {
                objects = this.dtpRwdClientService.listObject(queryParam);
                safeNode.recordStep("查询RWD对象");
            }
            if (BooleanUtils.isTrue(filterControlObject)) {
                infoCodeGateway.filterControlObjects(objects);
                safeNode.recordStep("过滤管控对象");
            }
            if (CollectionUtils.isEmpty(objects)) {
                return Lists.newArrayList();
            }
            List<InfoCodeObjectGroupVO> result = queryInfoCodeObjectGroupVo(param, objects);
            safeNode.recordStep("查询信息点对象分组");
            return result;
        }
    }

    /**
     * 判断当前信息点配置查询是否应读取新版对象模型。
     *
     * <p>旧版本兼容、模板缓存许可和一级类型 READY 共同组成同一个路由契约，集中判断可避免
     * 后续调用点遗漏任一条件。任一条件不满足时继续使用原有 RWD 查询链路。</p>
     *
     * @param param 信息点配置对象查询参数
     * @return 是否读取新版对象模型
     * @since v7.260701.0
     */
    private boolean shouldUseNewModel(@NonNull InfoCodeConfigByObjectQueryParam param) {
        return !LocalContextStorage.isDeprecatedVersion()
                && InfoCodeTemplateTypeEnum.allowCache(
                        param.getRunningMonitorType(), param.getTemplateType())
                && this.cacheObjectModelStatusService.isReady(param.getRunningMonitorType());
    }

    @Override
    public List<InfoCodeObjectGroupVO> listInfoCodeConfigsByObjectFilters(InfoCodeConfigObjectByFiltersQueryParam param) {
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        InfoCodeTemplateTypeEnum templateType = param.getTemplateType();
        DtpRwdObjectTypeEnum objectType = param.getObjectType();
        Set<String> serviceSpaceIds = param.getServiceSpaceIds();
        Set<String> inSpaceIds = param.getInSpaceIds();
        String localName = param.getLocalName();
        Set<String> classCodes = param.getClassCodes();
        Boolean filterControlObject = param.getFilterControlObject();
        InfoCodeObjectQueryParam infoCodeObjectQueryParam = new InfoCodeObjectQueryParam();
        infoCodeObjectQueryParam.setRunningMonitorType(runningMonitorType);
        infoCodeObjectQueryParam.setRunningMonitorSecondType(runningMonitorSecondType);
        infoCodeObjectQueryParam.setTemplateTypes(Lists.newArrayList(templateType));
        infoCodeObjectQueryParam.setObjectType(objectType);
        infoCodeObjectQueryParam.setFilterControlObject(filterControlObject);
        infoCodeObjectQueryParam.setServiceSpaceIds(serviceSpaceIds);
        infoCodeObjectQueryParam.setInSpaceIds(inSpaceIds);
        infoCodeObjectQueryParam.setOrders(param.getOrders());
        if (StringUtils.isNotBlank(localName)) {
            infoCodeObjectQueryParam.setLocalName(localName);
        }
        if (CollectionUtils.isNotEmpty(classCodes)) {
            infoCodeObjectQueryParam.setClassCodes(classCodes);
        }
        List<DtpRwdObjectDTO> objects = infoCodeGateway.listTemplateTypeObjects(infoCodeObjectQueryParam);
        if (CollectionUtils.isEmpty(objects)) {
            return Lists.newArrayList();
        }
        Set<String> objectIds = objects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());

        InfoCodeConfigByObjectQueryParam infoCodeConfigByObjectQueryParam =
                infoCodeConverter.toByObjectQueryParam(param, objectIds);
        return queryInfoCodeObjectGroupVo(infoCodeConfigByObjectQueryParam, objects);
    }

    @Override
    public List<SpaceByBuildingIdGroupFloorVO> listSpaceByBuildingIdGroupFloor(SpaceByBuildingIdGroupFloorQueryParam param) {
        String projectId = ContextUtil.getProjectId();
        RunningMonitorStrategy analyzeStrategy = param.getRunningMonitorSecondType().toRunningMonitorTypeEnum().getRunningMonitorStrategy();
        RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
        spaceQueryParam.setFilterBuildingIds(Collections.singleton(param.getBuildingId()));
        // 根据策略查询对应的空间 （空间有楼层信息）
        List<DtpRwdSpaceDTO> spaces = analyzeStrategy.listSpaces(spaceQueryParam);
        List<SpaceByBuildingIdGroupFloorVO> spaceByBuildingIdGroupFloorVOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(spaces)) {
            return spaceByBuildingIdGroupFloorVOList;
        }
        // 空间和楼层对应map
        Map<String, String> spaceByFloorMap = new HashMap<>(16);
        // 楼层set
        Set<String> floorSet = new HashSet<>(16);
        for (DtpRwdSpaceDTO space : spaces) {
            String floorId = space.getFloorId();
            String spaceId = space.getId();
            spaceByFloorMap.put(spaceId, floorId);
            floorSet.add(floorId);
        }

        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.setId(floorSet);
        // 查楼层
        List<DtpRwdFloorDTO> dtpRwdObjectFloorDTOS = dtpRwdClientService
                .listFloor(false, false, projectId, dtpRwdObjectQueryParam);
        // 楼层倒叙排序
        dtpRwdObjectFloorDTOS.sort(Comparator.comparing(DtpRwdFloorDTO::getFloorSequenceId).reversed());
        // 查询空间信息点信息
        InfoCodeConfigByObjectQueryParam infoCodeConfigByObjectQueryParam = new InfoCodeConfigByObjectQueryParam();
        infoCodeConfigByObjectQueryParam.setRunningMonitorType(param.getRunningMonitorSecondType().toRunningMonitorTypeEnum());
        infoCodeConfigByObjectQueryParam.setObjectIds(Sets.newHashSet(spaceByFloorMap.keySet()));
        infoCodeConfigByObjectQueryParam.setTemplateType(param.getTemplateCode());
        List<InfoCodeObjectGroupVO> equipmentInfoCodeObjectGroupVOS = listInfoCodeConfigsByObject(infoCodeConfigByObjectQueryParam);

        // 楼层对应空间的信息点信息
        Map<String, List<InfoCodeObjectGroupVO>> floorEquipmentInfoCodeObjectGroupVOMap = new HashMap<>(16);
        for (InfoCodeObjectGroupVO equipmentInfoCodeObjectGroupVO : equipmentInfoCodeObjectGroupVOS) {
            String objectId = equipmentInfoCodeObjectGroupVO.getId();
            String floorId = spaceByFloorMap.get(objectId);
            List<InfoCodeObjectGroupVO> equipmentInfoCodeObjectGroupVOSMap = floorEquipmentInfoCodeObjectGroupVOMap.get(floorId);
            if (CollectionUtils.isEmpty(equipmentInfoCodeObjectGroupVOSMap)) {
                equipmentInfoCodeObjectGroupVOSMap = Lists.newArrayList(equipmentInfoCodeObjectGroupVO);
                floorEquipmentInfoCodeObjectGroupVOMap.put(floorId, equipmentInfoCodeObjectGroupVOSMap);
            } else {
                equipmentInfoCodeObjectGroupVOSMap.add(equipmentInfoCodeObjectGroupVO);
                floorEquipmentInfoCodeObjectGroupVOMap.put(floorId, equipmentInfoCodeObjectGroupVOSMap);
            }
        }
        for (DtpRwdObjectDTO dtpRwdObjectFloorDTO : dtpRwdObjectFloorDTOS) {
            SpaceByBuildingIdGroupFloorVO spaceByBuildingIdGroupFloorVO = new SpaceByBuildingIdGroupFloorVO();
            spaceByBuildingIdGroupFloorVO.setFloorId(dtpRwdObjectFloorDTO.getId());
            spaceByBuildingIdGroupFloorVO.setFloorLocalName(dtpRwdObjectFloorDTO.getLocalName());
            spaceByBuildingIdGroupFloorVO.setFloorLocalId(dtpRwdObjectFloorDTO.getLocalId());
            List<InfoCodeObjectGroupVO> equipmentInfoCodeObjectGroupVOSFloor = floorEquipmentInfoCodeObjectGroupVOMap.get(dtpRwdObjectFloorDTO.getId());
            spaceByBuildingIdGroupFloorVO.setSpaces(equipmentInfoCodeObjectGroupVOSFloor);
            spaceByBuildingIdGroupFloorVOList.add(spaceByBuildingIdGroupFloorVO);
        }
        return spaceByBuildingIdGroupFloorVOList;
    }

    @Override
    public List<BuildingAndSpaceGroupFloorVO> listBuildingAndSpaceByProjectIdGroupFloor(BuildingAndSpaceByProjectIdQueryParam param) {
        String projectId = ContextUtil.getProjectId();
        RunningMonitorStrategy analyzeStrategy = param.getRunningMonitorSecondType().toRunningMonitorTypeEnum().getRunningMonitorStrategy();
        DemandInstanceValueObject valueObject = new DemandInstanceValueObject();
        valueObject.setInstanceType(DemandInstanceTypeEnum.PROJECT);
        valueObject.setInstanceId(param.getProjectId());
        // 根据策略查询对应的空间 （空间有楼层信息）
        List<DtpRwdSpaceDTO> spaces = analyzeStrategy.listSpaces(param.getRunningMonitorSecondType(), valueObject);

        if (CollectionUtils.isEmpty(spaces)) {
            return Lists.newArrayList();
        }
        // 空间和楼层对应map
        Map<String, String> spaceByFloorMap = new HashMap<>(16);
        // 楼层set
        Set<String> floorSet = new HashSet<>(16);
        // 建筑id
        Set<String> buildingIdSet = new HashSet<>(16);
        for (DtpRwdSpaceDTO space : spaces) {
            String floorId = space.getFloorId();
            String spaceId = space.getId();
            String buildingId = space.getBuildingId();
            spaceByFloorMap.put(spaceId, floorId);
            floorSet.add(floorId);
            buildingIdSet.add(buildingId);
        }
        if (CollectionUtils.isEmpty(buildingIdSet)) {
            return Lists.newArrayList();
        }

        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.setId(floorSet);
        // 查楼层
        List<DtpRwdFloorDTO> dtpRwdObjectFloorDTOS = dtpRwdClientService
                .listFloor(false, false, projectId, dtpRwdObjectQueryParam);


        // 楼层倒叙排序
        dtpRwdObjectFloorDTOS.sort(Comparator.comparing(DtpRwdFloorDTO::getFloorSequenceId).reversed());
        // 查询空间信息点信息
        InfoCodeConfigByObjectQueryParam infoCodeConfigByObjectQueryParam = new InfoCodeConfigByObjectQueryParam();
        infoCodeConfigByObjectQueryParam.setRunningMonitorType(param.getRunningMonitorSecondType().toRunningMonitorTypeEnum());
        infoCodeConfigByObjectQueryParam.setObjectIds(Sets.newHashSet(spaceByFloorMap.keySet()));
        infoCodeConfigByObjectQueryParam.setTemplateType(param.getTemplateCode());
        List<InfoCodeObjectGroupVO> equipmentInfoCodeObjectGroupVOS = listInfoCodeConfigsByObject(infoCodeConfigByObjectQueryParam);

        // 楼层对应空间的信息点信息
        Map<String, List<InfoCodeObjectGroupVO>> floorEquipmentInfoCodeObjectGroupVOMap = new HashMap<>(16);
        for (InfoCodeObjectGroupVO equipmentInfoCodeObjectGroupVO : equipmentInfoCodeObjectGroupVOS) {
            String objectId = equipmentInfoCodeObjectGroupVO.getId();
            String floorId = spaceByFloorMap.get(objectId);
            List<InfoCodeObjectGroupVO> equipmentInfoCodeObjectGroupVOSMap = floorEquipmentInfoCodeObjectGroupVOMap.get(floorId);
            if (CollectionUtils.isEmpty(equipmentInfoCodeObjectGroupVOSMap)) {
                equipmentInfoCodeObjectGroupVOSMap = Lists.newArrayList(equipmentInfoCodeObjectGroupVO);
                floorEquipmentInfoCodeObjectGroupVOMap.put(floorId, equipmentInfoCodeObjectGroupVOSMap);
            } else {
                equipmentInfoCodeObjectGroupVOSMap.add(equipmentInfoCodeObjectGroupVO);
                floorEquipmentInfoCodeObjectGroupVOMap.put(floorId, equipmentInfoCodeObjectGroupVOSMap);
            }
        }
        List<DtpRwdBuildingDTO> dtpRwdBuildingDTOS = dtpRwdClientService.listBuilding(false, false, projectId, dtpRwdObjectQueryParam.setId(buildingIdSet));
        Map<String, BuildingAndSpaceGroupFloorVO> buildingAndSpaceGroupFloorVOMap = new HashMap<>();
        for (DtpRwdBuildingDTO dtpRwdBuildingDTO : dtpRwdBuildingDTOS) {
            BuildingAndSpaceGroupFloorVO buildingAndSpaceGroupFloorVO = new BuildingAndSpaceGroupFloorVO();
            buildingAndSpaceGroupFloorVO.setBuildingId(dtpRwdBuildingDTO.getId());
            buildingAndSpaceGroupFloorVO.setBuildingName(dtpRwdBuildingDTO.getName());
            buildingAndSpaceGroupFloorVO.setBuildingLocalName(dtpRwdBuildingDTO.getLocalName());
            buildingAndSpaceGroupFloorVO.setBuildingLocalId(dtpRwdBuildingDTO.getLocalId());
            buildingAndSpaceGroupFloorVOMap.put(dtpRwdBuildingDTO.getId(), buildingAndSpaceGroupFloorVO);
        }
        for (DtpRwdFloorDTO dtpRwdObjectFloorDTO : dtpRwdObjectFloorDTOS) {
            SpaceByBuildingIdGroupFloorVO spaceByBuildingIdGroupFloorVO = new SpaceByBuildingIdGroupFloorVO();
            spaceByBuildingIdGroupFloorVO.setFloorId(dtpRwdObjectFloorDTO.getId());
            spaceByBuildingIdGroupFloorVO.setFloorLocalName(dtpRwdObjectFloorDTO.getLocalName());
            spaceByBuildingIdGroupFloorVO.setFloorLocalId(dtpRwdObjectFloorDTO.getLocalId());
            List<InfoCodeObjectGroupVO> equipmentInfoCodeObjectGroupVOSFloor = floorEquipmentInfoCodeObjectGroupVOMap.get(dtpRwdObjectFloorDTO.getId());
            spaceByBuildingIdGroupFloorVO.setSpaces(equipmentInfoCodeObjectGroupVOSFloor);
            String buildingId = dtpRwdObjectFloorDTO.getBuildingId();
            BuildingAndSpaceGroupFloorVO buildingAndSpaceGroupFloorVO = buildingAndSpaceGroupFloorVOMap.get(buildingId);
            List<SpaceByBuildingIdGroupFloorVO> floors = buildingAndSpaceGroupFloorVO.getFloors();
            if (CollectionUtils.isEmpty(floors)) {
                floors = Lists.newArrayList();
            }
            floors.add(spaceByBuildingIdGroupFloorVO);
            buildingAndSpaceGroupFloorVO.setFloors(floors);
            buildingAndSpaceGroupFloorVOMap.put(buildingId, buildingAndSpaceGroupFloorVO);
        }
        return Lists.newArrayList(buildingAndSpaceGroupFloorVOMap.values());
    }

    /**
     * 查询信息点对象分组VO
     *
     * @param param   查询参数
     * @param objects 对象
     * @return 信息点对象分组VO
     * @since 5.240802.0
     */
    private List<InfoCodeObjectGroupVO> queryInfoCodeObjectGroupVo(
            @NonNull InfoCodeConfigByObjectQueryParam param,
            @NonNull Collection<DtpRwdObjectDTO> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return Lists.newArrayList();
        }
        InfoCodeInitByObjectParam infoCodeInitByObjectParam = infoCodeConverter.toInitByObjectParam(param);
        infoCodeInitByObjectParam.setObjects(objects);
        List<InfoCodeAggregate> aggregates = infoCodeGateway.of(infoCodeInitByObjectParam);
        infoCodeGateway.fillInfoCodeDefineModel(aggregates);
        Map<String, Object> additionalDataValues = new HashMap<>();
        param.paramTimeToConditions(additionalDataValues);
        if (MapUtils.isNotEmpty(param.getPlaceholders())) {
            additionalDataValues.put(BaseInfoCodeAnalyzeStrategy.PLACEHOLDERS, param.getPlaceholders());
        }
        infoCodeGateway.fillInfoCodeDataModel(aggregates, additionalDataValues);
        return InfoCodeObjectGroupVO.of(aggregates);
    }

    /**
     * 过滤动态信息点
     *
     * @param context 上下文
     * @since 5.240602.0
     */
    @SuppressWarnings("unchecked")
    private void filterDynamicInfoCode(InfoCodeConfigQueryContext context) {
        String projectId = ContextUtil.getProjectId();
        List<DynamicInfoCodeFilter> dynamicInfoCodeFilters = context.getParam().getDynamicInfoCodeFilters();
        List<DtpRwdObjectDTO> objects = context.getObjects();
        if (CollectionUtils.isEmpty(objects) || CollectionUtils.isEmpty(dynamicInfoCodeFilters)) {
            return;
        }
        // 对象ID -> 对象
        Map<String, DtpRwdObjectDTO> objectIdAndData = objects.stream().collect(Collectors.toMap(
                DtpRwdObjectDTO::getId, Function.identity(), (o1, o2) -> o1));
        // 信息点编码 -> 信息点筛选条件
        Map<String, DynamicInfoCodeFilter> infoCodeAndFilter = dynamicInfoCodeFilters.stream().collect(Collectors.toMap(
                DynamicInfoCodeFilter::getInfoCode, Function.identity(), (o1, o2) -> o1));
        // 查询信息点值
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValue(
                projectId, null, objectIdAndData.keySet(), infoCodeAndFilter.keySet());
        if (CollectionUtils.isEmpty(infoCodeValues)) {
            context.setFailed(true);
            context.setObjects(Collections.emptyList());
            context.setObjectCount(-1);
            return;
        }
        List<DtpRwdObjectDTO> objectResult = new ArrayList<>();
        // 对象id---》》信息点code---》》信息点数据
        Map<String, Map<String, InfoCodeValueVO>> equipmentInfoCodeValueVOMap = new HashMap<>(16);
        for (InfoCodeValueVO infoCodeValue : infoCodeValues) {
            String objectId = infoCodeValue.getObjectId();
            String infoCode = infoCodeValue.getInfoCode();
            Map<String, InfoCodeValueVO> infoCodeValueVOMap = equipmentInfoCodeValueVOMap.get(objectId);
            if (MapUtils.isEmpty(infoCodeValueVOMap)) {
                infoCodeValueVOMap = new HashMap<>();
            }
            infoCodeValueVOMap.put(infoCode, infoCodeValue);
            equipmentInfoCodeValueVOMap.put(objectId, infoCodeValueVOMap);
        }

        for (String objectId : equipmentInfoCodeValueVOMap.keySet()) {
            Map<String, InfoCodeValueVO> infoCodeValueVOMap = equipmentInfoCodeValueVOMap.get(objectId);
            Boolean aBoolean = handleInfoCodeFilter(infoCodeValueVOMap, dynamicInfoCodeFilters);
            if (aBoolean) {
                DtpRwdObjectDTO dtpRwdObjectDTO = objectIdAndData.get(objectId);
                objectResult.add(dtpRwdObjectDTO);
            }
        }
        if (CollectionUtils.isEmpty(objectResult)) {
            context.setFailed(true);
            context.setObjects(Collections.emptyList());
            context.setObjectCount(-1);
            return;
        }
        context.setObjects(objectResult);
    }

    private Boolean handleInfoCodeFilter(Map<String, InfoCodeValueVO> infoCodeValueVOMap, List<DynamicInfoCodeFilter> dynamicInfoCodeFilters) {

        boolean flag = true;
        for (DynamicInfoCodeFilter dynamicInfoCodeFilter : dynamicInfoCodeFilters) {
            InfoCodeValueVO infoCodeValueVO = infoCodeValueVOMap.get(dynamicInfoCodeFilter.getInfoCode());
            // 当过滤条件存在且和IOT值不匹配时, 删除对象  有BUG（如果信息点为空则不会被过滤掉）改为第二种
            // 符合条件得拿出来返回
            if (Objects.isNull(infoCodeValueVO)) {
                flag = false;
            } else if (Objects.isNull(infoCodeValueVO.getInfoCodeValue())) {
                flag = false;
            } else if (!Objects.equals(infoCodeValueVO.getInfoCodeValue(), dynamicInfoCodeFilter.getDataValue())) {
                flag = false;
            }
        }

        return flag;

    }

    @Override
    public Set<String> getClassCodesForTwinSubscribe() {
        List<InfoCodeTemplateType> list = infoCodeTemplateTypeResource.list();
        return list.stream().map(InfoCodeTemplateType::getClassCode).collect(Collectors.toSet());
    }

    @Override
    public Set<InfoCodeTemplateTreeVO> treeTemplateType(InfoCodeConfigTemplateTypeQueryParam param) {
        /**
         * 组装建筑楼层空间树；
         * 处理和空间的关系（所在空间和服务空间）；
         * 处理自定义名称；
         * 处理多个classCode合并；
         **/
        Set<InfoCodeTemplateTreeVO> treeTemplateTypeNodes = new HashSet<>();
        Map<String, InfoCodeTemplateTreeVO> treeTemplateTypeNodeMap = new HashMap<>();
        TreeBuildingFloorSpaceQueryParam queryParam = new TreeBuildingFloorSpaceQueryParam();
        queryParam.setIsQuerySpace(false);
        List<DtpRwdBuildingDTO> dtpRwdBuildingDTOS = instanceService.treeBuildingFloorSpace(queryParam);
        if (CollectionUtil.isEmpty(dtpRwdBuildingDTOS)) {
            return treeTemplateTypeNodes;
        }

        // 构建节点map
        dtpRwdBuildingDTOS.stream().forEach(item -> {
            InfoCodeTemplateTreeVO treeVO = new InfoCodeTemplateTreeVO();
            treeVO.setNodeName(item.getLocalName());
            treeVO.setNodeId(item.getId());
            treeVO.setSequence(item.getBuildingSequenceId());
            treeTemplateTypeNodeMap.put(item.getId(), treeVO);

            List<DtpRwdFloorDTO> floors = item.getFloors();
            if (CollectionUtil.isNotEmpty(floors)) {
                floors.stream().forEach(floor -> {
                    InfoCodeTemplateTreeVO floorTreeVO = new InfoCodeTemplateTreeVO();
                    floorTreeVO.setNodeName(floor.getLocalName());
                    floorTreeVO.setNodeId(floor.getId());
                    floorTreeVO.setSequence(floor.getFloorSequenceId());
                    treeTemplateTypeNodeMap.put(floor.getId(), floorTreeVO);
                });
            }
        });

        // 获取模版配置
        List<InfoCodeTemplateTypeModel> infoCodeTemplateTypeModels = infoCodeGateway.listTemplateTypesWithObjects(infoCodeConverter.toTemplateQueryParam(param));
        if (CollectionUtil.isEmpty(infoCodeTemplateTypeModels)) {
            return treeTemplateTypeNodes;
        }

        Set<DtpRwdObjectDTO> objects = new HashSet<>();
        infoCodeTemplateTypeModels.stream().forEach(infoCodeTemplateTypeModel -> {
            objects.addAll(infoCodeTemplateTypeModel.getObjects());
        });
        InfoCodeInitByObjectParam infoCodeInitByObjectParam = new InfoCodeInitByObjectParam();
        infoCodeInitByObjectParam.setTemplateTypeModels(infoCodeTemplateTypeModels);
        infoCodeInitByObjectParam.setRunningMonitorType(param.getRunningMonitorType());
        infoCodeInitByObjectParam.setRunningMonitorSecondType(param.getRunningMonitorSecondType());
        infoCodeInitByObjectParam.setQueryIotInfoCodeRule(false);
        infoCodeInitByObjectParam.setObjects(objects);

        // 过滤对象附加信息,查所在空间和服务空间
        Collection<InfoCodeConfigQueryConfigValueObject> filterGroups = new ArrayList<>();
        InfoCodeConfigQueryConfigValueObject filterGroup = new InfoCodeConfigQueryConfigValueObject();
        filterGroup.setGroupCode("objectAdditionalInfo");
        filterGroup.setInfoCodes(Sets.newHashSet("inSpaceIds", "serviceSpaceIds"));
        filterGroups.add(filterGroup);

        infoCodeInitByObjectParam.setFilterGroups(filterGroups);
        List<InfoCodeAggregate> infoCodeAggregates = infoCodeGateway.of(infoCodeInitByObjectParam);
        infoCodeGateway.fillInfoCodeDataModel(infoCodeAggregates, null);
        if (CollectionUtil.isEmpty(infoCodeAggregates)) {
            return treeTemplateTypeNodes;
        }

        for (InfoCodeAggregate infoCodeAggregate : infoCodeAggregates) {
            InfoCodeDataModel infoCodeAndDataModel = infoCodeAggregate.getInfoCodeAndDataModel();
            String infoCode = infoCodeAndDataModel.getInfoCode();
            List<InfoCodeRwdObjectDetailDTO> infoCodeRwdObjectDetailDTOS = null;
            try {
                infoCodeRwdObjectDetailDTOS = (List<InfoCodeRwdObjectDetailDTO>) infoCodeAndDataModel.getDataValue();
            } catch (ClassCastException e) {
                logProjectInstance().error("数据转换异常", e);
                continue;
            }
            if (CollectionUtil.isEmpty(infoCodeRwdObjectDetailDTOS)) {
                continue;
            }

            infoCodeRwdObjectDetailDTOS.forEach(infoCodeRwdObjectDetailDTO -> {
                Map<String, Object> undefinedProperties = infoCodeRwdObjectDetailDTO.getUndefinedProperties();
                if (MapUtil.isEmpty(undefinedProperties)) {
                    return;
                }

                String floorId = (String) undefinedProperties.get("floorId");
                String spaceId = infoCodeRwdObjectDetailDTO.getId();

                InfoCodeTemplateTreeVO floorNode = treeTemplateTypeNodeMap.get(floorId);
                if (floorNode != null) {
                    Map<String, Set<String>> spaceMap = floorNode.getSpaceIds() == null ? new HashMap<>() : floorNode.getSpaceIds();
                    MapExecutor.dynamicPut(spaceMap, infoCode, spaceId, HashSet::new);
                    floorNode.setSpaceIds(spaceMap);

                    Set<String> equipmentIds = floorNode.getEquipmentIds() == null ? new HashSet<>() : floorNode.getEquipmentIds();
                    if (CollectionUtil.isNotEmpty(infoCodeRwdObjectDetailDTO.getRelObjIds())) {
                        equipmentIds.addAll(infoCodeRwdObjectDetailDTO.getRelObjIds());
                    }
                    floorNode.setEquipmentIds(equipmentIds);
                }
            });
        }

        dtpRwdBuildingDTOS.forEach(building -> {
            InfoCodeTemplateTreeVO buildingNode = treeTemplateTypeNodeMap.get(building.getId());
            Set<String> allEquipmentIds = new HashSet<>();
            if (buildingNode != null) {
                buildingNode.setChildren(new ArrayList<>());
                if (CollectionUtil.isEmpty(building.getFloors())) {
                    return;
                }

                building.getFloors().forEach(floor -> {
                    InfoCodeTemplateTreeVO floorNode = treeTemplateTypeNodeMap.get(floor.getId());
                    Set<String> equipmentIds = floorNode.getEquipmentIds();
                    if (CollectionUtils.isNotEmpty(equipmentIds)) {
                        floorNode.setTotalObjectCount(equipmentIds.size());
                        allEquipmentIds.addAll(equipmentIds);
                        floorNode.setEquipmentIds(null);
                        buildingNode.getChildren().add(floorNode);
                    }
                });
                buildingNode.setTotalObjectCount(allEquipmentIds.size());
                if (buildingNode.getTotalObjectCount() > 0) {
                    treeTemplateTypeNodes.add(buildingNode);
                }
            }
        });
        return treeTemplateTypeNodes;
    }

    @Override
    public String logTag() {
        return "信息点配置服务";
    }
}
