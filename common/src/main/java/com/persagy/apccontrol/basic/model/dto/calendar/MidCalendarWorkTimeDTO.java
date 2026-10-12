package com.persagy.apccontrol.basic.model.dto.calendar;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 工作历营业时间DTO
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Data
public class MidCalendarWorkTimeDTO {

    public static final String DEFAULT_WORK_TIME_SPLIT = "-";

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 是否为工作日
     */
    private Boolean workday;

    /**
     * 是否为店庆日
     */
    private Boolean anniversary;

    /**
     * 工作历ID
     */
    private String workId;

    /**
     * 开始营业时间
     */
    private LocalTime beginWorkTime;

    /**
     * 结束营业时间
     */
    private LocalTime endWorkTime;

}
