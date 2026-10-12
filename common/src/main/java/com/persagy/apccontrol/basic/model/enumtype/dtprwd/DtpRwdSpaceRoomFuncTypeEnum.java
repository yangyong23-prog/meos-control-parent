package com.persagy.apccontrol.basic.model.enumtype.dtprwd;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 空间功能类型枚举
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/3/11 16:49
 * @since 7.250301.0
 */
@Getter
@AllArgsConstructor
public enum DtpRwdSpaceRoomFuncTypeEnum implements BaseEnum<String>, IEnum<String> {

    SFT_01_05_00("SFT-01.05.00", "key_faf1b456c97545c288637dc99b2365b8"),

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
    public static DtpRwdSpaceRoomFuncTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DtpRwdSpaceRoomFuncTypeEnum.class);
    }
}
