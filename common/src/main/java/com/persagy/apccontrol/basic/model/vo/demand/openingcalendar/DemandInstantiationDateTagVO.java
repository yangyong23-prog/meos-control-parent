package com.persagy.apccontrol.basic.model.vo.demand.openingcalendar;

import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 需求管理-营业日历-实例化日期标签VO
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Schema(title = "需求管理-营业日历-实例化日期标签VO")
public class DemandInstantiationDateTagVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "工作历日期标签")
    private List<MidCalendarDateTagValueEnum> calendarTags;

}
