package com.persagy.apccontrol.basic.structure.bizcheck.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *业务检查策略类型枚举
 *
 * @author wxf
 * @date 2026-02-27
 */
@Getter
@AllArgsConstructor
public enum BizCheckStrategyTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     *工作日历检查
     */
    WORK_CALENDAR("WORK_CALENDAR", "key_check_type_work_calendar"),

    /**
     *空调运行标准检查
     */
    HVAC_OPERATION_STANDARD("HVAC_OPERATION_STANDARD", "key_check_type_hvac_operation_standard"),

    /**
     *单价配置检查
     */
    UNIT_PRICE_CONFIG("UNIT_PRICE_CONFIG", "key_check_type_unit_price_config"),

    /**
     *系统关系图检查
     */
    SYSTEM_RELATIONSHIP_DIAGRAM("SYSTEM_RELATIONSHIP_DIAGRAM", "key_check_type_system_relationship_diagram");

    /**
     *枚值举值
     */
    private final String value;

    /**
     *枚信息信息
     */
    private final String reasonPhrase;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static BizCheckStrategyTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, BizCheckStrategyTypeEnum.class);
    }
}