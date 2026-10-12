package com.persagy.apccontrol.basic.model.context.security;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.SecurityDtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.AccessDemandStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.DeploymentStatusEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigResultBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.apccontrol.basic.model.param.security.SecurityDashboardQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigResultVO;
import com.persagy.apccontrol.basic.model.vo.security.SecurityDashboardBuildingVO;
import com.persagy.apccontrol.basic.model.vo.security.SecurityDashboardDeviatedSpaceVO;
import com.persagy.apccontrol.basic.model.vo.security.SecurityDashboardFloorVO;
import com.persagy.apccontrol.basic.model.vo.security.SecurityDashboardVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizComputeService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorSecurityStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.security.DemandConfigSecurityArmingValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.security.DemandConfigSecurityPassingAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 安防-Dashboard查询上下文
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Data
@SuppressWarnings("unchecked")
public class SecurityDashboardQueryContext {

    /**
     * 计算精度
     */
    private final int scale = 10;

    // ------------------------- 入参 -------------------------

    /**
     * 查询参数
     */
    @NonNull
    private SecurityDashboardQueryParam param;

    /**
     * 安防策略
     */
    private final RunningMonitorSecurityStrategy runningMonitorSecurityStrategy;

    /**
     * 物理世界服务
     */
    private final DtpRwdClientService dtpRwdClientService;

    /**
     * IOT服务
     */
    private final AbstractIotDataCacheServer<?> abstractIotDataCacheServer;

    /**
     * 需求配置网关
     */
    private final DemandConfigGateway<BaseDemandConfig> demandConfigGateway;

    /**
     * 实例服务
     */
    private final InstanceService instanceService;

    /**
     * 计算服务
     */
    private final BizComputeService bizComputeService;

    /**
     * 创建单次安防 Dashboard 查询上下文。
     *
     * @param param 查询参数
     * @param runningMonitorSecurityStrategy 安防运行策略
     * @param dtpRwdClientService 物理世界服务
     * @param abstractIotDataCacheServer IoT 服务
     * @param demandConfigGateway 需求配置网关
     * @param instanceService 实例服务
     * @param bizComputeService 计算服务
     * @since v7.260802.0
     */
    public SecurityDashboardQueryContext(
            @NonNull SecurityDashboardQueryParam param,
            @NonNull RunningMonitorSecurityStrategy runningMonitorSecurityStrategy,
            @NonNull DtpRwdClientService dtpRwdClientService,
            @NonNull AbstractIotDataCacheServer<?> abstractIotDataCacheServer,
            @NonNull DemandConfigGateway<BaseDemandConfig> demandConfigGateway,
            @NonNull InstanceService instanceService,
            @NonNull BizComputeService bizComputeService) {
        this.param = param;
        this.runningMonitorSecurityStrategy = runningMonitorSecurityStrategy;
        this.dtpRwdClientService = dtpRwdClientService;
        this.abstractIotDataCacheServer = abstractIotDataCacheServer;
        this.demandConfigGateway = demandConfigGateway;
        this.instanceService = instanceService;
        this.bizComputeService = bizComputeService;
    }

    // ------------------------- 过程 -------------------------

    /**
     * 空间
     */
    private List<DtpRwdSpaceDTO> spaces = Collections.emptyList();

    /**
     * 建筑对象
     */
    private List<DtpRwdBuildingDTO> buildings = Collections.emptyList();

    /**
     * 建筑ID -> 建筑下楼层
     */
    private Map<String, List<DtpRwdFloorDTO>> buildingIdAndFloors = Collections.emptyMap();

    /**
     * 存在需求空间的配置
     */
    private List<DemandConfigAggregate<BaseDemandConfig>> existsDemandSpaceConfigs = Collections.emptyList();

    /**
     * 需求空间ID -> 上级ID
     */
    private Map<String, List<String>> spaceIdAndAllParentIds = Collections.emptyMap();

    /**
     * 需求配置中的实例 -> 下级实例ID, 用于计算需求一致性, 存在顺序
     */
    private Map<DemandInstanceValueObject, List<String>> instanceAndChildrenIdsInDemand = new LinkedHashMap<>();

    // ------------------------- 结果 -------------------------

    /**
     * 是否失败
     */
    private boolean failed = false;

    /**
     * 楼层下空间设备ID映射, 楼层ID-空间ID -> [设备类编码 -> 设备ID]
     */
    private Map<String, Map<String, Map<DtpRwdClassCodeEnum, Set<String>>>> floorIdSpaceIdAndEquipmentIds = new HashMap<>();

    /**
     * 空间实际数量统计, 对象ID -> [状态枚举 -> 对象ID]
     *
     * @see DeploymentStatusEnum
     * @see AccessDemandStatusEnum
     */
    private Map<String, Map<BaseEnum<?>, Set<String>>> objectIdAndRealTypeIds = new HashMap<>();

    /**
     * 空间需求数量统计, 对象ID -> [状态枚举 -> 对象ID]
     *
     * @see DeploymentStatusEnum
     * @see AccessDemandStatusEnum
     */
    private Map<String, Map<BaseEnum<?>, Set<String>>> objectIdAndDemandTypeIds = new HashMap<>();

    /**
     * 对象ID和需求一致性占比, 对象ID -> [状态枚举 -> 需求一致性]
     *
     * @see DemandTemplateTypeEnum#ARMING
     * @see DemandTemplateTypeEnum#PASSING
     */
    private Map<String, Map<BaseEnum<?>, BigDecimal>> objectIdAndTypeConsistency = new HashMap<>();

    /**
     * 转换结果
     *
     * @since 5.241201.0
     */
    public SecurityDashboardVO toDashboardVo() {
        String projectId = ContextUtil.getProjectId();
        DemandTemplateTypeEnum templateType = param.getRunningMonitorSecondType().toDemandTemplateTypeEnum();
        Map<String, List<DtpRwdSpaceDTO>> floorIdAndSpaces = spaces.stream().collect(
                Collectors.groupingBy(DtpRwdSpaceDTO::getFloorId));

        List<SecurityDashboardBuildingVO> buildingVos = new ArrayList<>();
        for (DtpRwdBuildingDTO building : buildings) {
            String buildingId = building.getId();

            List<DtpRwdFloorDTO> floors = buildingIdAndFloors.get(buildingId);
            if (CollectionUtils.isEmpty(floors)) {
                continue;
            }
            List<SecurityDashboardFloorVO> floorVos = new ArrayList<>();
            for (DtpRwdFloorDTO floor : floors) {
                String floorId = floor.getId();
                String floorLocalName = floor.getLocalName();

                List<SecurityDashboardDeviatedSpaceVO> demandDeviatedSpaces = new ArrayList<>();
                List<DtpRwdSpaceDTO> spaces = floorIdAndSpaces.get(floorId);
                if (CollectionUtils.isNotEmpty(spaces)) {
                    for (DtpRwdSpaceDTO space : spaces) {
                        String spaceId = space.getId();
                        String spaceLocalName = space.getLocalName();

                        Map<? extends BaseEnum<?>, Set<String>> demandArmingStatus = objectIdAndDemandTypeIds
                                .getOrDefault(spaceId, Collections.emptyMap());
                        Map<? extends BaseEnum<?>, Set<String>> realArmingStatus = objectIdAndRealTypeIds
                                .getOrDefault(spaceId, Collections.emptyMap());
                        DeploymentStatusEnum spaceDemandArmingStatus = MapUtils.isNotEmpty(demandArmingStatus)
                                ? (DeploymentStatusEnum) IterableUtils.first(demandArmingStatus.entrySet()).getKey()
                                : null;
                        DeploymentStatusEnum spaceRealArmingStatus = MapUtils.isNotEmpty(realArmingStatus)
                                ? (DeploymentStatusEnum) IterableUtils.first(realArmingStatus.entrySet()).getKey()
                                : DeploymentStatusEnum.UNKNOWN;
                        // 当空间无需求时或需求和实际一致时跳过
                        if (Objects.isNull(spaceDemandArmingStatus) || spaceDemandArmingStatus == spaceRealArmingStatus) {
                            continue;
                        }
                        SecurityDashboardDeviatedSpaceVO spaceVo = new SecurityDashboardDeviatedSpaceVO();
                        spaceVo.setId(space.getId());
                        spaceVo.setLocalName(spaceLocalName);
                        spaceVo.setDemandArmingStatus(spaceDemandArmingStatus);
                        spaceVo.setRealArmingStatus(spaceRealArmingStatus);
                        demandDeviatedSpaces.add(spaceVo);
                    }
                }

                SecurityDashboardFloorVO floorVo = new SecurityDashboardFloorVO();
                floorVo.setId(floorId);
                floorVo.setLocalName(floorLocalName);
                floorVo.setDemandConsistency(MapExecutor.dynamicGet(objectIdAndTypeConsistency, floorId, templateType));
                floorVo.setTotalDemandSpaceCount(toStatisticsResultMap(objectIdAndDemandTypeIds.get(floorId)));
                floorVo.setTotalRealSpaceCount(toStatisticsResultMap(objectIdAndRealTypeIds.get(floorId)));
                floorVo.setClassCodeAndObjectIds(analyzeClassCodeAndObjectIdsByFloor(floorId));
                floorVo.setDemandDeviatedSpaces(demandDeviatedSpaces);
                floorVos.add(floorVo);
            }
            SecurityDashboardBuildingVO buildingVo = new SecurityDashboardBuildingVO();
            buildingVo.setId(buildingId);
            buildingVo.setLocalName(building.getLocalName());
            buildingVo.setFloors(floorVos);
            buildingVos.add(buildingVo);
        }
        SecurityDashboardVO result = new SecurityDashboardVO();
        result.setDemandConsistency(MapExecutor.dynamicGet(objectIdAndTypeConsistency, projectId, templateType));
        result.setTotalDemandSpaceCount(toStatisticsResultMap(objectIdAndDemandTypeIds.get(projectId)));
        result.setBuildings(buildingVos);
        return result;
    }

    // ------------------------- 行为 -------------------------

    /**
     * 填充实例
     *
     * @since 5.241201.0
     */
    public void fillInstance() {
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        DemandTemplateTypeEnum templateType = runningMonitorSecondType.toDemandTemplateTypeEnum();
        DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();
        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(templateType);
        queryParam.queryOnlyControlled();
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName,
                DtpRwdSpaceDTO.Fields.floorId, DtpRwdSpaceDTO.Fields.buildingId);
        List<DtpRwdSpaceDTO> demandSpaces = demandStrategy.listDemandSpace(queryParam);
        if (CollectionUtils.isEmpty(demandSpaces)) {
            return;
        }
        Map<String, DtpRwdSpaceDTO> spaceIdAndData = demandSpaces.stream().collect(Collectors.toMap(
                DtpRwdObjectDTO::getId, Function.identity(), (o1, o2) -> o1));
        Set<String> spaceIds = spaceIdAndData.keySet();
        Map<String, Map<DtpRwdClassCodeEnum, Set<String>>> spaceIdAndClassCodeEquipmentIds = new HashMap<>();
        if (templateType == DemandTemplateTypeEnum.ARMING) {
            // 查询入侵防区报警控制器
            dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setClassCodeEnum(DtpRwdClassCodeEnum.SPIACU)
                            .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                                    DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjTo(spaceIds))
                            .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds)).stream()
                    .filter(item -> CollectionUtils.isNotEmpty(item.getRelObjIds())).forEach(item -> {
                        item.getRelObjIds().forEach(relSpaceId -> MapExecutor.dynamicPut(spaceIdAndClassCodeEquipmentIds,
                                relSpaceId, DtpRwdClassCodeEnum.SPIACU, item.getId(), HashMap::new, LinkedHashSet::new));
                    });
            // 查询具有撤布防能力的摄像头
            dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setClassCodeEnum(DtpRwdClassCodeEnum.SPVSCM)
                            .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                                    DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjTo(spaceIds))
                            .addInfoCode(InfoCodeEnum.HAS_ARMING_FUNC.value(), "1")
                            .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds)).stream()
                    .filter(item -> CollectionUtils.isNotEmpty(item.getRelObjIds())).forEach(item -> {
                        item.getRelObjIds().forEach(relSpaceId -> MapExecutor.dynamicPut(spaceIdAndClassCodeEquipmentIds,
                                relSpaceId, DtpRwdClassCodeEnum.SPVSCM, item.getId(), HashMap::new, LinkedHashSet::new));
                    });
        }
        List<DtpRwdSpaceDTO> filteredSpaces = demandSpaces.stream()
                .filter(item -> spaceIdAndClassCodeEquipmentIds.containsKey(item.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        this.fillStaticInstances(
                filteredSpaces, spaceIdAndClassCodeEquipmentIds, null, null);
    }

    /**
     * 使用 SECURITY READY 静态快照填充 Dashboard 实例。
     *
     * <p>缓存空集合是权威结果。本方法只组装静态对象和关系，不执行 RWD 或 IoT 查询。</p>
     *
     * @param cachedSpaces 项目空间快照
     * @param cachedEquipments 安防设备快照
     * @param serviceSpaceIdsByEquipment 设备服务空间关系
     * @param inSpaceIdsByEquipment 设备所在空间关系
     * @param excludedSpaceIds 屏蔽或黑名单空间
     * @param cachedBuildings 项目建筑快照
     * @param cachedFloors 项目楼层快照
     * @since v7.260802.0
     */
    public void fillInstanceFromCache(
            @NonNull List<DtpRwdSpaceDTO> cachedSpaces,
            @NonNull List<SecurityDtpRwdEquipmentDTO> cachedEquipments,
            @NonNull Map<String, Set<String>> serviceSpaceIdsByEquipment,
            @NonNull Map<String, Set<String>> inSpaceIdsByEquipment,
            @NonNull Set<String> excludedSpaceIds,
            @NonNull List<DtpRwdBuildingDTO> cachedBuildings,
            @NonNull List<DtpRwdFloorDTO> cachedFloors) {
        DemandTemplateTypeEnum templateType = param.getRunningMonitorSecondType()
                .toDemandTemplateTypeEnum();
        Map<String, Map<DtpRwdClassCodeEnum, Set<String>>>
                spaceIdAndClassCodeEquipmentIds = new LinkedHashMap<>();
        if (templateType == DemandTemplateTypeEnum.ARMING) {
            for (SecurityDtpRwdEquipmentDTO equipment : cachedEquipments) {
                Map<String, Set<String>> relations;
                DtpRwdClassCodeEnum classCode;
                if (DtpRwdClassCodeEnum.SPIACU.value().equals(equipment.getClassCode())) {
                    classCode = DtpRwdClassCodeEnum.SPIACU;
                    relations = serviceSpaceIdsByEquipment;
                } else if (DtpRwdClassCodeEnum.SPVSCM.value().equals(equipment.getClassCode())
                        && "1".equals(equipment.getHasArmingFunc())) {
                    classCode = DtpRwdClassCodeEnum.SPVSCM;
                    relations = inSpaceIdsByEquipment;
                } else {
                    continue;
                }
                for (String spaceId : relations.getOrDefault(
                        equipment.getId(), Collections.emptySet())) {
                    MapExecutor.dynamicPut(
                            spaceIdAndClassCodeEquipmentIds,
                            spaceId,
                            classCode,
                            equipment.getId(),
                            HashMap::new,
                            LinkedHashSet::new);
                }
            }
        }
        List<DtpRwdSpaceDTO> controlledSpaces = cachedSpaces.stream()
                .filter(space -> !excludedSpaceIds.contains(space.getId()))
                .filter(space -> spaceIdAndClassCodeEquipmentIds.containsKey(space.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        this.fillStaticInstances(
                controlledSpaces,
                spaceIdAndClassCodeEquipmentIds,
                cachedBuildings,
                cachedFloors);
    }

    /**
     * 统一填充空间、设备映射及建筑楼层树。
     *
     * @param filteredSpaces 已按需求范围和设备关系过滤的空间
     * @param spaceIdAndClassCodeEquipmentIds 空间下设备映射
     * @param cachedBuildings READY 建筑；为空时查询原 RWD 路径
     * @param cachedFloors READY 楼层；为空时查询原 RWD 路径
     * @since v7.260802.0
     */
    private void fillStaticInstances(
            @NonNull List<DtpRwdSpaceDTO> filteredSpaces,
            @NonNull Map<String, Map<DtpRwdClassCodeEnum, Set<String>>>
                    spaceIdAndClassCodeEquipmentIds,
            @Nullable List<DtpRwdBuildingDTO> cachedBuildings,
            @Nullable List<DtpRwdFloorDTO> cachedFloors) {
        this.spaces = filteredSpaces;
        if (CollectionUtils.isEmpty(this.spaces)) {
            return;
        }
        Map<String, DtpRwdSpaceDTO> spaceIdAndData = this.spaces.stream()
                .collect(Collectors.toMap(
                        DtpRwdObjectDTO::getId, Function.identity(), (o1, o2) -> o1));
        spaceIdAndClassCodeEquipmentIds.forEach((spaceId, equipmentIds) -> {
            DtpRwdSpaceDTO space = spaceIdAndData.get(spaceId);
            if (space != null) {
                MapExecutor.dynamicPut(floorIdSpaceIdAndEquipmentIds, space.getFloorId(),
                        space.getId(), equipmentIds, HashMap::new);
            }
        });
        Set<String> floorIds = new HashSet<>();
        Set<String> buildingIds = new HashSet<>();
        for (DtpRwdSpaceDTO space : this.spaces) {
            floorIds.add(space.getFloorId());
            buildingIds.add(space.getBuildingId());
        }
        if (CollectionUtils.isEmpty(floorIds) || CollectionUtils.isEmpty(buildingIds)) {
            return;
        }
        if (cachedBuildings != null && cachedFloors != null) {
            this.buildings = cachedBuildings.stream()
                    .filter(building -> buildingIds.contains(building.getId()))
                    // 与 RWD 路径 listBuilding 一致：建筑按 buildingSequenceId 升序（ObjectUtils.compare 语义，null 排最前）
                    .sorted(Comparator.comparing(DtpRwdBuildingDTO::getBuildingSequenceId,
                            Comparator.nullsFirst(Comparator.naturalOrder())))
                    .collect(Collectors.toCollection(ArrayList::new));
            this.buildingIdAndFloors = cachedFloors.stream()
                    .filter(floor -> floorIds.contains(floor.getId()))
                    // 与 RWD 路径 listFloor 一致：楼层按 floorSequenceId 从高到低
                    .sorted(Comparator.comparing(DtpRwdFloorDTO::getFloorSequenceId,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.groupingBy(DtpRwdFloorDTO::getBuildingId));
            return;
        }
        this.buildings = dtpRwdClientService.listBuilding(
                new DtpRwdObjectQueryParam().setId(buildingIds)
                        .addWithColumn(
                                DtpRwdObjectDTO.Fields.id,
                                DtpRwdObjectDTO.Fields.localName,
                                DtpRwdBuildingDTO.Fields.buildingSequenceId));
        this.buildingIdAndFloors = dtpRwdClientService.listFloor(
                new DtpRwdObjectQueryParam().setId(floorIds)
                        .addWithColumn(
                                DtpRwdObjectDTO.Fields.id,
                                DtpRwdObjectDTO.Fields.localName,
                                DtpRwdFloorDTO.Fields.buildingId,
                                DtpRwdFloorDTO.Fields.floorSequenceId)).stream()
                .collect(Collectors.groupingBy(DtpRwdFloorDTO::getBuildingId));
    }

    /**
     * 读取当前模板对应的预计算需求配置结果，并过滤为当前监测空间内存在有效最终配置的结果。
     *
     * @since 5.241201.0
     */
    public void fillDemandConfig() {
        DemandTemplateTypeEnum templateType = param.getRunningMonitorSecondType().toDemandTemplateTypeEnum();
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateType(templateType);
        templateParam.setDate(DateUtils.now().toLocalDate());
        DemandTemplate template = templateParam.queryAndGetTemplate();
        if (Objects.isNull(template)) {
            return;
        }
        DemandConfigResultBatchQueryParam resultQueryParam = new DemandConfigResultBatchQueryParam();
        resultQueryParam.setTemplateId(template.getId());
        resultQueryParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
        resultQueryParam.setInstanceIds(null);
        resultQueryParam.setFilterShieldedSpaces(true);
        resultQueryParam.setAutoFillEmptyInstanceIds(false);
        List<DemandConfigResultVO<BaseDemandConfig>> results =
                demandConfigGateway.listConfigResultsByInstances(resultQueryParam);
        this.existsDemandSpaceConfigs = results.stream()
                .filter(result -> Objects.nonNull(result.getFinalConfig()))
                .map(result -> this.toDemandConfigAggregate(template, result))
                .collect(Collectors.toCollection(ArrayList::new));
        // 移除未配置的空间或不在监测范围中的空间
        Set<String> spaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        this.existsDemandSpaceConfigs.removeIf(item -> Objects.isNull(item.getFinalConfig())
                || !spaceIds.contains(item.getIdentifier().getInstance().getInstanceId()));
    }

    /**
     * 将轻量需求结果 VO 适配为 Dashboard 统计所需的聚合结构，保持结果空间标识和最终配置不变，
     * 并以当前模板信息构造聚合标识。
     *
     * @param template 当前生效的需求模板，不得为 null
     * @param result 预计算需求结果，不得为 null
     * @return 包含空间实例标识和最终配置的需求配置聚合对象，不返回 null
     * @since v7.260802.0
     */
    private @NonNull DemandConfigAggregate<BaseDemandConfig> toDemandConfigAggregate(
            @NonNull DemandTemplate template,
            @NonNull DemandConfigResultVO<BaseDemandConfig> result) {
        DemandInstanceValueObject instance = new DemandInstanceValueObject(
                DemandInstanceTypeEnum.SPACE, result.getInstanceId());
        DemandConfigAggregate<BaseDemandConfig> aggregate = new DemandConfigAggregate<>();
        DemandConfigIdentifier identifier = DemandConfigIdentifier.of(
                template.getDemandTemplateType(), template.getDemandTemplateTag(), template.getId(),
                DateUtils.now().toLocalDate(), null, null, null, instance);
        aggregate.setIdentifier(identifier);
        aggregate.setFinalConfig(result.getFinalConfig());
        return aggregate;
    }

    /**
     * 填充空间ID和上级关系
     *
     * @since 5.241201.0
     */
    public void fillSpaceIdAndParents() {
        if (CollectionUtils.isEmpty(spaces)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        Map<String, List<String>> spaceIdAndAllParentIds = new HashMap<>();
        Map<DemandInstanceValueObject, List<String>> instanceAndChildrenIdsInDemand = new HashMap<>();
        Map<String, DtpRwdSpaceDTO> spaceIdAndData = new HashMap<>();
        for (DtpRwdSpaceDTO space : spaces) {
            MapExecutor.dynamicPut(spaceIdAndAllParentIds, space.getId(), space.getFloorId(), ArrayList::new);
            MapExecutor.dynamicPut(spaceIdAndAllParentIds, space.getId(), space.getBuildingId(), ArrayList::new);
            MapExecutor.dynamicPut(spaceIdAndAllParentIds, space.getId(), projectId, ArrayList::new);
            spaceIdAndData.put(space.getId(), space);
        }
        Set<String> demandSpaceIds = existsDemandSpaceConfigs.stream().map(item -> item.getIdentifier()
                .getInstance().getInstanceId()).collect(Collectors.toSet());
        for (String demandSpaceId : demandSpaceIds) {
            DtpRwdSpaceDTO space = spaceIdAndData.get(demandSpaceId);
            if (Objects.isNull(space)) {
                continue;
            }
            MapExecutor.dynamicPut(instanceAndChildrenIdsInDemand,
                    new DemandInstanceValueObject(DemandInstanceTypeEnum.PROJECT, projectId),
                    space.getId(), ArrayList::new);
            MapExecutor.dynamicPut(instanceAndChildrenIdsInDemand,
                    new DemandInstanceValueObject(DemandInstanceTypeEnum.PROJECT, projectId),
                    space.getFloorId(), ArrayList::new);
            MapExecutor.dynamicPut(instanceAndChildrenIdsInDemand,
                    new DemandInstanceValueObject(DemandInstanceTypeEnum.PROJECT, projectId),
                    space.getBuildingId(), ArrayList::new);
            MapExecutor.dynamicPut(instanceAndChildrenIdsInDemand,
                    new DemandInstanceValueObject(DemandInstanceTypeEnum.BUILDING, space.getBuildingId()),
                    space.getId(), ArrayList::new);
            MapExecutor.dynamicPut(instanceAndChildrenIdsInDemand,
                    new DemandInstanceValueObject(DemandInstanceTypeEnum.BUILDING, space.getBuildingId()),
                    space.getFloorId(), ArrayList::new);
            MapExecutor.dynamicPut(instanceAndChildrenIdsInDemand,
                    new DemandInstanceValueObject(DemandInstanceTypeEnum.FLOOR, space.getFloorId()),
                    space.getId(), ArrayList::new);
        }
        this.spaceIdAndAllParentIds = spaceIdAndAllParentIds;
        this.instanceAndChildrenIdsInDemand = instanceAndChildrenIdsInDemand;
    }

    /**
     * 填充IOT数据
     *
     * @since 5.241102.0
     */
    public void fillIotData() {
        if (CollectionUtils.isEmpty(spaces)) {
            return;
        }
        DemandTemplateTypeEnum templateType = param.getRunningMonitorSecondType().toDemandTemplateTypeEnum();
        Set<String> spaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        if (templateType == DemandTemplateTypeEnum.ARMING) {
            List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                    spaceIds, Lists.newArrayList(InfoCodeEnum.DEPLOYMENT_STATUS.value()));
            Map<String, Object> objectIdAndDataValue = infoCodeValues.stream().collect(Collectors.toMap(
                    InfoCodeValueVO::getObjectId, InfoCodeValueVO::getDataValue, (o1, o2) -> o1));
            for (String spaceId : spaceIds) {
                Set<String> toBeStatisticsIds = parseToBeStatisticsIds(spaceId);

                Object dataValue = objectIdAndDataValue.get(spaceId);
                DeploymentStatusEnum deploymentStatus = DeploymentStatusEnum.objectToEnum(dataValue);
                toBeStatisticsIds.forEach(item -> MapExecutor.dynamicPut(objectIdAndRealTypeIds, item,
                        Objects.nonNull(deploymentStatus) ? deploymentStatus : DeploymentStatusEnum.UNKNOWN,
                        spaceId, HashMap::new, HashSet::new));
            }
        }
    }

    /**
     * 计算统计数据
     *
     * @since 5.241201.0
     */
    public void computeStatisticsData() {
        if (CollectionUtils.isEmpty(existsDemandSpaceConfigs)) {
            return;
        }
        final LocalTime nowTime = LocalTime.now();
        for (DemandConfigAggregate<? extends BaseDemandConfig> demandSpaceConfig : existsDemandSpaceConfigs) {
            DemandConfigIdentifier identifier = demandSpaceConfig.getIdentifier();
            DemandTemplateTypeEnum templateType = identifier.getTemplateType();

            if (templateType == DemandTemplateTypeEnum.ARMING) {
                computeStatisticsDataByArming(nowTime, demandSpaceConfig);
            }
        }
        summaryConsistency();
    }

    /**
     * 汇总需求一致性
     *
     * @since 5.241102.0
     */
    private void summaryConsistency() {
        if (CollectionUtils.isEmpty(existsDemandSpaceConfigs)) {
            return;
        }
        // 先统计楼层的需求满足度
        instanceAndChildrenIdsInDemand.entrySet().stream()
                .filter(item -> item.getKey().getInstanceType() == DemandInstanceTypeEnum.FLOOR)
                .forEach(item -> {
                    String floorId = item.getKey().getInstanceId();
                    List<String> spaceIds = item.getValue();
                    // 撤布防满足数量
                    BigDecimal totalArmingConsistingCount = BigDecimal.ZERO;
                    // 下级对象总数
                    BigDecimal totalHasDemandSpaceCount = BigDecimal.valueOf(spaceIds.size());
                    for (String spaceId : spaceIds) {
                        BigDecimal deployStatus = MapExecutor.dynamicGet(objectIdAndTypeConsistency,
                                spaceId, DeploymentStatusEnum.DEPLOY_A_DEFENCE);
                        BigDecimal notDeployStatus = MapExecutor.dynamicGet(objectIdAndTypeConsistency,
                                spaceId, DeploymentStatusEnum.WITHDRAW_A_GARRISON);
                        // 当该空间无撤布防时, 分母需要减1
                        if (ObjectUtils.allNull(deployStatus, notDeployStatus)) {
                            totalHasDemandSpaceCount = totalHasDemandSpaceCount.subtract(BigDecimal.ONE);
                        }
                        // 每个空间下单独判断是否布放或是否撤防
                        if (BigDecimal.ONE.equals(deployStatus) || BigDecimal.ONE.equals(notDeployStatus)) {
                            totalArmingConsistingCount = totalArmingConsistingCount.add(BigDecimal.ONE);
                        }
                    }
                    // 满足度 = 满足数量 / 有需求空间的总数, 需求空间总数为0时满足度也为0
                    BigDecimal consistency = totalHasDemandSpaceCount.compareTo(BigDecimal.ZERO) != 0
                            ? totalArmingConsistingCount.divide(totalHasDemandSpaceCount, scale, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    MapExecutor.dynamicPut(objectIdAndTypeConsistency, floorId,
                            DemandTemplateTypeEnum.ARMING, consistency, HashMap::new);
                });

        for (Map.Entry<DemandInstanceValueObject, List<String>> entry : instanceAndChildrenIdsInDemand.entrySet()) {
            DemandInstanceValueObject instance = entry.getKey();
            DemandInstanceTypeEnum instanceType = instance.getInstanceType();
            String instanceId = instance.getInstanceId();
            List<String> childrenIds = entry.getValue();

            if (instanceType == DemandInstanceTypeEnum.FLOOR) {
                continue;
            }
            List<BigDecimal> allArmingConsistency = new ArrayList<>();
            for (String childrenId : childrenIds) {
                BigDecimal consistency = MapExecutor.dynamicGet(objectIdAndTypeConsistency,
                        childrenId, DemandTemplateTypeEnum.ARMING);
                if (Objects.nonNull(consistency)) {
                    allArmingConsistency.add(consistency);
                }
            }
            // 撤布防满足度
            BigDecimal armingConsistency = bizComputeService.calculateAverage(allArmingConsistency, scale);
            MapExecutor.dynamicPut(objectIdAndTypeConsistency, instanceId,
                    DemandTemplateTypeEnum.ARMING, armingConsistency, HashMap::new);
        }
    }

    /**
     * 计算撤布防统计值
     *
     * @param nowTime           当前时间
     * @param demandSpaceConfig 需求配置
     * @since 5.241201.0
     */
    @SuppressWarnings("unchecked")
    private void computeStatisticsDataByArming(
            @NonNull final LocalTime nowTime,
            @NonNull DemandConfigAggregate<? extends BaseDemandConfig> demandSpaceConfig) {
        if (Objects.isNull(demandSpaceConfig.getFinalConfig())) {
            return;
        }
        String spaceId = demandSpaceConfig.getIdentifier().getInstance().getInstanceId();
        DemandConfigValueObject<DemandConfigSecurityArmingValueObject> finalConfig =
                (DemandConfigValueObject<DemandConfigSecurityArmingValueObject>) demandSpaceConfig.getFinalConfig();
        DemandConfigSecurityArmingValueObject globalConfig = finalConfig.getGlobalConfig();
        if (Objects.isNull(globalConfig)) {
            return;
        }
        // 待统计的所有层级ID
        Set<String> toBeStatisticsIds = parseToBeStatisticsIds(spaceId);
        // 是否存在布放需求
        boolean matchArming = anyMatchTime(nowTime, globalConfig.getArmingTimeIntervals());
        // 是否存在撤防需求
        boolean matchDisarming = anyMatchTime(nowTime, globalConfig.getDisarmingTimeIntervals());
        DeploymentStatusEnum deploymentStatus = matchArming ? DeploymentStatusEnum.DEPLOY_A_DEFENCE
                : matchDisarming ? DeploymentStatusEnum.WITHDRAW_A_GARRISON : null;
        if (Objects.nonNull(deploymentStatus)) {
            toBeStatisticsIds.forEach(item -> MapExecutor.dynamicPut(objectIdAndDemandTypeIds,
                    item, deploymentStatus, spaceId, HashMap::new, HashSet::new));
            Map<BaseEnum<?>, Set<String>> spaceRealTypeIds = objectIdAndRealTypeIds.get(spaceId);
            // 是否满足需求一致性
            boolean consistency = MapUtils.isNotEmpty(spaceRealTypeIds)
                    && spaceRealTypeIds.containsKey(deploymentStatus);
            MapExecutor.dynamicPut(objectIdAndTypeConsistency, spaceId, deploymentStatus,
                    consistency ? BigDecimal.ONE : BigDecimal.ZERO, HashMap::new);
        }
    }

    /**
     * 根据楼层ID解析楼层下设备类编码和对应ID
     *
     * @param floorId 楼层ID
     * @return 设备类编码 -> 对象ID
     * @since 5.241201.0
     */
    private Map<String, Set<String>> analyzeClassCodeAndObjectIdsByFloor(@NonNull String floorId) {
        Map<String, Map<DtpRwdClassCodeEnum, Set<String>>> spaceIdAndClassCodeEquipmentIds =
                floorIdSpaceIdAndEquipmentIds.get(floorId);
        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
        if (MapUtils.isNotEmpty(spaceIdAndClassCodeEquipmentIds)) {
            spaceIdAndClassCodeEquipmentIds.forEach((spaceId, classCodeAndEquipmentIds) -> {
                MapExecutor.dynamicPut(classCodeAndObjectIds, DtpRwdClassCodeEnum.SPACE.value(), spaceId, LinkedHashSet::new);
                classCodeAndEquipmentIds.forEach((classCode, equipmentIds) -> {
                    MapExecutor.dynamicPutAll(classCodeAndObjectIds, classCode.value(), equipmentIds, LinkedHashSet::new);
                });
            });
        }
        return classCodeAndObjectIds;
    }

    /**
     * 转换统计结果Map
     *
     * @param objectIdAndTypeIds 对象ID -> 类型ID
     * @param <K>                键类型
     * @return 统计结果Map
     * @since 5.241201.0
     */
    private <K> Map<K, Integer> toStatisticsResultMap(@Nullable Map<K, Set<String>> objectIdAndTypeIds) {
        if (MapUtils.isEmpty(objectIdAndTypeIds)) {
            return Maps.newHashMap();
        }
        Map<K, Integer> result = new HashMap<>();
        objectIdAndTypeIds.forEach((objectId, typeIds) -> result.put(objectId, typeIds.size()));
        return result;
    }

    /**
     * 解析待统计的实例ID
     *
     * @param spaceId 空间ID
     * @return 待统计的实例ID
     * @since 5.241202.0
     */
    private Set<String> parseToBeStatisticsIds(@NonNull String spaceId) {
        Set<String> toBeStatisticsIds = new LinkedHashSet<>();
        toBeStatisticsIds.add(spaceId);
        toBeStatisticsIds.addAll(spaceIdAndAllParentIds.getOrDefault(spaceId, Collections.emptyList()));
        return toBeStatisticsIds;
    }

    /**
     * 时间点是否存在于任何一个时间区间中
     *
     * @param time          时间点
     * @param timeIntervals 时间区间
     * @return 时间点是否存在于任何一个时间区间中
     * @since 5.241201.0
     */
    private boolean anyMatchTime(@NonNull LocalTime time, @NonNull List<DemandTimeIntervalValueObject> timeIntervals) {
        if (CollectionUtils.isEmpty(timeIntervals)) {
            return false;
        }
        for (DemandTimeIntervalValueObject timeInterval : timeIntervals) {
            List<Range<LocalTime>> intervals = timeInterval.analyzeTimeInterval(
                    DemandConfigSecurityPassingAnalyzeStrategy.TIME_TYPE, null);
            if (intervals.stream().anyMatch(item -> item.contains(time))) {
                return true;
            }
        }
        return false;
    }
}
