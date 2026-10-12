package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 查询指定日期的项目下所有工作历营业时间
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MidCalendarOpenTimeProjectQueryParam {

    /**
     * 工作历日期
     */
    private LocalDate workDate;

}
