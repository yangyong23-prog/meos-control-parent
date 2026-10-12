package com.persagy.apccontrol.basic.model.dto.demand.airconditioning;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.ControlFieldParser;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.DemandConfigDataDTO;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.pattern.DatePattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Range;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;

/**
 * 需求配置-空调需求-配置数据
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@FieldNameConstants
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求配置-空调需求-配置数据")
public class DemandConfigAirconditioningDataDTO extends DemandConfigDataDTO implements BaseCheckParamProvider, ControlFieldParser {

    public static final List<String> SETTING_FIELD_NAMES = Lists.newArrayList(
            Fields.businessTimeRangeList,
            Fields.airConditioningDemandTimeRangeList,
            Fields.isParticipateInScheduling,
            Fields.isAllowEarlyShutdown,
            Fields.coolingSeasonTargetValue,
            Fields.coolingSeasonUpperLimitValue,
            Fields.coolingSeasonColdStorageValue,
            Fields.heatingSeasonTargetValue,
            Fields.heatingSeasonLowerLimitValue,
            Fields.heatingSeasonHeatStorageValue,
            Fields.temperatureScheduledHourInDay,
            Fields.temperatureContinuousScheduledHour,
            Fields.temperatureUnscheduledTimeRange,
            Fields.carbonDioxideTargetValue,
            Fields.carbonDioxideUpperLimitValue,
            Fields.carbonDioxideScheduledHourInDay,
            Fields.carbonDioxideContinuousScheduledHour,
            Fields.carbonDioxideUnscheduledTimeRange
    );

    // ------------------------- 基本信息 -------------------------

    @Schema(description = "营业时间, 前闭后闭")
    private LinkedHashSet<Range<LocalTime>> businessTimeRangeList;

    @Schema(description = "空调需求时间, 前闭后闭")
    private LinkedHashSet<Range<LocalTime>> airConditioningDemandTimeRangeList;

    @Schema(description = "是否参与调度")
    private Boolean isParticipateInScheduling;

    @Schema(description = "是否允许提前关机")
    private Boolean isAllowEarlyShutdown;

    // ------------------------- 温度标准 -------------------------

    @Schema(description = "供冷季目标值")
    private BigDecimal coolingSeasonTargetValue;

    @Schema(description = "供冷季上限值")
    private BigDecimal coolingSeasonUpperLimitValue;

    @Schema(description = "供冷季蓄冷值")
    private BigDecimal coolingSeasonColdStorageValue;

    @Schema(description = "采暖季目标值")
    private BigDecimal heatingSeasonTargetValue;

    @Schema(description = "采暖季下限值")
    private BigDecimal heatingSeasonLowerLimitValue;

    @Schema(description = "采暖季蓄热值")
    private BigDecimal heatingSeasonHeatStorageValue;

    @Schema(description = "温度日调度时长")
    private BigDecimal temperatureScheduledHourInDay;

    @Schema(description = "温度可连续调度时长")
    private BigDecimal temperatureContinuousScheduledHour;

    @Schema(description = "温度不可调度时段, 前闭后闭")
    private LinkedHashSet<Range<LocalTime>> temperatureUnscheduledTimeRange;

    // ------------------------- Co₂标准 -------------------------

    @Schema(description = "二氧化碳目标值")
    private BigDecimal carbonDioxideTargetValue;

    @Schema(description = "二氧化碳上限值")
    private BigDecimal carbonDioxideUpperLimitValue;

    @Schema(description = "二氧化碳日调度时长")
    private BigDecimal carbonDioxideScheduledHourInDay;

    @Schema(description = "二氧化碳可连续调度时长")
    private BigDecimal carbonDioxideContinuousScheduledHour;

    @Schema(description = "二氧化碳不可调度时段, 前闭后闭")
    private LinkedHashSet<Range<LocalTime>> carbonDioxideUnscheduledTimeRange;

    // ------------------------- 拓展字段 -------------------------

    @Schema(description = "价值选择类型")
    private BasicsConfigTypeEnum valueSelectType;
    /**
     * 校验设置值
     *
     * @since v6.0.0
     */
    @Override
    public void checkAndSet() {
        if (RangeUtils.isOverlapped(businessTimeRangeList)) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_1c3537e54a2c49879a136529a5a21844"));
        }
        if (RangeUtils.isOverlapped(airConditioningDemandTimeRangeList)) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_9f37b60a7c894806b22c09ab717b32b6"));
        }
        if (RangeUtils.isOverlapped(temperatureUnscheduledTimeRange)) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_767f1a4199474530b3e3bad5c180be30"));
        }
        if (RangeUtils.isOverlapped(carbonDioxideUnscheduledTimeRange)) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_bcca631d388b49ba9f1a7757fe01357d"));
        }
        fillIfNotParticipateInScheduling();
    }

    /**
     * 当不参与调度时重置相关调度字段值
     *
     * @since v6.0.0
     */
    @SneakyThrows
    public void fillIfNotParticipateInScheduling() {
        if (BooleanUtils.isFalse(isParticipateInScheduling)) {
            this.temperatureScheduledHourInDay = null;
            this.temperatureContinuousScheduledHour = null;
            this.temperatureUnscheduledTimeRange = null;
            this.carbonDioxideScheduledHourInDay = null;
            this.carbonDioxideContinuousScheduledHour = null;
            this.carbonDioxideUnscheduledTimeRange = null;
        }
    }

    // ------------------------- 序列化/反序列化 -------------------------

    @JsonGetter(Fields.businessTimeRangeList)
    public LinkedHashSet<String> serializeBusinessTimeRangeListOnWeekDays() {
        return RangeUtils.toString(this.businessTimeRangeList,
                item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(Fields.businessTimeRangeList)
    public void deserializeBusinessTimeRangeListOnWeekdays(LinkedHashSet<String> businessTimeRangeList) {
        this.businessTimeRangeList = RangeUtils.instanceByArray(businessTimeRangeList,
                item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

    @JsonGetter(Fields.airConditioningDemandTimeRangeList)
    public LinkedHashSet<String> serializeAirConditioningDemandTimeRangeList() {
        return RangeUtils.toString(this.airConditioningDemandTimeRangeList,
                item -> (Objects.nonNull(item) ? item : LocalTime.MAX).format(DatePattern.NORM_TIME_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonSetter(Fields.airConditioningDemandTimeRangeList)
    public void deserializeAirConditioningDemandTimeRangeList(LinkedHashSet<String> airConditioningDemandTimeRangeList) {
        this.airConditioningDemandTimeRangeList = RangeUtils.instanceByArray(airConditioningDemandTimeRangeList,
                item -> Objects.nonNull(item)
                        ? LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER)
                        : LocalTime.MAX,
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.temperatureUnscheduledTimeRange)
    public LinkedHashSet<String> serializeTemperatureUnscheduledTimeRange() {
        return RangeUtils.toString(this.temperatureUnscheduledTimeRange,
                item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(Fields.temperatureUnscheduledTimeRange)
    public void deserializeTemperatureUnscheduledTimeRange(LinkedHashSet<String> temperatureUnscheduledTimeRange) {
        this.temperatureUnscheduledTimeRange = RangeUtils.instanceByArray(temperatureUnscheduledTimeRange,
                item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

    @JsonGetter(Fields.carbonDioxideUnscheduledTimeRange)
    public LinkedHashSet<String> serializeCarbonDioxideUnscheduledTimeRange() {
        return RangeUtils.toString(this.carbonDioxideUnscheduledTimeRange,
                item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(Fields.carbonDioxideUnscheduledTimeRange)
    public void deserializeCarbonDioxideUnscheduledTimeRange(LinkedHashSet<String> carbonDioxideUnscheduledTimeRange) {
        this.carbonDioxideUnscheduledTimeRange = RangeUtils.instanceByArray(carbonDioxideUnscheduledTimeRange,
                item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

}
