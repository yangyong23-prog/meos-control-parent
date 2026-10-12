package com.persagy.apccontrol.basic.outside.feign.model.param.weather;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.common.WeatherCommonQueryParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 天气服务-24小时预报数据查询
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherHour24ForecastQueryParam extends WeatherCommonQueryParam {

}
