package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 工作历日期计划查询参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
public class MidCalendarDayPlanQueryParam {

    /**
     * 对象ID
     */
    @NotBlank(message = "{key_db46294d82c44d7aa47bd996f27c6154}")
    private String objId;

    /**
     * 开始时间, 闭区间
     */
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    private LocalDate beginDate;

    /**
     * 结束时间, 闭区间
     */
    @NotNull(message = "{key_3313241b85ef48d9a3d2c43cd5e7930d}")
    private LocalDate endDate;

}
