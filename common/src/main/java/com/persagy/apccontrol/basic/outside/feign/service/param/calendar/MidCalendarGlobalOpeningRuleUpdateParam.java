package com.persagy.apccontrol.basic.outside.feign.service.param.calendar;

import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarScheduleTagTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarWorkDayTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Range;

import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Collection;

/**
 * 工作里全局营业时间规则更新参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MidCalendarGlobalOpeningRuleUpdateParam {

    /**
     * 日期标签类型
     */
    @NotNull(message = "{key_4ed7af7e5f0447f9a76954f09a0d15eb}")
    private MidCalendarDateTagTypeEnum dateTagType;

    private MidCalendarDateTagValueEnum dateTagValueType;

    /**
     * periodDate: 1,2,3,4,5,6,0 分别表示周一到周日  传值样例： "periodDate": "1,2,3"
     */
    private String periodDate;

    /**
     * workDayType
     */
    private MidCalendarWorkDayTypeEnum workDayType;

    /**
     * 时间计划内容
     */
    @NotNull(message = "{key_7e04a433894c4bfcaf5f435f31bcdfe7}")
    private Collection<Range<LocalTime>> scheduleContent;

    @NotNull(message = "{key_fac63e89c0424371905a94c307b00420}")
    private MidCalendarScheduleTagTypeEnum scheduleTagType;

    public MidCalendarGlobalOpeningRuleUpdateParam(MidCalendarDateTagTypeEnum dateTagType, MidCalendarWorkDayTypeEnum workDayType, Collection<Range<LocalTime>> scheduleContent,MidCalendarDateTagValueEnum dateTagValueType,String periodDate) {
        this.dateTagType = dateTagType;
        this.dateTagValueType = dateTagValueType;
        this.workDayType = workDayType;
        this.scheduleContent = scheduleContent;
        this.scheduleTagType = MidCalendarScheduleTagTypeEnum.OPENING;
        this.periodDate = periodDate;
    }
}
