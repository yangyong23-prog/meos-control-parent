package com.persagy.apccontrol.basic.sse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/26 10:44
 */
public class CustomSseEmitter extends SseEmitter {

    public CustomSseEmitter(Long timeout) {
        super(timeout);
    }

    public CustomSseEmitter(Long timeout, SseSubscribeParam sseSubscribeParam) {
        super(timeout);
        this.sseSubscribeParam = sseSubscribeParam;
    }

    private SseSubscribeParam sseSubscribeParam;

    public SseSubscribeParam getSseSubscribeParam() {
        return sseSubscribeParam;
    }

    public void setSseSubscribeParam(SseSubscribeParam sseSubscribeParam) {
        this.sseSubscribeParam = sseSubscribeParam;
    }

    @Override
    protected void extendResponse(ServerHttpResponse outputMessage) {
        super.extendResponse(outputMessage);
        HttpHeaders headers = outputMessage.getHeaders();
        headers.setContentType(new MediaType("text", "event-stream", StandardCharsets.UTF_8));
    }

}
