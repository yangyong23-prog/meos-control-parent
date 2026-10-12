package com.persagy.apccontrol.task.param;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.validate.utility.pattern.DatePattern;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.Range;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 空间配置定时写入工作历任务参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@FieldNameConstants
public class WriteCalendarTaskParam {

    /**
     * 集团编码
     */
    private Set<String> groupCodes;

    /**
     * 项目ID
     */
    private LinkedHashSet<String> projectIds;

    /**
     * 营业时间, 前闭后闭
     */
    private Range<ChronoLocalDate> dateRange;

    /**
     * 写入对象ID
     */
    private Set<String> objectIds;

    @JsonGetter(Fields.dateRange)
    public String serializeDateRange() {
        return RangeUtils.toString(dateRange, item -> item.format(DatePattern.NORM_DATE_FORMATTER));
    }

    @JsonSetter(Fields.dateRange)
    public void deserializeDateRange(String dateRange) {
        this.dateRange = RangeUtils.instance(dateRange, item -> LocalDate.parse(item, DatePattern.NORM_DATE_FORMATTER));
    }
}
