package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.model.dto.weather.WeatherHour24ForecastDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherHourHistoryDataDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherNowWeatherDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherSunRiseAndSetDTO;
import com.persagy.apccontrol.basic.model.enumtype.weather.WeatherDataTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.weather.WeatherQueryTypeEnum;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 天气客户端服务
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
public interface WeatherClientService extends BaseFeignClientService {

    /**
     * 查询实时天气
     *
     * @param projectId 项目ID
     * @param dataType  数据类型
     * @return 实时天气
     * @since 5.240502.0
     */
    WeatherNowWeatherDTO getNowWeather(@NonNull String projectId, @NonNull WeatherDataTypeEnum dataType);

    /**
     * 查询24小时天气预报
     *
     * @param projectId 项目ID
     * @return 24小时天气预报
     * @since 5.240502.0
     */
    WeatherHour24ForecastDTO getHour24Forecast(@NonNull String projectId);

    List<WeatherHourHistoryDataDTO> listHourHistoryData(@NonNull String projectId, @NonNull LocalDateTime startTime, @NonNull LocalDateTime endTime);

    /**
     * 查询日出日落时间
     *
     * @param date 日期
     * @return 日出日落时间
     * @since 7.260302.0
     */
    WeatherSunRiseAndSetDTO getSunRiseAndSet(@NonNull LocalDate date);

    /**
     * 查询日出日落时间缓存
     *
     * @param projectId 项目ID
     * @param date      日期
     * @return 日出日落时间
     * @since 5.240402.0
     */
    WeatherSunRiseAndSetDTO getSunRiseAndSetCache(@NonNull String projectId, @NonNull LocalDate date);

    /**
     * 气象数据通用查询接口
     *
     * @param dataType  数据类型枚举
     * @param startTime 开始时间(包含)
     * @param endTime   结束时间(不包含)
     * @return 气象数据
     * @since 7.260302.0
     */
    <T> List<T> queryUnifier(
            @NonNull WeatherQueryTypeEnum dataType,
            @NonNull LocalDateTime startTime,
            @NonNull LocalDateTime endTime);

    /**
     * 查询日出日落时间
     *
     * @param beginDate 开始日期, 闭区间
     * @param endDate   结束日期, 开区间
     * @return 日出日落时间
     * @since 7.260302.0
     */
    List<WeatherSunRiseAndSetDTO> listSunRiseAndSet(@NonNull LocalDate beginDate, @NonNull LocalDate endDate);

    /**
     * 查询日出日落时间缓存
     *
     * @param projectId 项目ID
     * @param beginDate 开始日期, 闭区间
     * @param endDate   结束日期, 开区间
     * @return 日出日落时间
     * @since 5.240402.0
     */
    List<WeatherSunRiseAndSetDTO> listSunRiseAndSetCache(
            @NonNull String projectId, @NonNull LocalDate beginDate, @NonNull LocalDate endDate);

    String queryCitiCode(@NonNull String projectId);

}
