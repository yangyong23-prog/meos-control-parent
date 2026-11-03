package com.persagy.apccontrol.basic.controller;

import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.opt.OptSseKeepalive;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseEmitterServer;
import com.persagy.apccontrol.basic.sse.SseEmitters;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.framework.common.context.ContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/26 13:51
 */
@Slf4j
@RestController
@RequestMapping("/server-event")
@Tag(name = "SSE")
public class SseEmitterController {

    @Autowired
    private SseEmitterServer sseEmitterServer;

    @Autowired
    private SseEmitters sseEmitters;

    @Autowired
    private OptSseKeepalive optSseKeepalive;

    @GetMapping(path = "/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter addClient(@Validated @ModelAttribute SseSubscribeParam param) {
        param.setGroupCode(ContextUtil.getGroupCode());
        param.setRemoveDefaultIpType(LocalContextStorage.isRemoveDefaultIpType());
        return sseEmitterServer.add(param);
    }

    @PostMapping(path = "/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter multiSubscribe(@Validated @RequestBody List<SseSubscribeParam> params) {
        params.forEach(p -> p.setRemoveDefaultIpType(LocalContextStorage.isRemoveDefaultIpType()));
        return sseEmitterServer.addMulti(params);
    }

    /**
     * opt B9：客户端保活。eventKey 为连接建立时 EVENT_ID 事件里的值；switch.opt.sseKeepalive 关闭时为空操作。
     */
    @GetMapping(path = "/sse/ping")
    public void ping(@RequestParam("eventKey") String eventKey) {
        optSseKeepalive.ping(eventKey);
    }

    @PostMapping(path = "/sse/send")
    public void publishEvent(@RequestBody SseEmitterObject param) {
        this.sseEmitterServer.publishEvent(param);
    }

    @Operation(summary = "获取客户端集合")
    @GetMapping(path = "/sse/getMultiEmitters")
    public Map<String, CopyOnWriteArrayList<SseEmitter>> getMultiEmitters() {
        return sseEmitters.getMultiEmitters();
    }

    @Operation(summary = "获取eventId和客户端参数映射列表")
    @GetMapping(path = "/sse/getSseParamMap")
    public Map<String, SseSubscribeParam> getSseParamMap() {
        return sseEmitters.getSseParamMap();
    }



}
