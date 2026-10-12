package com.persagy.apccontrol.basic.structure.lighitng.listener;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitGroupSettingDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigService;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.DemandConfigLightingQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.po.instance.group.DemandInstanceGroupPO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.evnet.DemandInstanceGroupChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.DemandConfigLightingValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingFuncTypeValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandTemplateRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.DemandConfigAnalyzeStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 照明需求实例分组变更监听器
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LightingDemandInstanceGroupChangedListener implements LogComponent {

    private final DemandTemplateRepository demandTemplateRepository;
    private final DemandConfigRepository demandConfigRepository;
    private final DemandConfigService demandConfigService;

    @EventListener(classes = DemandInstanceGroupChangeEvent.class)
    public void execute(@NonNull DemandInstanceGroupChangeEvent event) {
        DemandInstanceGroupChangeEvent.Message message = (DemandInstanceGroupChangeEvent.Message) event.getSource();
        DemandTemplateTypeEnum templateType = message.getTemplateType();
        DataTypeEnum type = message.getType();
        DemandInstanceGroupPO oldGroup = message.getOldGroup();
        // 非删除时不处理
        if (type != DataTypeEnum.DELETE) {
            return;
        }
        DemandConfigLightingQueryDaoParam queryDaoParam = new DemandConfigLightingQueryDaoParam();
        queryDaoParam.setDemandType(templateType.toDemandTypeEnum());
        queryDaoParam.setDemandTemplateType(templateType);
        queryDaoParam.setLikeConfigData(oldGroup.getId());

        List<Long> templateIds = demandTemplateRepository.listTemplateIdsByTemplateType(templateType);
        if (CollectionUtils.isEmpty(templateIds)) {
            return;
        }
        DemandConfigByInstanceRepositoryBatchQueryParam batchQueryParam = new DemandConfigByInstanceRepositoryBatchQueryParam();
        batchQueryParam.setTemplateIds(templateIds);
        batchQueryParam.setLikeConfigData(oldGroup.getId());
        List<DemandConfig> configs = demandConfigRepository.listByInstances(batchQueryParam);
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        DemandConfigAnalyzeStrategy<DemandConfigLightingValueObject> configAnalyzeStrategy =
                templateType.demandConfigAnalyzeStrategy();
        Map<Long, DemandConfig> editConfigIdAndData = new HashMap<>();
        for (DemandConfig config : configs) {
            Long configId = config.getId();
            DemandConfigValueObject<DemandConfigLightingValueObject> configData =
                    configAnalyzeStrategy.analyzeConfig(config.getConfigData());
            DemandConfigLightingValueObject globalConfig = configData.getGlobalConfig();
            List<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>> spaceFuncTypeConfigs =
                    configData.getSpaceFuncTypeConfigs();
            if (Objects.nonNull(globalConfig)) {
                if (handleFuncTypeConfig(message, globalConfig.getGlobalSettings())) {
                    editConfigIdAndData.put(configId, config);
                }
                Map<String, List<DemandConfigLightingFuncTypeValueObject>> instanceIdAndFeedbackAdjustSettings =
                        globalConfig.getInstanceIdAndFeedbackAdjustSettings();
                if (MapUtils.isNotEmpty(instanceIdAndFeedbackAdjustSettings)) {
                    instanceIdAndFeedbackAdjustSettings.values().forEach((item) -> {
                        if (handleFuncTypeConfig(message, item)) {
                            editConfigIdAndData.put(configId, config);
                        }
                    });
                }
            }
            if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigs)) {
                spaceFuncTypeConfigs.stream()
                        .map(DemandSpaceFuncTypeConfig::getConfig)
                        .filter(Objects::nonNull).forEach(item -> {
                            if (handleFuncTypeConfig(message, item.getGlobalSettings())) {
                                editConfigIdAndData.put(configId, config);
                            }
                        });
            }
            if (editConfigIdAndData.containsKey(configId)) {
                config.fillConfigData(configData);
            }
        }
        if (MapUtils.isNotEmpty(editConfigIdAndData)) {
            demandConfigService.updateByIdValidate(editConfigIdAndData.values());
        }
    }

    /**
     * 处理功能类型配置
     *
     * @param message          消息
     * @param funcTypeSettings 功能类型配置
     * @return 是否被修改
     * @since 7.250602.0
     */
    private boolean handleFuncTypeConfig(
            DemandInstanceGroupChangeEvent.@NonNull Message message,
            @Nullable List<DemandConfigLightingFuncTypeValueObject> funcTypeSettings) {
        if (CollectionUtils.isEmpty(funcTypeSettings)) {
            return false;
        }
        DataTypeEnum type = message.getType();
        DemandInstanceGroupPO oldGroup = message.getOldGroup();

        AtomicBoolean edited = new AtomicBoolean(false);
        funcTypeSettings.removeIf(globalSetting -> {
            DemandLightingTypeSettingModeEnum settingMode = globalSetting.getSettingMode();
            if (settingMode != DemandLightingTypeSettingModeEnum.INSTANCE_GROUP) {
                return false;
            }
            List<DemandLightingCircuitGroupSettingDTO> circuitGroupSettings = globalSetting.getCircuitGroupSettings();
            if (CollectionUtils.isNotEmpty(circuitGroupSettings)) {
                Iterator<DemandLightingCircuitGroupSettingDTO> iterator = circuitGroupSettings.iterator();
                while (iterator.hasNext()) {
                    DemandLightingCircuitGroupSettingDTO groupSetting = iterator.next();
                    Long groupId = groupSetting.getGroupId();
                    if (!Objects.equals(groupId, oldGroup.getId())) {
                        continue;
                    }
                    if (type == DataTypeEnum.DELETE) {
                        iterator.remove();
                        edited.set(true);
                    }
                }
            }
            return CollectionUtils.isEmpty(circuitGroupSettings);
        });
        return edited.get();
    }

    @Override
    public String logTag() {
        return "照明需求实例编组变更监听器";
    }
}
