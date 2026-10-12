package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.structure.runanalysis.strategy.RunAnalysisReportStrategy;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Range;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 天气 DTO
 *
 * @author JiYinchuan
 * @since 7.260102.0
 */
@Data
public class WeatherDTO {

    /**
     * 平均温度
     */
    private BigDecimal avgTemperature;

    /**
     * 最低温度
     */
    private BigDecimal minTemperature;

    /**
     * 最高温度
     */
    private BigDecimal maxTemperature;

    /**
     * 平均湿度
     */
    private BigDecimal avgHumidity;

    /**
     * 最低湿度
     */
    private BigDecimal minHumidity;

    /**
     * 最高湿度
     */
    private BigDecimal maxHumidity;

    /**
     * 天气文本
     */
    private String dayText;

    /**
     * 夜间天气文本
     */
    private String nightText;

    /**
     * 天气详情
     */
    private List<Detail> details;

    /**
     * 气象站每15分钟的原始数据
     */
    private List<WeatherDTO.Detail> originalStationDetails;


    /**
     * 解析温度范围
     *
     * @param weathers 天气数据列表
     * @return 温度范围
     * @since 7.260102.0
     */
    @JsonIgnore
    public static @Nullable Range<BigDecimal> parseTemperatureRange(List<WeatherDTO.Detail> weathers) {
        return parseRange(weathers, WeatherDTO.Detail::getTemperature);
    }

    /**
     * 解析湿度范围
     *
     * @param weathers 天气数据列表
     * @return 湿度范围
     * @since 7.260102.0
     */
    @JsonIgnore
    public static @Nullable Range<BigDecimal> parseHumidityRange(List<WeatherDTO.Detail> weathers) {
        return parseRange(weathers, WeatherDTO.Detail::getHumidity);
    }

    /**
     * 解析温度平均值
     *
     * @param weathers 天气数据列表
     * @return 温度平均值
     * @since 7.260201.0
     */
    @JsonIgnore
    public static @Nullable BigDecimal parseTemperatureAverage(List<WeatherDTO.Detail> weathers) {
        return parseAverage(weathers, WeatherDTO.Detail::getTemperature);
    }

    /**
     * 解析湿度平均值
     *
     * @param weathers 天气数据列表
     * @return 湿度平均值
     * @since 7.260201.0
     */
    @JsonIgnore
    public static @Nullable BigDecimal parseHumidityAverage(List<WeatherDTO.Detail> weathers) {
        return parseAverage(weathers, WeatherDTO.Detail::getHumidity);
    }

    /**
     * 解析湿球温度平均值
     *
     * @param weathers 天气数据列表
     * @return 湿球温度平均值
     * @since 7.260302.0
     */
    @JsonIgnore
    public static @Nullable BigDecimal parseWetBulbTempAverage(List<WeatherDTO.Detail> weathers) {
        return parseAverage(weathers, WeatherDTO.Detail::getWetBulbTemp);
    }

    /**
     * 解析焓值平均值
     *
     * @param weathers 天气数据列表
     * @return 焓值平均值
     * @since 7.260302.0
     */
    @JsonIgnore
    public static @Nullable BigDecimal parseEnthalpyValueAverage(List<WeatherDTO.Detail> weathers) {
        return parseAverage(weathers, WeatherDTO.Detail::getEnthalpyValue);
    }


    /**
     * 解析范围通用方法
     *
     * @param weathers       天气数据列表
     * @param valueExtractor 值提取器
     * @return 范围
     */
    private static @Nullable Range<BigDecimal> parseRange(
            List<WeatherDTO.Detail> weathers,
            Function<WeatherDTO.Detail, BigDecimal> valueExtractor) {
        if (CollectionUtils.isEmpty(weathers)) {
            return null;
        }

        List<BigDecimal> values = weathers.stream()
                .map(valueExtractor)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        if (values.isEmpty()) {
            return null;
        }

        return Range.between(values.get(0), values.get(values.size() - 1));
    }

    /**
     * 解析平均值通用方法
     *
     * @param weathers       天气数据列表
     * @param valueExtractor 值提取器
     * @return 平均值
     */
    public static @Nullable BigDecimal parseAverage(
            List<WeatherDTO.Detail> weathers,
            Function<WeatherDTO.Detail, BigDecimal> valueExtractor) {
        if (CollectionUtils.isEmpty(weathers)) {
            return null;
        }

        List<BigDecimal> values = weathers.stream()
                .map(valueExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (values.isEmpty()) {
            return null;
        }

        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(values.size()), RunAnalysisReportStrategy.NEW_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 天气详情
     */
    @Data
    public static class Detail {

        /**
         * 时间
         */
        private LocalDateTime time;

        /**
         * 温度
         */
        private BigDecimal temperature;

        /**
         * 湿度
         */
        private BigDecimal humidity;

        /**
         * 湿球温度
         */
        private BigDecimal wetBulbTemp;

        /**
         * 焓值
         */
        private BigDecimal enthalpyValue;

        /**
         * 天气文本，仅天气服务存在
         */
        private String text;

    }
}
