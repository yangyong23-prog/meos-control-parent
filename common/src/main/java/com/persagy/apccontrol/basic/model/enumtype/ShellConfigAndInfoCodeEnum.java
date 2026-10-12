package com.persagy.apccontrol.basic.model.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @description：壳配置的配置项code和信息点编码之前的对应关系
 */
@Getter
@AllArgsConstructor
public enum ShellConfigAndInfoCodeEnum {

    BRANCH_DATA_TEMP_SET("policy_site_cooling_return_water_temp", "branchDetaTempSet", "对应支路供回水温差设定",""),
    BRANCH_DATA_PRESS_SET("policy_site_pump_return_water_vol", "branchDetaPressSet", "对应支路供回水压差设定",""),
    COOL_WATER_RETURN_TEMP_SET("policy_device_cooling_machine_water_temp", "coolWaterReturnTempSet", "冷机冷却水进水温度设定",""),
    CHILLER_WATER_DATA_TEMP_SET("group_policy_device_cooling", "chillWaterDetaTempSet", "冷冻水供回水温差设定",""),
    DISTRIBUTOR_COLLECTOR_DATA_PRESS_SET("policy_device_cooling_water_vol", "distributorCollectorDetaPressSet", "分集水器压差设定",""),
//    CHILLER_WATER_OUT_TEMP_SET("policy_device_chiller_water_temp", "chillWaterOutTempSet", "蒸发器出口水温设定",""),
    EQUIP_FREQ_SET("policy_device_fan_freq", "equipFreqSet", "设备频率设定", "ACCCOT"),
    EQUIP_FREQ_SET_CP("policy_device_pump_freq", "equipFreqSet", "设备频率设定", "ACCCCP"),
    OPENING_SET("policy_device_valve_degree", "openingSet", "开度设定",""),

    INDOOR_AIR_CO2_SET("policy_device_chiller_water_temp", "indoorAirCO2Set", "服务区域目标CO2浓度设定","ACATFU"),
    INDOOR_AIR_TEMP_SET("policy_device_chiller_water_temp", "indoorAirTempSet", "服务区域目标温度设定","ACATFU"),

    CHILL_WATER_VALVE_OPENING_SET("policy_end_vrvh_valve_degree", "chillWaterValveOpeningSet", "冷水阀开度设定","ACATAH"),
    FRESH_AIR_VALVE_OPENING_SET("policy_end_vrvh_fresh_air_degree", "freshAirValveOpeningSet", "新风阀开度设定","ACATAH"),
    RETURN_AIR_VALVE_OPENING_SET("policy_end_vrvh_return_air_degree", "returnAirValveOpeningSet", "回风阀开度设定","ACATAH"),
    SUPPLY_FAN_FREQ_SET("policy_end_vrvh_wind_freq", "supplyFanFreqSet", "送风机频率设定","ACATAH"),
    CHILL_WATER_VALVE_OPENING_SET_AH("policy_end_vrvv_valve_degree", "chillWaterValveOpeningSet", "冷水阀开度设定","ACATAH"),
    INDOOR_AIR_TEMP_SET_FC("policy_end_fcu_area_temp", "indoorAirTempSet", "服务区域目标温度设定","ACATFC"),

    PROJECT_POWER_ON("alg_project_power_on", "project_power_on", "最早开机时长设定（相对营业时间提前的最早时间）",""),
    PROJECT_POWER_OFF("alg_project_power_off", "project_power_off", "最早关闭时长设定（相对关店时间提前的最早时间）",""),

    COLD_HOT_PUMB_COUNT("policy_site_coldhot_machine_count", "chillerLNum", "空气源热泵控制组开启数量设定",""),
    COLDHOT_COLD_TEMP_SET("policy_site_coldhot_cold_temp_set", "chillWaterOutTempSet", "制冷供水温度设定",""),
    COLDHOT_WATER_TEMP("policy_site_coldhot_heat_temp_set", "heatWaterSupplyTempSet", "制冷供水温度设定",""),
    ;

    private String code;
    private String infoCode;
    private String name;
    private String classCode;

    public static String getCodeByInfoCode(String infoCode, String classCode) {
        for (ShellConfigAndInfoCodeEnum codeEnum : ShellConfigAndInfoCodeEnum.values()) {
            if (codeEnum.getInfoCode().equals(infoCode) &&
                    (StringUtils.isEmpty(classCode) || (StringUtils.isNotEmpty(classCode) && codeEnum.getClassCode().equals(classCode)))) {
                return codeEnum.getCode();
            }
        }
        return null;
    }
}
