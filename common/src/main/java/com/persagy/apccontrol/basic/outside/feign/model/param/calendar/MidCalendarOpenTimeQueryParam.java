package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarScheduleTagTypeEnum;
import lombok.Data;

/**
 *
 */
@Data
public class MidCalendarOpenTimeQueryParam extends MidCalendarQueryParam {

    /**
     * 格式20240901
     */
    private String workDateStart;
    private String workDateEnd;
    /**
     *营业Opening
     */
    private MidCalendarScheduleTagTypeEnum dictType;

}
