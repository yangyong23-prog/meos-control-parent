package com.persagy.apccontrol.basic.model.enumtype.system;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 运行模式
 * @author: yancheng
 * @date: 2023/3/21
 * @version: V1.0
 */
@Getter
public enum RunModeEnum implements BaseEnum<String>, IEnum<String> {
    AUTOMATIC("automatic", "key_bbd420d07d844ff088860faed7435df2", "key_2d0a025185334f569479f44a18e6c421"),
    ENGINEER("engineer", "key_906106140d894c80a3ea25f9cfd5aade", "key_aedbd9ca7b1e4b5caa558ae3143909d1"),
    ;
    /**
     * 编码
     */
    private final String code;
    /**
     * 名称
     */
    private final String name;
    /**
     * 描述
     */
    private final String explain;

    RunModeEnum(String code, String name, String explain) {
        this.code = code;
        this.name = name;
        this.explain = explain;
    }

    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static RunModeEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (RunModeEnum i : RunModeEnum.values()) {
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
    public static Map<String, String> getEnumMapByCode(String code) {
        Map<String, String> map = new HashMap<>(3);
        if (code == null) {
            return null;
        }
        for (RunModeEnum i : RunModeEnum.values()) {
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
     * 获取枚举值列表
     *
     * @return KEY VAL 数据列表
     */
    public static List<RunModeEnum> getList() {
        return ListUtil.toList(RunModeEnum.AUTOMATIC,
                RunModeEnum.ENGINEER);
    }

    @Override
    public String getValue() {
        return code;
    }

    @Override
    public String value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage( name);
    }
    public String getName() {
        return LocaleMessageUtil.getMessage(this.name);
    }
    public String getExplain() {
        return LocaleMessageUtil.getMessage(this.explain);
    }
}
