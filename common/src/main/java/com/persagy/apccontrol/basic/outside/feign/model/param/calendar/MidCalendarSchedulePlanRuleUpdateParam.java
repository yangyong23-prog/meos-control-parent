package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarScheduleTagTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * 工作历日期标签规则参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@Accessors(chain = true)
public class MidCalendarSchedulePlanRuleUpdateParam {

    /**
     * 类型
     *
     * <ul>
     *     <li><b>1</b> - 全局</li>
     * </ul>
     */
    @NotNull(message = "{key_fbbee5395df4412c9f9e5e4e8fadfcf1}")
    private Integer type;

    /**
     * 工作历ID
     */
    @NotBlank(message = "{key_e5b5d8e7d36c45abb215bf0f0aa77554}")
    private String workId;

    /**
     * 是否异步实例化每天规则
     */
    private Boolean asyncInit;

    /**
     * 内容
     */
    @NotEmpty(message = "{key_0fd2cc2b934e4cac8b3bf29418fb7d17}")
    @Valid
    private Collection<Content> contents;


    /**
     * 内容
     *
     * @author JiYinchuan
     * @since v5.7.0
     */
    @Data
    public static class Content {

        /**
         * 日期标签
         */
        @NotNull(message = "{key_e88a65bbbe4947548128334b97708a35}")
        private MidCalendarDateTagTypeEnum dateTagType;

        /**
         * 日期标签值
         */
        private MidCalendarDateTagValueEnum dateTagValue;


        /**
         * 时间计划标签
         */
        @NotNull(message = "{key_fac63e89c0424371905a94c307b00420}")
        private MidCalendarScheduleTagTypeEnum scheduleTagType;

        /**
         * 时间计划内容
         */
        private Collection<String> scheduleContent;

        /**
         * 时间段日期标签时间范围值
         *
         * <p>例如: [2022-10-20,2022-12-31]
         */
        private String periodDate;

        /**
         * 指定日期的日期标签时间点
         *
         * <p>例如: 2022-10-20
         */
        private String date;

    }
}
