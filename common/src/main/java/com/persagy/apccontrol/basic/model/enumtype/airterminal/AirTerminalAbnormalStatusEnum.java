package com.persagy.apccontrol.basic.model.enumtype.airterminal;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 空调末端-异常状态枚举
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@AllArgsConstructor
public enum AirTerminalAbnormalStatusEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 异常事件
     */
    ABNORMAL_EVENT("ABNORMAL_EVENT", "key_4e717c8ec9a34332a4a5ef73d80922d8"),

    /**
     * 控反不一致
     */
    ORDER_FAIL_ALARM("ORDER_FAIL_ALARM", "key_f0a572661bea44208463591020227964"),

    /**
     * 设备离线
     */
    EQUIPMENT_OFFLINE("EQUIPMENT_OFFLINE", "key_8e32dc25a5334668a15d6ca0be3c8d8f");

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
        return this.value;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AirTerminalAbnormalStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, AirTerminalAbnormalStatusEnum.class);
    }
}
