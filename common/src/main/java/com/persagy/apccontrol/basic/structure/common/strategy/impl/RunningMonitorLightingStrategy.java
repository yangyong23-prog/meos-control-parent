package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.convert.LightingCircuitConvert;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingCircuitDetailDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.EquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.SpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.LightingCacheObjectRelationService;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 照明监测策略实现
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Component(RunningMonitorLightingStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorLightingStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorLightingStrategy";

    /**
     * 改造前照明回路缓存实际返回的固定主体字段。
     *
     * <p>旧缓存读取不使用当前请求的 {@code withColumns}，而是直接反序列化构建时保存的完整
     * DTO。READY 分支继续使用这组字段，保证新旧缓存切换时接口响应不因请求参数变化而少字段。</p>
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<String> CACHE_CIRCUIT_FIELDS = ImmutableSet.of(
            DtpRwdObjectDTO.Fields.id,
            DtpRwdEquipmentDTO.Fields.buildingId,
            DtpRwdCircuitDTO.Fields.serviceAreaType,
            DtpRwdCircuitDTO.Fields.funcType,
            DtpRwdCircuitDTO.Fields.lampFuncType,
            DtpRwdObjectDTO.Fields.localName,
            DtpRwdEquipmentDTO.Fields.floorId,
            DtpRwdObjectDTO.Fields.classCode,
            InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value());

    /**
     * 改造前照明服务空间关系实际返回字段。
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<String> CACHE_RELATION_SPACE_FIELDS = ImmutableSet.of(
            DtpRwdObjectDTO.Fields.id,
            DtpRwdObjectDTO.Fields.localName,
            DtpRwdObjectDTO.Fields.classCode,
            DtpRwdEquipmentDTO.Fields.floorId,
            DtpRwdSpaceDTO.Fields.roomFuncType,
            InfoCodeEnum.REPLACED_BY_NAT_LIGHTING.getCode());

    /**
     * 改造前照明服务楼层关系实际返回字段。
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<String> CACHE_RELATION_FLOOR_FIELDS = ImmutableSet.of(
            DtpRwdObjectDTO.Fields.id,
            DtpRwdObjectDTO.Fields.localName,
            DtpRwdObjectDTO.Fields.classCode,
            DtpRwdEquipmentDTO.Fields.buildingId,
            DtpRwdEquipmentDTO.Fields.floorId);

    /**
     * 改造前照明控制模块和 DDC 关系实际返回字段。
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<String> CACHE_RELATION_CONTROL_OBJECT_FIELDS =
            ImmutableSet.of(
                    DtpRwdObjectDTO.Fields.id,
                    DtpRwdObjectDTO.Fields.localName,
                    DtpRwdObjectDTO.Fields.classCode,
                    DtpRwdEquipmentDTO.Fields.floorId);

    // 室内照明
    public static final List<DemandTemplateTypeEnum> PUBLIC_TEMPLATE_TYPE = Lists.newArrayList(
            DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING, DemandTemplateTypeEnum.PARKING_LOT_LIGHTING);
    // 夜景照明：楼体、景观、标识
    public static final List<DemandTemplateTypeEnum> OUT_DOOR_LIGHTING_TEMPLATE_TYPE = Lists.newArrayList(DemandTemplateTypeEnum.BUILDING_LIGHTING,
            DemandTemplateTypeEnum.LANDSCAPE_LIGHTING, DemandTemplateTypeEnum.SIGN_LIGHTING);
    // 所有照明，包含室内照明和夜景照明
    public static final List<DemandTemplateTypeEnum> LIGHTING_TEMPLATE_TYPE = CollectionUtils.collate(PUBLIC_TEMPLATE_TYPE, OUT_DOOR_LIGHTING_TEMPLATE_TYPE);


    private final DemandLightingStrategy demandLightingStrategy;
    private final DtpRwdClientService dtpRwdClientService;
    private final LightingCircuitConvert lightingCircuitConvert;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    /**
     * 缓存对象模型共享 READY 状态服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService
            cacheObjectModelStatusService;

    /**
     * 新缓存对象仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRepository cacheObjectRepository;

    /**
     * 新缓存对象关系仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRelRepository cacheObjectRelRepository;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.LIGHTING;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return InheritConfigTypeEnum.OBJECT;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        List<RunningMonitorSecondTypeEnum> runningMonitorSecondTypes = param.getRunningMonitorSecondTypes();
        List<DemandTemplateTypeEnum> demandTemplateTypes;
        // 存在二级类型时, 转换为需求模版类型, 不存在二级类型时, 查询非夜景照明空间
        if (CollectionUtils.isNotEmpty(runningMonitorSecondTypes)) {
            demandTemplateTypes = runningMonitorSecondTypes.stream()
                    .map(RunningMonitorSecondTypeEnum::toDemandTemplateTypeEnum)
                    .filter(item -> !item.isNightLighting())
                    .collect(Collectors.toList());
        } else {
            demandTemplateTypes = Arrays.stream(DemandTemplateTypeEnum.values())
                    .filter(item -> !item.isNightLighting())
                    .collect(Collectors.toList());
        }
        EquipmentQueryParam equipmentQueryParam = new EquipmentQueryParam();
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuits(
                demandTemplateTypes, equipmentQueryParam, param);
        if (CollectionUtils.isEmpty(circuits)) {
            return Lists.newArrayList();
        }
        Map<String, DtpRwdSpaceDTO> spaceIdAndData = new LinkedHashMap<>();
        for (DtpRwdCircuitDTO circuit : circuits) {
            List<DtpRwdSpaceDTO> serviceSpaces = circuit.getServiceSpaces();
            if (CollectionUtils.isEmpty(serviceSpaces)) {
                continue;
            }
            for (DtpRwdSpaceDTO serviceSpace : serviceSpaces) {
                spaceIdAndData.putIfAbsent(serviceSpace.getId(), serviceSpace);
            }
        }
        return new ArrayList<>(spaceIdAndData.values());
    }

    @Override
    public List<? extends DtpRwdSpaceVO> listDashboardSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        DemandStrategy demandStrategy = param.getRunningMonitorSecondType().toDemandTemplateTypeEnum().toDemandTypeEnum().demandStrategy();
        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(param.getRunningMonitorSecondType().toDemandTemplateTypeEnum());
        queryParam.setFilterObjectIds(param.getFilterObjectIds());
        queryParam.setFilterBuildingIds(param.getFilterBuildingIds());
        queryParam.setFilterFloorIds(param.getFilterFloorIds());
        queryParam.setWithColumns(param.getWithColumns());
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.localName);
        queryParam.addWithColumn(DtpRwdSpaceDTO.Fields.floorId);
        queryParam.addWithColumn(DtpRwdSpaceDTO.Fields.buildingId);
        List<DtpRwdSpaceDTO> spaces = demandStrategy.listDemandSpace(queryParam);
        return BeanUtil.copyToList(spaces, DtpRwdSpaceVO.class);
    }

    @Override
    public List<LightingCircuitDetailDTO> listEquipments(@NonNull @Valid RunningMonitorEquipmentQueryParam param) {
        if (param.isMysqlCache()
                && this.cacheObjectModelStatusService.isReady(
                        RunningMonitorTypeEnum.LIGHTING)) {
            List<RunningMonitorSecondTypeEnum> secondTypes = param.getRunningMonitorSecondTypes();
            if (CollectionUtils.isEmpty(secondTypes)) {
                // 未指定二级类型与实时分支一致，表示按固定顺序查询全部五类照明。
                secondTypes = RunningMonitorLightingStrategy.LIGHTING_TEMPLATE_TYPE.stream()
                        .map(DemandTemplateTypeEnum::toRunningMonitorSecondTypeEnum)
                        .collect(Collectors.toList());
            }
            List<String> cacheKeys = secondTypes.stream()
                    .map(CacheObjectModelKeyConfig
                            .LIGHTING_LIST_EQUIPMENT_BY_SECOND_TYPE::get)
                    .collect(Collectors.toList());
            // READY 是本次请求的最终路由：空结果也不回退 RWD。这里沿用旧缓存的固定
            // 返回字段，不把请求 withColumns 作为新旧缓存之间的响应差异来源。
            Map<String, List<LightingCircuitDetailDTO>> circuitsByCacheKey =
                    this.cacheObjectRepository.mapByCacheKeys(
                            cacheKeys, LightingCircuitDetailDTO.class,
                            RunningMonitorLightingStrategy.CACHE_CIRCUIT_FIELDS);
            List<LightingCircuitDetailDTO> cachedCircuits = new ArrayList<>();
            for (String cacheKey : cacheKeys) {
                cachedCircuits.addAll(circuitsByCacheKey.get(cacheKey));
            }
            // READY 后空集合直接作为新模型结果，避免继续查询关系和知识库。
            if (CollectionUtils.isEmpty(cachedCircuits)) {
                return Collections.emptyList();
            }
            // 关系、派生名称和主体固定字段都是该接口既有的业务返回内容，不能因 READY
            // 后切换缓存而消失；主体字段已按旧缓存契约限定查询范围。
            this.enrichCachedCircuitRelations(cachedCircuits);
            // 展示名称来自知识库码表，不属于 RWD 对象或关系缓存，读取时实时恢复。
            this.setInfoCodeName(cachedCircuits);
            return cachedCircuits;
        }
        List<LightingCircuitDetailDTO> circuits = listCircuits(param);
        // 并行执行两个方法
        CompletableFuture<Void> filterFuture = CompletableFuture.runAsync(() -> filterByRelController(circuits), BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture<Void> setRelFuture = CompletableFuture.runAsync(() -> setRelDDCode(circuits), BusinessThreadPoolExecutor.ttlExecutor());

        // 等待两个并行任务完成
        CompletableFuture.allOf(filterFuture, setRelFuture).join();

        return circuits;
    }

    /**
     * 使用新版关系表为缓存回路恢复服务空间、楼层、控制模块和 DDC。
     *
     * <p>该方法只由 READY 缓存分支调用，调用前已经保证回路集合非空。关系或目标对象未
     * 查询到时只保留实际命中结果，不回退旧缓存或 RWD。</p>
     *
     * @param circuits 已从新版对象表恢复的照明回路
     * @since v7.260701.0
     */
    private void enrichCachedCircuitRelations(
            @NonNull List<LightingCircuitDetailDTO> circuits) {
        Set<String> circuitIds = new LinkedHashSet<>();
        for (LightingCircuitDetailDTO circuit : circuits) {
            circuitIds.add(circuit.getId());
        }
        Map<String, List<String>> spaceIds = this.mapForwardRelationTargets(
                circuitIds, DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE);
        Map<String, List<String>> floorIds = this.mapForwardRelationTargets(
                circuitIds, DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_FLOOR);
        Map<String, List<String>> controllerIds = this.mapReverseRelationSubjects(
                circuitIds, DtpRwdRelationTypeEnum.EQUIPMENT_CONTROLLER);
        Map<String, List<String>> ddcIds = this.mapReverseRelationSubjects(
                circuitIds, DtpRwdRelationTypeEnum.WE_NETWORK);

        Map<String, DtpRwdSpaceDTO> spaces = this.listCachedRelatedObjects(
                this.collectRelationObjectIds(spaceIds), DtpRwdSpaceDTO.class,
                RunningMonitorLightingStrategy.CACHE_RELATION_SPACE_FIELDS);
        Map<String, DtpRwdFloorDTO> floors = this.listCachedRelatedObjects(
                this.collectRelationObjectIds(floorIds), DtpRwdFloorDTO.class,
                RunningMonitorLightingStrategy.CACHE_RELATION_FLOOR_FIELDS);
        Map<String, DtpRwdObjectDTO> controllers = this.listCachedRelatedObjects(
                this.collectRelationObjectIds(controllerIds), DtpRwdObjectDTO.class,
                RunningMonitorLightingStrategy.CACHE_RELATION_CONTROL_OBJECT_FIELDS);
        Map<String, DtpRwdObjectDTO> ddcs = this.listCachedRelatedObjects(
                this.collectRelationObjectIds(ddcIds), DtpRwdObjectDTO.class,
                RunningMonitorLightingStrategy.CACHE_RELATION_CONTROL_OBJECT_FIELDS);

        for (LightingCircuitDetailDTO circuit : circuits) {
            circuit.setRelSpace(this.mapRelationObjects(
                    spaceIds.get(circuit.getId()), spaces));
            circuit.setRelFloor(this.mapRelationObjects(
                    floorIds.get(circuit.getId()), floors));
            circuit.setRelController(this.mapRelationObjects(
                    controllerIds.get(circuit.getId()), controllers));
            circuit.setRelDDC(this.mapRelationObjects(
                    ddcIds.get(circuit.getId()), ddcs));
        }
    }

    /**
     * 映射回路主体到正向关系目标。
     *
     * @param circuitIds 回路主体 ID
     * @param relationType 关系类型
     * @return 回路 ID 到目标对象 ID
     * @since v7.260701.0
     */
    private @NonNull Map<String, List<String>> mapForwardRelationTargets(
            @NonNull Set<String> circuitIds,
            @NonNull DtpRwdRelationTypeEnum relationType) {
        CacheObjectRelQueryDaoParam param = this.toCacheRelationQueryParam(relationType);
        param.setObjectIds(circuitIds);
        Map<String, List<String>> targets = new LinkedHashMap<>();
        for (CacheObjectRel relation : this.cacheObjectRelRepository.listForward(param)) {
            targets.computeIfAbsent(relation.getObjectId(), key -> new ArrayList<>())
                    .add(relation.getRelObjectId());
        }
        return targets;
    }

    /**
     * 映射作为关系目标的回路到反向控制主体。
     *
     * @param circuitIds 回路目标 ID
     * @param relationType 关系类型
     * @return 回路 ID 到控制主体 ID
     * @since v7.260701.0
     */
    private @NonNull Map<String, List<String>> mapReverseRelationSubjects(
            @NonNull Set<String> circuitIds,
            @NonNull DtpRwdRelationTypeEnum relationType) {
        CacheObjectRelQueryDaoParam param = this.toCacheRelationQueryParam(relationType);
        param.setRelObjectIds(circuitIds);
        Map<String, List<String>> subjects = new LinkedHashMap<>();
        for (CacheObjectRel relation : this.cacheObjectRelRepository.listReverse(param)) {
            subjects.computeIfAbsent(relation.getRelObjectId(), key -> new ArrayList<>())
                    .add(relation.getObjectId());
        }
        return subjects;
    }

    /**
     * 构造限定图谱和关系编码的缓存关系查询参数。
     *
     * @param relationType 关系类型
     * @return 缓存关系查询参数
     * @since v7.260701.0
     */
    private @NonNull CacheObjectRelQueryDaoParam toCacheRelationQueryParam(
            @NonNull DtpRwdRelationTypeEnum relationType) {
        CacheObjectRelQueryDaoParam param = new CacheObjectRelQueryDaoParam();
        param.setGraphCode(relationType.value().getLeft());
        param.setRelCode(relationType.value().getRight());
        param.addColumns(CacheObjectRel::getObjectId, CacheObjectRel::getRelObjectId);
        return param;
    }

    /**
     * 批量读取关系对象并按实际命中结果建立 ID 映射。
     *
     * @param objectIds 关系对象 ID
     * @param targetClass 目标 DTO 类型
     * @param requestedFields 当前关系角色需要的字段
     * @param <T> DTO 类型
     * @return 关系对象 ID 到 DTO
     * @since v7.260701.0
     */
    private <T extends DtpRwdObjectDTO> @NonNull Map<String, T> listCachedRelatedObjects(
            @NonNull Set<String> objectIds,
            @NonNull Class<T> targetClass,
            @NonNull Collection<String> requestedFields) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return Collections.emptyMap();
        }
        List<T> objects = this.cacheObjectRepository.listByObjectIds(
                objectIds, targetClass, requestedFields);
        Map<String, T> result = new LinkedHashMap<>();
        for (T object : objects) {
            result.put(object.getId(), object);
        }
        return result;
    }

    /**
     * 合并关系映射中的全部对象 ID。
     *
     * @param objectIdsByCircuit 回路到关系对象 ID
     * @return 关系对象 ID 集合
     * @since v7.260701.0
     */
    private @NonNull Set<String> collectRelationObjectIds(
            @NonNull Map<String, List<String>> objectIdsByCircuit) {
        Set<String> result = new LinkedHashSet<>();
        for (List<String> objectIds : objectIdsByCircuit.values()) {
            result.addAll(objectIds);
        }
        return result;
    }

    /**
     * 按关系 ID 顺序恢复对象列表。
     *
     * @param objectIds 关系对象 ID
     * @param objectById 关系对象映射
     * @param <T> DTO 类型
     * @return 关系对象；无关系时返回 {@code null}
     * @since v7.260701.0
     */
    private <T extends DtpRwdObjectDTO> @Nullable List<T> mapRelationObjects(
            @Nullable List<String> objectIds,
            @NonNull Map<String, T> objectById) {
        if (objectIds == null) {
            return null;
        }
        return objectIds.stream()
                .map(objectById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<LightingCircuitDetailDTO> listCircuits(@NonNull @Valid RunningMonitorEquipmentQueryParam param) {
        // 普通业务查询保持原字段范围，缓存落盘所需的扩展字段只由缓存源入口请求。
        SpaceQueryParam spaceQueryParam = new SpaceQueryParam();
        spaceQueryParam.addWithColumn(
                DtpRwdObjectDTO.Fields.localName,
                DtpRwdObjectDTO.Fields.classCode,
                DtpRwdObjectDTO.Fields.relObjIds,
                DtpRwdSpaceDTO.Fields.floorId,
                DtpRwdSpaceDTO.Fields.roomFuncType,
                InfoCodeEnum.REPLACED_BY_NAT_LIGHTING.getCode());
        List<LightingCircuitDetailDTO> circuits = this.queryCircuits(
                param, spaceQueryParam, Collections.emptySet(), false);
        if (CollectionUtils.isEmpty(circuits)) {
            // 保持历史普通查询约定：调用方可继续在空结果上进行可变集合操作。
            return Lists.newArrayList();
        }
        // 业务查询需要展示知识库名称；缓存源查询只保存 RWD 原始编码。
        this.setInfoCodeName(circuits);
        return circuits;
    }

    /**
     * 查询缓存对象模型构建所需的照明回路源对象。
     *
     * <p>该入口沿用业务查询的回路条件、模板类型和可监可控状态筛选。非夜景照明继续按服务
     * 空间过滤回路；夜景照明仍查询并补充服务楼层，但楼层是否命中不参与回路过滤。该入口
     * 按各自 classCode 字段配置查询回路、空间和楼层的完整缓存快照，不影响普通业务查询负载。
     * 该入口不补充知识库展示名称、控制模块或 DDC 关系。控制模块和 DDC 关系由
     * {@link LightingCacheObjectRelationService} 在缓存关系快照准备阶段统一查询。</p>
     *
     * @param param 照明回路查询参数
     * @return 查询条件对应的完整照明回路源对象；无匹配对象时返回空集合
     * @since v7.260701.0
     */
    public @NonNull List<LightingCircuitDetailDTO> listCacheModelSourceCircuits(
            @NonNull @Valid RunningMonitorEquipmentQueryParam param) {
        Set<String> floorFields = new LinkedHashSet<>(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(
                        DtpRwdClassCodeEnum.FLOOR.value()));

        Set<String> spaceFields = new LinkedHashSet<>(
                CacheObjectExtensionFieldConfig.listRwdQueryFields(
                        DtpRwdClassCodeEnum.SPACE.value()));
        spaceFields.add(DtpRwdObjectDTO.Fields.relObjIds);
        SpaceQueryParam spaceQueryParam = new SpaceQueryParam();
        spaceQueryParam.addWithColumn(spaceFields.toArray(new String[0]));
        return this.queryCircuits(
                param, spaceQueryParam, floorFields, true);
    }

    /**
     * 按指定的回路和服务空间字段查询照明回路。
     *
     * <p>需求照明策略负责保留既有范围语义：非夜景回路缺少服务空间时被过滤，夜景回路缺少
     * 服务楼层时仍保留。该方法只转换已附着的空间和楼层，不查询展示名称或控制关系。</p>
     *
     * @param param 回路查询和过滤参数
     * @param spaceQueryParam 服务空间查询参数
     * @param floorWithColumns 缓存构建的服务楼层查询字段；普通查询不使用
     * @param cacheBuild 是否按缓存模型字段查询并保留 BIM 模型视图字段
     * @return 查询条件对应的照明回路；无匹配对象时返回空集合
     * @since v7.260701.0
     */
    private @NonNull List<LightingCircuitDetailDTO> queryCircuits(
            @NonNull RunningMonitorEquipmentQueryParam param,
            @NonNull SpaceQueryParam spaceQueryParam,
            @NonNull Set<String> floorWithColumns,
            boolean cacheBuild) {
        List<DemandTemplateTypeEnum> templateTypes =
                RunningMonitorLightingStrategy.LIGHTING_TEMPLATE_TYPE;
        List<RunningMonitorSecondTypeEnum> runningMonitorSecondTypes = param.getRunningMonitorSecondTypes();
        if (CollectionUtils.isNotEmpty(runningMonitorSecondTypes)) {
            templateTypes = runningMonitorSecondTypes.stream()
                    .map(RunningMonitorSecondTypeEnum::toDemandTemplateTypeEnum)
                    .collect(Collectors.toList());
        }
        List<DtpRwdCircuitDTO> sourceCircuits = cacheBuild
                ? this.demandLightingStrategy.listCircuitsForCacheBuild(
                        templateTypes, param, spaceQueryParam, floorWithColumns)
                : this.demandLightingStrategy.listCircuits(
                        templateTypes, param, spaceQueryParam);
        return sourceCircuits.stream()
                .map(circuit -> this.toDetailDTO(circuit, cacheBuild))
                .collect(Collectors.toList());
    }

    /**
     * 转换照明回路并按调用链决定是否保留模型视图字段。
     *
     * <p>普通业务转换器历史上明确忽略 {@code bimId}/{@code bimLocation}；缓存源查询已经
     * 按 classCode 字段配置请求这些值，因此只在缓存构建分支回填，避免扩大普通查询响应契约。</p>
     *
     * @param source RWD 照明回路
     * @param cacheBuild 是否为缓存构建；为 {@code true} 时保留 BIM 模型视图字段
     * @return 转换后的照明回路
     * @since v7.260701.0
     */
    private @NonNull LightingCircuitDetailDTO toDetailDTO(
            @NonNull DtpRwdCircuitDTO source,
            boolean cacheBuild) {
        LightingCircuitDetailDTO result = this.lightingCircuitConvert.toDetailDTO(source);
        if (cacheBuild) {
            // 回路 DTO 没有固定 BIM 字段，RWD 按 withColumns 将其放入动态属性。
            result.setBimId(source.analyzeProperties(DtpRwdEquipmentDTO.Fields.bimId));
            result.setBimLocation(source.analyzeProperties(DtpRwdEquipmentDTO.Fields.bimLocation));
        }
        return result;
    }

    public void setInfoCodeName(List<LightingCircuitDetailDTO> circuits) {
        // 查询信息定义
        Map<String, List<DtpKnowledgeInfoCodeDataSourceDTO>> infoCodeListMap = dtpKnowledgeClientService.mapInfoCodeAndDataSources(false, true, DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.getValue());
        if (MapUtil.isEmpty(infoCodeListMap)) {
            return;
        }

        Map<String, String> funcTypeMap = Optional.ofNullable(infoCodeListMap.get(InfoCodeEnum.FUNC_TYPE.value()))
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(Collectors.toMap(
                        DtpKnowledgeInfoCodeDataSourceDTO::getCode,
                        DtpKnowledgeInfoCodeDataSourceDTO::getName,
                        (o1, o2) -> o1));

        Map<String, String> lampFuncTypeMap = Optional.ofNullable(infoCodeListMap.get(InfoCodeEnum.LAMP_FUNC_TYPE.value()))
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(Collectors.toMap(
                        DtpKnowledgeInfoCodeDataSourceDTO::getCode,
                        DtpKnowledgeInfoCodeDataSourceDTO::getName,
                        (o1, o2) -> o1));

        Map<String, String> serviceAreaTypeMap = Optional.ofNullable(infoCodeListMap.get(InfoCodeEnum.SERVICE_AREA_TYPE.value()))
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(Collectors.toMap(
                        DtpKnowledgeInfoCodeDataSourceDTO::getCode,
                        DtpKnowledgeInfoCodeDataSourceDTO::getName,
                        (o1, o2) -> o1));

        circuits.forEach(circuitDTO -> {
            circuitDTO.setFuncTypeName(funcTypeMap.get(circuitDTO.getFuncType()));
            circuitDTO.setLampFuncTypeName(lampFuncTypeMap.get(circuitDTO.getLampFuncType()));
            circuitDTO.setServiceAreaTypeName(serviceAreaTypeMap.get(circuitDTO.getServiceAreaType()));
        });
    }

    public List<DtpRwdObjectDTO> queryDDC(Collection<String> circuitIds, Collection<String> withColumns) {
        if (CollectionUtils.isEmpty(circuitIds)) {
            return Lists.newArrayList();
        }
        if (CollectionUtils.isEmpty(withColumns)) {
            withColumns = Arrays.asList(DtpRwdObjectDTO.listSimpleWithColumns());
        }
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setWithColumns(withColumns);
        queryParam.setClassCodeEnum(DtpRwdClassCodeEnum.WEFCDD);
        queryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.WE_NETWORK).setObjTo(circuitIds));
        return dtpRwdClientService.listObject(queryParam);
    }

    public void setRelDDCode(List<LightingCircuitDetailDTO> circuits) {
        Map<String, LightingCircuitDetailDTO> circuitMap = circuits.stream().collect(Collectors.toMap(
                LightingCircuitDetailDTO::getId,
                Function.identity(),
                (o1, o2) -> o1));

        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = this.queryDDC(circuitMap.keySet(), Sets.newHashSet(
                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode,
                DtpRwdObjectDTO.Fields.localName, DtpRwdSpaceDTO.Fields.floorId, DtpRwdObjectDTO.Fields.relObjIds));
        dtpRwdObjectDTOS.forEach(item -> {
            Set<String> relObjIds = item.getRelObjIds();
            item.setRelObjIds(null);
            if (relObjIds != null) {
                relObjIds.forEach(id -> {
                    LightingCircuitDetailDTO circuit = circuitMap.get(id);
                    if (circuit != null) {
                        List<DtpRwdObjectDTO> rel = circuit.getRelDDC();
                        if (rel == null) {
                            rel = new ArrayList<>();
                        }
                        rel.add(item);
                        circuit.setRelDDC(rel);
                    }
                });
            }
        });

    }

    public List<LightingCircuitDetailDTO> filterByRelController(List<LightingCircuitDetailDTO> circuits) {
        Map<String, LightingCircuitDetailDTO> circuitMap = circuits.stream().collect(Collectors.toMap(
                LightingCircuitDetailDTO::getId,
                Function.identity(),
                (o1, o2) -> o1));

        List<DtpRwdObjectDTO> controllers = dtpRwdClientService.listCircuitControllerByCircuitIds(
                false, false, ContextUtil.getProjectId(), circuitMap.keySet(),
                new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.classCode,
                        DtpRwdObjectDTO.Fields.localName, DtpRwdSpaceDTO.Fields.floorId, DtpRwdObjectDTO.Fields.relObjIds));

        /*
        if (CollectionUtil.isEmpty(controllers)) {
            return new ArrayList<>();
        }

         */

        // Map<String,LightingCircuitDetailDTO> fileterCircuits = new HashMap<>();
        controllers.forEach(item -> {
            Set<String> relObjIds = item.getRelObjIds();
            item.setRelObjIds(null);
            if (relObjIds != null) {
                relObjIds.forEach(id -> {
                    LightingCircuitDetailDTO circuit = circuitMap.get(id);
                    if (circuit != null) {
                        List<DtpRwdObjectDTO> rel = circuit.getRelController();
                        if (rel == null) {
                            rel = new ArrayList<>();
                        }
                        rel.add(item);
                        circuit.setRelController(rel);

                        // fileterCircuits.put(id, circuit);
                    }
                });
            }
        });

        return new ArrayList<>(circuitMap.values());
        // return new ArrayList<>(fileterCircuits.values());
    }


    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "运行检测-照明策略";
    }
}
