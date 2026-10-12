package com.persagy.apccontrol.basic.outside.feign.model.response.weather;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 天气服务日出日落时间响应
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class WeatherSunRiseAndSetResponse extends BaseResponse {

    /**
     * 日出时间
     */
    private LocalDateTime sunRise;

    /**
     * 日落时间
     */
    private LocalDateTime sunSet;

}
