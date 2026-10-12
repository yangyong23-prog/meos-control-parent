package com.persagy.apccontrol.basic.model.enumtype;

import cn.hutool.core.bean.BeanUtil;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ShellConfigItemEnum {

    /**
     * 自动需求计算条件输入--算法条件输入
     */
    ALG_PROJECT_POWER_ON("alg", "group_alg_project", "算法条件输入", "alg_project_power_on", "最早开机时长设定（相对营业时间提前的最早时间）", "min", "120", 0, 0, 1440, "15的倍数，范围（0-1440）", "相对营业时间提前的最早时间T，即最早开机时间=营业时间-T", null, null, null),
    ALG_PROJECT_POWER_OFF("alg", "group_alg_project", "算法条件输入", "alg_project_power_off", "最早关闭时长设定（相对关店时间提前的最早时间）", "min", "120", 0, 0, 1440, "15的倍数，范围（0-1440）", "相对关店时间提前的最早时间T，即最早关闭时间=关店时间-T", null, null, null),

    ALG_COOLING_MACHINE_CHANGE_TIMING("alg", "group_alg_cooling", "算法条件输入", "alg_cooling_machine_change_timing", "冷机加减机动作后模式维持时间", "min", "60", 0, 0, 1440, "5的倍数，范围（0-1440）", "台数变化后需维持该策略不变的时间，过后才会进行加减机的再次调整", null, null, null),
    ALG_COOLING_MACHINE_MIX_ENABLED("alg", "group_alg_cooling", "算法条件输入", "alg_cooling_machine_mix_enabled", "是否支持同时开启大小机", "/", "是", -1, null, null, "", "", null, null, null),
    ALG_COOLING_MACHINE_LARGE_MAX("alg", "group_alg_cooling", "算法条件输入", "alg_cooling_machine_large_max", "大冷机允许运行台数上限", "台", "${large_cooler}", 0, 0, null, "范围（0-${large_cooler}）", "", null, null, null),
    ALG_COOLING_MACHINE_SMALL_MAX("alg", "group_alg_cooling", "算法条件输入", "alg_cooling_machine_small_max", "小冷机允许运行台数上限", "台", "${small_cooler}", 0, 0, null, "范围（0-${small_cooler}）", "", null, null, null),

    ALG_COOLING_HOT_MACHINE_CHANGE_TIMING("alg", "group_alg_cold_hot", "算法条件输入", "alg_cooling_machine_change_timing", "空气源热泵控制组加减机动作后模式维持时间", "min", "60", 0, 0, 1440, "5的倍数，范围（0-1440）", "台数变化后需维持该策略不变的时间，过后才会进行加减机的再次调整", null, null, null),
    ALG_COOLING_HOT_MACHINE_RUNNNG_MAX("alg", "group_alg_cold_hot", "算法条件输入", "alg_cooling_machine_running_max", "空气源热泵控制组允许运行数量上限", "台", "${count}", 0, 0, null, "范围（0-${count}）", "", null, null, null),

    /**
     * 自动需求计算条件输入--空间品质判断及需求跃变计算的条件输入
     */
    ALG_SPACE_ERROR_TIMING_S1("alg", "group_alg_demand_error", "空间品质判断及需求跃变计算的条件输入", "alg_space_error_timing_S1", "空间异常持续时间的判断（S1）", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "用于泵组温差/压差设定值计算，判断空间异常的持续时间", null, null, null),
    ALG_SPACE_ERROR_TIMING_S2("alg", "group_alg_demand_error", "空间品质判断及需求跃变计算的条件输入", "alg_space_error_timing_S2", "空间异常持续时间的判断（S2）", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "用于主机出水温度设定值计算，判断空间异常的持续时间", null, null, null),
    ALG_SPACE_ERROR_THRESHOLD("alg", "group_alg_demand_error", "空间品质判断及需求跃变计算的条件输入", "alg_space_error_threshold", "空间异常的阈值判定", "", "0.5", 1, 0, 10, "范围（0-10）", "用于设定空间温度偏差的阈值，来判断空间异常，该阈值判定适用于空间S1与S2，统一配置", null, null, null),
    ALG_SPACE_ERROR_SPEC_THRESHOLD("alg", "group_alg_demand_error", "空间品质判断及需求跃变计算的条件输入", "alg_space_error_spec_threshold", "空间无实时温度判断为特殊空间的持续时间判断（用于主机出水温度设定值计算的SS）", "min", "30", 1, 0, 9999, "范围（0-9999）", "用于主机出水温度设定值计算的SS", null, null, null),
    ALG_SPACE_SATISFACT_RATE("alg", "group_alg_demand_error", "空间品质判断及需求跃变计算的条件输入", "alg_space_satisfact_rate", "可调度空间不满足率的配置", "%", "10,30", 0, 0, 100, "范围（0-100），上限≥下限", "只适用于夏季，配置的是超上限或低于下限时要调节出水温度设定或供回水温差", null, null, null),
    ALG_RELATIVE_HUMIDITY_THRESHOLD("alg", "group_alg_demand_error", "空间品质判断及需求跃变计算的条件输入", "alg_relative_humidity_threshold", "相对湿度判断阈值", "%", "85", 0, 0, 100, "范围（0-100）", "用于设定相对湿度的阈值，来判断空间异常", null, null, null),

    ALG_COOLING_COSTS_PERCENT("alg", "group_alg_jump", "空间品质判断及需求跃变计算的条件输入", "alg_cooling_costs_percent", "空调成本占比设定", "%", "30,60", 0, 10, 90, "范围（10-90，且两个的差值≧20%），上限≥下限", "用于触发算法计算冷热源设备台数的时刻，该设定值与空调成本占比结合，用于判断是否需要触发算法计算", null, null, null),
    /**
     * 自动需求计算条件输入--空间品质判断及需求跃变计算的条件输入-空间品质判断及需求跃变计算的条件输入-供冷工况配置
     */
    ALG_SPACE_TEMP_TARGET_CALC_SUB_MAX("alg","group_alg_space_temp_set","空间品质判断及需求跃变计算的条件输入","alg_space_temp_target_calc_sub_max","空间温度目标值的计算方式：温度舒适区上限减去固定值","℃", "0.5", 1, 0, 10, "范围（0-10）", "当空间不被调度时，系统自动设定空间温度目标值的计算方式", null, null, null),
    ALG_SPACE_TEMP_DEMAND_CALC_SUB_HOT("alg","group_alg_space_temp_set","空间品质判断及需求跃变计算的条件输入","alg_space_temp_demand_calc_sub_hot","空间温度调度值的计算方式：温度过热门限减去固定值","℃", "0.5", 1, 0, 10, "范围（0-10）", "当空间被调度时，系统自动设定空间温度调度值的计算方式", null, null, null),
    ALG_SPACE_TEMP_COOL_CALC_ADD_COLD("alg","group_alg_space_temp_set","空间品质判断及需求跃变计算的条件输入","alg_space_temp_cool_calc_add_cold","空间温度蓄冷值的计算方式：温度过冷门限增加固定值","℃", "0.5", 1, 0, 10, "范围（0-10）", "当空间被调度时，系统自动设定空间温度蓄冷值的计算方式", null, null, null),
    /**
     * 自动需求计算条件输入--空间品质判断及需求跃变计算的条件输入-空间品质判断及需求跃变计算的条件输入-供暖工况配置
     */
    ALG_SPACE_TEMP_TARGET_CALC_ADD_MIN("alg","group_alg_space_temp_set","空间品质判断及需求跃变计算的条件输入","alg_space_temp_target_calc_add_min","空间温度目标值的计算方式：温度舒适区下限增加固定值","℃", "0.5", 1, 0, 10, "范围（0-10）", "当空间不被调度时，系统自动设定空间温度目标值的计算方式", null, null, null),
    ALG_SPACE_TEMP_DEMAND_CALC_SUB_COLD("alg","group_alg_space_temp_set","空间品质判断及需求跃变计算的条件输入","alg_space_temp_demand_calc_sub_cold","空间温度调度值的计算方式：温度过冷门限增加固定值","℃", "0.5", 1, 0, 10, "范围（0-10）", "当空间被调度时，系统自动设定空间温度调度值的计算方式", null, null, null),
    ALG_SPACE_TEMP_HOT_CALC_SUB_HOT("alg","group_alg_space_temp_set","空间品质判断及需求跃变计算的条件输入","alg_space_temp_hot_calc_sub_hot","空间温度蓄热值的计算方式：温度过热门限减去固定值","℃", "0.5", 1, 0, 10, "范围（0-10）", "当空间被调度时，系统自动设定空间温度蓄热值的计算方式", null, null, null),

    ALG_SPACE_RH_LOWER("alg", "group_alg_demand_error", "空间品质判断及需求跃变计算的条件输入", "alg_space_rh_lower", "判断空间潮湿需超过相对湿度舒适区上限持续时长", "min", "60", 0, 0, 1440, "5的倍数，范围（0-1440）", "室内相对湿度超过舒适区上限持续一段时间后，判断空间潮湿", null, null, null),
    ALG_SPACE_RH_THRESHOLD("alg", "group_alg_demand_error", "空间品质判断及需求跃变计算的条件输入", "alg_space_rh_threshold", "送风相对湿度设定值自动调节上下限", "%", "70,100", 0, 0, 100, "（0-100），上限≥下限", "", null, null, null),
    ALG_SPACE_ADJUST_TIME("alg", "group_alg_demand_error", "空间品质判断及需求跃变计算的条件输入", "alg_space_adjust_time", "送风相对湿度设定值调整后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "新指令发出后需要保持的时间", null, null, null),
    
    ALG_COOLING_PERIOD_COOL_DOWN_FACTOR("alg", "group_alg_advance_duration", "空间品质判断及需求跃变计算的条件输入", "alg_cooling_period_cool_down_factor", "供冷时段降温系数（计算供冷提前开启时长）", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, null, null),
    ALG_COOLING_PERIOD_WARM_UP_FACTOR("alg", "group_alg_advance_duration", "空间品质判断及需求跃变计算的条件输入", "alg_cooling_period_warm_up_factor", "供冷时段升温系数（计算供冷提前关闭时长）", "min", "45", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, null, null),
    ALG_HEATING_PERIOD_WARM_UP_FACTOR("alg", "group_alg_advance_duration", "空间品质判断及需求跃变计算的条件输入", "alg_heating_period_warm_up_factor", "供热时段升温系数（计算供热提前开启时长）", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, null, null),
    ALG_HEATING_PERIOD_COOL_DOWN_FACTOR("alg", "group_alg_advance_duration", "空间品质判断及需求跃变计算的条件输入", "alg_heating_period_cool_down_factor", "供热时段降温系数（计算供热提前关闭时长）", "min", "45", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, null, null),

    /**
     * 自动需求计算条件输入--冷站自动需求计算的条件输入
     */
    ALG_COOLING_ELE_IPLR_MAX("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_ele_iplr_max", "冷机负载率最大值", "%", "90", 1, 0, 100, "范围（0-100）", "如果无法获取到冷机负载率上限，取壳配置数值", null, null, null),
    ALG_COOLING_ADD_TIMING("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_add_timing", "满足加机判断条件的持续时长", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "设定满足加机条件需要持续的时长，即所有冷机满足条件并持续该时长后才进行加机操作", null, null, null),
    ALG_COOLING_ADD_AVERAGE_M("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_add_average_M", "判断加开大机还是小机的加权平均差值M的范围", "", "2,1", 1, 0, 10, "（0-10）", "M为所有冷机出水温度设定值与实际出水温度的差值的加权平均数\n超过加开大机限值，则输出加开大机；超过加开小机限值、但不超过大机限值，则输出加开小机；小于加开小机限值则不加机", null, null, null),
    ALG_COOLING_00_TIMING("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_00_timing", "间歇供冷-冷机0大0小持续时间的判断", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "用于冷机在间歇供冷0大0小的情况下持续多久需要加机的判断", null, null, null),
    ALG_COOLING_BRANCH_TEMP("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_branch_temp", "间歇供冷-支管供水温度阈值", "℃", "15", 0, 0, 99, "范围（0-99）", "用于冷机在间歇供冷0大0小的情况下泵组供水温度超过阈值需要加机的判断", null, null, null),
    ALG_COOLING_PUMP_TIMING("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_pump_timing", "间歇供冷-泵组连续不满足持续时长的判断", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "用于冷机在间歇供冷0大0小的情况下泵组供水温度不满足持续多久需要加机的判断", null, null, null),
    ALG_COOLING_SUB_TIMING("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_sub_timing", "满足减机判断条件的持续时长", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "设定满足减机条件需要持续的时长", null, null, null),
    ALG_COOLING_IPLR_THRESHOLD("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_iplr_threshold", "综合负载率阈值判断", "%", "70", 0, 0, 100, "范围（0-100）", "冷机的综合负载率低于阈值会触发减机判断", null, null, null),
    ALG_COOLING_IPLR_RATIONAL_THRESHOLD("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_iplr_rational_threshold", "综合负载率合理范围配置", "%", "0,85", 0, 0, 200, "范围（0-200），上限≥下限", "配置合理范围，用于判断是否能计算出来高效的组合", null, null, null),
    ALG_COOLING_LAST_COLD_MACH_THRESHOLD("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_last_cold_mach_threshold", "最后一台冷机的负载率阈值判断", "%", "50", 0, 0, 100, "范围（0-100）", "如果无法获取到最后一台冷机的负载率阈值，取壳配置数值", null, null, null),
    ALG_COOLING_IPLR_MAXPLRESET("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_iplr_maxplrset", "判断加机的平均负载率差值P的阈值", "%", "-3", 1, -100, 0, "范围（-100-0）", "用于判断冷机加机的前置条件之一，P为每台开启冷机的实际负载率与负载率最大值的差值的加权平均数，当P≥阈值则满足加机的负载率前置条件", null, null, null),
    ALG_COOLING_EVAP_CHILL_OUT_TEMP("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_evap_chill_out_temp", "判断加机的平均温度偏差阈值M", "℃", "1", 1, 0, 99, "范围（0-99）", "用于判断冷机加机的前置条件之一，M为每台开启冷机的出水温度设定值与实际出水温度的差值的加权平均数，当M≥阈值则满足加机的出水温度前置条件", null, null, null),
    ALG_COOLING_LAST_CHILL_S_NUM_THRESHOLD("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_last_chill_s_num_threshold", "小机判断停机的负载率", "%", "40", 0, 0, 100, "范围（0-100）", "判断小机停机需要达到的负载率阈值", null, null, null),
    ALG_COOLING_LAST_CHILL_L_NUM_THRESHOLD("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_last_chill_l_num_threshold", "大机判断停机的负载率", "%", "40", 0, 0, 100, "范围（0-100）", "判断大机停机需要达到的负载率阈值", null, null, null),
    ALG_COOLING_SUB_CHILL_CONTINUE_TIME("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_sub_chill_continue_time", "减机后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "减机后保持时间", null, null, null),
    ALG_COOLING_ADD_CHILL_CONTINUE_TIME("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_add_chill_continue_time", "加机后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "加机后需要保持的时间，该时间段内不会再次执行加机逻辑，避免连续加机", null, null, null),
    ALG_COOLING_UNABLE_SAME_TIME_COOL("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_unable_same_time_cool", "不能同时开启的冷机", null, null, 0, null, null, null, "最大开启数量为选中的冷机数量减1，如选中2台冷机则最多开启其中1台、选中3台冷机则最多开启其中2台", "MULTIPLE", "{\"relation\":{\"classCode\":[\"ACCCCC\",\"ACCCSC\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}}", null),
    ALG_COOLING_ENABLE_SAME_TIME_COOL("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_enable_same_time_cool", "是否支持同时开启大小机", "/", "是", -1, null, null, null, "", null, null, null),
    ALG_COOLING_CHILL_LOAD_RATE_THRESHOLD("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_chill_load_rate_threshold", "冷机负载率阈值初始判断", "%", "20", 0, 0, 100, "范围（0-100）", "防止冷机在刚开机的阶段影响减机逻辑", null, null, null),
    ALG_COOLING_START_INTERVAL_RULE("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_start_interval_rule", "冷机间隔开启限制规则",  "", "", 0, 0, 0, "", "", null, "{\"relation\":{\"classCode\":[\"ACCCCC\",\"ACCCSC\",\"ACCCGH\",\"ACCCWH\",\"ACCCAC\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}}", null),
    ALG_COOLING_SHIELD_ALL_ENABLED("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_shield_all_enabled", "屏蔽所有控制指令",  "", "false", 0, 0, 0, "", "", null, "[{\"name\":\"屏蔽\",\"value\":\"true\"},{\"name\":\"不屏蔽\",\"value\":\"false\"}]", null),
    ALG_COOLING_SHIELD_COUNT_ENABLED("alg", "group_alg_add_subtract", "冷站自动需求计算的条件输入", "alg_cooling_shield_count_enabled", "屏蔽台数整定逻辑",  "", "false", 0, 0, 0, "", "", null, "[{\"name\":\"屏蔽\",\"value\":\"true\"},{\"name\":\"不屏蔽\",\"value\":\"false\"}]", null),


    ALG_COOLING_OUT_TEMP_DEFAULT("alg", "group_alg_cold_out_temp", "冷站自动需求计算的条件输入", "alg_cooling_out_temp_default", "出水温度设定值的默认", "℃", "10", 1, 0, 99, "范围（0-99）", "默认的冷机出水温度设定值", null, null, null),
    ALG_COOLING_OUT_TEMP_THRESHOLD("alg", "group_alg_cold_out_temp", "冷站自动需求计算的条件输入", "alg_cooling_out_temp_threshold", "冷冻水出水温度设定值的上下限", "℃", "7,13", 0, 0, 99, "范围（0-99），上限≥下限", "根据不同类型系统配置自动需求的出水温度设定值的上下限(如大温差的系统，设定值可以达到6℃)", null, null, false),
    ALG_COOLING_OUT_TEMP_HIGH_TIMING("alg", "group_alg_cold_out_temp", "冷站自动需求计算的条件输入", "alg_cooling_out_temp_high_timing", "出水温度设定修改后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "新指令发出后需要保持的时间", null, null, null),
    ALG_COOLING_OUT_TEMP_OVERRUN_TIME("alg", "group_alg_cold_out_temp", "冷站自动需求计算的条件输入", "alg_cooling_out_temp_overrun_time", "供水温度超出标准可容忍时长", "min", "", 0, 0, 1440, "范围（0-1440）", "", null, null, false),
    ALG_COOLING_OUT_TEMP_SS_TEMP_SET_TIME("alg", "group_alg_cold_out_temp", "冷站自动需求计算的条件输入", "alg_cooling_out_temp_ss_temp_set_time", "特殊空间SS下的出水温度设定修改后保持时间", "min", "30", 0, 0, 9999, "范围（0-9999）", "特殊空间SS下的出水温度设定修改后保持时间", null, null, null),
    ALG_COOLING_OUT_TEMP_SS_TEMP_SET_DEFAULT_VALUE("alg", "group_alg_cold_out_temp", "冷站自动需求计算的条件输入", "alg_cooling_out_temp_ss_temp_set_default_value", "特殊空间SS下的出水温度默认设定值", "℃", "8", 0, 0, 99, "范围（0-99）", "特殊空间SS下的出水温度默认设定值", null, null, null),
    ALG_COOLING_OUT_TEMP_SS_TEMP_SET_DIFF_VALUE("alg", "group_alg_cold_out_temp", "冷站自动需求计算的条件输入", "alg_cooling_out_temp_ss_temp_set_diff_value", "特殊空间SS下的出水温度差值ΔT", "℃", "2", 0, 0, 99, "范围（0-99）", "特殊空间SS下的出水温度差值ΔT", null, null, null),

    ALG_COOLING_SYSTEM_TEMP_DIFF_DEFAULT("alg", "group_alg_temp_press_diff", "冷站自动需求计算的条件输入", "alg_cooling_system_temp_diff_default", "供回水温差设定值默认", "℃", "5", 1, 0, 99, "范围（0-99）", "默认的供回水温差设定值", null, null, null),
    ALG_COOLING_SYSTEM_TEMP_DIFF_THRESHOLD("alg", "group_alg_temp_press_diff", "冷站自动需求计算的条件输入", "alg_cooling_system_temp_diff_threshold", "冷冻水供回水温差设定值的上下限", "℃", "2,8", 0, 0, 99, "范围（0-99），上限≥下限", "根据不同类型系统配置自动需求的供回水温差设定值的上下限", null, null, false),
    ALG_COOLING_SYSTEM_TEMP_DIFF_HIGH_TIMING("alg", "group_alg_temp_press_diff", "冷站自动需求计算的条件输入", "alg_cooling_system_temp_diff_high_timing", "供回水温差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "新指令发出后需要保持的时间", null, null, null),
    ALG_COOLING_SYSTEM_PRESS_DIFF_DEFAULT("alg", "group_alg_temp_press_diff", "冷站自动需求计算的条件输入", "alg_cooling_system_press_diff_default", "供回水压差设定值默认", "kPa", "60", 1, 0, 999, "范围（0-999）", "默认的供回水压差设定值", null, null, null),
    ALG_COOLING_SYSTEM_PRESS_DIFF_UPDATE("alg", "group_alg_temp_press_diff", "冷站自动需求计算的条件输入", "alg_cooling_system_press_diff_update", "压差调整幅度的配置", "kPa", "10", 0, 0, 999, "范围（0-999）", "配置每次压差调整的幅度（不同压差设定值调整范围不同，比如最不利末端压差，可能配置的就只有2m，这时候可能需要按照5kPa来调）", null, null, null),
    ALG_COOLING_SYSTEM_PRESS_DIFF_THRESHOLD("alg", "group_alg_temp_press_diff", "冷站自动需求计算的条件输入", "alg_cooling_system_press_diff_threshold", "冷冻水供回水压差设定值的上下限", "kPa", "40,100", 0, 0, 999, "范围（0-999），上限≥下限", "根据不同类型系统配置自动需求的供回水压差设定值的上下限（不同的压差设定值不同，例如分集水器压差控制与最不利末端压差控制所需要的值不一样）", null, null, false),
    ALG_COOLING_SYSTEM_PRESS_DIFF_HIGH_TIMING("alg", "group_alg_temp_press_diff", "冷站自动需求计算的条件输入", "alg_cooling_system_press_diff_high_timing", "供回水压差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "新指令发出后需要保持的时间", null, null, null),


    ALG_COOLING_TEMP_DIFF_DEFAULT("alg", "group_alg_diff", "冷站自动需求计算的条件输入", "alg_cooling_temp_diff_default", "供回水温差设定值默认", "℃", "5", 1, 0, 99, "范围（0-99）", "默认的供回水温差设定值", null, null, null),
    ALG_COOLING_TEMP_DIFF_THRESHOLD("alg", "group_alg_diff", "冷站自动需求计算的条件输入", "alg_cooling_temp_diff_threshold", "冷冻水供回水温差设定值的上下限", "℃", "2,8", 0, 0, 99, "范围（0-99），上限≥下限", "根据不同类型系统配置自动需求的供回水温差设定值的上下限", null, null, false),
    ALG_COOLING_TEMP_DIFF_HIGH_TIMING("alg", "group_alg_diff", "冷站自动需求计算的条件输入", "alg_cooling_temp_diff_high_timing", "供回水温差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "新指令发出后需要保持的时间", null, null, null),
    ALG_COOLING_PRESS_DIFF_DEFAULT("alg", "group_alg_diff", "冷站自动需求计算的条件输入", "alg_cooling_press_diff_default", "供回水压差设定值默认", "kPa", "60", 1, 0, 999, "范围（0-999）", "默认的供回水压差设定值", null, null, null),
    ALG_COOLING_PRESS_DIFF_UPDATE("alg", "group_alg_diff", "冷站自动需求计算的条件输入", "alg_cooling_press_diff_update", "压差调整幅度的配置", "kPa", "10", 0, 0, 999, "范围（0-999）", "配置每次压差调整的幅度（不同压差设定值调整范围不同，比如最不利末端压差，可能配置的就只有2m，这时候可能需要按照5kPa来调）", null, null, null),
    ALG_COOLING_PRESS_DIFF_THRESHOLD("alg", "group_alg_diff", "冷站自动需求计算的条件输入", "alg_cooling_press_diff_threshold", "冷冻水供回水压差设定值的上下限", "kPa", "40,100", 0, 0, 999, "范围（0-999），上限≥下限", "根据不同类型系统配置自动需求的供回水压差设定值的上下限（不同的压差设定值不同，例如分集水器压差控制与最不利末端压差控制所需要的值不一样）", null, null, false),
    ALG_COOLING_PRESS_DIFF_HIGH_TIMING("alg", "group_alg_diff", "冷站自动需求计算的条件输入", "alg_cooling_press_diff_high_timing", "供回水压差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "新指令发出后需要保持的时间", null, null, null),
    ALG_COOLING_PRESS_DIFF_TEMP_THRESHOLD("alg", "group_alg_diff", "冷站自动需求计算的条件输入", "alg_cooling_press_diff_temp_threshold", "间歇供冷-支管供水温度阈值", "℃", "15", 1, 0, 99, "范围（0-99）", "默认的间歇供冷-支管供水温度阈值", null, null, null),
    ALG_COOLING_PRESS_DIFF_TEMP_UPPER("alg", "group_alg_diff", "冷站自动需求计算的条件输入", "alg_cooling_press_diff_temp_upper", "供水温度上限值", "℃", "13", 1, 0, 99, "范围（0-99）", "", null, null, false),
    ALG_COOLING_PRESS_DIFF_TEMP_OVERRUN_TIME("alg", "group_alg_diff", "冷站自动需求计算的条件输入", "alg_cooling_press_diff_temp_overrun_time", "供水温度超出标准可容忍时长", "min", "", 0, 0, 1440, "范围（0-1440）", "", null, null, false),

    //室外湿球温度配置
    ALG_COOLING_WET_BULB_TEMP_RANGE("alg", "group_alg_out_wet_ball_temp_accc", "冷站自动需求计算的条件输入", "alg_cooling_wet_bulb_temp_range", "室外湿球温度", "℃", "22,26", 1, 0, 99, "范围（0-99）", "", null, null, null),

    /** 自动需求计算条件输入--冷站冷源自动需求计算的条件输入 - 控制组频率约束*/
    //冷冻水泵控制组（一次泵组）
    ALG_COOLING_PUMP_1_FREQ_RANGE("alg", "group_alg_accc_freq", "冷站自动需求计算的条件输入", "alg_cooling_pump_1_freq_range", "频率调节范围", "Hz", "40,50", 1, 0, 99, "范围（0-99）", "", null, "{\"relation\":{\"classCode\":[\"ACCPC\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}}", null),

    //冷冻水泵控制组（二次泵组）
    ALG_COOLING_PUMP_2_FREQ_RANGE("alg", "group_alg_accc_freq", "冷站自动需求计算的条件输入", "alg_cooling_pump_2_freq_range", "频率调节范围", "Hz", "40,50", 1, 0, 99, "范围（0-99）", "", null, "{\"relation\":{\"classCode\":[\"ACCPC\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}}", null),

    //冷却水泵控制组
    ALG_COOLING_PUMP_ACOPC_FREQ_RANGE("alg", "group_alg_accc_freq", "冷站自动需求计算的条件输入", "alg_cooling_pump_acopc_freq_range", "频率调节范围", "Hz", "40,50", 1, 0, 99, "范围（0-99）", "", null, "{\"relation\":{\"classCode\":[\"ACOPC\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}}", null),

    //冷却塔控制组
    ALG_COOLING_PUMP_ACOTC_FREQ_RANGE("alg", "group_alg_accc_freq", "冷站自动需求计算的条件输入", "alg_cooling_pump_acotc_freq_range", "频率调节范围", "Hz", "40,50", 1, 0, 99, "范围（0-99）", "", null, "{\"relation\":{\"classCode\":[\"ACOTC\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}}", null),

    /**
     * 自动需求计算条件输入--冷热站自动需求计算的条件输入
     */
    ALG_COLDHOT_ELE_IPLR_MAX("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_ele_iplr_max", "主机负载率最大值", "%", "90", 1, 0, 100, "范围（0-100）", "如果无法获取到主机负载率上限，取壳配置数值", null, null, null),
    ALG_COLDHOT_ADD_TIMING("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_add_timing", "满足加机判断条件的持续时长", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "设定满足加机条件需要持续的时长，即所有主机满足条件并持续该时长后才进行加机操作", null, null, null),
    ALG_COLDHOT_ADD_KEEP_TIMING("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_add_keep_timing", "加机后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "加机后需要保持的时间，该时间段内不会再次执行加机逻辑，避免连续加机", null, null, null),
    ALG_COLDHOT_ADD_AVERAGE_M("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_add_average_m", "判断加开大机还是小机的加权平均差值M的范围", "", "2,1", 1, 0, 10, "（0-10）", "M为所有主机出水温度设定值与实际出水温度的差值的加权平均数\n" +
            "超过加开大机限值，则输出加开大机；超过加开小机限值、但不超过大机限值，则输出加开小机；小于加开小机限值则不加机", null, null, null),
    ALG_COLDHOT_00_TIMING("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_00_timing", "间歇供冷-主机0大0小持续时间的判断", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "用于主机在间歇供冷0大0小的情况下持续多久需要加机的判断", null, null, null),
    ALG_COLDHOT_BRANCH_TEMP("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_branch_temp", "间歇供冷-支管供水温度阈值", "℃", "15", 0, 0, 99, "范围（0-99）", "用于主机在间歇供冷0大0小的情况下泵组供水温度超过阈值需要加机的判断", null, null, null),
    ALG_COLDHOT_PUMP_TIMING("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_pump_timing", "间歇供冷-泵组连续不满足持续时长的判断", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "用于主机在间歇供冷0大0小的情况下泵组供水温度不满足持续多久需要加机的判断", null, null, null),
    ALG_COLDHOT_SUB_TIMING("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_sub_timing", "满足减机判断条件的持续时长", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "设定满足减机条件需要持续的时长", null, null, null),
    ALG_COLDHOT_SUB_KEEP_TIMING("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_sub_keep_timing", "减机后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "减机后保持时间", null, null, null),
    ALG_COLDHOT_IPLR_THRESHOLD("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_iplr_threshold", "综合负载率阈值判断", "%", "70", 0, 0, 100, "范围（0-100）", "主机的综合负载率低于阈值会触发减机判断", null, null, null),
    ALG_COLDHOT_IPLR_RATIONAL_THRESHOLD("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_iplr_rational_threshold", "综合负载率合理范围配置", "%", "0,85", 0, 0, 200, "范围（0-200），上限≥下限", "配置合理范围，用于判断是否能计算出来高效的组合", null, null, null),
    ALG_COLDHOT_ENABLE_SAME_TIME_COOL("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_enable_same_time_cool", "是否支持同时开启大小机", "/", "是", -1, null, null, null, "", null, null, null),
    ALG_COLDHOT_CHILL_LOAD_RATE_THRESHOLD("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_chill_load_rate_threshold", "主机负载率阈值初始判断", "%", "20", 0, 0, 100, "范围（0-100）", "防止冷机在刚开机的阶段影响减机逻辑", null, null, null),
    ALG_COLDHOT_ADD_CHILL_AVG_LOAD_RATE_THRESHOLD("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_add_chill_avg_load_rate_threshold", "判断加机的平均负载率差值P的阈值", "%", "-3", 1, -100, 0, "范围（-100-0）", "用于判断冷机加机的前置条件之一，P为每台开启冷机的实际负载率与负载率最大值的差值的加权平均数，当P≥阈值则满足加机的负载率前置条件", null, null, null),
    ALG_COLDHOT_ADD_CHILL_AVG_TEMP_THRESHOLD("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_add_chill_avg_temp_threshold", "判断加机的平均温度偏差阈值M", "℃", "1", 1, 0, 99, "范围（0-99）", "用于判断冷机加机的前置条件之一，M为每台开启冷机的出水温度设定值与实际出水温度的差值的加权平均数，当M≥阈值则满足加机的出水温度前置条件", null, null, null),
    ALG_COLDHOT_SUBTRACT_CHILL_SMALL_LOAD_RATE("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_subtract_chill_small_load_rate", "小机判断停机的负载率", "%", "40", 0, 0, 100, "范围（0-100）", "判断小机停机需要达到的负载率阈值", null, null, null),
    ALG_COLDHOT_SUBTRACT_CHILL_LARGE_LOAD_RATE("alg", "group_alg_add_subtract_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_subtract_chill_large_load_rate", "大机判断停机的负载率", "%", "40", 0, 0, 100, "范围（0-100）", "判断大机停机需要达到的负载率阈值", null, null, null),

    ALG_COLDHOT_ELE_IPLR_MAX_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_ele_iplr_max_heat", "主机负载率最大值", "%", "90", 1, 0, 100, "范围（0-100）", "如果无法获取到主机负载率上限，取壳配置数值", null, null, null),
    ALG_COLDHOT_ADD_KEEP_TIMING_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_add_keep_timing_heat", "加机后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "加机后需要保持的时间，该时间段内不会再次执行加机逻辑，避免连续加机", null, null, null),
    ALG_COLDHOT_ADD_TIMING_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_add_timing_heat", "满足加机判断条件的持续时长", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "设定满足加机条件需要持续的时长，即所有主机满足条件并持续该时长后才进行加机操作", null, null, null),
    ALG_COLDHOT_ADD_AVERAGE_M_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_add_average_M_heat", "判断加开大机还是小机的加权平均差值M的范围", "", "-5,-2", 1, -10, 0, "范围（-10-0）", "M为所有主机出水温度设定值与实际出水温度的差值的加权平均数\n超过加开大机限值，则输出加开大机；超过加开小机限值、但不超过大机限值，则输出加开小机；小于加开小机限值则不加机", null, null, null),
    ALG_COLDHOT_00_TIMING_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_00_timing_heat", "间歇供热-主机0大0小持续时间的判断", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "用于主机在间歇供热0大0小的情况下持续多久需要加机的判断", null, null, null),
    ALG_COLDHOT_BRANCH_TEMP_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_branch_temp_heat", "间歇供热-支管供水温度阈值", "℃", "30", 0, 0, 99, "范围（0-99）", "用于主机在间歇供热0大0小的情况下泵组供水温度超过阈值需要加机的判断", null, null, null),
    ALG_COLDHOT_PUMP_TIMING_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_pump_timing_heat", "间歇供热-泵组连续不满足持续时长的判断", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "用于主机在间歇供热0大0小的情况下泵组供水温度不满足持续多久需要加机的判断", null, null, null),
    ALG_COLDHOT_SUB_TIMING_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_sub_timing_heat", "满足减机判断条件的持续时长", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "设定满足减机条件需要持续的时长", null, null, null),
    ALG_COLDHOT_SUB_KEEP_TIMING_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_sub_keep_timing_heat", "减机后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "减机后保持时间", null, null, null),
    ALG_COLDHOT_IPLR_THRESHOLD_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_iplr_threshold_heat", "综合负载率阈值判断", "%", "70", 0, 0, 100, "范围（0-100）", "主机的综合负载率低于阈值会触发减机判断", null, null, null),
    ALG_COLDHOT_IPLR_RATIONAL_THRESHOLD_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_iplr_rational_threshold_heat", "综合负载率合理范围配置", "%", "0,85", 0, 0, 200, "范围（0-200），上限≥下限", "配置合理范围，用于判断是否能计算出来高效的组合", null, null, null),
    ALG_COLDHOT_ENABLE_SAME_TIME_COOL_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_enable_same_time_cool_heat", "是否支持同时开启大小机", "/", "是", -1, null, null, null, "", null, null, null),
    ALG_COLDHOT_CHILL_LOAD_RATE_THRESHOLD_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_chill_load_rate_threshold_heat", "主机负载率阈值初始判断", "%", "20", 0, 0, 100, "范围（0-100）", "防止冷机在刚开机的阶段影响减机逻辑", null, null, null),
    ALG_COLDHOT_ADD_CHILL_AVG_LOAD_RATE_THRESHOLD_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_add_chill_avg_load_rate_threshold_heat", "判断加机的平均负载率差值P的阈值", "%", "-3", 1, -100, 0, "范围（-100-0）", "用于判断冷机加机的前置条件之一，P为每台开启冷机的实际负载率与负载率最大值的差值的加权平均数，当P≥阈值则满足加机的负载率前置条件", null, null, null),
    ALG_COLDHOT_SUPPLY_TEMP_SET_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_supply_temp_set_heat", "判断加机的平均温度偏差阈值M", "℃", "-2", 1, -99, 99, "范围（-99-99）", "用于判断冷机加机的前置条件之一，M为每台开启冷机的出水温度设定值与实际出水温度的差值的加权平均数，当M≥阈值则满足加机的出水温度前置条件", null, null, null),
    ALG_COLDHOT_SUBTRACT_CHILL_SMALL_LOAD_RATE_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_subtract_chill_small_load_rate_heat", "小机判断停机的负载率", "%", "40", 0, 0, 100, "范围（0-100）", "判断小机停机需要达到的负载率阈值", null, null, null),
    ALG_COLDHOT_SUBTRACT_CHILL_LARGE_LOAD_RATE_HEAT("alg", "group_alg_add_subtract_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_subtract_chill_large_load_rate_heat", "大机判断停机的负载率", "%", "40", 0, 0, 100, "范围（0-100）", "判断大机停机需要达到的负载率阈值", null, null, null),

    ALG_COLDHOT_OUT_TEMP_DEFAULT("alg", "group_alg_cold_out_temp_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_out_temp_default", "出水温度设定值的默认", "℃", "10", 1, 0, 99, "范围（0-99）", "默认的主机出水温度设定值", null, null, null),
    ALG_COLDHOT_OUT_TEMP_THRESHOLD("alg", "group_alg_cold_out_temp_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_out_temp_threshold", "冷冻水出水温度设定值的上下限", "℃", "7,13", 0, 0, 99, "范围（0-99），上限≥下限", "根据不同类型系统配置自动需求的出水温度设定值的上下限", null, null, false),
    ALG_COLDHOT_OUT_TEMP_HIGH_TIMING("alg", "group_alg_cold_out_temp_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_out_temp_high_timing", "出水温度设定修改后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "新指令发出后需要保持的时间", null, null, null),
    ALG_COLDHOT_OUT_TEMP_OVERRUN_TIME("alg", "group_alg_cold_out_temp_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_out_temp_overtrun_time", "供水温度超出标准可容忍时长", "min", "", 0, 0, 1440, "范围（0-1440）", "", null, null, false),

    ALG_COLDHOT_OUT_TEMP_DEFAULT_HEAT("alg", "group_alg_cold_out_temp_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_out_temp_default_heat", "出水温度设定值的默认", "℃", "40", 1, 0, 99, "范围（0-99）", "默认的主机出水温度设定值", null, null, null),
    ALG_COLDHOT_OUT_TEMP_THRESHOLD_HEAT("alg", "group_alg_cold_out_temp_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_out_temp_threshold_heat", "冷冻水出水温度设定值的上下限", "℃", "30,50", 0, 0, 99, "范围（0-99），上限≥下限", "根据不同类型系统配置自动需求的出水温度设定值的上下限", null, null, false),
    ALG_COLDHOT_OUT_TEMP_HIGH_TIMING_HEAT("alg", "group_alg_cold_out_temp_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_out_temp_high_timing_heat", "出水温度设定修改后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "新指令发出后需要保持的时间", null, null, null),
    ALG_COLDHOT_OUT_TEMP_OVERRUN_TIME_HEAT("alg", "group_alg_cold_out_temp_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_out_temp_overtrun_time_heat", "供水温度超出标准可容忍时长", "min", "", 0, 0, 1440, "范围（0-1440）", "", null, null, false),

    ALG_COLDHOT_TEMP_DIFF_DEFAULT("alg", "group_alg_diff_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_temp_diff_default", "供回水温差设定值默认", "℃", "5", 1, 0, 99, "范围（0-99）", "默认的供回水温差设定值", null, null, null),
    ALG_COLDHOT_TEMP_DIFF_THRESHOLD("alg", "group_alg_diff_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_temp_diff_threshold", "冷冻水供回水温差设定值的上下限", "℃", "2,8", 0, 0, 99, "范围（0-99），上限≥下限", "根据不同类型系统配置自动需求的供回水温差设定值的上下限", null, null, false),
    ALG_COLDHOT_TEMP_DIFF_HIGH_TIMING("alg", "group_alg_diff_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_temp_diff_high_timing", "供回水温差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "新指令发出后需要保持的时间", null, null, null),
    ALG_COLDHOT_PRESS_DIFF_DEFAULT("alg", "group_alg_diff_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_press_diff_default", "供回水压差设定值默认", "kPa", "60", 1, 0, 999, "范围（0-999）", "默认的供回水压差设定值", null, null, null),
    ALG_COLDHOT_PRESS_DIFF_UPDATE("alg", "group_alg_diff_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_press_diff_update", "压差调整幅度的配置", "kPa", "10", 0, 0, 999, "范围（0-999）", "配置每次压差调整的幅度（不同压差设定值调整范围不同，比如最不利末端压差，可能配置的就只有2m，这时候可能需要按照5kPa来调）", null, null, null),
    ALG_COLDHOT_PRESS_DIFF_THRESHOLD("alg", "group_alg_diff_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_press_diff_threshold", "冷冻水供回水压差设定值的上下限", "kPa", "40,100", 0, 0, 999, "范围（0-999），上限≥下限", "根据不同类型系统配置自动需求的供回水压差设定值的上下限（不同的压差设定值不同，例如分集水器压差控制与最不利末端压差控制所需要的值不一样）", null, null, false),
    ALG_COLDHOT_PRESS_DIFF_HIGH_TIMING("alg", "group_alg_diff_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_press_diff_high_timing", "供回水压差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "新指令发出后需要保持的时间", null, null, null),
    ALG_COLDHOT_PRESS_DIFF_TEMP_UPPER("alg", "group_alg_diff_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_press_diff_temp_upper", "供水温度上限值", "℃", "13", 1, 0, 99, "范围（0-99）", "", null, null, false),
    ALG_COLDHOT_PRESS_DIFF_TEMP_OVERRUN_TIME("alg", "group_alg_diff_cold", "冷热站自动需求计算的条件输入", "alg_coldhot_press_diff_temp_overrun_time", "供水温度超出标准可容忍时长", "min", "", 0, 0, 1440, "范围（0-1440）", "", null, null, false),


    ALG_COLDHOT_SYSTEM_COOL_TEMP_DIFF_DEFAULT("alg", "group_alg_coldhot_temp_press_diff_cool", "冷热站自动需求计算的条件输入", "alg_coldhot_system_cool_temp_diff_default", "供回水温差设定值默认", "℃", "5", 1, 0, 99, "范围（0-99）", "未作调整时默认的供回水温差设定值", null, null, null),
    ALG_COLDHOT_SYSTEM_COOL_TEMP_DIFF_THRESHOLD("alg", "group_alg_coldhot_temp_press_diff_cool", "冷热站自动需求计算的条件输入", "alg_coldhot_system_cool_temp_diff_threshold", "冷冻水供回水温差设定值的上下限", "℃", "2,8", 0, 0, 99, "范围（0-99），上限≥下限", "泵组供回水温差自动调节的范围限制", null, null, false),
    ALG_COLDHOT_SYSTEM_COOL_TEMP_DIFF_HIGH_TIMING("alg", "group_alg_coldhot_temp_press_diff_cool", "冷热站自动需求计算的条件输入", "alg_coldhot_system_cool_temp_diff_high_timing", "供回水温差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "泵组供回水温差调节后需要保持的时间，该时间段内不会再次调节", null, null, null),
    ALG_COLDHOT_SYSTEM_COOL_PRESS_DIFF_DEFAULT("alg", "group_alg_coldhot_temp_press_diff_cool", "冷热站自动需求计算的条件输入", "alg_coldhot_system_cool_press_diff_default", "供回水压差设定值默认", "kPa", "60", 1, 0, 999, "范围（0-999）", "未作调整时默认的泵组供回水压差设定值", null, null, null),
    ALG_COLDHOT_SYSTEM_COOL_PRESS_DIFF_UPDATE("alg", "group_alg_coldhot_temp_press_diff_cool", "冷热站自动需求计算的条件输入", "alg_coldhot_system_cool_press_diff_update", "压差调整幅度的配置", "kPa", "10", 0, 0, 999, "范围（0-999）", "每次压差调整时的幅度，即每次增加或减少该数值", null, null, null),
    ALG_COLDHOT_SYSTEM_COOL_PRESS_DIFF_THRESHOLD("alg", "group_alg_coldhot_temp_press_diff_cool", "冷热站自动需求计算的条件输入", "alg_coldhot_system_cool_press_diff_threshold", "冷冻水供回水压差设定值的上下限", "kPa", "40,100", 0, 0, 999, "范围（0-999），上限≥下限", "泵组供回水压差自动调节的范围限制", null, null, false),
    ALG_COLDHOT_SYSTEM_COOL_PRESS_DIFF_HIGH_TIMING("alg", "group_alg_coldhot_temp_press_diff_cool", "冷热站自动需求计算的条件输入", "alg_coldhot_system_cool_press_diff_high_timing", "供回水压差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "泵组供回水压差调节后需要保持的时间，该时间段内不会再次调节", null, null, null),


    ALG_COLDHOT_SYSTEM_HOT_TEMP_DIFF_DEFAULT("alg", "group_alg_coldhot_temp_press_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_system_hot_temp_diff_default", "供回水温差设定值默认", "℃", "10", 1, 0, 99, "范围（0-99）", "未作调整时默认的供回水温差设定值", null, null, null),
    ALG_COLDHOT_SYSTEM_HOT_TEMP_DIFF_THRESHOLD("alg", "group_alg_coldhot_temp_press_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_system_hot_temp_diff_threshold", "冷冻水供回水温差设定值的上下限", "℃", "5,15", 0, 0, 99, "范围（0-99），上限≥下限", "泵组供回水温差自动调节的范围限制", null, null, false),
    ALG_COLDHOT_SYSTEM_HOT_TEMP_DIFF_HIGH_TIMING("alg", "group_alg_coldhot_temp_press_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_system_hot_temp_diff_high_timing", "供回水温差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "泵组供回水温差调节后需要保持的时间，该时间段内不会再次调节", null, null, null),
    ALG_COLDHOT_SYSTEM_HOT_PRESS_DIFF_DEFAULT("alg", "group_alg_coldhot_temp_press_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_system_hot_press_diff_default", "供回水压差设定值默认", "kPa", "60", 1, 0, 999, "范围（0-999）", "未作调整时默认的泵组供回水压差设定值", null, null, null),
    ALG_COLDHOT_SYSTEM_HOT_PRESS_DIFF_UPDATE("alg", "group_alg_coldhot_temp_press_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_system_hot_press_diff_update", "压差调整幅度的配置", "kPa", "10", 0, 0, 999, "范围（0-999）", "每次压差调整时的幅度，即每次增加或减少该数值", null, null, null),
    ALG_COLDHOT_SYSTEM_HOT_PRESS_DIFF_THRESHOLD("alg", "group_alg_coldhot_temp_press_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_system_hot_press_diff_threshold", "冷冻水供回水压差设定值的上下限", "kPa", "40,100", 0, 0, 999, "范围（0-999），上限≥下限", "泵组供回水压差自动调节的范围限制", null, null, false),
    ALG_COLDHOT_SYSTEM_HOT_PRESS_DIFF_HIGH_TIMING("alg", "group_alg_coldhot_temp_press_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_system_hot_press_diff_high_timing", "供回水压差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "泵组供回水压差调节后需要保持的时间，该时间段内不会再次调节", null, null, null),

    ALG_COLDHOT_TEMP_DIFF_DEFAULT_HEAT("alg", "group_alg_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_temp_diff_default_heat", "供回水温差设定值默认", "℃", "10", 1, 0, 99, "范围（0-99）", "默认的供回水温差设定值", null, null, null),
    ALG_COLDHOT_TEMP_DIFF_THRESHOLD_HEAT("alg", "group_alg_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_temp_diff_threshold_heat", "冷冻水供回水温差设定值的上下限", "℃", "5,15", 0, 0, 99, "范围（0-99），上限≥下限", "根据不同类型系统配置自动需求的供回水温差设定值的上下限", null, null, false),
    ALG_COLDHOT_TEMP_DIFF_HIGH_TIMING_HEAT("alg", "group_alg_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_temp_diff_high_timing_heat", "供回水温差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "新指令发出后需要保持的时间", null, null, null),
    ALG_COLDHOT_PRESS_DIFF_DEFAULT_HEAT("alg", "group_alg_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_press_diff_default_heat", "供回水压差设定值默认", "kPa", "60", 1, 0, 999, "范围（0-999）", "默认的供回水压差设定值", null, null, null),
    ALG_COLDHOT_PRESS_DIFF_UPDATE_HEAT("alg", "group_alg_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_press_diff_update_heat", "压差调整幅度的配置", "kPa", "10", 0, 0, 999, "范围（0-999）", "配置每次压差调整的幅度（不同压差设定值调整范围不同，比如最不利末端压差，可能配置的就只有2m，这时候可能需要按照5kPa来调）", null, null, null),
    ALG_COLDHOT_PRESS_DIFF_THRESHOLD_HEAT("alg", "group_alg_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_press_diff_threshold_heat", "冷冻水供回水压差设定值的上下限", "kPa", "40,100", 0, 0, 999, "范围（0-999），上限≥下限", "根据不同类型系统配置自动需求的供回水压差设定值的上下限（不同的压差设定值不同，例如分集水器压差控制与最不利末端压差控制所需要的值不一样）", null, null, false),
    ALG_COLDHOT_PRESS_DIFF_HIGH_TIMING_HEAT("alg", "group_alg_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_press_diff_high_timing_heat", "供回水压差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "新指令发出后需要保持的时间", null, null, null),
    ALG_COLDHOT_PRESS_DIFF_TEMP_UPPER_HEAT("alg", "group_alg_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_press_diff_temp_upper_heat", "供水温度下限值", "℃", "13", 1, 0, 99, "范围（0-99）", "", null, null, false),
    ALG_COLDHOT_PRESS_DIFF_TEMP_OVERRUN_TIME_HEAT("alg", "group_alg_diff_hot", "冷热站自动需求计算的条件输入", "alg_coldhot_press_diff_temp_overrun_time_heat", "供水温度超出标准可容忍时长", "min", "", 0, 0, 1440, "范围（0-1440）", "", null, null, false),


    //  热站 ======================================
    ALG_HOT_TEMP_DEFAULT("alg", "group_alg_hot_temp_system", "热站自动需求计算条件输入", "alg_hot_temp_default", "出水温度默认设定值", "℃", "40", 1, 0, 99, "范围（0-99）", "未作调整时默认的出水温度设定值", null, null, null),
    ALG_HOT_TEMP_DEFAULT_ADJUST_RANGE("alg", "group_alg_hot_temp_system", "热站自动需求计算条件输入", "alg_hot_temp_default_adjust_range", "出水温度调整幅度的配置", "℃", "5", 1, 0, 99, "范围（0-99）", "每次出水温度调整时的幅度，即每次增加或减少该数值", null, null, null),
    ALG_HOT_TEMP_DEFAULT_MAX_MIN("alg", "group_alg_hot_temp_system", "热站自动需求计算条件输入", "alg_hot_temp_default_max_min", "出水温度设定值上下限", "℃", "30,50", 1, 0, 99, "范围（0-99），上限≥下限", "出水温度自动调节的范围限制", null, null, false),
    ALG_HOT_TEMP_DEFAULT_OVERRUN_TIME("alg", "group_alg_hot_temp_system", "热站自动需求计算条件输入", "alg_hot_temp_default_overrun_time", "供水温度超出标准可容忍时长", "min", "", 0, 0, 1440, "范围（0-1440）", "", null, null, false),
    ALG_HOT_TEMP_DEFAULT_KEEP_TIME("alg", "group_alg_hot_temp_system", "热站自动需求计算条件输入", "alg_hot_temp_default_keep_time", "出水温度设定修改后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "出水温度调节后需要保持的时间，该时间段内不会再次调节", null, null, null),
    ALG_HOT_TEMP_DEFAULT_SS_TEMP_SET_TIME("alg", "group_alg_hot_temp_system", "热站自动需求计算条件输入", "alg_hot_temp_default_ss_temp_set_time", "特殊空间SS下的出水温度设定修改后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "判断为特殊空间SS的情况下，出水温度调节后需要保持的时间，该时间段内不会再次调节", null, null, null),
    ALG_HOT_TEMP_DEFAULT_SS_TEMP_SET_DEFAULT_VALUE("alg", "group_alg_hot_temp_system", "热站自动需求计算条件输入", "alg_hot_temp_default_ss_temp_set_default_value", "特殊空间SS下的出水温度默认设定值", "℃", "35", 0, 0, 99, "范围（0-99）", "判断为特殊空间SS的情况下，设置的默认出水温度设定值", null, null, null),
    ALG_HOT_TEMP_DEFAULT_SS_TEMP_SET_DIFF_VALUE("alg", "group_alg_hot_temp_system", "热站自动需求计算条件输入", "alg_hot_temp_default_ss_temp_set_diff_value", "特殊空间SS下的出水温度差值ΔT", "℃", "5", 0, 0, 99, "范围（0-99）", "判断为特殊空间SS的情况下，是否能直接下发默认的出水温度设定值：如果默认设定值-当前出水温度设定值≤ΔT，则直接下发默认的出水温度设定值；如果默认设定值-当前出水温度设定值>ΔT，则下发当前设定值-5", null, null, null),

    ALG_HOT_DETA_TEMP_PRESS_TEMP_DEFAULT("alg", "group_alg_hot_deta_temp_press_system", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_temp_default", "供回水温差默认设定值", "℃", "10", 1, 0, 99, "范围（0-99）", "未作调整时默认的供回水温差设定值", null, null, null),
    ALG_HOT_DETA_TEMP_PRESS_TEMP_ADJUST_RANGE("alg", "group_alg_hot_deta_temp_press_system", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_temp_adjust_range", "温差调整幅度的配置", "℃", "2", 1, 0, 99, "范围（0-99）", "每次温差调整时的幅度，即每次增加或减少该数值", null, null, null),
    ALG_HOT_DETA_TEMP_PRESS_TEMP_MAX_MIN("alg", "group_alg_hot_deta_temp_press_system", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_temp_max_min", "供回水温差设定值的上下限", "℃", "5,15", 1, 0, 99, "范围（0-99），上限≥下限", "泵组供回水温差自动调节的范围限制", null, null, false),
    ALG_HOT_DETA_TEMP_PRESS_TEMP_KEEP_TIME("alg", "group_alg_hot_deta_temp_press_system", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_temp_keep_time", "供回水温差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "泵组供回水温差调节后需要保持的时间，该时间段内不会再次调节", null, null, null),
    ALG_HOT_DETA_TEMP_PRESS_PRESS_DEFAULT("alg", "group_alg_hot_deta_temp_press_system", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_press_default", "供回水压差默认设定值", "kPa", "60", 1, 0, 999, "范围（0-999）", "未作调整时默认的泵组供回水压差设定值", null, null, null),
    ALG_HOT_DETA_TEMP_PRESS_PRESS_ADJUST_RANGE("alg", "group_alg_hot_deta_temp_press_system", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_press_adjust_range", "压差调整幅度的配置", "kPa", "10", 0, 0, 999, "范围（0-999）", "每次压差调整时的幅度，即每次增加或减少该数值", null, null, null),
    ALG_HOT_DETA_TEMP_PRESS_PRESS_MAX_MIN("alg", "group_alg_hot_deta_temp_press_system", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_press_max_min", "供回水压差设定值的上下限", "kPa", "40,100", 0, 0, 999, "范围（0-999），上限≥下限", "泵组供回水压差自动调节的范围限制", null, null, false),
    ALG_HOT_DETA_TEMP_PRESS_PRESS_KEEP_TIME("alg", "group_alg_hot_deta_temp_press_system", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_press_keep_time", "供回水压差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "泵组供回水压差调节后需要保持的时间，该时间段内不会再次调节", null, null, null),

    ALG_HOT_DETA_TEMP_PRESS_PUMP_TEMP_DEFAULT("alg", "group_alg_hot_deta_temp_press_pump", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_pump_temp_default", "供回水温差默认设定值", "℃", "10", 1, 0, 99, "范围（0-99）", "未作调整时默认的供回水温差设定值", null, null, null),
    ALG_HOT_DETA_TEMP_PRESS_PUMP_TEMP_ADJUST_RANGE("alg", "group_alg_hot_deta_temp_press_pump", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_pump_temp_adjust_range", "温差调整幅度的配置", "℃", "2", 1, 0, 99, "范围（0-99）", "每次温差调整时的幅度，即每次增加或减少该数值", null, null, null),
    ALG_HOT_DETA_TEMP_PRESS_PUMP_TEMP_MAX_MIN("alg", "group_alg_hot_deta_temp_press_pump", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_pump_temp_max_min", "供回水温差设定值的上下限", "℃", "5,15", 1, 0, 99, "范围（0-99），上限≥下限", "泵组供回水温差自动调节的范围限制", null, null, false),
    ALG_HOT_DETA_TEMP_PRESS_PUMP_TEMP_KEEP_TIME("alg", "group_alg_hot_deta_temp_press_pump", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_pump_temp_keep_time", "供回水温差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "泵组供回水温差调节后需要保持的时间，该时间段内不会再次调节", null, null, null),
    ALG_HOT_DETA_TEMP_PRESS_PUMP_PRESS_DEFAULT("alg", "group_alg_hot_deta_temp_press_pump", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_pump_press_default", "供回水压差默认设定值", "kPa", "60", 1, 0, 999, "范围（0-999）", "未作调整时默认的泵组供回水压差设定值", null, null, null),
    ALG_HOT_DETA_TEMP_PRESS_PUMP_PRESS_ADJUST_RANGE("alg", "group_alg_hot_deta_temp_press_pump", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_pump_press_adjust_range", "压差调整幅度的配置", "kPa", "10", 0, 0, 999, "范围（0-999）", "每次压差调整时的幅度，即每次增加或减少该数值", null, null, null),
    ALG_HOT_DETA_TEMP_PRESS_PUMP_PRESS_MAX_MIN("alg", "group_alg_hot_deta_temp_press_pump", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_pump_press_max_min", "供回水压差设定值的上下限", "kPa", "40,100", 0, 0, 999, "范围（0-999），上限≥下限", "泵组供回水压差自动调节的范围限制", null, null, false),
    ALG_HOT_DETA_TEMP_PRESS_PUMP_PRESS_KEEP_TIME("alg", "group_alg_hot_deta_temp_press_pump", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_pump_press_keep_time", "供回水压差设定修改后保持时间", "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "泵组供回水压差调节后需要保持的时间，该时间段内不会再次调节", null, null, null),
    ALG_HOT_DETA_TEMP_PRESS_PUMP_PRESS_UPPER("alg", "group_alg_hot_deta_temp_press_pump", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_pump_press_upper", "供水温度下限值", "℃", "40", 1, 0, 99, "范围（0-99）", "", null, null, false),
    ALG_HOT_DETA_TEMP_PRESS_PUMP_PRESS_OVERRUN_TIME("alg", "group_alg_hot_deta_temp_press_pump", "热站自动需求计算条件输入", "alg_hot_deta_temp_press_pump_press_overrun_time", "供水温度超出标准可容忍时长", "min", "", 0, 0, 1440, "范围（0-1440）", "", null, null, false),

    /**
     * 控制逻辑条件输入
     */
    CONTROL_VRV_TEMP_MAX("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_temp_max", "温度目标上限差值", "℃", "2", 0, 0, 99, "范围 （0-99）", "温度目标上限设定=温度目标设定值+温度目标上限差值ΔT1，用来判断是否触发末端温度模式的调节", null, null, null),
    CONTROL_VRV_TEMP_MIN("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_temp_min", "温度目标下限差值", "℃", "2", 0, 0, 99, "范围 （0-99）", "温度目标下限设定=温度目标设定值-温度目标下限差值ΔT2，用来判断是否触发末端温度模式的调节", null, null, null),
    CONTROL_VRV_COOLING_TEMP_MAX("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_cooling_temp_max", "供冷送风温度差", "℃", "7", 0, 0, 99, "范围 （0-99）", "供冷送风温度设定=温度目标设定值-供冷送风温度差ΔT3，用来计算供冷送风温度设定", null, null, null),
    CONTROL_VRV_HOT_TEMP_MAX("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_hot_temp_max", "供热送风温度差", "℃", "10", 0, 0, 99, "范围 （0-99）", "供热送风温度设定=温度目标设定值+供热送风温度差ΔT4，用来计算供热送风温度设定", null, null, null),
    CONTROL_VRV_WIND_TEMP_MAX("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_wind_temp_max", "除湿送风温度差", "℃", "10", 0, 0, 99, "范围 （0-99）", "除湿送风温度设定=温度目标设定值-除湿送风温度差ΔT5，用来计算除湿送风温度设定", null, null, null),
    CONTROL_VRV_CO2_MAX("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_co2_max", "CO₂浓度目标上限差值", "ppm", "400", 0, 0, 9999, "范围 （ 0-9999）", "CO₂浓度目标上限设定=CO₂浓度目标设定值+ CO₂浓度目标上限差值ΔCO₂，用来判断是否触发末端CO₂模式的调节", null, null, null),
    CONTROL_VRV_HUMID_MAX("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_humid_max", "相对湿度目标上限差值", "%", "20", 0, 0, 100, "范围 （0-100）", "相对湿度目标上限设定=相对湿度目标设定值+相对湿度目标上限差值ΔRH，用来判断是否触发末端湿度模式的调节", null, null, null),
    CONTROL_VRV_FREEZE_TEMP("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_freeze_temp", "防冻报警触发温度", "℃", "4", 0, 0, 99, "范围 （0-99）", "用于触发防冻报警，低于触发温度则会报警", null, null, null),
    CONTROL_VRV_PRE_FILTER_MAX("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_pre_filter_max", "初效过滤器压差上限", "Pa", "250", 0, 0, 999, "范围 （0-999）", "用于判断初效过滤器是否脏堵，如果实际压差超过上限值则会报警", null, null, null),
    CONTROL_VRV_MID_FILTER_MAX("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_mid_filter_max", "中效过滤器压差上限", "Pa", "330", 0, 0, 999, "范围 （0-999）", "用于判断中效过滤器是否脏堵，如果实际压差超过上限值则会报警", null, null, null),
    CONTROL_VRV_LOW_VOL_MAX("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_low_vol_max", "风机欠压比上限", "%", "40", 0, 0, 100, "范围 （ 0-100）", "送风压力低于“风机欠压比上限Ppa*设计机外余压”则触发风机送风欠压报警", null, null, null),
    CONTROL_VRV_FRESH_AIR_OFFSET("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_fresh_air_offset", "新风阀开度偏差", "%", "5", 0, 0, 100, "范围 （ 0-100）", "若新风阀开度反馈在“新风阀开度设定±ΔDfv”范围外，触发新风阀状态反馈与指令不符报警", null, null, null),
    CONTROL_VRV_RETURN_AIR_OFFSET("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_return_air_offset", "回风阀开度偏差", "%", "5", 0, 0, 100, "范围 （ 0-100）", "若回风阀开度反馈在【回风阀开度设定±ΔDrv】范围外，触发回风阀状态反馈与指令不符报警", null, null, null),
    CONTROL_VRV_COOLING_WATER_OFFSET("control", "group_control_vrv", "末端控制逻辑条件输入", "control_vrv_cooling_water_offset", "冷水阀开度偏差", "%", "5", 0, 0, 100, "范围 （ 0-100）", "若冷水阀开度反馈在【冷水阀开度设定±ΔDchv】范围外，触发冷水阀状态反馈与指令不符报警", null, null, null),
    CONTROL_FAN_TEMP_MAX("control", "group_control_fan", "末端控制逻辑条件输入", "control_fan_temp_max", "温度目标上限差值", "℃", "2", 0, 0, 99, "范围 （0-99）", "温度目标上限设定=温度目标设定值+温度目标上限差值ΔT1，用来判断是否触发末端温度模式的调节", null, null, null),
    CONTROL_FAN_TEMP_MIN("control", "group_control_fan", "末端控制逻辑条件输入", "control_fan_temp_min", "温度目标下限差值", "℃", "2", 0, 0, 99, "范围 （0-99）", "温度目标下限设定=温度目标设定值-温度目标下限差值ΔT2，用来判断是否触发末端温度模式的调节", null, null, null),
    CONTROL_AIR_TEMP_MAX("control", "group_control_air", "末端控制逻辑条件输入", "control_air_temp_max", "温度目标上限差值", "℃", "2", 0, 0, 99, "范围 （0-99）", "温度目标上限设定=温度目标设定值+温度目标上限差值ΔT1，用来判断是否触发末端温度模式的调节", null, null, null),
    CONTROL_AIR_CHIMNEY_TEMP_MAX("control", "group_control_air", "末端控制逻辑条件输入", "control_air_chimney_temp_max", "烟囱温度目标上限差值", "℃", "4", 0, 0, 99, "范围 （0-99）", "烟囱温度目标下限设定=温度目标设定值-温度目标设定值ΔT2，用来判断是否触发末端温度模式的调节", null, null, null),
    CONTROL_AIR_COOLING_TEMP("control", "group_control_air", "末端控制逻辑条件输入", "control_air_cooling_temp", "供冷送风温度差", "℃", "7", 0, 0, 99, "范围 （0-99）", "供冷送风温度设定=温度目标设定值-供冷送风温度差ΔT3，用来计算供冷送风温度设定", null, null, null),
    CONTROL_AIR_FREEZE_TEMP("control", "group_control_air", "末端控制逻辑条件输入", "control_air_freeze_temp", "防冻报警触发温度", "℃", "4", 0, 0, 99, "范围 （0-99）", "用于触发防冻报警，低于触发温度则会报警", null, null, null),
    CONTROL_AIR_PRE_FILTER_MAX("control", "group_control_air", "末端控制逻辑条件输入", "control_air_pre_filter_max", "初效过滤器压差上限", "Pa", "250", 0, 0, 999, "范围 （0-999）", "用于判断初效过滤器是否脏堵，如果实际压差超过上限值则会报警", null, null, null),
    CONTROL_AIR_MID_FILTER_MAX("control", "group_control_air", "末端控制逻辑条件输入", "control_air_mid_filter_max", "中效过滤器压差上限", "Pa", "330", 0, 0, 999, "范围 （0-999）", "用于判断中效过滤器是否脏堵，如果实际压差超过上限值则会报警", null, null, null),
    CONTROL_AIR_LOW_VOL_MAX("control", "group_control_air", "末端控制逻辑条件输入", "control_air_low_vol_max", "风机欠压比上限", "%", "40", 0, 0, 100, "范围 （ 0-100）", "送风压力低于“风机欠压比上限Ppa*设计机外余压”则触发风机送风欠压报警", null, null, null),
    CONTROL_AIR_FRESH_AIR_OFFSET("control", "group_control_air", "末端控制逻辑条件输入", "control_air_fresh_air_offset", "新风阀开度偏差", "%", "5", 0, 0, 100, "范围 （ 0-100）", "若新风阀开度反馈在“新风阀开度设定±ΔDfv”范围外，触发新风阀状态反馈与指令不符报警", null, null, null),
    CONTROL_AIR_COOLING_WATER_OFFSET("control", "group_control_air", "末端控制逻辑条件输入", "control_air_cooling_water_offset", "冷水阀开度偏差", "%", "5", 0, 0, 100, "范围 （ 0-100）", "若冷水阀开度反馈在【冷水阀开度设定±ΔDchv】范围外，触发冷水阀状态反馈与指令不符报警", null, null, null),

    CONTROL_SELECT_COOLING_ACCCCC("control", "group_control_cooling", "控制逻辑选机条件输入", "control_select_cooling_ACCCCC", "离心式冷水机组", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCCC\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_COOLING_ACCCSC("control", "group_control_cooling", "控制逻辑选机条件输入", "control_select_cooling_ACCCSC", "螺杆式冷水机组", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCSC\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_COOLING_ACCCAC("control", "group_control_cooling", "控制逻辑选机条件输入", "control_select_cooling_ACCCAC", "吸收式冷水机组", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCAC\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_COOLING_ACCCPC("control", "group_control_cooling", "控制逻辑选机条件输入", "control_select_cooling_ACCCPC", "活塞式冷水机组", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCPC\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_COOLING_ACCCCP("control", "group_control_cooling", "控制逻辑选机条件输入", "control_select_cooling_ACCCCP", "供冷冷冻水泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCCP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_COOLING_ACCCOP("control", "group_control_cooling", "控制逻辑选机条件输入", "control_select_cooling_ACCCOP", "供冷冷却水泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCOP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_COOLING_ACCCGP("control", "group_control_cooling", "控制逻辑选机条件输入", "control_select_cooling_ACCCGP", "供冷乙二醇泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCGP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_COOLING_ACCCTP("control", "group_control_cooling", "控制逻辑选机条件输入", "control_select_cooling_ACCCTP", "蓄冷热槽循环泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCTP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_COOLING_ACGEFP("control", "group_control_cooling", "控制逻辑选机条件输入", "control_select_cooling_ACGEFP", "补水泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACGEFP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_COOLING_ACCCOF("control", "group_control_cooling", "控制逻辑选机条件输入", "control_select_cooling_ACCCOF", "冷却塔风机", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCOF\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),

    /*
    CONTROL_SELECT_AIR_COOLING_ACCCCC("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACCCCC", "离心式冷水机组", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCCC\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_COOLING_ACCCSC("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACCCSC", "螺杆式冷水机组", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCSC\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_COOLING_ACCCAC("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACCCAC", "吸收式冷水机组", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCAC\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_COOLING_ACCCPC("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACCCPC", "活塞式冷水机组", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCPC\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_COOLING_ACCCCP("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACCCCP", "供冷冷冻水泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCCP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_COOLING_ACCCOP("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACCCOP", "供冷冷却水泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCOP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_COOLING_ACCCGP("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACCCGP", "供冷乙二醇泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCGP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_COOLING_ACCCTP("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACCCTP", "蓄冷热槽循环泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCTP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_COOLING_ACGEFP("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACGEFP", "补水泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACGEFP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_COOLING_ACCCOF("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACCCOF", "冷却塔风机", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCOF\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_COOLING_ACCCAH("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACCCAH", "空气源热泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCAH\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_COOLING_ACCCGH("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACCCGH", "地源热泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCGH\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_COOLING_ACCCWH("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling_ACCCWH", "水源热泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCWH\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
     */
    CONTROL_SELECT_AIR_COOLING("control", "group_control_air_cooling", "控制逻辑选机条件输入", "control_select_air_cooling", "key_ChillerAndHeatPump", "/", "accRunTime", -1, null, null, "", "", null, "[{\"classCode\":[\"ACCCCC\",\"ACCCSC\",\"ACCCAC\",\"ACCCPC\",\"ACCCWH\",\"ACCCGH\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"},{\"classCode\":[\"ACAHC\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}]", null),



    CONTROL_SELECT_COLD_HOT_ACAHC("control", "group_control_cold_hot", "控制逻辑选机条件输入", "control_select_cold_hot_ACCCAH", "空气源热泵控制组", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACAHC\",\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_COLD_HOT_ACCCGH("control", "group_control_cold_hot", "控制逻辑选机条件输入", "control_select_cold_hot_ACCCGH", "地源热泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCGH\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_COLD_HOT_ACCHCH("control", "group_control_cold_hot", "控制逻辑选机条件输入", "control_select_cold_hot_ACCHCH", "冷热水循环水泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCHCH\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_COLD_HOT_ACGEFP("control", "group_control_cold_hot", "控制逻辑选机条件输入", "control_select_cold_hot_ACGEFP", "补水泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACGEFP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),

    /*
    CONTROL_SELECT_AIR_HOT_ACCHEB("control", "group_control_air_hot", "控制逻辑选机条件输入", "control_select_cold_hot_ACCHEB", "电锅炉", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCHEB\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_HOT_ACCHCB("control", "group_control_air_hot", "控制逻辑选机条件输入", "control_select_cold_hot_ACCHCB", "燃煤锅炉", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCHCB\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_HOT_ACCHGB("control", "group_control_air_hot", "控制逻辑选机条件输入", "control_select_cold_hot_ACCHGB", "燃气锅炉", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCHGB\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_HOT_ACCHFB("control", "group_control_air_hot", "控制逻辑选机条件输入", "control_select_cold_hot_ACCHFB", "燃油锅炉", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCHFB\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_HOT_ACCCAC("control", "group_control_air_hot", "控制逻辑选机条件输入", "control_select_cold_hot_ACCCAC", "吸收式冷水机组", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCAC\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_HOT_ACAHC("control", "group_control_air_hot", "控制逻辑选机条件输入", "control_select_cold_hot_ACAHC", "空气源热泵控制组", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACAHC\",\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_HOT_ACCCWH("control", "group_control_air_hot", "控制逻辑选机条件输入", "control_select_cold_hot_ACCCWH", "水源热泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCWH\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_AIR_HOT_ACCCGH("control", "group_control_air_hot", "控制逻辑选机条件输入", "control_select_cold_hot_ACCCGH", "地源热泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCCGH\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
     */
    CONTROL_SELECT_AIR_HOT("control", "group_control_air_hot", "控制逻辑选机条件输入", "control_select_air_hot", "锅炉与热泵", "/", "accRunTime", -1, null, null, "", "", null, "[{\"classCode\":[\"ACCHEB\",\"ACCHCB\",\"ACCHGB\",\"ACCHFB\",\"ACCCAC\",\"ACCCWH\",\"ACCCGH\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"},{\"classCode\":[\"ACAHC\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}]", null),
    //冷机与热泵
    CONTROL_SELECT_AIR_COOLING_ALL("control", "group_control_cold_hot_all", "控制逻辑选机条件输入", "control_select_air_cooling_all", "key_ChillerAndHeatPump", "/", "accRunTime", -1, null, null, "", "", null, "[{\"classCode\":[\"ACCCCC\",\"ACCCSC\",\"ACCCAC\",\"ACCCPC\",\"ACCCWH\",\"ACCCGH\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"},{\"classCode\":[\"ACAHC\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}]", null),


    CONTROL_SELECT_HOT_ACCHGB("control", "group_control_hot", "控制逻辑选机条件输入", "control_select_hot_ACCHGB", "燃气锅炉", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCHGB\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_HOT_ACCHEB("control", "group_control_hot", "控制逻辑选机条件输入", "control_select_hot_ACCHEB", "电锅炉", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCHEB\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_HOT_ACCHFB("control", "group_control_hot", "控制逻辑选机条件输入", "control_select_hot_ACCHFB", "燃油锅炉", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCHFB\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_HOT_ACCHCB("control", "group_control_hot", "控制逻辑选机条件输入", "control_select_hot_ACCHCB", "燃煤锅炉", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCHCB\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_HOT_ACCHHP("control", "group_control_hot", "控制逻辑选机条件输入", "control_select_hot_ACCHHP", "供热水泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACCHHP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),
    CONTROL_SELECT_HOT_ACGEFP("control", "group_control_hot", "控制逻辑选机条件输入", "control_select_hot_ACGEFP", "补水泵", "/", "accRunTime", -1, null, null, "", "", null, "{\"classCode\":\"ACGEFP\",\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}", null),

    CONTROL_SELECT_COOL_PUMP_GROUP_ACCCCP("control", "group_control_cool_pump_group", "控制逻辑选机条件输入", "control_select_cool_pump_group_ACCCCP", "冷冻水泵", "/", "accRunTime", -1, null, null, "", "", null, null, null),
    CONTROL_SELECT_CHIL_PUMP_GROUP_ACCCOP("control", "group_control_chil_pump_group", "控制逻辑选机条件输入", "control_select_chil_pump_group_ACCCOP", "冷却水泵", "/", "accRunTime", -1, null, null, "", "", null, null, null),
    CONTROL_SELECT_COOL_TOWER_GROUP_ACCCOT("control", "group_control_cool_tower_group", "控制逻辑选机条件输入", "control_select_cool_tower_group_ACCCOT", "冷却塔风机", "/", "accRunTime", -1, null, null, "", "", null, null, null),
    CONTROL_SELECT_CGLY_PUMP_GROUP_ACCCGP("control", "group_control_cgly_pump_group", "控制逻辑选机条件输入", "control_select_cgly_pump_group_ACCCGP", "供冷乙二醇泵", "/", "accRunTime", -1, null, null, "", "", null, null, null),
    CONTROL_SELECT_COOL_HSLO_GROUP_ACCCST("control", "group_control_cool_hslo_group", "控制逻辑选机条件输入", "control_select_cool_hslo_group_ACCCST", "蓄冷热槽循环泵", "/", "accRunTime", -1, null, null, "", "", null, null, null),
    CONTROL_SELECT_HOT_PUMP_GROUP_ACCHHP("control", "group_control_hot_pump_group", "控制逻辑选机条件输入", "control_select_hot_pump_group_ACCHHP", "供热水泵", "/", "accRunTime", -1, null, null, "", "", null, null, null),
    CONTROL_SELECT_COOL_HOT_GROUP_ACCHCH("control", "group_control_cool_hot_pump", "控制逻辑选机条件输入", "control_select_cool_hot_group_ACCHCH", "冷热水循环水泵", "/", "accRunTime", -1, null, null, "", "", null, null, null),

    /**
     * 通风控制逻辑条件输入--穹顶通风
     */
    //穹顶排风控制
    CONTROL_VENT_DOME_EXHAUST_OPEN_TEMP("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_exhaust_open_temp", "开启排风机的穹顶温度设定值Ts", "℃", "45", 0, 0, 99, "范围 （0-99）", "用于判断穹顶温度是否过高：穹顶温度高于该设定值且持续\"穹顶温度高于设定值的持续时长\"后，判定穹顶温度过高（Qr=是），输出开启穹顶排风机", null, null, null),
    CONTROL_VENT_DOME_EXHAUST_OPEN_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_exhaust_open_duration", "穹顶温度高于设定值的持续时长", "min", "15", 0, 0, 1440, "范围 （0-1440）", "判定穹顶温度过高所需的温度超限持续时间，避免温度短时波动导致排风机频繁启停", null, null, null),
    CONTROL_VENT_DOME_EXHAUST_CLOSE_TEMP("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_exhaust_close_temp", "关闭排风机的穹顶温度设定值Tx", "℃", "35", 0, 0, 99, "范围 （0-99）", "穹顶温度低于该设定值且持续\"穹顶温度低于设定值的持续时长\"后，判定穹顶温度过高解除（Qr=否），输出关闭穹顶排风机", null, null, null),
    CONTROL_VENT_DOME_EXHAUST_CLOSE_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_exhaust_close_duration", "穹顶温度低于设定值的持续时长", "min", "15", 0, 0, 1440, "范围 （0-1440）", "判定穹顶温度过高解除所需的持续时间", null, null, null),

    //电动气窗控制
    CONTROL_VENT_DOME_WINDOW_HOLD_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_window_hold_duration", "电动气窗关闭后的保持时间", "min", "15", 0, 0, 1440, "范围 （0-1440）", "电动气窗从\"关闭\"状态到下次允许下发开启设定指令的最小间隔，避免气窗频繁开关", null, null, null),

    //非营业时段室外空气状态判断
    CONTROL_VENT_DOME_OFF_JUDGE_MODE("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_off_judge_mode", "判断室外空气状态的方式", "", "1", -1, null, null, "", "选择非营业时段判断室外空气是否可通风降温的方式：干球温度判断（默认，设置难度低、项目接受程度较高）、湿球温度判断（相对合理、设置难度较高）、干球温度+湿球温度综合判断（最合理、兼顾室外干球温度和相对湿度、设置难度较高）", null, null, null),
    CONTROL_VENT_DOME_OFF_WET_TEMP_DIFF("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_off_wet_temp_diff", "判断可通风的室内外湿球温差设定值", "℃", "6", 0, 0, 99, "范围 （0-99）", "室内外湿球温差=室内湿球温度-室外湿球温度；温差达到该设定值并持续\"判断室外湿球温度低的超限持续时长\"后，判定室外空气状态为\"凉\"（L），电动气窗可开启通风", null, null, null),
    CONTROL_VENT_DOME_OFF_WET_LOW_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_off_wet_low_duration", "判断室外湿球温度低的超限持续时长", "min", "60", 0, 0, 1440, "范围 （0-1440）", "室内外湿球温差超过设定值需持续该时长，才判定室外空气状态为\"凉\"（L）", null, null, null),
    CONTROL_VENT_DOME_OFF_WET_HIGH_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_off_wet_high_duration", "判断室外湿球温度高的超限持续时长", "min", "60", 0, 0, 1440, "范围 （0-1440）", "室内外湿球温差低于设定值需持续该时长，才判定室外空气状态为\"热\"（R），电动气窗输出关闭", null, null, null),
    CONTROL_VENT_DOME_OFF_DRY_TEMP_DIFF("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_off_dry_temp_diff", "判断可通风的室内外干球温差设定值", "℃", "6", 0, 0, 99, "范围 （0-99）", "室内外干球温差=室内干球温度-室外干球温度；温差达到该设定值并持续\"判断室外干球温度低的超限持续时长\"后，判定室外空气状态为\"凉\"（L），电动气窗可开启通风", null, null, null),
    CONTROL_VENT_DOME_OFF_DRY_LOW_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_off_dry_low_duration", "判断室外干球温度低的超限持续时长", "min", "60", 0, 0, 1440, "范围 （0-1440）", "室内外干球温差超过设定值需持续该时长，才判定室外空气状态为\"凉\"（L）", null, null, null),
    CONTROL_VENT_DOME_OFF_DRY_HIGH_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_off_dry_high_duration", "判断室外干球温度高的超限持续时长", "min", "60", 0, 0, 1440, "范围 （0-1440）", "室内外干球温差低于设定值需持续该时长，才判定室外空气状态为\"热\"（R），电动气窗输出关闭", null, null, null),

    //营业时段室外空气状态判断
    CONTROL_VENT_DOME_ON_JUDGE_MODE("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_on_judge_mode", "判断室外空气状态的方式", "", "1", -1, null, null, "", "选择营业时段判断室外空气是否可通风降温的方式，枚举与默认同非营业时段", null, null, null),
    CONTROL_VENT_DOME_ON_WET_TEMP_DIFF("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_on_wet_temp_diff", "判断可通风的室内外湿球温差设定值", "℃", "6", 0, 0, 99, "范围 （0-99）", "同非营业时段同名参数，仅作用于营业时段的判断", null, null, null),
    CONTROL_VENT_DOME_ON_WET_LOW_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_on_wet_low_duration", "判断室外湿球温度低的超限持续时长", "min", "60", 0, 0, 1440, "范围 （0-1440）", "同非营业时段同名参数，仅作用于营业时段的判断", null, null, null),
    CONTROL_VENT_DOME_ON_WET_HIGH_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_on_wet_high_duration", "判断室外湿球温度高的超限持续时长", "min", "60", 0, 0, 1440, "范围 （0-1440）", "同非营业时段同名参数，仅作用于营业时段的判断", null, null, null),
    CONTROL_VENT_DOME_ON_DRY_TEMP_DIFF("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_on_dry_temp_diff", "判断可通风的室内外干球温差设定值", "℃", "6", 0, 0, 99, "范围 （0-99）", "同非营业时段同名参数，仅作用于营业时段的判断", null, null, null),
    CONTROL_VENT_DOME_ON_DRY_LOW_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_on_dry_low_duration", "判断室外干球温度低的超限持续时长", "min", "60", 0, 0, 1440, "范围 （0-1440）", "同非营业时段同名参数，仅作用于营业时段的判断", null, null, null),
    CONTROL_VENT_DOME_ON_DRY_HIGH_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_on_dry_high_duration", "判断室外干球温度高的超限持续时长", "min", "60", 0, 0, 1440, "范围 （0-1440）", "同非营业时段同名参数，仅作用于营业时段的判断", null, null, null),

    //恶劣天气判断
    CONTROL_VENT_DOME_WEATHER_RH_THRESHOLD("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_weather_rh_threshold", "判断室外恶劣天气的室外相对湿度设定值", "%", "95", 0, 0, 100, "范围 （0-100）", "室外相对湿度超过该设定值且持续\"判断相对湿度超限的持续时长\"后，判定为恶劣天气（E=是，疑似降雨），电动气窗输出关闭", null, null, null),
    CONTROL_VENT_DOME_WEATHER_RH_OVER_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_weather_rh_over_duration", "判断相对湿度超限的持续时长", "min", "60", 0, 0, 1440, "范围 （0-1440）", "判定恶劣天气所需的室外相对湿度超限持续时间", null, null, null),
    CONTROL_VENT_DOME_WEATHER_CLEAR_DURATION("control", "group_control_vent_dome", "通风控制逻辑条件输入", "control_vent_dome_weather_clear_duration", "判断室外非恶劣天气的持续时长", "min", "60", 0, 0, 1440, "范围 （0-1440）", "恶劣天气条件消失后需持续该时长，才恢复判定为非恶劣天气（E=否），电动气窗方可按其他判断条件重新开启", null, null, null),

    /**
     * 空间需求条件输入
     */
    SPACE_QUALITY_COOLING_TEMP_MAX("space", "group_space_quality", null, "space_quality_cooling_temp_max", "供冷温度上限值", "℃", "20,30", 0, 0, 99, "范围 (0-99)，上限≥下限", "需求管理中供冷季温度上限值数值的可输入范围，该参数为供冷季在电价或碳因子较高时，温度目标值可以被突破的上限值", null, null, null),
    SPACE_QUALITY_COOLING_TEMP("space", "group_space_quality", null, "space_quality_cooling_temp", "供冷温度目标值", "℃", "20,30", 0, 0, 99, "范围 (0-99)，上限≥下限", "需求管理中供冷季温度目标值数值的可输入范围，该参数即为资管承诺给租户的控制标准", null, null, null),
    SPACE_QUALITY_COOLING_STORAGE("space", "group_space_quality", null, "space_quality_cooling_storage", "供冷蓄冷值", "℃", "20,30", 0, 0, 99, "范围 (0-99)，上限≥下限", "需求管理中供冷季温度蓄冷值数值的可输入范围，该参数为供冷季在电价或碳因子较低时，要向空间蓄冷，空间能达到的最低温度", null, null, null),
    SPACE_QUALITY_HOT_TEMP_MIN("space", "group_space_quality", null, "space_quality_hot_temp_min", "供热温度下限值", "℃", "10,30", 0, 0, 99, "范围 (0-99)，上限≥下限", "需求管理中供热季温度下限值数值的可输入范围，该参数为供热季在电价或碳因子较高时，温度目标值可以被突破的下限值", null, null, null),
    SPACE_QUALITY_HOT_TEMP("space", "group_space_quality", null, "space_quality_hot_temp", "供热温度目标值", "℃", "10,30", 0, 0, 99, "范围 (0-99)，上限≥下限", "需求管理中供热季温度目标值数值的可输入范围，该参数即为资管承诺给租户的控制标准", null, null, null),
    SPACE_QUALITY_HOT_STORAGE("space", "group_space_quality", null, "space_quality_hot_storage", "供热蓄热值", "℃", "10,30", 0, 0, 99, "范围 (0-99)，上限≥下限", "需求管理中供热季温度蓄热值数值的可输入范围，该参数为供热季在电价或碳因子较低时，要向空间蓄热，空间能达到的最高温度", null, null, null),
    SPACE_QUALITY_CO2_MAX("space", "group_space_quality", null, "space_quality_co2_max", "CO₂浓度上限值", "ppm", "300,2000", 0, 0, 9999, "范围 (0-9999)，上限≥下限", "需求管理中CO₂浓度上限值数值的可输入范围", null, null, null),
    SPACE_QUALITY_CO2("space", "group_space_quality", null, "space_quality_co2", "CO₂浓度目标值", "ppm", "300,2000", 0, 0, 9999, "范围 (0-9999)，上限≥下限", "需求管理中CO₂浓度目标值数值的可输入范围，该参数即为资管承诺给租户的控制标准", null, null, null),
//    SPACE_DEMAND_COOLING_COST_PERCENT("space", "group_space_demand", null, "space_demand_cooling_cost_percent", "供冷成本占比设定", "%", "30,60", 1, 10, 90, "范围 (10-90)，两个值差值≧20%，上限≥下限", "用以触发算法计算冷机台数的时刻，该设定值与供冷成本占比结合，用于判断是否需要触发算法计算"),

    /**
     * 策略需求目标及控制参数前端输入条件
     */
    POLICY_SITE_COOLING_MACHINE_LARGE("policy", "group_policy_site_cooling", "冷站策略干预", "policy_site_cooling_machine_large", "大冷机开启台数设定", "台", "${large_cooler}", 0, 0, null, "范围（0-${large_cooler}）", "", null, null, null),
    POLICY_SITE_COOLING_MACHINE_SMALL("policy", "group_policy_site_cooling", "冷站策略干预", "policy_site_cooling_machine_small", "小冷机开启台数设定", "台", "${small_cooler}", 0, 0, null, "范围（0-${small_cooler}）", "", null, null, null),
    POLICY_SITE_COOLING_WATER_TEMP("policy", "group_policy_site_cooling", "冷站策略干预", "policy_site_cooling_water_temp", "冷冻水供水温度设定", "℃", "6,15", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_SITE_COOLING_RETURN_WATER_TEMP("policy", "group_policy_site_pump", "冷站策略干预", "policy_site_cooling_return_water_temp", "对应支路供回水温差设定", "℃", "0,10", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_SITE_PUMP_RETURN_WATER_VOL("policy", "group_policy_site_pump", "冷站策略干预", "policy_site_pump_return_water_vol", "对应支路供回水压差设定", "kPa", "0,300", 0, 0, 999, "范围(0-999)，上限≥下限", "", null, null, null),

    POLICY_DEVICE_COOLING_MACHINE_WATER_TEMP("policy", "group_policy_device_cooling", "冷站系统及设备控制", "policy_device_cooling_machine_water_temp", "冷机冷却水进水温度设定", "℃", "0,50", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    //    POLICY_DEVICE_COOLING_RETURN_WATER_TEMP("policy", "group_policy_device_cooling", "冷站系统及设备控制", "policy_device_cooling_return_water_temp", "policy_device_cooling_return_water_temp冷冻水供回水温差设定", "℃", "0,10", 1, 0, 99, "范围(0-99)，上限≥下限", ""),
    POLICY_DEVICE_COOLING_WATER_VOL("policy", "group_policy_device_cooling", "冷站系统及设备控制", "policy_device_cooling_water_vol", "分集水器压差设定", "kPa", "0,300", 0, 0, 999, "范围(0-999)，上限≥下限", "", null, null, null),
    POLICY_DEVICE_CHILLER_WATER_TEMP("policy", "group_policy_device_chiller", "冷站系统及设备控制", "policy_device_chiller_water_temp", "蒸发器出口水温设定", "℃", "6,15", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_DEVICE_FAN_FREQ("policy", "group_policy_device_fan", "冷站系统及设备控制", "policy_device_fan_freq", "设备频率设定", "Hz", "20,50", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_DEVICE_PUMP_FREQ("policy", "group_policy_device_pump", "冷站系统及设备控制", "policy_device_pump_freq", "设备频率设定", "Hz", "20,50", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_DEVICE_VALVE_DEGREE("policy", "group_policy_device_valve", "冷站系统及设备控制", "policy_device_valve_degree", "开度设定", "%", "0,100", 0, 0, 100, "范围(0-100)，上限≥下限", "", null, null, null),

    POLICY_SITE_COLDHOT_MACHINE_COUNT("policy", "group_policy_site_cooling_hot", "冷热站策略干预", "policy_site_coldhot_machine_count", "空气源热泵控制组开启数量设定", "台", "${count}", 0, 0, null, "范围（0-${count}）", "", null, null, null),
    POLICY_SITE_COLDHOT_COLD_TEMP_SET("policy", "group_policy_site_cooling_hot", "冷热站策略干预", "policy_site_coldhot_cold_temp_set", "制冷供水温度设定", "℃", "6,15", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_SITE_COLDHOT_HEAT_TEMP_SET("policy", "group_policy_site_cooling_hot", "冷热站策略干预", "policy_site_coldhot_heat_temp_set", "制热供水温度设定", "℃", "30,60", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_SITE_COLDHOT_TEMP_DIFF("policy", "group_policy_site_pump_cold_hot", "冷热站策略干预", "policy_site_coldhot_temp_diff", "供回水温差设定", "℃", "0,20", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_SITE_COLDHOT_PRESS_DIFF("policy", "group_policy_site_pump_cold_hot", "冷热站策略干预", "policy_site_coldhot_press_diff", "供回水压差设定", "kPa", "0,300", 0, 0, 999, "范围(0-999)，上限≥下限", "", null, null, null),

    POLICY_DEVICE_COLDHOT_WATER_VOL("policy", "group_policy_device_cooling_hot", "冷热站系统及设备控制参数", "policy_device_coldhot_water_vol", "分集水器压差设定", "kPa", "0,300", 0, 0, 999, "范围(0-999)，上限≥下限", "", null, null, null),
    POLICY_DEVICE_COLDHOT_WATER_TEMP("policy", "group_policy_device_hot_pump", "冷热站系统及设备控制参数", "policy_device_coldhot_water_temp", "制冷供水温度设定", "℃", "6,15", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_DEVICE_COLDHOT_HEAT_TEMP("policy", "group_policy_device_hot_pump", "冷热站系统及设备控制参数", "policy_device_coldhot_heat_temp", "制热供水温度设定", "℃", "30,60", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_DEVICE_COLDHOT_FREQ("policy", "group_policy_device_circulat_pump", "冷热站系统及设备控制参数", "policy_device_coldhot_freq", "设备频率设定", "Hz", "20,50", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_DEVICE_COLDHOT_VALVE_DEGREE("policy", "group_policy_device_regulat_water", "冷热站系统及设备控制参数", "policy_device_coldhot_valve_degree", "开度设定", "%", "0,100", 0, 0, 100, "范围(0-100)，上限≥下限", "", null, null, null),

    POLICY_END_AIR_VALVE_DEGREE("policy", "group_policy_end_air", "末端控制干预", "policy_end_air_valve_degree", "冷水阀开度设定", "%", "0,100", 0, 0, 100, "范围(0-100)，上限≥下限", "", null, null, null),
    POLICY_END_AIR_AREA_CO2("policy", "group_policy_end_air", "末端控制干预", "policy_end_air_area_co2", "服务区域目标CO₂浓度设定", "ppm", "300,2000", 0, 0, 9999, "范围(0-9999)，上限≥下限", "", null, null, null),
    POLICY_END_AIR_AREA_TEMP("policy", "group_policy_end_air", "末端控制干预", "policy_end_air_area_temp", "服务区域目标温度设定", "℃", "10,30", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_END_VRVH_AREA_TEMP("policy", "group_policy_end_vrvh", "末端控制干预", "policy_end_vrvh_area_temp", "服务区域目标温度设定", "℃", "10,30", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_END_VRVH_AREA_CO2("policy", "group_policy_end_vrvh", "末端控制干预", "policy_end_vrvh_area_co2", "服务区域目标CO₂浓度设定", "ppm", "300,2000", 0, 0, 9999, "范围(0-9999)，上限≥下限", "", null, null, null),
    POLICY_END_VRVH_VALVE_DEGREE("policy", "group_policy_end_vrvh", "末端控制干预", "policy_end_vrvh_valve_degree", "冷水阀开度设定", "%", "0,100", 0, 0, 100, "范围(0-100)，上限≥下限", "", null, null, null),
    POLICY_END_VRVH_FRESH_AIR_DEGREE("policy", "group_policy_end_vrvh", "末端控制干预", "policy_end_vrvh_fresh_air_degree", "新风阀开度设定", "%", "0,100", 0, 0, 100, "范围(0-100)，上限≥下限", "", null, null, null),
    POLICY_END_VRVH_RETURN_AIR_DEGREE("policy", "group_policy_end_vrvh", "末端控制干预", "policy_end_vrvh_return_air_degree", "回风阀开度设定", "%", "0,100", 0, 0, 100, "范围(0-100)，上限≥下限", "", null, null, null),
    POLICY_END_VRVH_WIND_FREQ("policy", "group_policy_end_vrvh", "末端控制干预", "policy_end_vrvh_wind_freq", "送风机频率设定", "Hz", "20,50", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_END_VRVV_AREA_TEMP("policy", "group_policy_end_vrvv", "末端控制干预", "policy_end_vrvv_area_temp", "服务区域目标温度设定", "℃", "10,30", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),
    POLICY_END_VRVV_VALVE_DEGREE("policy", "group_policy_end_vrvv", "末端控制干预", "policy_end_vrvv_valve_degree", "冷水阀开度设定", "%", "0,100", 0, 0, 100, "范围(0-100)，上限≥下限", "", null, null, null),
    POLICY_END_FCU_AREA_TEMP("policy", "group_policy_end_fcu", "末端控制干预", "policy_end_fcu_area_temp", "服务区域目标温度设定", "℃", "10,30", 0, 0, 99, "范围(0-99)，上限≥下限", "", null, null, null),

    WEATHER_WEATHER_CHANGE("weather", "group_weather_change", "室外天气数据来源切换", "weather_weather_change", "室外天气数据来源切换", "/", "天气服务", -1, null, null, "", "①天气服务：数据来源于天气服务，在界面展示\"干球温度\"、\"相对湿度\"等参考值\n②气象监测站：数据来源于项目安装的室外气象监测站，在界面展示\"干球温度\"、\"相对湿度\"、\"湿球温度\"、\"焓值\"等参考值", null, null, null),
    /**
     * 室外天气数据来源配置
     * @since 7.260501.0
     */
    ALG_WEATHER_DATA_SOURCE("weather", "group_weather_change", "室外天气数据来源切换", "alg_weather_data_source", "请选择气象监测站来源", "", null, -1, null, null, "", null, null, null, null),

    /** 自动需求计算条件输入--空调冷源自动需求计算的条件输入-供冷工况*/
    //加机逻辑
    ALG_AIR_COOLING_CALCULATE_DEADLINE("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_calculate_deadline", "供冷满足状态计算截止时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "冷站关闭前一定时间内，满足状态计算停止，不再加机", null, null, null),
    ALG_AIR_COOLING_CHILLER_RATE_MAX("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_chiller_rate_max", "串联冷机组负载率最大值", "%", "90", 1, 0, 100, "范围（0-100）", "用于计算冷机负载率加权平均差值P，如采集不到冷机负载率最大值，则取本值进行判断", null, "{\"relation\":{\"classCode\":[\"ACSCG\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}}", null),
    ALG_AIR_COOLING_ELE_IPLR_MAX("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_ele_iplr_max", "冷机负载率最大值", "%", "90", 1, 0, 100, "范围（0-100）", "用于计算冷机负载率加权平均差值P，如采集不到冷机负载率最大值，则取本值进行判断", null, "{\"relation\":{\"classCode\":[\"ACCCCC\",\"ACCCSC\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"},\"infoCode\":{\"key\":\"isDualCondition\",\"value\":\"0\"}}", null),
    ALG_AIR_COOLING_CEN_IPLR_MAX("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_cen_iplr_max", "离心式冷机负载率最大值", "%", "90", 1, 0, 100, "范围（0-100）", "用于计算冷机负载率加权平均差值P，如采集不到冷机负载率最大值，则取本值进行判断", null, "{\"relation\":{\"classCode\":[\"ACCCCC\",\"ACCCSC\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"},\"infoCode\":{\"key\":\"isDualCondition\",\"value\":\"0\"}}", null),
    ALG_AIR_COOLING_SCR_IPLR_MAX("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_scr_iplr_max", "螺杆式冷机负载率最大值", "%", "90", 1, 0, 100, "范围（0-100）", "用于计算冷机负载率加权平均差值P，如采集不到冷机负载率最大值，则取本值进行判断", null, "{\"relation\":{\"classCode\":[\"ACCCCC\",\"ACCCSC\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"},\"infoCode\":{\"key\":\"isDualCondition\",\"value\":\"0\"}}", null),
    ALG_AIR_COOLING_DUA_IPLR_MAX("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_dua_iplr_max", "双工况冷机负载率最大值", "%", "90", 1, 0, 100, "范围（0-100）", "", null, "{\"relation\":{\"classCode\":[\"ACCCCC\",\"ACCCSC\",\"ACCCGH\",\"ACCCWH\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"},\"infoCode\":{\"key\":\"isDualCondition\",\"value\":\"1\"}}", null),
    ALG_AIR_COOLING_GSH_PULR_MAX("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_gsh_pulr_max", "地源热泵负载率最大值", "%", "90", 1, 0, 100, "范围（0-100）", "", null, "{\"relation\":{\"classCode\":[\"ACCCGH\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"},\"infoCode\":{\"key\":\"isDualCondition\",\"value\":\"0\"}}", null),
    ALG_AIR_COOLING_WSH_PULR_MAX("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_wsh_pulr_max", "水源热泵负载率最大值", "%", "90", 1, 0, 100, "范围（0-100）", "", null, "{\"relation\":{\"classCode\":[\"ACCCWH\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"},\"infoCode\":{\"key\":\"isDualCondition\",\"value\":\"0\"}}", null),
    ALG_AIR_COOLING_ABS_IPLR_MAX("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_abs_iplr_max", "吸收式冷机负载率最大值", "%", "90", 1, 0, 100, "范围（0-100）", "", null, "{\"relation\":{\"classCode\":[\"ACCCAC\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"},\"infoCode\":{\"key\":\"isDualCondition\",\"value\":\"0\"}}", null),
    ALG_AIR_COOLING_ADD_AVERAGE_P("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_add_average_P", "判断加机的负载率加权平均差值P", "%", "-3", 1, -100, 100, "范围（-100-100）", "用于判断冷机加机的前置条件之一，P为每台开启冷机的实际负载率与负载率最大值的差值的加权平均数，当P≥阈值则满足加机的负载率前置条件", null, null, null),
    ALG_AIR_COOLING_ADD_OTW_M("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_add_otw_M", "判断加机的出水温度加权平均差值M", "", "1", 1, -10, 10, "范围（-10-10）", "判断加开大机还是加开小机，M为每台开启冷机出水温度设定值与实际出水温度的差值的加权平均数；\n超过加开大机限值，则输出加开大机；超过加开小机限值、但不超过大机限值，则输出加开小机；小于加开小机限值则不加机", null, null, null),
    ALG_AIR_COOLING_ADD_AVERAGE_M("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_add_average_M", "判断加开大机还是小机的加权平均差值M的范围", "", "2,1", 1, -10, 10, "范围（-10-10）", "M为所有冷机出水温度设定值与实际出水温度的差值的加权平均数\n超过加开大机限值，则输出加开大机；超过加开小机限值、但不超过大机限值，则输出加开小机；小于加开小机限值则不加机", null, null, null),
    ALG_AIR_COOLING_ADD_PRE_DURATION("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_add_pre_duration", "满足加机前置条件持续时长", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "满足加机条件持续一定时间，才进行加机操作", null, null, null),
    ALG_AIR_COOLING_ADD_POST_DURATION("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_add_post_duration", "加机后保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "加机后需要保持的时间，该时间段内不会再次执行加机逻辑，避免连续加机", null, null, null),
    ALG_AIR_COOLING_SPACE_LOW_DURATION("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_space_low_duration", "判断空间数量占比过低的持续时长", "min", "60", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    
    
    //间歇供冷加机
    ALG_AIR_COOLING_STOP_HOLD_DURATION("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_stop_hold_duration", "间歇供冷-冷机全部停止后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "冷机减机到0台，进入间歇供冷后，保持一定时间才能再次进行加机判定，避免波动", null, null, null),
    ALG_AIR_COOLING_ADD_TMAX_DURATION("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_add_tmax_duration", "判断加机的负载率加权平均差值P间歇供冷-供水温度上限值与持续时间", "℃,min", "15,30", 1, 0, 99, "范围（0-99）;5的倍数，范围（0-1440）", "冷机进入间歇供冷后，再次加机的两个前置条件。当总管供水温度或泵组支管供水温度超上限，且持续一定时候后，才能进行加机操作", null, null, null),

    //减机前置条件
    ALG_AIR_COOLING_SUB_IPLR_MIN("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_sub_iplr_min", "触发减机判断的冷机负载率下限值", "%", "20", 1, 0, 100, "范围（0-100）", "防止冷机在刚开机的阶段影响减机逻辑", null, null, null),
    ALG_AIR_COOLING_EFC_IPLR_MIN("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_efc_iplr_min", "触发高效组合计算的冷机综合负载率下限值", "%", "80", 1, 0, 100, "范围（0-100）", "综合负载率为冷机负载率的加权平均值，当综合负载率低于设置的阈值才会开始进行减机的逻辑判断", null, null, null),
    ALG_AIR_COOLING_BEST_IPLR_THRESHOLD("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_best_iplr_threshold", "冷机组最优负荷率阈值", "%", "85", 1, 0, 200, "范围（0-200）", "", null, null, null),
    ALG_AIR_COOLING_ONE_IPLR_THRESHOLD("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_one_iplr_threshold", "只开一台冷机减机负载率阈值判断", "%", "45", 1, 0, 100, "范围（0-100）", "", null, null, null),
    ALG_AIR_COOLING_SUB_TIMING("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_sub_timing", "满足减机判断条件的持续时长", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "满足减机条件需要持续的时长，当综合负载率满足减机条件、或最小冷机台数时负载率达到阈值，要持续该时长后才会触发减机", null, null, null),
    ALG_AIR_COOLING_SUB_CHILL_DURATION("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_sub_chill_duration", "减机后保持时间",  "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "减机后需要保持的时间，该时间段内不会再次执行减机逻辑，避免连续减机", null, null, null),
    ALG_AIR_COOLING_NO_SUB_CHILL_TIME("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_no_sub_chill_time", "距离关机结束时间不做减机配置",  "min", "45", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_MUL_IPLR_THRESHOLD("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_mul_iplr_threshold", "多冷机按综合负载率减机阈值", "%", "65", 0, 0, 100, "范围（0-100）", "", null, null, null),
    ALG_AIR_COOLING_MUL_IPLR_DURATION("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_mul_iplr_duration", "多冷机按综合负载率减机持续时长", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_NO_LOAD_M("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_no_load_m", "无负载率主机减机加权平均差值 M", "", "-1", 0, -10, 10, "范围（-10-10）", "", null, null, null),
    ALG_AIR_COOLING_FREE_COOL_DIFF("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_free_cool_diff", "免费冷额定冷量与开启冷机额定冷量偏差", "%", "50", 0, 0, 100, "范围（0-100）", "", null, null, null),
    ALG_AIR_COOLING_WEIGHT_AVG_TEMP_DIFF_M("alg", "group_alg_cooling_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_weight_avg_temp_diff_m", "加权平均温度差值M", "℃", "1", 0, -10, 10, "支持输入一位小数，范围：（-10-10）", "", null, null, null),


    /** 自动需求计算条件输入--空调冷源自动需求计算的条件输入 - 蓄冷工况*/
    ALG_AIR_COOLING_NO_STRA_MAX("alg", "group_alg_cool_store_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_no_stra_max", "不启动蓄冷的蓄冷率上限",  "%", "85", 1, 0, 100, "范围（0-100）", "蓄冷开启后，若蓄冷率大于阈值，则不蓄冷", null, null, null),
    ALG_AIR_COOLING_MACHINE_OPEN_STRA("alg", "group_alg_cool_store_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_machine_open_stra", "基于蓄冷率判定冷机开启台数",  "%", "50", 1, 0, 100, "范围（0-100）", "蓄冷开启后，若蓄冷率大于阈值，双工况冷机全开蓄冷；若蓄冷率小于阈值但大于不启动蓄冷的蓄冷率上限，双工况主机半开蓄冷", null, null, null),

    ALG_AIR_COOLING_STOP_STRA_MIN("alg", "group_alg_cool_store_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_stop_stra_min", "蓄冷停止的蓄冷率下限",  "%", "98", 1, 0, 100, "范围（0-100）", "蓄冷过程中，若蓄冷率大于阈值，按最小可用级台数蓄冷", null, null, null),
    ALG_AIR_COOLING_SUB_RELT_MAX("alg", "group_alg_cool_store_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_sub_relt_max", "蓄冷减机的回液温度上限",  "℃", "-2.5", 1, -100, 100, "范围（-100-100）", "蓄冷过程中，若回液温度低于阈值，冷机台数减1，优先减小机", null, null, null),
    ALG_AIR_COOLING_NUM_CHANGE_DURATION("alg", "group_alg_cool_store_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_num_change_duration", "蓄冷台数变化保持时间",  "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "蓄冷过程中，冷机台数每次发生变化要保持一定时间，避免波动", null, null, null),

    /** 自动需求计算条件输入--空调冷源自动需求计算的条件输入-放冷工况*/
    ALG_AIR_COOLING_RELE_STRA_MAX("alg", "group_alg_cool_release_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_rele_stra_max", "完全放冷蓄冷率上限",  "%", "5", 1, 0, 100, "范围（0-100）", "当蓄冷率≤5%的时候，停止放冷", null, null, null),
    ALG_AIR_COOLING_RELE_OPEN_DURATION("alg", "group_alg_cool_release_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_rele_open_duration", "放冷开状态持续时间",  "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "放冷开启后持续一定时间，再进行满足状态计算", null, null, null),
    ALG_AIR_COOLING_ADD_WTDI_DURATION("alg", "group_alg_cool_release_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_add_wtdi_duration", "判断加机的供水温度差值与持续时间",  "℃", "1,5", 1, 0, 99, "范围（0-99）;5的倍数，范围（0-1440）", "冷冻水供水温度-设定值的差值M大于阈值，且持续一定时间，进行加机", null, null, null),
    ALG_AIR_COOLING_DIS_STRA_MAX("alg", "group_alg_cool_release_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_dis_stra_max", "放冷不满足状态蓄冷率上限",  "%", "10", 1, 0, 100, "范围（0-100）", "蓄冷率小于阈值，放冷进行加机", null, null, null),
    ALG_AIR_COOLING_RELE_CHANGE_DURATION("alg", "group_alg_cool_release_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_rele_change_duration", "放冷冷机台数变化保持时间",  "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "放冷过程中，冷机台数每次发生变化要保持一定时间，避免波动", null, null, null),
    ALG_AIR_COOLING_LOW_DURATION("alg", "group_alg_cool_release_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_low_duration", "平均蓄冷率低于下限值持续时长",  "min", "10", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_STA_DURATION("alg", "group_alg_cool_release_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_sta_duration", "需求冷量预测稳定状态持续时间",  "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, null, null),

    /** 自动需求计算条件输入--空调冷源自动需求计算的条件输入 - 免费冷工况*/
    //免费冷开启
    ALG_AIR_COOLING_FREE_START_TEMP_THRESHOLD("alg", "group_alg_cool_free_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_free_start_temp_threshold", "免费冷开启室外湿球温度阈值",  "℃", "14", 1, 0, 99, "范围（0-99）", "室外湿球温度小于阈值，免费冷开启", null, null, null),
    ALG_AIR_COOLING_FREE_OPEN_DURATION("alg", "group_alg_cool_free_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_free_open_duration", "免费冷开状态持续时长",  "min", "15", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    
    //免费冷关闭
    ALG_AIR_COOLING_FREE_CLOSE_TEMP_THRESHOLD("alg", "group_alg_cool_free_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_free_close_temp_threshold", "免费冷关闭供水温度阈值及持续时间",  "℃", "20,30", 1, 0, 99, "范围（0-99）;5 的倍数，范围（0-1440）", "当冷冻水供水总管温度超上限持续一定时间，免费冷关闭", null, null, null),

    /** 自动需求计算条件输入--空调冷源自动需求计算的条件输入-通用工况*/
    ALG_AIR_COOLING_OPEN_RULE("alg", "group_alg_common_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_open_rule", "冷机开启限制规则",  "", "", 0, 0, 0, "", "最大开启台数上限为选中的冷机数量减1，如选中2台冷机则最多开启其中1台、选中3台冷机则最多开启其中2台", null, "{\"relation\":{\"classCode\":[\"ACCCCC\",\"ACCCSC\",\"ACCCGH\",\"ACCCWH\",\"ACCCAC\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}}", null),
    ALG_AIR_COOLING_FLOW_DEMAND_CONF("alg", "group_alg_common_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_flow_demand_conf", "流量需求配置", "", "", 0, 0, 0, "", "", null, "{\"relation\":{\"classCode\":[\"ACCCCC\",\"ACCCSC\",\"ACCCAC\",\"ACCCGH\",\"ACCCWH\",\"ACGEHE\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}}", null),

    ALG_AIR_COOLING_MACHINE_MIX_ENABLED("alg", "group_alg_common_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_machine_mix_enabled", "是否支持同时开启大小机",  "", "", 0, 0, 0, "", "", null, "{\"querySysGroup\":1}", null),
    ALG_AIR_COOLING_WATER_RETURN_MAX("alg", "group_alg_common_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_water_return_max", "冷却水回水上限值",  "℃", "40", 1, 0, 99, "范围（0-99）", "冷却水回水温度过高持续一定时间，会触发散热异常计算，重新选择冷机组合，避免散热异常", null, "{\"relation\":{\"classCode\":[\"ACCCCC\",\"ACCCSC\",\"ACCCGH\",\"ACCCWH\",\"ACCCAC\",\"ACAHC\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}}", null),
    ALG_AIR_COOLING_WATER_RETURN_DURATION("alg", "group_alg_common_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_water_return_duration", "冷却水回水上限值持续时间",  "min", "30", 0, 1, 1440, "5 的倍数，范围（0-1440）", "冷却水回水温度过高持续一定时间，会触发散热异常计算，重新选择冷机组合，避免散热异常", null, null, null),
    ALG_AIR_COOLING_START_INTERVAL_RULE("alg", "group_alg_common_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_start_interval_rule", "冷机间隔开启限制规则",  "", "", 0, 0, 0, "", "", null, "{\"relation\":{\"classCode\":[\"ACCCCC\",\"ACCCSC\",\"ACCCGH\",\"ACCCWH\",\"ACCCAC\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}}", null),
    ALG_AIR_COOLING_SHIELD_ALL_ENABLED("alg", "group_alg_common_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_shield_all_enabled", "屏蔽所有控制指令",  "", "false", 0, 0, 0, "", "", null, "[{\"name\":\"屏蔽\",\"value\":\"true\"},{\"name\":\"不屏蔽\",\"value\":\"false\"}]", null),
    ALG_AIR_COOLING_SHIELD_COUNT_ENABLED("alg", "group_alg_common_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_shield_count_enabled", "屏蔽台数整定逻辑",  "", "false", 0, 0, 0, "", "", null, "[{\"name\":\"屏蔽\",\"value\":\"true\"},{\"name\":\"不屏蔽\",\"value\":\"false\"}]", null),

    ALG_AIR_COOLING_PUMP_DISH_INTER("alg", "group_alg_common_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_dish_inter", "热泵散热平衡计算间隔时间",  "h", "8760", 0, 0, 17520, "范围（0-17520）", "水/地源热泵不平衡计算间隔时间，例如，每隔一年计算一次", null, null, null),
    ALG_AIR_COOLING_SPLC_IMBA("alg", "group_alg_common_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_splc_imba", "供冷失衡",  "", "1.2", 1, 1, 3, "范围（1-3）", "当地源或水源泵不平衡计算判定为供冷失衡或供热失衡的时候，对应供冷工况或供热工况下，选机的优先级会降低一级，优先选择其他冷热源设备", null, null, null),
    ALG_AIR_COOLING_SPLH_IMBA("alg", "group_alg_common_condition", "空调冷源自动需求计算的条件输入", "alg_air_cooling_splh_imba", "供热失衡",  "", "0.8", 1, 0, 1, "范围（0-1）", "当地源或水源泵不平衡计算判定为供冷失衡或供热失衡的时候，对应供冷工况或供热工况下，选机的优先级会降低一级，优先选择其他冷热源设备", null, null, null),

    /** 自动需求计算条件输入--空调冷源自动需求计算的条件输入-冷机出水温度设定逻辑条件输入*/
    ALG_AIR_COOLING_TEMP_DEFAULT("alg", "group_alg_acac_cold_out_temp", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_default", "出水温度默认设定值",  "℃", "10", 1, 1, 99, "范围（1-99）", "未作调整时默认的冷机出水温度设定值", null, null, null),
    ALG_AIR_COOLING_TEMP_MAX_MIN("alg", "group_alg_acac_cold_out_temp", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_max_min", "出水温度设定值上下限",  "℃", "7,13", 1, 0, 99, "范围（0-99）", "冷机出水温度自动调节的范围限制", null, null, false),
    ALG_AIR_COOLING_TEMP_SET_DURATION("alg", "group_alg_acac_cold_out_temp", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_set_duration", "出水温度设定修改后保持时间", "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "冷机出水温度调节后需要保持的时间，该时间段内不会再次调节", null, null, null),
    ALG_AIR_COOLING_TEMP_OVERRUN_TIME("alg", "group_alg_acac_cold_out_temp", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_overrun_time", "供水温度超出标准可容忍时长", "min", "", 0, 0, 1440, "范围（0-1440）", "", null, null, false),
    ALG_AIR_COOLING_TEMP_RETURN_THRESHOLD("alg", "group_alg_acac_cold_out_temp", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_return_threshold", "回水温度设定值上下限", "℃", "-99,99", 1, -99, 99, "范围（-99-99）", "回水温度自动调节的范围限制", null, null, false),
    ALG_AIR_COOLING_TEMP_SS_DEFAULT("alg", "group_alg_acac_cold_out_temp", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_ss_default", "特殊空间SS下的出水温度默认设定值", "℃", "8", 1, 1, 99, "范围（1-99）", "判断为特殊空间SS的情况下，设置的默认出水温度设定值", null, null, null),
    ALG_AIR_COOLING_TEMP_SS_DIFF("alg", "group_alg_acac_cold_out_temp", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_ss_diff", "特殊空间SS下的出水温度差值ΔT", "℃", "2", 1, 1, 99, "范围（1-99）", "判断为特殊空间SS的情况下，是否能直接下发默认的出水温度设定值：如果默认设定值-当前出水温度设定值≤ΔT，则直接下发默认的出水温度设定值；如果默认设定值-当前出水温度设定值>ΔT，则下发当前设定值+1（避免默认出水温度设定值比当前冷机设定值高太多，导致指令下发后冷机喘振，需要逐步提高温度设定）", null, null, null),
    ALG_AIR_COOLING_TEMP_SS_DURATION("alg", "group_alg_acac_cold_out_temp", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_ss_duration", "特殊空间SS下的出水温度设定修改后保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "判断为特殊空间SS的情况下，冷机出水温度调节后需要保持的时间，该时间段内不会再次调节", null, null, null),
    ALG_AIR_COOLING_TEMP_ADJUST_RANGE("alg", "group_alg_acac_cold_out_temp", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_adjust_range", "冷冻水出水温度调节幅度",  "℃", "1", 1, 0, 99, "范围（0-99）", "", null, null, null),
    ALG_AIR_COOLING_TEMP_MAIN_ADJUST("alg", "group_alg_acac_cold_out_temp", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_main_adjust", "串联冷机主对象出水温度调整幅度", "℃", "0.5", 1, 0, 99, "范围（0-99）", "", null, null, null),
    
    /** 自动需求计算条件输入--空调冷源自动需求计算的条件输入 - 供回水温差/压差逻辑条件输入*/
    ALG_AIR_COOLING_PUMP_CONTROL_MODE("alg", "group_alg_acac_temp_press_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_control_mode", "冷冻水泵控制模式",  "", "", 0, 0, 0, "", "", null, "{\"key\":\"chillPumpType\",\"value\":\"1\",\"classCode\":\"ACAC\"}", null),
    ALG_AIR_COOLING_TEMP_DIFF_DEFAULT("alg", "group_alg_acac_temp_press_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_diff_default", "供回水温差默认设定值",  "℃", "5", 1, 0, 99, "范围（0-99）", "未作调整时默认的泵组供回水温差设定值", null, "{\"key\":\"chillPumpType\",\"value\":\"1\"}", null),
    ALG_AIR_COOLING_TEMP_DIFF_THRESHOLD("alg", "group_alg_acac_temp_press_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_diff_threshold", "供回水温差设定值的上下限",  "℃", "2,8", 1, 0, 99, "范围（0-99）", "供回水温差自动调节的范围限制", null, "{\"key\":\"chillPumpType\",\"value\":\"1\"}", false),
    ALG_AIR_COOLING_TEMP_DIFF_DURATION("alg", "group_alg_acac_temp_press_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_diff_duration", "供回水温差设定修改后保持时间",  "min", "15", 0, 0, 1440, "5的倍数,范围（0-1440）", "供回水温差调节后需要保持的时间，该时间段内不会再次调节", null, "{\"key\":\"chillPumpType\",\"value\":\"1\"}", null),
    ALG_AIR_COOLING_PRES_DIFF_DEFAULT("alg", "group_alg_acac_temp_press_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pres_diff_default", "供回水压差默认设定值",  "kPa", "60", 1, 0, 999, "范围（0-999）", "未作调整时默认的泵组供回水压差设定值", null, "{\"key\":\"chillPumpType\",\"value\":\"1\"}", null),
    ALG_AIR_COOLING_PRES_DIFF_RANGE("alg", "group_alg_acac_temp_press_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pres_diff_range", "压差调整幅度的配置",  "kPa", "10", 1, 0, 999, "范围（0-999）", "每次压差调整时的幅度，即每次增加或减少该数值", null, "{\"key\":\"chillPumpType\",\"value\":\"1\"}", null),
    ALG_AIR_COOLING_PRES_DIFF_THRESHOLD("alg", "group_alg_acac_temp_press_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pres_diff_threshold", "冷冻水供回水压差设定值的上下限",  "kPa", "40,100", 0, 0, 999, "范围（0-999）", "", null, "{\"key\":\"chillPumpType\",\"value\":\"1\"}", false),
    ALG_AIR_COOLING_PRES_DIFF_DURATION("alg", "group_alg_acac_temp_press_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pres_diff_duration", "供回水压差设定修改后保持时间",  "min", "15", 0, 0, 1440, "5的倍数,范围（0-1440）", "供回水压差调节后需要保持的时间，该时间段内不会再次调节", null, "{\"key\":\"chillPumpType\",\"value\":\"1\"}", null),
    ALG_AIR_COOLING_SUPPLY_RETURN_WATER_TEMP_DIFF_CONF("alg", "group_alg_acac_temp_press_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_supply_return_water_temp_diff_conf", "供回水温差调整幅度配置",  "℃", "1", 0, 0, 99, "支持输入一位小数，范围：（0-99）", "", null, "{\"key\":\"chillPumpType\",\"value\":\"1\"}", null),

    /** 自动需求计算条件输入--空调冷源自动需求计算的条件输入-泵组供回水温差/压差逻辑条件输入*/
    ALG_AIR_COOLING_PUMP_TEMP_DIFF_DEFAULT("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_temp_diff_default", "供回水温差默认设定值",  "℃", "5", 1, 0, 99, "范围（0-99）", "未作调整时默认的泵组供回水温差设定值", null, null, null),
    ALG_AIR_COOLING_PUMP_TEMP_DIFF_THRESHOLD("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_temp_diff_threshold", "供回水温差设定值的上下限",  "℃", "2,8", 1, 0, 99, "范围（0-99）", "泵组供回水温差自动调节的范围限制", null, null, false),
    ALG_AIR_COOLING_PUMP_TEMP_DIFF_DURATION("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_temp_diff_duration", "供回水温差设定修改后保持时间",  "min", "15", 0, 0, 1440, "5的倍数,范围（0-1440）", "泵组供回水温差调节后需要保持的时间，该时间段内不会再次调节", null, null, null),
    ALG_AIR_COOLING_PUMP_PRES_DIFF_DEFAULT("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_pres_diff_default", "供回水压差默认设定值",  "kPa", "60", 1, 0, 999, "范围（0-999）", "未作调整时默认的泵组供回水压差设定值", null, null, null),
    ALG_AIR_COOLING_PUMP_PRES_DIFF_RANGE("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_pres_diff_range", "压差调整幅度的配置",  "kPa", "10", 1, 0, 999, "范围（0-999）", "每次压差调整时的幅度，即每次增加或减少该数值", null, null, null),
    ALG_AIR_COOLING_PUMP_PRES_DIFF_THRESHOLD("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_pres_diff_threshold", "供回水压差设定值的上下限",  "kPa", "40,100", 0, 0, 999, "范围（0-999）", "泵组供回水压差自动调节的范围限制", null, null, false),
    ALG_AIR_COOLING_PUMP_PRES_DIFF_DURATION("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_pres_diff_duration", "供回水压差设定修改后保持时间",  "min", "15", 0, 0, 1440, "5的倍数,范围（0-1440）", "泵组供回水压差调节后需要保持的时间，该时间段内不会再次调节", null, null, null),
    ALG_AIR_COOLING_PUMP_PUMP_CONTROL_MODE("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_pump_control_mode", "泵组控制模式",  "", "", 0, 1, 99, "", "", null, "{\"infoCodes\":[\"branchDetaTempSet\",\"branchDetaPressSet\",\"worstTerminalDetaPressSet\"],\"classCode\":\"ACCPC\"}", null),
    ALG_AIR_COOLING_TEMP_DIFF_BEYOND_DURATION("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_diff_beyond_duration", "温差超阈值持续时间",  "min", "15", 0, 0, 1440, "5的倍数,范围（0-1440）", "当供回水温差超过上限值，且持续一定时间，自动调高压差设定值。反之，自动调低压差设定值", null, null, null),
    ALG_AIR_COOLING_TEMP_DIFF_UPPER("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_temp_diff_upper", "供水温度上限值",  "℃", "13", 0, 0, 99, "范围（0-99）", "", null, null, false),
    ALG_AIR_COOLING_PUMP_TEMP_OVERRUN_TIME("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_temp_overrun_time", "供水温度超出标准可容忍时长",  "min", "", 0, 0, 1440, "范围（0-1440）", "", null, null, false),
    ALG_AIR_COOLING_INTERMITTENT_TEMP_UPPER("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_intermittent_temp_upper", "间歇供冷-支管供水温度上限",  "℃", "15", 1, 0, 99, "范围（0-99）", "", null, null, null),
    ALG_AIR_COOLING_INTERMITTENT_DURATION("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_intermittent_duration", "间歇供冷-支管供水温差超上限容忍时长",  "min", "30", 0, 0, 1440, "范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_PUMP_SUPPLY_RETURN_WATER_TEMP_DIFF_CONF("alg", "group_alg_acac_diff", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_supply_return_water_temp_diff_conf", "供回水温差调整幅度配置",  "℃", "1", 0, 0, 99, "支持输入一位小数，范围：（0-99）", "", null, null, null),

    /** 自动需求计算条件输入--空调冷源自动需求计算的条件输入 - 控制组频率约束*/
    //冷冻水泵控制组（一次泵组）
    ALG_AIR_COOLING_PUMP_1_FREQ_RANGE("alg", "group_alg_acac_freq", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_1_freq_range", "频率调节范围", "Hz", "40,50", 1, 0, 99, "范围（0-99）", "", null, "{\"relation\":{\"classCode\":[\"ACCPC\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}}", null),

    //冷冻水泵控制组（二次泵组）
    ALG_AIR_COOLING_PUMP_2_FREQ_RANGE("alg", "group_alg_acac_freq", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_2_freq_range", "频率调节范围", "Hz", "40,50", 1, 0, 99, "范围（0-99）", "", null, "{\"relation\":{\"classCode\":[\"ACCPC\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}}", null),

    //冷热共用水泵控制组
    ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE("alg", "group_alg_acac_freq", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_acppc_freq_range", "频率调节范围", "Hz", "40,50", 1, 0, 99, "范围（0-99）", "", null, "{\"relation\":{\"classCode\":[\"ACPPC\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}}", null),

    //冷却水泵控制组
    ALG_AIR_COOLING_PUMP_ACOPC_FREQ_RANGE("alg", "group_alg_acac_freq", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_acopc_freq_range", "频率调节范围", "Hz", "40,50", 1, 0, 99, "范围（0-99）", "", null, "{\"relation\":{\"classCode\":[\"ACOPC\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}}", null),

    //冷却塔控制组
    ALG_AIR_COOLING_PUMP_ACOTC_FREQ_RANGE("alg", "group_alg_acac_freq", "空调冷源自动需求计算的条件输入", "alg_air_cooling_pump_acotc_freq_range", "频率调节范围", "Hz", "40,50", 1, 0, 99, "范围（0-99）", "", null, "{\"relation\":{\"classCode\":[\"ACOTC\"],\"graphCode\":\"CombineSubset\",\"relCode\":\"Sy2Eg\",\"relDirection\":\"from\"}}", null),

    //室外湿球温度配置
    ALG_AIR_COOLING_WET_BULB_TEMP_RANGE("alg", "group_alg_out_wet_ball_temp", "空调冷源自动需求计算的条件输入", "alg_air_cooling_wet_bulb_temp_range", "室外湿球温度", "℃", "22,26", 1, 0, 99, "范围（0-99）", "", null, null, null),


    /** 自动需求计算条件输入--空调热源自动需求计算条件输入 - 供热开关设定*/
    ALG_AIR_HOT_TEMP_CRACK_THRESHOLD("alg", "group_alg_acah_hot_set", "空调热源自动需求计算条件输入", "alg_air_hot_temp_crack_threshold", "触发冻裂风险的室外低温阈值",  "℃", "4", 1, -100, 100, "范围（-100-100）", "", null, null, null),

    /** 自动需求计算条件输入--空调热源自动需求计算条件输入-加机逻辑*/
    ALG_AIR_HOT_END_TIME("alg", "group_alg_acah_add", "空调热源自动需求计算条件输入", "alg_air_hot_end_time", "距离关机结束时长判断",  "min", "60", 0, 0, 1440, "5的倍数,范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_ADD_TIME("alg", "group_alg_acah_add", "空调热源自动需求计算条件输入", "alg_air_hot_add_time", "满足加机判断条件的持续时长",  "min", "30", 0, 0, 1440, "5的倍数,范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_SPACE_LOW_TIME("alg", "group_alg_acah_add", "空调热源自动需求计算条件输入", "alg_air_hot_space_low_time", "判断空间数量占比过低的时间间隔",  "min", "60", 0, 0, 1440, "5的倍数,范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_TEMP_OUT("alg", "group_alg_acah_add", "空调热源自动需求计算条件输入", "alg_air_hot_temp_out", "判断加机的出水温度加权平均差值M",  "℃", "-5", 1, -10, 10, "范围（-10-10）", "", null, null, null),
    ALG_AIR_HOT_AVERAGE_DIFF("alg", "group_alg_acah_add", "空调热源自动需求计算条件输入", "alg_air_hot_average_diff", "判断加开大机还是小机的加权平均差值M的范围",  "", "2,1", 1, -10, 10, "范围（-10-10）", "", null, null, null),
    ALG_AIR_HOT_AVERAGE_ADD("alg", "group_alg_acah_add", "空调热源自动需求计算条件输入", "alg_air_hot_average_add", "判断加机的负载率加权平均差值P",  "%", "-5", 1, -100, 100, "范围（-100-100）", "", null, null, null),
    ALG_AIR_HOT_TEMP("alg", "group_alg_acah_add", "空调热源自动需求计算条件输入", "alg_air_hot_temp", "间歇供热总管供水温度下限及持续时间",  "℃", "36,30", 1, 0, 99, "范围（0-99）;5的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_TEMP_ALARM("alg", "group_alg_acah_add", "空调热源自动需求计算条件输入", "alg_air_hot_temp_alarm", "防冻报警总管供水温度下限及持续时间",  "℃", "6,15", 1, 0, 99, "范围（0-99）;5的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_LAST_TIME("alg", "group_alg_acah_add", "空调热源自动需求计算条件输入", "alg_air_hot_last_time", "热源台数为0大0小持续时长",  "min", "30", 0, 0, 1440, "5的倍数,范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_HOLD_TIME("alg", "group_alg_acah_add", "空调热源自动需求计算条件输入", "alg_air_hot_keep_time", "加机后保持时间",  "min", "30", 0, 0, 1440, "5的倍数,范围（0-1440）", "加机后需要保持的时间，该时间段内不会再次执行加机逻辑，避免连续加机", null, null, null),

    /** 自动需求计算条件输入--空调热源自动需求计算条件输入-减机逻辑*/
    ALG_AIR_HOT_NO_LOAD_RATE("alg", "group_alg_acah_sub", "空调热源自动需求计算条件输入", "alg_air_hot_no_load_rate", "未交付负载率的主机加权平均温度差值M及持续时间",  "℃", "4,30", 1, 0, 99, "范围（0-99）;5的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_LOW_SUB("alg", "group_alg_acah_sub", "空调热源自动需求计算条件输入", "alg_air_hot_low_sub", "减机最低负载率判断阈值",  "%", "20", 1, 0, 100, "范围（0-100）", "", null, null, null),
    ALG_AIR_HOT_MUL_SUB("alg", "group_alg_acah_sub", "空调热源自动需求计算条件输入", "alg_air_hot_mul_sub", "多热源按综合负载率减机阈值",  "%", "65", 1, 0, 100, "范围（0-100）", "", null, null, null),
    ALG_AIR_HOT_MUL_TIME("alg", "group_alg_acah_sub", "空调热源自动需求计算条件输入", "alg_air_hot_mul_time", "多热源按综合负载率减机持续时长",  "min", "30", 0, 0, 1440, "5的倍数,范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_AVER_TEMP("alg", "group_alg_acah_sub", "空调热源自动需求计算条件输入", "alg_air_hot_aver_temp", "加权平均温度差值M",  "℃", "-5", 1, -10, 10, "范围（-10-10）", "", null, null, null),
    ALG_AIR_HOT_SUB_LIMIT("alg", "group_alg_acah_sub", "空调热源自动需求计算条件输入", "alg_air_hot_sub_limit", "开启单台设备触发减机的负载率上限判断",  "%", "45", 1, 0, 100, "范围（0-100）", "", null, null, null),
    ALG_AIR_HOT_SUB_TIME("alg", "group_alg_acah_sub", "空调热源自动需求计算条件输入", "alg_air_hot_sub_time", "满足减机判断条件的持续时长",  "min", "30", 0, 0, 1440, "5的倍数,范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_SUB_END_TIME("alg", "group_alg_acah_sub", "空调热源自动需求计算条件输入", "alg_air_hot_sub_end_time", "距离关机结束时长配置",  "min", "45", 0, 0, 1440, "5的倍数,范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_MUL_LIMIT("alg", "group_alg_acah_sub", "空调热源自动需求计算条件输入", "alg_air_hot_mul_limit", "综合负荷率阈值上限",  "%", "80", 1, 0, 100, "范围（0-100）", "", null, null, null),
    ALG_AIR_HOT_COM_LIMIT("alg", "group_alg_acah_sub", "空调热源自动需求计算条件输入", "alg_air_hot_com_limit", "组合负荷率阈值上限",  "%", "85", 1, 0, 100, "范围（0-100）", "", null, null, null),
    ALG_AIR_HOT_COM_LOW_TIME("alg", "group_alg_acah_sub", "空调热源自动需求计算条件输入", "alg_air_hot_com_low_time", "组合负荷率低于上限持续时长",  "min", "30", 0, 0, 1440, "5的倍数,范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_SUB_DURATION("alg", "group_alg_acah_sub", "空调热源自动需求计算条件输入", "alg_air_hot_sub_duration", "减机后保持时间",  "min", "30", 0, 0, 1440, "5的倍数,范围（0-1440）", "", null, null, null),

    /** 自动需求计算条件输入--空调热源自动需求计算条件输入-安全状态计算*/
    ALG_AIR_HOT_IN_TEMP("alg", "group_alg_cooling_safe_run_hot", "空调热源自动需求计算条件输入", "alg_air_hot_in_temp", "水地源热泵蒸发器进水温度平均值上限及持续时长",  "℃", "7,30", 1, 0, 99, "范围（0-99）;5的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_RELEASE_RISK("alg", "group_alg_cooling_safe_run_hot", "空调热源自动需求计算条件输入", "alg_air_hot_release_risk", "水地源热泵解除风险的下限值及持续时长",  "℃", "9,30", 1, 0, 99, "范围（0-99）;5的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_OUTDOOR_TEMP("alg", "group_alg_cooling_safe_run_hot", "空调热源自动需求计算条件输入", "alg_air_hot_outdoor_temp", "空气源热泵工作室外温度下限",  "℃", "10", 1, -100, 100, "范围（-100-100）", "", null, null, null),


    /** 自动需求计算条件输入--空调热源自动需求计算条件输入-出水温度设定逻辑条件（针对单个热站）*/
    ALG_AIR_HOT_TEMP_MAX_MIN("alg", "group_alg_acah_out_temp", "空调热源自动需求计算条件输入", "alg_air_hot_temp_max_min", "出水温度设定值上下限",  "℃", "30,50", 0, 0, 99, "范围（0-99）", "出水温度自动调节的范围限制", null, null, false),
    ALG_AIR_HOT_TEMP_OVERRUN_TIME("alg", "group_alg_acah_out_temp", "空调热源自动需求计算条件输入", "alg_air_hot_temp_overrun_time", "供水温度超出标准可容忍时长",  "min", "", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, null, false),
    ALG_AIR_HOT_TEMP_OUT_SET("alg", "group_alg_acah_out_temp", "空调热源自动需求计算条件输入", "alg_air_hot_temp_out_set", "出水温度设定值的默认",  "℃", "40", 1, 0, 99, "范围（0-99）", "", null, null, null),
    ALG_AIR_HOT_TEMP_KEEP_TIME("alg", "group_alg_acah_out_temp", "空调热源自动需求计算条件输入", "alg_air_hot_temp_keep_time", "出水温度设定修改后保持时间",  "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_TEMP_OUT_RANGE("alg", "group_alg_acah_out_temp", "空调热源自动需求计算条件输入", "alg_air_hot_temp_out_range", "采暖水出水温度设定调整幅度",  "℃", "2", 1, 0, 99, "范围（0-99）", "", null, null, null),
    ALG_AIR_HOT_DEV_P("alg", "group_alg_acah_out_temp", "空调热源自动需求计算条件输入", "alg_air_hot_dev_p", "负载率加权平均偏差P及持续时间",  "%", "-15,30", 1, -100, 100, "范围（-100-100）;5的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_TEMP_ALTER_TIME("alg", "group_alg_acah_out_temp", "空调热源自动需求计算条件输入", "alg_air_hot_temp_alter_time", "特殊空间SS下的出水温度设定修改后保持时",  "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_TEMP_OUT_SET_SS("alg", "group_alg_acah_out_temp", "空调热源自动需求计算条件输入", "alg_air_hot_temp_out_set_ss", "特殊空间SS下的出水温度默认设定值",  "℃", "40", 1, 0, 99, "范围（0-99）", "", null, null, null),
    ALG_AIR_HOT_TEMP_OUT_DIFF("alg", "group_alg_acah_out_temp", "空调热源自动需求计算条件输入", "alg_air_hot_temp_out_diff", "特殊空间SS下的出水温度差值ΔT",  "℃", "4", 1, 0, 99, "范围（0-99）", "", null, null, null),


    /** 自动需求计算条件输入--空调热源自动需求计算条件输入-温差/压差设定逻辑条件（针对单个热站）*/
    ALG_AIR_HOT_PUMP_CONTROL_MODE("alg", "group_alg_acah_hot_diff", "空调热源自动需求计算条件输入", "alg_air_hot_pump_control_mode", "水泵控制模式",  "", "", 0, 0, 0, "", "", null, "{\"key\":\"heatPumpType\",\"value\":\"1\",\"classCode\":\"ACAH\"}", null),
    ALG_AIR_HOT_TEMP_DIFF_THRESHOLD("alg", "group_alg_acah_hot_diff", "空调热源自动需求计算条件输入", "alg_air_hot_temp_diff_threshold", "供回水温差设定值的上下限",  "℃", "5,15", 0, 0, 99, "范围（0-99）", "泵组供回水温差自动调节的范围限制", null, "{\"key\":\"heatPumpType\",\"value\":\"1\",\"classCode\":\"ACAH\"}", false),
    ALG_AIR_HOT_PRES_DIFF_THRESHOLD("alg", "group_alg_acah_hot_diff", "空调热源自动需求计算条件输入", "alg_air_hot_pres_diff_threshold", "供回水压差设定值的上下限",  "kPa", "40,100", 0, 0, 999, "范围（0-999）", "泵组供回水压差自动调节的范围限制", null, "{\"key\":\"heatPumpType\",\"value\":\"1\",\"classCode\":\"ACAH\"}", false),
    ALG_AIR_HOT_TEMP_DIFF("alg", "group_alg_acah_hot_diff", "空调热源自动需求计算条件输入", "alg_air_hot_temp_diff", "供回水温差默认设定值",  "℃", "5", 1, 0, 99, "范围（0-99）", "", null, "{\"key\":\"heatPumpType\",\"value\":\"1\",\"classCode\":\"ACAH\"}", null),
    ALG_AIR_HOT_TEMP_RANGE("alg", "group_alg_acah_hot_diff", "空调热源自动需求计算条件输入", "alg_air_hot_temp_range", "采暖热水供回水温差设定调整幅度",  "℃", "2", 1, 0, 99, "范围（0-99）", "", null, "{\"key\":\"heatPumpType\",\"value\":\"1\",\"classCode\":\"ACAH\"}", null),
    ALG_AIR_HOT_TEMP_SET_DURATION("alg", "group_alg_acah_hot_diff", "空调热源自动需求计算条件输入", "alg_air_hot_temp_set_duration", "供回水温差设定修改后保持时间",  "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, "{\"key\":\"heatPumpType\",\"value\":\"1\",\"classCode\":\"ACAH\"}", null),
    ALG_AIR_HOT_PRE_DIFF("alg", "group_alg_acah_hot_diff", "空调热源自动需求计算条件输入", "alg_air_hot_pre_diff", "供回水压差默认设定值",  "kPa", "60", 1, 0, 999, "范围（0-999）", "", null, "{\"key\":\"heatPumpType\",\"value\":\"1\",\"classCode\":\"ACAH\"}", null),
    ALG_AIR_HOT_PRE_RANGE("alg", "group_alg_acah_hot_diff", "空调热源自动需求计算条件输入", "alg_air_hot_pre_range", "压差调整幅度的配置",  "kPa", "10", 1, 0, 999, "范围（0-999）", "", null, "{\"key\":\"heatPumpType\",\"value\":\"1\",\"classCode\":\"ACAH\"}", null),
    ALG_AIR_HOT_PRE_SET_DURATION("alg", "group_alg_acah_hot_diff", "空调热源自动需求计算条件输入", "alg_air_hot_pre_set_duration", "供回水压差设定修改后保持时间",  "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, "{\"key\":\"heatPumpType\",\"value\":\"1\",\"classCode\":\"ACAH\"}", null),


    /** 自动需求计算条件输入--空调热源自动需求计算条件输入-温差/压差设定逻辑条件（针对单个泵组）*/
    ALG_AIR_HOT_TEMP_DIFF_LOWER("alg", "group_alg_acah_pump_diff", "空调热源自动需求计算条件输入", "alg_air_hot_temp_diff_lower", "供水温度下限值",  "℃", "", 0, 0, 99, "范围（0-99）", "", null, null, false),
    ALG_AIR_HOT_PUMP_TEMP_OVERRUN_TIME("alg", "group_alg_acah_pump_diff", "空调热源自动需求计算条件输入", "alg_air_hot_pump_temp_overrun_time", "供水温度超出标准可容忍时长",  "min", "", 0, 0, 1440, "范围（0-1440）", "", null, null, false),
    ALG_AIR_HOT_PUMP_TEMP_DIFF_THRESHOLD("alg", "group_alg_acah_pump_diff", "空调热源自动需求计算条件输入", "alg_air_hot_pump_temp_diff_threshold", "供回水温差设定值的上下限",  "℃", "5,15", 0, 0, 99, "范围（0-99）", "泵组供回水温差自动调节的范围限制", null, null, false),
    ALG_AIR_HOT_PUMP_PRES_DIFF_THRESHOLD("alg", "group_alg_acah_pump_diff", "空调热源自动需求计算条件输入", "alg_air_hot_pump_pres_diff_threshold", "供回水压差设定值的上下限",  "kPa", "40,100", 0, 0, 999, "范围（0-999）", "泵组供回水压差自动调节的范围限制", null, null, false),
    ALG_AIR_HOT_PUMP_PUMP_CONTROL_MODE("alg", "group_alg_acah_pump_diff", "空调热源自动需求计算条件输入", "alg_air_hot_pump_pump_control_mode", "泵组控制模式",  "", "", 0, 0, 0, "", "", null, "{\"infoCodes\":[\"branchDetaTempSet\",\"branchDetaPressSet\",\"worstTerminalDetaPressSet\"],\"classCode\":\"ACHPC,ACPPC\"}", null),
    ALG_AIR_HOT_SURE_TEMP("alg", "group_alg_acah_pump_diff", "空调热源自动需求计算条件输入", "alg_air_hot_sure_temp", "供回水温差默认设定值",  "℃", "5", 1, 0, 99, "范围（0-99）", "", null, null, null),
    ALG_AIR_HOT_SURE_RANGE("alg", "group_alg_acah_pump_diff", "空调热源自动需求计算条件输入", "alg_air_hot_sure_range", "采暖热水供回水温差设定调整幅度",  "℃", "2", 1, 0, 99, "范围（0-99）", "", null, null, null),
    ALG_AIR_HOT_SURE_DURATION("alg", "group_alg_acah_pump_diff", "空调热源自动需求计算条件输入", "alg_air_hot_sure_duration", "供回水温差设定修改后保持时间",  "min", "30", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_HOT_SURE_PRE("alg", "group_alg_acah_pump_diff", "空调热源自动需求计算条件输入", "alg_air_hot_sure_pre", "供回水压差默认设定值",  "kPa", "60", 1, 0, 999, "范围（0-999）", "", null, null, null),
    ALG_AIR_HOT_SURE_PRE_RANGE("alg", "group_alg_acah_pump_diff", "空调热源自动需求计算条件输入", "alg_air_hot_sure_pre_range", "压差调整幅度的配置",  "kPa", "10", 1, 0, 999, "范围（0-999）", "", null, null, null),
    ALG_AIR_HOT_SURE_PRE_DURATION("alg", "group_alg_acah_pump_diff", "空调热源自动需求计算条件输入", "alg_air_hot_sure_pre_duration", "供回水压差设定修改后保持时间",  "min", "15", 0, 0, 1440, "5的倍数，范围（0-1440）", "", null, null, null),

    /** 自动需求计算条件输入--空调冷热源自动计算条件输入*/
    //供冷台数计算 - 供冷模式
    ALG_AIR_COOLING_HOT_ADD_FORBID_DURATION("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_forbid_duration", "关机前禁止加机策略时长", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_LOAD_RATE_P("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_load_rate_p", "判断加机的负载率加权平均差值 P", "%", "-3", 1, -100, 100, "范围（-100-100）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_TEMP_M("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_temp_m", "判断加机的出水温度加权平均差值 M", "℃", "1", 1, 0, 99, "范围（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_TEMP_M_RANGE("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_temp_m_range", "判断加开大机还是小机的加权平均差值 M'的范围", "", "2,1", 1, -10, 10, "范围（-10-10）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_TIMING("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_timing", "满足加机判断条件的持续时长", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_SPACE_LOW_TIME("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_space_low_time", "判断未来空间数量占比过低的时长", "min", "60", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_HOLD_TIME("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_hold_time", "加机后保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),

    // 间歇供冷加机
    ALG_AIR_COOLING_HOT_STOP_HOLD_DURATION("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_stop_hold_duration", "间歇供冷-机组全部停止后保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_TMAX_DURATION("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_tmax_duration", "判断加机的负载率加权平均差值P间歇供冷-供水温度上限值与持续时间", "℃", "15,30", 1, 0, 99, "范围（0-99）;5的倍数，范围（0-1440）", "", null, null, null),

    // 减机逻辑配置
    ALG_AIR_COOLING_HOT_NO_LOAD_RATE("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_no_load_rate", "未交付负载率的主机加权平均温度差值 M 及持续时间", "℃", "-1,30", 1, -10, 10, "范围：（-10-10）;5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_LOW_SUB("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_low_sub", "减机最低负载率判断阈值", "%", "20", 0, 0, 100, "范围：（0-100）", "", null, null, null),
    ALG_AIR_COOLING_HOT_AVER_TEMP("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_aver_temp", "加权平均温度差值 M", "℃", "1", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_MUL_LIMIT("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_mul_limit", "多主机综合负载率阈值", "%", "80", 0, 0, 100, "范围：（0-100）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SUB_LIMIT_DUR("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_sub_limit_dur", "开启单台主机减机的负载率阈值及持续时长", "%", "45,30", 0, 0, 100, "范围：（0-100）;5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_COM_LIMIT_DUR("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_com_limit_dur", "高效负荷率阈值上限及持续时长", "%", "85,30", 0, 0, 100, "范围：（0-100）;5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_NO_SUB_CHILL_TIME("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_no_sub_chill_time", "开单台主机时--关机前禁止减机策略时长", "min", "45", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_MUL_TIME("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_mul_time", "多主机按综合负载率减机阈值及持续时长", "%", "65,30", 0, 0, 100, "范围：（0-100）;5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SUB_CHILL_DURATION("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_sub_chill_duration", "减机后保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_FREE_PLATE_EXCHANGER_CAPACITY("alg", "group_alg_cooling_unit_num", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_free_plate_exchanger_capacity", "免费冷板换比开启冷机额定冷量偏高幅度", "%", "50", 0, 0, 100, "范围：（0-100）", "", null, null, null),


    //供水温度设定 - 供冷模式
    ALG_AIR_COOLING_HOT_SUPPLY_WATER_TEMP_SET("alg", "group_alg_cooling_supply_temp", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_supply_water_temp_set", "制冷供水温度默认设定值", "℃", "10", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SUPPLY_WATER_TEMP_LIMIT("alg", "group_alg_cooling_supply_temp", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_supply_water_temp_limit", "制冷供水温度设定值上下限", "℃", "7,13", 1, 0, 99, "范围：（0-99），上限≥下限", "", null, null, null),
    ALG_AIR_COOLING_HOT_LOAD_RATE_DIFF_DUR("alg", "group_alg_cooling_supply_temp", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_load_rate_diff_dur", "加权平均负载率差值及持续时长", "%", "-15,30", 1, -100, 100, "范围：（-100-100）;5的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SUPPLY_WATER_TEMP_ADJ("alg", "group_alg_cooling_supply_temp", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_supply_water_temp_adj", "制冷供水温度设定调节幅度", "℃", "1", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SUPPLY_WATER_TEMP_HOLD("alg", "group_alg_cooling_supply_temp", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_supply_water_temp_hold", "制冷供水温度设定修改后保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SUPPLY_WATER_TEMP_OVERRUN("alg", "group_alg_cooling_supply_temp", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_supply_water_temp_overrun", "制冷供水温度超出标准可容忍时长", "min", "60", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SS_SUPPLY_WATER_TEMP_SET("alg", "group_alg_cooling_supply_temp", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_ss_supply_water_temp_set", "特殊空间 SS 下的制冷供水温度默认设定值", "℃", "8", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SS_SUPPLY_WATER_TEMP_DIFF("alg", "group_alg_cooling_supply_temp", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_ss_supply_water_temp_diff", "特殊空间 SS 下的制冷供水温度差值ΔT", "℃", "2", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SS_SUPPLY_WATER_TEMP_HOLD("alg", "group_alg_cooling_supply_temp", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_ss_supply_water_temp_hold", "特殊空间 SS 下的制冷供水温度设定修改后保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),

    //安全运行-供冷模式
    ALG_AIR_COOLING_HOT_COOLING_WATER_RETURN_LIMIT("alg", "group_alg_cooling_safe_run", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_cooling_water_return_limit", "冷却水回水上限值", "℃", "40", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_COOLING_WATER_RETURN_HOLD("alg", "group_alg_cooling_safe_run", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_cooling_water_return_hold", "冷却水回水上限值持续时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),

    //供回水温差/压差逻辑条件输入 - 供冷模式
    ALG_AIR_COOLING_HOT_COLD_HOT_WATER_PUMP_MODE("alg", "group_alg_cooling_temp_press_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_cold_hot_water_pump_mode", "冷热水泵控制模式", "", "", 0, 0, 0, "", "", null, null, null),
    ALG_AIR_COOLING_HOT_TEMP_DIFF_SET("alg", "group_alg_cooling_temp_press_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_temp_diff_set", "供回水温差默认设定值", "℃", "5", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_TEMP_DIFF_LIMIT("alg", "group_alg_cooling_temp_press_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_temp_diff_limit", "供回水温差设定值的上下限", "℃", "2,8", 1, 0, 99, "范围：（0-99），上限≥下限", "", null, null, null),
    ALG_AIR_COOLING_HOT_TEMP_DIFF_HOLD("alg", "group_alg_cooling_temp_press_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_temp_diff_hold", "供回水温差设定修改后保持时间", "min", "15", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_TEMP_DIFF_ADJ("alg", "group_alg_cooling_temp_press_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_temp_diff_adj", "供回水温差调整幅度", "℃", "1", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_PRES_DIFF_SET("alg", "group_alg_cooling_temp_press_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pres_diff_set", "供回水压差默认设定值", "kPa", "60", 1, 0, 999, "范围：（0-999）", "", null, null, null),
    ALG_AIR_COOLING_HOT_PRES_DIFF_ADJ("alg", "group_alg_cooling_temp_press_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pres_diff_adj", "压差调整幅度的配置", "kPa", "10", 1, 0, 999, "范围：（0-999）", "", null, null, null),
    ALG_AIR_COOLING_HOT_PRES_DIFF_LIMIT("alg", "group_alg_cooling_temp_press_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pres_diff_limit", "制冷供回水压差设定值的上下限", "kPa", "40,100", 1, 0, 999, "范围：（0-999），上限≥下限", "", null, null, null),
    ALG_AIR_COOLING_HOT_PRES_DIFF_HOLD("alg", "group_alg_cooling_temp_press_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pres_diff_hold", "供回水压差设定修改后保持时间", "min", "15", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    
    //泵组供回水温差/压差逻辑条件输入 - 供冷模式
    ALG_AIR_COOLING_HOT_PUMP_TEMP_DIFF_SET("alg", "group_alg_cooling_pump_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pump_temp_diff_set", "供回水温差默认设定值", "℃", "5", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_PUMP_TEMP_DIFF_LIMIT("alg", "group_alg_cooling_pump_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pump_temp_diff_limit", "供回水温差设定值的上下限", "℃", "2,8", 1, 0, 99, "范围：（0-99），上限≥下限", "", null, null, null),
    ALG_AIR_COOLING_HOT_PUMP_TEMP_DIFF_HOLD("alg", "group_alg_cooling_pump_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pump_temp_diff_hold", "供回水温差设定修改后保持时间", "min", "15", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_PUMP_TEMP_DIFF_ADJ("alg", "group_alg_cooling_pump_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pump_temp_diff_adj", "供回水温差调整幅度", "℃", "1", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_PUMP_PRES_DIFF_SET("alg", "group_alg_cooling_pump_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pump_pres_diff_set", "供回水压差默认设定值", "kPa", "60", 1, 0, 999, "范围：（0-999）", "", null, null, null),
    ALG_AIR_COOLING_HOT_PUMP_PRES_DIFF_ADJ("alg", "group_alg_cooling_pump_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pump_pres_diff_adj", "压差调整幅度的配置", "kPa", "10", 1, 0, 999, "范围：（0-999）", "", null, null, null),
    ALG_AIR_COOLING_HOT_PUMP_PRES_DIFF_LIMIT("alg", "group_alg_cooling_pump_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pump_pres_diff_limit", "供回水压差设定值的上下限", "kPa", "40,100", 1, 0, 999, "范围：（0-999），上限≥下限", "", null, null, null),
    ALG_AIR_COOLING_HOT_PUMP_PRES_DIFF_HOLD("alg", "group_alg_cooling_pump_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pump_pres_diff_hold", "供回水压差设定修改后保持时间", "min", "15", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_PUMP_PUMP_CONTROL_MODE("alg", "group_alg_cooling_pump_diff", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_pump_pump_control_mode", "泵组控制模式", "", "", 0, 0, 0, "", "", null, null, null),

    //开关设定 - 供热模式
    ALG_AIR_COOLING_HOT_OUTDOOR_LOW_TEMP_THRESHOLD("alg", "group_alg_cooling_switch_set", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_outdoor_low_temp_threshold", "触发冻裂风险的室外低温阈值", "℃", "4", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    
    //台数计算 - 供热模式
    //加机条件
    ALG_AIR_COOLING_HOT_ADD_FORBID_DURATION_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_forbid_duration_h", "间歇供热--关机前禁止加机策略时长", "min", "60", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_LOAD_RATE_P_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_load_rate_p_h", "判断加机的负载率加权平均差值 P", "%", "-5", 0, -100, 100, "范围：（-100-100）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_TEMP_M_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_temp_m_h", "判断加机的出水温度加权平均差值 M", "℃", "-5", 1, -10, 10, "范围：（-10-10）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_TEMP_M_RANGE_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_temp_m_range_h", "判断加开大机还是小机的加权平均差值 M'的范围", "", "8,5", 1, -10, 10, "范围：（-10-10）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_TIMING_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_timing_h", "满足加机判断条件的持续时长", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_SPACE_LOW_TIME_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_space_low_time_h", "判断未来空间数量占比过低的时长", "min", "60", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ADD_HOLD_TIME_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_add_hold_time_h", "加机后台数保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_ANTI_FREEZE_TEMP_DUR_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_anti_freeze_temp_dur_h", "防冻报警总管供水温度下限及持续时间", "℃", "6,15", 1, 0, 99, "范围：（0-99）;5 的倍数，范围（0-1440）", "", null, null, null),

    //间歇供热
    ALG_AIR_COOLING_HOT_ZERO_HOST_DUR_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_zero_host_dur_h", "主机台数为 0 大 0 小持续时长", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_LOW_TEMP_DUR_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_low_temp_dur_h", "间歇供热总管供水温度下限及持续时间", "℃", "36,30", 1, 0, 99, "范围：（0-99）;5 的倍数，范围（0-1440）", "", null, null, null),

    //减机条件
    ALG_AIR_COOLING_HOT_NO_LOAD_RATE_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_no_load_rate_h", "未交付负载率的主机加权平均温度差值 M 及持续时间", "℃", "4,30", 1, 0, 99, "范围：（0-99）;5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_LOW_SUB_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_low_sub_h", "减机最低负载率判断阈值", "%", "20", 0, 0, 100, "范围：（0-100）", "", null, null, null),
    ALG_AIR_COOLING_HOT_AVER_TEMP_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_aver_temp_h", "加权平均温度差值 M", "℃", "-1", 1, -10, 10, "范围：（-10-10）", "", null, null, null),
    ALG_AIR_COOLING_HOT_MUL_LIMIT_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_mul_limit_h", "多主机综合负载率阈值", "%", "85", 0, 0, 100, "范围：（0-100）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SUB_LIMIT_DUR_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_sub_limit_dur_h", "开启单台主机减机的负载率阈值及持续时长", "%", "45,30", 0, 0, 100, "范围：（0-100）;5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_COM_LIMIT_DUR_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_com_limit_dur_h", "高效负荷率阈值上限及持续时长", "%", "85,30", 0, 0, 100, "范围：（0-100）;5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_NO_SUB_CHILL_TIME_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_no_sub_chill_time_h", "开单台主机时--关机前禁止减机策略时长", "min", "45", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_MUL_TIME_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_mul_time_h", "多主机按综合负载率减机阈值及持续时长", "%", "65,30", 0, 0, 100, "范围：（0-100）;5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SUB_CHILL_DURATION_H("alg", "group_alg_cooling_unit_num_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_sub_chill_duration_h", "减机后保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    
    //安全运行 - 供热模式
    ALG_AIR_COOLING_HOT_WATER_GSHP_EVAP_INLET_TEMP_UP_LIMIT_H("alg", "group_alg_cooling_safe_run_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_water_gshp_evap_inlet_temp_up_limit_h", "水地源热泵蒸发器进水温度平均值上限及持续时长", "℃", "7,30", 1, 0, 99, "范围：（0-99）;5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_WATER_GSHP_RISK_RELIEVE_LOW_LIMIT_H("alg", "group_alg_cooling_safe_run_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_water_gshp_risk_relieve_low_limit_h", "水地源热泵解除风险的下限值及持续时长", "℃", "9,30", 1, 0, 99, "范围：（0-99）;5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_AIR_SOURCE_OUTDOOR_TEMP_LOW_LIMIT_H("alg", "group_alg_cooling_safe_run_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_air_source_outdoor_temp_low_limit_h", "空气源热泵工作室外温度下限", "℃", "10", 0, 0, 99, "范围：（0-99）", "", null, null, null),
    
    //供水温度设定 - 供热模式
    ALG_AIR_COOLING_HOT_HEATING_SUPPLY_TEMP_DEFAULT("alg", "group_alg_cooling_supply_temp_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_supply_temp_default", "出水温度设定值的默认", "℃", "40", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_SUPPLY_TEMP_LIMIT("alg", "group_alg_cooling_supply_temp_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_supply_temp_limit", "制热供水温度设定值的上下限", "℃", "35,50", 1, 0, 99, "范围：（0-99），上限≥下限", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_SUPPLY_TEMP_HOLD_TIME("alg", "group_alg_cooling_supply_temp_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_supply_temp_hold_time", "制热供水温度设定修改后保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_SUPPLY_TEMP_ADJUST("alg", "group_alg_cooling_supply_temp_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_supply_temp_adjust", "制热供水温度设定调节幅度", "℃", "2", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_LOAD_RATE_DIFF_DUR("alg", "group_alg_cooling_supply_temp_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_load_rate_diff_dur", "加权平均负载率差值及持续时长", "%", "-15,30", 0, -100, 100, "范围：（-100-100）;5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SPECIAL_SPACE_HEATING_HOLD_TIME("alg", "group_alg_cooling_supply_temp_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_special_space_heating_hold_time", "特殊空间 SS 下的制热供水温度设定修改后保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SPECIAL_SPACE_HEATING_DEFAULT("alg", "group_alg_cooling_supply_temp_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_special_space_heating_default", "特殊空间 SS 下的制热供水温度默认设定值", "℃", "40", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_SPECIAL_SPACE_HEATING_TEMP_DIFF("alg", "group_alg_cooling_supply_temp_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_special_space_heating_temp_diff", "特殊空间 SS 下的制热供水温度差值ΔT", "℃", "4", 1, 0, 99, "范围：（0-99）", "", null, null, null),

    //供回水温差/压差逻辑条件输入 - 供热模式
    ALG_AIR_COOLING_HOT_HEATING_PUMP_CONTROL_MODE("alg", "group_alg_cooling_temp_press_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_pump_control_mode", "冷热水泵控制模式", "", "", 0, null, null, "", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_DELTA_TEMP_DEFAULT("alg", "group_alg_cooling_temp_press_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_delta_temp_default", "制热供回水温差设定值默认", "℃", "5", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_DELTA_TEMP_LIMIT("alg", "group_alg_cooling_temp_press_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_delta_temp_limit", "制热供回水温差设定值的上下限", "℃", "5,15", 1, 0, 99, "范围：（0-99），上限≥下限", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_DELTA_TEMP_ADJUST("alg", "group_alg_cooling_temp_press_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_delta_temp_adjust", "制热供回水温差设定调整幅度", "℃", "2", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_DELTA_TEMP_HOLD_TIME("alg", "group_alg_cooling_temp_press_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_delta_temp_hold_time", "供回水温差设定修改后保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_DELTA_PRESS_DEFAULT("alg", "group_alg_cooling_temp_press_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_delta_press_default", "制热供回水压差设定值默认", "kpa", "60", 1, 0, 999, "范围：（0-999）", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_DELTA_PRESS_ADJUST("alg", "group_alg_cooling_temp_press_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_delta_press_adjust", "制热供回水压差设定调整幅度", "kpa", "10", 1, 0, 999, "范围：（0-999）", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_DELTA_PRESS_LIMIT("alg", "group_alg_cooling_temp_press_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_delta_press_limit", "制热供回水压差设定值的上下限", "kpa", "40,100", 1, 0, 999, "范围：（0-999），上限≥下限", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_DELTA_PRESS_HOLD_TIME("alg", "group_alg_cooling_temp_press_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_delta_press_hold_time", "供回水压差设定修改后保持时间", "min", "15", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),

    //泵组供回水温差/压差逻辑条件输入 - 供热模式
    ALG_AIR_COOLING_HOT_BRANCH_PUMP_CONTROL_MODE("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_branch_pump_control_mode", "冷热水泵控制模式", "", "", 0, null, null, "", "", null, null, null),
    ALG_AIR_COOLING_HOT_INTERMITTENT_BRANCH_TEMP_UP("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_intermittent_branch_temp_up", "间歇供冷 - 支管供水温度上限", "℃", "15", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_INTERMITTENT_BRANCH_TEMP_UP_TOLERANCE("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_intermittent_branch_temp_up_tolerance", "间歇供冷 - 支管供水温差超上限容忍时长", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_INTERMITTENT_HEAT_BRANCH_TEMP_LOW("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_intermittent_heat_branch_temp_low", "间歇供热 - 支管供水温度下限", "℃", "30", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_INTERMITTENT_BRANCH_TEMP_LOW_TOLERANCE("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_intermittent_branch_temp_low_tolerance", "间歇供冷 - 支管供水温差低于下限容忍时长", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_BRANCH_HEATING_DELTA_TEMP_DEFAULT("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_branch_heating_delta_temp_default", "制热供回水温差设定值默认", "℃", "5", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_BRANCH_HEATING_DELTA_TEMP_LIMIT("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_branch_heating_delta_temp_limit", "制热供回水温差设定值的上下限", "℃", "5,15", 1, 0, 99, "范围：（0-99），上限≥下限", "", null, null, null),
    ALG_AIR_COOLING_HOT_BRANCH_HEATING_DELTA_TEMP_ADJUST("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_branch_heating_delta_temp_adjust", "制热供回水温差设定调整幅度", "℃", "2", 1, 0, 99, "范围：（0-99）", "", null, null, null),
    ALG_AIR_COOLING_HOT_BRANCH_HEATING_DELTA_TEMP_HOLD_TIME("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_branch_heating_delta_temp_hold_time", "供回水温差设定修改后保持时间", "min", "30", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),
    ALG_AIR_COOLING_HOT_BRANCH_HEATING_DELTA_PRESS_DEFAULT("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_branch_heating_delta_press_default", "制热供回水压差设定值默认", "kpa", "60", 1, 0, 999, "范围：（0-999）", "", null, null, null),
    ALG_AIR_COOLING_HOT_BRANCH_HEATING_DELTA_PRESS_ADJUST("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_branch_heating_delta_press_adjust", "制热供回水压差设定调整幅度", "kpa", "10", 1, 0, 999, "范围：（0-999）", "", null, null, null),
    ALG_AIR_COOLING_HOT_BRANCH_HEATING_DELTA_PRESS_LIMIT("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_branch_heating_delta_press_limit", "制热供回水压差设定值的上下限", "kpa", "40,100", 1, 0, 999, "范围：（0-999），上限≥下限", "", null, null, null),
    ALG_AIR_COOLING_HOT_BRANCH_HEATING_DELTA_PRESS_HOLD_TIME("alg", "group_alg_cooling_pump_diff_hot", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_branch_heating_delta_press_hold_time", "供回水压差设定修改后保持时间", "min", "15", 0, 0, 1440, "5 的倍数，范围（0-1440）", "", null, null, null),


    //通用工况
    ALG_AIR_COOLING_HOT_HOST_UNIT_LIMIT_RULE("alg", "group_alg_cooling_common_condition", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_host_unit_limit_rule", "主机开启台数限制规则", "", "", 0, null, null, "", "", null, "{\"relation\":{\"classCode\":[\"ACCCGH\",\"ACCCWH\",\"ACCCAC\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}}", null),
    ALG_AIR_COOLING_HOT_SUPPORT_BIG_SMALL_HOST("alg", "group_alg_cooling_common_condition", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_support_big_small_host", "是否支持大小机同时开", "", "", 0, null, null, "", "", null, "{\"relation\":{\"classCode\":[\"ACCCGH\",\"ACCCWH\",\"ACCCAC\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}}", null),
    ALG_AIR_COOLING_HOT_HEAT_BALANCE_INTERVAL("alg", "group_alg_cooling_common_condition", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heat_balance_interval", "热泵散热平衡计算间隔时间", "h", "8760", 0, 0, 17520, "范围：（0-17520）", "", null, null, null),
    ALG_AIR_COOLING_HOT_COOLING_IMBALANCE("alg", "group_alg_cooling_common_condition", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_cooling_imbalance", "供冷失衡", "", "1.2", 1, 1, 3, "范围：（1-3）", "", null, null, null),
    ALG_AIR_COOLING_HOT_HEATING_IMBALANCE("alg", "group_alg_cooling_common_condition", "空调冷热源自动计算条件输入", "alg_air_cooling_hot_heating_imbalance", "供热失衡", "", "0.8", 1, 0, 1, "范围：（0-1）", "", null, null, null),

    /** 效率指标配置 注意国际化翻译*/
    ALG_AIR_COOLING_EFF_SYSC("eff", "group_control_air_cooling_eff", null, "alg_air_cooling_eff_sysc", "冷站EER",  "", "0,2.5,3.5,4,6", 1, 0, null, "", "", null, "{\"infoCode\":\"sysCOP\"}", null),
    ALG_AIR_COOLING_EFF_CHIC("eff", "group_control_air_cooling_eff", null, "alg_air_cooling_eff_chic", "冷机组COP",  "", "0,3.5,5,5.5,8", 1, 0, null, "", "", null, "{\"infoCode\":\"chillersCOP\"}", null),
    ALG_AIR_COOLING_EFF_WTFCH("eff", "group_control_air_cooling_eff", null, "alg_air_cooling_eff_wtfch", "冷冻水输配系数",  "", "0,20,30,35,60", 1, 0, null, "", "", null, "{\"infoCode\":\"wTFch\"}", null),
    ALG_AIR_COOLING_EFF_WTFC("eff", "group_control_air_cooling_eff", null, "alg_air_cooling_eff_wtfc", "冷却水输配系数",  "", "0,20,30,35,60", 1, 0, null, "", "", null, "{\"infoCode\":\"wTFc\"}", null),
    ALG_AIR_COOLING_EFF_CTHF("eff", "group_control_air_cooling_eff", null, "alg_air_cooling_eff_cthf", "冷却塔散热系数",  "", "0,20,30,35,60", 1, 0, null, "", "", null, "{\"infoCode\":\"cTHeatFactor\"}", null),
    ALG_AIR_COOLING_EFF_TUBR("eff", "group_control_air_cooling_eff", null, "alg_air_cooling_eff_tubr", "热不平衡率",  "%", "-100,-10,-5,5,10,100", 1, -100, 100, "", "", null, "{\"infoCode\":\"thermalUnbalanceRate\"}", null),

    /** 关系图配置 */
    GRAPH_EFF_PIPE_SWITCH("graph", "group_graph_effect", "关系图配置", "graph_eff_pipe_switch", "关系图管道动画效果显隐",  "/", "show", -1, null, null, "", "", null, null, null),
    GRAPH_EFF_MOTION_TYPE("graph", "group_graph_effect", "关系图配置", "graph_eff_motion_type", "动画效果",  "/", "antLine", -1, null, null, "", "", null, null, null),
    GRAPH_ELE_OPEN_THRESHOLD("graph", "group_graph_electric", "关系图配置", "graph_ele_open_threshold", "判断电动调节水阀为开启状态的开度阈值",  "%", "5", 1, 0, 100, "范围（0-100）", "", null, null, null),

    /** 半剖图配置 */
    GRAPH_HSEC_ACATAH("hsec", "group_hsec_show", "半剖图配置", "graph_hsec_acatah", "空调机组显隐配置",  "/", "show", -1, null, null, "", "", null, "{\"classCode\":\"acatah\"}", null),
    GRAPH_HSEC_ACATAH_LIST("hsec", "group_hsec_show", "半剖图配置", "graph_hsec_acatah_list", "添加需隐藏设备",  "/", "", -1, null, null, "", "", null, "{\"classCode\":\"acatah\"}", null),

    GRAPH_HSEC_ACATFU("hsec", "group_hsec_show", "半剖图配置", "graph_hsec_acatfu", "新风机组显隐配置",  "/", "show", -1, null, null, "", "", null, "{\"classCode\":\"acatfu\"}", null),
    GRAPH_HSEC_ACATFU_LIST("hsec", "group_hsec_show", "半剖图配置", "graph_hsec_acatfu_list", "添加需隐藏设备",  "/", "", -1, null, null, "", "", null, "{\"classCode\":\"acatfu\"}", null),

    GRAPH_HSEC_ACAHG("hsec", "group_hsec_show", "半剖图配置", "graph_hsec_acahg", "空调机组控制组显隐配置",  "/", "show", -1, null, null, "", "", null, "{\"classCode\":\"acahg\"}", null),
    GRAPH_HSEC_ACAHG_LIST("hsec", "group_hsec_show", "半剖图配置", "graph_hsec_acahg_list", "添加需隐藏设备",  "/", "", -1, null, null, "", "", null, "{\"classCode\":\"acahg\"}", null),

    GRAPH_HSEC_MODEL("hsec", "group_hsec_display", "半剖图配置", "graph_hsec_model", "半剖图入口配置",  "/", "2", -1, null, null, "", "", null, "{\"1\":\"点击设备直接打开半剖图\",\"2\":\"从设备详情打开半剖图\"}", null),
    /** 空间与设备关系展示配置 */
    SPACE_DEVICE_ENTRY_MODE("hsec", "group_space_device_display", "半剖图配置", "space_device_entry_mode", "空间与设备关系入口配置",  "/", "2", -1, null, null, "", "", null, "{\"1\":\"点击空间直接打开空间设备关系拓扑图\",\"2\":\"从空间详情打开空间设备关系拓扑图\"}", null),

    /** 环境参数指标配置 */
    HSEC_ENV_INDICATOR("hsec", "group_hsec_env_indicator", "半剖图配置", "hsec_env_indicator", "环境参数指标配置",  "/", null, -1, null, null, "", "", "MULTIPLE_CHECKBOX", null, null),

    /**
     * 报告配置
     */
    REPORT_BUILD_SWITCH("report", "group_report_build", "报告生成配置", "report_build_switch", "是否生成日报",  "/", "yes", 0, null, null, "", "", null, null, null),
    //生成时间范围
    REPORT_BUILD_TIME_RANGE("report", "group_report_build", "报告生成配置", "report_build_time_range", "日报生成时间范围",  "", "{\"startTime\":\"\",\"endTime\":\"\"}", 0, null, null, "", "", null, null, null),
    //报告生成时间点
    REPORT_BUILD_TIME("report", "group_report_build", "报告生成配置", "report_build_time", "日报生成时间点",  "", "{\"acrossDay\":\"false\",\"time\":\"\"}", 0, null, null, "", "", null, null, null),

    //运行概述章节配置
    REPORT_CHAPTER_RUN_OVERVIEW("report", "group_report_chapter", "报告章节配置", "report_chapter_run_overview", "运行概述",  "/", "{\"name\":\"运行概述\",\"sort\":1}", 0, null, null, "", "", null, null, null),
    //能耗分析
    REPORT_CHAPTER_ENERGY_ANALYSIS("report", "group_report_chapter", "报告章节配置", "report_chapter_energy_analysis", "能耗分析",  "/", "{\"name\":\"能耗分析\",\"sort\":2}", 0, null, null, "", "配置用电分项", null, null, null),
    //能耗预算
    REPORT_CHAPTER_ENERGY_BUDGET("report", "group_report_chapter", "报告章节配置", "report_chapter_energy_budget", "能耗预算",  "/", "{\"name\":\"能耗预算\",\"sort\":3,\"benchmark\":\"target\"}", 0, null, null, "", "实际能耗对标设置", null,"{\"benchmark\":{\"target\":\"目标值\",\"control\":\"管控值\"}}",null),
    //空调
    REPORT_CHAPTER_AC("report", "group_report_chapter", "报告章节配置", "report_chapter_ac", "空调",  "/", "{\"name\":\"空调\",\"sort\":4}", 0, null, null, "", "", null, null, null),
    //冷量分析
    REPORT_CHAPTER_COOLING_ANALYSIS("report", "group_report_chapter", "报告章节配置", "report_chapter_cooling_analysis", "冷量分析",  "/", "{\"name\":\"冷量分析\",\"sort\":1,\"parent\":\"report_chapter_ac\"}", 0, null, null, "", "", null, null, null),
    //空间温度目标
    REPORT_CHAPTER_COOLING_TEMP_TARGET("report", "group_report_chapter", "报告章节配置", "report_chapter_cooling_temp_target", "空间温度目标",  "/", "{\"name\":\"空间温度目标\",\"sort\":1,\"parent\":\"report_chapter_cooling_analysis\"}", 0, null, null, "", "", null, null, null),
    //冷量及能耗
    REPORT_CHAPTER_COOLING_ENERGY("report", "group_report_chapter", "报告章节配置", "report_chapter_cooling_energy", "冷量及能耗",  "/", "{\"name\":\"冷量及能耗\",\"sort\":2,\"parent\":\"report_chapter_cooling_analysis\"}", 0, null, null, "", "", null, "{\"classCode\":\"ACAC\"}", null),
    //环境品质
    REPORT_CHAPTER_ENV_QUALITY("report", "group_report_chapter", "报告章节配置", "report_chapter_env_quality", "环境品质",  "/", "{\"name\":\"环境品质\",\"sort\":2,\"parent\":\"report_chapter_ac\",\"spaces\":[]}", 0, null, null, "", "温度点位配置", null, null, null),
    //冷站系统运行
    REPORT_CHAPTER_COOLING_SYS_RUN("report", "group_report_chapter", "报告章节配置", "report_chapter_cooling_sys_run", "冷站系统运行",  "/", "{\"name\":\"冷站系统运行\",\"sort\":3,\"parent\":\"report_chapter_ac\"}", 0, null, null, "", "", null, null, null),
    REPORT_CHAPTER_COOLING_SYS_RUN_ITEM("report", "group_report_chapter", "报告章节配置", "report_chapter_cooling_sys_run_item", "冷站系统运行",  "/", "{\"name\":\"冷站系统运行\",\"sort\":3,\"parent\":\"report_chapter_cooling_sys_run\"}", 0, null, null, "", "", null, null, null),

    /**
     * MPC智能体配置-中央空调供冷全系统
    **/
    MPC_APP_MODE("mpc", "group_mpc_agent_acac", null, "mpc_app_mode", "MPC智能体应用模式",  "", "no", 0, null, null, "", "", null, "[{\"name\":\"不应用AI策略\",\"code\":\"no\"},{\"name\":\"响应式AI策略\",\"code\":\"responsive\"},{\"name\":\"全时AI策略\",\"code\":\"all\"}]", null),
    MPC_CALC_POLICY("mpc", "group_mpc_agent_acac", null, "mpc_calc_policy", "是否应用AI计算策略",  "", "[\"host\",\"space\"]", 0, null, null, "", "", null, "[{\"name\":\"需求跃变时的主机策略（包含主机台数、供水温度设定值）\",\"code\":\"host\"},{\"name\":\"空间策略（包含空间提前开关机时间）\",\"code\":\"space\"}]", null),
    MPC_STAGE_UP("mpc", "group_mpc_agent_acac", null, "mpc_stage_up", "开机阶段（负荷突增）",  "", "", 0, null, null, "", "", null, "[{\"name\":\"主机策略（包含主机台数、供水温度设定值）\",\"code\":\"host\"},{\"name\":\"空间策略\",\"code\":\"space\"}]", null),
    MPC_STAGE_UP_CLASS_CODE("mpc", "group_mpc_agent_acac", null, "mpc_stage_up_class_code", "选择其他应用AI计算策略的设备类",  "", "", 0, null, null, "", "", null, "{\"relation\":{\"classCode\":[\"ACCCCP\",\"ACCCOP\",\"ACCHCH\",\"ACCCGP\",\"ACCCTP\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}}", null),

    MPC_STAGE_DAY("mpc", "group_mpc_agent_acac", null, "mpc_stage_day", "日间运行（负荷突增—>突降期间）",  "", "", 0, null, null, "", "", null, "[{\"name\":\"主机策略（包含主机台数、供水温度设定值）\",\"code\":\"host\"},{\"name\":\"空间策略\",\"code\":\"space\"}]", null),
    MPC_STAGE_DAY_CLASS_CODE("mpc", "group_mpc_agent_acac", null, "mpc_stage_day_class_code", "选择其他应用AI计算策略的设备类",  "", "", 0, null, null, "", "", null, "{\"relation\":{\"classCode\":[\"ACCCCP\",\"ACCCOP\",\"ACCHCH\",\"ACCCGP\",\"ACCCTP\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}}", null),

    MPC_STAGE_DOWN("mpc", "group_mpc_agent_acac", null, "mpc_stage_down", "关机阶段（负荷突降）",  "", "", 0, null, null, "", "", null, "[{\"name\":\"主机策略（包含主机台数、供水温度设定值）\",\"code\":\"host\"},{\"name\":\"空间策略\",\"code\":\"space\"}]", null),
    MPC_STAGE_DOWN_CLASS_CODE("mpc", "group_mpc_agent_acac", null, "mpc_stage_down_class_code", "选择其他应用AI计算策略的设备类",  "", "", 0, null, null, "", "", null, "{\"relation\":{\"classCode\":[\"ACCCCP\",\"ACCCOP\",\"ACCHCH\",\"ACCCGP\",\"ACCCTP\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}}", null),

    MPC_STAGE_NIGHT("mpc", "group_mpc_agent_acac", null, "mpc_stage_night", "夜间运行（负荷突降—>突增期间）",  "", "", 0, null, null, "", "", null, "[{\"name\":\"主机策略（包含主机台数、供水温度设定值）\",\"code\":\"host\"},{\"name\":\"空间策略\",\"code\":\"space\"}]", null),
    MPC_STAGE_NIGHT_CLASS_CODE("mpc", "group_mpc_agent_acac", null, "mpc_stage_night_class_code", "选择其他应用AI计算策略的设备类",  "", "", 0, null, null, "", "", null, "{\"relation\":{\"classCode\":[\"ACCCCP\",\"ACCCOP\",\"ACCHCH\",\"ACCCGP\",\"ACCCTP\"],\"graphCode\":\"MechSubset\",\"relCode\":\"Sy2Eq\",\"relDirection\":\"from\"}}", null),

    MPC_AIR_UNIT_POLICY("mpc", "group_mpc_agent_air_terminal", null, "mpc_air_unit_policy", "空调机组策略",  "", "", 0, null, null, "", "", null, "ACATAH", null),
    MPC_FRESH_UNIT_POLICY("mpc", "group_mpc_agent_air_terminal", null, "mpc_fresh_unit_policy", "新风机组策略",  "", "", 0, null, null, "", "", null, "ACATFU", null),
    MPC_FAN_COIL_POLICY("mpc", "group_mpc_agent_air_terminal", null, "mpc_fan_coil_policy", "风机盘管策略",  "", "", 0, null, null, "", "", null, "ACATFC", null),


    //末端监测空间范围-空间范围配置
    SPACE_RANGE_TERMINAL("whitelist_space","group_terminal_monitor_space",null,"space_range_terminal", "末端监测空间范围",  "/", "FULL", 0, null, null, "", "", null, "{\"FULL\":\"引用全量可管控空间（推荐）\", \"WHITE_LIST\":\"用户配置白名单\"}", null),

    //云端持久化数据显示规则
    PERSISTENT_DATA_QUERY_LOGIC("persistent_data_rule", "group_persistent_data_rule", "云端持久化数据显示规则", "persistent_data_query_logic", "持久化数据查询逻辑", "", "same_time", -1, null, null, "", "①查询同一个时刻点的数据：所有信息点都查询某一个时刻的固定分精度数据；若信息点的最小分精度数据大于设置的分精度则会获取不到数据，比如信息点字典定义的最小分精度数据15min，设置的分精度为5min，则永远查询不到数据\n②查询距离当前最新的数据：不限制分精度，有最新的数据上数到云端即可查询，但不同信息点的最新时间点可能不一致", null, "[{\"name\":\"查询同一个时刻点的数据\",\"value\":\"same_time\"},{\"name\":\"查询距离当前最新的数据\",\"value\":\"latest\"}]", null),
    PERSISTENT_DATA_TIME_INTERVAL("persistent_data_rule", "group_persistent_data_rule", "云端持久化数据显示规则", "persistent_data_time_interval", "请设置距离当前的时间间隔", "h", "1", 1, 1, 168, "整数，范围（1-168）", "仅查询设置的时间范围内的最新数据，若没有最新数据，则页面会显示为--。比如设置3h，3h内没有新的数据上数则显示为--，若在持续上数则显示最新的一个数据", null, null, null),

    ;

    private String scene;
    private String group;
    private String tab;
    private String code;
    private String name;
    private String unit;
    private String defaultValues;
    /**
     * 和前端商定，0：整数，1：保留一位小数，2：保留两位小数，
     */
    private Integer step;
    private Integer min;
    private Integer max;
    private String hint;
    private String desc;

    /**
     * 值类型，扩展字段-之前旧的不动，也就是为空什么都不代表
     * <p>
     * NUMBER-数值
     * RANGE-范围上下限
     * RADIO-单选控件
     * MULTIPLE-多选控件（前端可能是下拉列表）
     * STEP-步长输入框
     */
    private String valueType;

    /**
     * 值的实际来源，一般多选控件时，存在，json数据格式
     */
    private String valSource;

    /**
     * 是否允许前端进行编辑此参数
     */
    private Boolean editAuth;

    public static ShellConfigItemEnum getByCode(String code) {
        for (ShellConfigItemEnum value : ShellConfigItemEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    public String getName() {
        return LocaleMessageUtil.getMessage(name);
    }

    public static List<ShellConfigItem> getListByScene(String scene) {
        List<ShellConfigItemEnum> defaultValues = Arrays.stream(ShellConfigItemEnum.values()).filter(item -> item.getScene().equals(scene)).collect(Collectors.toList());
        List<ShellConfigItem> items = new ArrayList<>();
        defaultValues.forEach(defaultValue -> {
            ShellConfigItem item = ShellConfigItemEnum.convert(defaultValue);
            if(ShellConfigItemExtendEnum.getByCode(item.getCode()) != null){
                BeanUtil.copyProperties(ShellConfigItemExtendEnum.getByCode(item.getCode()), item);
                //item.setTabObjectName(ShellConfigItemExtendEnum.getByCode(item.getCode()));
            }
            items.add(item);
        });
        return items;
    }

    public static List<ShellConfigItem> getListByGroup(String group) {
        List<ShellConfigItemEnum> defaultValues = Arrays.stream(ShellConfigItemEnum.values()).filter(item -> item.getGroup().equals(group)).collect(Collectors.toList());
        List<ShellConfigItem> items = new ArrayList<>();
        defaultValues.forEach(defaultValue -> {
            items.add(ShellConfigItemEnum.convert(defaultValue));
        });
        return items;
    }

    public static ShellConfigItem convert(ShellConfigItemEnum itemEnum) {
        ShellConfigItem shellConfigItem = new ShellConfigItem();
        shellConfigItem.setScene(itemEnum.getScene());
        shellConfigItem.setGroup(itemEnum.getGroup());
        shellConfigItem.setTab(itemEnum.getTab());
        shellConfigItem.setCode(itemEnum.getCode());
        shellConfigItem.setName(itemEnum.getName());
        shellConfigItem.setUnit(itemEnum.getUnit());
        shellConfigItem.setDefaultValues(itemEnum.getDefaultValues());
        shellConfigItem.setStep(itemEnum.getStep());
        shellConfigItem.setMin(itemEnum.getMin());
        shellConfigItem.setMax(itemEnum.getMax());
        shellConfigItem.setHint(itemEnum.getHint());
        shellConfigItem.setDesc(itemEnum.getDesc());
        shellConfigItem.setEditAuth(itemEnum.getEditAuth() == null ? true : itemEnum.getEditAuth());
        shellConfigItem.setValueType(itemEnum.getValueType());
        shellConfigItem.setValSource(itemEnum.getValSource());
        return shellConfigItem;
    }
}
