package com.persagy.apccontrol.basic.outside.feign.model.response.weather;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 天气服务每日预报数据
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class WeatherServiceDailyForecastResponse extends BaseResponse {
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 白天天气现象文字
     */
    private String textDay;
    
    /**
     * 晚间天气现象文字
     */
    private String textNight;
    
    
    /**
     * 当天最高温度
     */
    private String high;
    
    
    /**
     * 当天最低温度
     */
    private String low;
    /**
     * 风向角度，范围0~360，0为正北，90为正东，180为正南，270为正西
     */
    private String windDirectionDegree;
    /**
     * 晚间天气现象代码
     */
    private String codeNight;
    /**
     * 云量，范围0~100，天空被云覆盖的百分比
     */
    private String humidity;
    /**
     * 风向文字
     */
    private String windDirection;
    /**
     * 风速
     */
    private String windSpeed;
    /**
     * 白天天气现象代码
     */
    private String codeDay;
    /**
     * 风力等级
     */
    private String windScale;
    
}
