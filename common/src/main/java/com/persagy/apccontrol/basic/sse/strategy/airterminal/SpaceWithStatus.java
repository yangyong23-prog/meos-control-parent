package com.persagy.apccontrol.basic.sse.strategy.airterminal;

import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalInstanceTypeEnum;
import com.persagy.apccontrol.basic.opt.OptSpaceWithStatusCacheReader;
import com.persagy.apccontrol.basic.model.param.airterminal.AirTerminalSpaceWithStatusQueryParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalSpaceWithStatusVO;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 3D模型数据
 *
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240402.00
 */
@Component(value = "SPACE_WITH_STATUS")
public class SpaceWithStatus extends AbstractSseEventStrategyTemplate {

    @Resource
    AirTerminalService airportService;

    @Lazy
    @Resource
    OptSpaceWithStatusCacheReader optSpaceWithStatusCacheReader;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.SPACE_WITH_STATUS;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":SPACE_WITH_STATUS";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        AirTerminalSpaceWithStatusQueryParam queryParam = new AirTerminalSpaceWithStatusQueryParam();
        queryParam.setInstanceId(param.getInstanceId());
        queryParam.setInstanceType(AirTerminalInstanceTypeEnum.analyze(param.getInstanceType().getValue()));
        queryParam.setProjectId(param.getProjectId());
        // opt B9：对象模型新缓存 READY 且 switch.opt.sseSpaceStatusCache 打开时不再每拍查 RWD
        AirTerminalSpaceWithStatusVO spaceWithStatus = optSpaceWithStatusCacheReader.applicable()
                ? optSpaceWithStatusCacheReader.read(queryParam)
                : airportService.listSpaceWithStatus(queryParam);

        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.SPACE_WITH_STATUS)
                .data(spaceWithStatus)
                .build();
    }
}
