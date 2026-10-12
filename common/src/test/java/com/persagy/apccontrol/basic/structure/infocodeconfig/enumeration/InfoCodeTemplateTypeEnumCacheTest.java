package com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 信息点模板类型固定缓存许可规则测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class InfoCodeTemplateTypeEnumCacheTest {

    /**
     * 验证按一级运行监测类型和模板类型判断缓存许可时保持既有固定矩阵。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldKeepFixedCachePermissionMatrix() {
        Assertions.assertTrue(InfoCodeTemplateTypeEnum.allowCache(
                RunningMonitorTypeEnum.AIR_TERMINAL, InfoCodeTemplateTypeEnum.GRID));
        Assertions.assertTrue(InfoCodeTemplateTypeEnum.allowCache(
                RunningMonitorTypeEnum.LIGHTING, InfoCodeTemplateTypeEnum.DETAIL));
        Assertions.assertTrue(InfoCodeTemplateTypeEnum.allowCache(
                RunningMonitorTypeEnum.VENTILATION, InfoCodeTemplateTypeEnum.TABLE_LIST));
        Assertions.assertTrue(InfoCodeTemplateTypeEnum.allowCache(
                RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY,
                InfoCodeTemplateTypeEnum.GRID));
        Assertions.assertTrue(InfoCodeTemplateTypeEnum.allowCache(
                RunningMonitorTypeEnum.SECURITY,
                InfoCodeTemplateTypeEnum.TABLE_LIST));

        Assertions.assertFalse(InfoCodeTemplateTypeEnum.allowCache(
                RunningMonitorTypeEnum.AIR_TERMINAL, InfoCodeTemplateTypeEnum.DETAIL));
        Assertions.assertFalse(InfoCodeTemplateTypeEnum.allowCache(
                RunningMonitorTypeEnum.LIGHTING, InfoCodeTemplateTypeEnum.GRID));
        Assertions.assertFalse(InfoCodeTemplateTypeEnum.allowCache(
                null, InfoCodeTemplateTypeEnum.TABLE_LIST));
        Assertions.assertFalse(InfoCodeTemplateTypeEnum.allowCache(
                RunningMonitorTypeEnum.AIR_TERMINAL, null));
    }
}
