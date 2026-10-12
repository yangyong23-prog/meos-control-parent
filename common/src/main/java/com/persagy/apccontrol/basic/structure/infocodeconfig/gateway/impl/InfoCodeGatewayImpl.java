package com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdTwinControlObjectDTO;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeObjectClassCodeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeTemplateTypeResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheRwdDomainService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.context.InfoCodeAggregateInitContext;
import com.persagy.apccontrol.basic.structure.infocodeconfig.context.InfoCodeIotDataContext;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.lifecycle.InfoCodeAggregateLifeAware;
import com.persagy.apccontrol.basic.structure.infocodeconfig.lifecycle.InfoCodeAggregateLifeStaticAware;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeFilterValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.knowledge.converter.DtpKnowledgeConverter;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 信息点网关实现
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
public class InfoCodeGatewayImpl implements InfoCodeGateway {

    private static final Comparator<InfoCodeTemplateTypeModel> TEMPLATE_TYPE_MODEL_COMPARATOR = Comparator.comparing(
            InfoCodeTemplateTypeModel::getSequence,
            Comparator.nullsFirst(Comparator.naturalOrder()));

    private final DtpKnowledgeConverter dtpKnowledgeConverter;
    private final InfoCodeConverter infoCodeConverter;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final DtpRwdClientService dtpRwdClientService;
    private final InfoCodeTemplateTypeResource infoCodeTemplateTypeResource;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final AbstractCommonServer abstractCommonServer;
    private final ObjectProvider<InfoCodeAggregateLifeAware> infoCodeAggregateLifeAware;
    private final RedisCacheTemplate redisCacheTemplate;
    private final CacheRwdDomainService cacheRwdDomainService;

    /**
     * 缓存对象模型共享 READY 状态服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService cacheObjectModelStatusService;

    /**
     * 新缓存对象仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRepository cacheObjectRepository;

    @Override
    public List<InfoCodeAggregate> of(@NonNull InfoCodeInitByObjectParam param) {
        param.checkAndSet();
        List<InfoCodeAggregateLifeAware> awares = infoCodeAggregateLifeAware.orderedStream()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(awares)) {
            awares.add(InfoCodeAggregateLifeStaticAware.INSTANCE);
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("创建信息点聚合根");
        try (PerformanceTracker.SafeNode safeNode = tracker.track("InfoCodeGatewayImpl#of")) {
            awares.forEach(item -> item.beforeContextInit(param));
            InfoCodeAggregateInitContext context = new InfoCodeAggregateInitContext(param, awares);
            context.fillTemplateTypes();
            safeNode.recordStep("填充模版类型模型");
            if (context.isFailed()) {
                return Lists.newArrayList();
            }
            context.fillInfoCodeGroupConfigFilters();
            safeNode.recordStep("填充分组信息点过滤配置");
            if (context.isFailed()) {
                return Lists.newArrayList();
            }
            context.fillInfoCodeConfigs();
            safeNode.recordStep("填充信息点配置模型");
            if (context.isFailed()) {
                return Lists.newArrayList();
            }
            context.fillGroupConfigs();
            safeNode.recordStep("填充分组模型");
            if (context.isFailed()) {
                return Lists.newArrayList();
            }
            awares.forEach(item -> item.handleDatabaseData(context));
            safeNode.recordStep("执行DB数据处理");
            if (context.isFailed()) {
                return Lists.newArrayList();
            }
            if (BooleanUtils.isNotFalse(param.getQueryIotInfoCodeRule())) {
                context.fillIotInfoCodeRule();
                safeNode.recordStep("填充IOT信息点规则");
            }
            awares.forEach(item -> item.afterContextInit(context));
            safeNode.recordStep("执行上下文后置处理");
            if (context.isFailed()) {
                return Lists.newArrayList();
            }
            List<InfoCodeAggregate> aggregates = context.toAggregates();
            safeNode.recordStep("转换聚合根");
            awares.forEach(item -> item.afterAggregateInit(aggregates));
            safeNode.recordStep("聚合根后置处理");
            if (BooleanUtils.isTrue(param.getFillInfoCodeDefineModel())) {
                this.fillInfoCodeDefineModel(aggregates);
                safeNode.recordStep("填充信息点定义模型");
            }
            if (BooleanUtils.isTrue(param.getFillInfoCodeDataModel())) {
                this.fillInfoCodeDataModel(aggregates, param.getPlaceholders());
                safeNode.recordStep("填充信息点数据模型");
            }
            return aggregates;
        }
    }

    @Override
    public List<InfoCodeAggregate> ofOrigin(@NonNull InfoCodeInitByObjectParam param) {
        InfoCodeInitByObjectParam copyParam = this.infoCodeConverter.copy(param);
        copyParam.setForceMathTemplate(true);
        copyParam.setNotFilterIotInfoCodeRule(false);
        return this.of(copyParam);
    }

    @Override
    public int countObject(@NonNull InfoCodeObjectQueryParam param) {
        return mapTemplateTypeAndObjectCount(param).values().stream().reduce(Integer::sum).orElse(-1);
    }

    @Override
    public List<DtpRwdObjectDTO> listObjectByIds(@NonNull Collection<String> objectIds) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return Collections.emptyList();
        }
        return dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                .setId(objectIds).addWithColumn(InfoCodeGateway.listObjectRequiredColumns()));
    }

    @Override
    public List<InfoCodeTemplateTypeModel> listTemplateTypes(@NonNull InfoCodeTemplateTypeQueryDaoParam param) {
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询模版类型");
        try (PerformanceTracker.SafeNode safeNode = tracker.track("InfoCodeGatewayImpl#listTemplateTypes")) {
            List<InfoCodeTemplateType> templateTypes = infoCodeTemplateTypeResource.list(param);
            safeNode.recordStep("查询模版类型配置");
            List<InfoCodeTemplateTypeModel> result = templateTypes.stream()
                    .map(infoCodeConverter::toTemplateTypeModel)
                    .sorted(TEMPLATE_TYPE_MODEL_COMPARATOR)
                    .collect(Collectors.toList());
            safeNode.recordStep("转换模版类型模型");
            return result;
        }
    }

    @Override
    public List<InfoCodeTemplateTypeModel> listTemplateTypesWithObjects(
            @NonNull InfoCodeObjectQueryParam param) {
        return mapTemplateTypeAndObjects(param).entrySet().stream().map(item -> {
            item.getKey().setObjects(item.getValue());
            return item.getKey();
        }).collect(Collectors.toList());
    }

    @Override
    public List<DtpRwdObjectDTO> listTemplateTypeObjects(@NonNull InfoCodeObjectQueryParam param) {
        return mapTemplateTypeAndObjects(param).values().stream()
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public Map<InfoCodeTemplateTypeModel, Integer> mapTemplateTypeAndObjectCount(
            @NonNull InfoCodeObjectQueryParam param) {
        Map<InfoCodeTemplateTypeModel, Integer> result = new HashMap<>();
        this.mapTemplateTypeAndObjects(param)
                .forEach((templateTypeModel, objects) -> result.put(templateTypeModel, objects.size()));
        return result;
    }

    @Override
    public Map<InfoCodeTemplateTypeModel, List<DtpRwdObjectDTO>> mapTemplateTypeAndObjects(
            @NonNull InfoCodeObjectQueryParam param) {
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询模版关联对象");
        try (PerformanceTracker.SafeNode safeNode = tracker.track("InfoCodeGatewayImpl#mapTemplateTypeAndObjects")) {
            Boolean filterControlObject = param.getFilterControlObject();
            Collection<InfoCodeTemplateTypeModel> templateTypeModels = param.getTemplateTypeModels();
            if (CollectionUtils.isEmpty(templateTypeModels)) {
                InfoCodeTemplateTypeQueryDaoParam templateTypeQueryParam = infoCodeConverter.toTemplateQueryDaoParam(param);
                List<InfoCodeTemplateType> templateTypes = infoCodeTemplateTypeResource.list(templateTypeQueryParam);
                safeNode.recordStep("查询模版类型配置");
                if (CollectionUtils.isEmpty(templateTypes)) {
                    return Collections.emptyMap();
                }
                templateTypeModels = infoCodeConverter.toTemplateTypeModel(templateTypes);
                safeNode.recordStep("转换模版类型模型");
            }
            if (CollectionUtils.isEmpty(templateTypeModels)) {
                return Collections.emptyMap();
            }
            boolean deprecatedVersion = LocalContextStorage.isDeprecatedVersion();
            // 一次请求可能包含同一一级类型的多个模板；READY 按一级类型缓存本次判断结果，
            // 避免模板数量放大 Redis 状态查询次数。
            Map<RunningMonitorTypeEnum, Boolean> readyByRunningMonitorType =
                    new EnumMap<>(RunningMonitorTypeEnum.class);
            Set<Long> newModelTemplateTypeIds = new HashSet<>();
            Set<String> newModelCacheKeys = new LinkedHashSet<>();
            for (InfoCodeTemplateTypeModel templateTypeModel : templateTypeModels) {
                if (!deprecatedVersion
                        && templateTypeModel.allowCache()
                        && readyByRunningMonitorType.computeIfAbsent(
                                templateTypeModel.getRunningMonitorType(),
                                this.cacheObjectModelStatusService::isReady)) {
                    newModelTemplateTypeIds.add(templateTypeModel.getTemplateTypeId());
                    newModelCacheKeys.add(templateTypeModel.getTemplateTypeKey());
                }
            }
            if (MapUtils.isNotEmpty(readyByRunningMonitorType)) {
                safeNode.recordStep("查询缓存模型状态");
            }
            // 新模型模板按本次缓存键集合批量读取，后续每个模板直接使用该 Key 实际查询到的对象。
            final Map<String, List<DtpRwdObjectDTO>> newModelObjectsByCacheKey;
            if (CollectionUtils.isEmpty(newModelCacheKeys)) {
                newModelObjectsByCacheKey = Collections.emptyMap();
            } else {
                newModelObjectsByCacheKey = this.cacheObjectRepository.mapByCacheKeys(
                        newModelCacheKeys,
                        Arrays.asList(InfoCodeGateway.listObjectRequiredColumns()));
                safeNode.recordStep("批量查询新缓存对象");
            }
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (InfoCodeTemplateTypeModel templateTypeModel : templateTypeModels) {
                PerformanceTracker.ParallelNode pNode = tracker.enterParallel(
                        String.format("mapTemplateTypeAndObjects[%s]", templateTypeModel.getTemplateTypeKey()));
                futures.add(CompletableFuture.runAsync(() -> {
                    try (PerformanceTracker.ParallelScope ignore = pNode.openScope()) {
                        // 新模型一旦被选中，空结果也不在同一请求中回退旧链路。
                        List<DtpRwdObjectDTO> objects;
                        boolean useNewModel = newModelTemplateTypeIds.contains(
                                templateTypeModel.getTemplateTypeId());
                        if (useNewModel) {
                            objects = new ArrayList<>(newModelObjectsByCacheKey.getOrDefault(
                                    templateTypeModel.getTemplateTypeKey(), Collections.emptyList()));
                            pNode.recordStep("读取新对象模型结果");
                        } else {
                            objects = Collections.emptyList();
                            // 其他监测系统保持旧 MySQL 缓存；末端、照明未 READY 时直接进入
                            // 下方实时查询及其既有 Redis 短缓存。
                            if (templateTypeModel.allowCache() && !deprecatedVersion
                                    && !this.cacheObjectModelStatusService.isSupported(
                                            templateTypeModel.getRunningMonitorType())) {
                                objects = this.cacheRwdDomainService
                                        .listObjects(templateTypeModel.getTemplateTypeKey());
                                pNode.recordStep("查询MySQL旧缓存对象");
                            }
                        }
                        if (!useNewModel && CollectionUtils.isEmpty(objects)) {
                            // 新老版本使用独立的缓存 key, 避免 withColumns 差异导致字段互相污染
                            String cacheKey = deprecatedVersion
                                    ? CACHE_KEY_TEMPLATE_OBJECT_DEPRECATED
                                    : CACHE_KEY_TEMPLATE_OBJECT;
                            objects = redisCacheTemplate.asyncRefreshTemplate(
                                    cacheKey,
                                    templateTypeModel.getTemplateTypeKey(),
                                    () -> {
                                        try (PerformanceTracker.SafeNode rwdNode = tracker.track(
                                                "InfoCodeTemplateTypeModel#queryTemplateObjects")) {
                                            List<DtpRwdObjectDTO> rwdObjects =
                                                    templateTypeModel.queryTemplateObjects();
                                            rwdNode.recordStep("查询RWD实时模版对象");
                                            return rwdObjects;
                                        }
                                    });
                            pNode.recordStep("查询Redis模版对象");
                        }
                        // 过滤管控对象
                        if (BooleanUtils.isTrue(filterControlObject)) {
                            this.filterControlObjects(objects);
                            pNode.recordStep("过滤管控对象");
                        }
                        // 根据入参在内存过滤参数
                        objects = param.filterObjects(objects);
                        pNode.recordStep("按入参过滤对象");
                        templateTypeModel.setObjects(objects);
                    }
                }, BusinessThreadPoolExecutor.ttlExecutor()));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            safeNode.recordStep("并发处理模版对象");
            Map<InfoCodeTemplateTypeModel, List<DtpRwdObjectDTO>> result = templateTypeModels.stream()
                    .sorted(TEMPLATE_TYPE_MODEL_COMPARATOR)
                    .collect(Collectors.toMap(
                            Function.identity(), InfoCodeTemplateTypeModel::getObjects,
                            (o1, o2) -> o1, LinkedHashMap::new));
            safeNode.recordStep("组装模版对象结果");
            return result;
        }
    }

    @Override
    public void fillInfoCodeTemplateTypeModel(@NonNull Collection<InfoCodeTemplateTypeModel> templateTypeModels) {
        if (CollectionUtils.isEmpty(templateTypeModels)) {
            return;
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("填充信息点模版类型模型");
        try (PerformanceTracker.SafeNode safeNode =
                     tracker.track("InfoCodeGatewayImpl#fillInfoCodeTemplateTypeModel")) {
            Set<String> classCodes = new HashSet<>();
            Set<String> hasInfoCodeClassCodes = new HashSet<>();
            for (InfoCodeTemplateTypeModel templateTypeModel : templateTypeModels) {
                String infoCodeFilter = templateTypeModel.getInfoCodeFilter();
                if (StringUtils.isNotBlank(infoCodeFilter)) {
                    hasInfoCodeClassCodes.add(templateTypeModel.getClassCode());
                }
                classCodes.add(templateTypeModel.getClassCode());
            }
            // 查询数据字典设备类定义
            Map<String, DtpKnowledgeObjectClassCodeResponse> classCodeAndDefine =
                    dtpKnowledgeClientService.listClassCodes(false, true, classCodes).stream()
                            .collect(Collectors.toMap(
                                    DtpKnowledgeObjectClassCodeResponse::getCode,
                                    Function.identity(), (o1, o2) -> o1));
            safeNode.recordStep("查询知识库设备类定义");
            Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> classCodeInfoCodeAndDefine = Collections.emptyMap();
            if (CollectionUtils.isNotEmpty(hasInfoCodeClassCodes)) {
                classCodeInfoCodeAndDefine = dtpKnowledgeClientService.mapClassCodeInfoCodeAndDefine(
                        false, true, hasInfoCodeClassCodes);
                safeNode.recordStep("查询知识库信息点定义");
            }
            for (InfoCodeTemplateTypeModel templateTypeModel : templateTypeModels) {
                String classCode = templateTypeModel.getClassCode();
                InfoCodeFilterValueObject filterValueObject = templateTypeModel.parseFilterValueObject();
                Map<String, Object> infoCodeQueryFilters = templateTypeModel.mapInfoCodeQueryFilters();
                Map<String, Object> infoCodeFilters = new LinkedHashMap<>();
                List<InfoCodeDefineModel> infoCodeDefines = new ArrayList<>();

                DtpKnowledgeObjectClassCodeResponse classCodeDefine = classCodeAndDefine.get(classCode);
                StringJoiner templateNameJoiner = new StringJoiner("-");
                String templateTypeName = null;
                templateNameJoiner.add(classCodeDefine.getName());
                if (MapUtils.isNotEmpty(infoCodeQueryFilters)) {
                    Set<String> allowedInfoCodes = infoCodeQueryFilters.keySet();
                    if (Objects.nonNull(filterValueObject)) {
                        Set<String> showInfoCodesWhitelistInName = filterValueObject.getShowInfoCodesWhitelistInName();
                        templateTypeName = filterValueObject.getCustomName();
                        // 存在信息点白名单时, 将不在白名单中的信息点移除
                        if (CollectionUtils.isEmpty(showInfoCodesWhitelistInName)) {
                            allowedInfoCodes.clear();
                        } else {
                            allowedInfoCodes.removeIf(infoCode -> !showInfoCodesWhitelistInName.contains(infoCode));
                        }
                    }
                    // 移除不需要拼接的信息点
                    if (CollectionUtils.isNotEmpty(allowedInfoCodes)) {
                        Map<String, DtpKnowledgeInfoCodeDTO> infoCodeAndDefine =
                                classCodeInfoCodeAndDefine.get(classCode);
                        for (String infoCode : allowedInfoCodes) {
                            Object infoValue = infoCodeQueryFilters.get(infoCode);
                            InfoCodeDefineModel infoCodeDefineModel = dtpKnowledgeConverter
                                    .toInfoCodeDefineModel(infoCodeAndDefine.get(infoCode));
                            // 查询不到信息点定义时跳过
                            if (Objects.isNull(infoCodeDefineModel)) {
                                continue;
                            }
                            infoCodeFilters.put(infoCode, infoValue);
                            infoCodeDefines.add(infoCodeDefineModel);
                            List<InfoCodeDataSourceDefineModel> dataSourceDefineModels = infoCodeDefineModel
                                    .getDataSources();
                            // 信息点数据源为空时跳过
                            if (CollectionUtils.isEmpty(dataSourceDefineModels)) {
                                continue;
                            }
                            Map<String, InfoCodeDataSourceDefineModel> codeAndSource = dataSourceDefineModels.stream()
                                    .collect(Collectors.toMap(
                                            InfoCodeDataSourceDefineModel::getCode,
                                            Function.identity(), (o1, o2) -> o1));
                            // 将信息点值解析为名称
                            List<InfoCodeDataSourceDefineModel> dataSources = new ArrayList<>();
                            if (infoValue instanceof Collection) {
                                ((Collection<?>) infoValue).forEach(item -> Optional.ofNullable(
                                        codeAndSource.get(String.valueOf(item))).ifPresent(dataSources::add));
                            } else {
                                Optional.ofNullable(codeAndSource.get(String.valueOf(infoValue)))
                                        .ifPresent(dataSources::add);
                            }
                            for (InfoCodeDataSourceDefineModel dataSource : dataSources) {
                                templateNameJoiner.add(dataSource.getName());
                            }
                        }
                    }
                }
                if (StringUtils.isBlank(templateTypeName)) {
                    templateTypeName = templateNameJoiner.toString();
                }
                templateTypeModel.setClassCodeName(classCodeDefine.getName());
                templateTypeModel.setInfoCodeFilterData(infoCodeFilters);
                templateTypeModel.setInfoCodeDefines(infoCodeDefines);
                templateTypeModel.setTemplateTypeName(templateTypeName);
            }
            safeNode.recordStep("填充模版类型模型");
        }
    }

    @Override
    public void fillInfoCodeDefineModel(@NonNull Collection<InfoCodeAggregate> aggregates) {
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("填充信息点定义模型");
        try (PerformanceTracker.SafeNode safeNode = tracker.track("InfoCodeGatewayImpl#fillInfoCodeDefineModel")) {
            infoCodeAggregateLifeAware.forEach(item -> item.beforeFillInfoCodeDefineModel(aggregates));
            Map<InfoCodeTypeEnum, List<InfoCodeAggregate>> infoCodeTypeAndAggregates = aggregates.stream()
                    .collect(Collectors.groupingBy(item -> item.getConfigModel().getInfoCodeType()));
            Map<String, Object> additionalDefineValues = mapAdditionalDefineValues(aggregates);
            safeNode.recordStep("预查信息点定义");
            for (Map.Entry<InfoCodeTypeEnum, List<InfoCodeAggregate>> entry : infoCodeTypeAndAggregates.entrySet()) {
                InfoCodeTypeEnum infoCodeType = entry.getKey();
                InfoCodeAnalyzeStrategy strategy = infoCodeType.getAnalyzeStrategy();
                strategy.fillInfoCodeDefineModel(entry.getValue(), additionalDefineValues);
                safeNode.recordStep(String.format("填充信息点定义类型 [%s]", infoCodeType));
            }
            infoCodeAggregateLifeAware.forEach(item -> item.afterFillInfoCodeDefineModel(aggregates));
            safeNode.recordStep("后置处理");
        }
    }

    @Override
    public void fillInfoCodeDataModel(@NonNull Collection<InfoCodeAggregate> aggregates,
                                      @Nullable Map<String, Object> additionalDataValues) {
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("填充信息点值模型");
        try (PerformanceTracker.SafeNode safeNode = tracker.track("InfoCodeGatewayImpl#fillInfoCodeDataModel")) {
            infoCodeAggregateLifeAware.forEach(item -> item.beforeFillInfoCodeDataModel(aggregates));
            Map<InfoCodeTypeEnum, List<InfoCodeAggregate>> infoCodeTypeAndAggregates = aggregates.stream()
                    .collect(Collectors.groupingBy(item -> item.getConfigModel().getInfoCodeType()));
            // 调用方可能传入 singletonMap 等只读实现，复制后再合并，避免修改调用方集合。
            Map<String, Object> mergedAdditionalDataValues = new HashMap<>();
            if (MapUtils.isNotEmpty(additionalDataValues)) {
                mergedAdditionalDataValues.putAll(additionalDataValues);
            }
            // 保持原有覆盖顺序：网关计算出的扩展数据覆盖同名入参值。
            mergedAdditionalDataValues.putAll(this.mapAdditionalDataValues(aggregates));
            safeNode.recordStep("获取拓展数据值");
            Map<String, Object> finalAdditionalDataValues = mergedAdditionalDataValues;
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Map.Entry<InfoCodeTypeEnum, List<InfoCodeAggregate>> entry : infoCodeTypeAndAggregates.entrySet()) {
                InfoCodeTypeEnum infoCodeType = entry.getKey();
                InfoCodeAnalyzeStrategy strategy = infoCodeType.getAnalyzeStrategy();
                PerformanceTracker.ParallelNode pNode = tracker.enterParallel(
                        String.format("fillInfoCodeDataModel[%s]", infoCodeType));
                futures.add(CompletableFuture.runAsync(() -> {
                    try (PerformanceTracker.ParallelScope parallelScope = pNode.openScope()) {
                        strategy.fillInfoCodeDataModel(entry.getValue(), finalAdditionalDataValues);
                    }
                }, BusinessThreadPoolExecutor.ttlExecutor()));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            safeNode.recordStep("并发填充信息点值模型");
            infoCodeAggregateLifeAware.forEach(item -> item.afterFillInfoCodeDataModel(aggregates));
            safeNode.recordStep("后置处理");
        }
    }

    @Override
    public void filterControlObjects(@NonNull List<DtpRwdObjectDTO> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return;
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("过滤管控对象");
        try (PerformanceTracker.SafeNode safeNode = tracker.enter("InfoCodeGatewayImpl#filterControlObjects")) {
            String projectId = ContextUtil.getProjectId();
            Set<String> allClassCodes = objects.stream().map(DtpRwdObjectDTO::getClassCode).collect(Collectors.toSet());
            List<DtpRwdTwinControlObjectDTO> controlObjects = dtpRwdClientService
                    .listTwinControlObjects(false, projectId, allClassCodes);
            safeNode.recordStep("查询孪生控制对象");
            // 移除非管控对象
            Set<String> controlObjectIds = controlObjects.stream()
                    .map(DtpRwdTwinControlObjectDTO::getObjectId)
                    .collect(Collectors.toSet());
            objects.removeIf(item -> !controlObjectIds.contains(item.getId()));
            if (CollectionUtils.isEmpty(objects)) {
                logProjectInstance().warn("所有对象均为非管控对象");
            }
        }
    }

    /**
     * 获取拓展定义值
     *
     * @param aggregates 聚合根
     * @return 拓展定义值
     * @since 5.241101.0
     */
    private Map<String, Object> mapAdditionalDefineValues(
            @NonNull Collection<InfoCodeAggregate> aggregates) {
        Map<String, Object> additionalMapValues = new HashMap<>();
        additionalMapValues.put(BaseInfoCodeAnalyzeStrategy.ADDITIONAL_KEY_KNOWLEDGE_DEFINE,
                queryClassCodeInfoCodeAndDefine(aggregates));
        return additionalMapValues;
    }

    /**
     * 获取拓展数据值
     *
     * @param aggregates 聚合根
     * @return 拓展数据值
     * @since 5.241101.0
     */
    private Map<String, Object> mapAdditionalDataValues(
            @NonNull Collection<InfoCodeAggregate> aggregates) {
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("获取拓展数据值");
        Map<String, Object> additionalMapValues = new ConcurrentHashMap<>();
        PerformanceTracker.ParallelNode iotNode = tracker.enterParallel("mapAdditionalDataValues[预查IOT数据]");
        CompletableFuture<Void> iotFuture = CompletableFuture.runAsync(() -> {
            try (PerformanceTracker.ParallelScope iotScope = iotNode.openScope()) {
                additionalMapValues.put(
                        BaseInfoCodeAnalyzeStrategy.ADDITIONAL_KEY_IOT_DATA_CONTEXT, queryIotDataContext(aggregates));
            }
        }, BusinessThreadPoolExecutor.ttlExecutor());
        PerformanceTracker.ParallelNode riskNode = tracker.enterParallel("mapAdditionalDataValues[预查异常事件数据]");
        CompletableFuture<Void> riskFuture = CompletableFuture.runAsync(() -> {
            try (PerformanceTracker.ParallelScope riskScope = riskNode.openScope()) {
                additionalMapValues.put(
                        BaseInfoCodeAnalyzeStrategy.ADDITIONAL_KEY_RISK_EVENT_DETAIL,
                        queryObjectIdAndRiskEvents(aggregates));
            }
        }, BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture.allOf(iotFuture, riskFuture).join();
        return additionalMapValues;
    }

    /**
     * 查询数据字典设备类信息点和定义映射
     *
     * @param aggregates 聚合根
     * @return 类型编码 -> [信息点 -> 信息点定义]
     * @since 5.241101.0
     */
    private MultiKeyMap<String, InfoCodeDefineModel> queryClassCodeInfoCodeAndDefine(
            @NonNull Collection<InfoCodeAggregate> aggregates) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return new MultiKeyMap<>();
        }
        List<InfoCodeTypeEnum> demandTargetInfoCodeTypes = Lists.newArrayList(
                InfoCodeTypeEnum.DEMAND_TARGET,
                InfoCodeTypeEnum.DEMAND_TARGET_STATISTIC);
        Set<String> classCodes = aggregates.stream()
                .filter(item -> item.getConfigModel().getInfoCodeType().isKnowledgeInfoCode()
                        || demandTargetInfoCodeTypes.contains(item.getConfigModel().getInfoCodeType()))
                .map(item -> item.getIdentifier().getObject().getClassCode())
                .collect(Collectors.toSet());
        // 查询数据字段信息点定义
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询知识库信息点定义");
        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> classCodeInfoCodeAndDefine;
        try (PerformanceTracker.SafeNode safeNode =
                     tracker.track("DtpKnowledgeClientService#mapClassCodeInfoCodeAndDefine")) {
            classCodeInfoCodeAndDefine = dtpKnowledgeClientService
                    .mapClassCodeInfoCodeAndDefine(false, true, classCodes);
            safeNode.recordStep("查询知识库信息点定义");
        }
        MultiKeyMap<String, InfoCodeDefineModel> result = new MultiKeyMap<>();
        classCodeInfoCodeAndDefine.forEach((classCode, infoCodeAndDefine) -> infoCodeAndDefine
                .forEach((infoCode, define) -> result.put(new MultiKey<>(classCode, infoCode),
                        dtpKnowledgeConverter.toInfoCodeDefineModel(define))));
        return result;
    }

    /**
     * 查询IOT数据上下文
     *
     * @param aggregates 聚合根
     * @return 查询IOT数据上下文
     * @since 5.241101.0
     */
    @SuppressWarnings("unchecked")
    private InfoCodeIotDataContext queryIotDataContext(@NonNull Collection<InfoCodeAggregate> aggregates) {
        InfoCodeIotDataContext context = new InfoCodeIotDataContext(aggregates);
        if (CollectionUtils.isEmpty(aggregates)) {
            return context;
        }
        aggregates = aggregates.stream()
                .filter(item -> item.getConfigModel().getInfoCodeType().isIotInfoCode()
                        && item.getConfigModel().analyzeTackValueLogicModel().isExecute()
                        && !Boolean.FALSE.equals(item.getConfigModel().getExistsIotRule())
                )
                .collect(Collectors.toList());
        Set<String> allObjectIds = new HashSet<>();
        Set<String> allIotInfoCodes = new HashSet<>();
        aggregates.forEach(item -> {
            allObjectIds.add(item.getIdentifier().getObject().getId());
            allIotInfoCodes.add(item.getConfigModel().getInfoCode());
        });
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询IOT数据上下文");
        List<InfoCodeValueVO> infoCodeValues;
        try (PerformanceTracker.SafeNode safeNode =
                     tracker.track("AbstractIotDataCacheServer#listInfoCodeValueInOneHour")) {
            infoCodeValues = abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                    allObjectIds, allIotInfoCodes);
            safeNode.recordStep("查询IOT最近一小时数据");
        }
        Map<String, Map<String, LocalDateTime>> objectIdIotSetInfoCodeAndRecoverTime;
        try (PerformanceTracker.SafeNode safeNode =
                     tracker.track("AbstractIotDataCacheServer#queryObjectIdIotSetInfoCodeAndRecoverTime")) {
            objectIdIotSetInfoCodeAndRecoverTime = abstractIotDataCacheServer
                    .queryObjectIdIotSetInfoCodeAndRecoverTime(aggregates);
            safeNode.recordStep("查询IOT设定恢复时间");
        }
        context.setInfoCodeValues(infoCodeValues);
        context.setObjectIdIotSetInfoCodeAndRecoverTime(objectIdIotSetInfoCodeAndRecoverTime);
        return context;
    }

    /**
     * 查询对象ID和异常事件映射
     *
     * @param aggregates 聚合根
     * @return 对象ID -> 异常事件
     * @since 5.241101.0
     */
    private Map<String, List<ExceptionEventBaseInfoVO>> queryObjectIdAndRiskEvents(
            @NonNull Collection<InfoCodeAggregate> aggregates) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return Maps.newHashMap();
        }
        aggregates = aggregates.stream()
                .filter(item -> item.getConfigModel().getInfoCodeType().isRiskEventInfoCode()
                        && item.getConfigModel().analyzeTackValueLogicModel().isExecute())
                .collect(Collectors.toList());
        List<String> objectIds = aggregates.stream()
                .map(item -> item.getIdentifier().getObject().getId())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(objectIds)) {
            return Maps.newHashMap();
        }
        Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2s = IterableUtils.first(aggregates).getIdentifier()
                .getRunningMonitorType()
                .getRunningMonitorStrategy().exceptionFilterRiskMeasureAimLevel2();
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询异常事件数据");
        try (PerformanceTracker.SafeNode safeNode = tracker.track("AbstractCommonServer#mapRiskEvent")) {
            Map<String, List<ExceptionEventBaseInfoVO>> result =
                    abstractCommonServer.mapRiskEvent(objectIds, riskMeasureAimLevel2s);
            safeNode.recordStep("查询异常事件数据");
            return result;
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-网关";
    }
}
