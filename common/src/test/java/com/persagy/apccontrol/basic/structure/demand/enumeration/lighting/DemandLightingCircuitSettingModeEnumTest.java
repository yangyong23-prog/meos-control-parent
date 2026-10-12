package com.persagy.apccontrol.basic.structure.demand.enumeration.lighting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 需求管理-照明需求-照明回路配置模式枚举测试
 *
 * @author JiYinchuan
 * @since v7.260801.0
 */
public class DemandLightingCircuitSettingModeEnumTest {

    /**
     * 验证新增回路(无已保存配置)在反馈调节功能类型下不继承反馈调节, 置为未配置。
     * 反馈条件已下沉至回路级, 新增回路无回路级条件, 需人为补充需求。
     *
     * @since v7.260801.0
     */
    @Test
    public void shouldReturnNotConfiguredWhenParentIsFeedbackAdjustmentAndCircuitNotConfigured() {
        DemandLightingCircuitSettingModeEnum result = DemandLightingCircuitSettingModeEnum.getSettingMode(
                DemandLightingTypeSettingModeEnum.FEEDBACK_ADJUSTMENT, null);

        Assertions.assertEquals(DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED, result);
    }

    /**
     * 验证新增回路(无已保存配置)按上级功能类型状态继承的既有行为不变。
     *
     * @since v7.260801.0
     */
    @Test
    public void shouldInheritParentSettingModeWhenCircuitNotConfigured() {
        Assertions.assertEquals(DemandLightingCircuitSettingModeEnum.OPEN,
                DemandLightingCircuitSettingModeEnum.getSettingMode(DemandLightingTypeSettingModeEnum.ALL_OPEN, null));
        Assertions.assertEquals(DemandLightingCircuitSettingModeEnum.CLOSE,
                DemandLightingCircuitSettingModeEnum.getSettingMode(DemandLightingTypeSettingModeEnum.ALL_CLOSE, null));
        Assertions.assertEquals(DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED,
                DemandLightingCircuitSettingModeEnum.getSettingMode(DemandLightingTypeSettingModeEnum.PARTIAL_OPEN, null));
        Assertions.assertEquals(DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED,
                DemandLightingCircuitSettingModeEnum.getSettingMode(DemandLightingTypeSettingModeEnum.NOT_CONFIGURED, null));
    }

    /**
     * 验证已保存配置的回路直接返回自身配置状态, 不受上级功能类型影响。
     *
     * @since v7.260801.0
     */
    @Test
    public void shouldReturnCircuitSettingModeWhenCircuitConfigured() {
        Assertions.assertEquals(DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT,
                DemandLightingCircuitSettingModeEnum.getSettingMode(DemandLightingTypeSettingModeEnum.FEEDBACK_ADJUSTMENT,
                        DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT));
        Assertions.assertEquals(DemandLightingCircuitSettingModeEnum.OPEN,
                DemandLightingCircuitSettingModeEnum.getSettingMode(DemandLightingTypeSettingModeEnum.FEEDBACK_ADJUSTMENT,
                        DemandLightingCircuitSettingModeEnum.OPEN));
        Assertions.assertEquals(DemandLightingCircuitSettingModeEnum.CLOSE,
                DemandLightingCircuitSettingModeEnum.getSettingMode(DemandLightingTypeSettingModeEnum.ALL_OPEN,
                        DemandLightingCircuitSettingModeEnum.CLOSE));
    }
}
