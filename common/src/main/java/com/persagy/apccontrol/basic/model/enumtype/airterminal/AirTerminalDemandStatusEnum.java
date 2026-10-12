package com.persagy.apccontrol.basic.model.enumtype.airterminal;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.EnumCalculateValueType;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 空调末端-需求状态枚举
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@AllArgsConstructor
public enum AirTerminalDemandStatusEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 目标值
     */
    TARGET_VALUE("TARGET_VALUE", "key_ec213f2de1a249ffa0ab3fcb3fde5a53"),

    /**
     * 上限值
     */
    UPPER_LIMIT_VALUE("UPPER_LIMIT_VALUE", "key_21130ae39e5149ae99d36f5ed902bc30"),

    /**
     * 蓄冷值
     */
    COLD_STORAGE_VALUE("COLD_STORAGE_VALUE", "key_46b36f9eeb134d289aed546794fcb80d"),

    /**
     * 下限值
     */
    LOWER_LIMIT_VALUE("LOWER_LIMIT_VALUE", "key_d75f557d7e4146cbbb5fc9ee6a2de575"),

    /**
     * 蓄热值
     */
    HEAT_STORAGE_VALUE("HEAT_STORAGE_VALUE", "key_edaf71231069441689b29cf6493d3e89");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 从价值类型枚举转换到需求状态枚举
     *
     * @param source 数据源
     * @return 需求状态枚举
     * @since 5.240502.0
     */
    public static AirTerminalDemandStatusEnum of(@NonNull EnumCalculateValueType source) {
        switch (source) {
            case TARGET_VALUE:
                return TARGET_VALUE;
            case UPPER_VALUE:
                return UPPER_LIMIT_VALUE;
            case COLD_STORAGE_VALUE:
                return COLD_STORAGE_VALUE;
            case LIMIT_VALUE:
                return LOWER_LIMIT_VALUE;
            case HEAT_STORAGE_VALUE:
                return HEAT_STORAGE_VALUE;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(source.getClass().getSimpleName(), source);
        }
    }

    /**
     * 根据当日的空调季，返回对应应该存在的标签集合
     *
     * @param season
     * @return
     */
    public static List<AirTerminalDemandStatusEnum> of(MidCalendarAcSeasonTypeEnum season, InfoCodeEnum infoCodeEnum) {
        if (season == null) {
            return null;
        }
        switch (season) {
            case SEASON_COOLING:
                return InfoCodeEnum.SPACE_TDB.equals(infoCodeEnum) ? Lists.newArrayList(UPPER_LIMIT_VALUE, TARGET_VALUE, COLD_STORAGE_VALUE)
                        : InfoCodeEnum.SPACE_CO2.equals(infoCodeEnum) ? Lists.newArrayList(UPPER_LIMIT_VALUE, TARGET_VALUE) : null;
            case SEASON_HEATING:
                return InfoCodeEnum.SPACE_TDB.equals(infoCodeEnum) ? Lists.newArrayList(HEAT_STORAGE_VALUE, TARGET_VALUE, LOWER_LIMIT_VALUE)
                        : InfoCodeEnum.SPACE_CO2.equals(infoCodeEnum) ? Lists.newArrayList(UPPER_LIMIT_VALUE, TARGET_VALUE) : null;
            default:
                return null;
        }
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AirTerminalDemandStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, AirTerminalDemandStatusEnum.class);
    }
}
