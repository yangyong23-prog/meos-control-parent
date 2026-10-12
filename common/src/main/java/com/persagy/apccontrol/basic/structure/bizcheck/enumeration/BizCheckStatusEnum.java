package com.persagy.apccontrol.basic.structure.bizcheck.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *检查状态枚举
 *
 * @author wxf
 * @date 2026-02-27
 */
@Getter
@AllArgsConstructor
public enum BizCheckStatusEnum implements BaseEnum<String>, IEnum<String> {

    /**
     *检查中
     */
    PENDING("PENDING", "key_check_status_pending"),

    /**
     *检查通过
     */
    COMPLETED("COMPLETED", "key_check_status_completed"),

    /**
     *检查未通过
     */
    REJECTED("REJECTED", "key_check_status_check_rejected"),

    /**
     *异常
     */
    EXCEPTION("EXCEPTION", "key_check_status_exception"),

    /**
     *失败
     */
    FAILED("FAILED", "key_check_status_failed"),

    /**
     *正常
     */
    NORMAL("NORMAL", "key_check_status_normal"),

    /**
     *待检查
     */
    TO_BE_CHECKED("TO_BE_CHECKED", "key_check_status_to_be_checked");

    /**
     *枚值举值
     */
    private final String value;

    /**
     *枚信息信息
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
    public static BizCheckStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, BizCheckStatusEnum.class);
    }
}