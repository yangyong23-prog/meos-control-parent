package com.persagy.apccontrol.basic.model.enumtype.dtprwd.freezepumpgroup;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物理世界冷冻泵组信息点枚举
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Getter
@AllArgsConstructor
public enum DtpRwdFreezePumpGroupInfoCodeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 是否末级水泵组
     */
    IS_TERMINAL_PUMP("isTerminalPump", "isTerminalPump", "key_e3bbd1a4867049118d84a173107b28fb"),

    /**
     * 对应支路供回水压差设定
     */
    BRANCH_DETA_PRESS_SET("branchDetaPressSet", "branchDetaPress", "key_638d30800b2c470dbff0c81521cd6f0e"),

    /**
     * 对应支路供回水温差设定
     */
    BRANCH_DETA_TEMP_SET("branchDetaTempSet", "branchDetaTemp", "key_40ac6744dea44b1f8fac755b30e0c594"),

    /**
     * 最不利末端压差
     */
    WORST_TERMINAL_DETA_PRESS_SET("worstTerminalDetaPressSet", "worstTerminalDetaPressSet", "key_5ea47df019c442abaa2a1e9f498b68f6"),

    ;

    /**
     * 枚举值
     */
    private final String value;

    private final String feedback;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 是否为信息点设置类型
     *
     * @return 是否为信息点设置类型
     * @since v5.6.0
     */
    public boolean isInfoCodeSetType() {
        switch (this) {
            case BRANCH_DETA_TEMP_SET:
            case BRANCH_DETA_PRESS_SET:
                return true;
            default:
                return false;
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String value() {
        return value;
    }

    public String getFeedback() {
        return feedback;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DtpRwdFreezePumpGroupInfoCodeEnum analyze(String value) {
        return BaseEnum.analyze(value, DtpRwdFreezePumpGroupInfoCodeEnum.class);
    }
}
