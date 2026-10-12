package com.persagy.apccontrol.basic.outside.enums;

import com.persagy.apccontrol.basic.utils.i18n.LocaleContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * 能源模型标签枚举-第三方标签名称解析测试
 *
 * @author JiYinchuan
 * @since v7.260901.0
 */
public class EnergyModelTagEnumTagNameTest {

    /**
     * 验证 name 解析为能源调研服务 energy_model_item_tag 字典名称（中文），
     * tagNames 请求参数据此按 tagName 匹配能耗模型。
     *
     * @since v7.260901.0
     */
    @Test
    public void shouldResolveZhTagNameFromDict() {
        Assertions.assertEquals("供冷系统用电量",
                LocaleContext.withLocale(Locale.SIMPLIFIED_CHINESE, EnergyModelTagEnum.COOL::getName));
        Assertions.assertEquals("热站",
                LocaleContext.withLocale(Locale.SIMPLIFIED_CHINESE, EnergyModelTagEnum.HEAT::getName));
        Assertions.assertEquals("冷热源",
                LocaleContext.withLocale(Locale.SIMPLIFIED_CHINESE, EnergyModelTagEnum.COOL_HEAT::getName));
        Assertions.assertEquals("空调末端",
                LocaleContext.withLocale(Locale.SIMPLIFIED_CHINESE, EnergyModelTagEnum.AIR_TERMINAL::getName));
    }

    /**
     * 验证英文请求语言下解析为字典英文名称（独立 key，不与运行日志分类等共享 key 冲突）。
     *
     * @since v7.260901.0
     */
    @Test
    public void shouldResolveEnTagNameFromDict() {
        Assertions.assertEquals("Cooling System Electricity Consumption",
                LocaleContext.withLocale(Locale.ENGLISH, EnergyModelTagEnum.COOL::getName));
        Assertions.assertEquals("Heat Station",
                LocaleContext.withLocale(Locale.ENGLISH, EnergyModelTagEnum.HEAT::getName));
        Assertions.assertEquals("Cooling and Heating Source",
                LocaleContext.withLocale(Locale.ENGLISH, EnergyModelTagEnum.COOL_HEAT::getName));
        Assertions.assertEquals("Air Conditioning Terminal",
                LocaleContext.withLocale(Locale.ENGLISH, EnergyModelTagEnum.AIR_TERMINAL::getName));
    }
}
