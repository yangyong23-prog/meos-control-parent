package com.persagy.apccontrol.basic.model.vo.airconditioning;

import com.persagy.apccontrol.basic.model.dto.weather.WeatherDTO;
import lombok.Data;

import java.util.List;

@Data
public class WeatherDataVO {

    /**
     * 历史数据
     */
    private List<WeatherDTO> historyData;

    /**
     * 未来数据
     */
    private List<WeatherDTO> futureData;

    /**
     * 壳配置类型: 天气服务 或者 气象站
     */
    private String shellConfig;

    /**
     * 分精度类型
     */
    private String ipType;

}
