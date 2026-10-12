package com.persagy.apccontrol.basic.config.properties.saas.children.demand;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.properties.BaseProperties;
import com.persagy.apccontrol.basic.config.properties.saas.children.DemandProperties;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.validate.utility.pattern.DatePattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.Range;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.List;

/**
 * 需求管理基础配置文件
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = DemandBasicProperties.KEY)
@RefreshScope
public class DemandBasicProperties extends BaseProperties {

    public static final String KEY = DemandProperties.KEY + ".basic";

    /**
     * 供冷季日期, 前闭后闭
     */
    private List<Range<MonthDay>> coolingSeasonDateRangeList = Lists.newArrayList(
            Range.between(MonthDay.of(5, 1), MonthDay.of(10, 31)));

    /**
     * 采暖季开日期, 前闭后闭
     */
    private List<Range<MonthDay>> heatingSeasonDateRangeList = Lists.newArrayList(
            Range.between(MonthDay.of(11, 1), MonthDay.of(12, 31)),
            Range.between(MonthDay.of(1, 1), MonthDay.of(4, 30)));

    /**
     * 工作日
     */
    private List<DayOfWeek> weekdays = Lists.newArrayList(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    /**
     * 营业时间, 前闭后闭
     */
    private List<Range<LocalTime>> businessTimeRangeList = Lists.newArrayList(
            Range.between(LocalTime.of(10, 0), LocalTime.of(22, 0)));

    /**
     * 设置供冷季日期
     *
     * @param coolingSeasonDateRangeList 供冷季日期, 前闭后闭
     * @since v5.7.0
     */
    public void setCoolingSeasonDateRangeList(List<String> coolingSeasonDateRangeList) {
        this.coolingSeasonDateRangeList = formatTimeRange(coolingSeasonDateRangeList,
                time -> MonthDay.parse(time, DatePattern.NORM_MONTHDAY_FORMATTER));
        if (RangeUtils.isOverlapped(this.coolingSeasonDateRangeList, this.heatingSeasonDateRangeList)) {
            throw new RuntimeException(LocaleMessageUtil.getMessage("key_a7dd7819986d4081b07f3a002326aa67"));
        }
    }

    /**
     * 设置采暖季开日期
     *
     * @param heatingSeasonDateRangeList 采暖季开日期, 前闭后闭
     * @since v5.7.0
     */
    public void setHeatingSeasonDateRangeList(List<String> heatingSeasonDateRangeList) {
        this.heatingSeasonDateRangeList = formatTimeRange(heatingSeasonDateRangeList,
                time -> MonthDay.parse(time, DatePattern.NORM_MONTHDAY_FORMATTER));
        if (RangeUtils.isOverlapped(this.heatingSeasonDateRangeList, this.coolingSeasonDateRangeList)) {
            throw new RuntimeException(LocaleMessageUtil.getMessage("key_7f5f913ef2c64cc5854ed340f6f0ca39"));
        }
    }

    /**
     * 设置工作日营业时间
     *
     * @param businessTimeRangeList 工作日营业时间, 前闭后闭
     * @since v5.7.0
     */
    public void setBusinessTimeRangeList(List<String> businessTimeRangeList) {
        this.businessTimeRangeList = formatTimeRange(businessTimeRangeList,
                time -> LocalTime.parse(time, DatePattern.NORM_TIME_FORMATTER));
    }
}
