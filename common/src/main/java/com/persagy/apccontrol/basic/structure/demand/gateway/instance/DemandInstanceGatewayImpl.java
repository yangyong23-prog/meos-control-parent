package com.persagy.apccontrol.basic.structure.demand.gateway.instance;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.demand.DemandInstanceConfigAddOrEditParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandInstanceConfigConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceConfigChangeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandInstanceConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.param.DemandInstanceConfigQueryParam;
import com.persagy.apccontrol.basic.structure.demand.lifecycle.DemandInstanceConfigLifeAware;
import com.persagy.apccontrol.basic.structure.demand.model.instance.domain.DemandInstanceConfigEntity;
import com.persagy.apccontrol.basic.structure.demand.model.instance.domain.identifier.DemandInstanceConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandInstanceRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandInstanceConfigRepositoryQueryParam;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 需求实例网关实现
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Component
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandInstanceGatewayImpl implements DemandInstanceGateway {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final DemandInstanceConfigConverter demandInstanceConfigConverter;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final DemandInstanceRepository demandInstanceRepository;
    private final List<DemandInstanceConfigLifeAware> demandInstanceConfigLifeAware;

    @Override
    public int countInstanceConfigs(@NonNull DemandInstanceConfigQueryParam param) {
        DemandInstanceConfigRepositoryQueryParam queryParam = demandInstanceConfigConverter.toRepositoryQueryParam(param);
        return demandInstanceRepository.countConfigs(queryParam);
    }

    @Override
    public Set<String> listInstanceIds(@NonNull DemandInstanceConfigQueryParam param) {
        DemandInstanceConfigRepositoryQueryParam queryParam = demandInstanceConfigConverter.toRepositoryQueryParam(param);
        queryParam.addColumns(DemandInstanceConfig::getDemandInstanceId);
        List<DemandInstanceConfig> configs = demandInstanceRepository.listConfig(queryParam);
        if (CollectionUtils.isEmpty(configs)) {
            return Collections.emptySet();
        }
        return configs.stream().map(DemandInstanceConfig::getDemandInstanceId).collect(Collectors.toSet());
    }

    @Override
    public List<DemandInstanceConfigEntity> listInstanceConfigs(@NonNull @Valid DemandInstanceConfigQueryParam param) {
        DemandInstanceConfigRepositoryQueryParam queryParam = demandInstanceConfigConverter.toRepositoryQueryParam(param);
        queryParam.addColumns(
                DemandInstanceConfig::getId,
                DemandInstanceConfig::getDemandTemplateType,
                DemandInstanceConfig::getDemandInstanceType,
                DemandInstanceConfig::getDemandInstanceId,
                DemandInstanceConfig::getConfigData);
        List<DemandInstanceConfig> configs = demandInstanceRepository.listConfig(queryParam);
        if (CollectionUtils.isEmpty(configs)) {
            return Lists.newArrayList();
        }
        return configs.stream().map(DemandInstanceConfigEntity::of)
                .filter(item -> item.getConfig().valid())
                .collect(Collectors.toList());
    }

    @Override
    public Map<DtpKnowledgeInfoCodeDataSourceDTO, List<String>> mapChildrenRoomFuncTypeAndSpaceIds(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandInstanceValueObject instance) {
        Map<String, String> childrenSpaceIdAndRoomFuncType = demandInstanceRepository
                .mapChildrenSpaceIdAndRoomFuncType(templateType, instance);
        if (MapUtils.isEmpty(childrenSpaceIdAndRoomFuncType)) {
            return Maps.newHashMap();
        }
        Map<String, List<String>> roomFuncTypeAndSpaceIds = new HashMap<>();
        childrenSpaceIdAndRoomFuncType.forEach((spaceId, roomFuncType) ->
                MapExecutor.dynamicPut(roomFuncTypeAndSpaceIds, roomFuncType, spaceId, ArrayList::new));
        // 填充空间功能类型名称
        DtpKnowledgeInfoCodeDTO roomFuncTypeDefine = dtpKnowledgeClientService.getInfoCode(true,
                DtpRwdClassCodeEnum.SPACE.value(), DtpRwdSpaceDTO.Fields.roomFuncType);
        Map<String, String> dataSourceCodeAndName = roomFuncTypeDefine.toDataSourceCodeAndName();
        Map<DtpKnowledgeInfoCodeDataSourceDTO, List<String>> result = new HashMap<>();
        roomFuncTypeAndSpaceIds.forEach((roomFuncType, spaceIds) -> result.put(new DtpKnowledgeInfoCodeDataSourceDTO(
                roomFuncType, dataSourceCodeAndName.get(roomFuncType)), spaceIds));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrEditInstanceConfig(@NonNull DemandInstanceConfigAddOrEditParam param) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        DemandInstanceTypeEnum instanceType = param.getInstanceType();
        Set<String> instanceIds = param.getInstanceIds();
        DemandInstanceConfigValueObject config = param.getConfig();

        // 进入黑名单的实例ID
        Set<String> inBlacklistInstanceIds = BooleanUtils.isTrue(config.getInBlacklisted())
                ? instanceIds : Collections.emptySet();
        // 移除黑名单的实例ID
        Set<String> outBlacklistInstanceIds = BooleanUtils.isFalse(config.getInBlacklisted())
                ? instanceIds : Collections.emptySet();
        // 进入屏蔽的实例ID
        Set<String> inShieldedInstanceIds = BooleanUtils.isTrue(config.getShielded())
                ? instanceIds : Collections.emptySet();
        // 移除屏蔽的实例ID
        Set<String> outShieldedInstanceIds = BooleanUtils.isFalse(config.getShielded())
                ? instanceIds : Collections.emptySet();

        // 设置配置回调
        Consumer<DemandInstanceConfigValueObject> setConfigCallback = configData -> {
            // 处理需要编辑的字段
            if (Objects.nonNull(config.getShielded())) {
                configData.setShielded(config.getShielded());
            }
            if (Objects.nonNull(config.getInBlacklisted())) {
                configData.setInBlacklisted(config.getInBlacklisted());
            }
        };
        // 查询实例配置
        DemandInstanceConfigQueryParam queryParam = new DemandInstanceConfigQueryParam();
        queryParam.setTemplateType(templateType);
        queryParam.setInstanceType(instanceType);
        queryParam.setInstanceIds(instanceIds);
        List<DemandInstanceConfigEntity> dbConfigs = listInstanceConfigs(queryParam);
        if (Objects.isNull(instanceIds)) {
            instanceIds = dbConfigs.stream()
                    .map(item -> item.getIdentifier().getInstance().getInstanceId())
                    .collect(Collectors.toSet());
        }

        // 需要新增的实例ID
        Set<String> needAddConfigInstanceIds = new HashSet<>(instanceIds);
        // 需要编辑的实体对象
        List<DemandInstanceConfigEntity> editEntities = new ArrayList<>();
        // 需要删除的配置ID
        Set<Long> delIds = new HashSet<>();
        for (DemandInstanceConfigEntity dbConfig : dbConfigs) {
            DemandInstanceConfigIdentifier identifier = dbConfig.getIdentifier();
            DemandInstanceValueObject instance = identifier.getInstance();
            String instanceId = instance.getInstanceId();
            DemandInstanceConfigValueObject configValueObject = dbConfig.getConfig();
            // 当数据库中已存在实例配置, 则移除
            if (instanceIds.contains(instanceId)) {
                needAddConfigInstanceIds.remove(instanceId);
            }
            setConfigCallback.accept(configValueObject);
            // 配置无效时直接删除
            if (!configValueObject.valid()) {
                delIds.add(dbConfig.getIdentifier().getConfigId());
                continue;
            }
            dbConfig.setConfig(configValueObject);
            editEntities.add(dbConfig);
        }
        // 存在需要新增的实例时进行新增配置
        if (CollectionUtils.isNotEmpty(needAddConfigInstanceIds)) {
            List<DemandInstanceConfigEntity> addEntities = new ArrayList<>();
            for (String spaceId : needAddConfigInstanceIds) {
                DemandInstanceConfigValueObject configValueObject = DemandInstanceConfigValueObject.newInstance();
                setConfigCallback.accept(configValueObject);

                DemandInstanceConfigIdentifier identifier = new DemandInstanceConfigIdentifier();
                identifier.setTemplateType(templateType);
                identifier.setInstance(new DemandInstanceValueObject(instanceType, spaceId));
                DemandInstanceConfigEntity entity = new DemandInstanceConfigEntity();
                entity.setIdentifier(identifier);
                entity.setConfig(configValueObject);
                if (configValueObject.valid()) {
                    addEntities.add(entity);
                }
            }
            demandInstanceConfigLifeAware.forEach(item -> item.beforeAdd(addEntities));
            demandInstanceRepository.addConfigs(addEntities.stream()
                    .map(DemandInstanceConfigEntity::toConfigAddParam)
                    .collect(Collectors.toList()));
            demandInstanceConfigLifeAware.forEach(item -> item.afterAdd(addEntities));
        }
        // 存在需要编辑的实体时进行编辑
        if (CollectionUtils.isNotEmpty(editEntities)) {
            demandInstanceConfigLifeAware.forEach(item -> item.beforeEdit(editEntities));
            demandInstanceRepository.editConfigs(editEntities.stream()
                    .map(DemandInstanceConfigEntity::toConfigEditParam)
                    .collect(Collectors.toList()));
            demandInstanceConfigLifeAware.forEach(item -> item.afterEdit(editEntities));
        }
        // 存在需要删除的ID时进行编辑
        if (CollectionUtils.isNotEmpty(delIds)) {
            demandInstanceConfigLifeAware.forEach(item -> item.beforeDel(delIds));
            demandInstanceRepository.del(delIds);
            demandInstanceConfigLifeAware.forEach(item -> item.afterDel(delIds));
        }
        // 发布黑名单变更事件
        if (CollectionUtils.isNotEmpty(inBlacklistInstanceIds) || CollectionUtils.isNotEmpty(outBlacklistInstanceIds)) {
            DemandInstanceConfigChangeEvent.Param eventParam = new DemandInstanceConfigChangeEvent.Param();
            eventParam.setTemplateType(templateType);
            eventParam.setChangeType(DemandInstanceConfigChangeTypeEnum.BLACKLIST);
            eventParam.setInInstanceIds(inBlacklistInstanceIds);
            eventParam.setOutInstanceIds(outBlacklistInstanceIds);
            applicationEventPublisher.publishEvent(new DemandInstanceConfigChangeEvent(eventParam));
        }
        // 发布屏蔽空间变更事件
        if (CollectionUtils.isNotEmpty(inShieldedInstanceIds) || CollectionUtils.isNotEmpty(outShieldedInstanceIds)) {
            DemandInstanceConfigChangeEvent.Param eventParam = new DemandInstanceConfigChangeEvent.Param();
            eventParam.setTemplateType(templateType);
            eventParam.setChangeType(DemandInstanceConfigChangeTypeEnum.SHIELDED);
            eventParam.setInInstanceIds(inShieldedInstanceIds);
            eventParam.setOutInstanceIds(outShieldedInstanceIds);
            applicationEventPublisher.publishEvent(new DemandInstanceConfigChangeEvent(eventParam));
        }
    }

    @Override
    public String logTag() {
        return "需求管理-需求实例配置";
    }
}
