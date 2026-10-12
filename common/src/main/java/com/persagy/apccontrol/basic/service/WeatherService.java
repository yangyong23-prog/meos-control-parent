package com.persagy.apccontrol.basic.service;


import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.param.weather.QueryWeatherParam;
import com.persagy.apccontrol.basic.model.vo.airconditioning.WeatherDataVO;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public interface WeatherService extends LogComponent {

    /**
     * 查询室外天气通用接口,可以选择是否查询壳配置，是否查询未来数据，不传日期默认查询当前时间前24小时历史数据
     *
     * @param param 参数
     * @return {@code WeatherDataVO }
     * @since 7.250501.0
     */
    WeatherDataVO queryOutdoorWeather(@NotNull QueryWeatherParam param);

    /**
     * 根据壳配置查询历史天气
     *
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param ipType
     * @param weatherDataVO
     * @return {@code List<WeatherDTO> }
     * @since 7.250501.0
     */
    List<WeatherDTO> queryConfigHistoryWeather(@NotNull LocalDateTime startTime, @NotNull LocalDateTime endTime, DtpRwdPersistenceTimeTypeEnum ipType, WeatherDataVO weatherDataVO);

    /**
     * 查询气象服务数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return {@code List<WeatherDTO> }
     * @since 7.250501.0
     */
    List<WeatherDTO> queryWeatherServiceData(@NotNull LocalDateTime startTime, @NotNull LocalDateTime endTime);

    /**
     * 获取气象站数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param ipType
     * @return {@code List<WeatherDTO> }
     * @since 7.250501.0
     */
    List<WeatherDTO> getWeatherStationData(@NotNull LocalDateTime startTime, @NotNull LocalDateTime endTime, DtpRwdPersistenceTimeTypeEnum ipType, String equipId);

    /**
     * 查询未来天气
     *
     * @return {@code List<WeatherDTO> }
     * @since 7.250501.0
     */
    List<WeatherDTO> queryFutureWeather();

}
