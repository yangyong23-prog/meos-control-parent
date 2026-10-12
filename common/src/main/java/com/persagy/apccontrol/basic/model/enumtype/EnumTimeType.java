package com.persagy.apccontrol.basic.model.enumtype;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.validate.utility.enumeration.base.BaseEnum;

import java.util.Objects;

/**
 * Description:
 * Company: Persagy
 *
 * @author shaohongbo
 * @version 1.0
 * @since: 2019年8月18日: 下午12:52:15
 * Update By shaohongbo 2019年8月18日: 下午12:52:15
 */
public enum EnumTimeType implements BaseEnum<Integer> {
    /**
     * hbanse  分表 时间类型
     */
    MINUTE5(0, "5min"),
    MINUTE15(0, "15min"),
    HOUR(1, "1h"),
    DAY(2, "1d"),
    MONTH(4, "1m"),
    YEAR(5, "1y");

    private final int code;
    private final String name;

    EnumTimeType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return name;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EnumTimeType resolve(Integer value) {
        return BaseEnum.analyze(value, EnumTimeType.class);
    }

    /**
     * Description: 根据code码，获取CompareType枚举值
     *
     * @param code
     * @return CompareType
     * @author JiShaoJun
     * @since 2018年8月30日: 下午3:19:17 Update By JiShaoJun 2018年8月30日: 下午3:19:17
     */
    public static EnumTimeType getCompareType(int code) {
        for (EnumTimeType item : EnumTimeType.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }

        return null;
    }

    public static EnumTimeType getCompareType(String name) {
        for (EnumTimeType item : EnumTimeType.values()) {
            if (Objects.equals(item.getName(), name)) {
                return item;
            }
        }

        return null;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}

