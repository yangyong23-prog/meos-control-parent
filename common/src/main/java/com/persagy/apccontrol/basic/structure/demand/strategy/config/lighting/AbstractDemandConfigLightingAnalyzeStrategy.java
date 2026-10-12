package com.persagy.apccontrol.basic.structure.demand.strategy.config.lighting;

import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.FeedbackConsistencyConflictVO;
import com.persagy.apccontrol.basic.service.demand.validator.DemandLightingFeedbackConsistencyValidator;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigSaveOrUpdateParam;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.DemandConfigLightingValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.AbstractDemandConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.Supplier;

/**
 * 照明配置策略
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
public abstract class AbstractDemandConfigLightingAnalyzeStrategy
        extends AbstractDemandConfigAnalyzeStrategy<DemandConfigLightingValueObject> {
    public static final String BEAN_NAME = "demandConfigLightingAnalyzeStrategy";

    @Autowired
    protected DemandLightingFeedbackConsistencyValidator feedbackConsistencyValidator;

    @Override
    public InheritConfigTypeEnum inheritConfigType() {
        return InheritConfigTypeEnum.OBJECT;
    }

    @Override
    public Supplier<DemandConfigLightingValueObject> emptyConfig() {
        return DemandConfigLightingValueObject::new;
    }

    @Override
    public Supplier<DemandConfigLightingValueObject> defaultConfig(
            @NonNull DemandInstanceTypeEnum instanceType) {
        return this.emptyConfig();
    }

    @Override
    public TypeReference<DemandConfigValueObject<DemandConfigLightingValueObject>> typeReference() {
        return new TypeReference<DemandConfigValueObject<DemandConfigLightingValueObject>>() {
        };
    }

    @Override
    public DemandConfigValueObject<DemandConfigLightingValueObject> beforeAddConfigHandler(
            @NonNull DemandConfigSaveOrUpdateParam<DemandConfigLightingValueObject> param) {
        DemandConfigValueObject<DemandConfigLightingValueObject> configData = param.getConfigData();
        DemandConfigLightingValueObject globalConfig = configData.getGlobalConfig();
        Long sceneId = param.getSceneId();
        if (sceneId != null && globalConfig != null) {
            List<FeedbackConsistencyConflictVO> conflicts =
                    this.feedbackConsistencyValidator.feedbackConsistencyCheck(
                            param.getTemplateId(), sceneId, globalConfig);
            if (!conflicts.isEmpty()) {
                throw new javax.validation.ValidationException(
                        LocaleMessageUtil.getMessage("key_bb2cbb9d91d94052b13789c4ab653a81"));
            }
        }
        return configData;
    }

    @Override
    public String logTag() {
        return "需求配置解析策略-照明需求";
    }
}
