package com.persagy.apccontrol.basic.model.enumtype.terminal;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备空间异常状态
 * 4.0.0 增加离线 3.0.0 故障状态  2.0.0 增加高、低液位报警  1.0.0 控反不一致、异常事件
 *
 * @author LiuYang
 * @version 4.0.0 增加离线
 * @date 2024/5/24 14:17
 * @since v5.240502.0
 */
@Getter
@AllArgsConstructor
public enum AbnormalStatusTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 控反不一致
     */
    ORDER_FAIL_ALARM("ORDER_FAIL_ALARM", "key_f0a572661bea44208463591020227964"),

    /**
     * 离线
     */
    OFFLINE("OFFLINE", "key_bcb07a983fc34e0e8b0878f0668597de"),

    /**
     * 异常事件
     */
    ABNORMAL_EVENT("ABNORMAL_EVENT", "key_4e717c8ec9a34332a4a5ef73d80922d8"),

    /**
     * 高液位报警
     */
    HIGH_LEV_ALARM("HIGH_LEV_ALARM", "key_e59a7772a8cf4fa7ac9fe23a2d793f0f"),

    /**
     * 低液位报警
     */
    LOW_LEV_ALARM("LOW_LEV_ALARM", "key_74d553a880f9482ba6c0d448675c2b5a"),

    /**
     * 故障状态
     */
    FAULT_STATUS("FAULT_STATUS", "key_0d2627830b2047f8bf2ba4661cd37319"),

    ;

    final String value;
    final String reasonPhrase;

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
    public static AbnormalStatusTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, AbnormalStatusTypeEnum.class);
    }
}
