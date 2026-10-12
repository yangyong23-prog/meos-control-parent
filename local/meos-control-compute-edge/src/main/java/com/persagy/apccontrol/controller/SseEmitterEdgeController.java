package com.persagy.apccontrol.controller;

import com.persagy.apccontrol.basic.model.dto.ReleaseInterventionDTO;
import com.persagy.apccontrol.basic.model.dto.SendStrategyMsgDTO;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseEmitterServer;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.annotation.SsePollingParam;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import com.persagy.apccontrol.common.server.SseEmitterEdgeServer;
import com.persagy.apccontrol.common.sse.strategy.business.EquipmentBusiness;
import com.persagy.apccontrol.common.sse.strategy.iot.IotDataChange;
import com.persagy.apccontrol.task.scheduled.sse.SseEventScheduledLocalService;
import com.persagy.framework.common.context.ContextUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * server 需要推送的事件，交由 compute去推送
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/6/27 17:08
 */
@Slf4j
@RestController
@RequestMapping("/server-event")
@Tag(name = "SSE")
public class SseEmitterEdgeController {

    @Autowired
    private SseEmitterEdgeServer sseEmitterEdgeServer;
    @Resource
    private SseEmitterServer sseEmitterServer;
    @Lazy
    @Resource
    private IotDataChange iotDataChange;
    @Autowired
    private SseEventScheduledLocalService sseEventScheduledLocalService;
    @Autowired
    private EquipmentBusiness equipmentBusiness;
    @Resource
    SseHandlerContext handlerContext;

    @PostMapping(path = "/equipmentBusiness")
    public SseEmitterObject equipmentBusiness(@RequestBody SseSubscribeParam param) {
        return equipmentBusiness.getSseEmitterObject(param);
    }

    @PostMapping(path = "/releaseIntervention")
    public void releaseIntervention(@RequestBody List<ReleaseInterventionDTO> interventionDTOS) {
        this.sseEmitterEdgeServer.releaseInterventions(interventionDTOS);
    }
    
    /**
     * 发送事件消息
     *
     * @param dto
     */
    @PostMapping(path = "/sendStrategyMsg")
    public void sendStrategyMsg(@RequestBody SendStrategyMsgDTO dto) {
        handlerContext.getStrategy(dto.getEvent()).doOperation(ContextUtil.getProjectId(), dto.getConditionDto());
    }

    @PostMapping(path = "/addPollingParam")
    public void addPollingParam(@RequestBody SsePollingParam param) {
        this.sseEmitterServer.addPollingParam(param);
    }

    @GetMapping(path = "/mapClaasCode2objectIds")
    public Map<String, Set<String>> mapClaasCode2objectIds(@Validated @ModelAttribute SseSubscribeParam param) {
        return iotDataChange.mapClaasCode2objectIds(param);
    }

    @PostMapping(path = "/publish")
    public void publishEvent() {
        sseEventScheduledLocalService.scheduled();
    }
}
