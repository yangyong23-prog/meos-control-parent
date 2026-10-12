package com.persagy.apccontrol.basic.outside.feign.service.param.calendar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.MonthDay;
import java.util.Collection;

/**
 * 工作里全局店庆日规则更新参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MidCalendarGlobalAnniversaryRuleUpdateParam {

    /**
     * 工作历ID
     */
    @NotBlank(message = "{key_e5b5d8e7d36c45abb215bf0f0aa77554}")
    private String calendarId;

    /**
     * 全局日期配置, 前闭后闭
     */
    @NotNull(message = "{key_cd59871596fa4e92a571b843b4c4d125}")
    private Collection<Range<MonthDay>> globalDate;

}
