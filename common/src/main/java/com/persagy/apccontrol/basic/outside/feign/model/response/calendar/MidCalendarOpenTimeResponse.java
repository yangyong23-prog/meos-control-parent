package com.persagy.apccontrol.basic.outside.feign.model.response.calendar;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 工作查询对象营业时间
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MidCalendarOpenTimeResponse extends BaseResponse {

    /**
     * 星期
     */
    private String week;

    /**
     * 日期, yyyyMMdd
     */
    private String workDate;

    /**
     * 是否为工作日
     */
    private Boolean workday;

    /**
     * 是否为店庆日
     */
    private Boolean anniversary;

    /**
     * 时间表
     */
    private List<TimeTable> timeTable;

    @Data
    @ToString(callSuper = true)
    public static class TimeTable {

        /**
         * 工作历ID
         */
        private String workId;

        /**
         * 字典类型
         */
        private String dictType;

        /**
         * 营业时间段  形如11:00-19:00这样的字符串
         */
        private String workTime;

    }
}
