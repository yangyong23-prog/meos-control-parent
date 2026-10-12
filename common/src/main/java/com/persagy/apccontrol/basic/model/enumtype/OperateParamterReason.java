package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 手自动切换原因
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/25 15:06
 */
@Getter
@AllArgsConstructor
public enum OperateParamterReason implements BaseEnum<String>, IEnum<String> {

    OPTIMIZE_SETVALUE("01", "key_8d34b9b9ce0f4582947ff378af90b020"),
    ADJUST_HEAT_DISSIPATION("02", "key_795955230b6f457b924d1d713b6ef33f"),
    ADJUST_COOLING_WATER_FLOW("03", "key_e64dab787c9b4e68a1b6f4082e8e8ec9"),
    OTHER("OTHER", "key_7d6652fc005d47b3854cdc48b0b5d4f7");

    private final String code;
    private final String desc;

    public static Map<String, String> getOperateParamterReason() {
        Map<String, String> map = new HashMap<>();
        OperateParamterReason[] types = OperateParamterReason.values();
        for (OperateParamterReason type : types) {
            map.put(type.getCode(), type.getDesc());
        }
        return map;
    }

    @Override
    public String getValue() {
        return this.code;
    }

    @Override
    public String value() {
        return this.code;
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

    @Override
    public String getReasonPhrase() {
        return this.getDesc();
    }

}