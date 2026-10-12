package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * 工作历规则查询参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
public class MidCalendarRuleQueryParam {

    /**
     * 工作历ID
     */
    @Nullable
    private String workId;

    /**
     * 日期编码
     */
    @Nullable
    private String tagType;

    /**
     * 日期值
     */
    @Nullable
    private String tagValue;

}
