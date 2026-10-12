package com.persagy.apccontrol.controller;

import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.common.service.IWSMessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webSocket")
@Slf4j
@Tag(name = "MQTT模拟发数接口")
public class WebSocketPushMsgController {

    @Autowired
    private IWSMessageService iwsMessageService;

    @PostMapping("/pushMessage")
    public void pushMessage(@RequestBody FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO) {
        iwsMessageService.pushMessageWeb(feedbackInfoMsgMqttVO);
    }
}
