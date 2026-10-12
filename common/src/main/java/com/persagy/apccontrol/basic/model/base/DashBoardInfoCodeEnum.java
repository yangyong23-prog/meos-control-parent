package com.persagy.apccontrol.basic.model.base;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 系统图固定显示信息点
 * @author: yancheng
 * @date: 2023/3/16
 * @version: V1.0
 */
@Getter
public enum DashBoardInfoCodeEnum implements BaseEnum<String>, IEnum<String> {


    SYS_COP("1", "sysCOP", "ColdStationEER", "key_a129ee280e2345d597ab8007217a255b", 1),
    CHILLERS_COP("1", "chillersCOP", "ColdUnitCOP", "key_02364e2a1dc8429794a70c0c1254d877", 2),
    WTF_CH("1", "wTFch", "FreezingPumpRate", "key_3ceb43402cd449cbb71cc63828538f13", 3),
    WTF_C("1", "wTFc", "CoolingPumpRate", "key_80ee76063e2d41be9ebfa14d62c5b322", 4),
    CT_HEAT_FACTOR("1", "cTHeatFactor", "CoolingTowerRate", "key_016d9e80e1ca4944931f4838d560d96b", 5),
    THERMAL_UNBALANCE_RATE("1", "thermalUnbalanceRate", "thermalUnbalanceRate", "key_924447d4d1484891b15d218237c57e70", 6),
    SUBENTRY_ACC_CONSUM("1", "subentryAccConsum", "subentryAccConsum", "key_86e203d33ac4417cad08d7001abf42d2"),

    IPLR("2", "iPLR", "iPLR", "key_dc79f1ac329e44a5adfad74fbdb8f5d4"),

    COOL_WATER_MAIN_RETURN_TEMP("3", "coolWaterMainReturnTemp", "coolWaterMainReturnTemp", "key_4e1336350b77422fb84b72b762537be3"),
    COOL_WATER_RETURN_TEMP_SET("3", "coolWaterReturnTempSet", "coolWaterReturnTempSet", "key_eb9326859c8746899aacb16f6207caa2"),

    CHILL_WATER_DETA_TEMP("4", "chillWaterDetaTemp", "chillWaterDetaTemp", "key_d68227e4cfa4474f875257cade26a9ec"),
    CHILL_WATER_DETA_TEMP_SET("4", "chillWaterDetaTempSet", "chillWaterDetaTempSet", "key_cef8eb4aa8d04f63ad90fdc6195fb09c"),

    PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP("5", "primaryChillWaterMainSupplyTemp", "primaryChillWaterMainSupplyTemp", "key_dfd9ae9f57b041fd85bf45264d3562bd"),
    PRIMARY_CHILL_WATER_MAIN_RETURN_TEMP("5", "primaryChillWaterMainReturnTemp", "primaryChillWaterMainReturnTemp", "key_a6488711c8c2410380859588a4726fcf"),
    CHILL_WATER_OUT_TEMP_SET("5", "chillWaterOutTempSet", "chillWaterOutTempSet", "key_f646f273350f4e10985d07705dba7767"),

    BRANCH_DETA_TEMP_SET("6", "branchDetaTempSet", "branchDetaTempSet", "key_5cd3a60434ed41499a78c79b3337b204"),
    BRANCH_DETA_TEMP("6", "branchDetaTemp", "branchDetaTemp", "key_60359714d9564587ae3def19268b654d"),
    BRANCH_SUPPLY_TEMP("6", "branchSupplyTemp", "branchSupplyTemp", "key_62099a3d1ea8495ebb8d599e8b28f31e"),
    BRANCH_RETURN_TEMP("6", "branchReturnTemp", "branchReturnTemp", "key_c0dfb5a77e9e4e21adde34c879d0eb3f"),

    BRANCH_DETA_PRESS_ET("7", "branchDetaPressSet", "branchDetaPressSet", "key_22ab88d41448464cbac01db0c346a679"),
    BRANCH_DETA_PRESS("7", "branchDetaPress", "branchDetaPress", "key_aa3ec35de0be4ffb92205469919dbdaf"),


    SYS_SWITCH_SET("9", "sysSwitchSet", "sysSwitchSet", "key_de31288027af446b8e28acdce8a99681"),

    CHILLER_L_NUM("9", "chillerLNum", "chillerLNum", "key_582ddbefdbaa450389ef1309e88f951a"),
    CHILLER_S_NUM("9", "chillerSNum", "chillerSNum", "key_14236a9f810048e58d91c658db93160b"),


    CHILL_WATER_DETA_PRESS_SET("9", "chillWaterDetaPressSet", "chillWaterDetaPressSet", "key_8f7243ab398f46b6b0380ffa7d043078"),
    CHILL_WATER_DETA_PRESS("9", "chillWaterDetaPress", "chillWaterDetaPress", "key_4ad868c4085443f78fcf941144038179"),
    ENABLE_CTRL("9", "enableCtrl", "enableCtrl", "key_bd0ff3d0eae3476a8d079b1586220bfd"),


    BRANCH_SUPPLY_PRESS("9", "branchSupplyPress", "branchSupplyPress", "key_0513da7a63b247e1bcb084104e5dd771"),
    BRANCH_RETURN_PRESS("9", "branchReturnPress", "branchReturnPress", "key_09d461c4bbff448eacb052749ec7941b"),

    MAX_PLR_SET("8", "maxPLRSet", "maxPLRSet", "key_9d91e73438a64b4b9dd32d521a114bd9"),
    EVAP_WATER_OUT_TEMP("8", "evapWaterOutTemp", "evapWaterOutTemp", "key_1aad8c2f85e4411985dec4e052edc37b"),


    /**
     * 冷热站
     */
    HEAT_WATER_SUPPLY_TEMP("10", "heatWaterSupplyTemp", "heatWaterSupplyTemp", "key_f8026dfaa102432c9e3e0ce438028bd7"),
    HEAT_WATER_SUPPLY_TEMP_SET("10", "heatWaterSupplyTempSet", "heatWaterSupplyTempSet", "key_1297ea4f90b6454dacde02431a593d64"),


    //供回水温差
    CHILL_WATER_MAIN_SUPPLY_TEMP("10", "chillWaterMainSupplyTemp", "chillWaterMainSupplyTemp", "key_f6ae8f0ccc104b339048ed37ee6b0932"),
    CHILL_WATER_MAIN_RETURN_TEMP("10", "chillWaterMainReturnTemp", "chillWaterMainReturnTemp", "key_6178054a0ace494ca933d16ddee7ac92"),

    //供回水压差
    CHILL_WATER_MAIN_SUPPLY_PRESS("10", "chillWaterMainSupplyPress", "chillWaterMainSupplyPress", "key_24f008d0b1c149cf92c50d757e41d874"),
    CHILL_WATER_MAIN_RETURN_PRESS("10", "chillWaterMainReturnPress", "chillWaterMainReturnPress", "key_16f98acdc4e84e34b167b983116b28d4"),

    CHILL_HEAT_WATER_DETA_TEMP("10", "chillHeatWaterDetaTemp", "chillHeatWaterDetaTemp", "key_8144d6421321446fbd8e554ee14a20ce"),
    CHILL_HEAT_WATER_DETA_PRESS("10", "chillHeatWaterDetaPress", "chillHeatWaterDetaPress", "key_619ed56bd4ca4e4385d7c2d53933bb9a"),

    /**
     * 热源
     */
    EQUIP_SWITCH_SET("11", "equipSwitchSet", "equipSwitchSet", "key_46f5d049b2624453865cdc94308e0d89"),

    /**
     * 供水温度设定  热源得
     * */
    HEAT_SECONDARY_SIDE_WATER_MAIN_SUPPLY_TEMP_SET("15", "heatSecondarySideWaterMainSupplyTempSet", "heatSecondarySideWaterMainSupplyTempSet", "key_4f8a97e09a7b414a83e2bdfaa80c5282"),


    /**
     *一次泵系统
     * */
    HEAT_SECONDARY_SIDE_WATER_DETA_TEMP_SET("14", "heatSecondarySideWaterDetaTempSet", "heatSecondarySideWaterDetaTempSet", "key_f5a565b0643d400c802de2385e529df6"),
    HEAT_SECONDARY_SIDE_WATER_DETA_PRESSURE_SET("14", "heatSecondarySideWaterDetaPressureSet", "heatSecondarySideWaterDetaPressureSet", "key_2fe2d943de4a41f2a0c68f43e5a278ca"),


    //供回水压差
    WORST_TERMINAL_DETA_PRESS("12", "worstTerminalDetaPress", "worstTerminalDetaPress", "key_b75490681a314861ad1f25eaa4d68df9"),
    WORST_TERMINAL_DETA_PRESS_SET("12", "worstTerminalDetaPressSet", "worstTerminalDetaPressSet", "key_5ea47df019c442abaa2a1e9f498b68f6"),

    HEAT_WATER_MAIN_SUPPLY_TEMP("13", "heatWaterMainSupplyTemp", "heatWaterMainSupplyTemp", "key_776022a131164e04a96662c827e41f31"),
    HEAT_WATER_MAIN_RETURN_TEMP("13", "heatWaterMainReturnTemp", "heatWaterMainReturnTemp", "key_01835bf561804ea290ef932fb2ae5a6e"),
    WATER_DETA_TEMP("13", "waterDetaTemp", "waterDetaTemp", "key_2b4a0613de304a769247660e1838ce4b"),

    HEAT_WATER_MAIN_SUPPLY_TEMP_2("13", "2heatWaterMainSupplyTemp", "2heatWaterMainSupplyTemp", "key_928bdfc3b44745c2a11c0a1babdf9658"),
    HEAT_WATER_MAIN_RETURN_TEMP_2("13", "2heatWaterMainReturnTemp", "2heatWaterMainReturnTemp", "key_431230d63afd4f23917d2490dd1c9675"),
    HEAT_SECONDARY_SIDE_WATER_DETA_TEMP("13", "heatSecondarySideWaterDetaTemp", "heatSecondarySideWaterDetaTemp", "key_faae2419e0ab48f9aa2709f38c5dcea6"),
    HEAT_SECONDARY_SIDE_WATER_DETA_PRESSURE("13", "heatSecondarySideWaterDetaPressure", "heatSecondarySideWaterDetaPressure", "key_faae2419e0ab48f9aa2709f38c5dcea6"),

    ;


    private final String id;
    private final String code;
    private final String aliasCode;
    private final String name;
    private final Integer order;

    DashBoardInfoCodeEnum(String id, String code, String aliasCode, String name) {
        this(id, code, aliasCode, name, null);
    }

    DashBoardInfoCodeEnum(String id, String code, String aliasCode, String name, Integer order) {
        this.id = id;
        this.code = code;
        this.aliasCode = aliasCode;
        this.name = name;
        this.order = order;
    }

    public static List<String> getInfoCodeList(String id) {
        List<String> infoCodes = new ArrayList<>();
        for (DashBoardInfoCodeEnum value :
                Arrays.stream(DashBoardInfoCodeEnum.values()).filter(param -> id.equals(param.getId())).collect(Collectors.toList())) {
            infoCodes.add(value.getCode());
        }
        return infoCodes;
    }

    public static DashBoardInfoCodeEnum getLocalCode(String aliasCode) {
        if (StrUtil.isBlank(aliasCode)) {
            return null;
        }
        for (DashBoardInfoCodeEnum value : DashBoardInfoCodeEnum.values()) {
            if (aliasCode.equals(value.getAliasCode())) {
                return value;
            }
        }
        return null;
    }

    /**
     * 获得排序，如果排序为空，则返回-99
     *
     * @param code 编码
     * @return {@code Integer }
     * @since 7.250801.0
     */
    @NotNull
    public static Integer getOrder(String code) {
        for (DashBoardInfoCodeEnum value : DashBoardInfoCodeEnum.values()) {
            if (code.equals(value.getCode())) {
                return value.getOrder() == null ? -99 : value.getOrder();
            }
        }
        return -99;
    }

    public static String getName(String code) {

        for (DashBoardInfoCodeEnum value : DashBoardInfoCodeEnum.values()) {
            if (code.equals(value.getCode())) {
                return value.getName();
            }
        }
        return null;
    }

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
        return LocaleMessageUtil.getMessage(name);
    }
    public String getName() {
        return LocaleMessageUtil.getMessage(name);
    }

}
