package com.persagy.apccontrol.basic.outside.feign.service.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherHour24ForecastDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherHourHistoryDataDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherNowWeatherDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherSunRiseAndSetDTO;
import com.persagy.apccontrol.basic.model.enumtype.weather.WeatherDataTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.weather.WeatherQueryTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.client.WeatherServerFeignClient;
import com.persagy.apccontrol.basic.outside.feign.client.impl.WeatherServerCacheClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.*;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherHour24ForecastResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherNowWeatherResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherSunRiseAndSetResponse;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherClientService;
import com.persagy.apccontrol.basic.structure.wather.converter.WeatherConverter;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 天气客户端服务实现
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WeatherClientServiceImpl implements WeatherClientService {

    private final WeatherConverter weatherConverter;
    private final MidPermissionClientService midPermissionClientService;
    private final WeatherServerFeignClient weatherServerFeignClient;
    private final WeatherServerCacheClient weatherServerCacheClient;

    @Override
    public WeatherNowWeatherDTO getNowWeather(@NonNull String projectId, @NonNull WeatherDataTypeEnum dataType) {
        WeatherNowWeatherQueryParam queryParam = new WeatherNowWeatherQueryParam();
        queryParam.setCityCode(queryCitiCode(projectId));
        queryParam.setDataType(dataType);
        List<WeatherNowWeatherResponse> responses = weatherServerCacheClient.listNowWeather(queryParam);
        if (CollectionUtils.isEmpty(responses)) {
            return null;
        }
        return weatherConverter.toNowWeatherDto(IterableUtils.first(responses));
    }

    @Override
    public WeatherHour24ForecastDTO getHour24Forecast(@NonNull String projectId) {
        WeatherHour24ForecastQueryParam queryParam = new WeatherHour24ForecastQueryParam();
        queryParam.setCityCode(queryCitiCode(projectId));
        List<WeatherHour24ForecastResponse> responses = weatherServerCacheClient.listHour24Forecast(queryParam);
        if (CollectionUtils.isEmpty(responses)) {
            return null;
        }
        return weatherConverter.toHour24ForecastDto(IterableUtils.first(responses));
    }

    @Override
    public List<WeatherHourHistoryDataDTO> listHourHistoryData(@NotNull String projectId, @NotNull LocalDateTime startTime, @NotNull LocalDateTime endTime) {
        WeatherHourHistoryDataQueryParam queryParam = new WeatherHourHistoryDataQueryParam();
        queryParam.setCityCode(queryCitiCode(projectId));
        queryParam.setMinResponse(true);
        queryParam.setStartTime(startTime);
        queryParam.setEndTime(endTime);
        List<WeatherNowWeatherResponse> responses = weatherServerCacheClient.listHourHistoryData(queryParam);
        return weatherConverter.toHourHistoryDataDto(responses);
    }

    @Override
    public WeatherSunRiseAndSetDTO getSunRiseAndSet(@NonNull LocalDate date) {
        return getOne(false, listSunRiseAndSet(date, date.plusDays(1)));
    }

    @Override
    public WeatherSunRiseAndSetDTO getSunRiseAndSetCache(@NonNull String projectId, @NonNull LocalDate date) {
        return getOne(false, listSunRiseAndSetCache(projectId, date, date.plusDays(1)));
    }

    @Override
    public <T> List<T> queryUnifier(
            @NonNull WeatherQueryTypeEnum dataType,
            @NonNull LocalDateTime startTime,
            @NonNull LocalDateTime endTime) {
        String projectId = this.getProjectId();
        WeatherUnifierQueryParam queryParam = new WeatherUnifierQueryParam();
        queryParam.setCityCode(this.queryCitiCode(projectId));
        queryParam.setProjectId(projectId);
        queryParam.setStartTime(startTime);
        queryParam.setEndTime(endTime);
        return this.query(false, dataType.getReasonPhrase(), queryParam,
                        () -> this.weatherServerFeignClient.queryUnifier(dataType.getValue(), queryParam),
                        Collections::emptyList).stream()
                .<T>map(dataType::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WeatherSunRiseAndSetDTO> listSunRiseAndSet(@NonNull LocalDate beginDate, @NonNull LocalDate endDate) {
        return this.querySunRiseAndSet(false, ContextUtil.getProjectId(), beginDate, endDate);
    }

    @Override
    public List<WeatherSunRiseAndSetDTO> listSunRiseAndSetCache(
            @NonNull String projectId, @NonNull LocalDate beginDate, @NonNull LocalDate endDate) {
        return this.querySunRiseAndSet(true, projectId, beginDate, endDate);
    }

    /**
     * 查询城市编码
     *
     * @param projectId 项目ID
     * @return 城市编码
     * @since 5.240502.0
     */
    @Override
    public String queryCitiCode(@NonNull String projectId) {
        return midPermissionClientService.getProject(false, true, projectId).getCityCode();
    }

    /**
     * 查询日出日落时间
     *
     * @param queryCache 是否查询缓存
     * @param projectId  项目ID
     * @param beginDate  开始日期
     * @param endDate    结束日期
     * @return 日出日落时间列表
     * @since 7.260302.0
     */
    private List<WeatherSunRiseAndSetDTO> querySunRiseAndSet(
            boolean queryCache,
            @NonNull String projectId,
            @NonNull LocalDate beginDate,
            @NonNull LocalDate endDate) {
        WeatherSunRiseAndSetQueryParam queryParam = new WeatherSunRiseAndSetQueryParam();
        queryParam.setQueryCache(queryCache);
        queryParam.setCityCode(queryCitiCode(projectId));
        queryParam.setProjectId(projectId);
        queryParam.setStartDay(beginDate);
        queryParam.setEndDay(endDate);
        List<WeatherSunRiseAndSetResponse> responses = weatherServerCacheClient.listSunRiseAndSet(queryParam);
        if (CollectionUtils.isEmpty(responses)) {
            return Lists.newArrayList();
        }
        List<WeatherSunRiseAndSetDTO> result = new ArrayList<>();
        for (WeatherSunRiseAndSetResponse response : responses) {
            LocalDateTime sunRise = response.getSunRise();
            LocalDateTime sunSet = response.getSunSet();

            if (ObjectUtils.anyNull(sunRise, sunSet)) {
                LogExecutor.project(projectId).warn(log(), logTag(), "日出日落时间为空", Arrays.asList(
                        Pair.of("sunRise", sunRise),
                        Pair.of("sunSet", sunSet)
                ));
                continue;
            }
            WeatherSunRiseAndSetDTO resultItem = new WeatherSunRiseAndSetDTO();
            resultItem.setDate(sunRise.toLocalDate());
            resultItem.setSunRise(sunRise);
            resultItem.setSunSet(sunSet);
            result.add(resultItem);
        }
        return result;
    }

    @Override
    public String logTag() {
        return "天气服务";
    }
}
