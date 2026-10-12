package com.persagy.apccontrol.basic.outside.feign.model.response.calendar;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 工作历规则响应
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MidCalendarRuleResponse extends BaseResponse {

    /**
     * 规则ID
     */
    private String id;

    /**
     * 工作历ID
     */
    private String workId;

    /**
     * 规则类型
     *
     * <ul>
     *     <li><b>1</b> - 全局</li>
     *     <li><b>2</b> - 时间段</li>
     *     <li><b>3</b> - 时间</li>
     * </ul>
     */
    private Integer calLevel;

    /**
     * 规则类型描述
     */
    private String calLevelName;

    /**
     * 标签编码
     */
    private String tagType;

    /**
     * 标签值
     */
    private String tagValue;

    /**
     * 循环周期, 全局时有效
     *
     * <ul>
     *     <li><b>1</b> - 年</li>
     *     <li><b>2</b> - 月</li>
     *     <li><b>3</b> - 周</li>
     * </ul>
     */
    private Integer periodType;

    /**
     * 循环周期描述, 全局时有效
     */
    private String periodTypeName;

    /**
     * 循环周期内容
     *
     * <ul>
     *     <li><b>年</b> - {@code ['11-15, 12-31']}</li>
     *     <li><b>月</b> - {@code ['1','2','3','4','999']}</li>
     *     <li><b>周</b> - {@code ['0','1','2','3','4','5','6']}</li>
     * </ul>
     */
    private List<String> periodContents;

    /**
     * 计划内容
     */
    private List<ScheduleContent> scheduleContents;

    /**
     * 开始日期
     */
    private String beginDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 计划内容
     *
     * @author JiYinchuan
     * @since v5.7.0
     */
    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public static class ScheduleContent extends BaseResponse {

        /**
         * 开闭店时间
         */
        public static final String TIME_TYPE_OPENING = "Opening";

        /**
         * 时间类型
         */
        private String timeType;

        /**
         * 时间内容
         *
         * <ul>
         *     <li><b>Opening</b> - {@code ['08:00,09:00','15:00,17:00']}</li>
         * </ul>
         */
        private List<TimeContent> timeContents;

        /**
         * 时间内容
         *
         * @author JiYinchuan
         * @since v5.7.0
         */
        @Data
        @ToString(callSuper = true)
        @EqualsAndHashCode(callSuper = true)
        public static class TimeContent extends BaseResponse {

            /**
             * 类型
             */
            private String type;

            /**
             * 时间内容
             */
            private List<String> timeContent;

            /**
             * 时间段时间计划值
             */
            private String periodDate;
        }
    }
}
