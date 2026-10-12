package com.persagy.apccontrol.basic.outside.feign.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.persagy.apccontrol.basic.model.dto.rwd.QueryWeatherDTO;
import com.persagy.apccontrol.basic.outside.dto.WeatherContent;
import com.persagy.apccontrol.basic.outside.dto.WeatherResult;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.*;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherHour24ForecastResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherNowWeatherResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherServiceDailyForecastResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherSunRiseAndSetResponse;
import feign.Response;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * WeatherClient
 *
 * @author yx
 * @date 2023/3/29
 */
@FeignClient(name = "dtp-weather-server", url = "${url.cloud-dtp-weather-server:}")
@RefreshScope
public interface WeatherServerFeignClient {

    /**
     * 气象数据通用查询接口
     *
     * @param dataType 数据类型枚举
     * @param param    查询参数
     * @return 气象数据
     * @since 7.260302.0
     */
    @PostMapping("/api/entrance/unifierJson/{dataType}")
    CustomerResponse<List<ObjectNode>> queryUnifier(
            @PathVariable String dataType,
            @RequestBody WeatherUnifierQueryParam param);

    /**
     * 实时天气
     *
     * @param param 查询参数
     * @return 日出日落时间
     * @since 5.240402.0
     */
    @PostMapping("/api/entrance/unifierJson/NowWeatherQuery")
    CustomerResponse<List<WeatherNowWeatherResponse>> listNowWeather(
            @RequestBody WeatherNowWeatherQueryParam param);

    /**
     * 查询二十四小时天气预测
     *
     * @param param 查询参数
     * @return 日出日落时间
     * @since 5.240402.0
     */
    @PostMapping("/api/entrance/unifierJson/Hour24Forecast")
    CustomerResponse<List<WeatherHour24ForecastResponse>> listHour24Forecast(
            @RequestBody WeatherHour24ForecastQueryParam param);

    /**
     * 查询日出日落时间
     *
     * @param param 查询参数
     * @return 日出日落时间
     * @since 5.240402.0
     */
    @PostMapping("/api/entrance/unifierJson/SunRiseAndSet")
    CustomerResponse<List<WeatherSunRiseAndSetResponse>> listSunRiseAndSet(
            @RequestBody WeatherSunRiseAndSetQueryParam param);

    /**
     * 查询当前天气数据
     *
     * @param queryWeatherDTO
     * @return
     */
    @Deprecated
    @PostMapping("/api/entrance/unifierJson/NowWeatherQuery")
    WeatherResult<WeatherContent> nowWeatherQuery(@RequestBody QueryWeatherDTO queryWeatherDTO);


    @GetMapping("/Spring/MVC/api/entrance/download/icon/{code}")
    Response downloadIcon(@PathVariable String code);

    /**
     * 查询天气历史数据
     *
     * @param param
     * @return
     */
    @PostMapping("/api/entrance/unifierJson/HourHistoryData")
    CustomerResponse<List<WeatherNowWeatherResponse>> listHourHistoryData(@RequestBody WeatherHourHistoryDataQueryParam param);

    /**
     * 查询日预报数据
     *
     * @param param
     * @return
     */
    @PostMapping("/api/entrance/unifierJson/QueryWeatherService")
    CustomerResponse<List<WeatherServiceDailyForecastResponse>> queryWeatherService(
            @RequestBody WeatherForecastQueryParam param);

}
