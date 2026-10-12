package com.persagy.apccontrol.basic.outside.feign.client.impl;

import com.persagy.apccontrol.basic.model.dto.rwd.QueryWeatherDTO;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.dto.WeatherContent;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.*;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherHour24ForecastResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherNowWeatherResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherServiceDailyForecastResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherSunRiseAndSetResponse;
import feign.Response;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * WeatherClient
 *
 * @author 薛涛
 * @date 2023/3/29
 */

public interface WeatherServerCacheClient extends BaseFeignClientService {

    /**
     * 实时天气
     *
     * @param param 查询参数
     * @return 日出日落时间
     * @since 5.240402.0
     */
    List<WeatherNowWeatherResponse> listNowWeather(WeatherNowWeatherQueryParam param);

    /**
     * 查询二十四小时天气预测
     *
     * @param param 查询参数
     * @return 日出日落时间
     * @since 5.240402.0
     */

    List<WeatherHour24ForecastResponse> listHour24Forecast(WeatherHour24ForecastQueryParam param);

    /**
     * 查询日出日落时间
     *
     * @param param 查询参数
     * @return 日出日落时间
     * @since 5.240402.0
     */
    List<WeatherSunRiseAndSetResponse> listSunRiseAndSet(WeatherSunRiseAndSetQueryParam param);

    List<WeatherNowWeatherResponse> listHourHistoryData(WeatherHourHistoryDataQueryParam param);
    
    /**
     * 查询日预报数据
     *
     * @param param
     * @return
     */
    public List<WeatherServiceDailyForecastResponse> queryWeatherService(WeatherForecastQueryParam param);
    /**
     * 查询当前天气数据
     *
     * @param queryWeatherDTO
     * @return
     */
    @Deprecated
    WeatherContent nowWeatherQuery(QueryWeatherDTO queryWeatherDTO);


    Response downloadIcon(@PathVariable String code);

}
