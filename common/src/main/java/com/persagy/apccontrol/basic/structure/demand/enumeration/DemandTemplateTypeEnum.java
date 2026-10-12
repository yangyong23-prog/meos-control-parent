package com.persagy.apccontrol.basic.structure.demand.enumeration;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.DemandConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.airconditioning.DemandConfigAirConditioningAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.kitchen.DemandConfigKitchenRangeHoodAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.lighting.*;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.security.DemandConfigSecurityArmingAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.security.DemandConfigSecurityPassingAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.ventilation.DemandConfigVentilationDomeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.ventilation.DemandConfigVentilationLogisticsAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.ventilation.DemandConfigVentilationParkingAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.ventilation.DemandConfigVentilationToiletAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 需求类模版型
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Getter
@AllArgsConstructor
public enum DemandTemplateTypeEnum implements BaseEnum<String>, IEnum<String> {

    // ------------------------- 空调需求 -------------------------

    /**
     * 空调
     */
    AIRCONDITIONING("AIRCONDITIONING", "key_950adc76cbe64f919f2c927cb779625d", DemandConfigAirConditioningAnalyzeStrategy.BEAN_NAME),

    // ------------------------- 照明需求 -------------------------

    /**
     * 公共
     */
    PUBLIC_AREA_LIGHTING("PUBLIC_AREA_LIGHTING", "key_b617f2089fd4454bb25c38eca23ab04b", DemandConfigLightingPublicAnalyzeStrategy.BEAN_NAME),

    /**
     * 停车场照明
     */
    PARKING_LOT_LIGHTING("PARKING_LOT_LIGHTING", "key_95ff2a2cd2ed4f72988119bc765c6151", DemandConfigLightingParkingAnalyzeStrategy.BEAN_NAME),

    /**
     * 楼体照明
     */
    BUILDING_LIGHTING("BUILDING_LIGHTING", "key_57ab18ad763346c781904c5fe71156fa", DemandConfigLightingBuildingAnalyzeStrategy.BEAN_NAME),

    /**
     * 景观照明
     */
    LANDSCAPE_LIGHTING("LANDSCAPE_LIGHTING", "key_303dd7191cc748a3837086829a0d0afd", DemandConfigLightingLandscapeAnalyzeStrategy.BEAN_NAME),

    /**
     * 标识照明
     */
    SIGN_LIGHTING("SIGN_LIGHTING", "key_045e41901fef446194d14c237beaf173", DemandConfigLightingSignAnalyzeStrategy.BEAN_NAME),


    // ------------------------- 安防需求 -------------------------

    /**
     * 撤布防
     */
    ARMING("ARMING", "key_75b48c18ccbc4dfb8840476bdbef181a", DemandConfigSecurityArmingAnalyzeStrategy.BEAN_NAME),

    /**
     * 通行
     */
    PASSING("PASSING", "key_58c903d9a38442e0914295961bb67924", DemandConfigSecurityPassingAnalyzeStrategy.BEAN_NAME),

    // ------------------------- 通风需求 -------------------------

    /**
     * 机房后勤通风
     */
    LOGISTICS_VENTILATION("LOGISTICS_VENTILATION", "key_d43eed2591c2444a9934771c5565a75b", DemandConfigVentilationLogisticsAnalyzeStrategy.BEAN_NAME),
    /**
     * 停车场通风
     */
    PARKING_VENTILATION("PARKING_VENTILATION", "key_a117391b54454fa8b0ba6c2b59fa316c", DemandConfigVentilationParkingAnalyzeStrategy.BEAN_NAME),

    /**
     * 卫生间通风
     */
    TOILET_VENTILATION("TOILET_VENTILATION", "key_554263bf6d5f4b41a3d6cb8543c5d962", DemandConfigVentilationToiletAnalyzeStrategy.BEAN_NAME),

    /**
     * 穹顶通风
     */
    DOME_VENTILATION("DOME_VENTILATION", "key_63d151af642d4ec89abe297d78bb18aa", DemandConfigVentilationDomeAnalyzeStrategy.BEAN_NAME),

    // ------------------------- 厨房排油烟需求 -------------------------

    /**
     * 厨房油烟机
     *
     * @since v7.261001.0
     */
    KITCHEN_RANGE_HOOD("KITCHEN_RANGE_HOOD", "key_69d5825940c14669b2fd0621f24a8f7a", DemandConfigKitchenRangeHoodAnalyzeStrategy.BEAN_NAME),

    ;

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 配置策略Bean名称
     */
    private final String configStrategyBeanName;

    /**
     * 转换为需求类型枚举
     *
     * @return 需求类型枚举
     * @since v6.0.0
     */
    public DemandTypeEnum toDemandTypeEnum() {
        switch (this) {
            case AIRCONDITIONING:
                return DemandTypeEnum.AIRCONDITIONING;
            case PUBLIC_AREA_LIGHTING:
            case PARKING_LOT_LIGHTING:
            case BUILDING_LIGHTING:
            case LANDSCAPE_LIGHTING:
            case SIGN_LIGHTING:
                return DemandTypeEnum.LIGHTING;
            case ARMING:
            case PASSING:
                return DemandTypeEnum.SECURITY;
            case PARKING_VENTILATION:
            case DOME_VENTILATION:
            case TOILET_VENTILATION:
            case LOGISTICS_VENTILATION:
                return DemandTypeEnum.VENTILATION;
            case KITCHEN_RANGE_HOOD:
                return DemandTypeEnum.KITCHEN_RANGE_HOOD;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

    /**
     * 转换运行监测二级类型枚举
     *
     * @return 运行监测二级类型枚举
     * @since 5.240702.0
     */
    public RunningMonitorSecondTypeEnum toRunningMonitorSecondTypeEnum() {
        switch (this) {
            case PUBLIC_AREA_LIGHTING:
                return RunningMonitorSecondTypeEnum.PUBLIC_AREA_LIGHTING;
            case PARKING_LOT_LIGHTING:
                return RunningMonitorSecondTypeEnum.PARKING_LOT_LIGHTING;
            case BUILDING_LIGHTING:
                return RunningMonitorSecondTypeEnum.BUILDING_LIGHTING;
            case LANDSCAPE_LIGHTING:
                return RunningMonitorSecondTypeEnum.LANDSCAPE_LIGHTING;
            case SIGN_LIGHTING:
                return RunningMonitorSecondTypeEnum.SIGN_LIGHTING;
            case ARMING:
                return RunningMonitorSecondTypeEnum.SECURITY_ARMING;
            case PASSING:
                return RunningMonitorSecondTypeEnum.SECURITY_ACCESS;
            case PARKING_VENTILATION:
                return RunningMonitorSecondTypeEnum.PARKING_VENTILATION;
            case DOME_VENTILATION:
                return RunningMonitorSecondTypeEnum.DOME_VENTILATION;
            case TOILET_VENTILATION:
                return RunningMonitorSecondTypeEnum.TOILET_VENTILATION;
            case LOGISTICS_VENTILATION:
                return RunningMonitorSecondTypeEnum.LOGISTICS_VENTILATION;
            case KITCHEN_RANGE_HOOD:
                return RunningMonitorSecondTypeEnum.KITCHEN_RANGE_HOOD;
            case AIRCONDITIONING:
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

    /**
     * 是否为夜景照明
     *
     * @return 是否为夜景照明
     * @since 5.240501.0
     */
    public boolean isNightLighting() {
        switch (this) {
            case BUILDING_LIGHTING:
            case LANDSCAPE_LIGHTING:
            case SIGN_LIGHTING:
                return true;
            default:
                return false;
        }
    }

    /**
     * 获取需求配置解析策略
     *
     * @return 需求配置解析策略
     * @since 5.240902.0
     */
    public <T extends BaseDemandConfig> DemandConfigAnalyzeStrategy<T> demandConfigAnalyzeStrategy() {
        return SpringUtil.getBean(getConfigStrategyBeanName());
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
    public static DemandTemplateTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandTemplateTypeEnum.class);
    }

    /**
     * 获取照明的配置模版
     *
     * @return
     */
    public static List<DemandTemplateTypeEnum> getLightingDemandTemplateType() {
        List<DemandTemplateTypeEnum> type = new ArrayList<>();
        type.add(PUBLIC_AREA_LIGHTING);
        type.add(PARKING_LOT_LIGHTING);
        type.add(BUILDING_LIGHTING);
        type.add(LANDSCAPE_LIGHTING);
        type.add(SIGN_LIGHTING);
        return type;
    }


    public static DemandTemplateTypeEnum getByCode(String code) {
        for (DemandTemplateTypeEnum value : DemandTemplateTypeEnum.values()) {
            if (value.getValue().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
