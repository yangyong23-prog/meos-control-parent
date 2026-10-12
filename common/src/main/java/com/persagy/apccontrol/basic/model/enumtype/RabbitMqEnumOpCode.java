package com.persagy.apccontrol.basic.model.enumtype;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * rabbitMQ消息传输类型
 *
 * @author yangwanyi
 */
@AllArgsConstructor
public enum RabbitMqEnumOpCode {

    RC_BASICS_CONFIG(1, "key_803496bd71ca4a8aab8ae0efe88b5219");

    @Setter
    @Getter
    private Integer type;

    @Setter
    @Getter
    private String desc;

    public String getDesc() {
        return LocaleMessageUtil.getMessage( desc);
    }

}
