package com.persagy.apccontrol.basic.structure.demand.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import com.persagy.apccontrol.basic.model.context.demand.DemandConfigAggregateInitByInstanceContext;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.*;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigGroupNameCheckParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigResultBatchQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandReferenceParentSpaceInFloorQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigResultVO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandInstanceTagVO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.context.DemandConfigCopyContext;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConfigConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceConfigChangeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateChangeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandInstanceConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandSceneChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandTemplateChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.param.DemandInstanceConfigQueryParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.*;
import com.persagy.apccontrol.basic.structure.demand.lifecycle.config.DemandConfigAirconditoningAware;
import com.persagy.apccontrol.basic.structure.demand.lifecycle.config.DemandConfigAirconditioningQueryEnricher;
import com.persagy.apccontrol.basic.structure.demand.lifecycle.config.DemandConfigLifeAware;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.dto.DemandCustomizedInstanceDTO;
import com.persagy.apccontrol.basic.structure.demand.model.dto.DemandInstanceCustomizedTagDTO;
import com.persagy.apccontrol.basic.structure.demand.model.instance.domain.DemandInstanceConfigEntity;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandConfigGroupEntity;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.*;
import com.persagy.apccontrol.basic.structure.demand.repository.param.*;
import com.persagy.apccontrol.basic.structure.demand.repository.scene.DemandSceneRepository;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.DemandConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 需求配置网关实现
 *
 * @param <T> 配置类型
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Component
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigGatewayImpl<T extends BaseDemandConfig> implements DemandConfigGateway<T> {

    private final DtpRwdClientService dtpRwdClientService;
    private final DemandConfigConverter demandConfigConverter;
    private final DemandTemplateRepository demandTemplateRepository;
    private final DemandSceneRepository demandSceneRepository;
    private final DemandInstanceRepository demandInstanceRepository;
    private final DemandConfigRepository demandConfigRepository;
    private final DemandConfigGroupRepository demandConfigGroupRepository;
    private final InstanceService instanceService;
    private final List<DemandConfigLifeAware<T>> demandConfigLifeAware;

    /**
     * 需求配置预计算结果仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull DemandConfigResultRepository demandConfigResultRepository;

    /**
     * 需求实例配置查询网关。
     *
     * @since v7.260701.0
     */
    private final @NonNull DemandInstanceGateway demandInstanceGateway;

    /**
     * 空调需求查询期动态配置补全器。
     *
     * @since v7.260701.0
     */
    private final @NonNull DemandConfigAirconditioningQueryEnricher demandConfigAirconditioningQueryEnricher;

    @Override
    public Optional<DemandTemplate> getByTemplate(@NonNull BaseDemandTemplateParam param) {
        return Optional.ofNullable(param.queryAndGetTemplate());
    }

    @Override
    public Optional<DemandConfigAggregate<T>> getConfigByInstance(@NonNull DemandConfigByInstanceQueryParam param) {
        DemandConfigByInstanceBatchQueryParam batchQueryParam = new DemandConfigByInstanceBatchQueryParam();
        batchQueryParam.setDate(param.getDate());
        batchQueryParam.setTemplateId(param.getTemplateId());
        batchQueryParam.setSceneId(param.getSceneId());
        batchQueryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(
                param.getInstance().getInstanceType(),
                Sets.newHashSet(param.getInstance().getInstanceId())));
        List<DemandConfigAggregate<T>> aggregates = listConfigsByInstances(batchQueryParam);
        if (CollectionUtils.isEmpty(aggregates)) {
            return Optional.empty();
        }
        return Optional.of(IterableUtils.first(aggregates));
    }

    @Override
    public boolean isExistsCustomizedInstance(@NonNull BaseDemandTemplateParam param) {
        Optional<DemandTemplate> templateOpt = getByTemplate(param);
        if (!templateOpt.isPresent()) {
            return false;
        }
        DemandTemplate template = templateOpt.get();
        return demandConfigRepository.countExistsCustomizedInstance(param.getDate(), template.getId()) > 0;
    }

    @Override
    public Set<String> listHasConfigSpaceIds(
            @NonNull BaseDemandTemplateParam param,
            @Nullable Set<String> filterSpaceIds) {
        Optional<DemandTemplate> template = getByTemplate(param);
        if (!template.isPresent()) {
            return Sets.newHashSet();
        }
        DemandConfigByInstanceBatchQueryParam queryParam = new DemandConfigByInstanceBatchQueryParam();
        queryParam.setDate(param.getDate());
        queryParam.setTemplateId(param.getTemplateId());
        queryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(DemandInstanceTypeEnum.SPACE, filterSpaceIds));
        queryParam.setAutoFillEmptyInstanceIds(CollectionUtils.isEmpty(filterSpaceIds));
        queryParam.setFilterShieldedSpaces(true);
        List<DemandConfigAggregate<T>> aggregates = listConfigsByInstances(queryParam);
        if (CollectionUtils.isEmpty(aggregates)) {
            return Sets.newHashSet();
        }
        return aggregates.stream()
                .filter(item -> Objects.nonNull(item.getFinalConfig()))
                .map(item -> item.getIdentifier().getInstance().getInstanceId())
                .collect(Collectors.toSet());
    }

    @Override
    public List<DemandConfigAggregate<T>> listOriginConfigs(
            @NonNull @RequestBody DemandConfigByInstanceRepositoryBatchQueryParam param) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        LocalDate date = param.getDate();
        if (Objects.nonNull(templateType)) {
            List<Long> templateIds = demandTemplateRepository.listTemplateIdsByTemplateType(templateType);
            if (CollectionUtils.isEmpty(templateIds)) {
                return Collections.emptyList();
            }
            param.setTemplateIds(templateIds);
        }
        List<DemandConfig> dbConfigs = demandConfigRepository.listByInstances(param);
        if (CollectionUtils.isEmpty(dbConfigs)) {
            return Collections.emptyList();
        }
        List<DemandConfigAggregate<T>> aggregates = Collections.synchronizedList(new ArrayList<>());
        MultiKeyMap<Long, List<DemandConfig>> templateSceneIdAndConfig =
                DemandConfig.toTemplateSceneIdAndConfigs(dbConfigs);
        Set<Long> templateIds = new HashSet<>();
        Set<Long> sceneIds = new HashSet<>();
        Set<Long> configGroupIds = new HashSet<>();
        Set<String> instanceIds = new HashSet<>();
        for (DemandConfig dbConfig : dbConfigs) {
            templateIds.add(dbConfig.getRelTemplateId());
            if (Objects.nonNull(dbConfig.getRelSceneId())) {
                sceneIds.add(dbConfig.getRelSceneId());
            }
            if (Objects.nonNull(dbConfig.getRelGroupId())) {
                configGroupIds.add(dbConfig.getRelGroupId());
            }
            instanceIds.add(dbConfig.getDemandInstanceId());
        }
        Map<Long, DemandTemplate> templateIdAndData = demandTemplateRepository.listByTemplateIds(templateIds).stream()
                .collect(Collectors.toMap(BaseAssignIdDO::getId, Function.identity(),
                        (o1, o2) -> o1));
        Map<Long, DemandSceneEntity> sceneIdAndData = CollectionUtils.isNotEmpty(sceneIds)
                ? demandSceneRepository.listByIdsAndDate(sceneIds, date).stream()
                .collect(Collectors.toMap(DemandSceneEntity::getId, Function.identity(),
                        (o1, o2) -> o1))
                : Collections.emptyMap();
        DemandConfigGroupRepositoryBatchQueryParam configGroupQueryParam =
                new DemandConfigGroupRepositoryBatchQueryParam();
        configGroupQueryParam.setConfigGroupIds(configGroupIds);
        configGroupQueryParam.setDate(date);
        Map<Long, DemandConfigGroupEntity> configGroupIdAndData = CollectionUtils.isNotEmpty(configGroupIds)
                ? demandConfigGroupRepository.list(configGroupQueryParam).stream()
                .map(demandConfigConverter::toConfigGroupEntity)
                .collect(Collectors.toMap(item -> item.getIdentifier().getGroupId(), Function.identity(),
                        (o1, o2) -> o1))
                : Collections.emptyMap();
        Map<String, String> instanceIdAndName = dtpRwdClientService.mapObjectIdAndLocalName(instanceIds);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<MultiKey<? extends Long>, List<DemandConfig>> entry : templateSceneIdAndConfig.entrySet()) {
            Long templateId = entry.getKey().getKey(0);
            Long sceneId = entry.getKey().getKey(1);
            List<DemandConfig> configs = entry.getValue();

            Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds = new HashMap<>();
            for (DemandConfig config : configs) {
                MapExecutor.dynamicPut(instanceTypeAndInstanceIds, config.getDemandInstanceType(),
                        config.getDemandInstanceId(), HashSet::new);
            }
            futures.add(CompletableFuture.runAsync(() -> {
                DemandTemplate template = templateIdAndData.get(templateId);
                DemandSceneEntity scene = sceneIdAndData.get(sceneId);
                if (Objects.isNull(template)) {
                    throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT,
                            String.format(LocaleMessageUtil.getMessage("key_c0605cc648374600a995d3f7f8e8092e"), templateId));
                }
                if (Objects.nonNull(sceneId) && Objects.isNull(scene)) {
                    throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT,
                            String.format(LocaleMessageUtil.getMessage("key_91a4fb8aaf2b4925a93321cba7f4296e"), sceneId));
                }
                DemandConfigByInstanceBatchQueryParam queryParam = new DemandConfigByInstanceBatchQueryParam();
                queryParam.setDate(date);
                queryParam.setTemplateId(templateId);
                queryParam.setSceneId(sceneId);
                queryParam.setInstanceTypeAndInstanceIds(instanceTypeAndInstanceIds);
                queryParam.setAutoFillEmptyInstanceIds(false);
                queryParam.setQueryInheritConfig(false);
                demandConfigLifeAware.forEach(item -> item.beforeContextInit(queryParam));
                DemandConfigAggregateInitByInstanceContext<T> context =
                        new DemandConfigAggregateInitByInstanceContext<>(queryParam);
                demandConfigLifeAware.forEach(item -> item.afterContextInit(context));
                // 填充需求模版
                context.setTemplate(template);
                // 填充场景
                context.setScene(scene);
                // 填充需求配置
                context.setConfigs(configs);
                // 填充需求分组配置
                context.setConfigGroupIdAndData(configGroupIdAndData);
                // 填充聚合根
                demandConfigLifeAware.forEach(item -> item.beforeAggregateInit(context));
                List<DemandConfigAggregate<T>> eachAggregates = context.toAggregates(context.getConfigs());
                // 填充实例名称
                context.setInstanceIdAndName(instanceIdAndName);
                context.fillInstanceName(eachAggregates);
                demandConfigLifeAware.forEach(item -> item.afterAggregateInit(eachAggregates));
                aggregates.addAll(eachAggregates);
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return aggregates;
    }

    @Override
    public @NonNull List<DemandConfigAggregate<T>> listStableConfigsByInstances(
            @NonNull DemandConfigByInstanceBatchQueryParam param) {
        // 稳定结果必须包含继承计算且不允许实例名称进入后续序列化边界。
        param.setQueryInheritConfig(true);
        param.setFillInstanceName(false);
        return this.listConfigsByInstances(param, false);
    }

    @Override
    public @NonNull List<DemandConfigAggregate<T>> listConfigsByInstances(
            @NonNull DemandConfigByInstanceBatchQueryParam param) {
        return this.listConfigsByInstances(param, true);
    }

    @Override
    public @NonNull List<DemandConfigResultVO<T>> listConfigResultsByInstances(
            @NonNull DemandConfigResultBatchQueryParam param) {
        DemandTemplate template = param.queryAndGetTemplate();
        if (Objects.isNull(template)) {
            this.logProjectInstance().warn("未查询到需求模板，返回空的需求配置结果", Arrays.asList(
                    Pair.of("templateType", param.getTemplateType()),
                    Pair.of("templateId", param.getTemplateId()),
                    Pair.of("templateTag", param.getTemplateTag()),
                    Pair.of("date", param.getDate())
            ));
            return Collections.emptyList();
        }
        Long templateId = template.getId();
        DemandConfigResultRepositoryQueryParam queryParam =
                this.toDemandConfigResultRepositoryQueryParam(param, templateId);
        List<DemandConfigResult> results = this.demandConfigResultRepository.list(queryParam);
        List<DemandConfigResultVO<T>> resultVOs =
                this.toDemandConfigResultVOs(results, param, template.getDemandTemplateType());
        // 结果表只保存稳定核心配置；按本次查询日期补充营业日历空调季和当前DTX/RWD运行态。
        this.demandConfigAirconditioningQueryEnricher.enrichResults(
                resultVOs,
                template.getDemandTemplateType(),
                templateId,
                param.getInstanceType(),
                param.getDate());
        return resultVOs;
    }

    /**
     * 转换需求配置结果，并按需补充实例标签.
     *
     * @param results 结果表记录
     * @param param 结果查询参数
     * @param templateType 需求模板类型
     * @return 需求配置结果VO
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResultVO<T>> toDemandConfigResultVOs(
            @NonNull List<DemandConfigResult> results,
            @NonNull DemandConfigResultBatchQueryParam param,
            @NonNull DemandTemplateTypeEnum templateType) {
        // 只有显式查询实例标签时才访问实例配置和外部标签，默认结果查询保持轻量。
        // 屏蔽、黑名单属于实例配置标签，其中黑名单对象不会写入结果表，不能仅根据结果行查询标签。
        Map<DemandInstanceValueObject, DemandInstanceConfigEntity> instanceConfigs =
                param.isQueryInstanceTag()
                        ? this.listDemandInstanceConfigs(templateType, param)
                        : Collections.emptyMap();
        if (param.isQueryInstanceTag()) {
            // 先补齐黑名单对象，再统一查询自定义标签和转换VO，保证同一请求中的标签范围一致。
            results = this.appendBlacklistedResults(results, instanceConfigs, param.isFilterShieldedSpaces());
        }
        Map<DemandInstanceValueObject, DemandInstanceCustomizedTagDTO> instanceCustomizedTags =
                param.isQueryInstanceTag()
                        ? this.listDemandInstanceCustomizedTags(templateType, param, results)
                        : Collections.emptyMap();
        return results.stream()
                .map(result -> this.toDemandConfigResultVO(
                        result, param.isQueryInstanceTag(), instanceConfigs, instanceCustomizedTags))
                .collect(Collectors.toList());
    }

    /**
     * 转换结果表查询参数.
     *
     * @param param 查询参数
     * @param templateId 解析后的模板ID
     * @return 结果表查询参数
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResultRepositoryQueryParam toDemandConfigResultRepositoryQueryParam(
            @NonNull DemandConfigResultBatchQueryParam param,
            @NonNull Long templateId) {
        DemandConfigResultRepositoryQueryParam queryParam = new DemandConfigResultRepositoryQueryParam();
        queryParam.setDate(param.getDate());
        queryParam.setRelTemplateId(templateId);
        queryParam.setRelSceneId(param.getSceneId());
        queryParam.setDemandInstanceType(param.getInstanceType());
        queryParam.setDemandInstanceIds(param.getInstanceIds());
        queryParam.setIsShielded(param.isFilterShieldedSpaces() ? Boolean.FALSE : null);
        // 预计算结果已覆盖查询范围，autoFillEmptyInstanceIds与fillInstanceName仅兼容入参，
        // 最终结果轻量VO不补齐实例ID或名称，禁止由此触发RWD查询。
        queryParam.addColumns(DemandConfigResult::getDemandTemplateType,
                DemandConfigResult::getDemandInstanceId,
                DemandConfigResult::getFinalConfigJson,
                DemandConfigResult::getIsShielded);
        if (param.isQueryInstanceTag()) {
            queryParam.addColumns(DemandConfigResult::getDemandInstanceType,
                    DemandConfigResult::getIsCustomizedConfig);
        }
        return queryParam;
    }

    /**
     * 转换结果表VO.
     *
     * <p>接口只返回实例ID、屏蔽标识和最终配置；请求查询实例标签时再返回实例标签，
     * 结果表中的模板类型仅用于选择配置解析策略，不需要再补模板、场景、继承链路或实例名称。</p>
     *
     * @param result 结果表记录
     * @param queryInstanceTag 是否查询实例标签
     * @param instanceConfigs 实例配置
     * @param instanceCustomizedTags 实例自定义标签
     * @return 结果表VO
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResultVO<T> toDemandConfigResultVO(
            @NonNull DemandConfigResult result,
            boolean queryInstanceTag,
            @NonNull Map<DemandInstanceValueObject, DemandInstanceConfigEntity> instanceConfigs,
            @NonNull Map<DemandInstanceValueObject, DemandInstanceCustomizedTagDTO> instanceCustomizedTags) {
        DemandConfigResultVO<T> vo = new DemandConfigResultVO<>();
        vo.setInstanceId(result.getDemandInstanceId());
        vo.setShielded(Boolean.TRUE.equals(result.getIsShielded()));
        if (queryInstanceTag) {
            vo.setInstanceTag(this.toDemandInstanceTag(result, instanceConfigs, instanceCustomizedTags));
        }
        vo.setFinalConfig(this.toFinalConfig(result));
        return vo;
    }

    /**
     * 按前端对象范围批量查询实例配置.
     *
     * <p>结果表不保存黑名单对象，标签查询必须直接读取实例配置表，并使用前端必传的实例类型和可选实例ID
     * 限定查询范围，避免依赖结果表反推黑名单对象.</p>
     *
     * @param templateType 需求模板类型
     * @param param 结果查询参数
     * @return 实例值对象到实例配置的映射
     * @since v7.260701.0
     */
    private @NonNull Map<DemandInstanceValueObject, DemandInstanceConfigEntity> listDemandInstanceConfigs(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandConfigResultBatchQueryParam param) {
        DemandInstanceConfigQueryParam queryParam = new DemandInstanceConfigQueryParam();
        queryParam.setDate(param.getDate());
        queryParam.setTemplateType(templateType);
        queryParam.setInstanceType(param.getInstanceType());
        // 实例ID为空由Gateway解释为查询该实例类型下的全部配置。
        queryParam.setInstanceIds(param.getInstanceIds());
        List<DemandInstanceConfigEntity> configs = this.demandInstanceGateway.listInstanceConfigs(queryParam);
        if (CollectionUtils.isEmpty(configs)) {
            return Collections.emptyMap();
        }
        return configs.stream().collect(Collectors.toMap(
                config -> config.getIdentifier().getInstance(), Function.identity()));
    }

    /**
     * 为结果查询补充结果表不存在的黑名单对象.
     *
     * <p>补充的结果对象只用于复用统一的VO转换流程，不会写回结果表。其最终配置保持为空，
     * 对应标签中的 {@code existCfg=false}；若同时被屏蔽，仍遵循请求的屏蔽过滤条件.</p>
     *
     * @param results 结果表记录
     * @param instanceConfigs 实例配置
     * @param filterShieldedSpaces 是否过滤屏蔽空间
     * @return 合并后的结果记录
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigResult> appendBlacklistedResults(
            @NonNull List<DemandConfigResult> results,
            @NonNull Map<DemandInstanceValueObject, DemandInstanceConfigEntity> instanceConfigs,
            boolean filterShieldedSpaces) {
        if (MapUtils.isEmpty(instanceConfigs)) {
            return results;
        }
        Set<DemandInstanceValueObject> resultInstances = results.stream()
                .map(this::toDemandInstance)
                .collect(Collectors.toSet());
        List<DemandConfigResult> mergedResults = new ArrayList<>(results);
        instanceConfigs.forEach((instance, config) -> {
            DemandInstanceConfigValueObject configValue = config.getConfig();
            // 只补结果表缺失的黑名单对象，并与结果表查询保持相同的屏蔽过滤语义。
            if (Objects.isNull(configValue)
                    || !Boolean.TRUE.equals(configValue.getInBlacklisted())
                    || resultInstances.contains(instance)
                    || (filterShieldedSpaces && Boolean.TRUE.equals(configValue.getShielded()))) {
                return;
            }
            // 构造只读占位结果，让黑名单对象沿用统一的标签和响应转换逻辑。
            DemandConfigResult result = new DemandConfigResult();
            result.setDemandInstanceType(instance.getInstanceType());
            result.setDemandInstanceId(instance.getInstanceId());
            result.setIsCustomizedConfig(Boolean.FALSE);
            result.setIsShielded(BooleanUtils.isTrue(configValue.getShielded()));
            mergedResults.add(result);
            resultInstances.add(instance);
        });
        return mergedResults;
    }

    /**
     * 批量查询结果实例的自定义标签.
     *
     * @param templateType 需求模板类型
     * @param param 结果查询参数
     * @param results 结果表记录
     * @return 实例值对象到自定义标签的映射
     * @since v7.260701.0
     */
    private @NonNull Map<DemandInstanceValueObject, DemandInstanceCustomizedTagDTO> listDemandInstanceCustomizedTags(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandConfigResultBatchQueryParam param,
            @NonNull List<DemandConfigResult> results) {
        Set<DemandInstanceValueObject> instances = this.toTagQueryInstances(param, results);
        if (CollectionUtils.isEmpty(instances)) {
            return Collections.emptyMap();
        }
        return templateType.toDemandTypeEnum().demandStrategy()
                .mapDemandInstanceCustomizedTagNameAndValue(templateType, instances).stream()
                .collect(Collectors.toMap(DemandInstanceCustomizedTagDTO::getInstance, Function.identity()));
    }

    /**
     * 构造实例标签查询范围.
     *
     * <p>显式实例ID优先使用前端入参；未指定实例ID时使用最终响应范围。黑名单对象已在前一步补入结果，
     * 因此不会依赖结果表反推标签范围.</p>
     *
     * @param param 结果查询参数
     * @param results 结果表记录
     * @return 标签查询实例
     * @since v7.260701.0
     */
    private @NonNull Set<DemandInstanceValueObject> toTagQueryInstances(
            @NonNull DemandConfigResultBatchQueryParam param,
            @NonNull List<DemandConfigResult> results) {
        // 显式ID代表前端指定的标签查询范围，即使其中部分对象最终因屏蔽条件不返回也不能改写该范围。
        if (CollectionUtils.isNotEmpty(param.getInstanceIds())) {
            return param.getInstanceIds().stream()
                    .map(instanceId -> new DemandInstanceValueObject(param.getInstanceType(), instanceId))
                    .collect(Collectors.toSet());
        }
        // 指定类型全量查询没有显式ID，使用已完成黑名单补齐和屏蔽过滤的最终响应范围。
        return results.stream().map(this::toDemandInstance).collect(Collectors.toSet());
    }

    /**
     * 转换结果记录中的实例.
     *
     * @param result 结果表记录
     * @return 实例值对象
     * @since v7.260701.0
     */
    private @NonNull DemandInstanceValueObject toDemandInstance(@NonNull DemandConfigResult result) {
        return new DemandInstanceValueObject(result.getDemandInstanceType(), result.getDemandInstanceId());
    }

    /**
     * 构造实例标签.
     *
     * @param result 结果表记录
     * @param instanceConfigs 实例配置
     * @param instanceCustomizedTags 实例自定义标签
     * @return 实例标签
     * @since v7.260701.0
     */
    private @NonNull DemandInstanceTagVO toDemandInstanceTag(
            @NonNull DemandConfigResult result,
            @NonNull Map<DemandInstanceValueObject, DemandInstanceConfigEntity> instanceConfigs,
            @NonNull Map<DemandInstanceValueObject, DemandInstanceCustomizedTagDTO> instanceCustomizedTags) {
        DemandInstanceTagVO instanceTag = new DemandInstanceTagVO();
        instanceTag.setCustomConfig(Boolean.TRUE.equals(result.getIsCustomizedConfig()));
        instanceTag.setExistCfg(Objects.nonNull(result.getFinalConfigJson()));
        DemandInstanceValueObject instance = this.toDemandInstance(result);
        DemandInstanceConfigEntity instanceConfig = instanceConfigs.get(instance);
        DemandInstanceConfigValueObject config = Objects.nonNull(instanceConfig)
                ? instanceConfig.getConfig() : null;
        instanceTag.setShielded(Objects.nonNull(config)
                ? BooleanUtils.isTrue(config.getShielded()) : Boolean.TRUE.equals(result.getIsShielded()));
        instanceTag.setInBlacklisted(Objects.nonNull(config) && BooleanUtils.isTrue(config.getInBlacklisted()));
        DemandInstanceCustomizedTagDTO customizedTag = instanceCustomizedTags.get(instance);
        if (Objects.nonNull(customizedTag)) {
            instanceTag.setCustomizedTag(customizedTag.getCustomizedTag());
        }
        return instanceTag;
    }

    /**
     * 解析最终配置.
     *
     * @param result 结果表记录
     * @return 最终配置
     * @since v7.260701.0
     */
    private @Nullable DemandConfigValueObject<T> toFinalConfig(@NonNull DemandConfigResult result) {
        if (Objects.isNull(result.getFinalConfigJson())) {
            return null;
        }
        return result.getDemandTemplateType()
                .<T>demandConfigAnalyzeStrategy()
                .analyzeConfig(result.getFinalConfigJson());
    }

    /**
     * 根据实例查询需求配置。
     *
     * @param param 查询参数
     * @param invokeAirconditioningAware 是否执行原空调查询期生命周期
     * @return 需求配置聚合根
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfigAggregate<T>> listConfigsByInstances(
            @NonNull DemandConfigByInstanceBatchQueryParam param,
            boolean invokeAirconditioningAware) {
        param.checkAndSet();
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询需求配置");
        // enter 自适应上下文：主线程直接调用时用 enter，并发子任务中被调用时用 enterParallel
        try (PerformanceTracker.SafeNode safeNode = tracker.enter("DemandConfigGatewayImpl#listConfigsByInstances")) {
            demandConfigLifeAware.forEach(item -> item.beforeContextInit(param));
            DemandConfigAggregateInitByInstanceContext<T> context = new DemandConfigAggregateInitByInstanceContext<>(param);
            demandConfigLifeAware.forEach(item -> item.afterContextInit(context));
            safeNode.recordStep("初始化上下文");
            // 填充需求模版
            context.fillTemplate();
            safeNode.recordStep("填充需求模版");
            // 填充场景
            context.fillScene();
            safeNode.recordStep("填充场景");
            // 填充实例ID
            context.fillInstanceIds();
            safeNode.recordStep("填充实例ID");
            if (context.isFailed()) {
                return Lists.newArrayList();
            }
            // 填充需求配置
            context.fillConfigs();
            safeNode.recordStep("填充需求配置");
            if (context.isFailed()) {
                return Lists.newArrayList();
            }
            context.fillConfigGroup();
            safeNode.recordStep("填充需求分组配置");
            if (context.isFailed()) {
                return Lists.newArrayList();
            }
            // 填充聚合根
            demandConfigLifeAware.forEach(item -> item.beforeAggregateInit(context));
            List<DemandConfigAggregate<T>> aggregates = context.toAggregates(context.getConfigs());
            safeNode.recordStep("填充聚合根");
            // 处理聚合根继承配置
            if (param.isQueryInheritConfig()) {
                demandConfigLifeAware.forEach(item -> item.beforeAggregateHandleInheritConfig(aggregates));
                context.handleAggregateInheritConfig(aggregates);
                demandConfigLifeAware.forEach(item -> item.afterAggregateHandleInheritConfig(aggregates));
                safeNode.recordStep("处理聚合根继承配置");
            }
            if (param.isFillInstanceName()) {
                context.fillInstanceName(aggregates);
                safeNode.recordStep("填充实例名称");
            }
            // 稳定结果路径只跳过会随运行态变化的空调补全，其余生命周期监听仍保持原顺序。
            demandConfigLifeAware.stream()
                    .filter(item -> invokeAirconditioningAware
                            || !(item instanceof DemandConfigAirconditoningAware))
                    .forEach(item -> item.afterAggregateInit(aggregates));
            safeNode.recordStep("结束查询");
            return aggregates;
        }
    }

    @Override
    public List<DemandCustomizedInstanceDTO> listCustomizedInstances(@NonNull DemandCustomizedInstanceQueryParam param) {
        Long templateId = param.getTemplateId();
        LocalDate date = param.getDate();
        DemandInstanceConfigValueObject filterConfig = param.getFilterConfig();

        DemandConfigByInstanceRepositoryBatchQueryParam queryParam = new DemandConfigByInstanceRepositoryBatchQueryParam();
        queryParam.setDate(date);
        queryParam.setTemplateId(templateId);
        queryParam.setSceneId(param.getSceneId());
        queryParam.addColumns(DemandConfig::getDemandInstanceType, DemandConfig::getDemandInstanceId, DemandConfig::getRelGroupId, BaseDO::getCreateTime);
        List<DemandConfig> demandConfigs = demandConfigRepository.listByInstances(queryParam);
        // 存在过滤配置时, 过滤不符合条件的实例结果
        if (Objects.nonNull(filterConfig)) {
            BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
            templateParam.setTemplateId(templateId);
            templateParam.setDate(date);
            Optional<DemandTemplate> templateOpt = getByTemplate(templateParam);
            if (!templateOpt.isPresent()) {
                throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
            }
            DemandTemplate template = templateOpt.get();
            DemandInstanceConfigRepositoryQueryParam instanceConfigQueryParam = new DemandInstanceConfigRepositoryQueryParam();
            instanceConfigQueryParam.setDate(param.getDate());
            instanceConfigQueryParam.setTemplateType(template.getDemandTemplateType());
            instanceConfigQueryParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
            instanceConfigQueryParam.setFilterConfig(filterConfig);
            instanceConfigQueryParam.addColumns(DemandInstanceConfig::getDemandInstanceId);
            Set<String> filterInstanceIds = demandInstanceRepository.listConfig(instanceConfigQueryParam).stream()
                    .map(DemandInstanceConfig::getDemandInstanceId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(filterInstanceIds)) {
                demandConfigs.removeIf(item -> filterInstanceIds.contains(item.getDemandInstanceId()));
            }
        }

        List<String> allInstanceIds = demandConfigs.stream().map(DemandConfig::getDemandInstanceId).collect(Collectors.toList());

        Map<String, String> instanceIdAndLocalName = dtpRwdClientService.mapObjectIdAndLocalName(false, allInstanceIds);

        Set<Long> allRelGroupIds = demandConfigs.stream()
                .map(DemandConfig::getRelGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, DemandConfigGroup> groupIdAndData = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(allRelGroupIds)) {
            DemandConfigGroupRepositoryBatchQueryParam configGroupBatChQueryParam =
                    new DemandConfigGroupRepositoryBatchQueryParam();
            configGroupBatChQueryParam.setConfigGroupIds(allRelGroupIds);
            configGroupBatChQueryParam.setDate(date);
            configGroupBatChQueryParam.addColumns(DemandConfigGroup::getId,
                    DemandConfigGroup::getGroupName, DemandConfigGroup::getRelTemplateId);
            groupIdAndData = demandConfigGroupRepository.list(configGroupBatChQueryParam).stream().collect(
                    Collectors.toMap(BaseAssignIdDO::getId, Function.identity()));
        }
        List<DemandCustomizedInstanceDTO> result = new ArrayList<>();
        for (DemandConfig demandConfig : demandConfigs) {
            DemandInstanceTypeEnum instanceType = demandConfig.getDemandInstanceType();
            String instanceId = demandConfig.getDemandInstanceId();
            Long relGroupId = demandConfig.getRelGroupId();

            DemandConfigGroupEntity configGroup = null;
            if (Objects.nonNull(relGroupId)) {
                DemandConfigGroup relGroup = groupIdAndData.get(relGroupId);
                if (Objects.nonNull(relGroup)) {
                    configGroup = DemandConfigGroupEntity.of(relGroup);
                }
            }
            DemandCustomizedInstanceDTO customizedInstance = new DemandCustomizedInstanceDTO();
            customizedInstance.setInstanceName(instanceIdAndLocalName.get(instanceId));
            customizedInstance.setInstanceType(instanceType);
            customizedInstance.setInstanceId(instanceId);
            customizedInstance.setConfigGroup(configGroup);
            customizedInstance.setCreateTime(demandConfig.getCreateTime());
            result.add(customizedInstance);
        }
        return result;
    }

    @Override
    public List<DtpRwdSpaceDTO> listReferenceParentSpaces(@NonNull DemandReferenceParentSpaceInFloorQueryParam param) {
        String floorId = param.getFloorId();
        LocalDate date = param.getDate();

        DemandTemplate template = param.queryAndGetTemplate();
        if (Objects.isNull(template)) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT,
                    LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
        }
        Long templateId = template.getId();
        DemandTemplateTypeEnum templateType = template.getDemandTemplateType();
        DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();
        List<DtpRwdSpaceDTO> spaces = demandStrategy.listDemandSpaceByInstance(templateType,
                new DemandInstanceValueObject(DemandInstanceTypeEnum.FLOOR, floorId),
                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName);
        if (CollectionUtils.isEmpty(spaces)) {
            return Lists.newArrayList();
        }
        Set<String> spaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        DemandFilterNotCustomizedConfigSpaceIdQueryParam filterNotCustomizedConfigSpaceIdQueryParam
                = new DemandFilterNotCustomizedConfigSpaceIdQueryParam();
        filterNotCustomizedConfigSpaceIdQueryParam.setTemplateId(templateId);
        filterNotCustomizedConfigSpaceIdQueryParam.setSpaceIds(spaceIds);
        filterNotCustomizedConfigSpaceIdQueryParam.setDate(date);
        filterNotCustomizedConfigSpaceIdQueryParam.addColumns(DemandConfig::getDemandInstanceId);
        spaceIds = demandConfigRepository.filterNotCustomizedConfigSpaceIds(filterNotCustomizedConfigSpaceIdQueryParam);
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Lists.newArrayList();
        }
        DemandConfigByInstanceBatchQueryParam queryParam = new DemandConfigByInstanceBatchQueryParam();
        queryParam.setDate(date);
        queryParam.setTemplateId(templateId);
        queryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(DemandInstanceTypeEnum.SPACE, spaceIds));
        List<DemandConfigAggregate<T>> aggregates = listConfigsByInstances(queryParam);
        if (CollectionUtils.isEmpty(aggregates)) {
            return Lists.newArrayList();
        }
        Set<String> referenceParentSpaceIds = aggregates.stream()
                .filter(item -> MapUtils.isNotEmpty(item.getInheritInstanceAndModes()))
                .map(item -> item.getIdentifier().getInstance().getInstanceId())
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(referenceParentSpaceIds)) {
            return Lists.newArrayList();
        }
        return spaces.stream().filter(item -> referenceParentSpaceIds.contains(item.getId())).collect(Collectors.toList());
    }

    @Override
    public boolean checkConfigGroupNameExists(@NonNull DemandConfigGroupNameCheckParam param) {
        return this.demandConfigGroupRepository.checkConfigGroupNameExists(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addOrUpdateConfig(@NonNull DemandConfigSaveOrUpdateParam<T> param) {
        param.checkAndSet();
        Long templateId = param.getTemplateId();
        Long sceneId = param.getSceneId();
        Long configGroupId = param.getConfigGroupId();
        String configGroupName = param.getConfigGroupName();
        DemandInstanceTypeEnum instanceType = param.getInstanceType();
        Set<String> instanceIds = param.getInstanceIds();
        Boolean addRoomFuncType = param.getAddRoomFuncType();

        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateId(templateId);
        DemandTemplate template = templateParam.queryAndGetTemplate();
        if (Objects.isNull(template)) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT,
                    LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
        }
        if (Objects.nonNull(sceneId)) {
            demandSceneRepository.getByIdAndDate(true, sceneId, null);
        }
        DemandConfigAnalyzeStrategy<T> configAnalyzeStrategy =
                template.getDemandTemplateType().demandConfigAnalyzeStrategy();
        // 为字段级继承, 且配置中所有字段都为空时, 不进行任何操作
        if (configAnalyzeStrategy.inheritConfigType() == InheritConfigTypeEnum.FIELD) {
            DemandConfigValueObject<T> configData = param.getConfigData();
            T globalConfig = configData.getGlobalConfig();
            List<DemandSpaceFuncTypeConfig<T>> spaceFuncTypeConfigs = configData.getSpaceFuncTypeConfigs();
            if (globalConfig.allFieldEmpty() && CollectionUtils.isEmpty(spaceFuncTypeConfigs)) {
                return null;
            }
        }
        DemandConfigValueObject<T> configData = configAnalyzeStrategy.beforeAddConfigHandler(param);

        Set<Long> operationRelGroupIds = new LinkedHashSet<>();
        Set<Long> operationRoomFuncTypeRelGroupIds = new LinkedHashSet<>();
        Set<Long> toBeDelConfigIds = new HashSet<>();
        DemandConfigByInstanceRepositoryBatchQueryParam batchQueryParam = demandConfigConverter.toBathQueryParam(param);
        if (Objects.isNull(configGroupId)) {
            batchQueryParam.setConfigGroupIds(null);
        } else {
            batchQueryParam.setInstanceTypeAndInstanceIds(null);
            batchQueryParam.setInstanceTypes(null);
            batchQueryParam.setInstanceIds(null);
        }
        List<DemandConfig> dbDemandConfigs = demandConfigRepository.listByInstances(batchQueryParam);

        // 分组ID不存在时创建分组
        if (Objects.isNull(configGroupId)) {
            if (CollectionUtils.isNotEmpty(dbDemandConfigs)) {
                // 批量查出每个实例的名称, 用于创建分组名称
                Supplier<Map<String, String>> instanceIdAndName = Suppliers.memoize(() ->
                        dtpRwdClientService.mapObjectIdAndLocalName(false, dbDemandConfigs.stream()
                                .map(DemandConfig::getDemandInstanceId).collect(Collectors.toSet())));
                for (DemandConfig dbDemandConfig : dbDemandConfigs) {
                    Long relGroupId = dbDemandConfig.getRelGroupId();
                    if (Objects.isNull(relGroupId)) {
                        // 分组不存在时创建分组
                        DemandConfigGroup configGroup = demandConfigGroupRepository.add(
                                demandConfigConverter.toAddConfigGroup(param, instanceIdAndName.get().getOrDefault(
                                        dbDemandConfig.getDemandInstanceId(), DemandConfigGroupRepository.FICTITIOUS_GROUP_NAME)));
                        dbDemandConfig.setRelGroupId(configGroup.getId());
                        // 更新配置关联的分组ID
                        DemandConfigAddOrEditParam addOrEditParam = demandConfigConverter.toAddOrEditParam(param);
                        addOrEditParam.setPublishEvent(false);
                        addOrEditParam.setConfigGroupId(configGroup.getId());
                        demandConfigRepository.addOrEditConfig(addOrEditParam);
                        operationRelGroupIds.add(configGroup.getId());
                    } else {
                        // 存在分组时更新分组名称
                        demandConfigGroupRepository.editGroupName(relGroupId, StringUtils.defaultIfBlank(
                                configGroupName, DemandConfigGroupRepository.FICTITIOUS_GROUP_NAME));
                        operationRelGroupIds.add(relGroupId);
                    }
                }
            } else {
                DemandConfigGroup configGroup = demandConfigGroupRepository.add(
                        demandConfigConverter.toAddConfigGroup(param, StringUtils.defaultIfBlank(
                                configGroupName, DemandConfigGroupRepository.FICTITIOUS_GROUP_NAME)));
                configGroupId = configGroup.getId();
                param.setConfigGroupId(configGroupId);
                operationRelGroupIds.add(configGroupId);
            }
        } else {
            // 存在分组时更新分组名称
            demandConfigGroupRepository.editGroupName(configGroupId, StringUtils.defaultIfBlank(
                    configGroupName, DemandConfigGroupRepository.FICTITIOUS_GROUP_NAME));
            operationRelGroupIds.add(configGroupId);
            // 存在分组时移除入参实例中不存在的实例
            dbDemandConfigs.removeIf(item -> {
                if (!instanceIds.contains(item.getDemandInstanceId())) {
                    toBeDelConfigIds.add(item.getId());
                    return true;
                }
                return false;
            });
        }
        // 为项目时, 判断空间功能类型分组ID不存在时创建分组
        if (instanceType == DemandInstanceTypeEnum.PROJECT && CollectionUtils.isNotEmpty(dbDemandConfigs)) {
            DemandConfigValueObject<T> dbProjectConfig = configAnalyzeStrategy.analyzeConfig(
                    IterableUtils.first(dbDemandConfigs).getConfigData());
            List<DemandSpaceFuncTypeConfig<T>> dbSpaceFuncTypeConfigs = dbProjectConfig.getSpaceFuncTypeConfigs();
            List<DemandSpaceFuncTypeConfig<T>> spaceFuncTypeConfigs = configData.getSpaceFuncTypeConfigs();
            // 需要删除的分组ID
            Set<Long> toBeDelRelGroupIds = new HashSet<>();
            if (CollectionUtils.isNotEmpty(dbSpaceFuncTypeConfigs)) {
                toBeDelRelGroupIds = dbSpaceFuncTypeConfigs.stream()
                        .map(DemandSpaceFuncTypeConfig::getRelConfigGroupId)
                        .filter(Objects::nonNull).collect(Collectors.toSet());
            }
            if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigs)) {
                Set<Long> spaceFuncTypeConfigRelGroupIds = new HashSet<>();
                // 处理入参的空间功能类型分组
                Set<Long> existConfigGroupIds = new HashSet<>();
                for (DemandSpaceFuncTypeConfig<T> spaceFuncTypeConfig : spaceFuncTypeConfigs) {
                    List<DtpKnowledgeInfoCodeDataSourceDTO> funcTypes = spaceFuncTypeConfig.getFuncTypes();
                    Long relConfigGroupId = spaceFuncTypeConfig.getRelConfigGroupId();

                    String roomFuncTypeGroupName = StringUtils.defaultIfBlank(
                            spaceFuncTypeConfig.getConfigGroupName(), funcTypes.stream()
                                    .map(DtpKnowledgeInfoCodeDataSourceDTO::getName)
                                    .collect(Collectors.joining("、")));
                    if (Objects.isNull(relConfigGroupId)) {
                        // 分组不存在时创建分组
                        DemandConfigGroup configGroup = demandConfigGroupRepository.add(
                                demandConfigConverter.toAddConfigGroup(param, roomFuncTypeGroupName));
                        spaceFuncTypeConfig.setRelConfigGroupId(configGroup.getId());
                        operationRoomFuncTypeRelGroupIds.add(configGroup.getId());
                        relConfigGroupId = configGroup.getId();
                    } else {
                        if (CollectionUtils.isEmpty(existConfigGroupIds)) {
                            existConfigGroupIds.add(relConfigGroupId);
                        } else if (existConfigGroupIds.contains(relConfigGroupId)) {
                            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_e7dcf98c559b46c5a07d4fd0e8aa1573"));
                        }
                        // 存在分组时更新分组名称
                        demandConfigGroupRepository.editGroupName(relConfigGroupId, roomFuncTypeGroupName);
                    }
                    operationRelGroupIds.add(relConfigGroupId);
                    spaceFuncTypeConfigRelGroupIds.add(relConfigGroupId);
                }
                // 处理数据库中需要删除的空间类型分组
                if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigRelGroupIds)) {
                    toBeDelRelGroupIds.removeIf(spaceFuncTypeConfigRelGroupIds::contains);
                }
            }
            if (CollectionUtils.isNotEmpty(toBeDelRelGroupIds)) {
                demandConfigGroupRepository.del(toBeDelRelGroupIds);
            }
        }
        DemandConfigAddOrEditParam addOrEditParam = demandConfigConverter.toAddOrEditParam(param);
        addOrEditParam.setInstanceIds(instanceIds);
        addOrEditParam.setConfigGroupId(configGroupId);
        demandConfigRepository.addOrEditConfig(addOrEditParam);
        if (CollectionUtils.isNotEmpty(toBeDelConfigIds)) {
            // 删除配置事件会同步触发空分组清理，必须等本次新增/编辑落库后再发布删除事件；
            // 否则“替换分组下全部实例”的场景会在新配置写入前误删当前分组。
            DemandConfigByInstanceRepositoryRemoveParam removeParam =
                    new DemandConfigByInstanceRepositoryRemoveParam();
            removeParam.setConfigIds(toBeDelConfigIds);
            this.demandConfigRepository.delConfigByInstance(removeParam);
        }
        if (BooleanUtils.isTrue(addRoomFuncType) && CollectionUtils.isNotEmpty(operationRoomFuncTypeRelGroupIds)) {
            return IterableUtils.first(operationRoomFuncTypeRelGroupIds);
        }
        return IterableUtils.first(operationRelGroupIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copyConfig(@NonNull Pair<DemandConfigCopyPairParam, DemandConfigCopyPairParam> param) {
        DemandConfigCopyContext<T> context = new DemandConfigCopyContext<>(param);
        if (context.isFailed()) {
            return;
        }
        // 填充模版场景
        context.fillTemplateScene();
        if (context.isFailed()) {
            return;
        }
        // 填充来源配置
        context.fillFromConfigs();
        if (context.isFailed()) {
            return;
        }
        // 复制配置分组
        context.copyConfigGroups();
        if (context.isFailed()) {
            return;
        }
        // 复制配置
        context.copyConfigs();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReference(@NonNull DemandConfigReferenceCancelParam param) {
        DemandConfigSaveOrUpdateParam<T> addParam = new DemandConfigSaveOrUpdateParam<>();
        addParam.setTemplateId(param.getTemplateId());
        addParam.setSceneId(param.getSceneId());
        addParam.setInstanceType(param.getInstance().getInstanceType());
        addParam.setInstanceIds(Sets.newHashSet(param.getInstance().getInstanceId()));

        switch (param.getClearConfigType()) {
            case RETAIN_CONFIG:
                // 取消引用并保留配置
                // 获取上级配置
                DemandConfigByInstanceQueryParam dciq = new DemandConfigByInstanceQueryParam();
                dciq.setTemplateId(param.getTemplateId());
                dciq.setSceneId(param.getSceneId());
                dciq.setInstance(param.getInstance());
                Optional<DemandConfigAggregate<T>> cfg = this.getConfigByInstance(dciq);
                if (!cfg.isPresent()) {
                    return;
                }

                // 调用保存 把上级配置给当前实例
                addParam.setConfigData(cfg.get().getParentConfig());
                addParam.setDeleteFlag(true);
                break;
            case DELETE_CONFIG:
                // 保存一个空配置
                addParam.setConfigData(new DemandConfigValueObject<>());
                addParam.setDeleteFlag(true);
        }
        this.addOrUpdateConfig(addParam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearConfig(@NonNull DemandConfigClearParam param) {
        DemandConfigByInstanceRepositoryRemoveParam delParam = new DemandConfigByInstanceRepositoryRemoveParam();
        delParam.setTemplateId(param.getTemplateId());
        delParam.setSceneId(param.getSceneId());
        Set<String> instanceIds = new HashSet<>();
        switch (param.getClearConfigType()) {
            case CURRENT:
                // 删除当前实例配置
                instanceIds.addAll(param.getInstanceIds());
                break;
            case CURRENT_AND_CHILDREN:
                // 删除当前以及下级对象的配置
                instanceIds.addAll(param.getInstanceIds());
            case CHILDREN:
                // 删除下级对象配置
                Set<String> childIds = instanceService.listChildrenInstanceIdsByInstance(
                        param.getInstanceType(),
                        param.getInstanceIds().toArray(new String[0])[0]);
                instanceIds.addAll(childIds);
        }

        if (Objects.nonNull(param.getConfigGroupId())) {
            demandConfigGroupRepository.del(Sets.newHashSet(param.getConfigGroupId()));
        }

        if (CollectionUtils.isEmpty(instanceIds)) {
            LogExecutor.instance().warn(log(), logTag(), "没有需要删除的配置", Collections.singletonList(Pair.of("param", param)));
            return;
        }

        // 删除配置
        delParam.setInstanceIds(instanceIds);
        demandConfigRepository.delConfigByInstance(delParam);
    }

    /**
     * 模板删除时同步删除关联配置.
     *
     * @param event 模板变化事件
     */
    @EventListener(DemandTemplateChangeEvent.class)
    public void onDemandTemplateChangeEvent(@NonNull DemandTemplateChangeEvent event) {
        DemandTemplateChangeEvent.Message message = (DemandTemplateChangeEvent.Message) event.getSource();
        DemandTemplateChangeTypeEnum changeType = message.getDataType();
        DemandTemplate oldTemplate = message.getOldTemplate();

        Long oldTemplateId;
        switch (changeType) {
            case REMOVE:
                oldTemplateId = oldTemplate.getId();
                DemandConfigByInstanceRepositoryRemoveParam removeParam =
                        new DemandConfigByInstanceRepositoryRemoveParam();
                removeParam.setTemplateId(oldTemplateId);
                // 模板删除已经有独立事件清理结果表；这里仅级联清理原始配置，避免再次触发配置删除刷新。
                removeParam.setPublishEvent(false);
                this.demandConfigRepository.delConfigByInstance(removeParam);
                break;
            case INSERT:
            case APPLIED:
            case UPDATE:
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(changeType.getClass().getSimpleName(), changeType);
        }
    }

    /**
     * 场景删除时同步删除关联配置.
     *
     * @param event 场景变化事件
     */
    @EventListener(DemandSceneChangeEvent.class)
    public void onDemandSceneRemoveEvent(@NonNull DemandSceneChangeEvent event) {
        DemandSceneChangeEvent.Message message = (DemandSceneChangeEvent.Message) event.getSource();
        Long templateId = message.getTemplateId();
        Set<Long> toBeDelSceneIds = message.getDetails().stream()
                .filter(item -> item.getDataType() == DataTypeEnum.DELETE)
                .flatMap(item -> item.getConfigs().stream().map(scene -> scene.getOldScene().getId()))
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(toBeDelSceneIds)) {
            return;
        }
        for (Long toBeDelSceneId : toBeDelSceneIds) {
            DemandConfigByInstanceRepositoryRemoveParam removeParam = new DemandConfigByInstanceRepositoryRemoveParam();
            removeParam.setTemplateId(templateId);
            removeParam.setSceneId(toBeDelSceneId);
            // 场景删除已经有独立事件清理结果表；这里仅级联清理原始配置，避免再次触发配置删除刷新。
            removeParam.setPublishEvent(false);
            this.demandConfigRepository.delConfigByInstance(removeParam);
        }
    }

    /**
     * 配置删除后清理无关联的配置分组.
     *
     * @param event 配置变化事件
     */
    @EventListener(DemandConfigChangeEvent.class)
    public void onDemandConfigRemoveEvent(@NonNull DemandConfigChangeEvent event) {
        DemandConfigChangeEvent.Message message = (DemandConfigChangeEvent.Message) event.getSource();
        Long templateId = message.getTemplateId();
        Long sceneId = message.getSceneId();
        List<DemandConfigChangeEvent.Detail> details = message.getDetails();
        for (DemandConfigChangeEvent.Detail detail : details) {
            DataTypeEnum dataType = detail.getDataType();
            List<DemandConfigChangeEvent.Config> configs = detail.getConfigs();
            if (dataType != DataTypeEnum.DELETE) {
                continue;
            }
            Set<Long> groupIds = configs.stream()
                    .map(item -> item.getOldConfig().getRelGroupId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(groupIds)) {
                return;
            }
            // 删除配置后检查是否需要删除分组
            DemandConfigByInstanceRepositoryBatchQueryParam queryParam = new DemandConfigByInstanceRepositoryBatchQueryParam();
            queryParam.setTemplateId(templateId);
            queryParam.setSceneId(sceneId);
            queryParam.setConfigGroupIds(groupIds);
            queryParam.addColumns(BaseAssignIdDO::getId, DemandConfig::getRelGroupId);
            List<DemandConfig> existsDemandConfigs = this.demandConfigRepository.listByInstances(queryParam);
            Set<Long> toBeDelConfigGroupIds = new HashSet<>(groupIds);
            if (CollectionUtils.isNotEmpty(existsDemandConfigs)) {
                Map<Long, List<DemandConfig>> groupIdAndRelConfig = existsDemandConfigs.stream()
                        .filter(item -> Objects.nonNull(item.getRelGroupId()))
                        .collect(Collectors.groupingBy(DemandConfig::getRelGroupId));
                if (MapUtils.isNotEmpty(groupIdAndRelConfig)) {
                    toBeDelConfigGroupIds = groupIds.stream()
                            .filter(item -> !groupIdAndRelConfig.containsKey(item))
                            .collect(Collectors.toSet());
                }
            }
            this.demandConfigGroupRepository.del(toBeDelConfigGroupIds);
        }
    }

    /**
     * 黑名单实例变化时同步删除关联配置.
     *
     * @param event 实例配置变化事件
     */
    @EventListener(DemandInstanceConfigChangeEvent.class)
    public void onDemandInstanceConfigChangeEvent(@NonNull DemandInstanceConfigChangeEvent event) {
        DemandInstanceConfigChangeEvent.Param param = (DemandInstanceConfigChangeEvent.Param) event.getSource();
        if (param.getChangeType() != DemandInstanceConfigChangeTypeEnum.BLACKLIST) {
            return;
        }
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        Set<String> inInstanceIds = param.getInInstanceIds();
        if (CollectionUtils.isEmpty(inInstanceIds)) {
            return;
        }
        List<Long> templateIds = this.demandTemplateRepository.listTemplateIdsByTemplateType(templateType);
        if (CollectionUtils.isEmpty(templateIds)) {
            return;
        }
        for (Long templateId : templateIds) {
            DemandConfigByInstanceRepositoryRemoveParam removeParam = new DemandConfigByInstanceRepositoryRemoveParam();
            removeParam.setTemplateId(templateId);
            removeParam.setInstanceIds(inInstanceIds);
            // 进入黑名单已有实例配置事件删除结果表；这里仅级联清理原始配置，避免重复刷新。
            removeParam.setPublishEvent(false);
            this.demandConfigRepository.delConfigByInstance(removeParam);
        }
    }

    @Override
    public String logTag() {
        return "需求管理-需求配置-网关";
    }
}
