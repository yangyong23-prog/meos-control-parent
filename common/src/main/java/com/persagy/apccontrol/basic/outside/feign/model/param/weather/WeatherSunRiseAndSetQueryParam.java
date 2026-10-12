package com.persagy.apccontrol.basic.outside.feign.model.param.weather;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.common.WeatherCommonQueryParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 天气服务日出日落时间查询参数
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherSunRiseAndSetQueryParam extends WeatherCommonQueryParam {

    /**
     * 开始日期, 闭区间
     */
    private LocalDate startDay;

    /**
     * 结束日期, 开区间
     */
    private LocalDate endDay;

    /**
     * 是否查询缓存
     */
    private boolean queryCache = true;

}
