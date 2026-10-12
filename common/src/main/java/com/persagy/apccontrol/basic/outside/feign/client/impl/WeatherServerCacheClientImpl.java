package com.persagy.apccontrol.basic.outside.feign.client.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.model.dto.rwd.QueryWeatherDTO;
import com.persagy.apccontrol.basic.model.enumtype.weather.WeatherDataTypeEnum;
import com.persagy.apccontrol.basic.outside.dto.WeatherContent;
import com.persagy.apccontrol.basic.outside.feign.client.WeatherServerFeignClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.*;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherHour24ForecastResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherNowWeatherResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherServiceDailyForecastResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherSunRiseAndSetResponse;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.DateTypeEnum;
import com.persagy.validate.utility.executor.DateTimeRangeExecutor;
import feign.Response;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * WeatherClient
 *
 * @author yx
 * @date 2023/3/29
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WeatherServerCacheClientImpl implements WeatherServerCacheClient {

    private final WeatherServerFeignClient weatherServerFeignClient;

    private final RedisCacheTemplate redisCacheTemplate;

    /**
     * 日出日落缓存
     */
    private final String listSunRiseAndSetCache = "listSunRiseAndSet";

    /**
     * 实时天气缓存
     */
    private final String listNowWeather = "listNowWeather";
    /**
     * 查询二十四小时天气预测缓存
     */
    private final String listHour24Forecast = "listHour24Forecast";

    /**
     * 查询当前天气数据
     */
    private final String nowWeatherQuery = "nowWeatherQuery";


    /**
     * 实时天气
     *
     * @param param 查询参数
     * @return 日出日落时间
     * @since 5.240402.0
     */
    @Override
    public List<WeatherNowWeatherResponse> listNowWeather(
            WeatherNowWeatherQueryParam param) {
        List<WeatherNowWeatherResponse> responses = query(false, null,
                param.getProjectId(), "实时天气", param,
                () -> weatherServerFeignClient.listNowWeather(param), ArrayList::new);
        responses = cacheListNowWeather(param, responses);
        return responses;
    }

    /**
     * @return java.util.List<com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherNowWeatherResponse>
     * @Author 薛涛
     * @Description 实时天气缓存
     * @Date 16:07 2024/8/5
     * @Param [param, responses]
     **/
    public List<WeatherNowWeatherResponse> cacheListNowWeather(
            WeatherNowWeatherQueryParam param, List<WeatherNowWeatherResponse> responses) {

        WeatherDataTypeEnum dataType = param.getDataType();
        String cityCode = param.getCityCode();
        String date2Str = DateUtils.date2Str(DateUtils.now().toLocalDate().atStartOfDay(), DateUtils.SDF_DAY);

        // 如果这个值没有说明需要从缓存拿
        if (CollectionUtils.isEmpty(responses)) {
            String listNowWeatherCacheString = redisCacheTemplate.object().get(listNowWeather + dataType.getValue() + cityCode + date2Str);
            List<WeatherNowWeatherResponse> weatherNowWeatherResponses = JSONArray.parseArray(listNowWeatherCacheString, WeatherNowWeatherResponse.class);
            return weatherNowWeatherResponses;
        }
        // 缓存24小时
        redisCacheTemplate.object().set(listNowWeather + dataType.getValue() + cityCode + date2Str
                , JSONArray.toJSONString(responses), 24, TimeUnit.HOURS);
        return responses;
    }


    /**
     * 查询二十四小时天气预测
     *
     * @param param 查询参数
     * @return 日出日落时间
     * @since 5.240402.0
     */
    @Override
    public List<WeatherHour24ForecastResponse> listHour24Forecast(
            WeatherHour24ForecastQueryParam param) {
        List<WeatherHour24ForecastResponse> responses = query(false, null,
                param.getProjectId(), "实时天气", param,
                () -> weatherServerFeignClient.listHour24Forecast(param), ArrayList::new);
        responses = cacheListHour24Forecast(param, responses);
        return responses;

    }

    /**
     * @return java.util.List<com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherHour24ForecastResponse>
     * @Author 薛涛
     * @Description 查询二十四小时天气预测缓存
     * @Date 17:37 2024/8/5
     * @Param [param, responses]
     **/
    public List<WeatherHour24ForecastResponse> cacheListHour24Forecast(
            WeatherHour24ForecastQueryParam param, List<WeatherHour24ForecastResponse> responses) {

        String cityCode = param.getCityCode();
        String date2Str = DateUtils.date2Str(DateUtils.now().toLocalDate().atStartOfDay(), DateUtils.SDF_DAY);

        // 如果这个值没有说明需要从缓存拿
        if (CollectionUtils.isEmpty(responses)) {
            String listNowWeatherCacheString = redisCacheTemplate.object().get(listHour24Forecast + cityCode + date2Str);
            List<WeatherHour24ForecastResponse> weatherNowWeatherResponses = JSONArray.parseArray(listNowWeatherCacheString, WeatherHour24ForecastResponse.class);
            return weatherNowWeatherResponses;
        }
        // 缓存24小时
        redisCacheTemplate.object().set(listHour24Forecast + cityCode + date2Str
                , JSONArray.toJSONString(responses), 24, TimeUnit.HOURS);
        return responses;
    }

    /**
     * 查询当前天气数据
     *
     * @param queryWeatherDTO
     * @return
     */
    @Override
    public WeatherContent nowWeatherQuery(QueryWeatherDTO queryWeatherDTO) {
        WeatherNowWeatherQueryParam param = new WeatherNowWeatherQueryParam();
        param.setCityCode(queryWeatherDTO.getCityCode());
        param.setDataType(WeatherDataTypeEnum.analyze(queryWeatherDTO.getDataType()));
        List<WeatherNowWeatherResponse> weatherNowWeatherResponses = listNowWeather(param);

        // WeatherResult<WeatherContent> weatherResult = this.weatherServerFeignClient.nowWeatherQuery(queryWeatherDTO);
        if (CollectionUtil.isNotEmpty(weatherNowWeatherResponses)) {

            WeatherNowWeatherResponse weatherNowWeatherResponse = weatherNowWeatherResponses.get(0);
            WeatherContent weatherContent = new WeatherContent();
            weatherContent.setHumidity(weatherNowWeatherResponse.getHumidity());
            weatherContent.setTemperature(weatherNowWeatherResponse.getTemperature());
            weatherContent.setWet_bulb_temp(weatherNowWeatherResponse.getWetBulbTemp());
            weatherContent.setH(weatherNowWeatherResponse.getEnthalpyValue());

            return cacheNowWeatherQuery(queryWeatherDTO, weatherContent);
        } else {
            return cacheNowWeatherQuery(queryWeatherDTO, null);
        }
    }

    /**
     * @return com.persagy.apccontrol.basic.outside.dto.WeatherContent
     * @Author 薛涛
     * @Description 查询当前天气数据缓存
     * @Date 17:59 2024/8/5
     * @Param [queryWeatherDTO, responses]
     **/
    public WeatherContent cacheNowWeatherQuery(
            QueryWeatherDTO queryWeatherDTO, WeatherContent responses) {
        String cityCode = queryWeatherDTO.getCityCode();
        String date2Str = DateUtils.date2Str(DateUtils.now().toLocalDate().atStartOfDay(), DateUtils.SDF_DAY);
        // 如果这个值没有说明需要从缓存拿
        if (Objects.isNull(responses)) {
            String listNowWeatherCacheString = redisCacheTemplate.object().get(nowWeatherQuery + cityCode + date2Str);
            WeatherContent weatherContent = JSONObject.parseObject(listNowWeatherCacheString, WeatherContent.class);
            return weatherContent;
        }
        // 缓存24小时
        redisCacheTemplate.object().set(nowWeatherQuery + cityCode + date2Str
                , JSONObject.toJSONString(responses), 24, TimeUnit.HOURS);
        return responses;
    }


    /**
     * 查询日出日落时间
     *
     * @param param 查询参数
     * @return 日出日落时间
     * @since 5.240402.0
     */
    @Override
    public List<WeatherSunRiseAndSetResponse> listSunRiseAndSet(
            WeatherSunRiseAndSetQueryParam param) {
        if (!param.isQueryCache()) {
            return query(false, null, param.getProjectId(), "日出日落", param,
                    () -> weatherServerFeignClient.listSunRiseAndSet(param), Collections::emptyList);
        }

        LocalDate startDay = param.getStartDay();
        LocalDate endDay = param.getEndDay();

        // 左闭右开原则
        Collection<LocalDate> localDates = DateTimeRangeExecutor.generateDate(DateTypeEnum.DAY, startDay, endDay, Function.identity()).values();
        List<WeatherSunRiseAndSetResponse> list = new ArrayList<>();
        for (LocalDate localDate : localDates) {
            WeatherSunRiseAndSetResponse weatherSunRiseAndSetResponse = listSunRiseAndSetCache(localDate, param);
            if (weatherSunRiseAndSetResponse != null) {
                list.add(weatherSunRiseAndSetResponse);
            }
        }
        // 1.从缓存拿数据
        // 1.2如果没有去查接口（七天）
        // 1.3 如果有那么先查缓存里面是否有旧数据。如果有全部删除重新拉取最新的七天（剪掉一天看看有木有数据就知道是不是旧数据）
        return list;
    }

    @Override
    public List<WeatherNowWeatherResponse> listHourHistoryData(WeatherHourHistoryDataQueryParam param) {
        List<WeatherNowWeatherResponse> responses = query(false, null,
                param.getProjectId(), "实时天气", param,
                () -> weatherServerFeignClient.listHourHistoryData(param), ArrayList::new);
        return responses;
    }


    @Override
    public List<WeatherServiceDailyForecastResponse> queryWeatherService(WeatherForecastQueryParam param) {
        return query(false, null,
                param.getProjectId(), "查询预报天气", param,
                () -> weatherServerFeignClient.queryWeatherService(param), ArrayList::new);
    }

    /**
     * @return com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherSunRiseAndSetResponse
     * @Author 薛涛
     * @Description 获取日落时间缓存
     * @Date 19:43 2024/7/26
     * @Param [localDate]
     **/
    private WeatherSunRiseAndSetResponse listSunRiseAndSetCache(LocalDate localDate, WeatherSunRiseAndSetQueryParam param) {
        JSONObject sunRiseAndSetJsonObjectOld = redisCacheTemplate.object().get(listSunRiseAndSetCache + ContextUtil.getGroupCode() + param.getProjectId());
        if (sunRiseAndSetJsonObjectOld != null) {
            String date2Str = DateUtils.date2Str(localDate.atStartOfDay(), DateUtils.SDF_DAY);
            WeatherSunRiseAndSetResponse sunRiseAndSetJsonObjectOldObject = sunRiseAndSetJsonObjectOld.getObject(date2Str, WeatherSunRiseAndSetResponse.class);
            if (Objects.nonNull(sunRiseAndSetJsonObjectOldObject)) {
                LocalDate localDateOldOne = DateUtils.now().toLocalDate().plusDays(-1);
                date2Str = DateUtils.date2Str(localDateOldOne.atStartOfDay(), DateUtils.SDF_DAY);
                WeatherSunRiseAndSetResponse sunRiseAndSetJsonObjectOldOneObject = sunRiseAndSetJsonObjectOld.getObject(date2Str, WeatherSunRiseAndSetResponse.class);
                // 如果缓存里面有前一天数据说明该缓存了
                if (Objects.nonNull(sunRiseAndSetJsonObjectOldOneObject)) {
                    sunRiseAndSetCache(localDate, param);
                }
                return sunRiseAndSetJsonObjectOldObject;
            }
        }
        return sunRiseAndSetCache(localDate, param);
    }

    /**
     * @return void
     * @Author 薛涛
     * @Description 做缓存(存完缓存顺便返回需要得数据)
     * @Date 20:15 2024/7/26
     * @Param [param]
     **/
    private WeatherSunRiseAndSetResponse sunRiseAndSetCache(LocalDate localDate, WeatherSunRiseAndSetQueryParam param) {
        param.setStartDay(DateUtils.now().toLocalDate());
        param.setEndDay(DateUtils.now().toLocalDate().plusDays(30));
        try {
            CustomerResponse<List<WeatherSunRiseAndSetResponse>> listCustomerResponse = weatherServerFeignClient.listSunRiseAndSet(param);
            List<WeatherSunRiseAndSetResponse> content = listCustomerResponse.getContent();
            JSONObject sunRiseAndSetJsonObject = new JSONObject();
            for (WeatherSunRiseAndSetResponse weatherSunRiseAndSetResponse : content) {
                LocalDateTime sunRise = weatherSunRiseAndSetResponse.getSunRise();
                String date2Str = DateUtils.date2Str(sunRise, DateUtils.SDF_DAY);
                sunRiseAndSetJsonObject.put(date2Str, weatherSunRiseAndSetResponse);
            }
            redisCacheTemplate.object().set(listSunRiseAndSetCache + ContextUtil.getGroupCode() + param.getProjectId(), sunRiseAndSetJsonObject);
            String date2Str = DateUtils.date2Str(localDate.atStartOfDay(), DateUtils.SDF_DAY);
            return sunRiseAndSetJsonObject.getObject(date2Str, WeatherSunRiseAndSetResponse.class);
        } catch (Exception ex) {
            return new WeatherSunRiseAndSetResponse();
        }

    }


    @Override
    public Response downloadIcon(String code) {
        return weatherServerFeignClient.downloadIcon(code);
    }

    @Override
    public String logTag() {
        return "天气服务Cache";
    }
}
