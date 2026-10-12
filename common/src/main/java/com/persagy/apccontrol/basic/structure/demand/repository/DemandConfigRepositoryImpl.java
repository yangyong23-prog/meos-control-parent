package com.persagy.apccontrol.basic.structure.demand.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigService;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConfigConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.param.*;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求配置仓储实现
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigRepositoryImpl implements DemandConfigRepository {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final DemandConfigConverter demandConfigConverter;
    private final DemandConfigService demandConfigService;
    private final ObjectMapper objectMapper;

    @Override
    public int countExistsCustomizedInstance(@Nullable LocalDate date, @NonNull Long templateId) {
        return demandConfigService.countExistsCustomizedInstance(date, templateId);
    }

    @Override
    public Optional<DemandConfig> getOptByInstance(@NonNull DemandConfigByInstanceRepositoryQueryParam param) {
        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds = new HashMap<>();
        instanceTypeAndInstanceIds.put(param.getInstance().getInstanceType(), Sets.newHashSet(param.getInstance().getInstanceId()));
        DemandConfigByInstanceRepositoryBatchQueryParam batchQueryParam =
                new DemandConfigByInstanceRepositoryBatchQueryParam();
        if (Objects.nonNull(param.getConfigId())) {
            batchQueryParam.setConfigIds(Sets.newHashSet(param.getConfigId()));
        }
        batchQueryParam.setTemplateId(param.getTemplateId());
        batchQueryParam.setSceneId(param.getSceneId());
        if (Objects.nonNull(param.getConfigGroupId())) {
            batchQueryParam.setConfigGroupIds(Sets.newHashSet(param.getConfigGroupId()));
        }
        batchQueryParam.setInstanceTypeAndInstanceIds(instanceTypeAndInstanceIds);
        batchQueryParam.setSelectColumns(param.getSelectColumns());
        return listByInstances(batchQueryParam).stream().findFirst();
    }

    @Override
    public List<DemandConfig> listByInstances(@NonNull DemandConfigByInstanceRepositoryBatchQueryParam param) {
        Set<Long> groupIds = param.getConfigGroupIds();
        if (CollectionUtils.isNotEmpty(groupIds) && CollectionUtils.isNotEmpty(param.getSelectColumns())) {
            param.addColumns(BaseAssignIdDO::getId, DemandConfig::getDemandInstanceType);
        }
        return demandConfigService.listByInstances(param);
    }

    @Override
    public Set<String> filterNotCustomizedConfigSpaceIds(
            @NonNull DemandFilterNotCustomizedConfigSpaceIdQueryParam param) {
        DemandConfigByInstanceRepositoryBatchQueryParam queryParam = new DemandConfigByInstanceRepositoryBatchQueryParam();
        queryParam.setTemplateId(param.getTemplateId());
        queryParam.setSceneId(param.getSceneId());
        queryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(DemandInstanceTypeEnum.SPACE, param.getSpaceIds()));
        queryParam.setDate(param.getDate());
        queryParam.addColumns(DemandConfig::getDemandInstanceId);
        Set<String> existsConfigSpaceIds = demandConfigService.listByInstances(queryParam).stream()
                .map(DemandConfig::getDemandInstanceId)
                .collect(Collectors.toSet());
        Set<String> result = new HashSet<>(param.getSpaceIds());
        if (CollectionUtils.isNotEmpty(existsConfigSpaceIds)) {
            result = result.stream().filter(item -> !existsConfigSpaceIds.contains(item)).collect(Collectors.toSet());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<DataTypeEnum, List<DemandConfigChangeEvent.Config>> addOrEditConfig(@NonNull DemandConfigAddOrEditParam param) {
        param.checkAndSet();
        Long configGroupId = param.getConfigGroupId();
        Long templateId = param.getTemplateId();
        Long sceneId = param.getSceneId();
        Set<String> instanceIds = param.getInstanceIds();
        DemandConfigValueObject<?> configData = param.getConfigData();
        boolean publishEvent = param.isPublishEvent();
        DemandConfigByInstanceRepositoryBatchQueryParam batchQueryParam = demandConfigConverter.toBathQueryParam(param);
        if (CollectionUtils.isNotEmpty(batchQueryParam.getSelectColumns())) {
            batchQueryParam.addColumns(BaseAssignIdDO::getId, DemandConfig::getDemandInstanceId);
        }
        batchQueryParam.setConfigGroupIds(null);
        List<DemandConfig> demandConfigs = demandConfigService.listByInstances(batchQueryParam);
        // 分组关联有配置则编辑, 无配置则新增
        Map<DataTypeEnum, List<DemandConfigChangeEvent.Config>> typeAndConfigs = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(demandConfigs)) {
            // 入参中存在, 数据库中不存在的新增
            Collection<String> toBeAddInstanceIds = CollectionUtils.subtract(instanceIds,
                    demandConfigs.stream().map(DemandConfig::getDemandInstanceId).collect(Collectors.toSet()));
            if (CollectionUtils.isNotEmpty(toBeAddInstanceIds)) {
                List<DemandConfig> toBeAddConfigs = toBeAddInstanceIds.stream()
                        .map(item -> demandConfigConverter.toAddConfig(param, item))
                        .peek(item -> MapExecutor.dynamicPut(typeAndConfigs, DataTypeEnum.ADD,
                                new DemandConfigChangeEvent.Config(null, item), ArrayList::new))
                        .collect(Collectors.toList());
                demandConfigService.saveValidate(toBeAddConfigs);
            }
            // 入参中存在, 数据库中存在的编辑
            List<DemandConfig> toBeEditConfigs = new ArrayList<>();
            JsonNode config = objectMapper.convertValue(configData, JsonNode.class);
            for (DemandConfig demandConfig : demandConfigs) {
                DemandConfig finalConfig = demandConfigConverter.copy(demandConfig);
                // 重新设置配置内容
                finalConfig.setConfigData(config);
                // 兼容旧数据没有分组ID的情况
                if (Objects.nonNull(configGroupId)) {
                    finalConfig.setRelGroupId(configGroupId);
                }
                toBeEditConfigs.add(finalConfig);
                MapExecutor.dynamicPut(typeAndConfigs, DataTypeEnum.EDIT,
                        new DemandConfigChangeEvent.Config(demandConfig, finalConfig), ArrayList::new);
            }
            demandConfigService.updateByIdValidate(toBeEditConfigs);
        } else {
            // 数据库中无记录则直接新增
            List<DemandConfig> toBeAddConfigs = instanceIds.stream()
                    .map(item -> demandConfigConverter.toAddConfig(param, item))
                    .peek(item -> MapExecutor.dynamicPut(typeAndConfigs, DataTypeEnum.ADD,
                            new DemandConfigChangeEvent.Config(null, item), ArrayList::new))
                    .collect(Collectors.toList());
            demandConfigService.saveValidate(toBeAddConfigs);
        }
        if (publishEvent) {
            this.publishChangeEvent(templateId, sceneId, typeAndConfigs.entrySet().stream()
                    .map(item -> new DemandConfigChangeEvent.Detail(item.getKey(), item.getValue()))
                    .collect(Collectors.toList()));
        }
        return typeAndConfigs;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrEditConfig(@NonNull List<DemandConfigAddOrEditParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return;
        }
        MultiKeyMap<Long, Map<DataTypeEnum, List<DemandConfigChangeEvent.Config>>>
                templateSceneIdAndTypeConfigs = new MultiKeyMap<>();
        for (DemandConfigAddOrEditParam param : params) {
            Long templateId = param.getTemplateId();
            Long sceneId = param.getSceneId();

            Map<DataTypeEnum, List<DemandConfigChangeEvent.Config>> typeAndConfigs = this.addOrEditConfig(param);
            if (param.isPublishEvent() && MapUtils.isNotEmpty(typeAndConfigs)) {
                MultiKey<Long> multiKey = new MultiKey<>(templateId, sceneId);
                typeAndConfigs.forEach((type, configs) -> MapExecutor.dynamicPutAll(
                        templateSceneIdAndTypeConfigs, multiKey, type, configs, LinkedHashMap::new, ArrayList::new));
            }
        }
        if (MapUtils.isEmpty(templateSceneIdAndTypeConfigs)) {
            return;
        }
        templateSceneIdAndTypeConfigs.forEach((multiKey, typeAndConfigs) -> {
            this.publishChangeEvent(multiKey.getKey(0), multiKey.getKey(1), typeAndConfigs.entrySet().stream()
                    .map(item -> new DemandConfigChangeEvent.Detail(item.getKey(), item.getValue()))
                    .collect(Collectors.toList()));
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editConfigDataWithoutEvent(@NonNull List<DemandConfig> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        this.demandConfigService.updateByIdValidate(configs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onlyDelConfigById(@NonNull Set<Long> configIds) {
        if (CollectionUtils.isEmpty(configIds)) {
            return;
        }
        this.demandConfigService.removeByIdIgnore(configIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delConfigByInstance(@NonNull DemandConfigByInstanceRepositoryRemoveParam param) {
        Set<Long> configIds = param.getConfigIds();
        Long templateId = param.getTemplateId();
        Long sceneId = param.getSceneId();
        Set<String> instanceIds = param.getInstanceIds();
        boolean publishEvent = param.isPublishEvent();

        DemandConfigByInstanceRepositoryBatchQueryParam queryParam =
                new DemandConfigByInstanceRepositoryBatchQueryParam();
        queryParam.setConfigIds(configIds);
        queryParam.setTemplateId(templateId);
        queryParam.setSceneId(sceneId);
        queryParam.setInstanceIds(instanceIds);
        queryParam.addColumns(BaseAssignIdDO::getId, DemandConfig::getRelGroupId,
                DemandConfig::getRelTemplateId, DemandConfig::getRelSceneId,
                DemandConfig::getDemandInstanceType, DemandConfig::getDemandInstanceId);
        List<DemandConfig> demandConfigs = this.listByInstances(queryParam);
        if (CollectionUtils.isEmpty(demandConfigs)) {
            return;
        }
        // 删除需求配置
        Set<Long> toBeDelConfigIds = demandConfigs.stream().map(BaseAssignIdDO::getId).collect(Collectors.toSet());
        this.demandConfigService.removeByIdIgnore(toBeDelConfigIds);
        if (publishEvent) {
            // 发布删除需求配置事件
            DemandConfig.toTemplateSceneIdAndConfigs(demandConfigs).forEach((multiKey, configs) -> {
                this.publishChangeEvent(multiKey.getKey(0), multiKey.getKey(1), Lists.newArrayList(
                        new DemandConfigChangeEvent.Detail(DataTypeEnum.DELETE, configs.stream()
                                .map(item -> new DemandConfigChangeEvent.Config(item, null))
                                .collect(Collectors.toList()))));
            });
        }
    }

    /**
     * 发布变更事件
     *
     * @param templateId 模版ID
     * @param sceneId    场景ID
     * @param configs    变更配置
     * @since 7.250801.0
     */
    private void publishChangeEvent(
            @NonNull Long templateId,
            @Nullable Long sceneId,
            @NonNull List<DemandConfigChangeEvent.Detail> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        DemandConfigChangeEvent.Message message = new DemandConfigChangeEvent.Message();
        message.setTemplateId(templateId);
        message.setSceneId(sceneId);
        message.setDetails(configs);
        applicationEventPublisher.publishEvent(new DemandConfigChangeEvent(message));
    }

    @Override
    public String logTag() {
        return "需求管理-需求配置仓储";
    }
}
