package com.persagy.apccontrol.basic.service.security.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.context.security.SecurityDashboardQueryContext;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.model.enumtype.AbnormalEquipmentEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.*;
import com.persagy.apccontrol.basic.model.param.security.SecurityDashboardQueryParam;
import com.persagy.apccontrol.basic.model.param.security.SecurityModelViewParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceRelObjVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.model.vo.security.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectModelViewQueryDaoParam;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.BizComputeService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.service.security.SecurityService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectModelViewRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorSecurityStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.param.DemandInstanceConfigQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.security.converter.SecurityConverter;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 安防空间
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/4 18:54
 * @since v5.240801.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SecurityServiceImpl implements SecurityService {

    /**
     * READY Dashboard 空间静态字段。
     *
     * @since v7.260802.0
     */
    private static final @NonNull Set<String> SECURITY_DASHBOARD_SPACE_FIELDS =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    DtpRwdObjectDTO.Fields.id,
                    DtpRwdObjectDTO.Fields.localName,
                    DtpRwdSpaceDTO.Fields.floorId,
                    DtpRwdSpaceDTO.Fields.buildingId)));

    /**
     * READY Dashboard 设备静态字段。
     *
     * @since v7.260802.0
     */
    private static final @NonNull Set<String> SECURITY_DASHBOARD_EQUIPMENT_FIELDS =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    DtpRwdObjectDTO.Fields.id,
                    DtpRwdObjectDTO.Fields.classCode,
                    SecurityDtpRwdEquipmentDTO.Fields.hasArmingFunc)));

    /**
     * READY Dashboard 建筑静态字段。
     *
     * @since v7.260802.0
     */
    private static final @NonNull Set<String> SECURITY_DASHBOARD_BUILDING_FIELDS =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    DtpRwdObjectDTO.Fields.id,
                    DtpRwdObjectDTO.Fields.localName,
                    DtpRwdBuildingDTO.Fields.buildingSequenceId)));

    /**
     * READY Dashboard 楼层静态字段。
     *
     * @since v7.260802.0
     */
    private static final @NonNull Set<String> SECURITY_DASHBOARD_FLOOR_FIELDS =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    DtpRwdObjectDTO.Fields.id,
                    DtpRwdObjectDTO.Fields.localName,
                    DtpRwdFloorDTO.Fields.buildingId,
                    DtpRwdFloorDTO.Fields.floorSequenceId)));

    /**
     * READY 设备列表从对象缓存恢复时固定使用的字段集合。
     *
     * <p>字段由 SECURITY 十二类设备的对象字段配置合并得到并在类加载时固化；模型图字段
     * 不属于对象主表读取范围，仍由模型视图批量补齐。</p>
     *
     * @since v7.260802.0
    */
    private static final @NonNull Set<String> SECURITY_EQUIPMENT_CACHE_FIELDS =
            CacheObjectExtensionFieldConfig.listObjectQueryFields(
                    SECURITY_EQUIPMENT_CLASS_VALUE);

    /**
     * 设备统计缓存候选限定的六类设备。
     *
     * @since v7.260802.0
     */
    private static final @NonNull Set<String> SECURITY_STATISTICS_CLASS_CODES =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    DtpRwdClassCodeEnum.CFCSDR.value(),
                    DtpRwdClassCodeEnum.CFCSFR.value(),
                    DtpRwdClassCodeEnum.SPEGFG.value(),
                    DtpRwdClassCodeEnum.SPIACU.value(),
                    DtpRwdClassCodeEnum.SPVSCM.value(),
                    DtpRwdClassCodeEnum.WEGMCM.value())));

    /**
     * READY 设备统计读取静态候选时固定查询的字段。
     *
     * @since v7.260802.0
     */
    private static final @NonNull Set<String> SECURITY_STATISTICS_CACHE_FIELDS =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    DtpRwdObjectDTO.Fields.id,
                    DtpRwdObjectDTO.Fields.classCode,
                    DtpRwdEquipmentDTO.Fields.floorId,
                    MONITORING_SURVEILLANCE_STATUS_CODE,
                    HAS_ARMING_FUNC_CODE,
                    CAMERA_TYPE_CODE,
                    InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode())));

    /**
     * 设备统计固定实时查询的四个状态信息点。
     *
     * @since v7.260802.0
     */
    private static final @NonNull List<String> SECURITY_STATISTICS_INFO_CODES =
            Collections.unmodifiableList(Arrays.asList(
                    InfoCodeEnum.ARMING_STATUS.value(),
                    InfoCodeEnum.ONLINE_STATUS.getCode(),
                    InfoCodeEnum.RUN_STATUS.getCode(),
                    InfoCodeEnum.DEPLOY_WITHDRAWALARM_STATUS.getCode()));

    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final AbstractCommonServer abstractCommonServer;
    private final RunningMonitorSecurityStrategy runningMonitorSecurityStrategy;
    private final SecurityConverter securityConverter;
    private final BizDtpRwdService bizDtpRwdService;
    private final InstanceService instanceService;
    private final BizComputeService bizComputeService;
    private final DemandConfigGateway<BaseDemandConfig> demandConfigGateway;
    private final DemandInstanceGateway demandInstanceGateway;
    private final CacheObjectModelStatusService cacheObjectModelStatusService;
    private final CacheObjectRepository cacheObjectRepository;
    private final CacheObjectRelRepository cacheObjectRelRepository;
    private final CacheObjectModelViewRepository cacheObjectModelViewRepository;
    private final CacheObjectModelViewRecordConverter cacheObjectModelViewRecordConverter;

    @Override
    public SecurityDashboardVO dashboard(@NonNull SecurityDashboardQueryParam param) {
        SecurityDashboardQueryContext context = this.newDashboardQueryContext(param);
        StopWatch stopWatch = StopWatch.create("Dashboard");
        stopWatch.start("填充实例");
        if (this.cacheObjectModelStatusService.isReady(RunningMonitorTypeEnum.SECURITY)) {
            this.fillDashboardInstanceFromCache(context, param);
        } else {
            context.fillInstance();
        }
        stopWatch.stop();
        stopWatch.start("填充需求配置");
        context.fillDemandConfig();
        stopWatch.stop();
        stopWatch.start("填充空间ID和上级关系");
        context.fillSpaceIdAndParents();
        stopWatch.stop();
        stopWatch.start("填充IOT数据");
        context.fillIotData();
        stopWatch.stop();
        stopWatch.start("计算统计数据");
        context.computeStatisticsData();
        stopWatch.stop();
        stopWatch.start("输出结果");
        SecurityDashboardVO result = context.toDashboardVo();
        stopWatch.stop();
        logProjectInstance().debug("查询Dashboard",
                Pair.of("data", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return result;
    }

    /**
     * 创建单次 Dashboard 查询上下文。
     *
     * @param param Dashboard 查询参数
     * @return 查询上下文
     * @since v7.260802.0
     */
    SecurityDashboardQueryContext newDashboardQueryContext(
            @NonNull SecurityDashboardQueryParam param) {
        return new SecurityDashboardQueryContext(
                param,
                this.runningMonitorSecurityStrategy,
                this.dtpRwdClientService,
                this.abstractIotDataCacheServer,
                this.demandConfigGateway,
                this.instanceService,
                this.bizComputeService);
    }

    /**
     * 从 SECURITY 完整静态快照填充 Dashboard 实例。
     *
     * <p>READY 分支只访问本地对象、关系和需求实例配置，不访问 RWD；四类对象的字段集合
     * 固定在类常量中。缓存空集合是权威结果。</p>
     *
     * @param context 单次查询上下文
     * @param param Dashboard 查询参数
     * @since v7.260802.0
     */
    private void fillDashboardInstanceFromCache(
            @NonNull SecurityDashboardQueryContext context,
            @NonNull SecurityDashboardQueryParam param) {
        DemandTemplateTypeEnum templateType = param.getRunningMonitorSecondType()
                .toDemandTemplateTypeEnum();
        List<DtpRwdSpaceDTO> spaces = this.cacheObjectRepository.listByCacheKey(
                CacheObjectModelKeyConfig.SECURITY_LIST_SPACE,
                DtpRwdSpaceDTO.class,
                SecurityServiceImpl.SECURITY_DASHBOARD_SPACE_FIELDS);
        if (spaces.isEmpty()) {
            context.fillInstanceFromCache(
                    spaces,
                    Collections.emptyList(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptySet(),
                    Collections.emptyList(),
                    Collections.emptyList());
            return;
        }

        List<SecurityDtpRwdEquipmentDTO> equipments =
                this.cacheObjectRepository.listByCacheKey(
                        CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT,
                        SecurityDtpRwdEquipmentDTO.class,
                        SecurityServiceImpl.SECURITY_DASHBOARD_EQUIPMENT_FIELDS);
        Map<String, Set<String>> serviceSpaceIdsByEquipment = Collections.emptyMap();
        Map<String, Set<String>> inSpaceIdsByEquipment = Collections.emptyMap();
        if (templateType == DemandTemplateTypeEnum.ARMING) {
            Set<String> controllerIds = equipments.stream()
                    .filter(equipment -> DtpRwdClassCodeEnum.SPIACU.value().equals(
                            equipment.getClassCode()))
                    .map(DtpRwdObjectDTO::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            Set<String> armingCameraIds = equipments.stream()
                    .filter(equipment -> DtpRwdClassCodeEnum.SPVSCM.value().equals(
                            equipment.getClassCode()))
                    .filter(equipment -> HAS_ARMING_FUNC.equals(
                            equipment.getHasArmingFunc()))
                    .map(DtpRwdObjectDTO::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            serviceSpaceIdsByEquipment = this.mapForwardRelationTargets(
                    controllerIds, DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE);
            inSpaceIdsByEquipment = this.mapForwardRelationTargets(
                    armingCameraIds, DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE);
        }

        Set<String> spaceIds = spaces.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        DemandInstanceConfigQueryParam controlledQuery =
                new DemandInstanceConfigQueryParam();
        controlledQuery.setTemplateType(templateType);
        controlledQuery.setInstanceType(DemandInstanceTypeEnum.SPACE);
        controlledQuery.setInstanceIds(spaceIds);
        controlledQuery.setFilterConfig(new DemandInstanceConfigValueObject()
                .setShielded(true)
                .setInBlacklisted(true));
        Set<String> excludedSpaceIds = this.demandInstanceGateway
                .listInstanceIds(controlledQuery);
        List<DtpRwdBuildingDTO> buildings = this.cacheObjectRepository.listByClassCode(
                DtpRwdClassCodeEnum.BUILDING.value(),
                DtpRwdBuildingDTO.class,
                SecurityServiceImpl.SECURITY_DASHBOARD_BUILDING_FIELDS);
        List<DtpRwdFloorDTO> floors = this.cacheObjectRepository.listByClassCode(
                DtpRwdClassCodeEnum.FLOOR.value(),
                DtpRwdFloorDTO.class,
                SecurityServiceImpl.SECURITY_DASHBOARD_FLOOR_FIELDS);
        context.fillInstanceFromCache(
                spaces,
                equipments,
                serviceSpaceIdsByEquipment,
                inSpaceIdsByEquipment,
                excludedSpaceIds,
                buildings,
                floors);
    }

    /**
     * @return java.util.List<com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO>
     * @author wxf
     * @description 获取通行空间
     * @date 18:18 2024/12/27
     **/
    @Override
    public List<DtpRwdSpaceRelObjVO> listAccessSpace() {
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = runningMonitorSecurityStrategy.listAccessSpace();
        return BeanUtil.copyToList(dtpRwdSpaceDTOS, DtpRwdSpaceRelObjVO.class);
    }

    @Override
    public List<SecurityEquipmentStatusVO> listEquipment() {
        if (this.cacheObjectModelStatusService.isReady(
                RunningMonitorTypeEnum.SECURITY)) {
            List<SecurityDtpRwdEquipmentDTO> cachedEquipments =
                    this.listSecurityEquipmentFromCache();
            this.mergeEquipmentModelViewFields(cachedEquipments);
            return processingEquipmentInfo(
                    cachedEquipments, new HashMap<>(), new HashMap<>(), new HashMap<>());
        }
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.addWithColumn(SecurityDtpRwdEquipmentDTO.Fields.class);
        List<SecurityDtpRwdEquipmentDTO> secureAllEquipmentList =
                this.getSecureAllEquipmentList(
                        ContextUtil.getProjectId(), false, dtpRwdObjectQueryParam);
        return processingEquipmentInfo(
                secureAllEquipmentList, new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    /**
     * @return java.util.List<com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO>
     * @author wxf
     * @description 获取撤布防空间
     * @date 18:18 2024/12/27
     **/
    @Override
    public List<DtpRwdSpaceVO> listDeploymentSpace() {
        if (this.cacheObjectModelStatusService.isReady(RunningMonitorTypeEnum.SECURITY)) {
            return this.listDeploymentSpacesFromCache();
        }
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = runningMonitorSecurityStrategy.listDeploymentSpace();
        return BeanUtil.copyToList(dtpRwdSpaceDTOS, DtpRwdSpaceVO.class);
    }


    @Override
    public SecurityModelViewVO querySecurityModelView(SecurityModelViewParam viewParam) {
        StopWatch stopWatch = StopWatch.create("查询安防模型图");
        String projectId = ContextUtil.getProjectId();
        // 当前视角下的所有空间
        List<DtpRwdSpaceDTO> instanceRelatedAllSpaceList = getAllSpaceInPerspective(viewParam);

        SecurityModelViewVO securityModelViewVO = new SecurityModelViewVO();
        if (CollectionUtils.isNotEmpty(instanceRelatedAllSpaceList)) {
            List<String> spaceIdList = instanceRelatedAllSpaceList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());

            // 查询所有设备
            DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
            dtpRwdObjectQueryParam.addWithColumn(SecurityDtpRwdEquipmentDTO.Fields.class);
            List<SecurityDtpRwdEquipmentDTO> dtpRwdEquipmentList = getSecureAllEquipmentList(projectId, true, dtpRwdObjectQueryParam);

            List<SecurityDtpRwdEquipmentDTO> doorDtpEquipmentList =
                    dtpRwdEquipmentList.stream().filter(x -> DOOR_EQUIPMENT_CLASS_STRING.contains(x.getClassCode())).collect(Collectors.toList());
            // 当前视角下的所有设备
            Map<String, RunStatusEnum> equipmentRunStateMap = new HashMap<>();
            List<String> equipmentIdList = dtpRwdEquipmentList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());

            // 模型数据转枚举
            AtomicReference<Map<String, DeploymentStatusEnum>> deploymentStatusAtomicReference = new AtomicReference<>();
            AtomicReference<Map<String, AccessDemandStatusEnum>> spaceAccessDemandAtomicReference = new AtomicReference<>();
            AtomicReference<Map<String, List<AbnormalEquipmentEnum>>> spaceAndEquipmentAbnormalStateAtomicReference = new AtomicReference<>();

            // 空间和设备异常状态
            CompletableFuture<AtomicReference<Map<String, List<AbnormalEquipmentEnum>>>> atomicReferenceAbnormalCompletableFuture =
                    CompletableFuture.supplyAsync(() -> {
                        spaceAndEquipmentAbnormalStateAtomicReference.set(
                                getSpaceAndEquipmentAbnormalStateMap(projectId, equipmentIdList, spaceIdList, equipmentRunStateMap));
                        return null;
                    }, BusinessThreadPoolExecutor.ttlExecutor());

            // 空间撤布防状态
            CompletableFuture<AtomicReference<Map<String, DeploymentStatusEnum>>> mapDeploymentStatusFuture =
                    CompletableFuture.supplyAsync(() -> {
                        deploymentStatusAtomicReference.set(mapSpaceEvacuationDefenseStatus(spaceIdList));
                        return null;
                    }, BusinessThreadPoolExecutor.ttlExecutor());

            Map<String, AccessDemandStatusEnum> equipmentAccessMap = new HashMap<>();
            // 空间通行状态
            CompletableFuture<AtomicReference<Map<String, AccessDemandStatusEnum>>> atomicReferenceAccessDemandCompletableFuture =
                    CompletableFuture.supplyAsync(() ->
                    {
                        if (viewParam.getInstanceType() == DemandInstanceTypeEnum.PROJECT) {
                            spaceAccessDemandAtomicReference.set(mapSpaceAccessStatePartialQuantity(projectId));
                        } else {
                            spaceAccessDemandAtomicReference.set(mapSpaceAccessState(projectId, spaceIdList, doorDtpEquipmentList, equipmentAccessMap));
                        }
                        return null;
                    }, BusinessThreadPoolExecutor.ttlExecutor());

            // 撤布防空间转换
            AtomicReference<List<String>> withdrawGarrisonAtomicReference = new AtomicReference<>();
            CompletableFuture<AtomicReference<List<String>>> atomicReferenceWithdrawGarrisonFuture =
                    CompletableFuture.supplyAsync(() -> {
                        withdrawGarrisonAtomicReference.set(runningMonitorSecurityStrategy.listEvacuationDefenseSpaceByEquipment(projectId));
                        return null;
                    }, BusinessThreadPoolExecutor.ttlExecutor());

            AtomicReference<Set<String>> trafficAtomicReference = new AtomicReference<>();
            // 有门的通行状态空间
            CompletableFuture<AtomicReference<List<String>>> atomicReferenceTrafficFuture =
                    CompletableFuture.supplyAsync(() -> {
                        trafficAtomicReference.set(getSecureDoorSpaceList(projectId));
                        return null;
                    }, BusinessThreadPoolExecutor.ttlExecutor());

            stopWatch.start("joinAll");
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    mapDeploymentStatusFuture,
                    atomicReferenceAccessDemandCompletableFuture,
                    atomicReferenceAbnormalCompletableFuture,
                    atomicReferenceWithdrawGarrisonFuture,
                    atomicReferenceTrafficFuture
            );
            allFutures.join();
            stopWatch.stop();


            stopWatch.start("数据处理");
            // 数据处理
            Map<String, List<AbnormalEquipmentEnum>> spaceAndEquipmentAbnormalStateMap = spaceAndEquipmentAbnormalStateAtomicReference.get();
            securityModelViewVO.setSpaces(
                    processingSpaceInfo(
                            instanceRelatedAllSpaceList, deploymentStatusAtomicReference.get(),
                            spaceAccessDemandAtomicReference.get(), spaceAndEquipmentAbnormalStateMap,
                            withdrawGarrisonAtomicReference.get(), trafficAtomicReference.get()
                    )
            );
            securityModelViewVO.setEquipments(
                    processingEquipmentInfo(
                            dtpRwdEquipmentList, spaceAndEquipmentAbnormalStateMap,
                            equipmentRunStateMap, equipmentAccessMap
                    )
            );

            stopWatch.stop();

        }

        logProjectInstance(projectId).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return securityModelViewVO;
    }

    /**
     * 处理设备数据转换
     *
     * @param dtpRwdEquipmentList               所有设备信息
     * @param spaceAndEquipmentAbnormalStateMap 空间设备异常状态集合
     * @return 设备合并状态转换后的VO
     */
    public List<SecurityEquipmentStatusVO> processingEquipmentInfo(List<SecurityDtpRwdEquipmentDTO> dtpRwdEquipmentList,
                                                                   Map<String, List<AbnormalEquipmentEnum>> spaceAndEquipmentAbnormalStateMap,
                                                                   Map<String, RunStatusEnum> equipmentRunStateMap,
                                                                   Map<String, AccessDemandStatusEnum> equipmentAccessMap) {
        Set<String> classCodeSet = dtpRwdEquipmentList.stream().map(SecurityDtpRwdEquipmentDTO::getClassCode).collect(Collectors.toSet());

        //查询是否可控
        List<DtpRwdTwinControlObjectDTO> controlObjects = dtpRwdClientService.listTwinControlObjects(false, ContextUtil.getProjectId(), classCodeSet);
        Map<String, List<String>> controlObjectIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(controlObjects)) {
            // 按className分组,后将id合并为数组
            controlObjectIdMap = controlObjects.stream()
                    .collect(Collectors.groupingBy(
                                    DtpRwdTwinControlObjectDTO::getClassCode,
                                    Collectors.mapping(DtpRwdTwinControlObjectDTO::getObjectId,
                                            Collectors.toList())
                            )
                    );
        }

        List<SecurityEquipmentStatusVO> equipmentResultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(dtpRwdEquipmentList)) {
            return equipmentResultList;
        }
        Map<String, List<String>> finalControlObjectIdMap = controlObjectIdMap;
        dtpRwdEquipmentList.forEach(equipment -> {
            SecurityEquipmentStatusVO equipmentStatusVo = securityConverter.toEquipmentStatusVo(equipment);
            String equipmentId = equipment.getId();
            equipmentStatusVo.setAbnormalTypes(spaceAndEquipmentAbnormalStateMap.getOrDefault(equipmentId, new ArrayList<>()));
            List<String> controlObjectsList = finalControlObjectIdMap.getOrDefault(equipment.getClassCode(), null);
            equipmentStatusVo.setCameraIsControl(equipment.getIsControl());
            equipmentStatusVo.setIsControl(CollectionUtils.isNotEmpty(controlObjectsList) && controlObjectsList.contains(equipmentId));
            equipmentStatusVo.setIconType(getEquipmentIconType(equipment));
            equipmentStatusVo.setRunStatus(equipmentRunStateMap.getOrDefault(equipmentId, null));
            if (MapUtils.isNotEmpty(equipmentAccessMap)) {
                equipmentStatusVo.setAccessDemandStatus(equipmentAccessMap.get(equipmentId));
            }
            equipmentResultList.add(equipmentStatusVo);
        });
        return equipmentResultList;
    }

    /**
     * 获取图标类型
     *
     * @param equipment 设备信息
     * @return 图标类型
     */
    public IconTypeEnum getEquipmentIconType(SecurityDtpRwdEquipmentDTO equipment) {
        String classCode = equipment.getClassCode();
        if (FIRST_CLASS_SMALL_ICON_EQUIPMENT_CLASS.contains(classCode)) {
            if (SPVSCM_CAMERA_TYPE.contains(equipment.getCameraType())
                    || Objects.equals(equipment.getHasArmingFunc(), CameraEnum.CAMERAS_WITH_REMOVAL_CAPABILITY.value())) {
                return IconTypeEnum.FIRST_CLASS_SMALL_ICON;
            }
        } else if (SECOND_CLASS_SMALL_ICON_EQUIPMENT_CLASS.contains(classCode)) {
            return IconTypeEnum.SECOND_CLASS_SMALL_ICON;
        }
        return IconTypeEnum.NORMAL_ICON;
    }

    /**
     * 处理空间数据转换
     *
     * @param instanceRelatedAllSpaceList       所有空间信息
     * @param deploymentStatusMap               撤布防状态
     * @param spaceAccessDemandMap              空间通行状态
     * @param spaceAndEquipmentAbnormalStateMap 空间设备异常状态集合
     * @return 空间合并状态转换后的VO
     */
    public List<SecuritySpaceStatusVO> processingSpaceInfo(List<DtpRwdSpaceDTO> instanceRelatedAllSpaceList,
                                                           Map<String, DeploymentStatusEnum> deploymentStatusMap,
                                                           Map<String, AccessDemandStatusEnum> spaceAccessDemandMap,
                                                           Map<String, List<AbnormalEquipmentEnum>> spaceAndEquipmentAbnormalStateMap,
                                                           List<String> withdrawGarrisonIdList,
                                                           Set<String> trafficIdList) {
        List<SecuritySpaceStatusVO> spaceResultList = new ArrayList<>();
        instanceRelatedAllSpaceList.forEach(space -> {
            SecuritySpaceStatusVO spaceStatusVo = securityConverter.toSpaceStatusVo(space);
            String id = space.getId();
            if (withdrawGarrisonIdList.contains(id)) {
                spaceStatusVo.setDeploymentStatus(deploymentStatusMap.getOrDefault(id, DeploymentStatusEnum.UNKNOWN));
            }
            if (MapUtils.isNotEmpty(spaceAccessDemandMap) && trafficIdList.contains(id)) {
                spaceStatusVo.setAccessDemandStatus(spaceAccessDemandMap.getOrDefault(id, AccessDemandStatusEnum.UNMONITORED));
            }
            spaceStatusVo.setAbnormal(spaceAndEquipmentAbnormalStateMap.containsKey(id));
            spaceResultList.add(spaceStatusVo);
        });
        return spaceResultList;
    }

    /**
     * 获取当前视角下门所在的空间
     *
     * @param projectId 项目id
     * @return rwd对象带有安防特有的判断字段
     */
    public Set<String> getSecureDoorSpaceList(String projectId) {
        StopWatch stopWatch = StopWatch.create("查询安防模型图getSecureDoorSpaceList");
        stopWatch.start("数据处理");
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        dtpRwdObjectQueryParam.setClassCodeEnum(DOOR_EQUIPMENT_CLASS);
        dtpRwdObjectQueryParam.filterMonitoringSurveillanceStatus();
        List<DtpRwdEquipmentDTO> secureEquipmentList = dtpRwdClientService.listEquipment(dtpRwdObjectQueryParam);
        if (CollectionUtils.isNotEmpty(secureEquipmentList)) {
            List<String> equipmentId = secureEquipmentList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
            List<DtpRwdSpaceDTO> rwdSpaceList = dtpRwdClientService.listSpaceInByEquipmentId(equipmentId,
                    new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.id));
            if (CollectionUtils.isEmpty(rwdSpaceList)) {
                return new HashSet<>();
            }
            stopWatch.stop();
            logProjectInstance(projectId).debug("响应时间记录",
                    Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
            return rwdSpaceList.stream()
                    .map(DtpRwdObjectDTO::getId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
        }
        stopWatch.stop();
        logProjectInstance(projectId).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return new HashSet<>();
    }


    /**
     * 获取当前视角下安防设备类所有设备类
     *
     * @param projectId 项目id
     * @return rwd对象带有安防特有的判断字段
     */
    @Override
    public List<SecurityDtpRwdEquipmentDTO> getSecureAllEquipmentList(String projectId, Boolean needRelation,
                                                                      DtpRwdObjectQueryParam dtpRwdObjectQueryParam) {
        return this.getSecureAllEquipmentListFromRwd(
                projectId, needRelation, dtpRwdObjectQueryParam);
    }

    /**
     * 使用原 RWD 路径查询并过滤安防设备。
     *
     * @param projectId 项目 ID
     * @param needRelation 是否返回设备所在空间关系
     * @param dtpRwdObjectQueryParam 原查询参数
     * @return 与改造前一致的安防设备列表
     * @since v7.260802.0
     */
    private @NonNull List<SecurityDtpRwdEquipmentDTO> getSecureAllEquipmentListFromRwd(
            @NonNull String projectId,
            @NonNull Boolean needRelation,
            @NonNull DtpRwdObjectQueryParam dtpRwdObjectQueryParam) {
        dtpRwdObjectQueryParam.addWithColumn(HAS_ARMING_FUNC_CODE, MONITORING_SURVEILLANCE_STATUS_CODE, CAMERA_TYPE_CODE);
        dtpRwdObjectQueryParam.setClassCodeEnum(SECURITY_EQUIPMENT_CLASS);
        List<SecurityDtpRwdEquipmentDTO> secureEquipmentList = dtpRwdClientService.listSecurityEquipment(
                false, projectId, dtpRwdObjectQueryParam);

        ArrayList<SecurityDtpRwdEquipmentDTO> resultFilteredEquipments = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(secureEquipmentList)) {
            for (SecurityDtpRwdEquipmentDTO securityDtpRwdEquipmentDTO : secureEquipmentList) {
                String classCode = securityDtpRwdEquipmentDTO.getClassCode();
                // if为门组  else if为摄像头  else 其他设备直接加入响应集合
                if (DOOR_EQUIPMENT_CLASS_STRING.contains(classCode)) {
                    // 过滤可监、可控的门，如果为可监、可控的门则加入响应集合
                    if (MONITORING_SURVEILLANCE_STATUS_LIST.contains(getUnDefinedValue(securityDtpRwdEquipmentDTO, MONITORING_SURVEILLANCE_STATUS_CODE))) {
                        resultFilteredEquipments.add(securityDtpRwdEquipmentDTO);
                    }
                } else if (classCode.equals(DtpRwdClassCodeEnum.SPVSCM.value())) {
                    // CameraEnum cameraType为1、2、3的设备    else if或者为hasArmingFunc为1的设备  则加入响应集合
                    if (SPVSCM_CAMERA_TYPE.contains(securityDtpRwdEquipmentDTO.getCameraType())) {
                        resultFilteredEquipments.add(securityDtpRwdEquipmentDTO);
                    } else if (CameraEnum.CAMERAS_WITH_REMOVAL_CAPABILITY.value().equals(securityDtpRwdEquipmentDTO.getHasArmingFunc())) {
                        resultFilteredEquipments.add(securityDtpRwdEquipmentDTO);
                    }
                } else {
                    resultFilteredEquipments.add(securityDtpRwdEquipmentDTO);
                }
            }

            if (needRelation && CollectionUtils.isNotEmpty(resultFilteredEquipments)) {
                List<String> equipmentIdList = resultFilteredEquipments.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
                List<JSONObject> queryRelationlist = this.bizDtpRwdService.queryRelation(
                        ContextUtil.getGroupCode(), projectId, "MechInArch", "Eq2Sp", DtpRwdClassCodeEnum.SPACE.getValue(), equipmentIdList, null);
                if (CollectionUtils.isNotEmpty(queryRelationlist)) {
                    Map<String, Set<String>> relationMap = queryRelationlist.stream()
                            .collect(Collectors.groupingBy(
                                    json -> json.getString("objFrom"),
                                    Collectors.mapping(json -> json.getString("objTo"), Collectors.toSet())
                            ));
                    resultFilteredEquipments.forEach(x -> x.setRelObjIds(relationMap.getOrDefault(x.getId(), new HashSet<>())));
                }
            }
            //给摄像头的设备添加空间功能类型
            if (CollectionUtils.isNotEmpty(resultFilteredEquipments)) {
                List<SecurityDtpRwdEquipmentDTO> cameraEquips = resultFilteredEquipments.stream().filter(x -> CAMERA_CLASS_CODE_LIST.contains(x.getClassCode())).collect(Collectors.toList());
                Set<String> objectSet = cameraEquips.stream().map(SecurityDtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
                //查询空间
                List<DtpRwdSpaceDTO> dtpRwdSpaces = dtpRwdClientService.listSpaceInByEquipmentId(objectSet, null);
                Map<String, List<DtpRwdSpaceDTO>> objectIdSpaceListMap = DtpRwdSpaceDTO.mapRelObjIdAndData(dtpRwdSpaces);
                Map<String, List<String>> objectIdAndSpaceFuncTypesMap = Maps.newHashMap();
                for (Map.Entry<String, List<DtpRwdSpaceDTO>> entry : objectIdSpaceListMap.entrySet()) {
                    objectIdAndSpaceFuncTypesMap.put(entry.getKey(), entry.getValue().stream().map(DtpRwdSpaceDTO::getRoomFuncType).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
                }
                resultFilteredEquipments.forEach(x -> {
                    x.setRoomFuncTypes(objectIdAndSpaceFuncTypesMap.getOrDefault(x.getId(), new ArrayList<>()));
                });
            }
        }
        return resultFilteredEquipments;
    }

    /**
     * 从 SECURITY 新对象模型读取并按旧接口规则组装安防设备。
     *
     * <p>READY 分支不访问 RWD。对象字段、所在空间关系和空间功能类型全部批量读取；缓存
     * 空结果是权威结果，不触发实时穿透。</p>
     *
     * @return 过滤和关系补全后的安防设备
     * @since v7.260802.0
     */
    private @NonNull List<SecurityDtpRwdEquipmentDTO> listSecurityEquipmentFromCache() {
        List<SecurityDtpRwdEquipmentDTO> cachedEquipments =
                this.cacheObjectRepository.listByCacheKey(
                        CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT,
                        SecurityDtpRwdEquipmentDTO.class,
                        SecurityServiceImpl.SECURITY_EQUIPMENT_CACHE_FIELDS);
        List<SecurityDtpRwdEquipmentDTO> filteredEquipments = cachedEquipments.stream()
                .filter(this::matchesSecurityEquipmentContract)
                .collect(Collectors.toCollection(ArrayList::new));
        if (CollectionUtils.isEmpty(filteredEquipments)) {
            return filteredEquipments;
        }

        Set<String> equipmentIds = filteredEquipments.stream()
                .filter(item -> DOOR_EQUIPMENT_CLASS_STRING.contains(item.getClassCode())
                        || CAMERA_CLASS_CODE_LIST.contains(item.getClassCode()))
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, Set<String>> inSpaceIdsByEquipment = this.mapForwardRelationTargets(
                equipmentIds, DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE);

        Set<String> cameraIds = filteredEquipments.stream()
                .filter(item -> CAMERA_CLASS_CODE_LIST.contains(item.getClassCode()))
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> cameraSpaceIds = cameraIds.stream()
                .flatMap(id -> inSpaceIdsByEquipment.getOrDefault(
                        id, Collections.emptySet()).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<DtpRwdSpaceDTO> cameraSpaces = cameraSpaceIds.isEmpty()
                ? Collections.emptyList()
                : this.cacheObjectRepository.listByObjectIds(
                cameraSpaceIds,
                DtpRwdSpaceDTO.class,
                Arrays.asList(
                        DtpRwdObjectDTO.Fields.id,
                        DtpRwdSpaceDTO.Fields.roomFuncType));
        Map<String, String> roomFuncTypeBySpaceId = cameraSpaces.stream()
                .filter(space -> StringUtils.isNotBlank(space.getRoomFuncType()))
                .collect(Collectors.toMap(
                        DtpRwdObjectDTO::getId,
                        DtpRwdSpaceDTO::getRoomFuncType,
                        (first, second) -> first,
                        LinkedHashMap::new));
        for (SecurityDtpRwdEquipmentDTO equipment : filteredEquipments) {
            List<String> roomFuncTypes = inSpaceIdsByEquipment
                    .getOrDefault(equipment.getId(), Collections.emptySet())
                    .stream()
                    .map(roomFuncTypeBySpaceId::get)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            equipment.setRoomFuncTypes(roomFuncTypes);
        }
        return filteredEquipments;
    }

    /**
     * 从 SECURITY 设备 Key 读取功能开关所需的最小静态字段。
     *
     * <p>该路径不读取关系或空间，避免功能接口为布尔判断加载无关静态数据。</p>
     *
     * @return 摄像头、入侵报警控制器和满足监控状态要求的门
     * @since v7.260802.0
     */
    private @NonNull List<SecurityDtpRwdEquipmentDTO>
            listSecurityFunctionEquipmentFromCache() {
        List<SecurityDtpRwdEquipmentDTO> equipments =
                this.cacheObjectRepository.listByCacheKey(
                        CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT,
                        SecurityDtpRwdEquipmentDTO.class,
                        Arrays.asList(
                                DtpRwdObjectDTO.Fields.id,
                                DtpRwdObjectDTO.Fields.classCode,
                                SecurityDtpRwdEquipmentDTO.Fields.hasArmingFunc,
                                SecurityDtpRwdEquipmentDTO.Fields.cameraType,
                                MONITORING_SURVEILLANCE_STATUS_CODE));
        return equipments.stream()
                .filter(equipment -> DtpRwdClassCodeEnum.SPVSCM.value().equals(
                        equipment.getClassCode())
                        || DtpRwdClassCodeEnum.SPIACU.value().equals(
                        equipment.getClassCode())
                        || DOOR_EQUIPMENT_CLASS_STRING.contains(equipment.getClassCode()))
                .filter(equipment -> !DOOR_EQUIPMENT_CLASS_STRING.contains(
                        equipment.getClassCode())
                        || MONITORING_SURVEILLANCE_STATUS_LIST.contains(
                        getUnDefinedValue(
                                equipment, MONITORING_SURVEILLANCE_STATUS_CODE)))
                .collect(Collectors.toList());
    }

    /**
     * 判断缓存设备是否满足旧安防接口的静态过滤规则。
     *
     * @param equipment 缓存设备
     * @return 是否保留
     * @since v7.260802.0
     */
    private boolean matchesSecurityEquipmentContract(
            @NonNull SecurityDtpRwdEquipmentDTO equipment) {
        if (DOOR_EQUIPMENT_CLASS_STRING.contains(equipment.getClassCode())) {
            return MONITORING_SURVEILLANCE_STATUS_LIST.contains(
                    getUnDefinedValue(equipment, MONITORING_SURVEILLANCE_STATUS_CODE));
        }
        if (DtpRwdClassCodeEnum.SPVSCM.value().equals(equipment.getClassCode())) {
            return SPVSCM_CAMERA_TYPE.contains(equipment.getCameraType())
                    || CameraEnum.CAMERAS_WITH_REMOVAL_CAPABILITY.value().equals(
                    equipment.getHasArmingFunc());
        }
        return true;
    }

    /**
     * 批量读取指定关系主体的正向目标。
     *
     * @param subjectIds 关系主体 ID
     * @param relationType 关系类型
     * @return 主体 ID 到去重目标 ID
     * @since v7.260802.0
     */
    private @NonNull Map<String, Set<String>> mapForwardRelationTargets(
            @NonNull Collection<String> subjectIds,
            @NonNull DtpRwdRelationTypeEnum relationType) {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (String subjectId : subjectIds) {
            result.put(subjectId, new LinkedHashSet<>());
        }
        if (subjectIds.isEmpty()) {
            return result;
        }
        CacheObjectRelQueryDaoParam queryParam = new CacheObjectRelQueryDaoParam();
        queryParam.setObjectIds(subjectIds);
        queryParam.setGraphCode(relationType.value().getLeft());
        queryParam.setRelCode(relationType.value().getRight());
        queryParam.addColumns(CacheObjectRel::getObjectId, CacheObjectRel::getRelObjectId);
        this.cacheObjectRelRepository.listForward(queryParam).forEach(relation ->
                result.computeIfAbsent(
                        relation.getObjectId(), key -> new LinkedHashSet<>())
                        .add(relation.getRelObjectId()));
        return result;
    }

    /**
     * 从模型视图表一次批量补齐安防设备 BIM 字段。
     *
     * @param equipments 当前设备列表
     * @since v7.260802.0
     */
    private void mergeEquipmentModelViewFields(
            @NonNull List<SecurityDtpRwdEquipmentDTO> equipments) {
        if (CollectionUtils.isEmpty(equipments)) {
            return;
        }
        Set<String> equipmentIds = equipments.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, SecurityDtpRwdEquipmentDTO> modelViewById =
                this.listModelViewObjects(equipmentIds, SecurityDtpRwdEquipmentDTO.class)
                        .stream()
                        .collect(Collectors.toMap(
                                DtpRwdObjectDTO::getId,
                                Function.identity(),
                                (first, second) -> first,
                                LinkedHashMap::new));
        for (SecurityDtpRwdEquipmentDTO equipment : equipments) {
            SecurityDtpRwdEquipmentDTO modelView = modelViewById.get(equipment.getId());
            if (modelView == null) {
                continue;
            }
            equipment.setBimId(modelView.getBimId());
            equipment.setBimLocation(modelView.getBimLocation());
        }
    }

    /**
     * 从模型视图表直接批量重建对象，READY 分支不得再次执行状态判断。
     *
     * @param objectIds 对象 ID
     * @param targetClass 目标 DTO 类型
     * @param <T> RWD DTO 类型
     * @return 实际命中的模型视图对象
     * @since v7.260802.0
     */
    private <T extends DtpRwdObjectDTO> @NonNull List<T> listModelViewObjects(
            @NonNull Collection<String> objectIds,
            @NonNull Class<T> targetClass) {
        if (objectIds.isEmpty()) {
            return Collections.emptyList();
        }
        CacheObjectModelViewQueryDaoParam queryParam =
                new CacheObjectModelViewQueryDaoParam();
        queryParam.setIds(objectIds);
        List<T> result = new ArrayList<>();
        this.cacheObjectModelViewRepository.listModelViews(queryParam).forEach(modelView -> {
            T object = this.cacheObjectModelViewRecordConverter.toRwdObject(
                    modelView, targetClass);
            if (object != null) {
                result.add(object);
            }
        });
        return result;
    }

    /**
     * 从缓存组装撤布防空间，并保持旧响应的完整空间模型视图字段。
     *
     * @return 入侵报警控制器服务空间和具备布撤防能力摄像头所在空间
     * @since v7.260802.0
     */
    private @NonNull List<DtpRwdSpaceVO> listDeploymentSpacesFromCache() {
        Set<String> equipmentFields = new LinkedHashSet<>(Arrays.asList(
                DtpRwdObjectDTO.Fields.id,
                DtpRwdObjectDTO.Fields.classCode,
                SecurityDtpRwdEquipmentDTO.Fields.hasArmingFunc));
        List<SecurityDtpRwdEquipmentDTO> equipments = this.cacheObjectRepository
                .listByCacheKey(
                        CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT,
                        SecurityDtpRwdEquipmentDTO.class,
                        equipmentFields);
        Set<String> controllerIds = equipments.stream()
                .filter(item -> DtpRwdClassCodeEnum.SPIACU.value().equals(
                        item.getClassCode()))
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> armingCameraIds = equipments.stream()
                .filter(item -> DtpRwdClassCodeEnum.SPVSCM.value().equals(
                        item.getClassCode()))
                .filter(item -> HAS_ARMING_FUNC.equals(item.getHasArmingFunc()))
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> spaceIds = new LinkedHashSet<>();
        this.mapForwardRelationTargets(
                controllerIds, DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE)
                .values().forEach(spaceIds::addAll);
        this.mapForwardRelationTargets(
                armingCameraIds, DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE)
                .values().forEach(spaceIds::addAll);
        if (spaceIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<DtpRwdSpaceDTO> spaces = this.cacheObjectRepository.listByObjectIds(
                spaceIds,
                DtpRwdSpaceDTO.class,
                CacheObjectExtensionFieldConfig.listObjectQueryFields(
                        DtpRwdClassCodeEnum.SPACE.value()));
        Map<String, DtpRwdSpaceDTO> modelViewById = this.listModelViewObjects(
                spaceIds, DtpRwdSpaceDTO.class).stream()
                .collect(Collectors.toMap(
                        DtpRwdObjectDTO::getId,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new));
        for (DtpRwdSpaceDTO space : spaces) {
            DtpRwdSpaceDTO modelView = modelViewById.get(space.getId());
            if (modelView == null) {
                continue;
            }
            space.setBimId(modelView.getBimId());
            space.setBimLocation(modelView.getBimLocation());
            space.setOutline(modelView.getOutline());
        }
        return BeanUtil.copyToList(spaces, DtpRwdSpaceVO.class);
    }

    /**
     * 查询设备和空间异常
     *
     * @param projectId     项目id
     * @param equipmentList 设备id
     * @param spaceIdList   空间id
     * @return 对象id->异常状态
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<AbnormalEquipmentEnum>> getSpaceAndEquipmentAbnormalStateMap(
            String projectId,
            List<String> equipmentList,
            List<String> spaceIdList,
            Map<String, RunStatusEnum> equipmentRunStateMap) {

        HashMap<String, List<AbnormalEquipmentEnum>> resultMap = new HashMap<>();
        if (CollectionUtils.isEmpty(equipmentList)) {
            return resultMap;
        }

        StopWatch stopWatch = StopWatch.create("查询设备运行/异常状态");

        stopWatch.start("异常");
        ArrayList<String> allObjectIdList = new ArrayList<>(equipmentList);
        allObjectIdList.addAll(spaceIdList);

        Map<String, List<ExceptionEventBaseInfoVO>> riskEventMap =
                this.abstractCommonServer.mapRiskEvent(
                        allObjectIdList, null);
        // 异常
        if (MapUtils.isNotEmpty(riskEventMap)) {
            riskEventMap.forEach((objectId, riskEventList) -> resultMap.put(objectId, Lists.newArrayList(AbnormalEquipmentEnum.ABNORMAL)));
        }
        stopWatch.stop();

        // 控反不一致 离线 运行停止
        stopWatch.start("信息点查询");

        List<InfoCodeValueVO> infoCodeList =
                this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                        equipmentList,
                        Lists.newArrayList(
                                InfoCodeEnum.ONLINE_STATUS.value(),
                                InfoCodeEnum.ORDER_FAIL_ALARM.value(),
                                InfoCodeEnum.RUN_STATUS.value()
                        ));
        stopWatch.stop();


        stopWatch.start("数据处理");
        if (CollectionUtils.isNotEmpty(infoCodeList)) {
            List<InfoCodeValueVO> collect = infoCodeList.stream().filter(
                    x -> (InfoCodeEnum.ONLINE_STATUS.value().equals(x.getInfoCode()) && Objects.equals(x.getInfoCodeValue(), "0"))
                            || (InfoCodeEnum.ORDER_FAIL_ALARM.value().equals(x.getInfoCode()) && Objects.equals(x.getInfoCodeValue(), "1"))
                            || InfoCodeEnum.RUN_STATUS.value().equals(x.getInfoCode())).collect(Collectors.toList());
            collect.forEach(x -> updateObjectInfo(x, resultMap, equipmentRunStateMap));
        }
        stopWatch.stop();


        logProjectInstance(projectId).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return resultMap;
    }

    /**
     * 将信息点的数据转换为枚举值
     *
     * @param infoPoint     信息点数据
     * @param objectInfoMap 对象id->异常枚举值
     */
    public static void updateObjectInfo(InfoCodeValueVO infoPoint, Map<String, List<AbnormalEquipmentEnum>> objectInfoMap,
                                        Map<String, RunStatusEnum> equipmentRunStateMap) {
        String objectId = infoPoint.getObjectId();
        List<AbnormalEquipmentEnum> enumList = objectInfoMap.getOrDefault(objectId, new ArrayList<>());

        if (InfoCodeEnum.ONLINE_STATUS.value().equals(infoPoint.getInfoCode())) {
            // 离线状态
            enumList.add(AbnormalEquipmentEnum.OFFLINE);
            objectInfoMap.put(objectId, enumList);
        } else if (InfoCodeEnum.ORDER_FAIL_ALARM.value().equals(infoPoint.getInfoCode())) {
            // 控反不一致
            enumList.add(AbnormalEquipmentEnum.CONTROL_REVERSE_INCONSISTENT);
            objectInfoMap.put(objectId, enumList);
        } else if (InfoCodeEnum.RUN_STATUS.value().equals(infoPoint.getInfoCode())) {
            equipmentRunStateMap.put(objectId,
                    Objects.equals(infoPoint.getInfoCodeValue(), "1") ? RunStatusEnum.OPEN : RunStatusEnum.CLOSE);
        }

    }

    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    @Override
    public Map<String, AccessDemandStatusEnum> mapSpaceAccessState(@NonNull String projectId,
                                                                   @NonNull List<String> spaceIdList,
                                                                   @Nullable List<SecurityDtpRwdEquipmentDTO> dtpRwdEquipmentList,
                                                                   @Nullable Map<String, AccessDemandStatusEnum> equipmentAccessMap) {
        if (CollectionUtils.isEmpty(dtpRwdEquipmentList)) {
            dtpRwdEquipmentList = new ArrayList<>();
            DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
            dtpRwdObjectQueryParam.setClassCodeEnum(DOOR_EQUIPMENT_CLASS);
            dtpRwdObjectQueryParam.addWithColumn(SecurityDtpRwdEquipmentDTO.Fields.class);
            dtpRwdObjectQueryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjTo(spaceIdList));
            dtpRwdObjectQueryParam.filterMonitoringSurveillanceStatus();
            dtpRwdEquipmentList.addAll(dtpRwdClientService.listSecurityEquipment(
                    false, projectId, dtpRwdObjectQueryParam));
        }

        HashMap<String, AccessDemandStatusEnum> spaceAccessDemandMap = new HashMap<>(16);

        // 使用Map<Object, List<String>>进行分组，key为所属空间id，value为设备id，即当前空间下的所有门
        Map<String, List<String>> mapAllEquipmentInSpaceBySpaceId = new HashMap<>(16);

        for (SecurityDtpRwdEquipmentDTO obj : dtpRwdEquipmentList) {
            Set<String> relObjIds = obj.getRelObjIds();
            if (CollectionUtils.isEmpty(relObjIds)) {
                continue;
            }
            relObjIds = relObjIds.stream().filter(spaceIdList::contains).collect(Collectors.toSet());
            for (String value : relObjIds) {
                mapAllEquipmentInSpaceBySpaceId
                        .computeIfAbsent(value, k -> new ArrayList<>())
                        .add(obj.getId());
            }
        }

        String trafficInfoCode = InfoCodeEnum.ACCESS_DEMAND.value();
        List<String> infoCodeList = Lists.newArrayList(trafficInfoCode);

        // 查信息点规则
        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>(16);
        dtpRwdEquipmentList.forEach(equipment -> MapExecutor.dynamicPut(classCodeAndObjectIds, equipment.getClassCode(), equipment.getId(), HashSet::new));
        Map<String, Set<String>> ruleInfoCodeMap = this.abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(projectId, classCodeAndObjectIds, infoCodeList);

        Map<String, String> infoCodeValueByObjectIdMap = new HashMap<>();
        // 移除不满足条件的键值对
        ruleInfoCodeMap.entrySet().removeIf(entry -> entry.getValue().isEmpty() || entry.getValue().stream().noneMatch(s -> s.equals(trafficInfoCode)));
        Set<String> equipmentHasRuleIdList = ruleInfoCodeMap.keySet();
        if (MapUtils.isEmpty(ruleInfoCodeMap)) {
            infoCodeRuleTurnEnum(mapAllEquipmentInSpaceBySpaceId, infoCodeValueByObjectIdMap, equipmentHasRuleIdList, spaceAccessDemandMap);
            return spaceAccessDemandMap;
        }

        // 查询信息点数据  规则过滤后未使用到
        List<String> equipmentIdList = dtpRwdEquipmentList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        List<InfoCodeValueVO> trafficStatusInfoList = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(equipmentIdList, infoCodeList);
        if (CollectionUtils.isNotEmpty(trafficStatusInfoList)) {
            infoCodeValueByObjectIdMap = trafficStatusInfoList.stream()
                    .collect(Collectors.toMap(InfoCodeValueVO::getObjectId, obj -> String.valueOf(obj.getInfoCodeValue())));
        }
        if (Objects.nonNull(equipmentAccessMap)) {
            // 门的通行状态
            doorAccessDemandStatus(equipmentIdList, infoCodeValueByObjectIdMap, ruleInfoCodeMap, equipmentAccessMap);
        }

        // 空间下的设备通行状态与信息点规则->转为空间的通行状态
        infoCodeRuleTurnEnum(mapAllEquipmentInSpaceBySpaceId, infoCodeValueByObjectIdMap, equipmentHasRuleIdList, spaceAccessDemandMap);
        return spaceAccessDemandMap;
    }

    /**
     * 门的通行状态转换
     *
     * @param dtpRwdEquipmentList        设备信息
     * @param infoCodeValueByObjectIdMap 信息点集合
     * @param ruleInfoCodeMap            规则集合
     */
    public void doorAccessDemandStatus(List<String> dtpRwdEquipmentList,
                                       Map<String, String> infoCodeValueByObjectIdMap,
                                       Map<String, Set<String>> ruleInfoCodeMap,
                                       Map<String, AccessDemandStatusEnum> equipmentAccessMap) {
        Set<String> equipmentHasRuleIdList = ruleInfoCodeMap.keySet();
        dtpRwdEquipmentList.forEach(id -> {
            // 如果规则包含当前门则为常开，常关，权限开启的判断逻辑
            // 否则为无法监测
            if (equipmentHasRuleIdList.contains(id) && infoCodeValueByObjectIdMap.containsKey(id)) {
                // 将map中的value转为集合
                equipmentAccessMap.put(id, getAccessDemandEnum(
                        Collections.singleton(infoCodeValueByObjectIdMap.get(id))));
            } else {
                equipmentAccessMap.put(id, AccessDemandStatusEnum.UNMONITORED);
            }
        });
    }

    /**
     * 空间信息点相关规则转枚举
     *
     * @param mapAllEquipmentInSpaceBySpaceId 空间下的设备信息
     * @param infoCodeValueByObjectIdMap      信息点数值
     * @param equipmentHasRuleIdList          信息点的规则
     * @param spaceAccessDemandMap            空间通行状态
     */
    @SuppressWarnings({"DuplicatedCode"})
    public static void infoCodeRuleTurnEnum(Map<String, List<String>> mapAllEquipmentInSpaceBySpaceId,
                                            Map<String, String> infoCodeValueByObjectIdMap,
                                            Set<String> equipmentHasRuleIdList,
                                            Map<String, AccessDemandStatusEnum> spaceAccessDemandMap) {
        mapAllEquipmentInSpaceBySpaceId.forEach((spaceId, equipmentList) -> {
            // 如果规则包含当前空间下的所有设备则为常开，常关，权限开启的判断逻辑
            // 否则为无法监测
            if (equipmentHasRuleIdList.containsAll(equipmentList)) {
                // 将map中的value转为集合
                Set<String> valuesSet = equipmentList.stream()
                        .filter(infoCodeValueByObjectIdMap::containsKey) // 确保数组中的键在map中存在
                        .map(infoCodeValueByObjectIdMap::get)            // 获取map中对应的value
                        .collect(Collectors.toSet()); // 收集到Set中
                AccessDemandStatusEnum accessDemandStatusEnum = getAccessDemandEnum(valuesSet);
                spaceAccessDemandMap.put(spaceId, accessDemandStatusEnum);
            }
        });
    }

    /**
     * 将当前空间下的所有门的信息点状态转为通行状态枚举值
     *
     * @param values 门的信息点状态数组
     * @return 通行状态
     */
    public static AccessDemandStatusEnum getAccessDemandEnum(Set<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return AccessDemandStatusEnum.UNMONITORED;
        }
        // 至少有一个实例的通行需求状态accessDemand=1（常开）
        AccessDemandStatusEnum normallyOpen = AccessDemandStatusEnum.NORMALLY_OPEN;
        boolean hasOne = values.contains(normallyOpen.conditionValue());

        // 所有实例的通行需求状态accessDemand=2（常闭）
        AccessDemandStatusEnum normalClose = AccessDemandStatusEnum.NORMAL_CLOSE;
        String normalCloseValue = normalClose.conditionValue();
        boolean allTwo = values.stream().allMatch(value -> Objects.equals(value, normalCloseValue));

        // 至少有一个实例的通行需求状态accessDemand=3（有权限即可通过），且其余所有实例的通行需求状态accessDemand=2。
        // 或者所有实例的通行需求状态accessDemand=3。
        AccessDemandStatusEnum permissionPass = AccessDemandStatusEnum.PERMISSION_PASS;
        String permissionPassValue = permissionPass.conditionValue();
        boolean hasThree = values.contains(permissionPassValue);
        boolean allTwoExceptOne = hasThree && values.stream()
                .filter(value -> !Objects.equals(value, permissionPassValue))
                .allMatch(value -> Objects.equals(value, normalCloseValue));

        // 判断状态并返回
        if (hasOne) {
            return normallyOpen;
        } else if (allTwo) {
            return normalClose;
        } else if (allTwoExceptOne) {
            return permissionPass;
        }
        // 如果没有满足的条件
        return AccessDemandStatusEnum.UNMONITORED;
    }

    /**
     * 空间撤布防状态
     *
     * @param spaceIdList 空间信息
     * @return 空间状态
     */
    public Map<String, DeploymentStatusEnum> mapSpaceEvacuationDefenseStatus(List<String> spaceIdList) {
        StopWatch stopWatch = StopWatch.create("查询安防模型图getSecureDoorSpaceList");
        stopWatch.start("数据处理");

        if (CollectionUtils.isNotEmpty(spaceIdList)) {
            return getDeploymentState(spaceIdList);
        }
        stopWatch.stop();
        logProjectInstance(ContextUtil.getProjectId()).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return new HashMap<>(16);
    }

    /**
     * 空间信息点撤布防状态
     *
     * @param spaceIdList 空间id
     * @return 撤布防状态map集合
     */
    @SuppressWarnings("unchecked")
    private Map<String, DeploymentStatusEnum> getDeploymentState(List<String> spaceIdList) {
        HashMap<String, DeploymentStatusEnum> spaceDeploymentStatusMap = new HashMap<>(16);
        List<InfoCodeValueVO> list = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                spaceIdList, Lists.newArrayList(InfoCodeEnum.DEPLOYMENT_STATUS.value()));
        if (CollectionUtils.isEmpty(list)) {
            return spaceDeploymentStatusMap;
        }
        return list.stream().collect(
                Collectors.toMap(InfoCodeValueVO::getObjectId, InfoCodeValueVO::valueToDeploymentStatus));
    }

    /**
     * 空间安防异常事件叠加风控目标
     *
     * @param param 通风入参
     * @return 异常事件列表
     */
    @Override
    public List<ExceptionEventBaseInfoVO> getAbnormalDataList(SecurityModelViewParam param) {
        String projectId = ContextUtil.getProjectId();
        // 当前视角下的所有空间
        List<DtpRwdSpaceDTO> instanceRelatedAllSpaceList = getAllSpaceInPerspective(param, DtpRwdObjectDTO.Fields.id);
        if (CollectionUtils.isEmpty(instanceRelatedAllSpaceList)) {
            return new ArrayList<>();
        }
        List<String> spaceIdList = instanceRelatedAllSpaceList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        List<String> objectIdList = new ArrayList<>(spaceIdList);

        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        if (param.getInstanceType() != DemandInstanceTypeEnum.PROJECT) {
            dtpRwdObjectQueryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjTo(spaceIdList));
        }
        // 空间下所有的设备
        List<SecurityDtpRwdEquipmentDTO> secureAllEquipmentList = getSecureAllEquipmentList(projectId, false, dtpRwdObjectQueryParam);
        if (CollectionUtils.isNotEmpty(secureAllEquipmentList)) {
            objectIdList.addAll(secureAllEquipmentList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()));
        }
        // 设备或者空间的对象集合不为空则查询异常事件
        return this.abstractCommonServer.listAbstractOverlayRiskControlTargetConditions(
                objectIdList, null);
    }

    @Override
    public SecuritySpaceStatisticsVo getArmingSpaceStatistics(SecurityModelViewParam param) {

        SecuritySpaceStatisticsVo statistics = new SecuritySpaceStatisticsVo();
        List<SecuritySpaceStatusStatisticsVo> spaceStatusStatisticsVoList = statistics.initData().getSpaceList();
        Map<String, DeploymentStatusEnum> spaceIdToStatusMap = getArmingSpaceCollect(param);
        Map<DeploymentStatusEnum, List<String>> groupedByStatus = Maps.newHashMap();
        if (!spaceIdToStatusMap.isEmpty()) {
            groupedByStatus = spaceIdToStatusMap.entrySet().stream()
                    .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
        }
        if (!groupedByStatus.isEmpty()) {
            groupedByStatus.forEach((status, spaceIdList) -> {
                SecuritySpaceStatusStatisticsVo statisticsVo = new SecuritySpaceStatusStatisticsVo();
                statisticsVo.setCount(spaceIdList.size());
                spaceStatusStatisticsVoList.forEach(vo -> {
                    if (status.equals(vo.getSpaceStatus())) {
                        vo.setCount(spaceIdList.size());
                    }
                });
                statistics.setSum(statistics.getSum() + spaceIdList.size());
            });
        }
        return statistics;
    }

    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    public Map<String, AccessDemandStatusEnum> mapSpaceAccessStatePartialQuantity(@NonNull String projectId) {
        StopWatch stopWatch = StopWatch.create("查询安防模型图mapSpaceAccessStatePartialQuantity");
        stopWatch.start("joinAll");

        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        param.setClassCodeEnum(DOOR_EQUIPMENT_CLASS);
        param.filterMonitoringSurveillanceStatus();
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentList = dtpRwdClientService.listEquipment(param);

        if (CollectionUtils.isEmpty(dtpRwdEquipmentList)) {
            return new HashMap<>();
        }
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam()
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
        List<DtpRwdSpaceDTO> rwdSpaceList = new ArrayList<>();
        List<String> equipmentList = dtpRwdEquipmentList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(equipmentList)) {
            rwdSpaceList = dtpRwdClientService.listSpaceInByEquipmentId(equipmentList, dtpRwdObjectQueryParam);
        }
        if (CollectionUtils.isEmpty(rwdSpaceList)) {
            return new HashMap<>();
        }

        HashMap<String, AccessDemandStatusEnum> spaceAccessDemandMap = new HashMap<>(16);

        // 使用Map<Object, List<String>>进行分组，key为所属空间id，value为设备id，即当前空间下的所有门
        Map<String, Set<String>> mapAllEquipmentInSpaceBySpaceId = new HashMap<>(16);

        for (DtpRwdSpaceDTO obj : rwdSpaceList) {
            Set<String> relObjIds = obj.getRelObjIds();
            if (CollectionUtils.isEmpty(relObjIds)) {
                continue;
            }
            mapAllEquipmentInSpaceBySpaceId.put(obj.getId(), relObjIds);
        }

        String trafficInfoCode = InfoCodeEnum.ACCESS_DEMAND.value();
        List<String> infoCodeList = Lists.newArrayList(trafficInfoCode);

        // 查信息点规则
        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>(16);
        dtpRwdEquipmentList.forEach(equipment -> MapExecutor.dynamicPut(classCodeAndObjectIds, equipment.getClassCode(), equipment.getId(), HashSet::new));
        Map<String, Set<String>> ruleInfoCodeMap = this.abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(projectId, classCodeAndObjectIds, infoCodeList);

        Map<String, String> infoCodeValueByObjectIdMap = new HashMap<>();
        // 移除不满足条件的键值对
        ruleInfoCodeMap.entrySet().removeIf(entry -> entry.getValue().isEmpty() || entry.getValue().stream().noneMatch(s -> s.equals(trafficInfoCode)));

        Set<String> equipmentHasRuleIdList = ruleInfoCodeMap.keySet();
        if (MapUtils.isEmpty(ruleInfoCodeMap)) {
            infoCodeRuleTurnEnumPartialQuantity(mapAllEquipmentInSpaceBySpaceId, infoCodeValueByObjectIdMap, equipmentHasRuleIdList, spaceAccessDemandMap);
            return spaceAccessDemandMap;
        }

        // 查询信息点数据  规则过滤后未使用到
        List<String> equipmentIdList = dtpRwdEquipmentList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        List<InfoCodeValueVO> trafficStatusInfoList = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(equipmentIdList, infoCodeList);
        if (CollectionUtils.isNotEmpty(trafficStatusInfoList)) {
            infoCodeValueByObjectIdMap = trafficStatusInfoList.stream()
                    .collect(Collectors.toMap(InfoCodeValueVO::getObjectId, obj -> String.valueOf(obj.getInfoCodeValue())));
        }

        // 空间下的设备通行状态与信息点规则->转为空间的通行状态
        infoCodeRuleTurnEnumPartialQuantity(mapAllEquipmentInSpaceBySpaceId, infoCodeValueByObjectIdMap, equipmentHasRuleIdList, spaceAccessDemandMap);
        stopWatch.stop();

        logProjectInstance(projectId).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return spaceAccessDemandMap;
    }

    /**
     * 空间信息点相关规则转枚举
     *
     * @param mapAllEquipmentInSpaceBySpaceId 空间下的设备信息
     * @param infoCodeValueByObjectIdMap      信息点数值
     * @param spaceAccessDemandMap            空间通行状态
     */
    @SuppressWarnings({"DuplicatedCode"})
    public static void infoCodeRuleTurnEnumPartialQuantity(Map<String, Set<String>> mapAllEquipmentInSpaceBySpaceId,
                                                           Map<String, String> infoCodeValueByObjectIdMap,
                                                           Set<String> equipmentHasRuleIdList,
                                                           Map<String, AccessDemandStatusEnum> spaceAccessDemandMap) {
        mapAllEquipmentInSpaceBySpaceId.forEach((spaceId, equipmentList) -> {
            // 如果规则包含当前空间下的所有设备则为常开，常关，权限开启的判断逻辑
            // 否则为无法监测
            if (equipmentHasRuleIdList.containsAll(equipmentList)) {
                // 将map中的value转为集合
                Set<String> valuesSet = equipmentList.stream()
                        .filter(infoCodeValueByObjectIdMap::containsKey) // 确保数组中的键在map中存在
                        .map(infoCodeValueByObjectIdMap::get)            // 获取map中对应的value
                        .collect(Collectors.toSet()); // 收集到Set中
                AccessDemandStatusEnum accessDemandStatusEnum = getAccessDemandEnum(valuesSet);
                spaceAccessDemandMap.put(spaceId, accessDemandStatusEnum);
            }
        });
    }

    @Override
    public SecurityAccessSpaceStatisticsVo getAccessSpaceStatistics(SecurityModelViewParam param) {
        StopWatch stopWatch = StopWatch.create("通行空间统计");
        SecurityAccessSpaceStatisticsVo accessSpaceStatistics = new SecurityAccessSpaceStatisticsVo();
        List<SecurityAccessSpaceStatusStatisticsVo> accessStatusStatisticsVoList = accessSpaceStatistics.initData().getSpaceList();
        accessSpaceStatistics.setSpaceList(accessStatusStatisticsVoList);

        stopWatch.start("查询空间");
        String projectId = ContextUtil.getProjectId();
        stopWatch.stop();

        stopWatch.start("通行空间统计");
        Map<String, AccessDemandStatusEnum> spaceIdToAccessStatusMap;
        if (param.getInstanceType().equals(DemandInstanceTypeEnum.PROJECT)) {
            spaceIdToAccessStatusMap = mapSpaceAccessStatePartialQuantity(projectId);
        } else {
            List<String> spaceIds = getAllSpaceInPerspective(param, DtpRwdObjectDTO.Fields.id).stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(spaceIds)) {
                return accessSpaceStatistics;
            }
            spaceIdToAccessStatusMap = mapSpaceAccessState(projectId, spaceIds, null, null);
        }

        stopWatch.stop();
        if (spaceIdToAccessStatusMap.isEmpty()) {
            return accessSpaceStatistics;
        }
        Map<AccessDemandStatusEnum, List<String>> groupedByStatus = spaceIdToAccessStatusMap.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

        if (!groupedByStatus.isEmpty()) {
            groupedByStatus.forEach((status, ids) -> {
                accessStatusStatisticsVoList.forEach(vo -> {
                    if (status.equals(vo.getSpaceStatus())) {
                        vo.setCount(ids.size());
                    }
                });
                accessSpaceStatistics.setSum(accessSpaceStatistics.getSum() + ids.size());
            });
        }
        logProjectInstance(ContextUtil.getProjectId()).info("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return accessSpaceStatistics;
    }

    /**
     * 获取当前视角下的所有空间
     *
     * @param param 视角入参
     * @return 视角下的所有空间
     */
    public List<DtpRwdSpaceDTO> getAllSpaceInPerspective(SecurityModelViewParam param, @Nullable String... withColumns) {
        // 空间
        DemandInstanceTypeEnum instanceType = param.getInstanceType();
        String instanceId = param.getInstanceId();
        if (StringUtils.isBlank(instanceId)) {
            instanceId = ContextUtil.getProjectId();
        }
        Set<String> spaceIds = null;
        if (instanceType != DemandInstanceTypeEnum.PROJECT) {
            spaceIds = instanceService.listSpaceIdsByInstance(instanceType, instanceId);
            if (CollectionUtils.isEmpty(spaceIds)) {
                return Lists.newArrayList();
            }
        }
        // 模型图异常视角展示需要全量空间
        return dtpRwdClientService.listSpace(new DtpRwdObjectQueryParam().setId(spaceIds).addWithColumn(withColumns));
    }

    public List<String> filterObjectIdList(SecurityModelViewParam securityModelViewParam,
                                           List<String> deploymentStatusSpaceIdList) {
        DemandInstanceTypeEnum instanceType = securityModelViewParam.getInstanceType();
        String instanceId = securityModelViewParam.getInstanceId();
        String projectId = ContextUtil.getProjectId();

        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addWithColumn(DtpRwdObjectDTO.Fields.id);
        param.setId(deploymentStatusSpaceIdList);
        List<DtpRwdSpaceDTO> rwdSpaceList;
        switch (instanceType) {
            case BUILDING:
                rwdSpaceList = runningMonitorSecurityStrategy.querySpaceInBuilding(
                        projectId, Lists.newArrayList(instanceId), param);
                break;
            case FLOOR:
                rwdSpaceList = runningMonitorSecurityStrategy.querySpacesInFloor(
                        projectId, Lists.newArrayList(instanceId), param);
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(instanceType.getClass().getSimpleName(), instanceType);
        }
        return rwdSpaceList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
    }

    /**
     * 查询布放空间集合
     *
     * @param param 安防通用入参
     * @return 空间状态->撤布防状态
     */
    public Map<String, DeploymentStatusEnum> getArmingSpaceCollect(SecurityModelViewParam param) {
        List<String> deploymentStatusSpaceIdList = runningMonitorSecurityStrategy.listEvacuationDefenseSpaceByEquipment(ContextUtil.getProjectId());
        if (CollectionUtils.isEmpty(deploymentStatusSpaceIdList)) {
            return new HashMap<>();
        }
        List<String> perspectiveLogicIdList;
        if (param.getInstanceType().equals(DemandInstanceTypeEnum.PROJECT)) {
            perspectiveLogicIdList = deploymentStatusSpaceIdList;
        } else {
            perspectiveLogicIdList = filterObjectIdList(param, deploymentStatusSpaceIdList);
        }
        Map<String, DeploymentStatusEnum> deploymentStatusEnumMap = mapSpaceEvacuationDefenseStatus(perspectiveLogicIdList);
        perspectiveLogicIdList.forEach(spaceId -> {
            if (!deploymentStatusEnumMap.containsKey(spaceId)) {
                deploymentStatusEnumMap.put(spaceId, DeploymentStatusEnum.UNKNOWN);
            }
        });
        return deploymentStatusEnumMap;
    }

    @Override
    public Map<String, Set<String>> mapClaasCode2objectIds(String projectId, String instanceId, DemandInstanceTypeEnum instanceType) {
        HashMap<String, Set<String>> objectObjectHashMap = new HashMap<>();
        // 空间
        List<DtpRwdSpaceDTO> spaces = getAllSpaceInPerspective(new SecurityModelViewParam(instanceId, instanceType), DtpRwdObjectDTO.Fields.id);
        if (CollUtil.isEmpty(spaces)) {
            // id集合
            // 获取space列表中的id集合
            Set<String> spaceIds = spaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
            objectObjectHashMap.put(CommonConst.DtpRwdConst.GeneralZone, spaceIds);
        }

        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        // 设备
        List<SecurityDtpRwdEquipmentDTO> secureAllEquipmentList = getSecureAllEquipmentList(projectId, false, dtpRwdObjectQueryParam);
        // 根据classCode分组对象id列表
        Map<String, Set<String>> objectIdMap = secureAllEquipmentList.stream()
                .collect(Collectors.groupingBy(DtpRwdObjectDTO::getClassCode, Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));

        objectObjectHashMap.putAll(objectIdMap);
        return objectObjectHashMap;
    }

    @Override
    public List<SecurityEquipmentTypeStatisticsVo> getEquipmentTypeStatistics(SecurityModelViewParam param) {

        StopWatch stopWatch = StopWatch.create("queryDoorStatistics");

        stopWatch.start("查询门设备");
        Map<SecurityEquipmentStatisticsVO, List<DtpRwdEquipmentDTO>> doorStatisticsMap =
                getEquipmentStatisticsVOListMap(param);

        stopWatch.stop();

        stopWatch.start("信息点数据");

        // 全量设备id
        Set<String> equipmentIds = doorStatisticsMap.values().stream()
                .flatMap(List::stream)
                .map(DtpRwdEquipmentDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        CompletableFuture<Map<SecurityEquipmentStatusEnum, Set<String>>>
                infoCodeValueFuture = CompletableFuture.supplyAsync(
                () -> this.mapEquipmentStatuses(equipmentIds),
                BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture<Set<String>> exceptionEventFuture =
                CompletableFuture.supplyAsync(
                        () -> this.listExceptionEquipmentIds(equipmentIds),
                        BusinessThreadPoolExecutor.ttlExecutor());

        this.awaitStatisticsFutures(infoCodeValueFuture, exceptionEventFuture);
        Map<SecurityEquipmentStatusEnum, Set<String>> infoCodeEnumMap =
                new LinkedHashMap<>(infoCodeValueFuture.join());
        infoCodeEnumMap.put(SecurityEquipmentStatusEnum.EXCEPTION,
                exceptionEventFuture.join());


        stopWatch.stop();
        stopWatch.start("统计");

        HashMap<SecurityEquipmentTypeEnum, SecurityEquipmentTypeStatisticsVo> enumAndObjectMap = new LinkedHashMap<>();
        Lists.newArrayList(SecurityEquipmentTypeEnum.values()).forEach(equipmentTypeEnum -> {
            SecurityEquipmentTypeStatisticsVo equipmentTypeStatisticsVo = new SecurityEquipmentTypeStatisticsVo();
            equipmentTypeStatisticsVo.setObjectCode(equipmentTypeEnum);
            equipmentTypeStatisticsVo.setObjectName(equipmentTypeEnum.getReasonPhrase());
            enumAndObjectMap.put(equipmentTypeEnum, equipmentTypeStatisticsVo);
        });
        if (MapUtils.isNotEmpty(doorStatisticsMap)) {
            // 优化后的处理逻辑
            doorStatisticsMap.forEach((equipmentStatisticsVo, equips) -> {

                // 预计算设备ID集合（优化为不可变集合）
                final Set<String> equipIds = Collections.unmodifiableSet(
                        equips.stream()
                                .map(DtpRwdEquipmentDTO::getId)
                                .collect(Collectors.toSet())
                );

                // 统一处理公共状态
                handleEquipmentStatuses(equipIds, equipmentStatisticsVo,
                        infoCodeEnumMap,
                        SecurityEquipmentStatusEnum.EXCEPTION,
                        SecurityEquipmentStatusEnum.OFFLINE
                );

                // 根据设备类型分发处理逻辑
                final String objectCode = equipmentStatisticsVo.getObjectCode();
                if (DOOR_EQUIPMENT_CLASS_STRING.contains(objectCode)) {
                    handleDoorEquipment(equipIds, equipmentStatisticsVo, infoCodeEnumMap, enumAndObjectMap);
                } else if (DtpRwdClassCodeEnum.SPIACU.value().equals(objectCode)) {
                    handleBurglarAlarmEquipment(equipIds, equipmentStatisticsVo, infoCodeEnumMap, enumAndObjectMap);
                } else if (DtpRwdClassCodeEnum.WEGMCM.value().equals(objectCode)) {
                    handleCameraEquipment(equipIds, equipmentStatisticsVo, enumAndObjectMap);
                } else if (SPVSCM_CODE.equals(objectCode)) {
                    handlerCameraTypeAndHasArmingFunc(equips, infoCodeEnumMap, enumAndObjectMap);
                }
            });
        }

        stopWatch.stop();
        logProjectInstance(ContextUtil.getProjectId()).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return Lists.newArrayList(enumAndObjectMap.values());
    }

    /**
     * 并发查询设备信息
     *
     * @param param
     * @return
     */

    private Map<SecurityEquipmentStatisticsVO, List<DtpRwdEquipmentDTO>> getEquipmentStatisticsVOListMap(SecurityModelViewParam param) {
        if (this.cacheObjectModelStatusService.isReady(
                RunningMonitorTypeEnum.SECURITY)) {
            return this.mapEquipmentStatistics(
                    this.listStatisticsEquipmentFromCache(param));
        }

        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCode(Arrays.asList(
                        DtpRwdClassCodeEnum.CFCSDR.value(),
                        DtpRwdClassCodeEnum.CFCSFR.value(),
                        DtpRwdClassCodeEnum.SPEGFG.value(),
                        DtpRwdClassCodeEnum.SPIACU.value()
                )
        );
        queryParam.filterMonitoringSurveillanceStatus();
        queryParam.addWithColumn(HAS_ARMING_FUNC_CODE, CAMERA_TYPE_CODE);

        //摄像头的设备单独查询 需要过滤存在 实时流协议（realTimeStreamProtocol）静态信息点的数据
        DtpRwdObjectQueryParam cameraQueryParam = new DtpRwdObjectQueryParam();
        cameraQueryParam.setClassCode(Arrays.asList(
                        DtpRwdClassCodeEnum.SPVSCM.value(), DtpRwdClassCodeEnum.WEGMCM.value()
                )
        );
        cameraQueryParam.addWithColumn(HAS_ARMING_FUNC_CODE, CAMERA_TYPE_CODE,
                InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode(),InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value());

        CompletableFuture<Map<SecurityEquipmentStatisticsVO, List<DtpRwdEquipmentDTO>>>
                equipmentFuture = CompletableFuture.supplyAsync(
                () -> this.getEquipmentStatistics(
                        param.getInstanceId(), param.getInstanceType(), queryParam),
                BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture<Map<SecurityEquipmentStatisticsVO, List<DtpRwdEquipmentDTO>>>
                cameraFuture = CompletableFuture.supplyAsync(
                () -> this.getEquipmentStatistics(
                        param.getInstanceId(), param.getInstanceType(), cameraQueryParam),
                BusinessThreadPoolExecutor.ttlExecutor());
        this.awaitStatisticsFutures(equipmentFuture, cameraFuture);

        Map<SecurityEquipmentStatisticsVO, List<DtpRwdEquipmentDTO>> result =
                new LinkedHashMap<>(equipmentFuture.join());
        result.putAll(cameraFuture.join());
        return result;
    }

    /**
     * 从 SECURITY 设备 Key 读取统计所需静态候选。
     *
     * <p>缓存只承担静态范围和分类字段；楼层视角在缓存候选上本地收窄，空结果不穿透 RWD。</p>
     *
     * @param param 统计视角
     * @return 最终去重设备候选
     * @since v7.260802.0
     */
    private @NonNull List<DtpRwdEquipmentDTO> listStatisticsEquipmentFromCache(
            @NonNull SecurityModelViewParam param) {
        if (!DemandInstanceTypeEnum.PROJECT.equals(param.getInstanceType())
                && !DemandInstanceTypeEnum.FLOOR.equals(param.getInstanceType())) {
            return Collections.emptyList();
        }
        List<DtpRwdEquipmentDTO> cachedEquipments =
                this.cacheObjectRepository.listByCacheKey(
                        CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT,
                        DtpRwdEquipmentDTO.class,
                        SecurityServiceImpl.SECURITY_STATISTICS_CACHE_FIELDS);
        Map<String, DtpRwdEquipmentDTO> equipmentById = new LinkedHashMap<>();
        for (DtpRwdEquipmentDTO equipment : cachedEquipments) {
            if (!SecurityServiceImpl.SECURITY_STATISTICS_CLASS_CODES.contains(
                    equipment.getClassCode())) {
                continue;
            }
            if (DemandInstanceTypeEnum.FLOOR.equals(param.getInstanceType())
                    && !StringUtils.equals(
                    param.getInstanceId(), equipment.getFloorId())) {
                continue;
            }
            if (this.matchesSecurityStatisticsContract(equipment)) {
                equipmentById.putIfAbsent(equipment.getId(), equipment);
            }
        }
        return new ArrayList<>(equipmentById.values());
    }

    /**
     * 保持原设备统计的静态过滤和摄像头分类语义。
     *
     * @param equipment 缓存设备候选
     * @return 是否进入最终动态查询范围
     * @since v7.260802.0
     */
    private boolean matchesSecurityStatisticsContract(
            @NonNull DtpRwdEquipmentDTO equipment) {
        String classCode = equipment.getClassCode();
        if (DOOR_EQUIPMENT_CLASS_STRING.contains(classCode)) {
            return MONITORING_SURVEILLANCE_STATUS_LIST.contains(
                    getUnDefinedValue(
                            equipment, MONITORING_SURVEILLANCE_STATUS_CODE));
        }
        String streamProtocol = getUnDefinedValue(
                equipment, InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode());
        if (DtpRwdClassCodeEnum.WEGMCM.value().equals(classCode)) {
            return StringUtils.isNotEmpty(streamProtocol);
        }
        if (DtpRwdClassCodeEnum.SPVSCM.value().equals(classCode)) {
            boolean matchesCameraType = SPVSCM_CAMERA_TYPE.contains(
                    getUnDefinedValue(equipment, CAMERA_TYPE_CODE))
                    && StringUtils.isNotEmpty(streamProtocol);
            boolean matchesArmingFunction = HAS_ARMING_FUNC.equals(
                    getUnDefinedValue(equipment, HAS_ARMING_FUNC_CODE))
                    && MONITORING_SURVEILLANCE_STATUS_LIST.contains(
                    getUnDefinedValue(
                            equipment, MONITORING_SURVEILLANCE_STATUS_CODE));
            return matchesCameraType || matchesArmingFunction;
        }
        return true;
    }

    /**
     * 查询指定空间下指定的设备类统计
     *
     * @return key 是按照给定设备类统计，value 物理世界对象列表
     */
    public Map<SecurityEquipmentStatisticsVO, List<DtpRwdEquipmentDTO>> getEquipmentStatistics(
            @NonNull String instanceId,
            DemandInstanceTypeEnum instanceType,
            DtpRwdObjectQueryParam queryParam) {

        StopWatch stopWatch = StopWatch.create("getEquipmentStatistics");
        stopWatch.start("查询前的操作");
        if (Objects.isNull(queryParam)) {
            queryParam = new DtpRwdObjectQueryParam();
        }
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        stopWatch.stop();
        stopWatch.start("查寻rwd实体类信息");
        List<DtpRwdEquipmentDTO> equipmentDTOS = new ArrayList<>();
        if (instanceType.equals(DemandInstanceTypeEnum.PROJECT)) {
            equipmentDTOS = dtpRwdClientService.listEquipment(queryParam);
        }
        if (instanceType.equals(DemandInstanceTypeEnum.FLOOR)) {
            equipmentDTOS = dtpRwdClientService.listEquipmentInByFloorIds(Collections.singletonList(instanceId), queryParam);
        }
        //车库摄像头过滤掉没有实时流协议的数据
        equipmentDTOS.removeIf(dtpRwdEquipmentDTO -> {
            String realTimeStreamProtocol =
                    dtpRwdEquipmentDTO.analyzeProperties(InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.value());
            return StringUtils.equals(DtpRwdClassCodeEnum.WEGMCM.value(), dtpRwdEquipmentDTO.getClassCode()) && StringUtils.isEmpty(realTimeStreamProtocol);
        });

        stopWatch.stop();
        stopWatch.start("查询后数据处理");
        Map<SecurityEquipmentStatisticsVO, List<DtpRwdEquipmentDTO>>
                equipmentStatisticsMap = this.mapEquipmentStatistics(equipmentDTOS);
        stopWatch.stop();
        logProjectInstance(ContextUtil.getProjectId()).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return equipmentStatisticsMap;
    }

    /**
     * 按设备类生成统计分组，保持缓存和原 RWD 路径返回结构一致。
     *
     * @param equipments 设备列表
     * @return 设备类统计到设备列表
     * @since v7.260802.0
     */
    private @NonNull Map<SecurityEquipmentStatisticsVO, List<DtpRwdEquipmentDTO>>
            mapEquipmentStatistics(@NonNull List<DtpRwdEquipmentDTO> equipments) {
        if (CollectionUtils.isEmpty(equipments)) {
            return new LinkedHashMap<>();
        }
        return equipments.stream()
                .collect(Collectors.groupingBy(
                        DtpRwdEquipmentDTO::getClassCode,
                        LinkedHashMap::new,
                        Collectors.toList()))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> {
                            String classCode = entry.getKey();
                            return new SecurityEquipmentStatisticsVO(
                                    classCode,
                                    DtpRwdClassCodeEnum.analyze(classCode)
                                            .getReasonPhrase(),
                                    entry.getValue().size(),
                                    new ArrayList<>());
                        },
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new));
    }

    /**
     * 查询最终设备集合的固定四项动态状态并按业务状态分组。
     *
     * @param equipmentIds 最终去重设备 ID
     * @return 状态到设备 ID
     * @since v7.260802.0
     */
    private @NonNull Map<SecurityEquipmentStatusEnum, Set<String>>
            mapEquipmentStatuses(@NonNull Set<String> equipmentIds) {
        if (equipmentIds.isEmpty()) {
            return new LinkedHashMap<>();
        }
        List<InfoCodeValueVO> infoCodeValues =
                this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                        equipmentIds,
                        SecurityServiceImpl.SECURITY_STATISTICS_INFO_CODES);
        return infoCodeValues.stream()
                .filter(vo -> Objects.nonNull(vo.getInfoCodeValue()))
                .collect(Collectors.groupingBy(
                        vo -> this.mapToStatusEnum(
                                vo.getInfoCode(), vo.getInfoCodeValue()),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                InfoCodeValueVO::getObjectId,
                                Collectors.toCollection(LinkedHashSet::new))));
    }

    /**
     * 查询最终设备集合关联的异常事件设备 ID。
     *
     * @param equipmentIds 最终去重设备 ID
     * @return 存在异常事件的设备 ID
     * @since v7.260802.0
     */
    private @NonNull Set<String> listExceptionEquipmentIds(
            @NonNull Set<String> equipmentIds) {
        if (equipmentIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return this.abstractCommonServer
                .listAbstractOverlayRiskControlTargetConditions(
                        equipmentIds, null).stream()
                .map(ExceptionEventBaseInfoVO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 等待设备统计异步查询，并将业务运行时异常按原异常类型抛出。
     *
     * @param futures 异步查询
     * @since v7.260802.0
     */
    private void awaitStatisticsFutures(
            @NonNull CompletableFuture<?>... futures) {
        try {
            CompletableFuture.allOf(futures).join();
        } catch (CompletionException exception) {
            if (exception.getCause() instanceof RuntimeException) {
                throw (RuntimeException) exception.getCause();
            }
            throw exception;
        }
    }

    /**
     * 处理设备状态信息，并更新统计视图对象
     *
     * @param equipIds        设备ID集合
     * @param statisticsVo    安防设备统计视图对象，用于存储统计结果
     * @param infoCodeEnumMap 状态枚举与对应设备ID集合的映射关系
     * @param statuses        需要处理的状态枚举数组（可变参数）
     */
    private void handleEquipmentStatuses(Set<String> equipIds,
                                         SecurityEquipmentStatisticsVO statisticsVo,
                                         Map<SecurityEquipmentStatusEnum, Set<String>> infoCodeEnumMap,
                                         SecurityEquipmentStatusEnum... statuses) {
        Arrays.stream(statuses).forEach(status ->
                handleNonCameraEquipment(status, equipIds, statisticsVo, infoCodeEnumMap)
        );
    }

    /**
     * 处理门禁设备相关的统计信息
     *
     * @param equipIds         设备ID集合
     * @param statisticsVo     安全设备统计VO对象，用于存储统计结果
     * @param infoCodeEnumMap  设备状态枚举与对应设备ID集合的映射
     * @param enumAndObjectMap 设备类型枚举与对应统计VO对象的映射
     */
    private void handleDoorEquipment(Set<String> equipIds,
                                     SecurityEquipmentStatisticsVO statisticsVo,
                                     Map<SecurityEquipmentStatusEnum, Set<String>> infoCodeEnumMap,
                                     HashMap<SecurityEquipmentTypeEnum, SecurityEquipmentTypeStatisticsVo> enumAndObjectMap) {
        // 处理门禁设备的状态统计（开启和关闭状态）
        handleEquipmentStatuses(equipIds, statisticsVo, infoCodeEnumMap,
                SecurityEquipmentStatusEnum.OPEN,
                SecurityEquipmentStatusEnum.CLOSE
        );

        // 更新门禁设备类型的统计信息
        updateTypeStatistics(
                enumAndObjectMap.get(SecurityEquipmentTypeEnum.DOOR), // 获取门禁设备类型的统计VO
                equipIds.size(), // 传入设备数量
                statisticsVo // 传入统计结果VO
        );
    }

    /**
     * 入侵报警设备相关的统计信息
     *
     * @param equipIds         设备ID集合
     * @param statisticsVo     安全设备统计VO对象，用于存储统计结果
     * @param infoCodeEnumMap  设备状态枚举与对应设备ID集合的映射
     * @param enumAndObjectMap 设备类型枚举与对应统计VO对象的映射
     */
    private void handleBurglarAlarmEquipment(Set<String> equipIds,
                                             SecurityEquipmentStatisticsVO statisticsVo,
                                             Map<SecurityEquipmentStatusEnum, Set<String>> infoCodeEnumMap,
                                             HashMap<SecurityEquipmentTypeEnum, SecurityEquipmentTypeStatisticsVo> enumAndObjectMap) {
        handleEquipmentStatuses(equipIds, statisticsVo, infoCodeEnumMap,
                SecurityEquipmentStatusEnum.DISAMAZING,
                SecurityEquipmentStatusEnum.AMAZING
        );

        updateTypeStatistics(
                enumAndObjectMap.get(SecurityEquipmentTypeEnum.BURGLAR_ALARM_DEVICE),
                equipIds.size(),
                statisticsVo
        );
    }

    /**
     * 摄像头设备的统计信息
     *
     * @param equipIds         设备ID集合
     * @param statisticsVo     安全设备统计VO对象，用于存储统计结果
     * @param enumAndObjectMap 设备类型枚举与对应统计VO对象的映射
     */
    private void handleCameraEquipment(Set<String> equipIds,
                                       SecurityEquipmentStatisticsVO statisticsVo,
                                       HashMap<SecurityEquipmentTypeEnum, SecurityEquipmentTypeStatisticsVo> enumAndObjectMap) {
        updateTypeStatistics(enumAndObjectMap.get(SecurityEquipmentTypeEnum.CAMERA), equipIds.size(), statisticsVo);
    }

    /**
     * 统一统计更新方法
     *
     * @param statistics   设备大类对象
     * @param count        设备个数
     * @param statisticsVo 状态vo
     */
    private void updateTypeStatistics(SecurityEquipmentTypeStatisticsVo statistics,
                                      int count,
                                      SecurityEquipmentStatisticsVO statisticsVo) {
        statistics.setCount(statistics.getCount() + count);
        statistics.getList().add(statisticsVo);
    }

    /**
     * 处理非摄像头设备的状态统计
     *
     * @param status                设备状态枚举值
     * @param equipIds              当前需要统计的设备ID集合
     * @param equipmentStatisticsVo 设备统计结果VO对象
     * @param infoCodeEnumMap       状态码映射表（Key为状态枚举，Value为该状态对应的设备ID集合）
     */
    private void handleNonCameraEquipment(SecurityEquipmentStatusEnum status,
                                          Set<String> equipIds,
                                          SecurityEquipmentStatisticsVO equipmentStatisticsVo,
                                          Map<SecurityEquipmentStatusEnum, Set<String>> infoCodeEnumMap) {
        // 获取该状态对应的设备ID集合（不存在则返回空集合）
        Set<String> statusCodes = infoCodeEnumMap.getOrDefault(status, Collections.emptySet());

        // 计算当前设备中处于该状态的数量
        long count = equipIds.stream().filter(statusCodes::contains).count();

        // 构造统计VO对象（排除数量为0的离线状态）
        Optional.ofNullable(
                (!status.equals(SecurityEquipmentStatusEnum.OFFLINE) || count > 0)
                        ? new SecurityStatusStatisticsVo(status, status.getReasonPhrase(), (int) count)
                        : null
        ).ifPresent(vo -> equipmentStatisticsVo.getStatusList().add(vo));
    }

    /**
     * 根据信息码(infoCode)和对应的值(infoCodeValue)映射到安全设备状态枚举
     *
     * @param infoCode      信息码，用于区分不同的状态类型（如布防状态、运行状态、在线状态等）
     * @param infoCodeValue 信息码对应的值，通常为字符串形式的"1"或其他值
     * @return SecurityEquipmentStatusEnum 返回映射后的枚举值，若未匹配则返回null
     */
    private SecurityEquipmentStatusEnum mapToStatusEnum(String infoCode, Object infoCodeValue) {
        // 将infoCodeValue转换为字符串形式以便比较
        String value = String.valueOf(infoCodeValue);

        // 根据不同的infoCode进行状态映射
        switch (infoCode) {
            // 布防状态/部署撤防报警状态："1"表示布防(AMAZING)，"0"表示撤防(DISAMAZING)
            case "armingStatus":
            case "deployWithdrawAlarmStatus":
                return "1".equals(value) ? SecurityEquipmentStatusEnum.AMAZING : SecurityEquipmentStatusEnum.DISAMAZING;

            // 运行状态："1"表示开启(OPEN)，"0"表示关闭(CLOSE)
            case "runStatus":
                return "1".equals(value) ? SecurityEquipmentStatusEnum.OPEN : SecurityEquipmentStatusEnum.CLOSE;

            // 在线状态："1"表示在线(ONLINE)，"0"表示离线(OFFLINE)
            case "onlineStatus":
                return "1".equals(value) ? SecurityEquipmentStatusEnum.ONLINE : SecurityEquipmentStatusEnum.OFFLINE;
        }

        // 未匹配到任何信息码时返回null
        return null;
    }


    /**
     * 处理摄像头类型和布防功能相关的设备统计信息
     *
     * @param equips           <枚举设备分类,设备信息>
     * @param infoCodeEnumMap  设备状态枚举与对应设备ID集合的映射
     * @param enumAndObjectMap 设备类型枚举与统计VO对象的映射
     */
    public void handlerCameraTypeAndHasArmingFunc(List<DtpRwdEquipmentDTO> equips,
                                                  Map<SecurityEquipmentStatusEnum, Set<String>> infoCodeEnumMap,
                                                  HashMap<SecurityEquipmentTypeEnum, SecurityEquipmentTypeStatisticsVo> enumAndObjectMap) {

        // 定义摄像头类型与枚举的映射关系（可扩展）
        // 三元组结构：<设备属性值, 设备属性代码, 对应的摄像头类型枚举>
        List<Triple<String, String, SecurityCameraTypeEnum>> cameraTypeConfigs = Arrays.asList(
                Triple.of("1", CAMERA_TYPE_CODE, SecurityCameraTypeEnum.SPVSCM_CAMERATYPE_1),
                Triple.of("2", CAMERA_TYPE_CODE, SecurityCameraTypeEnum.SPVSCM_CAMERATYPE_2),
                Triple.of("3", CAMERA_TYPE_CODE, SecurityCameraTypeEnum.SPVSCM_CAMERATYPE_3)
        );

        // 定义布防功能与枚举的映射关系（可扩展）
        List<Triple<String, String, SecurityCameraTypeEnum>> hasArmingFuncConfig = Collections.singletonList(
                Triple.of(HAS_ARMING_FUNC, HAS_ARMING_FUNC_CODE, SecurityCameraTypeEnum.SPVSCM_HASARMINGFUNC_1)
        );

        // 将设备按照配置规则分组  结果映射：<配置规则三元组, 匹配该规则的设备ID集合>
        Map<Triple<String, String, SecurityCameraTypeEnum>, Set<String>> grouped =
                Stream.of(cameraTypeConfigs, hasArmingFuncConfig)
                        .flatMap(configs -> equips.parallelStream()
                                .collect(Collectors.groupingBy(
                                        equipment -> findMatchingConfig(configs, equipment), // 根据配置规则匹配设备
                                        Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet()) // 收集匹配设备的ID
                                )).entrySet().stream())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (existing, replacement) -> { // 合并相同key的value集合
                                    existing.addAll(replacement);
                                    return existing;
                                }
                        ));

        // 处理分组后的结果
        grouped.forEach((config, equipIds) -> {
            // 只处理有效分组（配置不为空且设备ID集合不为空）
            if (isValidGroup(config, equipIds)) {
                // 获取当前分组的摄像头类型枚举
                final SecurityCameraTypeEnum type = config.getRight();
                // 获取当前分组的设备数量
                final int count = equipIds.size();

                // 创建统计VO对象
                SecurityEquipmentStatisticsVO statisticsVo = createStatisticsVo(type, count);
                // 处理设备状态信息
                processEquipmentStatuses(equipIds, statisticsVo, infoCodeEnumMap);

                // 设备大类
                SecurityEquipmentTypeEnum equipmentType = determineEquipmentType(type);
                // 更新类型统计信息
                updateTypeStatistics(equipmentType, count, statisticsVo, enumAndObjectMap);
            }
        });
    }

    /**
     * 从配置列表中查找与设备匹配的配置项
     *
     * @param configs   配置列表，每个配置项是一个三元组，包含：
     *                  - 左值：匹配值
     *                  - 中值：设备属性名
     *                  - 右值：摄像头类型枚举
     * @param equipment 设备DTO对象
     * @return 匹配到的配置项三元组，若未找到则返回默认值DEFAULT_KEY
     */
    private Triple<String, String, SecurityCameraTypeEnum> findMatchingConfig(
            List<Triple<String, String, SecurityCameraTypeEnum>> configs,
            DtpRwdObjectDTO equipment) {
        for (Triple<String, String, SecurityCameraTypeEnum> config : configs) {
            String expectedValue = config.getLeft();
            String actualValue = getUnDefinedValue(equipment, config.getMiddle());

            //如果SPVSCM是 SPVSCM_HASARMINGFUNC_1 枚举需要过滤realTimeStreamProtocol!=null
            // 其它的需要过滤monitoringSurveillanceStatus=2/3
            if (expectedValue.equals(actualValue)) {
                if (config.getRight() == SecurityCameraTypeEnum.SPVSCM_HASARMINGFUNC_1 ){
                    if("2".equals(getUnDefinedValue(equipment, InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.getCode()))  ||
                            "3".equals(getUnDefinedValue(equipment, InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.getCode()))
                    ){
                        return config;
                    }
                }else {
                    if (StringUtils.isNotEmpty(getUnDefinedValue(equipment,
                            InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode()))) {
                        return config;
                    }
                }
            }
        }
        return DEFAULT_KEY;
    }

    /**
     * 校验安全摄像头配置分组是否有效
     *
     * @param config   三元组配置对象，包含摄像头分组信息。其中第三个元素为摄像头类型枚举
     * @param equipIds 设备ID集合，表示该分组下的设备集合
     * @return boolean 返回分组是否有效：
     * 1. 配置对象非空
     * 2. 设备集合非空
     * 3. 摄像头类型不是UNKNOWN
     */
    private boolean isValidGroup(Triple<?, ?, SecurityCameraTypeEnum> config, Set<String> equipIds) {
        return config != null
                && !equipIds.isEmpty()
                && config.getRight() != SecurityCameraTypeEnum.UNKNOWN;
    }

    /**
     * 创建安全设备统计值对象
     *
     * @param type  安全摄像头类型枚举，用于获取设备类型值和描述
     * @param count 该类型设备的数量统计
     * @return SecurityEquipmentStatisticsVo 包含设备类型信息、数量统计和空列表的统计值对象
     */
    private SecurityEquipmentStatisticsVO createStatisticsVo(SecurityCameraTypeEnum type, int count) {
        return new SecurityEquipmentStatisticsVO(type.value(), type.getReasonPhrase(), count, new ArrayList<>());
    }

    /**
     * 处理设备状态信息，统计不同状态下的设备数量
     *
     * @param equipIds    设备ID集合
     * @param vo          安防设备统计VO对象，用于存储统计结果
     * @param infoCodeMap 设备状态与对应设备ID集合的映射关系
     */
    private void processEquipmentStatuses(Set<String> equipIds,
                                          SecurityEquipmentStatisticsVO vo,
                                          Map<SecurityEquipmentStatusEnum, Set<String>> infoCodeMap) {
        // 遍历四种设备状态：异常、离线、布防、撤防
        Stream.of(
                        SecurityEquipmentStatusEnum.EXCEPTION,
                        SecurityEquipmentStatusEnum.OFFLINE,
                        SecurityEquipmentStatusEnum.DISAMAZING,
                        SecurityEquipmentStatusEnum.AMAZING
                )
                .forEach(status -> handleNonCameraEquipment(status, equipIds, vo, infoCodeMap));
    }

    /**
     * 根据摄像头类型确定对应的安全设备类型
     *
     * @param type 摄像头类型枚举值，参考SecurityCameraTypeEnum
     * @return SecurityEquipmentTypeEnum 返回对应的安全设备类型枚举值：
     * - 如果是带布防功能的摄像头(SPVSCM_HASARMINGFUNC_1)，返回入侵防盗设备(BURGLAR_ALARM_DEVICE)
     * - 其他所有摄像头类型都返回摄像头类型(CAMERA)
     */
    private SecurityEquipmentTypeEnum determineEquipmentType(SecurityCameraTypeEnum type) {
        return type == SecurityCameraTypeEnum.SPVSCM_HASARMINGFUNC_1
                ? SecurityEquipmentTypeEnum.BURGLAR_ALARM_DEVICE
                : SecurityEquipmentTypeEnum.CAMERA;
    }

    /**
     * 更新安全设备类型统计信息
     *
     * @param type             安全设备类型枚举，标识要更新的设备类型
     * @param count            要增加的设备数量
     * @param vo               安全设备统计值对象，包含设备详细信息
     * @param enumAndObjectMap 设备类型与统计值对象的映射Map
     *                         Key: 设备类型枚举
     *                         Value: 该类型对应的统计信息对象
     */
    private void updateTypeStatistics(SecurityEquipmentTypeEnum type,
                                      int count,
                                      SecurityEquipmentStatisticsVO vo,
                                      HashMap<SecurityEquipmentTypeEnum, SecurityEquipmentTypeStatisticsVo> enumAndObjectMap) {
        // 从映射Map中获取指定设备类型的统计对象
        SecurityEquipmentTypeStatisticsVo statistics = enumAndObjectMap.get(type);

        // 更新该类型设备的总数（累加新增数量）
        statistics.setCount(statistics.getCount() + count);

        // 将当前设备VO添加到该类型的设备列表中
        statistics.getList().add(vo);
    }

    /**
     * 参数转换
     *
     * @param dto 数据集
     * @param key 需要获取的未定义的缓存key
     * @return 数值
     */
    public static String getUnDefinedValue(DtpRwdObjectDTO dto, String key) {
        Map<String, Object> resultMap = dto.getUndefinedProperties();
        if (MapUtils.isEmpty(resultMap)) {
            return "";
        }
        if (resultMap.containsKey(key)) {
            return String.valueOf(resultMap.get(key));
        }
        return "";
    }

    @Override
    public SecurityFunctionVO querySecurityFunction() {
        boolean cacheReady = this.cacheObjectModelStatusService.isReady(
                RunningMonitorTypeEnum.SECURITY);
        List<SecurityDtpRwdEquipmentDTO> securityEquipments;
        if (cacheReady) {
            securityEquipments = this.listSecurityFunctionEquipmentFromCache();
        } else {
            securityEquipments = this.listSecurityFunctionEquipmentFromRwd();
        }

        boolean accessDemandFunction = securityEquipments.stream()
                .anyMatch(equipment -> DOOR_EQUIPMENT_CLASS_STRING.contains(
                        equipment.getClassCode()));
        boolean hasAlarmController = securityEquipments.stream()
                .anyMatch(equipment -> DtpRwdClassCodeEnum.SPIACU.value().equals(
                        equipment.getClassCode()));
        boolean hasArmingFunc = securityEquipments.stream()
                .anyMatch(equipment -> HAS_ARMING_FUNC.equals(
                        equipment.getHasArmingFunc()));
        boolean hasSurveillanceCamera = securityEquipments.stream()
                .anyMatch(equipment -> SPVSCM_CAMERA_TYPE.contains(
                        equipment.getCameraType()));

        SecurityFunctionVO securityFunctionVO = new SecurityFunctionVO();
        securityFunctionVO.setAccessDemandFunction(accessDemandFunction);
        securityFunctionVO.setDeploymentStateFunction(hasAlarmController || hasArmingFunc);
        securityFunctionVO.setOverallFunction(
                accessDemandFunction || hasSurveillanceCamera || hasAlarmController);
        return securityFunctionVO;
    }

    /**
     * 使用原有两次 RWD 查询组装安防功能判断所需设备。
     *
     * @return 摄像头、入侵报警控制器和满足监控状态要求的门
     * @since v7.260802.0
     */
    private @NonNull List<SecurityDtpRwdEquipmentDTO> listSecurityFunctionEquipmentFromRwd() {
        String projectId = ContextUtil.getProjectId();

        // 查询安防所有入侵报警防区控制器和监控摄像头
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode, HAS_ARMING_FUNC_CODE, CAMERA_TYPE_CODE);
        dtpRwdObjectQueryParam.setClassCode(Lists.newArrayList(DtpRwdClassCodeEnum.SPVSCM.value(), DtpRwdClassCodeEnum.SPIACU.value()));
        List<SecurityDtpRwdEquipmentDTO> listSurveillanceCameraAndAlarmController = dtpRwdClientService.listSecurityEquipment(
                false, projectId, dtpRwdObjectQueryParam);

        // 查询安防所有可监可控的门
        dtpRwdObjectQueryParam.setClassCodeEnum(DOOR_EQUIPMENT_CLASS);
        dtpRwdObjectQueryParam.filterMonitoringSurveillanceStatus();
        List<SecurityDtpRwdEquipmentDTO> listDoor = dtpRwdClientService.listSecurityEquipment(
                false, projectId, dtpRwdObjectQueryParam);
        List<SecurityDtpRwdEquipmentDTO> result = new ArrayList<>(
                listSurveillanceCameraAndAlarmController.size() + listDoor.size());
        result.addAll(listSurveillanceCameraAndAlarmController);
        result.addAll(listDoor);
        return result;
    }


    @Override
    public String logTag() {
        return "空间安防";
    }
}
