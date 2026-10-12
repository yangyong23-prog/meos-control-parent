package com.persagy.apccontrol.basic.model.enumtype.dtprwd;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 物理世界系统类编码枚举
 *
 * @author ShaoHongbo
 * @since v5.6.0
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum DtpRwdSystemClassCodeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 中央供冷系统
     */
    ACCC("ACCC", "key_c1c0e430273742cb9d6786036bf5bf9f"),

    /**
     * 中央空调供冷全系统
     */
    ACAC("ACAC", "key_d7b145fd9abb4b4f8be7faf9bdb676f5"),

    /**
     * 空调末端系统
     */
    ACAT("ACAT", "key_8fe62d59824143dbbef9de6c3a3b8374"),

    /**
     * 中央冷热源系统
     */
    ACCI("ACCI", "key_6dd716f933c84625b56b7ace8e43aed6"),
    /**
     * 中央热源系统
     */
    ACCH("ACCH", "key_aa10273443864b3cb86eb3c2d930d0ed"),
    /**
     * 空调冷热源系统
     */
    ACAI("ACAI", "key_0c76a5ab003b4931abb309e20aeefc8c"),
    /**
     * 高压配电系统
     */
    SEHD("SEHD", "key_5257c7fd2e9841fc898f89e8c41f7a88"),
    /**
     * 低压配电系统
     */
    SELD("SELD", "key_7b56e44d64454ab2b585843ec937b45b"),

    /****** 记录日志需要，所以这里写入非系统级别的模块类型，新版非系统级别的 systemId 与一级菜单编码 都与 SubscribeTypeEnum 保持一致 ******/
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
    WS("WS", "key_f0d1a86b5f534cea87c13ca8b7926a93"),

    /**
     * 冷源/冷热源
     */
    AC("AC", "key_2b6d657f7cf24c849f50a7953310df73"),

    /**
     * 客流
     */
    SE("SE", "key_2d0c647119f0427fbf2efca1dfb19431"),

    /**
     * 停车场
     */
    PARKING("PARKING", "key_deb5ed39ffe14c5695e0a8ba1ce6eb39"),

    /**
     * 厨房油烟机
     */
    KITCHEN_RANGE_HOOD("KITCHEN_RANGE_HOOD", "key_69d5825940c14669b2fd0621f24a8f7a"),
    
    /**
     * 中央空调供冷全系统
     */
    ACAC_SYSTEM("ACAC", "key_d7b145fd9abb4b4f8be7faf9bdb676f5"),

    /**
     * 生活热水系统
     */
    WATER_SUPPLY_HOT("WSDH", "key_statusandcontrol_ws"),

    /**
     * 生活热水系统（系统关系映射）
     */
    WATER_SUPPLY_HOT_SYSTEM("WSDH", "key_statusandcontrol_ws"),
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
    public static DtpRwdSystemClassCodeEnum analyze(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return BaseEnum.analyze(value, DtpRwdSystemClassCodeEnum.class);
        } catch (Exception e) {
            log.error("枚举解析失败{}", value, e);
        }
        return null;
    }

}
