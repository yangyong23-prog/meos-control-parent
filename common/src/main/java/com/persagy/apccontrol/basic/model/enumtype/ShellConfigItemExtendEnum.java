package com.persagy.apccontrol.basic.model.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: wxf
 * @create: 2025-05-07 19:00
 **/
@Getter
@AllArgsConstructor
public enum ShellConfigItemExtendEnum {
    ALG_AIR_COOLING_CALCULATE_DEADLINE("alg_air_cooling_calculate_deadline", "加机逻辑"),
    ALG_AIR_COOLING_CHILLER_RATE_MAX("alg_air_cooling_chiller_rate_max", "加机逻辑"),
    ALG_AIR_COOLING_ELE_IPLR_MAX("alg_air_cooling_ele_iplr_max", "加机逻辑"),
    ALG_AIR_COOLING_CEN_IPLR_MAX("alg_air_cooling_cen_iplr_max", "加机逻辑"),
    ALG_AIR_COOLING_SCR_IPLR_MAX("alg_air_cooling_scr_iplr_max", "加机逻辑"),
    ALG_AIR_COOLING_DUA_IPLR_MAX("alg_air_cooling_dua_iplr_max", "加机逻辑"),
    ALG_AIR_COOLING_GSH_PULR_MAX("alg_air_cooling_gsh_pulr_max", "加机逻辑"),
    ALG_AIR_COOLING_WSH_PULR_MAX("alg_air_cooling_wsh_pulr_max", "加机逻辑"),
    ALG_AIR_COOLING_ABS_IPLR_MAX("alg_air_cooling_abs_iplr_max", "加机逻辑"),
    ALG_AIR_COOLING_ADD_AVERAGE_P("alg_air_cooling_add_average_P", "加机逻辑"),
    ALG_AIR_COOLING_ADD_OTW_M("alg_air_cooling_add_otw_M", "加机逻辑"),
    ALG_AIR_COOLING_ADD_AVERAGE_M("alg_air_cooling_add_average_M", "加机逻辑"),
    ALG_AIR_COOLING_ADD_PRE_DURATION("alg_air_cooling_add_pre_duration", "加机逻辑"),
    ALG_AIR_COOLING_ADD_POST_DURATION("alg_air_cooling_add_post_duration", "加机逻辑"),
    ALG_AIR_COOLING_SPACE_LOW_DURATION("alg_air_cooling_space_low_duration", "加机逻辑"),

    ALG_AIR_COOLING_STOP_HOLD_DURATION("alg_air_cooling_stop_hold_duration", "间歇供冷加机"),
    ALG_AIR_COOLING_ADD_TMAX_DURATION("alg_air_cooling_add_tmax_duration", "间歇供冷加机"),

    ALG_AIR_COOLING_SUB_IPLR_MIN("alg_air_cooling_sub_iplr_min", "减机前置条件"),
    ALG_AIR_COOLING_EFC_IPLR_MIN("alg_air_cooling_efc_iplr_min", "减机前置条件"),
    ALG_AIR_COOLING_BEST_IPLR_THRESHOLD("alg_air_cooling_best_iplr_threshold", "减机前置条件"),
    ALG_AIR_COOLING_ONE_IPLR_THRESHOLD("alg_air_cooling_one_iplr_threshold", "减机前置条件"),
    ALG_AIR_COOLING_SUB_TIMING("alg_air_cooling_sub_timing", "减机前置条件"),
    ALG_AIR_COOLING_SUB_CHILL_DURATION("alg_air_cooling_sub_chill_duration", "减机前置条件"),
    ALG_AIR_COOLING_NO_SUB_CHILL_TIME("alg_air_cooling_no_sub_chill_time", "减机前置条件"),
    ALG_AIR_COOLING_MUL_IPLR_THRESHOLD("alg_air_cooling_mul_iplr_threshold", "减机前置条件"),
    ALG_AIR_COOLING_MUL_IPLR_DURATION("alg_air_cooling_mul_iplr_duration", "减机前置条件"),
    ALG_AIR_COOLING_NO_LOAD_M("alg_air_cooling_no_load_m", "减机前置条件"),
    ALG_AIR_COOLING_FREE_COOL_DIFF("alg_air_cooling_free_cool_diff", "减机前置条件"),
    ALG_AIR_COOLING_WEIGHT_AVG_TEMP_DIFF_M("alg_air_cooling_weight_avg_temp_diff_m", "减机前置条件"),


    ALG_AIR_COOLING_NO_STRA_MAX("alg_air_cooling_no_stra_max", "蓄冷开启阶段"),
    ALG_AIR_COOLING_MACHINE_OPEN_STRA("alg_air_cooling_machine_open_stra", "蓄冷开启阶段"),

    ALG_AIR_COOLING_STOP_STRA_MIN("alg_air_cooling_stop_stra_min", "蓄冷运行阶段"),
    ALG_AIR_COOLING_SUB_RELT_MAX("alg_air_cooling_sub_relt_max", "蓄冷运行阶段"),
    ALG_AIR_COOLING_NUM_CHANGE_DURATION("alg_air_cooling_num_change_duration", "蓄冷运行阶段"),

    ALG_AIR_COOLING_OPEN_RULE("alg_air_cooling_open_rule", "安全运行"),
    ALG_AIR_COOLING_MACHINE_MIX_ENABLED("alg_air_cooling_machine_mix_enabled", "安全运行"),
    ALG_AIR_COOLING_WATER_RETURN_MAX("alg_air_cooling_water_return_max", "安全运行"),
    ALG_AIR_COOLING_WATER_RETURN_DURATION("alg_air_cooling_water_return_duration", "安全运行"),
    ALG_AIR_COOLING_START_INTERVAL_RULE("alg_air_cooling_start_interval_rule", "安全运行"),
    ALG_AIR_COOLING_SHIELD_ALL_ENABLED("alg_air_cooling_shield_all_enabled", "安全运行"),
    ALG_AIR_COOLING_SHIELD_COUNT_ENABLED("alg_air_cooling_shield_count_enabled", "安全运行"),
    ALG_AIR_COOLING_FLOW_DEMAND_CONF("alg_air_cooling_flow_demand_conf", "安全运行"),

    ALG_AIR_COOLING_PUMP_DISH_INTER("alg_air_cooling_pump_dish_inter", "高效运行"),
    ALG_AIR_COOLING_SPLC_IMBA("alg_air_cooling_splc_imba", "高效运行"),
    ALG_AIR_COOLING_SPLH_IMBA("alg_air_cooling_splh_imba", "高效运行"),
    ALG_AIR_COOLING_FREE_START_TEMP_THRESHOLD("alg_air_cooling_free_start_temp_threshold","免费冷开启"),
    ALG_AIR_COOLING_FREE_OPEN_DURATION("alg_air_cooling_free_open_duration","免费冷开启"),
    ALG_AIR_COOLING_FREE_CLOSE_TEMP_THRESHOLD("alg_air_cooling_free_close_temp_threshold","免费冷关闭"),
    ALG_SPACE_TEMP_TARGET_CALC_SUB_MAX("alg_space_temp_target_calc_sub_max","供冷工况配置"),//空间温度目标值的计算方式温度过热门限减去固定值
    ALG_SPACE_TEMP_DEMAND_CALC_SUB_HOT("alg_space_temp_demand_calc_sub_hot","供冷工况配置"),
    ALG_SPACE_TEMP_COOL_CALC_ADD_COLD("alg_space_temp_cool_calc_add_cold","供冷工况配置"),
    ALG_SPACE_TEMP_TARGET_CALC_ADD_MIN("alg_space_temp_target_calc_add_min","供暖工况配置"),
    ALG_SPACE_TEMP_DEMAND_CALC_SUB_COLD("alg_space_temp_demand_calc_sub_cold","供暖工况配置"),
    ALG_SPACE_TEMP_HOT_CALC_SUB_HOT("alg_space_temp_hot_calc_sub_hot","供暖工况配置"),

    ALG_AIR_COOLING_PUMP_1_FREQ_RANGE("alg_air_cooling_pump_1_freq_range","冷冻水泵控制组（一次泵组）"),
    ALG_COOLING_PUMP_1_FREQ_RANGE("alg_cooling_pump_1_freq_range","冷冻水泵控制组（一次泵组）"),
    ALG_AIR_COOLING_PUMP_2_FREQ_RANGE("alg_air_cooling_pump_2_freq_range","冷冻水泵控制组（二次泵组）"),
    ALG_COOLING_PUMP_2_FREQ_RANGE("alg_cooling_pump_2_freq_range","冷冻水泵控制组（二次泵组）"),
    ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE("alg_air_cooling_pump_acppc_freq_range","冷热共用水泵控制组"),
    ALG_AIR_COOLING_PUMP_ACOPC_FREQ_RANGE("alg_air_cooling_pump_acopc_freq_range","冷却水泵控制组"),
    ALG_COOLING_PUMP_ACOPC_FREQ_RANGE("alg_cooling_pump_acopc_freq_range","冷却水泵控制组"),
    ALG_AIR_COOLING_PUMP_ACOTC_FREQ_RANGE("alg_air_cooling_pump_acotc_freq_range","冷却塔控制组"),
    ALG_COOLING_PUMP_ACOTC_FREQ_RANGE("alg_cooling_pump_acotc_freq_range","冷却塔控制组"),

    ALG_AIR_COOLING_HOT_ADD_FORBID_DURATION("alg_air_cooling_hot_add_forbid_duration","加机条件"),
    ALG_AIR_COOLING_HOT_ADD_LOAD_RATE_P("alg_air_cooling_hot_add_load_rate_p","加机条件"),
    ALG_AIR_COOLING_HOT_ADD_TEMP_M("alg_air_cooling_hot_add_temp_m","加机条件"),
    ALG_AIR_COOLING_HOT_ADD_TEMP_M_RANGE("alg_air_cooling_hot_add_temp_m_range","加机条件"),
    ALG_AIR_COOLING_HOT_ADD_TIMING("alg_air_cooling_hot_add_timing","加机条件"),
    ALG_AIR_COOLING_HOT_ADD_SPACE_LOW_TIME("alg_air_cooling_hot_add_space_low_time","加机条件"),
    ALG_AIR_COOLING_HOT_ADD_HOLD_TIME("alg_air_cooling_hot_add_hold_time","加机条件"),

    ALG_AIR_COOLING_HOT_STOP_HOLD_DURATION("alg_air_cooling_hot_stop_hold_duration","间歇供冷加机"),
    ALG_AIR_COOLING_HOT_ADD_TMAX_DURATION("alg_air_cooling_hot_add_tmax_duration","间歇供冷加机"),

    ALG_AIR_COOLING_HOT_NO_LOAD_RATE("alg_air_cooling_hot_no_load_rate", "减机条件"),
    ALG_AIR_COOLING_HOT_LOW_SUB("alg_air_cooling_hot_low_sub", "减机条件"),
    ALG_AIR_COOLING_HOT_AVER_TEMP("alg_air_cooling_hot_aver_temp", "减机条件"),
    ALG_AIR_COOLING_HOT_MUL_LIMIT("alg_air_cooling_hot_mul_limit", "减机条件"),
    ALG_AIR_COOLING_HOT_SUB_LIMIT_DUR("alg_air_cooling_hot_sub_limit_dur", "减机条件"),
    ALG_AIR_COOLING_HOT_COM_LIMIT_DUR("alg_air_cooling_hot_com_limit_dur", "减机条件"),
    ALG_AIR_COOLING_HOT_NO_SUB_CHILL_TIME("alg_air_cooling_hot_no_sub_chill_time", "减机条件"),
    ALG_AIR_COOLING_HOT_MUL_TIME("alg_air_cooling_hot_mul_time", "减机条件"),
    ALG_AIR_COOLING_HOT_SUB_CHILL_DURATION("alg_air_cooling_hot_sub_chill_duration", "减机条件"),
    ALG_AIR_COOLING_HOT_FREE_PLATE_EXCHANGER_CAPACITY("alg_air_cooling_hot_free_plate_exchanger_capacity", "减机条件"),

    ALG_AIR_COOLING_HOT_ADD_FORBID_DURATION_H("alg_air_cooling_hot_add_forbid_duration_h", "加机条件"),
    ALG_AIR_COOLING_HOT_ADD_LOAD_RATE_P_H("alg_air_cooling_hot_add_load_rate_p_h", "加机条件"),
    ALG_AIR_COOLING_HOT_ADD_TEMP_M_H("alg_air_cooling_hot_add_temp_m_h", "加机条件"),
    ALG_AIR_COOLING_HOT_ADD_TEMP_M_RANGE_H("alg_air_cooling_hot_add_temp_m_range_h", "加机条件"),
    ALG_AIR_COOLING_HOT_ADD_TIMING_H("alg_air_cooling_hot_add_timing_h", "加机条件"),
    ALG_AIR_COOLING_HOT_ADD_SPACE_LOW_TIME_H("alg_air_cooling_hot_add_space_low_time_h", "加机条件"),
    ALG_AIR_COOLING_HOT_ADD_HOLD_TIME_H("alg_air_cooling_hot_add_hold_time_h", "加机条件"),
    ALG_AIR_COOLING_HOT_ANTI_FREEZE_TEMP_DUR_H("alg_air_cooling_hot_anti_freeze_temp_dur_h", "加机条件"),

    ALG_AIR_COOLING_HOT_NO_LOAD_RATE_H("alg_air_cooling_hot_no_load_rate_h", "减机条件"),
    ALG_AIR_COOLING_HOT_LOW_SUB_H("alg_air_cooling_hot_low_sub_h", "减机条件"),
    ALG_AIR_COOLING_HOT_AVER_TEMP_H("alg_air_cooling_hot_aver_temp_h", "减机条件"),
    ALG_AIR_COOLING_HOT_MUL_LIMIT_H("alg_air_cooling_hot_mul_limit_h", "减机条件"),
    ALG_AIR_COOLING_HOT_SUB_LIMIT_DUR_H("alg_air_cooling_hot_sub_limit_dur_h", "减机条件"),
    ALG_AIR_COOLING_HOT_COM_LIMIT_DUR_H("alg_air_cooling_hot_com_limit_dur_h", "减机条件"),
    ALG_AIR_COOLING_HOT_NO_SUB_CHILL_TIME_H("alg_air_cooling_hot_no_sub_chill_time_h", "减机条件"),
    ALG_AIR_COOLING_HOT_MUL_TIME_H("alg_air_cooling_hot_mul_time_h", "减机条件"),
    ALG_AIR_COOLING_HOT_SUB_CHILL_DURATION_H("alg_air_cooling_hot_sub_chill_duration_h", "减机条件"),

    //ALG_AIR_COOLING_HOT_COOLING_WATER_RETURN_LIMIT("alg_air_cooling_hot_cooling_water_return_limit", "安全运行"),
    //ALG_AIR_COOLING_HOT_COOLING_WATER_RETURN_HOLD("alg_air_cooling_hot_cooling_water_return_hold", "安全运行"),

    ALG_AIR_COOLING_HOT_ZERO_HOST_DUR_H("alg_air_cooling_hot_zero_host_dur_h", "间歇供热"),
    ALG_AIR_COOLING_HOT_HEATING_LOW_TEMP_DUR_H("alg_air_cooling_hot_heating_low_temp_dur_h", "间歇供热"),

    /*
    ALG_AIR_COOLING_HOT_HEATING_DELTA_PRESS_HOLD_TIME("alg_air_cooling_hot_heating_delta_press_hold_time", "按压差设定控制"),
    ALG_AIR_COOLING_HOT_HEATING_DELTA_PRESS_LIMIT("alg_air_cooling_hot_heating_delta_press_limit", "按压差设定控制"),
    ALG_AIR_COOLING_HOT_HEATING_DELTA_PRESS_ADJUST("alg_air_cooling_hot_heating_delta_press_adjust", "按压差设定控制"),
    ALG_AIR_COOLING_HOT_HEATING_DELTA_PRESS_DEFAULT("alg_air_cooling_hot_heating_delta_press_default", "按压差设定控制"),
     */

    ALG_AIR_COOLING_HOT_HOST_UNIT_LIMIT_RULE("alg_air_cooling_hot_host_unit_limit_rule", "安全运行"),
    ALG_AIR_COOLING_HOT_SUPPORT_BIG_SMALL_HOST("alg_air_cooling_hot_support_big_small_host", "安全运行"),
    ALG_AIR_COOLING_HOT_HEAT_BALANCE_INTERVAL("alg_air_cooling_hot_heat_balance_interval", "高效运行"),
    ALG_AIR_COOLING_HOT_COOLING_IMBALANCE("alg_air_cooling_hot_cooling_imbalance", "高效运行"),
    ALG_AIR_COOLING_HOT_HEATING_IMBALANCE("alg_air_cooling_hot_heating_imbalance", "高效运行"),

    //通风控制逻辑条件输入-穹顶通风
    CONTROL_VENT_DOME_EXHAUST_OPEN_TEMP("control_vent_dome_exhaust_open_temp", "穹顶排风控制"),
    CONTROL_VENT_DOME_EXHAUST_OPEN_DURATION("control_vent_dome_exhaust_open_duration", "穹顶排风控制"),
    CONTROL_VENT_DOME_EXHAUST_CLOSE_TEMP("control_vent_dome_exhaust_close_temp", "穹顶排风控制"),
    CONTROL_VENT_DOME_EXHAUST_CLOSE_DURATION("control_vent_dome_exhaust_close_duration", "穹顶排风控制"),
    CONTROL_VENT_DOME_WINDOW_HOLD_DURATION("control_vent_dome_window_hold_duration", "电动气窗控制"),
    CONTROL_VENT_DOME_OFF_JUDGE_MODE("control_vent_dome_off_judge_mode", "非营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_OFF_WET_TEMP_DIFF("control_vent_dome_off_wet_temp_diff", "非营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_OFF_WET_LOW_DURATION("control_vent_dome_off_wet_low_duration", "非营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_OFF_WET_HIGH_DURATION("control_vent_dome_off_wet_high_duration", "非营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_OFF_DRY_TEMP_DIFF("control_vent_dome_off_dry_temp_diff", "非营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_OFF_DRY_LOW_DURATION("control_vent_dome_off_dry_low_duration", "非营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_OFF_DRY_HIGH_DURATION("control_vent_dome_off_dry_high_duration", "非营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_ON_JUDGE_MODE("control_vent_dome_on_judge_mode", "营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_ON_WET_TEMP_DIFF("control_vent_dome_on_wet_temp_diff", "营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_ON_WET_LOW_DURATION("control_vent_dome_on_wet_low_duration", "营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_ON_WET_HIGH_DURATION("control_vent_dome_on_wet_high_duration", "营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_ON_DRY_TEMP_DIFF("control_vent_dome_on_dry_temp_diff", "营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_ON_DRY_LOW_DURATION("control_vent_dome_on_dry_low_duration", "营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_ON_DRY_HIGH_DURATION("control_vent_dome_on_dry_high_duration", "营业时段室外空气状态判断"),
    CONTROL_VENT_DOME_WEATHER_RH_THRESHOLD("control_vent_dome_weather_rh_threshold", "恶劣天气判断"),
    CONTROL_VENT_DOME_WEATHER_RH_OVER_DURATION("control_vent_dome_weather_rh_over_duration", "恶劣天气判断"),
    CONTROL_VENT_DOME_WEATHER_CLEAR_DURATION("control_vent_dome_weather_clear_duration", "恶劣天气判断"),

    ;
    private static Map<String,ShellConfigItemExtendEnum> code2ObjectNameMap = new HashMap<>();
    static{
        for(ShellConfigItemExtendEnum shellConfigItemExtendEnum : ShellConfigItemExtendEnum.values()){
            code2ObjectNameMap.put(shellConfigItemExtendEnum.getCode(),shellConfigItemExtendEnum);
        }
    }
    private String code;
    private String tabObjectName;
    public static ShellConfigItemExtendEnum getByCode(String code){
        return code2ObjectNameMap.get(code);
    }
}