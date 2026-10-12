package com.persagy.apccontrol.basic.structure.intervention.strategy.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandConfigLightingDataDTO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitSettingDTO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingFuncTypeSettingDTO;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.*;
import com.persagy.apccontrol.basic.model.vo.intervention.ConfigDetailsVo;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionQueryParam;
import com.persagy.apccontrol.basic.model.vo.intervention.LightConfigData;
import com.persagy.apccontrol.basic.resource.intervention.InterventionConfigQueryResourceService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandConfigLightingHandler;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.model.valueobject.DateTimeRangeValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionOperationTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionStatusEnum;
import com.persagy.apccontrol.basic.structure.intervention.event.lighting.InterventionLightingConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.intervention.event.lighting.InterventionLightingCurrentConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.InterventionConfigQueryInfrastructure;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.param.InterventionConfigQueryParam;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import com.persagy.apccontrol.basic.structure.intervention.model.dto.ReleaseInterventionCommand;
import com.persagy.apccontrol.basic.structure.intervention.strategy.InterventionConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 照明干预配置解析策略
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Component(InterventionLightingConfigAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InterventionLightingConfigAnalyzeStrategy implements InterventionConfigAnalyzeStrategy, LogComponent {

    public static final String BEAN_NAME = "interventionLightingConfigAnalyzeStrategy";

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final InterventionConfigQueryResourceService interventionConfigQueryResourceService;
    private final DemandConfigLightingHandler demandConfigLightingHandler;
    private final InstanceService instanceService;
    private final InterventionConfigQueryInfrastructure<? extends InterventionConfigData> interventionConfigQueryInfrastructure;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.LIGHTING;
    }

    @Override
    public InheritConfigTypeEnum inheritConfigType() {
        return InheritConfigTypeEnum.OBJECT;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends InterventionConfigData> Class<T> configType() {
        return (Class<T>) DemandConfigLightingDataDTO.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends InterventionConfigData> Supplier<T> createSettingSupplier() {
        return () -> (T) DemandConfigLightingDataDTO.newInstance();
    }

    @Override
    public <T extends InterventionConfigData> T analyzeConfigObject(@NonNull JsonNode configData) {
        try {
            return objectMapper.treeToValue(configData, configType());
        } catch (JsonProcessingException e) {
            logProjectInstance().error("解析配置失败", e, Pair.of("data", configData));
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T extends InterventionConfigData> void publishInterventionChangeEvent(
            @NonNull InterventionOperationTypeEnum operationType,
            @NonNull InterventionConfigModel<T> configModel) {
        switch (operationType) {
            case ADD:
            case EDIT:
                applicationEventPublisher.publishEvent(new InterventionLightingConfigChangeEvent<>(configModel));
                break;
            case RELEASE:
                // 当前场景正在生效中时, 直接删除预指令
                if (configModel.isEffectiveByNow()) {
                    Set<ReleaseInterventionCommand> releaseInterventionCommands = generateReleaseCommands(configModel);
                    applicationEventPublisher.publishEvent(new InterventionLightingCurrentConfigChangeEvent<>(
                            configModel, releaseInterventionCommands));
                    break;
                }
                applicationEventPublisher.publishEvent(new InterventionLightingConfigChangeEvent<>(configModel));
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(operationType.getClass().getSimpleName(), operationType);
        }
    }

    @Override
    public ConfigDetailsVo getConfigDetails(InterventionQueryParam param) {
        InterventionConfig interventionConfig = interventionConfigQueryResourceService.getById(param.getConfigId());
        ConfigDetailsVo configDetailsVo = new ConfigDetailsVo();
        configDetailsVo.setReason(interventionConfig.getReason());
        configDetailsVo.setBeginTime(DateUtil.format(interventionConfig.getBeginTime(), "yyyy-MM-dd HH:mm:ss"));
        configDetailsVo.setEndTime(DateUtil.format(interventionConfig.getEndTime(), "yyyy-MM-dd HH:mm:ss"));
        DemandConfigLightingGlobalConfigVO globalConfigVO = new DemandConfigLightingGlobalConfigVO();
        DemandConfigLightingSpaceConfigVO spaceConfigVO = new DemandConfigLightingSpaceConfigVO();
        configDetailsVo.setConfigData(new LightConfigData(globalConfigVO, spaceConfigVO));

        JsonNode configData = interventionConfig.getConfigData();
        DemandConfigLightingDataDTO interventionConfigData = analyzeConfigObject(configData);

        // 1、=====查询实例ID对应的基线配置树=====
        DemandLightingTypeGroupTree lightingTypeGroupTree = demandConfigLightingHandler.getDemandLightingTypeGroupTree(ContextUtil.getProjectId(), null, DemandTemplateTypeEnum.analyze(param.getRunningMonitorSecondType().value()),
                param.getInstanceId(), param.getInstanceType(), -1);
        if (CollUtil.isEmpty(lightingTypeGroupTree.getFuncTypeSettings())) {
            log().info("无可配置回路对象");
            return configDetailsVo;
        }

        // 回路总数
        int total = 0;
        for (DemandLightingFuncTypeSettingVO funcTypeSetting : lightingTypeGroupTree.getFuncTypeSettings()) {
            total += funcTypeSetting.getCircuitIds().size();
        }

        // 全局配置
        if (CollUtil.isEmpty(interventionConfigData.getGlobalSettings())) {
            DemandLightingCircuitSummaryVO summaryVO = new DemandLightingCircuitSummaryVO(total, null, null, null, null);
            globalConfigVO.setSummary(summaryVO);
        } else {
            //-----全局配置构建-----
            List<DemandLightingFuncTypeSettingVO> funcTypeSettings = demandConfigLightingHandler.globalConfigBuilding(lightingTypeGroupTree,
                    interventionConfigData.getGlobalSettings());
            globalConfigVO.setFuncTypeSettings(funcTypeSettings);
            //-----统计-----
            demandConfigLightingHandler.configSummary(globalConfigVO);
        }

        //=====空间类型配置=====
        List<DemandLightingSpaceFuncTypeSettingVO> spaceFuncTypeSettings;

        if (CollUtil.isNotEmpty(interventionConfigData.getSpaceSettings())
                && !DemandInstanceTypeEnum.SPACE.getValue().equals(interventionConfig.getInstanceType().getValue())) {
            spaceFuncTypeSettings = demandConfigLightingHandler.spaceConfigBuildingAndSummary(param.getInstanceType(), lightingTypeGroupTree, interventionConfigData.getSpaceSettings());
            if (CollUtil.isNotEmpty(spaceFuncTypeSettings)) {
                //-----空间在引用时，根据该空间的空间类型查询是否有空间类型的配置，若有则页面显示的内容为空间类型的配置；若没有则引用全局配置-----
                if (DemandInstanceTypeEnum.SPACE.equals(param.getInstanceType())) {
                    //-----使用空间类型配置作为全局配置使用-----
                    globalConfigVO.setFuncTypeSettings(spaceFuncTypeSettings.get(0).getFuncTypeSettings());
                    globalConfigVO.setSummary(spaceFuncTypeSettings.get(0).getSummary());
                } else {
                    spaceConfigVO.setSettings(spaceFuncTypeSettings);
                }
            }
        }

        return configDetailsVo;
    }

    /**
     * 生成解除干预指令
     *
     * @param configModel 配置模型
     * @param <T>         指令类型
     * @return 解除干预指令
     * @since 5.240702.0
     */
    private <T extends InterventionConfigData> Set<ReleaseInterventionCommand> generateReleaseCommands(
            @NonNull InterventionConfigModel<T> configModel) {
        String projectId = ContextUtil.getProjectId();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = configModel.getRunningMonitorSecondType();
        DemandInstanceValueObject instance = configModel.getInstance();
        DemandInstanceTypeEnum instanceType = instance.getInstanceType();
        String instanceId = instance.getInstanceId();

        if (instanceType == DemandInstanceTypeEnum.SPACE) {
            DemandConfigLightingDataDTO finalConfig = (DemandConfigLightingDataDTO) configModel.getConfig().getFinalConfig();
            List<DemandLightingFuncTypeSettingDTO> globalSettings = finalConfig.getGlobalSettings();
            return analyzeSpaceReleaseInfoCode(instanceId, globalSettings);
        }
        Set<String> relSpaceIds = instanceService.listSpaceIdsByInstance(instanceType, instanceId);
        if (CollectionUtils.isEmpty(relSpaceIds)) {
            return Sets.newHashSet();
        }
        LocalDateTime now = DateUtils.now();
        InterventionConfigQueryParam configQueryParam = new InterventionConfigQueryParam();
        configQueryParam.setRunningMonitorSecondType(runningMonitorSecondType);
        configQueryParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
        configQueryParam.setFilterInstanceIds(relSpaceIds);
        configQueryParam.setDateTimeContainRange(new DateTimeRangeValueObject(now, now));
        configQueryParam.setInterventionStatus(InterventionStatusEnum.EFFECTING);
        List<? extends InterventionConfigModel<? extends InterventionConfigData>> configModels =
                interventionConfigQueryInfrastructure.of(configQueryParam);
        if (CollectionUtils.isEmpty(configModels)) {
            return Sets.newHashSet();
        }
        Set<ReleaseInterventionCommand> result = new HashSet<>();
        for (InterventionConfigModel<? extends InterventionConfigData> model : configModels) {
            DemandConfigLightingDataDTO finalConfig = (DemandConfigLightingDataDTO) configModel.getConfig().getFinalConfig();
            List<DemandInstanceValueObject> inheritInstances = model.getConfig().getInheritInstances();
            if (!model.isEffectiveByNow()) {
                continue;
            }
            // 当空间配置引用了当前解除干预实例时, 认为该空间需要被解除
            if (inheritInstances.stream().anyMatch(item -> StringUtils.equals(item.getInstanceId(), instanceId))) {
                result.addAll(analyzeSpaceReleaseInfoCode(instanceId, finalConfig.getGlobalSettings()));
            }
        }
        return result;
    }

    /**
     * 解析空间解除干预信息点指令
     *
     * @param spaceId  空间ID
     * @param settings 配置
     * @return 空间解除干预信息点指令
     * @since 5.240702.0
     */
    private Set<ReleaseInterventionCommand> analyzeSpaceReleaseInfoCode(
            @NonNull String spaceId, @NonNull List<DemandLightingFuncTypeSettingDTO> settings) {
        if (CollectionUtils.isEmpty(settings)) {
            return Sets.newHashSet();
        }
        Set<ReleaseInterventionCommand> result = new HashSet<>();
        for (DemandLightingFuncTypeSettingDTO setting : settings) {
            DemandLightingTypeSettingModeEnum settingMode = setting.getSettingMode();
            DtpRwdCircuitFuncTypeEnum funcType = setting.getFuncType();
            List<DemandLightingCircuitSettingDTO> circuitSettings = setting.getCircuitSettings();
            List<AutoMaticInfoCodeEnum> autoMaticInfoCodeEnums = listAllLightingReleaseInfoCodeEnum();
            switch (settingMode) {
                case ALL_OPEN:
                case ALL_CLOSE:
                    result.addAll(autoMaticInfoCodeEnums.stream()
                            .filter(item -> StringUtils.equals(item.getCode(), funcType.value()))
                            .map(item -> new ReleaseInterventionCommand(spaceId, item.getPracticalCode()))
                            .collect(Collectors.toList()));
                case PARTIAL_OPEN:
                    if (CollectionUtils.isNotEmpty(circuitSettings)) {
                        result.addAll(circuitSettings.stream()
                                .map(item -> new ReleaseInterventionCommand(item.getCircuitId(),
                                        AutoMaticInfoCodeEnum.CONTROL_DEMAND.getPracticalCode()))
                                .collect(Collectors.toList()));
                    }
                default:
            }
        }
        return result;
    }

    /**
     * 获取所有炸名解除信息点枚举
     *
     * @return 所有炸名解除信息点枚举
     * @since 5.240702.0
     */
    private List<AutoMaticInfoCodeEnum> listAllLightingReleaseInfoCodeEnum() {
        return Arrays.stream(AutoMaticInfoCodeEnum.values())
                .filter(item -> StringUtils.equals(item.getId(), "4"))
                .collect(Collectors.toList());
    }

    @Override
    public String logTag() {
        return "干预配置解析策略-照明";
    }
}
