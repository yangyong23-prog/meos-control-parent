package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.outside.dto.WeatherContent;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherServiceDailyForecastResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherSunRiseAndSetResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * 天气服务
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/12/15 10:25
 */
public interface WeatherServerService extends LogComponent {

    /**
     * 获取配置的天气数据，气象监测站或天气服务
     *
     * @param groupCode
     * @param projectId
     * @return
     */
    @Deprecated
    WeatherContent queryConfigWeather(String groupCode, String projectId);

    /**
     * 获取实时天气
     *
     * @param projectId
     * @return
     */
    @Deprecated
    WeatherContent queryRealWeather(String projectId);


    /**
     * 获取日出日落具体时间
     *
     * @param projectId
     * @param startDay yyyy-MM-dd
     * @param endDay yyyy-MM-dd
     * @return
     */
    List<WeatherSunRiseAndSetResponse> sunRiseAndSet(String projectId, String startDay, String endDay);
    
    /**
     * 查询日预报数据
     *
     * @param date
     * @return
     */
    public WeatherServiceDailyForecastResponse queryWeatherService(LocalDate date);
    
    /**
     * 获取天气图标
     *
     * @param code
     * @return
     */
    byte[] downloadIcon(String code) throws Exception;

}
