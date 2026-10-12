package com.persagy.apccontrol.basic.model.enumtype.calendar;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 工作历空调工况类型枚举
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@AllArgsConstructor
public enum MidCalendarAcSeasonTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 供冷季
     */
    SEASON_COOLING("ACSeason-1", "key_bc52fd0bd5b549e2ae716d44f8ec6b60", "1"),

    /**
     * 供暖季
     */
    SEASON_HEATING("ACSeason-2", "key_c58783d3d304414881abd3d00b887940", "2"),

    /**
     * 过渡季
     */
    SEASON_TRANSITION("ACSeason-3", "key_59c78454aef74f4893feecc6174f3e4c","0");


    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * bdtp信息点值
     */
    private final String infoValue;

    /**
     * 转换工作历日期标签值
     *
     * @return 工作历日期标签值
     * @since v5.7.0
     */
    public MidCalendarDateTagValueEnum toDateTagValue() {
        switch (this) {
            case SEASON_COOLING:
                return MidCalendarDateTagValueEnum.SEASON_COOLING;
            case SEASON_HEATING:
                return MidCalendarDateTagValueEnum.SEASON_HEATING;
            case SEASON_TRANSITION:
                return MidCalendarDateTagValueEnum.SEASON_TRANSITION;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static MidCalendarAcSeasonTypeEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (MidCalendarAcSeasonTypeEnum i : MidCalendarAcSeasonTypeEnum.values()) {
            if (code.equals(i.getValue())) {
                return i;
            }
        }
        return null;
    }

    public String getInfoValue() {
        return infoValue;
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

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static MidCalendarAcSeasonTypeEnum analyze(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return BaseEnum.analyze(value, MidCalendarAcSeasonTypeEnum.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
