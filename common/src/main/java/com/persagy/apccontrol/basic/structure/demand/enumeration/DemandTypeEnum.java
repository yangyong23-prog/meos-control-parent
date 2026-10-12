package com.persagy.apccontrol.basic.structure.demand.enumeration;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.airconditioning.DemandAirconditioningStrategyImpl;
import com.persagy.apccontrol.basic.structure.demand.strategy.kitchen.DemandKitchenRangeHoodStrategyImpl;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategyImpl;
import com.persagy.apccontrol.basic.structure.demand.strategy.security.DemandSecurityStrategyImpl;
import com.persagy.apccontrol.basic.structure.demand.strategy.ventilation.DemandVentilationStrategyImpl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 需求类型
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Getter
@AllArgsConstructor
public enum DemandTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 空调需求
     */
    AIRCONDITIONING("AIRCONDITIONING", "key_a335f7fe48684206afd3250c9a84c823", "requirementConfigurationManagement", DemandAirconditioningStrategyImpl.BEAN_NAME, 1),

    /**
     * 照明需求
     */
    LIGHTING("LIGHTING", "key_87eed129a5dc482ebb12011a8d864d44", "lightingDemandManagement", DemandLightingStrategyImpl.BEAN_NAME, 2),

    /**
     * 安防需求
     */
    SECURITY("SECURITY", "key_1ea46795637d4f17aa4d985a1e7a4f84", "securityDemand", DemandSecurityStrategyImpl.BEAN_NAME, 3),

    /**
     * 通风需求
     */
    VENTILATION("VENTILATION", "key_32a26cd54ef345728d6d4f84967f163b", "ventilationDemand", DemandVentilationStrategyImpl.BEAN_NAME, 4),

    /**
     * 厨房排油烟需求
     *
     * @since v7.261001.0
     */
    KITCHEN_RANGE_HOOD("KITCHEN_RANGE_HOOD", "key_69d5825940c14669b2fd0621f24a8f7a", "kitchenRangeHoodDemand", DemandKitchenRangeHoodStrategyImpl.BEAN_NAME, 5);

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 产品功能编码
     */
    private final String productCode;

    /**
     * 策略Bean名称
     */
    private final String beanName;

    /**
     * 排序
     */
    private final int ordered;

    /**
     * 转换为模版类型枚举
     *
     * @return 需求类型枚举
     * @since 24Q1 T3S1-5.17.0
     */
    public List<DemandTemplateTypeEnum> toDemandTemplateTypes() {
        switch (this) {
            case AIRCONDITIONING:
                return Lists.newArrayList(DemandTemplateTypeEnum.AIRCONDITIONING);
            case VENTILATION:
                return Lists.newArrayList(
                        DemandTemplateTypeEnum.LOGISTICS_VENTILATION,
                        DemandTemplateTypeEnum.PARKING_VENTILATION,
                        DemandTemplateTypeEnum.TOILET_VENTILATION,
                        DemandTemplateTypeEnum.DOME_VENTILATION
                );
            case KITCHEN_RANGE_HOOD:
                return Lists.newArrayList(DemandTemplateTypeEnum.KITCHEN_RANGE_HOOD);
            case SECURITY:
                return Lists.newArrayList(
                        DemandTemplateTypeEnum.ARMING,
                        DemandTemplateTypeEnum.PASSING
                );
            case LIGHTING:
                return Lists.newArrayList(
                        DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING,
                        DemandTemplateTypeEnum.PARKING_LOT_LIGHTING,
                        DemandTemplateTypeEnum.BUILDING_LIGHTING,
                        DemandTemplateTypeEnum.LANDSCAPE_LIGHTING,
                        DemandTemplateTypeEnum.SIGN_LIGHTING);
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

    /**
     * 转换为需求类型枚举
     *
     * @return 需求类型枚举
     * @since 5.241001.0
     */
    public RunningMonitorTypeEnum toRunningMonitorTypeEnum() {
        switch (this) {
            case LIGHTING:
                return RunningMonitorTypeEnum.LIGHTING;
            case VENTILATION:
                return RunningMonitorTypeEnum.AIR_TERMINAL;
            case SECURITY:
                return RunningMonitorTypeEnum.SECURITY;
            case KITCHEN_RANGE_HOOD:
                return RunningMonitorTypeEnum.KITCHEN_RANGE_HOOD;
            case AIRCONDITIONING:
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

    /**
     * 获取需求策略
     *
     * @return 需求策略
     * @since 5.240902.0
     */
    public DemandStrategy demandStrategy() {
        return SpringUtil.getBean(getBeanName());
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DemandTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandTypeEnum.class);
    }
}
