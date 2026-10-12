package com.persagy.apccontrol.common.sse.strategy.business;

import cn.hutool.core.collection.CollUtil;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalSpaceTagDTO;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalInstanceTypeEnum;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;


/**
 * iot 空间标签
 * 目前只支持末端, 后面业务定义空间配置后 该方法移除
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240602.00
 */
@Slf4j
@Component(value = "OBJECT_TAG")
public class ObjectTag extends AbstractSseEventStrategyTemplate {

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.OBJECT_TAG;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":OBJECT_TAG";
    }

    @Resource
    AirTerminalService airTerminalService;


    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        if (param.getSubscribeType().equals(SubscribeTypeEnum.AIR_TERMINAL)) {
            // 空间id
            if (param.getClaasCode2objectIds() == null) {
                param.setClaasCode2objectIds(airTerminalService.mapClaasCode2objectIds(param.getProjectId(), param.getInstanceId(), AirTerminalInstanceTypeEnum.analyze(param.getInstanceType().getValue())));
            }
            Set<String> spaceIds = param.getClaasCode2objectIds().get(CommonConst.DtpRwdConst.GeneralZone);
            if (CollUtil.isEmpty(spaceIds)) {
                log.warn("OBJECT_TAG 空间id集合为空,忽略推送");
                return null;
            }
            // 获取空间标签集合
            Map<String, AirTerminalSpaceTagDTO> map = airTerminalService.mapSpaceIdAndTag(param.getProjectId(), spaceIds);
            if (CollUtil.isNotEmpty(map)) {
                return SseEmitterObject.builder()
                        .projectId(param.getProjectId())
                        .eventType(SseEmitterEventEnum.OBJECT_TAG)
                        .data(map)
                        .build();
            }
        }
        return null;
    }
}
