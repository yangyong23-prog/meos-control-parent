package com.persagy.apccontrol.basic.model.enumtype.objectgroup;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 编组类型枚举
 *
 * @author MuAnFu
 * @date 2025-04-21
 */
@Getter
@AllArgsConstructor
public enum ObjectGroupTypeEnum implements BaseEnum<String>, IEnum<String> {

    VEDIO_MONITORING("VEDIO_MONITORING", "key_4b81f319f305482e8b1715f980cded6d", true, Collections.singletonList(InfoCodeEnum.CAMERA_TYPE.getCode())),

    ;

    private final String value;
    private final String reasonPhrase;
    private final Boolean isSyncCloud;
    private final List<String> addInfoCodes;

    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取枚举值
     *
     * @return 枚举值
     * @since 1.3.0
     */
    @Override
    public String value() {
        return value;
    }

    /**
     * 获取枚举信息
     *
     * @return 枚举信息
     * @since 1.3.0
     */
    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ObjectGroupTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, ObjectGroupTypeEnum.class);
    }
}
