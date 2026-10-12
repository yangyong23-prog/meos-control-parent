package com.persagy.apccontrol.controller;

import com.persagy.apccontrol.task.sse.SseEventScheduledService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 云端SSE控制器
 *
 * @author JiYinchuan
 * @since 7.250403.0
 */
@RestController
@RequestMapping("/server-event")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SseEmitterSaasController {

    private final SseEventScheduledService sseEventScheduledService;

    @PostMapping(path = "/publish")
    public void publishEvent() {
        sseEventScheduledService.scheduled();
    }
}
