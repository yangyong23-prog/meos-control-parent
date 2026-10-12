package com.persagy.apccontrol.basic.model.enumtype.ventilation;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VentilationSpaceStatusEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 正送风+排风
     */
    SEND_AND_REMOVE("SEND_AND_REMOVE", "key_d723543763994a81a2ca9e34e7647840"),
    /**
     * 正在送风
     */
    SENDING("SENDING", "key_33fdd5ebc1a9488ab6abed14e5fda0cf"),
    /**
     * 正在排风
     */
    REMOVING("REMOVING", "key_081169d213b343f281cce4dd73994b58"),
    /**
     * 未通风
     */
    NOT_VENTILATION("NOT_VENTILATION", "key_be5fa7e2c224433caf48f20955b01029"),
    /**
     * 无法检测
     */
    UNABLE_TO_DETECT("UNABLE_TO_DETECT", "key_341d08838ecd41f0a17983564146929e");

    private String value;
    private String reasonPhrase;

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }
}
