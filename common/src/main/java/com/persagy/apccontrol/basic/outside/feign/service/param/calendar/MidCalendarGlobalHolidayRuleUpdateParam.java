package com.persagy.apccontrol.basic.outside.feign.service.param.calendar;

import com.persagy.validate.utility.pattern.DatePattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Range;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Collection;
import java.util.LinkedHashSet;

import static com.persagy.apccontrol.basic.utils.range.RangeUtils.DEFAULT_RANGE_SPLIT_REGEX;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/4 14:34
　　 * @description: 节假日全局规则更新
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MidCalendarGlobalHolidayRuleUpdateParam {

    /**
     * 工作历ID
     */
    @NotBlank(message = "{key_e5b5d8e7d36c45abb215bf0f0aa77554}")
    private String calendarId;

    /**
     * 通用配置
     */
    private Collection<Range<MonthDay>> globalDate;

    /**
     * 特殊配置
     */
    private Collection<Range<LocalDate>> periodDate;


}
