package com.persagy.apccontrol.basic.model.enumtype.systemgraph;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author houyuanzhi
 * @date 2023/10/7 15:04
 */
@Getter
@AllArgsConstructor
public enum SystemGraphType {
    NONE("none","key_3c698c199e5b4ccdaede7b7fa9046c30"),
    NEW("new","key_3474a5fd475f46b48d6fba929a24bf72"),
    OLD("old","key_d2a4c033fb7b4d1dbdc8613daccdb4da"),
    ;

    @EnumValue
    private String value;

    private String desc;

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }
}
