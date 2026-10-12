package com.persagy.apccontrol.basic.model.dto.twin;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.enumtype.EnumCalculateValueType;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务数据类型枚举类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/12/24 10:21
 */
@Getter
@AllArgsConstructor
public enum BusinessTypeEnum implements BaseEnum<String>, IEnum<String> {

    LIGHT_DEMAND_CHANGE("LIGHT_DEMAND_CHANGE", "key_5bb6d18830044d7da6553073a132b6ee"),
    
    RUN_CONSOLE_DEMAND_CHANGE("RUN_CONSOLE_DEMAND_CHANGE", "key_8ec3dcf8ffd441ddbccf2b7a3f56a8a8"),
    RUN_CONSOLE_DEMAND_CHANGE_SWITCH_SET("RUN_CONSOLE_DEMAND_CHANGE_SWITCH_SET", "key_e74f45775dd145d7b2f4b619e1c2a7f4"),

    RUNNING_LOG("RUNNING_LOG", "key_2f264796a74a460292841a17f0f7105c"),
    
    BUSINESS_RUNNING_LOG("BUSINESS_RUNNING_LOG", "key_7f5c78191ff646ba9ceefe6599f409d0"),
    RELEASE_INTERVENTION("RELEASE_INTERVENTION", "key_68e0a65b3a884a7b9efbc7872adb520f");

    private final String value;

    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EnumCalculateValueType analyze(String value) {
        return BaseEnum.analyze(value, EnumCalculateValueType.class, false);
    }

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
        return LocaleMessageUtil.getMessage( desc);
    }
    public String getDesc() {
        return LocaleMessageUtil.getMessage( desc);
    }


}
