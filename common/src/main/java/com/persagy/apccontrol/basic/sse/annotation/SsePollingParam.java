package com.persagy.apccontrol.basic.sse.annotation;

import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述
 *
 * @author yuecaipu
 * @date 2024/8/21 17:20
 * @since v5.240402.00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SsePollingParam {

    /**
     * sse事件ID
     */
    private String eventId;

    /**
     * sse事件类型
     */
    private SseEmitterEventEnum eventType;

    /**
     * sse事件订阅参数
     */
    private Object param;
}
