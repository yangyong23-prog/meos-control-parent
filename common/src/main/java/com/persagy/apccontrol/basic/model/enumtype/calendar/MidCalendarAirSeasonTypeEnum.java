package com.persagy.apccontrol.basic.model.enumtype.calendar;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 工作历日期标签类型枚举--仅是空调季
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@AllArgsConstructor
public enum MidCalendarAirSeasonTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 夏季
     */
    AIR_SUMMER("AirSummer", "key_fa717a2d4456488a9058da5357b94bfc"),
    /**
     * 夏季高峰
     */
    AIR_SUMMER_PEAK("AirSummerPeak", "key_f3090e940dc44f598ac1b2bee2a25b66"),
    /**
     * 冬季
     */
    AIR_WINTER("AirWinter", "key_6778c5ec0c8845ca93f5c62d0c66cb21"),
    /**
     * 过渡季（春）
     */
    AIR_SEASON_SPRING("AirSeasonSpring", "key_70e0f303bd374d56a8b05a82fc8e6fcc"),
    /**
     * 过渡季（秋）
     */
    AIR_SEASON_AUTUMN("AirSeasonAutumn", "key_22bc81be27c043c2adcd6c2159614111"),

    ;
    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

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

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static MidCalendarAirSeasonTypeEnum analyze(String value) {
        if (StrUtil.isNotBlank(value)) {
            try {
                return BaseEnum.analyze(value, MidCalendarAirSeasonTypeEnum.class);
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static MidCalendarAirSeasonTypeEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (MidCalendarAirSeasonTypeEnum i : MidCalendarAirSeasonTypeEnum.values()) {
            if (code.equals(i.getValue())) {
                return i;
            }
        }
        return null;
    }
}
