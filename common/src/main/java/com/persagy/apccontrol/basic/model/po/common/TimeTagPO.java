package com.persagy.apccontrol.basic.model.po.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.Range;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class TimeTagPO {


    /**
     * 时间段
     */
    private Range<LocalTime> time;
    /**
     * 标签值
     */
    private String tag;

    @JsonSetter(Fields.time)
    public void deserializeTime(
            String businessTime) {
        this.time = RangeUtils.instance(businessTime,
                item -> LocalTime.parse(item, MidCalendarClientService.OPENING_TIME_FORMATTER));
    }

    @JsonGetter(Fields.time)
    public String serializeTime() {
        return RangeUtils.toString(this.time,
                item -> item.format(MidCalendarClientService.OPENING_TIME_FORMATTER));
    }

    public String getTagValue() {
        if (tag == null) {
            return null;
        }
        String[] parts = tag.split(":");
        return parts.length > 1 ? parts[1] : "";
    }

    public String getTagKey() {
        if (tag == null) {
            return null;
        }
        String[] parts = tag.split(":");
        return parts.length > 0 ? parts[0] : "";
    }

}
