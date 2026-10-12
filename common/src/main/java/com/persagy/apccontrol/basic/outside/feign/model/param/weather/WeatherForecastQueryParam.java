package com.persagy.apccontrol.basic.outside.feign.model.param.weather;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.common.WeatherCommonQueryParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 天气服务-2预报数据查询
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherForecastQueryParam extends WeatherCommonQueryParam {
    
    
    /**
     * 查询日期
     */
    private LocalDateTime date;
    /**
     * 查询数量 15 天内的
     */
    private int limit = 15;
    
}
