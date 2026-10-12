package com.persagy.apccontrol.basic.model.enumtype;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * @Description: 数据同步
 * @author: yancheng
 * @date: 2023/3/16
 * @version: V1.0
 */
@AllArgsConstructor
public enum DataSynRunEnum implements BaseEnum<String> {


    /**
     * 基础任务
     */
    RC_BASICS_ASYNC_TASK("rc_basics_async_task", "key_13f7b8dabd354643b5b495198cf895c5"),

    /**
     * 基础配置
     */
    RC_BASICS_CONFIG("rc_basics_config", "key_4ae8f5538a474f8888838d2de1d054f9"),

    /**
     * 系统图表 系统图数据
     */
    RC_SYSTEM_PIC("rc_system_pic", "key_6c2fd467e97943d29116400be532e062"),

    /**
     * 系统图表 里面的图片
     */
    RC_SYSTEM_PIC_EQUIPMENT("rc_system_pic_equipment", "key_9a33809fbf6640edbf0a390d6b7d7b04"),

    /**
     * 系统设备信息关系表
     */
    RC_EQ_INFO_CODE_CFG("rc_eq_info_code_cfg", "key_e6ce21afee6945a49a091d5f8640792d"),

    /**
     * 系统信息关系表
     */
    RC_INFO_CODE_CFG("rc_info_code_cfg", "key_36be53680e144802927329a4209536e2");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DataSynRunEnum resolve(String value) {
        return BaseEnum.analyze(value, DataSynRunEnum.class, true, false);
    }
}
