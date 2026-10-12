package com.persagy.apccontrol.basic.structure.demand.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResult;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultBusinessKey;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultRepositoryQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandInstanceConfigRepositoryQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 需求配置结果刷新仓储实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigResultRefreshRepositoryImpl<T extends BaseDemandConfig>
        implements DemandConfigResultRefreshRepository, LogComponent {

    /**
     * 同一模板类型下模板ID刷新并发数。
     *
     * <p>首次初始化按集团/项目遍历，项目会被 XXL-JOB 分片。当前已接入无场景的空调、通风和安防需求，
     * 单项目通常存在4到10个模板、需求对象量可按1万估算。因此把单项目内的模板ID并发控制在3，
     * 相比2能减少10个模板ID时的等待批次，同时避免 RWD、数据库连接和公共业务线程池占用继续放大。</p>
     */
    private static final int REFRESH_TEMPLATE_ID_PARALLEL_SIZE = 3;

    /**
     * 当前已接入需求配置结果预计算的需求类型。
     *
     * <p>新增需求类型时只在这里调整支持范围；Listener、XXL-JOB、Debug 和刷新入口统一通过
     * {@link #listSupportedTemplateTypes()} / {@link #supportsTemplateType(DemandTemplateTypeEnum)} 判断，
     * 不得再分散硬编码具体需求类型。</p>
     */
    private static final @NonNull Set<DemandTypeEnum> SUPPORTED_DEMAND_TYPES =
            Collections.unmodifiableSet(EnumSet.of(
                    DemandTypeEnum.AIRCONDITIONING,
                    DemandTypeEnum.SECURITY,
                    DemandTypeEnum.VENTILATION));

    /**
     * 根据支持的需求类型派生模板类型列表。
     */
    private static final @NonNull List<DemandTemplateTypeEnum> SUPPORTED_TEMPLATE_TYPES =
            Collections.unmodifiableList(DemandConfigResultRefreshRepositoryImpl.SUPPORTED_DEMAND_TYPES.stream()
                    .flatMap(demandType -> demandType.toDemandTemplateTypes().stream())
                    .distinct()
                    .collect(Collectors.toList()));

    private final @NonNull DemandConfigGateway<T> demandConfigGateway;
    private final @NonNull DemandTemplateRepository demandTemplateRepository;
    private final @NonNull DemandInstanceRepository demandInstanceRepository;
    private final @NonNull InstanceService instanceService;
    private final @NonNull DemandConfigResultRepository demandConfigResultRepository;

    @Override
    public @NonNull List<DemandTemplateTypeEnum> listSupportedTemplateTypes() {
        return DemandConfigResultRefreshRepositoryImpl.SUPPORTED_TEMPLATE_TYPES;
    }

    @Override
    public boolean supportsTemplateType(@NonNull DemandTemplateTypeEnum templateType) {
        return DemandConfigResultRefreshRepositoryImpl.SUPPORTED_DEMAND_TYPES.contains(
                templateType.toDemandTypeEnum());
    }

    @Override
    public boolean supportsTemplate(@NonNull Long templateId) {
        return this.supportsTemplateType(this.queryTemplate(templateId).getDemandTemplateType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshInstance(
            @NonNull Long templateId,
            @Nullable Long sceneId,
            @NonNull DemandInstanceValueObject instance) {
        if (!this.supportsTemplate(templateId)) {
            return;
        }
        try {
            this.refreshInstances(templateId, sceneId, Collections.singleton(instance));
        } catch (RuntimeException e) {
            this.logRefreshError("刷新单实例结果失败", e, templateId, sceneId, 1);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshByInstanceAndChildren(
            @NonNull Long templateId,
            @Nullable Long sceneId,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull Set<String> instanceIds) {
        if (CollectionUtils.isEmpty(instanceIds) || !this.supportsTemplate(templateId)) {
            return;
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("刷新需求配置结果");
        try {
            try (PerformanceTracker.SafeNode safeNode = tracker.enter(
                    "DemandConfigResultRefreshRepositoryImpl#refreshByInstanceAndChildren")) {
            Set<DemandInstanceValueObject> instances = this.expandInstancesAndChildren(instanceType, instanceIds);
            safeNode.recordStep("查询并展开下级实例");
            this.refreshInstances(templateId, sceneId, instances);
            safeNode.recordStep("刷新展开实例结果");
            }
        } catch (RuntimeException e) {
            this.logProjectInstance().error("刷新实例及下级结果失败", e, Arrays.asList(
                    Pair.of("templateId", templateId),
                    Pair.of("sceneId", sceneId),
                    Pair.of("instanceType", instanceType),
                    Pair.of("instanceCount", instanceIds.size())));
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshByTemplate(@NonNull Long templateId) {
        if (!this.supportsTemplate(templateId)) {
            return;
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("刷新需求配置结果");
        try {
            try (PerformanceTracker.SafeNode safeNode = tracker.enter(
                    "DemandConfigResultRefreshRepositoryImpl#refreshByTemplate")) {
                this.refreshByTemplateScope(
                        templateId, DemandInstanceTypeEnum.toAllInstanceTypeAndInstanceIds(), true);
                safeNode.recordStep("刷新空调模板结果");
            }
        } catch (RuntimeException e) {
            this.logRefreshError("按模板刷新结果失败", e, templateId, null, null);
            throw e;
        }
    }

    @Override
    public void refreshByTemplateType(@NonNull DemandTemplateTypeEnum templateType) {
        if (!this.supportsTemplateType(templateType)) {
            return;
        }
        /*
         * 该入口只负责编排模板查询、RWD需求范围预取和并发任务等待，不开启外层数据库事务。
         * 每个模板最终通过 syncRefreshScope(...) 进入独立事务，避免编排线程长期占用事务资源，
         * 也避免形成外层事务能够回滚并发子任务的错误语义。
         */
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("刷新需求配置结果");
        try {
            try (PerformanceTracker.SafeNode safeNode = tracker.enter(
                    "DemandConfigResultRefreshRepositoryImpl#refreshByTemplateType")) {
                List<Long> templateIds = this.listTemplateIds(templateType, safeNode);
                if (CollectionUtils.isEmpty(templateIds)) {
                    return;
                }
                Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds =
                        this.toDemandScopeInstanceTypeAndInstanceIds(templateType);
                safeNode.recordStep("查询需求空间范围");
                if (MapUtils.isEmpty(instanceTypeAndInstanceIds)) {
                    this.deleteResultsByTemplateType(templateType, templateIds);
                    safeNode.recordStep("删除无需求空间的模板类型结果和快照");
                    return;
                }
                Set<String> shieldedSpaceIds =
                        this.listImmutableShieldedSpaceIds(templateType, instanceTypeAndInstanceIds);
                safeNode.recordStep("查询屏蔽空间范围");
                this.refreshTemplatesInParallel(
                        tracker, templateType, templateIds, instanceTypeAndInstanceIds, shieldedSpaceIds);
                safeNode.recordStep("按模板分片并发刷新结果");
            }
        } catch (RuntimeException e) {
            this.logProjectInstance().error("按模板类型刷新结果失败", e, Collections.singletonList(
                    Pair.of("templateType", templateType)));
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshByDtxChangedInstances(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<DemandInstanceValueObject> instances) {
        if (!this.supportsTemplateType(templateType) || CollectionUtils.isEmpty(instances)) {
            return;
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("刷新需求配置结果");
        try {
            try (PerformanceTracker.SafeNode safeNode = tracker.enter(
                    "DemandConfigResultRefreshRepositoryImpl#refreshByDtxChangedInstances")) {
                List<Long> templateIds = this.listTemplateIds(templateType, safeNode);
                for (Long templateId : templateIds) {
                    this.refreshInstances(templateId, null, instances);
                }
                safeNode.recordStep("按空调模板循环刷新DTX变更实例");
            }
        } catch (RuntimeException e) {
            this.logProjectInstance().error("按DTX变更实例刷新结果失败", e, Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("instanceCount", instances.size())));
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShielded(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Set<String> instanceIds,
            boolean shielded) {
        if (!this.supportsTemplateType(templateType) || CollectionUtils.isEmpty(instanceIds)) {
            return;
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("刷新需求配置结果");
        try {
            try (PerformanceTracker.SafeNode safeNode = tracker.enter(
                    "DemandConfigResultRefreshRepositoryImpl#updateShielded")) {
            DemandConfigResultRepositoryQueryParam queryParam = new DemandConfigResultRepositoryQueryParam();
            queryParam.setDemandTemplateType(templateType);
            queryParam.setDemandInstanceType(DemandInstanceTypeEnum.SPACE);
            queryParam.setDemandInstanceIds(instanceIds);
            // 屏蔽更新只需要业务键，不读取final_config_json和inherit_instances_json这类大字段。
            queryParam.addColumns(DemandConfigResult::getDemandTemplateType,
                    DemandConfigResult::getDemandInstanceType,
                    DemandConfigResult::getDemandInstanceId,
                    DemandConfigResult::getRelTemplateId,
                    DemandConfigResult::getRelSceneId);
            List<DemandConfigResultBusinessKey> businessKeys = this.demandConfigResultRepository.list(queryParam).stream()
                    .map(DemandConfigResultBusinessKey::of)
                    .collect(Collectors.toList());
            safeNode.recordStep("查询屏蔽更新业务键");
            this.demandConfigResultRepository.updateShielded(businessKeys, shielded);
            safeNode.recordStep("更新屏蔽标识和快照");
            }
        } catch (RuntimeException e) {
            this.logProjectInstance().error("更新预计算结果屏蔽标识失败", e, Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("instanceCount", instanceIds.size()),
                    Pair.of("shielded", shielded)));
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByTemplateTypeAndInstances(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<DemandInstanceValueObject> instances) {
        if (!this.supportsTemplateType(templateType) || CollectionUtils.isEmpty(instances)) {
            return;
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("刷新需求配置结果");
        try {
            try (PerformanceTracker.SafeNode safeNode = tracker.enter(
                    "DemandConfigResultRefreshRepositoryImpl#removeByTemplateTypeAndInstances")) {
                List<Long> templateIds = this.listTemplateIds(templateType, safeNode);
                /*
                 * 进入黑名单的空间已经不应该存在结果。空调需求没有场景，直接按模板业务键删除结果并关闭快照，
                 * 避免黑名单新增时做无意义的 RWD 和配置计算。
                 */
                for (Long templateId : templateIds) {
                    this.deleteResults(this.toDeleteParams(templateType, templateId, null, instances));
                }
                safeNode.recordStep("按空调模板删除指定实例结果");
            }
        } catch (RuntimeException e) {
            this.logProjectInstance().error("删除模板类型下指定实例结果失败", e, Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("instanceCount", instances.size())));
            throw e;
        }
    }

    /**
     * 刷新指定实例集合。
     *
     * @param templateId 模板ID
     * @param sceneId    场景ID
     * @param instances  实例集合
     * @since v7.260701.0
     */
    private void refreshInstances(
            @NonNull Long templateId,
            @Nullable Long sceneId,
            @NonNull Collection<DemandInstanceValueObject> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return;
        }
        // 实例级刷新的统一前置过滤：结果表只写入需求空间及其关联层级，防止物理层级展开
        // 进来的非需求空间被字段继承出非空配置后写入结果表（预计算系统的通用不变量）。
        Set<DemandInstanceValueObject> effectiveInstances =
                this.filterInstancesByDemandSpaces(templateId, instances);
        if (CollectionUtils.isEmpty(effectiveInstances)) {
            return;
        }
        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds =
                this.toInstanceTypeAndInstanceIds(effectiveInstances);
        DemandConfigByInstanceBatchQueryParam queryParam = new DemandConfigByInstanceBatchQueryParam();
        queryParam.setTemplateId(templateId);
        queryParam.setSceneId(sceneId);
        queryParam.setInstanceTypeAndInstanceIds(instanceTypeAndInstanceIds);
        queryParam.setAutoFillEmptyInstanceIds(false);
        queryParam.setQueryInheritConfig(true);
        queryParam.setFilterShieldedSpaces(false);
        queryParam.setFillInstanceName(false);

        List<DemandConfigAggregate<T>> aggregates =
                this.demandConfigGateway.listStableConfigsByInstances(queryParam);
        if (CollectionUtils.isEmpty(aggregates)) {
            // 稳定计算链路已经算不到这些实例的有效结果，结果表不能继续保留旧快照，
            // 否则后续读取预计算结果会拿到过期配置。
            List<DemandConfigResult> deleteParams = this.toDeleteParams(templateId, sceneId, effectiveInstances);
            this.deleteResults(deleteParams);
            return;
        }

        // 这里做“稳定计算结果”和“预计算结果表”的差异同步：
        // 有finalConfig的实例新增/更新，稳定计算未命中的实例删除旧结果。
        List<DemandConfigResult> upsertParams = this.toUpsertParams(aggregates);
        // 网关只会返回当前仍有效的稳定聚合结果；对本次要求刷新但稳定计算未命中的实例，
        // 需要删除结果表旧快照，避免后续预计算查询读到已经失效的配置。
        List<DemandConfigResult> deleteParams = this.toMissingDeleteParams(templateId, sceneId, effectiveInstances, aggregates);
        this.demandConfigResultRepository.syncResults(deleteParams, upsertParams);
    }

    /**
     * 按需求空间过滤实例集合。
     *
     * <p>结果表只保存需求空间及其关联层级，这是预计算系统的通用不变量。实例级刷新入口
     * （配置事件、黑名单移出、Debug 单实例）统一在此过滤，防止物理层级展开进来的非需求
     * 空间被字段继承出非空配置后写入结果表。过滤采用全项目需求空间口径，不依赖各需求
     * 策略对按实例范围过滤的支持度；需求空间查询失败返回空集合时只做保守过滤（跳过
     * 空间/楼层/建筑刷新），不会删除结果表旧行。</p>
     *
     * @param templateId 模板ID
     * @param instances  待刷新实例集合
     * @return 过滤后的实例集合
     * @since v7.260801.0
     */
    private @NonNull Set<DemandInstanceValueObject> filterInstancesByDemandSpaces(
            @NonNull Long templateId,
            @NonNull Collection<DemandInstanceValueObject> instances) {
        Set<String> spaceIds = new HashSet<>();
        Set<String> floorIds = new HashSet<>();
        Set<String> buildingIds = new HashSet<>();
        for (DemandInstanceValueObject instance : instances) {
            switch (instance.getInstanceType()) {
                case SPACE:
                    spaceIds.add(instance.getInstanceId());
                    break;
                case FLOOR:
                    floorIds.add(instance.getInstanceId());
                    break;
                case BUILDING:
                    buildingIds.add(instance.getInstanceId());
                    break;
                default:
                    break;
            }
        }
        if (spaceIds.isEmpty() && floorIds.isEmpty() && buildingIds.isEmpty()) {
            // 仅有项目等上级实例时无需过滤，也避免无意义的需求空间查询
            return new LinkedHashSet<>(instances);
        }
        DemandTemplateTypeEnum templateType =
                this.queryTemplate(templateId).getDemandTemplateType();
        List<DtpRwdSpaceDTO> demandSpaces = this.listDemandSpaces(templateType);
        if (CollectionUtils.isEmpty(demandSpaces)) {
            // 无需求空间时只保留项目实例，与定时任务路径“无需求空间删除整类结果”的收敛语义一致
            return instances.stream()
                    .filter(item -> item.getInstanceType() == DemandInstanceTypeEnum.PROJECT)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        Set<String> demandSpaceIds = new HashSet<>();
        Set<String> demandFloorIds = new HashSet<>();
        Set<String> demandBuildingIds = new HashSet<>();
        for (DtpRwdSpaceDTO demandSpace : demandSpaces) {
            if (StringUtils.isNotBlank(demandSpace.getId())) {
                demandSpaceIds.add(demandSpace.getId());
            }
            if (StringUtils.isNotBlank(demandSpace.getFloorId())) {
                demandFloorIds.add(demandSpace.getFloorId());
            }
            if (StringUtils.isNotBlank(demandSpace.getBuildingId())) {
                demandBuildingIds.add(demandSpace.getBuildingId());
            }
        }
        return instances.stream()
                .filter(instance -> {
                    switch (instance.getInstanceType()) {
                        case SPACE:
                            return demandSpaceIds.contains(instance.getInstanceId());
                        case FLOOR:
                            return demandFloorIds.contains(instance.getInstanceId());
                        case BUILDING:
                            return demandBuildingIds.contains(instance.getInstanceId());
                        case PROJECT:
                            return true;
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 查询模板类型的全项目需求空间。
     *
     * <p>统一采用全项目口径，不按来源实例范围过滤：各需求策略对范围过滤的支持度不一致
     * （通风策略经 BaseRunningMonitorStrategy 委托仅支持单实例且空间过滤不生效），全项目
     * 查询是所有需求策略共有的正确口径，与定时任务全量刷新路径保持一致。包内可见以支持
     * 测试子类替换需求空间查询，稳定验证过滤行为。</p>
     *
     * @param templateType 模板类型
     * @return 需求空间列表
     * @since v7.260801.0
     */
    @NonNull List<DtpRwdSpaceDTO> listDemandSpaces(
            @NonNull DemandTemplateTypeEnum templateType) {
        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(templateType);
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id,
                DtpRwdSpaceDTO.Fields.floorId, DtpRwdSpaceDTO.Fields.buildingId);
        return templateType.toDemandTypeEnum().demandStrategy().listDemandSpace(queryParam);
    }

    /**
     * 根据查询参数刷新。
     *
     * @param queryParam 查询参数
     * @since v7.260701.0
     */
    private void refreshByQueryParam(@NonNull DemandConfigByInstanceBatchQueryParam queryParam) {
        queryParam.setQueryInheritConfig(true);
        queryParam.setFilterShieldedSpaces(false);
        queryParam.setFillInstanceName(false);
        List<DemandConfigAggregate<T>> aggregates =
                this.demandConfigGateway.listStableConfigsByInstances(queryParam);
        List<DemandConfigResult> upsertParams = this.toUpsertParams(aggregates);
        DemandConfigResultRepositoryQueryParam scopeParam = this.toRefreshScopeQueryParam(queryParam);
        this.demandConfigResultRepository.syncRefreshScope(scopeParam, upsertParams);
    }

    /**
     * 根据查询参数和模板类型级预取的屏蔽空间刷新。
     *
     * @param queryParam       查询参数
     * @param templateType    模板类型
     * @param shieldedSpaceIds 已预取的不可变屏蔽空间ID集合
     * @since v7.260701.0
     */
    private void refreshByQueryParam(
            @NonNull DemandConfigByInstanceBatchQueryParam queryParam,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Set<String> shieldedSpaceIds) {
        queryParam.setQueryInheritConfig(true);
        queryParam.setFilterShieldedSpaces(false);
        queryParam.setFillInstanceName(false);
        List<DemandConfigAggregate<T>> aggregates =
                this.demandConfigGateway.listStableConfigsByInstances(queryParam);
        Map<DemandTemplateTypeEnum, Set<String>> shieldedSpaceIdsByTemplateType =
                Collections.singletonMap(templateType, shieldedSpaceIds);
        List<DemandConfigResult> upsertParams =
                this.toUpsertParams(aggregates, shieldedSpaceIdsByTemplateType);
        DemandConfigResultRepositoryQueryParam scopeParam = this.toRefreshScopeQueryParam(queryParam);
        this.demandConfigResultRepository.syncRefreshScope(scopeParam, upsertParams);
    }

    /**
     * 按模板维度受控并行刷新。
     *
     * <p>同一模板类型下每个模板的需求空间范围一致，先预取一次实例ID后再分批并行；
     * 模板通常不超过十个，这里保留受控并发窗口，避免多项目同时刷新时把全局业务线程池和数据库连接打满。</p>
     *
     * @param templateType                模板类型
     * @param templateIds                 模板ID集合
     * @param instanceTypeAndInstanceIds  实例类型和实例ID映射
     * @param shieldedSpaceIds            已预取的不可变屏蔽空间ID集合
     * @since v7.260701.0
     */
    private void refreshTemplatesInParallel(
            @NonNull PerformanceTracker tracker,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull List<Long> templateIds,
            @NonNull Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds,
            @NonNull Set<String> shieldedSpaceIds) {
        for (List<Long> templateIdPartition : Lists.partition(templateIds,
                DemandConfigResultRefreshRepositoryImpl.REFRESH_TEMPLATE_ID_PARALLEL_SIZE)) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Long templateId : templateIdPartition) {
                PerformanceTracker.ParallelNode parallelNode = tracker.enterParallel(
                        String.format("refreshTemplate[%s]", templateId));
                futures.add(CompletableFuture.runAsync(
                        () -> {
                            try (PerformanceTracker.ParallelScope ignored = parallelNode.openScope()) {
                                this.refreshTemplate(
                                        templateType, templateId, instanceTypeAndInstanceIds, shieldedSpaceIds);
                                parallelNode.recordStep("刷新单模板结果");
                            } catch (RuntimeException e) {
                                this.logRefreshError("并发刷新模板结果失败", e, templateId, null,
                                        this.countInstanceIds(instanceTypeAndInstanceIds));
                                throw e;
                            }
                        },
                        this.templateRefreshExecutor()));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            tracker.recordStep(String.format(Locale.ROOT, "完成模板分片刷新[%d]", templateIdPartition.size()));
        }
    }

    /**
     * 获取模板并发刷新执行器。
     *
     * <p>生产环境继续使用公共TTL线程池；包内测试可替换执行器以稳定验证并发编排，不改变并发上限和分片逻辑。</p>
     *
     * @return 模板刷新执行器
     * @since v7.260701.0
     */
    @NonNull Executor templateRefreshExecutor() {
        return BusinessThreadPoolExecutor.ttlExecutor();
    }

    /**
     * 刷新单个模板。
     *
     * @param templateType                模板类型
     * @param templateId                  模板ID
     * @param instanceTypeAndInstanceIds  实例类型和实例ID映射
     * @param shieldedSpaceIds            已预取的不可变屏蔽空间ID集合
     * @since v7.260701.0
     */
    void refreshTemplate(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Long templateId,
            @NonNull Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds,
            @NonNull Set<String> shieldedSpaceIds) {
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("刷新需求配置结果");
        try {
            try (PerformanceTracker.SafeNode safeNode = tracker.enter(
                    "DemandConfigResultRefreshRepositoryImpl#refreshTemplate")) {
                this.refreshByTemplateScope(
                        templateType, templateId, instanceTypeAndInstanceIds, shieldedSpaceIds);
                safeNode.recordStep("刷新空调模板结果");
            }
        } catch (RuntimeException e) {
            this.logRefreshError("刷新单模板结果失败", e, templateId, null,
                    this.countInstanceIds(instanceTypeAndInstanceIds));
            throw e;
        }
    }

    /**
     * 刷新空调模板下的结果数据。
     *
     * @param templateId                 模板ID
     * @param instanceTypeAndInstanceIds 实例类型和实例ID映射
     * @param autoFillEmptyInstanceIds   是否自动补齐空实例ID
     * @since v7.260701.0
     */
    private void refreshByTemplateScope(
            @NonNull Long templateId,
            @NonNull Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds,
            boolean autoFillEmptyInstanceIds) {
        DemandConfigByInstanceBatchQueryParam queryParam = new DemandConfigByInstanceBatchQueryParam();
        queryParam.setTemplateId(templateId);
        // 每个刷新任务会在网关内补充/规整查询参数，必须复制实例范围，避免并行任务共享可变集合。
        queryParam.setInstanceTypeAndInstanceIds(this.copyInstanceTypeAndInstanceIds(instanceTypeAndInstanceIds));
        queryParam.setAutoFillEmptyInstanceIds(autoFillEmptyInstanceIds);
        this.refreshByQueryParam(queryParam);
    }

    /**
     * 使用模板类型级预取的屏蔽空间刷新结果数据。
     *
     * <p>模板类型刷新已经预填充需求对象范围，因此屏蔽状态也在进入模板并发任务前按同一范围查询一次。
     * 每个模板只复用不可变集合完成结果转换，避免按模板或模板分片重复查询相同屏蔽空间。</p>
     *
     * @param templateType               模板类型
     * @param templateId                 模板ID
     * @param instanceTypeAndInstanceIds 实例类型和实例ID映射
     * @param shieldedSpaceIds           已预取的不可变屏蔽空间ID集合
     * @since v7.260701.0
     */
    private void refreshByTemplateScope(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Long templateId,
            @NonNull Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds,
            @NonNull Set<String> shieldedSpaceIds) {
        DemandConfigByInstanceBatchQueryParam queryParam = new DemandConfigByInstanceBatchQueryParam();
        queryParam.setTemplateId(templateId);
        // 每个刷新任务会在网关内补充/规整查询参数，必须复制实例范围，避免并行任务共享可变集合。
        queryParam.setInstanceTypeAndInstanceIds(this.copyInstanceTypeAndInstanceIds(instanceTypeAndInstanceIds));
        queryParam.setAutoFillEmptyInstanceIds(false);
        this.refreshByQueryParam(queryParam, templateType, shieldedSpaceIds);
    }

    /**
     * 查询模板。
     *
     * <p>包内可见以支持测试子类替换模板解析，避免单测依赖 Spring 容器。</p>
     *
     * @param templateId 模板ID
     * @return 模板
     * @since v7.260701.0
     */
    @NonNull DemandTemplate queryTemplate(@NonNull Long templateId) {
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateId(templateId);
        DemandTemplate template = templateParam.queryAndGetTemplate();
        if (Objects.isNull(template)) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT,
                    LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
        }
        return template;
    }

    /**
     * 查询模板类型下的模板ID。
     *
     * @param templateType 模板类型
     * @param safeNode     性能追踪节点
     * @return 模板ID集合
     * @since v7.260701.0
     */
    private @NonNull List<Long> listTemplateIds(
            @NonNull DemandTemplateTypeEnum templateType,
            PerformanceTracker.@NonNull SafeNode safeNode) {
        List<Long> templateIds = this.demandTemplateRepository.listTemplateIdsByTemplateType(templateType);
        safeNode.recordStep("查询模板ID列表");
        return CollectionUtils.isEmpty(templateIds) ? Collections.emptyList() : templateIds;
    }

    /**
     * 构造批量刷新范围查询参数。
     *
     * <p>批量刷新必须以模板和场景为完整边界同步结果。旧结果查询和删除差异计算由结果仓储在
     * 获取范围写锁后执行，避免并发刷新在锁外读取旧结果后遗漏其他刷新刚写入的失效数据。</p>
     *
     * @param queryParam 稳定配置查询参数
     * @return 刷新范围查询参数
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResultRepositoryQueryParam toRefreshScopeQueryParam(
            @NonNull DemandConfigByInstanceBatchQueryParam queryParam) {
        DemandConfigResultRepositoryQueryParam resultQueryParam = new DemandConfigResultRepositoryQueryParam();
        resultQueryParam.setRelTemplateId(queryParam.getTemplateId());
        Long sceneId = queryParam.getSceneId();
        if (Objects.nonNull(sceneId)) {
            resultQueryParam.setRelSceneId(sceneId);
        } else {
            resultQueryParam.setRelSceneIdIsNull(true);
        }
        return resultQueryParam;
    }

    /**
     * 按实例类型聚合实例ID。
     *
     * @param instances 实例集合
     * @return 实例类型和实例ID映射
     * @since v7.260701.0
     */
    private @NonNull Map<DemandInstanceTypeEnum, Set<String>> toInstanceTypeAndInstanceIds(
            @NonNull Collection<DemandInstanceValueObject> instances) {
        Map<DemandInstanceTypeEnum, Set<String>> result = new LinkedHashMap<>();
        for (DemandInstanceValueObject instance : instances) {
            MapExecutor.dynamicPut(result, instance.getInstanceType(), instance.getInstanceId(), LinkedHashSet::new);
        }
        return result;
    }

    /**
     * 转换需求管理范围内实例ID。
     *
     * @param templateType 模板类型
     * @return 实例类型和实例ID映射
     * @since v7.260701.0
     */
    @NonNull Map<DemandInstanceTypeEnum, Set<String>> toDemandScopeInstanceTypeAndInstanceIds(
            @NonNull DemandTemplateTypeEnum templateType) {
        DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();
        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(templateType);
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id,
                DtpRwdSpaceDTO.Fields.floorId, DtpRwdSpaceDTO.Fields.buildingId);
        queryParam.setFilterInBlacklistedSpace(true);
        queryParam.setFilterInShieldedSpace(false);
        List<DtpRwdSpaceDTO> demandSpaces = demandStrategy.listDemandSpace(queryParam);
        if (CollectionUtils.isEmpty(demandSpaces)) {
            // 没有需求空间时没有可刷新对象，不能回退成“全实例类型空ID”的查询，
            // 否则网关会重新自动补全实例范围，反而触发不必要的全量计算。
            return Collections.emptyMap();
        }
        Set<String> spaceIds = new LinkedHashSet<>();
        Set<String> floorIds = new LinkedHashSet<>();
        Set<String> buildingIds = new LinkedHashSet<>();
        for (DtpRwdSpaceDTO demandSpace : demandSpaces) {
            if (StringUtils.isNotBlank(demandSpace.getId())) {
                spaceIds.add(demandSpace.getId());
            }
            if (StringUtils.isNotBlank(demandSpace.getFloorId())) {
                floorIds.add(demandSpace.getFloorId());
            }
            if (StringUtils.isNotBlank(demandSpace.getBuildingId())) {
                buildingIds.add(demandSpace.getBuildingId());
            }
        }
        Map<DemandInstanceTypeEnum, Set<String>> result = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(spaceIds)) {
            MapExecutor.dynamicPutAll(result, DemandInstanceTypeEnum.SPACE, spaceIds, LinkedHashSet::new);
        }
        if (CollectionUtils.isNotEmpty(floorIds)) {
            MapExecutor.dynamicPutAll(result, DemandInstanceTypeEnum.FLOOR, floorIds, LinkedHashSet::new);
        }
        if (CollectionUtils.isNotEmpty(buildingIds)) {
            MapExecutor.dynamicPutAll(result, DemandInstanceTypeEnum.BUILDING, buildingIds, LinkedHashSet::new);
        }
        result.put(DemandInstanceTypeEnum.PROJECT, Sets.newHashSet(ContextUtil.getProjectId()));
        return result;
    }

    /**
     * 复制实例范围，隔离模板并行刷新任务之间的可变入参。
     *
     * <p>刷新前已经按需求空间预填充实例范围，并关闭网关自动补全；这里仍为每个模板任务复制
     * 独立的 {@link Set}，避免后续上下文初始化或网关逻辑调整时修改共享集合，导致并行任务互相影响。</p>
     *
     * @param source 实例类型和实例ID映射
     * @return 独立的实例类型和实例ID映射副本
     * @since v7.260701.0
     */
    private @NonNull Map<DemandInstanceTypeEnum, Set<String>> copyInstanceTypeAndInstanceIds(
            @NonNull Map<DemandInstanceTypeEnum, Set<String>> source) {
        Map<DemandInstanceTypeEnum, Set<String>> result = new LinkedHashMap<>();
        for (Map.Entry<DemandInstanceTypeEnum, Set<String>> entry : source.entrySet()) {
            Set<String> instanceIds = entry.getValue();
            if (Objects.nonNull(instanceIds)) {
                result.put(entry.getKey(), new LinkedHashSet<>(instanceIds));
            }
        }
        return result;
    }

    /**
     * 转换为结果表新增或更新参数集合。
     *
     * @param aggregates 需求配置聚合根集合
     * @return 预计算结果集合
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> toUpsertParams(
            @NonNull Collection<DemandConfigAggregate<T>> aggregates) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return Collections.emptyList();
        }
        List<DemandConfigAggregate<T>> effectiveAggregates = aggregates.stream()
                .filter(item -> Objects.nonNull(item.getFinalConfig()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(effectiveAggregates)) {
            return Collections.emptyList();
        }
        Map<DemandTemplateTypeEnum, Set<String>> shieldedSpaceIds =
                this.mapShieldedSpaceIds(effectiveAggregates);
        return this.toUpsertParams(effectiveAggregates, shieldedSpaceIds);
    }

    /**
     * 使用已查询的屏蔽空间ID转换结果表新增或更新参数集合。
     *
     * @param aggregates       需求配置聚合根集合
     * @param shieldedSpaceIds 模板类型到屏蔽空间ID的映射
     * @return 预计算结果集合
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> toUpsertParams(
            @NonNull Collection<DemandConfigAggregate<T>> aggregates,
            @NonNull Map<DemandTemplateTypeEnum, Set<String>> shieldedSpaceIds) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return Collections.emptyList();
        }
        return aggregates.stream()
                .filter(item -> Objects.nonNull(item.getFinalConfig()))
                .map(aggregate -> this.toUpsertParam(aggregate, shieldedSpaceIds))
                .collect(Collectors.toList());
    }

    /**
     * 转换为结果表新增或更新参数。
     *
     * @param aggregate        需求配置聚合根
     * @param shieldedSpaceIds 模板类型到屏蔽空间ID的映射
     * @return 预计算结果
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResult toUpsertParam(
            @NonNull DemandConfigAggregate<T> aggregate,
            @NonNull Map<DemandTemplateTypeEnum, Set<String>> shieldedSpaceIds) {
        DemandConfigIdentifier identifier = aggregate.getIdentifier();
        DemandInstanceValueObject instance = identifier.getInstance();
        // 预计算结果表保存稳定JSON快照，但是否自定义、是否屏蔽必须来自聚合和屏蔽配置，
        // 不能当作普通默认值填充。
        JsonNode finalConfigJson = ObjectMapperUtils.convertValue(aggregate.getFinalConfig(), JsonNode.class);
        this.removeAirconditioningQueryFields(identifier.getTemplateType(), finalConfigJson);
        JsonNode inheritInstancesJson = ObjectMapperUtils.convertValue(aggregate.getInheritInstanceAndModes(), JsonNode.class);
        DemandConfigResult result = new DemandConfigResult();
        result.setProjectId(ContextUtil.getProjectId());
        result.setDemandTemplateType(identifier.getTemplateType());
        result.setDemandInstanceType(instance.getInstanceType());
        result.setDemandInstanceId(instance.getInstanceId());
        result.setFinalConfigJson(finalConfigJson);
        result.setIsCustomizedConfig(aggregate.isCustomizedConfig());
        result.setIsShielded(this.isShielded(identifier.getTemplateType(), instance, shieldedSpaceIds));
        result.setInheritInstancesJson(inheritInstancesJson);
        result.setRelTemplateId(identifier.getTemplateId());
        result.setRelSceneId(Objects.nonNull(identifier.getScene()) ? identifier.getScene().getId() : null);
        return result;
    }

    /**
     * 移除空调查询期动态字段，防止历史脏原始配置污染稳定结果JSON。
     *
     * <p>项目价值选择和空调工况由专表、当前DTX/RWD及本地化信息在查询时生成。
     * 使用JSON节点删除字段可以避免修改聚合根及其继承对象引用。</p>
     *
     * @param templateType 模板类型
     * @param finalConfigJson 最终配置JSON
     * @since v7.260701.0
     */
    private void removeAirconditioningQueryFields(
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable JsonNode finalConfigJson) {
        if (templateType != DemandTemplateTypeEnum.AIRCONDITIONING
                || !(finalConfigJson instanceof ObjectNode)) {
            return;
        }
        JsonNode globalConfigJson = finalConfigJson.get(DemandConfigValueObject.Fields.globalConfig);
        if (!(globalConfigJson instanceof ObjectNode)) {
            return;
        }
        ((ObjectNode) globalConfigJson).remove(Arrays.asList(
                DemandConfigAirconditioningValueObject.Fields.valueSelect,
                DemandConfigAirconditioningValueObject.Fields.workCondition));
    }

    /**
     * 转换为结果表删除定位参数集合。
     *
     * @param templateId 模板ID
     * @param sceneId    场景ID
     * @param instances  实例集合
     * @return 预计算结果删除定位参数集合
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> toDeleteParams(
            @NonNull Long templateId,
            @Nullable Long sceneId,
            @NonNull Collection<DemandInstanceValueObject> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return Collections.emptyList();
        }
        DemandTemplate template = this.queryTemplate(templateId);
        return instances.stream()
                .map(instance -> this.toDeleteParam(template, templateId, sceneId, instance))
                .collect(Collectors.toList());
    }

    /**
     * 转换为指定模板类型的结果表删除定位参数集合。
     *
     * @param templateType 模板类型
     * @param templateId   模板ID
     * @param sceneId      场景ID
     * @param instances    实例集合
     * @return 预计算结果删除定位参数集合
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> toDeleteParams(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Long templateId,
            @Nullable Long sceneId,
            @NonNull Collection<DemandInstanceValueObject> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return Collections.emptyList();
        }
        return instances.stream()
                .map(instance -> this.toDeleteParam(templateType, templateId, sceneId, instance))
                .collect(Collectors.toList());
    }

    /**
     * 转换为结果表删除定位参数。
     *
     * @param template   模板
     * @param templateId 模板ID
     * @param sceneId    场景ID
     * @param instance   实例
     * @return 预计算结果删除定位参数
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResult toDeleteParam(
            @NonNull DemandTemplate template,
            @NonNull Long templateId,
            @Nullable Long sceneId,
            @NonNull DemandInstanceValueObject instance) {
        return this.toDeleteParam(template.getDemandTemplateType(), templateId, sceneId, instance);
    }

    /**
     * 转换为结果表删除定位参数。
     *
     * @param templateType 模板类型
     * @param templateId   模板ID
     * @param sceneId      场景ID
     * @param instance     实例
     * @return 预计算结果删除定位参数
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResult toDeleteParam(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Long templateId,
            @Nullable Long sceneId,
            @NonNull DemandInstanceValueObject instance) {
        DemandConfigResult result = new DemandConfigResult();
        result.setProjectId(ContextUtil.getProjectId());
        result.setDemandTemplateType(templateType);
        result.setDemandInstanceType(instance.getInstanceType());
        result.setDemandInstanceId(instance.getInstanceId());
        result.setRelTemplateId(templateId);
        result.setRelSceneId(sceneId);
        return result;
    }

    /**
     * 转换结果表未命中实例的删除定位参数。
     *
     * @param templateId 模板ID
     * @param sceneId    场景ID
     * @param instances  待刷新实例集合
     * @param aggregates 稳定计算得到的聚合根集合
     * @return 预计算结果删除定位参数集合
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> toMissingDeleteParams(
            @NonNull Long templateId,
            @Nullable Long sceneId,
            @NonNull Collection<DemandInstanceValueObject> instances,
            @NonNull List<DemandConfigAggregate<T>> aggregates) {
        Set<DemandInstanceValueObject> existsInstances = aggregates.stream()
                .filter(item -> Objects.nonNull(item.getFinalConfig()))
                .map(item -> item.getIdentifier().getInstance())
                .collect(Collectors.toSet());
        // 请求刷新但稳定结果未返回finalConfig的实例，代表当前继承链路下已经没有有效结果，
        // 需要删除旧预计算数据，而不是把旧结果继续留在结果表。
        List<DemandInstanceValueObject> missingInstances = instances.stream()
                .filter(instance -> !existsInstances.contains(instance))
                .collect(Collectors.toList());
        return this.toDeleteParams(templateId, sceneId, missingInstances);
    }

    /**
     * 删除预计算结果并关闭开放快照。
     *
     * @param results 待删除结果定位参数集合
     * @since v7.260701.0
     */
    private void deleteResults(@NonNull Collection<DemandConfigResult> results) {
        if (CollectionUtils.isEmpty(results)) {
            return;
        }
        this.demandConfigResultRepository.syncResults(results, Collections.emptyList());
    }

    /**
     * 删除模板类型下的全部预计算结果并关闭开放快照。
     *
     * <p>当物理世界变更导致需求类型下已经没有需求空间时，继续跳过刷新会保留旧结果。
     * 这里按模板类型整体清理，确保结果表只保留当前物理世界仍可计算出的配置。</p>
     *
     * @param templateType 模板类型
     * @param templateIds 模板ID集合
     * @since v7.260701.0
     */
    private void deleteResultsByTemplateType(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<Long> templateIds) {
        for (Long templateId : templateIds) {
            /*
             * 删除按无场景空调模板边界执行，而不是只用 templateType 一次性清理：
             * 1. SQL 可以命中 project + template + scene 索引前缀；
             * 2. Repository 能在删除前拿到和 upsert 相同粒度的写锁；
             * 3. 每个边界内部再按固定大小分页删除，避免一次加载全部ID。
             */
            this.deleteResultsByTemplate(templateType, templateId);
        }
    }

    /**
     * 删除指定空调模板下的预计算结果。
     *
     * @param templateType 模板类型
     * @param templateId   模板ID
     * @since v7.260701.0
     */
    private void deleteResultsByTemplate(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Long templateId) {
        DemandConfigResultRepositoryQueryParam queryParam = new DemandConfigResultRepositoryQueryParam();
        queryParam.setDemandTemplateType(templateType);
        queryParam.setRelTemplateId(templateId);
        queryParam.setRelSceneIdIsNull(true);
        this.demandConfigResultRepository.removeResults(queryParam);
    }

    /**
     * 判断实例是否为屏蔽空间。
     *
     * @param templateType 模板类型
     * @param instance     实例
     * @return 是否屏蔽
     * @since v7.260701.0
     */
    private boolean isShielded(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandInstanceValueObject instance,
            @NonNull Map<DemandTemplateTypeEnum, Set<String>> shieldedSpaceIds) {
        if (instance.getInstanceType() != DemandInstanceTypeEnum.SPACE) {
            return false;
        }
        Set<String> instanceIds = shieldedSpaceIds.get(templateType);
        return CollectionUtils.isNotEmpty(instanceIds) && instanceIds.contains(instance.getInstanceId());
    }

    /**
     * 按模板类型刷新范围一次性查询并冻结屏蔽空间ID。
     *
     * <p>返回集合始终非空引用且不包含空值。空集合表示该需求范围内没有屏蔽空间，
     * 不作为“未预取”的哨兵值；并发模板任务只读复用该集合。</p>
     *
     * @param templateType                 模板类型
     * @param instanceTypeAndInstanceIds   已预填充的实例类型和实例ID映射
     * @return 不可变屏蔽空间ID集合
     * @since v7.260701.0
     */
    private @NonNull Set<String> listImmutableShieldedSpaceIds(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds) {
        Set<String> spaceIds = instanceTypeAndInstanceIds.get(DemandInstanceTypeEnum.SPACE);
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Collections.emptySet();
        }
        Set<String> shieldedSpaceIds = this.listShieldedSpaceIds(templateType, spaceIds);
        if (CollectionUtils.isEmpty(shieldedSpaceIds)) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(shieldedSpaceIds));
    }

    /**
     * 批量查询屏蔽空间ID。
     *
     * <p>单实例、配置增量、DTX 或单模板刷新仍需按实际聚合结果查询屏蔽状态。
     * 这里按模板类型和空间ID分片查询后在内存判断，避免每个聚合根单独查库。</p>
     *
     * @param aggregates 有效聚合根集合
     * @return 模板类型到屏蔽空间ID的映射
     * @since v7.260701.0
     */
    private @NonNull Map<DemandTemplateTypeEnum, Set<String>> mapShieldedSpaceIds(
            @NonNull Collection<DemandConfigAggregate<T>> aggregates) {
        Map<DemandTemplateTypeEnum, Set<String>> queryInstanceIds = new LinkedHashMap<>();
        for (DemandConfigAggregate<T> aggregate : aggregates) {
            DemandConfigIdentifier identifier = aggregate.getIdentifier();
            DemandInstanceValueObject instance = identifier.getInstance();
            if (instance.getInstanceType() == DemandInstanceTypeEnum.SPACE
                    && StringUtils.isNotBlank(instance.getInstanceId())) {
                MapExecutor.dynamicPut(queryInstanceIds, identifier.getTemplateType(),
                        instance.getInstanceId(), LinkedHashSet::new);
            }
        }
        if (MapUtils.isEmpty(queryInstanceIds)) {
            return Collections.emptyMap();
        }
        Map<DemandTemplateTypeEnum, Set<String>> result = new LinkedHashMap<>();
        for (Map.Entry<DemandTemplateTypeEnum, Set<String>> entry : queryInstanceIds.entrySet()) {
            Set<String> shieldedInstanceIds = this.listShieldedSpaceIds(entry.getKey(), entry.getValue());
            if (CollectionUtils.isNotEmpty(shieldedInstanceIds)) {
                result.put(entry.getKey(), shieldedInstanceIds);
            }
        }
        return result;
    }

    /**
     * 分片查询指定模板类型下的屏蔽空间ID。
     *
     * @param templateType 模板类型
     * @param instanceIds  空间ID集合
     * @return 屏蔽空间ID集合
     * @since v7.260701.0
     */
    private @NonNull Set<String> listShieldedSpaceIds(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Set<String> instanceIds) {
        Set<String> result = new LinkedHashSet<>();
        for (List<String> instanceIdPartition : Lists.partition(new ArrayList<>(instanceIds), 500)) {
            DemandInstanceConfigRepositoryQueryParam queryParam = new DemandInstanceConfigRepositoryQueryParam();
            queryParam.setTemplateType(templateType);
            queryParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
            queryParam.setInstanceIds(new LinkedHashSet<>(instanceIdPartition));
            queryParam.setFilterConfig(new DemandInstanceConfigValueObject().setShielded(true));
            queryParam.addColumns(DemandInstanceConfig::getDemandInstanceId);
            this.demandInstanceRepository.listConfig(queryParam).stream()
                    .map(DemandInstanceConfig::getDemandInstanceId)
                    .filter(StringUtils::isNotBlank)
                    .forEach(result::add);
        }
        return result;
    }

    /**
     * 批量展开实例及其下级实例。
     *
     * <p>配置变更入口可能一次携带多个同类型对象。这里按实例类型批量查询下级，避免逐个实例调用
     * RWD 并再逐个推断子级类型。</p>
     *
     * @param instanceType 来源实例类型
     * @param instanceIds  来源实例ID集合
     * @return 来源实例及下级实例集合
     * @since v7.260701.0
     */
    private @NonNull Set<DemandInstanceValueObject> expandInstancesAndChildren(
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull Set<String> instanceIds) {
        Set<DemandInstanceValueObject> result = new LinkedHashSet<>();
        this.addInstances(result, instanceType, instanceIds);
        if (instanceType == DemandInstanceTypeEnum.SPACE) {
            return result;
        }
        if (instanceType == DemandInstanceTypeEnum.PROJECT) {
            this.addInstances(result, DemandInstanceTypeEnum.BUILDING,
                    this.instanceService.listBuildingIdsByInstance(instanceType, instanceIds));
        }
        if (instanceType == DemandInstanceTypeEnum.PROJECT || instanceType == DemandInstanceTypeEnum.BUILDING) {
            this.addInstances(result, DemandInstanceTypeEnum.FLOOR,
                    this.instanceService.listFloorIdsByInstance(instanceType, instanceIds));
        }
        this.addInstances(result, DemandInstanceTypeEnum.SPACE,
                this.instanceService.listSpaceIdsByInstance(instanceType, instanceIds));
        return result;
    }

    /**
     * 向实例集合中添加指定类型实例。
     *
     * @param result       实例集合
     * @param instanceType 实例类型
     * @param instanceIds  实例ID集合
     * @since v7.260701.0
     */
    private void addInstances(
            @NonNull Set<DemandInstanceValueObject> result,
            @NonNull DemandInstanceTypeEnum instanceType,
            @Nullable Set<String> instanceIds) {
        if (CollectionUtils.isEmpty(instanceIds)) {
            return;
        }
        for (String instanceId : instanceIds) {
            if (StringUtils.isNotBlank(instanceId)) {
                result.add(new DemandInstanceValueObject(instanceType, instanceId));
            }
        }
    }

    /**
     * 统计实例类型下实例数量。
     *
     * @param instanceTypeAndInstanceIds 实例类型和实例ID映射
     * @return 实例总数
     * @since v7.260701.0
     */
    private @NonNull Integer countInstanceIds(
            @Nullable Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds) {
        if (MapUtils.isEmpty(instanceTypeAndInstanceIds)) {
            return 0;
        }
        int result = 0;
        for (Set<String> instanceIds : instanceTypeAndInstanceIds.values()) {
            result += CollectionUtils.size(instanceIds);
        }
        return result;
    }

    /**
     * 输出刷新异常日志。
     *
     * @param message       日志描述
     * @param e             异常
     * @param templateId    模板ID
     * @param sceneId       场景ID
     * @param instanceCount 实例数量
     * @since v7.260701.0
     */
    private void logRefreshError(
            @NonNull String message,
            @NonNull RuntimeException e,
            @Nullable Long templateId,
            @Nullable Long sceneId,
            @Nullable Integer instanceCount) {
        this.logProjectInstance().error(message, e, Arrays.asList(
                Pair.of("templateId", templateId),
                Pair.of("sceneId", sceneId),
                Pair.of("instanceCount", instanceCount)));
    }

    @Override
    public @NonNull String logTag() {
        return "需求管理-配置结果刷新仓储";
    }

}
