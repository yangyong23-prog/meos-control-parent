package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
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
public class MidCalendarDateTagRuleUpdateParam {

    /**
     * 是否异步实例化每天规则
     */
    private Boolean asyncInit;

    /**
     * 请求内容
     *
     * <p>为全局配置只能传到下标为 [0] 的数据中
     */
    @NotEmpty(message = "{key_3dc6e823362c4806ae482c4b093e7543}")
    @Valid
    private Collection<Content> contents;


    /**
     * 请求内容
     *
     * @author JiYinchuan
     * @since v5.7.0
     */
    @Data
    @Accessors(chain = true)
    public static class Content {

        /**
         * 工作历ID
         */
        @NotBlank(message = "{key_e5b5d8e7d36c45abb215bf0f0aa77554}")
        private String workId;

        /**
         * 日期标签
         */
        @NotNull(message = "{key_e88a65bbbe4947548128334b97708a35}")
        private MidCalendarDateTagTypeEnum dateTagType;

        /**
         * 日期标签值
         */
        @NotNull(message = "{key_a4259e618cec47a78e17e5b5d1214bc4}")
        private MidCalendarDateTagValueEnum dateTagValue;

        /**
         * 指定日期的日期标签时间点
         *
         * <p>例如: 2022-10-20
         */
        private String date;

        /**
         * 时间段日期标签时间范围值
         *
         * <p>例如: [2022-10-20,2022-12-31]
         */
        private String periodDate;

        /**
         * 全局日期计划标签时间
         *
         * <p><b>工休日设置</b> - 周一到周五为 ["0","1","2","3","4"], [0-6] 代表星期一到星期日
         * <p><b>供暖冷季设置</b> - ["11-15,03-15"]
         */
        private Collection<String> globalDate;

    }
}
