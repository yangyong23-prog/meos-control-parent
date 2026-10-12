package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * @author zyc
 * @version 1.0
 * @description: 指令下发的数据来源
 * @date 2023/12/20 17:33
 */
@Getter
@AllArgsConstructor
public enum SourceFromEnum implements BaseEnum<String>, IEnum<String>, CharSequence {

    /**
     * 指令类型
     */
    SCHEDULE("schedule", "key_46af7a725ee3416d98a43e001afb96e2"),
    AI_DEMAND("ai_demand", "key_6e9963d140054b31af6b9dc0777959d9"),
    ARTIFICIAL("artificial_demand", "key_5f03f45a33d140268eba4e79175fc325"),
    Lighting_ARTIFICIAL("lighting_artificial_demand", "key_cb0885cae2794079a6ab92b605ca7690"),
    RELEASE_INTERVENTION("RELEASE_INTERVENTION", "key_40dd435a0db648209b6964a43d355104"),
    OTHER("other", "key_227319888e0c474e82a92ca0d7d7ed7f");


    private final String code;

    private final String desc;

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
        return LocaleMessageUtil.getMessage(desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SourceFromEnum analyze(String value) {
        return BaseEnum.analyze(value, SourceFromEnum.class);
    }


    @Override
    public int length() {
        return 0;
    }

    @Override
    public char charAt(int index) {
        return 0;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return null;
    }
}
