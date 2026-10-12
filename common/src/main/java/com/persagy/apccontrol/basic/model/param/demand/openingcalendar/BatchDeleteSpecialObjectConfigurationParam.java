package com.persagy.apccontrol.basic.model.param.demand.openingcalendar;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagTypeEnum;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.validate.utility.pattern.DatePattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.Range;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleVO.localDateRange;
import static com.persagy.apccontrol.basic.utils.range.RangeUtils.DEFAULT_RANGE_SPLIT_REGEX;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2025/12/31 15:33
　　 * @description: 批量删除-特殊对象配置
*/
@Data
@FieldNameConstants
@Schema(title = "批量删除-特殊对象配置请求参数")
public class BatchDeleteSpecialObjectConfigurationParam {

    @Schema(description = "实例ID集合")
    private Set<String> instanceIds;

    @Schema(description = "项目id")
    private String projectId;


}
