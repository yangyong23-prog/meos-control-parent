package com.persagy.apccontrol.basic.model.enumtype.runningLog;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RunningLogTemplateEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 日志模版
     */
    GENERAL(LocaleMessageUtil.getMessage("key_old_runninglog_general_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_general_desc"),
            LocaleMessageUtil.getMessage("key_old_runninglog_general_keyWord")),
    
    SWITCH_RUN_MODE(LocaleMessageUtil.getMessage("key_old_runninglog_switch_run_mode_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_switch_run_mode_desc"),
            LocaleMessageUtil.getMessage("key_old_runninglog_switch_run_mode_keyword")),
    OPEN_COMMAND_RESULT("{0}{1}{2}",
            LocaleMessageUtil.getMessage("key_old_runninglog_open_command_result_desc"),
            "{0} {2}"),
    SCHEDULE_COMMAND_RESULT("{0}{1}",
            LocaleMessageUtil.getMessage("key_old_runninglog_schedule_command_result_desc"),
            "{0} {1}"),
    
    //操作详情模板
    CONTEN_DETAIL(LocaleMessageUtil.getMessage("key_old_runninglog_content_detail_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_content_detail_desc"),
            ""),
    PARAM_ADJUST_DETAIL(LocaleMessageUtil.getMessage("key_old_runninglog_param_adjust_detail_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_param_adjust_detail_desc"),
            ""),
    VALUE_SELECTION_PARAM_ADJUST_DETAIL(LocaleMessageUtil.getMessage("key_old_runninglog_value_selection_param_adjust_detail_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_value_selection_param_adjust_detail_desc"),
            ""),
    VALUE_SELECTION_CONFIG_ADJUST_DETAIL(LocaleMessageUtil.getMessage("key_old_runninglog_value_selection_config_adjust_detail_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_value_selection_config_adjust_detail_desc"),
            ""),
    VALUE_SELECTION_PARAM_CONFIG_ADJUST_DETAIL(LocaleMessageUtil.getMessage(
            "key_old_runninglog_value_selection_param_config_adjust_detail_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_value_selection_param_config_adjust_detail_desc"),
            ""),
    
    /**
     * 照明详情模板-设备手动控制-人工
     **/
    LIGHTING_SINGLE_ON(LocaleMessageUtil.getMessage("key_old_runninglog_lighting_single_on_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_lighting_single_on_desc"),
            LocaleMessageUtil.getMessage("key_old_runninglog_lighting_single_on_keyword")),
    LIGHTING_BATCH_ON(LocaleMessageUtil.getMessage("key_old_runninglog_lighting_batch_on_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_lighting_batch_on_desc"),
            LocaleMessageUtil.getMessage("key_old_runninglog_lighting_batch_on_keyword")),
    LIGHTING_SINGLE_OFF(LocaleMessageUtil.getMessage("key_old_runninglog_lighting_single_off_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_lighting_single_off_desc"),
            LocaleMessageUtil.getMessage("key_old_runninglog_lighting_single_off_keyword")),
    LIGHTING_BATCH_OFF(LocaleMessageUtil.getMessage("key_old_runninglog_lighting_batch_off_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_lighting_batch_off_desc"),
            LocaleMessageUtil.getMessage("key_old_runninglog_lighting_batch_off_keyword")),
    
    /**
     * 设备手动/强制操作
     **/
    EQUIPMENT_MODE_SWITCH(LocaleMessageUtil.getMessage("key_old_runninglog_equipment_mode_switch_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_equipment_mode_switch_desc"),
            LocaleMessageUtil.getMessage("key_old_runninglog_equipment_mode_switch_keyword")),
    EQUIPMENT_MODE_SWITCH_BATCH(LocaleMessageUtil.getMessage("key_old_runninglog_equipment_mode_switch_batch_template"),
            LocaleMessageUtil.getMessage("key_old_runninglog_equipment_mode_switch_batch_desc"),
            LocaleMessageUtil.getMessage("key_old_runninglog_equipment_mode_switch_batch_keyword"));
    

    /**
     * 模版信息
     **/
    private final String template;
    /**
     * 描述
     **/
    private final String desc;
    /**
     * 关键词
     **/
    private final String keyWord;

    @Override
    public String getValue() {
        return this.template;
    }

    @Override
    public String value() {
        return this.template;
    }

    @Override
    public String getReasonPhrase() {
        return this.desc;
    }
}
