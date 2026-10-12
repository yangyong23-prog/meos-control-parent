package com.persagy.apccontrol.basic.model.vo.midcalendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 工作历营业时间VO
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@Data
public class MidCalendarWorkTimeVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "是否为工作日")
    private Boolean workday;

    @Schema(description = "是否为店庆日")
    private Boolean anniversary;

    @Schema(description = "营业时间详情")
    private List<MidCalendarWorkTimeDetailVO> details;

    @Data
    public static class MidCalendarWorkTimeDetailVO {

        @Schema(description = "对象ID")
        private String objectId;

        @Schema(description = "营业时间ID")
        private String workId;

        @Schema(description = "开始营业时间")
        private LocalTime beginWorkTime;

        @Schema(description = "结束营业时间")
        private LocalTime endWorkTime;

    }
}
