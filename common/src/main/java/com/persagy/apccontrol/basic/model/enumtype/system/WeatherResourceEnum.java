package com.persagy.apccontrol.basic.model.enumtype.system;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 天气来源
 * @author: yancheng
 * @date: 2023/3/25
 * @version: V1.0
 */
@Getter
@AllArgsConstructor
public enum WeatherResourceEnum implements BaseEnum<String>, IEnum<String> {
    WEATHER_SERVICES("weather_services", "key_ea1f862dc93a49be83fcc03e390ca00b", "天气服务", "key_36af4094b71d453a9ad22c736c44e261"),
    WEATHER_STATION("weather_station", "key_ea4f3d4c573a45288a838d56cc8ba5c2", "气象监测站", "key_044bfb339e5c489490ab87719f0b579e");

    /**
     * 编码
     */
    private final String code;
    /**
     * 名称
     */
    private final String name;
    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String explain;


    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static WeatherResourceEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (WeatherResourceEnum i : WeatherResourceEnum.values()) {
            if (code.equals(i.getCode())) {
                return i;
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
    public static Map<String,String> getEnumMapByCode(String code) {
        Map<String,String> map = new HashMap<>(3);
        if (code == null) {
            return null;
        }
        for (WeatherResourceEnum i : WeatherResourceEnum.values()) {
            if (code.equals(i.getCode())) {
                map.put("code", i.getCode());
                map.put("name", i.getName());
                map.put("explain", i.getExplain());
                return map;
            }
        }
        return null;
    }

    /**
     * 通过CODE 获取枚举
     *
     * @return
     */
    public static List<WeatherResourceEnum> getList() {
        return ListUtil.toList(WeatherResourceEnum.WEATHER_STATION,
                WeatherResourceEnum.WEATHER_SERVICES);
    }

    @Override
    public String getValue() {
        return this.code;
    }

    @Override
    public String value() {
        return this.code;
    }

    @Override
    public String getReasonPhrase() {
        return this.getInternationalName();
    }

    public String getName() {
        return this.value;
    }

    public String getInternationalName() {
        return LocaleMessageUtil.getMessage(this.name);
    }

    public String getExplain() {
        return LocaleMessageUtil.getMessage(this.explain);
    }
}
