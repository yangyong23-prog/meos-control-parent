package com.persagy.apccontrol.basic.controller.outside;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.weather.QueryWeatherParam;
import com.persagy.apccontrol.basic.model.param.outside.SunRiseAndSetParam;
import com.persagy.apccontrol.basic.model.vo.ProjectVO;
import com.persagy.apccontrol.basic.model.vo.WeatherVO;
import com.persagy.apccontrol.basic.model.vo.airconditioning.WeatherDataVO;
import com.persagy.apccontrol.basic.outside.dto.WeatherContent;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherServiceDailyForecastResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.weather.WeatherSunRiseAndSetResponse;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherServerService;
import com.persagy.apccontrol.basic.service.WeatherService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * WeatherController
 *
 * @author yx
 * @date 2023/3/27
 */
@Tag(name = "室外天气数据")
@Validated
@RestController
@RequestMapping("/weather")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WeatherController {

    private final WeatherServerService weatherServerService;
    private final WeatherService weatherService;

    @Operation(summary = "查询天气数据", tags = {VersionConstant.V5_6_0})
    @PostMapping("/queryWeather")
    public R<WeatherVO> queryWeather(@RequestBody @Valid ProjectVO projectVO) {
        WeatherVO resultVO = new WeatherVO();
        WeatherContent weatherContent = this.weatherServerService.queryConfigWeather(ContextUtil.getGroupCode(), projectVO.getProjectId());
        resultVO.setConfig(weatherContent.getConfig());
        resultVO.setDryBulb(StrUtil.isBlank(weatherContent.getTemperature()) ? -0.9999 : Double.valueOf(weatherContent.getTemperature()));
        resultVO.setWetBulb(StrUtil.isBlank(weatherContent.getWet_bulb_temp()) ? -0.9999 : Double.valueOf(weatherContent.getWet_bulb_temp()));
        resultVO.setRelativeHumidity(StrUtil.isBlank(weatherContent.getHumidity()) ? -0.9999 : Double.valueOf(weatherContent.getHumidity()));
        resultVO.setAlkane(StrUtil.isBlank(weatherContent.getH()) ? -0.9999 : Double.valueOf(weatherContent.getH()));
        return R.success(resultVO);
    }

    @Operation(summary = "查询日出日落时间", tags = {VersionConstant.V5_8_0})
    @PostMapping("/sunRiseAndSet")
    public R<List<WeatherSunRiseAndSetResponse>> sunRiseAndSet(@RequestBody SunRiseAndSetParam sunRiseAndSetParam) {
        List<WeatherSunRiseAndSetResponse> sunRiseAndSetList = this.weatherServerService.sunRiseAndSet(
                sunRiseAndSetParam.getProjectId(),
                sunRiseAndSetParam.getStartTime(),
                sunRiseAndSetParam.getEndTime());
        return R.success(CollectionUtil.isEmpty(sunRiseAndSetList) ? new ArrayList<>() : sunRiseAndSetList);
    }
    
    
    @Operation(summary = "查询日预报数据", tags = {VersionConstant.V7_250402_0})
    @GetMapping("/queryWeatherService/{date}")
    public R<WeatherServiceDailyForecastResponse> queryWeatherService(@PathVariable LocalDate date) {
        return R.success(this.weatherServerService.queryWeatherService(date));
    }

    /**
     * 查询室外天气
     *
     * @return {@code R<List<WeatherVO>> }
     * @since 7.250501.0
     */
    @Operation(summary = "查询-室外天气", tags = VersionConstant.V7_250501_0)
    @PostMapping("/queryOutdoorWeather")
    public R<WeatherDataVO> queryOutdoorWeather(@RequestBody QueryWeatherParam param) {
        return R.success(weatherService.queryOutdoorWeather(param));
    }
}
