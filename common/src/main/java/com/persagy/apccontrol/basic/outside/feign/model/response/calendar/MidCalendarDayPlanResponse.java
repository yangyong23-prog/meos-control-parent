package com.persagy.apccontrol.basic.outside.feign.model.response.calendar;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工作历计划内容响应
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MidCalendarDayPlanResponse extends BaseResponse {

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
     * 计划内容
     */
    private List<PlanContent> planContents;


    /**
     * 计划内容
     *
     * @author JiYinchuan
     * @since v5.7.0
     */
    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public static class PlanContent extends BaseResponse {

        /**
         * 每天24小时, 按照5分钟进行分割, 总共分割288分, num的值的范围为 0~287
         */
        private Integer num;

        /**
         * 计划内容Map
         */
        private Map<String, Object> planContentMap;

    }
}
