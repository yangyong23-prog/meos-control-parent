package com.persagy.apccontrol.basic.structure.demand.strategy.config.airconditioning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.airconditioning.DemandAirconditioningDefaultSettingProperties;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConfigConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigSaveOrUpdateParam;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningEnvironmentQualityObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningPressureDiffQualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.ValidationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 需求配置解析策略-空调-空调需求
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Component(DemandConfigAirConditioningAnalyzeStrategy.BEAN_NAME)
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigAirConditioningAnalyzeStrategy extends AbstractDemandAirConditioningConfigAnalyzeStrategy<DemandConfigAirconditioningValueObject> {

    public static final String BEAN_NAME = "DemandConfigAirConditioningAnalyzeStrategy";

    private final ObjectMapper objectMapper;
    private final DemandAirconditioningDefaultSettingProperties demandAirconditioningDefaultSettingProperties;
    private final DemandConfigConverter demandConfigConverter;
    private final DemandConfigAirconditioningCustomService demandConfigAirconditioningCustomService;
    private final DemandConfigRepository demandConfigRepository;

    @Override
    public DemandTemplateTypeEnum templateType() {
        return DemandTemplateTypeEnum.AIRCONDITIONING;
    }

    @Override
    public InheritConfigTypeEnum inheritConfigType() {
        return InheritConfigTypeEnum.OBJECT;
    }

    @Override
    public Supplier<DemandConfigAirconditioningValueObject> emptyConfig() {
        return DemandConfigAirconditioningValueObject::new;
    }

    @Override
    public Supplier<DemandConfigAirconditioningValueObject> defaultConfig(
            @NonNull DemandInstanceTypeEnum instanceType) {
        return DemandInstanceTypeEnum.PROJECT.equals(instanceType) ? () -> {
            try {
                return objectMapper.readValue(objectMapper.writeValueAsString(
                                demandAirconditioningDefaultSettingProperties.getValueObject()),
                        DemandConfigAirconditioningValueObject.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } : emptyConfig();
    }

    @Override
    public TypeReference<DemandConfigValueObject<DemandConfigAirconditioningValueObject>> typeReference() {
        return new TypeReference<DemandConfigValueObject<DemandConfigAirconditioningValueObject>>() {
        };
    }

    @Override
    public DemandConfigValueObject<DemandConfigAirconditioningValueObject> beforeAddConfigHandler(
            @NonNull DemandConfigSaveOrUpdateParam<DemandConfigAirconditioningValueObject> param) {
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("保存空调需求配置");
        try (PerformanceTracker.SafeNode safeNode = tracker.enter(
                "DemandConfigAirConditioningAnalyzeStrategy#beforeAddConfigHandler")) {
            this.handleProjectEnvironmentQualitySwitch(param);
            safeNode.recordStep("处理项目环境品质开关");
            DemandConfigValueObject<DemandConfigAirconditioningValueObject> result =
                    this.airConditioningConfigHandler(param);
            safeNode.recordStep("保存空调自定义配置");
            return result;
        }
    }

    /**
     * 处理项目对象湿度、压差配置开关。
     *
     * <p>两个开关共享一次已存配置读取，但仍保持湿度先清理、压差后清理的写入顺序。
     * 子配置清理不单独发布事件，随后的项目配置事件会统一刷新项目及全部下级实例。</p>
     *
     * @param param 配置参数
     * @since v7.260701.0
     */
    private void handleProjectEnvironmentQualitySwitch(
            @NonNull DemandConfigSaveOrUpdateParam<DemandConfigAirconditioningValueObject> param) {
        DemandInstanceTypeEnum instanceType = param.getInstanceType();
        if (instanceType != DemandInstanceTypeEnum.PROJECT) {
            return;
        }
        DemandConfigValueObject<DemandConfigAirconditioningValueObject> configData = param.getConfigData();
        DemandConfigAirconditioningValueObject globalConfig = configData.getGlobalConfig();
        if (globalConfig == null) {
            return;
        }
        DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = globalConfig.getEnvironmentQuality();
        if (environmentQuality == null) {
            return;
        }
        boolean clearRhQuality = BooleanUtils.isFalse(environmentQuality.getIsRhQualityEnabled());
        boolean clearPressureDiffQuality =
                BooleanUtils.isFalse(environmentQuality.getIsPressureDiffQualityEnabled());
        if (!clearRhQuality && !clearPressureDiffQuality) {
            this.validatePressureDiffQuality(environmentQuality);
            return;
        }

        List<Pair<DemandConfig, DemandConfigValueObject<DemandConfigAirconditioningValueObject>>> storedConfigs =
                this.listStoredConfigs(param);
        if (clearRhQuality) {
            this.clearRhQualityConfig(configData);
            this.demandConfigRepository.editConfigDataWithoutEvent(
                    this.clearStoredQualityConfig(storedConfigs, this::clearRhQualityConfig));
        }
        if (!clearPressureDiffQuality) {
            this.validatePressureDiffQuality(environmentQuality);
            return;
        }
        this.clearPressureDiffQualityConfig(configData);
        this.demandConfigRepository.editConfigDataWithoutEvent(
                this.clearStoredQualityConfig(storedConfigs, this::clearPressureDiffQualityConfig));
    }

    /**
     * 一次查询并解析当前模板下的已存配置。
     *
     * @param param 项目配置保存参数
     * @return 按数据库顺序保留的实体及配置对象
     * @since v7.260701.0
     */
    private @NonNull List<Pair<DemandConfig,
            DemandConfigValueObject<DemandConfigAirconditioningValueObject>>> listStoredConfigs(
            @NonNull DemandConfigSaveOrUpdateParam<DemandConfigAirconditioningValueObject> param) {
        DemandConfigByInstanceRepositoryBatchQueryParam queryParam = new DemandConfigByInstanceRepositoryBatchQueryParam();
        queryParam.setTemplateId(param.getTemplateId());
        queryParam.setSceneId(param.getSceneId());
        List<Pair<DemandConfig, DemandConfigValueObject<DemandConfigAirconditioningValueObject>>> result =
                new ArrayList<>();
        for (DemandConfig demandConfig : this.demandConfigRepository.listByInstances(queryParam)) {
            JsonNode configJson = demandConfig.getConfigData();
            if (configJson != null) {
                result.add(Pair.of(demandConfig, this.analyzeConfig(configJson)));
            }
        }
        return result;
    }

    /**
     * 在共享的配置对象上顺序清理字段，并构建一次批量更新数据。
     *
     * <p>第二次调用会看到第一次清理后的对象，因此压差写入的 JSON 与原有
     * “湿度写入后重新读取”的最终内容一致。</p>
     *
     * @param storedConfigs 已解析配置
     * @param clearHandler 字段清理逻辑
     * @return 需要批量更新的实体
     * @since v7.260701.0
     */
    private @NonNull List<DemandConfig> clearStoredQualityConfig(
            @NonNull List<Pair<DemandConfig,
                    DemandConfigValueObject<DemandConfigAirconditioningValueObject>>> storedConfigs,
            @NonNull Function<DemandConfigValueObject<DemandConfigAirconditioningValueObject>, Boolean>
                    clearHandler) {
        List<DemandConfig> updates = new ArrayList<>();
        for (Pair<DemandConfig, DemandConfigValueObject<DemandConfigAirconditioningValueObject>> storedConfig
                : storedConfigs) {
            DemandConfigValueObject<DemandConfigAirconditioningValueObject> config = storedConfig.getRight();
            if (clearHandler.apply(config)) {
                DemandConfig update = this.demandConfigConverter.copy(storedConfig.getLeft());
                update.setConfigData(this.objectMapper.convertValue(config, JsonNode.class));
                updates.add(update);
            }
        }
        return updates;
    }

    /**
     * 清空压差需求配置
     *
     * @param config 配置
     * @return 是否已清空
     * @since 7.260502.0
     */
    private boolean clearPressureDiffQualityConfig(@Nullable DemandConfigValueObject<DemandConfigAirconditioningValueObject> config) {
        if (config == null) {
            return false;
        }
        AtomicBoolean edited = new AtomicBoolean(false);
        DemandConfigAirconditioningValueObject globalConfig = config.getGlobalConfig();
        if (globalConfig != null) {
            DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = globalConfig.getEnvironmentQuality();
            if (environmentQuality != null) {
                environmentQuality.setIsPressureDiffQualityEnabled(null);
                environmentQuality.setPressureDiffQuality(null);
                edited.set(true);
            }
        }
        List<DemandSpaceFuncTypeConfig<DemandConfigAirconditioningValueObject>>
                spaceFuncTypeConfigs = config.getSpaceFuncTypeConfigs();
        if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigs)) {
            spaceFuncTypeConfigs.stream()
                    .map(DemandSpaceFuncTypeConfig::getConfig)
                    .filter(Objects::nonNull)
                    .map(DemandConfigAirconditioningValueObject::getEnvironmentQuality)
                    .filter(Objects::nonNull)
                    .forEach(environmentQualityObject -> {
                        environmentQualityObject.setIsPressureDiffQualityEnabled(null);
                        environmentQualityObject.setPressureDiffQuality(null);
                        edited.set(true);
                    });
        }
        return edited.get();
    }

    /**
     * 校验压差品质配置数值
     *
     * @param environmentQuality 环境品质配置
     * @since 7.260502.0
     */
    private void validatePressureDiffQuality(DemandConfigAirconditioningEnvironmentQualityObject environmentQuality) {
        if (environmentQuality == null) {
            return;
        }
        DemandConfigAirconditioningPressureDiffQualityValueObject pressureDiffQuality = environmentQuality.getPressureDiffQuality();
        if (pressureDiffQuality == null) {
            return;
        }
        // 校验上下限值范围 [-1000, 1000]
        if (pressureDiffQuality.getLowerLimitValue() != null) {
            if (pressureDiffQuality.getLowerLimitValue().compareTo(new BigDecimal("-1000")) < 0
                    || pressureDiffQuality.getLowerLimitValue().compareTo(new BigDecimal("1000")) > 0) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_83f5355f1d564aef9de7585f0aaba234"));
            }
        }
        if (pressureDiffQuality.getUpperLimitValue() != null) {
            if (pressureDiffQuality.getUpperLimitValue().compareTo(new BigDecimal("-1000")) < 0
                    || pressureDiffQuality.getUpperLimitValue().compareTo(new BigDecimal("1000")) > 0) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_c67ce05dcf1b41dca14fcfd5b3f4f586"));
            }
        }
        // 校验下限 ≤ 上限
        if (pressureDiffQuality.getLowerLimitValue() != null && pressureDiffQuality.getUpperLimitValue() != null
                && pressureDiffQuality.getLowerLimitValue().compareTo(pressureDiffQuality.getUpperLimitValue()) > 0) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_0701b8acff5b405a99d2db135abe98ff"));
        }
    }

    /**
     * 清空湿度需求配置
     *
     * @param config 配置
     * @return 是否已清空
     * @since 7.251201.0
     */
    private boolean clearRhQualityConfig(@Nullable DemandConfigValueObject<DemandConfigAirconditioningValueObject> config) {
        if (config == null) {
            return false;
        }
        AtomicBoolean edited = new AtomicBoolean(false);
        DemandConfigAirconditioningValueObject globalConfig = config.getGlobalConfig();
        if (globalConfig != null) {
            DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = globalConfig.getEnvironmentQuality();
            if (environmentQuality != null) {
                environmentQuality.setIsRhQualityEnabled(null);
                environmentQuality.setRhQuality(null);
                edited.set(true);
            }
        }
        List<DemandSpaceFuncTypeConfig<DemandConfigAirconditioningValueObject>>
                spaceFuncTypeConfigs = config.getSpaceFuncTypeConfigs();
        if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigs)) {
            spaceFuncTypeConfigs.stream()
                    .map(DemandSpaceFuncTypeConfig::getConfig)
                    .filter(Objects::nonNull)
                    .map(DemandConfigAirconditioningValueObject::getEnvironmentQuality)
                    .filter(Objects::nonNull)
                    .forEach(environmentQualityObject -> {
                        environmentQualityObject.setIsRhQualityEnabled(null);
                        environmentQualityObject.setRhQuality(null);
                        edited.set(true);
                    });
        }
        return edited.get();
    }

    /**
     * 处理空调需求自定义配置
     *
     * @param param 新增配置参数
     */
    private DemandConfigValueObject<DemandConfigAirconditioningValueObject> airConditioningConfigHandler(DemandConfigSaveOrUpdateParam<DemandConfigAirconditioningValueObject> param) {
        if (param == null || param.getConfigData() == null) {
            return null;
        }

        DemandConfigValueObject<DemandConfigAirconditioningValueObject> configData = param.getConfigData();
        DemandConfigAirconditioningValueObject globalConfig = configData.getGlobalConfig();
        if (globalConfig == null) {
            return configData;
        }
        if (!DemandInstanceTypeEnum.PROJECT.equals(param.getInstanceType())) {
            globalConfig.setValueSelect(null);
            globalConfig.setWorkCondition(null);
            return configData;
        }

        // 处理项目级别的自定义功能全局配置：价值选择方案与空调工况
        BasicsConfigTypeEnum valueSelectType = globalConfig.getValueSelect() == null ? null : globalConfig.getValueSelect().getValueSelectType();
        this.demandConfigAirconditioningCustomService.saveOrUpdateConfigData(ContextUtil.getProjectId(), param.getTemplateId(), valueSelectType, globalConfig.getWorkCondition());
        globalConfig.setValueSelect(null);
        globalConfig.setWorkCondition(null);
        return configData;
    }

    @Override
    public String logTag() {
        return "需求配置解析策略-空调-空调需求";
    }

}
