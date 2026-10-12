package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * 运行监测二级类型枚举
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Getter
@AllArgsConstructor
public enum RunningMonitorSecondTypeEnum implements BaseEnum<String>, IEnum<String> {

    // ------------------------- 公共 -------------------------

    /**
     * 固定对象
     */
    FIXED_OBJECT("FIXED_OBJECT", "key_34fa3aba1fc54367a57b831a8f83e684"),

    // ------------------------- 冷源 -------------------------

    /**
     * 冷源
     */
    COLD_SOURCE("COLD_SOURCE", "key_235e4049874342d1a209fa13c3711563"),

    // ------------------------- 空调冷热源 -------------------------

    /**
     * 空调冷热源
     */
    COLD_HEAT_SOURCE("COLD_HEAT_SOURCE", "key_0c76a5ab003b4931abb309e20aeefc8c"),

    // ------------------------- 热源 -------------------------

    /**
     * 热源
     */
    HEAT_SOURCE("HEAT_SOURCE", "key_2a7b0b8cf79743fb9409e9fcfa0f45fa"),

    // ------------------------- 照明 -------------------------

    /**
     * 公共
     */
    PUBLIC_AREA_LIGHTING("PUBLIC_AREA_LIGHTING", "key_b617f2089fd4454bb25c38eca23ab04b"),

    /**
     * 停车场照明
     */
    PARKING_LOT_LIGHTING("PARKING_LOT_LIGHTING", "key_95ff2a2cd2ed4f72988119bc765c6151"),

    /**
     * 楼体照明
     */
    BUILDING_LIGHTING("BUILDING_LIGHTING", "key_57ab18ad763346c781904c5fe71156fa"),

    /**
     * 景观照明
     */
    LANDSCAPE_LIGHTING("LANDSCAPE_LIGHTING", "key_303dd7191cc748a3837086829a0d0afd"),

    /**
     * 标识照明
     */
    SIGN_LIGHTING("SIGN_LIGHTING", "key_045e41901fef446194d14c237beaf173"),

    // ------------------------- 空调环境与调节 -------------------------

    /**
     * 空调末端
     */
    AIR_TERMINAL("AIR_TERMINAL", "key_56a25250df214dbbb7c1e62c8aae8801"),

    // ------------------------- 通风需求 -------------------------

    /**
     * 机房后勤通风
     */
    LOGISTICS_VENTILATION("LOGISTICS_VENTILATION", "key_d43eed2591c2444a9934771c5565a75b"),

    /**
     * 停车场通风
     */
    PARKING_VENTILATION("PARKING_VENTILATION", "key_a117391b54454fa8b0ba6c2b59fa316c"),

    /**
     * 卫生间通风
     */
    TOILET_VENTILATION("TOILET_VENTILATION", "key_554263bf6d5f4b41a3d6cb8543c5d962"),

    /**
     * 穹顶通风
     */
    DOME_VENTILATION("DOME_VENTILATION", "key_63d151af642d4ec89abe297d78bb18aa"),
    /**
     * 通风
     */
    VENTILATION("VENTILATION", "key_03a55d56f20348f5ae2578117c489cb6"),

    // ------------------------- 安防 -------------------------

    /**
     * 安防-撤布防
     */
    SECURITY_ARMING("SECURITY_ARMING", "key_6e9ef1662faa441cac3999183bd3decb"),

    /**
     * 安防-通行
     */
    SECURITY_ACCESS("SECURITY_ACCESS", "key_62b970abe23540068c8cb5446e97796b"),

    // ------------------------- 电梯 -------------------------

    /**
     * 电梯
     */
    ELEVATOR("ELEVATOR", "key_7429dede25db40f28973ab3e90e1f28c"),

    // ------------------------- 客流 -------------------------

    /**
     * 客流
     */
    PASSENGER_FLOW("PASSENGER_FLOW", "key_8991792fb896439b99ae1d6642633bef"),

    /**
     * 停车场
     */
    PARKING("PARKING", "key_deb5ed39ffe14c5695e0a8ba1ce6eb39"),

    /**
     * 厨房油烟机
     */
    KITCHEN_RANGE_HOOD("KITCHEN_RANGE_HOOD", "key_69d5825940c14669b2fd0621f24a8f7a"),

    /**
     * 机房安全
     */
    COMPUTER_ROOM_SECURITY("COMPUTER_ROOM_SECURITY", "key_966adee3e1e64b5b99ef8591e9932269"),

    /**
     * 消防
     */
    FIRE_FIGHTING("FIRE_FIGHTING", "key_9b92b4f9c84b449dafeb9254c11d2596"),

    // ------------------------- 运行分析 -------------------------

    /**
     * 营业期间内温度分析
     */
    RUN_ANALYSIS_TDB("RUN_ANALYSIS_TDB", "key_f09951967721483fb03279ffb92736c9"),

    /**
     * 营业期间内CO2分析
     */
    RUN_ANALYSIS_CO2("RUN_ANALYSIS_CO2", "key_7f3f874172ca42b3b59f9cd12aaee22d"),

    /**
     * 末端设备运行分析
     */
    RUN_ANALYSIS_AIR_TERMINAL("RUN_ANALYSIS_AIR_TERMINAL", "key_c59c34e712104c53bf4427725d91c7e1"),

    /**
     * 新风分析
     */
    RUN_ANALYSIS_FRESH_AIR("RUN_ANALYSIS_FRESH_AIR", "key_483d26996d98444686c3cc0995a93be2"),

    /**
     * 营业期间内温度达标
     */
    RUN_QUALIFY_TDB("RUN_QUALIFY_TDB", "key_f09951967721483fb03279ffb92736c8"),

    /**
     * 营业期间内CO2分析
     */
    RUN_QUALIFY_CO2("RUN_QUALIFY_CO2", "key_7f3f874172ca42b3b59f9cd12aaee228"),

    // ------------------------- 污水处理 -------------------------

    /**
     * 污水处理
     */
    SEWAGE("SEWAGE", "key_35cf9f6389744fce8946524f9baee565"),

    // ------------------------- 给排水 -------------------------

    /**
     * 给排水
     */
    WATER_SUPPLY("WATER_SUPPLY", "key_4aa65d96aaa54593a8795abd551b50a7"),

    // ------------------------- 配电 -------------------------

    /**
     * 配电
     */
    POWER_DISTRIBUTION("POWER_DISTRIBUTION", "key_6c1ca7fe3dd14248850c5513092c7971"),

    // ------------------------- 弱电 -------------------------

    /**
     * 楼宇智能化系统
     */
    INTELLIGENT_BUILDING_AUTOMATION("INTELLIGENT_BUILDING_AUTOMATION", "key_ff62f7d8db2b4713aea364f6b2c2ea8c"),

    /**
     * 能源管理系统
     */
    ENERGY_MANAGEMENT("ENERGY_MANAGEMENT", "key_44e88b41cd074e37bfd3ed47c2c3f4d0"),

    /**
     * 环境监测系统
     */
    INDOOR_ENVIRONMENT_MONITORING("INDOOR_ENVIRONMENT_MONITORING", "key_fc2cbe468f29418e946677d9560ec9e1"),

    /**
     * 照明监测系统
     */
    LIGHTING_MONITORING("LIGHTING_MONITORING", "key_40ba5d8135474ec49977d0d3cac9a542"),

    /**
     * 空调冷源
     */
    AIR_CONDITIONING_COLD_SOURCE("AIR_CONDITIONING_COLD_SOURCE", "key_dfbd48a8133047f8a6b51f2b5e34909f"),

    /**
     * 空调热源
     */
    AIR_CONDITIONING_HEAT_SOURCE("AIR_CONDITIONING_HEAT_SOURCE", "key_air_conditioning_heat_source"),

    /**
     * 空调冷热源
     */
    AIR_CONDITIONING_C_H_SOURCE("AIR_CONDITIONING_C_H_SOURCE", "key_air_conditioning_cold_heat_source"),

    /**
     * 卫生间
     */
    TOILET("TOILET", "key_adb4c71ef151472d82d64d276029bc96"),


    /**
     * 光伏
     */
    PHOTOVOLTAIC("PHOTOVOLTAIC", "key_a7b7692f7c3b458bb2c8028911dfc825"),

    /**
     * 空压机
     */
    AIR_COMPRESSOR("AIR_COMPRESSOR", "key_fdbeef56be0e472d93700c4237abffb2"),

    /**
     * 智能体
     */
    AI("AI", "key_0b66c14e86704b2798dd12dacd29d7df"),

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
     * 创建当前实例
     *
     * @param source 数据源
     * @return 当前实例
     * @since 5.241002.0
     */
    @Nullable
    public static RunningMonitorSecondTypeEnum of(@NonNull SubscribeTypeEnum source) {
        switch (source) {
            case ACCC:
                return COLD_SOURCE;
            case ACCI:
                return COLD_HEAT_SOURCE;
            case ACCH:
                return HEAT_SOURCE;
            case ACAI:
                return AIR_CONDITIONING_C_H_SOURCE;
            case AIR_TERMINAL:
                return AIR_TERMINAL;
            case SECURITY_ARMING:
                return SECURITY_ARMING;
            case SECURITY_PASSING:
                return SECURITY_ACCESS;
            case ELEVATOR:
                return ELEVATOR;
            case PASSENGER_FLOW:
                return PASSENGER_FLOW;
            case PARKING:
                return PARKING;
            case KITCHEN_RANGE_HOOD:
                return KITCHEN_RANGE_HOOD;
            case COMPUTER_ROOM_SECURITY:
                return COMPUTER_ROOM_SECURITY;
            case FIRE_FIGHTING:
                return FIRE_FIGHTING;
            case SEWAGE:
                return SEWAGE;
            case WATER_SUPPLY:
                return WATER_SUPPLY;
            case SE:
                return POWER_DISTRIBUTION;
            case AIR_CONDITIONING_COLD_SOURCE:
                return AIR_CONDITIONING_COLD_SOURCE;
            case PHOTOVOLTAIC:
                return PHOTOVOLTAIC;
            case AIR_COMPRESSOR:
                return AIR_COMPRESSOR;
            case TOILET:
                return TOILET;
            case VENTILATION:
                return VENTILATION;
            case AI:
                return AI;
            case ACAH:
                return AIR_CONDITIONING_HEAT_SOURCE;
            default:
                return null;
        }
    }

    /**
     * 转换需求模版类型枚举
     *
     * @return 需求模版类型枚举
     * @since 5.240702.0
     */
    public DemandTemplateTypeEnum toDemandTemplateTypeEnum() {
        switch (this) {
            case COLD_HEAT_SOURCE:
            case AIR_TERMINAL:
                return DemandTemplateTypeEnum.AIRCONDITIONING;
            case PUBLIC_AREA_LIGHTING:
                return DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING;
            case PARKING_LOT_LIGHTING:
                return DemandTemplateTypeEnum.PARKING_LOT_LIGHTING;
            case BUILDING_LIGHTING:
                return DemandTemplateTypeEnum.BUILDING_LIGHTING;
            case LANDSCAPE_LIGHTING:
                return DemandTemplateTypeEnum.LANDSCAPE_LIGHTING;
            case SIGN_LIGHTING:
                return DemandTemplateTypeEnum.SIGN_LIGHTING;
            case PARKING_VENTILATION:
                return DemandTemplateTypeEnum.PARKING_VENTILATION;
            case DOME_VENTILATION:
                return DemandTemplateTypeEnum.DOME_VENTILATION;
            case LOGISTICS_VENTILATION:
                return DemandTemplateTypeEnum.LOGISTICS_VENTILATION;
            case TOILET_VENTILATION:
                return DemandTemplateTypeEnum.TOILET_VENTILATION;
            case SECURITY_ARMING:
                return DemandTemplateTypeEnum.ARMING;
            case SECURITY_ACCESS:
                return DemandTemplateTypeEnum.PASSING;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

    /**
     * 转换为运行监测类型枚举
     *
     * @return 需求类型枚举
     * @since v6.0.0
     */
    public RunningMonitorTypeEnum toRunningMonitorTypeEnum() {
        switch (this) {
            case FIXED_OBJECT:
                return RunningMonitorTypeEnum.COMMON;
            case COLD_SOURCE:
                return RunningMonitorTypeEnum.COLD_SOURCE;
            case COLD_HEAT_SOURCE:
                return RunningMonitorTypeEnum.COLD_HEAT_SOURCE;
            case HEAT_SOURCE:
                return RunningMonitorTypeEnum.HEAT_SOURCE;
            case PUBLIC_AREA_LIGHTING:
            case PARKING_LOT_LIGHTING:
            case BUILDING_LIGHTING:
            case LANDSCAPE_LIGHTING:
            case SIGN_LIGHTING:
                return RunningMonitorTypeEnum.LIGHTING;
            case AIR_TERMINAL:
                return RunningMonitorTypeEnum.AIR_TERMINAL;
            case PARKING_VENTILATION:
            case DOME_VENTILATION:
            case LOGISTICS_VENTILATION:
            case TOILET_VENTILATION:
            case VENTILATION:
                return RunningMonitorTypeEnum.VENTILATION;
            case SECURITY_ACCESS:
            case SECURITY_ARMING:
                return RunningMonitorTypeEnum.SECURITY;
            case ELEVATOR:
                return RunningMonitorTypeEnum.ELEVATOR;
            case PASSENGER_FLOW:
                return RunningMonitorTypeEnum.PASSENGER_FLOW;
            case PARKING:
                return RunningMonitorTypeEnum.PARKING;
            case KITCHEN_RANGE_HOOD:
                return RunningMonitorTypeEnum.KITCHEN_RANGE_HOOD;
            case COMPUTER_ROOM_SECURITY:
                return RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY;
            case FIRE_FIGHTING:
                return RunningMonitorTypeEnum.FIRE_FIGHTING;
            case RUN_ANALYSIS_TDB:
            case RUN_ANALYSIS_CO2:
            case RUN_ANALYSIS_AIR_TERMINAL:
            case RUN_ANALYSIS_FRESH_AIR:
            case RUN_QUALIFY_CO2:
            case RUN_QUALIFY_TDB:
                return RunningMonitorTypeEnum.RUN_ANALYSIS;
            case SEWAGE:
                return RunningMonitorTypeEnum.SEWAGE;
            case WATER_SUPPLY:
                return RunningMonitorTypeEnum.WATER_SUPPLY;
            case POWER_DISTRIBUTION:
                return RunningMonitorTypeEnum.POWER_DISTRIBUTION;
            case INTELLIGENT_BUILDING_AUTOMATION:
            case ENERGY_MANAGEMENT:
            case INDOOR_ENVIRONMENT_MONITORING:
            case LIGHTING_MONITORING:
                return RunningMonitorTypeEnum.WEAK_POWER;
            case AIR_CONDITIONING_COLD_SOURCE:
                return RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE;
            case AIR_CONDITIONING_HEAT_SOURCE:
                return RunningMonitorTypeEnum.AIR_CONDITIONING_HEAT_SOURCE;
            case AIR_CONDITIONING_C_H_SOURCE:
                return RunningMonitorTypeEnum.AIR_CONDITIONING_C_H_SOURCE;
            case AIR_COMPRESSOR:
                return RunningMonitorTypeEnum.AIR_COMPRESSOR;
            case TOILET:
                return RunningMonitorTypeEnum.TOILET;
            case PHOTOVOLTAIC:
                return RunningMonitorTypeEnum.PHOTOVOLTAIC;
            case AI:
                return RunningMonitorTypeEnum.AI;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RunningMonitorSecondTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, RunningMonitorSecondTypeEnum.class);
    }
}
