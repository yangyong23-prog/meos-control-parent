package com.persagy.apccontrol.basic.model.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xiaofengwang
 */

@Getter
@AllArgsConstructor
public enum ShellConfigGroupEnum {

    GROUP_ALG_PROJECT("group_alg_project", "项目", "", false),
    GROUP_ALG_COOLING("group_alg_cooling", "中央供冷系统", "ACCC", true),
    GROUP_ALG_HOT("group_alg_hot", "中央供热系统", "ACCH", true),
    GROUP_ALG_COLD_HOT("group_alg_cold_hot", "中央冷热源系统", "ACCI", true),
    GROUP_ALG_DEMAND_ERROR("group_alg_demand_error", "空间品质异常判断逻辑条件输入", "", false),
    //提前开关时长
    GROUP_ALG_ADVANCE_DURATION("group_alg_advance_duration", "提前开关时长", "", false),
    //空间温度各项设定值的计算方式配置
    GROUP_ALG_JUMP("group_alg_jump", "跃变与加减机逻辑条件输入", "", false),
    GROUP_ALG_SPACE_TEMP_SET("group_alg_space_temp_set", "空间温度各项设定值的计算方式配置", "", false),
    GROUP_ALG_ADD_SUBTRACT("group_alg_add_subtract", "加减机逻辑条件输入（针对单个冷站）", "ACCC", false),
    GROUP_ALG_TEMP_PRESS_DIFF("group_alg_temp_press_diff", "供回水温差/压差逻辑条件输入（针对单个冷站）", "ACCC", false),

    //控制组频率约
    GROUP_ALG_ACCC_FREQ("group_alg_accc_freq", "控制组频率约束", "ACCC", false),

    //室外湿球温度配置
    GROUP_ALG_OUT_WET_BALL_TEMP_ACCC("group_alg_out_wet_ball_temp_accc", "室外湿球温度配置", null, false),

    GROUP_ALG_COLD_OUT_TEMP("group_alg_cold_out_temp", "冷机出水温度设定逻辑条件输入（针对单个冷站）", "ACCC", false),
    GROUP_ALG_DIFF("group_alg_diff", "泵组供回水温差/压差逻辑条件输入（针对单个泵组）", "ACCPC", false),
    GROUP_ALG_ADD_SUBTRACT_COLD("group_alg_add_subtract_cold", "加减机逻辑条件输入-供冷模式（针对单个冷热站）", "ACCI", false),
    GROUP_ALG_ADD_SUBTRACT_HOT("group_alg_add_subtract_hot", "加减机逻辑条件输入-供热模式（针对单个冷热站）", "ACCI", false),
    GROUP_ALG_COLD_OUT_TEMP_COLD("group_alg_cold_out_temp_cold", "主机出水温度设定逻辑条件输入-供冷模式（针对单个冷热站）", "ACCI", false),
    GROUP_ALG_COLD_OUT_TEMP_HOT("group_alg_cold_out_temp_hot", "主机出水温度设定逻辑条件输入-供热模式（针对单个冷热站）", "ACCI", false),
    GROUP_ALG_DIFF_COLD("group_alg_diff_cold", "泵组供回水温差/压差逻辑条件输入-供冷模式（针对单个泵组）", "ACPPC", false),
    GROUP_ALG_DIFF_HOT("group_alg_diff_hot", "泵组供回水温差/压差逻辑条件输入-供热模式（针对单个泵组）", "ACPPC", false),
    GROUP_ALG_COLDHOT_TEMP_PRESS_DIFF_COOL("group_alg_coldhot_temp_press_diff_cool", "供回水温差/压差逻辑条件输入-供冷模式（针对单个冷热站）", "ACCI", false),
    GROUP_ALG_COLDHOT_TEMP_PRESS_DIFF_hot("group_alg_coldhot_temp_press_diff_hot", "供回水温差/压差逻辑条件输入-供热模式（针对单个冷热站）", "ACCI", false),

    GROUP_ALG_HOT_TEMP_SYSTEM("group_alg_hot_temp_system", "出水温度设定逻辑条件（针对单个热站）", "ACCH", false),
    GROUP_ALG_HOT_DETA_TEMP_PRESS_SYSTEM("group_alg_hot_deta_temp_press_system", "温差/压差设定逻辑条件（针对单个热站）", "ACCH", false),
    GROUP_ALG_HOT_DETA_TEMP_PRESS_PUMP("group_alg_hot_deta_temp_press_pump", "温差/压差设定逻辑条件（针对单个泵组）", "ACHPC", false),

    GROUP_CONTROL_VRV("group_control_vrv", "空调机组控制逻辑条件输入", "", false),
    GROUP_CONTROL_FAN("group_control_fan", "风机盘管控制逻辑条件输入", "", false),
    GROUP_CONTROL_AIR("group_control_air", "新风机组控制逻辑条件输入", "", false),

    GROUP_CONTROL_COOLING("group_control_cooling", "中央供冷系统", "ACCC", true),
    GROUP_CONTROL_AIR_COOLING("group_control_air_cooling", "中央空调供冷全系统", "ACAC", true),
    GROUP_CONTROL_AIR_COOLING_EFF("group_control_air_cooling_eff", "中央空调供冷全系统", "ACAC", true),
    GROUP_CONTROL_AIR_HOT("group_control_air_hot", "中央空调供热全系统", "ACAH", true),
    //中央空调冷热源全系统
    GROUP_CONTROL_COLD_HOT_ALL("group_control_cold_hot_all", "中央空调冷热源全系统", "ACAI", true),
    GROUP_CONTROL_HOT("group_control_hot", "中央供热系统", "ACCH", true),
    GROUP_CONTROL_COLD_HOT("group_control_cold_hot", "中央冷热源系统", "ACCI", true),
    GROUP_CONTROL_COOL_PUMP_GROUP("group_control_cool_pump_group", "冷冻水泵控制组", "ACAC,ACAI,ACCPC", true),
    GROUP_CONTROL_CHIL_PUMP_GROUP("group_control_chil_pump_group", "冷却水泵控制组", "ACAC,ACAI,ACOPC", true),
    GROUP_CONTROL_COOL_TOWER_GROUP("group_control_cool_tower_group", "冷却塔控制组", "ACAC,ACAI,ACOTC", true),
    GROUP_CONTROL_CGLY_PUMP_GROUP("group_control_cgly_pump_group", "供冷乙二醇泵控制组", "ACAC,ACAI,ACCGP", true),
    GROUP_CONTROL_COOL_HSLO_GROUP("group_control_cool_hslo_group", "蓄冷热槽控制组", "ACAC,ACAI,ACCST", true),
    GROUP_CONTROL_HOT_PUMP_GROUP("group_control_hot_pump_group", "供热水泵控制组", "ACAH,ACAI,ACHPC", true),
    GROUP_CONTROL_COOL_HOT_PUMP("group_control_cool_hot_pump", "冷热共用水泵控制组", "ACAC,ACAH,ACAI,ACPPC", true),

    //通风控制逻辑条件输入-穹顶通风（场景分组，项目级配置）
    GROUP_CONTROL_VENT_DOME("group_control_vent_dome", "穹顶通风", "", false),

    GROUP_SPACE_QUALITY("group_space_quality", "空间品质需求输入条件", "", false),

    GROUP_POLICY_SITE_COOLING("group_policy_site_cooling", "中央供冷系统", "ACCC", true),
    GROUP_POLICY_SITE_PUMP("group_policy_site_pump", "冷冻水泵控制组", "ACCPC", false),
    GROUP_POLICY_DEVICE_COOLING("group_policy_device_cooling", "中央供冷系统", "ACCC", false),
    GROUP_POLICY_DEVICE_CHILLER("group_policy_device_chiller", "压缩式冷水机组", "", false),
    GROUP_POLICY_DEVICE_FAN("group_policy_device_fan", "冷却塔风机", "", false),
    GROUP_POLICY_DEVICE_PUMP("group_policy_device_pump", "冷冻水泵", "", false),
    GROUP_POLICY_DEVICE_VALVE("group_policy_device_valve", "调节水阀", "", false),
    GROUP_POLICY_SITE_COOLING_HOT("group_policy_site_cooling_hot", "中央冷热源系统", "ACCI", true),
    GROUP_POLICY_SITE_PUMP_COLD_HOT("group_policy_site_pump_cold_hot", "冷热共用水泵控制组", "", false),
    GROUP_POLICY_DEVICE_COOLING_HOT("group_policy_device_cooling_hot", "中央冷热源系统", "ACCI", false),
    GROUP_POLICY_DEVICE_HOT_PUMP("group_policy_device_hot_pump", "空气源热泵控制组", "", false),
    GROUP_POLICY_DEVICE_CIRCULAT_PUMP("group_policy_device_circulat_pump", "循环泵", "", false),
    GROUP_POLICY_DEVICE_REGULAT_WATER("group_policy_device_regulat_water", "调节水阀", "", false),
    GROUP_POLICY_END_AIR("group_policy_end_air", "新风机组", "", false),
    GROUP_POLICY_END_VRVH("group_policy_end_vrvh", "空调机组-卧式", "", false),
    GROUP_POLICY_END_VRVV("group_policy_end_vrvv", "空调机组-吊式", "", false),
    GROUP_POLICY_END_FCU("group_policy_end_fcu", "风机盘管", "", false),

    GROUP_WEATHER_CHANGE("group_weather_change", "室外天气数据来源切换", "", false),

    GROUP_ALG_COOLING_CONDITION("group_alg_cooling_condition", "供冷工况", "ACAC", false),
    GROUP_ALG_COMMON_CONDITION("group_alg_common_condition", "通用工况", "ACAC", false),
    GROUP_ALG_COOL_STORE_CONDITION("group_alg_cool_store_condition", "蓄冷工况", "ACAC", false),
    GROUP_ALG_COOL_RELEASE_CONDITION("group_alg_cool_release_condition", "放冷工况", "ACAC", false),
    GROUP_ALG_COOL_FREE_CONDITION("group_alg_cool_free_condition", "免费冷工况", "ACAC", false),
    GROUP_ALG_ACAC_COLD_OUT_TEMP("group_alg_acac_cold_out_temp", "冷机出水温度设定逻辑条件输入（针对单个冷站）", "ACAC", false),
    GROUP_ALG_ACAC_TEMP_PRESS_DIFF("group_alg_acac_temp_press_diff", "供回水温差/压差逻辑条件输入（针对单个冷站）", "ACAC", false),
    GROUP_ALG_ACAC_DIFF("group_alg_acac_diff", "泵组供回水温差/压差逻辑条件输入", "ACAC", false),

    //控制组频率约
    GROUP_ALG_ACAC_FREQ("group_alg_acac_freq", "控制组频率约束", "ACAC", false),

    //室外湿球温度配置
    GROUP_ALG_OUT_WET_BALL_TEMP("group_alg_out_wet_ball_temp", "室外湿球温度配置", null, false),

    GROUP_ALG_ACAH_HOT_SET("group_alg_acah_hot_set", "供热开关设定", "ACAH", false),
    GROUP_ALG_ACAH_ADD("group_alg_acah_add", "加机逻辑", "ACAH", false),
    GROUP_ALG_ACAH_SUB("group_alg_acah_sub", "减机逻辑", "ACAH", false),
    GROUP_ALG_ACAH_SECU_CALC("group_alg_acah_secu_calc", "安全状态计算", "ACAH", false),
    GROUP_ALG_ACAH_OUT_TEMP("group_alg_acah_out_temp", "出水温度设定逻辑条件（针对单个热站）", "ACAH", false),
    GROUP_ALG_ACAH_HOT_DIFF("group_alg_acah_hot_diff", "温差/压差设定逻辑条件（针对单个热站）", "ACAH", false),
    GROUP_ALG_ACAH_PUMP_DIFF("group_alg_acah_pump_diff", "温差/压差设定逻辑条件（针对单个泵组）", "ACHPC,ACPPC", false),

    GROUP_GRAPH_EFFECT("group_graph_effect", "关系图动画效果显隐控制",null,false),
    GROUP_GRAPH_ELECTRIC("group_graph_electric", "电动调节水阀开度配置",null,false),

    GROUP_HSEC_SHOW("group_hsec_show", "半剖图显隐配置",null,false),
    GROUP_HSEC_DISPLAY("group_hsec_display", "半剖图展示配置",null,false),
    // 空间与设备关系展示配置
    GROUP_SPACE_DEVICE_DISPLAY("group_space_device_display", "空间与设备关系展示配置", null, false),
    // 环境参数指标配置
    GROUP_HSEC_ENV_INDICATOR("group_hsec_env_indicator", "环境参数指标配置", null, false),
    // 报告配置
    GROUP_REPORT_BUILD("group_report_build", "报告生成配置",null,false),
    GROUP_REPORT_CHAPTER("group_report_chapter", "报告章节配置","ACAC",false),

    //MPC智能体配置
    GROUP_MPC_AGENT_ACAC("group_mpc_agent_acac", "","ACAC",false),
    GROUP_MPC_AGENT_ACAI("group_mpc_agent_acai", "","ACAI",false),
    GROUP_MPC_AGENT_AIR_TERMINAL("group_mpc_agent_air_terminal", "空调末端",null,false),

    //空调冷热源自动计算条件输入
    //供冷台数计算-供冷模式
    GROUP_ALG_COOLING_UNIT_NUM("group_alg_cooling_unit_num", "供冷台数计算-供冷模式", "ACAI", false),
    //供水温度设定-供冷模式
    GROUP_ALG_COOLING_SUPPLY_TEMP("group_alg_cooling_supply_temp", "供水温度设定-供冷模式", "ACAI", false),
    //安全运行-供冷模式
    GROUP_ALG_COOLING_SAFE_RUN("group_alg_cooling_safe_run", "安全运行-供冷模式", "ACAI", false),
    //供回水温差/压差逻辑条件输入-供冷模式
    GROUP_ALG_COOLING_TEMP_PRESS_DIFF("group_alg_cooling_temp_press_diff", "供回水温差/压差逻辑条件输入-供冷模式", "ACAI", false),
    //泵组供回水温差/压差逻辑条件输入-供冷模式
    GROUP_ALG_COOLING_PUMP_DIFF("group_alg_cooling_pump_diff", "泵组供回水温差/压差逻辑条件输入-供冷模式", "ACAI", false),
    //开关设定-供热模式
    GROUP_ALG_COOLING_SWITCH_SET("group_alg_cooling_switch_set", "开关设定-供热模式", "ACAI", false),
    //台数计算-供热模式
    GROUP_ALG_COOLING_UNIT_NUM_HOT("group_alg_cooling_unit_num_hot", "台数计算-供热模式", "ACAI", false),
    //安全运行-供热模式
    GROUP_ALG_COOLING_SAFE_RUN_HOT("group_alg_cooling_safe_run_hot", "安全运行-供热模式", "ACAI", false),
    //供水温度设定-供热模式
    GROUP_ALG_COOLING_SUPPLY_TEMP_HOT("group_alg_cooling_supply_temp_hot", "供水温度设定-供热模式", "ACAI", false),
    //供回水温差/压差逻辑条件输入-供热模式
    GROUP_ALG_COOLING_TEMP_PRESS_DIFF_HOT("group_alg_cooling_temp_press_diff_hot", "供回水温差/压差逻辑条件输入-供热模式", "ACAI", false),
    //泵组供回水温差/压差逻辑条件输入-供热模式
    GROUP_ALG_COOLING_PUMP_DIFF_HOT("group_alg_cooling_pump_diff_hot", "泵组供回水温差/压差逻辑条件输入-供热模式", "ACAI", false),
    //通用工况
    GROUP_ALG_COOLING_COMMON_CONDITION("group_alg_cooling_common_condition", "通用工况", "ACAI", false),

    //末端监测空间范围
    GROUP_TERMINAL_MONITOR_SPACE("group_terminal_monitor_space", "末端监测空间范围", null, false),

    //云端持久化数据显示规则
    GROUP_PERSISTENT_DATA_RULE("group_persistent_data_rule", "云端持久化数据显示规则", "", false),
    ;

    private String code;
    private String name;
    private String systemType;
    private boolean resetParam;

    public static ShellConfigGroupEnum getGroupEnumByCode(String group) {
        for (ShellConfigGroupEnum groupEnum : values()) {
            if (groupEnum.code.equals(group)) {
                return groupEnum;
            }
        }
        return null;
    }
}
