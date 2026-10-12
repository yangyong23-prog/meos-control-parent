package com.persagy.apccontrol.basic.model.enumtype.dtprwd;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 物理世界对象类型枚举
 *
 * @author MuAnfu
 * @since 5.240602.0
 */
@AllArgsConstructor
public enum DtpRwdObjectTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 项目
     */
    PROJECT("project", "key_c8ff811cb5234f69b587f5dd1fa73141"),

    /**
     * 建筑
     */
    BUILDING("building", "key_e977b930027f4fcfbf29bdf9ed47663f"),

    /**
     * 楼层
     */
    FLOOR("floor", "key_860f403627e4419496e3711477912a39"),

    /**
     * 设备
     */
    EQUIPMENT("equipment", "key_b057517385f34aca901cb69c4f64433d"),

    /**
     * 空间
     */
    SPACE("space", "key_93930e54e51b4dbda89417183881b172"),

    /**
     * 系统
     */
    SYSTEM("system", "key_97300a9731294466884c1933c2a6cb49"),

    /**
     * 竖井
     */
    SHAFT("shaft", "key_f4acdd3c2ec34905a6f8e61554a0553c"),

    /**
     * 设备组
     */
    EQUIPMENT_GROUP("equipGroup", "key_cd9c69bfd5174d7bbd6ee06fe4ae011c"),

    /**
     * 主要功能区
     */
    PARTITION("partition", "key_d153b6aab9c146c3af01e28dd69ed6e0");

    /**
     * 枚举值
     */
    private final String code;

    /**
     * 枚举信息
     */
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

    @Override
    public String toString() {
        return code;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DtpRwdObjectTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DtpRwdObjectTypeEnum.class);
    }
}
