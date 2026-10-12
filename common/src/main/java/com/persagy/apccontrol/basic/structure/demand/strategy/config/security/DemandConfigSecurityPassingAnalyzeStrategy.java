package com.persagy.apccontrol.basic.structure.demand.strategy.config.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigSaveOrUpdateParam;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.security.DemandConfigSecurityPassingValueObject;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 需求配置解析策略-安防-通行需求
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Component(DemandConfigSecurityPassingAnalyzeStrategy.BEAN_NAME)
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigSecurityPassingAnalyzeStrategy
        extends AbstractDemandSecurityConfigAnalyzeStrategy<DemandConfigSecurityPassingValueObject> {

    public static final String BEAN_NAME = "demandConfigSecurityPassingAnalyzeStrategy";

    @Override
    public DemandTemplateTypeEnum templateType() {
        return DemandTemplateTypeEnum.PASSING;
    }

    @Override
    public InheritConfigTypeEnum inheritConfigType() {
        return InheritConfigTypeEnum.OBJECT;
    }

    @Override
    public Supplier<DemandConfigSecurityPassingValueObject> emptyConfig() {
        return DemandConfigSecurityPassingValueObject::new;
    }

    @Override
    public Supplier<DemandConfigSecurityPassingValueObject> defaultConfig(
            @NonNull DemandInstanceTypeEnum instanceType) {
        return emptyConfig();
    }

    @Override
    public TypeReference<DemandConfigValueObject<DemandConfigSecurityPassingValueObject>> typeReference() {
        return new TypeReference<DemandConfigValueObject<DemandConfigSecurityPassingValueObject>>() {
        };
    }

    @Override
    public DemandConfigValueObject<DemandConfigSecurityPassingValueObject> beforeAddConfigHandler(
            @NonNull DemandConfigSaveOrUpdateParam<DemandConfigSecurityPassingValueObject> param) {
        return passConfigHandler(param);
    }

    private DemandConfigValueObject<DemandConfigSecurityPassingValueObject> passConfigHandler(
            DemandConfigSaveOrUpdateParam<DemandConfigSecurityPassingValueObject> param) {
        if (param == null) {
            return null;
        }
        DemandConfigValueObject<DemandConfigSecurityPassingValueObject> configData = param.getConfigData();
        if (configData == null) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_2d7e0a8432f44c7f889ad60b4df43dc8"));
        }
        DemandConfigSecurityPassingValueObject globalConfig = configData.getGlobalConfig();
        if (Objects.isNull(globalConfig)) {
            globalConfig = new DemandConfigSecurityPassingValueObject();
            configData.setGlobalConfig(globalConfig);
        } else {
            List<DemandTimeIntervalValueObject> passableTimeIntervals = globalConfig.getPassableTimeIntervals();
            List<DemandTimeIntervalValueObject> unpassableTimeIntervals = globalConfig.getUnpassableTimeIntervals();
            handlerPassingValue(globalConfig, passableTimeIntervals, unpassableTimeIntervals, param.getDeleteFlag());
        }
        List<DemandSpaceFuncTypeConfig<DemandConfigSecurityPassingValueObject>> spaceFuncTypeConfigs =
                configData.getSpaceFuncTypeConfigs();
        if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigs)) {
            spaceFuncTypeConfigs.forEach(spaceFuncTypeConfig -> {
                List<DemandTimeIntervalValueObject> passableTimeIntervals1 =
                        spaceFuncTypeConfig.getConfig().getPassableTimeIntervals();
                List<DemandTimeIntervalValueObject> unpassableTimeIntervals1 =
                        spaceFuncTypeConfig.getConfig().getUnpassableTimeIntervals();
                handlerPassingValue(spaceFuncTypeConfig.getConfig(),
                        passableTimeIntervals1, unpassableTimeIntervals1, param.getDeleteFlag());
            });
        } else {
            configData.setSpaceFuncTypeConfigs(new ArrayList<>());
        }
        return configData;
    }

    /**
     * 处理通行的时间段配置
     *
     * @param passingValueObject      通行的配置
     * @param passableTimeIntervals   通行时间段
     * @param unpassableTimeIntervals 不可通行的时间段
     * @param deleteFlag              是否是删除接口
     */

    void handlerPassingValue(DemandConfigSecurityPassingValueObject passingValueObject,
                             List<DemandTimeIntervalValueObject> passableTimeIntervals,
                             List<DemandTimeIntervalValueObject> unpassableTimeIntervals,
                             Boolean deleteFlag) {
        if (CollectionUtils.isNotEmpty(passableTimeIntervals) || CollectionUtils.isNotEmpty(unpassableTimeIntervals)) {
            List<DemandTimeIntervalValueObject> noPermissionsTimeIntervals = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(passableTimeIntervals)) {
                noPermissionsTimeIntervals.addAll(passableTimeIntervals);
            }
            if (CollectionUtils.isNotEmpty(unpassableTimeIntervals)) {
                noPermissionsTimeIntervals.addAll(unpassableTimeIntervals);
            }
            List<DemandTimeIntervalValueObject> permissionPassTimeIntervals =
                    calculateNotGivenDemandTimeIntervalValue(noPermissionsTimeIntervals);
            passingValueObject.setPermissionPassTimeIntervals(permissionPassTimeIntervals);

        } else {
            //如果是删除 不在设置默认值
            if (!deleteFlag) {
                passingValueObject.setPermissionPassTimeIntervals(Collections.singletonList(
                        DemandTimeIntervalValueObject.initFullDayData()));
            }
        }
    }

    @Override
    public String logTag() {
        return "需求配置解析策略-安防-通行需求";
    }
}
