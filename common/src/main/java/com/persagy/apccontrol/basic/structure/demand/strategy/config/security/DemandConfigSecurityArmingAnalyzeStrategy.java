package com.persagy.apccontrol.basic.structure.demand.strategy.config.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigSaveOrUpdateParam;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.security.DemandConfigSecurityArmingValueObject;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 需求配置解析策略-安防-撤布防需求
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Component(DemandConfigSecurityArmingAnalyzeStrategy.BEAN_NAME)
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigSecurityArmingAnalyzeStrategy
        extends AbstractDemandSecurityConfigAnalyzeStrategy<DemandConfigSecurityArmingValueObject> {

    public static final String BEAN_NAME = "demandConfigSecurityArmingAnalyzeStrategy";

    @Override
    public DemandTemplateTypeEnum templateType() {
        return DemandTemplateTypeEnum.ARMING;
    }

    @Override
    public InheritConfigTypeEnum inheritConfigType() {
        return InheritConfigTypeEnum.OBJECT;
    }

    @Override
    public Supplier<DemandConfigSecurityArmingValueObject> emptyConfig() {
        return DemandConfigSecurityArmingValueObject::new;
    }

    @Override
    public Supplier<DemandConfigSecurityArmingValueObject> defaultConfig(
            @NonNull DemandInstanceTypeEnum instanceType) {
        return emptyConfig();
    }

    @Override
    public TypeReference<DemandConfigValueObject<DemandConfigSecurityArmingValueObject>> typeReference() {
        return new TypeReference<DemandConfigValueObject<DemandConfigSecurityArmingValueObject>>() {
        };
    }

    @Override
    public DemandConfigValueObject<DemandConfigSecurityArmingValueObject> beforeAddConfigHandler(
            @NonNull DemandConfigSaveOrUpdateParam<DemandConfigSecurityArmingValueObject> param) {
        return armingConfigHandler(param);
    }

    /**
     * 处理安防撤布防的配置
     *
     * @param param 新增配置参数
     */
    private DemandConfigValueObject<DemandConfigSecurityArmingValueObject> armingConfigHandler(
            DemandConfigSaveOrUpdateParam<DemandConfigSecurityArmingValueObject> param) {
        if (param == null) {
            return null;
        }
        DemandConfigValueObject<DemandConfigSecurityArmingValueObject> configData = param.getConfigData();
        if (configData == null) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_2d7e0a8432f44c7f889ad60b4df43dc8"));
        }
        DemandConfigSecurityArmingValueObject globalConfig = configData.getGlobalConfig();
        List<DemandTimeIntervalValueObject> list;
        if (globalConfig != null) {
            list = globalConfig.getArmingTimeIntervals();
            handlerArmingValue(globalConfig, list, param.getDeleteFlag());
        } else {
            //空的时候存一个空对象，代表对象类型的集成不去找继承关系
            configData.setGlobalConfig(new DemandConfigSecurityArmingValueObject());
        }
        List<DemandSpaceFuncTypeConfig<DemandConfigSecurityArmingValueObject>> spaceFuncTypeConfigs =
                configData.getSpaceFuncTypeConfigs();
        if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigs)) {
            spaceFuncTypeConfigs.forEach(spaceFuncTypeConfig -> handlerArmingValue(
                    spaceFuncTypeConfig.getConfig(),
                    spaceFuncTypeConfig.getConfig().getArmingTimeIntervals(),
                    param.getDeleteFlag()));
        } else {
            configData.setSpaceFuncTypeConfigs(new ArrayList<>());
        }

        return configData;
    }

    /**
     * 处理撤布防的时间段配置
     *
     * @param armingValueObject   撤布防配置
     * @param armingTimeIntervals 撤防时间段
     * @param deleteFlag          是否是删除标识
     */
    void handlerArmingValue(DemandConfigSecurityArmingValueObject armingValueObject,
                            List<DemandTimeIntervalValueObject> armingTimeIntervals,
                            Boolean deleteFlag) {
        if (CollectionUtils.isEmpty(armingTimeIntervals)) {
            //全天撤防
            List<DemandTimeIntervalValueObject> disarmingTimeIntervals = new ArrayList<>();
            disarmingTimeIntervals.add(DemandTimeIntervalValueObject.initFullDayData());
            if (!deleteFlag) {
                armingValueObject.setDisarmingTimeIntervals(disarmingTimeIntervals);
            }
        } else {
            if (!deleteFlag) {
                List<DemandTimeIntervalValueObject> disarmingTimeIntervals =
                        calculateNotGivenDemandTimeIntervalValue(armingTimeIntervals);
                armingValueObject.setDisarmingTimeIntervals(disarmingTimeIntervals);
            }
        }
    }

    @Override
    public String logTag() {
        return "需求配置解析策略-安防-撤布防需求";
    }
}
