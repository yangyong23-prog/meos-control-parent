package com.persagy.apccontrol.basic.model.enumtype.systemgraph;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangPing
 * @date 2023/8/23
 */
@Getter
@AllArgsConstructor
public enum IconType {

    /**
     * 图标类型
     */
    ELECTRICAL_ICON(0, "key_62851f63e8c1420482e1af0d4293d1b6"),
    SMALL_ICON(1, "key_b8eea374b2e6411286c7c08e7044077f"),
    ZOOM_ICON(2, "key_0b64f3cfe0cb4332ac451d8b32d9b874"),
    DISPLAY_NONE(3, "key_af0bb8e7ca2944a39896535b8f045310"),
    SKELETON_ICON(4, "key_400dfec8cea14840938915320fcae193"),
    ;

    @EnumValue
    private final Integer value;
    private final String desc;

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }

    /**
     * 获取图标类型
     *
     * @author wangPing
     * @date 2023/8/23
     */
    public static IconType getIconType(Integer value) {

        for (IconType type : IconType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return ELECTRICAL_ICON;
    }

}
