package com.persagy.apccontrol.basic.model.vo.runanalysis;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.po.common.TimeTagPO;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.Range;

import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@FieldNameConstants
public class SpacesAnalysis {
    @Schema(description = "空间ID")
    private String spaceId;

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "楼层 ID")
    private String floorId;

    @Schema(description = "温度时段标签数据 key是时段 10:00:00,12:00:00,val是标签")
    private List<TimeTagPO> timeTag;

    @Schema(description = "营业时间")
    private LinkedHashSet<Range<LocalTime>> businessTime;

    @Schema(description = "需要过滤信息点规则列表 co2/tdb")
    private Set<String> infoCodeRule;

    @JsonSetter(Fields.businessTime)
    public void deserializeBusinessTime(
            LinkedHashSet<String> businessTime) {
        this.businessTime = RangeUtils.instanceByArray(businessTime,
                item -> LocalTime.parse(item, MidCalendarClientService.OPENING_TIME_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.businessTime)
    public LinkedHashSet<String> serializeBusinessTime() {
        return RangeUtils.toString(this.businessTime,
                item -> item.format(MidCalendarClientService.OPENING_TIME_FORMATTER), LinkedHashSet::new);
    }
}
