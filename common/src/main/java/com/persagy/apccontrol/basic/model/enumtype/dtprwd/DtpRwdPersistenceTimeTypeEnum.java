package com.persagy.apccontrol.basic.model.enumtype.dtprwd;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.LoopConditions;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 物理世界时间类型枚举
 *
 * @author LiuYang
 * @since 5.240802.0
 */
@AllArgsConstructor
public enum DtpRwdPersistenceTimeTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 以5分钟为精度
     */
    FIVE_MINUTES("5min", "key_4446ff92b2ea49cf8e13310eeb6e3905"),

    /**
     * 以15分钟为精度
     */
    FIFTEEN_MINUTES("15min", "key_0b75984092784ae094777dabf87daf0e"),

    /**
     * 以1小时为精度
     */
    HOUR("1h", "key_e98e3e4c0d854bfca13dead6c4f5765f"),

    /**
     * 以1个天为精度
     */
    DAY("1d", "key_9a28927f44c541709d569ea0ea33f204"),

    /**
     * 以1个月为精度
     */
    MONTH("1m", "key_7cd287354ff14895aa14716e0aa1d271"),

    /**
     * 以1年为精度
     */
    YEAR("1y", "key_7cd287354ff14895aa14716e0aa1d272");

    /**
     * 枚举值
     */
    private final String type;

    /**
     * 枚举信息
     */
    private final String desc;

    @Override
    public String getValue() {
        return type;
    }

    @Override
    public String value() {
        return type;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DtpRwdPersistenceTimeTypeEnum analyze(String type) {
        return BaseEnum.analyze(type, DtpRwdPersistenceTimeTypeEnum.class);
    }

    /**
     * 时间类型转为时间补齐条件，暂时不区分运行监测类型
     * @param timeTypeEnum 时间类型
     * @return 循环条件
     */
    public static LoopConditions timeTypeToTimeComplementConditions(DtpRwdPersistenceTimeTypeEnum timeTypeEnum){
        switch (timeTypeEnum){
            case FIVE_MINUTES:
                return new LoopConditions(1,288,1);
            case FIFTEEN_MINUTES:
                return new LoopConditions(1,96,1);
            case HOUR:
                return new LoopConditions(1,24,1);
            default:
                return new LoopConditions(1,1,1);
        }
    }

}
