package com.persagy.apccontrol.basic.model.enumtype.runningLog;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RunningLogObjectCategpryEnum implements BaseEnum<String>, IEnum<String> {
    //"project","system","building","floor","space","group"
    PROJECT("project", "key_c8ff811cb5234f69b587f5dd1fa73141"),
    SYSTEM("system", "key_97300a9731294466884c1933c2a6cb49"),
    EQUIPMENT("equipment", "key_b057517385f34aca901cb69c4f64433d"),
    BUILDING("building", "key_e977b930027f4fcfbf29bdf9ed47663f"),
    FLOOR("floor", "key_860f403627e4419496e3711477912a39"),
    SPACE("space", "key_93930e54e51b4dbda89417183881b172"),
    GROUP("group", "key_cd9c69bfd5174d7bbd6ee06fe4ae011c"),
    ;

    private final String code;

    private final String desc;

    @Override
    public String getValue() {
        return this.code;
    }

    @Override
    public String value() {
        return this.code;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.desc);
    }
    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

    public static RunningLogObjectCategpryEnum getCategoryByObjType(String objType) {
        if (StringUtils.isEmpty(objType)) {
            return null;
        }
        switch (objType) {
            case "system":
                return RunningLogObjectCategpryEnum.SYSTEM;
            case "equipGroup":
                return RunningLogObjectCategpryEnum.GROUP;
            case "space":
                return RunningLogObjectCategpryEnum.SPACE;
            case "floor":
                return RunningLogObjectCategpryEnum.FLOOR;
            case "building":
                return RunningLogObjectCategpryEnum.BUILDING;
            case "project":
                return RunningLogObjectCategpryEnum.PROJECT;
            default:
                return RunningLogObjectCategpryEnum.EQUIPMENT;
        }
    }

}
