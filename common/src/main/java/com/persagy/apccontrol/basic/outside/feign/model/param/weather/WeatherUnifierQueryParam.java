package com.persagy.apccontrol.basic.outside.feign.model.param.weather;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.common.WeatherCommonQueryParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 天气服务通用查询参数
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherUnifierQueryParam extends WeatherCommonQueryParam {

    /**
     * 开始时间(包含)
     */
    private LocalDateTime startTime;

    /**
     * 结束时间(不包含)
     */
    private LocalDateTime endTime;

    /**
     * 查询天数限制(日预报数据使用)
     */
    private Integer limit;

    /**
     * 经度(日出日落数据可使用经纬度代替城市信息)
     */
    private BigDecimal longitude;

    /**
     * 纬度(日出日落数据可使用经纬度代替城市信息)
     */
    private BigDecimal latitude;
}
