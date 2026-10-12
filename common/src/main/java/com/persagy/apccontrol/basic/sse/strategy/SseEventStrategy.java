package com.persagy.apccontrol.basic.sse.strategy;

import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 描述
 *
 * @author yuecaipu
 * @date 2024/5/4 15:34
 * @since v5.240402.00
 */
public interface SseEventStrategy {

    void doOperation(String eventKey, SseSubscribeParam param, List<SseEmitter> sseEmitters);

    void doOperation(String projectId, SubscribeTypeEnum subscribeType);

    void doOperation(String projectId, SubscribeTypeEnum subscribeType, Object condition);

    void doOperation(String projectId, Object condition);

}
