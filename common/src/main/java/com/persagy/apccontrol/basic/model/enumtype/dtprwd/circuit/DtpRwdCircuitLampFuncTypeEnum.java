package com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.structure.map.OpenEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 物理世界-照明回路-灯具功能类型枚举
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Slf4j
@Getter
@AllArgsConstructor
@OpenEnum(DtpRwdCircuitDTO.Fields.lampFuncType)
public enum DtpRwdCircuitLampFuncTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 筒灯
     */
    DOWN_LIGHT("1", "key_0ed21331ece243829d179ab60633a46f"),

    /**
     * 射灯
     */
    LED_SPOT_LIGHT("2", "key_6cb85b5c3f374747baeb86bcb7357eb6"),

    /**
     * 灯带
     */
    LIGHT_STRIP("3", "key_3124367434e74b199f246738aa7b5ccc"),

    /**
     * 广告灯箱
     */
    ADVERTISING_LIGHT_BOXES("4", "key_f254c09f35ac4a2c89694d0a5d93c870"),

    /**
     * 标识指示灯
     */
    IDENTIFICATION_INDICATOR_LAMP("5", "key_be4bf64d9496464ebfc2418f1f227289"),

    /**
     * 应急灯
     */
    EMERGENCY_LAMP("6", "key_56d9609e7ff24392b4f60a6ecff0ede2"),

    /**
     * 金卤灯
     */
    METAL_HALIDE_LAMP("7", "key_b68285ddad2546c486b84061f591af00"),

    /**
     * 洗墙灯
     */
    LED_WALL_WASHER("8", "key_e9d40bc78a2d44e4b1f7a4e06b725e16"),

    /**
     * 泛光灯
     */
    FLOOD_LIGHT("9", "key_0e28567710ea4e2498fe323b82f6af6f"),

    /**
     * 投光灯
     */
    SPOTLIGHTS("10", "key_b7253e327134484a89eca5f00bc55409"),

    /**
     * 点光源
     */
    POINT_LIGHT_SOURCE("11", "key_e1bdeafd873e4dc6acbd80f21af45af0"),

    /**
     * 线条灯
     */
    LED_LINEAR_LIGHT("12", "key_83d5f2b2a698497fadbb47f0788f4e76"),

    /**
     * LOGO灯
     */
    LOGO_LIGHT("13", "key_2e6aea96b52644ceb574c256d08cca87"),

    /**
     * 路灯
     */
    ROAD_LAMP("14", "key_53f8086b21704b268476cc340a0242d6"),

    /**
     * 高杆灯
     */
    HIGH_POLE_LAMP("15", "key_29127ac1834d4785adee98dc371d1daa"),

    /**
     * 地埋灯
     */
    GROUND_BURIED_LAMP("16", "key_b0e993a5f81b44b4a442aa00beacc41f"),

    /**
     * 草坪灯
     */
    LAWN_LAMP("17", "key_25b09d0b72044dffab19544b191ee79e"),

    /**
     * 树灯
     */
    TREE_LIGHT("18", "key_7d4e538e54a741eab01bfd6c85120698"),

    /**
     * 庭院灯
     */
    COURTYARD_LAMP("19", "key_145c9cedcdee486ea1a69e602ef337be"),

    /**
     * 花坛轮廓灯
     */
    FLOWER_BED_BORDER_LIGHT("20", "key_2509cd556377483784abf2d46ea6e854"),

    /**
     * 造型装饰灯
     */
    DECORATIVE_SHAPE_LIGHT("21", "key_41e7aaa34a904119889519e4f5fad4a9"),

    /**
     * 小品灯
     */
    ESSAY_LAMP("22", "key_f4a9dec8bf6047258c0c0864dbe02a81"),

    /**
     * 水下灯
     */
    UNDERWATER_LAMP("23", "key_0819b1dd117044339142b811556d3272"),

    /**
     * 荧光灯
     */
    FLUORESCENT_LAMP("24", "key_7bb484739369423384b090ddb5071521"),

    /**
     * 日光灯
     */
    DAYLIGHT_LAMP("25", "key_cbd687c7c6f044669e7ab4af78377391"),

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
    public static DtpRwdCircuitLampFuncTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DtpRwdCircuitLampFuncTypeEnum.class, false);
    }
}
