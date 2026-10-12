package com.persagy.apccontrol.basic.outside.feign.model.param.weather;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.common.WeatherCommonQueryParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 天气服务日出日落时间查询参数
 *
 * @author muanfu
 * @since 5.241102.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherHourHistoryDataQueryParam extends WeatherCommonQueryParam {
    //{
    //    "cityName": "上海",
    //    "minResponse": true,
    //    "startTime": "2024-11-11 00:00:00",
    //    "endTime": "2024-11-12 00:00:00"
    //}

    /**
     * 是否返回部分数据
     */
    private boolean minResponse;
    /**
     * 开始时间(包含)
     */
    private LocalDateTime startTime;
    /**
     * 结束时间(不包含)
     */
    private LocalDateTime endTime;

}
