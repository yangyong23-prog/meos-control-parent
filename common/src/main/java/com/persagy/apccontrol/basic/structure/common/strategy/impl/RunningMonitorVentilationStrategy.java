package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdCriteriaSignEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSpaceRoomFuncTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigResultBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigResultVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationDtpRwdEquipmentVO;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationDtpRwdSpaceVO;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import com.persagy.apccontrol.basic.service.ventilation.VentilationService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheRwdDomainService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation.DemandConfigVentilationLogisticsValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation.DemandConfigVentilationParkingValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation.DemandConfigVentilationToiletValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.ventilation.converter.VentilationConverter;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通风策略实现
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Component(RunningMonitorVentilationStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorVentilationStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorVentilationStrategy";
    public static final String LOGISTICS_FUNCTION_TYPE = "SFT-02.";
    public static final String PARKING_FUNCTION_TYPE = "SFT-04.";
    public static final String TOILET_FUNCTION_TYPE = "SFT-01.06.";
    public static final String DOME_FUNCTION_TYPE = "SFT-01.05.";
    /**
     * 风机设备类白名单：EQUIPMENT_SERVICE_SPACE 关系读取时仅接纳这些设备类
     */
    private static final Set<String> FAN_CLASS_CODES = Sets.newHashSet(
            DtpRwdClassCodeEnum.FFSCMF.getValue(),
            DtpRwdClassCodeEnum.FFSCAF.getValue(),
            DtpRwdClassCodeEnum.ACVTSF.getValue());
    /**
     * 窗类设备类白名单：EQUIPMENT_IN_SPACE 关系读取时仅接纳这些设备类（PRD-290902）
     */
    private static final Set<String> WINDOW_CLASS_CODES = Sets.newHashSet(
            DtpRwdClassCodeEnum.CFCSWN.getValue(),
            DtpRwdClassCodeEnum.CFCSEW.getValue());
    private final DtpRwdClientService dtpRwdClientService;
    private final VentilationConverter ventilationConverter;
    private final ObjectMapper objectMapper;
    private final DemandConfigGateway<BaseDemandConfig> demandConfigGateway;
    private final MidCalendarClientService midCalendarClientService;
    private final RedisCacheTemplate redisCacheTemplate;
    private final InfoCodeGateway infoCodeGateway;
    private final CacheRwdDomainService cacheRwdDomainService;
    private final com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService cacheObjectModelStatusService;
    private final com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository cacheObjectRepository;
    private final com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository cacheObjectRelRepository;


    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.VENTILATION;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return InheritConfigTypeEnum.FIELD;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(
            @NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {

        DemandInstanceTypeEnum instanceType = instance.getInstanceType();
        String instanceId = instance.getInstanceId();
        String roomFuncType;
        switch (runningMonitorSecondType) {
            case LOGISTICS_VENTILATION:
                roomFuncType = LOGISTICS_FUNCTION_TYPE;
                break;
            case PARKING_VENTILATION:
                roomFuncType = PARKING_FUNCTION_TYPE;
                break;
            case TOILET_VENTILATION:
                roomFuncType = TOILET_FUNCTION_TYPE;
                break;
            case DOME_VENTILATION:
                roomFuncType = DOME_FUNCTION_TYPE;
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }

        // 查询指定设备类下所有对象ID
        List<DtpRwdEquipmentDTO> dtpRwdEquipments = dtpRwdClientService.listEquipment(
                new DtpRwdObjectQueryParam()
                        .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode, "funcType")
                        .setClassCodeEnum(VentilationService.ALL_EQUIPMENT_CLASS_CODES)
        );
        if (CollectionUtils.isEmpty(dtpRwdEquipments)) {
            return Lists.newArrayList();
        }
        //筛选设备通风风机（ACVTSF）  功能类型（funcType）取值为2“排风机”  功能类型（funcType）取值为1“送风机
        Set<String> equipmentIds = new HashSet<>();
        for (DtpRwdEquipmentDTO dtpRwdEquipmentDTO : dtpRwdEquipments) {
            String classCode = dtpRwdEquipmentDTO.getClassCode();
            String id = dtpRwdEquipmentDTO.getId();
            if (classCode.equals(DtpRwdClassCodeEnum.ACVTSF.getValue())) {
                Map<String, Object> undefinedProperties = dtpRwdEquipmentDTO.getUndefinedProperties();
                if (MapUtils.isEmpty(undefinedProperties)) {
                    continue;
                }
                Object funcType = undefinedProperties.get("funcType");
                if (Objects.isNull(funcType)) {
                    continue;
                }
                if ("1".equals(funcType) || "2".equals(funcType)) {
                    equipmentIds.add(id);
                }
            } else {
                equipmentIds.add(id);
            }
        }
        if (CollectionUtils.isEmpty(equipmentIds)) {
            return Lists.newArrayList();
        }
        // 根据设备服务空间查询空间实例
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .addInfoCode("roomFuncType", DtpRwdCriteriaSignEnum.LIKE_RIGHT, roomFuncType)
                .addWithColumnWithDefault(DtpRwdSpaceDTO.Fields.class, withColumns);
        switch (instanceType) {
            case BUILDING:
                queryParam.setId(querySpaceInBuilding(Lists.newArrayList(instanceId), roomFuncType));
                break;
            case FLOOR:
                queryParam.setId(querySpacesInFloor(Lists.newArrayList(instanceId), roomFuncType));
                break;
        }
        List<DtpRwdSpaceDTO> fanSpaces = dtpRwdClientService.listSpaceServiceByEquipmentId(equipmentIds, queryParam);

        // PRD-290902: DOME_VENTILATION 额外查询窗类设备所在穹顶空间，与风机服务空间取并集
        if (runningMonitorSecondType == RunningMonitorSecondTypeEnum.DOME_VENTILATION) {
            Set<String> windowEquipIds = dtpRwdEquipments.stream()
                    .filter(e -> DtpRwdClassCodeEnum.CFCSWN.getValue().equals(e.getClassCode())
                            || DtpRwdClassCodeEnum.CFCSEW.getValue().equals(e.getClassCode()))
                    .map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(windowEquipIds)) {
                DtpRwdObjectQueryParam windowQueryParam = new DtpRwdObjectQueryParam()
                        .addInfoCode("roomFuncType", DtpRwdCriteriaSignEnum.LIKE_RIGHT, roomFuncType)
                        .addWithColumnWithDefault(DtpRwdSpaceDTO.Fields.class, withColumns);
                switch (instanceType) {
                    case BUILDING:
                        windowQueryParam.setId(querySpaceInBuilding(Lists.newArrayList(instanceId), roomFuncType));
                        break;
                    case FLOOR:
                        windowQueryParam.setId(querySpacesInFloor(Lists.newArrayList(instanceId), roomFuncType));
                        break;
                }
                List<DtpRwdSpaceDTO> windowSpaces = dtpRwdClientService.listSpaceInByEquipmentId(
                        windowEquipIds, windowQueryParam);
                Set<String> fanSpaceIds = fanSpaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
                for (DtpRwdSpaceDTO ws : windowSpaces) {
                    if (fanSpaceIds.add(ws.getId())) {
                        fanSpaces.add(ws);
                    }
                }
            }
        }
        return fanSpaces;
    }

    @Override
    public List<? extends DtpRwdEquipmentDTO> listEquipments(@NonNull @Valid RunningMonitorEquipmentQueryParam param) {
        Set<String> equipmentWithColumns = Sets.newHashSet(DtpRwdObjectDTO.Fields.id,
                DtpRwdObjectDTO.Fields.classCode, DtpRwdObjectDTO.Fields.localName,
                DtpRwdObjectDTO.Fields.localId, DtpRwdObjectDTO.Fields.name,
                VentilationDtpRwdEquipmentVO.Fields.funcType);

        if (CollectionUtils.isNotEmpty(param.getWithColumns())) {
            equipmentWithColumns.addAll(param.getWithColumns());
        }
        RedisCacheTemplate.MethodCall<List<DtpRwdEquipmentDTO>> methodToCache =
                () -> getDtpRwdEquipmentDTOS(param.getFilterEquipmentServiceSpaceIds(),
                        param.getFilterFloorIds(), equipmentWithColumns);
        //缓存热刷新
        return redisCacheTemplate.asyncRefreshTemplate("listEquipmentsVENTILATION", param, methodToCache);
    }

    /**
     * 缓存构建专用设备查询，返回原始 DtpRwdEquipmentDTO（含 filtrationId 过滤）。
     *
     * <p>该方法直接查询 RWD，不经过任何缓存层，专用于新版缓存对象模型构建写入。</p>
     *
     * @param spaceIds              空间 ID 集合
     * @param equipmentWithColumns  设备查询字段
     * @return 过滤后的原始设备列表
     * @since v7.260701.0
     */
    public List<DtpRwdEquipmentDTO> listEquipmentsForCacheBuild(
            @NonNull Set<String> spaceIds, @NonNull Set<String> equipmentWithColumns) {
        return getDtpRwdEquipmentDTOS(spaceIds, null, equipmentWithColumns);
    }


    /**
     * 查询建筑物下的空间
     *
     * @param filterBuildingIds 过滤建筑ID
     * @return {@link List }<{@link DtpRwdEquipmentDTO }>
     * @since 5.240702.0
     */
    private Set<String> querySpaceInBuilding(
            @Nullable Collection<String> filterBuildingIds, @NonNull String roomFuncType) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addInfoCode("roomFuncType", DtpRwdCriteriaSignEnum.LIKE_RIGHT, roomFuncType);
        param.addWithColumn(DtpRwdObjectDTO.Fields.id);
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpaceByBuildingId(filterBuildingIds, param);
        if (CollectionUtils.isEmpty(spaces)) {
            logProjectInstance(ContextUtil.getProjectId()).warn("无有效建筑下的设备", Pair.of("filterBuildingIds", filterBuildingIds));
            return SetUtils.emptySet();
        }
        return spaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
    }

    /**
     * 查询楼层下的空间
     *
     * @param filterFloorIds 筛选楼层ID, 为空时查询所有
     * @return 设备列表, {@code relObjIds} 字段为关联楼层ID
     * @since 5.240702.0
     */
    private Set<String> querySpacesInFloor(
            @Nullable Collection<String> filterFloorIds, @NonNull String roomFuncType) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addInfoCode("roomFuncType", DtpRwdCriteriaSignEnum.LIKE_RIGHT, roomFuncType);
        param.addWithColumn(DtpRwdObjectDTO.Fields.id);
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpaceByFloorId(filterFloorIds, param);
        if (CollectionUtils.isEmpty(spaces)) {
            logProjectInstance(ContextUtil.getProjectId()).warn("无有效楼层下的设备", Pair.of("filterFloorIds", filterFloorIds));
            return SetUtils.emptySet();
        }
        return spaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> exceptionFilterCondition() {
        return x -> x.stream().filter(item -> {
            boolean riskConditions = item.getObjectType().equals(ObjectTypeEnum.SPACE.value())
                    && CollectionUtils.isNotEmpty(item.getRiskMeasureAimLevel2Collect())
                    && item.getRiskMeasureAimLevel2Collect().stream().anyMatch(RISK_EVENT_LEVEL_SECOND_LIST::contains);
            return riskConditions || item.getObjectType().equals(ObjectTypeEnum.EQUIPMENT.value());
        }).collect(Collectors.toList());
    }

    @Override
    public Collection<RiskMeasureAimLevel2> exceptionFilterRiskMeasureAimLevel2() {
        return Lists.newArrayList(RiskMeasureAimLevel2.E5, RiskMeasureAimLevel2.E34, RiskMeasureAimLevel2.E35);
    }


    @Override
    public List<? extends DtpRwdSpaceVO> listDashboardSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        RedisCacheTemplate.MethodCall<List<VentilationDtpRwdSpaceVO>> methodToCache =
                () -> getVentilationDtpRwdSpaceVOS(param);

        // READY 后走新缓存读取路径
        if (cacheObjectModelStatusService.isReady(RunningMonitorTypeEnum.VENTILATION)) {
            return getVentilationDtpRwdSpaceVOSFromNewCache();
        }
        logProjectInstance().info("未命中缓存，直接查询获取通风空间数据");
        try {
            return methodToCache.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从新缓存读取并组装 VentilationDtpRwdSpaceVO。
     *
     * <p>READY 后从新缓存表读取原始 DtpRwdSpaceDTO 和 DtpRwdEquipmentDTO，
     * 通过 cache_object_rel 表读取设备→空间关系，再实时查询需求配置和空调季，
     * 最后在内存中组装复合 VO。</p>
     *
     * @return 组装后的通风空间 VO 列表
     * @since v7.260701.0
     */
    private List<VentilationDtpRwdSpaceVO> getVentilationDtpRwdSpaceVOSFromNewCache() {
        // ❶ 从新缓存读取原始空间 DTO
        // 模型图字段不属于对象主表契约，3D 地图定位字段由模型视图字段接口独立提供。
        Set<String> spaceWithColumns = new LinkedHashSet<>(
                CacheObjectExtensionFieldConfig.listObjectQueryFields(DtpRwdClassCodeEnum.SPACE.value()));
        spaceWithColumns.add(DtpRwdObjectDTO.Fields.name);
        List<DtpRwdSpaceDTO> spaceList = cacheObjectRepository.listByCacheKey(
                CacheObjectModelKeyConfig.VENTILATION_LIST_SPACE,
                DtpRwdSpaceDTO.class, spaceWithColumns);
        if (CollectionUtils.isEmpty(spaceList)) {
            return new ArrayList<>();
        }
        Set<String> spaceIds = spaceList.stream()
                .map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());

        // ❷ 从新缓存读取原始设备 DTO
        // 显式字段替代全字段恢复：基础列加对象扩展字段，不读回模型图字段。
        Set<String> equipmentWithColumns = new LinkedHashSet<>();
        equipmentWithColumns.addAll(CacheObjectExtensionFieldConfig.listObjectQueryFields(
                DtpRwdClassCodeEnum.FFSCMF.value()));
        equipmentWithColumns.addAll(CacheObjectExtensionFieldConfig.listObjectQueryFields(
                DtpRwdClassCodeEnum.FFSCAF.value()));
        equipmentWithColumns.addAll(CacheObjectExtensionFieldConfig.listObjectQueryFields(
                DtpRwdClassCodeEnum.ACVTSF.value()));
        // PRD-290902: 窗类设备扩展字段白名单
        equipmentWithColumns.addAll(CacheObjectExtensionFieldConfig.listObjectQueryFields(
                DtpRwdClassCodeEnum.CFCSWN.value()));
        equipmentWithColumns.addAll(CacheObjectExtensionFieldConfig.listObjectQueryFields(
                DtpRwdClassCodeEnum.CFCSEW.value()));
        equipmentWithColumns.add(DtpRwdObjectDTO.Fields.name);
        List<DtpRwdEquipmentDTO> equipments = cacheObjectRepository.listByCacheKey(
                CacheObjectModelKeyConfig.VENTILATION_LIST_EQUIPMENT,
                DtpRwdEquipmentDTO.class, equipmentWithColumns);

        // ❸ 从 cache_object_rel 表读取设备→空间关系
        // (graphCode, relCode) 作用域为各监控模块共享：机房安防会向 EQUIPMENT_SERVICE_SPACE
        // 写入其全量设备（含窗类）的关系行，因此风机与窗类两段读取均须按设备类白名单过滤，
        // 且同一空间内按设备 ID 去重，避免其他模块写入的关系行混入并造成设备重复
        CacheObjectRelQueryDaoParam relParam = new CacheObjectRelQueryDaoParam();
        relParam.setRelObjectIds(spaceIds);
        relParam.setGraphCode(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE.value().getLeft());
        relParam.setRelCode(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE.value().getRight());
        List<CacheObjectRel> rels = cacheObjectRelRepository.listReverse(relParam);

        // 构建 spaceId → equipmentIds 映射
        Map<String, List<DtpRwdEquipmentDTO>> mapRelObjIdAndData = new HashMap<>();
        Map<String, Set<String>> mappedEquipmentIds = new HashMap<>();
        Map<String, DtpRwdEquipmentDTO> equipMap = equipments.stream()
                .collect(Collectors.toMap(DtpRwdEquipmentDTO::getId, Function.identity(), (o1, o2) -> o1));
        // 风机 via EQUIPMENT_SERVICE_SPACE（服务空间），仅接纳风机设备类
        appendEquipmentsToSpace(rels, equipMap, FAN_CLASS_CODES,
                mapRelObjIdAndData, mappedEquipmentIds);

        // PRD-290902: 第二次查询窗类设备 via EQUIPMENT_IN_SPACE 关系，合并到同一映射
        CacheObjectRelQueryDaoParam windowRelParam = new CacheObjectRelQueryDaoParam();
        windowRelParam.setRelObjectIds(spaceIds);
        windowRelParam.setGraphCode(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE.value().getLeft());
        windowRelParam.setRelCode(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE.value().getRight());
        List<CacheObjectRel> windowRels = cacheObjectRelRepository.listReverse(windowRelParam);
        // 窗类 via EQUIPMENT_IN_SPACE（所在空间），仅接纳窗类设备类
        appendEquipmentsToSpace(windowRels, equipMap, WINDOW_CLASS_CODES,
                mapRelObjIdAndData, mappedEquipmentIds);

        // ❹ 查询需求配置
        Map<String, Map<DemandTemplateTypeEnum, DemandConfigResultVO<BaseDemandConfig>>> spaceTemplateCfg =
                new ConcurrentHashMap<>();
        getStringMapMap(spaceTemplateCfg);

        // ❺ 查询空调季
        MidCalendarAirSeasonTypeEnum airSeasonType =
                midCalendarClientService.getAirSeasonType(ContextUtil.getProjectId(), DateUtils.now().toLocalDate());

        // ❻ 内存组装 VentilationDtpRwdSpaceVO
        List<VentilationDtpRwdSpaceVO> result = new ArrayList<>();
        spaceList.forEach(space -> {
            List<DtpRwdEquipmentDTO> equipmentDTOs = mapRelObjIdAndData.getOrDefault(space.getId(),
                    Collections.emptyList());
            if (!equipmentDTOs.isEmpty()) {
                VentilationDtpRwdSpaceVO vo = ventilationConverter.toVentilationDtpRwdSpaceVO(space);
                vo.setRelObjects(objectMapper.convertValue(equipmentDTOs,
                        new TypeReference<List<VentilationDtpRwdEquipmentVO>>() {
                        }));
                putCfg(space, spaceTemplateCfg, vo, airSeasonType);
                result.add(vo);
            }
        });

        return result;
    }

    /**
     * 将关系行反查到的设备按设备类白名单合并进空间→设备映射。
     *
     * <p>cache_object_rel 的 (graphCode, relCode) 作用域为各监控模块共享，
     * 机房安防等模块会向 EQUIPMENT_SERVICE_SPACE 写入全量设备（含窗类）的关系行，
     * 因此风机与窗类两段读取都必须按设备类过滤，避免其他模块写入的关系行混入；
     * 同一空间内按设备 ID 去重，防止同一设备重复出现。</p>
     *
     * @param rels               关系行（设备 → 空间）
     * @param equipMap           设备 ID → 设备 DTO 映射
     * @param acceptClassCodes   本段关系接纳的设备类集合
     * @param mapRelObjIdAndData 空间 ID → 设备列表（跨关系类型累积输出）
     * @param mappedEquipmentIds 空间 ID → 已合入的设备 ID 集合（跨关系类型去重）
     * @since v7.260802.3
     */
    private void appendEquipmentsToSpace(List<CacheObjectRel> rels,
            Map<String, DtpRwdEquipmentDTO> equipMap, Set<String> acceptClassCodes,
            Map<String, List<DtpRwdEquipmentDTO>> mapRelObjIdAndData,
            Map<String, Set<String>> mappedEquipmentIds) {
        for (CacheObjectRel rel : rels) {
            String spaceId = rel.getRelObjectId();
            String equipmentId = rel.getObjectId();
            DtpRwdEquipmentDTO equip = equipMap.get(equipmentId);
            if (equip == null || !acceptClassCodes.contains(equip.getClassCode())) {
                continue;
            }
            Set<String> equipmentIds = mappedEquipmentIds.computeIfAbsent(spaceId, k -> new HashSet<>());
            if (equipmentIds.add(equipmentId)) {
                mapRelObjIdAndData.computeIfAbsent(spaceId, k -> new ArrayList<>()).add(equip);
            }
        }
    }

    private List<VentilationDtpRwdSpaceVO> getVentilationDtpRwdSpaceVOS(RunningMonitorSpaceQueryParam param) {
        List<VentilationDtpRwdSpaceVO> ventilationDtpRwdSpaceVO = new ArrayList<>();

        //查询空间
        Set<String> withColumns = Sets.newHashSet(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode,
                DtpRwdObjectDTO.Fields.localName
                , DtpRwdObjectDTO.Fields.localId, DtpRwdObjectDTO.Fields.name, DtpRwdSpaceDTO.Fields.floorId,
                DtpRwdSpaceDTO.Fields.roomFuncType,
                DtpRwdSpaceDTO.Fields.buildingId);
        if (CollectionUtils.isNotEmpty(param.getWithColumns())) {
            withColumns.addAll(param.getWithColumns());
        }
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setWithColumns(withColumns);
        List<DtpRwdSpaceDTO> spaceList = dtpRwdClientService.listSpace(queryParam);
        if (CollectionUtils.isEmpty(spaceList)) {
            return ventilationDtpRwdSpaceVO;
        }
        Set<String> spaceIds = spaceList.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());

        //空间 -> 模版 -> 配置
        Map<String, Map<DemandTemplateTypeEnum, DemandConfigResultVO<BaseDemandConfig>>> spaceTemplateCfg =
                new ConcurrentHashMap<>();
        //获取配置
        getStringMapMap(spaceTemplateCfg);
        //查询设备
        Set<String> equipmentWithColumns = Sets.newHashSet(DtpRwdObjectDTO.Fields.id,
                DtpRwdObjectDTO.Fields.classCode, DtpRwdObjectDTO.Fields.localName
                , DtpRwdObjectDTO.Fields.localId, DtpRwdObjectDTO.Fields.name, DtpRwdEquipmentDTO.Fields.floorId,
                DtpRwdEquipmentDTO.Fields.bimLocation,
                VentilationDtpRwdEquipmentVO.Fields.monitoringSurveillanceStatus,
                VentilationDtpRwdEquipmentVO.Fields.funcType, DtpRwdEquipmentDTO.Fields.buildingId,
                DtpRwdObjectDTO.Fields.relObjIds);
        List<DtpRwdEquipmentDTO> equipments = getDtpRwdEquipmentDTOS(spaceIds, null, equipmentWithColumns);

        Map<String, List<DtpRwdEquipmentDTO>> mapRelObjIdAndData = DtpRwdEquipmentDTO.mapRelObjIdAndData(equipments);
        //查询当前时间的空调季
        MidCalendarAirSeasonTypeEnum airSeasonType =
                midCalendarClientService.getAirSeasonType(ContextUtil.getProjectId(), DateUtils.now().toLocalDate());

        //构建返回对象 排除固定设备 空间功能类型 = SFT-01.05.00 排除设备 classCode = ACVTSF /FFSCMF 对象转换为 VentilationDtpRwdSpaceVO
        // 构建返回对象
        spaceList.forEach(space -> {
            List<DtpRwdEquipmentDTO> equipmentDTOs = mapRelObjIdAndData.getOrDefault(space.getId(),
                    Collections.emptyList());
            // 如果空间下有设备(过滤后的)，则转换为VO对象
            if (!equipmentDTOs.isEmpty()) {
                VentilationDtpRwdSpaceVO vo = ventilationConverter.toVentilationDtpRwdSpaceVO(space);
                vo.setRelObjects(objectMapper.convertValue(equipmentDTOs,
                        new TypeReference<List<VentilationDtpRwdEquipmentVO>>() {
                }));
                putCfg(space, spaceTemplateCfg, vo, airSeasonType);
                ventilationDtpRwdSpaceVO.add(vo);
            }
        });

        return ventilationDtpRwdSpaceVO;
    }

    /**
     * 空间维度 查询传递进来空间 ID 下的设备
     * 楼层维度,直接查询楼层下的设备
     * 项目纬度 查询全项目的设备
     *
     * @param spaceIds
     * @return
     */
    private List<DtpRwdEquipmentDTO> getDtpRwdEquipmentDTOS(Set<String> spaceIds, Set<String> floorIds,
                                                            Set<String> equipmentWithColumns) {
        //查询空间下的设备
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        //获取需要过滤的设备 ID
        Set<String> filtrationIds = new HashSet<>();
        futures.add(CompletableFuture.runAsync(() -> filtrationIds.addAll(filtrationId()),
                BusinessThreadPoolExecutor.ttlExecutor()));
        // 三个并行分支并发 addAll，必须使用线程安全集合（join 后为单线程遍历，无需外部同步）
        List<DtpRwdEquipmentDTO> equipments = Collections.synchronizedList(new ArrayList<>());

        futures.add(CompletableFuture.runAsync(() -> {
            Set<DtpRwdClassCodeEnum> classCode = Sets.newHashSet(DtpRwdClassCodeEnum.FFSCMF,
                    DtpRwdClassCodeEnum.FFSCAF);
            DtpRwdObjectQueryParam equipmentQueryParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(classCode)
                    .setWithColumns(equipmentWithColumns);
            objFiltration(spaceIds, floorIds, equipmentQueryParam);
            equipmentQueryParam.filterMonitoringSurveillanceStatus();

            equipments.addAll(dtpRwdClientService.listEquipment(equipmentQueryParam));
        }, BusinessThreadPoolExecutor.ttlExecutor()));

        //通风风机（ACVTSF）
        futures.add(CompletableFuture.runAsync(() -> {
            Set<DtpRwdClassCodeEnum> classCode = Sets.newHashSet(DtpRwdClassCodeEnum.ACVTSF);
            DtpRwdObjectQueryParam equipmentQueryParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(classCode)
                    .setWithColumns(equipmentWithColumns);
            equipmentQueryParam.filterMonitoringSurveillanceStatus();
            equipmentQueryParam.addInfoCode(VentilationDtpRwdEquipmentVO.Fields.funcType, Sets.newHashSet("1", "2"));
            objFiltration(spaceIds, floorIds, equipmentQueryParam);
            equipments.addAll(dtpRwdClientService.listEquipment(equipmentQueryParam));
        }, BusinessThreadPoolExecutor.ttlExecutor()));

        // PRD-290902: 窗类设备（CFCSWN/CFCSEW）via EQUIPMENT_IN_SPACE 关系关联穹顶空间
        futures.add(CompletableFuture.runAsync(() -> {
            Set<DtpRwdClassCodeEnum> classCode = Sets.newHashSet(DtpRwdClassCodeEnum.CFCSWN,
                    DtpRwdClassCodeEnum.CFCSEW);
            DtpRwdObjectQueryParam equipmentQueryParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(classCode)
                    .setWithColumns(equipmentWithColumns);
            equipmentQueryParam.filterMonitoringSurveillanceStatus();
            // 查询穹顶空间（SFT-01.05.*）作为窗所在空间
            DtpRwdObjectQueryParam domeSpaceParam = new DtpRwdObjectQueryParam()
                    .addWithColumn(DtpRwdObjectDTO.Fields.id)
                    .addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType,
                            DtpRwdCriteriaSignEnum.LIKE_RIGHT, DOME_FUNCTION_TYPE);
            Set<String> domeSpaceIds = dtpRwdClientService.listSpace(domeSpaceParam).stream()
                    .map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(domeSpaceIds)) {
                equipmentQueryParam.setRelationFrom(
                        new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE)
                                .setObjTo(domeSpaceIds));
                equipments.addAll(dtpRwdClientService.listEquipment(equipmentQueryParam));
            }
        }, BusinessThreadPoolExecutor.ttlExecutor()));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        //删除需要过滤的设备 ID
        equipments.removeIf(equipment -> filtrationIds.contains(equipment.getId()));
        // 并发写入已结束，脱去同步包装再返回，避免 GenericJackson2JsonRedisSerializer
        // 将 Collections$SynchronizedRandomAccessList 固化为缓存类型信息导致反序列化失败
        return new ArrayList<>(equipments);
    }

    /**
     * 对象过滤
     *
     * @param spaceIds
     * @param floorIds
     * @param equipmentQueryParam
     */
    private void objFiltration(Set<String> spaceIds, Set<String> floorIds, DtpRwdObjectQueryParam equipmentQueryParam) {
        if (CollectionUtils.isNotEmpty(floorIds)) {
            equipmentQueryParam.addInfoCode(DtpRwdSpaceDTO.Fields.floorId, floorIds);
        }
        if (CollectionUtils.isNotEmpty(spaceIds)) {
            equipmentQueryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjTo(spaceIds));
        }
    }

    /**
     * 查询出需要过滤的设备 ID
     * PRD-290902: 废除 SFT-01.05.00 剔除规则，窗类设备纳入穹顶空间后不再排除服务挑空空间的风机
     */
    private Set<String> filtrationId() {
        // 原逻辑：查询 SFT-01.05.00 挑空的顶层空间，剔除服务该空间的 ACVTSF/FFSCMF 设备
        // return equipments.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
        return Collections.emptySet();
    }

    /**
     * 获取需求配置
     *
     */
    private void getStringMapMap(Map<String, Map<DemandTemplateTypeEnum, DemandConfigResultVO<BaseDemandConfig>>> spaceTemplateCfg) {
        //查询配置
        List<DemandTemplateTypeEnum> templateTypeList = Arrays.asList(DemandTemplateTypeEnum.AIRCONDITIONING,
                DemandTemplateTypeEnum.LOGISTICS_VENTILATION,
                DemandTemplateTypeEnum.PARKING_VENTILATION,
                DemandTemplateTypeEnum.TOILET_VENTILATION
        );
        List<CompletableFuture<Void>> futures = new ArrayList<>();


        templateTypeList.forEach(templateType -> {
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                // 直接查询预计算需求配置结果，无需先查空间和模板
                DemandConfigResultBatchQueryParam queryCfgParam = new DemandConfigResultBatchQueryParam();
                queryCfgParam.setTemplateType(templateType);
                queryCfgParam.setDate(DateUtils.now().toLocalDate());
                queryCfgParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
                List<DemandConfigResultVO<BaseDemandConfig>> existsDemandSpaceConfigs =
                        demandConfigGateway.listConfigResultsByInstances(queryCfgParam);
                existsDemandSpaceConfigs.forEach(item -> spaceTemplateCfg
                        .computeIfAbsent(item.getInstanceId(), k -> new ConcurrentHashMap<>())
                        .put(templateType, item));
            }, BusinessThreadPoolExecutor.ttlExecutor());
            futures.add(task);
        });
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * 补充配置
     *
     * @param space
     * @param spaceTemplateCfg
     * @param vo
     */
    private void putCfg(DtpRwdSpaceDTO space, Map<String, Map<DemandTemplateTypeEnum,
                                DemandConfigResultVO<BaseDemandConfig>>> spaceTemplateCfg, VentilationDtpRwdSpaceVO vo,
                        MidCalendarAirSeasonTypeEnum airSeasonType) {
        //添加空间的配置
        Map<DemandTemplateTypeEnum, DemandConfigResultVO<BaseDemandConfig>> cfgMap =
                spaceTemplateCfg.getOrDefault(space.getId(), Collections.emptyMap());
        cfgMap.forEach((templateType, cfg) -> {
            DemandConfigValueObject<BaseDemandConfig> demandConfig = cfg.getFinalConfig();
            if (demandConfig == null || demandConfig.getGlobalConfig() == null) {
                return;
            }
            if (DemandTemplateTypeEnum.AIRCONDITIONING.equals(templateType)) {
                DemandConfigAirconditioningValueObject c =
                        (DemandConfigAirconditioningValueObject) demandConfig.getGlobalConfig();
                if (airSeasonType != null && c.getEnvironmentQuality() != null && c.getEnvironmentQuality().getTdbQuality() != null &&
                        CollectionUtils.isNotEmpty(c.getEnvironmentQuality().getTdbQuality().getSeasonQuality())) {
                    List<DemandConfigAirconditioningTdbSeasonValueObject> sq =
                            c.getEnvironmentQuality().getTdbQuality().getSeasonQuality();
                    sq.forEach(i -> {
                        if (airSeasonType.equals(i.getAirSeason())) {
                            vo.setTdbQuality(i);
                        }
                    });
                }
                if (c.getEnvironmentQuality() != null) {
                    vo.setCo2Quality(c.getEnvironmentQuality().getCO2Quality());
                }
            }
            if (DemandTemplateTypeEnum.LOGISTICS_VENTILATION.equals(templateType)) {
                DemandConfigVentilationLogisticsValueObject c =
                        (DemandConfigVentilationLogisticsValueObject) demandConfig.getGlobalConfig();
                vo.setO2Target(c.getO2Target());
            }
            if (DemandTemplateTypeEnum.PARKING_VENTILATION.equals(templateType)) {
                DemandConfigVentilationParkingValueObject c =
                        (DemandConfigVentilationParkingValueObject) demandConfig.getGlobalConfig();
                vo.setCoTarget(c.getCOTarget());
            }
            if (DemandTemplateTypeEnum.TOILET_VENTILATION.equals(templateType)) {
                DemandConfigVentilationToiletValueObject c =
                        (DemandConfigVentilationToiletValueObject) demandConfig.getGlobalConfig();
                vo.setH2sTarget(c.getH2STarget());
                vo.setNh3Target(c.getNH3Target());
            }
        });
    }


    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "运行监测-通风策略";
    }
}
