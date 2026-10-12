package com.persagy.apccontrol.basic.model.dto.calendar;

import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarObjectTypeEnum;
import lombok.Data;

/**
 * 工作历DTO
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
public class MidCalendarDTO {

    // ------------------------- 基础字段 -------------------------

    /**
     * 工作历ID
     */
    private String calendarId;

    /**
     * 工作历名称
     */
    private String calendarName;

    // ------------------------- 拓展字段, 当未指定查询对象时才会存在 -------------------------

    /**
     * 对象ID
     */
    private String objectId;

    /**
     * 对象名称
     */
    private String objectName;

    /**
     * 对象类型
     */
    private MidCalendarObjectTypeEnum objectType;

}
