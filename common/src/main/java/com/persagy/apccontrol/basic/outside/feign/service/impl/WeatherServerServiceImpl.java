package com.persagy.apccontrol.basic.outside.feign.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.QueryIOTEnum;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.QueryWeatherDTO;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.WeatherResourceEnum;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.outside.dto.DtpPointInfo;
import com.persagy.apccontrol.basic.outside.dto.WeatherContent;
import com.persagy.apccontrol.basic.outside.feign.client.impl.WeatherServerCacheClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.WeatherForecastQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.WeatherSunRiseAndSetQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherServiceDailyForecastResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherSunRiseAndSetResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherClientService;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherServerService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.structure.wather.converter.WeatherConverter;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import feign.Response;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * 天气服务
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/12/15 10:25
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WeatherServerServiceImpl implements WeatherServerService {

    private final BizDtpRwdService bizDtpRwdService;
    private final WeatherServerCacheClient weatherServerClient;
    private final WeatherClientService weatherClientService;
    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractCommonServer abstractCommonServer;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final MidPermissionClientService midPermissionClientService;
    private final WeatherConverter weatherConverter;

    @Override
    public WeatherContent queryConfigWeather(String groupCode, String projectId) {
        try {
            String shellConfig = this.abstractCommonServer.getShellConfig(groupCode, projectId, null, ShellConfigItemEnum.WEATHER_WEATHER_CHANGE.getCode(), null);
            if (shellConfig == null || WeatherResourceEnum.WEATHER_SERVICES.getName().equals(shellConfig)) {
                return this.queryRealWeather(projectId);
            }
            // 气象站场景：weatherDataSource为设备ID
            // 获取新的天气数据源配置
            String equipId = this.abstractCommonServer.getShellConfig(groupCode, projectId, null, ShellConfigItemEnum.ALG_WEATHER_DATA_SOURCE.getCode(), null);
            return this.getWeatherContent(groupCode, projectId, equipId);
        } catch (Exception e) {
            logProjectInstance().error("查询天气数据出错", e, Pair.of("projectId", projectId));
        }
        return new WeatherContent();
    }

    /**
     * 获取气象监测站数据
     *
     * @param groupCode
     * @param projectId
     * @return
     */
    private WeatherContent getWeatherContent(String groupCode, String projectId, String equipId) {
        WeatherContent weatherContent = new WeatherContent();
        weatherContent.setConfig(WeatherResourceEnum.WEATHER_STATION.getCode());
        String objectId = null;
        if(StringUtils.isEmpty(equipId)){
            // 取气象监测站的天气数据
            List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(QueryIOTEnum.H.getDeviceCoding()), null, null, null, null);
            if (CollectionUtil.isEmpty(objectList)) {
                return weatherContent;
            }
            objectId = objectList.get(0).getString("id");
        }else {
            objectId = equipId;
        }

        List<String> infoCodes = QueryIOTEnum.getInfoCodes();

        List<Object> responses = this.abstractIotDataCacheServer.queryIotRealDataList(groupCode, projectId, objectId, QueryIOTEnum.H.getDeviceCoding(), infoCodes);
        //List<EnergyDataResponse> responses = this.dtpRwdClientService.queryIpDataOnMoment(groupCode, projectId, Lists.newArrayList(objectId), infoCodes);
        if (CollectionUtil.isNotEmpty(responses)) {
            for (Object respons : responses) {
                if (respons instanceof DtpPointInfo) {
                    DtpPointInfo dtpPointInfo = (DtpPointInfo) respons;
                    if (QueryIOTEnum.AMBIENT_TEMP.getInfoCode().equals(dtpPointInfo.getInfoCode())) {
                        weatherContent.setTemperature(dtpPointInfo.getIpValue() == null ? null : dtpPointInfo.getIpValue());
                    } else if (QueryIOTEnum.AMBIENT_RH.getInfoCode().equals(dtpPointInfo.getInfoCode())) {
                        weatherContent.setHumidity(dtpPointInfo.getIpValue() == null ? null : dtpPointInfo.getIpValue());
                    } else if (QueryIOTEnum.T_W_B.getInfoCode().equals(dtpPointInfo.getInfoCode())) {
                        weatherContent.setWet_bulb_temp(dtpPointInfo.getIpValue() == null ? null : dtpPointInfo.getIpValue());
                    } else if (QueryIOTEnum.H.getInfoCode().equals(dtpPointInfo.getInfoCode())) {
                        weatherContent.setH(dtpPointInfo.getIpValue() == null ? null : dtpPointInfo.getIpValue());
                    }
                } else if (respons instanceof FeedbackInfoMsgMqttVO) {
                    FeedbackInfoMsgMqttVO infoMsgMqttVO = (FeedbackInfoMsgMqttVO) respons;
                    if (QueryIOTEnum.AMBIENT_TEMP.getInfoCode().equals(infoMsgMqttVO.getInfoCode())) {
                        weatherContent.setTemperature(infoMsgMqttVO.getDataValue() == null ? null : infoMsgMqttVO.getDataValue());
                    } else if (QueryIOTEnum.AMBIENT_RH.getInfoCode().equals(infoMsgMqttVO.getInfoCode())) {
                        weatherContent.setHumidity(infoMsgMqttVO.getDataValue() == null ? null : infoMsgMqttVO.getDataValue());
                    } else if (QueryIOTEnum.T_W_B.getInfoCode().equals(infoMsgMqttVO.getInfoCode())) {
                        weatherContent.setWet_bulb_temp(infoMsgMqttVO.getDataValue() == null ? null : infoMsgMqttVO.getDataValue());
                    } else if (QueryIOTEnum.H.getInfoCode().equals(infoMsgMqttVO.getInfoCode())) {
                        weatherContent.setH(infoMsgMqttVO.getDataValue() == null ? null : infoMsgMqttVO.getDataValue());
                    }
                }
            }
        }
        return weatherContent;
    }

    @Override
    @Deprecated
    public WeatherContent queryRealWeather(String projectId) {
        WeatherContent weatherContent = new WeatherContent();

        try {
            ProjectDTO projectDTO = midPermissionClientService.getProject(false, true, projectId);
            String cityCode = projectDTO == null ? null : projectDTO.getCityCode();

            QueryWeatherDTO weatherDTO = new QueryWeatherDTO(cityCode, "weather");
            weatherContent = this.weatherServerClient.nowWeatherQuery(weatherDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        weatherContent.setConfig(WeatherResourceEnum.WEATHER_SERVICES.getCode());
        return weatherContent;
    }


    @Override
    public List<WeatherSunRiseAndSetResponse> sunRiseAndSet(String projectId, String startDay, String endDay) {
        try {
            String citiCode = weatherClientService.queryCitiCode(projectId);
            WeatherSunRiseAndSetQueryParam queryParam = new WeatherSunRiseAndSetQueryParam();
            queryParam.setCityCode(citiCode);
            queryParam.setProjectId(projectId);
            queryParam.setStartDay(DateUtils.str2LocalDate(startDay, DateUtils.SDF_DAY));
            queryParam.setEndDay(DateUtils.str2LocalDate(endDay, DateUtils.SDF_DAY));
            return this.weatherServerClient.listSunRiseAndSet(queryParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public WeatherServiceDailyForecastResponse queryWeatherService(LocalDate date) {
        String citiCode = weatherClientService.queryCitiCode(ContextUtil.getProjectId());
        WeatherForecastQueryParam queryParam = new WeatherForecastQueryParam();
        queryParam.setCityCode(citiCode);
        queryParam.setDate(date.atStartOfDay());
        List<WeatherServiceDailyForecastResponse> r = this.weatherServerClient.queryWeatherService(queryParam);

        return r.stream()
                .filter(response -> response.getDate().equals(date))
                .findFirst()
                .orElse(null);
    }

    @Override
    public byte[] downloadIcon(String code) throws Exception {
        Response response = this.weatherServerClient.downloadIcon(code);
        return IOUtils.toByteArray(response.body().asInputStream());
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "天气服务Service";
    }
}
