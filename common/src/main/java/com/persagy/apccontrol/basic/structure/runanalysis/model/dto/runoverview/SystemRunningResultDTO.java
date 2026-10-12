package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.runoverview;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.validate.utility.pattern.DatePattern;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.Range;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统运行结果 DTO
 *
 * @author JiYinchuan
 * @since 7.260102.0
 */
@Data
@FieldNameConstants
public class SystemRunningResultDTO {

    /**
     * 系统 ID
     */
    private String id;

    /**
     * 系统名称
     */
    private String localName;

    /**
     * 系统运行时间
     */
    private List<Range<ChronoLocalDateTime<?>>> systemRunningTimes;

    /**
     * 系统服务空间达标率
     */
    private BigDecimal serviceSpaceSatisfyPercentage;

    /**
     * 系统累计用电量
     */
    private BigDecimal dayAccElecConsum;

    /**
     * 系统累计用环比
     */
    private BigDecimal accElecConsumMom;

    @JsonGetter(Fields.systemRunningTimes)
    public List<String> serializeSystemRunningTimes() {
        return RangeUtils.toString(this.systemRunningTimes,
                item -> item.format(DatePattern.NORM_DATETIME_FORMATTER), ArrayList::new);
    }

    @JsonSetter(Fields.systemRunningTimes)
    public void deserializeSystemRunningTimes(List<String> systemRunningTimesList) {
        this.systemRunningTimes = RangeUtils.instanceByArray(systemRunningTimesList,
                item -> LocalDateTime.parse(item, DatePattern.NORM_DATETIME_FORMATTER), ArrayList::new);
    }
}
