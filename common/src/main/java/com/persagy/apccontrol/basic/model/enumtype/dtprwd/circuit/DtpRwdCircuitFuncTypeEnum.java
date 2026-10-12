package com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 物理世界照明回路功能类型枚举
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public enum DtpRwdCircuitFuncTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 普通照明
     */
    GENERAL_LIGHTING("1", "key_41bb731b18a8450c8cb6612ffb131c7a"),

    /**
     * 广告照明
     */
    ADVERTISING_LIGHTING("3", "key_cee5ff425ea74aae9136cf300ede90ee"),

    /**
     * 备用照明
     */
    BACKUP_LIGHTING("4", "key_c914958327ec49e3b17d1679dadf5255"),

    /**
     * 标识照明
     */
    SIGN_LIGHTING("7", "key_045e41901fef446194d14c237beaf173"),

    /**
     * 装饰照明
     */
    DECORATIVE_LIGHTING("8", "key_7e1226ab64ec46618890b94be3bad77c"),

    /**
     * 常亮照明
     */
    ALWAYS_LIGHTING("9", "key_39de81aada0443cfb75dc01b15a7337d"),

    /**
     * 其他
     */
    OTHER("99", "key_7d6652fc005d47b3854cdc48b0b5d4f7");

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
        return String.valueOf(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DtpRwdCircuitFuncTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DtpRwdCircuitFuncTypeEnum.class, false);
    }
}
