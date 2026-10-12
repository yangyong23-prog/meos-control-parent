package com.persagy.apccontrol.basic.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.QueryIOTEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherHour24ForecastDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherHourHistoryDataDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherHourlyDTO;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.WeatherResourceEnum;
import com.persagy.apccontrol.basic.model.param.weather.QueryWeatherParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.airconditioning.WeatherDataVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.WeatherHour24ForecastQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherClientService;
import com.persagy.apccontrol.basic.service.WeatherService;
import com.persagy.apccontrol.basic.structure.wather.converter.WeatherConverter;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.util.BigDecimalUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WeatherServiceImpl implements WeatherService {

    private final WeatherClientService weatherClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final AbstractCommonServer abstractCommonServer;
    private final WeatherConverter weatherConverter;
    private final DtpRwdClientService dtpRwdClientService;

    /**
     * 查询室外天气
     *
     * @param param 参数
     * @return {@code WeatherDataVO }
     * @since 7.250501.0
     */
    @Override
    public WeatherDataVO queryOutdoorWeather(@NotNull QueryWeatherParam param) {
        WeatherDataVO weatherDataVO = new WeatherDataVO();
        LocalDateTime startTime = param.getStartTime();
        LocalDateTime endTime = param.getEndTime();
        Boolean queryFuture = param.getQueryFuture();
        DtpRwdPersistenceTimeTypeEnum ipType = param.getIpType();
        if (Objects.isNull(ipType)) {
            ipType = DtpRwdPersistenceTimeTypeEnum.HOUR;
        }
        Boolean queryShellConfig = param.getQueryShellConfig();
        if (Objects.isNull(startTime) || Objects.isNull(endTime)) {
            LocalDateTime now = DateUtils.now();
            startTime = DateUtils.localDateTimeWithHour(now.plusDays(-1));
            endTime = DateUtils.localDateTimeWithHour(now);
        }
        List<WeatherDTO> weatherList;
        weatherDataVO.setShellConfig(WeatherResourceEnum.WEATHER_SERVICES.getInternationalName());
        if (queryShellConfig) {
            weatherList = queryConfigHistoryWeather(startTime, endTime, ipType, weatherDataVO);
        } else {
            weatherList = queryWeatherServiceData(startTime, endTime);
        }
        weatherDataVO.setHistoryData(weatherList);
        weatherDataVO.setIpType(WeatherResourceEnum.WEATHER_STATION.getName().equals(weatherDataVO.getShellConfig())
                ? ipType.getValue() : DtpRwdPersistenceTimeTypeEnum.HOUR.getValue());
        if (queryFuture) {
            List<WeatherDTO> futureWeatherList = queryFutureWeather();
            weatherDataVO.setFutureData(futureWeatherList);
        }
        return weatherDataVO;
    }


    /**
     * 根据配置查询历史天气
     *
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param ipType        分精度类型
     * @param weatherDataVO
     * @return {@code Collection<WeatherDTO> }
     * @since 7.250501.0
     */
    @Override
    public List<WeatherDTO> queryConfigHistoryWeather(@NotNull LocalDateTime startTime, @NotNull LocalDateTime endTime, DtpRwdPersistenceTimeTypeEnum ipType, WeatherDataVO weatherDataVO) {
        try {
            //查询来源类型
            String shellConfig = this.abstractCommonServer.getShellConfig(ContextUtil.getGroupCode(), ContextUtil.getProjectId(),
                    null, ShellConfigItemEnum.WEATHER_WEATHER_CHANGE.getCode(), null);
            weatherDataVO.setShellConfig(shellConfig);
            if (shellConfig == null || WeatherResourceEnum.WEATHER_SERVICES.getName().equals(shellConfig)) {
                return queryWeatherServiceData(startTime, endTime);
            }
            //查询气象检测站
            //查询气象监测站实例id
            String equipId = this.abstractCommonServer.getShellConfig(
                    ContextUtil.getGroupCode(), ContextUtil.getProjectId(), null, ShellConfigItemEnum.ALG_WEATHER_DATA_SOURCE.getCode(), null);
            return this.getWeatherStationData(startTime, endTime, ipType,equipId);
        } catch (Exception e) {
            logProjectInstance().error("查询天气历史数据出错", e, Arrays.asList(Pair.of("projectId", ContextUtil.getProjectId()),
                    Pair.of("startTime", startTime), Pair.of("endTime", endTime)));
        }
        return Collections.emptyList();
    }

    /**
     * 查询天气服务数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return {@code List<WeatherDTO> }
     * @since 7.250501.0
     */
    @Override
    public List<WeatherDTO> queryWeatherServiceData(@NotNull LocalDateTime startTime, @NotNull LocalDateTime endTime) {
        //查询天气服务
        List<WeatherHourHistoryDataDTO> hourHistoryDataList = weatherClientService.listHourHistoryData(ContextUtil.getProjectId(), startTime, endTime);
        if (CollectionUtils.isEmpty(hourHistoryDataList)) {
            return Collections.emptyList();
        }
        return weatherConverter.convertWeatherDTO(hourHistoryDataList);
    }


    /**
     * 获取气象站数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param ipType
     * @return {@code List<WeatherDTO> }
     * @since 7.250501.0
     */
    @Override
    public List<WeatherDTO> getWeatherStationData(@NotNull LocalDateTime startTime, @NotNull LocalDateTime endTime, DtpRwdPersistenceTimeTypeEnum ipType, String equipId) {
        List<WeatherDTO> weatherHourHistoryDataDTOS = Lists.newArrayList();
        String objectId = null;
        if(StringUtils.isEmpty(equipId)){
            // 查询气象监测站实例
            DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
            param.setClassCode(DtpRwdClassCodeEnum.WESEMS);
            DtpRwdObjectDTO object = dtpRwdClientService.getObject(param);
            if (Objects.isNull(object)) {
                return weatherHourHistoryDataDTOS;
            }
            objectId = object.getId();
        }else {
            objectId = equipId;
        }
        List<InfoCodeValueVO> dataList = abstractIotDataCacheServer.queryObjectIdPersistentDataList(ipType,
                Collections.singletonList(objectId),
                Arrays.asList(InfoCodeEnum.AMBIENT_TEMP.getValue(), InfoCodeEnum.AMBIENT_RH.getValue(), InfoCodeEnum.TWB.getValue(),
                        QueryIOTEnum.H.getInfoCode()),
                DateUtils.localDateTimeToString(startTime), DateUtils.localDateTimeToString(endTime), false);
        if (CollectionUtil.isEmpty(dataList)) {
            return weatherHourHistoryDataDTOS;
        }
        Map<LocalDateTime, List<InfoCodeValueVO>> timeInfoCodeValueMap = dataList.stream().filter(x -> Objects.nonNull(x.getUpdateTime()))
                .collect(Collectors.groupingBy(InfoCodeValueVO::getDataTime));
        Map<LocalDateTime, WeatherDTO> localDateTimeWeatherDTOMap = new HashMap<>(16);
        for (Map.Entry<LocalDateTime, List<InfoCodeValueVO>> entry : timeInfoCodeValueMap.entrySet()) {
            WeatherDTO weatherDTO = localDateTimeWeatherDTOMap.computeIfAbsent(entry.getKey(), k -> new WeatherDTO());
            weatherDTO.setTime(entry.getKey());
            for (InfoCodeValueVO infoCodeValueVO : entry.getValue()) {
                Object infoCodeValue = infoCodeValueVO.getInfoCodeValue();
                if (Objects.isNull(infoCodeValue)) {
                    continue;
                }
                BigDecimal value = BigDecimalUtils.decimal(infoCodeValue.toString());
                if (InfoCodeEnum.AMBIENT_TEMP.getValue().equals(infoCodeValueVO.getInfoCode())) {
                    weatherDTO.setTemperature(value);
                    continue;
                }
                if (InfoCodeEnum.AMBIENT_RH.getValue().equals(infoCodeValueVO.getInfoCode())) {
                    weatherDTO.setHumidity(value);
                    continue;
                }
                if (InfoCodeEnum.TWB.getValue().equals(infoCodeValueVO.getInfoCode())) {
                    weatherDTO.setWetBulbTemp(value);
                }
                if (QueryIOTEnum.H.getInfoCode().equals(infoCodeValueVO.getInfoCode())) {
                    weatherDTO.setEnthalpyValue(value);
                }
            }
        }
        return localDateTimeWeatherDTOMap.values().stream()
                .sorted(Comparator.nullsLast(Comparator.comparing(WeatherDTO::getTime)))
                .collect(Collectors.toList());
    }

    /**
     * 查询未来天气
     *
     * @return {@code List<WeatherDTO> }
     * @since 7.250501.0
     */
    @Override
    public List<WeatherDTO> queryFutureWeather() {
        String cityCode = weatherClientService.queryCitiCode(ContextUtil.getProjectId());
        if (cityCode == null) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_e973963a62634e0da91d13548500721b"), LocaleMessageUtil.getMessage("key_e973963a62634e0da91d13548500721b"));
        }
        WeatherHour24ForecastQueryParam param = new WeatherHour24ForecastQueryParam();
        param.setCityCode(cityCode);
        WeatherHour24ForecastDTO weatherHour24ForecastDTO = weatherClientService.getHour24Forecast(ContextUtil.getProjectId());
        if (Objects.isNull(weatherHour24ForecastDTO)) {
            return Collections.emptyList();
        }
        TreeSet<WeatherHourlyDTO> hourlies = weatherHour24ForecastDTO.getHourlies();
        if (CollectionUtils.isEmpty(hourlies)) {
            return Collections.emptyList();
        }
        return weatherConverter.hourlyToHourHistoryDataDto(hourlies);
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "天气服务实现";
    }
}
