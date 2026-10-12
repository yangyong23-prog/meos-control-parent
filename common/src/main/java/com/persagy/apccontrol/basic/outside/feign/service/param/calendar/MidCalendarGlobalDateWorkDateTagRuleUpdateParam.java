package com.persagy.apccontrol.basic.outside.feign.service.param.calendar;

import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarWorkDayTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.util.Collection;

/**
 * 工作里全局时间标签规则更新参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MidCalendarGlobalDateWorkDateTagRuleUpdateParam {

    /**
     * 工作历ID
     */
    @NotBlank(message = "{key_e5b5d8e7d36c45abb215bf0f0aa77554}")
    private String calendarId;

    /**
     * 工休日类型
     */
    @NotNull(message = "{key_efc2310eccfc4401a83af5660622001d}")
    private MidCalendarWorkDayTypeEnum workDayType;

    /**
     * 工休日
     */
    @NotNull(message = "{key_887b52d0fb15400c9c0cd43e12f97b39}")
    private Collection<DayOfWeek> globalDate;

}
