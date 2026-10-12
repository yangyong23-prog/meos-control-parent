package com.persagy.apccontrol.basic.model.enumtype.system;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

/**
 * @author yuyao
 * @date 2022/7/27 18:26
 * 自定义系统返回结果code
 */
public enum SysResponseCode {

    /**
     * 1. 数据不存在 2. 数据异常
     */
    FAIL_K0001("K0001", "key_46b9ca60baf34c97ac1f34f32877765f"),

    SYS_BUSY("B0210", "key_30118dd8f03a4968bbfad116e9be7088"),

    /*************计算引擎异常开始*********************/
    OVER_ERROR("over_error", "key_cc0ca8ee5ca6471ca692a8e75c54e6cc"),

    BREAK_ERROR("break_error", "key_3fc8ba2c87754379a585d52cf418d4b4"),

    /*************计算引擎异常结束*********************/
    ;


    private final String code;
    private final String description;

    private SysResponseCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return LocaleMessageUtil.getMessage(this.description);
    }
}
