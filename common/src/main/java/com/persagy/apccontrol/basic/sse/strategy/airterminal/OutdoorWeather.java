package com.persagy.apccontrol.basic.sse.strategy.airterminal;

import com.persagy.apccontrol.basic.model.param.airterminal.AirTerminalOutdoorWeatherQueryParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalOutdoorWeatherVO;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 室外天气
 *
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240402.00
 */
@Component(value = "OUTDOOR_WEATHER")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OutdoorWeather extends AbstractSseEventStrategyTemplate {

    @Resource
    AirTerminalService airTerminalService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.OUTDOOR_WEATHER;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":OUTDOOR_WEATHER";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        AirTerminalOutdoorWeatherQueryParam queryParam = new AirTerminalOutdoorWeatherQueryParam();
        queryParam.setProjectId(param.getProjectId());
        AirTerminalOutdoorWeatherVO outdoorWeather = airTerminalService.getOutdoorWeather(queryParam);

        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.OUTDOOR_WEATHER)
                .data(outdoorWeather)
                .build();
    }

}
