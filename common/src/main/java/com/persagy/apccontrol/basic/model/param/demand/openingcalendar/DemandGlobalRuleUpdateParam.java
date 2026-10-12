package com.persagy.apccontrol.basic.model.param.demand.openingcalendar;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleVO;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.pattern.DatePattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.Range;
import org.springframework.util.CollectionUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.LinkedHashSet;

import static com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleVO.localDateRange;
import static com.persagy.apccontrol.basic.utils.range.RangeUtils.DEFAULT_RANGE_SPLIT_REGEX;

/**
 * 需求管理-营业日历全局规则编辑参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@FieldNameConstants
@Schema(title = "需求管理-营业日历全局规则编辑参数")
public class DemandGlobalRuleUpdateParam implements BaseCheckParamProvider {

    @Schema(description = "工作日")
    private LinkedHashSet<DayOfWeek> weekdays;

    //空调工况 开始
    @Schema(description = "供冷季日期, 前闭后闭")
    private LinkedHashSet<Range<MonthDay>> coolingSeasonDateRangeList;

    @Schema(description = "采暖季开日期, 前闭后闭")
    private LinkedHashSet<Range<MonthDay>> heatingSeasonDateRangeList;

    @Schema(description = "不供热不供暖开日期, 前闭后闭")
    private LinkedHashSet<Range<MonthDay>> notCoolingAndHeatingSeasonDateRangeList;

    //空调工况 结束

    @Schema(description = "全局店庆日")
    private LinkedHashSet<Range<MonthDay>> anniversaries;

    @Schema(description = "营业时间请求参数")
    private BusinessTimeRangeParam businessTimeRange = new BusinessTimeRangeParam();

    @Schema(description = "空调季参数")
    private SeasonRangeParam seasonRangeParam = new SeasonRangeParam();


    @Schema(description = "节假日配置-通用配置")
    private LinkedHashSet<Range<MonthDay>> holidayGeneralConfiguration;

    @Schema(description = "节假日配置-特殊配置(注意剔除掉不含【今日及未来日】的日期段)")
    private LinkedHashSet<Range<LocalDate>> holidaySpecialConfiguration;

    @Override
    public void checkAndSet() {
        if (RangeUtils.isOverlapped(coolingSeasonDateRangeList, heatingSeasonDateRangeList)) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_f536ca1593ed4e2b855cfdbdffa23ba5"));
        }
    }

    // -------------------- CoolingSeasonDateRangeList --------------------

    @JsonGetter(DemandGlobalRuleUpdateParam.Fields.coolingSeasonDateRangeList)
    public LinkedHashSet<String> serializeCoolingSeasonDateRangeList() {
        return RangeUtils.toString(this.coolingSeasonDateRangeList,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(DemandGlobalRuleUpdateParam.Fields.coolingSeasonDateRangeList)
    public void deserializeCoolingSeasonDateRangeList(LinkedHashSet<String> coolingSeasonDateRangeList) {
        this.coolingSeasonDateRangeList = RangeUtils.instanceByArray(coolingSeasonDateRangeList,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    // -------------------- HeatingSeasonDateRangeList --------------------

    @JsonGetter(DemandGlobalRuleUpdateParam.Fields.heatingSeasonDateRangeList)
    public LinkedHashSet<String> serializeHeatingSeasonDateRangeList() {
        return RangeUtils.toString(this.heatingSeasonDateRangeList,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(DemandGlobalRuleUpdateParam.Fields.heatingSeasonDateRangeList)
    public void deserializeHeatingSeasonDateRangeList(LinkedHashSet<String> heatingSeasonDateRangeList) {
        this.heatingSeasonDateRangeList = RangeUtils.instanceByArray(heatingSeasonDateRangeList,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    // -------------------- Anniversaries --------------------

    @JsonSetter(DemandGlobalRuleUpdateParam.Fields.anniversaries)
    public void deserializeGlobalAnniversaries(
            LinkedHashSet<String> globalAnniversaries) {
        this.anniversaries = RangeUtils.instanceByArray(globalAnniversaries,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(DemandGlobalRuleUpdateParam.Fields.anniversaries)
    public LinkedHashSet<String> serializeGlobalAnniversaries() {
        return RangeUtils.toString(this.anniversaries,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }


    @JsonSetter(DemandGlobalRuleVO.Fields.notCoolingAndHeatingSeasonDateRangeList)
    public void deserializeNotCoolingAndHeatingSeasonDateRangeList(
            LinkedHashSet<String> globalAnniversaries) {
        this.notCoolingAndHeatingSeasonDateRangeList = RangeUtils.instanceByArray(globalAnniversaries,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(DemandGlobalRuleVO.Fields.notCoolingAndHeatingSeasonDateRangeList)
    public LinkedHashSet<String> serializeNotCoolingAndHeatingSeasonDateRangeList() {
        return RangeUtils.toString(this.notCoolingAndHeatingSeasonDateRangeList,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    // -------------------- 节假日 --------------------
    @JsonGetter(DemandGlobalRuleUpdateParam.Fields.holidayGeneralConfiguration)
    public LinkedHashSet<String> serializeHolidayGeneralConfiguration() {
        return RangeUtils.toString(this.holidayGeneralConfiguration,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(DemandGlobalRuleUpdateParam.Fields.holidayGeneralConfiguration)
    public void deserializeHolidayGeneralConfiguration(LinkedHashSet<String> holidayGeneralConfiguration) {
        this.holidayGeneralConfiguration = RangeUtils.instanceByArray(holidayGeneralConfiguration,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    @JsonGetter(DemandGlobalRuleVO.Fields.holidaySpecialConfiguration)
    public LinkedHashSet<String> serializeHolidaySpecialConfiguration() {
        if(CollectionUtils.isEmpty(this.holidaySpecialConfiguration)){
            return null;
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Range<LocalDate> localDateRange : this.holidaySpecialConfiguration) {
            result.add(String.join(DEFAULT_RANGE_SPLIT_REGEX, localDateRange.getMinimum().format(DatePattern.NORM_DATE_FORMATTER),localDateRange.getMaximum().format(DatePattern.NORM_DATE_FORMATTER)));
        }
        return result;
    }
    @JsonSetter(DemandGlobalRuleVO.Fields.holidaySpecialConfiguration)
    public void deserializeHolidaySpecialConfiguration(LinkedHashSet<String> holidaySpecialConfiguration) {
        if(CollectionUtils.isEmpty(holidaySpecialConfiguration)){
            return ;
        }
        LinkedHashSet<Range<LocalDate>> linkedHashSet = new LinkedHashSet<>();
        for (String source : holidaySpecialConfiguration) {
            String[] split = source.split(DEFAULT_RANGE_SPLIT_REGEX);
            // 1. 先把两端写成 LocalDate 变量，杜绝任何向上转型
            LocalDate start = LocalDate.parse(split[0], DatePattern.NORM_DATE_FORMATTER);
            LocalDate end   = LocalDate.parse(split[1], DatePattern.NORM_DATE_FORMATTER);
            // 2. 显式指定 <LocalDate>，且实参就是 LocalDate 变量
            linkedHashSet.add(localDateRange(start,end));
        }
        this.holidaySpecialConfiguration = linkedHashSet;
    }



}
