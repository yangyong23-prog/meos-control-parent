package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSpaceRoomFuncTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.model.param.airterminal.AirTerminalRunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 空调末端策略实现
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Component(RunningMonitorAirTerminalStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
public class RunningMonitorAirTerminalStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorAirTerminalStrategy";

    /**
     * 末端白名单空间未指定查询字段时需要返回的完整缓存字段。
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<String> CACHE_SPACE_FIELDS = ImmutableSet.of(
            DtpRwdObjectDTO.Fields.id,
            DtpRwdObjectDTO.Fields.projectId,
            DtpRwdObjectDTO.Fields.localName,
            DtpRwdObjectDTO.Fields.classCode,
            DtpRwdObjectDTO.Fields.objType,
            DtpRwdSpaceDTO.Fields.buildingId,
            DtpRwdSpaceDTO.Fields.floorId,
            DtpRwdSpaceDTO.Fields.roomFuncType,
            InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value());

    private final DtpRwdClientService dtpRwdClientService;

    private final RedisCacheTemplate redisCacheTemplate;

    /**
     * 缓存对象模型共享 READY 状态服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService
            cacheObjectModelStatusService;

    /**
     * 新版缓存键关联仓储，未 READY 时只提供白名单配置对象 ID。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRepository cacheObjectRepository;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.AIR_TERMINAL;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return InheritConfigTypeEnum.OBJECT;
    }

    /**
     * 查询末端白名单空间。
     *
     * <p>该入口的空间范围固定由新版白名单 Key 决定，不接收调用方范围开关。
     * {@code meosCache} 只控制 READY 时读取新版对象表，或按 Key 中的对象 ID 实时查询 RWD。</p>
     *
     * @param param 空间查询参数
     * @return 白名单范围内的空间
     * @since v7.260701.0
     */
    public @NonNull List<DtpRwdSpaceDTO> listWhitelistSpaces(
            @NonNull RunningMonitorSpaceQueryParam param) {
        String whitelistKey = CacheObjectModelKeyConfig.AIR_TERMINAL_WHITELIST_SPACE;
        if (param.isMeosCache()
                && this.cacheObjectModelStatusService.isReady(
                        RunningMonitorTypeEnum.AIR_TERMINAL)) {
            // READY 后只读取白名单键；实际未命中的对象不回退项目全量键或 RWD。
            Collection<String> requestedFields = CollectionUtils.isEmpty(param.getWithColumns())
                    ? RunningMonitorAirTerminalStrategy.CACHE_SPACE_FIELDS
                    : param.getWithColumns();
            return this.cacheObjectRepository.listByCacheKey(
                    whitelistKey, DtpRwdSpaceDTO.class, requestedFields);
        }
        List<String> spaceIds = this.cacheObjectRepository
                .listObjectIdsByCacheKey(whitelistKey);
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Collections.emptyList();
        }
        // 未 READY 时新版 Key 只限定配置范围，对象业务字段始终从 RWD 实时取得。
        return param.queryObject(new DtpRwdObjectQueryParam().setId(spaceIds));
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        return this.listSpacesInternal(param, null);
    }

    /**
     * 查询末端空间，并复用调用方已加载的设备 ID 查询设备服务空间。
     *
     * <p>设备服务空间仍与冷源空间、需求空间并发查询；传入空设备 ID 时该分支直接返回空，
     * 不重新查询末端设备，也不向 RWD 传入会扩大为项目全量的空关系条件。</p>
     *
     * @param param                 空间查询参数
     * @param preloadedEquipmentIds 调用方已加载的设备 ID
     * @return 去重后的末端空间
     * @since v7.260701.0
     */
    public @NonNull List<DtpRwdSpaceDTO> listSpaces(
            @NonNull RunningMonitorSpaceQueryParam param,
            @NonNull Collection<String> preloadedEquipmentIds) {
        return this.listSpacesInternal(param, preloadedEquipmentIds);
    }

    /**
     * 并行查询冷源、需求和（有设备 ID 时）设备服务空间，并区分历史入口自行查询设备与
     * 调用方显式预加载设备两种语义。
     *
     * @param param                 空间查询参数
     * @param preloadedEquipmentIds 调用方预加载的设备 ID；{@code null} 表示按历史逻辑自行查询设备
     * @return 去重后的末端空间
     * @since v7.260701.0
     */
    private @NonNull List<DtpRwdSpaceDTO> listSpacesInternal(
            @NonNull RunningMonitorSpaceQueryParam param,
            @Nullable Collection<String> preloadedEquipmentIds) {
        List<DtpRwdSpaceDTO> spaces = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.add(this.runRwdQuery(
                "listColdSpaces",
                () -> spaces.addAll(this.listColdSpaces(param))));
        futures.add(this.runRwdQuery(
                "listDemandSpace",
                () -> spaces.addAll(this.listDemandSpace(param))));
        // null 表示历史入口未预加载设备 ID，保留由 listEquipmentSpaces(param) 内部查询设备的逻辑。
        // 非空集合表示调用方已完成设备查询，直接使用已加载的设备 ID；空集合表示已确认没有设备，
        // 不创建第三个任务，避免重复查询或把空关系条件传给 RWD 后扩大范围。
        if (preloadedEquipmentIds == null) {
            futures.add(this.runRwdQuery(
                    "listEquipmentSpaces",
                    () -> spaces.addAll(this.listEquipmentSpaces(param))));
        } else if (CollectionUtils.isNotEmpty(preloadedEquipmentIds)) {
            futures.add(this.runRwdQuery(
                    "listEquipmentSpaces",
                    () -> spaces.addAll(this.listEquipmentSpaces(
                            param, preloadedEquipmentIds))));
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            this.logProjectInstance().error("Task timeout", e);
            // 可以考虑取消未完成的任务
            futures.forEach(f -> f.cancel(true));
        } catch (InterruptedException e) {
            this.logProjectInstance().error("Task interrupted", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            this.logProjectInstance().error("Task failed", e);
        }

        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = this.filterInvalidDemandSpace(spaces, DemandTemplateTypeEnum.AIRCONDITIONING);
        Map<String, DtpRwdSpaceDTO> spaceMap = dtpRwdSpaceDTOS.stream().collect(Collectors.toMap(DtpRwdSpaceDTO::getId, Function.identity(), (o1, o2) -> o1));
        return new ArrayList<>(spaceMap.values());
    }

    /**
     * 异步执行一条 RWD 查询，失败时沿用历史行为记录日志并返回其余分支结果。
     *
     * @param operation 操作名称
     * @param query 查询任务
     * @return 查询 Future
     * @since v7.260701.0
     */
    private @NonNull CompletableFuture<Void> runRwdQuery(
            @NonNull String operation,
            @NonNull Runnable query) {
        return CompletableFuture.runAsync(
                query, BusinessThreadPoolExecutor.ttlExecutor()).exceptionally(exception -> {
            this.logProjectInstance().error(operation, exception);
            return null;
        });
    }

    /**
     * @param param
     * @return java.util.List<com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO>
     * @author wxf
     * @description 获取设备所在和服务空间
     * @date 15:29 2025/6/27
     **/
    public List<DtpRwdSpaceDTO> listEquipmentSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        // 查询指定设备类下所有对象ID
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        Set<String> equipmentIds = this.listEquipments(equipmentQueryParam).stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toSet());
        return this.listEquipmentSpaces(param, equipmentIds);
    }

    /**
     * 根据调用方已加载的设备 ID 查询设备服务空间。
     *
     * <p>该入口不再查询末端设备。显式空设备 ID 表示本次没有设备服务空间，必须直接返回空；
     * RWD 的空设备条件表示查询项目全部空间，不能继续下传。</p>
     *
     * @param param                 空间查询参数
     * @param preloadedEquipmentIds 调用方已加载的设备 ID
     * @return 设备服务空间；设备 ID 为空时返回空列表
     * @since v7.260701.0
     */
    private @NonNull List<DtpRwdSpaceDTO> listEquipmentSpaces(
            @NonNull RunningMonitorSpaceQueryParam param,
            @NonNull Collection<String> preloadedEquipmentIds) {
        if (CollectionUtils.isEmpty(preloadedEquipmentIds)) {
            return Collections.emptyList();
        }

        Set<String> withColumns = param.getWithColumns();

         /*不要设备所在空间
        // 设备服务空间
        CompletableFuture<List<DtpRwdSpaceDTO>> serviceSpacesFuture = CompletableFuture.supplyAsync(() -> {
            DtpRwdObjectQueryParam serviceSpaceQueryParam = new DtpRwdObjectQueryParam();
            serviceSpaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            if (CollectionUtils.isEmpty(withColumns)) {
                serviceSpaceQueryParam.addWithColumn(DtpRwdSpaceDTO.Fields.class);
            }
            return param.queryObject(serviceSpaceQueryParam, (queryParam) ->
                    this.dtpRwdClientService.listSpaceServiceByEquipmentId(equipmentIds, queryParam));
        }, BusinessThreadPoolExecutor.ttlExecutor());
        // 设备所在空间
        CompletableFuture<List<DtpRwdSpaceDTO>> inSpacesFuture = CompletableFuture.supplyAsync(() -> {
            DtpRwdObjectQueryParam inSpaceQueryParam = new DtpRwdObjectQueryParam();
            inSpaceQueryParam.addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType, DtpRwdCriteriaSignEnum.LIKE_RIGHT, "SFT-02.04.");
            inSpaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            if (CollectionUtils.isEmpty(withColumns)) {
                inSpaceQueryParam.addWithColumn(DtpRwdSpaceDTO.Fields.class);
            }
            return param.queryObject(inSpaceQueryParam, (queryParam) ->
                    this.dtpRwdClientService.listSpaceInByEquipmentId(equipmentIds, queryParam));
        }, BusinessThreadPoolExecutor.ttlExecutor());

        List<DtpRwdSpaceDTO> serviceSpaces = serviceSpacesFuture.join();
        List<DtpRwdSpaceDTO> inSpaces = inSpacesFuture.join();

        // 根据所在关系查询出空间时进行数据合并
        if (CollectionUtils.isNotEmpty(inSpaces)) {
            Set<String> inSpaceIds = inSpaces.stream()
                    .map(DtpRwdObjectDTO::getId)
                    .collect(Collectors.toSet());

            serviceSpaces = serviceSpaces.stream()
                    .filter(item -> !inSpaceIds.contains(item.getId()))
                    .collect(Collectors.toList());
            serviceSpaces.addAll(inSpaces);
        }

         */


        DtpRwdObjectQueryParam serviceSpaceQueryParam = new DtpRwdObjectQueryParam();
        serviceSpaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        if (CollectionUtils.isEmpty(withColumns)) {
            serviceSpaceQueryParam.addWithColumn(DtpRwdSpaceDTO.Fields.class);
        }
        return param.queryObject(serviceSpaceQueryParam, queryParam ->
                this.dtpRwdClientService.listSpaceServiceByEquipmentId(
                        preloadedEquipmentIds, queryParam));
        // return filterInvalidDemandSpace(serviceSpaces, DemandTemplateTypeEnum.AIRCONDITIONING);
    }

    /**
     * 查询需求管理的空间
     * 默认查询
     *
     * @param param 查询参数
     * @return 需求管理的空间
     */
    public List<DtpRwdSpaceDTO> listDemandSpace(RunningMonitorSpaceQueryParam param) {
        if (param instanceof AirTerminalRunningMonitorSpaceQueryParam) {
            AirTerminalRunningMonitorSpaceQueryParam queryParam = (AirTerminalRunningMonitorSpaceQueryParam) param;
            // 不查询直接返回
            if (!queryParam.isQueryAirConditionDemandSpace()) {
                return null;
            }
        }
        DemandSpacesQueryParam spaceQueryParam = new DemandSpacesQueryParam(DemandTemplateTypeEnum.AIRCONDITIONING);
        spaceQueryParam.queryOnlyControlled();
        spaceQueryParam.setWithColumns(param.getWithColumns());
        return DemandTypeEnum.AIRCONDITIONING.demandStrategy().listDemandSpace(spaceQueryParam);
    }

    @Override
    public List<? extends DtpRwdSpaceVO> listDashboardSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        List<DtpRwdSpaceDTO> spaces = this.listSpaces(param);
        if (CollectionUtils.isEmpty(spaces)) {
            return Collections.emptyList();
        }
        return BeanUtil.copyToList(spaces, DtpRwdSpaceVO.class);
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipments(@NonNull RunningMonitorEquipmentQueryParam param) {
        RedisCacheTemplate.MethodCall<List<DtpRwdEquipmentDTO>> methodToCache =
                () -> this.queryEquipments(param);
        // 缓存热刷新
        return this.redisCacheTemplate.asyncRefreshTemplate("listEquipmentsAIR_TERMINAL", param, methodToCache);
    }

    /**
     * ACVVEA递归查询最大深度
     *
     * @since v7.260502.0
     */
    private static final int ACVVEA_RECURSIVE_MAX_DEPTH = 10;

    /**
     * 查询末端设备数据
     * <p>分两阶段并行查询：
     * <ol>
     *     <li>Phase 1: 并行查询基础ID数据（挑空空间、空调控制分区、空调机组、控制组、新风机组）</li>
     *     <li>Phase 2: 并行查询各类设备（基本设备、排风兼排烟风机、水阀、通风风机、风阀、通断风阀）</li>
     * </ol>
     *
     * @param param 查询参数
     * @return 设备列表
     * @since v7.260502.0
     */
    private @NonNull ArrayList<DtpRwdEquipmentDTO> queryEquipments(
            @NonNull RunningMonitorEquipmentQueryParam param) {
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询末端设备");
        try (PerformanceTracker.SafeNode ignored = tracker.track("queryEquipments")) {
            // Phase 1: 并行查询基础数据
            BaseIdData baseData = this.queryBaseIdData(tracker);

            // Phase 2: 并行查询各类设备
            Map<String, DtpRwdEquipmentDTO> idAndData = this.queryAllEquipments(
                    tracker, param, baseData);

            if (MapUtils.isEmpty(idAndData)) {
                logProjectInstance().warn("无有效的设备");
                return Lists.newArrayList();
            }
            return new ArrayList<>(idAndData.values());
        }
    }

    /**
     * Phase 1 基础查询数据持有类
     * <p>存储并行查询获取的各类基础ID集合，供 Phase 2 设备查询使用
     *
     * @since v7.260502.0
     */
    private static class BaseIdData {
        /**
         * 全量空间ID集合（用于过滤有服务空间关系的设备）
         */
        final Set<String> allSpaceIds;
        /**
         * 空调控制分区ID集合
         */
        final Set<String> acSpaceIds;
        /**
         * 全量空调机组ID集合（ACATAH）
         */
        final Set<String> acatahIds;
        /**
         * 空调机组控制组ID集合（ACAHG_GROUP）
         */
        final Set<String> acahgIds;
        /**
         * 新风机组ID集合（ACATFU）
         */
        final Set<String> acatfuIds;
        /**
         * 空调机组控制组ID -> 关联空调机组ID集合
         */
        final Map<String, Set<String>> acahgIdAndAcatahIds;
        /**
         * 独立空调机组ID集合 = 全量空调机组 - 属于控制组的空调机组
         */
        final Set<String> independentAcatahIds;

        BaseIdData(Set<String> allSpaceIds, Set<String> acSpaceIds,
                   Set<String> acatahIds, Set<String> acahgIds,
                   Set<String> acatfuIds, Map<String, Set<String>> acahgIdAndAcatahIds,
                   Set<String> independentAcatahIds) {
            this.allSpaceIds = allSpaceIds;
            this.acSpaceIds = acSpaceIds;
            this.acatahIds = acatahIds;
            this.acahgIds = acahgIds;
            this.acatfuIds = acatfuIds;
            this.acahgIdAndAcatahIds = acahgIdAndAcatahIds;
            this.independentAcatahIds = independentAcatahIds;
        }
    }

    /**
     * Phase 1: 并行查询基础ID数据
     * <p>5路独立并行 + 1路依赖查询（控制组→属于控制组的空调机组）
     * <ul>
     *     <li>全量空间（用于过滤有服务空间关系的设备）</li>
     *     <li>空调控制分区（AC_SPACE）</li>
     *     <li>空调机组（ACATAH）</li>
     *     <li>空调机组控制组（ACAHG_GROUP）</li>
     *     <li>新风机组（ACATFU）</li>
     *     <li>属于控制组的空调机组（依赖ACAHG_GROUP结果，通过EQUIP_IN_GROUP关系查询）</li>
     * </ul>
     * <p>最终计算：独立空调机组 = 全量空调机组 - 属于控制组的空调机组
     *
     * @param tracker 性能追踪器
     * @return 基础数据
     * @since v7.260502.0
     */
    @SuppressWarnings("DuplicatedCode")
    private BaseIdData queryBaseIdData(@NonNull PerformanceTracker tracker) {
        AtomicReference<Set<String>> allSpaceIds =
                new AtomicReference<>(Collections.emptySet());
        AtomicReference<Set<String>> acSpaceIds =
                new AtomicReference<>(Collections.emptySet());
        AtomicReference<Set<String>> acatahIds =
                new AtomicReference<>(Collections.emptySet());
        AtomicReference<Set<String>> acahgIds =
                new AtomicReference<>(Collections.emptySet());
        AtomicReference<Set<String>> acatfuIds =
                new AtomicReference<>(Collections.emptySet());
        AtomicReference<Set<String>> acahgGroupAcatahIds =
                new AtomicReference<>(Collections.emptySet());
        AtomicReference<Map<String, Set<String>>> acahgIdAndAcatahIds = new AtomicReference<>(Collections.emptyMap());

        PerformanceTracker.ParallelNode allSpaceNode = tracker.enterParallel("查询全量空间");
        PerformanceTracker.ParallelNode acSpaceNode = tracker.enterParallel("查询空调控制分区");
        PerformanceTracker.ParallelNode acatahNode = tracker.enterParallel("查询空调机组");
        PerformanceTracker.ParallelNode acahgNode = tracker.enterParallel("查询空调机组控制组");
        PerformanceTracker.ParallelNode acatfuNode = tracker.enterParallel("查询新风机组");

        // 查询全量空间ID（用于过滤有服务空间关系的设备）
        CompletableFuture<Void> allSpaceFuture = this.runRwdQuery("查询全量空间", () -> {
            try (PerformanceTracker.ParallelScope ignored = allSpaceNode.openScope()) {
                DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam()
                        .addWithColumn(DtpRwdObjectDTO.Fields.id);
                allSpaceIds.set(this.dtpRwdClientService.listSpace(spaceQueryParam)
                        .stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet()));
            }
        });

        CompletableFuture<Void> acSpaceFuture = this.runRwdQuery("查询空调控制分区", () -> {
            try (PerformanceTracker.ParallelScope ignored = acSpaceNode.openScope()) {
                DtpRwdObjectQueryParam acSpaceQueryParam = new DtpRwdObjectQueryParam()
                        .setClassCodeEnum(DtpRwdClassCodeEnum.AC_SPACE)
                        .addWithColumn(DtpRwdObjectDTO.Fields.id);
                acSpaceIds.set(this.dtpRwdClientService.listObject(acSpaceQueryParam)
                        .stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet()));
            }
        });

        CompletableFuture<Void> acatahFuture = this.runRwdQuery("查询空调机组", () -> {
            try (PerformanceTracker.ParallelScope ignored = acatahNode.openScope()) {
                DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                        .setClassCodeEnum(DtpRwdClassCodeEnum.ACATAH)
                        .filterMonitoringSurveillanceStatus()
                        .addWithColumn(DtpRwdObjectDTO.Fields.id, InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value());
                acatahIds.set(this.dtpRwdClientService.listObject(queryParam)
                        .stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet()));
            }
        });

        CompletableFuture<Void> acahgFuture = this.runRwdQuery("查询空调机组控制组", () -> {
            try (PerformanceTracker.ParallelScope ignored = acahgNode.openScope()) {
                DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                        .setClassCodeEnum(DtpRwdClassCodeEnum.ACAHG_GROUP)
                        .filterMonitoringSurveillanceStatus()
                        .addWithColumn(DtpRwdObjectDTO.Fields.id, InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value());
                acahgIds.set(this.dtpRwdClientService.listObject(queryParam)
                        .stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet()));
            }
        });

        CompletableFuture<Void> acatfuFuture = this.runRwdQuery("查询新风机组", () -> {
            try (PerformanceTracker.ParallelScope ignored = acatfuNode.openScope()) {
                DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                        .setClassCodeEnum(DtpRwdClassCodeEnum.ACATFU)
                        .filterMonitoringSurveillanceStatus()
                        .addWithColumn(DtpRwdObjectDTO.Fields.id, InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value());
                acatfuIds.set(this.dtpRwdClientService.listObject(queryParam)
                        .stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet()));
            }
        });

        // 查询属于控制组且已在末端范围内的空调机组（依赖acahgIds和acatahIds）
        PerformanceTracker.ParallelNode groupAcatahNode = tracker.enterParallel("查询属于控制组的空调机组");
        CompletableFuture<Void> groupAcatahFuture = CompletableFuture.allOf(acahgFuture, acatahFuture).thenRunAsync(() -> {
            try (PerformanceTracker.ParallelScope ignored = groupAcatahNode.openScope()) {
                if (CollectionUtils.isNotEmpty(acahgIds.get()) && CollectionUtils.isNotEmpty(acatahIds.get())) {
                    DtpRwdObjectRelationQueryParam relationParam = new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.EQUIP_IN_GROUP).setObjFrom(acahgIds.get());
                    List<DtpRwdObjectDTO> groupAcatahObjects = this.dtpRwdClientService.listObject(
                            new DtpRwdObjectQueryParam()
                                    .setId(acatahIds.get())
                                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACATAH)
                                    .setRelationTo(relationParam)
                                    .filterMonitoringSurveillanceStatus()
                                    .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds));
                    Set<String> groupAcatahIdSet = groupAcatahObjects.stream()
                            .map(DtpRwdObjectDTO::getId)
                            .collect(Collectors.toSet());
                    acahgGroupAcatahIds.set(groupAcatahIdSet);
                    acahgIdAndAcatahIds.set(DtpRwdObjectDTO.mapRelObjIdAndIds(groupAcatahObjects));
                } else {
                    acahgGroupAcatahIds.set(Collections.emptySet());
                    acahgIdAndAcatahIds.set(Collections.emptyMap());
                }
            }
        }, BusinessThreadPoolExecutor.ttlExecutor()).exceptionally(ex -> {
            this.logProjectInstance().error("查询属于控制组的空调机组", ex);
            return null;
        });

        // 等待Phase 1所有任务完成
        CompletableFuture.allOf(allSpaceFuture, acSpaceFuture, acatahFuture,
                acatfuFuture, groupAcatahFuture).join();

        // 计算独立空调机组 = 全量空调机组 - 属于控制组的空调机组
        Set<String> independentAcatahIds = new HashSet<>(acatahIds.get());
        independentAcatahIds.removeAll(acahgGroupAcatahIds.get());

        return new BaseIdData(allSpaceIds.get(), acSpaceIds.get(), acatahIds.get(),
                acahgIds.get(), acatfuIds.get(), acahgIdAndAcatahIds.get(), independentAcatahIds);
    }

    /**
     * Phase 2: 并行查询各类设备
     * <p>5路并行查询，结果合并到 idAndData：
     * <ul>
     *     <li>基本设备（排除ACVTSF/FFSCAF/ACVVEJ/ACVVER/ACVVEA）</li>
     *     <li>电动调节水阀（服务空调控制分区，条件执行）</li>
     *     <li>通风风机（2路并集查询）</li>
     *     <li>电动调节风阀（3路并集查询）</li>
     *     <li>电动通断风阀（递归查询）</li>
     * </ul>
     *
     * @param tracker  性能追踪器
     * @param param    查询参数
     * @param baseData Phase 1基础数据
     * @return 设备ID与数据映射
     * @since v7.260502.0
     */
    private @NonNull Map<String, DtpRwdEquipmentDTO> queryAllEquipments(
            @NonNull PerformanceTracker tracker,
            @NonNull RunningMonitorEquipmentQueryParam param,
            @NonNull BaseIdData baseData) {
        param.addRequiredWithColumn(DtpRwdObjectDTO.Fields.id);
        Map<String, DtpRwdEquipmentDTO> idAndData = new ConcurrentHashMap<>();

        PerformanceTracker.ParallelNode basicEquipNode = tracker.enterParallel("查询基本设备");
        PerformanceTracker.ParallelNode acvvejNode = tracker.enterParallel("查询电动调节水阀");
        PerformanceTracker.ParallelNode acvtsfNode = tracker.enterParallel("查询通风风机");
        PerformanceTracker.ParallelNode acvverNode = tracker.enterParallel("查询电动调节风阀");
        PerformanceTracker.ParallelNode acvveaNode = tracker.enterParallel("查询电动通断风阀");

        List<CompletableFuture<Void>> phase2Futures = new ArrayList<>();

        // 基本设备查询
        phase2Futures.add(this.runRwdQuery("查询基本设备", () -> {
            try (PerformanceTracker.ParallelScope ignored = basicEquipNode.openScope()) {
                this.queryBasicEquipments(param, idAndData);
            }
        }));

        // 电动调节水阀
        if (CollectionUtils.isNotEmpty(baseData.acSpaceIds)) {
            phase2Futures.add(this.runRwdQuery("查询电动调节水阀", () -> {
                try (PerformanceTracker.ParallelScope ignored = acvvejNode.openScope()) {
                    this.queryACVVEJByACZone(
                            param, baseData.acSpaceIds, idAndData);
                }
            }));
        } else {
            acvvejNode.finish();
        }

        // 通风风机 3 路并集
        phase2Futures.add(this.runRwdQuery("查询通风风机", () -> {
            try (PerformanceTracker.ParallelScope ignored = acvtsfNode.openScope()) {
                this.queryACVTSFThreeWay(
                        param, baseData.allSpaceIds, baseData.acatahIds,
                        baseData.acatfuIds, idAndData);
            }
        }));

        // 电动调节风阀 3 路并集
        phase2Futures.add(this.runRwdQuery("查询电动调节风阀", () -> {
            try (PerformanceTracker.ParallelScope ignored = acvverNode.openScope()) {
                this.queryACVVERThreeWay(
                        param, baseData.acahgIds, baseData.independentAcatahIds,
                        baseData.acatfuIds, idAndData);
            }
        }));

        // 电动通断风阀 递归查询
        phase2Futures.add(this.runRwdQuery("查询电动通断风阀", () -> {
            try (PerformanceTracker.ParallelScope ignored = acvveaNode.openScope()) {
                this.queryACVVEARecursive(
                        param, baseData.allSpaceIds, idAndData);
            }
        }));

        // 等待Phase 2所有任务完成
        CompletableFuture.allOf(phase2Futures.toArray(new CompletableFuture[0])).join();
        this.fillAirHandlingGroupRelEquipmentIds(idAndData, baseData.acahgIdAndAcatahIds);

        return idAndData;
    }

    /**
     * 将空调机组控制组关联的空调机组ID写入底层设备DTO，供接口返回和实时查询结果复用。
     *
     * @param idAndData           设备ID -> 设备DTO
     * @param acahgIdAndAcatahIds 空调机组控制组ID -> 空调机组ID集合
     * @since v7.260502.0
     */
    private void fillAirHandlingGroupRelEquipmentIds(@NonNull Map<String, DtpRwdEquipmentDTO> idAndData,
                                                     @NonNull Map<String, Set<String>> acahgIdAndAcatahIds) {
        if (MapUtils.isEmpty(idAndData)) {
            return;
        }
        idAndData.values().stream()
                .filter(item -> DtpRwdClassCodeEnum.ACAHG_GROUP.value().equals(item.getClassCode()))
                .forEach(item -> item.setRelObjIds(
                        acahgIdAndAcatahIds.getOrDefault(item.getId(), Collections.emptySet())));
    }

    /**
     * 查询基本设备（排除特殊设备类）
     *
     * @param param     查询参数
     * @param idAndData 结果集
     * @since v7.260502.0
     */
    private void queryBasicEquipments(
            @NonNull RunningMonitorEquipmentQueryParam param,
            @NonNull Map<String, DtpRwdEquipmentDTO> idAndData) {
        List<DtpRwdClassCodeEnum> specialEquipmentClassCodes = Lists.newArrayList(
                DtpRwdClassCodeEnum.ACVTSF, DtpRwdClassCodeEnum.FFSCAF, DtpRwdClassCodeEnum.ACVVEJ,
                DtpRwdClassCodeEnum.ACVVER, DtpRwdClassCodeEnum.ACVVEA);
        List<DtpRwdClassCodeEnum> finalQueryClassCodes = AirTerminalService.ALL_EQUIPMENT_CLASS_CODES.stream()
                .filter(item -> !specialEquipmentClassCodes.contains(item))
                .collect(Collectors.toList());
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setClassCodeEnum(finalQueryClassCodes)
                .filterMonitoringSurveillanceStatus();
        List<DtpRwdEquipmentDTO> equipments = param.queryObject(queryParam);
        if (CollectionUtils.isNotEmpty(equipments)) {
            equipments.forEach(item -> idAndData.put(item.getId(), item));
        }
    }

    /**
     * 查询电动调节水阀（服务空调控制分区）
     *
     * @param param      查询参数
     * @param acSpaceIds 空调控制分区ID集合
     * @param idAndData  结果集
     * @since v7.260502.0
     */
    private void queryACVVEJByACZone(
            @NonNull RunningMonitorEquipmentQueryParam param,
            @NonNull Set<String> acSpaceIds,
            @NonNull Map<String, DtpRwdEquipmentDTO> idAndData) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setClassCodeEnum(DtpRwdClassCodeEnum.ACVVEJ)
                .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjTo(acSpaceIds))
                .filterMonitoringSurveillanceStatus();
        List<DtpRwdEquipmentDTO> equipments = param.queryObject(queryParam);
        if (CollectionUtils.isNotEmpty(equipments)) {
            equipments.forEach(item -> idAndData.put(item.getId(), item));
        }
    }

    /**
     * 查询通风风机（3路并集）
     * <p>① 空调接力风机：ACAirNetwork/Supply(from=ACATAH) + FreshAirNetwork/Fresh(from=ACATFU)，funcType=8
     * <p>② 排风机：funcType=10 + EQUIPMENT_SERVICE_SPACE（有服务空间关系的排风机）
     *
     * @param param       查询参数
     * @param allSpaceIds 全量空间ID集合（用于EQUIPMENT_SERVICE_SPACE过滤）
     * @param acatahIds   空调机组ID集合
     * @param acatfuIds   新风机组ID集合
     * @param idAndData   结果集
     * @since v7.260502.0
     */
    private void queryACVTSFThreeWay(
            @NonNull RunningMonitorEquipmentQueryParam param,
            @NonNull Set<String> allSpaceIds,
            @NonNull Set<String> acatahIds,
            @NonNull Set<String> acatfuIds,
            @NonNull Map<String, DtpRwdEquipmentDTO> idAndData) {
        Set<String> relayFanIds = new HashSet<>();
        Set<String> exhaustFanIds = new HashSet<>();

        // ① 接力风机：ACAirNetwork/Supply + FreshAirNetwork/Fresh，funcType=8
        // 通过 ACAirNetwork/Supply 查询接力风机
        if (CollectionUtils.isNotEmpty(acatahIds)) {
            DtpRwdObjectQueryParam supplyParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACVTSF)
                    .addInfoCode(InfoCodeEnum.FUNC_TYPE.value(), "8")
                    .setRelationTo(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.AC_AIR_NETWORK_SUPPLY).setObjFrom(acatahIds))
                    .filterMonitoringSurveillanceStatus();
            List<DtpRwdEquipmentDTO> relayFromAcatah = param.queryObject(supplyParam);
            if (CollectionUtils.isNotEmpty(relayFromAcatah)) {
                relayFromAcatah.forEach(item -> {
                    relayFanIds.add(item.getId());
                    idAndData.put(item.getId(), item);
                });
            }
        }

        // 通过 FreshAirNetwork/Fresh 查询接力风机
        if (CollectionUtils.isNotEmpty(acatfuIds)) {
            DtpRwdObjectQueryParam freshParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACVTSF)
                    .addInfoCode(InfoCodeEnum.FUNC_TYPE.value(), "8")
                    .setRelationTo(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.FRESH_AIR_NETWORK).setObjFrom(acatfuIds))
                    .filterMonitoringSurveillanceStatus();
            List<DtpRwdEquipmentDTO> relayFromAcatfu = param.queryObject(freshParam);
            if (CollectionUtils.isNotEmpty(relayFromAcatfu)) {
                relayFromAcatfu.forEach(item -> {
                    relayFanIds.add(item.getId());
                    idAndData.put(item.getId(), item);
                });
            }
        }

        // ② 排风机：funcType=10 + EQUIPMENT_SERVICE_SPACE（有服务空间关系的排风机）
        if (CollectionUtils.isNotEmpty(allSpaceIds)) {
            DtpRwdObjectQueryParam exhaustParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACVTSF)
                    .addInfoCode(InfoCodeEnum.FUNC_TYPE.value(), "10")
                    .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjTo(allSpaceIds))
                    .filterMonitoringSurveillanceStatus();
            List<DtpRwdEquipmentDTO> exhaustFans = param.queryObject(exhaustParam);
            if (CollectionUtils.isNotEmpty(exhaustFans)) {
                exhaustFans.forEach(item -> {
                    exhaustFanIds.add(item.getId());
                    idAndData.put(item.getId(), item);
                });
            }
        }

        logProjectInstance().debug("通风风机3路查询完成",
                Arrays.asList(
                        Pair.of("接力风机", relayFanIds.size()),
                        Pair.of("排风机", exhaustFanIds.size())));
    }

    /**
     * 查询电动调节风阀（3 路并集）
     * <p>① 与空调机组控制组关联：ACAirNetwork/Supply + Return + ReandEx
     * <p>② 与独立空调机组关联：差集ACATAH + ACAirNetwork/Supply + Return
     * <p>③ 与新风机组关联：FreshAirNetwork/Fresh
     *
     * @param param                查询参数
     * @param acahgIds             空调机组控制组ID集合
     * @param independentAcatahIds 独立空调机组ID集合
     * @param acatfuIds            新风机组ID集合
     * @param idAndData            结果集
     * @since v7.260502.0
     */
    @SuppressWarnings("DuplicatedCode")
    private void queryACVVERThreeWay(
            @NonNull RunningMonitorEquipmentQueryParam param,
            @NonNull Set<String> acahgIds,
            @NonNull Set<String> independentAcatahIds,
            @NonNull Set<String> acatfuIds,
            @NonNull Map<String, DtpRwdEquipmentDTO> idAndData) {
        // ① 与空调机组控制组关联
        if (CollectionUtils.isNotEmpty(acahgIds)) {
            // ACAirNetwork/Supply: from=ACAHG, to=ACVVER
            DtpRwdObjectQueryParam supplyParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACVVER)
                    .setRelationTo(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.AC_AIR_NETWORK_SUPPLY).setObjFrom(acahgIds))
                    .filterMonitoringSurveillanceStatus();
            this.addEquipmentsToMap(param.queryObject(supplyParam), idAndData);

            // ACAirNetwork/Return: from=ACVVER, to=ACAHG
            DtpRwdObjectQueryParam returnParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACVVER)
                    .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.AC_AIR_NETWORK_RETURN).setObjTo(acahgIds))
                    .filterMonitoringSurveillanceStatus();
            this.addEquipmentsToMap(param.queryObject(returnParam), idAndData);

            // ACAirNetwork/ReandEx: from=ACVVER, to=ACAHG
            DtpRwdObjectQueryParam reandExParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACVVER)
                    .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.AC_AIR_NETWORK_RE_AND_EX).setObjTo(acahgIds))
                    .filterMonitoringSurveillanceStatus();
            this.addEquipmentsToMap(param.queryObject(reandExParam), idAndData);
        }

        // ② 与独立空调机组关联
        if (CollectionUtils.isNotEmpty(independentAcatahIds)) {
            // ACAirNetwork/Supply: from=ACATAH, to=ACVVER
            DtpRwdObjectQueryParam supplyParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACVVER)
                    .setRelationTo(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.AC_AIR_NETWORK_SUPPLY).setObjFrom(independentAcatahIds))
                    .filterMonitoringSurveillanceStatus();
            this.addEquipmentsToMap(param.queryObject(supplyParam), idAndData);

            // ACAirNetwork/Return: from=ACVVER, to=ACATAH
            DtpRwdObjectQueryParam returnParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACVVER)
                    .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.AC_AIR_NETWORK_RETURN).setObjTo(independentAcatahIds))
                    .filterMonitoringSurveillanceStatus();
            this.addEquipmentsToMap(param.queryObject(returnParam), idAndData);
        }

        // ③ 与新风机组关联：FreshAirNetwork/Fresh
        if (CollectionUtils.isNotEmpty(acatfuIds)) {
            DtpRwdObjectQueryParam freshParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACVVER)
                    .setRelationTo(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.FRESH_AIR_NETWORK).setObjFrom(acatfuIds))
                    .filterMonitoringSurveillanceStatus();
            this.addEquipmentsToMap(param.queryObject(freshParam), idAndData);
        }

        logProjectInstance().debug("电动调节风阀3路查询完成");
    }

    /**
     * 查询电动通断风阀（递归查询）
     * <p>Step1: 查询有服务空间关系的排风机 ACVTSF(funcType=10) + EQUIPMENT_SERVICE_SPACE
     * <p>Step2: 查询与排风机有关联的阀门：VentNetwork/Discharge 双向
     * <p>Step3: 递归查询阀门关联的阀门，直至找不到新设备
     *
     * @param param       查询参数
     * @param allSpaceIds 全量空间ID集合（用于EQUIPMENT_SERVICE_SPACE过滤）
     * @param idAndData   结果集
     * @since v7.260502.0
     */
    @SuppressWarnings("DuplicatedCode")
    private void queryACVVEARecursive(
            @NonNull RunningMonitorEquipmentQueryParam param,
            @NonNull Set<String> allSpaceIds,
            @NonNull Map<String, DtpRwdEquipmentDTO> idAndData) {
        // Step1: 查询有服务空间关系的排风机（funcType=10 + EQUIPMENT_SERVICE_SPACE）
        if (CollectionUtils.isEmpty(allSpaceIds)) {
            return;
        }
        DtpRwdObjectQueryParam exhaustParam = new DtpRwdObjectQueryParam()
                .setClassCodeEnum(DtpRwdClassCodeEnum.ACVTSF)
                .addInfoCode(InfoCodeEnum.FUNC_TYPE.value(), "10")
                .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjTo(allSpaceIds))
                .filterMonitoringSurveillanceStatus();
        List<DtpRwdEquipmentDTO> exhaustFans = param.queryObject(exhaustParam);
        if (CollectionUtils.isEmpty(exhaustFans)) {
            return;
        }

        Set<String> currentSourceIds = exhaustFans.stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        Set<String> allValveIds = new HashSet<>();
        int depth = 0;

        // Step2-3: 递归查询阀门
        while (CollectionUtils.isNotEmpty(currentSourceIds) && depth < ACVVEA_RECURSIVE_MAX_DEPTH) {
            depth++;

            // 查询 VentNetwork/Discharge: from=当前设备, to=ACVVEA
            Set<String> newValveIdsFrom = new HashSet<>();
            DtpRwdObjectQueryParam fromParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACVVEA)
                    .setRelationTo(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.VENT_NETWORK_DISCHARGE).setObjFrom(currentSourceIds))
                    .filterMonitoringSurveillanceStatus();
            List<DtpRwdEquipmentDTO> valvesFrom = param.queryObject(fromParam);
            if (CollectionUtils.isNotEmpty(valvesFrom)) {
                valvesFrom.forEach(item -> {
                    newValveIdsFrom.add(item.getId());
                    idAndData.put(item.getId(), item);
                });
            }

            // 查询 VentNetwork/Discharge: from=ACVVEA, to=当前设备
            Set<String> newValveIdsTo = new HashSet<>();
            DtpRwdObjectQueryParam toParam = new DtpRwdObjectQueryParam()
                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACVVEA)
                    .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.VENT_NETWORK_DISCHARGE).setObjTo(currentSourceIds))
                    .filterMonitoringSurveillanceStatus();
            List<DtpRwdEquipmentDTO> valvesTo = param.queryObject(toParam);
            if (CollectionUtils.isNotEmpty(valvesTo)) {
                valvesTo.forEach(item -> {
                    newValveIdsTo.add(item.getId());
                    idAndData.put(item.getId(), item);
                });
            }

            // 合并本轮新阀门ID，并排除已查询过的
            Set<String> newValveIds = new HashSet<>();
            newValveIds.addAll(newValveIdsFrom);
            newValveIds.addAll(newValveIdsTo);
            newValveIds.removeAll(allValveIds);

            if (newValveIds.isEmpty()) {
                break;
            }

            allValveIds.addAll(newValveIds);
            // 下一轮以新阀门为源继续递归
            currentSourceIds = newValveIds;
        }

        if (depth >= ACVVEA_RECURSIVE_MAX_DEPTH) {
            logProjectInstance().warn("电动通断风阀递归查询达到最大深度",
                    Arrays.asList(
                            Pair.of("maxDepth", ACVVEA_RECURSIVE_MAX_DEPTH),
                            Pair.of("totalValves", allValveIds.size())));
        } else {
            logProjectInstance().debug("电动通断风阀递归查询完成",
                    Arrays.asList(
                            Pair.of("depth", depth),
                            Pair.of("totalValves", allValveIds.size())));
        }
    }

    /**
     * 将设备列表添加到结果集
     *
     * @param equipments 设备列表
     * @param idAndData  结果集
     * @since v7.260502.0
     */
    private void addEquipmentsToMap(List<DtpRwdEquipmentDTO> equipments,
                                    Map<String, DtpRwdEquipmentDTO> idAndData) {
        if (CollectionUtils.isNotEmpty(equipments)) {
            equipments.forEach(item -> idAndData.put(item.getId(), item));
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> exceptionFilterCondition() {
        return x -> x.stream().filter(item -> {
            boolean riskConditions = item.getObjectType().equals(ObjectTypeEnum.SPACE.value())
                    && CollectionUtils.isNotEmpty(item.getRiskMeasureAimLevel2Collect())
                    && item.getRiskMeasureAimLevel2Collect().stream()
                    .anyMatch(Lists.newArrayList(RiskMeasureAimLevel2.E5.getCode(), RiskMeasureAimLevel2.E34.getCode(), RiskMeasureAimLevel2.E35.getCode())::contains);
            return riskConditions || item.getObjectType().equals(ObjectTypeEnum.EQUIPMENT.value());
        }).collect(Collectors.toList());
    }

    @Override
    public Collection<RiskMeasureAimLevel2> exceptionFilterRiskMeasureAimLevel2() {
        return Lists.newArrayList(RiskMeasureAimLevel2.E5, RiskMeasureAimLevel2.E34, RiskMeasureAimLevel2.E35);
    }

    /**
     * 查询冷站空间列表
     *
     * @param param 查询参数
     * @return {@link List }<{@link DtpRwdSpaceDTO }>
     */
    private @NonNull List<DtpRwdSpaceDTO> listColdSpaces(
            @NonNull RunningMonitorSpaceQueryParam param) {
        // 查询冷源系统
        DtpRwdObjectQueryParam coldSystemQueryParam = new DtpRwdObjectQueryParam();
        coldSystemQueryParam.setClassCode(Arrays.asList(DtpRwdClassCodeEnum.COLD_SYSTEM.value(),
                DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.value(),
                DtpRwdClassCodeEnum.HOT_SYSTEM.value(),
                DtpRwdClassCodeEnum.ACAC_SYSTEM.value()));
        coldSystemQueryParam.filterMonitoringSurveillanceStatus();
        coldSystemQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        List<String> coldSystemIds = this.dtpRwdClientService.listObject(coldSystemQueryParam).stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(coldSystemIds)) {
            return Lists.newArrayList();
        }
        DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam();
        spaceQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE).setObjFrom(coldSystemIds));
        return param.queryObject(spaceQueryParam, this.dtpRwdClientService::listSpace);
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "运行检测-空调末端策略";
    }
}
