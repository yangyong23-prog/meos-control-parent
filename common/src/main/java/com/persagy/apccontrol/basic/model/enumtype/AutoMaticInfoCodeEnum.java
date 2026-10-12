package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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
@AllArgsConstructor
public enum AutoMaticInfoCodeEnum implements BaseEnum<String>, IEnum<String> {


    /**
     * 冷站
     */
    RUN_STATUS("1", "runStatus", "sysSwitchSet", "key_93705a5b85af40e599687dfd5f3d6001"),

    CHILL_WATER_OUT_TEMP_SET("1", "chillWaterOutTempSet", "chillWaterOutTempSet", "key_f646f273350f4e10985d07705dba7767"),

    CHILLER_L_NUM_S("1", "chillerLNum", "chillerLNum", "key_582ddbefdbaa450389ef1309e88f951a"),
    CHILLER_S_NUM_S("1", "chillerSNum", "chillerSNum", "key_14236a9f810048e58d91c658db93160b"),

//    CHILL_WATER_DETA_TEMP_SET("1", "chillWaterOutTempSet", "chillWaterOutTempSet","冷冻水供回水温差设定"),
//    CHILL_WATER_DETA_PRESS_SET("1", "chillWaterDetaPressSet", "冷冻水供回水压差设定"),

    /**
     * 泵组
     */
    ENABLE_CTRL("2", "runStatus", "enableCtrl", "key_bd0ff3d0eae3476a8d079b1586220bfd"),
//    CHILLER_L_NUM_E("2", "chillerLNum", "chillerLNum","key_582ddbefdbaa450389ef1309e88f951a"),
//    CHILLER_S_NUM_E("2", "chillerSNum", "chillerSNum","key_14236a9f810048e58d91c658db93160b");
//    BRANCH_DETA_TEMP_SET("2", "branchDetaTempSet", "供回水温差设定"),
//    BRANCH_DETA_PRESSS_ET("2", "branchDetaPressSet", "供回水压差设定");

    /**
     * 冷热源系统
     */
    COLD_HOT_SYS_SWITCH_SET("3", "runStatus", "sysSwitchSet", "key_2aab399449b649aabc305cd6e05a1dde"),
    //主机出水温度设定
    COLD_HOT_CHILL_WATER_OUT_TEMP_SET("3", "chillWaterOutTempSet", "chillWaterOutTempSet", "key_300fea1e95344f1690841baa1ba41055"),
    COLD_HOT_CHILL_WATER_MAIN_SUPPLY_TEMP_SET("3", "heatWaterSupplyTempSet", "heatWaterSupplyTempSet", "key_02b01082c4ce499fbb3a7482fc12632b"),

    //和大机信息点相同
    // COLD_HOT_CHILLER_GROUPING_NUM("3", "chillerLNum", "chillerLNum", "组机组数"),
    COLD_HOT_CHILLER_L_NUM("3", "chillerLNum", "chillerLNum", "key_1a85012f95fd49d497b99de0b8830b42"),


    /**
     * 照明需求 CODE的为 照明回路功能类型的枚举值
     */
    OUTDOOR_ILLUMINATION_VALUE_TO_TURN_ON_GENERAL_LIGHTING("4", "2", "outdoorIlluminationValueToTurnOnGeneralLighting", "key_764b0955fa974be687ef067b4094980e"),
    OUTDOOR_ILLUMINATION_VALUE_TO_TURN_OFF_GENERAL_LIGHTING("4", "4", "outdoorIlluminationValueToTurnOffGeneralLighting", "key_3d5841b77c6146bbbcec75d7c74c0df1"),

    CONTROL_DEMAND("4", "controlDemand", "controlDemand", "key_0bd835ce0a35494f9b8410a52cb35edf"),
    ORD_LIGHT_CONTROL_DEMAND("4", "1", "ordLightControlDemand", "key_0290d1e7a82041e58db77024b14766cb"),
    AD_LIGHT_CONTROL_DEMAND("4", "3", "adLightControlDemand", "key_96f5ea72d648449e9844f0cdcae7eb4f"),
    SIGN_LIGHT_CONTROL_DEMAND("4", "7", "signLightControlDemand", "key_e4f2d4aba38744a1b2e7fbd8274b3e03"),
    DEC_LIGHT_CONTROL_DEMAND("4", "8", "decLightControlDemand", "key_e1ccaa3a8c7e418fa2dc2eddc240a614"),
    CONST_LIGHT_CONTROL_DEMAND("4", "9", "constLightControlDemand", "key_4473f9a316094b64b508c9068a1f82a7"),


    /**
     * 热源系统
     */
    HOT_SYS_SWITCH_SET("5", "runStatus", "sysSwitchSet", "key_d8c78a956d32482984cd9e5b264df917"),
    HOT_WATER_OUT_TEMP_SET("5", "hotWaterOutTempSet", "heatSecondarySideWaterMainSupplyTempSet", "key_0d7baf0b866749a286869e856b97a6a9"),
    HEAT_SECONDARY_SIDE_WATER_DETA_TEMP_SET("5", "hotWaterDetaSet", "heatSecondarySideWaterDetaTempSet", "key_8144d6421321446fbd8e554ee14a20ce"),
    HEAT_SECONDARY_SIDE_WATER_DETA_PRESSURE_SET("5", "hotWaterDetaSet", "heatSecondarySideWaterDetaPressureSet", "key_619ed56bd4ca4e4385d7c2d53933bb9a"),

    ;
    private final String id;
    private final String code;
    private final String practicalCode;
    private final String name;

    public static List<AutoMaticInfoCodeEnum> getInfoCodeList(String id) {
        return new ArrayList<>(Arrays.stream(AutoMaticInfoCodeEnum.values()).filter(param -> id.equals(param.getId())).collect(Collectors.toList()));
    }

    public static AutoMaticInfoCodeEnum getByCode(String code) {

        for (AutoMaticInfoCodeEnum value : AutoMaticInfoCodeEnum.values()) {
            if (code.equals(value.getCode())) {
                return value;
            }
        }
        return null;
    }

    public static String getName(String code) {

        for (AutoMaticInfoCodeEnum value : AutoMaticInfoCodeEnum.values()) {
            if (code.equals(value.getCode())) {
                return value.getName();
            }
        }
        return null;
    }

    /**
     * 判断是否是开关信息点
     *
     * @return
     */
    public static boolean switchState(String infoCode) {
        if (AutoMaticInfoCodeEnum.RUN_STATUS.getPracticalCode().equals(infoCode) ||
                AutoMaticInfoCodeEnum.ENABLE_CTRL.getPracticalCode().equals(infoCode) ||
                AutoMaticInfoCodeEnum.CONTROL_DEMAND.getPracticalCode().equals(infoCode)
        ) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是照明信息点
     *
     * @return
     */
    public static boolean controlDemandSwitchState(String infoCode) {
        if (AutoMaticInfoCodeEnum.CONTROL_DEMAND.getPracticalCode().equals(infoCode) ||
                AutoMaticInfoCodeEnum.ORD_LIGHT_CONTROL_DEMAND.getPracticalCode().equals(infoCode) ||
                AutoMaticInfoCodeEnum.AD_LIGHT_CONTROL_DEMAND.getPracticalCode().equals(infoCode) ||
                AutoMaticInfoCodeEnum.SIGN_LIGHT_CONTROL_DEMAND.getPracticalCode().equals(infoCode) ||
                AutoMaticInfoCodeEnum.DEC_LIGHT_CONTROL_DEMAND.getPracticalCode().equals(infoCode) ||
                AutoMaticInfoCodeEnum.CONST_LIGHT_CONTROL_DEMAND.getPracticalCode().equals(infoCode)||
                AutoMaticInfoCodeEnum.OUTDOOR_ILLUMINATION_VALUE_TO_TURN_ON_GENERAL_LIGHTING.getPracticalCode().equals(infoCode)||
                AutoMaticInfoCodeEnum.OUTDOOR_ILLUMINATION_VALUE_TO_TURN_OFF_GENERAL_LIGHTING.getPracticalCode().equals(infoCode)
        ) {
            return true;
        }
        return false;
    }

    /**
     * 获取信息点列表
     *
     * @return
     */
    public static List<String> getControlDemandSwitchState(String code) {
        List<AutoMaticInfoCodeEnum> list = getInfoCodeList(code);

        List<String> infoCode = new ArrayList<>();

        list.forEach(i -> infoCode.add(i.getPracticalCode()));
        return infoCode;
    }

    /**
     * 获取照明的信息点
     *
     * @param circuitFuncType
     * @return
     */
    public static AutoMaticInfoCodeEnum getControlDemand(String circuitFuncType) {
        for (AutoMaticInfoCodeEnum value : AutoMaticInfoCodeEnum.values()) {
            if (StringUtils.equals(value.getCode(), circuitFuncType)) {
                return value;
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
