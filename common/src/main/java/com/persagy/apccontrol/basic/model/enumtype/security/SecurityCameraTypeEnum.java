package com.persagy.apccontrol.basic.model.enumtype.security;


import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SecurityCameraTypeEnum implements BaseEnum<String>, IEnum<String> {

    SPVSCM_CAMERATYPE_1("SPVSCM_CAMERATYPE_1", "key_200761a35c3b46cd8b7d289ee939ba6b"),
    SPVSCM_CAMERATYPE_2("SPVSCM_CAMERATYPE_2", "key_67b2f119dfe64897b99797eefc51819a"),
    SPVSCM_CAMERATYPE_3("SPVSCM_CAMERATYPE_3", "key_02a82a005e9c40d98a15ce2e659dd3ed"),
    SPVSCM_HASARMINGFUNC_1("SPVSCM_HASARMINGFUNC_1", "key_61d8f2dcd7d04621aa9d951b229692f1"),
    UNKNOWN("UNKNOWN", "key_08241d179eeb4c989389940b30544847"),
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
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SecurityCameraTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, SecurityCameraTypeEnum.class);
    }
}
