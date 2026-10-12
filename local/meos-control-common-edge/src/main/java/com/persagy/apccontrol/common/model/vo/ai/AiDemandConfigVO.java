package com.persagy.apccontrol.common.model.vo.ai;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningCO2QualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningEnvironmentQualityObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbQualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求配置VO
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@Data
public class AiDemandConfigVO {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM.dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static String NOT_CONTIG_ENVIRONMENT_QUALITY = LocaleMessageUtil.getMessage("key_3ab77e96337c45a593f9305fdac6c73b");
    private static String NOT_CONTIG_SPACE_SCHEDULING = LocaleMessageUtil.getMessage("key_9d5ba263637144e0a1bdb4c422d2dad0");

    @Schema(description = "下一15分钟")
    private LocalDateTime next15minute;

    @Schema(description = "环境品质文本")
    private List<String> environmentQualityTexts;

    @Schema(description = "空间调度文本")
    private List<String> spaceSchedulingTexts;

    @Schema(description = "价值选择文本")
    private String valueSelectText;

    @Schema(description = "配置详情")
    private Map<LocalDate, AiDemandConfigDetailVO> details;

    /**
     * 填充文本
     *
     * @param spaces 空间对象
     * @since 7.250702.0
     */
    public void fillTexts(@NonNull List<DtpRwdSpaceDTO> spaces) {
        if (CollectionUtils.isEmpty(spaces) || MapUtils.isEmpty(this.details)) {
            return;
        }
        List<String> environmentQualityTexts = Optional.ofNullable(this.environmentQualityTexts).orElseGet(ArrayList::new);
        List<String> spaceSchedulingTexts = Optional.ofNullable(this.spaceSchedulingTexts).orElseGet(ArrayList::new);
        for (DtpRwdSpaceDTO space : spaces) {
            String spaceId = space.getId();
            String localName = space.getLocalName();
            StringJoiner environmentJoiner = new StringJoiner("、");
            StringJoiner schedulingJoiner = new StringJoiner("、");
            for (Map.Entry<LocalDate, AiDemandConfigDetailVO> entry : this.details.entrySet()) {
                LocalDate date = entry.getKey();
                AiDemandConfigDetailVO value = entry.getValue();
                Map<String, DemandConfigAirconditioningEnvironmentQualityObject> spaceIdAndConfig = value.getSpaceIdAndConfig();

                String environmentQualityText = NOT_CONTIG_ENVIRONMENT_QUALITY;
                String spaceSchedulingText = NOT_CONTIG_SPACE_SCHEDULING;
                if (MapUtils.isNotEmpty(spaceIdAndConfig)) {
                    DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = spaceIdAndConfig.get(spaceId);
                    List<MidCalendarDateTagValueEnum> dateTags = value.getDateTags();
                    if (Objects.nonNull(environmentQuality)) {
                        environmentQualityText = StringUtils.defaultIfBlank(parseEnvironmentQualityText(
                                dateTags, environmentQuality.getTdbQuality()), NOT_CONTIG_ENVIRONMENT_QUALITY);
                        spaceSchedulingText = StringUtils.defaultIfBlank(parseSpaceSchedulingText(
                                date, environmentQuality), NOT_CONTIG_SPACE_SCHEDULING);
                    }
                }
                String dateString = parseDateTime(date);
                environmentJoiner.add(dateString + environmentQualityText);
                schedulingJoiner.add(dateString + spaceSchedulingText);
            }
            if (environmentJoiner.length() > 0) {
                environmentQualityTexts.add(localName + "：" + environmentJoiner);
            }
            if (schedulingJoiner.length() > 0) {
                spaceSchedulingTexts.add(localName + "：" + schedulingJoiner);
            }
        }
        this.environmentQualityTexts = environmentQualityTexts;
        this.spaceSchedulingTexts = spaceSchedulingTexts;
    }

    /**
     * 填充价值选择文本
     *
     * @since 7.250702.0
     */
    public void fillValueSelectText() {
        if (MapUtils.isEmpty(this.details)) {
            return;
        }
        StringJoiner valueSelectJoiner = new StringJoiner("、");
        for (Map.Entry<LocalDate, AiDemandConfigDetailVO> entry : this.details.entrySet()) {
            LocalDate date = entry.getKey();
            AiDemandConfigDetailVO value = entry.getValue();
            DemandConfigAirconditioningCustomDTO valueSelect = value.getValueSelect();

            String dateString = parseDateTime(date);
            String valueSelectText = StringUtils.defaultIfBlank(
                    parseValueSelectText(valueSelect), LocaleMessageUtil.getMessage("key_8ea4a40d8ff644c4922099da76527579"));
            valueSelectJoiner.add(dateString + valueSelectText);
        }
        this.valueSelectText = valueSelectJoiner.toString();
    }

    /**
     * 解析日期时间
     *
     * @param date 日期
     * @return 日期时间
     * @since 7.250702.0
     */
    private String parseDateTime(@NonNull LocalDate date) {
        LocalDate timeDate = this.next15minute.toLocalDate();
        String beginTimeString = TIME_FORMAT.format(this.next15minute);
        String endTimeString;
        if (date.isEqual(timeDate)) {
            endTimeString = "24:00";
        } else {
            endTimeString = beginTimeString;
            beginTimeString = TIME_FORMAT.format(LocalTime.MIN);
        }
        String dateString = DATE_FORMAT.format(date);
        return dateString + " " + beginTimeString
                + "~"
                + dateString + " " + endTimeString;
    }

    /**
     * 解析价值选择文本
     *
     * @param valueSelect 价值选择配置
     * @return 价值选择文本
     * @since 7.250702.0
     */
    private @Nullable String parseValueSelectText(@Nullable DemandConfigAirconditioningCustomDTO valueSelect) {
        if (Objects.isNull(valueSelect)) {
            return null;
        }
        BasicsConfigTypeEnum valueSelectType = valueSelect.getValueSelectType();
        List<ValueSelectDTO> valueSelectConfig = valueSelect.getValueSelectConfig();
        if (Objects.isNull(valueSelectType)) {
            return null;
        }
        Map<BasicsConfigTypeEnum, List<ValueSelectConfigDto>> valueSelectAndConfig = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(valueSelectConfig)) {
            valueSelectAndConfig = valueSelectConfig.stream().collect(Collectors.toMap(
                    ValueSelectDTO::getValueSelectType, ValueSelectDTO::getValueSelectRange));
        }
        List<ValueSelectConfigDto> configs = valueSelectAndConfig.get(valueSelectType);
        switch (valueSelectType) {
            case VALUE_SELECT_QUALITY_FIRST:
                return LocaleMessageUtil.getMessage("key_f37e4b313e4f4b698380f7f87b04ed56");
            case VALUE_SELECT_LOWEST_CARBON_EMISSIONS:
            case VALUE_SELECT_LOWEST_CAST:
                StringBuilder intervalBuilder = new StringBuilder();
                intervalBuilder.append(valueSelectType == BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CARBON_EMISSIONS
                        ? LocaleMessageUtil.getMessage("key_3b2847eabd07485989125de7f39d7c7d") : LocaleMessageUtil.getMessage("key_0806c1f458ad431f8757869fef33c38f"));
                if (CollectionUtils.isNotEmpty(configs)) {
                    intervalBuilder.append("，");
                    Iterator<ValueSelectConfigDto> iterator = configs.iterator();
                    while (iterator.hasNext()) {
                        ValueSelectConfigDto config = iterator.next();
                        BigDecimal intervalMin = config.getIntervalMin();
                        BigDecimal intervalMax = config.getIntervalMax();
                        BigDecimal tempFillRate = config.getTempFillRate();
                        BigDecimal co2FillRate = config.getCo2FillRate();
                        intervalBuilder.append(LocaleMessageUtil.getMessage("key_519c7010443240a9a14c732a38258f38")).append(intervalMin.toPlainString())
                                .append("<=")
                                .append(valueSelectType == BasicsConfigTypeEnum
                                        .VALUE_SELECT_LOWEST_CARBON_EMISSIONS ? LocaleMessageUtil.getMessage("key_8aff2df472084977acf45633ae254330") : LocaleMessageUtil.getMessage("key_585357157efb4d12aa94dbd2126061aa"))
                                .append("<=")
                                .append(intervalMax.toPlainString()).append(valueSelectType == BasicsConfigTypeEnum
                                        .VALUE_SELECT_LOWEST_CARBON_EMISSIONS ? "kgCO₂/kWh" : LocaleMessageUtil.getMessage("key_1b9799c7f08d477f8f8c1492daf227d8"))
                                .append(LocaleMessageUtil.getMessage("key_ecff7ab8ba3d40c9890a5a9272414333"))
                                .append(LocaleMessageUtil.getMessage("key_c7f94669e0c94d1497a505e8c288a703")).append(tempFillRate.toPlainString()).append("%").append("，")
                                .append(LocaleMessageUtil.getMessage("key_3f6c2765c6504148a93a642c1d247484")).append(co2FillRate.toPlainString()).append("%");
                        if (iterator.hasNext()) {
                            intervalBuilder.append("，");
                        }
                    }
                }
                return intervalBuilder.toString();
            case VALUE_SELECT_LOWEST_ENERGY:
                StringBuilder lowestEnergyBuilder = new StringBuilder();
                lowestEnergyBuilder.append(LocaleMessageUtil.getMessage("key_7574d8e1423c467388d269b965635720"));
                if (CollectionUtils.isNotEmpty(configs)) {
                    lowestEnergyBuilder.append("，");
                    Iterator<ValueSelectConfigDto> iterator = configs.iterator();
                    while (iterator.hasNext()) {
                        ValueSelectConfigDto config = iterator.next();
                        BigDecimal tempFillRate = config.getTempFillRate();
                        BigDecimal co2FillRate = config.getCo2FillRate();
                        lowestEnergyBuilder.append(LocaleMessageUtil.getMessage("key_c7f94669e0c94d1497a505e8c288a703")).append(tempFillRate.toPlainString()).append("%").append("，")
                                .append(LocaleMessageUtil.getMessage("key_3f6c2765c6504148a93a642c1d247484")).append(co2FillRate.toPlainString()).append("%");
                        if (iterator.hasNext()) {
                            lowestEnergyBuilder.append("，");
                        }
                    }
                }
                return lowestEnergyBuilder.toString();
            default:
                return null;
        }
    }

    /**
     * 解析空间调度标准文本
     *
     * <p>空间的运行标准中“是否参与调度=否”：“时间段+‘不可调度’”；如03.01 12:00~03.01 24:00 不可调度</p>
     * <p>空间的运行标准中“是否参与调度=是”：“时间段+“环境参数名称”+不可调度时间段为’+不可调度时间段，‘可连续调度时长为’+时长）”；
     * 比如03.02 00:00~03.02 12:00 温度不可调度时间段为03:00~05:00,07:00~08:00，可连续调度时长为2h、CO2不可调度时间段为03:00~05:00,07:00~08:00，可连续调度时长为2h。如果没有不可调度时间段则展示“全天可调度”
     * </p>
     *
     * @param environmentQuality 环境品质配置
     * @return 空间调度标准文本
     * @since 7.250702.0
     */
    private @Nullable String parseSpaceSchedulingText(
            @NonNull LocalDate date,
            @Nullable DemandConfigAirconditioningEnvironmentQualityObject environmentQuality) {
        if (Objects.isNull(environmentQuality)) {
            return null;
        }
        Boolean isParticipateInScheduling = environmentQuality.getIsParticipateInScheduling();
        DemandConfigAirconditioningTdbQualityValueObject tdbQuality = environmentQuality.getTdbQuality();
        DemandConfigAirconditioningCO2QualityValueObject co2Quality = environmentQuality.getCO2Quality();
        if (Objects.isNull(co2Quality)) {
            return null;
        }
        List<DemandTimeIntervalValueObject> temperatureUnscheduledTimeRange = tdbQuality.getTemperatureUnscheduledTimeRange();
        BigDecimal temperatureContinuousScheduledHour = tdbQuality.getTemperatureContinuousScheduledHour();
        List<DemandTimeIntervalValueObject> carbonDioxideUnscheduledTimeRange = co2Quality.getCarbonDioxideUnscheduledTimeRange();
        BigDecimal carbonDioxideContinuousScheduledHour = co2Quality.getCarbonDioxideContinuousScheduledHour();
        StringBuilder spaceSchedulingTextBuilder = new StringBuilder();
        if (BooleanUtils.isTrue(isParticipateInScheduling)) {
            spaceSchedulingTextBuilder.append(LocaleMessageUtil.getMessage("key_7274b502eecc4af1ac679f90941dfcbe"));
            spaceSchedulingTextBuilder.append(parseSchedulingTextContent(date,
                    temperatureUnscheduledTimeRange, temperatureContinuousScheduledHour));
            spaceSchedulingTextBuilder.append("、");
            spaceSchedulingTextBuilder.append("CO2");
            spaceSchedulingTextBuilder.append(parseSchedulingTextContent(date,
                    carbonDioxideUnscheduledTimeRange, carbonDioxideContinuousScheduledHour));
            return spaceSchedulingTextBuilder.toString();
        }
        spaceSchedulingTextBuilder.append(LocaleMessageUtil.getMessage("key_0f6d2bf0d87c472ba2f8b117748d347c"));
        return spaceSchedulingTextBuilder.toString();
    }

    /**
     * 解析调度标准文本内容
     *
     * @param date                          日期
     * @param unscheduledTimeRangeIntervals 不可调度时间段
     * @param continuousScheduledHour       可连续调度时长
     * @return 调度标准文本内容
     * @since 7.250702.0
     */
    private StringBuilder parseSchedulingTextContent(
            @NonNull LocalDate date,
            @Nullable List<DemandTimeIntervalValueObject> unscheduledTimeRangeIntervals,
            @Nullable BigDecimal continuousScheduledHour) {
        StringBuilder builder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(unscheduledTimeRangeIntervals)) {
            builder.append(LocaleMessageUtil.getMessage("key_3fb7dce65f334bf0abfcb1e8d0f708e5"));
            for (int i = 0; i < unscheduledTimeRangeIntervals.size(); i++) {
                if (i == 0) {
                    builder.append(DATE_FORMAT.format(date)).append(" ");
                } else {
                    builder.append(",");
                }
                builder.append(parseTimeRange(unscheduledTimeRangeIntervals.get(i)));
            }
        } else {
            builder.append(LocaleMessageUtil.getMessage("key_3269ba0aa36b40fca8fd59826996c4df"));
        }
        if (Objects.nonNull(continuousScheduledHour)) {
            builder.append(LocaleMessageUtil.getMessage("key_810d98d944c54e7783de7fdec527d4f3")).append(continuousScheduledHour.toPlainString()).append("h");
        }
        return builder;
    }

    /**
     * 解析时间区间
     *
     * @param timeInterval 时间区间
     * @return 时间区间文本
     * @since 7.250702.0
     */
    private @Nullable String parseTimeRange(@Nullable DemandTimeIntervalValueObject timeInterval) {
        if (Objects.isNull(timeInterval)) {
            return null;
        }
        DemandTimeValueObject beginTime = timeInterval.getBeginTime();
        DemandTimeValueObject endTime = timeInterval.getEndTime();
        return TIME_FORMAT.format(beginTime.analyzeAbsoluteTime())
                + "~"
                + TIME_FORMAT.format(endTime.analyzeAbsoluteTime());
    }

    /**
     * 解析环境品质标准文本
     *
     * <p>比如“走廊：03.01 12:00~03.01 24:00（舒适区间为22℃~24℃，过热门限为28℃，过冷门限为20℃）、03.02 00:00~03.02 12:00 （舒适区间为22℃~24℃，过热门限为28℃，过冷门限为20℃）</p>
     *
     * @param dateTags   日期标签
     * @param tdbQuality 温度标准配置
     * @return 环境品质标准文本
     * @since 7.250702.0
     */
    private @Nullable String parseEnvironmentQualityText(
            @Nullable List<MidCalendarDateTagValueEnum> dateTags,
            @Nullable DemandConfigAirconditioningTdbQualityValueObject tdbQuality) {
        if (CollectionUtils.isEmpty(dateTags) || Objects.isNull(tdbQuality)) {
            return null;
        }
        List<DemandConfigAirconditioningTdbSeasonValueObject> seasonQualities = tdbQuality.getSeasonQuality();
        if (CollectionUtils.isEmpty(seasonQualities)) {
            return null;
        }
        Optional<DemandConfigAirconditioningTdbSeasonValueObject> seasonQualityOpt = seasonQualities.stream()
                .filter(item -> dateTags.stream().anyMatch(dateTag ->
                        StringUtils.equals(dateTag.getValue(), item.getAirSeason().getValue()))).findFirst();
        if (!seasonQualityOpt.isPresent()) {
            return null;
        }
        DemandConfigAirconditioningTdbSeasonValueObject seasonQuality = seasonQualityOpt.get();
        StringBuilder environmentQualityTextBuilder = new StringBuilder();
        if (ObjectUtils.allNotNull(seasonQuality.getLowerComfortSeasonLimitValue(),
                seasonQuality.getUpperComfortSeasonLimitValue())) {
            environmentQualityTextBuilder
                    .append(LocaleMessageUtil.getMessage("key_320ff4c54f72456cb4b9809d907c7ac5"))
                    .append(seasonQuality.getLowerComfortSeasonLimitValue().toPlainString()).append("℃")
                    .append("~")
                    .append(seasonQuality.getUpperComfortSeasonLimitValue().toPlainString()).append("℃");
        }
        if (Objects.nonNull(seasonQuality.getOverHeatingSeasonLimitValue())) {
            environmentQualityTextBuilder
                    .append("，")
                    .append(LocaleMessageUtil.getMessage("key_6e398239ef8f49b5b123041ce532ca1e"))
                    .append(seasonQuality.getOverHeatingSeasonLimitValue().toPlainString()).append("℃");
        }
        if (Objects.nonNull(seasonQuality.getUnderCoolingSeasonLimitValue())) {
            environmentQualityTextBuilder
                    .append("，")
                    .append(LocaleMessageUtil.getMessage("key_4cef6f901e7e4db2860d3cdf8ac8170a"))
                    .append(seasonQuality.getUnderCoolingSeasonLimitValue().toPlainString()).append("℃");
        }
        return environmentQualityTextBuilder.toString();
    }
}
