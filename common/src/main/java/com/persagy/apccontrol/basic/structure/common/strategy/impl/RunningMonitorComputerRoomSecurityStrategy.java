package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.model.entity.instance.config.InstanceConfig;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdCriteriaSignEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.instance.InstanceConfigTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.rwd.ObjectQueryParam;
import com.persagy.apccontrol.basic.model.vo.computeroom.ComputerRoomDashboardSpaceVO;
import com.persagy.apccontrol.basic.model.vo.computeroom.ComputerRoomEquipmentVo;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.model.page.query.PageQuery;
import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeClassCodeTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeClassCodeByTagResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeClassCodeTagResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectModelViewQueryDaoParam;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import com.persagy.apccontrol.basic.resource.cache.param.CacheRwdObjectQueryDaoParam;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.service.instance.config.InstanceConfigService;
import com.persagy.apccontrol.basic.structure.cacherwd.model.entity.CacheRwdObject;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheRwdObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl.CacheRwdComputerRoomSecurityStrategy;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.computeroom.converter.ComputerRoomConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectDetailModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeConfigModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeGroupModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeConfigQueryConfigValueObject;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import com.persagy.apccontrol.basic.structure.performance.StopWatch;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.ObjectExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(RunningMonitorComputerRoomSecurityStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorComputerRoomSecurityStrategy
        extends BaseRunningMonitorStrategy
        implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorComputerRoomSecurityStrategy";

    private final ComputerRoomConverter computerRoomConverter;
    private final DtpRwdClientService dtpRwdClientService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final InstanceConfigService instanceConfigService;
    private final ObjectMapper objectMapper;
    private final InfoCodeGateway infoCodeGateway;
    private final CacheRwdObjectRepository cacheRwdObjectRepository;
    private final InstanceService instanceService;

    /**
     * 新缓存对象仓库，读取缓存空间和设备数据。
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
    public String logTag() {
        return "运行监测-机房安全";
    }

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return InheritConfigTypeEnum.OBJECT;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        Set<String> roomFuncTypes = getRoomFuncTypes();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        InstanceConfig instanceConfig = instanceConfigService.getInstanceConfig(InstanceConfigTypeEnum.WHITE_LIST, DtpRwdObjectTypeEnum.SPACE,
                runningMonitorSecondType, runningMonitorSecondType.toRunningMonitorTypeEnum(), null);
        // 如果有配置
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        if (Objects.nonNull(instanceConfig)) {
            JsonNode configData = instanceConfig.getConfigData();
            // 配置为空，返回空对象
            if (Objects.isNull(configData) || CollectionUtils.isEmpty(objectMapper.convertValue(configData, List.class))) {
                return Collections.emptyList();
            }
            queryParam.setId(objectMapper.convertValue(configData, new TypeReference<Collection<String>>() {
            }));
        }
        queryParam.addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType, roomFuncTypes);
        List<DtpRwdSpaceDTO> spaces = param.queryObject(queryParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return Lists.newArrayList();
        }
        return spaces;
    }

    public List<DtpRwdSpaceDTO> listSpacesComputerRoomSecurity(@NonNull RunningMonitorSpaceQueryParam param) {
        Set<String> roomFuncTypes = getRoomFuncTypes();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        InstanceConfig instanceConfig = instanceConfigService.getInstanceConfig(InstanceConfigTypeEnum.WHITE_LIST, DtpRwdObjectTypeEnum.SPACE,
                runningMonitorSecondType, runningMonitorSecondType.toRunningMonitorTypeEnum(), null);
        // 如果有配置
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        if (Objects.nonNull(instanceConfig)) {
            JsonNode configData = instanceConfig.getConfigData();
            // 配置为空，返回空对象
            if (Objects.isNull(configData) || CollectionUtils.isEmpty(objectMapper.convertValue(configData, List.class))) {
                return Collections.emptyList();
            }
            queryParam.setId(objectMapper.convertValue(configData, new TypeReference<Collection<String>>() {
            }));
        }
        queryParam.addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType, roomFuncTypes);
        List<DtpRwdSpaceDTO> spaces = param.queryObject(queryParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return Lists.newArrayList();
        }
        return spaces;
    }
    public List<DtpRwdSpaceDTO> listSpacesComputerRoomSecurityCache(@NonNull RunningMonitorSpaceQueryParam param) {
        Set<String> roomFuncTypes = getRoomFuncTypes();
        // 如果有配置
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType, roomFuncTypes);
        List<DtpRwdSpaceDTO> spaces = param.queryObject(queryParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return Lists.newArrayList();
        }
        return spaces;
    }

    /**
     * 串行分页变体：查询机房安全空间，使用指定 pageSize 进行串行分页，
     * 避免 FeignPageExecutor 并行分页对 BDTP 造成过大压力。
     *
     * <p>每次只发起 1 个 BDTP 请求，逐页串行获取全量数据。</p>
     *
     * @param param    空间查询参数
     * @param pageSize 每页大小，建议设为 page.size.limit（默认 1000）以走 FeignPageExecutor 同步分支
     * @return 全量空间 DTO 列表
     * @since v7.260802.0
     */
    public @NonNull List<DtpRwdSpaceDTO> listSpacesComputerRoomSecurityCacheSerial(
            @NonNull RunningMonitorSpaceQueryParam param, int pageSize) {
        Set<String> roomFuncTypes = getRoomFuncTypes();
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType, roomFuncTypes);

        List<DtpRwdSpaceDTO> allSpaces = new ArrayList<>();
        int pageNo = 1;
        while (true) {
            queryParam.setPageQuery(new PageQuery(pageNo, pageSize));
            // withColumns 可能被 checkAndSetParam 清除，每次迭代重新设置
            if (CollectionUtils.isNotEmpty(param.getWithColumns())) {
                queryParam.setWithColumns(param.getWithColumns());
            }
            List<DtpRwdSpaceDTO> pageResult = param.queryObject(queryParam);
            if (CollectionUtils.isEmpty(pageResult)) {
                break;
            }
            allSpaces.addAll(pageResult);
            if (pageResult.size() < pageSize) {
                break;
            }
            pageNo++;
        }
        return allSpaces;
    }

    /**
     * 缓存构建专用设备查询，返回原始 DtpRwdEquipmentDTO。
     *
     * <p>该方法直接查询 BDTP，不经过任何缓存层，不含 Knowledge 数据填充，
     * 专用于新版缓存对象模型构建写入。</p>
     *
     * @param param 设备查询参数（含 spaceIds 和 withColumns）
     * @return 原始设备 DTO 列表
     * @since v7.260802.0
     */
    public @NonNull List<DtpRwdEquipmentDTO> listEquipmentsForCacheBuild(
            @NonNull RunningMonitorEquipmentQueryParam param) {
        List<DtpRwdEquipmentDTO> equipments = param.queryObject(new DtpRwdObjectQueryParam());
        if (CollectionUtils.isEmpty(equipments)) {
            return Collections.emptyList();
        }
        return equipments;
    }

    /**
     * 缓存构建专用设备查询，串行分页拉取全量分页数据。
     *
     * <p>pageSize 不超过 page.size.limit（默认 1000）时底层 FeignPageExecutor 走同步分支，
     * 每次请求精确返回一页；本方法逐页串行查询，任一页返回不足一页即终止。
     * 异常直接向上抛出，不做吞没处理。</p>
     *
     * <p>注意：pageSize 超过 page.size.limit 会触发底层 FeignPageExecutor 并行分页，
     * 其 totalPage 按传入 pageSize 计算而后续页按 limit 翻页，页码语义与外层冲突，
     * 会导致数据重复或遗漏，因此 pageSize 不得超过 page.size.limit。</p>
     *
     * @param param       设备查询参数（含 spaceIds）
     * @param withColumns 查询列投影集合
     * @param pageSize    每页大小，不得超过 page.size.limit（默认 1000）
     * @return 原始设备 DTO 列表（按页序合并）
     * @since v7.260802.0
     */
    public @NonNull List<DtpRwdEquipmentDTO> listEquipmentsForCacheBuildParallel(
            @NonNull RunningMonitorEquipmentQueryParam param,
            @NonNull Collection<String> withColumns,
            int pageSize) {
        long start = System.currentTimeMillis();
        List<DtpRwdEquipmentDTO> allEquipments = new ArrayList<>();
        int pageNo = 1;
        int maxRetries = 3;
        int prevSize = 0;
        while (true) {
            List<DtpRwdEquipmentDTO> pageResult = queryEquipmentForCacheBuildPage(param, withColumns, pageNo, pageSize);

            if (CollectionUtils.isNotEmpty(pageResult)) {
                allEquipments.addAll(pageResult);
                prevSize = pageResult.size();
                if (pageResult.size() < pageSize) {
                    break;
                }
                pageNo++;
                continue;
            }

            // pageResult 为空
            if (pageNo == 1) {
                // 第一页就为空，数据本身为空，直接返回
                break;
            }

            // 上一页是满页但当前页突然变空，可能是底层 RPC 异常被静默吞没（isThrows=false）
            // 正常分页结束时尾页应返回不足一页的数据，不应从满页直接跳到空页
            if (prevSize >= pageSize) {
                boolean recovered = false;
                for (int retry = 1; retry <= maxRetries; retry++) {
                    logProjectInstance().warn("[性能] listEquipmentsForCacheBuildParallel-异常空页检测, "
                            + "pageNo: " + pageNo + ", 上一页数据量: " + prevSize
                            + ", 重试 " + retry + "/" + maxRetries);
                    pageResult = queryEquipmentForCacheBuildPage(param, withColumns, pageNo, pageSize);
                    if (CollectionUtils.isNotEmpty(pageResult)) {
                        allEquipments.addAll(pageResult);
                        prevSize = pageResult.size();
                        if (pageResult.size() < pageSize) {
                            // 尾页，正常结束
                            recovered = true;
                            break;
                        }
                        pageNo++;
                        recovered = true;
                        break;
                    }
                }
                if (!recovered) {
                    throw new RuntimeException("[性能] listEquipmentsForCacheBuildParallel-分页查询异常, "
                            + "pageNo: " + pageNo + " 连续返回空结果（重试 " + maxRetries + " 次），"
                            + "上一页数据量: " + prevSize + ", 已采集数据量: " + allEquipments.size()
                            + "，疑似底层 RPC 异常被静默吞没");
                }
                continue;
            }

            // 上一页不足一页，当前页为空，正常结束
            break;
        }
        logProjectInstance().info("[性能] listEquipmentsForCacheBuildParallel-串行分页 耗时: "
                + (System.currentTimeMillis() - start) + "ms"
                + ", 每页大小: " + pageSize + ", 总页数: " + pageNo
                + ", 数据量: " + allEquipments.size());
        return allEquipments;
    }

    /**
     * 查询单页缓存构建设备数据。
     *
     * @param param       设备查询参数（含 spaceIds）
     * @param withColumns 查询列投影集合
     * @param pageNo      页码
     * @param pageSize    每页大小
     * @return 该页设备 DTO 列表，可能为空
     * @since v7.260802.0
     */
    private @NonNull List<DtpRwdEquipmentDTO> queryEquipmentForCacheBuildPage(
            @NonNull RunningMonitorEquipmentQueryParam param,
            @NonNull Collection<String> withColumns,
            int pageNo, int pageSize) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setPageQuery(new PageQuery(pageNo, pageSize));
        // withColumns 可能被 checkAndSetParam 清除，每次查询重新设置
        queryParam.setWithColumns(withColumns);
        return param.queryObject(queryParam);
    }

    /**
     * 获取白名单空间ID集合
     *
     * <p>返回值含义：</p>
     * <ul>
     *     <li>null - 无白名单配置，不需要按白名单过滤</li>
     *     <li>空 Set - 白名单配置存在但数据为空，应返回空列表</li>
     *     <li>非空 Set - 白名单中的空间ID集合</li>
     * </ul>
     *
     * @param runningMonitorSecondType 运行监测二级类型
     * @return 白名单空间ID集合，null 表示无白名单配置
     * @since 7.260402.0
     */
    private @Nullable Set<String> getWhitelistSpaceIds(@NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType) {
        InstanceConfig instanceConfig = instanceConfigService.getInstanceConfig(
                InstanceConfigTypeEnum.WHITE_LIST, DtpRwdObjectTypeEnum.SPACE,
                runningMonitorSecondType, runningMonitorSecondType.toRunningMonitorTypeEnum(), null);
        if (Objects.isNull(instanceConfig)) {
            // 无白名单配置
            return null;
        }
        JsonNode configData = instanceConfig.getConfigData();
        if (Objects.isNull(configData) || CollectionUtils.isEmpty(objectMapper.convertValue(configData, List.class))) {
            // 白名单配置为空
            return Collections.emptySet();
        }
        Collection<String> spaceIds = objectMapper.convertValue(configData, new TypeReference<Collection<String>>() {
        });
        return new HashSet<>(spaceIds);
    }

    /**
     * 查询机房配置的空间功能类型列表
     *
     * @return 空间功能类型集合
     * @since 7.260402.0
     */
    private @NonNull Set<String> getRoomFuncTypes() {
        InfoCodeInitByObjectParam initByObjectParam = new InfoCodeInitByObjectParam();
        initByObjectParam.setRunningMonitorType(RunningMonitorSecondTypeEnum.FIXED_OBJECT.toRunningMonitorTypeEnum());
        initByObjectParam.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.FIXED_OBJECT);
        initByObjectParam.setTemplateTypes(Collections.singleton(InfoCodeTemplateTypeEnum.DEFAULT));
        InfoCodeConfigQueryConfigValueObject queryConfigValueObject = new InfoCodeConfigQueryConfigValueObject();
        queryConfigValueObject.setGroupCode(InfoCodeGroupModel.DEFAULT);
        queryConfigValueObject.setInfoCodes(Collections.singleton(InfoCodeConfigModel.COMPUTER_ROOM_SECURITY_ROOM_FUNC_TYPE));
        initByObjectParam.setFilterGroups(Collections.singleton(queryConfigValueObject));

        List<DtpRwdObjectDTO> obj = infoCodeGateway.listObjectByIds(Collections.singleton(ContextUtil.getProjectId()));
        if (CollectionUtils.isEmpty(obj)) {
            return Collections.emptySet();
        }
        initByObjectParam.setObjects(obj);
        List<InfoCodeAggregate> aggregates = infoCodeGateway.of(initByObjectParam);
        if (CollectionUtils.isEmpty(aggregates)) {
            return Collections.emptySet();
        }
        InfoCodeAggregate infoCodeAggregate = aggregates.get(0);
        JsonNode roomFuncTypes = infoCodeAggregate.getConfigModel().getTackValueLogic();
        if (roomFuncTypes == null) {
            return Collections.emptySet();
        }
        return objectMapper.convertValue(roomFuncTypes, new TypeReference<Set<String>>() {
        });
    }

    @Override
    public List<ComputerRoomEquipmentVo> listEquipments(@NonNull RunningMonitorEquipmentQueryParam param) {
        return listEquipmentsFromRwd(param);
    }

    /**
     * 从 RWD 实时查询设备数据
     *
     * @param param 查询参数
     * @return 设备列表
     * @since 7.260402.0
     */
    private @NonNull List<ComputerRoomEquipmentVo> listEquipmentsFromRwd(@NonNull RunningMonitorEquipmentQueryParam param) {
        StopWatch stopWatch = StopWatch.create("Equipment");
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();

        if (CollectionUtils.isEmpty(param.getFilterEquipmentInSpaceIds())) {
            stopWatch.start("查询所在空间");
            RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
            spaceQueryParam.setRunningMonitorSecondType(runningMonitorSecondType);
            spaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            Set<String> spaceIds = listSpaces(spaceQueryParam).stream()
                    .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(spaceIds)) {
                return Lists.newArrayList();
            }
            param.setFilterEquipmentInSpaceIds(spaceIds);
            stopWatch.stop();
        }
        stopWatch.start("查询设备");
        Set<String> filterExcludeClassCodes = getFilterExcludeClassCodes();
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        if (CollectionUtils.isNotEmpty(filterExcludeClassCodes)) {
            queryParam.addInfoCode(DtpRwdObjectDTO.Fields.classCode,
                    DtpRwdCriteriaSignEnum.NOT_EQUALS_IN, filterExcludeClassCodes);
        }
        List<ComputerRoomEquipmentVo> equipments = computerRoomConverter.toEquipmentVo(
                BusinessThreadPoolExecutor.parallelExecute(() -> param.queryObject(queryParam)));
        stopWatch.stop();
        if (CollectionUtils.isEmpty(equipments)) {
            return Collections.emptyList();
        }
        List<StopWatch.TaskGroup> taskGroups = new ArrayList<>();
        AtomicReference<Map<String, String>> classCodeAndName = new AtomicReference<>(Collections.emptyMap());
        Map<String, String> classCodeAndCriticalLevel = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(equipments)) {
            Set<String> classCodes = equipments.stream().map(DtpRwdObjectDTO::getClassCode).collect(Collectors.toSet());
            taskGroups.add(new StopWatch.TaskGroup("查询设备类名称", () -> classCodeAndName.set(
                    dtpKnowledgeClientService.mapClassCodeAndName(false, false, classCodes))));
            taskGroups.add(new StopWatch.TaskGroup("查询设备类标签", () -> {
                DtpKnowledgeClassCodeTagQueryParam tagQueryParam = new DtpKnowledgeClassCodeTagQueryParam();
                tagQueryParam.setTagNames(Sets.newHashSet(ComputerRoomEquipmentVo.Fields.equipCriticalLevel));
                tagQueryParam.setObjTypes(Sets.newHashSet(DtpRwdObjectTypeEnum.EQUIPMENT.value()));
                Map<String, Map<String, DtpKnowledgeClassCodeTagResponse>> classCodeTypeAndTag =
                        dtpKnowledgeClientService.mapClassCodeTypeAndTag(tagQueryParam);
                classCodeTypeAndTag.forEach((classCode, tagAndData) -> {
                    DtpKnowledgeClassCodeTagResponse tagResponse = tagAndData.get(
                            ComputerRoomEquipmentVo.Fields.equipCriticalLevel);
                    if (Objects.isNull(tagResponse)) {
                        return;
                    }
                    classCodeAndCriticalLevel.put(classCode, tagResponse.getTagValue());
                });
            }));
        }
        stopWatch.addAsyncTaskGroup("设备的设备类名称+标签", taskGroups);
        for (ComputerRoomEquipmentVo equipment : equipments) {
            String classCode = equipment.getClassCode();
            equipment.setClassCodeName(classCodeAndName.get().get(classCode));
            equipment.setEquipCriticalLevel(classCodeAndCriticalLevel.get(classCode));
        }
        logProjectInstance().debug("设备-静态对象", Pair.of("data", stopWatch.prettyPrintByGroup()));
        return equipments;
    }

    public @NonNull List<? extends DtpRwdObjectDTO> listEquipmentsFromRwdFlor(String floorId) {
        // 新缓存路径
        if (this.cacheObjectModelStatusService.isReady(RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY)) {
            return listEquipmentsFromRwdFlorFromCache(floorId);
        }

        RunningMonitorEquipmentQueryParam param = new RunningMonitorEquipmentQueryParam();
        param.setWithColumns(Sets.newHashSet("id", "localName", "classCode", "bimId", "bimLocation", "relObjIds", "objType", "equipCriticalLevel", "monitoringSurveillanceStatus"));
        param.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.COMPUTER_ROOM_SECURITY);

        InfoCodeTakeValueLogicRwdObjectDetailModel takeValueLogic = new InfoCodeTakeValueLogicRwdObjectDetailModel();
        DtpRwdObjectQueryParam innerQueryParam = new DtpRwdObjectQueryParam();
        //前端也是用的这个两边一致
        innerQueryParam.setClassCode(Lists.newArrayList(
                DtpRwdClassCodeEnum.SPACE.value(),
                DtpRwdClassCodeEnum.SPVSCM.value(),
                DtpRwdClassCodeEnum.CFCSDR.value(),
                DtpRwdClassCodeEnum.CFCSFR.value(),
                DtpRwdClassCodeEnum.SPEGFG.value()));
        innerQueryParam.addInfoCode(DtpRwdSpaceDTO.Fields.floorId, Collections.singleton(floorId));
        innerQueryParam.setWithColumns(Lists.newArrayList(
                "id", "localName", "classCode", "roomFuncType", "roomFuncTypeName",
                "buildingId", "floorId", "bimId", "bimLocation", "outline", "monitoringSurveillanceStatus"));
        takeValueLogic.setQueryParam(innerQueryParam);
        ObjectQueryParam objectQueryParam = new ObjectQueryParam();
        objectQueryParam.setRunningMonitorType(RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY);
        objectQueryParam.setTakeValueLogic(takeValueLogic);
        List<? extends DtpRwdObjectDTO> floorSpaces = instanceService.listObjectsByTackValueLogic(objectQueryParam);
        if(CollectionUtils.isEmpty(floorSpaces)){
            return Lists.newArrayList();
        }
        Set<String> filterEquipmentInSpaceIds = floorSpaces.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toSet());
        param.setFilterEquipmentInSpaceIds(filterEquipmentInSpaceIds);
        StopWatch stopWatch = StopWatch.create("Equipment");

        stopWatch.start("查询设备");
        Set<String> filterExcludeClassCodes = getFilterExcludeClassCodes();
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        if (CollectionUtils.isNotEmpty(filterExcludeClassCodes)) {
            queryParam.addInfoCode(DtpRwdObjectDTO.Fields.classCode,
                    DtpRwdCriteriaSignEnum.NOT_EQUALS_IN, filterExcludeClassCodes);
        }
        List<ComputerRoomEquipmentVo> equipments = computerRoomConverter.toEquipmentVo(
                BusinessThreadPoolExecutor.parallelExecute(() -> param.queryObject(queryParam)));
        stopWatch.stop();
        if (CollectionUtils.isEmpty(equipments)) {
            return Collections.emptyList();
        }
        List<StopWatch.TaskGroup> taskGroups = new ArrayList<>();
        AtomicReference<Map<String, String>> classCodeAndName = new AtomicReference<>(Collections.emptyMap());
        Map<String, String> classCodeAndCriticalLevel = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(equipments)) {
            Set<String> classCodes = equipments.stream().map(DtpRwdObjectDTO::getClassCode).collect(Collectors.toSet());
            taskGroups.add(new StopWatch.TaskGroup("查询设备类名称", () -> classCodeAndName.set(
                    dtpKnowledgeClientService.mapClassCodeAndName(false, false, classCodes))));
            taskGroups.add(new StopWatch.TaskGroup("查询设备类标签", () -> {
                DtpKnowledgeClassCodeTagQueryParam tagQueryParam = new DtpKnowledgeClassCodeTagQueryParam();
                tagQueryParam.setTagNames(Sets.newHashSet(ComputerRoomEquipmentVo.Fields.equipCriticalLevel));
                tagQueryParam.setObjTypes(Sets.newHashSet(DtpRwdObjectTypeEnum.EQUIPMENT.value()));
                Map<String, Map<String, DtpKnowledgeClassCodeTagResponse>> classCodeTypeAndTag =
                        dtpKnowledgeClientService.mapClassCodeTypeAndTag(tagQueryParam);
                classCodeTypeAndTag.forEach((classCode, tagAndData) -> {
                    DtpKnowledgeClassCodeTagResponse tagResponse = tagAndData.get(
                            ComputerRoomEquipmentVo.Fields.equipCriticalLevel);
                    if (Objects.isNull(tagResponse)) {
                        return;
                    }
                    classCodeAndCriticalLevel.put(classCode, tagResponse.getTagValue());
                });
            }));
        }
        stopWatch.addAsyncTaskGroup("设备的设备类名称+标签", taskGroups);
        for (ComputerRoomEquipmentVo equipment : equipments) {
            String classCode = equipment.getClassCode();
            equipment.setClassCodeName(classCodeAndName.get().get(classCode));
            equipment.setEquipCriticalLevel(classCodeAndCriticalLevel.get(classCode));
        }
        // 组装空间 + 设备混合列表，与缓存路径返回结构保持一致
        List<DtpRwdObjectDTO> result = new ArrayList<>(floorSpaces.size() + equipments.size());
        result.addAll(floorSpaces);
        result.addAll(equipments);
        logProjectInstance().debug("设备-静态对象", Pair.of("data", stopWatch.prettyPrintByGroup()));
        return result;
    }

    /**
     * 从新缓存读取楼层设备列表（API 3）。
     *
     * <p>从 CRS_LIST_SPACE 缓存空间中按 floorId 过滤出 spaceIds（含 SPACE/SPVSCM/CFCSDR/CFCSFR/SPEGFG），
     * 再从 cache_object_rel 按 spaceIds 查设备 ID，最后从 cache_object 取设备数据。
     * Knowledge 数据实时查询。</p>
     *
     * @param floorId 楼层 ID
     * @return 设备 VO 列表
     * @since v7.260802.0
     */
    private @NonNull List<? extends DtpRwdObjectDTO> listEquipmentsFromRwdFlorFromCache(String floorId) {
        StopWatch stopWatch = StopWatch.create("Equipment-Cache");

        // Step 1: 从缓存空间中按 floorId 过滤出 spaceIds（含 SPACE/SPVSCM/CFCSDR/CFCSFR/SPEGFG）
        stopWatch.start("缓存空间定位");
        List<DtpRwdSpaceDTO> allSpaces = this.cacheObjectRepository.listByCacheKey(
                CacheObjectModelKeyConfig.COMPUTER_ROOM_SECURITY_LIST_SPACE,
                DtpRwdSpaceDTO.class, null);
        List<DtpRwdSpaceDTO> floorSpaces = allSpaces.stream()
                .filter(s -> StringUtils.equals(s.getFloorId(), floorId))
                .collect(Collectors.toList());
        Set<String> spaceIds = floorSpaces.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toSet());
        stopWatch.stop();
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Collections.emptyList();
        }

        // Step 2: 从 cache_object_rel 按 spaceIds 查设备 ID，并保留设备→空间关系用于重建 relObjIds
        stopWatch.start("缓存关系查询");
        CacheObjectRelQueryDaoParam relParam = new CacheObjectRelQueryDaoParam();
        relParam.setRelObjectIds(spaceIds);
        relParam.setGraphCode(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE.value().getLeft());
        relParam.setRelCode(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE.value().getRight());
        List<CacheObjectRel> rels = this.cacheObjectRelRepository.listReverse(relParam);
        Map<String, Set<String>> equipmentToSpaceIds = new LinkedHashMap<>();
        for (CacheObjectRel rel : rels) {
            equipmentToSpaceIds
                    .computeIfAbsent(rel.getObjectId(), k -> new LinkedHashSet<>())
                    .add(rel.getRelObjectId());
        }
        Set<String> equipmentIds = equipmentToSpaceIds.keySet();
        stopWatch.stop();
        if (CollectionUtils.isEmpty(equipmentIds)) {
            return Collections.emptyList();
        }

        // Step 3: 从 cache_object 取设备数据
        stopWatch.start("缓存设备读取");
        List<DtpRwdEquipmentDTO> equipments = this.cacheObjectRepository.listByObjectIds(
                equipmentIds, DtpRwdEquipmentDTO.class, null);
        stopWatch.stop();

        // Step 4: excludeClassCodes 过滤
        Set<String> filterExcludeClassCodes = getFilterExcludeClassCodes();
        if (CollectionUtils.isNotEmpty(filterExcludeClassCodes)) {
            equipments = equipments.stream()
                    .filter(eq -> !filterExcludeClassCodes.contains(eq.getClassCode()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(equipments)) {
            return Collections.emptyList();
        }

        // Step 5: DTO → VO 转换
        List<ComputerRoomEquipmentVo> equipmentVos = computerRoomConverter.toEquipmentVo(equipments);

        // Step 5.5: 补充 relObjIds、bimId/bimLocation（设备+空间）和 outline（SPACE 空间）
        stopWatch.start("补充relObjIds+bimLocation+outline");
        for (ComputerRoomEquipmentVo vo : equipmentVos) {
            vo.setRelObjIds(equipmentToSpaceIds.getOrDefault(vo.getId(), Collections.emptySet()));
        }
        // 合并查询：全部空间 ID + 设备 ID，一次取出 model_view
        Set<String> allModelViewIds = new LinkedHashSet<>(equipmentIds);
        for (DtpRwdSpaceDTO space : floorSpaces) {
            allModelViewIds.add(space.getId());
        }
        CacheObjectModelViewQueryDaoParam mvParam = new CacheObjectModelViewQueryDaoParam();
        mvParam.setIds(allModelViewIds);
        mvParam.addColumns(CacheObjectModelView::getId, CacheObjectModelView::getViewData);
        List<CacheObjectModelView> modelViews = this.cacheObjectModelViewRepository.listModelViews(mvParam);
        Map<String, CacheObjectModelView> modelViewById = new LinkedHashMap<>();
        for (CacheObjectModelView mv : modelViews) {
            modelViewById.put(mv.getId(), mv);
        }
        // 回填设备 bimId/bimLocation
        for (ComputerRoomEquipmentVo vo : equipmentVos) {
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
        // 回填空间 bimId/bimLocation（所有类型）和 outline（SPACE 类）
        for (DtpRwdSpaceDTO space : floorSpaces) {
            CacheObjectModelView mv = modelViewById.get(space.getId());
            if (mv != null && StringUtils.isNotBlank(mv.getViewData())) {
                Map<String, Object> viewFields = ObjectMapperUtils.readMapFailSafe(mv.getViewData());
                if (viewFields != null) {
                    if (viewFields.containsKey("bimId")) {
                        space.setBimId(String.valueOf(viewFields.get("bimId")));
                    }
                    if (viewFields.containsKey("bimLocation")) {
                        space.setBimLocation(String.valueOf(viewFields.get("bimLocation")));
                    }
                    if (DtpRwdClassCodeEnum.SPACE.value().equals(space.getClassCode())
                            && viewFields.containsKey("outline")) {
                        space.setOutline(String.valueOf(viewFields.get("outline")));
                    }
                }
            }
        }
        stopWatch.stop();

        // Step 6: Knowledge 实时填充 classCodeName + equipCriticalLevel
        stopWatch.start("Knowledge实时填充");
        Set<String> classCodes = equipmentVos.stream()
                .map(DtpRwdObjectDTO::getClassCode).collect(Collectors.toSet());

        Map<String, String> classCodeAndName = new ConcurrentHashMap<>();
        Map<String, String> classCodeAndCriticalLevel = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(CompletableFuture.runAsync(() -> classCodeAndName.putAll(
                dtpKnowledgeClientService.mapClassCodeAndName(false, false, classCodes)),
                BusinessThreadPoolExecutor.ttlExecutor()));
        futures.add(CompletableFuture.runAsync(() -> {
            DtpKnowledgeClassCodeTagQueryParam tagQueryParam = new DtpKnowledgeClassCodeTagQueryParam();
            tagQueryParam.setTagNames(Sets.newHashSet(ComputerRoomEquipmentVo.Fields.equipCriticalLevel));
            tagQueryParam.setObjTypes(Sets.newHashSet(DtpRwdObjectTypeEnum.EQUIPMENT.value()));
            Map<String, Map<String, DtpKnowledgeClassCodeTagResponse>> classCodeTypeAndTag =
                    dtpKnowledgeClientService.mapClassCodeTypeAndTag(tagQueryParam);
            classCodeTypeAndTag.forEach((classCode, tagAndData) -> {
                DtpKnowledgeClassCodeTagResponse tagResponse = tagAndData.get(
                        ComputerRoomEquipmentVo.Fields.equipCriticalLevel);
                if (Objects.nonNull(tagResponse)) {
                    classCodeAndCriticalLevel.put(classCode, tagResponse.getTagValue());
                }
            });
        }, BusinessThreadPoolExecutor.ttlExecutor()));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        stopWatch.stop();

        for (ComputerRoomEquipmentVo equipment : equipmentVos) {
            String classCode = equipment.getClassCode();
            equipment.setClassCodeName(classCodeAndName.get(classCode));
            equipment.setEquipCriticalLevel(classCodeAndCriticalLevel.get(classCode));
        }

        // Step 7: 组装空间 + 设备混合列表
        List<DtpRwdObjectDTO> result = new ArrayList<>(floorSpaces.size() + equipmentVos.size());
        result.addAll(floorSpaces);
        result.addAll(equipmentVos);
        logProjectInstance().debug("设备-缓存对象", Pair.of("data", stopWatch.prettyPrintByGroup()));
        return result;
    }

    private @NonNull List<ComputerRoomEquipmentVo> listEquipmentsFromRwdComputerRoomSecurity(@NonNull RunningMonitorEquipmentQueryParam param) {
        StopWatch stopWatch = StopWatch.create("Equipment");
        stopWatch.start("查询设备");
        Set<String> filterExcludeClassCodes = getFilterExcludeClassCodes();
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        if (CollectionUtils.isNotEmpty(filterExcludeClassCodes)) {
            queryParam.addInfoCode(DtpRwdObjectDTO.Fields.classCode,
                    DtpRwdCriteriaSignEnum.NOT_EQUALS_IN, filterExcludeClassCodes);
        }
        List<ComputerRoomEquipmentVo> equipments = computerRoomConverter.toEquipmentVo(
                BusinessThreadPoolExecutor.parallelExecute(() -> param.queryObject(queryParam)));
        stopWatch.stop();
        if (CollectionUtils.isEmpty(equipments)) {
            return Collections.emptyList();
        }
        List<StopWatch.TaskGroup> taskGroups = new ArrayList<>();
        AtomicReference<Map<String, String>> classCodeAndName = new AtomicReference<>(Collections.emptyMap());
        Map<String, String> classCodeAndCriticalLevel = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(equipments)) {
            Set<String> classCodes = equipments.stream().map(DtpRwdObjectDTO::getClassCode).collect(Collectors.toSet());
            taskGroups.add(new StopWatch.TaskGroup("查询设备类名称", () -> classCodeAndName.set(
                    dtpKnowledgeClientService.mapClassCodeAndName(false, false, classCodes))));
            taskGroups.add(new StopWatch.TaskGroup("查询设备类标签", () -> {
                DtpKnowledgeClassCodeTagQueryParam tagQueryParam = new DtpKnowledgeClassCodeTagQueryParam();
                tagQueryParam.setTagNames(Sets.newHashSet(ComputerRoomEquipmentVo.Fields.equipCriticalLevel));
                tagQueryParam.setObjTypes(Sets.newHashSet(DtpRwdObjectTypeEnum.EQUIPMENT.value()));
                Map<String, Map<String, DtpKnowledgeClassCodeTagResponse>> classCodeTypeAndTag =
                        dtpKnowledgeClientService.mapClassCodeTypeAndTag(tagQueryParam);
                classCodeTypeAndTag.forEach((classCode, tagAndData) -> {
                    DtpKnowledgeClassCodeTagResponse tagResponse = tagAndData.get(
                            ComputerRoomEquipmentVo.Fields.equipCriticalLevel);
                    if (Objects.isNull(tagResponse)) {
                        return;
                    }
                    classCodeAndCriticalLevel.put(classCode, tagResponse.getTagValue());
                });
            }));
        }
        stopWatch.addAsyncTaskGroup("设备的设备类名称+标签", taskGroups);
        for (ComputerRoomEquipmentVo equipment : equipments) {
            String classCode = equipment.getClassCode();
            equipment.setClassCodeName(classCodeAndName.get().get(classCode));
            equipment.setEquipCriticalLevel(classCodeAndCriticalLevel.get(classCode));
        }
        logProjectInstance().debug("设备-静态对象", Pair.of("data", stopWatch.prettyPrintByGroup()));
        return equipments;
    }
    @NotNull
    private Set<String> getFilterExcludeClassCodes() {
        Set<String> filterExcludeClassCodes = Collections.synchronizedSet(new HashSet<>());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(CompletableFuture.runAsync(() -> filterExcludeClassCodes.addAll(dtpKnowledgeClientService
                        .listClassCodesByLastLevelTag(ObjectTypeEnum.EQUIPMENT, Collections.singletonMap(
                                "equipPosInMajor", Sets.newHashSet("TJJG"))).stream()
                        .map(DtpKnowledgeClassCodeByTagResponse::getClassCode).collect(Collectors.toSet())),
                BusinessThreadPoolExecutor.ttlExecutor()));
        futures.add(CompletableFuture.runAsync(() -> filterExcludeClassCodes.addAll(dtpKnowledgeClientService
                        .listClassCodesByLastLevelTag(ObjectTypeEnum.EQUIPMENT, Collections.singletonMap(
                                "genFluidNetwork", Sets.newHashSet("pipeline"))).stream()
                        .map(DtpKnowledgeClassCodeByTagResponse::getClassCode).collect(Collectors.toSet())),
                BusinessThreadPoolExecutor.ttlExecutor()));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return filterExcludeClassCodes;
    }

    @Override
    public List<? extends DtpRwdSpaceVO> listDashboardSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        if (CollectionUtils.isNotEmpty(param.getWithColumns())) {
            param.addWithColumn(DtpRwdObjectDTO.Fields.id);
            param.addWithColumn(DtpRwdSpaceDTO.Fields.roomFuncType);
        }

        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("Dashboard");
        try (PerformanceTracker.SafeNode ignored = tracker.track("listDashboardSpaces")) {
            List<DtpRwdSpaceDTO> spaces;
            // 查询全量空间不进行空间过滤，spaceTree 模式跳过缓存
            if (param.isSpaceTree()) {
                Set<String> roomFuncTypes = getRoomFuncTypes();
                DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
                queryParam.addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType, roomFuncTypes);
                spaces = param.queryObject(queryParam);
                if (CollectionUtils.isEmpty(spaces)) {
                    return Lists.newArrayList();
                }
                return spaces.stream().map(computerRoomConverter::toDashboardSpaceVo).collect(Collectors.toList());
            }

            // 缓存读取逻辑：当 meosCache 为 true 时尝试从缓存获取
            if (param.isMeosCache()) {
                String spaceCacheKey = CacheRwdComputerRoomSecurityStrategy.SPACE_CACHE_KEY;

                // 白名单过滤：先查白名单，通过 Repository 按 objectIds 直接在数据库层面过滤
                Set<String> whitelistSpaceIds = getWhitelistSpaceIds(param.getRunningMonitorSecondType());

                CacheRwdObjectQueryDaoParam queryDaoParam = new CacheRwdObjectQueryDaoParam();
                queryDaoParam.setCustomIndexKey(spaceCacheKey);
                if (CollectionUtils.isNotEmpty(whitelistSpaceIds)) {
                    // 有白名单配置，按白名单空间ID过滤
                    queryDaoParam.setObjectIds(whitelistSpaceIds);
                }
                queryDaoParam.addColumns(CacheRwdObject::getObjectData);
                //机房界面上的操作做了定制化的改造，其他地方的调用就直接查询
                List<CacheRwdObject> cachedObjects = null;

                if (CollectionUtils.isNotEmpty(cachedObjects)) {
                    logProjectInstance().info("从缓存获取机房安全空间数据",
                            Arrays.asList(Pair.of("key", spaceCacheKey),
                                    Pair.of("whitelistSpaceIds", whitelistSpaceIds)));

                    return cachedObjects.stream()
                            .filter(cacheObj -> cacheObj.getObjectData() != null)
                            .map(cacheObj -> ObjectMapperUtils.readValueFailSafe(cacheObj.getObjectData(), ComputerRoomDashboardSpaceVO.class))
                            .collect(Collectors.toList());
                }

                logProjectInstance().info("从缓存未获取机房安全空间数据",
                        Pair.of("key", spaceCacheKey));
            }
            // 缓存构建路径：查询全量空间，不应用白名单过滤
            if (!param.isMeosCache()) {
                Set<String> roomFuncTypes = getRoomFuncTypes();
                DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
                queryParam.addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType, roomFuncTypes);
                spaces = param.queryObject(queryParam);
            } else {
                // 正常查询路径（缓存未命中），通过 listSpaces 应用白名单过滤
                spaces = this.listSpaces(param);
            }
            tracker.recordStep("查询空间");
            if (CollectionUtils.isEmpty(spaces)) {
                return Lists.newArrayList();
            }
            Set<String> spaceIds = new HashSet<>();
            Set<String> roomFuncTypes = new HashSet<>();
            for (DtpRwdSpaceDTO space : spaces) {
                spaceIds.add(space.getId());
                ObjectExecutor.notBlankExecute(space.getRoomFuncType(), () -> roomFuncTypes.add(space.getRoomFuncType()));
            }
            // 查询空间下设备
            List<ComputerRoomEquipmentVo> equipments = Collections.synchronizedList(new ArrayList<>());
            PerformanceTracker.ParallelNode equipmentNode = tracker.enterParallel("查询空间下设备");
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            futures.add(CompletableFuture.runAsync(() -> {
                try (PerformanceTracker.ParallelScope ignored2 = equipmentNode.openScope()) {
                    RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
                    equipmentQueryParam.setRunningMonitorSecondType(param.getRunningMonitorSecondType());
                    equipmentQueryParam.setFilterEquipmentInSpaceIds(spaceIds);
                    equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.listSimpleWithColumns());
                    equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.relObjIds);
                    equipmentQueryParam.addWithColumn(InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode());
                    equipments.addAll(this.listEquipments(equipmentQueryParam));
                }
            }, BusinessThreadPoolExecutor.ttlExecutor()));
            // 查询空间功能类型名称
            Map<String, String> roomFuncTypeAndName = new HashMap<>();
            if (CollectionUtils.isNotEmpty(roomFuncTypes)) {
                PerformanceTracker.ParallelNode roomFuncTypeNode = tracker.enterParallel("查询空间功能类型名称");
                futures.add(CompletableFuture.runAsync(() -> {
                    try (PerformanceTracker.ParallelScope ignored2 = roomFuncTypeNode.openScope()) {
                        DtpKnowledgeInfoCodeDTO roomFuncTypeDefined = dtpKnowledgeClientService.getInfoCode(false,
                                DtpRwdClassCodeEnum.SPACE.value(), DtpRwdSpaceDTO.Fields.roomFuncType);
                        if (Objects.nonNull(roomFuncTypeDefined)) {
                            roomFuncTypeAndName.putAll(roomFuncTypeDefined.toDataSourceCodeAndName());
                        }
                    }
                }, BusinessThreadPoolExecutor.ttlExecutor()));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            tracker.recordStep("查询空间下设备和辅助信息");
            Map<String, List<ComputerRoomEquipmentVo>> spaceIdAndEquipments =
                    DtpRwdEquipmentDTO.mapRelObjIdAndData(equipments);
            List<ComputerRoomDashboardSpaceVO> result = new ArrayList<>();
            for (DtpRwdSpaceDTO space : spaces) {
                String spaceId = space.getId();
                String roomFuncType = space.getRoomFuncType();
                String roomFuncTypeName = roomFuncTypeAndName.get(roomFuncType);
                List<ComputerRoomEquipmentVo> relEquipments = spaceIdAndEquipments.get(spaceId);

                ComputerRoomDashboardSpaceVO resultItem = computerRoomConverter.toDashboardSpaceVo(space);
                resultItem.setRoomFuncTypeName(roomFuncTypeName);
                resultItem.setEquipmentsInSpace(relEquipments);
                result.add(resultItem);
            }
            tracker.recordStep("组装数据");
            return result;
        }
    }
    public List<? extends DtpRwdSpaceVO> listDashboardSpacesComputerRoomSecurity(@NonNull RunningMonitorSpaceQueryParam param) {
        // 新缓存路径
        if (this.cacheObjectModelStatusService.isReady(RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY)) {
            return listDashboardSpacesComputerRoomSecurityFromCache();
        }

        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("Dashboard");
        try (PerformanceTracker.SafeNode ignored = tracker.track("listDashboardSpaces")) {
            // 正常查询路径，通过 listSpaces 应用白名单过滤
            List<DtpRwdSpaceDTO> spaces = this.listSpacesComputerRoomSecurity(param);
            tracker.recordStep("查询空间");
            if (CollectionUtils.isEmpty(spaces)) {
                return Lists.newArrayList();
            }
            Set<String> spaceIds = new HashSet<>();
            Set<String> roomFuncTypes = new HashSet<>();
            for (DtpRwdSpaceDTO space : spaces) {
                spaceIds.add(space.getId());
                ObjectExecutor.notBlankExecute(space.getRoomFuncType(), () -> roomFuncTypes.add(space.getRoomFuncType()));
            }
            // 查询空间下设备
            List<ComputerRoomEquipmentVo> equipments = Collections.synchronizedList(new ArrayList<>());
            PerformanceTracker.ParallelNode equipmentNode = tracker.enterParallel("查询空间下设备");
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            futures.add(CompletableFuture.runAsync(() -> {
                try (PerformanceTracker.ParallelScope ignored2 = equipmentNode.openScope()) {
                    RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
                    equipmentQueryParam.setRunningMonitorSecondType(param.getRunningMonitorSecondType());
                    equipmentQueryParam.setFilterEquipmentInSpaceIds(spaceIds);
                    equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.listSimpleWithColumns());
                    equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.relObjIds);
                    equipmentQueryParam.addWithColumn(InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode());
                    equipments.addAll(this.listEquipmentsFromRwdComputerRoomSecurity(equipmentQueryParam));
                }
            }, BusinessThreadPoolExecutor.ttlExecutor()));
            // 查询空间功能类型名称
            Map<String, String> roomFuncTypeAndName = new HashMap<>();
            if (CollectionUtils.isNotEmpty(roomFuncTypes)) {
                PerformanceTracker.ParallelNode roomFuncTypeNode = tracker.enterParallel("查询空间功能类型名称");
                futures.add(CompletableFuture.runAsync(() -> {
                    try (PerformanceTracker.ParallelScope ignored2 = roomFuncTypeNode.openScope()) {
                        DtpKnowledgeInfoCodeDTO roomFuncTypeDefined = dtpKnowledgeClientService.getInfoCode(false,
                                DtpRwdClassCodeEnum.SPACE.value(), DtpRwdSpaceDTO.Fields.roomFuncType);
                        if (Objects.nonNull(roomFuncTypeDefined)) {
                            roomFuncTypeAndName.putAll(roomFuncTypeDefined.toDataSourceCodeAndName());
                        }
                    }
                }, BusinessThreadPoolExecutor.ttlExecutor()));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            tracker.recordStep("查询空间下设备和辅助信息");
            Map<String, List<ComputerRoomEquipmentVo>> spaceIdAndEquipments =
                    DtpRwdEquipmentDTO.mapRelObjIdAndData(equipments);
            List<ComputerRoomDashboardSpaceVO> result = new ArrayList<>();
            for (DtpRwdSpaceDTO space : spaces) {
                String spaceId = space.getId();
                String roomFuncType = space.getRoomFuncType();
                String roomFuncTypeName = roomFuncTypeAndName.get(roomFuncType);
                List<ComputerRoomEquipmentVo> relEquipments = spaceIdAndEquipments.get(spaceId);

                ComputerRoomDashboardSpaceVO resultItem = computerRoomConverter.toDashboardSpaceVo(space);
                resultItem.setRoomFuncTypeName(roomFuncTypeName);
                resultItem.setEquipmentsInSpace(relEquipments);
                result.add(resultItem);
            }
            tracker.recordStep("组装数据");
            return result;
        }
    }

    /**
     * 从新缓存读取并组装 Dashboard 空间数据。
     *
     * <p>READY 后从新缓存表读取原始空间和设备 DTO，通过 cache_object_rel 读取
     * 设备-空间关系，实时查询 Knowledge 数据填充名称和等级，在内存中组装 VO。</p>
     *
     * @return 组装后的机房安全 Dashboard 空间 VO 列表
     * @since v7.260802.0
     */
    private @NonNull List<ComputerRoomDashboardSpaceVO> listDashboardSpacesComputerRoomSecurityFromCache() {
        long cacheStart = System.currentTimeMillis();
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("Dashboard-Cache");
        try (PerformanceTracker.SafeNode ignored = tracker.track("listDashboardSpacesFromCache")) {

            // Step 1: 并行加载空间缓存 + roomFuncTypes + 白名单 + 排除类编码 + roomFuncType名称
            // 这些任务彼此无依赖，并行执行以减少串行等待
            Set<String> spaceWithColumns = new LinkedHashSet<>(
                    CacheObjectExtensionFieldConfig.listObjectQueryFields(DtpRwdClassCodeEnum.SPACE.value()));
            spaceWithColumns.add(DtpRwdObjectDTO.Fields.name);

            CompletableFuture<List<DtpRwdSpaceDTO>> spacesFuture = CompletableFuture.supplyAsync(() ->
                    this.cacheObjectRepository.listByCacheKey(
                            CacheObjectModelKeyConfig.COMPUTER_ROOM_SECURITY_LIST_SPACE,
                            DtpRwdSpaceDTO.class, spaceWithColumns),
                    BusinessThreadPoolExecutor.ttlExecutor());
            CompletableFuture<Set<String>> roomFuncTypesFuture = CompletableFuture.supplyAsync(
                    this::getRoomFuncTypes, BusinessThreadPoolExecutor.ttlExecutor());
            CompletableFuture<Set<String>> whitelistFuture = CompletableFuture.supplyAsync(
                    () -> getWhitelistSpaceIds(RunningMonitorSecondTypeEnum.COMPUTER_ROOM_SECURITY),
                    BusinessThreadPoolExecutor.ttlExecutor());
            CompletableFuture<Set<String>> excludeClassCodesFuture = CompletableFuture.supplyAsync(
                    this::getFilterExcludeClassCodes, BusinessThreadPoolExecutor.ttlExecutor());
            CompletableFuture<Map<String, String>> roomFuncTypeNameFuture = CompletableFuture.supplyAsync(() -> {
                DtpKnowledgeInfoCodeDTO roomFuncTypeDefined = this.dtpKnowledgeClientService.getInfoCode(false,
                        DtpRwdClassCodeEnum.SPACE.value(), DtpRwdSpaceDTO.Fields.roomFuncType);
                return Objects.nonNull(roomFuncTypeDefined)
                        ? roomFuncTypeDefined.toDataSourceCodeAndName() : Collections.emptyMap();
            }, BusinessThreadPoolExecutor.ttlExecutor());

            // Step 2: 等待空间缓存和配置数据就绪后执行过滤
            List<DtpRwdSpaceDTO> allSpaces = spacesFuture.join();
            if (CollectionUtils.isEmpty(allSpaces)) {
                return new ArrayList<>();
            }

            Set<String> roomFuncTypes = roomFuncTypesFuture.join();
            List<DtpRwdSpaceDTO> spaces = allSpaces.stream()
                    .filter(s -> roomFuncTypes.contains(s.getRoomFuncType()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(spaces)) {
                return new ArrayList<>();
            }

            Set<String> whitelistSpaceIds = whitelistFuture.join();
            if (CollectionUtils.isNotEmpty(whitelistSpaceIds)) {
                spaces = spaces.stream()
                        .filter(s -> whitelistSpaceIds.contains(s.getId()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(spaces)) {
                    return new ArrayList<>();
                }
            }

            Set<String> spaceIds = spaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
            logProjectInstance().info("[性能] listDashboardSpacesComputerRoomSecurityFromCache-并行加载空间+过滤 耗时: " + (System.currentTimeMillis() - cacheStart) + "ms, 数据量: " + allSpaces.size() + "→" + spaces.size());
            tracker.recordStep("并行加载空间+过滤");

            // Step 3: 先查设备-空间关系，获取关联设备ID（避免全量加载设备）
            CacheObjectRelQueryDaoParam relParam = new CacheObjectRelQueryDaoParam();
            relParam.setRelObjectIds(spaceIds);
            relParam.setGraphCode(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE.value().getLeft());
            relParam.setRelCode(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE.value().getRight());
            List<CacheObjectRel> rels = this.cacheObjectRelRepository.listReverse(relParam);
            logProjectInstance().info("[性能] listDashboardSpacesComputerRoomSecurityFromCache-缓存读取设备关系 耗时: " + (System.currentTimeMillis() - cacheStart) + "ms, 数据量: " + rels.size());

            // Step 4: 按关联设备ID精准加载设备（仅加载实际需要的设备，而非全量设备）
            Set<String> relatedEquipmentIds = rels.stream()
                    .map(CacheObjectRel::getObjectId)
                    .collect(Collectors.toSet());
            Set<String> equipmentWithColumns = new LinkedHashSet<>();
            Collections.addAll(equipmentWithColumns, DtpRwdObjectDTO.listSimpleWithColumns());
            equipmentWithColumns.add(DtpRwdObjectDTO.Fields.relObjIds);
            equipmentWithColumns.add(InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode());
            equipmentWithColumns.add(DtpRwdEquipmentDTO.Fields.bimId);
            equipmentWithColumns.add(DtpRwdEquipmentDTO.Fields.bimLocation);
            List<DtpRwdEquipmentDTO> equipments = CollectionUtils.isEmpty(relatedEquipmentIds)
                    ? Collections.emptyList()
                    : this.cacheObjectRepository.listByObjectIds(
                            relatedEquipmentIds, DtpRwdEquipmentDTO.class, equipmentWithColumns);
            logProjectInstance().info("[性能] listDashboardSpacesComputerRoomSecurityFromCache-精准加载设备 耗时: " + (System.currentTimeMillis() - cacheStart) + "ms, 数据量: " + equipments.size() + "（关联ID: " + relatedEquipmentIds.size() + "）");

            // Step 5: 构建 spaceId → equipment 映射（filterExcludeClassCodes 已在并行阶段获取）
            Set<String> filterExcludeClassCodes = excludeClassCodesFuture.join();
            Map<String, DtpRwdEquipmentDTO> equipMap = equipments.stream()
                    .collect(Collectors.toMap(DtpRwdEquipmentDTO::getId, Function.identity(), (o1, o2) -> o1));
            Map<String, List<DtpRwdEquipmentDTO>> spaceIdAndEquipments = new HashMap<>();
            for (CacheObjectRel rel : rels) {
                String spaceId = rel.getRelObjectId();
                String equipmentId = rel.getObjectId();
                DtpRwdEquipmentDTO equip = equipMap.get(equipmentId);
                if (equip != null && !filterExcludeClassCodes.contains(equip.getClassCode())) {
                    spaceIdAndEquipments.computeIfAbsent(spaceId, k -> new ArrayList<>()).add(equip);
                }
            }
            logProjectInstance().info("[性能] listDashboardSpacesComputerRoomSecurityFromCache-设备+关系映射 耗时: " + (System.currentTimeMillis() - cacheStart) + "ms, 数据量: " + rels.size());
            tracker.recordStep("精准加载设备+关系映射");

            // Step 6: Knowledge 实时填充（roomFuncType名称已在并行阶段获取）
            List<ComputerRoomEquipmentVo> allEquipmentVos = new ArrayList<>();
            for (List<DtpRwdEquipmentDTO> eqList : spaceIdAndEquipments.values()) {
                allEquipmentVos.addAll(this.computerRoomConverter.toEquipmentVo(eqList));
            }

            Map<String, String> classCodeAndName = new ConcurrentHashMap<>();
            Map<String, String> classCodeAndCriticalLevel = new ConcurrentHashMap<>();

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(allEquipmentVos)) {
                Set<String> classCodes = allEquipmentVos.stream()
                        .map(ComputerRoomEquipmentVo::getClassCode).collect(Collectors.toSet());
                futures.add(CompletableFuture.runAsync(() -> classCodeAndName.putAll(
                        this.dtpKnowledgeClientService.mapClassCodeAndName(false, false, classCodes)),
                        BusinessThreadPoolExecutor.ttlExecutor()));
                futures.add(CompletableFuture.runAsync(() -> {
                    DtpKnowledgeClassCodeTagQueryParam tagQueryParam = new DtpKnowledgeClassCodeTagQueryParam();
                    tagQueryParam.setTagNames(Sets.newHashSet(ComputerRoomEquipmentVo.Fields.equipCriticalLevel));
                    tagQueryParam.setObjTypes(Sets.newHashSet(DtpRwdObjectTypeEnum.EQUIPMENT.value()));
                    Map<String, Map<String, DtpKnowledgeClassCodeTagResponse>> classCodeTypeAndTag =
                            this.dtpKnowledgeClientService.mapClassCodeTypeAndTag(tagQueryParam);
                    classCodeTypeAndTag.forEach((classCode, tagAndData) -> {
                        DtpKnowledgeClassCodeTagResponse tagResponse = tagAndData.get(
                                ComputerRoomEquipmentVo.Fields.equipCriticalLevel);
                        if (Objects.nonNull(tagResponse)) {
                            classCodeAndCriticalLevel.put(classCode, tagResponse.getTagValue());
                        }
                    });
                }, BusinessThreadPoolExecutor.ttlExecutor()));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            Map<String, String> roomFuncTypeAndName = roomFuncTypeNameFuture.join();
            logProjectInstance().info("[性能] listDashboardSpacesComputerRoomSecurityFromCache-Knowledge实时填充 耗时: " + (System.currentTimeMillis() - cacheStart) + "ms");
            tracker.recordStep("Knowledge实时填充");

            // 填充设备 Knowledge 数据
            for (ComputerRoomEquipmentVo vo : allEquipmentVos) {
                String classCode = vo.getClassCode();
                vo.setClassCodeName(classCodeAndName.get(classCode));
                vo.setEquipCriticalLevel(classCodeAndCriticalLevel.get(classCode));
            }

            // 重建 spaceId → enriched VO 映射
            Map<String, ComputerRoomEquipmentVo> idToVo = allEquipmentVos.stream()
                    .collect(Collectors.toMap(ComputerRoomEquipmentVo::getId, Function.identity(), (o1, o2) -> o1));
            Map<String, List<ComputerRoomEquipmentVo>> spaceIdAndEquipmentVos = new HashMap<>();
            for (Map.Entry<String, List<DtpRwdEquipmentDTO>> entry : spaceIdAndEquipments.entrySet()) {
                List<ComputerRoomEquipmentVo> vos = entry.getValue().stream()
                        .map(eq -> idToVo.get(eq.getId()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                spaceIdAndEquipmentVos.put(entry.getKey(), vos);
            }

            // Step 7: 组装 ComputerRoomDashboardSpaceVO
            List<ComputerRoomDashboardSpaceVO> result = new ArrayList<>();
            for (DtpRwdSpaceDTO space : spaces) {
                String spaceId = space.getId();
                String roomFuncType = space.getRoomFuncType();
                String roomFuncTypeName = roomFuncTypeAndName.get(roomFuncType);
                List<ComputerRoomEquipmentVo> relEquipments = spaceIdAndEquipmentVos.get(spaceId);

                ComputerRoomDashboardSpaceVO resultItem = this.computerRoomConverter.toDashboardSpaceVo(space);
                resultItem.setRoomFuncTypeName(roomFuncTypeName);
                resultItem.setEquipmentsInSpace(relEquipments);
                result.add(resultItem);
            }
            tracker.recordStep("组装数据");
            logProjectInstance().info("[性能] listDashboardSpacesComputerRoomSecurityFromCache-总耗时: " + (System.currentTimeMillis() - cacheStart) + "ms, 结果数据量: " + result.size());
            return result;
        }
    }

    @Override
    public Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> exceptionFilterCondition() {
        long methodStart = System.currentTimeMillis();
        // 新缓存路径：缓存就绪时走新路径
        long isReadyStart = System.currentTimeMillis();
        boolean isReady = this.cacheObjectModelStatusService.isReady(RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY);
        logProjectInstance().info("[性能] exceptionFilterCondition-isReady检查[串行] 耗时: " + (System.currentTimeMillis() - isReadyStart) + "ms, isReady: " + isReady);
        if (isReady) {
            logProjectInstance().info("[性能] exceptionFilterCondition-构建闭包总耗时(缓存路径) 耗时: " + (System.currentTimeMillis() - methodStart) + "ms");
            return exceptionFilterConditionNewFromCache();
        }
        long getExcludeStart = System.currentTimeMillis();
        Set<String> filterExcludeClassCodes = getFilterExcludeClassCodes();
        logProjectInstance().info("[性能] exceptionFilterCondition-getFilterExcludeClassCodes[串行] 耗时: " + (System.currentTimeMillis() - getExcludeStart) + "ms, 排除类型数量: " + filterExcludeClassCodes.size());
        logProjectInstance().info("[性能] exceptionFilterCondition-构建闭包总耗时(BDTP路径) 耗时: " + (System.currentTimeMillis() - methodStart) + "ms");
        return items -> {
            if (CollectionUtils.isEmpty(items)) {
                return items;
            }
            List<ExceptionEventBaseInfoVO> events = items.stream()
                    .filter(item -> StringUtils.equalsAny(item.getObjectType(),
                            ObjectTypeEnum.SPACE.value(), ObjectTypeEnum.EQUIPMENT.value()))
                    .collect(Collectors.toList());
            Set<String> equipmentIds = events.stream()
                    .filter(item -> StringUtils.equals(item.getObjectType(), DtpRwdObjectTypeEnum.EQUIPMENT.value()))
                    .map(ExceptionEventBaseInfoVO::getId)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(equipmentIds)) {
                long queryDoorsStart = System.currentTimeMillis();
                List<DtpRwdObjectDTO> allowDoors = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                        .setId(equipmentIds)
                        .setClassCodeEnum(Lists.newArrayList(DtpRwdClassCodeEnum.CFCSDR,
                                DtpRwdClassCodeEnum.CFCSFR, DtpRwdClassCodeEnum.SPEGFG))
                        .addWithColumn(DtpRwdObjectDTO.Fields.id)
                        .addWithColumnByMonitoringSurveillanceStatus());
                Map<String, Boolean> doorIdAndControlled = allowDoors.stream().collect(Collectors.toMap(
                        DtpRwdObjectDTO::getId, DtpRwdObjectDTO::isControlled));
                logProjectInstance().info("[性能] exceptionFilterCondition-查询门禁设备(BDTP)[串行] 耗时: " + (System.currentTimeMillis() - queryDoorsStart) + "ms, 数据量: " + allowDoors.size());
                long queryExcludeStart = System.currentTimeMillis();
                DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam().setId(equipmentIds).setClassCode(filterExcludeClassCodes).addWithColumn(DtpRwdObjectDTO.Fields.id);
                Set<String> excludeEquipmentIds = dtpRwdClientService.listEquipment(param).stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
                logProjectInstance().info("[性能] exceptionFilterCondition-查询排除设备(BDTP)[串行] 耗时: " + (System.currentTimeMillis() - queryExcludeStart) + "ms, 数据量: " + excludeEquipmentIds.size());
                // 先过滤排除的设备
                events.removeIf(item -> excludeEquipmentIds.contains(item.getId()));
                // 再过滤非控制
                events.removeIf(item -> {
                    Boolean controlled = doorIdAndControlled.get(item.getId());
                    if (Objects.isNull(controlled)) {
                        return false;
                    }
                    return !controlled;
                });
            }
            return events;
        };
    }


    /**
     * 从新缓存执行异常事件过滤（API 4）。
     *
     * <p>通过 CacheObjectRepository.listByObjectIds 按设备 ID 批量查缓存数据，
     * 内存中按 classCode 分别过滤门禁设备和排除类型设备，替代 2 次 BDTP Feign。
     * getFilterExcludeClassCodes 的 Knowledge Feign 保留。</p>
     *
     * @return 过滤函数
     * @since v7.260802.0
     */
    private Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> exceptionFilterConditionNewFromCache() {
        Set<String> filterExcludeClassCodes = getFilterExcludeClassCodes();
        return items -> {
            if (CollectionUtils.isEmpty(items)) {
                return items;
            }
            List<ExceptionEventBaseInfoVO> events = items.stream()
                    .filter(item -> StringUtils.equalsAny(item.getObjectType(),
                            ObjectTypeEnum.SPACE.value(), ObjectTypeEnum.EQUIPMENT.value()))
                    .collect(Collectors.toList());
            Set<String> equipmentIds = events.stream()
                    .filter(item -> StringUtils.equals(item.getObjectType(), DtpRwdObjectTypeEnum.EQUIPMENT.value()))
                    .map(ExceptionEventBaseInfoVO::getId)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(equipmentIds)) {
                // 从缓存一次查询替代 2 次 BDTP Feign
                long queryCacheStart = System.currentTimeMillis();
                List<DtpRwdObjectDTO> cachedEquipments = this.cacheObjectRepository.listByObjectIds(
                        equipmentIds, Arrays.asList(
                                DtpRwdObjectDTO.Fields.id,
                                DtpRwdObjectDTO.Fields.classCode,
                                InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value()));
                logProjectInstance().info("[性能] exceptionFilterConditionNewFromCache-查询缓存设备[串行] 耗时: " + (System.currentTimeMillis() - queryCacheStart) + "ms, 数据量: " + cachedEquipments.size());

                // 门禁受控过滤：从缓存结果中按 classCode 过滤门禁设备
                Map<String, Boolean> doorIdAndControlled = cachedEquipments.stream()
                        .filter(eq -> StringUtils.equalsAny(eq.getClassCode(),
                                DtpRwdClassCodeEnum.CFCSDR.value(),
                                DtpRwdClassCodeEnum.CFCSFR.value(),
                                DtpRwdClassCodeEnum.SPEGFG.value()))
                        .collect(Collectors.toMap(DtpRwdObjectDTO::getId, DtpRwdObjectDTO::isControlled));

                // 排除类型过滤：从缓存结果中按 classCode 过滤排除设备
                Set<String> excludeEquipmentIds = cachedEquipments.stream()
                        .filter(eq -> filterExcludeClassCodes.contains(eq.getClassCode()))
                        .map(DtpRwdObjectDTO::getId)
                        .collect(Collectors.toSet());

                // 先过滤排除的设备
                events.removeIf(item -> excludeEquipmentIds.contains(item.getId()));
                // 再过滤非控制
                events.removeIf(item -> {
                    Boolean controlled = doorIdAndControlled.get(item.getId());
                    if (Objects.isNull(controlled)) {
                        return false;
                    }
                    return !controlled;
                });
            }
            return events;
        };
    }
}
