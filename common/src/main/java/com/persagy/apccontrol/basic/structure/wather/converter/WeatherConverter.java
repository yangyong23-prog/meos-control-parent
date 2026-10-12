package com.persagy.apccontrol.basic.structure.wather.converter;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.weather.*;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherHour24ForecastResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherNowWeatherResponse;
import com.persagy.validate.utility.pattern.DatePattern;
import org.mapstruct.*;

import java.util.List;
import java.util.TreeSet;

/**
 * 天气服务转换器
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WeatherConverter {

    // ------------------------- To DTO -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240502.0
     */
    WeatherNowWeatherDTO toNowWeatherDto(WeatherNowWeatherResponse source);

    /**
     * 转换成小时历史数据dto
     *
     * @param source 来源
     * @return {@link WeatherHourHistoryDataDTO }
     */
    List<WeatherHourHistoryDataDTO> toHourHistoryDataDto(List<WeatherNowWeatherResponse> source);

    /**
     * 转换成小时历史数据dto
     *
     * @param source 来源
     * @return {@link WeatherDTO }
     */
    WeatherDTO hourlyToHourHistoryDataDto(WeatherHourlyDTO source);

    @InheritConfiguration
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<WeatherDTO> hourlyToHourHistoryDataDto(List<WeatherHourlyDTO> hourlies);

    @InheritConfiguration
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<WeatherDTO> hourlyToHourHistoryDataDto(TreeSet<WeatherHourlyDTO> hourlies);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240502.0
     */
    @Mapping(target = "hourlies", source = "hourly", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    WeatherHour24ForecastDTO toHour24ForecastDto(WeatherHour24ForecastResponse source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240502.0
     */
    WeatherLocationDTO toLocationDto(WeatherHour24ForecastResponse.Location source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240502.0
     */
    @Mapping(target = "time", source = "time", dateFormat = DatePattern.NORM_DATETIME_PATTERN)
    WeatherHourlyDTO toHourlyDto(WeatherHour24ForecastResponse.Hourly source);

    @Mapping(target = "time", source = "hourTime", dateFormat = DatePattern.NORM_DATETIME_PATTERN)
    @Mapping(target = "windDirection", ignore = true)
    @Mapping(target = "windSpeed", ignore = true)
    WeatherDTO toWeatherDTO(WeatherHourHistoryDataDTO source);


    List<WeatherDTO> convertWeatherDTO(List<WeatherHourHistoryDataDTO> hourHistoryDataList);
}
