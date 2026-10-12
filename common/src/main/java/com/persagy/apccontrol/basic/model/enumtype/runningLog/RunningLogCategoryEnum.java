package com.persagy.apccontrol.basic.model.enumtype.runningLog;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.model.dto.RwdObjectBusinessFeildDTO;
import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogCategoryDto;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public enum RunningLogCategoryEnum  implements BaseEnum<String>, IEnum<String>  {
    /**一级分类*/
    airConditioningColdSource("airConditioningColdSource", null, "key_dfbd48a8133047f8a6b51f2b5e34909f", 1),//切换新版日志
    ACCC("ACCC", null, "key_ffd567238d36487bb75fd52074e46ca2", 1),
    StatusAndControl_AC("StatusAndControl_AC", null, "key_9e432c33a6f3404cbc4295b2bdead343", 1),//切换新版日志
    airConditioningHeatSource("airConditioningHeatSource", null, "key_air_conditioning_heat_source", 1),//切换新版日志
    ACCH("ACCH", null, "key_2a7b0b8cf79743fb9409e9fcfa0f45fa", 1),
    StatusAndControl_SPEVCD("StatusAndControl_SPEVCD", null, "key_56a25250df214dbbb7c1e62c8aae8801", 1),//切换新版日志
    lightxplantrunmonitor("lightxplantrunmonitor", null, "key_987c10b8393a415e91973b7a4cb6c93c", 1),//切换新版日志
    odlightxplantrunmonitor("odlightxplantrunmonitor", null, "key_081e6208d87e40ef9eeb1839024351f9", 1),//切换新版日志
    airConditioningHeatAndColdSource("airConditioningHeatAndColdSource", null, "key_081e6208d87e40ef9eeb1839024352f9", 1),//切换新版日志
    
//    VENTILATION("VENTILATION", null, "key_03a55d56f20348f5ae2578117c489cb6", 1),
    ventilatexplantrunmonitor("ventilatexplantrunmonitor", null, "key_03a55d56f20348f5ae2578117c489cb6", 1),//切换新版日志
//    KITCHEN_RANGE_HOOD("KITCHEN_RANGE_HOOD", null, "key_69d5825940c14669b2fd0621f24a8f7a", 1),
    StatusAndControl_KEF("StatusAndControl_KEF", null, "key_69d5825940c14669b2fd0621f24a8f7a", 1),//切换新版日志
//    SECURITY("SECURITY", null, "key_2b4bbb66befd48ae8b0c6df69ac7be89", 1),
    space_Security("space_Security", null, "key_2b4bbb66befd48ae8b0c6df69ac7be89", 1),//切换新版日志
    wsSewageTreatment("wsSewageTreatment", null, "key_35cf9f6389744fce8946524f9baee565", 1),//切换新版日志
    DEMAND_MANANGE("demand_manange", null, "key_c9f4d31fc4a640e4805cc93828970fa1", 1),

    /**冷站下的二级分类*/
    STRATEGY_ADJUST("strategy_adjust", "ACCC", "key_e26a590bf2e4416ba996250ca60c0c53", 2),
    SYSTEM_PARAM_MODIFY("system_param_modify", "ACCC", "key_b20290a8eb4346d383640a877c30ea50", 2),
    EQUIPMENT_MENUAL_OPERATION("equipment_menual_operation", "ACCC", "key_97c24006d3b143df8472c268fb1195e5", 2),
    CONTROL_COMMAND_RESULT("control_command_result", "ACCC", "key_3ab3e0ef6396438690affa505c4fe421", 2),
    // COLDSTATION_DEVICE_EXCEPTION("coldstation_device_exception", "ACCC", "设备异常", 2),

    /**冷站下的三级分类*/
    SCHEDULE_MODIFY("schedule_modify", "strategy_adjust", "key_b593e04f156b4d739ec76b517bcf468e", 3),
    STRATEGY_INTERVENTION("strategy_intervention", "strategy_adjust", "key_624894eeee8f4a5cb79b29b0fec034f7", 3),
    WATER_COLLECTOR_DIFF_PRESSURE_SET("water_collector_diff_pressure_set", "system_param_modify", "key_ee84783c43964da38fc89348518bc5e3", 3),
    COOL_TOWER_WATER_OUT_TEMP_SET("cool_tower_water_out_temp_set", "system_param_modify", "key_eb9326859c8746899aacb16f6207caa2", 3),
    MANUAL_SWITCH("manual_switch", "equipment_menual_operation", "key_f8dfc448b1cf48e3a0b8977f2e7e7f45", 3),
    COLDSTATION_ORDER_FAIL_ALARM_RESULT("coldstation_order_fail_alarm_result", "coldstation_device_exception", "key_f0a572661bea44208463591020227964", 3),
    COLDSTATION_ONLINE_STATUS_RESULT("coldstation_online_status_result", "coldstation_device_exception", "key_bcb07a983fc34e0e8b0878f0668597de", 3),
    EQUIPMENT_MENUAL_OPERATION_ACCCCC("equipment_menual_operation_ACCCCC", "equipment_menual_operation", "key_0e9f4a6f1ce743a7ab11c03f2f115bed", 3),
    EQUIPMENT_MENUAL_OPERATION_ACCCCP("equipment_menual_operation_ACCCCP", "equipment_menual_operation", "key_ee2c4a48170b4519a93be3374d43556f", 3),
    EQUIPMENT_MENUAL_OPERATION_ACCCOP("equipment_menual_operation_ACCCOP", "equipment_menual_operation", "key_1019bd43064a467a82451a754e95aab6", 3),
    EQUIPMENT_MENUAL_OPERATION_ACCCOT("equipment_menual_operation_ACCCOT", "equipment_menual_operation", "key_26e7898ccd254cc4b8249f187322c716", 3),
    EQUIPMENT_MENUAL_OPERATION_ACVVEO("equipment_menual_operation_ACVVEO", "equipment_menual_operation", "key_be78fb4c497c46689bea05f8e8d3cc55", 3),
    EQUIPMENT_MENUAL_OPERATION_ACVVEJ("equipment_menual_operation_ACVVEJ", "equipment_menual_operation", "key_1da146afc2634338b103735097b39f91", 3),
    CONTROL_COMMAND_RESULT_ACCCCC("control_command_result_ACCCCC", "control_command_result", "key_0e9f4a6f1ce743a7ab11c03f2f115bed", 3),
    CONTROL_COMMAND_RESULT_ACCCCP("control_command_result_ACCCCP", "control_command_result", "key_ee2c4a48170b4519a93be3374d43556f", 3),
    CONTROL_COMMAND_RESULT_ACCCOP("control_command_result_ACCCOP", "control_command_result", "key_1019bd43064a467a82451a754e95aab6", 3),
    CONTROL_COMMAND_RESULT_ACCCOT("control_command_result_ACCCOT", "control_command_result", "key_26e7898ccd254cc4b8249f187322c716", 3),
    CONTROL_COMMAND_RESULT_ACVVEO("control_command_result_ACVVEO", "control_command_result", "key_be78fb4c497c46689bea05f8e8d3cc55", 3),
    CONTROL_COMMAND_RESULT_ACVVEJ("control_command_result_ACVVEJ", "control_command_result", "key_1da146afc2634338b103735097b39f91", 3),
    
    /**
     * 需求管理下的二级分类
     */
    DEMAND_SETTING_ADJUST("demand_setting_adjust", "demand_manange", "key_dd461d73648b4cf6bc95875152855ab1", 2),
    VALUE_SELECTION_ADJUST("value_selection_adjust", "demand_manange", "key_54e7d7a3585241458e14720782ef7a2f", 2),

    /**
     * 需求管理下的三级分类
     */
    BASIC_INFO_ADJUST("basic_info_adjust", "demand_setting_adjust", "key_f095fa2885394b63868cff7c22c88c4c", 3),
    TEMP_TARGET_ADJUST("temp_target_adjust", "demand_setting_adjust", "key_04a864a71443459a9b2b924509e19a5b", 3),
    CARBON_TARGET_ADJUST("carbon_target_adjust", "demand_setting_adjust", "key_ed67cb839b794079a0c1fce67441c438", 3),
    SPACE_SHIELD_RECOVER("space_shield_recover", "demand_setting_adjust", "key_3ef902d4bfd4450bb9465a7f6933ba30", 3),
    AUTO_ADJUST_VALUE_SELECTION("auto_adjust_value_selection", "demand_setting_adjust", "key_c60bb66fb74443af92c3f4bff463aef8", 3),
    PUBLIC_LIGHTING_COMMON_CONFIG("public_lighting_common_config", "demand_setting_adjust", "key_4e57fb22cde64f5299578201b174c8c0", 3),
    PUBLIC_LIGHTING_DEMAND_CONFIG("public_lighting_demand_config", "demand_setting_adjust", "key_712f005f202d467bb1c1388aeb8797e2", 3),
    VALUE_SELECTION_SWITCH("value_selection_switch", "value_selection_adjust", "key_08b0f26c66c6424894bbfc4837592574", 3),
    ENERGY_LOWEST_ADJUST("energy_lowest_adjust", "value_selection_adjust", "key_cae5b0dcbbf54e378e2192ba92193c65", 3),
    CARBON_EMISSIONS_LOWEST_ADJUST("carbon_emissions_lowest_adjust", "value_selection_adjust", "key_547d2ba5c54d4aba835629af8dc2f043", 3),
    COST_LOWEST_ADJUST("cost_lowest_adjust", "value_selection_adjust", "key_50d017d5aa544ca39132c28e23cfeba7", 3),

    /**
     * 照明下的二级分类
     */
    LIGHT_MONITORING_MANUAL_CONTROL("light_monitoring_manual_control", "LIGHT_MONITORING", "key_97c24006d3b143df8472c268fb1195e5", 2),
    LIGHT_MONITORING_COMMAND_RESULT("lighting_command_result", "LIGHT_MONITORING", "key_3ab3e0ef6396438690affa505c4fe421", 2),
    LIGHT_MONITORING_INTERVENTION("light_monitoring_intervention", "LIGHT_MONITORING", "key_e3277b402bff46139ffd08467f50f9d0", 2),
    // LIGHT_MONITORING_DEVICE_EXCEPTION("light_monitoring_device_exception", "LIGHT_MONITORING", "设备异常", 2),

    /**
     * 照明下的三级分类
     */
    LIGHT_MONITORING_MANUAL_CONTROL_ARTIFICIAL("light_monitoring_manual_control_artificial", "light_monitoring_manual_control", "key_c0293c3c5576420c9989dd329fd8ce09", 3),
    LIGHT_MONITORING_COMMAND_RESULT_LTLTCC("light_monitoring_command_result_LTLTCC", "lighting_command_result", "key_10829ce5750c42f099af7dc60ca0205d", 3),
    LIGHT_MONITORING_ORDER_FAIL_ALARM_RESULT("light_monitoring_order_fail_alarm_result", "light_monitoring_device_exception", "key_f0a572661bea44208463591020227964", 3),
    LIGHT_MONITORING_ONLINE_STATUS_RESULT("light_monitoring_online_status_result", "light_monitoring_device_exception", "key_bcb07a983fc34e0e8b0878f0668597de", 3),
    
    
    /********************************************** 热源 *************************************/
    /**
     * 热源下的二级分类
     */
    HOT_STRATEGY_ADJUST("hot_strategy_adjust", "ACCH", "key_e26a590bf2e4416ba996250ca60c0c53", 2),
    HOT_SYSTEM_PARAM_MODIFY("hot_system_param_modify", "ACCH", "key_b20290a8eb4346d383640a877c30ea50", 2),
    HOT_EQUIPMENT_MENUAL_OPERATION("hot_equipment_menual_operation", "ACCH", "key_97c24006d3b143df8472c268fb1195e5", 2),
    HOT_CONTROL_COMMAND_RESULT("hot_control_command_result", "ACCH", "key_3ab3e0ef6396438690affa505c4fe421", 2),
    // HOT_DEVICE_EXCEPTION("hot_device_exception", "ACCH", "设备异常", 2),

    /**热源下的三级分类*/
    HOT_ORDER_FAIL_ALARM_RESULT("hot_order_fail_alarm_result", "hot_device_exception", "key_f0a572661bea44208463591020227964", 3),
    HOT_ONLINE_STATUS_RESULT("hot_online_status_result", "hot_device_exception", "key_bcb07a983fc34e0e8b0878f0668597de", 3),

    HOT_SYSTEM_SCHEDULE_MODIFY("hot_system_schedule_modify", "hot_strategy_adjust", "key_b593e04f156b4d739ec76b517bcf468e", 3),
    HOT_SYSTEM_STRATEGY_INTERVENTION("hot_system_strategy_intervention", "hot_strategy_adjust", "key_624894eeee8f4a5cb79b29b0fec034f7", 3),
    HOT_SYSTEM_TEMP_DIFF_PRESSURE_SET("hot_system_temp_diff_pressure_set", "hot_system_param_modify", "key_6921e11665ab4f55a5268b9a5aff5790", 3),
    HOT_SYSTEM_WATER_COLLECTOR_DIFF_PRESSURE_SET("hot_system_water_collector_diff_pressure_set", "hot_system_param_modify", "key_552ec5e4c2fa4ebf8f789ac39218d37a", 3),
    HOT_SYSTEM_MANUAL_SWITCH("hot_system_manual_switch", "hot_equipment_menual_operation", "key_f8dfc448b1cf48e3a0b8977f2e7e7f45", 3),

    HOT_EQUIPMENT_MENUAL_OPERATION_ACGEHE("hot_equipment_menual_operation_ACGEHE", "hot_equipment_menual_operation", "key_7b39d2004dd84ee6a5ecb8c78b8a169a", 3),
    HOT_EQUIPMENT_MENUAL_OPERATION_ACCHHP("hot_equipment_menual_operation_ACCHHP", "hot_equipment_menual_operation", "key_57464a3438e443669e508e35d1f27e02", 3),
    HOT_EQUIPMENT_MENUAL_OPERATION_ACCHGB("hot_equipment_menual_operation_ACCHGB", "hot_equipment_menual_operation", "key_bf040a2cf97a4da593dd4ff8e2df5659", 3),
    HOT_EQUIPMENT_MENUAL_OPERATION_ACVVEO("hot_equipment_menual_operation_ACVVEO", "hot_equipment_menual_operation", "key_be78fb4c497c46689bea05f8e8d3cc55", 3),
    HOT_EQUIPMENT_MENUAL_OPERATION_ACVVEJ("hot_equipment_menual_operation_ACVVEJ", "hot_equipment_menual_operation", "key_1da146afc2634338b103735097b39f91", 3),
    HOT_CONTROL_COMMAND_RESULT_ACGEHE("hot_control_command_result_ACGEHE", "hot_control_command_result", "key_7b39d2004dd84ee6a5ecb8c78b8a169a", 3),
    HOT_CONTROL_COMMAND_RESULT_ACCHHP("hot_control_command_result_ACCHHP", "hot_control_command_result", "key_57464a3438e443669e508e35d1f27e02", 3),
    HOT_CONTROL_COMMAND_RESULT_ACCHGB("hot_control_command_result_ACCHGB", "hot_control_command_result", "key_bf040a2cf97a4da593dd4ff8e2df5659", 3),
    HOT_CONTROL_COMMAND_RESULT_ACVVEO("hot_control_command_result_ACVVEO", "hot_control_command_result", "key_be78fb4c497c46689bea05f8e8d3cc55", 3),
    HOT_CONTROL_COMMAND_RESULT_ACVVEJ("hot_control_command_result_ACVVEJ", "hot_control_command_result", "key_1da146afc2634338b103735097b39f91", 3),


    /**厨房排油烟二级分类*/
    KITCHEN_RANGE_HOOD_MANUAL_CONTROL("kitchen_range_hood_manual_control", "KITCHEN_RANGE_HOOD", "key_97c24006d3b143df8472c268fb1195e5", 2),
    KITCHEN_RANGE_HOOD_COMMAND_RESULT("kitchen_range_hood_command_result", "KITCHEN_RANGE_HOOD", "key_3ab3e0ef6396438690affa505c4fe421", 2),
    // KITCHEN_RANGE_DEVICE_EXCEPTION("kitchen_range_device_exception", "KITCHEN_RANGE_HOOD", "设备异常", 2),

    /**厨房排油烟三级分类*/
    KITCHEN_RANGE_HOOD_MANUAL_CONTROL_ACKLSF("kitchen_range_hood_manual_control_ACKLSF", "kitchen_range_hood_manual_control", "key_8c661f00552643dda5bd06e0316a4f77", 3),
    KITCHEN_RANGE_HOOD_MANUAL_CONTROL_ACATFU("kitchen_range_hood_manual_control_ACATFU", "kitchen_range_hood_manual_control", "key_128dcb016f7d444992e96dfc92acb8c4", 3),
    KITCHEN_RANGE_HOOD_MANUAL_CONTROL_ACVTSF("kitchen_range_hood_manual_control_ACVTSF", "kitchen_range_hood_manual_control", "key_e5beefd52e684925b7b1dc89f28a8ca8", 3),
    KITCHEN_RANGE_HOOD_MANUAL_CONTROL_ACKLAP("kitchen_range_hood_manual_control_ACKLAP", "kitchen_range_hood_manual_control", "key_12287cb380244b1c9d3e8d98dbfad00d", 3),

    KITCHEN_RANGE_HOOD_COMMAND_RESULT_ACKLSF("kitchen_range_hood_command_result_ACKLSF", "kitchen_range_hood_command_result", "key_8c661f00552643dda5bd06e0316a4f77", 3),
    KITCHEN_RANGE_HOOD_COMMAND_RESULT_ACATFU("kitchen_range_hood_command_result_ACATFU", "kitchen_range_hood_command_result", "key_128dcb016f7d444992e96dfc92acb8c4", 3),
    KITCHEN_RANGE_HOOD_COMMAND_RESULT_ACVTSF("kitchen_range_hood_command_result_ACVTSF", "kitchen_range_hood_command_result", "key_e5beefd52e684925b7b1dc89f28a8ca8", 3),
    KITCHEN_RANGE_HOOD_COMMAND_RESULT_ACKLAP("kitchen_range_hood_command_result_ACKLAP", "kitchen_range_hood_command_result", "key_12287cb380244b1c9d3e8d98dbfad00d", 3),

    KITCHEN_RANGE_ORDER_FAIL_ALARM_RESULT("kitchen_range_order_fail_alarm_result", "kitchen_range_device_exception", "key_f0a572661bea44208463591020227964", 3),
    KITCHEN_RANGE_ONLINE_STATUS_RESULT("kitchen_range_online_status_result", "kitchen_range_device_exception", "key_bcb07a983fc34e0e8b0878f0668597de", 3),

    /**通风二级分类*/
    VENTILATION_MANUAL_CONTROL("ventilation_manual_control", "VENTILATION", "key_97c24006d3b143df8472c268fb1195e5", 2),
    VENTILATION_COMMAND_RESULT("ventilation_command_result", "VENTILATION", "key_3ab3e0ef6396438690affa505c4fe421", 2),
    // VENTILATION_DEVICE_EXCEPTION("ventilation_device_exception", "VENTILATION", "设备异常", 2),

    /**通风三级分类*/
    VENTILATION_ORDER_FAIL_ALARM_RESULT("ventilation_order_fail_alarm_result", "ventilation_device_exception", "key_f0a572661bea44208463591020227964", 3),
    VENTILATION_ONLINE_STATUS_RESULT("ventilation_online_status_result", "ventilation_device_exception", "key_bcb07a983fc34e0e8b0878f0668597de", 3),

    VENTILATION_MANUAL_CONTROL_FFSCAF("ventilation_manual_control_ACKLSF", "ventilation_manual_control", "key_a5f73583fccd4853a7c060e126a03e2c", 3),
    VENTILATION_MANUAL_CONTROL_ACVTSF("ventilation_manual_control_ACVTSF", "ventilation_manual_control", "key_e5beefd52e684925b7b1dc89f28a8ca8", 3),
    VENTILATION_MANUAL_CONTROL_FFSCMF("ventilation_manual_control_FFSCMF", "ventilation_manual_control", "key_8cc120ed974043d8872de690b9ab6286", 3),

    VENTILATION_COMMAND_RESULT_FFSCAF("ventilation_command_result_ACKLSF", "ventilation_command_result", "key_a5f73583fccd4853a7c060e126a03e2c", 3),
    VENTILATION_COMMAND_RESULT_ACVTSF("ventilation_command_result_ACVTSF", "ventilation_command_result", "key_e5beefd52e684925b7b1dc89f28a8ca8", 3),
    VENTILATION_COMMAND_RESULT_FFSCMF("ventilation_command_result_FFSCMF", "ventilation_command_result", "key_8cc120ed974043d8872de690b9ab6286", 3),

    /**安防二级分类*/
    SECURITY_EQUIPMENT_MANUAL_FORCE_OPERATE("security_equipment_manual_force_operate", "SECURITY", "key_97c24006d3b143df8472c268fb1195e5", 2),
    SECURITY_COMMAND_RESULT("security_command_result", "SECURITY", "key_3ab3e0ef6396438690affa505c4fe421", 2),
    // SECURITY_DEVICE_EXCEPTION("security_device_exception", "SECURITY", "设备异常", 2),

    /**安防三级分类*/
    SECURITY_EQUIPMENT_MANUAL_FORCE_OPERATE_SPEGCR_CFCSDR_CFCSFR_SPEGFG("security_equipment_manual_force_operate_SPEGCR_CFCSDR_CFCSFR_SPEGFG", "security_equipment_manual_force_operate", "key_6f9a72e8896f45d1b675014388cf4bfb", 3),
    SECURITY_EQUIPMENT_MANUAL_FORCE_OPERATE_SPVSCM("security_equipment_manual_force_operate_SPVSCM", "security_equipment_manual_force_operate", "key_5a477826b9e3444799a5968b75470e9f", 3),
    SECURITY_EQUIPMENT_MANUAL_FORCE_OPERATE_SPIACU("security_equipment_manual_force_operate_SPIACU", "security_equipment_manual_force_operate", "key_1687a857bead42d8809f4cd23155b15c", 3),

    SECURITY_COMMAND_RESULT_SPEGCR_CFCSDR_CFCSFR_SPEGFG("security_command_result_SPEGCR_CFCSDR_CFCSFR_SPEGFG", "security_command_result", "key_6f9a72e8896f45d1b675014388cf4bfb", 3),
    SECURITY_COMMAND_RESULT_SPVSCM("security_command_result_SPVSCM", "security_command_result", "key_5a477826b9e3444799a5968b75470e9f", 3),
    SECURITY_COMMAND_RESULT_SPIACU("security_command_result_SPIACU", "security_command_result", "key_1687a857bead42d8809f4cd23155b15c", 3),

    SECURITY_ORDER_FAIL_ALARM_RESULT("security_order_fail_alarm_result", "security_device_exception", "key_f0a572661bea44208463591020227964", 3),
    SECURITY_ONLINE_STATUS_RESULT("security_online_status_result", "security_device_exception", "key_bcb07a983fc34e0e8b0878f0668597de", 3),

    /********************************************** 生活热水 *************************************/
    /**生活热水一级分类*/
    StatusAndControl_WS("StatusAndControl_WS", null, "key_statusandcontrol_ws", 1),

    /**生活热水下的二级分类*/
    StatusAndControl_WS_WSDHHE("StatusAndControl_WS_WSDHHE", "StatusAndControl_WS", "key_statusandcontrol_ws_wsdhhe", 2),
    StatusAndControl_WS_WSDHHP("StatusAndControl_WS_WSDHHP", "StatusAndControl_WS", "key_statusandcontrol_ws_wsdhhp", 2),

    ;
    /**
     * 分类编码
     */
    private String code;

    /**
     * 上级分类编码
     */
    private String parentCode;
    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类等级
     */
    private int level;

    public static List<RunningLogCategoryEnum> getEnumByParentCode(String parentCode) {
        List<RunningLogCategoryEnum> childRunningLogCategoryEnums = new ArrayList<>();
        for (RunningLogCategoryEnum categoryEnum : values()) {
            if (categoryEnum.getParentCode() != null && categoryEnum.getParentCode().equals(parentCode)) {
                childRunningLogCategoryEnums.add(categoryEnum);
            }
        }
        return childRunningLogCategoryEnums;
    }

    public static RunningLogCategoryEnum getEnumByCode(String code) {
        for (RunningLogCategoryEnum categoryEnum : values()) {
            if (categoryEnum.getCode().equals(code)) {
                return categoryEnum;
            }
        }
        return null;
    }

    public static RunningLogCategoryEnum getEnumByName(String name) {
        for (RunningLogCategoryEnum categoryEnum : values()) {
            if (categoryEnum.getName().equals(name)) {
                return categoryEnum;
            }
        }
        return null;
    }


    public static List<RunningLogCategoryDto> getCategoryList() {
        List<RunningLogCategoryDto> categoryDtos = new ArrayList<>();
        for (RunningLogCategoryEnum categoryEnum : values()) {
            RunningLogCategoryDto dto = new RunningLogCategoryDto();
            dto.setCode(categoryEnum.code);
            dto.setName(categoryEnum.getName());
            dto.setParentCode(categoryEnum.parentCode);
            dto.setLevel(categoryEnum.level);
            categoryDtos.add(dto);
        }
        return categoryDtos;
    }

    /**
     * 手动执行操作记录
     *
     * @param infoCode
     * @param businessFeildDTO
     * @return
     */
    public static RunningLogCategoryEnum getManualResultLogCategory(String infoCode, RwdObjectBusinessFeildDTO businessFeildDTO) {
        if (businessFeildDTO == null || businessFeildDTO.getSystemType() == null) {
            return null;
        }
        String classCode = businessFeildDTO.getClassCode();
        DtpRwdSystemClassCodeEnum systemType = businessFeildDTO.getSystemType();

        if ("coolWaterReturnTempSet".equals(infoCode)) {
            // 冷却塔出水温度设定
            return DtpRwdSystemClassCodeEnum.ACCC.equals(systemType) ? COOL_TOWER_WATER_OUT_TEMP_SET : null;
        } else if ("distributorCollectorDetaPressSet".equals(infoCode)) {
            // 分集水器压差设定、旁通阀压差设定
            return DtpRwdSystemClassCodeEnum.ACCC.equals(systemType) ? WATER_COLLECTOR_DIFF_PRESSURE_SET
                    : DtpRwdSystemClassCodeEnum.ACCH.equals(systemType) ? HOT_SYSTEM_WATER_COLLECTOR_DIFF_PRESSURE_SET : null;
        }

        if (RunningLogObjectCategpryEnum.SYSTEM.getCode().equals(businessFeildDTO.getObjType())) {
            //一键手自动切换菜单分类
            if (DtpRwdSystemClassCodeEnum.ACCC.value().equals(classCode)) {
                return MANUAL_SWITCH;
            } else if (DtpRwdSystemClassCodeEnum.ACCH.value().equals(classCode)) {
                return HOT_SYSTEM_MANUAL_SWITCH;
            }
        }
        if (DtpRwdSystemClassCodeEnum.ACCC.equals(systemType)) {
            switch (classCode) {
                case "ACCCCC":
                case "ACCCSC":
                    return EQUIPMENT_MENUAL_OPERATION_ACCCCC;
                case "ACCCCP":
                    return EQUIPMENT_MENUAL_OPERATION_ACCCCP;
                case "ACCCOP":
                    return EQUIPMENT_MENUAL_OPERATION_ACCCOP;
                case "ACOTU":
                case "ACCCOF":
                    return EQUIPMENT_MENUAL_OPERATION_ACCCOT;
                case "ACVVEO":
                    return EQUIPMENT_MENUAL_OPERATION_ACVVEO;
                case "ACVVEJ":
                    return EQUIPMENT_MENUAL_OPERATION_ACVVEJ;
                default:
                    return null;
            }
        }
        if (DtpRwdSystemClassCodeEnum.ACCH.equals(systemType)) {
            switch (classCode) {
                case "ACGEHE":
                    return RunningLogCategoryEnum.HOT_EQUIPMENT_MENUAL_OPERATION_ACGEHE;
                case "ACCHHP":
                    return RunningLogCategoryEnum.HOT_EQUIPMENT_MENUAL_OPERATION_ACCHHP;
                case "ACCHGB":
                    return RunningLogCategoryEnum.HOT_EQUIPMENT_MENUAL_OPERATION_ACCHGB;
                case "ACVVEO":
                    return RunningLogCategoryEnum.HOT_EQUIPMENT_MENUAL_OPERATION_ACVVEO;
                case "ACVVEJ":
                    return RunningLogCategoryEnum.HOT_EQUIPMENT_MENUAL_OPERATION_ACVVEJ;
                default:
                    return null;
            }
        }
        
        if (DtpRwdSystemClassCodeEnum.LIGHT_MONITORING.equals(systemType)) {
            switch (classCode) {
                case "LTLTCC":
                    return LIGHT_MONITORING_MANUAL_CONTROL_ARTIFICIAL;
                default:
                    return null;
            }
        }
        if (DtpRwdSystemClassCodeEnum.VENTILATION.equals(systemType)) {
            switch (classCode) {
                case "ACVTSF":
                    if (businessFeildDTO.enableVentilationFunType()) {
                        return VENTILATION_MANUAL_CONTROL_ACVTSF;
                    }
                    break;
                case "FFSCAF":
                    return VENTILATION_MANUAL_CONTROL_FFSCAF;
                case "FFSCMF":
                    return VENTILATION_MANUAL_CONTROL_FFSCMF;
                default:
                    return null;
            }
        }
        if (DtpRwdSystemClassCodeEnum.SECURITY.equals(systemType)) {
            switch (classCode) {
                case "SPEGCR":
                case "CFCSDR":
                case "CFCSFR":
                case "SPEGFG":
                    return SECURITY_EQUIPMENT_MANUAL_FORCE_OPERATE_SPEGCR_CFCSDR_CFCSFR_SPEGFG;
                case "SPVSCM":
                    if (businessFeildDTO.enableSecurityCameraType()) {
                        return SECURITY_EQUIPMENT_MANUAL_FORCE_OPERATE_SPVSCM;
                    }
                    break;
                case "SPIACU":
                    return SECURITY_EQUIPMENT_MANUAL_FORCE_OPERATE_SPIACU;
                default:
                    return null;
            }
        }
        if (DtpRwdSystemClassCodeEnum.KITCHEN_RANGE_HOOD.equals(systemType)) {
            switch (classCode) {
                case "ACKLSF":
                    return KITCHEN_RANGE_HOOD_MANUAL_CONTROL_ACKLSF;
                case "ACATFU":
                    return KITCHEN_RANGE_HOOD_MANUAL_CONTROL_ACATFU;
                case "ACVTSF":
                    if (businessFeildDTO.enableKitchenRangeHoodFunType()) {
                        return KITCHEN_RANGE_HOOD_MANUAL_CONTROL_ACVTSF;
                    }
                    break;
                case "ACKLAP":
                    return KITCHEN_RANGE_HOOD_MANUAL_CONTROL_ACKLAP;
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 控制指令执行结果
     *
     * @param businessFeildDTO
     * @return
     */
    public static RunningLogCategoryEnum getCommandResultLogCategory(RwdObjectBusinessFeildDTO businessFeildDTO) {
        if (businessFeildDTO == null || businessFeildDTO.getSystemType() == null) {
            return null;
        }

        String classCode = businessFeildDTO.getClassCode();
        DtpRwdSystemClassCodeEnum systemType = businessFeildDTO.getSystemType();

        if (DtpRwdSystemClassCodeEnum.ACCC.equals(systemType)) {
            switch (classCode) {
                case "ACCCCC":
                case "ACCCSC":
                    return CONTROL_COMMAND_RESULT_ACCCCC;
                case "ACCCCP":
                    return RunningLogCategoryEnum.CONTROL_COMMAND_RESULT_ACCCCP;
                case "ACCCOP":
                    return RunningLogCategoryEnum.CONTROL_COMMAND_RESULT_ACCCOP;
                case "ACCCOF":
                case "ACOTU":
                    return RunningLogCategoryEnum.CONTROL_COMMAND_RESULT_ACCCOT;
                case "ACVVEO":
                    return RunningLogCategoryEnum.CONTROL_COMMAND_RESULT_ACVVEO;
                case "ACVVEJ":
                    return RunningLogCategoryEnum.CONTROL_COMMAND_RESULT_ACVVEJ;
                default:
                    return null;
            }
        }
        
        if (DtpRwdSystemClassCodeEnum.ACCH.equals(systemType)) {
            switch (classCode) {
                case "ACGEHE":
                    return RunningLogCategoryEnum.HOT_CONTROL_COMMAND_RESULT_ACGEHE;
                case "ACCHHP":
                    return RunningLogCategoryEnum.HOT_CONTROL_COMMAND_RESULT_ACCHHP;
                case "ACCHGB":
                    return RunningLogCategoryEnum.HOT_CONTROL_COMMAND_RESULT_ACCHGB;
                case "ACVVEO":
                    return RunningLogCategoryEnum.HOT_CONTROL_COMMAND_RESULT_ACVVEO;
                case "ACVVEJ":
                    return RunningLogCategoryEnum.HOT_CONTROL_COMMAND_RESULT_ACVVEJ;
                default:
                    return null;
            }
        }
        
        if (DtpRwdSystemClassCodeEnum.LIGHT_MONITORING.equals(systemType)) {
            switch (classCode) {
                case "LTLTCC":
                    return LIGHT_MONITORING_COMMAND_RESULT_LTLTCC;
                default:
                    return null;
            }
        }
        if (DtpRwdSystemClassCodeEnum.VENTILATION.equals(systemType)) {
            switch (classCode) {
                case "ACVTSF":
                    if (businessFeildDTO.enableVentilationFunType()) {
                        return VENTILATION_COMMAND_RESULT_ACVTSF;
                    }
                    break;
                case "FFSCAF":
                    return VENTILATION_COMMAND_RESULT_FFSCAF;
                case "FFSCMF":
                    return VENTILATION_COMMAND_RESULT_FFSCMF;
                default:
                    return null;
            }
        }
        if (DtpRwdSystemClassCodeEnum.SECURITY.equals(systemType)) {
            switch (classCode) {
                case "SPEGCR":
                case "CFCSDR":
                case "CFCSFR":
                case "SPEGFG":
                    return SECURITY_COMMAND_RESULT_SPEGCR_CFCSDR_CFCSFR_SPEGFG;
                case "SPVSCM":
                    if (businessFeildDTO.enableSecurityCameraType()) {
                        return SECURITY_COMMAND_RESULT_SPVSCM;
                    }
                    break;
                case "SPIACU":
                    return SECURITY_COMMAND_RESULT_SPIACU;
                default:
                    return null;
            }
        }
        if (DtpRwdSystemClassCodeEnum.KITCHEN_RANGE_HOOD.equals(systemType)) {
            switch (classCode) {
                case "ACKLSF":
                    return KITCHEN_RANGE_HOOD_COMMAND_RESULT_ACKLSF;
               // case "ACATFU": 去掉
               //     return KITCHEN_RANGE_HOOD_COMMAND_RESULT_ACATFU;
                case "ACVTSF":
                    if (businessFeildDTO.enableKitchenRangeHoodFunType()) {
                        return KITCHEN_RANGE_HOOD_COMMAND_RESULT_ACVTSF;
                    }
                    break;
                case "ACKLAP":
                    return KITCHEN_RANGE_HOOD_COMMAND_RESULT_ACKLAP;
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 控制需求目标调整
     *
     * @param businessFeildDTO
     * @return
     */
    public static RunningLogCategoryEnum getCommandChangeLogCategory(RwdObjectBusinessFeildDTO businessFeildDTO) {
        if (businessFeildDTO == null || businessFeildDTO.getSystemType() == null) {
            return null;
        }

        DtpRwdSystemClassCodeEnum systemType = businessFeildDTO.getSystemType();

        if (DtpRwdSystemClassCodeEnum.ACCC.equals(systemType)) {
            return STRATEGY_INTERVENTION;
        }
        if (DtpRwdSystemClassCodeEnum.ACCH.equals(systemType)) {
            return HOT_SYSTEM_STRATEGY_INTERVENTION;
        }
        return null;
    }

    @Override
    public String getValue() {
        return this.code;
    }

    @Override
    public String value() {
        return this.code;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.name);
    }
    public String getName() {
        return LocaleMessageUtil.getMessage(this.name);
    }
}
