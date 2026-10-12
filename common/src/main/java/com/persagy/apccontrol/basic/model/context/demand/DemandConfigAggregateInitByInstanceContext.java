package com.persagy.apccontrol.basic.model.context.demand;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConfigConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInheritModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandConfigGroupEntity;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigGroupRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandInstanceRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandTemplateRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigGroupRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandInstanceConfigRepositoryQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.scene.DemandSceneRepository;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.DemandConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.beans.PropertyDescriptor;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 需求配置聚合根根据实例初始化上下文
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandConfigAggregateInitByInstanceContext<T extends BaseDemandConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandConfigAggregateInitByInstanceContext.class);
    private static final String LOG_TAG = "需求配置聚合根初始化";

    // ------------------------- 入参 -------------------------

    /**
     * 查询参数
     */
    private final DemandConfigByInstanceBatchQueryParam param;

    /**
     * 需求模版仓储
     */
    private final DemandConfigConverter demandConfigConverter = SpringUtil.getBean(DemandConfigConverter.class);

    /**
     * 需求模版仓储
     */
    private final DemandTemplateRepository demandTemplateRepository = SpringUtil.getBean(DemandTemplateRepository.class);

    /**
     * 需求场景仓储
     */
    private final DemandSceneRepository demandSceneRepository = SpringUtil.getBean(DemandSceneRepository.class);

    /**
     * 需求配置仓储
     */
    private final DemandConfigRepository demandConfigRepository = SpringUtil.getBean(DemandConfigRepository.class);

    /**
     * 需求配置组仓储
     */
    private final DemandConfigGroupRepository demandConfigGroupRepository = SpringUtil.getBean(DemandConfigGroupRepository.class);

    /**
     * 需求实例配置仓储
     */
    private final DemandInstanceRepository demandInstanceRepository = SpringUtil.getBean(DemandInstanceRepository.class);

    /**
     * 实例服务
     */
    private final InstanceService instanceService = SpringUtil.getBean(InstanceService.class);

    /**
     * 物理世界服务
     */
    private final DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);

    /**
     * 多线程
     */
    private final Executor executor = SpringUtil.getBean(Executor.class);

    // ------------------------- 过程 -------------------------

    // -------------------- 过程-配置 --------------------

    /**
     * 需求模版
     */
    private DemandTemplate template;

    /**
     * 继承类型
     */
    private InheritConfigTypeEnum inheritConfigType;

    /**
     * 需求场景
     */
    @Nullable
    private DemandSceneEntity scene;

    /**
     * 需求配置
     */
    private List<DemandConfig> configs = null;

    /**
     * 分组配置ID -> 分组配置
     */
    private Map<Long, DemandConfigGroupEntity> configGroupIdAndData = Collections.emptyMap();

    /**
     * 实例类型 -> 实例ID
     */
    private Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds = Collections.emptyMap();

    /**
     * 实例ID -> 实例名称
     */
    private Map<String, String> instanceIdAndName = new HashMap<>();

    // -------------------- 过程-继承 --------------------

    /**
     * 实例ID -> 所有上级实例
     */
    private Map<String, List<DemandInstanceValueObject>> instanceIdAndAllParents = Collections.emptyMap();

    /**
     * 实例ID -> 上级聚合根
     */
    private Map<String, List<DemandConfigAggregate<T>>> instanceIdAndParentAggregates = Collections.emptyMap();

    /**
     * 实例 -> 下级空间功能类型
     */
    private LazyMap<DemandInstanceValueObject, Set<String>> instanceAndChildrenRoomFuncTypes =
            LazyMap.lazyMap(new ConcurrentHashMap<>(), instance -> {
                DemandTemplateTypeEnum templateType = template.getDemandTemplateType();
                DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();
                DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(templateType);
                queryParam.fillByInstance(instance);
                queryParam.addWithColumn(DtpRwdSpaceDTO.Fields.roomFuncType);
                return demandStrategy.listDemandSpace(queryParam).stream()
                        .map(DtpRwdSpaceDTO::getRoomFuncType)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toSet());
            });

    // ------------------------- 结果 -------------------------

    /**
     * 是否失败
     */
    private boolean failed = false;

    /**
     * 配置日期
     */
    private LocalDate configDate;

    // ------------------------- 行为 -------------------------

    /**
     * 填充需求模版
     *
     * @since 5.240902.0
     */
    public void fillTemplate() {
        DemandTemplate template = demandTemplateRepository.getByTemplateIdAndDate(true, param.getTemplateId(), param.getDate());
        this.inheritConfigType = template.getDemandTemplateType().demandConfigAnalyzeStrategy().inheritConfigType();
        this.template = template;
    }

    /**
     * 填充需求模版
     *
     * @since 5.240902.0
     */
    public void fillScene() {
        Long sceneId = param.getSceneId();
        if (Objects.nonNull(sceneId)) {
            this.scene = demandSceneRepository.getByIdAndDate(true, sceneId, param.getDate());
        }
    }

    /**
     * 填充实例ID
     *
     * @since 5.240902.0
     */
    public void fillInstanceIds() {
        // 填充实例类型和实例ID映射
        // 填充实例ID和本地名称映射
        // 填充实例ID和上级实例映射
        boolean filterInBlackedInstance = this.fillInstanceIdsWithAutoFull();
        // 当查询对象为空时, 查询数据库中已有的所有配置
        boolean allEmpty = true;
        for (Set<String> instanceIds : this.instanceTypeAndInstanceIds.values()) {
            if (CollectionUtils.isNotEmpty(instanceIds)) {
                allEmpty = false;
                break;
            }
        }
        if (allEmpty) {
            List<DemandConfig> configs = demandConfigRepository.listByInstances(
                    toConfigByInstanceRepositoryBatchQueryParam());
            if (CollectionUtils.isEmpty(configs)) {
                this.failed = true;
                return;
            }
            Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds = new LinkedHashMap<>();
            for (DemandConfig config : configs) {
                DemandInstanceTypeEnum instanceType = config.getDemandInstanceType();
                String instanceId = config.getDemandInstanceId();
                MapExecutor.dynamicPut(instanceTypeAndInstanceIds, instanceType, instanceId, LinkedHashSet::new);
            }
            if (CollectionUtils.isEmpty(configs)) {
                this.failed = true;
                return;
            }
            if (!filterInBlackedInstance) {
                instanceTypeAndInstanceIds = removeInvalidInstances(
                        instanceTypeAndInstanceIds, this.param.isFilterShieldedSpaces());
                filterInBlackedInstance = true;
                if (instanceTypeAndInstanceIds.values().stream().allMatch(CollectionUtils::isEmpty)) {
                    this.failed = true;
                    return;
                }
            }
            this.instanceTypeAndInstanceIds = instanceTypeAndInstanceIds;
        }
        if (this.failed) {
            return;
        }
        // 移除无效对象
        if (!filterInBlackedInstance) {
            this.instanceTypeAndInstanceIds = removeInvalidInstances(
                    this.instanceTypeAndInstanceIds, this.param.isFilterShieldedSpaces());
        }
        for (Map.Entry<DemandInstanceTypeEnum, Set<String>> entry : this.instanceTypeAndInstanceIds.entrySet()) {
            DemandInstanceTypeEnum instanceType = entry.getKey();
            Set<String> instanceIds = entry.getValue();
            if (CollectionUtils.isNotEmpty(instanceIds)) {
                continue;
            }
            LogExecutor.project().warn(LOGGER, LOG_TAG, "无有效需求对象", Arrays.asList(
                    Pair.of("instanceType", instanceType),
                    Pair.of("instanceIds", instanceIds)
            ));
        }
    }

    /**
     * 填充需求配置
     *
     * @since 5.240902.0
     */
    public void fillConfigs() {
        Long templateId = param.getTemplateId();
        Long sceneId = param.getSceneId();

        // 前面如果已经查询过则不重复查询
        List<DemandConfig> configs = this.configs;
        if (Objects.isNull(configs)) {
            configs = demandConfigRepository.listByInstances(toConfigByInstanceRepositoryBatchQueryParam());
        }
        // 填充不存在的实例配置, 用于处理继承逻辑
        Map<DemandInstanceValueObject, DemandConfig> instanceAndDemandConfigs = configs.stream().collect(
                Collectors.toMap(item -> new DemandInstanceValueObject(item.getDemandInstanceType(),
                        item.getDemandInstanceId()), Function.identity(), (o1, o2) -> o1));
        // 当数据库中不存在实例配置时, 创建一个空配置
        Map<DemandInstanceValueObject, DemandConfig> notExistsInstanceAndDemandConfigs = new HashMap<>();
        instanceTypeAndInstanceIds.forEach((instanceType, instanceIds) -> {
            if (CollectionUtils.isEmpty(instanceIds)) {
                instanceIds = instanceAndDemandConfigs.keySet().stream()
                        .filter(item -> item.getInstanceType() == instanceType)
                        .map(DemandInstanceValueObject::getInstanceId)
                        .collect(Collectors.toSet());
            }
            for (String instanceId : instanceIds) {
                DemandInstanceValueObject instance = new DemandInstanceValueObject(instanceType, instanceId);
                DemandConfig config = instanceAndDemandConfigs.get(instance);
                if (Objects.isNull(config)) {
                    notExistsInstanceAndDemandConfigs.put(instance, DemandConfig.of(templateId, sceneId,
                            new DemandInstanceValueObject(instanceType, instanceId), null));
                }
            }
        });
        instanceAndDemandConfigs.putAll(notExistsInstanceAndDemandConfigs);
        this.configs = new ArrayList<>(instanceAndDemandConfigs.values());
    }

    /**
     * 填充配置分组
     *
     * @since 7.250202.0
     */
    public void fillConfigGroup() {
        if (CollectionUtils.isEmpty(this.configs)) {
            return;
        }
        // 从配置中解析出所有分组ID
        Set<Long> groupIds = analyzeGroupIdsByConfig(this.configs);
        if (CollectionUtils.isEmpty(groupIds)) {
            return;
        }
        DemandConfigGroupRepositoryBatchQueryParam queryParam = new DemandConfigGroupRepositoryBatchQueryParam();
        queryParam.setTemplateId(param.getTemplateId());
        queryParam.setSceneId(param.getSceneId());
        queryParam.setConfigGroupIds(groupIds);
        queryParam.setDate(param.getDate());
        queryParam.addColumns(BaseAssignIdDO::getId, DemandConfigGroup::getGroupName,
                DemandConfigGroup::getRelTemplateId, DemandConfigGroup::getRelSceneId);
        List<DemandConfigGroup> configGroups = demandConfigGroupRepository.list(queryParam);
        if (CollectionUtils.isEmpty(configGroups)) {
            return;
        }
        this.configGroupIdAndData = configGroups.stream()
                .map(demandConfigConverter::toConfigGroupEntity)
                .collect(Collectors.toMap(item -> item.getIdentifier().getGroupId(),
                        Function.identity(), (o1, o2) -> o1));
    }

    /**
     * 转换聚合根
     *
     * @return 聚合根
     * @since 5.240902.0
     */
    public List<DemandConfigAggregate<T>> toAggregates(@NonNull List<DemandConfig> configs) {
        fillConfigDate();
        DemandTemplateTypeEnum templateType = template.getDemandTemplateType();
        String demandTemplateTag = template.getDemandTemplateTag();
        List<DemandConfigAggregate<T>> aggregates = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(configs)) {
            for (DemandConfig config : configs) {
                DemandInstanceTypeEnum instanceType = config.getDemandInstanceType();
                String instanceId = config.getDemandInstanceId();
                JsonNode configData = config.getConfigData();
                Long relGroupId = config.getRelGroupId();

                // 需求关联分组
                DemandConfigGroupEntity configGroup = configGroupIdAndData.get(relGroupId);
                // 空间功能类型关联分组
                Set<Long> roomFuncTypeRelGroupIds = analyzeGroupIdsByConfig(Lists.newArrayList(config));
                Map<Long, DemandConfigGroupEntity> roomFuncTypeRelGroupIdAndConfigGroup = roomFuncTypeRelGroupIds.stream()
                        .map(item -> configGroupIdAndData.get(item))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(item -> item.getIdentifier().getGroupId(),
                                Function.identity(), (o1, o2) -> o1));

                DemandInstanceValueObject instance = new DemandInstanceValueObject(instanceType, instanceId);
                DemandConfigAggregate<T> aggregate = DemandConfigAggregate.of(templateType, demandTemplateTag,
                        config.getRelTemplateId(), configDate, scene,
                        configGroup, roomFuncTypeRelGroupIdAndConfigGroup,
                        instance, configData);
                aggregates.add(aggregate);
            }
        }
        return aggregates;
    }

    /**
     * 处理继承配置
     *
     * @param aggregates 聚合根
     * @since 5.240902.0
     */
    public void handleAggregateInheritConfig(@NonNull List<DemandConfigAggregate<T>> aggregates) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询需求配置");
        try (PerformanceTracker.SafeNode safeNode = tracker.enter(
                "DemandConfigAggregateInitByInstanceContext#handleAggregateInheritConfig")) {
            handleAggregateInheritConfig(aggregates, safeNode);
        }
    }

    /**
     * 处理继承配置
     *
     * @param aggregates 聚合根
     * @param safeNode   性能监控节点，用于内部步骤计时
     * @since 5.240902.0
     */
    public void handleAggregateInheritConfig(@NonNull List<DemandConfigAggregate<T>> aggregates,
                                             @NonNull PerformanceTracker.SafeNode safeNode) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        // 填充项目默认值
        for (DemandConfigAggregate<T> aggregate : aggregates) {
            DemandConfigIdentifier identifier = aggregate.getIdentifier();
            DemandInstanceTypeEnum instanceType = identifier.getInstance().getInstanceType();
            if (instanceType == DemandInstanceTypeEnum.PROJECT) {
                T defaultConfig = identifier.getTemplateType().<T>demandConfigAnalyzeStrategy()
                        .defaultConfig(DemandInstanceTypeEnum.PROJECT).get();
                DemandConfigValueObject<T> valueObject = new DemandConfigValueObject<>();
                valueObject.setGlobalConfig(defaultConfig);
                aggregate.setParentConfig(valueObject);
            }
        }
        safeNode.recordStep("填充项目默认值");
        // 填充实例和对应的上级对象
        fillInstanceIdAndAllParents(aggregates);
        safeNode.recordStep("填充实例上级对象");
        // 填充实例ID和上级聚合根
        fillInstanceIdAndParentAggregates();
        safeNode.recordStep("填充实例上级聚合根");
        if (MapUtils.isEmpty(this.instanceIdAndParentAggregates)) {
            return;
        }
        // 实例 -> 下级空间功能类型
        fillInstanceAndChildrenRoomFuncTypes(aggregates);
        safeNode.recordStep("填充实例下级空间功能类型");
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (DemandConfigAggregate<T> aggregate : aggregates) {
            DemandConfigIdentifier identifier = aggregate.getIdentifier();
            DemandInstanceTypeEnum instanceType = identifier.getInstance().getInstanceType();
            String instanceId = identifier.getInstance().getInstanceId();
            DemandConfigValueObject<T> config = aggregate.getConfig();

            if (instanceType == DemandInstanceTypeEnum.PROJECT) {
                continue;
            }
            // 配置预检
            if (Objects.nonNull(config) && config.notEmpty()) {
                // 当为对象继承且存在自身配置支持时, 跳过继承逻辑
                if (inheritConfigType == InheritConfigTypeEnum.OBJECT) {
                    continue;
                }
                // 当为字段继承且所有字段均有值时, 跳过继承逻辑
                if (inheritConfigType == InheritConfigTypeEnum.FIELD && config.allFieldHasValue()) {
                    continue;
                }
            }
            // 找到所有上级配置聚合根
            List<DemandConfigAggregate<T>> parentAggregates = instanceIdAndParentAggregates.get(instanceId);
            if (CollectionUtils.isEmpty(parentAggregates)) {
                continue;
            }
            // 处理聚合根继承配置
            switch (inheritConfigType) {
                case OBJECT:
                    futures.add(CompletableFuture.runAsync(
                            () -> handleAggregateInheritConfigByObject(aggregate, parentAggregates),
                            TtlExecutors.getTtlExecutor(executor)));
                    break;
                case FIELD:
                    futures.add(CompletableFuture.runAsync(
                            () -> handleAggregateInheritConfigByField(aggregate, parentAggregates),
                            TtlExecutors.getTtlExecutor(executor)));
                    break;
                default:
                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(
                            inheritConfigType.getClass().getSimpleName(), inheritConfigType);
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        safeNode.recordStep("并发处理继承配置");
    }

    /**
     * 填充实例名称
     *
     * @param aggregates 聚合根
     * @since 5.240902.0
     */
    public void fillInstanceName(@NonNull List<DemandConfigAggregate<T>> aggregates) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询需求配置");
        try (PerformanceTracker.SafeNode safeNode = tracker.enter(
                "DemandConfigAggregateInitByInstanceContext#fillInstanceName")) {
            fillInstanceName(aggregates, safeNode);
        }
    }

    /**
     * 填充实例名称
     *
     * @param aggregates 聚合根
     * @param safeNode   性能监控节点，用于内部步骤计时
     * @since 5.240902.0
     */
    public void fillInstanceName(@NonNull List<DemandConfigAggregate<T>> aggregates,
                                 @NonNull PerformanceTracker.SafeNode safeNode) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        // 收集待查询实例ID
        Set<String> instanceIds = aggregates.stream()
                .map(item -> item.getIdentifier().getInstance().getInstanceId())
                .filter(id -> !instanceIdAndName.containsKey(id))
                .collect(Collectors.toSet());
        safeNode.recordStep("收集待查询实例ID");
        // 查询实例名称
        if (CollectionUtils.isNotEmpty(instanceIds)) {
            Map<String, String> queriedNames = dtpRwdClientService.mapObjectIdAndLocalName(instanceIds);
            instanceIdAndName.putAll(queriedNames);
        }
        safeNode.recordStep("查询实例名称");
        // 填充实例名称
        for (DemandConfigAggregate<T> aggregate : aggregates) {
            String instanceId = aggregate.getIdentifier().getInstance().getInstanceId();
            aggregate.getIdentifier().getInstance().setInstanceName(instanceIdAndName.get(instanceId));
        }
        safeNode.recordStep("填充实例名称");
    }

    /**
     * 根据需求配置解析需求分组ID
     *
     * @return 需求分组ID
     * @since 7.250202.0
     */
    private Set<Long> analyzeGroupIdsByConfig(@Nullable Collection<DemandConfig> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            return Sets.newHashSet();
        }
        Set<Long> groupIds = new HashSet<>();
        for (DemandConfig config : configs) {
            JsonNode configData = config.getConfigData();
            Long relGroupId = config.getRelGroupId();
            // 添加自身关联分组
            if (Objects.nonNull(relGroupId)) {
                groupIds.add(relGroupId);
            }
            // 添加空间功能类型中的关联分组
            if (Objects.nonNull(configData)) {
                JsonNode spaceFuncTypeConfigs = configData.get(DemandConfigValueObject.Fields.spaceFuncTypeConfigs);
                if (Objects.nonNull(spaceFuncTypeConfigs)) {
                    Iterator<JsonNode> spaceFuncTypeConfigIterator = spaceFuncTypeConfigs.elements();
                    while (spaceFuncTypeConfigIterator.hasNext()) {
                        JsonNode spaceFuncTypeConfig = spaceFuncTypeConfigIterator.next();
                        JsonNode spaceFuncTypeConfigRelGroupId = spaceFuncTypeConfig.get(
                                DemandSpaceFuncTypeConfig.Fields.relConfigGroupId);
                        if (Objects.nonNull(spaceFuncTypeConfigRelGroupId)) {
                            groupIds.add(spaceFuncTypeConfigRelGroupId.asLong());
                        }
                    }
                }
            }
        }
        return groupIds;
    }

    /**
     * 转换根据实例批量查询配置参数
     *
     * @return 根据实例批量查询配置参数
     * @since 7.250202.0
     */
    private DemandConfigByInstanceRepositoryBatchQueryParam toConfigByInstanceRepositoryBatchQueryParam() {
        DemandConfigByInstanceRepositoryBatchQueryParam queryParam = demandConfigConverter.toBathQueryParam(this.param);
        queryParam.addColumns(BaseAssignIdDO::getId,
                DemandConfig::getDemandInstanceType, DemandConfig::getDemandInstanceId,
                DemandConfig::getConfigData, DemandConfig::getRelTemplateId,
                DemandConfig::getRelSceneId, DemandConfig::getRelGroupId);
        return queryParam;
    }

    /**
     * 自动填充实例, 当实例ID为空时, 自动填充实例ID
     *
     * @return 是否过滤了黑名单空间
     * @since 7.250202.
     */
    private boolean fillInstanceIdsWithAutoFull() {
        // 分组存在时单独处理
        Set<Long> configGroupIds = this.param.getConfigGroupIds();
        if (CollectionUtils.isNotEmpty(configGroupIds)) {
            this.configs = demandConfigRepository.listByInstances(toConfigByInstanceRepositoryBatchQueryParam());
            this.instanceTypeAndInstanceIds = this.configs.stream().collect(Collectors.groupingBy(
                    DemandConfig::getDemandInstanceType, Collectors.mapping(
                            DemandConfig::getDemandInstanceId, Collectors.toSet())));
            return false;
        }
        String projectId = ContextUtil.getProjectId();
        this.instanceTypeAndInstanceIds = new HashMap<>(this.param.getInstanceTypeAndInstanceIds());
        boolean autoFillEmptyInstanceIds = this.param.isAutoFillEmptyInstanceIds();
        if (!autoFillEmptyInstanceIds) {
            return false;
        }
        // 当查询实例中只有项目时, 则设置项目ID并跳过
        if (this.instanceTypeAndInstanceIds.size() == 1
                && this.instanceTypeAndInstanceIds.containsKey(DemandInstanceTypeEnum.PROJECT)) {
            this.instanceTypeAndInstanceIds.put(DemandInstanceTypeEnum.PROJECT, Sets.newHashSet(projectId));
            return false;
        }
        if (this.instanceTypeAndInstanceIds.values().stream().allMatch(CollectionUtils::isNotEmpty)) {
            return false;
        }
        DemandStrategy demandStrategy = template.getDemandType().demandStrategy();
        DemandSpacesQueryParam queryParam = toDemandSpaceQueryParam(this.instanceTypeAndInstanceIds);
        List<DtpRwdSpaceDTO> demandSpaces = demandStrategy.listDemandSpace(queryParam);
        if (CollectionUtils.isEmpty(demandSpaces)) {
            return true;
        }
        Set<String> allSpaceIds = new HashSet<>();
        Set<String> allFloorIds = new HashSet<>();
        Set<String> allBuildingIds = new HashSet<>();
        for (DtpRwdSpaceDTO demandSpace : demandSpaces) {
            allSpaceIds.add(demandSpace.getId());
            if (StringUtils.isNotBlank(demandSpace.getFloorId())) {
                allFloorIds.add(demandSpace.getFloorId());
            }
            if (StringUtils.isNotBlank(demandSpace.getBuildingId())) {
                allBuildingIds.add(demandSpace.getBuildingId());
            }
        }
        for (Map.Entry<DemandInstanceTypeEnum, Set<String>> entry : this.instanceTypeAndInstanceIds.entrySet()) {
            DemandInstanceTypeEnum instanceType = entry.getKey();
            Set<String> instanceIds = entry.getValue();
            boolean instanceNotEmpty = CollectionUtils.isNotEmpty(instanceIds);
            Set<String> fillInstanceIds = Collections.emptySet();
            switch (instanceType) {
                case SPACE:
                    fillInstanceIds = instanceNotEmpty
                            ? SetUtils.disjunction(instanceIds, allSpaceIds)
                            : allSpaceIds;
                    break;
                case FLOOR:
                    fillInstanceIds = instanceNotEmpty
                            ? SetUtils.disjunction(instanceIds, allFloorIds)
                            : allFloorIds;
                    break;
                case BUILDING:
                    fillInstanceIds = instanceNotEmpty
                            ? SetUtils.disjunction(instanceIds, allBuildingIds)
                            : allBuildingIds;
                    break;
                case PROJECT:
                    fillInstanceIds = Sets.newHashSet(projectId);
                    break;
                default:
            }
            entry.setValue(fillInstanceIds);
        }
        // 预填充实例详情
        preFillInstanceDetail(demandSpaces);
        return true;
    }

    /**
     * 转换需求空间查询参数
     *
     * @param instanceTypeAndInstanceIds 实例类型 -> 实例ID
     * @return 需求空间查询参数
     * @since 7.250301.0
     */
    private DemandSpacesQueryParam toDemandSpaceQueryParam(Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds) {
        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(template.getDemandTemplateType());
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName);
        // 查询继承配置时需要查询空间关联的空间功能类型/楼层/建筑
        if (param.isQueryInheritConfig()) {
            queryParam.addWithColumn(DtpRwdSpaceDTO.Fields.roomFuncType,
                    DtpRwdSpaceDTO.Fields.floorId, DtpRwdSpaceDTO.Fields.buildingId);
        }
        if (instanceTypeAndInstanceIds.containsKey(DemandInstanceTypeEnum.BUILDING)) {
            queryParam.addWithColumn(DtpRwdSpaceDTO.Fields.buildingId);
        }
        if (instanceTypeAndInstanceIds.containsKey(DemandInstanceTypeEnum.FLOOR)) {
            queryParam.addWithColumn(DtpRwdSpaceDTO.Fields.floorId);
        }
        queryParam.setFilterInBlacklistedSpace(true);
        queryParam.setFilterInShieldedSpace(this.param.isFilterShieldedSpaces());
        return queryParam;
    }

    /**
     * 预填充实例详情
     *
     * @param demandSpaces 需求空间
     * @since 7.250301.0
     */
    private void preFillInstanceDetail(@NonNull List<DtpRwdSpaceDTO> demandSpaces) {
        String projectId = ContextUtil.getProjectId();
        Map<String, String> instanceIdAndName = new HashMap<>();
        Map<String, List<DemandInstanceValueObject>> instanceIdAndAllParents = new HashMap<>();
        Map<DemandInstanceValueObject, Set<String>> spaceAndChildrenRoomFuncTypes = new HashMap<>();
        Map<DemandInstanceValueObject, Set<String>> floorAndChildrenRoomFuncTypes = new HashMap<>();
        Map<DemandInstanceValueObject, Set<String>> buildingAndChildrenRoomFuncTypes = new HashMap<>();
        Map<DemandInstanceValueObject, Set<String>> projectAndChildrenRoomFuncTypes = new HashMap<>();
        Map<DemandInstanceValueObject, Set<String>> instanceAndChildrenRoomFuncTypes = new HashMap<>();
        for (DtpRwdSpaceDTO demandSpace : demandSpaces) {
            // 填充空间名称
            instanceIdAndName.put(demandSpace.getId(), demandSpace.getLocalName());
            // 预填充对象和下级空间功能类型
            if (StringUtils.isNotBlank(demandSpace.getRoomFuncType())) {
                MapExecutor.dynamicPut(spaceAndChildrenRoomFuncTypes,
                        new DemandInstanceValueObject(DemandInstanceTypeEnum.SPACE, demandSpace.getId()),
                        demandSpace.getRoomFuncType(), HashSet::new);
                MapExecutor.dynamicPut(floorAndChildrenRoomFuncTypes,
                        new DemandInstanceValueObject(DemandInstanceTypeEnum.FLOOR, demandSpace.getFloorId()),
                        demandSpace.getRoomFuncType(), HashSet::new);
                MapExecutor.dynamicPut(buildingAndChildrenRoomFuncTypes,
                        new DemandInstanceValueObject(DemandInstanceTypeEnum.BUILDING, demandSpace.getBuildingId()),
                        demandSpace.getRoomFuncType(), HashSet::new);
                MapExecutor.dynamicPut(projectAndChildrenRoomFuncTypes,
                        new DemandInstanceValueObject(DemandInstanceTypeEnum.PROJECT, projectId),
                        demandSpace.getRoomFuncType(), HashSet::new);
            }
            // 填充空间上级对象
            MapExecutor.dynamicPut(instanceIdAndAllParents, demandSpace.getId(),
                    new DemandInstanceValueObject(DemandInstanceTypeEnum.FLOOR,
                            demandSpace.getFloorId()), ArrayList::new);
            MapExecutor.dynamicPut(instanceIdAndAllParents, demandSpace.getId(),
                    new DemandInstanceValueObject(DemandInstanceTypeEnum.BUILDING,
                            demandSpace.getBuildingId()), ArrayList::new);
            MapExecutor.dynamicPut(instanceIdAndAllParents, demandSpace.getId(),
                    new DemandInstanceValueObject(DemandInstanceTypeEnum.PROJECT,
                            projectId), ArrayList::new);
            // 填充楼层上级对象
            MapExecutor.dynamicPut(instanceIdAndAllParents, demandSpace.getFloorId(),
                    new DemandInstanceValueObject(DemandInstanceTypeEnum.BUILDING,
                            demandSpace.getBuildingId()), ArrayList::new);
            MapExecutor.dynamicPut(instanceIdAndAllParents, demandSpace.getFloorId(),
                    new DemandInstanceValueObject(DemandInstanceTypeEnum.PROJECT,
                            projectId), ArrayList::new);
            // 填充建筑上级对象
            MapExecutor.dynamicPut(instanceIdAndAllParents, demandSpace.getBuildingId(),
                    new DemandInstanceValueObject(DemandInstanceTypeEnum.PROJECT,
                            projectId), ArrayList::new);
        }
        instanceAndChildrenRoomFuncTypes.putAll(spaceAndChildrenRoomFuncTypes);
        instanceAndChildrenRoomFuncTypes.putAll(floorAndChildrenRoomFuncTypes);
        instanceAndChildrenRoomFuncTypes.putAll(buildingAndChildrenRoomFuncTypes);
        instanceAndChildrenRoomFuncTypes.putAll(projectAndChildrenRoomFuncTypes);
        this.instanceIdAndAllParents = instanceIdAndAllParents;
        this.instanceAndChildrenRoomFuncTypes.putAll(instanceAndChildrenRoomFuncTypes);
        this.instanceIdAndName = instanceIdAndName;
    }

    /**
     * 移除无效对象
     *
     * @param instanceTypeAndInstanceIds 实例类型 -> 实例ID
     * @param filterShieldedSpaces       是否过滤屏蔽空间
     * @since 5.241102.0
     */
    private Map<DemandInstanceTypeEnum, Set<String>> removeInvalidInstances(
            @NonNull Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds,
            boolean filterShieldedSpaces) {
        if (MapUtils.isEmpty(instanceTypeAndInstanceIds)) {
            return instanceTypeAndInstanceIds;
        }
        Set<String> allInstanceIds = instanceTypeAndInstanceIds.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(allInstanceIds)) {
            return instanceTypeAndInstanceIds;
        }
        Set<String> inBlacklistedInstanceIds = queryInvalidInstanceIds(allInstanceIds, filterShieldedSpaces);
        if (CollectionUtils.isEmpty(inBlacklistedInstanceIds)) {
            return instanceTypeAndInstanceIds;
        }
        Map<DemandInstanceTypeEnum, Set<String>> result = new HashMap<>();
        instanceTypeAndInstanceIds.forEach((instanceType, instanceIds) -> {
            if (CollectionUtils.isNotEmpty(instanceIds)) {
                result.put(instanceType, instanceIds.stream()
                        .filter(instanceId -> !inBlacklistedInstanceIds.contains(instanceId))
                        .collect(Collectors.toSet()));
            }
        });
        return result;
    }

    /**
     * 查询无效实例对象
     *
     * @param instanceIds             对象ID
     * @param filterShieldedInstances 是否过滤屏蔽对象
     * @return 无效实例对象
     * @since 7.250801.0
     */
    private Set<String> queryInvalidInstanceIds(@Nullable Set<String> instanceIds, boolean filterShieldedInstances) {
        DemandInstanceConfigValueObject instanceConfigValueObject = new DemandInstanceConfigValueObject();
        instanceConfigValueObject.setInBlacklisted(true);
        if (filterShieldedInstances) {
            instanceConfigValueObject.setShielded(true);
        }
        DemandInstanceConfigRepositoryQueryParam queryParam = new DemandInstanceConfigRepositoryQueryParam();
        queryParam.setTemplateType(template.getDemandTemplateType());
        queryParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
        queryParam.setInstanceIds(instanceIds);
        queryParam.setFilterConfig(instanceConfigValueObject);
        queryParam.setDate(param.getDate());
        queryParam.addColumns(DemandInstanceConfig::getDemandInstanceId);
        return demandInstanceRepository.listConfig(queryParam).stream()
                .map(DemandInstanceConfig::getDemandInstanceId)
                .collect(Collectors.toSet());
    }

    /**
     * 处理聚合根字段继承配置
     *
     * @param aggregate        聚合根
     * @param parentAggregates 所有上级配置聚合根
     * @since 5.241002.0
     */
    @SneakyThrows
    @SuppressWarnings("DuplicatedCode")
    private void handleAggregateInheritConfigByField(
            @NonNull DemandConfigAggregate<T> aggregate,
            @NonNull List<DemandConfigAggregate<T>> parentAggregates) {
        if (CollectionUtils.isEmpty(parentAggregates)) {
            return;
        }
        DemandTemplateTypeEnum templateType = aggregate.getIdentifier().getTemplateType();
        DemandConfigAnalyzeStrategy<T> configAnalyzeStrategy = templateType.demandConfigAnalyzeStrategy();
        T tempParentGlobalConfig = configAnalyzeStrategy.emptyConfig().get();
        LinkedHashMap<DemandInstanceValueObject, Set<DemandInheritModeEnum>> inheritInstanceAndModes = new LinkedHashMap<>();
        for (DemandConfigAggregate<T> parentAggregate : parentAggregates) {
            DemandInstanceValueObject parentInstance = parentAggregate.getIdentifier().getInstance();
            DemandInstanceTypeEnum parentInstanceType = parentInstance.getInstanceType();
            DemandConfigValueObject<T> parentConfig = parentAggregate.getConfig();
            // 当为顶级/项目实例时, 无自身配置则赋值父级配置
            if (parentInstanceType == DemandInstanceTypeEnum.PROJECT) {
                parentConfig = parentAggregate.getFinalConfig();
            }
            // 当上级配置为空时, 不进行继承
            if (Objects.isNull(parentConfig)) {
                continue;
            }
            T parentGlobalConfig = parentConfig.getGlobalConfig();
            if (Objects.isNull(parentGlobalConfig)) {
                continue;
            }
            // 遍历上级配置中的每个字段, 当字段值不为空时, 赋值到临时上级配置中
            Set<DemandInheritModeEnum> inheritModes = new HashSet<>();
            for (String fieldName : tempParentGlobalConfig.cacheFieldNames()) {
                PropertyDescriptor descriptor = tempParentGlobalConfig.cachePropertyDescriptor(fieldName);
                // 当自身值为空时, 查询上级的值, 不为空时赋值到自身
                Object fieldValue = descriptor.getReadMethod().invoke(tempParentGlobalConfig);
                if (Objects.isNull(fieldValue)) {
                    Object parentFieldValue = descriptor.getReadMethod().invoke(parentGlobalConfig);
                    if (Objects.nonNull(parentFieldValue)) {
                        inheritModes.add(DemandInheritModeEnum.GLOBAL);
                        descriptor.getWriteMethod().invoke(tempParentGlobalConfig, parentFieldValue);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(inheritModes)) {
                inheritInstanceAndModes.put(parentInstance, inheritModes);
            }
            if (tempParentGlobalConfig.allFieldNotEmpty()) {
                break;
            }
        }
        DemandConfigValueObject<T> configValueObject = new DemandConfigValueObject<>();
        configValueObject.setGlobalConfig(tempParentGlobalConfig);
        aggregate.setParentConfig(configValueObject);
        aggregate.setInheritInstanceAndModes(inheritInstanceAndModes);
    }

    /**
     * 处理聚合根对象继承配置
     *
     * @param aggregate        聚合根
     * @param parentAggregates 所有上级配置聚合根
     * @since 5.240902.0
     */
    private void handleAggregateInheritConfigByObject(
            @NonNull DemandConfigAggregate<T> aggregate,
            @NonNull List<DemandConfigAggregate<T>> parentAggregates) {
        handleAggregateInheritConfigByObject(aggregate, parentAggregates, true);
        if (MapUtils.isEmpty(aggregate.getInheritInstanceAndModes())) {
            handleAggregateInheritConfigByObject(aggregate, parentAggregates, false);
        }
    }

    /**
     * 处理聚合根对象继承配置
     *
     * @param aggregate        聚合根
     * @param parentAggregates 所有上级配置聚合根
     * @since 7.250301.0
     */
    private void handleAggregateInheritConfigByObject(
            @NonNull DemandConfigAggregate<T> aggregate,
            @NonNull List<DemandConfigAggregate<T>> parentAggregates,
            boolean roomFuncType) {
        if (CollectionUtils.isEmpty(parentAggregates)) {
            return;
        }
        DemandInstanceValueObject instance = aggregate.getIdentifier().getInstance();
        DemandInstanceTypeEnum instanceType = aggregate.getIdentifier().getInstance().getInstanceType();

        DemandConfigValueObject<T> finalParentConfig = null;
        LinkedHashMap<DemandInstanceValueObject, Set<DemandInheritModeEnum>> inheritInstanceAndModes = new LinkedHashMap<>();
        for (DemandConfigAggregate<T> parentAggregate : parentAggregates) {
            DemandInstanceValueObject parentInstance = parentAggregate.getIdentifier().getInstance();
            DemandInstanceTypeEnum parentInstanceType = parentInstance.getInstanceType();
            DemandConfigValueObject<T> parentConfig = parentAggregate.getConfig();
            // 当为顶级/项目实例时, 无自身配置则赋值父级配置
            if (parentInstanceType == DemandInstanceTypeEnum.PROJECT && Objects.isNull(parentConfig)) {
                parentConfig = parentAggregate.getParentConfig();
            }
            // 当上级配置为空时, 不进行继承
            if (Objects.isNull(parentConfig)) {
                continue;
            }
            Set<DemandInheritModeEnum> inheritModes = new HashSet<>();
            T globalConfig = parentConfig.getGlobalConfig();
            List<DemandSpaceFuncTypeConfig<T>> spaceFuncTypeConfigs = parentConfig.getSpaceFuncTypeConfigs();
            DemandConfigValueObject<T> configValueObject = new DemandConfigValueObject<>();
            if (roomFuncType) {
                if (CollectionUtils.isEmpty(spaceFuncTypeConfigs)) {
                    continue;
                }
                Set<String> childrenRoomFuncTypes = instanceAndChildrenRoomFuncTypes.get(instance);
                List<DemandSpaceFuncTypeConfig<T>> matchSpaceFuncTypeConfigs = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(childrenRoomFuncTypes)) {
                    // 构建新的空间功能那类型配置
                    for (DemandSpaceFuncTypeConfig<T> spaceFuncTypeConfig : spaceFuncTypeConfigs) {
                        List<DtpKnowledgeInfoCodeDataSourceDTO> funcTypes = spaceFuncTypeConfig.getFuncTypes();
                        T funcTypeConfig = spaceFuncTypeConfig.getConfig();
                        if (CollectionUtils.isEmpty(funcTypes)) {
                            continue;
                        }
                        List<DtpKnowledgeInfoCodeDataSourceDTO> matchFuncTypes = new ArrayList<>();
                        // 当下级配置存在该空间功能类型时, 添加到新的配置中
                        for (DtpKnowledgeInfoCodeDataSourceDTO funcType : funcTypes) {
                            if (childrenRoomFuncTypes.contains(funcType.getCode())) {
                                matchFuncTypes.add(funcType);
                            }
                        }
                        if (CollectionUtils.isNotEmpty(matchFuncTypes)) {
                            matchSpaceFuncTypeConfigs.add(DemandSpaceFuncTypeConfig.of(matchFuncTypes, funcTypeConfig));
                        }
                    }
                }
                if (CollectionUtils.isEmpty(matchSpaceFuncTypeConfigs)) {
                    continue;
                }
                // 当存在空间功能类型时, 使用空间功能类型配置作为全局配置
                if (instanceType == DemandInstanceTypeEnum.SPACE) {
                    T spaceGlobalConfig = IterableUtils.first(matchSpaceFuncTypeConfigs).getConfig();
                    configValueObject.setGlobalConfig(spaceGlobalConfig);
                } else {
                    configValueObject.setGlobalConfig(globalConfig);
                    configValueObject.setSpaceFuncTypeConfigs(matchSpaceFuncTypeConfigs);
                }
                inheritModes.add(DemandInheritModeEnum.ROOM_FUN_TYPE);
                finalParentConfig = configValueObject;
                inheritInstanceAndModes.put(parentInstance, inheritModes);
                break;
            }
            configValueObject.setGlobalConfig(globalConfig);
            if (instanceType != DemandInstanceTypeEnum.SPACE) {
                configValueObject.setSpaceFuncTypeConfigs(spaceFuncTypeConfigs);
            }
            inheritModes.add(DemandInheritModeEnum.GLOBAL);
            finalParentConfig = configValueObject;
            inheritInstanceAndModes.put(parentInstance, inheritModes);
            break;
        }
        aggregate.setParentConfig(finalParentConfig);
        aggregate.setInheritInstanceAndModes(inheritInstanceAndModes);
    }

    /**
     * 查询实例和下级空间功能类型映射
     *
     * @param aggregates 聚合根
     * @since 5.241001.0
     */
    private void fillInstanceAndChildrenRoomFuncTypes(@NonNull List<DemandConfigAggregate<T>> aggregates) {
        if (CollectionUtils.isEmpty(aggregates) && MapUtils.isEmpty(this.instanceIdAndParentAggregates)) {
            return;
        }
        Map<DemandInstanceValueObject, Set<String>> instanceAndChildrenRoomFuncTypes =
                new HashMap<>(this.instanceAndChildrenRoomFuncTypes);
        // 如果存在空间则直接查询空间的功能类型
        Set<String> spaceIds = aggregates.stream()
                .filter(item -> item.getIdentifier().getInstance().getInstanceType() == DemandInstanceTypeEnum.SPACE)
                .map(item -> item.getIdentifier().getInstance().getInstanceId())
                .collect(Collectors.toSet());
        Set<String> existsSpaceIds = instanceAndChildrenRoomFuncTypes.keySet().stream()
                .filter(item -> item.getInstanceType() == DemandInstanceTypeEnum.SPACE)
                .map(DemandInstanceValueObject::getInstanceId)
                .collect(Collectors.toSet());
        Collection<String> querySpaceIds = CollectionUtils.subtract(spaceIds, existsSpaceIds);
        if (CollectionUtils.isNotEmpty(querySpaceIds)) {
            DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setId(querySpaceIds)
                    .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdSpaceDTO.Fields.roomFuncType);
            List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpace(queryParam);
            for (DtpRwdSpaceDTO space : spaces) {
                instanceAndChildrenRoomFuncTypes.put(
                        new DemandInstanceValueObject(DemandInstanceTypeEnum.SPACE, space.getId()),
                        Sets.newHashSet(space.getRoomFuncType()));
            }
        }
        this.instanceAndChildrenRoomFuncTypes.putAll(instanceAndChildrenRoomFuncTypes);
    }

    /**
     * 填充实例ID和所有父级实例
     *
     * @param aggregates 聚合根
     * @since 5.240902.0
     */
    private void fillInstanceIdAndAllParents(@NonNull List<DemandConfigAggregate<T>> aggregates) {
        if (CollectionUtils.isEmpty(configs) || MapUtils.isNotEmpty(instanceIdAndAllParents)) {
            return;
        }
        Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds = aggregates.stream()
                .map(item -> item.getIdentifier().getInstance())
                .collect(Collectors.groupingBy(DemandInstanceValueObject::getInstanceType, Collectors.mapping(
                        DemandInstanceValueObject::getInstanceId, Collectors.toCollection(HashSet::new))));
        this.instanceIdAndAllParents = instanceService.mapInstanceIdAndAllParentsByDirectQuery(instanceTypeAndIds, false);
    }

    /**
     * 填充实例ID和上级聚合根映射
     *
     * @since 5.240902.0
     */
    private void fillInstanceIdAndParentAggregates() {
        if (MapUtils.isEmpty(this.instanceIdAndAllParents)) {
            return;
        }
        String projectId = ContextUtil.getProjectId();
        AtomicBoolean containsProject = new AtomicBoolean(false);
        // 查询所有上级聚合根
        Map<DemandInstanceTypeEnum, Set<String>> allParentInstanceTypeAndIds = new HashMap<>();
        this.instanceIdAndAllParents.values().stream().flatMap(Collection::stream).forEach(parentInstance -> {
            if (parentInstance.getInstanceType() == DemandInstanceTypeEnum.PROJECT
                    && StringUtils.equals(parentInstance.getInstanceId(), projectId)) {
                containsProject.set(true);
            }
            MapExecutor.dynamicPut(allParentInstanceTypeAndIds, parentInstance.getInstanceType(),
                    parentInstance.getInstanceId(), HashSet::new);
        });
        DemandConfigByInstanceRepositoryBatchQueryParam batchQueryParam =
                new DemandConfigByInstanceRepositoryBatchQueryParam();
        batchQueryParam.setDate(configDate);
        batchQueryParam.setTemplateId(template.getId());
        if (Objects.nonNull(scene)) {
            batchQueryParam.setSceneId(scene.getId());
        }
        batchQueryParam.setInstanceTypeAndInstanceIds(allParentInstanceTypeAndIds);
        List<DemandConfig> configs = demandConfigRepository.listByInstances(batchQueryParam);
        List<DemandConfigAggregate<T>> parentAggregates = new ArrayList<>(toAggregates(configs));
        // 如果查询上级包含项目, 但结果中没有项目, 则处理项目默认值
        if (containsProject.get()) {
            DemandConfigAnalyzeStrategy<T> configAnalyzeStrategy =
                    template.getDemandTemplateType().demandConfigAnalyzeStrategy();
            T projectDefaultConfig = configAnalyzeStrategy.defaultConfig(DemandInstanceTypeEnum.PROJECT).get();
            // 当项目存在默认值时, 修改项目配置聚合根
            if (Objects.nonNull(projectDefaultConfig) && projectDefaultConfig.anyFieldNotEmpty()) {
                // 存在项目配置时修改, 不存在时新增
                boolean notContainsProjectAggregate = true;
                for (DemandConfigAggregate<T> parentAggregate : parentAggregates) {
                    DemandInstanceTypeEnum instanceType = parentAggregate.getIdentifier().getInstance().getInstanceType();
                    if (instanceType == DemandInstanceTypeEnum.PROJECT) {
                        parentAggregate.setParentConfig(DemandConfigValueObject.ofDefault(projectDefaultConfig, null));
                        notContainsProjectAggregate = false;
                    }
                }
                if (notContainsProjectAggregate) {
                    DemandConfigAggregate<T> aggregate = DemandConfigAggregate.of(template.getDemandTemplateType(),
                            template.getDemandTemplateTag(), template.getId(), configDate, scene,
                            new DemandInstanceValueObject(DemandInstanceTypeEnum.PROJECT, projectId), null);
                    aggregate.setParentConfig(DemandConfigValueObject.ofDefault(projectDefaultConfig, null));
                    parentAggregates.add(aggregate);
                }
            }
        }
        if (CollectionUtils.isEmpty(parentAggregates)) {
            return;
        }
        // 将所有上级聚合根转换为实例ID和上级聚合根映射
        Map<String, DemandConfigAggregate<T>> instanceIdAndParentAggregate = parentAggregates.stream()
                .collect(Collectors.toMap(item -> item.getIdentifier().getInstance().getInstanceId(),
                        Function.identity(), (o1, o2) -> o1));
        Map<String, List<DemandConfigAggregate<T>>> result = new HashMap<>();
        this.instanceIdAndAllParents.forEach((instanceId, parentInstances) -> {
            for (DemandInstanceValueObject parentInstance : parentInstances) {
                DemandConfigAggregate<T> parentAggregate = instanceIdAndParentAggregate.get(parentInstance.getInstanceId());
                // 当存在上级聚合根且存在配置时添加
                if (Objects.nonNull(parentAggregate)) {
                    MapExecutor.dynamicPut(result, instanceId, parentAggregate, ArrayList::new);
                }
            }
        });
        this.instanceIdAndParentAggregates = result;
    }

    /**
     * 填充配置日期
     *
     * @since 5.241201.0
     */
    private void fillConfigDate() {
        configDate = ObjectUtils.defaultIfNull(param.getDate(), DateUtils.now().toLocalDate());
    }
}
