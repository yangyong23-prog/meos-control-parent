package com.persagy.apccontrol.basic.model.enumtype.systemgraph;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author houyuanzhi
 * @date 2023/8/11 15:56
 */
@Getter
@AllArgsConstructor
public enum LegendSourceStatus {

    DEFAULT(0, "key_1f44623b840d44b199b70a6239a60063"),
    UPDATED(1, "key_2122399ab1604b9f894e179e477c63b2"),
    DELETED(2, "key_c6652b85ecbe4dd6ba39de79ecdee66a"),
    ;

    @EnumValue
    private int value;
    private String desc;

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }
}
