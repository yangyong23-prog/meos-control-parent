package com.persagy.apccontrol.common.service.demand.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.param.demand.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandConfigClearTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.*;
import com.persagy.apccontrol.basic.structure.demand.gateway.scene.DemandSceneGateway;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandTemplateRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.DemandConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.task.BaseTask;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.common.service.demand.DemandService;
import com.persagy.validate.utility.enumeration.error.ErrorCodeConstants;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求服务实现
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandServiceImpl<T extends BaseDemandConfig> implements DemandService<T>, LogComponent {

    private final DtpRwdClientService dtpRwdClientService;
    private final DemandInstanceGateway demandInstanceGateway;
    private final DemandConfigGateway<T> demandConfigGateway;
    private final DemandTemplateRepository demandTemplateRepository;
    private final DemandConfigAirconditioningCustomService demandConfigAirconditioningCustomService;
    private final DemandSceneGateway demandSceneGateway;

    @Override
    public boolean addInstanceConfig(@NonNull DemandInstanceConfigAddOrEditParam param) {
        Set<String> instanceIds = param.getInstanceIds();
        // 校验入参实例ID有效性
        Set<String> existsInstanceIds = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                        .setId(instanceIds).addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        if (existsInstanceIds.size() != instanceIds.size()) {
            logProjectInstance().error("未找到部分实例", Collections.singletonList(
                    Pair.of("notFoundInstanceId", CollectionUtils.subtract(instanceIds, existsInstanceIds))));
            return false;
        }
        demandInstanceGateway.addOrEditInstanceConfig(param);
        return true;
    }

    @Override
    @SneakyThrows
    public Object addDemandConfig(DemandAddParam param) {
        param.checkAndSet();
        DemandTemplate template = demandTemplateRepository.getByTemplateId(true, param.getTemplateId());
        DemandConfigAnalyzeStrategy<T> demandConfigAnalyzeStrategy =
                template.getDemandTemplateType().demandConfigAnalyzeStrategy();
        DemandConfigValueObject<T> config = demandConfigAnalyzeStrategy.analyzeConfig(param.getConfigData());
        DemandConfigSaveOrUpdateParam<T> gatewayParam = new DemandConfigSaveOrUpdateParam<>();
        gatewayParam.setTemplateId(param.getTemplateId());
        gatewayParam.setSceneId(param.getSceneId());
        gatewayParam.setConfigGroupId(param.getConfigGroupId());
        gatewayParam.setConfigGroupName(param.getConfigGroupName());
        if (Objects.nonNull(param.getInstanceType()) && CollectionUtils.isNotEmpty(param.getInstanceIds())) {
            gatewayParam.setInstanceType(param.getInstanceType());
            gatewayParam.setInstanceIds(param.getInstanceIds());
        }
        gatewayParam.setConfigData(config);
        gatewayParam.setAddRoomFuncType(param.getAddRoomFuncType());
        gatewayParam.setDeleteFlag(param.getDeleteFlag());
        Long groupId = demandConfigGateway.addOrUpdateConfig(gatewayParam);
        return Objects.nonNull(groupId) ? groupId : true;
    }

    @Override
    public void demandReferenceSettings(DemandReferenceParam param) {
        DemandConfigReferenceCancelParam p = new DemandConfigReferenceCancelParam();
        p.setTemplateId(param.getTemplateId());
        p.setSceneId(param.getSceneId());
        p.setInstance(new DemandInstanceValueObject(param.getInstanceType(), param.getInstanceId()));
        p.setClearConfigType(param.getReferenceType());
        demandConfigGateway.cancelReference(p);
    }

    @Override
    public void demandClearSettings(DemandClearParam param) {
        param.checkAndSet();
        Long templateId = param.getTemplateId();

        DemandConfigClearParam clearConfig = new DemandConfigClearParam();
        clearConfig.setTemplateId(templateId);
        clearConfig.setSceneId(param.getSceneId());

        if (Objects.nonNull(param.getConfigGroupId())) {
            DemandConfigByInstanceBatchQueryParam instanceQueryParam = new DemandConfigByInstanceBatchQueryParam();
            instanceQueryParam.setTemplateId(templateId);
            instanceQueryParam.setConfigGroupIds(SetUtils.unmodifiableSet(param.getConfigGroupId()));
            List<DemandConfigAggregate<T>> demandConfigAggregates = demandConfigGateway.listConfigsByInstances(instanceQueryParam);
            if (CollectionUtils.isEmpty(demandConfigAggregates)) {
                logProjectInstance().error("未找到配置组对应的实例", Collections.singletonList(
                        Pair.of("notFoundInstanceId", param.getConfigGroupId())));
                return;
            }

            Set<String> instanceIds = demandConfigAggregates.stream()
                    .map(DemandConfigAggregate::getIdentifier)
                    .map(DemandConfigIdentifier::getInstance)
                    .map(DemandInstanceValueObject::getInstanceId)
                    .collect(Collectors.toSet());
            clearConfig.setInstanceIds(instanceIds);
            clearConfig.setConfigGroupId(param.getConfigGroupId());
            clearConfig.setClearConfigType(DemandConfigClearTypeEnum.CURRENT);
        } else {
            clearConfig.setInstanceIds(Collections.singleton(param.getInstanceId()));
            clearConfig.setClearConfigType(param.getClearRange());
        }
        clearConfig.setInstanceType(param.getInstanceType());
        demandConfigGateway.clearConfig(clearConfig);

        // 同步清除末端自定义功能配置
        if (DemandInstanceTypeEnum.PROJECT.equals(param.getInstanceType())) {
            this.demandConfigAirconditioningCustomService.clearConfigData(param.getInstanceId(), templateId.toString());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copyConfigs(@NonNull DemandConfigCopyParam param) {
        DemandConfigCopyPairParam fromCopyParam = new DemandConfigCopyPairParam();
        fromCopyParam.setTemplateId(param.getFromTemplateId());
        fromCopyParam.setSceneId(param.getFromSceneId());
        DemandConfigCopyPairParam toCopyParam = new DemandConfigCopyPairParam();
        toCopyParam.setTemplateId(param.getToTemplateId());
        toCopyParam.setSceneId(param.getToSceneId());
        try {
            demandConfigGateway.copyConfig(Pair.of(fromCopyParam, toCopyParam));
        } catch (Exception e) {
            logProjectInstance().error("场景复制失败", e);
            throw new ApplicationException(ErrorCodeConstants.SYSTEM_ERROR_B0001,
                    LocaleMessageUtil.getMessage("key_demand_copy_config_error"));
        }
        return true;
    }

    @Override
    public boolean checkConfigGroupNameExists(@NonNull DemandConfigGroupNameCheckParam param) {
        return demandConfigGateway.checkConfigGroupNameExists(param);
    }

    @Override
    public boolean clearAirConditioningSpaceRoomFuncTypes() {
        return new ClearAirConditioningSpaceRoomFuncTypes().allExecute();
    }

    public class ClearAirConditioningSpaceRoomFuncTypes extends BaseTask {

        @SneakyThrows
        public boolean allExecute() {
            asyncHandlerGroup(null, group -> {
                execute();
            });
            return true;
        }

        public boolean execute() {
            List<Long> templateIds = demandTemplateRepository.listTemplateIdsByTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
            if (CollectionUtils.isEmpty(templateIds)) {
                return true;
            }
            DemandConfigService demandConfigService = SpringUtil.getBean(DemandConfigService.class);
            List<DemandConfig> editConfigs = new ArrayList<>();
            for (Long templateId : templateIds) {
                // 只处理建筑和楼层, 项目不用处理, 空间没有
                Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds = new HashMap<>();
                instanceTypeAndInstanceIds.put(DemandInstanceTypeEnum.BUILDING, null);
                instanceTypeAndInstanceIds.put(DemandInstanceTypeEnum.FLOOR, null);

                DemandConfigByInstanceRepositoryBatchQueryParam queryParam = new DemandConfigByInstanceRepositoryBatchQueryParam();
                queryParam.setTemplateId(templateId);
                queryParam.setInstanceTypeAndInstanceIds(instanceTypeAndInstanceIds);
                List<DemandConfig> demandConfigs = demandConfigService.listByInstances(queryParam);
                if (CollectionUtils.isEmpty(demandConfigs)) {
                    continue;
                }
                for (DemandConfig demandConfig : demandConfigs) {
                    JsonNode configData = demandConfig.getConfigData();
                    if (Objects.isNull(configData)) {
                        continue;
                    }
                    JsonNode spaceFuncTypeConfigs = configData.get(DemandConfigValueObject.Fields.spaceFuncTypeConfigs);
                    if (Objects.isNull(spaceFuncTypeConfigs) || spaceFuncTypeConfigs.isEmpty()) {
                        continue;
                    }
                    // 清空建筑和楼层的空间功能类型
                    ObjectNode tempConfig = (ObjectNode) configData;
                    tempConfig.remove(DemandConfigValueObject.Fields.spaceFuncTypeConfigs);
                    demandConfig.setConfigData(tempConfig);
                    editConfigs.add(demandConfig);
                }
            }
            if (CollectionUtils.isEmpty(editConfigs)) {
                return false;
            }
            return demandConfigService.updateBatchById(editConfigs);
        }

        @Override
        public String logTag() {
            return "清除空调需求空间功能类型";
        }
    }

    @Override
    public String logTag() {
        return "需求服务";
    }
}
