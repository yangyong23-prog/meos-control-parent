package com.persagy.apccontrol.basic.model.enumtype.strategy;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 策略业务类型枚举
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Getter
@AllArgsConstructor
public enum StrategyBusinessTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 冷机
     */
    CHILLER_UNIT("CHILLER_UNIT", "key_0e9f4a6f1ce743a7ab11c03f2f115bed", "strategyChillerUnitExecutor"),
    /**
     * 冷热源
     */
    HOT_COLD_STATION("HOT_COLD_STATION", "key_adc230c018a34f41b5d9616329ae9b4e", "strategyHotColdStationUnitExecutor"),
    /**
     * 热源
     */
    HOT_STATION("HOT_STATION", "key_2a7b0b8cf79743fb9409e9fcfa0f45fa", "strategyHotStationUnitExecutor"),

    /**
     * 末级冷冻泵组
     */
    LAST_FREEZE_PUMP_GROUP("LAST_FREEZE_PUMP_GROUP", "key_0b94170a5e5f452ea85ac691da9fde3a", "strategyLastFreezePumpGroupExecutor");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 策略执行器BEAN名称
     */
    private final String beanNameOfExecutor;

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
    public static StrategyBusinessTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, StrategyBusinessTypeEnum.class);
    }
}
