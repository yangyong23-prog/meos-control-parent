package com.persagy.apccontrol.basic.structure.demand.enumeration.lighting;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求管理-照明需求-照明回路配置模式枚举
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Getter
@AllArgsConstructor
public enum DemandLightingCircuitSettingModeEnum implements BaseEnum<String>, IEnum<String> {


    /**
     * OPEN
     */
    OPEN("OPEN", "key_5f6a4957123248279ddf22f6cfa74063", 1),
    
    /**
     * 反馈调节
     */
    FEEDBACK_ADJUSTMENT("FEEDBACK_ADJUSTMENT", "key_a1b3ebc3093645f4933ba4b20f14b9de", 2),
    
    /**
     * 全关
     */
    CLOSE("CLOSE", "key_b1620633958840638440dd1b3e10024e", 3),
    
    /**
     * 未配置
     */
    NOT_CONFIGURED("NOT_CONFIGURED", "key_a09d40c0ec674d9583b1c6b8c7f22ee1", 4),
    
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
     * 优先级
     */
    private final int priority;

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
    public static DemandLightingCircuitSettingModeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandLightingCircuitSettingModeEnum.class);
    }


    /**
     * 获取回路配置状态
     *
     * @param parentSettingMode     上级配置状态
     * @param circuitSettingMode    当前回路配置
     * @return {@link DemandLightingCircuitSettingModeEnum}
     * @author yuecaipu
     * @date 2024/4/8 19:23
     * @since 24Q1 T3S2-5.18.0
     */
    public static DemandLightingCircuitSettingModeEnum getSettingMode(DemandLightingTypeSettingModeEnum parentSettingMode,
                                                                      DemandLightingCircuitSettingModeEnum circuitSettingMode) {
        // 当前配置存在，直接返回当前配置状态
        if (null != circuitSettingMode) {
            return circuitSettingMode;
        }
        // 上级部分开启或无配置，当前无配置
        if (DemandLightingTypeSettingModeEnum.PARTIAL_OPEN.equals(parentSettingMode)
                || DemandLightingTypeSettingModeEnum.NOT_CONFIGURED.equals(parentSettingMode)) {
            return DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED;
        }

        // 上级全关 回路关
        if (DemandLightingTypeSettingModeEnum.ALL_CLOSE.equals(parentSettingMode)) {
            return DemandLightingCircuitSettingModeEnum.CLOSE;
        }

        // 上级全开，回路开
        if (DemandLightingTypeSettingModeEnum.ALL_OPEN.equals(parentSettingMode)) {
            return DemandLightingCircuitSettingModeEnum.OPEN;
        }

        // 反馈调节不继承: 反馈条件已下沉至回路级, 新增回路无回路级条件, 置为未配置, 需人为补充需求
        if (DemandLightingTypeSettingModeEnum.FEEDBACK_ADJUSTMENT.equals(parentSettingMode)) {
            return DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED;
        }

        return DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED;
    }
    
    /**
     * 比较两个设置模式的优先级
     *
     * @param currentMode 当前模式
     * @param newMode     新模式
     * @return true-应该更新，false-不应该更新
     */
    public static boolean shouldUpdate(DemandLightingCircuitSettingModeEnum currentMode,
                                       DemandLightingCircuitSettingModeEnum newMode) {
        
        if (currentMode == null || newMode == null) {
            return true;
        }
        
        // 优先级数字越小，优先级越高
        return newMode.getPriority() < currentMode.getPriority();
    }
}
