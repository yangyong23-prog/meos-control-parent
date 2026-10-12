package com.persagy.apccontrol.basic.sse.strategy;

import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sse执行策略上下文
 *
 * @author yuecaipu
 * @date 2024/5/4 16:13
 * @since v5.240402.00
 */
@Component
public class SseHandlerContext {

    private final Map<String, SseEventStrategy> HANDLER_MAP = new ConcurrentHashMap<>();

    @Autowired
    public void strategyInterface(Map<String, AbstractSseEventStrategyTemplate> map) {

        this.HANDLER_MAP.putAll(map);

    }

    /**
     * 根据sse事件类型获取对应的执行策略
     * 说明：策略实现类value要和SseEmitterEventEnum枚举定义保持一致
     * @param event
     * @return {@link SseEventStrategy}
     * @author yuecaipu
     * @date 2024/5/4 23:42
     * @since v5.240402.00
     */
    public SseEventStrategy getStrategy(SseEmitterEventEnum event) {
        return HANDLER_MAP.get(event.getValue());
    }


    /**
     * 获取所有已注册sse执行策略
     *
     * @return {@link SseEventStrategy}
     * @author yuecaipu
     * @date 2024/6/24 15:08
     * @since v5.240602.00
     */
    public Collection<SseEventStrategy> getStrategy() {
        return HANDLER_MAP.values();
    }

}
