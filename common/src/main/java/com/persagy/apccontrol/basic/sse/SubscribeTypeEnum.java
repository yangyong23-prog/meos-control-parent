package com.persagy.apccontrol.basic.sse;


import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * sse订阅类型
 * 暂定和DemandTemplateTypeEnum枚举值一致
 *
 * @author yuecaipu
 * @date 2024/4/29 15:00
 * @since v5.240402.00
 */
@Getter
@AllArgsConstructor
public enum SubscribeTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 照明监测
     */
    LIGHT_MONITORING("LIGHT_MONITORING", "key_1f55dccb06c94d7fa6bb1c4ade132b07"),

    /**
     * 空调末端
     */
    AIR_TERMINAL("AIR_TERMINAL", "key_56a25250df214dbbb7c1e62c8aae8801"),

    /**
     * 通风
     */
    VENTILATION("VENTILATION", "key_03a55d56f20348f5ae2578117c489cb6"),

    /**
     * 机器人
     */
    ROBOT("ROBOT", "key_030e7de44c9e470b826f01c0b2a5ffaf"),

    /**
     * 安防
     */
    SECURITY("SECURITY", "key_2b4bbb66befd48ae8b0c6df69ac7be89"),

    /**
     * 安防-撤布防
     */
    SECURITY_ARMING("SECURITY_ARMING", "key_6e9ef1662faa441cac3999183bd3decb"),

    /**
     * 安防-通行
     */
    SECURITY_PASSING("SECURITY_PASSING", "key_62b970abe23540068c8cb5446e97796b"),

    /**
     * 电梯
     */
    ELEVATOR("ELEVATOR", "key_7429dede25db40f28973ab3e90e1f28c"),

    /**
     * 客流
     */
    PASSENGER_FLOW("PASSENGER_FLOW", "key_8991792fb896439b99ae1d6642633bef"),

    /**
     * 给排水/中水
     */
    WATER_SUPPLY("WATER_SUPPLY", "key_f0d1a86b5f534cea87c13ca8b7926a93"),

    /**
     * 冷源/冷热源
     */
    AC("AC", "key_2b6d657f7cf24c849f50a7953310df73"),

    /**
     * 供配电与储能
     */
    SE("SE", "key_2d0c647119f0427fbf2efca1dfb19431"),

    /**
     * 停车场
     */
    PARKING("PARKING", "key_deb5ed39ffe14c5695e0a8ba1ce6eb39"),

    /**
     * 冷源
     */
    ACCC("ACCC", "key_235e4049874342d1a209fa13c3711563"),

    /**
     * 冷热源
     */
    ACCI("ACCI", "key_9e432c33a6f3404cbc4295b2bdead343"),

    /**
     * 热源
     */
    ACCH("ACCH", "key_2a7b0b8cf79743fb9409e9fcfa0f45fa"),
    /**
     * 新热源
     */
    ACAH("ACAH", "key_2a7b0b8cf79743fb9409e9fcfa0f45fa"),

    /**
     * 空调冷热源
     */
    ACAI("ACAI", "key_air_conditioning_cold_heat_source"),

    /**
     * 厨房油烟机
     */
    KITCHEN_RANGE_HOOD("KITCHEN_RANGE_HOOD", "key_69d5825940c14669b2fd0621f24a8f7a"),

    /**
     * 机房安全
     */
    COMPUTER_ROOM_SECURITY("COMPUTER_ROOM_SECURITY", "key_966adee3e1e64b5b99ef8591e9932269"),

    /**
     * 消防
     */
    FIRE_FIGHTING("FIRE_FIGHTING", "key_9b92b4f9c84b449dafeb9254c11d2596"),

    /**
     * 污水处理
     */
    SEWAGE("SEWAGE", "key_35cf9f6389744fce8946524f9baee565"),

    /**
     * 弱电
     */
    WEAK_POWER("WEAK_POWER", "key_53425b0f3d3944f98b24e415e80b71b6"),

    /**
     * 消防电
     */
    FIRE_FIGHTING_ELECTRICITY("FIRE_FIGHTING_ELECTRICITY", "key_bb73faf2d8bf4cf9a6558ba2f2cb1737"),

    /**
     * 空调冷源
     */
    AIR_CONDITIONING_COLD_SOURCE("AIR_CONDITIONING_COLD_SOURCE", "key_dfbd48a8133047f8a6b51f2b5e34909f"),
    /**
     * 空压机
     */
    AIR_COMPRESSOR("AIR_COMPRESSOR", "key_fdbeef56be0e472d93700c4237abffb2"),

    /**
     * 卫生间
     */
    TOILET("TOILET", "key_adb4c71ef151472d82d64d276029bc96"),
    /**
     * 光伏
     */
    PHOTOVOLTAIC("PHOTOVOLTAIC", "key_a7b7692f7c3b458bb2c8028911dfc825"),

    /**
     * 运行智能体
     */
    AI("AI", "key_9acc36387fdf4acba3251add2e37e552"),

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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SubscribeTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, SubscribeTypeEnum.class);
    }

    public static SubscribeTypeEnum getByCode(String code) {
        for (SubscribeTypeEnum value : SubscribeTypeEnum.values()) {
            if (value.getValue().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
