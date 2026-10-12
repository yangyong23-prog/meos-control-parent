package com.persagy.apccontrol.basic.model.enumtype.strategy;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.model.base.DashBoardInfoCodeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StrategyInfoCodeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 冷站开启状态
     */
    RUNSTATUS("runStatus", "key_d8c78a956d32482984cd9e5b264df917", ""),

    /**
     * 冷机开启台数
     */
    CHILLERNUM("chillerNum", "key_3eea8a31cd3243a6900dd2fa17c7acc5", ""),

    /**
     * 冷机出水温度设定值
     */
    CHILLWATEROUTTEMSET("chillWaterOutTempSet", "key_7931bb5c6f7b4cdca0e539844364814b", "℃"),

    /**
     * 出水温度设定--供热
     */
    HEAT_WATER_SUPPLY_TEMP_SET("heatWaterSupplyTempSet", "heatWaterSupplyTempSet", "key_1297ea4f90b6454dacde02431a593d64"),

    /**
     * 冷冻一次侧供回水温差设定
     */
    CHILL_WATER_DETA_TEMP_SET("chillWaterDetaTempSet", "key_d68227e4cfa4474f875257cade26a9ec", "℃"),

    /**
     * 冷冻一次一级泵供回水压差设定
     */
    CHILL_WATER_DETA_PRESS_SET("chillWaterDetaPressSet", "key_9ef762901c3947b3a0a331f63e68c7bb", "kPa"),

    /**
     * 对应支路供回水压差设定
     */
    BRANCH_DETA_PRESS_SET("branchDetaPressSet", "key_2030ebbe33b64218be465226dcb44f06", "kPa"),

    /**
     * 对应支路供回水温差设定
     */
    BRANCH_DETA_TEMP_SET("branchDetaTempSet", "key_0bad0d117622430594550a47a9a2da4c", "℃"),

    /**
     * 热源二次侧供水干管温度设定
     */
    HEAT_SECONDARY_SIDE_WATER_MAIN_SUPPLY_TEMP_SET("heatSecondarySideWaterMainSupplyTempSet", "key_0d7baf0b866749a286869e856b97a6a9", "℃"),

    /**
     * 热源二次侧供回水温差设定
     */
    HEAT_SECONDARY_SIDE_WATER_DETA_TEMP_SET("heatSecondarySideWaterDetaTempSet", "key_8144d6421321446fbd8e554ee14a20ce", "℃"),

    /**
     * 热源二次侧供回水压差设定
     */
    HEAT_SECONDARY_SIDE_WATER_DETA_PRESSURE_SET("heatSecondarySideWaterDetaPressureSet", "key_619ed56bd4ca4e4385d7c2d53933bb9a", "kPa"),

    /**
     * 系统-最不利末端压差
     */
    WORST_TERMINAL_DETA_PRESS_SET("worstTerminalDetaPressSet", "key_5ea47df019c442abaa2a1e9f498b68f6", "kPa");

    /**
     * 信息点编码
     */
    private final String infoCode;

    /**
     * 信息点名称
     */
    private final String name;

    /**
     * 单位
     */
    private final String unit;

    @Override
    public String getValue() {
        return infoCode;
    }

    @Override
    public String value() {
        return infoCode;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage( name);
    }
    public String getName() {
        return LocaleMessageUtil.getMessage( name);
    }

    public static StrategyInfoCodeEnum getByCode(String code) {
        for (StrategyInfoCodeEnum value : StrategyInfoCodeEnum.values()) {
            if (value.getValue().equals(code)) {
                return value;
            }
        }
        return null;
    }


    /**
     * 大小机信息点特殊处理
     *
     * @param infoCode
     * @return
     */
    public static String getChillerNumByInfoCode(String infoCode) {
        //大小机信息点特殊处理
        if (DashBoardInfoCodeEnum.CHILLER_L_NUM.getCode().equals(infoCode) ||
                DashBoardInfoCodeEnum.CHILLER_S_NUM.getCode().equals(infoCode)
        ) {
            infoCode = StrategyInfoCodeEnum.CHILLERNUM.getInfoCode();
        }
        return infoCode;
    }
}
