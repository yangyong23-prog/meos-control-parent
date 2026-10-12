package com.persagy.apccontrol.basic.model.enumtype;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.StaticRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.*;
import com.persagy.apccontrol.basic.structure.intervention.strategy.InterventionConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.intervention.strategy.impl.InterventionLightingConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * 运行监测类型枚举
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Getter
@AllArgsConstructor
public enum RunningMonitorTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 公共, 虚拟类型
     */
    COMMON("COMMON", "key_41d2574eb77e420383bd949df36782ba", null),

    /**
     * 冷热源
     */
    COLD_HEAT_SOURCE("COLD_HEAT_SOURCE", "key_9e432c33a6f3404cbc4295b2bdead343", RunningMonitorColdHeatSourceStrategy.BEAN_NAME),

    /**
     * 冷源
     */
    COLD_SOURCE("COLD_SOURCE", "key_235e4049874342d1a209fa13c3711563", RunningMonitorColdSourceStrategy.BEAN_NAME),

    /**
     * 热源
     */
    HEAT_SOURCE("HEAT_SOURCE", "key_2a7b0b8cf79743fb9409e9fcfa0f45fa", RunningMonitorHeatSourceStrategy.BEAN_NAME),

    /**
     * 照明
     */
    LIGHTING("LIGHTING", "key_9b518dfa099e4fb09fa8ecda9c79cc70", RunningMonitorLightingStrategy.BEAN_NAME),

    /**
     * 空调末端
     */
    AIR_TERMINAL("AIR_TERMINAL", "key_56a25250df214dbbb7c1e62c8aae8801", RunningMonitorAirTerminalStrategy.BEAN_NAME),

    /**
     * 通风
     */
    VENTILATION("VENTILATION", "key_03a55d56f20348f5ae2578117c489cb6", RunningMonitorVentilationStrategy.BEAN_NAME),

    /**
     * 安防
     */
    SECURITY("SECURITY", "key_2b4bbb66befd48ae8b0c6df69ac7be89", RunningMonitorSecurityStrategy.BEAN_NAME),

    /**
     * 机器人
     */
    ROBOT("Robot", "key_030e7de44c9e470b826f01c0b2a5ffaf", null),

    /**
     * 电梯
     */
    ELEVATOR("ELEVATOR", "key_7429dede25db40f28973ab3e90e1f28c", RunningMonitorElevatorStrategy.BEAN_NAME),

    /**
     * 客流
     */
    PASSENGER_FLOW("PASSENGER_FLOW", "key_8991792fb896439b99ae1d6642633bef", RunningMonitorPassengerFlowStrategy.BEAN_NAME),

    /**
     * 停车场
     */
    PARKING("PARKING", "key_deb5ed39ffe14c5695e0a8ba1ce6eb39", RunningMonitorParkingStrategy.BEAN_NAME),

    /**
     * 厨房油烟机
     */
    KITCHEN_RANGE_HOOD("KITCHEN_RANGE_HOOD", "key_69d5825940c14669b2fd0621f24a8f7a", RunningMonitorKitChenRangeHoodStrategy.BEAN_NAME),

    /**
     * 机房安全
     */
    COMPUTER_ROOM_SECURITY("COMPUTER_ROOM_SECURITY", "key_966adee3e1e64b5b99ef8591e9932269", RunningMonitorComputerRoomSecurityStrategy.BEAN_NAME),

    /**
     * 消防
     */
    FIRE_FIGHTING("FIRE_FIGHTING", "key_9b92b4f9c84b449dafeb9254c11d2596", RunningMonitorFireFightingStrategy.BEAN_NAME),
    /**
     * 消防电
     */
    FIRE_FIGHTING_ELECTRICITY("FIRE_FIGHTING_ELECTRICITY", "key_bb73faf2d8bf4cf9a6558ba2f2cb1737",
            RunningMonitorFireFightingElectricityStrategy.BEAN_NAME),

    /**
     * 运行分析
     */
    RUN_ANALYSIS("RUN_ANALYSIS", "key_577bc546155c46e9a81fb12f1aa448a8", RunningMonitorRunAnalysisStrategy.BEAN_NAME),

    /**
     * 污水处理
     */
    SEWAGE("SEWAGE", "key_35cf9f6389744fce8946524f9baee565", RunningMonitorSewageStrategy.BEAN_NAME),

    /**
     * 给排水
     */
    WATER_SUPPLY("WATER_SUPPLY", "key_4aa65d96aaa54593a8795abd551b50a7", RunningMonitorWaterSupplyStrategy.BEAN_NAME),

    /**
     * 配电
     */
    POWER_DISTRIBUTION("POWER_DISTRIBUTION", "key_6c1ca7fe3dd14248850c5513092c7971", null),

    /**
     * 弱电
     */
    WEAK_POWER("WEAK_POWER", "key_53425b0f3d3944f98b24e415e80b71b6", RunningMonitorWeakPowerStrategy.BEAN_NAME),

    /**
     * 空调冷源
     */
    AIR_CONDITIONING_COLD_SOURCE("AIR_CONDITIONING_COLD_SOURCE", "key_dfbd48a8133047f8a6b51f2b5e34909f", RunningMonitorAirConditioningColdSourceStrategy.BEAN_NAME),

    /**
     * 空调冷源调试工具
     */
    AIR_CONDITIONING_COLD_SOURCE_DEBUG_TOOL("A_C_C_S_DEBUG_TOOL", "key_air_conditioning_cold_source_debug_tool", null),

    /**
     * 空调热源
     */
    AIR_CONDITIONING_HEAT_SOURCE("AIR_CONDITIONING_HEAT_SOURCE", "key_air_conditioning_heat_source", RunningMonitorAirConditioningHeatSourceStrategy.BEAN_NAME),

    /**
     * 空调冷热源
     */
    AIR_CONDITIONING_C_H_SOURCE("AIR_CONDITIONING_C_H_SOURCE", "key_air_conditioning_cold_heat_source", RunningMonitorAirConditioningColdHeatSourceStrategy.BEAN_NAME),

    /**
     * 空压机
     */
    AIR_COMPRESSOR("AIR_COMPRESSOR", "key_fdbeef56be0e472d93700c4237abffb2", RunningMonitorAirCompressorStrategy.BEAN_NAME),

    /**
     * 卫生间
     */
    TOILET("TOILET", "key_adb4c71ef151472d82d64d276029bc96", RunningMonitorToiletStrategy.BEAN_NAME),
    /**
     * 光伏
     */
    PHOTOVOLTAIC("PHOTOVOLTAIC", "key_a7b7692f7c3b458bb2c8028911dfc825", RunningMonitorPhotovoltaicStrategy.BEAN_NAME),

    /**
     * 智能体
     */
    AI("AI", "key_0b66c14e86704b2798dd12dacd29d7df", RunningMonitorAIStrategy.BEAN_NAME),

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
     * 策略Bean名称
     */
    private final String strategyBeanName;

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    /**
     * 创建当前实例
     *
     * @param source 数据源
     * @return 当前实例
     * @since 5.240602.0
     */
    @Nullable
    public static RunningMonitorTypeEnum of(@NonNull SubscribeTypeEnum source) {
        switch (source) {
            case LIGHT_MONITORING:
                return LIGHTING;
            case AIR_TERMINAL:
                return AIR_TERMINAL;
            case VENTILATION:
                return VENTILATION;
            case SECURITY:
            case SECURITY_ARMING:
            case SECURITY_PASSING:
                return SECURITY;
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
            case ACCC:
                return COLD_SOURCE;
            case ACCI:
                return COLD_HEAT_SOURCE;
            case ACCH:
                return HEAT_SOURCE;
            case ACAI:
                return AIR_CONDITIONING_C_H_SOURCE;
            case WEAK_POWER:
                return WEAK_POWER;
            case AIR_CONDITIONING_COLD_SOURCE:
                return AIR_CONDITIONING_COLD_SOURCE;
            case FIRE_FIGHTING_ELECTRICITY:
                return FIRE_FIGHTING_ELECTRICITY;
            case AIR_COMPRESSOR:
                return AIR_COMPRESSOR;
            case TOILET:
                return TOILET;
            case PHOTOVOLTAIC:
                return PHOTOVOLTAIC;
            case AI:
                return AI;
            case ACAH:
                return AIR_CONDITIONING_HEAT_SOURCE;
            default:
                return null;
        }
    }

    /**
     * 获取调试工具关联的运行监测类型
     *
     * @return 调试工具关联的运行监测类型
     * @since v7.260602.0
     */
    @Nullable
    public RunningMonitorTypeEnum toDebugToolRunningMonitorType() {
        switch (this) {
            case AIR_CONDITIONING_COLD_SOURCE:
                return AIR_CONDITIONING_COLD_SOURCE_DEBUG_TOOL;
            default:
                return null;
        }
    }

    /**
     * 获取干预配置解析策略
     *
     * @return 干预配置解析策略
     * @since 5.240702.0
     */
    @NonNull
    public InterventionConfigAnalyzeStrategy getInterventionConfigAnalyzeStrategy() {
        Map<String, InterventionConfigAnalyzeStrategy> strategyMap =
                SpringUtil.getBeansOfType(InterventionConfigAnalyzeStrategy.class);
        if (this == RunningMonitorTypeEnum.LIGHTING) {
            return strategyMap.get(InterventionLightingConfigAnalyzeStrategy.BEAN_NAME);
        }
        throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
    }

    /**
     * 获取运行监测策略
     *
     * @return 信息点解析策略
     * @since 5.240602.0
     */
    public RunningMonitorStrategy getRunningMonitorStrategy() {
        return StringUtils.isNotBlank(strategyBeanName)
                ? SpringUtil.getBean(strategyBeanName)
                : new StaticRunningMonitorStrategy(this);
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RunningMonitorTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, RunningMonitorTypeEnum.class);
    }
}
