package com.persagy.apccontrol.basic.outside.feign.model.response.calendar;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作历日期标签响应
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MidCalendarDayDateTagResponse extends BaseResponse {

    /**
     * 工作历ID
     */
    private String workId;

    /**
     * 当前日期
     */
    private LocalDateTime currentDay;

    /**
     * 年
     */
    private Integer year;

    /**
     * 月
     */
    private Integer month;

    /**
     * 日
     */
    private Integer day;

    /**
     * 星期
     */
    private Integer week;

    /**
     * 日期标签
     *
     * @see com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum
     */
    private List<String> tagList;

}
